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
	this.labelEditor = null;
	this.urlEditor = null;
	this.classIdEditor = null;
	this.categIdEditor = null;

	// event
	this.eventHandler = new classification.EventHandler(this);

	// item
	this.currentItem = null;

	// disabled
	this.disabled = false;
};

( function() {

	function create(/*boolean*/ showId) {
		// create div
		var mainTable = dojo.create("table", {className: "categoryEditorMainTable"});
		this.mainPane.set("content", mainTable);
		// create label editor
		this.labelEditor = new classification.LabelEditor();
		this.labelEditor.create();
		var tr = dojo.create("tr", {}, mainTable);
		dojo.create("td", {innerHTML: "<b>Label</b>", className: "categoryEditorDescription"}, tr);
		var labelEditorTD = dojo.create("td", {className: "categoryEditorValue"}, tr);
		labelEditorTD.appendChild(this.labelEditor.domNode);
		// url
		this.urlEditor = new dijit.form.TextBox({
			intermediateChanges: true,
			onChange: dojo.hitch(this, handleURLChanged)
		});
		dojo.addClass(this.urlEditor.domNode, "largeComponent");
		var tr2 = dojo.create("tr", {}, mainTable);
		dojo.create("td", {innerHTML: "<b>URL</b>", className: "categoryEditorDescription"}, tr2);
		var urlEditorTD = dojo.create("td", {className: "categoryEditorValue"}, tr2);
		urlEditorTD.appendChild(this.urlEditor.domNode);
		// id
		if(showId) {
			// classification id (root id)
			this.classIdEditor = new dijit.form.ValidationTextBox({
				required: true,
				intermediateChanges: true,
				regExp: "[a-zA-Z_\\-0-9]*",
				onChange: dojo.hitch(this, handleIdChanged)
		    });
			dojo.addClass(this.classIdEditor.domNode, "largeComponent");
			var tr3 = dojo.create("tr", {}, mainTable);
			dojo.create("td", {innerHTML: "<b>Class Id</b>", className: "categoryEditorDescription"}, tr3);
			var classIdEditorTD = dojo.create("td", {className: "categoryEditorValue"}, tr3);
			classIdEditorTD.appendChild(this.classIdEditor.domNode);
			// category id
			this.categIdEditor = new dijit.form.ValidationTextBox({
				required: true,
				intermediateChanges: true,
				regExp: "[a-zA-Z_\\-0-9]*",
				onChange: dojo.hitch(this, handleIdChanged)
			});
			dojo.addClass(this.categIdEditor.domNode, "largeComponent");
			var tr4 = dojo.create("tr", {}, mainTable);
			dojo.create("td", {innerHTML: "<b>Categ Id</b>", className: "categoryEditorDescription"}, tr4);
			var categIdEditorTD = dojo.create("td", {className: "categoryEditorValue"}, tr4);
			categIdEditorTD.appendChild(this.categIdEditor.domNode);
		}

		// handle events
		this.labelEditor.eventHandler.attach(dojo.hitch(this, handleLabelEditorEvents));
	}

	function update(/*dojo.data.item*/ treeItem) {
		this.currentItem = treeItem;
		this.labelEditor.update(this.currentItem.labels);
		this.urlEditor.set("value", this.currentItem.uri != undefined ? this.currentItem.uri : null);
		if(this.classIdEditor != null && this.categIdEditor != null) {
			// get id
			var id = this.currentItem.id[0];
			var classId = getClassificationId(id);
			var categId = getCategoryId(id);
			// set classification and category id
			this.classIdEditor.set("value", classId);
			this.categIdEditor.set("value", categId);
			// set editable
			var idEditable = this.currentItem.idEditable;
			var isClassification = categId == "";
			var hasChildren = false;
			if(isClassification && this.currentItem.children) {
				hasChildren = this.currentItem.children[0] != false
			}
			this.classIdEditor.set("disabled", this.disabled || !(idEditable && isClassification && !hasChildren));
			this.categIdEditor.set("disabled", this.disabled || !(idEditable && !isClassification));
		}
	}

	function handleLabelEditorEvents(/*LabelEditor*/ source, /*JSON*/ args) {
		if(args.type == "categoryRemoved" || args.type == "categoryChanged") {
			if(this.currentItem == null) {
				return;
			}
			var labels = this.labelEditor.getValues();
			// check if something changed
			if(deepEquals(labels, this.currentItem.labels)) {
				return;
			}
			// fire event
			this.eventHandler.notify({"type" : "labelChanged", "item": this.currentItem, "value": labels});
		}
	}

	function handleURLChanged(/*String*/ newURL) {
		if(newURL != this.currentItem.uri) {
			this.eventHandler.notify({"type" : "urlChanged", "item": this.currentItem, "value": newURL});
		}
	}
	function handleIdChanged() {
		// get id from editors
		var classId = this.classIdEditor.get("value");
		var categId = this.categIdEditor.get("value");
		var newId = classId + "." + categId;
		// check is valid
		var isClassification = getCategoryId(this.currentItem.id[0]) == "";
		if(!this.classIdEditor.isValid() || (!isClassification && !this.categIdEditor.isValid())) {
			return;
		}
		// check if new and fire event
		if(this.currentItem.id != newId) {
			this.eventHandler.notify({"type" : "idChanged", "item": this.currentItem, "value": newId});
		}
	}
	
	function setDisabled(/*boolean*/ value) {
		this.disabled = value;
		this.labelEditor.setDisabled(value);
		this.urlEditor.set("disabled", value);
		if(this.classIdEditor != null) {
			this.classIdEditor.set("disabled", value);
		}
		if(this.categIdEditor != null) {
			this.categIdEditor.set("disabled", value);
		}
	}

	classification.CategoryEditorPane.prototype.create = create;
	classification.CategoryEditorPane.prototype.update = update;
	classification.CategoryEditorPane.prototype.setDisabled = setDisabled;

})();
