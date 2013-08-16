var jp = jp || {};
$(document).ready(function(){
	var id = $('menu#jp-main-menu').attr('journalid');
	var infoFilesButtons = $('.jp-infoFiles-button');
	
	var masterCtr = new MasterController();
	
	infoFilesButtons.each(function(){
		var ctr = new InfoEditorCtr($(this));
		masterCtr.add(ctr);
	})
});

var MasterController = function(){
	var controllers = [];

	this.trigger = function(event){
		for (var i = 0; i < controllers.length; i++){
			if(controllers[i][event]){
				controllers[i][event]();
			}
		}
	}
	 
	this.add = function(ctr){
		ctr.setMasterCtr(this);
		controllers.push(ctr);
	} 
	 
	return this;
}

var InfoEditorCtr = function(buttonTag){
	var controller = this;
	var masterCtr = null;
	var buttonLabel = buttonTag.html();
	var type = buttonTag.attr('type');
	var journalID = buttonTag.attr('journalid');
	var containerID = buttonTag.attr('containerid');
	var backend = new FSConnector(type);
	var imprintID = backend.get(journalID);
	var selectBox = new Select();
	var editBox = new EditBox();
	var editor = new Editor(type);
	var button = $('<div>' + buttonLabel +'</div>');
	var imprintContainer = $("<div class='imprintContainer' />").appendTo("#" + containerID);
	
	buttonTag.html(button);
	
	controller.setMasterCtr = function(ctr){
		masterCtr = ctr;
	}
	
	controller.deactivateButton = function(){
		if(buttonTag.hasClass('active')){
			if(editor.hasChanged() && confirm('Änderung speichern?')){
				editor.saveButton.trigger('click');
			}else{
				editor.close();
			}
			
			buttonTag.removeClass('active');
			editBox.detach();
			buttonTag.html(button);
		}
	}
	
	function trigger(event){
		if(masterCtr != null){
			masterCtr.trigger(event);
		}
	}
	
	button.on('click', function(){
		trigger('deactivateButton');
		buttonTag.addClass('active');
		selectBox.empty();
		backend.list(function(idList){
	    	if(idList.length != 0) {
	    		editBox.toggleDel();
	    		selectBox.addOption(null, "bitte wählen... / neuer Eintrag", imprintID == null);
				for(var i = 0; i < idList.length; i++) {
					selectBox.addOption(idList[i], idList[i], imprintID == idList[i]);
				}
			} else {
				selectBox.addOption(null, "Keine Eintrag", true);
			}
	    	
	    	editBox.addSelectBox(selectBox);

	    	button.detach();
	    	buttonTag.html(editBox);
	    })
	});
	
	editBox.editButton.on('click', function(){
		var imprintID = selectBox.getValue();
		
		if(imprintID != null) {
			backend.retrieve(imprintID, function(xml) {
				editor.setContent(xml);
				editor.setID(imprintID);
			}, function(err) {
				console.log(err);
				alert("Error while loading. Please inform the administrator.");
			});
		} 
		
		editor.start();
		imprintContainer.append(editor);
	});
	
	editor.saveButton.on('click', function(){
		var newID = editor.getID();
		
		if(newID == null || newID == "") {
			alert("Bitte geben Sie einen Namen für den Eintrag an.");
			editor.idInput.focus();
			return false;
		}
		
		if(newID == imprintID && selectBox.has(newID)) {
			var confirmUpdate = confirm("Es existiert bereits ein Eintrag mit dem Namen '" + imprintID + "'. Wollen Sie den Eintrag überschreiben?");
			
			if(!confirmUpdate){
				return;
			}
		}
		
		var data = editor.getData();
		backend.save(newID, data, function() {
			selectBox.addOption(newID, newID, true);
			editor.close();
			
		}, function(err) {
			alert('Es ist ein Fehler beim speichern aufgetreten.');
			console.log(err);
		});
		backend.set(journalID, newID);
	});
	
	editor.cancelButton.on('click', function(){
		editor.close();
	})
	
	selectBox.on('change', function(){
		var newID = selectBox.getValue();
		if(newID != null){
			backend.set(journalID, newID);
		}
	})
	
	editBox.removeButton.on('click', function() {
		if(imprintID == null || !confirm("Eintrag wirklich löschen?")) {
			return;
		}
		backend.remove(imprintID, function() {
			editor.close();
			selectBox.delOption(imprintID);
		});
	});
	
	return controller;
}

var Editor = function(type){
	var title = {
			imprint : 'Impressum',
			partner : 'Partner'
	}
	var editorFrame = $("<div/>");
	var editorOverlay = $("<div/>").appendTo(editorFrame);
	var editorCenter = $("<div/>").appendTo(editorFrame);
	var editorNode = $("<div/>").appendTo(editorCenter);
	editorNode.append($("<h3>" + title[type] + " bearbeiten</h3>"));
	var idInput = $("<input type='text' site='40' />");
	editorNode.append("<span style='display: inline-block; padding: 0px 8px 12px 0px;'>Name:</span>").append(idInput);
    var ckEditor = $("<div class='ckeditor' id='imprintEditor'/>").appendTo(editorNode);
    var buttonUl = $("<ul class='ckeditorButtons ckGUI'/>").appendTo(editorNode);
    var cancelLi = $("<li />").appendTo(buttonUl);
    var cancelButton = $("<input type='button' value='Abbrechen' />").appendTo(cancelLi);
    var saveLi = $("<li />").appendTo(buttonUl);
    var saveButton = $("<input type='button' value='Speichern' />").appendTo(saveLi);
    
    editorOverlay.css({
    	width: '100%',
    	height: '100%',
    	position: 'fixed',
    	top: '0',
    	left: '0',
    	'z-index': '3000',
    	background: 'black',
    	 filter: 'alpha(opacity=50)',
        '-moz-opacity': '0.5',
        '-khtml-opacity': '0.5', 
    	opacity: '0.5'
    });
    
    editorCenter.css({
    	position: 'absolute',
    	top: '50%',
    	left: '50%',
    	'z-index': '5000'
    });
    
    editorNode.css({
    	position: 'relative',
    	'margin-left': '-329px',
    	'margin-top': '-300px',
    	width: '658px',
    	background: 'white',
    	padding: '20px'
    });
    
    editorFrame.start = function(){
    	ckEditor.ckeditor({
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
    }
    
    editorFrame.setContent = function(xml){
    	if(xml != null){
    		ckEditor.append(xml);
    	}
    }
    
    editorFrame.setID = function(id){
    	idInput.val(id);
    }
    
    editorFrame.getData = function(){
    	return ckEditor.ckeditorGet().getData();
    }
    
    editorFrame.getID = function(){
    	return idInput.val();
    }
    
    editorFrame.close = function(){
    	var ckEditorInst = CKEDITOR.instances['imprintEditor'];
    	
    	if(ckEditorInst){
    		ckEditorInst.destroy();
    		editorFrame.remove();
    	}
    }
    
    editorFrame.hasChanged = function(){
    	var ckEditorInst = CKEDITOR.instances['imprintEditor'];
    	
    	if(ckEditorInst){
    		return ckEditorInst.checkDirty();
    	}
    	
    	return false;
    }
    
    editorFrame.cancelButton = cancelButton;
    editorFrame.saveButton = saveButton;
    editorFrame.idInput = idInput;
    
    return editorFrame;
}

var EditBox = function(){
	var domNode = $("<div class='editBox'/>");
	var editButton = $("<i class='editButton icon-pencil pointer' />").appendTo(domNode);
	var removeButton = $("<i class='removeButton icon-trash pointer'/>");
	
	domNode.addSelectBox = function(selectBox){
		editButton.before(selectBox);
	}
	
	domNode.toggleDel = function(){
		domNode.append(removeButton);
	}
	
	domNode.setData = function(data){
		editButton.data('data', data);
	}
	
	domNode.editButton = editButton;
	domNode.removeButton = removeButton;
	
	return domNode;
}

var Select = function(){
	var select = $("<select />");
	
	select.addOption = function(/* String */ value, /* String */ label, /* boolean */ selected){
		var newOption = $("<option" + (selected ? " selected='selected'" : "") + "/>");
		newOption.val(value);
		newOption.text(label);
		select.append(newOption);
	}
	
	select.delOption = function(/*string*/ imprintID){
		select.find("option[value='" + imprintID + "']").remove();
	}
	
	select.getValue = function(){
		var val = select.find(":selected").val();
		return val != null ? val : null;
	}
	
	return select;
}

var FSConnector = function(/*string*/ type){
	var baseURL = "/rsc/fs/" + type;
	
	return {
		get: function(/*string*/ objectID) {
			var notFound = false;
			var importID = $.ajax({
				url: baseURL + "/get/" + objectID,
				dataType: "json",
				async: false,
				contentType: 'text/plain; charset=UTF-8',
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
				url: baseURL + "/set?objID=" + objectID,
				data: imprintID,
				contentType: 'text/plain; charset=UTF-8',
				error: function(err) {
					alert("Es ist ein Fehler beim setzen des Impressums aufgetreten. Bitte informieren Sie den Administrator.");
					console.log(err);
				}
			}).done(onSuccess);
		},

		retrieve: function(/*string*/ imprintID, /*function*/ onSuccess, /*function*/ onError) {
			$.ajax({
				url: baseURL + "/retrieve/" + imprintID,
				dataType: "text"
			}).done(onSuccess).error(onError);
		},

		list: function(/*function*/ onSuccess, /*function*/ onError) {
			$.ajax({
				url: baseURL + "/list",
				dataType: "json",
				contentType: 'application/json; charset=UTF-8'
			}).done(onSuccess).error(onError);
		},
		
		remove: function(/*string*/ imprintID, /*function*/ onSuccess) {
			$.ajax({
				type: "DELETE",
				url: baseURL + "/delete/" + imprintID
			}).done(onSuccess).error(function(err) {
				alert("Es ist ein Fehler beim löschen des Impressums aufgetreten. Bitte informieren Sie den Administrator.");
				console.log(err);
			});
		},

		save: function(/*string*/ imprintID, /*string*/ data, /*function*/ onSuccess, /*function*/ onError) {
			$.ajax({
				type: "POST",
				url: baseURL + "/save",
				data: JSON.stringify({imprintID: imprintID, content: data}),
				dataType: 'json',
				contentType: 'application/json; charset=UTF-8'
			}).done(onSuccess).error(onError);
		}
	}
}