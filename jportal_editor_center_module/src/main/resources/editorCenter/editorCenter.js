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
    
    function initView(){
        var tabs = $('#tabs').tabs();
        
        $('#tabs, .ui-tabs-nav').removeClass('ui-corner-all');
        tabs.addClass('ui-corner-bottom');
        tabs.find('.removeButton').button({'disabled': true});
        nonResizeableDiag('#errMsg');
        nonResizeableDiag('#progress').parent().find('a.ui-dialog-titlebar-close').hide();
    }
    
    var TabController = function(type){
        var id = '#' + type;
        var counter = $(id).find('.counter');
        var removeButton = $(id).find('.removeButton');
        var errMsgDiag = $('#errMsg');
        var progressDiag = $('#progress');
        
        function updateCounter(){
            $.getJSON('/rsc/editorCenter/numDoubletsOf/' + type, function(data){
                counter.html(data.num);
                if(parseInt(data.num) > 0){
                    removeButton.button('enable');
                } else{
                    removeButton.button('disable');
                }
            })
        }
        
        var proc = 0;
        function processing(){
            if(proc == 0){
                $.ajax({ 
                    type :  'GET',
                    url: '/servlets/MCRWebCLIServlet?request=getStatus', 
                    success: function(data){
                        if(data.running == false){
                            setTimeout(function(){
                                $('#progress').dialog('close');
                                updateCounter();
                            }, 2000);
                            proc = 100;
                        }
                    }, 
                    complete: processing, 
                    timeout: 1000
                });
            } else {
                proc = 0;
            }
        }
        
        
        function removeDuplicate(){
            $.get('/servlets/MCRWebCLIServlet?run=jp clean up ' + type, function(data){
                progressDiag.dialog('open');
                processing();
            });
        }
        
        removeButton.click(function(){
            $.get('/servlets/MCRWebCLIServlet?request=getKnownCommands', function(data){
                removeDuplicate();
            }).error(function(err){
                if(err.status == 403){
                    errMsgDiag.dialog('open');
                }
            });
        })
        
        updateCounter();
    }
    
    initView()
    new TabController('person');
    new TabController('jpinst');
})