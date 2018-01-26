var jp = jp || {};
var baseURL = jp.baseURL;

let UIModel = {
    activeTabs: new Set(),
    facets: [],
    usedFacets: new Set(),
    titleFilter: "",
    selectedTab: "",
    journals: [],
    numFound: 0
};

function clear(container) {
    while (container.firstChild) {
        container.removeChild(container.firstChild);
    }
}

let filterInput = document.getElementById("atozFilter");
let titleFilter = Rx.Observable.fromEvent(filterInput, "input")
    .debounceTime(500)
    .map(event => model => Object.assign({}, model, {titleFilter: event.target.value}));

let clearFilterButton = document.getElementById("atozFilterRemoveButton");
let clearFilter = Rx.Observable.fromEvent(clearFilterButton, "click")
    .map(event => model => Object.assign({}, model, {titleFilter: ""}));

function searchResultsToModel(searchResult) {
    return Rx.Observable.merge(
        getActiveTabs(searchResult),
        getFacetObjStream(searchResult),
        getUsedFacetObjStream(searchResult),
        Rx.Observable.of(searchResult)
            .map(r => r.response.numFound)
            .map(numFound => model => Object.assign({}, model, {numFound: numFound}))
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
    } else if (tabLetter != null && tabLetter !== "") {
        qry = ' %2Bmaintitle_sort:' + tabLetter.toLowerCase() + '*';
    }

    let facetParams = "";
    facets.forEach(id => facetParams = facetParams + ' %2BjournalType:"' + id + '"%20');
    titleFilter = titleFilter !== "" ? "&fq=maintitle_sort:*" + titleFilter + "*" : "";

    return searchURL + qry + facetParams + titlesFlag + '&facet.field=journalType&facet=true' + titleFilter;
}

function getTabsChar(model) {
  let journalTitlesSearchUrl = getSearchURL(model.usedFacets, model.titleFilter, true);

  let journalTitlesStream = Rx.Observable.fromPromise($.getJSON(journalTitlesSearchUrl))
      .flatMap(searchResults => searchResultsToModel(searchResults));

  return journalTitlesStream.reduce((m, changeFn) => changeFn(m), model);
}

function getResultList(model) {
  let selectedTabSearchUrl = getSearchURL(model.usedFacets, model.titleFilter, false, model.selectedTab);

  let journalsStream = Rx.Observable.fromPromise($.getJSON(selectedTabSearchUrl))
      .map(searchResults => journalsToModel(searchResults));

  return journalsStream.reduce((m, changeFn) => changeFn(m), model);
}

let hitCountContainer = document.getElementById("document_hits");
let facetsContainer = document.getElementById("document_type");
let tabNavContainer = document.getElementById("tabNav");
let resultListContainer = document.getElementById("objectList");

let facetsCheckboxChangeEvents = new Rx.Subject()
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
        updateLocationHash(model);
        return model;
    });

let tabNavClickEvents = new Rx.Subject()
    .map(event => model => {
        model.selectedTab = event.target.textContent;
        updateLocationHash(model);
        return model;
    });

let facetCheckboxEventHandler = input => Rx.Observable.fromEvent(input, "click")
    .subscribe(event => facetsCheckboxChangeEvents.next(event));


let tabNavEventHandler = tab => Rx.Observable.fromEvent(tab, "click")
    .subscribe(event => tabNavClickEvents.next(event));

let clearFilterButtonIcon = document.getElementById("atozFilterRemoveIcon");

let UIEvents = Rx.Observable.merge(
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
  updateLocationHash(model);
  return model;
}

function getLocationHash(model) {
    let tab = "A";
    if (location.hash != null && location.hash !== "") {
        tab = fixHashTab(location.hash.substring(1, 2).toUpperCase());
        let facetsAsString = location.hash.substring(3);
        facetsAsString.split("&").forEach(facet => {
            if(facet !== "") {
                model.usedFacets.add(facet);
            }
        });
    }
    model.selectedTab = tab;
    return model;
}

function updateLocationHash(model) {
    let tab = fixHashTab(model.selectedTab);
    let facets = Array.from(model.usedFacets).join("&");
    location.hash = tab + (facets !== "" ? ("/" + facets) : "");
}

function fixHashTab(c) {
    if (c === '_') {
        return '#';
    } else if (c === '#') {
        return '_';
    }
    return c;
}

function renderHitCount(model, container) {
    let text = model.numFound === 1 ? "Periodikum" : "Periodika";
    container.innerHTML = model.numFound + " " + text;
}

UIEvents
    .startWith(UIModel)
    .map(getLocationHash)
    .flatMap(getTabsChar)
    .map(checkSelectedTab)
    .flatMap(getResultList)
    .subscribe(model => {
        renderHitCount(model, hitCountContainer);

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
