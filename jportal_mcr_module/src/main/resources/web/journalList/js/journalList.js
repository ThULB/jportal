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

$(document).ready(function() {
    $.getJSON(jp.az.getSearchURL() + '&fl=maintitle', function(searchResult) {
    	var azList = jp.az.getList(searchResult.response);
    	jp.az.print(azList);
    });
});

jp.az = {

	getHost: function() {
		return 'http://' + $(location).attr('host');
	},

	getSearchURL: function() {
		var url = jp.az.getHost() + '/rsc/search?wt=json&sort=maintitle_lowercase asc&rows=9999&q=';
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
	        document.createStyleSheet('/journalList/css/journalList.css');
	    } else {
	        var link = $('<link>').attr({
	            type : 'text/css',
	            rel : 'stylesheet',
	            href : '/journalList/css/journalList.css',
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
	
	    var publishedStr = 'Erscheinungsverlauf: ';
	    if(metadata["date.published"]) {
	    	publishedStr += metadata["date.published"];
	        resultListEntry.append($('<div class="journal-published"/>').html(publishedStr));
	    } else if(metadata["date.published_from"]) {
	    	publishedStr += metadata["date.published_from"] + ' - ';
	        if (metadata["date.published_until"]) {
	            publishedStr = publishedStr + metadata["date.published_until"];
	        }
	        resultListEntry.append($('<div class="journal-published"/>').html(publishedStr));
	    }
	    if (metadata.publisher) {
	    	for(var i = 0; i < metadata.publisher.length; i++) {
	    		if(metadata.publisher[i].indexOf('#') == -1) {
	    			console.log("Invalid publisher format for '" + metadata.publisher[i] + "'.");
	    			continue;
	    		}
	    		var publisherID = metadata.publisher[i].substring(0, metadata.publisher[i].indexOf('#'));
	    		var publisherText = metadata.publisher[i].substring(metadata.publisher[i].indexOf('#') + 1);
	            var publisherLink = $('<a/>').html(publisherText).attr('href', jp.az.getHost() + '/receive/' + publisherID);
	            var publisher = $('<div class="journal-author"/>').html('Herausgeber: ').append(publisherLink);
	            resultListEntry.append(publisher);
	    	}
	    }
	},
}