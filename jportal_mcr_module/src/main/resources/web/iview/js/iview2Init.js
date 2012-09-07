var styleFolderUri = 'gfx/';
var chapterEmbedded = 'false';
var chapDynResize = 'false';
var DampInViewer = 'true';
var i18n;

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
        that.find('div.jp-layout-hidden-Button').hide();
        initIviewContainer();
        
        var image = that.find('img');
        var derivInfo = Tools.getDerivInfo(image.attr('src'));
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
//        setTimeout(function() {
//            iviewObj.toggleViewerMode();
//        }, 50)
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
    $('.jp-layout-derivateList').delegate('div.jp-layout-derivateWrapper', 'mouseenter', function() {
        $(this).find('div.jp-layout-hidden-Button').show();
    }).delegate('div.jp-layout-derivateWrapper', 'mouseleave click', function() {
        $(this).find('div.jp-layout-hidden-Button').hide();
    }).delegate('div.jp-layout-derivateWrapper', 'click', function() {
        clickToEnlarge($(this));
    })
}

$(document).ready(jpInitIview2());