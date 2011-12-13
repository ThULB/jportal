/**
 * 
 */
$(document).ready(function() {
    console.log('Ready!');
    $('#editor').ckeditor({resize_enabled : false}).val('Mein Text').dialog();
    $('#button').button().click(function(){
        var content = $('#editor').val();
        console.log('Text: ' + content);
    });
})