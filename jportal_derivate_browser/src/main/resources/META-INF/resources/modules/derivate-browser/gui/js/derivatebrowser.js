var DerivateBrowser = function () {

    return {
        init: function () {
            derivateBrowserNavigation.init();
            derivateBrowserDocumentView.init();
            derivateBrowserDerivateView.init();
            derivateBrowserLargeView.init();
            derivateBrowserMoveAndLink.init();
            derivateBrowserUpload.init();
            derivateBrowserTools.init("full");
        }
    };
    

};

$(document).ready(function() {
    if($("#filebrowser").length > 0) {
        derivateBrowserInstance = new DerivateBrowser();
        derivateBrowserInstance.init();
    }
});