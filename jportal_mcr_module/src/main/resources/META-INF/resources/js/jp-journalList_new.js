// URLStream -------------------->
//

baseURL = "http://localhost:8291/jportal/";

function createFacetObj(journalType, isUsed) {
    var categID = journalType[0];
    var count = journalType[1];
    var labelsUrl = baseURL + 'rsc/facets/label/' + categID;
    let lookupTableUrl = baseURL + 'rsc/facets/lookupTable';
    var lookupTableStream = Rx.Observable.fromPromise($.get(lookupTableUrl));

    if (isUsed === null || isUsed === undefined) {
        isUsed = false;
    }

    return Rx.Observable.fromPromise($.get(labelsUrl))
        .combineLatest(lookupTableStream, function (label, lookup) {
            var rootID = categID.split(':')[0];
            var parent = lookup[rootID];
            //console.log('Parent: ' + categID + ' # ' + parent);

            return {
                "label": label,
                "categID": categID,
                "count": count,
                "parent": parent,
                "isUsed": isUsed
            };
        })

}

function getActiveTabs(searchResult) {
    return Rx.Observable.from(searchResult.response.docs)
        .map(doc => doc.maintitle)
        .filter(title => title != null && title.length > 0)
        .map(title => title[0].toUpperCase())
        .map(char => char.charCodeAt(0))
        .filter(c => c >= 65 && c <= 90)
        .distinct()
        .defaultIfEmpty("#".charCodeAt(0))
        .reduce(function (set, c) {
            set.add(c);
            return set;
        }, new Set());
}

function createFacetObjStream(searchResult) {
    var journalType = searchResult.facet_counts.facet_fields.journalType;
    return Rx.Observable.from(journalType)
        .bufferWithCount(2)
        .filter(journalType => journalType[1] > 0)
        .flatMap(journalType => createFacetObj(journalType));
}

function createUsedFacetObjStream(searchResult) {
    return Rx.Observable.of(searchResult)
        .map(r => r.responseHeader.params.q)
        .flatMap(q => q.split(' '))
        .filter(q => q.startsWith('+journalType'))
        .map(q => q.replace('+journalType:', ''))
        .map(q => q.replace(/"/g, ''))
        .flatMap(journalType => createFacetObj(journalType, true));
}

function printTabNav() {
    Rx.Observable.range(65, 26)
        .map(String.fromCharCode)
        .startWith('#')
        .map(function (titleChar) {
            var li = document.createElement('li');
            li.textContent = titleChar;
            return li;
        })
        .reduce(function (fragment, li) {
            fragment.appendChild(li);
            return fragment;
        }, document.createDocumentFragment())
        .subscribe(liFragment => document.getElementById('tabNav').appendChild(liFragment));
}

function setActiveTabNav(activeTabs) {
    Rx.Observable.of(document.getElementById('tabNav'))
        .flatMap(tabNav => tabNav.getElementsByTagName('li'))
        .combineLatest(activeTabs, function (li, active) {
            if (active.has(li.textContent.charCodeAt(0))) {
                li.classList.add('active');
            } else {
                li.classList.remove('active')
            }

            return li;
        })
        .subscribe(li => console.log("Set active tab nav done!"))
}

function renderFacetListEntry(facetObj, rxSubject) {
    var li = document.createElement('li');
    li.dataset.categID = facetObj.categID;
    li.dataset.parent = facetObj.parent;
    var span = document.createElement('span');
    span.className = "facetButton";
    span.textContent = facetObj.label + " (" + facetObj.count + ")";
    var input = document.createElement('input');
    input.type = "checkbox";

    li.appendChild(input);
    li.appendChild(span);

    if (rxSubject != null && rxSubject != undefined) {
        rxSubject.onNext(input);
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

function createFacetButtonClickStream(facetObjStream, container) {
    return facetObjStream
        .map(jp.az.createFacetListEntry)
        // .reduce(function (f, li) {
        //     f.appendChild(li);
        //     return f;
        // }, document.createDocumentFragment())
        .reduce(function (list, li) {
            list.push(li);
            return list;
        }, [])
        .map(jp.az.treeify2)
        .flatMap(fragment => jp.az.renderFacetList(fragment, container))
        .flatMap(b => Rx.Observable.fromEvent(b, 'click'))
        .map(click => click.currentTarget.dataset.categID)
        .map(id => ' %2BjournalType:"' + id + '"%20');
}

function treeify(list) {
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

var searchURL = baseURL + 'servlets/solr/select?wt=json&sort=maintitle_sort asc&rows=9999&q=%2BobjectType:jpjournal';
var tabAndFacetURL = searchURL + '&fl=maintitle&facet.field=journalType&facet=true';
var initialTabFacetStream = Rx.Observable.fromPromise($.getJSON(tabAndFacetURL));
printTabNav();

initialTabFacetStream
    .subscribe(function (searchResult) {
        var activeTabs = getActiveTabs(searchResult);
        setActiveTabNav(activeTabs);

        var facetSubject = new Rx.Subject();

        createFacetObjStream(searchResult)
            .map(facetObj => renderFacetListEntry(facetObj, facetSubject))
            .reduce(function (list, li) {
                list.push(li);
                return list;
            }, [])
            .map(treeify)
            .subscribe(function (facetsListFragment) {
                var facetsContainer = document.querySelector('#atozFacets');
                var newList = document.createElement('ul');
                newList.id = 'atozFacetsItems';
                newList.appendChild(facetsListFragment);

                var currentList = facetsContainer.querySelector('#' + newList.id);
                if (currentList != null) {
                    currentList.replaceWith(newList);
                } else {
                    facetsContainer.appendChild(newList);
                }
            });


        //jp.az.getSearchURL() + facetFilter + facetOnOption + jp.az.getFilterQuery()
        // filterQuery filter != "" ? "&fq=maintitle_sort:*" + filter + "*" : "";
        // facetParam ' %2BjournalType:"' + id + '"%20'
        facetSubject
            .flatMap(b => Rx.Observable.fromEvent(b, 'click'))
            .map(click => click.currentTarget.parentElement.dataset.categID)
            .map(id => ' %2BjournalType:"' + id + '"%20')
            .subscribe(param => console.log(tabAndFacetURL + param));

        createUsedFacetObjStream(searchResult)
            .subscribe(obj => console.log("facet used: " + obj.categID));
    });

// urlStream -------------------------------->
// render html -> event goes back to urlStream
var testStr = "foo";

var testStream = Rx.Observable.of(testStr);
var clickStream = new Rx.Subject();
clickStream.flatMap(b => Rx.Observable.fromEvent(b, 'click'))
    .map(c => "goooo!");
    //.subscribe(click => console.log("click"));

var testFilterInput = document.getElementById('testFilter');
var testFilterStream = Rx.Observable.of(testFilterInput)
    .map(input => input.value);

var testFilterInputStream = Rx.Observable.fromEvent(testFilterInput, "input").delay(1500);

testFilterInputStream
    .map(event => event.target.value)
    .distinct()
    .subscribe(f => clickStream.onNext(f));

testFilterStream
    .combineLatest(testFilterInputStream, function(filter, input){

    })
    .subscribe(f => console.log("input: " + f));

// testStream
//     .combineLatest(clickStream, function(test, click){
//         return test;
//     })
clickStream
    .startWith('start click')
    .subscribe(function (s) {
        var container = document.getElementById('testcontainer');
        var button = container.querySelector('#button');
        var newbutton = document.createElement("button");
        newbutton.id = "button";
        newbutton.textContent = s;

        if (button != null) {
            button.replaceWith(newbutton);
        } else {
            container.appendChild(newbutton);
        }


        Rx.Observable.fromEvent(newbutton, 'click')
            .map(c => "boah")
            .subscribe(s => clickStream.onNext(s))
    });



