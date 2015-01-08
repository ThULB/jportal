var DerivateBrowser = function(){
	var i18nKeys =[];
	var qpara = [], hash;
	var uploadList = {};
	var currentDocID = "";
	var currentDeriID = "";
	var currentPath = "";
	var currentUploadCheck = {};
	var dragcounter = 0;
	var mouseDown = false;
	var dragElm = null;
	var dragObj = null;
	var timeOutID = null;

	return {
		init: function() {
			
			$.address.internalChange(function() {
//				console.log("internal");
			});
			
			$.address.externalChange(function() {
//				console.log("external");
				var paths = $.address.pathNames();
				var path = "";
				if (paths.length > 1){
					path = "/" + paths.slice(1).join("/");
				}
				path = path.replace("%20", " ");
				if (paths[0] != undefined){
					currentDocID = paths[0];
					if (paths[0].search("derivate") != -1){
						currentDeriID = paths[0];
					}
					else{
						currentDeriID = "";
					}
					currentPath = path;
					$("#derivat-panel").addClass("hidden");
					$("#derivate-browser").addClass("hidden");
					$("#journal-info").addClass("hidden");
					derivateBrowserNavigation.goToDocument(paths[0], path);
					derivateBrowserFileView.showDerivateOrDoc(paths[0], path);
				}
			});
			
			$("body").on("click", "#btn-upload-cancel", function() {
				$('#lightbox-upload-overwrite').modal('hide');
				uploadList = {};
			});
			
			$("body").on("click", "#btn-upload-skip", function() {
				$('#lightbox-upload-overwrite').modal('hide');
				currentUploadCheck.currentPos = currentUploadCheck.currentPos + 1;
				if ($(".btn-upload-all").data("check") == true){
					currentUploadCheck.skipAll = true;
				}
				uploadFilesAndAsk();
			});
			
			$("body").on("click", ".btn-upload-all", function() {
				if($(this).data("check") == true){
					$(this).removeData("check");
					$(this).addClass("glyphicon-unchecked");
					$(this).removeClass("glyphicon-check");
				}
				else{
					$(this).data("check", true);
					$(this).removeClass("glyphicon-unchecked");
					$(this).addClass("glyphicon-check");
				}
			});
						
			$("body").on("click", "#btn-upload-overwrite", function() {
				var upload = uploadList[currentUploadCheck.data.files[currentUploadCheck.currentPos].id];
				upload.statusbar = $("#upload-status-bar-table").append(upload.getStatus());
				showUploadBar();
				$('#lightbox-upload-overwrite').modal('hide');
				currentUploadCheck.currentPos = currentUploadCheck.currentPos + 1;
				if ($(".btn-upload-all").data("check") == true){
					currentUploadCheck.overwriteAll = true;
				}
				uploadFile(upload);
			});
			
			$('#lightbox-upload-overwrite').on('hidden.bs.modal', function (e) {
				$(this).data("open", false);
				if ($(this).data("openagain") != undefined){
					$(this).removeData("openagain");
					$('#lightbox-upload-overwrite').modal("show");
				}			
			});
			
			$('#lightbox-upload-overwrite').on('shown.bs.modal', function (e) {
				$(this).data("open", true);		
			});
			
			
			$("body").on("mousedown", ".popover-file", function(event) {
				mouseDown = true;
				//mouseY = event.pageY;
				dragElm = $(this).closest(".browser-table-entry");
			});
			
			$("body").on("mousedown", ".folder:not(.journal) > .folder-name, .folder:not(.journal) > span.icon", function(event) {
				mouseDown = true;
				dragElm = $(this).closest(".folder");
			});
			
		    $("body").on("mousemove", function(e) {
		    	if (mouseDown){		    		
			        if ($(dragObj)){
			        	$(dragObj).offset({
			                top: e.pageY - 1 /*- ($(dragObj).height() / 2)*/,
			                left: e.pageX + 1 //- ($(dragObj).width() / 2)
			            }, 1);
//			        	$(dragObj).addClass("hidden");
			        	if ($(dragElm).hasClass("folder")){
			        		$(".drag-elm-hover").removeClass("drag-elm-hover");
			        		var hoverObj = document.elementFromPoint(e.pageX, e.pageY).closest(".folder");
			        		$(hoverObj).addClass("drag-elm-hover");
			        	}
//		        		$(dragObj).removeClass("hidden");
			        }
			        if ((dragObj == null) /*&& (Math.abs(mouseY - e.pageY) > 20)*/){
			        	if ($(dragElm).hasClass("folder")){
			        		var elm = $("<div id='drag-doc'></div>");
			        		$(elm).append($(dragElm).children("span.icon").clone());
			        		$(elm).append($(dragElm).children("div.folder-name").clone());
			        		$(elm).width($(dragElm).width());
//			        		var elm = dragElm.find("span.icon").clone();
			        		$("body").append(elm);
			        		$(dragElm).addClass("faded");
			        		$(elm).offset($(dragElm).offset());
			        		$(elm).animate({
								"width": "100px",
							});
			        		dragObj = elm;
			        	}
			        	if ($(dragElm).hasClass("browser-table-file")){
				        	var img = $('<img id="drag-img" src="/servlets/MCRFileNodeServlet/' + dragElm.data("deriID") + dragElm.data("path") + '">');
							$("body").append(img);
							dragObj = img;
			        	}
						window.getSelection().removeAllRanges();
		        		$("body").attr('unselectable','on')
					     .addClass("noSelect").bind('selectstart', function(){ return false; });
			        }
			        
		    	}
		    });
		    
			$("body").on("mouseup", function(e) {
				mouseDown = false;
				//mouseY = 0;
				if (dragObj){
//					dragObj.remove();
					dropObj = document.elementFromPoint(e.pageX, e.pageY);
					if (($(dropObj).closest("#derivat-panel-startfile").length > 0) && $(dragElm).hasClass("browser-table-file")){
//						$(dragObj).animate({
//							top: $("#panel-img").offset().top,
//							left: $("#panel-img").offset().left,
//							height: $("#panel-img").height(),								
//							"max-width": $("#panel-img").width()
//						},400, function() {
//							dragObj.remove();
//						});
						dragObj.remove();
						changeStartFile(dragElm);
					}
					if (($(dropObj).closest(".folder").length > 0) && $(dragElm).hasClass("folder")){
						var moveTo = $(dropObj).closest(".folder");
						if (!$(moveTo).hasClass("faded") && !($(dragElm).parent().closest(".folder")[0] == $(moveTo)[0])){
							if ((!$(moveTo).hasClass("derivat") && !$(moveTo).hasClass("article")) || ($(moveTo).hasClass("article") && $(dragElm).hasClass("derivat"))){
								if (!($(moveTo).hasClass("journal") && $(dragElm).hasClass("article"))){
									dragObj.remove();
									var json = [];
									json.push({"objId": $(dragElm).data("docID"), "newParentId": $(moveTo).data("docID")});
//									console.log("move");
									moveDocTo(json);
								}
							}							
						}
					}
					$(".drag-elm-hover").removeClass("drag-elm-hover");
	        		$(dragObj).animate({
						top: $(dragElm).offset().top,
						left: $(dragElm).offset().left + 20
					},400, function() {
						$(dragElm).removeClass("faded");
						dragObj.remove();
						dragElm = null;
						dragObj = null;
					});
					$("body").removeAttr('unselectable')
				     .removeClass("noSelect").bind('selectstart', function(){ return true; });
					
				}
			});
			
			$("body").on("click", "span.btn-new-urn", function(event) {
				var entry = $(this).parents(".browser-table-entry");
				var json = {
						"deriID": entry.data("deriID"),
						"completeDeri": false,
						"files": []
					};
				var json2 = {
						"path": entry.data("path")
				}
				json.files.push(json2);
				addURN(json);
			});
			
			$("body").on("click", "#btn-urnAll", function(event) {
				var json = {
						"deriID": currentDeriID,
						"completeDeri": true,
						"files": []
					};
				addURN(json);
			});
			
			
			$("body").on("drop", "#files", function(event) {
				event.preventDefault();
				event.stopPropagation();
				var files = event.originalEvent.dataTransfer.files;
				var deriID;
				if ($(".aktiv").hasClass("derivat-folder")){
				   	deriID = $(".aktiv").data("deriID");
				}
				else{
				  	deriID = $(".aktiv").data("id");
				}
				var path;
			    if ($(".aktiv").data("path") == undefined){
			    	path = "";
			    }
			    else{
			    	path = $(".aktiv").data("path");
			    }
				var json = {
						"deriID": deriID,
						"path": path,
						"files": [],
					};
				$.each(files, function(i, file) {
					if (file.type != ""){
						var upload = new Upload(currentDocID, currentDeriID, currentPath, file);
						uploadList[upload.getID()] = upload;
					}
					else{
						showAlert(geti18n("db.alert.filetype"), false);
					}
				});
				existsCheck();
				dragcounter = 0;
				$("#upload-overlay").addClass("hidden");
			});
			
			$("body").on("click", ".btn-close-usb", function() {
				$("#upload-status-bar").animate({'height': '0px'}, 500, function() {
					$("#upload-status-bar").addClass("hidden");
					$("#upload-status-bar-table").html("");
				});
			});
			
			$("body").on("click", ".btn-mini-usb", function() {
				if($(this).data("status") == "maxi"){
					$(this).data("status", "mini");
					$("#upload-status-bar").animate({'height': '32px'}, 500, function() {
						$("#upload-status-bar-table").addClass("hidden");
					});
				}
				else{
					$(this).data("status", "maxi");
					$("#upload-status-bar-table").removeClass("hidden");
					$("#upload-status-bar").animate({'height': '300px'}, 500);
				}
			});
			
			$("body").on("dragenter", "#files", function(event) {
				if (dragcounter == 0){
					$("#upload-overlay").removeClass("hidden");
//					console.log("enter");
				}
				dragcounter++;
			});
			
			$("body").on("dragleave", "#files", function(event) {
				dragcounter--;
				if (dragcounter == 0){
					$("#upload-overlay").addClass("hidden");
//					console.log("leave");
				}
			});
			
			$("body").on("click", "#journal-info-button-edit", function() {
				getEditor($("li.folder.aktiv").data("docID"));
			});
			
			$("body").on("keydown", "#folder-list-search-input", function(key) {
				if(key.which == 13){
					searchJournals($(this).val());
				}
			});
			
			$("body").on("click", "#folder-list-search-button", function(key) {
				searchJournals($("#folder-list-search-input").val());
			});
			
			$("body").on("click", ".folder:not(.derivat) > .folder-name, .folder:not(.derivat) > span.icon", function() {
				derivateBrowserNavigation.selectDocument($(this).parent());
				derivateBrowserFileView.showDerivateOrDoc($(this).parent().data("docID"), "");
				currentDocID = $(this).parent().data("docID");
				currentDeriID = "";
				currentPath = "";
				$.address.path("/" + $(this).parent().data("docID") + "/");
			});
			
			$("body").on("click", ".button-expand", function() {
				derivateBrowserNavigation.expandDoc($(this).closest(".folder").data("docID"));
			});
			
			$("body").on("click", ".button-contract", function() {
				$(this).siblings("ul.children").addClass("hide-folder");
				$(this).siblings("ul.children").html("");
				$(this).removeClass("button-contract glyphicon-minus");
				$(this).addClass("button-expand glyphicon-plus");
			});
			
			$("body").on("click", ".derivat > .folder-name, .derivat > span.icon", function() {
				derivateBrowserNavigation.selectDocument($(this).parent());
				derivateBrowserFileView.showDerivateOrDoc($(this).parent().data("deriID"), "");
				currentDeriID = $(this).parent().data("deriID");
				currentPath = "";
				currentDocID = $(this).closest(".folder").data("docID");
				$.address.path("/"+ $(this).parent().data("deriID") + "/");
			});
			
			$("body").on("click", ".derivat-folder > .folder-name, .derivat-folder > span.icon", function() {
				derivateBrowserNavigation.selectDocument($(this).parent());
				derivateBrowserFileView.showDerivateOrDoc($(this).parent().data("deriID"), $(this).parent().data("path"));
				currentDeriID = $(this).parent().data("deriID");
				currentPath = $(this).parent().data("path");
				currentDocID = $(this).closest(".folder").data("docID");
				$.address.path("/" + $(this).parent().data("deriID")  + $(this).parent().data("path") + "/");
			});
			
			$("body").on("click", ".derivate-browser-breadcrumb-entry", function() {
				var deriID = $(this).data("deriID");
				var path = $(this).data("path");
				derivateBrowserNavigation.selectDocumentPerID(deriID, path);
				derivateBrowserFileView.showDerivateOrDoc(deriID, path);
				currentPath = path;
			});
			
			$("body").on("click", ".btn-folder", function() {
				var deriID = $(this).parents(".browser-table-folder").data("deriID");
				var path = $(this).parents(".browser-table-folder").data("path");
				derivateBrowserNavigation.selectDocumentPerID(deriID, path);
				derivateBrowserFileView.showDerivateOrDoc(deriID, path);
				currentPath = path;
			});
			
			$("body").on("click", ".btn-edit", function(){
				if (!$(this).data("edit")){
					$(this).data("edit", true);
					var name = $(this).parents(".browser-table-entry").find(".browser-table-file-name");
					name.data("oldName", name.html());
					name.html("<input type='text' value='" + name.data("oldName") + "' class='form-control input-sm'></input>");
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
						if ($(this).val() != $(this).parent().data("oldName")){
							var main = ($(this).parents(".browser-table-entry").data("startfile") == true ? "true" : "false");
							renameFile($(this).parents(".browser-table-entry").data("path"), $(this).parents(".browser-table-entry").data("deriID"), $(this).val(), main, $(this).parents(".browser-table-entry"));
						}
						else{
							$(this).parent().html($(this).parent().data("oldName"));
						}
					}
					if ( event.which == 27 ) {
						$(this).parent().html($(this).parent().data("oldName"));
					}
				}
			});
			
			$("body").on("click", ".btn-delete", function(){
				var entry = $(this).parents(".browser-table-entry");
				var startfile = $("#derivat-panel-startfile").data("startfile");
				if ($(entry).data("startfile") != true && startfile.indexOf($(entry).data("path")) != 0){
//					var json = {
//							"files": []
//						};
//					var json2 = {
//							"deriID": $(entry).data("deriID"),
//							"path": $(entry).data("path")
//					}
//					json.files.push(json2);					
//					if(confirm(geti18n("db.alert.delete.confirm",$(entry).data("path")))){
//						deleteFile(entry);
//					}
					entry.addClass("delete");
					showDeleteAlert();
				}
				else{
					showAlert(geti18n("db.alert.delete.startfile"), false);
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
					checkIfNothingSelected();
				}
				else{
					$(parent).removeClass("checked");
					$(parent).removeData("checked");
					$(this).addClass("glyphicon-unchecked");
					checkIfNothingSelected();
				}

			});
			
			$("body").on("click", ".btn-check-all", function(){
				if ($(this).data("checked") != true){
					$(this).removeClass("glyphicon-unchecked");
					$(this).addClass("glyphicon-check");
					$(this).data("checked", true);
					$(".browser-table-entry:visible").each(function(i, node) {
						$(node).addClass("checked");
						$(node).data("checked", true);
						$(node).find(".btn-check").removeClass("glyphicon-unchecked");
						$(node).find(".btn-check").removeClass("invisable");
						$(node).find(".btn-check").addClass("glyphicon-check");
					});
					checkIfNothingSelected();
				}
				else{
					$(this).addClass("glyphicon-unchecked");
					$(this).removeClass("glyphicon-check");
					$(this).removeData("checked");
					$(".browser-table-entry.checked").each(function(i, node) {
						$(node).removeClass("checked");
						$(node).removeData("checked");
						$(node).find(".btn-check").addClass("glyphicon-unchecked");
						$(node).find(".btn-check").addClass("invisable");
						$(node).find(".btn-check").removeClass("glyphicon-check");
					});
					checkIfNothingSelected();
				}
			});
			
			$("body").on("click", ".btn-delete-all", function(){				
				var json = {
						"files": []
					};
				var canDelete = true;
				var startfile = $("#derivat-panel-startfile").data("startfile");
				if($(".glyphicon-check").length > 0){
					var entrys =  $(".browser-table-entry").filter(function() {
										return $(this).data("checked") == true;
									});
					entrys.each(function(i, node) {
						if ($(this).data("startfile") != true && startfile.indexOf($(this).data("path")) != 0){
							$(node).addClass("delete");
						}
						else{
							showAlert(geti18n("db.alert.delete.startfile"), false);
							canDelete = false;
							return;
						}
					});
				}				
				if(canDelete){
					showDeleteAlert();
				}
				else{
					$(".delete").removeClass("delete");
				}
			});
			
			$("body").on("click", "#lightbox-alert-delete-confirm", function(){				
				var json = {
						"files": []
					};
				$(".delete").each(function(i) {
					var json2 = {
							"deriID": $(this).data("deriID"),
							"path": $(this).data("path")
					}
					json.files.push(json2);
				});
				deleteMultipleFiles(json);
				$("#lightbox-alert-delete").modal('hide');
			});
			
			$("body").on("click", ".lightbox-alert-delete-cancel", function(){
				$("#lightbox-alert-delete-list").html("");
				$(".delete").removeClass("delete");
				$("#lightbox-alert-delete").modal('hide');
			});
			
			$("body").on("click", ".btn-add", function(){
				derivateBrowserFileView.createTempFolder();
			});
			
			$("body").on("keydown", ".input-new", function(event){
				if ( event.which == 13 ) {
					var parent = $(this).parents(".browser-table-folder");
					newFolder($(this).val(), $(this).data("temp"));
				}
				if ( event.which == 27 ) {
					$(this).parents(".browser-table-entry").remove();
				}
			});
			
			$("body").on("mouseenter", ".browser-table-entry", function() {
				$(this).find("span.btn").removeClass("invisable");
				$(this).find("div.no-urn").addClass("hidden");
			});
			
			$("body").on("mouseleave", ".browser-table-entry", function() {
				var file = $(this);
				$(this).find("span.btn").addClass("invisable");
				$(this).find("div.no-urn").removeClass("hidden");
				$(this).find("span.btn-check").filter(function() {
					return file.data("checked") == true;
				}).removeClass("invisable");
			});
			
			$("body").on("click", "#collapse-btn", function() {
				if ($(this).hasClass("glyphicon-chevron-up")){
					$("#browser-table-wrapper").addClass("small-table");
					$(this).removeClass("glyphicon-chevron-up");
					$(this).addClass("glyphicon-chevron-down");
				}
				else{
					$("#browser-table-wrapper").removeClass("small-table");
					$(this).removeClass("glyphicon-chevron-down");
					$(this).addClass("glyphicon-chevron-up");
				}
			});
			
			$("body").on("mouseenter", "#panel-img", function() {
				$("#derivat-panel-startfile-overlay-div").removeClass("hidden");
			});
			
			$("body").on("mouseleave", "#derivat-panel-startfile-overlay-div", function() {
				$("#derivat-panel-startfile-overlay-div").addClass("hidden");
			});
					
			$("body").on("click", ".btn-move-all", function(){				
				if($(".glyphicon-check").length > 0){
					getTargetFolders($("#derivat-panel-name").html());
				}
			});
			
			$("body").on("click", ".target-folder-entry > .folder-name, .target-folder-entry > span.icon", function() {
				if (!$(this).hasClass("faded")){
					$(".target-folder-selected").removeClass("target-folder-selected");
					$(this).parent().addClass("target-folder-selected");
				}
			});
			
			$("body").on("click", "#lightbox-multi-move-confirm", function() {
				if ($(".target-folder-selected").length > 0){
					var moveTo = $("#target-panel-childlist div.folder-name").html() + ":" + $(".target-folder-selected").attr("data-path");
					var json = {
							"moveTo": moveTo,
							"files": [],
					};
					
					if($(".glyphicon-check").length > 0){
						var entrys =  $(".browser-table-entry").filter(function() {
							return $(this).data("checked") == true;
						});
						entrys.each(function(i, node) {
							var file = $(this).data("deriID") + ":" + $(this).data("path");
							var path = "/";
							var type = "file";
							if ($(this).hasClass("browser-table-folder")){
								type = "folder";
							}
							if (file != moveTo && $(this).data("deriID") + ":" + currentPath != moveTo){
								json2 ={
										"file": file,
										"type": type
								};
								json.files.push(json2);
							}
						});
					}
					moveFiles(json);
				}
				else{
					showAlert(geti18n("db.alert.move.targetFolder"), false);
				}
			});
			
//			$(derivateBrowserNavigation).on("Test", function() {
//				console.log("TEST");
//			});
			
			$(derivateBrowserNavigation).on("DerivatFolder", function(e, name, filepath, deriID, absPath) {
				derivateBrowserNavigation.addChildToDerivat(name, filepath, deriID, absPath);
			});
			readQueryParameter();
			var lang =  qpara["lang"];
			if (lang == undefined) lang = "de";
			jQuery.getJSON("../../servlets/MCRLocaleServlet/" + lang + "/db.*", function(data) { 
				i18nKeys = data;
				changeAllI18n();
			});	
			jp.baseURL = "/";
		}
	}

	function renameFile(oldName, deriID, newName, start, entry){		
		$.ajax({
			url: "/rsc/derivatebrowser/rename?file=" + deriID + ":" + oldName + "&name=" +  newName + "&mainFile=" + start,
			type: "POST",
			dataType: "json",
			statusCode: {
				200: function() {
					$(entry).data("path", oldName.substring(0, oldName.lastIndexOf("/") + 1) + newName);
					$(entry).find(".browser-table-file-name").html(newName);
					$(entry).find(".browser-table-file-name").removeData("oldName");
					if (start == "true"){
						$("#derivat-panel-startfile").data("startfile", $(entry).data("path"));
						setStartFile(entry);
					}
				},
				409: function(error) {
					$(entry).find(".browser-table-file-name").addClass("has-error");
					showAlert(geti18n("db.alert.rename.already", newName), false);
				},
				500: function(error) {
					showAlert(geti18n("db.alert.rename.error", oldName), false);
				}
			}
		});
	}
	
//	function deleteFile(entry){
//		$.ajax({
//			url: "/rsc/derivatebrowser/" + entry.data("deriID") + entry.data("path"),
//			type: "DELETE",
//			dataType: "json",
//			statusCode: {
//				200: function() {
//					derivateBrowserNavigation.removeDocPerID(entry.data("deriID"), entry.data("path"));
//					derivateBrowserFileView.removeFile(entry);
//					checkIfNothingSelected();
//				},
//				500: function(error) {
//					showAlert(geti18n("db.alert.delete.error", entry.data("path")) , false);
//				}
//			}
//		});
//	}
	
	function deleteMultipleFiles(json){
		$.ajax({
			url: "/rsc/derivatebrowser/multiple",
			type: "DELETE",
			contentType: 'application/json',
			dataType: "json",
			data: JSON.stringify(json),
			statusCode: {
				200: function(data) {
					var notAll = 0;
					$.each(data.files, function(i, file) {
						if (file.status == 1){
							derivateBrowserFileView.removeFileWithPath(file.path);
							derivateBrowserNavigation.removeDocPerID(file.deriID, file.path);
							checkIfNothingSelected();
						}
						else{
							notAll++;
						}
					});
					if (notAll > 0){
						showAlert(geti18n("db.alert.delete.notAll", notAll) , false);
					}
					$(".delete").removeClass("delete");
				},
				500: function(error) {
					showAlert(geti18n("db.alert.delete.errorMulti"), false);
					$(".delete").removeClass("delete");
				}
			}
		});
	}
	
	function newFolder(input, temp) {
		var path = currentDeriID + currentPath + "/" +  input;
		$.ajax({
			url: "/rsc/derivatebrowser/" + path ,
			type: "POST",
			dataType: "json",
			statusCode: {
				200: function() {
					derivateBrowserFileView.tempToFolder(input, temp);
				},
				500: function(error) {
					showAlert(geti18n("db.alert.newFolder.error", input), false);
				},
				409: function(error) {
					showAlert(geti18n("db.alert.newFolder.already", input), false);
				}
			}
		});
	}
	
	function changeStartFile(entry){
		$.ajax({
			url: "/rsc/derivatebrowser/" + entry.data("deriID") + entry.data("path") + "/main",
			type: "PUT",
			dataType: "json",
			statusCode: {
				200: function() {
					$("#derivat-panel-startfile").data("startfile", $(entry).data("path"));
					setStartFile(entry);
				},
				500: function(error) {
					var oldStartfile = $("#derivat-panel-startfile").data("startfile");
					var oldEntry = $(".browser-table-file").filter(function() {
						return $(this).data("path") == oldStartfile;
					});
					setStartFile(oldEntry);
					$("#derivat-panel-startfile").data("startfile", $(oldEntry).data("path"));
					showAlert(geti18n("db.alert.startfile", input), false);
				}
			}
		});
	}
	
	function getTargetFolders(deriID){
		$.ajax({
			url: "/rsc/derivatebrowser/folders/" + deriID,
			type: "GET",
			dataType: "json",
			success: function(data) {
						var template = $("#target-folder-entry-template").html();
						var output = Mustache.render(template, data,  { recursive : template});
						$("#target-panel-childlist").html(output);
						var path = currentPath;
						if (path == "") path = "/";
						$("li.target-folder-entry[data-path='" + path + "'] > div").addClass("faded");
						$("li.target-folder-entry[data-path='" + path + "'] > span").addClass("faded");
						$('#lightbox-multi-move').modal('show');
					},
			error: function(error) {
						showAlert(geti18n("db.alert.move.noTargetFolder"), false);
					}
		});
	}
	
	function moveFiles(json) {
		$.ajax({
			url: "/rsc/derivatebrowser/moveDeriFiles",
			type: "POST",
			contentType: 'application/json',
			dataType: "json",
			data: JSON.stringify(json),
			statusCode: {
				200: function(data) {
					var notMoved = 0;
					$.each(data.files, function(i, oneFile) {
						if (oneFile.status == "1"){
							var deriID = oneFile.file.substr(0, oneFile.file.indexOf(":"));
							var path = oneFile.file.substr(oneFile.file.indexOf(":") + 1);
							var name = oneFile.file.substr(oneFile.file.lastIndexOf("/") + 1);
							derivateBrowserFileView.removeFileWithPath(path);
							derivateBrowserNavigation.removeDocPerID(deriID, path);
							if (oneFile.type == "folder"){
								derivateBrowserNavigation.addChildToDerivat(name, path, deriID, data.moveTo.substr(oneFile.file.indexOf(":") + 1));
							};							
						}
						else{
							notMoved++;
						}
					});
					$('#lightbox-multi-move').modal('hide');
					if (notMoved > 0){
						showAlert(geti18n("db.alert.move.notAll", notMoved), false);
					}
				},
				500: function(error) {
					showAlert(geti18n("db.alert.move.error"), false);
				}
			}
		});
	}
	
	function getEditor(id) {
		$.ajax({
			url: "/editor/start.xed?id=" + id + "&type=" + id.substring(id.indexOf("_") + 1, id.lastIndexOf("_")) + "&action=update",
			type: "GET",
			dataType: "html",
			statusCode: {
				200: function(data) {
					var html = $(data);
					$("#journal-info-text").html($("<div></div>").append(html).find("#main"));
					$(".twoButtons > input").each(function( index ) {
						$(this).attr("onClick", "");
					});
				},
				500: function(error) {
					alert(error);
				}
			}
		});
	}
	
	function uploadFile(upload){
		$.ajax({
			url: "/rsc/derivatebrowser/upload",
			type: "POST",
			processData: false,
			contentType : false,
			data: upload.getFormData(),
			xhr: function() {
						var xhr = new window.XMLHttpRequest();
						xhr.upload.addEventListener("progress", function(evt) {
							if (evt.lengthComputable){
								var percentComplete = evt.loaded / evt.total;
								$(upload.statusbar).find(".statusbar-progress-status").width(percentComplete + '%');
								$(upload.statusbar).find(".statusbar-progress-status").html(percentComplete + '%');
							}
						}, false);
						return xhr;
			},
			success: function() {
						$(upload.statusbar).find(".upload-preview-status").html(geti18n("db.alert.upload.success"));
						if (upload.exists){
							derivateBrowserFileView.removeFileWithPath(upload.getCompletePath());
						}
						derivateBrowserFileView.addFile(upload.getaddToBrowserJson());
						uploadFilesAndAsk();
					},
			error: function(error) {
						showAlert(geti18n("db.alert.upload.error"), false);
					}
		});		
	}	
	
	function addURN(json) {
		$.ajax({
			url: "/rsc/derivatebrowser/addURN",
			type: "POST",
			contentType: 'application/json',
			dataType: "json",
			data: JSON.stringify(json),
			statusCode: {
				200: function(data) {
					if (json.completeDeri){
						derivateBrowserFileView.showDerivateOrDoc(currentDeriID, currentPath);
					}
					else{
						var dID = data.deriID;
						$.each(data.files, function(i, file) {
							if (file.URN != ""){
								$(".browser-table-file").filter(function() {
									return ($(this).data("deriID") == dID) && ($(this).data("path") == file.path);
								}).find("td.browser-table-file-urn").html(file.URN);
							}
							else{
								showAlert(geti18n("db.alert.urn"), false);
							}
						});	
					}
				},
				500: function(error) {
					showAlert(geti18n("db.alert.urn"), false);
				}
			}
		});
	}
	
	function doExistsCheck(json) {
		$.ajax({
			url: "/rsc/derivatebrowser/exists",
			type: "POST",
			contentType: 'application/json',
			dataType: "json",
			data: JSON.stringify(json),
			statusCode: {
				200: function(data) {
					currentUploadCheck.data = data;
					currentUploadCheck.currentPos = 0;
					currentUploadCheck.overwriteAll = false;
					currentUploadCheck.skipAll = false;
					$(".btn-upload-all").removeData("check");
					$(".btn-upload-all").addClass("glyphicon-unchecked");
					$(".btn-upload-all").removeClass("glyphicon-check");
					uploadFilesAndAsk();
				},
				500: function(error) {
					showAlert(geti18n("db.alert.upload.error"), false);
				}
			}
		});
	}
	
	function moveDocTo(json) {
		$.ajax({
			url: "moveDocs",
			type: "PUT",
			contentType: 'application/json',
			data: JSON.stringify(json),
			statusCode: {
				200: function() {
//					console.log("moved");
					derivateBrowserNavigation.removeDocPerID(json[0].objId);
					derivateBrowserNavigation.addDoc(json[0].objId);
				},
				401: function() {
					showAlert(geti18n("db.alert.unauthorized"), false);
				},
				500: function(error) {
					showAlert(geti18n("db.alert.move.error"), false);
				}
			}
		});	
	}

	function geti18n(key) {
		var string = i18nKeys[key];
		if (string != undefined){
			for (i = 0; i < arguments.length-1; i++){
				string = string.replace(new RegExp('\\{' + i + '\\}', "g"), arguments[i+1]);
			}
			return string;
		}
		else{
			return "";
		}
	}

	function setStartFile(entry) {
		$(".startfile").removeClass("startfile");
		$(".browser-table-file").filter(function() {
			return $(this).data("startfile") == true;
		}).removeData("startfile");
		$(entry).data("startfile", true);
		$("#panel-img").attr("src", "/servlets/MCRFileNodeServlet/" + $(entry).data("deriID") + $(entry).data("path"));
		$("#derivat-panel-startfile-label").html($(entry).data("path"));
	}

	function existsCheck() {
		var json = {
				"deriID": currentDeriID,
				"path": currentPath,
				"files": [],
			};
		$.each(uploadList, function(index, value) {
			if (value instanceof Upload){
				if (value.exists == undefined){
					json.files.push(value.getCheckJson());
				}
			}
		});
		doExistsCheck(json)
	}
	
	function uploadFilesAndAsk() {		
		var data = currentUploadCheck.data;
		if(currentUploadCheck.currentPos < Object.keys(data.files).length){			
			var file = data.files[currentUploadCheck.currentPos];
			var upload = uploadList[file.id];
			//TODO fix abfrage
			if (file.exists == "1" && currentUploadCheck.skipAll){
				currentUploadCheck.currentPos = currentUploadCheck.currentPos + 1;
				uploadFilesAndAsk();
				return false;
			}
			if (file.exists == "2"){
				currentUploadCheck.currentPos = currentUploadCheck.currentPos + 1;
				uploadFilesAndAsk();
				//TODO show Alert Filetype not supported
				return false;
			}
			if (file.exists == "1" && !currentUploadCheck.overwriteAll){
				upload.exists = true;
				upload.askOverwrite(file.existingFile, data.deriID, data.path);
			}
			if (file.exists == "1" && currentUploadCheck.overwriteAll){
				upload.exists = true;
			}
			if (file.exists == "0"){
				upload.exists = false;
			}
			if (file.exists == "0" || currentUploadCheck.overwriteAll){
				upload.statusbar = $("#upload-status-bar-table").append(upload.getStatus());
				showUploadBar();
				uploadFile(upload);
				currentUploadCheck.currentPos = currentUploadCheck.currentPos + 1;
			}
		}
		else{
			currentUploadCheck = {};
		}
	}
	
	function searchJournals(query) {
		$("#folder-list-ul").html("");
		$("#derivate-browser").addClass("hidden");
		$("#derivat-panel").addClass("hidden");
		if (query == "") query = "*";
		derivateBrowserNavigation.searchJournals(query);
	}
	
	function showUploadBar() {
		$("#upload-status-bar-table").removeClass("hidden");
		$("#upload-status-bar").removeClass("hidden");
		$("#upload-status-bar").animate({'height': '300px'}, 500);
		$(".btn-mini-usb").data("status", "maxi");
	}
	
	function checkIfNothingSelected() {
		if ($(".browser-table-entry .glyphicon-check").length == 0){
			$(".btn-delete-all").addClass("faded");
			$(".btn-move-all").addClass("faded");
		}
		else{
			$(".btn-delete-all").removeClass("faded");
			$(".btn-move-all").removeClass("faded");
		}
	}
	
	function showAlert(text, success) {
		$('#alert-area').removeClass("show-alert");
		$("#alert-area").removeClass("alert-success");
		$("#alert-area").removeClass("alert-danger");
		if (timeOutID != null){
			window.clearTimeout(timeOutID);
		}

		$("#alert-area").html(text);
		if (success){
			$("#alert-area").addClass("alert-success");
			$("#alert-area").addClass("show-alert");
		}
		else{
			$("#alert-area").addClass("alert-danger");
			$("#alert-area").addClass("show-alert");
		}
		timeOutID = window.setTimeout(function() {
				$('#alert-area').removeClass("show-alert");
			}, 5000);
	}
	
	function readQueryParameter() {
		var q = document.URL.split(/\?(.+)?/)[1];
		if(q != undefined){
	        q = q.split('&');
	        for(var i = 0; i < q.length; i++){
	            hash = q[i].split(/=(.+)?/);
	            qpara.push(hash[1]);
	            qpara[hash[0]] = hash[1];
	        }
		}
	}
	
	function changeAllI18n() {
		$("#folder-list-search-input").attr("placeholder", i18nKeys["db.label.search"]); 
		$(".i18n").each(function(i, elm) {
			var i18nKey = i18nKeys[$(elm).attr("i18n")];
			if (i18nKey != undefined){
				$(elm).html(i18nKey);
			}
			else{
				$(elm).html($(elm).attr("i18n-def"));
			}
		});
	}
	
	function showDeleteAlert(json) {
		$("#lightbox-alert-delete-list").html("");
		$(".delete").each(function(i) {
			$("#lightbox-alert-delete-list").append("<p>" + $(this).data("path") + "</p>")
		});
		$("#lightbox-alert-delete").modal('show');
	}
}

$(document).ready(function() {
	var DerivateBrowserInstance = new DerivateBrowser();
	DerivateBrowserInstance.init();
});