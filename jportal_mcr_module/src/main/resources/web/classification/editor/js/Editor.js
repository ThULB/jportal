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
	// classification & category
	this.classificationId = null;
	this.categoryId = null;

	// toolbar
	this.toolbar = null;
	// content
	this.treePane = null;
	this.categoryEditorPane = null;

	// save
	this.saveArray = [];
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
			tooltip: "Änderungen speichern",
			onClick: dojo.hitch(this, save)
		});
		this.navigationToolbar.addChild(this.saveButton);

		// events
		this.treePane.tree.eventHandler.attach(dojo.hitch(this, handleTreeEvents));
		this.categoryEditorPane.eventHandler.attach(dojo.hitch(this, handleCategoryEditorEvents));
	}

	/**
	 * Loads a new classification - if this string is empty, all
	 * classifications are loaded.
	 */
	function loadClassification(/*String*/ classificationId, /*String*/ categoryId) {
		this.classificationId = classificationId;
		this.categoryId = categoryId;
		this.treePane.loadClassification(classificationId, categoryId);
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
		} else if(args.type == "itemAdded") {
			this.updateSaveArray("update", args.item, args.parent);
			this.updateToolbar(true);
		} else if(args.type == "itemMoved") {
			this.updateSaveArray("update", args.item, args.parent);
			this.updateToolbar(true);
		} else if(args.type == "itemsRemoved") {
			for(var i = 0; i < args.items.length; i++) {
				this.updateSaveArray("delete", args.items[i]);
			}
			this.categoryEditorPane.setDisabled(true);
			this.updateToolbar(true);
		}
	}

	function handleCategoryEditorEvents(/*CategoryEditorPane*/ source, /*JSON*/ args) {
		if(args.type == "labelChanged" || args.type == "urlChanged" || args.type == "idChanged") {
			var key = undefined;
			if(args.type == "labelChanged") {
				key = "labels";
			} else if(args.type == "urlChanged") {
				key = "uri";
			} else if(args.type == "idChanged") {
				key = "id";
			}
			this.treePane.tree.update(args.item, key, args.value);
			this.updateSaveArray("update", args.item);
			this.updateToolbar(true);
		}
	}

	function updateToolbar(/*boolean*/ dirty) {
		if(dirty && this.saveButton.get("disabled")) {
			this.saveButton.set("disabled", false);
			this.saveButton.set("iconClass", "icon16 saveIcon");
		}
	}

	function updateSaveArray(/*String*/ state, /*dojo.data.item*/ item, /*dojo.data.item*/ parent) {
		// get object from array
		var saveObject = null;
		for(var i = 0; i < this.saveArray.length; i++) {
			if(isIdEqual(this.saveArray[i].item.id[0], item.id[0])) {
				saveObject = this.saveArray[i];
			}
		}
		// if not defined -> create new and add to array
		if(saveObject == null) {
			saveObject = {};
			this.saveArray.push(saveObject);
		}
		// set new data
		saveObject.item = item;
		saveObject.state = state;
		if(parent != null) {
			saveObject.parent = parent;
		}
	}

	function save() {
		var finalArray = [];
		for(var i = 0; i < this.saveArray.length; i++) {
			var saveObject = this.saveArray[i];
			var cleanedSaveObject = {
				item: cloneAndCleanUp(saveObject.item),
				state: saveObject.state
			}
			if(saveObject.state == "update" && saveObject.parent) {
				if(saveObject.parent.children) {
					cleanedSaveObject.parentId = saveObject.parent.id[0];
					var index = this.treePane.tree.indexAt(saveObject.parent, saveObject.item.id[0]);
					cleanedSaveObject.index = index;
				} else {
					cleanedSaveObject.state = "delete";
				}
			}
			finalArray.push(cleanedSaveObject);
		}

		var navXhrArgs = {
			url :  this.resourcePath + "save",
			postData : dojo.toJson(finalArray),
			handleAs : "json",
			headers: { "Content-Type": "application/json; charset=utf-8"},
			error : dojo.hitch(this, function(error) {
				console.log("error while saving");
			}),
			load : dojo.hitch(this, function(data) {
				console.log("saving done");
				console.log(data);
			})
		};
		dojo.xhrPost(navXhrArgs);
	}

	function cloneAndCleanUp(/*dojo.data.item*/ item) {
		var newItem = {
			id: item.id[0],
			labels: item.labels
		};
		if(item.uri) {
			newItem.uri = item.uri[0];
		}
		return newItem;
	}
	
	classification.Editor.prototype.create = create;
	classification.Editor.prototype.loadClassification = loadClassification;
	classification.Editor.prototype.updateToolbar = updateToolbar;
	classification.Editor.prototype.updateSaveArray = updateSaveArray;

})();
