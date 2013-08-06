function startClassificationEditor() {
	require(["dijit/registry"], function(registry) {
	    var diagID = 'classiDiag';
	    var diag = typeof(dijit) !== "undefined" ? registry.byId(diagID) : undefined;
	    if (diag === undefined) {
	      includeClassificationEditor(function(classEditor) {
	    	  diag = registry.byId(diagID);
	    	  diag.show();
	    	  classEditor.startup();
	      });
	    } else {
	    	diag.show();
	    }
	});
}

function includeClassificationEditor(onReady) {
  require(["dojo/ready", "dojo/promise/all", "mycore/util/DOMUtil"], function(ready, all, domUtil) {
    ready(function() {
      all([domUtil.loadCSS("http://ajax.googleapis.com/ajax/libs/dojo/"+classeditor.dojoVersion +"/dijit/themes/claro/claro.css"),
           domUtil.loadCSS(classeditor.settings.cssURL + "/classificationEditor.css"),
           domUtil.loadCSS(classeditor.settings.cssURL + "/mycore.dojo.css"),
           domUtil.loadCSS(classeditor.settings.cssURL + "/modern-pictograms.css")]).then(function() {
        require([
          "dojo/dom-class", "dijit/Dialog", "mycore/classification/Editor"
        ], function(domClass) {
          ready(function() {
            domUtil.updateBodyTheme();
            var classEditor = new mycore.classification.Editor({settings: classeditor.settings});
            diag = new dijit.Dialog({
              id : "classiDiag",
              content : classEditor
            });
            domClass.add(diag.domNode, "classeditorDialog");
            classEditor.loadClassification(classeditor.classId, classeditor.categoryId);
            onReady(classEditor);
          });
        });
      });
    });
  });
}
