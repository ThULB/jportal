var jp = jp || {};
$(document).ready(function(){
	var id = $('menu#jp-main-menu').attr('journalid');
	var infoFilesButtons = $('.jp-infoFiles-button');
	
	var masterCtr = new MasterController();
	
//	infoFilesButtons.each(function(){
//		var ctr = new InfoEditorCtr($(this));
//		masterCtr.add(ctr);
//	})
	
	infoFilesButtons.each(function(){
		initStartEditorButton($(this));
	})
	
	function Controller(){
		
	}
	
	function initStartEditorButton(button){
		var buttonLabel = button.html();
		var type = button.attr('type');
		var journalID = button.attr('journalid');
		var containerID = button.attr('containerid');
		
		button.on('click', function(){
			initGUI($('#' + containerID), type, journalID);
		})
	}
	
	function initGUI(GUIcontainer, type, journalID){
		$("<div/>").load(jp.baseURL + "html/jp-imprint.html #GUI", function(){
			var imprintGUI = $(this);
			var mainGUI = imprintGUI.find("#imprintGUIMain");
			
			var addButton = imprintGUI.find("#addImprintButton");
			var warmEmptyName = imprintGUI.find("#warnEmptyName");
			var selectBox = mainGUI.find("#imprintSelBox");
			var ckEditor = mainGUI.find("#ckEditorContainer");
			
			GUIcontainer.append(mainGUI);
			
			var masterCtr = new MasterController(journalID, type);
			initCKEditorCtr(ckEditor, masterCtr);
			initSelectBoxCtr(selectBox, masterCtr);
			initAddButtonCtr(addButton, masterCtr);
			initWarnEmptyNameCtr(warmEmptyName, mainGUI, masterCtr);
		})
	}
	
	function initWarnEmptyNameCtr(popup, container, masterCtr){
		var okButton = popup.find('#warnEmptyNameOKButton');
		
		okButton.on('click', function(){
			popup.detach();
		})
		
		this.popupWarnEmptyName = function(callback){
			okButton.on('click', function(){
				callback();
			})
			container.append(popup);
		}
		masterCtr.add(this);
	}
	
	function initAddButtonCtr(button, masterCtr){
		button.on("click", function(){
			console.log("click Add");
			masterCtr.trigger("newItem");
		})
		
		$(document).keypress(function(e){
			// ctrl + '+'
			if(e.ctrlKey && e.which == 43){
				button.click();
			}
		})
	}
	
	function initCKEditorCtr(container, masterCtr){
		container.ckeditor({
    		resize_enabled : false,
    		entities: false,
    		enterMode: CKEDITOR.ENTER_BR,
    		entities_processNumerical: 'force',
    		tabSpaces: 4,
    		fillEmptyBlocks: false,
    		height : '500px',
    		toolbar : [ [ 'Undo', 'Undo', 'Redo', '-', 'Bold', 'Italic', '-', 'NumberedList', 'BulletedList', '-',
    		             'Link', 'Unlink', 'Source', 'Save' ] ]
    	});
    	$("body").css("overflow","hidden");
    	
    	container.loadContent = function(imprintID){
    		if(imprintID != null && imprintID != '') {
    			masterCtr.getBackend().retrieve(imprintID, function(xml) {
    				container.ckeditorGet().setData(xml);
    			}, function(err) {
    				console.log("Error");
    				console.log(err);
    				alert("Error while loading. Please inform the administrator.");
    			});
    		} 
    	}
    	
    	container.newItem = function(){
    		container.ckeditorGet().setData('')
    	}
    	
    	masterCtr.add(container);
	}
	
	function initSelectBoxCtr(select, masterCtr){
		var activeItem = select.find('.active');
		
		masterCtr.getImprintID(function(imprintID){
			masterCtr.getBackend().list(function(idList){
				idList.sort();
				if(idList.length != 0) {
					for(var i = 0; i < idList.length; i++) {
						select.addOption(idList[i], idList[i], imprintID == idList[i]);
					}
				} else {
					select.addOption(null, "Keine Eintrag", true);
				}
				
				masterCtr.add(select);
			})
		})
		
		select.newItem = function(){
			var newItem = $("<a href='#' class='list-group-item'/>");
			var input = $('<input class="form-control input-sm" type="text">').appendTo(newItem);
			var oldActiveItem = activeItem;
			
			select.setActive(newItem);
			select.append(newItem);
			input.focus();
			
			$(document).keypress(function(e){
				if(e.keyCode == 13){
					var inputVal = input.val();
					if(inputVal == null || inputVal == ''){
						console.log('Empty Name');
						newItem.addClass('has-error');
						input.addClass('alert-danger');
						input.blur();
						masterCtr.trigger("popupWarnEmptyName", function(){
							newItem.removeClass('has-error');
							input.removeClass('alert-danger');
							input.focus();
						});
					}
				}else if(e.keyCode == 27){
					newItem.remove();
					select.setActive(oldActiveItem);
				}
			})
		}
		
		select.addOption = function(/* String */ value, /* String */ label, /* boolean */ selected){
			var newOption = $("<a href='#' class='list-group-item'/>");
			
			newOption.text(label);
			newOption.data("value", value);
			select.append(newOption);
			
			if(selected){
				select.setActive(newOption);
			}
		}
		
		select.setActive = function(item){
			activeItem.removeClass('active');
			item.addClass('active');
			activeItem = item;
			masterCtr.trigger("loadContent", item.data("value"));
		}
		
		select.delOption = function(/* string */ imprintID){
			select.find("option[value='" + imprintID + "']").remove();
		}
		
		select.getValue = function(){
			var val = select.find(":selected").val();
			return val != null ? val : null;
		}
		
		select.delegate("a.list-group-item", "click", function(){
			var clickedItem = $(this);
			select.setActive(clickedItem);
			masterCtr.trigger("setContent", clickedItem.data("value"));
		})
		
		return select;
	}
});

var MasterController = function(journalID, type){
	var controllers = [];
	var backend = new FSConnector(type);

	this.trigger = function(event, dataOrFunction){
		for (var i = 0; i < controllers.length; i++){
			if(controllers[i][event]){
				controllers[i][event](dataOrFunction);
			}
		}
	}
	 
	this.add = function(ctr){
		controllers.push(ctr);
	} 
	
	this.getBackend = function(){
		return backend;
	}
	
	this.getImprintID = function(callBack){
		backend.get(journalID, callBack);
	}
	 
	return this;
}

var InfoEditorCtr = function(button){
	var controller = this;
	var masterCtr = null;
	var buttonLabel = button.html();
	var type = button.attr('type');
	var journalID = button.attr('journalid');
	var containerID = button.attr('containerid');
	var backend = new FSConnector(type);
	var imprintID = backend.get(journalID);
	var editBox = new EditBox();
	var editor = new Editor(type, controller);
	var imprintContainer = $("<div class='imprintContainer' />").load(jp.baseURL + "html/jp-imprint.html #imprintEditorGUI");
	
	controller.setMasterCtr = function(ctr){
		masterCtr = ctr;
	}
	
	controller.has = function(id){
		return id == imprintID && editor.selectBox.has(id);
	}
	
	controller.save = function(id, data){
		backend.save(id, data, function() {
			editor.selectBox.addOption(id, id, true);
			editor.close();
		}, function(err) {
			alert('Es ist ein Fehler beim speichern aufgetreten.');
		});
		backend.set(journalID, id);
	}
	
	controller.deactivateButton = function(){
		if(button.hasClass('active')){
			if(editor.hasChanged() && confirm('Änderung speichern?')){
				editor.saveButton.trigger('click');
			}else{
				editor.close();
			}
			
			button.removeClass('active');
			editBox.detach();
		}
	}
	
	function trigger(event){
		if(masterCtr != null){
			masterCtr.trigger(event);
		}
	}
	
	button.on('click', function(){
		
		if(imprintID != null && imprintID != '') {
			backend.retrieve(imprintID, function(xml) {
				editor.setContent(xml);
				editor.setID(imprintID);
			}, function(err) {
				console.log("Error");
				console.log(err);
				alert("Error while loading. Please inform the administrator.");
			});
		} 
		
		editor.start("#" + containerID);
		
		
			backend.list(function(idList){
				editor.setIdList(idList, imprintID)
			})
	})
			    	
	editBox.editButton.on('click', function(){
		var imprintID = editor.selectBox.getValue();
		
		if(imprintID != null && imprintID != '') {
			backend.retrieve(imprintID, function(xml) {
				editor.setContent(xml);
				editor.setID(imprintID);
			}, function(err) {
				console.log("Error");
				console.log(err);
				alert("Error while loading. Please inform the administrator.");
			});
		} 
		
		editor.start("#" + containerID);
	});
	
	controller.selectBoxChanged = function(newID){
		if(newID != null && newID != ""){
			backend.set(journalID, newID);
		}
	}
	
	editBox.removeButton.on('click', function() {
		if(imprintID == null || !confirm("Eintrag wirklich löschen?")) {
			return;
		}
		backend.remove(imprintID, function() {
			editor.close();
			editor.selectBox.delOption(imprintID);
		});
	});
	
	return controller;
}

var Editor = function(type, ctr){
	var title = {
			imprint : 'Impressum',
			partner : 'Partner'
	}
	
	var editorFrame = $("<div/>");
	editorFrame.load(jp.baseURL + "html/jp-imprint.html #imprintEditorGUI", function(){
		var cancelButton = editorFrame.find("#imprintCancelButton");
		var saveButton = editorFrame.find("#imprintSaveButton");
		var idInput = editorFrame.find("#inputName");
		var ckEditor = editorFrame.find("#ckEditorContainer");
		var newImprintButton = editorFrame.find("#newImprintButton");
		var newImprintInput = editorFrame.find("#newImprintInput");
		
		cancelButton.on('click', function(){
			editorFrame.close();
		});
		
		saveButton.on('click', function(){
			var newID = idInput.val();
			
			if(newID == null || newID == "") {
				alert("Bitte geben Sie einen Namen für den Eintrag an.");
				editor.idInput.focus();
				return false;
			}
			
			if(selectBox.has(newID)) {
				var confirmUpdate = confirm("Es existiert bereits ein Eintrag mit dem Namen '" + newID + "'. Wollen Sie den Eintrag überschreiben?");
				
				if(!confirmUpdate){
					return;
				}
			}
			
			var data = editorFrame.getData();
			ctr.save(newID, data)
		});
		
		editorFrame.start = function(appendTo){
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
	    	$("body").css("overflow","hidden");
	    	editorFrame.appendTo(appendTo);
	    }
	    
	    editorFrame.setContent = function(xml){
	    	if(xml != null){
	    		ckEditor.html(xml);
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
	    	var ckEditorInst = CKEDITOR.instances['ckEditorContainer'];
	    	
	    	if(ckEditorInst){
	    		ckEditorInst.destroy();
	    	}
	    	editorFrame.detach();
	    	$("body").css("overflow","");
	    }
	    
	    editorFrame.hasChanged = function(){
	    	var ckEditorInst = CKEDITOR.instances['imprintEditor'];
	    	
	    	if(ckEditorInst){
	    		return ckEditorInst.checkDirty();
	    	}
	    	
	    	return false;
	    }
	    
	    editorFrame.checkAllreadyExist = function(){
	    	return false;
	    }
	    
	    var selectBox = new Select(editorFrame.find("#imprintSelBox"));
	    selectBox.on('change', function(){
			var newID = selectBox.getValue();
			
			controller.selectBoxChanged(newID);
		})
		
	    editorFrame.setIdList = function(idList, imprintID){
	    	idList.sort();
	    	if(idList.length != 0) {
	    		for(var i = 0; i < idList.length; i++) {
	    			selectBox.addOption(idList[i], idList[i], imprintID == idList[i]);
	    		}
	    	} else {
	    		selectBox.addOption(null, "Keine Eintrag", true);
	    	}
	    }
	    
	    newImprintButton.on('click', function(){
	    	console.log('new Imprint');
	    	var newName = newImprintInput.val();
	    	selectBox.addOption(newName, newName, true);
	    })
	    
		editorFrame.getSelBoxVal = function(){
	    	return selectBox.getValue();
	    }
	    
	    editorFrame.selectBoxempty = function(){
	    	selectBox.empty();
	    }
	    
	    editorFrame.addSelOption = function(/* String */ value, /* String */ label, /* boolean */ selected){
	    	selectBox.addOption(value, label, selected);
	    }
		
	});
	
	
	
	var editorOverlay = $("<div id='overlay' class='well'/>")// .appendTo(editorFrame);
	var editorCenter = $("<div/>")// .appendTo(editorFrame);
	var editorNode = $("<div/>").appendTo(editorCenter);
	editorNode.append($("<h3>" + title[type] + " bearbeiten</h3>"));
	var idInput = $("<input type='text' site='40' />");
	editorNode.append("<span style='display: inline-block; padding: 0px 8px 12px 0px;'>Name:</span>").append(idInput);
    var ckEditor = $("<div class='ckeditor' id='imprintEditor'/>").appendTo(editorNode);
    var buttonUl = $("<ul class='ckeditorButtons ckGUI'/>").appendTo(editorNode);
    var cancelLi = $("<li />").appendTo(buttonUl);
    var saveLi = $("<li />").appendTo(buttonUl);
    
    return editorFrame;
}

var EditBox = function(){
	var domNode = $("<div class='editBox'/>");
	var editButton = $("<i class='editButton fa fa-edit pointer' />").appendTo(domNode);
	var removeButton = $("<i class='removeButton fa fa-trash-o pointer'/>");
	
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

var SelectCtr = function(select, masterCtr){
	var activeElem = select.find('.active');
	
	masterCtr.getImprintID(function(imprintID){
		masterCtr.getBackend().list(function(idList){
			idList.sort();
			if(idList.length != 0) {
				for(var i = 0; i < idList.length; i++) {
					select.addOption(idList[i], idList[i], imprintID == idList[i]);
				}
			} else {
				select.addOption(null, "Keine Eintrag", true);
			}
			
			masterCtr.add(select);
		})
	})
	
	select.addOption = function(/* String */ value, /* String */ label, /* boolean */ selected){
//		var newOption = $("<option" + (selected ? " selected='selected'" : "") + "/>");
		var newOption = $("<a href='#' class='list-group-item'/>");
//		newOption.val(value);
		
		if(selected){
			activeElem.removeClass('active');
			newOption.addClass('active');
			activeElem = newOption;
		}
		
		newOption.text(label);
		select.append(newOption);
	}
	
	select.delOption = function(/* string */ imprintID){
		select.find("option[value='" + imprintID + "']").remove();
	}
	
	select.getValue = function(){
		var val = select.find(":selected").val();
		return val != null ? val : null;
	}
	
	return select;
}

var FSConnector = function(/* string */ type){
	var baseURL = jp.baseURL + "rsc/fs/" + type;

	return {
		get: function(/* string */ objectID, /* function */ onSuccess, /* function */ onError) {
			var notFound = false;
			$.ajax({
				url: baseURL + "/get/" + objectID,
				dataType: "text",
				contentType: 'text/plain; charset=UTF-8',
				error: function(err) {
					if(err.status == 404) {
						notFound = true;
					} else if(err.status != 200) {
						alert("Das Impressum wurde aufgrund eines unbekannten Fehlers nicht geladen werden. Bitte informieren Sie den Administrator.");
						console.log(err);
					}
				}
			}).done(onSuccess).error(onError);
		},

		set: function(/* string */ objectID, /* string */ imprintID, /* function */ onSuccess) {
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

		retrieve: function(/* string */ imprintID, /* function */ onSuccess, /* function */ onError) {
			$.ajax({
				url: baseURL + "/retrieve/" + imprintID,
				dataType: "text"
			}).done(onSuccess).error(onError);
		},

		list: function(/* function */ onSuccess, /* function */ onError) {
			$.ajax({
				url: baseURL + "/list",
				dataType: "json",
				contentType: 'application/json; charset=UTF-8'
			}).done(onSuccess).error(onError);
		},
		
		remove: function(/* string */ imprintID, /* function */ onSuccess) {
			$.ajax({
				type: "DELETE",
				url: baseURL + "/delete/" + imprintID
			}).done(onSuccess).error(function(err) {
				alert("Es ist ein Fehler beim löschen des Impressums aufgetreten. Bitte informieren Sie den Administrator.");
				console.log(err);
			});
		},

		save: function(/* string */ imprintID, /* string */ data, /* function */ onSuccess, /* function */ onError) {
			$.ajax({
				type: "POST",
				url: baseURL + "/save",
				data: JSON.stringify({imprintID: imprintID, content: data}),
				contentType: 'application/json; charset=UTF-8'
			}).done(onSuccess).fail(onError);
		}
	}
}