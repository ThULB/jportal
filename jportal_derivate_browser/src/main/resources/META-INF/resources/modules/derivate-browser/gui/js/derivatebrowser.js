var DerivateBrowser = function(){
	var i18nKeys =[];
	var mode = "normal";
	var dragcounter = 0;

	return {
		init: function() {
			
			$("body").on("click", "span.btn-new-urn", function(event) {
				var entry = $(this).parents(".browser-table-entry");
				var json = {
						"deriID": entry.data("deriID"),
						"files": []
					};
				var json2 = {
						"path": entry.data("path")
				}
				json.files.push(json2);
//				console.log(json);
				addURN(json);
			});			
			
//			$("body").on("click", ".twoButtons > input", function(event) {
//				event.preventDefault();
//			});
			
			$("body").on("drop", "#files", function(event) {
				event.preventDefault();
				event.stopPropagation();
				var files = event.originalEvent.dataTransfer.files;
				console.log(files);
				$.each(files, function(i, file) {
					if (file.type != "" && file.type != "text/xml"){
					    var data = new FormData();
					    data.append("documentID", $(".aktiv").closest(".folder").data("id"));
					    data.append("derivateID", $(".aktiv").data("deriID"));
					    if ($(".aktiv").data("path") == undefined){
					    	data.append("path", "");
					    }
					    else{
					    	data.append("path", $(".aktiv").data("path") + "/");
					    }
					    data.append("derivateID", $(".aktiv").data("id"));
					    data.append("file", file);
					    //createStatusBar($("#upload-status-bar-table"), file);
						uploadFile(data, createStatusBar($("#upload-status-bar-table"), file), file);
					}
					else{
						alert("invalid File Type")
					}
				});
				dragcounter = 0;
				$("#upload-overlay").addClass("hidden");
				$("#upload-status-bar").removeClass("hidden");
			});
			
			$("body").on("click", ".btn-close-usb", function() {
				$("#upload-status-bar").addClass("hidden");
				$("#upload-status-bar-table").html("");
			});
			
			$("body").on("dragenter", "#files", function(event) {
				if (dragcounter == 0){
					$("#upload-overlay").removeClass("hidden");
					console.log("enter");
				}
				dragcounter++;
			});
			
			$("body").on("dragleave", "#files", function(event) {
				dragcounter--;
				if (dragcounter == 0){
					$("#upload-overlay").addClass("hidden");
					console.log("leave");
				}
			});
			
			$("body").on("click", "#journal-info-button-edit", function() {
				getEditor($("li.folder.aktiv").data("id"));
			});
			
			$("body").on("keydown", "#folder-list-search-input", function(key) {
				if(key.which == 13){
					searchJournals($(this).val());
				}
			});
			
			$("body").on("click", "#folder-list-search-button", function(key) {
				searchJournals($("#folder-list-search-input").val());
			});
			
			$("body").on("click", ".folder > .folder-name, .folder > span.icon", function() {
				$("#derivate-browser").addClass("hidden");
				$("#derivat-panel").addClass("hidden");
				$("#browser-table-files").html("");
				highlight($(this).parent());
				jp.subselect.get($(this).parent().data("id"), addInfo);
			});
			
			$("body").on("click", ".button-expand", function() {
				getDerivateIDs($(this).siblings("ul.children"), $(this).closest(".folder").data("id"));
				expandFolder($(this).closest(".folder"));
			});
			
			$("body").on("click", ".button-contract", function() {
				$(this).siblings("ul.children").addClass("hide-folder");
				$(this).siblings("ul.children").html("");
//				$(this).siblings(".icon").removeClass("glyphicon-folder-open");
//				$(this).siblings(".icon").addClass("glyphicon-folder-close");
				$(this).removeClass("button-contract glyphicon-minus");
				$(this).addClass("button-expand glyphicon-plus");
			});
			
			$("body").on("click", ".derivat > .folder-name, .derivat > span.icon", function() {
				$("#journal-info").addClass("hidden");
				highlight($(this).parent());
				getDerivate($(this).parent().data("id"), "");
			});
			
			$("body").on("click", ".derivat-folder > .folder-name, .derivat-folder > span.icon", function() {
				$("#journal-info").addClass("hidden");
				highlight($(this).parent());
				getDerivate($(this).parent().data("deriID"), $(this).parent().data("path"));
			});
			
			$("body").on("click", ".derivate-browser-breadcrumb-entry", function() {
				var deriID = $(this).data("deriID");
				var path = $(this).data("path");
				highlight($(".derivat-folder, .derivat").filter(function() {
					return $(this).data("id") == deriID + path;
				}));
				getDerivate(deriID, path);
			});
			
			$("body").on("click", ".btn-folder", function() {
				var deriID = $(this).parents(".browser-table-folder").data("deriID");
				var path = $(this).parents(".browser-table-folder").data("path");
				var parentInTreeView = $(".derivat-folder").filter(function() {
					return $(this).data("id") == deriID + path;
				});
				highlight(parentInTreeView);
				getDerivate(deriID, path);
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
						renameFile($(this).parents(".browser-table-entry").data("path"), $(this).parents(".browser-table-entry").data("deriID"), $(this).val(), $(this).parents(".browser-table-entry"));
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
					if(confirm("Datei " + $(entry).data("path") + "wirklich löschen?")){
						deleteFile(entry);
					}
				}
				else{
					alert("You can not delete the Startfile.")
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
							var json2 = {
									"deriID": $(this).data("deriID"),
									"path": $(this).data("path")
							}
							json.files.push(json2);
						}
						else{
							alert("You can not delete the Startfile.");
							canDelete = false;
							return;
						}
					});
//						$(".browser-table-entry").filter(function() {
//							return $(this).data("checked") == true;
//						}).remove();
				}
				if(canDelete && confirm("Ausgewählte Dateien wirklich löschen?")){
					deleteMultipleFiles(entrys, json);
				}
			});
			
			$("body").on("click", ".btn-add", function(){
				var tr = $("<tr class='browser-table-folder browser-table-entry'></tr>");
				var td = $("<td><span class='glyphicon glyphicon-folder-close btn-folder'></span></td>");
				tr.append(td);
				tr.append("<td><div class='btns'><span class='glyphicon glyphicon-unchecked btn-check btn invisable'></span><span class='glyphicon glyphicon-edit btn-edit btn invisable'></span><span class='glyphicon glyphicon-trash btn-delete btn invisable'></span></div></td>");
				tr.append("<td class='browser-table-file-name'><input class='input-new' type='text' value=''></input></td>");
				tr.append("<td class='browser-table-file-urn'>-</td>");
				$("#browser-table-files").append(tr);
			});
			
			$("body").on("keydown", ".input-new", function(event){
				if ( event.which == 13 ) {
					newFolder($(this));
				}
				if ( event.which == 27 ) {
					$(this).parents(".browser-table-entry").remove();
				}
			});
			
			$("body").on("mouseenter", ".browser-table-entry", function() {
				if (mode != "startfile"){
					$(this).find("span.btn").removeClass("invisable");
					$(this).find("div.no-urn").addClass("hidden");
				}
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
					$(this).removeClass("glyphicon-chevron-up");
					$(this).addClass("glyphicon-chevron-down");
				}
				else{
					$(this).removeClass("glyphicon-chevron-down");
					$(this).addClass("glyphicon-chevron-up");
				}
			});
			
			$("body").on("mouseenter", "#panel-img", function() {
				$("#derivat-panel-startfile-btn-div").removeClass("hidden");
			});
			
			$("body").on("mouseleave", "#derivat-panel-startfile-btn-div", function() {
				if(mode != "startfile"){
					$("#derivat-panel-startfile-btn-div").addClass("hidden");
				}
			});
			
			$("body").on("click", "#derivat-panel-startfile-btn-edit", function() {
				if (mode == "normal"){
					toggleStartMode();
				}
			});
			
			$("body").on("click", "#derivat-panel-startfile-btn-confirm", function() {
				if (mode == "startfile"){
					var newStartFile = $(".browser-table-entry").filter(function() {
						return $(this).data("startfile") == true;
					});
					changeStartFile(newStartFile);
				}
			});
			
			$("body").on("click", "#derivat-panel-startfile-btn-cancel", function() {
				if (mode == "startfile"){
					var oldStartfile = $("#derivat-panel-startfile").data("startfile");
					var oldEntry = $(".browser-table-file").filter(function() {
						return $(this).data("path") == oldStartfile;
					});
					if (oldEntry.length != 0){
						setStartFile(oldEntry);
					}
					else{
						$("#panel-img").attr("src", $("#panel-img").data("oldSrc"));
						$(".startfile").removeClass("startfile");
						$(".browser-table-file").filter(function() {
							return $(this).data("startfile") == true;
						}).removeData("startfile");
						$("#derivat-panel-startfile-label").html($("#derivat-panel-startfile").data("startfile"));
					}
					toggleStartMode();
				}
			});
			
			$("body").on("click", ".browser-table-file", function() {
				if (mode == "startfile"){
					setStartFile($(this));
				}
			});
			
			$("body").on("click", ".btn-move-all", function(){				
				if($(".glyphicon-check").length > 0){
					getTargetFolders($("#derivat-panel-name").html());
				}
			});
			
			$("body").on("click", ".target-folder-entry > .folder-name, .target-folder-entry > span.icon", function() {
				$(".target-folder-selected").removeClass("target-folder-selected");
				$(this).parent().addClass("target-folder-selected");
			});
			
			$("body").on("click", "#lightbox-multi-move-confirm", function() {
				var moveTo = $("#derivat-panel-name").html() + ":" + $(".target-folder-selected").attr("data-path");
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
						if ($("li.aktiv").data("id").indexOf("/") > -1){
							path = $("li.aktiv").data("id").substr($("li.aktiv").data("id").indexOf("/"));
						}
						if (file != moveTo && $(this).data("deriID") + ":" + path != moveTo){
							json.files.push(file);
						}
					});
				}
				moveFiles(json);
			});
			jp.baseURL = "/";
		}
	}
	function getJournals(query, start) {
		var url = "/servlets/solr/select?q=" + query + "&start=" + start +  "&rows=10&fq=objectType%3Ajpjournal&sort=maintitle+asc&wt=json&indent=true";
		$.getJSON(url, function(search){
			if(search.response.numFound > 0){
				var results = search.response.docs;
				for (result in results){
					createFolder($("#folder-list-ul"), results[result]);
				}
			}
			if(search.response.numFound > 10){
				getJournals(start + 10);
			}
		});
	}	
	
	function getChilds(node, id,  start) {
		$.getJSON("/servlets/solr/select?q=%2Bparent%3A" + id + "&start=" + start + "&rows=100&wt=json&sort=maintitle%20asc&wt=json", function(search){
			if(search.response.numFound > 0){
				var results = search.response.docs;
				for (result in results){
					createFolder(node, results[result]);
				}
				if(search.response.numFound > 100 && search.response.start < search.response.numFound){
					getChilds(node, id, start + 100);
				}
			}
		});
	};
	
	function getDerivateIDs(node, id) {
		$.ajax({
			url: "/rsc/derivatebrowser/deriid/" + id,
			type: "GET",
			dataType: "json",
			success: function(data) {
						for (id in data){
							createDerivate(node, data[id]);
						}
						getChilds(node, id, 0);
					},
			error: function(error) {
						getChilds(node, id, 0);
					}
		});
	}
	
	function getDerivate(deriID, path){
		$.ajax({
			url: "/rsc/derivatebrowser/" + deriID + path,
			type: "GET",
			dataType: "json",
			success: function(data) {
						showDerivatOrFolder(deriID, data);
//						createFiles($("#files"), data);
//						buildTable(data.children);
//						setStartFile(data.maindocName);
					},
			error: function(error) {
						alert(error);
					}
		});
	}

	function renameFile(oldName, deriID, newName, entry){		
		$.ajax({
			url: "/rsc/derivatebrowser/rename?file=" + deriID + ":" + oldName + "&name=" +  newName,
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
	
	function deleteFile(entry){
		$.ajax({
			url: "/rsc/derivatebrowser/" + entry.data("deriID") + entry.data("path"),
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
	
	function deleteMultipleFiles(entrys, json){
		$.ajax({
			url: "/rsc/derivatebrowser/multiple",
			type: "DELETE",
			contentType: 'application/json',
			dataType: "json",
			data: JSON.stringify(json),
			statusCode: {
				200: function(data) {
					$.each(data.files, function(i, file) {
						if (file.status == 1){
							$(entrys).filter(function() {
								return ($(this).data("deriID") == file.deriID) && ($(this).data("path") == file.path);
							}).remove();
							$(".derivat-folder").filter(function() {
								return ($(this).data("deriID") == file.deriID) && ($(this).data("path") == file.path);
							}).remove();
						}
					});
				},
				500: function(error) {
					alert(error);
				}
			}
		});
	}
	
	function newFolder(input) {
		var path = $("li.aktiv").data("id") + "/" + $(input).val();
		$.ajax({
			url: "/rsc/derivatebrowser/" + path ,
			type: "POST",
			dataType: "json",
			statusCode: {
				200: function() {
					$(input).parents(".browser-table-folder").data("deriID", path.substr(0, path.indexOf("/")));
					$(input).parents(".browser-table-folder").data("path", path.substr(path.indexOf("/")));
					$(input).parent().html($(input).val());
					createDerivateFolder($("li.aktiv").children("ul"), $(input).val(), path.substr(path.indexOf("/")), path.substr(0, path.indexOf("/")));
				},
				500: function(error) {
					alert(error);
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
					toggleStartMode();
				},
				500: function(error) {
					var oldStartfile = $("#derivat-panel-startfile").data("startfile");
					var oldEntry = $(".browser-table-file").filter(function() {
						return $(this).data("path") == oldStartfile;
					});
					setStartFile(oldEntry);
					$("#derivat-panel-startfile").data("startfile", $(oldEntry).data("path"));
					toggleStartMode();
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
						$('#lightbox-multi-move').modal('show');
					},
			error: function(error) {
						alert(error);
					}
		});
	}
	
	function moveFiles(json) {
		//console.log(json);
		$.ajax({
			url: "/rsc/derivatebrowser/move",
			type: "POST",
			contentType: 'application/json',
			dataType: "json",
			data: JSON.stringify(json),
			statusCode: {
				200: function() {
					$.each(json.files, function(i, file) {
						var deriID = file.substr(0, file.indexOf(":"));
						var path = file.substr(file.indexOf(":")+1);
						$(".browser-table-entry").filter(function() {
							return ($(this).data("deriID") == deriID) && ($(this).data("path") == path);
						}).remove();
						$(".derivat-folder").filter(function() {
							return ($(this).data("deriID") == deriID) && ($(this).data("path") == path);
						}).remove();
					});
					$('#lightbox-multi-move').modal('hide')							
				},
				500: function(error) {
					alert(error);
				}
			}
		});
	}
	
	function getEditor(id) {
		console.log(id);
		$.ajax({
			url: "/rsc/editor/update/" + id,
			type: "GET",
			dataType: "html",
			statusCode: {
				200: function(data) {
					var html = $(data);
//					console.log($("<div></div>").append(html).find("#main"));
//					console.log($(data).find("div"));
					$("#journal-info-text").html($("<div></div>").append(html).find("#main"));
					$(".twoButtons > input").each(function( index ) {
						$(this).attr("onClick", "");
					});
//					$("#journal-info-text").html(data);
				},
				500: function(error) {
					alert(error);
				}
			}
		});
	}
	
	function uploadFile(data, statusbar, file){
		$.ajax({
			url: "/rsc/derivatebrowser/upload",
			type: "POST",
			processData: false, 
			contentType: false,
			data: data,
			xhr: function() {
				var xhr = new window.XMLHttpRequest();
				xhr.upload.addEventListener("progress", function(evt) {
					if (evt.lengthComputable){
						var percentComplete = evt.loaded / evt.total;
						console.log(percentComplete);
						$(statusbar).find(".statusbar-progress-status").width(percentComplete + '%');
			            $(statusbar).find(".statusbar-progress-status").html(percentComplete + '%');
					}
				}, false);
				return xhr;
			},
			success: function(data) {
						console.log("Complete");
						$(statusbar).find(".upload-preview-status").html('Hochgeladen');
						addFileToDerivateBrowser({"name": file.name, "size": file.size, "lastmodified": file.lastModifiedDate, "absPath": "/" + file.name, "deriID": $(".aktiv").data("id")});
					},
			error: function(error) {
						alert(error);
					}
		});		
	}	
	
	function addURN(json) {
		//console.log(json);
		$.ajax({
			url: "/rsc/derivatebrowser/addURN",
			type: "POST",
			contentType: 'application/json',
			dataType: "json",
			data: JSON.stringify(json),
			statusCode: {
				200: function(data) {
					console.log(data);
					var dID = data.deriID;
					$.each(data.files, function(i, file) {
						console.log(file);
						if (file.URN != ""){
							$(".browser-table-file").filter(function() {
								return ($(this).data("deriID") == dID) && ($(this).data("path") == file.path);
							}).find("td.browser-table-file-urn").html(file.URN);
						}
					});					
				},
				500: function(error) {
					alert(error);
				}
			}
		});
	}
		
	function createFolder(parent, data) {
		var li = $("<li class='folder'><div class='folder-name'>" + data.maintitle + "</div></li>");
		if (data.objectType == "jpjournal"){
			$(li).prepend("<span class='glyphicon glyphicon-book icon'></span>");
		}
		else{
			$(li).prepend("<span class='glyphicon glyphicon-file icon'></span>");
		}
		if(data.childrenCount > 0 || data.derivateCount > 0){
			$(li).prepend("<span class='glyphicon glyphicon-plus button button-expand'></span>");
			var ul = $("<ul class='hide-folder children'></ul>");
			$(li).append(ul);
//			if(data.childrenCount > 0 ){
//				getChilds(ul, data.id, 0);
//			}
//			if(data.derivateCount > 0 ){
//				getDerivateIDs(ul, data.id);
//			}
		}
		else{
			$(li).prepend("<div class='no-button'>&nbsp;</div>");
		}
		$(li).data("id", data.id);
		$(parent).append(li);
	}
	
	function createDerivate(parent, data) {
		var li = $("<li class='derivat'><div class='folder-name'>" + data.id + "</div></li>");
		$(li).prepend("<span class='glyphicon glyphicon-picture icon'></span>");
		$(li).prepend("<div class='no-button'>&nbsp;</div>");
		$(li).append("<ul class='children'></ul>");
		$(li).data("id", data.id);
		$(parent).append(li);
	}
	
	function showDerivatOrFolder(deriID, data){
		var parentInTreeView = $(".derivat").filter(function() {
			return $(this).data("id") == deriID;
		}).children(".children");
		if (data.absPath != "/"){
			parentInTreeView = $(parentInTreeView).find(".derivat-folder").filter(function() {
				return $(this).data("path") == data.absPath;
			}).children(".children");
			$("#derivat-panel").removeClass("hidden");
		}
		else{
			showPanel(data);
		}
		$(parentInTreeView).html("");
		$("#browser-table-files").html("");
		var popoverTemplate = $("#popover-template").html();
		var fileEntryTemplate = $("#file-entry-template").html();
		var folderEntryTemplate = $("#folder-entry-template").html();
		$.each(data.children, function(i, file) {
			file.deriID = deriID;
			if (file.type == "file"){
				var popOverOutput = $(Mustache.render(popoverTemplate, file));
				var fileEntryOutput = $(Mustache.render(fileEntryTemplate, file));
				$(popOverOutput).find(".img-size").html(getReadableSize($(popOverOutput).find(".img-size").html(),0));
				$(fileEntryOutput).find(".popover-file").popover({content: popOverOutput, html: true});
				$(fileEntryOutput).data("path", file.absPath);
				$(fileEntryOutput).data("deriID", deriID);
				if (data.maindocName == file.absPath){
					$(fileEntryOutput).data("startfile", true);
				}
				$(fileEntryOutput).appendTo("#browser-table-files");
			}
			else{
				var folderEntryOutput = $(Mustache.render(folderEntryTemplate, file));
				$(folderEntryOutput).data("path", file.absPath);
				$(folderEntryOutput).data("deriID", deriID);
				$(folderEntryOutput).appendTo("#browser-table-files");
				createDerivateFolder(parentInTreeView, file.name, file.absPath , deriID);
			}
		});
		$("#derivate-browser").removeClass("hidden");
		createBreadcrumb(deriID, data.absPath);
	}
	
	function addFileToDerivateBrowser(file) {
		var popoverTemplate = $("#popover-template").html();
		var fileEntryTemplate = $("#file-entry-template").html();
		var popOverOutput = $(Mustache.render(popoverTemplate, file));
		var fileEntryOutput = $(Mustache.render(fileEntryTemplate, file));
		$(popOverOutput).find(".img-size").html(getReadableSize($(popOverOutput).find(".img-size").html(),0));
		$(fileEntryOutput).find(".popover-file").popover({content: popOverOutput, html: true});
		$(fileEntryOutput).data("path", file.absPath);
		$(fileEntryOutput).data("deriID", file.deriID);
		$(fileEntryOutput).appendTo("#browser-table-files");
	}
	
	function createDerivateFolder(parentInTreeView, name, path, id) {
		var li = $("<li class='derivat-folder'><div class='folder-name'>" + name + "</div></li>");
		$(li).prepend("<span class='glyphicon glyphicon-folder-close icon'></span>");
		$(li).prepend("<div class='no-button'>&nbsp;</div>");
		$(li).append("<ul class='children'></ul>");
		$(li).data("id", id + path);
		$(li).data("deriID", id);
		$(li).data("path", path);
		$(parentInTreeView).append(li);
	}

	function expandFolder(node) {
		var button = $(node).children(".button");
		if (button.hasClass("button-expand")){
			button.siblings("ul.children").removeClass("hide-folder");
			button.removeClass("button-expand glyphicon-plus");
			button.addClass("button-contract glyphicon-minus");
		}
	}

	function highlight(entry){
		$(".aktiv").removeClass("aktiv");
		entry.addClass("aktiv");
	}

	function showPanel(data) {
		$("#derivat-panel-name").html(data.name);
		$("#derivat-panel-size").html(getReadableSize(data.size, 0));
		$("#derivat-panel-last").html(data.lastmodified);
		$("#derivat-panel-startfile-label").html(data.maindocName);
		var path = data.maindocName;
		if (path.indexOf("/") != 0) path = "/" + path;
		$("#panel-img").attr("src", "/servlets/MCRFileNodeServlet/" + data.name + path);
		$("#derivat-panel-startfile").data("startfile", path);
		$("#derivat-panel").removeClass("hidden");
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
		$(entry).data("startfile", true).addClass("startfile");
		$("#panel-img").attr("src", "/servlets/MCRFileNodeServlet/" + $(entry).data("deriID") + $(entry).data("path"));
		$("#derivat-panel-startfile-label").html($(entry).data("path"));
	}

	function toggleStartMode() {
		if (mode == "normal"){
			$(".browser-table-entry").filter(function() {
				return $(this).data("startfile") == true;
			}).addClass("startfile");
			$("#derivat-panel-startfile-btn-edit").addClass("hidden");
			$("#derivat-panel-startfile-btn-confirm").removeClass("hidden");
			$("#derivat-panel-startfile-btn-cancel").removeClass("hidden");
			$("#panel-img").data("oldSrc", $("#panel-img").attr("src"));
			mode = "startfile";
		}
		else{
			$(".browser-table-entry").filter(function() {
				return $(this).data("startfile") == true;
			}).removeClass("startfile");
			$("#derivat-panel-startfile-btn-edit").removeClass("hidden");
			$("#derivat-panel-startfile-btn-confirm").addClass("hidden");
			$("#derivat-panel-startfile-btn-cancel").addClass("hidden");
			$("#panel-img").removeData("oldSrc");
			mode = "normal";
		}
	}

	function searchJournals(query) {
		$("#folder-list-ul").html("");
		$("#derivate-browser").addClass("hidden");
		$("#derivat-panel").addClass("hidden");
		if (query == "") query = "*";
		getJournals(query, 0);
	}
	
	function getReadableSize(size, unit) {
		var conSize = convertSize({number: size, unit: unit});
		var unitString = "";
		switch (conSize.unit){
			case 0:
				unitString = "bytes";
				break;
			case 1:
				unitString = "kB";
				break;
			case 2:
				unitString = "MB";
				break;
			case 3:
				unitString = "GB";
				break;
			default:
				unitString = "GB";
				break;
		}
		return conSize.number + " " + unitString;
	}
	
	function convertSize(sizeAndUnit){
		if (sizeAndUnit.unit < 3){
			if (sizeAndUnit.number > 1024){
				var size2 = Math.round((sizeAndUnit.number / 1024) * 100)/ 100;
				return convertSize({number: size2, unit: sizeAndUnit.unit + 1});
			}
		}
		return {number: sizeAndUnit.number, unit: sizeAndUnit.unit};
	}
	
	function addInfo(info) {
		$("#journal-info-text").html(info);
		$("#journal-info").removeClass("hidden");
	}
	
	function createBreadcrumb(deriID, path) {
		$("#derivate-browser-breadcrumb").html("");
		var firstli = $('<li class="derivate-browser-breadcrumb-entry">' + deriID + '</li>');
		firstli.data("deriID", deriID);
		firstli.data("path", "");
		$("#derivate-browser-breadcrumb").append(firstli);
		if (path != "/"){
			var tempPath = "";
			path = path.substring(1);
			$.each(path.split("/"), function(index, elm) {
				tempPath = tempPath + "/" + elm;
				var li = $('<li class="derivate-browser-breadcrumb-entry">' + elm + '</li>');
				li.data("deriID", deriID);
				li.data("path", tempPath);
				$("#derivate-browser-breadcrumb").append(li);
			});
		}
	}
	
	function createStatusBar(parent, file){
		var template = $("#upload-entry-template").html();
		var status = $(Mustache.render(template, {"name": file.name, "size": getReadableSize(file.size,0)}));
		if (file.type.match(/image.*/)){
			readImg(file, $(status).find("img.upload-preview-image"));
		}
		$(parent).append(status);
		return status;
	}
	
	function readImg(file, display) {
		var reader = new FileReader();
		reader.onload =  function(e) {
			display.attr("src", reader.result);
		}
		reader.readAsDataURL(file);
	}
}


$(document).ready(function() {
	var DerivateBrowserInstance = new DerivateBrowser();
	DerivateBrowserInstance.init();
});