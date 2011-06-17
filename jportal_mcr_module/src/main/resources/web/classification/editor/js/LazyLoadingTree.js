/*
 * @package classification
 */
var classification = classification || {};

/**
 * 
 */
classification.LazyLoadingTree = function(/*String*/ classBaseURL) {
	// tree
	this.tree = null;
	this.store = null;
	this.treeModel = null;
	// event
	this.eventHandler = new classification.EventHandler(this);

	// tree
	this.rootItem = null;

	// url
	this.classBaseURL = classBaseURL;
	// id - current loaded classification
	this.classificationID = null;
};

( function() {

	function create(/*String*/ classificationID) {
		this.classificationID = classificationID;

		var xhrArgs = {
			url :  this.classBaseURL + this.classificationID,
			handleAs : "json",
			load : dojo.hitch(this, function(items) {
				var createTreeFunc = dojo.hitch(this, createTree);
				createTreeFunc(items);
			}),
			error : dojo.hitch(this, function(error) {
				console.log("error while retrieving classification items from url " + this.targetURL + "! " + error);
			})
		};
		dojo.xhrGet(xhrArgs);
	}

	function createTree(items) {
		var showRoot = true;
		if(dojo.isArray(items)) {
			// bunch of classifications
			this.rootItem = {
				root: true,
				labels: [
				    {lang: "de", text: "Klassifikationen"},
				    {lang: "en", text: "Classifications"}
				],
				isItem: false
			}
		} else {
			// single classification/category
			if(!items.children) {
				// if a category is loaded without children
				this.rootItem = items;
				this.rootItem.isItem = true;
				this.rootItem.root = true;
//				items = [items];
//				showRoot = false;
			} else {
				// single classification/category with children
				this.rootItem = items;
				this.rootItem.isItem = true;
				this.rootItem.root = true;
				items = items.children;
			}
		}

		this.store = dojoclasses.SimpleRESTStore({
			classBaseURL: this.classBaseURL,
			hierarchical: false,
			data: {items: items}
		});

		this.treeModel = new dijit.tree.ForestStoreModel({
			store: this.store,
			deferItemLoadingUntilExpand: false,
			mayHaveChildren: function(item) {
				if(item.children)
					if(item.children[0] == false)
						return false;
				return true;
			}
		});
		this.tree = new dijit.Tree({
			model: this.treeModel,
//			dndController: "dijit.tree.dndSource",
//			betweenThreshold: "5",
//			dragThreshold: "5",
			persist: false,
			showRoot: showRoot,
			getLabel: dojo.hitch(this, getLabel),
			getIconClass: dojo.hitch(this, getIconClass),
			checkItemAcceptance: dojo.hitch(this, checkItemAcceptance),
			expandNode: function(/*TreeNode*/ node) {
				// TODO - maybe there is a better solution than calling a private method
				this._expandNode(node, false);
			},
			getIconStyle: function () {
				return {
					height: "22px",
					width: "22px",
				};
			}
		});
		dojo.connect(this.tree, "focusNode", this, itemFocused);
		this.eventHandler.notify({"type" : "treeCreated"});
	}

	function getLabel(/* TreeItem */ treeItem) {
		var currentLang = SimpleI18nManager.getInstance().getCurrentLanguage();
		if(treeItem.root && this.rootItem && this.rootItem.labels) {
			return getLabelText(currentLang, this.rootItem.labels);
		}
		if(treeItem.labels) {
			return getLabelText(currentLang, treeItem.labels);
		}
		return "undefined";
	}

	function getLabelText(/*String*/ currentLang, /*Array*/ labels) {
		for(var i = 0; i < labels.length; i++) {
			if(labels[i].lang == currentLang)
				return labels[i].text;
		}
		return labels[0].text;
	}

	function getIconClass(/* TreeItem */ treeItem) {
		// root
		if(treeItem.root)
			return "icon24 classifications";
		// klassifikation (ends with a point)
		if(treeItem.$ref && treeItem.$ref[0].match(".$") == ".") 
			return "icon24 classification"; 
		// category
		return "icon24 category";
	}
	function checkItemAcceptance() {
		// TODO
		return true;
	}

	function itemFocused() {
		var treeItem = this.tree.lastFocused.item;
		if(treeItem.root && this.rootItem) {
			if(this.rootItem.isItem) {
				treeItem = this.rootItem;
			} else {
				treeItem = null;
			}
		}
		this.eventHandler.notify({"type" : "itemSelected", "item": treeItem});
	}

	function update(/*dojo.data.item*/ item, /*String*/ attribute, /*Object*/ value) {
		if(item.root) {
			this.rootItem = item;
			this.rootItem[attribute] = value;
			// TODO: update label of root node
		} else {
			this.store.setValue(item, attribute, value);
		}
	}

	/**
	 * Creates a new tree item and adds them to the last focused item.
	 */
	function addToSelected() {
		var selectedNode = this.tree.lastFocused;
		var selectedItem = selectedNode.item;
		// expand node if its not expanded yet
		if(selectedNode.isExpandable && !selectedNode.isExpanded) {
			this.tree.expandNode(selectedNode);
		}
		// get root id
		var rootId = "";
		if(selectedItem.root) {
			if(this.rootItem && this.rootItem.isItem) {
				rootId = getClassificationId(this.rootItem.id);
			}
		} else {
			var ref = selectedItem.$ref[0];
			rootId = getClassificationId(ref);
		}
		var rootIdRequestPath = rootId.length > 0 ? "/" + rootId : "";
		// get new category id
		var xhrArgs = {
			url :  this.classBaseURL + "newID" + rootIdRequestPath,
			handleAs : "text",
			load : dojo.hitch(this, function(newId) {
				var newItemFunc = dojo.hitch(this, newItem);
				newItemFunc(selectedItem, newId);
			}),
			error : dojo.hitch(this, function(error) {
				console.log("error while retrieving new id: " + error);
			})
		};
		dojo.xhrGet(xhrArgs);
	}

	/**
	 * Removes all selected tree items.
	 */
	function removeSelected() {
		var selectedTreeItems = this.tree.selectedItems;
		// add only items which are not a descendant of another selected
		// its important to avoid side effects
		var itemsToRemoveArray = [];
		for(var i = 0; i < selectedTreeItems.length; i++) {
			var descendant = false;
			for(var j = 0; j < selectedTreeItems.length; j++) {
				if(isDescendant(selectedTreeItems[i], selectedTreeItems[j])) {
					descendant = true;
					break;
				}
			}
			if(!descendant) {
				itemsToRemoveArray.push(selectedTreeItems[i]);
			}
		}
		// remove from tree
		var removeTreeItemFunc = dojo.hitch(this, removeTreeItem);
		for(var i = 0; i < itemsToRemoveArray.length; i++) {
			removeTreeItemFunc(itemsToRemoveArray[i]);
		}
		this.eventHandler.notify({"type" : "itemsRemoved", "items": itemsToRemoveArray});
	}

	function getClassificationId(/*String*/ id) {
		var i = id.indexOf(".");
		if(i == -1) {
			return id;
		}
		return id.substring(0, i);
	}

	function newItem(/*dojo.data.item*/ parent, /*String*/ newId) {
		if(parent.children && parent.children[0] == false) {
			delete(parent.children);
		}
		var newItem = this.treeModel.newItem({
			$ref: newId,
			labels: [
			    {lang: "de", text: "neuer Eintrag"}
			],
			children: false
		}, parent);
		this.eventHandler.notify({"type" : "itemAdded", "item": newItem});
	}

	/**
	 * This method deletes an item and all its children from the store. That is
	 * necessary because store.deleteItem() doesn't delete recursive.
	 */
	function removeTreeItem(/* dojo.data.item */ item) {
		// delete children
		var removeTreeItemFunc = dojo.hitch(this, removeTreeItem);
		while(item.children && (typeof item.children[0]) != "boolean") {
			removeTreeItemFunc(item.children[0]);
		}
		// delete tree item
		this.store.deleteItem(item);
		this.eventHandler.notify({"type" : "itemRemoved", "item": item});
	}

	/**
	 * Checks if an item is an descendant of another item.
	 */
	function isDescendant(/*dojo.data.item*/ item, /*dojo.data.item*/ ancestor) {
		// same item
		if(item.$ref == ancestor.$ref)
			return false;
		if(ancestor.children && (typeof ancestor.children[0]) != "boolean") {
			for(var i = 0; i < ancestor.children.length; i++) {
				var childItem = ancestor.children[i];
				if(item.$ref == childItem.$ref || isDescendant(item, childItem)) {
					return true;
				}
			}
		}
		return false;
	}

	classification.LazyLoadingTree.prototype.create = create;
	classification.LazyLoadingTree.prototype.update = update;
	classification.LazyLoadingTree.prototype.addToSelected = addToSelected;
	classification.LazyLoadingTree.prototype.removeSelected = removeSelected;

})();
