var baseURL = "http://localhost:8291/jportal/"
var searchURL = baseURL + 'servlets/solr/select?wt=json&sort=maintitle_sort asc&rows=9999&q=%2BobjectType:jpjournal';

var UIModel = {
    activeTabs: new Set(),
    facets: [],
    usedFacets: new Set(),
    tileFilter: "",
    selectedTab: ""
};

function getActiveTabs(searchResult) {
    return Rx.Observable.from(searchResult.response.docs)
        .map(doc => doc.maintitle)
        .filter(title => title != null && title.length > 0)
        .map(title => title[0].toUpperCase())
        .map(char => {
            return (char.charCodeAt(0) < 65 || char.charCodeAt(0) > 90) ? "#" : char
        })
        .distinct()
        .reduce((set, c) => {
            set.add(c);
            return set;
        }, new Set())
        .map(set => model => Object.assign({}, model, {activeTabs: set}));
}

function getFacetLabel(facetObj) {
    var labelsUrl = baseURL + 'rsc/facets/label/' + facetObj.categID;
    return Rx.Observable.fromPromise($.get(labelsUrl))
        .map(label => Object.assign({}, facetObj, {label: label}));
}

function getFacetParent(facetObj) {
    var lookupTableUrl = baseURL + 'rsc/facets/lookupTable';
    var rootID = facetObj.categID.split(':')[0];
    return Rx.Observable.fromPromise($.get(lookupTableUrl))
        .map(lookup => lookup[rootID])
        .map(parent => Object.assign({}, facetObj, {parent: parent}));
}

function getUsedFacetObjStream(searchResult) {
    return Rx.Observable.of(searchResult)
        .map(r => r.responseHeader.params.q)
        .flatMap(q => q.split(' '))
        .filter(q => q.startsWith('+journalType'))
        .map(q => q.replace('+journalType:', ''))
        .map(q => q.replace(/"/g, ''))
        .reduce((set, id) => {
            set.add(id);
            return set;
        }, new Set())
        .map(set => model => Object.assign({}, model, {usedFacets: set}));
}

function getFacetObjStream(searchResult) {
    return Rx.Observable.from(searchResult.facet_counts.facet_fields.journalType)
        .bufferCount(2)
        .filter(journalType => journalType[1] > 0)
        .map(journalType => ({
            categID: journalType[0],
            count: journalType[1]
        }))
        .flatMap(getFacetLabel)
        .flatMap(getFacetParent)
        .reduce((array, facet) => {
            array.push(facet);
            return array;
        }, [])
        .map(array => model => Object.assign({}, model, {facets: array}));
}

function renderFacetListEntry(facetObj, /*function*/ usedFacets, /*function*/ eventHandler) {
    var li = document.createElement('li');
    li.dataset.categID = facetObj.categID;
    li.dataset.parent = facetObj.parent;
    var span = document.createElement('span');
    span.className = "facetButton";
    span.textContent = facetObj.label + " (" + facetObj.count + ")";
    var input = document.createElement('input');
    input.type = "checkbox";

    if (usedFacets.has(facetObj.categID)) {
        input.checked = true;
    }

    li.appendChild(input);
    li.appendChild(span);

    if (eventHandler != null && eventHandler != undefined) {
        eventHandler(input);
    }
    return li;
}

function renderFacetList(fragment, container) {
    var newList = document.createElement('ul');
    newList.id = 'atozFacetsItems';
    newList.appendChild(fragment);

    var currentList = container.querySelector('#' + newList.id);
    if (currentList != null) {
        currentList.replaceWith(newList);
    } else {
        container.appendChild(newList);
    }

    return Rx.Observable.from(newList.children);
}

function renderTabNav(activeTabs,container) {
    Rx.Observable.range(65, 26)
        .map(i => String.fromCharCode(i))
        .startWith("#")
        .map(c => {
            var li = document.createElement("li");
            li.textContent = c;
            if(activeTabs.has(c)){
                li.classList.add("active");
            }
            return li;
        })
        .reduce((fragment, li) => {
            fragment.appendChild(li);
            return fragment;
        }, document.createDocumentFragment())
        .subscribe(fragment => {
            while(container.firstChild){
                container.removeChild(container.firstChild);
            }
            container.appendChild(fragment)
        });
}

function treeifyFacets(list) {
    var fragment = document.createDocumentFragment();
    var lookup = {};

    list.forEach(function (li) {
        lookup[li.dataset.categID] = li;
    });
    list.forEach(function (li) {
        if (li.dataset.parent != "null") {
            var parent = lookup[li.dataset.parent];
            if (parent != null) {
                var listOfChildNodes = parent.getElementsByTagName('ul')

                var childNodes;
                if (listOfChildNodes.length > 0) {
                    childNodes = listOfChildNodes[0];
                } else {
                    childNodes = document.createElement('ul');
                    parent.appendChild(childNodes);
                }
                var nodeWithParent = lookup[li.dataset.categID]
                childNodes.appendChild(nodeWithParent);
            }
        } else {
            var nodeWithNoParent = lookup[li.dataset.categID];
            fragment.appendChild(nodeWithNoParent);
        }
    });

    return fragment;
}

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

var filterInput = document.getElementById("atozFilter")
var titleFilter = Rx.Observable.fromEvent(filterInput, "input")
    .debounceTime(500)
    .map(event => model => Object.assign({}, model, {tileFilter: event.target.value}));

var clearFilterButton = document.getElementById("atozFilterRemoveButton");
var clearFilter = Rx.Observable.fromEvent(clearFilterButton, "click")
    .map(event => model => Object.assign({}, model, {tileFilter: ""}));

function searchResultsToModel(searchResult, model) {
    return Rx.Observable.merge(
        getActiveTabs(searchResult),
        getFacetObjStream(searchResult),
        getUsedFacetObjStream(searchResult)
    ).reduce((model, changeFn) => changeFn(model), model);
}

function update(model) {
    var facetParams = "";
    model.usedFacets.forEach(id => facetParams = facetParams + ' %2BjournalType:"' + id + '"%20');
    var tileFilter = model.tileFilter != "" ? "&fq=maintitle_sort:*" + model.tileFilter + "*" : "";

    var url = searchURL + facetParams + '&fl=maintitle&facet.field=journalType&facet=true' + tileFilter;

    if(location.hash != ""){
        model.selectedTab = location.hash.substring(1, 2).toUpperCase();
    }

    return Rx.Observable.fromPromise($.getJSON(url))
        .flatMap(searchResults => searchResultsToModel(searchResults, model))
}

var facetsContainer = document.getElementById("atozFacets");
var tabNavContainer = document.getElementById("tabNav");

var UIEvents = Rx.Observable.merge(
    facetsCheckboxChangeEvents,
    titleFilter,
    clearFilter
).scan((model, changeFn) => changeFn(model), UIModel);

var facetCheckboxEventHandler = input => Rx.Observable.fromEvent(input, "change")
    .subscribe(event => facetsCheckboxChangeEvents.next(event));

var clearFilterButtonIcon = document.getElementById("atozFilterRemoveIcon")
UIEvents
    .startWith(UIModel)
    .flatMap(update)
    .subscribe(model => {
        Rx.Observable.from(model.facets)
            .map(f => renderFacetListEntry(f, model.usedFacets, facetCheckboxEventHandler))
            .reduce((array, f) => {
                array.push(f);
                return array;
            }, [])
            .map(treeifyFacets)
            .subscribe(f => renderFacetList(f, facetsContainer));

        renderTabNav(model.activeTabs, tabNavContainer);

        filterInput.value = model.tileFilter;

        if(model.tileFilter != ""){
            clearFilterButtonIcon.style.visibility = "visible";
        } else {
            clearFilterButtonIcon.style.visibility = "";
        }

        console.log("Hash: " + model.selectedTab);
    });
