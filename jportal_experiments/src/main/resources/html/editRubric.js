/**
 * Load scripts for classification editor 
 * conf = { baseUrl : string,
 *          resourcePath : string,
 *          classificationId : string,
 *          categoryId : string,
 *          showId : boolean,
 *          currentLang : string,
 *          jsPath : string,
 *          buttonID : string 
 *        }
 */
var dojoConfig = {};
function initDojoConfig(conf) {
    dojoConfig = {
        isDebug : true,
        parseOnLoad : true,
        baseUrl : conf.baseUrl,
        modulePaths : {
            "dojoclasses" : "js/dojoclasses"
        },
        xdWaitSeconds : 10
    };
}

function startClassificationEditor(conf) {
    initDojoConfig(conf);
    $.getScript('http://ajax.googleapis.com/ajax/libs/dojo/1.6.1/dojo/dojo.xd.js');
    $("head")
            .append(
                    "<link rel='stylesheet' type='text/css' href='http://ajax.googleapis.com/ajax/libs/dojo/1.6.1/dijit/themes/claro/claro.css'></link>");
    $("head").append(
            "<link rel='stylesheet' type='text/css' href='" + conf.baseUrl
                    + "css/classificationEditor.css'></link>");

    var _interval = setInterval(function() {
        // wait till dojo is loaded
        if (window.dojo !== undefined) {
            clearInterval(_interval);

            $.getScript(conf.jsPath + '/dojoInclude.js');
            $.getScript(conf.jsPath + '/ClassificationUtils.js');
            $.getScript(conf.jsPath + '/SimpleI18nManager.js');
            $.getScript(conf.jsPath + '/EventHandler.js');
            $.getScript(conf.jsPath + '/LazyLoadingTree.js');
            $.getScript(conf.jsPath + '/LabelEditor.js');
            $.getScript(conf.jsPath + '/CategoryEditorPane.js');
            $.getScript(conf.jsPath + '/TreePane.js');
            $.getScript(conf.jsPath + '/Editor.js');
            var _interval2 = setInterval(function() {
                // wait till classification is loaded
                if (window.classification !== undefined) {
                    clearInterval(_interval2);
                    dojo.require("dijit.Dialog");
                    // Show the dialog
                    function showDialog() {
                        var diagID = 'classiDiag';
                        var diag = dijit.byId(diagID);
                        if (diag !== undefined) {
                            diag.show();
                        } else {
                            dojo.query('body').forEach(function(node) {
                                dojo.attr(node, "class", "claro");
                            });
                            var supportedLanguages = [ "de", "en", "pl" ];

                            var classEditor = new classification.Editor(conf.resourcePath);
                            classEditor.create(conf.resourcePath, supportedLanguages, conf.currentLang, conf.showId);

                            diag = new dijit.Dialog({
                                id : diagID,
                                // The dialog's title
                                title : "Klassifikationseditor",
                                // The dialog's content
                                content : classEditor.domNode,
                                // Hard-code the dialog width
                                style : "width: 967px; height: 648px;"
                            });
                            diag.show();
                            dojo.query(".dijitDialogUnderlay").style({
                                'background' : 'black'
                            });
                            classEditor.loadClassification(
                                    conf.classificationId, conf.categoryId);
                        }
                    }
                }
                dojo.ready(function(){
                    $('#'+conf.buttonID).click(showDialog)
                });
            }, 1000);
        }
    }, 1000);
}