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
    }
    ,

    getFilterQuery: function () {
        var filter = $("#atozFilter").val().toLowerCase();
        ;
        return filter != "" ? "&fq=maintitle_sort:*" + filter + "*" : "";
    }
    ,

    getTabsAndFacets: function (/* function */ onSuccess, facetParamStream) {
        // get tabs and facets
        let facetOnOption = '&fl=maintitle&facet.field=journalType&facet=true';

        facetParamStream
            .distinct()
            .reduce((facetFilter, f) => facetFilter + f, '')
            .map(facetFilter => jp.az.getSearchURL() + facetFilter + facetOnOption + jp.az.getFilterQuery())
            .do(url => console.log("getTabsAndFacets: " + url))
            .flatMap(url => Rx.Observable.fromPromise($.getJSON(url)))
            .flatMap(function (searchResult) {
                var facetObjStream = jp.az.createFacetObjStream(searchResult);
                var alreadyUsedFacetFilterStream = jp.az.getFacetFilterFrom(searchResult);
                return jp.az.createTabStream(searchResult)
                    .map(tabs => ({
                        tabs: tabs,
                        facetObjStream: facetObjStream,
                        alreadyUsedFacetFilterStream: alreadyUsedFacetFilterStream
                    }));
            })
            .subscribe(function (r) {
                onSuccess(r.tabs, r.facetObjStream, r.alreadyUsedFacetFilterStream);
            });
    }
    ,

    createTabStream: function (searchResult) {
        return Rx.Observable.from(searchResult.response.docs)
            .map(doc => doc.maintitle)
            .filter(title => title != null && title.length > 0)
            .map(title => title[0].toUpperCase())
            .map(char => (char.charCodeAt(0) < 65 || char.charCodeAt(0) > 90) ? "#" : char)
            .distinct()
            .reduce(function (tabs, char) {
                tabs.push(char);
                return tabs;
            }, []);
    }
    ,

    getFacetFilterFrom: function (searchResult) {
        return Rx.Observable.of(searchResult)
            .map(r => r.responseHeader.params.q)
            .flatMap(q => q.split(' '))
            .filter(q => q.startsWith('+journalType'))
            .map(q => q.replace('+journalType:', ''))
            .map(q => q.replace(/"/g, ''))
            .map(id => ' %2BjournalType:"' + id + '"%20');
    }
    ,

    getJournals: function (/* string */ tabLetter, /* function */ onSuccess, facetParamStream) {
        var qry = '';
        if (tabLetter == '#') {
            qry = '-maintitle_sort:[a TO z] -maintitle_sort:z*';
        } else {
            qry = '%2Bmaintitle_sort:' + tabLetter.toLowerCase() + '*';
        }

        facetParamStream
            .distinct()
            .reduce((facetFilter, f) => facetFilter + f, '')
            .map(facetFilter => jp.az.getSearchURL() + facetFilter + ' ' + qry + jp.az.getFilterQuery())
            .do(url => console.log("getJournals: " + url))
            .flatMap(url => Rx.Observable.fromPromise($.getJSON(url)))
            .subscribe(searchResult => onSuccess(searchResult.response));
    }
    ,

    load: function () {
        jp.az.importCSS();
        jp.az.printTabNav();
        jp.az.printFilter();

        jp.az.updateTabsAndFacets(null);
        var tab = $(location).attr('hash').substring(1, 2).toUpperCase();
        tab = (tab == "" || tab == null) ? "A" : tab;
        jp.az.setTab(tab);
        jp.az.updateJournals();
    }
    ,

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
    }
    ,

    printTabNav: function () {
        var tabsHTML = "<li>#</li>";
        for (var i = 65; i <= 90; i++) {
            var char = String.fromCharCode(i);
            tabsHTML += "<li>" + char + "</li>";
        }
        $('#tabNav').append(tabsHTML);
    }
    ,

    printFilter: function () {
        var filterInput = document.querySelector('#atozFilter');
        var filterRemoveButton = document.querySelector('#atozFilterRemoveButton');
        var filterInputStream = Rx.Observable.of(filterInput);
        var filterInputEventStream = Rx.Observable.fromEvent(filterInput, 'input');
        var filterRemoveButtonClickStream = Rx.Observable.fromEvent(filterRemoveButton, 'click');
        var facetParamStream = $('#atozFilter').data('facetFilter')

        if(facetParamStream === undefined ){
            facetParamStream = Rx.Observable.empty();
        }

        filterInputEventStream.merge(filterRemoveButtonClickStream)
            .combineLatest(filterInputStream, function (event, input) {
                if (event instanceof MouseEvent) {
                    console.log("click remove");
                    input.value = '';
                }

                return input;
            })
            .map(input => input.value)
            .subscribe(function (inputVal) {
                var facetParamStream = $('#atozFilter').data('facetFilter')

                if(facetParamStream === undefined ){
                    facetParamStream = Rx.Observable.empty();
                }

                jp.az.updateFilterRemoveButtonCSS(inputVal);
                jp.az.updateTabsAndFacets(facetParamStream);
                jp.az.updateJournals(facetParamStream);
            });
    }
    ,

    updateFilterRemoveButtonCSS: function (inputVal) {
        var filterRemoveIcon = document.querySelector('#atozFilterRemoveIcon');
        var filterRemoveButton = document.querySelector('#atozFilterRemoveButton');
        if (inputVal === "") {
            filterRemoveButton.setAttribute("style", "cursor:default;");
            filterRemoveIcon.setAttribute("style", "visibility:hidden;");
        } else {
            filterRemoveButton.setAttribute("style", "cursor:pointer;");
            filterRemoveIcon.setAttribute("style", "visibility:visible;");
        }
    },

    setTab: function (/* string */ tab) {
        $(location).attr('hash', tab);
        $("#tabNav > li").removeClass("selected-tab");
        $("#tabNav > li:contains('" + tab + "')").addClass("selected-tab");
    }
    ,

    getTab: function () {
        return $("#tabNav > li.selected-tab").text();
    }
    ,

    updateTabsAndFacets: function (facetParam) {
        // update tabs and facets
        if (facetParam === null || facetParam === undefined) {
            facetParam = Rx.Observable.empty();
        }

        jp.az.getTabsAndFacets(function (activeTabs, facetObjStream, alreadyUsedFacetFilterStream) {
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

            $('#atozFilter').data('facetFilter', alreadyUsedFacetFilterStream);
            //jp.az.printFilter(alreadyUsedFacetFilterStream);
            jp.az.updateFacets(facetObjStream, alreadyUsedFacetFilterStream);
        }, facetParam);
    }
    ,

    updateJournals: function (facetParamStream) {
        if (facetParamStream === null || facetParamStream === undefined) {
            facetParamStream = Rx.Observable.empty();
        }

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
        }, facetParamStream);
    }
    ,

    printJournalEntry: function (resultListEntry, metadata) {
        var titleLink = $('<a/>').html(metadata.maintitle).attr('href', jp.baseURL + 'receive/' + metadata.id);
        var title = $('<h3 class="journal-title"/>').append(titleLink);
        resultListEntry.append(title);
        jp.az.printPublished(resultListEntry, metadata);
        jp.az.printPublisher(resultListEntry, metadata, "participant.mainPublisher", "Herausgeber");
        jp.az.printPublisher(resultListEntry, metadata, "participant.author", "Autor");
    }
    ,

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
    }
    ,

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
    }
    ,

    updateFilter: function () {
        if ($('#atozFilter').val() == '') {
            $('#atozFilterRemoveIcon').css("visibility", 'hidden');
        } else {
            $('#atozFilterRemoveIcon').css("visibility", 'visible');
        }
    }
    ,

    createFacetObj: function (journalType) {
        var categID = journalType[0];
        var count = journalType[1];
        var labelsUrl = jp.baseURL + 'rsc/facets/label/' + categID;
        let lookupTableUrl = jp.baseURL + 'rsc/facets/lookupTable';
        var lookupTableStream = Rx.Observable.fromPromise($.get(lookupTableUrl));

        return Rx.Observable.fromPromise($.get(labelsUrl))
            .combineLatest(lookupTableStream, function (label, lookup) {
                var rootID = categID.split(':')[0];
                var parent = lookup[rootID];
                console.log('Parent: ' + categID + ' # ' + parent);

                return {
                    "label": label,
                    "categID": categID,
                    "count": count,
                    "parent": parent
                };
            })

    }
    ,

    createFacetListEntry: function (journalType) {
        var button = document.createElement('button');
        button.dataset.categID = journalType.categID;
        button.dataset.parent = journalType.parent;
        button.className = "facetButton";
        button.textContent = journalType.label + " (" + journalType.count + ")";
        var li = document.createElement('li');
        li.appendChild(button);
        li.appendChild(button);
        return li;
    }
    ,

    renderFacetList: function (fragment, container) {
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
    ,

    createFacetObjStream: function (searchResult) {
        var journalType = searchResult.facet_counts.facet_fields.journalType;
        return Rx.Observable.from(journalType)
            .bufferWithCount(2)
            .filter(journalType => journalType[1] > 0)
            .flatMap(journalType => jp.az.createFacetObj(journalType));
    }
    ,

    createFacetButtonClickStream: function (facetObjStream, container) {
        return facetObjStream
            .map(jp.az.createFacetListEntry)
            .reduce(function (f, li) {
                f.appendChild(li);
                return f;
            }, document.createDocumentFragment())
            .flatMap(fragment => jp.az.renderFacetList(fragment, container))
            .map(li => li.querySelector('.facetButton'))
            .flatMap(b => Rx.Observable.fromEvent(b, 'click'))
            .map(click => click.target.dataset.categID)
            .map(id => ' %2BjournalType:"' + id + '"%20');
    }
    ,

    updateFacets: function (facetObjStream, alreadyUsedFacetFilterStream) {
        var facetsContainer = document.querySelector('#atozFacets');
        jp.az.createFacetButtonClickStream(facetObjStream, facetsContainer)
            .subscribe(function (facetParam) {
                var param = alreadyUsedFacetFilterStream.concat(Rx.Observable.of(facetParam))
                jp.az.updateTabsAndFacets(param);
                jp.az.updateJournals(param);
            });
    }
    ,

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