var styleFolderUri = 'gfx/';
var chapterEmbedded = 'false';
var chapDynResize = 'false';
var DampInViewer = 'true';
var i18n;
var loadOnStartup = false;

function jpInitIview2() {
    var Tools = {
        getBaseURL : function() {
            return location.protocol + '//' + location.host + '/';
        },

        getServletBaseURL : function() {
            return this.getBaseURL() + 'servlets/';
        },

        getCurrentLang : function() {
            return navigator.language.split("-")[0];
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
//        setTimeout(function() {
//            iviewObj.toggleViewerMode();
//        }, 50)
    }

    function showIview(derivInfo) {
    	initIviewContainer();
        var container = $('#viewerContainer');
        var containerOptions = {
            derivateId : derivInfo.ID,
            webappBaseUri : WebApplicationBaseURL,
            baseUri : [ ServletsBaseURL + 'MCRTileServlet' ],
            startWidth : 192,
            startHeight : 192
        };
        var iviewObj = new iview.IViewInstance(container, containerOptions);
        iview.addInstance(iviewObj);
        iviewObj.startViewer(derivInfo.file, "true");
    }
    
/*
    $('.viewerContainer.min').each(function(i, container) {
        var derivID = $(container).attr('id');
        var startFile = $(container).attr('file');
        var containerOptions = createOptions(derivID);
        var iviewObj = new iview.IViewInstance($(container), containerOptions);
        iview.addInstance(iviewObj);
        iviewObj.startViewer(startFile);
    });
*/
    $('.jp-layout-derivateList').delegate('div.jp-layout-derivateWrapper .image', 'mouseenter', function() {
        $(this).find('div.jp-layout-hidden-Button').show();
    }).delegate('div.jp-layout-derivateWrapper .image', 'mouseleave click', function() {
        $(this).find('div.jp-layout-hidden-Button').hide();
    }).delegate('div.jp-layout-derivateWrapper .image', 'click', function() {
        clickToEnlarge($(this));
    });

    if(loadOnStartup) {
    	showIview({
    		ID: Tools.getParam("derivate"),
    		file: Tools.getParam("page")
    	});
    }
}

$(document).ready(jpInitIview2());