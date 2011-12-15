/**
 * 
 */

$(document).ready(function(){
//    $('#toolbar > div').resizable();
    function nonResizeableDiag(id){
        return $(id).dialog({ 
            modal: true,
            autoOpen: false,
            resizable: false
        });
    }
    
    function removeDuplicateForType(type){
        $('#progress').dialog('open');
        $.get('/servlets/MCRWebCLIServlet?run=jp clean up ' + type, function(data){
            console.log('remove person');
            $('#progress').dialog('close');
        });
    }
    
    $('#tabs').tabs();
    $('#tabs, .ui-tabs-nav').removeClass('ui-corner-all');
    $('#tabs').addClass('ui-corner-bottom');
    $('#removePerson').button({'disabled': true});
    
    nonResizeableDiag('#errMsg');
    nonResizeableDiag('#progress').parent().find('a.ui-dialog-titlebar-close').hide();
    
    $.getJSON('/rsc/editorCenter/numDoubletsOf/person', function(data){
        if(parseInt(data.num) > 0){
            $('#removePerson').button('enable');
            
            $('#numDuplicatesPers').html(data.num);
            
            $('#removePerson').click(function(){
                $.get('/servlets/MCRWebCLIServlet?request=getKnownCommands', function(data){
                    console.log('Data: ' + data);
                    removeDuplicateForType('person');
                }).error(function(err){
                    if(err.status == 403){
                        $('#errMsg').dialog('open');
                    }
                });
            })
        }
    })
})