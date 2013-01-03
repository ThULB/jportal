var styleFolderUri = 'gfx/';
var chapterEmbedded = 'false';
var chapDynResize = 'false';
var DampInViewer = 'true';
var i18n;
var loadOnStartup = false;
var defaultOptions = {};

function jpAddDefaultOptions(option) {
	defaultOptions[option.id] = option.options;
}

function jpInitIview2(settings) {
    var Tools = {
        getBaseURL : function() {
            return location.protocol + '//' + location.host + '/';
        },

        getServletBaseURL : function() {
            return this.getBaseURL() + 'servlets/';
        },

        getCurrentLang : function() {
            return settings.currentLang;
        },

        getParam : function(name) {
            return (location.search.match(new RegExp('[\\?&]' + name + '=([^&#]*)')) || [ , "" ])[1];
        },

        getDerivInfo : function(srcString) {
            var matchResult = srcString.match(new RegExp('[^&#]*MCRThumbnailServlet/([^&#]*_derivate_[^&#]*)(/[^&#]*)\\?[^&#]*')
                    || [ , "" ]);
            return {
                ID : matchResult[1],
                file : matchResult[2]
            }
        }
    }

    var WebApplicationBaseURL = Tools.getBaseURL();
    var ServletsBaseURL = Tools.getServletBaseURL();
    var CurrentLang = Tools.getCurrentLang();
    var jqueryUIVersion = '1.8.17';
    var loadOnStartup = Tools.getParam("page") != "";

    i18n = i18n || new iview.i18n(WebApplicationBaseURL, CurrentLang, 'component.iview2');

    var iviewCSSFile = 'iview2.min.css';
    if (Tools.getParam('XSL.iview2.debug') == 'true') {
        iviewCSSFile = 'iview2.css';
    }

    loadCssFile(WebApplicationBaseURL + 'modules/iview2/gfx/default/' + iviewCSSFile, 'iviewCss');
    loadCssFile('http://ajax.googleapis.com/ajax/libs/jqueryui/' + jqueryUIVersion + '/themes/base/jquery-ui.css');

    function initIviewContainer() {
        $(
                '<div/><div id="viewerContainer"  class="viewerContainer min">\
                    <div class="viewer" onmousedown="return false;">\
                        <div class="surface" style="width:100%;height:100%;z-index:30" />\
                        <div class="well"/>\
                    </div>\
            </div>')
                .appendTo($('#viewerContainerWrapper').empty());
    }

    function clickToEnlarge(that) {
        var hiddenButton = that.find('div.jp-layout-hidden-Button');
        if(hiddenButton) {
        	hiddenButton.hide();        	
        }
        var image = that.find('img');
        var derivInfo = Tools.getDerivInfo(image.attr('src'));
        showIview(derivInfo);
    }

    function showIview(derivInfo) {
    	initIviewContainer();
        var container = $('#viewerContainer');
        var finalOptions = {};
        var containerOptions = {
            derivateId : derivInfo.ID,
            webappBaseUri : WebApplicationBaseURL,
            baseUri : [ ServletsBaseURL + 'MCRTileServlet' ],
            startWidth : 192,
            startHeight : 192
        };
        jQuery.extend(finalOptions, defaultOptions[derivInfo.ID], containerOptions);
        var iviewObj = new iview.IViewInstance(container, finalOptions);
        iview.addInstance(iviewObj);
        iviewObj.startViewer(derivInfo.file, "true");
    }

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

    if(loadOnStartup) {
    	showIview({
    		ID: Tools.getParam("derivate"),
    		file: Tools.getParam("page")
    	});
    }
}