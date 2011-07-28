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

	// url
	this.classBaseURL = classBaseURL;
	// id - current loaded classification & category
	this.classificationId = null;
	this.categoryId = null;
};

( function() {

	function create(/*String*/ classificationId, /*String*/ categoryId) {
		this.classificationId = classificationId;
		this.categoryId = categoryId;
		var url = this.classBaseURL + this.classificationId;
		if(categoryId != null && categoryId != "") {
			url += "/" + categoryId;
		}

		var xhrArgs = {
			url :  url,
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
			var items = {
				id: "_placeboid_",
				labels: [
					{lang: "de", text: "Klassifikationen"},
					{lang: "en", text: "Classifications"}
				],
				notAnItem: true,
				children: items
			};
		}

		this.store = dojoclasses.SimpleRESTStore({
			classBaseURL: this.classBaseURL,
			data: {items: [items]}
		});

		this.treeModel = new dijit.tree.TreeStoreModel({
			store: this.store,
			deferItemLoadingUntilExpand: false,
			mayHaveChildren: hasChildren
		});

		this.tree = new dijit.Tree({
			model: this.treeModel,
			dndController: "dojoclasses.TreeDndSource",
			betweenThreshold: "5",
			dragThreshold: "5",
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

        dojo.connect(this.tree.dndController, "onDndDrop", this, onDndDrop);
		dojo.connect(this.tree, "focusNode", this, itemFocused);
		this.eventHandler.notify({"type" : "treeCreated"});
	}

	/**
	 * Set moved attribute to dropped items.
	 * 
	 * @param source
	 * @param nodes
	 */
    function onDndDrop(source, nodes) {
    	var parent = null;
    	dojo.forEach(nodes, dojo.hitch(this, function(node) {
    		var sourceItem = source.getItem(node.id);
    		var item = sourceItem.data.item;
    		if(parent == null) {
    			var parent = sourceItem.data.getParent().item;
    		}
    		var index = indexAt(parent, item.id[0]);
    		this.eventHandler.notify({"type": "itemMoved", "item": item, "parent": parent, "index": index});
    	}));
	}

	/**
	 * Returns the position of the children.
	 * 
	 * @param parent
	 * @param id
	 *            id of children
	 * @return position as integer
	 */
    function indexAt(/* dojo.data.item */ parent, /* JSON */ id) {
		for(var index = 0; index < parent.children.length; index++) {
			var childItem = parent.children[index];
			if(isIdEqual(childItem.id[0], id)) {
				return index;
			}
		}
		return -1;
    }

    
	function getLabel(/* TreeItem */ treeItem) {
		var currentLang = SimpleI18nManager.getInstance().getCurrentLanguage();
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
		if(treeItem.notAnItem && treeItem.notAnItem[0] == true) {
			return "icon22 classification";
		}
		// klassifikation (ends with a point)
		if(isClassification(treeItem)) { 
			return "icon22 classification";
		} else {
			if(hasChildren(treeItem)) {
				return "icon22 category"
			} else {
				return "icon22 category2";
			}
		}
	}

	function checkItemAcceptance(/*TreeNode*/ node, source, position) {
		var item = dijit.getEnclosingWidget(node).item;
		var nodes = source.getSelectedTreeNodes();
		for(var i = 0; i < nodes.length; i++) {
    		var sourceItem = source.getItem(nodes[i].id);
			var draggedItem = sourceItem.data.item;
			if(draggedItem == null)
				return false;
			// unknown ids
			if(!item.id || !draggedItem.id)
				return false;
			// cannot drop an item behind/after a classification
			if(isClassification(item) && position != "over")
				return false;
			// cannot drop an item in another classification
			if(item.id[0].rootid != draggedItem.id[0].rootid)
				return false;
		}
		return true;
	}

	function focusItem(/*dojo.data.item*/ item) {
		// select it
		this.tree.set("selectedItem", item);
		// try to focus
		var treeNode = this.tree.getNodesByItem(item)[0];
		var parentNode = treeNode.getParent();
		// expand parent node if its not expanded yet
		if(parentNode.isExpandable && !parentNode.isExpanded) {
			this.tree.expandNode(parentNode);
		}
		// focus it!
		this.tree.focusNode(treeNode);
	}

	function itemFocused() {
		var treeItem = this.tree.lastFocused.item;
		if(treeItem.notAnItem && treeItem.notAnItem[0] == true) {
			treeItem = null;
		}
		this.eventHandler.notify({"type" : "itemSelected", "item": treeItem});
	}

	/**
	 * Updates the attribute of item with value. The modified flag is also
	 * set. 
	 */
	function update(/*dojo.data.item*/ item, /*String*/ attribute, /*Object*/ value) {
		this.store.setValue(item, attribute, value);
		this.store.setValue(item, "modified", true);
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
		var rootId = getClassificationId(selectedItem);
		var rootIdRequestPath = rootId.length > 0 ? "/" + rootId : "";
		// get new category id
		var xhrArgs = {
			url :  this.classBaseURL + "newID" + rootIdRequestPath,
			handleAs : "json",
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
		var selectedTreeItems = this.getSelectedItems();
		// remove only items which are not a descendant of another selected
		// this is important to avoid side effects
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

	function newItem(/*dojo.data.item*/ parent, /*JSON*/ newId) {
		if(parent.children && parent.children[0] == false) {
			delete(parent.children);
		}
		var newItem = {
			id: newId,
			labels: [
			    {lang: "de", text: "neuer Eintrag"}
			],
			added: true	
		};
		newItem = this.store.newItem(newItem, {parent: parent, attribute: "children"});
		this.store.save();
		this.eventHandler.notify({"type" : "itemAdded", "item": newItem, "parent": parent});
	}

	/**
	 * This method deletes an item and all its children from the store. That is
	 * necessary because store.deleteItem() doesn't delete recursive.
	 */
	function removeTreeItem(/* dojo.data.item */ item) {
		// delete children
		var removeTreeItemFunc = dojo.hitch(this, removeTreeItem);
		while(hasChildrenLoaded(item)) {
			removeTreeItemFunc(item.children[0]);
		}
		// delete tree item
		this.store.deleteItem(item);
		// FIRE!!!!!
		this.eventHandler.notify({"type" : "itemRemoved", "item": item});
	}

	/**
	 * Checks if an item is an descendant of another item.
	 */
	function isDescendant(/*dojo.data.item*/ item, /*dojo.data.item*/ ancestor) {
		// same item
		if(item.id == ancestor.id)
			return false;
		if(ancestor.children && (typeof ancestor.children[0]) != "boolean") {
			for(var i = 0; i < ancestor.children.length; i++) {
				var childItem = ancestor.children[i];
				if(item.id == childItem.id || isDescendant(item, childItem)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get all items selected in tree.
	 */
	function getSelectedItems() {
		return this.tree.selectedItems;
	}
	
	classification.LazyLoadingTree.prototype.create = create;
	classification.LazyLoadingTree.prototype.update = update;
	classification.LazyLoadingTree.prototype.focusItem = focusItem;
	classification.LazyLoadingTree.prototype.addToSelected = addToSelected;
	classification.LazyLoadingTree.prototype.removeSelected = removeSelected;
	classification.LazyLoadingTree.prototype.getSelectedItems = getSelectedItems;
	classification.LazyLoadingTree.prototype.indexAt = indexAt;

})();
