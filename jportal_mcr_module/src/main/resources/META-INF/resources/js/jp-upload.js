$(document).ready(function() {
    if ($('#lightbox-upload').length > 0 && derivateBrowserUpload != undefined) {
        $.get(jp.baseURL + "rsc/derivatebrowser/gui/derivatebrowser.html", function (template) {
            $("#main").append($(template).filter("#upload-entry-template3"));
            derivateBrowserTools.initStandalone();
            derivateBrowserUpload.initStandalone($('#lightbox-upload'), jp.baseURL);
        });

        $('#jp-upload-new-button').on('click', function (evt) {
            evt.preventDefault();
            $("body").trigger("openUploadModal");
        });

        $("body").on("derivateCreated", function (event, deriID) {
            window.location.href = jp.baseURL + "rsc/derivatebrowser/compact#/" + deriID + "/";
        });
    }
});