// iview settings
var styleFolderUri = 'gfx/';
var chapterEmbedded = false;
var i18n;

// jp specific settings
var defaultOptions = {};

var jpIviewSettings = {
    getBaseURL : function() {
        return location.protocol + '//' + location.host + '/';
    },
    getServletBaseURL : function() {
        return this.getBaseURL() + 'servlets/';
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
    jqueryUIVersion: '1.8.17',
}

function jpAddDefaultOptions(option) {
	defaultOptions[option.id] = option.options;
}

function jpInitIview2(settings) {
	var baseURL = jpIviewSettings.getBaseURL();
	i18n = i18n || new iview.i18n(baseURL, settings.currentLang, 'component.iview2');
    loadCssFile(baseURL + 'modules/iview2/gfx/default/' + jpIviewSettings.getIviewCSS(), 'iviewCss');
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
				<div class="well"/>\
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
        webappBaseUri : jpIviewSettings.getBaseURL(),
        baseUri : [ jpIviewSettings.getServletBaseURL() + 'MCRTileServlet' ],
        startWidth : 192,
        startHeight : 192
    };
    jQuery.extend(finalOptions, defaultOptions[derivInfo.ID], containerOptions);
    var iviewObj = new iview.IViewInstance(container, finalOptions);
    iview.addInstance(iviewObj);
    iviewObj.startViewer(derivInfo.file, "true");
}
