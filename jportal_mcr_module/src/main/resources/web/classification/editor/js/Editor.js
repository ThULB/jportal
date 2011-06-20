/*
 * @package classification
 */
var classification = classification || {};

/**
 * Create a new instance of the classification editor.
 */
classification.Editor = function() {
	// divs
	this.domNode = null;

	// class base url
	this.resourcePath = resourcePath;
	// current loaded classification
	this.classificationID = null;

	// toolbar
	this.toolbar = null;
	// content
	this.treePane = null;
	this.categoryEditorPane = null;
};

( function() {

	/**
	 * Creates all important instances and the dom structure.
	 * 
	 * @param resourcePath path to the classification resource ('/rsc/classifications/')
	 * @param supportedLanguages array of all languages
	 * @param currentLanguage language to initialize the editor 
	 * @param showId if the id of a classification/category is shown and editable
	 */
	function create(/*String*/ resourcePath, /*Array*/ supportedLanguages, /*String*/ currentLanguage, /*boolean*/ showId) {
		// set resource path
		this.resourcePath = resourcePath;
		// I18nManager
		SimpleI18nManager.getInstance().initialize(supportedLanguages, currentLanguage);

		// toolbar
		this.navigationToolbar = new dijit.Toolbar({
			splitter: false,
			region: "top"
		});
		// create tree & category editor
		this.treePane = new classification.TreePane();
		this.treePane.create(this.resourcePath);
		this.categoryEditorPane = new classification.CategoryEditorPane();
		this.categoryEditorPane.create(showId);
		this.categoryEditorPane.setDisabled(true);

		// create borderlayout in tab
		var borderContainer = new dijit.layout.BorderContainer({
			style: "border: none"
		});
		this.domNode = borderContainer.domNode;
		// create panes
		this.treePane.mainPane.set("region", "left");
		dojo.style(this.treePane.mainPane.domNode, {width: "300px"});
		this.categoryEditorPane.mainPane.set("region", "center");
		// add to dom
		borderContainer.addChild(this.navigationToolbar);
		borderContainer.addChild(this.treePane.mainPane);
		borderContainer.addChild(this.categoryEditorPane.mainPane);
		borderContainer.layout();
		// toolbar buttons
		this.saveButton = new dijit.form.Button({
			showLabel: false,
			disabled: true,
			iconClass: "icon16 saveDisabledIcon",
			tooltip: "Änderungen speichern"
		});
		this.navigationToolbar.addChild(this.saveButton);

		// events
		this.treePane.tree.eventHandler.attach(dojo.hitch(this, handleTreeEvents));
		this.categoryEditorPane.eventHandler.attach(dojo.hitch(this, handleCategoryEditorEvents));
	}

	/**
	 * Loads a new classification - if this string is empty, all
	 * classifications are loaded
	 */
	function loadClassification(/*String*/ classificationID) {
		this.classificationID = classificationID;
		this.treePane.loadClassification(classificationID);
	}

	function handleTreeEvents(/*LazyLoadingTree*/ source, /*JSON*/ args) {
		if(args.type == "itemSelected") {
			if(args.item == null) {
				this.categoryEditorPane.setDisabled(true);
			} else {
				if(this.categoryEditorPane.disabled) {
					this.categoryEditorPane.setDisabled(false);
				}
				this.categoryEditorPane.update(args.item);
			}
		} else if(args.type == "itemsRemoved") {
			this.categoryEditorPane.setDisabled(true);
		} else if(args.type == "itemAdded") {
			if(args.parent != null && args.parent.root != true) {
				this.categoryEditorPane.update(args.parent);
			}
		}
	}

	function handleCategoryEditorEvents(/*CategoryEditorPane*/ source, /*JSON*/ args) {
		if(args.type == "labelChanged") {
			this.treePane.tree.update(args.item, "labels", args.value);
		} else if(args.type == "urlChanged") {
			this.treePane.tree.update(args.item, "uri", args.value);
		} else if(args.type == "idChanged") {
			this.treePane.tree.update(args.item, "id", args.value);
		}
	}

	classification.Editor.prototype.create = create;
	classification.Editor.prototype.loadClassification = loadClassification;

})();
