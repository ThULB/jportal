/*########################################################################
#	T E R I T O R R I E S
########################################################################*/

TerritoryArrayEditor = function(count) {
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

	function addRow(/*int*/ count, /*int*/ row) {
		var row = new SimpleTerrRow(this, count, row);
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

	TerritoryArrayEditor.prototype.addRow = addRow;
	TerritoryArrayEditor.prototype.removeRow = removeRow;
	TerritoryArrayEditor.prototype.update = update;
	TerritoryArrayEditor.prototype.getValues = getValues;
	TerritoryArrayEditor.prototype.reset = reset;
})();


// provides a Simple Row with Label, Textbox and Deletebutton
SimpleTerrRow = function(arrayEditor, /*int*/ count, /*int*/ rownumber) {
	var instance = this;
	this.domNode = dojo.create("div");

	this.valueBox = new dijit.form.Select({	
		name: "terr"+count+"_"+rownumber,
		title: "Territorium auswählen",
		options: Territories
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

	SimpleTerrRow.prototype.update = update;
	SimpleTerrRow.prototype.destroy = destroy;
	SimpleTerrRow.prototype.setDisabled = setDisabled;
	SimpleTerrRow.prototype.getValue = getValue;
})();





//Searchset for territories
//function createTerritories(){
//	var instance = this;
//	this.domNode = dojo.create("div", { id: "territorien", innerHTML: "<p>Territorien</p>" });
//
//
//	var AllButton = new dijit.form.Button({name: "all", label: "alle Territorien",  checked: false, onClick: function() {selectAllTerritories();} });
//	this.domNode.appendChild(AllButton.domNode);
//
//	var InvertButton = new dijit.form.Button({name: "invert", label: "Auswahl umkehren",  checked: false, onClick: function() {reverseTerritoriesSelects(arguments[0]);}});
//	this.domNode.appendChild(InvertButton.domNode);*/
//
//};
//( function() {
//
//	// alle Territorien anwählen
//	function selectAllTerritories(checked) {
//		dojo.query('input[name="tt"]').forEach(function(node, index, arr){
//			dijit.byId(node.id).attr('checked', true);
//		});
//	}
//
//	// Auswahl der Territorien umkehren 
//	function reverseTerritoriesSelects(checked) {
//		dojo.query('input[name="tt"]').forEach(function(node, index, arr){
//			var checked = (node.checked) ? false : true;
//			dijit.byId(node.id).attr('checked', checked);
//		});
//
//	}
//	
//	// reset all Checkboxes
//	function reset() {
//		while(this.rowList.length > 0) {
//			var row = this.rowList.pop();
//			row.destroy();
//		}
//	}
//
//	function getCount() {
//		if (typeof rowList != 'undefined'){
//			for(var i = 0; i < this.rowList.length; i++){
//				if(this.rowList[i] == row){
//					break;
//					return i;
//				} else {
//					return -1;
//				}
//			}
//		} else {
//			return 0;
//		}
//	}
//
//	createTerritories.prototype.reset = reset;
//	createTerritories.prototype.getCount = getCount;
//})();