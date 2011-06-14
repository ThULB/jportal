/*
 * @package classification
 */
var classification = classification || {};

/**
 * 
 */
classification.Editor = function(/*String*/ classBaseURL) {
	// divs
	this.domNode = null;

	// class base url
	this.classBaseURL = classBaseURL;
	// current loaded classification
	this.classificationID = null;

	// content
	this.treePane = null;
	this.categoryEditorPane = null;
};

( function() {

	/**
	 * Creates all important instances and the dom structure.
	 */
	function create() {
		// create tree & category editor
		this.treePane = new classification.TreePane();
		this.treePane.create(this.classBaseURL);
		this.categoryEditorPane = new classification.CategoryEditorPane();
		this.categoryEditorPane.create();
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
		borderContainer.addChild(this.treePane.mainPane);
		borderContainer.addChild(this.categoryEditorPane.mainPane);
		borderContainer.layout();

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
			if(this.categoryEditorPane.disabled) {
				this.categoryEditorPane.setDisabled(false);
			}
			this.categoryEditorPane.update(args.item);
		}
	}

	function handleCategoryEditorEvents(/*CategoryEditorPane*/ source, /*JSON*/ args) {
		if(args.type == "labelChanged") {
			this.treePane.tree.update(args.item, "labels", args.value);
		}
	}

	classification.Editor.prototype.create = create;
	classification.Editor.prototype.loadClassification = loadClassification;

})();
