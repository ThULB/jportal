
var derivateBrowserFileView = (function () {

    //private Properties
    var currentDocID = "",
        currentPath = "";


    //private Methods
	function showDerivatOrFolder(deriID, data){
		$("#journal-info").addClass("hidden");
		$(".btn-delete-all").addClass("faded");
		$(".btn-move-all").addClass("faded");
		if (data.absPath != undefined && data.absPath != "" && data.absPath != "/"){
			$("#derivat-panel").removeClass("hidden");
			showPanel(data, true);
		}
		else{
			showPanel(data, false);
		}
		$("#browser-table-files").html("");
//		var popoverTemplate = $("#popover-template").html();
//		var fileEntryTemplate = $("#file-entry-template").html();
//		var folderEntryTemplate = $("#folder-entry-template").html();
		$.each(data.children, function(i, file) {
			file.deriID = deriID;
			if (file.type == "file"){
				addFileToView(file, data.mainDoc);
//				var popOverOutput = $(Mustache.render(popoverTemplate, file));
//				var fileEntryOutput = $(Mustache.render(fileEntryTemplate, file));
//				$(popOverOutput).find(".img-size").html(getReadableSize($(popOverOutput).find(".img-size").html(),0));
//				$(fileEntryOutput).find(".popover-file").popover({content: popOverOutput, html: true});
//				$(fileEntryOutput).data("path", file.absPath);
//				$(fileEntryOutput).data("deriID", deriID);
//				$(fileEntryOutput).data("docID", deriID);
//				if (data.maindocName == file.absPath){
//					$(fileEntryOutput).data("startfile", true);
//				}
//				$(fileEntryOutput).appendTo("#browser-table-files");
			}
			else{
				addFolderToView(file, data.absPath);
//				var folderEntryOutput = $(Mustache.render(folderEntryTemplate, file));
//				$(folderEntryOutput).data("path", file.absPath);
//				$(folderEntryOutput).data("deriID", deriID);
//				$(folderEntryOutput).appendTo("#browser-table-files");
//				$(derivateBrowserNavigation).trigger("DerivatFolder", [file.name, file.absPath, deriID, data.absPath]);
//				//nicht so!!!
////				derivateBrowserNavigation.addChildToDerivat(file.name, file.absPath, deriID, data.absPath)
			}
		});
		$("#derivate-browser").removeClass("hidden");
		createBreadcrumb(deriID, data.absPath);
	}
	
	function addFileToView(file , mainDoc) {
		var fileEntryTemplate = $("#file-entry-template").html();
		var fileEntryOutput = $(Mustache.render(fileEntryTemplate, file));
		$(fileEntryOutput).find(".popover-file").data("lastMod", file.lastmodified);
		$(fileEntryOutput).find(".popover-file").data("size", getReadableSize(file.size,0));
		$(fileEntryOutput).data("path", file.absPath);
		$(fileEntryOutput).data("deriID", file.deriID);
		$(fileEntryOutput).data("docID", file.deriID);
		if (mainDoc == file.absPath){
			$(fileEntryOutput).data("startfile", true);
		}
		$(fileEntryOutput).appendTo("#browser-table-files");
	}
	
	function addFolderToView(folder, path) {
		var folderEntryTemplate = $("#folder-entry-template").html();
		var folderEntryOutput = $(Mustache.render(folderEntryTemplate, folder));
		$(folderEntryOutput).data("path", folder.absPath);
		$(folderEntryOutput).data("deriID", folder.deriID);
		$(folderEntryOutput).appendTo("#browser-table-files");
		if (!folder.temp){
			$(derivateBrowserNavigation).trigger("DerivatFolder", [folder.name, folder.absPath, folder.deriID, path]);			
		}
		else{
			$(folderEntryOutput).find("input.input-new").data("temp", folder.temp);
		}
	}
	
	function showPanel(data, child) {
		var deriName = "";
		if (child){
			if ($("#derivat-panel-name").html() != data.parentName){
				$("#derivat-panel-name").html(data.parentName);
				$("#derivat-panel-size").html(getReadableSize(data.parentSize, 0));
				$("#derivat-panel-last").html(data.parentLastMod);
				deriName = data.parentName;
			}
			else{
				return;
			}
		}
		else{
			$("#derivat-panel-name").html(data.name);
			$("#derivat-panel-size").html(getReadableSize(data.size, 0));
			$("#derivat-panel-last").html(data.lastmodified);
			deriName = data.name;
		}
		$("#derivat-panel-startfile-label").html(data.maindocName);
		if (data.hasURN){
			$("#btn-urnAll").addClass("hidden");
		}
		else{
			$("#btn-urnAll").removeClass("hidden");
		}
		var path = data.maindocName;
		if (path.indexOf("/") != 0) path = "/" + path;
		derivateBrowserTools.setImgPath($("#panel-img"), deriName, path);
		$("#derivat-panel-startfile").data("startfile", path);
		$("#derivat-panel").removeClass("hidden");
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
	
	function showDoc(docID) {
		$("#derivate-browser").addClass("hidden");
		$("#derivat-panel").addClass("hidden");
		$("#browser-table-files").html("");
		jp.subselect.get(docID, addInfo);
	}
	
	function addInfo(info) {
		$("#journal-info-text").html(info);
		$("#journal-info").removeClass("hidden");
	}
	
	function convertTempFolder(input, temp){
		var parent = $("#temp-folder-" + temp).parents(".browser-table-folder");
		parent.data("path", currentPath + "/" + input);
		parent.find(".browser-table-file-name").html(input);
		$(derivateBrowserNavigation).trigger("DerivatFolder", [input, currentPath + "/" + input, currentDocID, currentPath]);
	}
	
	function findFile(path) {
		return $(".browser-table-entry").filter(function() {
					return ($(this).data("path") == path);
				});
	}
    
    //ajax Methods   
	function getDerivate(deriID, path){
		$.ajax({
			url: "/rsc/derivatebrowser/" + deriID + path,
			type: "GET",
			dataType: "json",
			success: function(data) {
						showDerivatOrFolder(deriID, data);
					},
			error: function(error) {
						alert(error);
					}
		});
	}
    
    return {
        //public
        init: function() {

        },

        showDerivateOrDoc: function(docID, path){
        	currentDocID = docID;
            currentPath = path;
            if (docID.search("derivate") != -1){
            	getDerivate(docID, path);            	
            }
            else{
            	showDoc(docID);
            }
        },
        
        createTempFolder: function() {
			var folder = {
					"temp": Math.floor((Math.random() * 1000) + 1),
					"deriID": currentDocID,
					"absPath": currentPath + "/temp"
				};
			addFolderToView(folder, currentPath);
		},
        
        tempToFolder: function(input, temp) {
        	convertTempFolder(input, temp);
		},
		
		removeFileWithPath: function(path) {
			var node = findFile(path);
			this.removeFile(node);
		},
		
		removeFile: function(node) {
			node.remove();
		},
		
		addFile: function(json) {
			addFileToView(json);
		}
		
    };
})();