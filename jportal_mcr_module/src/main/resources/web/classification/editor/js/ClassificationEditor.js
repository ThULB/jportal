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
var dojoConfig = {
        isDebug : false,
        parseOnLoad : true,
        modulePaths : {
            "dojoclasses" : "js/dojoclasses"
        },
        xdWaitSeconds : 10
    };
    

function startClassificationEditor(element, conf) {
    element.hide();
    dojoConfig.baseUrl = conf.baseUrl;
    
    var includes = [
                    'dojoInclude.js',
                    'ClassificationUtils.js',
                    'SimpleI18nManager.js',
                    'EventHandler.js',
                    'LazyLoadingTree.js',
                    'LabelEditor.js',
                    'CategoryEditorPane.js',
                    'TreePane.js',
                    'Editor.js'
                    ];
    
    $.getScript('http://ajax.googleapis.com/ajax/libs/dojo/1.6.1/dojo/dojo.xd.js', function(data, status){
        element.trigger('loadDojoSuccessful');
    });
    $("head").append('<link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/dojo/1.6.1/dijit/themes/claro/claro.css"></link>');
    $("head").append('<link rel="stylesheet" type="text/css" href="' + conf.baseUrl + 'css/classificationEditor.css"></link>');

    element.bind('loadDojoSuccessful', function(){
        $.each(includes, function(i, include){
            $.getScript(conf.jsPath + '/' + include);
        })
        
        dojo.require("dijit.Dialog");
        dojo.ready(function(){
            element.click(showDialog);
            element.show();
        });
    });
    
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