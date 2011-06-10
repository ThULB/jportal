/*
 * @package classification
 */
var classification = classification || {};

/**
 * 
 */
classification.LazyLoadingTree = function(/*String*/ targetURL) {
	this.targetURL = targetURL;

	// tree
	this.tree = null;
	this.store = null;
	this.treeModel = null;
	// event
	this.eventHandler = new classification.EventHandler(this);
};

( function() {

	function create() {
		var xhrArgs = {
			url :  this.targetURL,
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
		this.store = dojoclasses.SimpleRESTStore({
			targetURL: this.targetURL + "/",
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
			getLabel: dojo.hitch(this, getLabel),
			getIconClass: dojo.hitch(this, getIconClass),
			checkItemAcceptance: dojo.hitch(this, checkItemAcceptance),
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
		if(treeItem.labels)
			return treeItem.labels[0].text;
		if(treeItem.root)
			return "Klassifikationen";
		return "undefined";
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
		this.eventHandler.notify({"type" : "itemSelected", "item": treeItem});
	}

	function update(/*dojo.data.item*/ item, /*String*/ attribute, /*Object*/ value) {
		this.store.setValue(item, attribute, value);
	}

	classification.LazyLoadingTree.prototype.create = create;
	classification.LazyLoadingTree.prototype.update = update;
	
})();
