var jp = jp || {};

(function($) {
    $.fn.jpResultList = function(searchURL, fill) {
        var resultList = $('<ul/>');
        var resultListContainer = $(this);

        $.getJSON(searchURL, function(searchResult) {
        	var response = searchResult.response;
            for (var i = 0; i < response.numFound; i++) {
                var resultListEntry = $('<li/>');
                fill(resultListEntry, response.docs[i]);
                resultList.append(resultListEntry);
            }

            if (response.numFound == 0) {
                resultList.html('<span class="ui-msg">Keine Einträge unter dieser Katgorie.</span>')
            }

            resultListContainer.empty().append(resultList);
        });
    }
})(jQuery);

jp.az = {

	load: function() {
	    $.getJSON(jp.az.getSearchURL() + '&fl=maintitle', function(searchResult) {
	    	var azList = jp.az.getList(searchResult.response);
	    	jp.az.print(azList);
	    });
	},

	getHost: function() {
		return 'http://' + $(location).attr('host');
	},

	getSearchURL: function() {
		var url = jp.az.getHost() + '/servlets/solr/select?wt=json&sort=maintitle_lowercase asc&rows=9999&q=';
		var additionalQuery = $('#firstLetterTab').attr('additionalQuery');
		return url + additionalQuery;
	},

	getList: function(response) {
		var az = [];
		for(var i = 0; i < response.numFound; i++) {
			var maintitle = response.docs[i].maintitle;
			if(maintitle != null && maintitle.length > 0) {
				var char = maintitle[0].toUpperCase();
				char = (char.charCodeAt(0) < 65 || char.charCodeAt(0) > 90) ? "#" : char;
				if($.inArray(char, az) == -1) {
					az.push(char);
				}
			}
		}
		return az;
	},

	onTabClick: function() {
		$('.tab-nav li.selected-tab').toggleClass('default-tab selected-tab');
		$(this).toggleClass('default-tab selected-tab');

	    var selectedChar = $(this).html();
	    $(location).attr('hash', selectedChar);
	    var searchQuery = '';
	    if (selectedChar == '#') {
	        searchQuery = '-maintitle_lowercase:[a TO z] -maintitle_lowercase:z*';
	    } else {
	        searchQuery = '%2Bmaintitle_lowercase:' + selectedChar.toLowerCase() + '*';
	    }
	    $('#resultList').jpResultList(jp.az.getSearchURL() + ' ' + searchQuery, jp.az.printJournalEntry);
	},

	print: function(azList) {
		var charListHTML = "";
	    for (var i = 0; i < azList.length; i++) {
	    	charListHTML += "<li class='default-tab'>" + azList[i] + "</li>";
	    }
	    var tabNav = $('#tabNav');
	    tabNav.append(charListHTML);

	    $('.tab-nav').delegate('li', 'click', jp.az.onTabClick);

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

	    $('div.headline:contains("Blättern A - Z")').remove();

	    var hash = $(location).attr('hash').substring(1).toUpperCase();
	    if(hash.length == 1) {
	    	$("#tabNav li:contains('" + hash + "')").click();
	    } else {
	    	$('#tabNav li:first-child').click();
	    }
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