/*########################################################################
#	F U L L T E X T
########################################################################*/
// provides control over all instances of SimpleArrayRow and a button for a new SimpleArrayRow
FulltextArrayEditor = function(count) {
	var instance = this;
	var rownumber = 0;
	this.domNode = dojo.create("div");
	this.disabled = false;
	this.rowList = new Array();

	// alle Woerter: AND
	// irgendeines der Woerter: OR
	this.fulltxtJoin = new dijit.form.Select({	
		name: "fulltxtJoin",
		id: "fulltxtJoin",
		options: Joinparams
	});

	this.addRowButton = new dijit.form.Button({
		label: "+",
		onClick: function() {
			instance.addRow("", count, ++rownumber);
		}
	});
	
	this.domNode.appendChild(this.addRowButton.domNode);
	this.domNode.appendChild(this.fulltxtJoin.domNode);
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
		var row = new SimpleFulltxtRow(value, this, count, row);
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

	FulltextArrayEditor.prototype.addRow = addRow;
	FulltextArrayEditor.prototype.removeRow = removeRow;
	FulltextArrayEditor.prototype.update = update;
	FulltextArrayEditor.prototype.getValues = getValues;
	FulltextArrayEditor.prototype.reset = reset;
})();


// provides a Simple Row with Label, Textbox and Deletebutton
SimpleFulltxtRow = function(/*String*/ value, arrayEditor, /*int*/ count, /*int*/ rownumber) {
	var instance = this;
	this.domNode = dojo.create("div");

	this.valueBox = new dijit.form.TextBox({
		value: value,
		name: "fulltxt"+count+"_"+rownumber,
		title: "Suchbegriff eintragen",
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

	SimpleFulltxtRow.prototype.update = update;
	SimpleFulltxtRow.prototype.destroy = destroy;
	SimpleFulltxtRow.prototype.setDisabled = setDisabled;
	SimpleFulltxtRow.prototype.getValue = getValue;
})();
