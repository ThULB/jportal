var jp = jp || {};
var baseURL = jp.baseURL

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

var filterInput = document.getElementById("atozFilter")
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
    var additionalQuery = "%2BobjectType:jpjournal";
    var searchURL = baseURL + 'servlets/solr/select?wt=json&sort=maintitle_sort asc&rows=9999&q=' + additionalQuery;

    var titlesFlag = "";
    if (justTitles) {
        titlesFlag = "&fl=maintitle";
    }

    var qry = '';
    if (tabLetter == '#') {
        qry = ' -maintitle_sort:[a TO z] -maintitle_sort:z*';
    } else if (tabLetter != undefined && tabLetter != "") {
        qry = ' %2Bmaintitle_sort:' + tabLetter.toLowerCase() + '*';
    }

    var facetParams = "";
    facets.forEach(id => facetParams = facetParams + ' %2BjournalType:"' + id + '"%20');
    var titleFilter = titleFilter != "" ? "&fq=maintitle_sort:*" + titleFilter + "*" : "";

    return searchURL + qry + facetParams + titlesFlag + '&facet.field=journalType&facet=true' + titleFilter;
}

function update(model) {
    if (location.hash != "" && location.hash != null) {
        model.selectedTab = location.hash.substring(1, 2).toUpperCase();
    } else {
        model.selectedTab = "A";
    }

    var justTitlesSearchUrl = getSearchURL(model.usedFacets, model.titleFilter, true);
    var selectedTabSearchUrl = getSearchURL(model.usedFacets, model.titleFilter, false, model.selectedTab);

    var justTitlesStream = Rx.Observable.fromPromise($.getJSON(justTitlesSearchUrl))
        .flatMap(searchResults => searchResultsToModel(searchResults));

    var journalsStream = Rx.Observable.fromPromise($.getJSON(selectedTabSearchUrl))
        .map(searchResults => journalsToModel(searchResults));

    return Rx.Observable.merge(
        justTitlesStream,
        journalsStream
    ).reduce((model, changeFn) => changeFn(model), model);
}

var facetsContainer = document.getElementById("document_type");
var tabNavContainer = document.getElementById("tabNav");
var resultListContainer = document.getElementById("objectList");

var facetsCheckboxChangeEvents = new Rx.Subject()
    .map(event => model => {
        var categID = event.currentTarget.parentElement.dataset.categID;
        if (event.currentTarget.checked) {
            model.usedFacets.add(categID);
        } else {
            model.usedFacets.delete(categID);
        }

        return model;
    });

var tabNavClickEvents = new Rx.Subject()
    .map(event => model => {
        model.selectedTab = event.target.textContent;
        location.hash = model.selectedTab;

        return model;
    });

var facetCheckboxEventHandler = input => Rx.Observable.fromEvent(input, "change")
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
        var link = $('<link>').attr({
            type: 'text/css',
            rel: 'stylesheet',
            href: jp.baseURL + 'css/jp-journalList.css',
            'class': 'myStyle'
        });
        $('head').append(link);
    }
}

UIEvents
    .startWith(UIModel)
    .flatMap(update)
    .do(m => console.log(m))
    .subscribe(model => {
        renderFacetList(model, facetsContainer, facetCheckboxEventHandler);

        renderTabNav(model, tabNavContainer, tabNavEventHandler);

        filterInput.value = model.titleFilter;

        if (model.titleFilter != "") {
            clearFilterButtonIcon.style.visibility = "visible";
        } else {
            clearFilterButtonIcon.style.visibility = "";
        }

        renderResultList(model, resultListContainer);
    });
