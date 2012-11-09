/**
 * 
 */
(function($) {
    $.fn.jpResultList = function(searchURL, fill) {
        var resultList = $('<ul/>');
        var resultListContainer = $(this);

        $.getJSON(searchURL, function(searchResult) {
        	var response = searchResult.response;
            for (i = 0; i < response.numFound; i++) {
                var resultListEntry = $('<li/>');
                fill(resultListEntry, response.docs[i]);
                resultList.append(resultListEntry);
            }

            if (response.numFound == 0) {
                resultList.html('<span class="ui-msg">Keine Einträge unter dieser Katgorie.</span>')
            }

            resultListContainer.empty().append(resultList);
        })
    }
})(jQuery);

$(document).ready(function() {
    var tabNav = $('#tabNav');
    var charListHTML = "";
    for ( var charCode = 65; charCode <= 90; charCode++) {
    	charListHTML += "<li class='default-tab'>" + String.fromCharCode(charCode) + "</li>";
    }
    charListHTML += "<li class='default-tab'>#</li>"
    tabNav.append(charListHTML);

    var additionalQuery = $('#firstLetterTab').attr('additionalQuery');

    $('.tab-nav').delegate('li', 'click', function() {
        $('.tab-nav li.selected-tab').toggleClass('default-tab selected-tab');
        $(this).toggleClass('default-tab selected-tab');

        var host = 'http://' + $(location).attr('host');
        var selectedChar = $(this).html();
        $(location).attr('hash', selectedChar);

        var searchQuery = '';
        if (selectedChar == '#') {
            for (var charCode = 48; charCode <= 57; charCode++) {
                searchQuery = searchQuery + ' %2Bmaintitle_lowercase:' + String.fromCharCode(charCode) + '*';
            }
        } else {
            searchQuery = '%2Bmaintitle_lowercase:' + selectedChar.toLowerCase() + '*'
        }

        if (additionalQuery != '') {
            searchQuery = searchQuery + ' ' + additionalQuery;
        }
        var searchUrl = host + '/rsc/search?s=maintitle_lowercase&q=' + searchQuery;

        $('#resultList').jpResultList(searchUrl, function(resultListEntry, metadata) {
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
            if (metadata.publisher && metadata.publisherID) {
                var publisherLink = $('<a/>').html(metadata.publisher).attr('href', host + '/receive/' + metadata.publisherID);
                var publisher = $('<div class="journal-author"/>').html('Herausgeber: ').append(publisherLink);
                resultListEntry.append(publisher);
            }
        });
    });

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
})