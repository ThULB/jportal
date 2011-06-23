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

	// deleted items
	this.deletedItemArray = [];
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
		} else if(args.type == "itemRemoved") {
			var item = args.item;
			if(item.added) {
				return;
			}
			// add item.id to deleteItemArray - use dojo.filter for unique list
			var itemId = item.id[0];
			this.deletedItemArray = dojo.filter(this.deletedItemArray, function(idInList) {
				return !isIdEqual(itemId, idInList);
			});
			this.deletedItemArray.push(itemId);
		} else if(args.type == "itemsRemoved") {
			this.categoryEditorPane.setDisabled(true);
			this.updateToolbar(true);
		} else if(args.type == "itemAdded") {
			if(args.parent != null && args.parent.root != true) {
				this.categoryEditorPane.update(args.parent);
			}
			this.updateToolbar(true);
		}
	}

	function handleCategoryEditorEvents(/*CategoryEditorPane*/ source, /*JSON*/ args) {
		console.log(args);
		if(args.type == "labelChanged") {
			this.treePane.tree.update(args.item, "labels", args.value);
			this.updateToolbar(true);
		} else if(args.type == "urlChanged") {
			this.treePane.tree.update(args.item, "uri", args.value);
			this.updateToolbar(true);
		} else if(args.type == "idChanged") {
			this.treePane.tree.update(args.item, "id", args.value);
			this.updateToolbar(true);
		}
	}

	function updateToolbar(/*boolean*/ dirty) {
		if(dirty && this.saveButton.get("disabled")) {
			this.saveButton.set("disabled", false);
			this.saveButton.set("iconClass", "icon16 saveIcon");
		}
	}

	function save() {
		// get tree
		var tree = this.treePane.tree;
		// create arrays
		var addedArray = [];
		var modifiedArray = [];
		// go recursive through tree and get all added and modified items
		if(tree.rootItem.modified) {
			var cleanedRootItem = cloneAndCleanUp(tree.rootItem);
			modifiedArray.push(cleanedRootItem);
		}
		tree.treeModel.getRoot(function(treeRootItem) {
			fillArrays(addedArray, modifiedArray, treeRootItem);
		});
		console.log(addedArray);
		console.log(modifiedArray);
		console.log(this.deletedItemArray);
	}

	function fillArrays(addedArray, modifiedArray, parent) {
		dojo.forEach(parent.children, function(item, index) {
			if(item.added) {
				var cleanedItem = cloneAndCleanUp(item, true);
				cleanedItem.parentId = parent.id[0];
				cleanedItem.index = index;
				addedArray.push(cleanedItem);
			} else {
				if(item.modified) {
					var cleanedItem = cloneAndCleanUp(item, false);
					modifiedArray.push(cleanedItem);
				}
				if(hasChildrenLoaded(item)) {
					// do recursive calls for children
					fillArrays(addedArray, modifiedArray, item);
				}
			}
		});
	}

	function cloneAndCleanUp(/*dojo.data.item*/ item, /*boolean*/ withChildren) {
		var newItem = {
			id: item.id[0],
			labels: item.labels
		};
		if(withChildren && hasChildrenLoaded(item)) {
			newItem.children = [];
			dojo.forEach(item.children, function(childItem) {
				var newChildItem = cloneAndCleanUp(childItem, true);
				newItem.children.push(newChildItem);
			});
		}
		return newItem;
	}

	classification.Editor.prototype.create = create;
	classification.Editor.prototype.loadClassification = loadClassification;
	classification.Editor.prototype.updateToolbar = updateToolbar;

})();
