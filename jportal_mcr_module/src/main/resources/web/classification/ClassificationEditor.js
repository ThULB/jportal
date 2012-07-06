/**
 * Load scripts for classification editor.
 * @param conf
 *  cssURL - url to css
 *  jsUR - url to js
 *  debug - is debug enabled
 */
function loadClassificationEditor(conf, /*function*/ onSuccess, /*function*/ onError) {
	console.log("load classification editor");
	$.getScript('http://ajax.googleapis.com/ajax/libs/dojo/1.6.1/dojo/dojo.xd.js')
	.done(function(data, status) {
		// css
		$("head").append('<link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/dojo/1.6.1/dijit/themes/claro/claro.css"></link>');
		$("head").append('<link rel="stylesheet" type="text/css" href="' + conf.cssURL + '/classificationEditor.css"></link>');
		// classeditor js
		var classEditorJS = "classificationEditor.min.js";
		if(conf.debug) {
			classEditorJS = "classificationEditor.js";
		}
		$.getScript(conf.jsURL + '/' + "dojoInclude.js").fail(onError);
		$.getScript(conf.jsURL + '/' + classEditorJS).fail(onError);
		dojo.ready(function() {
			console.log("classification editor successfully loaded");
			onSuccess();
		});
	})
	.fail(onError);
}

/**
 * Shows the classification editor.
 * @param conf see classeditor.Editor documentation
 */
function startClassificationEditor(conf) {
	var diagID = 'classiDiag';
	var diag = dijit.byId(diagID);
	if (diag === undefined) {
        updateBodyTheme();
        var classEditor = new classeditor.Editor(classeditor.settings);
        var diag = new dijit.Dialog({
            id : "classiDiag",
            content : classEditor.domNode
        });
        dojo.addClass(diag.domNode, "classeditorDialog");
        classEditor.create();
        diag.set("title", SimpleI18nManager.getInstance().get("component.classeditor"));
        classEditor.loadClassification(classeditor.classId, classeditor.categoryId);
	}
	diag.show();
}