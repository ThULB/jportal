dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.DateTextBox");
dojo.require("dojox.layout.TableContainer");
dojo.require("dijit.form.Select");

var COUNT = 0;
var extendedSearchOptions = {
	fulltxt: 1,
	year: 1,
	territories: 1
};

var Territories = [
   {label: "Sachsen-Weimar-Eisenach", value: "swe"},
   {label: "Sachsen-Altenburg", value: "sa"},
   {label: "Sachsen-Coburg", value: "sc"},
   {label: "Sachsen-Meiningen", value: "sm"},
   {label: "Reuß älterer Linie", value: "ral"},
   {label: "Reuß jüngerer Linie", value: "rjl"},
   {label: "Schwarzburg-Rudolstadt", value: "sr"},
   {label: "Schwarzburg-Sondershausen", value: "ss"}
];

var Joinparams = [
   {label: "mit ALLEN Wörtern", value: "AND"},
   {label: "mit IRGENDEINEM der Wörter", value: "OR"}
];

dojo.addOnLoad(init);


/*
 * show easysearch if not specified
 */
function init(searchType /* String */) {
	if (searchType == "extended") {
		var extendedSearch = new ExtendedSearch();
		extendedSearch.create();
	} else {
		var easySearch = new EasySearch();
		easySearch.create();
	}
}

/*
 * easysearch simple textbox for fulltext search
 */
EasySearch = function() {
	this.searchDiv = dojo.byId("searchWrap");
	this.searchBox = null;
	this.extendedSearchLink = null;
};
(function() {

	function create() {
		this.searchBox = new dijit.form.TextBox( {
			name : "searchterm",
			id : "searchterm",
			value : "",
			placeHolder : "enter your searchterm",
			style : {
				margin : "0 10 10 0px"
			}
		}, "search");

		this.extendedSearchLink = dojo.create("a", {
			innerHTML : "erweiterte Suche",
			onclick : dojo.hitch(this, function() {
				this.destroy();
				init("extended");
			})
		});

		dojo.place(this.searchBox.domNode, this.searchDiv, "only");
		dojo.place(this.extendedSearchLink, this.searchDiv, "last");
	}

	function destroy() {
		// destroy widgets
		this.searchBox.destroy();
		dojo.destroy(this.extendedSearchLink);
		// remove from dom
		dojo.destroy(this.domNode);
	}

	EasySearch.prototype.destroy = destroy;
	EasySearch.prototype.create = create;
})();

/*
 * extended search
 * drop textbox
 * create extended search mask with
 * createNewSearchSet()
 */
ExtendedSearch = function extendedSearch() {
	this.searchDiv = dojo.byId("searchWrap");

};
(function() {

	function create() {
//		this.addButton = new dijit.form.Button( {
//			label : "Suchmenge hinzufügen",
//			onClick : function() {
//				createNewSearchSet();
//			}
//		});
		this.easySearchLink = dojo.create("a", {
			innerHTML : "einfache Suche",
			onclick : dojo.hitch(this, function() {
				this.destroy();
				init();
			})
		});

		//dojo.place(this.addButton.domNode, this.searchDiv, "only");
		dojo.place(this.easySearchLink, this.searchDiv, "last");
		createNewSearchSet();
	}

	function destroy() {
		// destroy widgets
		this.addButton.destroy();
		dojo.destroy(this.easySearchLink);
		// remove from dom
		dojo.destroy(this.domNode);
	}

	ExtendedSearch.prototype.destroy = destroy;
	ExtendedSearch.prototype.create = create;
})();


// Searchset for several searchitems
// creates new object createSearchSet
function createNewSearchSet() {
	++COUNT;
	// we need the Wrapper div
	var searchDiv = dojo.byId("searchWrap");
	// creating a new SearchSet
	var searchSet = new SearchSet();
	// append the Set to DOM-tree
	dojo.place(searchSet.domNode, searchDiv, "last");
}

// creating a new searchset
// fulltext
// year
// territory
function SearchSet() {
	var instance = this;
	this.domNode = dojo.create("div");
	
	if (extendedSearchOptions.fulltxt == 1) {
		var fulltextDiv = dojo.create("div", {
			id : "volltextsuche",
			innerHTML : "<p>Volltextsuche</p>"
		});
		var fulltxt = new FulltextArrayEditor(COUNT);
		fulltextDiv.appendChild(fulltxt.domNode);
		this.domNode.appendChild(fulltextDiv);
	}
	
	if (extendedSearchOptions.territories == 1) {
		var terrDiv = dojo.create("div", {
			id : "territories",
			innerHTML : "<p>Territorien</p>"
		});
		var terr = new TerritoryArrayEditor(COUNT);
		terrDiv.appendChild(terr.domNode);
		this.domNode.appendChild(terrDiv);
	}

	if (extendedSearchOptions.year == 1) {
		var yearDiv = dojo.create("div", {
			id : "jahrgang",
			innerHTML : "<p>Jahrgänge</p>"
		});
		var year = new createYears(COUNT);
		yearDiv.appendChild(year.domNode);
		this.domNode.appendChild(yearDiv);
	}
	
//	this.deleteButton = new dijit.form.Button( {
//		label : "Suchmenge entfernen",
//		onClick : dojo.hitch(this, function() {
//			dojo.destroy(this.domNode);
//		})
//	});
//	this.domNode.appendChild(this.deleteButton.domNode);

}


function submitForm() {
	createJSON("search_form");
	
}


function createJSON(formID /*String*/) {
	if (typeof(formID) != "undefined") {
		var JSONfromForm = dojo.toJson(dojo.formToObject(formID));
		alert(dojo.toJson(JSONfromForm, true));
	}
}