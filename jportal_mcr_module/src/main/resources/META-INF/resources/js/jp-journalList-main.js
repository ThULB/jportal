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

  init: function () {
    moment.locale(jp.lang);
    jp.journalList.getLocationHash();

    let dataPromise = jp.util.getJSON(jp.baseURL + "rsc/facets/initData")
      .then(data => {
        jp.journalList.labelsMap = data.labelsMap;
        jp.journalList.lookupTable = data.lookupTable;
        jp.journalList.excludedFacets = data.excludedFacets;
      });
    let translatePromise = jp.util.translate("jp.journallist.*").then(data => {
        jp.journalList.translation = data;
    });
    Promise.all([dataPromise, translatePromise]).then(() => {
      jp.journalList.update();
    });
  },

  getLocationHash: function () {
    let tab = "A";
    if (location.hash !== undefined && location.hash !== null && location.hash !== "") {
      tab = jp.journalList.fixHashTab(location.hash.substring(1, 2).toUpperCase());

      jp.journalList.usedFacets = [];
      location.hash.substring(3)
        .split("&")
        .filter((v, i, array) => array.indexOf(v) === i)// remove duplicates in Array;
        .filter(f => f !== "")
        .forEach(f => {
          if (f.indexOf("titleFilter:") > -1) {
            jp.journalList.titleFilter = f.replace("titleFilter:", "");
          } else {
            jp.journalList.usedFacets.push(f)
          }

        });
    }
    jp.journalList.selectedTab = tab;
  },

  fixHashTab: function (c) {
    if (c === '_') {
      return '#';
    } else if (c === '#') {
      return '_';
    }
    return c;
  },

  getSearchURL: function (/*List*/ facets, titleFilter, /*boolean*/ justTitles, tabLetter) {
    let additionalQuery = "%2BobjectType:jpjournal";
    let searchURL = jp.baseURL
        + 'servlets/solr/select?wt=json&sort=maintitle_sort asc&rows=9999&q='
        + additionalQuery;

    let titlesFlag = justTitles ? "&fl=maintitle" : "";

    let qry = '';
    if (tabLetter === '#') {
      qry = ' -maintitle_sort:[a TO z] -maintitle_sort:z*';
    } else if (tabLetter !== undefined && tabLetter !== null && tabLetter !== "") {
      qry = ' %2Bmaintitle_sort:' + tabLetter.toLowerCase() + '*';
    }

    let facetParams = "";
    facets.forEach(id => facetParams = facetParams + ' %2BjournalType:"' + id + '"%20');
    titleFilter = titleFilter !== "" ? "&fq=maintitle_sort:*" + titleFilter + "*" : "";

    return searchURL + qry + facetParams + titlesFlag + '&facet.field=journalType&facet=true' + titleFilter;
  },

  getJournalTitles: function () {
    let model = jp.journalList;
    let journalTitlesSearchUrl = jp.journalList.getSearchURL(model.usedFacets, model.titleFilter, true);
    return jp.util.getJSON(journalTitlesSearchUrl);
  },

  getResultList: function () {
    let model = jp.journalList;
    let selectedTabSearchUrl = jp.journalList.getSearchURL(model.usedFacets, model.titleFilter, false, model.selectedTab);
    return jp.util.getJSON(selectedTabSearchUrl);
  },

  bufferCount: function (array, count) {
    let bufferCount = [];
    for (i = 0; i < array.length; i = i + count) {
      bufferCount.push(array.slice(i, i + count));
    }

    return bufferCount;
  },

  update: function () {
    jp.journalList.view.renderResultListSpinner();
    return jp.journalList.getJournalTitles()
        .then(jp.journalList.updateModel)
        .then(() => jp.journalList.updateResultList());
  },

  updateResultList: function () {
    jp.journalList.view.renderResultListSpinner();
    return jp.journalList.getResultList()
        .then(searchResults => jp.journalList.view.renderResultList(searchResults.response.docs))
  },

  updateTabs: function (searchResult) {
    let activeTabs = searchResult.response.docs
        .map(doc => doc.maintitle)
        .filter(title => title !== undefined && title !== null && title.length > 0)
        .map(title => title[0].toUpperCase())
        .map(char => (char.charCodeAt(0) < 65 || char.charCodeAt(0) > 90) ? "#" : char)
        .filter((v, i, array) => array.indexOf(v) === i) // remove duplicates in Array
        .sort();

    if (activeTabs.length > 0 && activeTabs.indexOf(jp.journalList.selectedTab) === -1) {
      let i = 0;
      if (activeTabs[i].indexOf('#') > -1 && activeTabs.length > 1) {
        i = 1;
      }

      jp.journalList.selectedTab = activeTabs[i];
    }

    jp.journalList.activeTabs = activeTabs;
  },

  updateFacets: function (searchResult) {
    let usedFacets = searchResult.responseHeader.params.q
        .split(' ')
        .filter(q => q.startsWith('+journalType'))
        .map(q => q.replace('+journalType:', ''))
        .map(q => q.replace(/"/g, ''))
        .filter((v, i, array) => array.indexOf(v) === i)// remove duplicates in Array;
        .filter(f => f !== "");

    let facetsFields = searchResult.facet_counts.facet_fields.journalType;
    let facets = jp.journalList.bufferCount(facetsFields, 2)
        .filter(journalType => journalType[1] > 0 || usedFacets.indexOf(journalType[0]) > -1)
        .map(journalType => ({
          categID: journalType[0],
          count: journalType[1],
          label: jp.journalList.labelsMap[journalType[0]].label,
          parent: jp.journalList.lookupTable[journalType[0].split(':')[0]],
          excluded: !!jp.journalList.excludedFacets.filter(ex => journalType[0].indexOf(ex) > -1).pop(),
          inUse: usedFacets.indexOf(journalType[0]) > -1
        }));

    jp.journalList.usedFacets = usedFacets;
    jp.journalList.facets = facets;
  },

  updateNumFound: function (searchResult) {
    jp.journalList.numFound = searchResult.response.numFound;
  },

  updateLocationHash: function () {
    let tab = jp.journalList.fixHashTab(jp.journalList.selectedTab);
    let facets = jp.journalList.usedFacets;

    if (jp.journalList.titleFilter !== undefined && jp.journalList.titleFilter !== "") {
      facets = facets.concat("titleFilter:" + jp.journalList.titleFilter);
    }

    facets = facets.join("&");

    location.hash = tab + (facets !== "" ? ("/" + facets) : "");
  },

  updateModel: function (searchResult) {
    jp.journalList.updateTabs(searchResult);
    jp.journalList.updateFacets(searchResult);
    jp.journalList.updateNumFound(searchResult);
    jp.journalList.updateLocationHash();

    let model = jp.journalList;
    jp.journalList.view.renderHitCount(model.numFound);
    jp.journalList.view.renderFacetList(model.facets, model.handleFacetClick);
    jp.journalList.view.renderTabNav(model.activeTabs, model.selectedTab, model.handleTabClick);
    jp.journalList.view.renderFilterInput(model.titleFilter, model.handleFilterInput);
  },

  handleTabClick: function (tab) {
    jp.journalList.selectedTab = tab;
    jp.journalList.updateResultList()
        .then(() => jp.journalList.updateLocationHash());
  },

  handleFilterInput: function (value) {
    jp.journalList.titleFilter = value;
    jp.journalList.update();
  },

  handleFacetClick: function (facet) {
    if (facet.add) {
      jp.journalList.usedFacets.push(facet.add);
    } else if (facet.delete) {
      let index = jp.journalList.usedFacets.indexOf(facet.delete);
      jp.journalList.usedFacets.splice(index, 1);
    }

    jp.journalList.update();
  },
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

  renderHitCount: function (num) {
    let text = num === 1 ? jp.journalList.translation["jp.journallist.periodical"] :
                           jp.journalList.translation["jp.journallist.periodicals"];
    jp.journalList.view.hitCount.innerHTML = num + " " + text;
  },

  treeifyFacets: function (list) {
    let fragment = document.createDocumentFragment();
    let lookup = {};

    list.forEach(function (row) {
      lookup[row.dataset.id] = row;
    });
    list.forEach(function (row) {
      let parent = lookup[row.dataset.parent];
      if (parent !== undefined && parent !== null && row.dataset.parent !== "null") {
        parent.appendChild(lookup[row.dataset.id]);
      } else {
        fragment.appendChild(lookup[row.dataset.id]);
      }
    });
    return fragment;
  },

  renderFilterInput: function (titleFilter, eventHandler) {
    function toggleClearFilterButton() {
      let valueIsEmpty = jp.journalList.view.filter.value === "";

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

    jp.journalList.view.filter.oninput = e => {
      let saveOnInput = e.target.oninput;
      e.target.oninput = undefined;
      setTimeout(() => {
        toggleClearFilterButton();
        eventHandler(e.target.value);
        e.target.oninput = saveOnInput;
      }, 500);
    };
  },

  renderFacetListEntry: function (facetObj, /*function*/ eventHandler) {
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

    if (facetObj.inUse) {
      input.checked = true;
    }

    function onclick(e) {
      if (eventHandler !== undefined && eventHandler !== null) {
        let operation = facetObj.inUse ? "delete" : "add";
        let event = JSON.parse('{"' + operation + '":"' + facetObj.categID + '"}');
        eventHandler(event)
      }
    }

    entry.onclick = onclick;
    input.onclick = onclick;

    return row;
  },

  renderFacetList: function (facets, facetCheckboxEventHandler) {
    let sortedFacets = facets.sort((f1, f2) => f1.label.localeCompare(f2.label));
    let facetsEntries = sortedFacets.filter(f => !(jp.isGuest && f.excluded))
        .map(f => jp.journalList.view.renderFacetListEntry(f, facetCheckboxEventHandler));

    let newList = document.createElement("div");
    newList.id = 'atozFacetsItems';
    newList.appendChild(jp.journalList.view.treeifyFacets(facetsEntries));

    jp.journalList.view.facets.innerHTML = "";
    jp.journalList.view.facets.appendChild(newList);
  },

  renderTabNav: function (activeTabs, selectedTab, eventHandler) {
    let tabs = jp.journalList.view.tabs;
    for (i = 0; i < tabs.length; i++) {
      let tab = tabs.item(i);

      if (activeTabs.indexOf(tab.textContent) > -1) {
        tab.classList.add("active");
        tab.onclick = e => {
          jp.journalList.view.setSelectedTab(tab);
          eventHandler(e.target.textContent)
        };
      } else {
        tab.classList.remove("active");
        tab.onclick = undefined;
      }

      if (tab.textContent === selectedTab) {
        jp.journalList.view.setSelectedTab(tab);
      }
    }
  },

  setSelectedTab: function (tab) {
    if (jp.journalList.view.selectedTab !== undefined) {
      jp.journalList.view.selectedTab.classList.remove("selected-tab");
    }

    tab.classList.add("selected-tab");
    jp.journalList.view.selectedTab = tab;
  },

  renderJournalTitle: function (journal) {
    let titleLink = document.createElement("a");
    let title = document.createElement("h3");

    title.classList.add("journal-title");
    title.appendChild(titleLink);

    titleLink.textContent = journal.maintitle;
    titleLink.setAttribute("href", jp.baseURL + 'receive/' + journal.id);

    return title;
  },

  renderJournalPublished: function (journal) {
    let publishedDiv = document.createElement("div");
    publishedDiv.classList.add("journal-published");
    let dateFormat = "Do MMM YYYY";
    let publishedStr = jp.journalList.translation["jp.journallist.published"] + ': ';
    let publishedValue = journal["date.published"];
    let toIndex = publishedValue.indexOf(" TO ");
    if(toIndex > 0) {
      let from = publishedValue.substring(1, toIndex);
      let until = publishedValue.substring(toIndex + 4, publishedValue.length - 1);
      publishedStr += moment(from, "YYYY-MM-DD").format(dateFormat);
      publishedStr += " - ";
      publishedStr += moment(until, "YYYY-MM-DD").format(dateFormat);
    } else {
        publishedStr += moment(publishedValue, "YYYY-MM-DD").format(dateFormat);
    }
    publishedDiv.textContent = publishedStr;
    return publishedDiv;
  },

  renderJournalPublisher: function (journal, solrKey, caption) {
    let div = document.createElement("div");
    div.classList.add("publisher");

    let publisherList = journal[solrKey];
    if (publisherList) {
      let pusblisherStr = caption + ': ';
      for (let i = 0; i < publisherList.length; i++) {
        let publisher = publisherList[i];
        let indexOfHash = publisher.indexOf('#');
        if (indexOfHash === -1) {
          console.log("Invalid publisher format for '" + publisher + "'.");
          continue;
        }
        let publisherID = publisher.substring(0, indexOfHash);
        let publisherText = publisher.substring(indexOfHash + 1);
        pusblisherStr += "<a href='" + jp.baseURL + 'receive/' + publisherID + "'>" + publisherText + "</a>";
        if (i + 1 < publisherList.length) {
          pusblisherStr += "; ";
        }
      }
      div.insertAdjacentHTML("afterbegin", pusblisherStr);
    }

    return div;
  },

  renderResultListEntry: function (journal) {
    let journalTitle = jp.journalList.view.renderJournalTitle(journal);
    let journalPublished = jp.journalList.view.renderJournalPublished(journal);
    let publisher = jp.journalList.view.renderJournalPublisher(journal, "participant.mainPublisher",
        jp.journalList.translation["jp.journallist.publisher"]);
    let author = jp.journalList.view.renderJournalPublisher(journal, "participant.author",
        jp.journalList.translation["jp.journallist.author"]);

    let li = document.createElement("li");
    li.appendChild(journalTitle);
    li.appendChild(journalPublished);
    li.appendChild(publisher);
    li.appendChild(author);
    return li;
  },

  renderResultList: function (resultList) {
    jp.journalList.view.resultList.textContent = "";

    if (resultList.length > 0) {
      let fragment = document.createElement("ul");
      resultList.map(jp.journalList.view.renderResultListEntry)
          .forEach(entry => fragment.appendChild(entry));

      jp.journalList.view.resultList.appendChild(fragment);
    } else {
      jp.journalList.view.resultList
          .insertAdjacentHTML('afterbegin', '<span class="ui-msg">Keine Einträge unter dieser Katgorie.</span>');
    }
  },

  renderResultListSpinner: function() {
    let firstChild = jp.journalList.view.resultList.firstElementChild;
    if(firstChild != null && firstChild.tagName.toLocaleLowerCase() === "i") {
      return;
    }
    let spinner = document.createElement("i");
    spinner.classList.add("fa", "fa-circle-o-notch", "fa-spin", "fa-2x", "jp-journalList-spinner");
    jp.journalList.view.resultList.insertBefore(spinner, jp.journalList.view.resultList.firstChild);
  }

};

jp.journalList.init();
