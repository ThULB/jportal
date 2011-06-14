/*
 * @package classification
 */
var classification = classification || {};

/**
 * 
 */
classification.TreePane = function() {
	// dom
	this.mainPane = new dijit.layout.ContentPane({
		gutters: false,
		splitter: true
	});
	this.treePane = null;
	this.toolbar = null;

	// buttons
	this.addTreeItemButton = null;
	this.removeTreeItemButton = null;

	// tree
	this.tree = null;
};

( function() {

	function create(/*String*/ classBaseURL) {
		// create tree
		this.tree = new classification.LazyLoadingTree(classBaseURL);
		this.tree.eventHandler.attach(dojo.hitch(this, handleTreeEvents));

		// create dom
		var createDomFunc = dojo.hitch(this, createDom);
		createDomFunc();
	}

	function loadClassification(/*String*/ classificationID) {
		this.tree.create(classificationID);
	}

	function handleTreeEvents(/*LazyLoadingTree*/ source, /*JSON*/ args) {
		if(args.type == "treeCreated") {
			var addTreeToDOMFunc = dojo.hitch(this, addTreeToDOM);
			addTreeToDOMFunc();
		}
	}

	function createDom() {
		// border container
		var treeContainer = new dijit.layout.BorderContainer({
			id: "navigationTreeContainer",
			gutters: false,
			splitter: false
		});
		// tree
		this.treePane = new dijit.layout.ContentPane({
			id: "navigationTreePane",
			region: "center",
			gutters: false,
			splitter: false
		});

		// toolbar
		this.toolbar = new dijit.Toolbar({
			id: "navigationTreeToolbar",
			region: "bottom",
			splitter: false
		});
		var createToolbarFunc = dojo.hitch(this, createToolbar);
		createToolbarFunc();

		// add to dom
		this.mainPane.set("content", treeContainer);
		treeContainer.addChild(this.treePane);
		treeContainer.addChild(this.toolbar);

		// add loading gif
		var loading = dojo.create("div");
		dojo.style(loading, {
			"backgroundImage": "url('images/loading.gif')",
			"backgroundPosition": "center center",
			"backgroundRepeat": "no-repeat",
			"height" : "100%"
		});
		this.treePane.set("content", loading);
	}

	function addTreeToDOM() {
		// surrounding div fixes bug 10585 @see
		// http://bugs.dojotoolkit.org/ticket/10585
		// TODO: enable this for correct dnd support -> check scrollbars!!
//		var surroundingTreeDiv = dojo.create("div");
//		surroundingTreeDiv.appendChild(this.tree.tree.domNode);
		this.treePane.set("content", this.tree.tree.domNode);
		this.tree.tree.startup();
	}

	function createToolbar() {
		var addMenu = new dijit.Menu();

	    // toolbar buttons
		this.addTreeItemButton = new dijit.form.Button({
			showLabel: false, iconClass: "icon16 addDisabledIcon16", disabled: true,
			onClick: dojo.hitch(this, function() {
				console.log("todo add on click");
			})
		});
		this.removeTreeItemButton = new dijit.form.Button({
			showLabel: false, iconClass: "icon16 removeDisabledIcon16", disabled: true
		});

		// hierarchy
		this.toolbar.addChild(this.addTreeItemButton);
		this.toolbar.addChild(this.removeTreeItemButton);
	}

	classification.TreePane.prototype.create = create;
	classification.TreePane.prototype.loadClassification = loadClassification;

})();