/**
 * 
 */
(function($) {
    $.fn.jpResultList = function(searchURL, fill) {
        var resultList = $('<ul class="resultList"/>');
        var resultListContainer = $(this);

        $.getJSON(searchURL, function(searchResults) {
            for (i = 0; i < searchResults.numHits; i++) {
                var resultListEntry = $('<li class="entry"/>');
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
    console.log('template_master2');
    var resultList = $('#latestArticles');
    console.log('resultList: ' + resultList);
    if (resultList) {

        var host = 'http://' + $(location).attr('host');
        var searchNewestArticleURL = host + '/rsc/search?s=created&m=3&o=descending&q=(objectType = jparticle)';

        $.getJSON(searchNewestArticleURL, function(searchResults) {

            for (i = 0; i < searchResults.numHits; i++) {
                var resultListEntry = $('<li/>');
                resultList.append(resultListEntry);
                var metaData = searchResults.hits[i].metaData;

                var titleLink = $('<a/>').html(metaData.maintitles).attr('href', host + '/receive/' + searchResults.hits[i].id);
                var title = $('<h3 class="journal-title"/>').append(titleLink);
                resultListEntry.append(title);
                var publishedIn = $('<div class="publishedIn"/>').html('In: ' + metaData.journalTitle);
                resultListEntry.append(publishedIn);

                if (metaData.sizes_art) {
                    var sizes = $('<div class="journal-published"/>').html('Seitenbereich: ' + metaData.sizes_art);
                    resultListEntry.append(sizes);
                }
            }
        })
    }

    var resultListEntryObj = {
        title : function(title, href){
            return $('<h3 class="title"><a href="' + href + '">' + title + '</a></h3>');
        },
        
        jpjournal : function(resultListEntry, hit) {
            var metaData = hit.metaData;
           
            resultListEntry.append(this.title(metaData.maintitles, host + metaData.webcontext));

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
        },

        jparticle : function(resultListEntry, hit) {
            var metaData = hit.metaData;

            resultListEntry.append(this.title(metaData.maintitles, host + '/receive/' + hit.id));
            var publishedIn = $('<div class="publishedIn"/>').html('In: ' + metaData.journalTitle);
            resultListEntry.append(publishedIn);

            if (metaData.sizes_art) {
                var sizes = $('<div class="journal-published"/>').html('Seitenbereich: ' + metaData.sizes_art);
                resultListEntry.append(sizes);
            }
        },
        
        jpvolume : function(resultListEntry, hit) {
            this.jparticle(resultListEntry, hit);
        }
    }

    function search(searchTerm){
        var searchURL = host + '/rsc/search/all?s=maintitles&q=' + searchTerm;
        console.log('send to search');
        $('#content_area').jpResultList(searchURL, function(listEntry, hit) {

            if (typeof(resultListEntryObj[hit.metaData.objectType]) == 'function') {
                resultListEntryObj[hit.metaData.objectType](listEntry, hit);
            } else {
                var titleLink = $('<a/>').html(hit.metaData.maintitles).attr('href', host + '/receive/' + hit.id);
                var title = $('<h3 class="journal-title"/>').append(titleLink);
                listEntry.append(title);
            }
        })
        $(location).attr('hash', searchTerm);
    }
    
    $('#search_input').keypress(function(pressed) {
        if (pressed.keyCode == 13) {
            search($('#search_input').val());
        }
    })
    
    
    $(window).bind('hashchange', function(){
        var hash = $(location).attr('hash').replace('#','');
        console.log('Hash changed: ' + hash);
        
        if(hash == ''){
            location.reload();
        }else{
            search(hash);
        }
    })
})