
function jpInitIview2() {
    jQuery('div.jp-layout-derivateWrapper .image').on({
    	mouseenter: function() {
    		jQuery(this).find('div.jp-layout-hidden-Button').show();
    	},
    	mouseleave: function() {
    		jQuery(this).find('div.jp-layout-hidden-Button').hide();
    	}
    });
}

//function addButton() {
//	 this.jpControllGroup = new mycore.iview.widgets.toolbar.ToolbarGroup("JPortalControllGroup");
//	 this.paperClipButton = new mycore.iview.widgets.toolbar.ToolbarButton("PaperclipButton", "", "paperclip", "paperclip");
//	 this.jpControllGroup.addComponent(this.paperClipButton); 
//}


// PIWIK
/*

$(iviewObj.currentImage).bind("imageChanged", function(e) {
// piwik tracking
if(typeof Piwik !== 'undefined') {
	var currentImage = e.target;
	var properties = currentImage.viewer.properties;
	var trackURL = properties.webappBaseUri + "receive/" + properties.objectId + "?derivate=" + properties.derivateId + "&page=" + currentImage.name;
	var tracker = Piwik.getAsyncTracker();
	tracker.trackLink(trackURL, 'download');
}
});

*/