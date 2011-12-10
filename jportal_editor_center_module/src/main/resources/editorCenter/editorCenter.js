/**
 * 
 */

$(document).ready(function(){
//    $('#toolbar > div').resizable();
    $('#tabs').tabs();
    $('#tabs, .ui-tabs-nav').removeClass('ui-corner-all');
    $('#tabs').addClass('ui-corner-bottom');
    $('#removePerson').button().click(function(){
        $.get('/servlets/MCRWebCLIServlet?request=getKnownCommands', function(data){
            console.log('Data: ' + data);
        }).error(function(err){
            console.log("Error: " + err);
        });
        $.get('/servlets/MCRWebCLIServlet?run=jp clean up person', function(data){
            console.log('remove person');
        });
    })
})