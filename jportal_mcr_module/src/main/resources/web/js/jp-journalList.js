var jp = jp || {};

jp.az = {

	getHost: function() {
		return 'http://' + $(location).attr('host');
	},

	getSearchURL: function() {
		var url = jp.az.getHost() + '/servlets/solr/select?wt=json&sort=maintitle_lowercase asc&rows=9999&q=';
		var additionalQuery = $('#firstLetterTab').attr('additionalQuery');
		return url + additionalQuery;
	},

	getFilterQuery: function() {
		var filter = $("#atozFilter").val().toLowerCase();;
		return filter != "" ? "&fq=maintitle_lowercase:*" + filter + "*" : "";
	},

	getTabs: function(/*function*/ onSuccess) {
		$.getJSON(jp.az.getSearchURL() + '&fl=maintitle' + jp.az.getFilterQuery(), function(searchResult) {
			var response = searchResult.response;
			var tabs = [];
			for(var i = 0; i < response.numFound; i++) {
				var maintitle = response.docs[i].maintitle;
				if(maintitle != null && maintitle.length > 0) {
					var char = maintitle[0].toUpperCase();
					char = (char.charCodeAt(0) < 65 || char.charCodeAt(0) > 90) ? "#" : char;
					if(tabs.indexOf(char) == -1) {
						tabs.push(char);
					}
				}
			}
			onSuccess(tabs);
		});
	},

	getJournals: function(/*string*/ tabLetter, /*function*/ onSuccess) {
	    var qry = '';
	    if (tabLetter == '#') {
	    	qry = '-maintitle_lowercase:[a TO z] -maintitle_lowercase:z*';
	    } else {
	    	qry = '%2Bmaintitle_lowercase:' + tabLetter.toLowerCase() + '*';
	    }
		$.getJSON(jp.az.getSearchURL() + ' ' + qry + jp.az.getFilterQuery(), function(searchResult) {
			onSuccess(searchResult.response);
		});
	},

	load: function() {
		jp.az.importCSS();
		jp.az.printTabNav();
		jp.az.printFilter();

		jp.az.updateTabs(null);
		var tab = $(location).attr('hash').substring(1, 2).toUpperCase();
		tab = (tab == "" || tab == null) ? "A" : tab; 
		jp.az.setTab(tab);
		jp.az.updateJournals();
	},

	importCSS: function() {
	    if (document.createStyleSheet) {
	        document.createStyleSheet('/journalList/css/jp-journalList.css');
	    } else {
	        var link = $('<link>').attr({
	            type : 'text/css',
	            rel : 'stylesheet',
	            href : '/css/jp-journalList.css',
	            'class' : 'myStyle'
	        });
	        $('head').append(link);
	    }
	},

	printTabNav: function() {
		var tabsHTML = "<li>#</li>";
		for(var i = 65; i <= 90; i++) {
			var char = String.fromCharCode(i);
			tabsHTML += "<li>" + char + "</li>";
		}
		$('#tabNav').append(tabsHTML);
	},

	printFilter: function() {
		var filter = $("#atozFilter");
		filter.on("change", function() {
			jp.az.updateTabs();
			jp.az.updateJournals();
		});
	},

	setTab: function(/*string*/ tab) {
		$(location).attr('hash', tab);
		$("#tabNav > li").removeClass("selected-tab");
		$("#tabNav > li:contains('" + tab + "')").addClass("selected-tab");
	},

	getTab: function() {
		return $("#tabNav > li.selected-tab").text();
	},

	updateTabs: function() {
		jp.az.getTabs(function(activeTabs) {
			$("#tabNav > li").each(function() {
				var li = $(this);
				li.off("click");
				if(activeTabs.indexOf(li.text()) >= 0) {
					li.addClass('active');
					li.on("click", function() {
						jp.az.setTab($(this).text());
						jp.az.updateJournals();
					});
				} else {
					li.removeClass('active');
				}
			});
		});
	},

	updateJournals: function() {
		var tab = jp.az.getTab();
		jp.az.getJournals(tab, function(response) {
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

	printJournalEntry: function(resultListEntry, metadata) {
	    var titleLink = $('<a/>').html(metadata.maintitle).attr('href', '/receive/' + metadata.id);
	    var title = $('<h3 class="journal-title"/>').append(titleLink);
	    resultListEntry.append(title);
	    jp.az.printPublished(resultListEntry, metadata);
	    jp.az.printPublisher(resultListEntry, metadata);
	},

	printPublished: function(node, journal) {
	    var publishedStr = 'Erscheinungsverlauf: ';
	    if(journal["date.published"]) {
	    	publishedStr += journal["date.published"];
	    	node.append($('<div class="journal-published"/>').html(publishedStr));
	    } else if(journal["date.published_from"]) {
	    	publishedStr += journal["date.published_from"] + ' - ';
	        if (journal["date.published_until"]) {
	            publishedStr = publishedStr + journal["date.published_until"];
	        }
	        node.append($('<div class="journal-published"/>').html(publishedStr));
	    }
	},

	printPublisher: function(node, journal) {
		var publisherList = journal.publisher;
		if(publisherList) {
			var pusblisherStr = '<div class="publisher">Herausgeber: ';
	    	for(var i = 0; i < publisherList.length; i++) {
	    		var publisher = publisherList[i];
	    		if(publisher.indexOf('#') == -1) {
	    			console.log("Invalid publisher format for '" + publisher + "'.");
	    			continue;
	    		}
	    		var publisherID = publisher.substring(0, publisher.indexOf('#'));
	    		var publisherText = publisher.substring(publisher.indexOf('#') + 1);
	            var publisherLink = "<a href='" + jp.az.getHost() + '/receive/' + publisherID + "'>" + publisherText + "</a>";
	            pusblisherStr += publisherLink;
	            if(i + 1 < publisherList.length) {
	            	pusblisherStr += "; ";
	            }
	    	}
	    	node.append(pusblisherStr + "</div>");
		}
	}

}