/**
 * 
 */
$(document).ready(function(){
    $("#dialog-form").dialog({
        resizable: false,
        height: 204,
        width: 222,
        modal: true,
        buttons : {
            "Anmelden" : function(){
                var userID = $('#login').val();
                var password = $('#password').val();
                console.log('user: ' + userID + ' pass: ' + password);
                $.ajax({
                    type :  'POST',
                    url : '/rsc/login',
                    data : '{userID: ' + userID+', password:'+password+'}',
                    contentType: "application/json; charset=utf-8",
                    success : function(data){
                        console.log('data: ' + data);
                        history.back();
                    }
                })
            },
            "Abbrechen" : function(){
                
            }
        }
    })
})