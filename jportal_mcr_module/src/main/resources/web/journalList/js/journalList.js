/**
 * 
 */
(function($) {
    $.fn.jpResultList = function(searchURL, fill) {
        var resultList = $('<ul/>');
        var resultListContainer = $(this);

        $.getJSON(searchURL, function(searchResults) {
            for (i = 0; i < searchResults.numHits; i++) {
                var resultListEntry = $('<li/>');
                fill(resultListEntry, searchResults.hits[i]);
                resultList.append(resultListEntry);
            }

            if (searchResults.numHits == 0) {
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
            for ( var charCode = 48; charCode <= 57; charCode++) {
                searchQuery = searchQuery + '(maintitles_plain like "' + String.fromCharCode(charCode) + '*")';

                if (charCode < 57) {
                    searchQuery = searchQuery + " or "
                }
            }
        } else {
            searchQuery = 'maintitles_plain like "' + selectedChar.toLowerCase() + '*"'
        }

        if (additionalQuery != '') {
            searchQuery = '(' + searchQuery + ') ' + additionalQuery;
        }
        var searchUrl = host + '/rsc/search?s=maintitles_plain&q=' + searchQuery;

        $('#resultList').jpResultList(searchUrl, function(resultListEntry, hit) {
            var metaData = hit.metaData;

            var titleLink = $('<a/>').html(metaData.maintitles).attr('href', '/receive/' + hit.id);
            var title = $('<h3 class="journal-title"/>').append(titleLink);
            resultListEntry.append(title);

            if (metaData.published_from) {
                var publishedStr = 'Erscheinungsverlauf: ' + metaData.published_from + ' - ';
                if (metaData.published_until) {
                    publishedStr = publishedStr + metaData.published_until;
                }
                var published = $('<div class="journal-published"/>').html(publishedStr);

                resultListEntry.append(published);
            }

            if (metaData.publisher && metaData.publisherID) {
                var publisherLink = $('<a/>').html(metaData.publisher).attr('href', host + '/receive/' + metaData.publisherID);
                var publisher = $('<div class="journal-author"/>').html('Herausgeber: ').append(publisherLink);
                resultListEntry.append(publisher);
            }
        });
    })

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