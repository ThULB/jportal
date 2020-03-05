"use strict";

var jp = jp || {};

jp.journalList = {
  activeTabs: [],
  facets: [],
  usedFacets: [],
  titleFilter: "",
  selectedTab: "",
  numFound: 0,
  excludedFacets: [],
  labelsMap: {},
  lookupTable: {},
  translation: {},
  showSpinner: true,

  init: function init() {
    moment.locale(jp.lang);
    jp.journalList.getLocationHash();

    var checkInternetSpeedStartTime = moment().valueOf();
    var dataPromise = jp.util.getJSON(jp.baseURL + "rsc/facets/initData").then(function (data) {
      jp.journalList.labelsMap = data.labelsMap;
      jp.journalList.lookupTable = data.lookupTable;
      jp.journalList.excludedFacets = data.excludedFacets;
    }).catch(function (error) {
      console.log("Error while retrieving '" + jp.baseURL + "rsc/facets/initData'");
      console.log(error);
    });
    var translatePromise = jp.util.translate("jp.journallist.*").then(function (data) {
      jp.journalList.translation = data;
    }).catch(function (error) {
      console.log("Error while translating 'jp.journallist.*'");
      console.log(error);
    });
    Promise.all([dataPromise, translatePromise]).then(function () {
      jp.journalList.showSpinner = moment().valueOf() - checkInternetSpeedStartTime > 300;
      jp.journalList.update();
    });
  },

  getLocationHash: function getLocationHash() {
    var tab = "A";
    if (location.hash !== undefined && location.hash !== null && location.hash !== "") {
      tab = jp.journalList.fixHashTab(location.hash.substring(1, 2).toUpperCase());

      jp.journalList.usedFacets = [];
      location.hash.substring(3).split("&").filter(function (v, i, array) {
        return array.indexOf(v) === i;
      }) // remove duplicates in Array;
        .filter(function (f) {
          return f !== "";
        }).forEach(function (f) {
        if (f.indexOf("titleFilter:") > -1) {
          jp.journalList.titleFilter = f.replace("titleFilter:", "");
        } else {
          jp.journalList.usedFacets.push(f);
        }
      });
    }
    jp.journalList.selectedTab = tab;
  },

  fixHashTab: function fixHashTab(c) {
    if (c === '_') {
      return '#';
    } else if (c === '#') {
      return '_';
    }
    return c;
  },

  getSearchURL: function getSearchURL( /*List*/facets, titleFilter, /*boolean*/justTitles, tabLetter) {
    var additionalQuery = "%2BobjectType:jpjournal";
    var searchURL = jp.baseURL + 'servlets/solr/select?wt=json&sort=maintitle_sort asc&rows=9999&q=' + additionalQuery;

    var titlesFlag = justTitles ? "&fl=maintitle" : "";

    var qry = '';
    if (tabLetter === '#') {
      qry = ' -maintitle_sort:[a TO z] -maintitle_sort:z*';
    } else if (tabLetter !== undefined && tabLetter !== null && tabLetter !== "") {
      qry = ' %2Bmaintitle_sort:' + tabLetter.toLowerCase() + '*';
    }

    var facetParams = "";
    facets.forEach(function (id) {
      return facetParams = facetParams + ' %2BjournalType:"' + id + '"%20';
    });
    titleFilter = titleFilter !== "" ? "&fq=maintitle_sort:*" + titleFilter + "*" : "";

    return searchURL + qry + facetParams + titlesFlag + '&facet.field=journalType&facet=true' + titleFilter;
  },

  getJournalTitles: function getJournalTitles() {
    var model = jp.journalList;
    var journalTitlesSearchUrl = jp.journalList.getSearchURL(model.usedFacets, model.titleFilter, true);
    return jp.util.getJSON(journalTitlesSearchUrl);
  },

  getResultList: function getResultList() {
    var model = jp.journalList;
    var selectedTabSearchUrl = jp.journalList.getSearchURL(model.usedFacets, model.titleFilter, false, model.selectedTab);
    return jp.util.getJSON(selectedTabSearchUrl);
  },

  bufferCount: function bufferCount(array, count) {
    var bufferCount = [];
    for (var i = 0; i < array.length; i = i + count) {
      bufferCount.push(array.slice(i, i + count));
    }

    return bufferCount;
  },

  update: function update() {
    jp.journalList.view.renderResultListSpinner();
    return jp.journalList.getJournalTitles().then(jp.journalList.updateModel).then(function () {
      return jp.journalList.updateResultList();
    });
  },

  updateResultList: function updateResultList() {
    jp.journalList.view.renderResultListSpinner();
    return jp.journalList.getResultList().then(function (searchResults) {
      return jp.journalList.view.renderResultList(searchResults.response.docs);
    });
  },

  updateTabs: function updateTabs(searchResult) {
    var activeTabs = searchResult.response.docs.map(function (doc) {
      return doc.maintitle;
    }).filter(function (title) {
      return title !== undefined && title !== null && title.length > 0;
    }).map(function (title) {
      return title[0].toUpperCase();
    }).map(function (char) {
      return char.charCodeAt(0) < 65 || char.charCodeAt(0) > 90 ? "#" : char;
    }).filter(function (v, i, array) {
      return array.indexOf(v) === i;
    }) // remove duplicates in Array
      .sort();

    if (activeTabs.length > 0 && activeTabs.indexOf(jp.journalList.selectedTab) === -1) {
      var i = 0;
      if (activeTabs[i].indexOf('#') > -1 && activeTabs.length > 1) {
        i = 1;
      }

      jp.journalList.selectedTab = activeTabs[i];
    }

    jp.journalList.activeTabs = activeTabs;
  },

  updateFacets: function updateFacets(searchResult) {
    var usedFacets = searchResult.responseHeader.params.q.split(' ').filter(function (q) {
      return q.startsWith('+journalType');
    }).map(function (q) {
      return q.replace('+journalType:', '');
    }).map(function (q) {
      return q.replace(/"/g, '');
    }).filter(function (v, i, array) {
      return array.indexOf(v) === i;
    }) // remove duplicates in Array;
      .filter(function (f) {
        return f !== "";
      });

    var facetsFields = searchResult.facet_counts.facet_fields.journalType;
    var facets = jp.journalList.bufferCount(facetsFields, 2).filter(function (journalType) {
      return journalType[1] > 0 || usedFacets.indexOf(journalType[0]) > -1;
    }).map(function (journalType) {
      var categID = journalType[0];
      var count = journalType[1];
      var label = jp.journalList.labelsMap[categID] != null ? jp.journalList.labelsMap[categID].label : "undefined";
      return {
        categID: categID,
        count: count,
        label: label,
        parent: jp.journalList.lookupTable[categID.split(':')[0]],
        excluded: !!jp.journalList.excludedFacets.filter(function (ex) {
          return categID.indexOf(ex) > -1;
        }).pop(),
        inUse: usedFacets.indexOf(categID) > -1
      };
    });
    jp.journalList.usedFacets = usedFacets;
    jp.journalList.facets = facets;
  },

  updateNumFound: function updateNumFound(searchResult) {
    jp.journalList.numFound = searchResult.response.numFound;
  },

  updateLocationHash: function updateLocationHash() {
    var tab = jp.journalList.fixHashTab(jp.journalList.selectedTab);
    var facets = jp.journalList.usedFacets;

    if (jp.journalList.titleFilter !== undefined && jp.journalList.titleFilter !== "") {
      facets = facets.concat("titleFilter:" + jp.journalList.titleFilter);
    }

    facets = facets.join("&");

    location.hash = tab + (facets !== "" ? "/" + facets : "");
  },

  updateModel: function updateModel(searchResult) {
    jp.journalList.updateTabs(searchResult);
    jp.journalList.updateFacets(searchResult);
    jp.journalList.updateNumFound(searchResult);
    jp.journalList.updateLocationHash();

    var model = jp.journalList;
    jp.journalList.view.renderHitCount(model.numFound);
    jp.journalList.view.renderFacetList(model.facets, model.handleFacetClick);
    jp.journalList.view.renderTabNav(model.activeTabs, model.selectedTab, model.handleTabClick);
    jp.journalList.view.renderFilterInput(model.titleFilter, model.handleFilterInput);
  },

  handleTabClick: function handleTabClick(tab) {
    jp.journalList.selectedTab = tab;
    jp.journalList.updateResultList().then(function () {
      return jp.journalList.updateLocationHash();
    });
  },

  handleFilterInput: function handleFilterInput(value) {
    jp.journalList.titleFilter = value;
    jp.journalList.update();
  },

  handleFacetClick: function handleFacetClick(facet) {
    if (facet.add) {
      jp.journalList.usedFacets.push(facet.add);
    } else if (facet.delete) {
      var index = jp.journalList.usedFacets.indexOf(facet.delete);
      jp.journalList.usedFacets.splice(index, 1);
    }

    jp.journalList.update();
  }
};

jp.journalList.view = {
  hitCount: document.getElementById("document_hits"),
  facets: document.getElementById("document_type"),
  tabs: document.getElementById("tabNav").getElementsByTagName("li"),
  selectedTab: undefined,
  resultList: document.getElementById("objectList"),
  filter: document.getElementById("atozFilter"),
  clearFilterButton: document.getElementById("atozFilterRemoveButton"),
  clearFilterButtonIcon: document.getElementById("atozFilterRemoveIcon"),

  renderHitCount: function renderHitCount(num) {
    var text = num === 1 ? jp.journalList.translation["jp.journallist.periodical"] : jp.journalList.translation["jp.journallist.periodicals"];
    jp.journalList.view.hitCount.innerHTML = num + " " + text;
  },

  treeifyFacets: function treeifyFacets(list) {
    var fragment = document.createDocumentFragment();
    var lookup = {};

    list.forEach(function (row) {
      lookup[row.dataset.id] = row;
    });
    list.forEach(function (row) {
      var parent = lookup[row.dataset.parent];
      if (parent !== undefined && parent !== null && row.dataset.parent !== "null") {
        parent.appendChild(lookup[row.dataset.id]);
      } else {
        fragment.appendChild(lookup[row.dataset.id]);
      }
    });
    return fragment;
  },

  renderFilterInput: function renderFilterInput(titleFilter, eventHandler) {
    function toggleClearFilterButton() {
      var valueIsEmpty = jp.journalList.view.filter.value === "";

      jp.journalList.view.clearFilterButtonIcon.style.visibility = valueIsEmpty ? "hidden" : "visible";
      jp.journalList.view.clearFilterButton.style.cursor = valueIsEmpty ? "default" : "pointer";
      jp.journalList.view.clearFilterButton.onclick = valueIsEmpty ? undefined : filterButtonClick;
    }

    function filterButtonClick(e) {
      jp.journalList.view.filter.value = "";
      toggleClearFilterButton();
      eventHandler(jp.journalList.view.filter.value);
    }

    if (titleFilter !== undefined && titleFilter !== "") {
      jp.journalList.view.filter.value = titleFilter;
      toggleClearFilterButton();
    }

    jp.journalList.view.filter.oninput = function (e) {
      var saveOnInput = e.target.oninput;
      e.target.oninput = undefined;
      setTimeout(function () {
        toggleClearFilterButton();
        eventHandler(e.target.value);
        e.target.oninput = saveOnInput;
      }, 500);
    };
  },

  renderFacetListEntry: function renderFacetListEntry(facetObj, /*function*/eventHandler) {
    var row = document.createElement("div");
    row.className = "jp-journalList-facet-row";
    row.dataset.id = facetObj.categID;
    row.dataset.parent = facetObj.parent;

    var entry = document.createElement("div");
    entry.className = "jp-journalList-facet-entry";

    var linkContainer = document.createElement("div");
    linkContainer.className = "jp-journalList-facet-linkContainer";

    var input = document.createElement("input");
    input.className = "jp-journalList-facet-checkbox";
    input.type = "checkbox";

    var label = document.createElement("div");
    label.className = "jp-journalList-facet-label";
    label.textContent = facetObj.label;

    var count = document.createElement("div");
    count.className = "jp-journalList-facet-count";
    count.textContent = facetObj.count;

    linkContainer.appendChild(input);
    linkContainer.appendChild(label);
    entry.appendChild(linkContainer);
    entry.appendChild(count);
    row.appendChild(entry);

    if (facetObj.inUse) {
      input.checked = true;
    }

    function onclick(e) {
      if (eventHandler !== undefined && eventHandler !== null) {
        var operation = facetObj.inUse ? "delete" : "add";
        var event = JSON.parse('{"' + operation + '":"' + facetObj.categID + '"}');
        eventHandler(event);
      }
    }

    entry.onclick = onclick;
    input.onclick = onclick;

    return row;
  },

  renderFacetList: function renderFacetList(facets, facetCheckboxEventHandler) {
    var sortedFacets = facets.sort(function (f1, f2) {
      return f1.label.localeCompare(f2.label);
    });
    var facetsEntries = sortedFacets.filter(function (f) {
      return !(jp.isGuest && f.excluded);
    }).map(function (f) {
      return jp.journalList.view.renderFacetListEntry(f, facetCheckboxEventHandler);
    });

    var newList = document.createElement("div");
    newList.id = 'atozFacetsItems';
    newList.appendChild(jp.journalList.view.treeifyFacets(facetsEntries));

    jp.journalList.view.facets.innerHTML = "";
    jp.journalList.view.facets.appendChild(newList);
  },

  renderTabNav: function renderTabNav(activeTabs, selectedTab, eventHandler) {
    var tabs = jp.journalList.view.tabs;

    var _loop = function _loop(i) {
      var tab = tabs.item(i);

      if (activeTabs.indexOf(tab.textContent) > -1) {
        tab.classList.add("active");
        tab.onclick = function (e) {
          jp.journalList.view.setSelectedTab(tab);
          eventHandler(e.target.textContent);
        };
      } else {
        tab.classList.remove("active");
        tab.onclick = undefined;
      }

      if (tab.textContent === selectedTab) {
        jp.journalList.view.setSelectedTab(tab);
      }
    };

    for (var i = 0; i < tabs.length; i++) {
      _loop(i);
    }
  },

  setSelectedTab: function setSelectedTab(tab) {
    if (jp.journalList.view.selectedTab !== undefined) {
      jp.journalList.view.selectedTab.classList.remove("selected-tab");
    }

    tab.classList.add("selected-tab");
    jp.journalList.view.selectedTab = tab;
  },

  renderJournalTitle: function renderJournalTitle(journal) {
    var titleLink = document.createElement("a");
    var title = document.createElement("h3");

    title.classList.add("journal-title");
    title.appendChild(titleLink);

    titleLink.textContent = journal.maintitle;
    titleLink.setAttribute("href", jp.baseURL + 'receive/' + journal.id);

    return title;
  },

  renderJournalPublished: function renderJournalPublished(journal) {
    var publishedDiv = document.createElement("div");
    publishedDiv.classList.add("journal-published");
    var publishedValue = journal["date.published"];
    if (publishedValue == null) {
      return publishedDiv;
    }
    var dateFormat = "Do MMM YYYY";
    var publishedStr = jp.journalList.translation["jp.journallist.published"] + ': ';
    var toIndex = publishedValue.indexOf(" TO ");
    if (toIndex > 0) {
      var from = publishedValue.substring(1, toIndex);
      var until = publishedValue.substring(toIndex + 4, publishedValue.length - 1);
      publishedStr += jp.journalList.view.renderDate(from, dateFormat);
      publishedStr += " - ";
      publishedStr += jp.journalList.view.renderDate(until, dateFormat);
    } else {
      publishedStr += jp.journalList.view.renderDate(publishedValue, dateFormat);
    }
    publishedDiv.textContent = publishedStr;
    return publishedDiv;
  },

  renderDate: function renderDate(date, dateFormat) {
    if (date.length === 4) {
      return date;
    } else {
      return moment(date, "YYYY-MM-DD").format(dateFormat);
    }
  },

  renderJournalPublisher: function renderJournalPublisher(journal, solrKey, caption) {
    var div = document.createElement("div");
    div.classList.add("publisher");

    var publisherList = journal[solrKey];
    if (publisherList) {
      var pusblisherStr = caption + ': ';
      for (var i = 0; i < publisherList.length; i++) {
        var publisher = publisherList[i];
        var indexOfHash = publisher.indexOf('#');
        if (indexOfHash === -1) {
          console.log("Invalid publisher format for '" + publisher + "'.");
          continue;
        }
        var publisherID = publisher.substring(0, indexOfHash);
        var publisherText = publisher.substring(indexOfHash + 1);
        pusblisherStr += "<a href='" + jp.baseURL + 'receive/' + publisherID + "'>" + publisherText + "</a>";
        if (i + 1 < publisherList.length) {
          pusblisherStr += "; ";
        }
      }
      div.insertAdjacentHTML("afterbegin", pusblisherStr);
    }

    return div;
  },

  renderResultListEntry: function renderResultListEntry(journal) {
    var journalTitle = jp.journalList.view.renderJournalTitle(journal);
    var journalPublished = jp.journalList.view.renderJournalPublished(journal);
    var publisher = jp.journalList.view.renderJournalPublisher(journal, "participant.mainPublisher", jp.journalList.translation["jp.journallist.publisher"]);
    var author = jp.journalList.view.renderJournalPublisher(journal, "participant.author", jp.journalList.translation["jp.journallist.author"]);

    var li = document.createElement("li");
    li.appendChild(journalTitle);
    li.appendChild(journalPublished);
    li.appendChild(publisher);
    li.appendChild(author);
    return li;
  },

  renderResultList: function renderResultList(resultList) {
    jp.journalList.view.resultList.textContent = "";

    if (resultList.length > 0) {
      var fragment = document.createElement("ul");
      resultList.map(jp.journalList.view.renderResultListEntry).forEach(function (entry) {
        return fragment.appendChild(entry);
      });

      jp.journalList.view.resultList.appendChild(fragment);
    } else {
      jp.journalList.view.resultList.insertAdjacentHTML('afterbegin', '<span class="ui-msg">Keine Einträge unter dieser Katgorie.</span>');
    }
  },

  renderResultListSpinner: function renderResultListSpinner() {
    if (!jp.journalList.showSpinner) {
      return;
    }
    var firstChild = jp.journalList.view.resultList.firstElementChild;
    if (firstChild != null && firstChild.tagName.toLocaleLowerCase() === "i") {
      return;
    }
    var spinner = document.createElement("i");
    spinner.classList.add("fas", "fa-circle-notch", "fa-spin", "fa-2x", "jp-journalList-spinner");
    jp.journalList.view.resultList.insertBefore(spinner, jp.journalList.view.resultList.firstChild);
  }

};

jp.journalList.init();