var jp = jp || {};
/*
*   GUI: Filter, A-Z list, facet list, result list
*   A-Z list and facet list -> 1 solr request, then split result list and facet list
*
 */
jp.az = {

    getSearchURL: function () {
        var url = jp.baseURL + 'servlets/solr/select?wt=json&sort=maintitle_sort asc&rows=9999&q=';
        var additionalQuery = $('#firstLetterTab').attr('additionalQuery');
        return url + additionalQuery;
    },

    getFilterQuery: function () {
        var filter = $("#atozFilter").val().toLowerCase();
        ;
        return filter != "" ? "&fq=maintitle_sort:*" + filter + "*" : "";
    },

    getTabs: function (/* function */ onSuccess) {
        var url = jp.az.getSearchURL() + '&fl=maintitle&facet.field=journalType&facet=true' + jp.az.getFilterQuery();

        /*
        $.getJSON(url, function (searchResult) {
            var response = searchResult.response;
            var tabs = [];
            for (var i = 0; i < response.numFound; i++) {
                var maintitle = response.docs[i].maintitle;
                if (maintitle != null && maintitle.length > 0) {
                    var char = maintitle[0].toUpperCase();
                    char = (char.charCodeAt(0) < 65 || char.charCodeAt(0) > 90) ? "#" : char;
                    if ($.inArray(char, tabs) == -1) {
                        tabs.push(char);
                    }
                }
            }
            onSuccess(tabs);
        });
        */


        var searchResultsStream = Rx.Observable.fromPromise($.getJSON(url));
        var journalsStream =  searchResultsStream.flatMap(searchResult => searchResult.response.docs);
        var facetsStream =  searchResultsStream.flatMap(searchResult => searchResult.facet_counts.facet_fields.journalType);

        facetsStream.bufferWithCount(2).subscribe(facet => console.log("Facet: " + facet));


        journalsStream
            .map(doc => doc.maintitle)
            .filter(title => title != null && title.length > 0)
            .map(title => title[0].toUpperCase())
            .map(char => (char.charCodeAt(0) < 65 || char.charCodeAt(0) > 90) ? "#" : char)
            .distinct()
            .reduce(function (tabs, char) {
                tabs.push(char);
                return tabs;
            }, [])
            .subscribe(tabs => onSuccess(tabs));
    },

    getJournals: function (/* string */ tabLetter, /* function */ onSuccess) {
        var qry = '';
        if (tabLetter == '#') {
            qry = '-maintitle_sort:[a TO z] -maintitle_sort:z*';
        } else {
            qry = '%2Bmaintitle_sort:' + tabLetter.toLowerCase() + '*';
        }
        $.getJSON(jp.az.getSearchURL() + ' ' + qry + jp.az.getFilterQuery(), function (searchResult) {
            onSuccess(searchResult.response);
        });
    },

    load: function () {
        jp.az.importCSS();
        jp.az.printTabNav();
        jp.az.printFilter();

        jp.az.updateTabs(null);
        var tab = $(location).attr('hash').substring(1, 2).toUpperCase();
        tab = (tab == "" || tab == null) ? "A" : tab;
        jp.az.setTab(tab);
        jp.az.updateJournals();
        jp.az.loadFacets();
    },

    importCSS: function () {
        if (document.createStyleSheet) {
            document.createStyleSheet(jp.baseURL + 'css/jp-journalList.css');
        } else {
            var link = $('<link>').attr({
                type: 'text/css',
                rel: 'stylesheet',
                href: jp.baseURL + 'css/jp-journalList.css',
                'class': 'myStyle'
            });
            $('head').append(link);
        }
    },

    printTabNav: function () {
        var tabsHTML = "<li>#</li>";
        for (var i = 65; i <= 90; i++) {
            var char = String.fromCharCode(i);
            tabsHTML += "<li>" + char + "</li>";
        }
        $('#tabNav').append(tabsHTML);
    },

    printFilter: function () {
        /*
        var filter = $("#atozFilter");
        var filterRemoveButton = $("#atozFilterRemoveButton");
        filter.on("keyup paste", function () {
            filterRemoveButton.css("cursor", "pointer");
            jp.az.updateTabs();
            jp.az.updateJournals();
            jp.az.updateFilter();
        });
        filterRemoveButton.on("click", function () {
            $('#atozFilter').val('');
            $('#atozFilterRemoveIcon').css("visibility", 'hidden');
            filterRemoveButton.css("cursor", "");
            jp.az.updateTabs();
            jp.az.updateJournals();
        });
        */

        var filterInput = document.querySelector('#atozFilter');
        var filterRemoveButton = document.querySelector('#atozFilterRemoveButton');
        var filterRemoveIcon = document.querySelector('#atozFilterRemoveIcon');
        var filterInputStream = Rx.Observable.of(filterInput);
        var filterInputEventStream = Rx.Observable.fromEvent(filterInput, 'input');
        var filterRemoveButtonClickStream = Rx.Observable.fromEvent(filterRemoveButton, 'click');

        function updateButtonCSS(inputVal){
            if(inputVal === ""){
                    filterRemoveButton.setAttribute("style", "cursor:default;");
                    filterRemoveIcon.setAttribute("style", "visibility:hidden;");
            }else {
                filterRemoveButton.setAttribute("style", "cursor:pointer;");
                filterRemoveIcon.setAttribute("style", "visibility:visible;");
            }
        }

        filterInputEventStream.merge(filterRemoveButtonClickStream)
            .combineLatest(filterInputStream, function(event, input){
                if(event instanceof MouseEvent){
                    input.value = '';
                }

                return input;
            })
            .map(input => input.value)
            .subscribe(function(inputVal){
                updateButtonCSS(inputVal);
                jp.az.updateTabs();
                jp.az.updateJournals();
            });
    },

    setTab: function (/* string */ tab) {
        $(location).attr('hash', tab);
        $("#tabNav > li").removeClass("selected-tab");
        $("#tabNav > li:contains('" + tab + "')").addClass("selected-tab");
    },

    getTab: function () {
        return $("#tabNav > li.selected-tab").text();
    },

    updateTabs: function () {
        jp.az.getTabs(function (activeTabs) {
            $("#tabNav > li").each(function () {
                var li = $(this);
                li.off("click");
                if ($.inArray(li.text(), activeTabs) >= 0) {
                    li.addClass('active');
                    li.on("click", function () {
                        jp.az.setTab($(this).text());
                        jp.az.updateJournals();
                    });
                } else {
                    li.removeClass('active');
                }
            });
        });
    },

    updateJournals: function () {
        var tab = jp.az.getTab();
        jp.az.getJournals(tab, function (response) {
            var resultList = $('<ul/>');
            if (response.numFound == 0) {
                resultList.html('<span class="ui-msg">Keine Einträge unter dieser Katgorie.</span>')
            }
            for (var i = 0; i < response.numFound; i++) {
                var resultListEntry = $('<li/>');
                jp.az.printJournalEntry(resultListEntry, response.docs[i]);
                resultList.append(resultListEntry);
            }
            $("#resultList").empty().append(resultList);
        });
    },

    printJournalEntry: function (resultListEntry, metadata) {
        var titleLink = $('<a/>').html(metadata.maintitle).attr('href', jp.baseURL + 'receive/' + metadata.id);
        var title = $('<h3 class="journal-title"/>').append(titleLink);
        resultListEntry.append(title);
        jp.az.printPublished(resultListEntry, metadata);
        jp.az.printPublisher(resultListEntry, metadata, "participant.mainPublisher", "Herausgeber");
        jp.az.printPublisher(resultListEntry, metadata, "participant.author", "Autor");
    },

    printPublished: function (node, journal) {
        var publishedStr = 'Erscheinungsverlauf: ';
        if (journal["date.published"]) {
            publishedStr += journal["date.published"];
            node.append($('<div class="journal-published"/>').html(publishedStr));
        } else if (journal["date.published_from"]) {
            publishedStr += journal["date.published_from"] + ' - ';
            if (journal["date.published_until"]) {
                publishedStr = publishedStr + journal["date.published_until"];
            }
            node.append($('<div class="journal-published"/>').html(publishedStr));
        }
    },

    printPublisher: function (node, journal, solrKey, caption) {
        var publisherList = journal[solrKey];
        if (publisherList) {
            var pusblisherStr = '<div class="publisher">' + caption + ': ';
            for (var i = 0; i < publisherList.length; i++) {
                var publisher = publisherList[i];
                var indexOfHash = publisher.indexOf('#');
                if (indexOfHash == -1) {
                    console.log("Invalid publisher format for '" + publisher + "'.");
                    continue;
                }
                var publisherID = publisher.substring(0, indexOfHash);
                var publisherText = publisher.substring(indexOfHash + 1);
                var publisherLink = "<a href='" + jp.baseURL + 'receive/' + publisherID + "'>" + publisherText + "</a>";
                pusblisherStr += publisherLink;
                if (i + 1 < publisherList.length) {
                    pusblisherStr += "; ";
                }
            }
            node.append(pusblisherStr + "</div>");
        }
    },

    updateFilter: function () {
        if ($('#atozFilter').val() == '') {
            $('#atozFilterRemoveIcon').css("visibility", 'hidden');
        } else {
            $('#atozFilterRemoveIcon').css("visibility", 'visible');
        }
    },

    // - request A-Z list with facets
    // - split for A-Z and facets list
    // - Observeable filter + facets
    // - solr facets stream
    // - label of facets
    // - class hierarchy in layoutDefaultSettings.xml
    loadFacets: function () {
        //params={q=objectType:"jpjournal"+AND+journalType:*+&facet.field=journalType&indent=true&wt=json&facet=true&_=1495637943282}
        //$.getJSON(jp.az.getSearchURL() + ' ' + qry + jp.az.getFilterQuery(), function(searchResult)

        // $('#atozFacets').empty().append('<ul id="atozFacetsItems"/>');
        var listContainer = document.createElement('ul');
        listContainer.id = 'atozFacetsItems';
        var facetsContainer = document.querySelector('#atozFacets');
        facetsContainer.innerHTML = "";
        facetsContainer.append(listContainer);

        function createFacetsRequestStream(param) {
            var facetQry = ' &facet.field=journalType&indent=true&wt=json&facet=true';
            if (param != null && param != '') {
                facetQry = param + facetQry;
            }

            var qry = jp.az.getSearchURL() + facetQry + jp.az.getFilterQuery();

            console.log(qry);

            return Rx.Observable.just(qry);
        }

        function createFacetsResponseStream(facetsRequestStream) {
            return facetsRequestStream
                .flatMap(requestURL => Rx.Observable.fromPromise($.getJSON(requestURL)));
        }

        var lookupTableStream = Rx.Observable.fromPromise($.get(jp.baseURL + 'rsc/facets/lookupTable'));

        function createFacetObj(journalType) {
            var categID = journalType[0];
            var url = jp.baseURL + 'rsc/facets/label/' + categID;

            return Rx.Observable.fromPromise($.get(url))
                .map(label => ({
                        "label": label,
                        "categID": journalType[0],
                        "count": journalType[1]
                    })
                ).combineLatest(lookupTableStream, function(journalType, lookup){
                    var rootID = journalType.categID.split(':')[0];
                    var parent = lookup[rootID];
                    console.log('Parent: ' + journalType.categID + ' # ' + parent);
                    journalType.parent = parent;
                    return journalType;
                })
        }

        function renderButton(journalType) {
            var button = document.createElement('button');
            button.className = 'clickButton';
            button.dataset.categID = journalType.categID;
            button.dataset.parent = journalType.parent;
            button.className = "facetButton";
            button.textContent = journalType.label + " (" + journalType.count + ")";
            return button;
        }

        function renderListElement(button, listContainer) {
            var li = document.createElement('li');
            li.appendChild(button);
            listContainer.appendChild(li);
        }

        function createFacetObjStream(facetsResponseStream) {
            return facetsResponseStream
                .flatMap(response => Rx.Observable.from(response.facet_counts.facet_fields.journalType))
                .bufferWithCount(2)
                .filter(journalType => journalType[1] > 0)
                .flatMap(journalType => createFacetObj(journalType));
        }

        function createClickStream(containerStream, facetsParam) {
            var facetsRequestStream = createFacetsRequestStream(facetsParam);
            var facetsResponseStream = createFacetsResponseStream(facetsRequestStream);
            var facetObjStream = createFacetObjStream(facetsResponseStream);
            return facetObjStream
                .map(facet => renderButton(facet))
                .flatMap(button => Rx.Observable.fromEvent(button, 'click')
                    .combineLatest(containerStream, function (click, container) {
                        container.innerHTML = "";
                        return click;
                    })
                    .flatMap(click => createClickStream(containerStream, ' %2BjournalType:"' + button.dataset.categID + '"%20'))
                    .startWith(button)
                );
        }

        // var container = document.querySelector('#atozFacetsItems');
        var containerStream = Rx.Observable.of(listContainer);

        createClickStream(containerStream).subscribe(function (button) {
            //render selected "root" category first
            renderListElement(button, listContainer);
        })
    },

    //http://stackoverflow.com/a/22367819/3123195
    treeify: function (list, idAttr, parentAttr, childrenAttr) {
        if (!idAttr) idAttr = 'id';
        if (!parentAttr) parentAttr = 'parent';
        if (!childrenAttr) childrenAttr = 'children';
        var treeList = [];
        var lookup = {};
        list.forEach(function (obj) {
            lookup[obj[idAttr]] = obj;
            obj[childrenAttr] = [];
        });
        list.forEach(function (obj) {
            if (obj[parentAttr] != null) {
                lookup[obj[parentAttr]][childrenAttr].push(obj);
            } else {
                treeList.push(obj);
            }
        });
        return treeList;
    }
}