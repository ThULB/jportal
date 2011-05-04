dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.DateTextBox");
dojo.require("dojox.layout.TableContainer");

dojo.addOnLoad(init);
dojo.addOnLoad(createNewSearchSet);

var COUNT = 0;

/* init function
 * add single addButton
 */
function init(){
	var searchDiv = dojo.byId("searchWrap");
	var addButton = new dijit.form.Button({
		label: "Suchmenge hinzufügen",
		onClick: function() {
			createNewSearchSet();
		}
	});
	dojo.place(addButton.domNode, searchDiv, "first");
}

// Searchset for several searchitems
// creates new object createSearchSet
function createNewSearchSet(){
	++COUNT;
	// we need the Wrapper div
	var searchDiv = dojo.byId("searchWrap");
	// creating a new SearchSet
	var searchSet = new SearchSet();
	// append the Set to DOM-tree
	dojo.place(searchSet.domNode, searchDiv, "last");
}


// creating a new searchset
// 		fulltext
//		year
//		territory
function SearchSet(){
	var instance = this;
	this.domNode = dojo.create("div");
	this.domNode = dojo.create("blockquote");
	var table = dojo.create("table");
	var tbody = dojo.create("tbody");
	var tr1 = dojo.create("tr");
	var td1 = dojo.create("td");
	var td2 = dojo.create("td");
	var td3 = dojo.create("td");

	var fulltextDiv = dojo.create("div", { id: "volltextsuche", innerHTML: "<p>Volltextsuche</p>" });
	var fulltxt = new SimpleArrayEditor(COUNT);
	fulltextDiv.appendChild(fulltxt.domNode);
	td1.appendChild(fulltextDiv);

	var yearDiv = dojo.create("div", { id: "jahrgang", innerHTML: "<p>Jahrgänge</p>" });
	var year = new createYears(COUNT);
	yearDiv.appendChild(year.domNode);
	td2.appendChild(yearDiv);

	var terrDiv = dojo.create("div", { id: "terr", innerHTML: "<p>Territorien</p>" });
	//terrDiv.appendChild(createTerritories());
	//td3.appendChild(terrDiv);

	this.deleteButton = new dijit.form.Button({
		label: "Suchmenge entfernen",
		onClick: dojo.hitch(this, function() {
			dojo.destroy(this.domNode);
		})
	});

	tr1.appendChild(td1);
	tr1.appendChild(td2);
	tr1.appendChild(td3);
	tbody.appendChild(tr1);
	table.appendChild(tbody);
	this.domNode.appendChild(table);
	this.domNode.appendChild(this.deleteButton.domNode);
}



/*########################################################################
#	Y E A R I N T E R V A L
########################################################################*/
function createYears(/*int*/ count){
	var instance = this;
	this.domNode = dojo.create("div");
	var table = dojo.create("table");
	var tbody = dojo.create("tbody");
	var tr1 = dojo.create("tr");
	var tr2 = dojo.create("tr");
	var td1 = dojo.create("td");
	var td2 = dojo.create("td");
	var td3 = dojo.create("td");
	var td4 = dojo.create("td");

	var von = new dijit.form.DateTextBox({name: "yearFrom"+count, onChange: function(){dijit.byId('yearUntil'+count).constraints.min = arguments[0];} });
	td2.appendChild(von.domNode);
	dojo.create("label", { innerHTML: "von" }, td1);
	tr1.appendChild(td1);
	tr1.appendChild(td2);
	var bis = new dijit.form.DateTextBox({name: "yearUntil"+count, onChange: function(){dijit.byId('yearFrom'+count).constraints.min = arguments[0];} });
	td4.appendChild(bis.domNode);
	dojo.create("label", { innerHTML: "bis" }, td3);
	tr2.appendChild(td3);
	tr2.appendChild(td4);

	tbody.appendChild(tr1);
	tbody.appendChild(tr2);
	table.appendChild(tbody);
	this.domNode.appendChild(table);
	
};
( function() {
	// falls "alle Jahrgänge" angewählt wird, sollen die Datumsfelder verschwinden und leer sein.
	function hideYearInterval(checked) {
		if ( checked ) {
			dojo.attr("YearInterval", "style", {
	            display: "none"
	        });
			var w = dijit.byId('yearFrom');
	        w.attr('value', '');
			w = dijit.byId('yearUntil');
	        w.attr('value', '');
		} else {
			dojo.attr("YearInterval", "style", {
	            display: "block"
	        });
			var w = dijit.byId('yearFrom');
	        w.attr('value', '1805-01-01');
			w = dijit.byId('yearUntil');
	        w.attr('value', '1805-12-30');
		}
	}
	createYears.prototype.hideYearInterval = hideYearInterval;
})();


/*########################################################################
#	T E R I T O R R I E S
########################################################################*/

//Searchset for territories
function createTerritories(){
	var terrDiv = dojo.create("div", { id: "territorien", innerHTML: "<p>Territorien</p>" });
	var terrList = new TerritoriesList();
	terrDiv.appendChild(terrList.domNode);
}

TerritoriesList = function() {
	var instance = this;
	this.domNode = dojo.create("div");

	var label = "Sachsen-Weimar-Eisenach";
	var checkBox = new dijit.form.CheckBox({name: "tt", value: label,  checked: false}, "checkBox");
	this.domNode.appendChild(checkBox.domNode);
	this.domNode.appendChild("<label>"+label+"</label><br>");
	/*label = "Sachsen-Altenburg";
	checkBox = new dijit.form.CheckBox({name: "tt", value: label,  checked: false}, "checkBox");
	this.domNode.appendChild(checkBox.domNode);
	dojo.place("<label>"+label+"</label><br>", this.domNode, "last");
	label = "Sachsen-Coburg";
	checkBox = new dijit.form.CheckBox({name: "tt", value: label,  checked: false}, "checkBox");
	this.domNode.appendChild(checkBox.domNode);
	dojo.place("<label>"+label+"</label><br>", this.domNode, "last");
	label = "Sachsen-Meiningen";
	checkBox = new dijit.form.CheckBox({name: "tt", value: label,  checked: false}, "checkBox");
	this.domNode.appendChild(checkBox.domNode);
	dojo.place("<label>"+label+"</label><br>", this.domNode, "last");
	label = "Reuß älterer Linie";
	checkBox = new dijit.form.CheckBox({name: "tt", value: label,  checked: false}, "checkBox");
	this.domNode.appendChild(checkBox.domNode);
	dojo.place("<label>"+label+"</label><br>", this.domNode, "last");
	label = "Reuß jüngerer Linie";
	checkBox = new dijit.form.CheckBox({name: "tt", value: label,  checked: false}, "checkBox");
	this.domNode.appendChild(checkBox.domNode);
	dojo.place("<label>"+label+"</label><br>", this.domNode, "last");
	label = "Schwarzburg-Rudolstadt";
	checkBox = new dijit.form.CheckBox({name: "tt", value: label,  checked: false}, "checkBox");
	this.domNode.appendChild(checkBox.domNode);
	dojo.place("<label>"+label+"</label><br>", this.domNode, "last");
	label = "Schwarzburg-Sondershausen";
	checkBox = new dijit.form.CheckBox({name: "tt", value: label,  checked: false}, "checkBox");
	this.domNode.appendChild(checkBox.domNode);
	dojo.place("<label>"+label+"</label><br><br>", this.domNode, "last");

	var AllButton = new dijit.form.Button({name: "all", label: "alle Territorien",  checked: false, onClick: function() {selectAllTerritories();} });
	this.domNode.appendChild(AllButton.domNode);

	var InvertButton = new dijit.form.Button({name: "invert", label: "Auswahl umkehren",  checked: false, onClick: function() {reverseTerritoriesSelects(arguments[0]);}});
	this.domNode.appendChild(InvertButton.domNode);*/
	
	//this.domNode.appendChild(this.container.domNode);
};
( function() {

	// alle Territorien anwählen
	function selectAllTerritories(checked) {
		dojo.query('input[name="tt"]').forEach(function(node, index, arr){
			dijit.byId(node.id).attr('checked', true);
		});
	}

	// Auswahl der Territorien umkehren 
	function reverseTerritoriesSelects(checked) {
		dojo.query('input[name="tt"]').forEach(function(node, index, arr){
			var checked = (node.checked) ? false : true;
			dijit.byId(node.id).attr('checked', checked);
		});

	}
	
	// reset all Checkboxes
	function reset() {
		while(this.rowList.length > 0) {
			var row = this.rowList.pop();
			row.destroy();
		}
	}

	function getCount() {
		if (typeof rowList != 'undefined'){
			for(var i = 0; i < this.rowList.length; i++){
				if(this.rowList[i] == row){
					break;
					return i;
				} else {
					return -1;
				}
			}
		} else {
			return 0;
		}
	}

	TerritoriesList.prototype.reset = reset;
	TerritoriesList.prototype.getCount = getCount;
})();



/*########################################################################
#	F U L L T E X T
########################################################################*/
// provides control over all instances of SimpleArrayRow and a button for a new SimpleArrayRow
SimpleArrayEditor = function(count) {
	var instance = this;
	var rownumber = 0;
	this.domNode = dojo.create("div");
	this.disabled = false;
	this.rowList = new Array();
	
	this.addRowButton = new dijit.form.Button({
		label: "+",
		onClick: function() {
			instance.addRow("", count, ++rownumber);
		}
	});
	
	this.domNode.appendChild(this.addRowButton.domNode);
	instance.addRow("", count, ++rownumber);
};
( function() {
	function update(/*Array*/ valueArray) {
		// add and update rows
		var internalCount = 0;
		for (var i = 0; i < valueArray.length; i++) {
			if(internalCount < this.rowList.length) {
				var row = this.rowList[internalCount];
				row.update(valueArray[i]);
			} else
				this.addRow(valueArray[i]);
			internalCount++;
		}
		// remove rows
		while(this.rowList.length > internalCount) {
			var row = this.rowList.pop();
			row.destroy();
		}
	}

	function getValues() {
		var returnArray = new Array();
		for(var i = 0; i < this.rowList.length; i++) {
			var row = this.rowList[i];
			returnArray[i] = row.getValue();
		}
		return returnArray;
	}

	function reset() {
		while(this.rowList.length > 0) {
			var row = this.rowList.pop();
			row.destroy();
		}
	}

	function addRow(/*String*/ value, /*int*/ count, /*int*/ row) {
		var row = new SimpleArrayRow(value, this, count, row);
		dojo.place(row.domNode, this.addRowButton.domNode, "before");
		this.rowList.push(row);
		return row;
	}

	function removeRow(/*Row*/ row) {
		var getRowNumberFunc = dojo.hitch(this, getRowNumber);
		var rowNumber = getRowNumberFunc(row); 
		var rest1 = this.rowList.slice(0, rowNumber);
		var rest2 = this.rowList.slice(rowNumber + 1);
		this.rowList = rest1.concat(rest2);
		row.destroy();
		row = null;
	}

	function getRowNumber(/*Row*/ row) {
		if (typeof rowList != 'undefined'){
			for(var i = 0; i < this.rowList.length; i++){
				if(this.rowList[i] == row){
					break;
					return i;
				} else {
					return -1;
				}
			}
		} else {
			return 0;
		}
	}

	SimpleArrayEditor.prototype.addRow = addRow;
	SimpleArrayEditor.prototype.removeRow = removeRow;
	SimpleArrayEditor.prototype.update = update;
	SimpleArrayEditor.prototype.getValues = getValues;
	SimpleArrayEditor.prototype.reset = reset;
})();


// provides a Simple Row with Label, Textbox and Deletebutton
SimpleArrayRow = function(/*String*/ value, arrayEditor, /*int*/ count, /*int*/ rownumber) {
	var instance = this;
	var label =  "Suche nach:";
	this.domNode = dojo.create("div");
    dojo.place("<label>"+label+" </label> ", this.domNode, "first");

	this.valueBox = new dijit.form.TextBox({
		value: value,
		name: "fulltxt"+count+"_"+rownumber,
		title: "Suchbegriff " + count+"_"+rownumber,
	});
	
	this.removeButton = new dijit.form.Button({
		label: "-",
		onClick: function() {
			console.log("remove clicked");
			arrayEditor.removeRow(instance);
		}
	});
	this.domNode.appendChild(this.valueBox.domNode);
	this.domNode.appendChild(this.removeButton.domNode);
};
( function() {

	function destroy() {
		// destroy widgets
		this.valueBox.destroy();
		this.removeButton.destroy();
		// remove from dom
		dojo.destroy(this.domNode);
	}

	function update(/*String*/ lang, /*String*/ label) {
		this.valueBox.set("value", label);
	}

	function setDisabled(/*boolean*/ value) {
		this.valueBox.set("disabled", value);
		this.removeButton.set("disabled", value);
	}
	
	function getValue() {
		return this.valueBox.get("value");
	}

	SimpleArrayRow.prototype.update = update;
	SimpleArrayRow.prototype.destroy = destroy;
	SimpleArrayRow.prototype.setDisabled = setDisabled;
	SimpleArrayRow.prototype.getValue = getValue;
})();