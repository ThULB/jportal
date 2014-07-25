var Prototype = function(){
	var currentPath = "";
	var derivatID = "";
	var mode = "normal";
	var dragObj = null;
	var dragObjName = null;
	var mouseY = 0;
	var mouseDown = false;
	var dragElm = null;
	
	return {
		init: function() {
//			$("body").on("click", "bla", function() {
//				
//			});
			
			$("body").on("click", ".browser-table-file", function() {
				if (mode == "startfile"){
					var filename = $(this).data("path");
					$(".startfile").removeClass("startfile");
					$(this).addClass("startfile");
					$("#browser-startfile-label").html("Startdatei: " + filename);
				}
//				if (mode == "delete"){
//					if (!$(this).hasClass("delete")){
//						$(this).addClass("delete");
//					}
//					else{
//						$(this).removeClass("delete");
//					}
//				}
//				if (mode == "choose"){
//					if (!$(this).hasClass("choose")){
//						$(this).addClass("choose");
//					}
//					else{
//						$(this).removeClass("choose");
//					}
//				}
			});
			
			$("body").on("mouseup", function(e) {
				mouseDown = false;
				mouseY = 0;
				dragElm = null;
				if (dragObj){
					dragObj.remove();
					dropObj = document.elementFromPoint(e.pageX, e.pageY);
					if (!$(dropObj).hasClass("browser-table-entry")){
						dropObj = $(dropObj).parents(".browser-table-entry");
					}
//					if($(dropObj).hasClass("browser-table-folder")){
//						if($(".glyphicon-check").length > 0){
//							$(".browser-table-entry").filter(function() {
//								return $(this).data("checked") == true;
//							}).each(function(i, node) {
//								$(this).data("parent", $(dropObj).data("filename"));
//								$(this).addClass("hidden");
//								$(this).insertAfter(dropObj);
//							});
//							$(".browser-table-file").each(function(i, node) {
//								$(node).removeClass("checked");
//								$(node).removeData("checked");
//								$(node).find(".btn-check").addClass("glyphicon-unchecked");
//								$(node).find(".btn-check").addClass("invisable");
//								$(node).find(".btn-check").removeClass("glyphicon-check");
//							});
//						}
//						else{
//							var fileToMove = $(this).find(".browser-table-file").filter(function() {
//								return $(this).data("filename") == dragObjName;
//							});
//							$(fileToMove).data("parent", $(dropObj).data("filename"));
//							$(fileToMove).addClass("hidden");
//							$(fileToMove).insertAfter(dropObj);
//						}
//					}
					
					if($(dropObj).hasClass("browser-table-folder")){
						var json = {
									"moveTo": derivatID + ":" + $(dropObj).data("path"),
									"files": [],
								};
						if($(".glyphicon-check").length > 0){
							$(".browser-table-entry").filter(function() {
								return $(this).data("checked") == true;
							}).each(function(i, node) {
								json.files.push(derivatID + ":" + $(this).data("path"));
//								$(this).removeData("parent");
//								$(this).addClass("hidden");
//								$(this).insertAfter(dropObj);
							});
							$(".browser-table-file").each(function(i, node) {
								$(node).removeClass("checked");
								$(node).removeData("checked");
								$(node).find(".btn-check").addClass("glyphicon-unchecked");
								$(node).find(".btn-check").addClass("invisable");
								$(node).find(".btn-check").removeClass("glyphicon-check");
							});
						}
						else{
							var fileToMove = $(this).find(".browser-table-file").filter(function() {
								return $(this).data("filename") == dragObjName;
							});
							json.files.push(derivatID + ":" + $(fileToMove).data("path"));
//							$(fileToMove).removeData();
//							$(fileToMove).addClass("hidden");
//							$(fileToMove).insertAfter(dropObj);
						}
						moveFiles(json);
					}				
					
					$("body").removeAttr('unselectable')
				     .css({'-moz-user-select':'text',
				           '-moz-user-select':'text',
				           '-o-user-select':'text',
				           '-khtml-user-select':'text', /* you could also put this in a class */
				           '-webkit-user-select':'text',/* and add the CSS class here instead */
				           '-ms-user-select':'text',
				           'user-select':'text'
				     }).bind('selectstart', function(){ return true; });
					dragObj = null;
				}
			});
			
			$("body").on("mousedown", ".browser-table-entry", function(event) {
				mouseDown = true;
				mouseY = event.pageY;
				dragElm = $(this);
			});
			
		    $("body").on("mousemove", function(e) {
		    	if (mouseDown){
//		    		console.log(mouseY - e.pageY);
		    		
			        if ($(dragObj)){
			        	$(dragObj).offset({
			                top: e.pageY,
			                left: 5
			            });
			        }
			        if ((dragObj == null) && (Math.abs(mouseY - e.pageY) > 20)){
						var copy = dragElm.clone();
						dragObjName = dragElm.data("filename"); 
						$(copy).css("position", "absolute");
						$(copy).removeClass("checked");
						if($(".glyphicon-check").length > 0){
							$(copy).find(".browser-table-file-name").html("multiple Files");
							$(copy).find(".browser-table-file-urn").html("");
						}
						$("body").append(copy);
						dragObj = copy;
						window.getSelection().removeAllRanges();
//						console.log(copy);
						$("body").attr('unselectable','on')
					     .css({'-moz-user-select':'-moz-none',
					           '-moz-user-select':'none',
					           '-o-user-select':'none',
					           '-khtml-user-select':'none', /* you could also put this in a class */
					           '-webkit-user-select':'none',/* and add the CSS class here instead */
					           '-ms-user-select':'none',
					           'user-select':'none'
					     }).bind('selectstart', function(){ return false; });
			        }
			        
		    	}
		    });
			
			$("body").on("mouseenter", ".browser-table-entry", function() {
				if (dragObj == null){
					$(this).find("span.btn").removeClass("invisable");
				}
			});
			
			$("body").on("mouseleave", ".browser-table-entry", function() {
				var file = $(this);
				$(this).find("span.btn").addClass("invisable");
				$(this).find("span.btn-startfile").filter(function() {
					return file.data("startfile") == true;
				}).removeClass("invisable");
				$(this).find("span.btn-check").filter(function() {
					return file.data("checked") == true;
				}).removeClass("invisable");
			});
			
			$("body").on("click", ".btn-folder", function() {
				window.location.href = window.location.href.substr(0,window.location.href.lastIndexOf('gui2/') + 5) + derivatID + $(this).parents("tr").data("path");
//				var parent = $(this).parents(".browser-table-entry");
//				$(this).removeClass("btn-folder");
//				$(this).addClass("btn-folder-open");
//				if (mode != "edit"){
//					var name = $(parent).data("filename");
//					$("#browser-table-files > tr").addClass("hidden");
//					$(parent).removeClass("hidden");
//					$(parent).addClass("browser-table-folder-open");
//					$(parent).removeClass("browser-table-folder");
//					$(parent).find("span.glyphicon-folder-close").addClass("glyphicon-folder-open");
//					$(parent).find("span.glyphicon-folder-close").removeClass("glyphicon-folder-close");
//					$(parent).find("td.browser-table-file-name").html("...");
//					$("tr.browser-table-file").filter(function() {
//						return $(this).data("parent") == name;
//					}).removeClass("hidden");
//				}
			});
			
			$("body").on("click", "#browser-delete-button", function() {
				if (mode == "normal"){
					$(this).html("Löschen bestätigen");
					$(this).addClass("btn-primary");
					$(this).removeClass("btn-default");
					$("#browser-delete-button-cancel").removeClass("hidden");
					mode = "delete";
				}
				else{
					if (mode == "delete"){
						$(this).html("Mehrere löschen");
						$(this).addClass("btn-default");
						$(this).removeClass("btn-primary");
						$("#browser-delete-button-cancel").addClass("hidden");
						mode = "normal";
						$(".delete").remove();
					}
				}
			});
			
			$("body").on("click", "#browser-delete-button-cancel", function() {
				$("#browser-delete-button").html("Mehrere löschen");
				$("#browser-delete-button").addClass("btn-default");
				$("#browser-delete-button").removeClass("btn-primary");
				$("#browser-delete-button-cancel").addClass("hidden");
				$(".delete").removeClass("delete");
				mode = "normal";
			});
			
			$("body").on("click", ".btn-folder-open", function() {
				var parent = $(this).parents(".browser-table-entry");
				$(this).removeClass("btn-folder-open");
				$(this).addClass("btn-folder");
				var name = $(parent).data("filename");
				$("#browser-table-files > tr").addClass("hidden");
				$(parent).removeClass("browser-table-folder-open");
				$(parent).addClass("browser-table-folder");
				$(parent).find("span.glyphicon-folder-open").addClass("glyphicon-folder-close");
				$(parent).find("span.glyphicon-folder-open").removeClass("glyphicon-folder-open");
				$(parent).find("td.browser-table-file-name").html(name);
				$("tr.browser-table-entry").filter(function() {
					return $(this).data("parent") == undefined;
				}).removeClass("hidden");
			});
			
			$("body").on("click", "#browser-edit-button", function() {
				if (mode == "normal"){
					$(this).html("Ausawahl bestätigen");
					$(this).addClass("btn-primary");
					$(this).removeClass("btn-default");
					$("#browser-edit-button-cancel").removeClass("hidden");
					mode = "choose";
				}
				else{
					if (mode == "choose"){
						$("tr.browser-table-entry.choose").each(function(i, file) {
							var name = $(this).find("td.browser-table-file-name");
							name.data("oldName", name.html());
							name.html("<input class='reg' type='text' value='" + name.data("oldName") + "'></input><input class='normal' type='text' value='" + name.data("oldName") + "'></input>");
							mode = "edit";
						});
						$(this).html("Bearbeiten bestätigen");
						$(this).addClass("btn-primary");
						$(this).removeClass("btn-default");
						$("#browser-edit-button-cancel").removeClass("hidden");
					}
					else{
						if (mode == "edit"){
							$("tr.browser-table-entry").each(function(i, file) {
								var name = $(this).find("td.browser-table-file-name");
								name.removeData("oldName");
								name.html(name.find("input.normal").val());
								mode = "normal";
							});
							$(this).html("Mehrere bearbeiten");
							$(this).addClass("btn-default");
							$(this).removeClass("btn-primary");
							$("#browser-edit-button-cancel").addClass("hidden");
							$("tr.browser-table-entry.choose").removeClass("choose");
						}
					}
				}

			});
			
			$("body").on("click", "#browser-edit-button-cancel", function() {
				$("#browser-edit-button").html("Alles bearbeiten");
				$("#browser-edit-button").addClass("btn-default");
				$("#browser-edit-button").removeClass("btn-primary");
				$("#browser-edit-button-cancel").addClass("hidden");
				$("tr.browser-table-entry").each(function(i, file) {
					var name = $(this).find("td.browser-table-file-name");
					name.html(name.data("oldName"));
					name.removeData("oldName");
				});
				$("tr.browser-table-entry.choose").removeClass("choose");
				mode = "normal";
			});
			
			$("body").on("click", "#browser-startfile-button", function() {
				if (mode == "normal"){
					$(this).html("Änderung bestätigen");
					$(this).addClass("btn-primary");
					$(this).removeClass("btn-default");
					$("#browser-startfile-button-cancel").removeClass("hidden");
					var startfile  = $("#browser-startfile-label").data("filename");
					var file = $("tr").filter(function() {
						return $(this).data("startfile") == true;
					});
					console.log(file);
					$(file).addClass("startfile");
					mode = "startfile";
				}
				else{
					if (mode == "startfile"){
						changeStartFile($(".startfile").data("path"));
//						$("#browser-startfile-label").data("filename", $(".startfile").data("path"));
						$(".startfile").removeClass("startfile");
						$(this).addClass("btn-default");
						$(this).removeClass("btn-primary");
						$(this).html("Startdatei ändern");
						$("#browser-startfile-button-cancel").addClass("hidden");						
						mode = "normal";
					}
				}			
			});
			
			$("body").on("click", "#browser-startfile-button-cancel", function() {
				$("#browser-startfile-label").html("Startdatei: " + $("#browser-startfile-label").data("filename"));
				$(".startfile").removeClass("startfile");
				$("#browser-startfile-button").addClass("btn-default");
				$("#browser-startfile-button").removeClass("btn-primary");
				$("#browser-startfile-button").html("Startdatei ändern");
				$("#browser-startfile-button-cancel").addClass("hidden");						
				mode = "normal";
			});
			
			$("body").on("keydown", "input.reg", function(event) {
				if ( event.which == 13 ) {
					var val = $(this).val();
					if (val.indexOf("{i}") != -1){
						$("input.reg").not(this).each(function(i, inp) {
							console.log("test");
							$(inp).val(val.replace("{i}", i+1));
						});
						val = val.replace("{i}", 0);
						$(this).val(val);

					}
					else{
						$("input.reg").not(this).val($(this).val());
					}
					$("input.normal").each(function(i, sib) {
						$(this).val($(this).siblings("input.reg").val());
					});
				}
			});	
			
			$("body").on("click", ".btn-startfile", function(){
				var oldStart = $(".browser-table-entry").filter(function() {
									return $(this).data("startfile") == true;
								});
				oldStart.removeData("startfile");
				oldStart.find("span.btn-startfile").addClass("invisable");
				$(this).parents(".browser-table-entry").data("startfile",true);
				$(this).find("span.btn-startfile").removeClass("invisable");
			});
			
			$("body").on("click", ".btn-delete", function(){
				var entry = $(this).parents(".browser-table-entry");
				if ($(entry).data("startfile") != true){
					if(confirm("Datei " + $(entry).data("path") + "wirklich löschen?")){
						deleteFile(entry);
					}
				}
				else{
					alert("You can not delete the Startfile.")
				}
			});
			
			$("body").on("click", ".btn-edit", function(){
				if (!$(this).data("edit")){
					$(this).data("edit", true);
					var name = $(this).parents(".browser-table-entry").find(".browser-table-file-name");
					name.data("oldName", name.html());
					name.html("<input type='text' value='" + name.data("oldName") + "'></input>");
				}
				else{
					$(this).removeData("edit");
					var name = $(this).parents(".browser-table-entry").find(".browser-table-file-name");
					name.data("oldName", name.html());
					name.html($(name).find("input").val());
				}
			});
			
			$("body").on("keydown", "td.browser-table-file-name > input", function(event){
				if(!$(this).hasClass("input-new")){
					if ( event.which == 13 ) {
//						$(this).parent().html($(this).val());
						renameFile($(this).parents(".browser-table-entry").data("path"), $(this).val(), $(this).parents(".browser-table-entry"));
					}
					if ( event.which == 27 ) {
						$(this).parent().html($(this).parent().data("oldName"));
					}
				}
			});
			
			$("body").on("click", ".btn-check", function(){
				var parent = $(this).parents(".browser-table-entry");
				if ($(parent).data("checked") != true){
					$(parent).addClass("checked");
					$(parent).data("checked", true);
					$(this).removeClass("glyphicon-unchecked");
					$(this).removeClass("invisable");
					$(this).addClass("glyphicon-check");
				}
				else{
					$(parent).removeClass("checked");
					$(parent).removeData("checked");
					$(this).addClass("glyphicon-unchecked");
					$(this).removeClass("glyphicon-check");
				}

			});
			$("body").on("click", ".btn-check-all", function(){
				if ($(this).data("checked") != true){
					$(this).data("checked", true);
					$(".browser-table-file:visible").each(function(i, node) {
						$(node).addClass("checked");
						$(node).data("checked", true);
						$(node).find(".btn-check").removeClass("glyphicon-unchecked");
						$(node).find(".btn-check").removeClass("invisable");
						$(node).find(".btn-check").addClass("glyphicon-check");
					});
				}
				else{
					$(this).removeData("checked");
					$(".browser-table-file.checked").each(function(i, node) {
						$(node).removeClass("checked");
						$(node).removeData("checked");
						$(node).find(".btn-check").addClass("glyphicon-unchecked");
						$(node).find(".btn-check").addClass("invisable");
						$(node).find(".btn-check").removeClass("glyphicon-check");
					});
				}
			});
			
			$("body").on("click", ".btn-delete-all", function(){
				$(".browser-table-entry").filter(function() {
					return $(this).data("checked") == true;
				}).remove();
			});
			
			$("body").on("click", ".btn-edit-all", function(){
				if ($(this).data("edit") != true){
					$(this).data("edit", true);
					$(".browser-table-entry").filter(function() {
						return $(this).data("checked") == true;
					}).find(".browser-table-file-name").each(function(i, node) {
						$(node).data("oldName", $(node).html());
						$(node).html("<input type='text' value='" + $(node).data("oldName") + "'></input>");
					});
				}
				else{
					$(this).removeData("edit");
					$(".browser-table-entry").filter(function() {
						return $(this).data("checked") == true;
					}).find(".browser-table-file-name").each(function(i, node) {
						$(node).html($(node).find("input").val());
						$(node).removeData("oldName");
						
					});
				}
			});
			
			$("body").on("click", ".btn-add", function(){
				var tr = $("<tr class='browser-table-folder browser-table-entry'></tr>");
				var td = $("<td><span class='glyphicon glyphicon-folder-close btn-folder'></span></td>");
				tr.append(td);
				tr.append("<td><div class='btns'><span class='glyphicon glyphicon-unchecked btn-check btn invisable'></span><div class='no-button'> </div></span><span class='glyphicon glyphicon-edit btn-edit btn invisable'></span><span class='glyphicon glyphicon-trash btn-delete btn invisable'></span></div></td>");
				tr.append("<td class='browser-table-file-name'><input class='input-new' type='text' value=''></input></td>");
				tr.append("<td class='browser-table-file-urn'>-</td>");
				$("#browser-table-files").append(tr);
			});
			
			$("body").on("keydown", ".input-new", function(event){
				if ( event.which == 13 ) {
					createFolder($(this));
				}
				if ( event.which == 27 ) {
					$(this).parents(".browser-table-entry").remove();
				}
			});
			
			$("body").on("click", ".goBack", function(event){
				window.location.href = window.location.href.substr(0,window.location.href.lastIndexOf('/'));
			});
			
			//functionen die zuerst geladen werden

			currentPath = window.location.href.substr(window.location.href.lastIndexOf('gui2/') + 5);
			if (currentPath.indexOf("/") != -1){
				derivatID = currentPath.substring(0, currentPath.indexOf("/"));
			}
			else{
				derivatID = currentPath;
			}
			console.log(currentPath);
			console.log(derivatID);
			getDerivate();
		}
	}
	function getDerivate(){
		$.ajax({
			url: "/rsc/filebrowser/" + currentPath,
			type: "GET",
			dataType: "json",
			success: function(data) {
						console.log(data);
						buildTable(data.children);
						setStartFile(data.maindocName);
					},
			error: function(error) {
						alert(error);
					}
		});
	}
	
	function changeStartFile(path){
		$.ajax({
			url: "/rsc/filebrowser/" + derivatID + path + "/main",
			type: "PUT",
			dataType: "json",
			statusCode: {
				200: function() {
					setStartFile(path);
				},
				500: function(error) {
					setStartFile($("#browser-startfile-label").data("filename"));
					alert(error);
				}
			}
		});
	}
//	derivateid:/
	function deleteFile(entry){
		$.ajax({
			url: "/rsc/filebrowser/" + derivatID  + entry.data("path"),
			type: "DELETE",
			dataType: "json",
			statusCode: {
				200: function() {
					entry.remove();
				},
				500: function(error) {
					alert(error);
				}
			}
		});
	}
	
	function renameFile(oldName, newName, entry){		
		$.ajax({
			url: "/rsc/filebrowser/rename?file=" + derivatID + ":" + oldName + "&name=" +  newName,
			type: "POST",
			dataType: "json",
			statusCode: {
				200: function() {
					$(entry).data("path", oldName.substring(0, oldName.lastIndexOf("/") + 1) + newName);
					$(entry).find(".browser-table-file-name").html(newName);
					$(entry).find(".browser-table-file-name").removeData("oldName");
				},
				500: function(error) {
					alert(error);
				}
			}
		});
	}
	
	function createFolder(input) {
//		console.log("/rsc/filebrowser/" + derivatID + ":" + currentPath.replace(derivatID, "") + "/" + name);
		$.ajax({
			url: "/rsc/filebrowser/" + currentPath + "/" + $(input).val(),
			type: "POST",
			dataType: "json",
			statusCode: {
				200: function() {
					$(input).parents(".browser-table-folder").data("filename",$(input).val());
					$(input).parent().html($(input).val());
				},
				500: function(error) {
					alert(error);
				}
			}
		});
	}
	
	function moveFiles(json) {
		$.ajax({
			url: "/rsc/filebrowser/move",
			type: "POST",
			contentType: 'application/json',
			dataType: "json",
			data: JSON.stringify(json),
			statusCode: {
				200: function() {
				},
				500: function(error) {
					alert(error);
				}
			}
		});
	}
	
	
	function buildTable(files) {
		console.log(files);
		$.each(files, function(i, file) {
			var btns = $("<td><div class='btns'><span class='glyphicon glyphicon-unchecked btn-check btn invisable'></span></span><span class='glyphicon glyphicon-edit btn-edit btn invisable'></span><span class='glyphicon glyphicon-trash btn-delete btn invisable'></span></div></td>")
			if (file.contentType == "jpeg"){
				var tr = $("<tr class='browser-table-file browser-table-entry'></tr>");
				var td = $("<td><span class='glyphicon glyphicon-picture' rel='popover' data-toggle='popover' data-placement='right' data-title='" + file.name + "' data-trigger='hover'></span></td>");
				td.find("span").popover({content: "<img class='popover-img' src='/servlets/MCRFileNodeServlet/jportal_derivate_00000001"+ file.absPath + "'></img>", html: true});
				tr.append(td);
				tr.append(btns.clone());
				tr.append("<td class='browser-table-file-name'>" + file.name + "</td>");
//				tr.append("<td class='browser-table-file-urn'>" + file.urn + "</td>");
				tr.append("<td class='browser-table-file-urn'>" + "-" + "</td>");
				tr.data("path", file.absPath);
//				if (file.startFile == true){
//					$("#browser-startfile-label").html("Startdatei: " + file.filename);
//					$("#browser-startfile-label").data("filename", file.filename);
//					$(tr).find("span.btn-startfile").removeClass("invisable");
//					$(tr).data("startfile", true);
//				}
				$("#browser-table-files").append(tr);
			}
			else{
				var tr = $("<tr class='browser-table-folder browser-table-entry'></tr>");
				var td = $("<td><span class='glyphicon glyphicon-folder-close btn-folder'></span></td>");
				tr.append(td);
				tr.append(btns.clone());
				tr.append("<td class='browser-table-file-name'>" + file.name + "</td>");
				tr.append("<td class='browser-table-file-urn'>-</td>");
				tr.data("path", file.absPath);
				$("#browser-table-files").append(tr);
//				$.each(file.childs, function(i, child) {
//					var tr = $("<tr class='browser-table-file browser-table-entry hidden'></tr>");
//					var td = $("<td><span class='glyphicon glyphicon-picture' rel='popover' data-toggle='popover' data-placement='right' data-title='" + child.filename + "' data-trigger='hover'></span></td>");
//					td.find("span").popover({content: "<img class='popover-img' src='img/"+ child.img + "'></img>", html: true});
//					tr.append(td);
//					tr.append(btns.clone());
//					tr.append("<td class='browser-table-file-name'>" + child.filename + "</td>");
//					tr.append("<td class='browser-table-file-urn'>" + child.urn + "</td>");
//					tr.data("filename", child.filename);
//					tr.data("parent", file.filename);
//					$("#browser-table-files").append(tr);
//				});
			}

		});
//		$("#brwser-table-files").append("<tr><td><span class='glyphicon glyphicon-plus'></span></td><td><span class='glyphicon glyphicon-unchecked btn-check-all btn'></span><div class='no-button'> </div></span><span class='glyphicon glyphicon-edit btn-edit-all btn'></span><span class='glyphicon glyphicon-trash btn-delete-all btn'></span></td><td></td><td></td></tr>");
		
//		if(derivatID.indexOf("/") != -1){
		if(currentPath != derivatID){
			var row = $("<tr/>");
			row.append('<td class="goBack">...</td>');
			$("#browser-table-files").prepend(row);
		}
		
	}
	
	function setStartFile(name) {
		$("#browser-startfile-label").html("Startdatei: " + name);
		$("#browser-startfile-label").data("filename", name);
		$(".browser-table-entry").filter(function() {
			return $(this).data("startfile") == true;
		}).removeData("startfile");
		$(".browser-table-entry").filter(function() {
			return $(this).data("path") == name;
		}).data("startfile", true);
	}
	
//	function bla() {
//		
//	}
}

$(document).ready(function() {
	var prototypeInstance = new Prototype();
	prototypeInstance.init();
});