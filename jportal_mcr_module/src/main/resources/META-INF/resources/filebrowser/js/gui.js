/**
 * 
 */
$(document).ready(function(){
	$("#filetable").delegate("span.openFolder.glyphicon-folder-open", "click", openFolder);
	$("#filetable").delegate("td.filename", "click", fileNameEdit);
	$("#filetable").delegate("span.deleteFile.glyphicon-trash", "click", deleteFile);
	$("#filetable").delegate("span.glyphicon-unchecked", "click", setAsMainFile);
	$("#filetable").delegate("td.goBack", "click", goBack);
	
	var derivatID = window.location.href.substr(window.location.href.lastIndexOf('gui/') + 4);
	var currentPath = derivatID;
	
	function openFolder() {
		window.location.href = window.location.href + "/" + $(this).parents("tr").find(".filename").html();
//		currentPath = currentPath + "/" + $(this).parents("tr").find(".filename").html();
//		init(currentPath);
	}
	
	function fileNameEdit(){
		var textfield = $(this);
		var textfieldValue = textfield.text();
		var textfieldInput = $("<input type='text' class='form-control'/>").val(textfieldValue);
		
		function swapClass(){
			textfield.toggleClass("filename filenameEdit");
		}
		
		function setInputValue(val){
			textfield.html(val);
			textfield.toggleClass("textfield");
			swapClass();
		}

		function commitChanges(){
			$.ajax({
				url: "/rsc/filebrowser/rename?file=" + currentPath + ":/" + textfieldValue + "&name=" +  textfieldInput.val(),
				type: "POST",
				statusCode: {
					200: function() {
						setInputValue(textfieldInput.val());
					}
				}
			});
		}

		function cancelChanges(){
			setInputValue(textfieldValue);
		}
		
		textfieldInput.blur(commitChanges);
		
		textfieldInput.keydown(function(e) {
			// check if Enter key is pressed
			if(e.which == 13) {
				commitChanges();
			// check if ESC key is pressed
			}else if(e.which == 27){
				cancelChanges();
			}
		});
		
		textfieldInput.focus(function(){
			$(this).select();
		});
		
		swapClass();
		textfield.empty().append(textfieldInput);
		textfieldInput.trigger("focus");
	}
	
	function deleteFile(){
		var parent = $(this).parents("tr");
		if (parent.find(".setAsMainFile").hasClass("setAsMainFile")){
			if (confirm("Derivat ist Start-Datei, wirklich löschen?")){
				doDelete(parent);
			}
		}
		else{
			doDelete(parent);
		}	
	}
	
	function doDelete(parent) {
//		console.log("Delete file!");
		var file = parent.find(".filename").html();		
		$.ajax({
			url: "/rsc/filebrowser/" + currentPath + "/" + file,
			type: "DELETE",
			statusCode: {
				200: function() {
					parent.remove();
				}			
			}
		});
	}
	
	function setAsMainFile(){
//		console.log("Set as main file!");
		var newMainFile = $(this);
		var filename = $(this).parents("tr").find(".filename").html();
		$.ajax({
			url: "/rsc/filebrowser/" + currentPath + "/" + filename + "/main",
			type: "PUT",
			statusCode: {
				200: function() {
					$(".glyphicon-check").addClass("glyphicon-unchecked");
					$(".glyphicon-check").removeClass("glyphicon-check setAsMainFile");
					newMainFile.addClass("glyphicon-check setAsMainFile");
					newMainFile.removeClass("glyphicon-unchecked");
				}			
			}
		});
	}
	
	function goBack() {
		window.location.href = window.location.href.substr(0,window.location.href.lastIndexOf('/'));
//		currentPath = currentPath.substr(0,currentPath.lastIndexOf('/'));
//		init(currentPath);
	}
		
	function init(id){
		var fileList = $("#fileList")
		fileList.html("");
		$.get("/rsc/filebrowser/" + id, function(data){
			if(data.maindocName){
				$("#maindocName").html(data.maindocName);
			}
			
			for(i = 0; i<data.children.length; i++){
				var obj = data.children[i];
				var row = $("<tr/>");
				//picture
				if(obj.type == "file"){
					row.append("<td><span class='glyphicon glyphicon-picture'/></td>");
				}
				else{
					row.append("<td><span class='glyphicon glyphicon-folder-open openFolder'/></td>");
				}
				//filename
				row.append("<td class='filename'>"+ obj.name +"</td>");
				//urn
				if (obj.urn != undefined){
					row.append("<td>"+ obj.urn +"</td>");
				}
				else{
					row.append("<td>-</td>");
				}
				//maindoc
				if (obj.type == "file"){
					if (obj.maindoc != true){
						row.append("<td><span class='glyphicon glyphicon-unchecked'/></td>");
					}
					else{
						row.append("<td><span class='glyphicon glyphicon-check setAsMainFile'/></td>");
					}
				}
				else{
					row.append("<td>-</td>");
				}

				//size
				row.append("<td>"+ obj.size +" Bytes</td>");
				if (obj.contentType != undefined){
					row.append("<td>"+ obj.contentType +"</td>");
				}
				else{
					row.append("<td>"+ obj.type +"</td>");
				}
				//lastmodified
				row.append("<td>"+ obj.lastmodified +"</td>");
				//delete
				row.append("<td><span class='glyphicon glyphicon-trash deleteFile'/></td>");
				fileList.append(row);
			}
		});
		if(id.indexOf("/") != -1){
			var row = $("<tr/>");
			row.append('<td class="goBack">...</td>');
			fileList.append(row);
		}
	}
	
	init(derivatID);
});