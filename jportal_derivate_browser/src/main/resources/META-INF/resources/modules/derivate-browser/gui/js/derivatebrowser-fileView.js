
var derivateBrowserFileView = (function () {

    //private Methods
    $("body").on("changed", "#main", function(){
        console.log("main changed");
        $("#main").find("form").attr("id", "doc-editor-form");
        $("#main").find("input[name='_xed_submit_servlet:UpdateObjectServlet']").remove();
        $("#main").find("input[name='_xed_submit_servlet:CreateObjectServlet']").remove();
        $("#main").find("input[name='_xed_submit_cancel']").remove();
    });

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
		$.each(data.children, function(i, file) {
			file.deriID = deriID;
			if (file.type == "file"){
				addFileToView(file, data.maindocName);
			}
			else{
				addFolderToView(file, data.absPath);
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
			var panelName = $("#derivat-panel-name");
			if ($(panelName).html() != data.parentName){
				$(panelName).html(data.parentName);
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
		var breadcrumb = $("#derivate-browser-breadcrumb");
		$(breadcrumb).html("");
		var firstli = $('<li class="derivate-browser-breadcrumb-entry">' + deriID + '</li>');
		firstli.data("deriID", deriID);
		firstli.data("path", "");
		$(breadcrumb).append(firstli);
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
		if (!docID.contains("derivate")){
            $("#journal-info-text").html("");
			$("#derivate-browser").addClass("hidden");
			$("#derivat-panel").addClass("hidden");
			$("#browser-table-files").html("");
			$("#journal-info-button-whileEdit").addClass("hidden");
			$("#journal-info-button-notEdit").removeClass("hidden");
			getDocument(docID, addInfo);
		}
		else{
			$("#journal-info").addClass("hidden");
			$("#derivate-browser").removeClass("hidden");
		}
	}

	function addInfo(info) {
		$("#journal-info-text").html(info);
		$("#journal-info").removeClass("hidden");
	}

	function convertTempFolder(input, temp){
		var parent = $("#temp-folder-" + temp).parents(".browser-table-folder");
		parent.data("path", derivateBrowserTools.getCurrentPath() + "/" + input);
		parent.find(".browser-table-file-name").html(input);
		$(derivateBrowserNavigation).trigger("DerivatFolder", [input, derivateBrowserTools.getCurrentPath() + "/" + input, derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath()]);
	}

	function findFile(path) {
		return $(".browser-table-entry").filter(function() {
					return ($(this).data("path") == path);
				});
	}

	function removeFromView(){
		var parentID = derivateBrowserNavigation.getParentDocID(derivateBrowserTools.getCurrentDocID());
		derivateBrowserNavigation.removeDocPerID(derivateBrowserTools.getCurrentDocID(), "");
		derivateBrowserTools.goTo(parentID, "");
	}

	function showEditor(data, mode){
		var html = $("<div></div>").append(data).find("#main");
		$(html).find("form").attr("id", "doc-editor-form");
		$(html).find("input[name='_xed_submit_servlet:UpdateObjectServlet']").remove();
		$(html).find("input[name='_xed_submit_servlet:CreateObjectServlet']").remove();
		$(html).find("input[name='_xed_submit_cancel']").remove();
        if ($("#journal-info").hasClass("loaded")) {
            $(html).find("script:not('.jp-db-reload')").remove();
        }
        else{
            $(html).find("script:not('.jp-db-load')").remove();
        }
        $("#journal-info-button-save").data("mode", mode);
        $("#journal-info-button-whileEdit").removeClass("hidden");
        $("#journal-info-button-notEdit").addClass("hidden");
		if ($("#journal-info").hasClass("hidden")){
			$("#journal-info").removeClass("hidden");
			$("#derivate-browser").addClass("hidden");
		}
        $("#journal-info").addClass("loaded");
        $("#journal-info-text").html(html);
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
                    console.log(error);
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.loadFailed", deriID), false);
					}
		});
	}

	function getDocument(docID, callback) {
		$.ajax({
			url: "/rsc/render/object/" + docID,
			type: "GET",
			dataType: "html",
			success: function(data) {
					callback(data);
				},
			error: function(error) {
                    console.log(error);
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.loadFailed", docID), false);
				}
		});
	}

	function updateDocument(mode) {
		var target = "&_xed_submit_servlet:UpdateObjectServlet=save";
		if (mode == "create") {
			target = "&_xed_submit_servlet:CreateObjectServlet=create";
		}
		$.ajax({
			url: "/servlets/XEditor",
			type: "GET",
			data: $('#doc-editor-form').serialize() + target,
			success: function(data, textStatus, request) {
                    if (data.contains("help-inline")){
                        showEditor(data, mode);
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.editorNotComplete"));
                    }
                    else{
                        var docID = request.getResponseHeader("Content-Disposition");
                        docID = docID.substr(docID.indexOf("filename") + 10);
                        docID = docID.substring(0, docID.lastIndexOf("."));
                        var type = docID.match("_(.*?)_")[1];
                        if (mode != "create"){
                            showDoc(derivateBrowserTools.getCurrentDocID(), "");
                            var title = ($("<div></div>").append(data).find("#jp-maintitle").html());
                            title = title.substring(0, title.indexOf("<"));
                            derivateBrowserNavigation.renameDoc(derivateBrowserTools.getCurrentDocID(), "", title);
                            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.edited"), true);
                        }
                        else{
                            var name = $("#doc-editor-form textarea[name='/mycoreobject/metadata/maintitles/maintitle']").val();
                            $("#journal-info-text").html("");
                            derivateBrowserNavigation.addTempDoc(docID, name, type, derivateBrowserTools.getCurrentDocID());
                            derivateBrowserTools.goTo(docID, "");
                            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.created"), true);
                        }
                    }
				},
			error: function(error) {
                    console.log(error);
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.error"), false);
				}
		});
	}

	function getDocEditor(type, mode, docID) {
		var url = "/editor/start.xed?&type=" + type + "&action=" + mode;
		if(mode != "create" && (docID != "" || docID != undefined)){
			url = url + "&id=" + docID;
		}
        if(mode == "create" && (docID != "" || docID != undefined) && type != "jpjournal"){
            url = url + "&parent=" + docID;
        }
		$.ajax({
			url: url,
			type: "GET",
			dataType: "html",
			statusCode: {
				200: function(data) {
					showEditor(data, mode);
				},
				500: function(error) {
					alert(error);
				}
			}
		});
	}

	function deleteDocument(docID, callback) {
		$.ajax({
			url: "/rsc/derivatebrowser/" + docID,
			type: "DELETE",

			success: function() {
					callback();
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.deleted"), true);
				},
			error: function(error) {
                    console.log(error);
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.delete.error"), false);
				}
		});
	}

    return {
        //public
        init: function() {

        },

        showDerivateOrDoc: function(docID, path){
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
					"deriID": derivateBrowserTools.getCurrentDocID(),
					"absPath": derivateBrowserTools.getCurrentPath() + "/temp"
				};
			addFolderToView(folder, derivateBrowserTools.getCurrentPath());
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
		},

		updateDoc: function(mode) {
			updateDocument(mode);
		},

		cancelEditDoc: function() {
			showDoc(derivateBrowserTools.getCurrentDocID(), "");
		},

		editDoc: function() {
			docID = derivateBrowserTools.getCurrentDocID();
			getDocEditor(docID.substring(docID.indexOf("_") + 1, docID.lastIndexOf("_")), "update", derivateBrowserTools.getCurrentDocID());
		},

		deleteDoc: function() {
			deleteDocument(derivateBrowserTools.getCurrentDocID(), removeFromView);
		},

		newDoc: function(type) {
			getDocEditor(type, "create", derivateBrowserTools.getCurrentDocID());
		}
    };
})();