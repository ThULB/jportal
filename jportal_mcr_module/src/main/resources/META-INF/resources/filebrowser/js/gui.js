/**
 * 
 */
$(document).ready(function(){
	$("#filetable").delegate("td.filename", "click", fileNameEdit);
	$("#filetable").delegate("span.deleteFile.glyphicon-trash", "click", deleteFile);
	$("#filetable").delegate("span.setAsMainFile.glyphicon-unchecked", "click", setAsMainFile);
	
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
			setInputValue(textfieldInput.val());
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
		console.log("Delete file!");
	}
	
	function setAsMainFile(){
		console.log("Set as main file!");
	}
	
	function init(id){
		var fileList = $("#fileList")
		$.get("/rsc/filebrowser/" + id, function(data){
			for(i = 0; i<data.children.length; i++){
				var obj = data.children[i];
				var row = $("<tr/>");
				row.append("<td class='filename'>"+ obj.name +"</td>");
				row.append("<td>"+ obj.urn +"</td>");
				row.append("<td><span class='glyphicon glyphicon-unchecked setAsMainFile'/></td>");
				row.append("<td>"+ obj.size +" Bytes</td>");
				row.append("<td>"+ obj.contentType +"</td>");
				row.append("<td>"+ obj.latmodified +"</td>");
				row.append("<td><span class='glyphicon glyphicon-trash deleteFile'/></td>");
				fileList.append(row);
			}
		});
	}
	
	init("jportal_derivate_00000023");
});