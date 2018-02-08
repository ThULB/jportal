/**
 * Created by chi on 09.10.17.
 */
function renderFacetListEntry(facetObj, /*function*/ usedFacets, /*function*/ eventHandler) {
    let row = document.createElement("div");
    row.className = "jp-journalList-facet-row";
    row.dataset.id = facetObj.categID;
    row.dataset.parent = facetObj.parent;

    let entry = document.createElement("div");
    entry.className = "jp-journalList-facet-entry";

    let linkContainer = document.createElement("div");
    linkContainer.className = "jp-journalList-facet-linkContainer";

    let input = document.createElement("input");
    input.className = "jp-journalList-facet-checkbox";
    input.type = "checkbox";

    let label = document.createElement("div");
    label.className = "jp-journalList-facet-label";
    label.textContent = facetObj.label;

    let count = document.createElement("div");
    count.className = "jp-journalList-facet-count";
    count.textContent = facetObj.count;

    linkContainer.appendChild(input);
    linkContainer.appendChild(label);
    entry.appendChild(linkContainer);
    entry.appendChild(count);
    row.appendChild(entry);

    if (usedFacets.has(facetObj.categID)) {
        input.checked = true;
    }

    if (eventHandler != null) {
        eventHandler(entry);
    }
    return row;
}

function renderFacetList(model, container, facetCheckboxEventHandler) {
    let sortedFacets = model.facets.sort((f1, f2) => f1.label.localeCompare(f2.label));
    Rx.Observable.from(sortedFacets)
        .filter(f => !(jp.isGuest && f.excluded))
        .map(f => renderFacetListEntry(f, model.usedFacets, facetCheckboxEventHandler))
        .reduce((array, f) => {
            array.push(f);
            return array;
        }, [])
        .map(treeifyFacets)
        .subscribe(f => {
            let newList = document.createElement("div");
            newList.id = 'atozFacetsItems';
            newList.appendChild(f);

            let currentList = container.querySelector('#' + newList.id);
            if (currentList != null) {
                currentList.replaceWith(newList);
            } else {
                container.appendChild(newList);
            }
        });
}

function treeifyFacets(list) {
    let fragment = document.createDocumentFragment();
    let lookup = {};

    list.forEach(function (row) {
        lookup[row.dataset.id] = row;
    });
    list.forEach(function (row) {
        let parent = lookup[row.dataset.parent];
        if (parent != null && row.dataset.parent !== "null") {
            parent.appendChild(lookup[row.dataset.id]);
        } else {
            fragment.appendChild(lookup[row.dataset.id]);
        }
    });
    return fragment;
}
