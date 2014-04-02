/**
 * 
 */
$(document).ready(function(){
	$("#filetable").delegate("span.openFolder.glyphicon-folder-open", "click", openFolder);
	$("#filetable").delegate("td.filename", "click", fileNameEdit);
	$("#filetable").delegate("span.deleteFile.glyphicon-trash", "click", deleteFile);
	$("#filetable").delegate("span.setAsMainFile.glyphicon-unchecked", "click", setAsMainFile);
	$("#filetable").delegate("td.goBack", "click", goBack);
//	$("#filebrowser-lightbox-delete-main").delegate("tr.filebrowser-lightbox-delete-main-fileList-row", "click", selectRow);	
	
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
				url: "../rename?newFile=" + "/" + currentPath + "/" + textfieldInput.val() + "&oldFile=" + "/" + currentPath + "/" +  textfieldValue,
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
		if (parent.find(".setAsMainFile").hasClass("glyphicon-check")){
			if (confirm("Derivat ist Start-Datei, wirklich löschen?")){
				doDelete(parent);
			}
		}
		else{
			doDelete(parent);
		}	
	}
	
	function doDelete(parent) {
		console.log("Delete file!");
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
		console.log("Set as main file!");
		$(".setAsMainFile.glyphicon-check").addClass("glyphicon-unchecked");
		$(".setAsMainFile.glyphicon-check").removeClass("glyphicon-check");
		$(this).addClass("glyphicon-check");
		$(this).removeClass("glyphicon-unchecked");
	}
	
	function goBack() {
		window.location.href = window.location.href.substr(0,window.location.href.lastIndexOf('/'));
//		currentPath = currentPath.substr(0,currentPath.lastIndexOf('/'));
//		init(currentPath);
	}
	
//	function selectRow() {
//		$(".filebrowser-lightbox-delete-main-fileList-row").removeClass("fileBrowser-selected");
//		$(this).addClass("fileBrowser-selected");
//	}
	
//	function initLightboxDeleteMain(id) {
//		var fileList = $("#filebrowser-lightbox-delete-main-fileList")
//		fileList.html("");
//		$.get("/rsc/filebrowser/" + id, function(data){
//			for(i = 0; i<data.children.length; i++){
//				var obj = data.children[i];
//				var row = $('<tr class="filebrowser-lightbox-delete-main-fileList-row"/>');
//				if(obj.type == "file"){
//					row.append("<td><span class='glyphicon glyphicon-picture'/></td>");
//				}
//				else{
//					row.append("<td><span class='glyphicon glyphicon-folder-open openFolder'/></td>");
//				}
//				row.append("<td class='filename'>"+ obj.name +"</td>");
//				fileList.append(row);
//			}
//		});
//		if(id.indexOf("/") != -1){
//			var row = $("<tr/>");
//			row.append('<td class="goBack">...</td>');
//			fileList.append(row);
//		}
//	}
	
	function init(id){
		var fileList = $("#fileList")
		fileList.html("");
		$.get("/rsc/filebrowser/" + id, function(data){
			for(i = 0; i<data.children.length; i++){
				var obj = data.children[i];
				var row = $("<tr/>");
				if(obj.type == "file"){
					row.append("<td><span class='glyphicon glyphicon-picture'/></td>");
				}
				else{
					row.append("<td><span class='glyphicon glyphicon-folder-open openFolder'/></td>");
				}
				row.append("<td class='filename'>"+ obj.name +"</td>");
				if (obj.urn != undefined){
					row.append("<td>"+ obj.urn +"</td>");
				}
				else{
					row.append("<td>-</td>");
				}
				row.append("<td><span class='glyphicon glyphicon-unchecked setAsMainFile'/></td>");
				row.append("<td>"+ obj.size +" Bytes</td>");
				if (obj.contentType != undefined){
					row.append("<td>"+ obj.contentType +"</td>");
				}
				else{
					row.append("<td>"+ obj.type +"</td>");
				}
				row.append("<td>"+ obj.lastmodified +"</td>");
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