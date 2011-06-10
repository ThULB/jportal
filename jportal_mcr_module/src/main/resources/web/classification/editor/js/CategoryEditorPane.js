/*
 * @package classification
 */
var classification = classification || {};

/**
 * 
 */
classification.CategoryEditorPane = function() {
	// nodes
	this.mainPane = new dijit.layout.ContentPane({
		splitter: true
	});
	
	// editor
	this.categoryEditor = null;
	this.urlEditor = null;
	
	// event
	this.eventHandler = new classification.EventHandler(this);
	
	// item
	this.currentItem = null;
};

( function() {

	function create() {
		// create editor
		this.categoryEditor = new classification.CategoryEditor();
		this.categoryEditor.create();
		// create div
		var mainDiv = dojo.create("div");
		this.mainPane.set("content", mainDiv);
		mainDiv.appendChild(this.categoryEditor.domNode);

		// handle events
		this.categoryEditor.eventHandler.attach(dojo.hitch(this, handleCategoryEditorEvents));
	}

	function update(/*dojo.data.item*/ treeItem) {
		this.currentItem = treeItem;
		console.log(this.currentItem);
		this.categoryEditor.update(treeItem.labels);
	}

	function handleCategoryEditorEvents(/*CategoryEditor*/ source, /*JSON*/ args) {
		if(args.type == "categoryRemoved" || args.type == "categoryChanged") {
			if(this.currentItem == null) {
				return;
			}
			var labels = this.categoryEditor.getValues();
			// check if something changed
			if(deepEquals(labels, this.currentItem.labels)) {
				return;
			}
//			this.currentItem.labels = labels;
//			var itemChangedFunc = dojo.hitch(this, fireItemChanged);
//			itemChangedFunc(this.currentItem);
		}
	}

	function fireItemChanged(/*dojo.data.item*/ treeItem) {
		this.eventHandler.notify({"type" : "itemChanged", "item": treeItem});
	}

	classification.CategoryEditorPane.prototype.create = create;
	classification.CategoryEditorPane.prototype.update = update;

})();
