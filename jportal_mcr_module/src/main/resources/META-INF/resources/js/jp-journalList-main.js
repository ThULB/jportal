var jp = jp || {};
var baseURL = jp.baseURL;

var UIModel = {
    activeTabs: new Set(),
    facets: [],
    usedFacets: new Set(),
    titleFilter: "",
    selectedTab: "",
    journals: []
};


function clear(container) {
    while (container.firstChild) {
        container.removeChild(container.firstChild);
    }
}

var filterInput = document.getElementById("atozFilter");
var titleFilter = Rx.Observable.fromEvent(filterInput, "input")
    .debounceTime(500)
    .map(event => model => Object.assign({}, model, {titleFilter: event.target.value}));

var clearFilterButton = document.getElementById("atozFilterRemoveButton");
var clearFilter = Rx.Observable.fromEvent(clearFilterButton, "click")
    .map(event => model => Object.assign({}, model, {titleFilter: ""}));

function searchResultsToModel(searchResult) {
    return Rx.Observable.merge(
        getActiveTabs(searchResult),
        getFacetObjStream(searchResult),
        getUsedFacetObjStream(searchResult)
    );
}

function getSearchURL(/*List*/ facets, titleFilter, /*boolean*/ justTitles, tabLetter) {
    // var additionalQuery = $('#firstLetterTab').attr('additionalQuery');
    let additionalQuery = "%2BobjectType:jpjournal";
    let searchURL = baseURL + 'servlets/solr/select?wt=json&sort=maintitle_sort asc&rows=9999&q=' + additionalQuery;

    let titlesFlag = "";
    if (justTitles) {
        titlesFlag = "&fl=maintitle";
    }

    let qry = '';
    if (tabLetter === '#') {
        qry = ' -maintitle_sort:[a TO z] -maintitle_sort:z*';
    } else if (tabLetter != undefined && tabLetter != "") {
        qry = ' %2Bmaintitle_sort:' + tabLetter.toLowerCase() + '*';
    }

    let facetParams = "";
    facets.forEach(id => facetParams = facetParams + ' %2BjournalType:"' + id + '"%20');
    titleFilter = titleFilter != "" ? "&fq=maintitle_sort:*" + titleFilter + "*" : "";

    return searchURL + qry + facetParams + titlesFlag + '&facet.field=journalType&facet=true' + titleFilter;
}

function getTabsChar(model) {
  var journalTitlesSearchUrl = getSearchURL(model.usedFacets, model.titleFilter, true);

  var journalTitlesStream = Rx.Observable.fromPromise($.getJSON(journalTitlesSearchUrl))
      .flatMap(searchResults => searchResultsToModel(searchResults));

  return journalTitlesStream.reduce((m, changeFn) => changeFn(m), model);
}

function getResultList(model) {
  let selectedTabSearchUrl = getSearchURL(model.usedFacets, model.titleFilter, false, model.selectedTab);

  let journalsStream = Rx.Observable.fromPromise($.getJSON(selectedTabSearchUrl))
      .map(searchResults => journalsToModel(searchResults));

  return journalsStream.reduce((m, changeFn) => changeFn(m), model);
}

var facetsContainer = document.getElementById("document_type");
var tabNavContainer = document.getElementById("tabNav");
var resultListContainer = document.getElementById("objectList");

var facetsCheckboxChangeEvents = new Rx.Subject()
    .map(event => model => {
        let row = event.currentTarget.closest(".jp-journalList-facet-row");
        if(row == null) {
            console.warn("Unable to find parent .jp-journalList-facet-row");
            return model;
        }
        let checkbox = row.querySelector("input");
        if(event.target !== checkbox) {
            checkbox.checked = !checkbox.checked;
        }
        let categID = row.dataset.id;
        if(categID == null) {
            console.warn("Unable to find data-id of .jp-journalList-facet-row");
            return model;
        }
        if (checkbox.checked) {
            model.usedFacets.add(categID);
        } else {
            model.usedFacets.delete(categID);
        }
        return model;
    });

var tabNavClickEvents = new Rx.Subject()
    .map(event => model => {
        model.selectedTab = event.target.textContent;
        location.hash = fixHashTab(model.selectedTab);

        return model;
    });

var facetCheckboxEventHandler = input => Rx.Observable.fromEvent(input, "click")
    .subscribe(event => facetsCheckboxChangeEvents.next(event));


var tabNavEventHandler = tab => Rx.Observable.fromEvent(tab, "click")
    .subscribe(event => tabNavClickEvents.next(event));

var clearFilterButtonIcon = document.getElementById("atozFilterRemoveIcon");

var UIEvents = Rx.Observable.merge(
    facetsCheckboxChangeEvents,
    tabNavClickEvents,
    titleFilter,
    clearFilter
).scan((model, changeFn) => changeFn(model), UIModel);

function importCSS() {
    if (document.createStyleSheet) {
        document.createStyleSheet(baseURL + 'css/jp-journalList.css');
    } else {
        let link = $('<link>').attr({
            type: 'text/css',
            rel: 'stylesheet',
            href: jp.baseURL + 'css/jp-journalList.css',
            'class': 'myStyle'
        });
        $('head').append(link);
    }
}

function checkSelectedTab(model){
  if(!model.activeTabs.has(model.selectedTab)){
    Rx.Observable.from(model.activeTabs)
        .min()
        .subscribe(min => model.selectedTab = min)
  }

  location.hash = model.selectedTab;

  return model;
}

function getLocationHash(model) {
  if (location.hash != null && location.hash !== "") {
    model.selectedTab = fixHashTab(location.hash.substring(1, 2).toUpperCase());
  } else {
    model.selectedTab = "A";
  }
  return model;
}

function fixHashTab(c){
  if(c === '_'){
    return '#';
  }

  if(c === '#'){
    return '_';
  }

  return c
}

UIEvents
    .startWith(UIModel)
    .map(getLocationHash)
    .flatMap(getTabsChar)
    .map(checkSelectedTab)
    .flatMap(getResultList)
    .subscribe(model => {
        renderFacetList(model, facetsContainer, facetCheckboxEventHandler);

        renderTabNav(model, tabNavContainer, tabNavEventHandler);

        filterInput.value = model.titleFilter;

        if (model.titleFilter !== "") {
            clearFilterButtonIcon.style.visibility = "visible";
        } else {
            clearFilterButtonIcon.style.visibility = "";
        }

        renderResultList(model, resultListContainer);
    });
