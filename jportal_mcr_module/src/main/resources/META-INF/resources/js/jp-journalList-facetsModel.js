/**
 * Created by chi on 09.10.17.
 */
var labelsMap = null;
function getLabels(){
  if(labelsMap == null){
      let labelsUrl = baseURL + 'rsc/facets/labelsMap';
    labelsMap = Rx.Observable.fromPromise($.get(labelsUrl))
        .publishLast();

    labelsMap.connect();
  }

  return labelsMap;
}


function checkFacetLabel(label, facetObj) {
  if(label === undefined){
    let labelsUrl = baseURL + 'rsc/facets/label/' + facetObj.categID;
    return Rx.Observable.fromPromise($.get(labelsUrl));
  }

  return Rx.Observable.of(label.label)
}


function getFacetLabel(facetObj) {
    return getLabels()
        .map(labels => labels[facetObj.categID])
        .flatMap(label => checkFacetLabel(label, facetObj))
        .map(label => Object.assign({}, facetObj, {label: label}));
}

var lookUpTable = null;
function getLookupTable(){
    if(lookUpTable == null){
        let lookupTableUrl = baseURL + 'rsc/facets/lookupTable';
        lookUpTable = Rx.Observable.fromPromise($.get(lookupTableUrl))
            .publishLast();

        lookUpTable.connect();
    }

    return lookUpTable;
}
function getFacetParent(facetObj) {
    let rootID = facetObj.categID.split(':')[0];
    return getLookupTable()
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
