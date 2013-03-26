/**
 * 
 */

$(document).ready(function(){
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
        var authFailDiag = $('#errMsg');
        var progressDiag = $('#progress');
        var progressBar = $('#progressbar');
        var remainderDispl = $('#remainder');
        var numOfDuplicates = 0;
        var controller = $(this);
        var processRunning = false;
        
        function initCounter(){
            $.getJSON('/rsc/editorCenter/numDoubletsOf/' + type, function(data){
                numOfDuplicates = data.num;
                if(parseInt(data.num) > 0){
                    var linkToDuplicateList = $('<a/>').html(numOfDuplicates);
                    linkToDuplicateList.attr('href', '/servlets/MCRSearchServlet?query=(doubletOf like "*") AND (objectType = "' + type + '")&maxResults=0&numPerPage=10');
                    counter.html(linkToDuplicateList);
                    removeButton.button('enable');
                } else{
                    counter.html(numOfDuplicates);
                    removeButton.button('disable');
                }
            })
        }
        
        function checkAuthorization(){
            $.get('/servlets/MCRWebCLIServlet?request=getKnownCommands', function(data){
                controller.trigger('startRemoveDuplicate');
            }).error(function(err){
                if(err.status == 403){
                    authFailDiag.dialog('open');
                }
            });
        }
        
        function removeDuplicate(){
            $.get('/servlets/MCRWebCLIServlet?run=jp clean up ' + type, function(data){
                processRunning = true;
                controller.trigger('startPollingStatus');
                controller.trigger('openProgressDiag');
                controller.trigger('updateProgressDiag');
            }).error(function(err){
                console.log('removeDuplicate failed with Error Code ' + err.status);
            });
        }
        
        function pollingStatus(){
            if(processRunning == true){
                $.ajax({ 
                    type :  'GET',
                    url: '/servlets/MCRWebCLIServlet?request=getStatus', 
                    success: function(data){
                        processRunning = data.running;
                    }, 
                    complete: pollingStatus, 
                    timeout: 2000
                });
            } 
        }
        
        function updateProgressDiag(){
            if(processRunning == true){
                $.ajax({ 
                    type :  'GET',
                    url: '/servlets/MCRWebCLIServlet?request=getCommandQueue', 
                    success: function(data){
                        var oldRemainder = remainderDispl.html();
                        var newRemainder = data.commandQueue.length;
                        
                        if(((oldRemainder - newRemainder) != oldRemainder) && (newRemainder < oldRemainder)){
                            remainderDispl.html(newRemainder);
                            var percent = (1 - (newRemainder / numOfDuplicates)) * 100;
                            progressBar.progressbar({value : percent});
                        }
                    }, 
                    complete: updateProgressDiag, 
                    timeout: 2000
                });
            } else {
                setTimeout(function(){
                    initCounter();
                    controller.trigger('closeProgressDiag');
                }, 2000);
            }
        }
        
        function openProgressDiag(){
            remainderDispl.html(numOfDuplicates);
            progressBar.progressbar({value : 0});
            progressDiag.dialog('open')
        }
        
        (function initController($){
            console.log("Init controller for type " + type);
            initCounter();
            
            controller.bind('initRemoveDuplicate', checkAuthorization);
            controller.bind('startRemoveDuplicate', removeDuplicate);
            controller.bind('startPollingStatus', pollingStatus);
            controller.bind('updateProgressDiag', updateProgressDiag);
            controller.bind('openProgressDiag', openProgressDiag);
            controller.bind('closeProgressDiag', function(){progressDiag.dialog('close')});
            
            removeButton.click(function(){
                controller.trigger('initRemoveDuplicate')
            });
        }(jQuery))
    }
    
    initView()
    new TabController('person');
    new TabController('jpinst');
})