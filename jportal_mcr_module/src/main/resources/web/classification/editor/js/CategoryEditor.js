/*
 * @package classification
 */
var classification = classification || {};

/**
 * 
 */
classification.CategoryEditor = function() {
	var instance = this;
	this.domNode = dojo.create("div");
	this.table = dojo.create("table");

	this.disabled = false;
	this.addRowButton = new dijit.form.Button({
		iconClass: "icon12 addIcon12",
		showLabel: false,
		onClick: function() {
			instance.addRow("de", "", "");
		}
	});
	this.rowList = new Array();

	this.eventHandler = new classification.EventHandler(this);
};

( function() {

	function create() {
		this.domNode.appendChild(this.table);
		// create table header
		var tr = dojo.create("tr");
		var langTH = dojo.create("th", {innerHTML: "Sprache"});
		var textTH = dojo.create("th", {innerHTML: "Text"});
		var descTH = dojo.create("th", {innerHTML: "Beschreibung"});
		tr.appendChild(langTH);
		tr.appendChild(textTH);
		tr.appendChild(descTH);
		this.table.appendChild(tr);

		// add add-button
		this.domNode.appendChild(this.addRowButton.domNode);

		// add one row as default
		this.addRow("de", "", "");
	}

	function update(/*Array*/ labels) {
		// add and update rows
		var internalCount = 0;
		for (var internalCount = 0; internalCount < labels.length; internalCount++) {
			var label = labels[internalCount];
			this.updateLangSelect(label.lang);
			if(internalCount < this.rowList.length) {
				var row = this.rowList[internalCount];
				row.update(label.lang, label.text, label.description);
			} else {
				this.addRow(label.lang, label.text, label.description);
			}
		}
		// remove rows
		while(this.rowList.length > internalCount) {
			var row = this.rowList.pop();
			row.destroy();
		}
	}

	function getValues() {
		var labels = [];
		for(var i = 0; i < this.rowList.length; i++) {
			var row = this.rowList[i];
			var lang = row.langSelect.get("value");
			var text = row.textBox.get("value");
			if(lang == null || lang == "" || text == null || text == "")
				continue;
			var desc = row.descBox.get("value");
			var label = {
				lang: lang,
				text: text
			};
			if(desc != null && desc != "") {
				label.description = desc;
			}
			labels.push(label);
		}
		return labels;
	}

	function reset() {
		while(this.rowList.length > 0) {
			var row = this.rowList.pop();
			row.destroy();
		}
	}

	function addRow(/*String*/ lang, /*String*/ text, /*String*/ desc) {
		var removeable = this.rowList.length > 0;
		this.updateLangSelect(lang);
		var row = new classification.CategoryEditor.Row(lang, text, desc, removeable, this);
		row.create();
		dojo.place(row.domNode, this.table);
		this.rowList.push(row);
		this.eventHandler.notify({"type" : "categoryAdded", "row": row});
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
		this.eventHandler.notify({"type" : "categoryRemoved", "row": row});
	}

	function setDisabled(/*Boolean*/ disabled) {
		this.disabled = disabled;
		this.addRowButton.set("disabled", disabled);
		for(var i = 0; i < this.rowList.length; i++) {
			var row = this.rowList[i];
			row.setDisabled(disabled);
		}
	}

	function getRowNumber(/*Row*/ row) {
		for(var i = 0; i < this.rowList.length; i++)
			if(this.rowList[i] == row)
				return i;
		return -1;
	}

	function updateLangSelect(/*String*/ lang) {
		var im = SimpleI18nManager.getInstance();
		if(!im.isSupportedLanguage(lang)) {
			im.addSupportedLanguage(lang);
			// update existing rows
			for(var i = 0; i < this.rowList.length; i++) {
				var row = this.rowList[i];
				row.langSelect.addOption({value: lang, label: lang});
			}
		}
	}

	/**
	 * Fires a categoryChanged event. Possible types are:
	 * -lang
	 * -text
	 * -description
	 */
	function categoryChanged(/*Row*/ row, /*String*/ editType, /*String*/ value) {
		this.eventHandler.notify({"type" : "categoryChanged", "row": row, "editType" : editType, "value": value});
	}

	classification.CategoryEditor.prototype.create = create;
	classification.CategoryEditor.prototype.addRow = addRow;
	classification.CategoryEditor.prototype.removeRow = removeRow;
	classification.CategoryEditor.prototype.update = update;
	classification.CategoryEditor.prototype.getValues = getValues;
	classification.CategoryEditor.prototype.reset = reset;
	classification.CategoryEditor.prototype.setDisabled = setDisabled;
	classification.CategoryEditor.prototype.categoryChanged = categoryChanged;
	classification.CategoryEditor.prototype.updateLangSelect = updateLangSelect;

})();

classification.CategoryEditor.Row = function Row(/*String*/ lang, /*String*/ text, /*String*/ desc, /*boolean*/ removeable,/*classification.CategoryEditor*/ categEditor) {
	this.domNode = dojo.create("tr");

	this.lang = lang;
	this.text = text;
	this.desc = desc;
	this.removeable = removeable;
	this.categoryEditor = categEditor;
	
	this.langSelect = null;
	this.textBox = null;
	this.descBox = null;
	this.removeButton = null;
};

( function() {

	function create() {
		this.langSelect = new dijit.form.Select({
			onChange: dojo.hitch(this, function(/*String*/ newLang) {
				this.categoryEditor.categoryChanged(this, "lang", newLang);
			})
		});
		var im = SimpleI18nManager.getInstance();
		var supportedLanguages = im.getSupportedLanguages();
		for(var i = 0; i < supportedLanguages.length; i++) {
			this.langSelect.addOption({value: supportedLanguages[i], label: supportedLanguages[i]});
		}
		this.langSelect.set("value", this.lang);

		this.textBox = new dijit.form.TextBox({
			value: this.text,
			intermediateChanges: true,
			onChange: dojo.hitch(this, function(/*String*/ newText) {
				this.categoryEditor.categoryChanged(this, "text", newText);
			})
		});
//		this.textBox.set("class", "i18nLang");

		this.descBox = new dijit.form.TextBox({
			value: this.desc,
			intermediateChanges: true,
			onChange: dojo.hitch(this, function(/*String*/ newDesc) {
				this.categoryEditor.categoryChanged(this, "description", newDesc);
			})
		});
//		this.descBox.set("class", "i18nLabel");

		var langTD = dojo.create("td", {}, this.domNode);
		var textTD = dojo.create("td", {}, this.domNode);
		var descTD = dojo.create("td", {}, this.domNode);
		langTD.appendChild(this.langSelect.domNode);
		textTD.appendChild(this.textBox.domNode);
		descTD.appendChild(this.descBox.domNode);

		if(this.removeable) {
			this.removeButton = new dijit.form.Button({
				iconClass: "icon12 removeIcon12",
				showLabel: false,
				onClick: dojo.hitch(this, function() {
					this.categoryEditor.removeRow(instance);
				})
			});
			var rmBtTD = dojo.create("td", {}, this.domNode);
			rmBtTD.appendChild(this.removeButton.domNode);
		}
	}

	function destroy() {
		// destroy widgets
		this.langSelect.destroy();
		this.textBox.destroy();
		this.descBox.destroy();
		this.removeButton.destroy();
		// remove from dom
		dojo.destroy(this.domNode);
	}

	function update(/*String*/ lang, /*String*/ text, /*String*/ desc) {
		this.langSelect.set("value", lang);
		this.textBox.set("value", text == undefined ? null : text);
		this.descBox.set("value", desc == undefined ? null : desc);
	}

	function setDisabled(/*boolean*/ value) {
		this.langSelect.set("disabled", value);
		this.textBox.set("disabled", value);
		this.descBox.set("disabled", value);
		if(this.removeButton)
			this.removeButton.set("disabled", value);
	}
	
	classification.CategoryEditor.Row.prototype.create = create;
	classification.CategoryEditor.Row.prototype.update = update;
	classification.CategoryEditor.Row.prototype.destroy = destroy;
	classification.CategoryEditor.Row.prototype.setDisabled = setDisabled;
})();