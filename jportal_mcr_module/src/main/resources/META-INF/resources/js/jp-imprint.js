var jp = jp || {};

jp.imprint = {

	get: function(/*string*/ objectID) {
		var notFound = false;
		var importID = $.ajax({
			url: jp.imprint.baseURL + "/get/" + objectID,
			dataType: "json",
			async: false,
			error: function(err) {
				if(err.status == 404) {
					notFound = true;
				} else if(err.status != 200) {
					alert("Das Impressum wurde aufgrund eines unbekannten Fehlers nicht geladen werden. Bitte informieren Sie den Administrator.");
					console.log(err);
				}
			}
		}).responseText;
		return notFound ? null : importID;
	},

	set: function(/*string*/ objectID, /*string*/ imprintID, /*function*/ onSuccess) {
		$.ajax({
			type: "POST",
			url: jp.imprint.baseURL + "/set?objID=" + objectID + "&imprintID=" + imprintID,
			error: function(err) {
				alert("Es ist ein Fehler beim setzen des Impressums aufgetreten. Bitte informieren Sie den Administrator.");
				console.log(err);
			}
		}).done(onSuccess);
	},

	retrieve: function(/*string*/ imprintID, /*function*/ onSuccess, /*function*/ onError) {
		$.ajax({
			url: jp.imprint.baseURL + "/retrieve/" + imprintID,
			dataType: "text"
		}).done(onSuccess).error(onError);
	},

	list: function(/*function*/ onSuccess, /*function*/ onError) {
		$.ajax({
			url: jp.imprint.baseURL + "/list",
			dataType: "json"
		}).done(onSuccess).error(onError);
	},
	
	remove: function(/*string*/ imprintID, /*function*/ onSuccess) {
		$.ajax({
			type: "DELETE",
			url: jp.imprint.baseURL + "/delete/" + imprintID
		}).done(onSuccess).error(function(err) {
			alert("Es ist ein Fehler beim löschen des Impressums aufgetreten. Bitte informieren Sie den Administrator.");
			console.log(err);
		});
	},

	save: function(/*string*/ imprintID, /*string*/ data, /*function*/ onSuccess, /*function*/ onError) {
		$.ajax({
			type: "POST",
			url: jp.imprint.baseURL + "/save/" + imprintID,
			data: data,
			contentType: 'text/plain; charset=UTF-8'
		}).done(onSuccess).error(onError);
	},

	baseURL: null,

	setWebApplicationBaseURL: function(/*string*/ webApplicationBaseURL) {
		jp.imprint.baseURL = webApplicationBaseURL + "rsc/imprint";
	}

};

jp.imprint.Select = function(/*string*/ journalID) {

	this.journalID = journalID;

	this.domNode = null;
	this.select = null;
	this.editButton = null;
	this.removeButton = null;

	this.render = function() {
		this.domNode = $("<div />");
		this.select = $("<select />");
		this.editButton = $("<i class='icon-pencil pointer' />")
		this.removeButton = $("<i class='icon-trash pointer' />")

		this.select.on('change', $.proxy(this.change, this));
		this.editButton.on('click', $.proxy(this.onEdit, this));
		this.removeButton.on('click', $.proxy(this.remove, this));

		this.domNode.append(this.select);
		this.domNode.append(this.editButton);
		this.domNode.append(this.removeButton);

		jp.imprint.list($.proxy(function(idList) {
			var imprintID = jp.imprint.get(this.journalID);
			if(imprintID == null) {
				this.removeButton.css("visibility", "hidden");
			}
			if(idList.length != 0) {
				this.select.append(this.getOption(null, "bitte wählen...", imprintID == null));
				for(var i = 0; i < idList.length; i++) {
					this.select.append(this.getOption(idList[i], idList[i], imprintID == idList[i]));
				}
			} else {
				this.select.html(this.getOption(null, "Keine Impressen gefunden", true));
			}
			this.onRender();
		}, this));
	};

	this.getOption = function(/* String */ value, /* String */ label, /* boolean */ selected) {
		return "<option value='" + value + "'" + (selected ? " selected='selected'" : "") + ">" + label + "</option>";
	};

	this.setValue = function(/*string*/ imprintID) {
		if(this.select.find("option[value='" + imprintID + "']").length <= 0) {
			this.select.append(this.getOption(imprintID, imprintID, true));
		}
		this.select.val(imprintID).trigger('change');
	};

	this.getValue = function() {
		var val = this.select.val();
		return val != "null" ? val : null;
	};

	this.change = function() {
		var imprintID = this.getValue();
		if(imprintID == null) {
			this.removeButton.css("visibility", "hidden");
		} else {
			this.removeButton.css("visibility", "visible");
		}
		jp.imprint.set(this.journalID, imprintID);
		this.onChange(imprintID);
	};

	this.remove = function() {
		var imprintID = this.getValue();
		if(imprintID == null || !confirm("Impressum wirklich löschen?")) {
			return;
		}
		jp.imprint.remove(imprintID, $.proxy(function() {
			this.select.find("option[value='" + imprintID + "']").remove();
			this.removeButton.css("visibility", "hidden");
			this.onRemove(imprintID);
		}, this));
	};

	this.has = function(/*string*/ imprintID) {
		return this.select.find("option[value='" + imprintID + "']").length > 0;
	};

	/***************************************************************************
	 * EVENTS
	 **************************************************************************/
	this.onRender = function() {};
	this.onChange = function(imprintID) {};
	this.onEdit = function() {};
	this.onRemove = function(imprintID) {};
};

jp.imprint.Editor = function(/*string*/ imprintID) {

	this.imprintID = imprintID;

	this.domNode = null;

	this.idInput = null;

	this.ckEditor = null;

	this.render = function() {
		if(this.imprintID != null) {
			jp.imprint.retrieve(this.imprintID, $.proxy(function(xmlContent) {
				this.build(xmlContent);
			}, this), $.proxy(function(err) {
				console.log(err);
				alert("Error while loading imprint. Please inform the administrator.");
				this.build();
			}, this));
		} else {
			this.build();
		}
		this.onRender();
	};

	this.build = function(/*string*/ xmlContent) {
		if(this.domNode != null) {
			this.domNode.empty();
		}
		this.domNode = $("<div />");
		this.onBeforeBuild();
		this.domNode.append($("<h3>Impressum bearbeiten</h3>"));
		this.idInput = $("<input name='imprintID' type='text' site='40' value='" + 
			(this.imprintID == null ? "" : this.imprintID) + "'/>");
		this.domNode.append("<span style='display: inline-block; padding: 0px 8px 12px 0px;'>Name:</span>").append(this.idInput);
	    this.ckEditor = $("<div class='ckeditor' id='imprintEditor' />").appendTo(this.domNode);
		if(xmlContent != null) {
			this.ckEditor.append(xmlContent);
		}
		this.ckEditor.ckeditor({
            resize_enabled : false,
            entities: false,
            enterMode: CKEDITOR.ENTER_BR,
            entities_processNumerical: 'force',
            tabSpaces: 4,
            fillEmptyBlocks: false,
            height : '500px',
            toolbar : [ ['Undo', 'Redo', '-', 'Bold', 'Italic', '-', 'NumberedList', 'BulletedList', '-',
                    'Link', 'Unlink', 'Source' ] ]
        });
		var buttonUl = $("<ul class='ckeditorButtons ckGUI'/>").appendTo(this.domNode);
		var cancelLi = $("<li />").appendTo(buttonUl);
		var cancelButton = $("<input type='button' value='Abbrechen' />").appendTo(cancelLi).on('click', $.proxy(function() {
			this.onCancel();
			this.close();
		}, this));
		var saveLi = $("<li />").appendTo(buttonUl);
		var saveButton = $("<input type='button' value='Speichern' />").appendTo(saveLi).on('click', $.proxy(function() {
			this.save($.proxy(function() {
				this.close();
			}, this));
		}, this));
	};

	this.update = function(/*string*/ imprintID) {
		if(this.imprintID == imprintID) {
			return;
		}
		this.imprintID = imprintID;
		this.idInput.val((imprintID != null) ? imprintID : "");
		if(imprintID != null) {
			$.attr(this.idInput, "value", imprintID);
			jp.imprint.retrieve(this.imprintID, $.proxy(function(xmlContent) {
				CKEDITOR.instances["imprintEditor"].setData(xmlContent);
			}, this));
		} else {
			CKEDITOR.instances["imprintEditor"].setData("");
		}
	};

	this.close = function() {
		this.domNode.empty();
		this.domNode = null;
		this.onClose();
	};

	this.save = function(/*function*/ onSuccess) {
		var oldID = this.imprintID;
		var newID = this.idInput.val();
		if(newID == null || newID == "") {
			alert("Bitte geben Sie einen Namen für das Impressum an.");
			this.idInput.focus();
			return false;
		}
		var data = this.ckEditor.ckeditorGet().getData();
		if(!this.onBeforeSave(newID, oldID, data)) {
			return false;	
		}
		this.imprintID = newID;
		jp.imprint.save(newID, data, $.proxy(function() {
			this.onSave(newID, oldID, data);
			onSuccess();
		}, this), function(err) {
			alert('Es ist ein Fehler beim speichern aufgetreten.');
			console.log(err);
		});
	};

	/***************************************************************************
	 * EVENTS
	 **************************************************************************/
	this.onRender = function() {};
	this.onBeforeBuild = function() {};
	this.onCancel = function() {};
	this.onBeforeSave = function(newImprintID, oldImprintID, data) {
		return true;
	};
	this.onSave = function(newImprintID, oldImprintID, data) {};
	this.onClose = function() {};

};
