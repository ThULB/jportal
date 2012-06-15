var baseUrl = location.protocol + '//' + location.host + '/';
var runid = [];
jQuery(document).bind("toolbarloaded", function(e) {
    // just run if a given viewType is created
    if (e.model.id != "mainTb" || runid[e.viewer.viewID]) {
        return;
    }
    runid[e.viewer.viewID] = true;
    var toolbarModel = e.model;
    var i = toolbarModel.getElementIndex('spring');
    var buttonSet = new ToolbarButtonsetModel("softLink");
    var button = new ToolbarButtonModel(buttonSet.elementName, {
        'type' : 'buttonDefault'
    }, {
        'label' : "softLink",
        'text' : false,
        'icons' : {
            primary : 'paperClip-icon'
        }
    }, "Verlinken", true, false);
    toolbarModel.addElement(buttonSet, i);
    buttonSet.addButton(button);
    // attach to events of view
    jQuery.each(e.getViews(), function(index, view) {
        jQuery(view).bind("press", function(sender, args) {
            if (args.parentName == buttonSet.elementName) {
                if (args.elementName == buttonSet.elementName) {
                    var file = decodeURI(e.viewer.currentImage.name);
                    var derivId = e.viewer.properties.derivateId;
                    var servletPath = baseUrl + "servlets/DerivateLinkServlet";
                    jQuery.post(servletPath, {
                        mode : "setImage",
                        derivateId : derivId,
                        file : file
                    });
                }
            }
        })
    });
});