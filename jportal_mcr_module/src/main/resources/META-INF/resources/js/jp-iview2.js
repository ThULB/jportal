// iview settings
var styleFolderUri = 'gfx/';
var chapterEmbedded = false;
var i18n;

// jp specific settings
var defaultOptions = {};

var jpIviewSettings = {

    getServletBaseURL : function() {
        return jp.baseURL + 'servlets/';
    },
    getParam : function(name) {
        return (location.search.match(new RegExp('[\\?&]' + name + '=([^&#]*)')) || [ , "" ])[1];
    },
    getDerivInfo : function(srcString) {
        var matchResult = srcString.match(new RegExp('[^&#]*MCRThumbnailServlet/([^&#]*_derivate_[^&#/]*)(/[^&#]*)\\?[^&#]*') || [ , "" ]);
        return matchResult == null ? null : {
            ID : matchResult[1],
            file : decodeURIComponent(matchResult[2])
        };
    },
    getIviewCSS: function() {
        if (this.getParam('iview2.debug') == 'true') {
            return 'iview2.css';
        }
        return 'iview2.min.css';
    },
    loadOnStartup: function() {
    	return this.getParam("page") != "";
    },    
    jqueryUIVersion: '1.9.2',
}

function jpAddDefaultOptions(option) {
	defaultOptions[option.id] = option.options;
}

function jpInitIview2(settings) {
	i18n = i18n || new iview.i18n(jp.baseURL, settings.currentLang, 'component.iview2');
    loadCssFile(jp.baseURL + 'modules/iview2/gfx/default/' + jpIviewSettings.getIviewCSS(), 'iviewCss');
    loadCssFile('http://ajax.googleapis.com/ajax/libs/jqueryui/' + jpIviewSettings.jqueryUIVersion + '/themes/base/jquery-ui.css');
    jQuery('div.jp-layout-derivateWrapper .image').on({
    	mouseenter: function() {
    		jQuery(this).find('div.jp-layout-hidden-Button').show();
    	},
    	mouseleave: function() {
    		jQuery(this).find('div.jp-layout-hidden-Button').hide();
    	},
    	click: function() {
    		jQuery(this).find('div.jp-layout-hidden-Button').hide();
    		clickToEnlarge(jQuery(this));
    	}
    });
    if(jpIviewSettings.loadOnStartup()) {
    	showIview({
    		ID: jpIviewSettings.getParam("derivate"),
    		file: jpIviewSettings.getParam("page")
    	});
    }
}

function jpInitIviewContainer() {
	$(	'<div/><div id="viewerContainer"  class="viewerContainer min">\
			<div class="viewer" onmousedown="return false;">\
				<div class="surface" style="width:100%;height:100%;z-index:30" />\
				<div class="iview_well">\
				</div>\
			</div>\
        </div>').appendTo($('#viewerContainerWrapper').empty()
    );
}

function clickToEnlarge(that) {
    var hiddenButton = that.find('div.jp-layout-hidden-Button');
    if(hiddenButton) {
    	hiddenButton.hide();        	
    }
    var image = that.find('img');
    var derivInfo = jpIviewSettings.getDerivInfo(image.attr('src'));
    showIview(derivInfo);
}

function showIview(derivInfo) {
	if(derivInfo == null) {
		return;
	}
	jpInitIviewContainer();
    var container = $('#viewerContainer');
    var finalOptions = {};
    var containerOptions = {
        derivateId : derivInfo.ID,
        webappBaseUri : jp.baseURL,
        baseUri : [ jpIviewSettings.getServletBaseURL() + 'MCRTileServlet' ],
        startWidth : 192,
        startHeight : 192
    };
    jQuery.extend(finalOptions, defaultOptions[derivInfo.ID], containerOptions);
    var iviewObj = new iview.IViewInstance(container, finalOptions);
    iview.addInstance(iviewObj);
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
    iviewObj.startViewer(derivInfo.file, "true");
}
