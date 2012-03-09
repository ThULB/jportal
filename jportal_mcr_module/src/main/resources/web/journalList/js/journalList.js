/**
 * 
 */
$(document).ready(function(){
    var tabNav = $('#tabNav');
    for ( var charCode = 65; charCode <= 90; charCode++) {
        tabNav.append($('<li class="default-tab"/>').html(String.fromCharCode(charCode)));
    }
    tabNav.append($('<li class="default-tab"/>').html('#'));
    
    var additionalQuery = $('#firstLetterTab').attr('additionalQuery');
    
    $('.tab-nav').delegate('li', 'click', function(){
        $('.tab-nav li.selected-tab').toggleClass('default-tab selected-tab');
        $(this).toggleClass('default-tab selected-tab');
        
        var host = 'http://' + $(location).attr('host');
        var searchUrl =  host + '/rsc/search?sortBy=maintitles_plain';
        var selectedChar = $(this).html();
        
        var searchQuery = '';
        if(selectedChar == '#'){
            for ( var charCode = 48; charCode <= 57; charCode++) {
                searchQuery = searchQuery + '(maintitles_plain like "' + String.fromCharCode(charCode) +'*")';
                
                if(charCode < 57){
                    searchQuery = searchQuery + " or "
                }
            }
        } else{
            searchQuery = 'maintitles_plain like "' + selectedChar +'*"'
        }
        
        if(additionalQuery != ''){
            searchQuery = '(' + searchQuery + ') ' + additionalQuery;
        }
        
        $.post(searchUrl,searchQuery, function(searchResults){
            var resultList = $('<ul/>');
            
            for(i=0; i < searchResults.numHits; i++){
                var metaData = searchResults.hits[i].metaData;
                var resultListEntry = $('<li/>');
                console.log('Title: ' + searchResults.hits[i].metaData.maintitles_plain);
                
                var titleLink = $('<a/>').html(metaData.maintitles_plain).attr('href', host + metaData.webcontext);
                var title = $('<h3 class="journal-title"/>').append(titleLink);
                resultListEntry.append(title);
                
                if(metaData.published_from){
                    var publishedStr = 'Erscheinungsverlauf: ' + metaData.published_from + ' - ';
                    if(metaData.published_until){
                        publishedStr = publishedStr + metaData.published_until;
                    }
                    var published = $('<div class="journal-published"/>').html(publishedStr);
                    
                    resultListEntry.append(published);
                }
                
                if(metaData.publisher && metaData.publisherID){
                    var publisherLink = $('<a/>').html(metaData.publisher).attr('href', host + '/receive/' + metaData.publisherID);
                    var publisher = $('<div class="journal-author"/>').html('Herausgeber: ').append(publisherLink);
                    resultListEntry.append(publisher);
                }
                
                
                resultList.append(resultListEntry);
            }
            
            if(searchResults.numHits == 0){
                resultList.html('<span class="ui-msg">Keine Einträge unter dieser Katgorie.</span>')
            }
            
            $('#resultList').empty().append(resultList);
        })
    })
    
    if (document.createStyleSheet){
        document.createStyleSheet('/journalList/css/journalList.css');
    }else{
        var link = $('<link>').attr({
            type: 'text/css',
            rel: 'stylesheet',
            href: '/journalList/css/journalList.css',
            'class': 'myStyle'
        });
        $('head').append(link);
    }
    
    $('div.headline:contains("Blättern A - Z")').remove();
    
    $('#tabNav li:first-child').click();
})