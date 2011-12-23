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
        $('#progressbar').progressbar({value: 0});
        nonResizeableDiag('#errMsg');
        nonResizeableDiag('#progress').parent().find('a.ui-dialog-titlebar-close').hide();
    }
    
    var TabController = function(type){
        var id = '#' + type;
        var counter = $(id).find('.counter');
        var removeButton = $(id).find('.removeButton');
        var errMsgDiag = $('#errMsg');
        var progressDiag = $('#progress');
        var progressBar = $('#progressbar');
        var remainderDispl = $('#remainder');
        var numOfDuplicates = 0;
        
        function updateCounter(){
            $.getJSON('/rsc/editorCenter/numDoubletsOf/' + type, function(data){
                numOfDuplicates = data.num;
                counter.html(numOfDuplicates);
                if(parseInt(data.num) > 0){
                    removeButton.button('enable');
                } else{
                    removeButton.button('disable');
                }
            })
        }
        
        function updateProgressBar(remainder){
            remainderDispl.html(remainder);
            if(remainder == 0){
                progressBar.progressbar({value : 100});
            } else {
                var percent = (numOfDuplicates / remainder) * 100;
                progressBar.progressbar({value : percent});
            }
        }
        
        var proc = 0;
        var remainder = 0;
        function processing(){
            $.get('/servlets/MCRWebCLIServlet?request=getCommandQueue', function(data){
                var tmpRemainder = data.commandQueue.length;
                
                if(tmpRemainder < remainder){
                    remainder = tmpRemainder;
                }
            });
            
            if(proc == 0){
                $.ajax({ 
                    type :  'GET',
                    url: '/servlets/MCRWebCLIServlet?request=getStatus', 
                    success: function(data){
                        updateProgressBar(remainder);
                        if(data.running == false){
                            setTimeout(function(){
                                updateCounter();
                                closeProgressDiag();
                            }, 2000);
                            proc = 100;
                        }
                    }, 
                    complete: processing, 
                    timeout: 2000
                });
            } else {
                proc = 0;
            }
        }
        
        function openProgressDiag(){
            remainderDispl.html(numOfDuplicates);
            progressDiag.dialog('open');
        }
        
        function closeProgressDiag(){
            remainderDispl.html(0);
            progressDiag.dialog('close');
        }
        
        function removeDuplicate(){
            $.get('/servlets/MCRWebCLIServlet?run=jp clean up ' + type, function(data){
                openProgressDiag();
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