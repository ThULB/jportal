/**
 * Created by chi on 09.10.17.
 */
function renderFacetListEntry(facetObj, /*function*/ usedFacets, /*function*/ eventHandler) {
    var li = document.createElement('li');
    li.dataset.categID = facetObj.categID;
    li.dataset.parent = facetObj.parent;
    // li.classList.add("list-group-item")
    var label = document.createElement('span');
    label.className = "facetButton";
    label.textContent = facetObj.label;
    var count = document.createElement('span');
    count.classList.add("pull-right")
    count.textContent = facetObj.count;
    var input = document.createElement('input');
    input.type = "checkbox";

    if (usedFacets.has(facetObj.categID)) {
        input.checked = true;
    }

    li.appendChild(input);
    li.appendChild(label);
    li.appendChild(count);

    if (eventHandler != null && eventHandler != undefined) {
        eventHandler(input);
    }
    return li;
}

function renderFacetList(model, container, facetCheckboxEventHandler) {
    Rx.Observable.from(model.facets)
        .map(f => renderFacetListEntry(f, model.usedFacets, facetCheckboxEventHandler))
        .reduce((array, f) => {
            array.push(f);
            return array;
        }, [])
        .map(treeifyFacets)
        .subscribe(f => {
            var newList = document.createElement('ul');
            newList.id = 'atozFacetsItems';
            // newList.classList.add("list-group");
            newList.appendChild(f);

            var currentList = container.querySelector('#' + newList.id);
            if (currentList != null) {
                currentList.replaceWith(newList);
            } else {
                container.appendChild(newList);
            }
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
