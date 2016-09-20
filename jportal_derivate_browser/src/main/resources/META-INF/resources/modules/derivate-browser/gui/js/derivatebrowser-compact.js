$(document).ready(function() {
    $.get('gui/derivatebrowser.html', function (template) {
        $("#main").append($(template).filter("#filebrowser"));
        $("#main").find("#folder-list-div").remove();
        $("#main").append($(template).filter("#lightbox-multi-move"));
        $("#main").append($(template).filter("#lightbox-alert-delete"));
        $("#main").append($(template).filter("#lightbox-alert-deleteDoc"));
        $("#main").append($(template).filter("#lightbox-upload-overwrite"));
        $("#main").append($(template).filter("#lightbox-new-derivate"));
        $("#main").append($(template).filter("#popover-template"));
        $("#main").append($(template).filter("#file-entry-template"));
        $("#main").append($(template).filter("#xml-entry-template"));
        $("#main").append($(template).filter("#folder-entry-template"));
        $("#main").append($(template).filter("#target-folder-entry-template"));
        $("#main").append($(template).filter("#upload-entry-template"));
        $("#main").append($(template).filter("#upload-overwrite-template"));
        $("#main").append($(template).filter("#large-view-status-template"));
        $("#main").append($(template).filter("#upload-entry-template3"));

        derivateBrowserDerivateView.init();
        derivateBrowserLargeView.init();
        derivateBrowserMoveAndLink.init();
        derivateBrowserUpload.init();
        derivateBrowserTools.init("compact");
    });
});