
var derivateBrowserFileView = (function () {

    //private Methods
    $("body").on("changed", "#main", function(){
        console.log("main changed");
        $("#main").find("form").attr("id", "doc-editor-form");
        $("#main").find("input[name='_xed_submit_servlet:UpdateObjectServlet']").remove();
        $("#main").find("input[name='_xed_submit_servlet:CreateObjectServlet']").remove();
        $("#main").find("input[name='_xed_submit_cancel']").remove();
    });

    /**
     * @property maindocName
     */
	function showDerivatOrFolder(deriID, data, filename){
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
        derivateBrowserLargeView.resetList();
		$.each(data.children, function(i, file) {
			file.deriID = deriID;
			if (file.type == "file"){
                if (file.contentType == "xml"){
                    addXMLToView(file);
                }
                else{
                    addFileToView(file, data.maindocName);
                }
			}
			else{
				addFolderToView(file, data.absPath);
			}
		});
		$("#derivate-browser").removeClass("hidden");
        $("#browser-table-sort-click").find(".glyphicon").addClass("hidden");
        //$("#browser-table-sort").stupidsort('asc');
		createBreadcrumb(deriID, data.absPath);
        if (($("#file-view-large").hasClass("hidden") && filename != "" && filename != undefined) || !$("#file-view-large").hasClass("hidden")){
            $("#file-view").addClass("hidden");
            if (data.absPath != "/") filename = "/" + filename;
            derivateBrowserLargeView.loadViewer(deriID + data.absPath + filename);
        }
	}

	function addFileToView(file , mainDoc) {
		var fileEntryTemplate = $("#file-entry-template").html();
		var fileEntryOutput = $(Mustache.render(fileEntryTemplate, file));
		$(fileEntryOutput).find(".popover-file").data("lastMod", file.lastmodified);
		$(fileEntryOutput).find(".popover-file").data("size", derivateBrowserTools.getReadableSize(file.size,0));
		$(fileEntryOutput).data("path", file.absPath);
		$(fileEntryOutput).data("deriID", file.deriID);
		$(fileEntryOutput).data("docID", file.deriID);
        derivateBrowserLargeView.addFileToList(new LargeViewEntry(file.deriID, file.absPath, file.size, file.lastmodified, file.urn));
		if ((mainDoc == file.absPath) || ("/" + mainDoc == file.absPath)){
			$(fileEntryOutput).data("startfile", true);
		}
		$(fileEntryOutput).appendTo("#browser-table-files");
	}

    function addXMLToView(file) {
        var xmlEntryTemplate = $("#xml-entry-template").html();
        var xmlEntryOutput = $(Mustache.render(xmlEntryTemplate, file));
        $(xmlEntryOutput).data("path", file.absPath);
        $(xmlEntryOutput).data("deriID", file.deriID);
        $(xmlEntryOutput).data("docID", file.deriID);
        $(xmlEntryOutput).appendTo("#browser-table-files");
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

    /**
     * @property parentName
     * @property parentSize
     * @property parentLastMod
     * @property hasURN
     */
	function showPanel(data, child) {
		var deriName = "";
		if (child){
			var panelName = $("#derivat-panel-name");
			if ($(panelName).html() != data.parentName){
				$(panelName).html(data.parentName);
				$("#derivat-panel-size").html(derivateBrowserTools.getReadableSize(data.parentSize, 0));
				$("#derivat-panel-last").html(data.parentLastMod);
				deriName = data.parentName;
			}
			else{
				return;
			}
		}
		else{
			$("#derivat-panel-name").html(data.name);
			$("#derivat-panel-size").html(derivateBrowserTools.getReadableSize(data.size, 0));
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
            $("#journal-info-text").removeClass("journal-info-text-large");
			$("#derivate-browser").addClass("hidden");
			$("#derivat-panel").addClass("hidden");
			$("#browser-table-files").html("");
			$("#journal-info-button-whileEdit").addClass("hidden");
			$("#journal-info-button-notEdit").removeClass("hidden");
			getDocument(docID, addInfo);
            getDocLinks(docID);
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
        $("#editor-loading").addClass("hidden");
        $("#journal-info-linklist").addClass("hidden");
		if ($("#journal-info").hasClass("hidden")){
			$("#journal-info").removeClass("hidden");
			$("#derivate-browser").addClass("hidden");
		}
        $("#journal-info-text").addClass("journal-info-text-large");
        $("#journal-info").addClass("loaded");
        $("#journal-info-text").html(html);
	}

    function hideCurrentView(){
        $("#journal-info").addClass("hidden");
        $("#derivate-browser").addClass("hidden");
        $("#editor-loading").removeClass("hidden");
        $("#journal-info-button-whileEdit").addClass("hidden");
        $("#journal-info-button-notEdit").addClass("hidden");
    }

    function addLinksToView(links){
        if (links != undefined){
            $("#journal-info-linklist").html("");
            $.each(links, function(i, link) {
                var img = $("<div class='link-preview'><img class='link-preview-img' src='http://localhost:8291/jportal/servlets/MCRTileCombineServlet/MIN/" + link + "'></div>");
                $(img).append("<div class='link-info'><h6 class='mightOverflow'>" + link + "</h6><span class='btn-remove-link glyphicon glyphicon-remove'></span></div>");
                $(img).data("path", link);
                $("#journal-info-linklist").append(img);
            });
            $("#journal-info-linklist").removeClass("hidden");
            $("#journal-info-text").removeClass("journal-info-text-large");
        }
        else{
            $("#journal-info-linklist").addClass("hidden");
            $("#journal-info-text").addClass("journal-info-text-large");
        }
    }

    function removeLinkFromView(path) {
        $(".link-preview").filter(function() {
            return ($(this).data("path") == path);
        }).remove();
    }

    //ajax Methods
	function getDerivate(deriID, path, filename){
		$.ajax({
			url: "./" + deriID + path,
			type: "GET",
			dataType: "json",
			success: function(data) {
					showDerivatOrFolder(deriID, data, filename);
					},
			error: function(error) {
                    console.log(error);
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.loadFailed", deriID), false);
					}
		});
	}

	function getDocument(docID, callback) {
		$.ajax({
			url: jp.baseURL + "rsc/render/object/" + docID,
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
			url: jp.baseURL + "servlets/XEditor",
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
        hideCurrentView();
		var url = jp.baseURL + "editor/start.xed?&type=" + type + "&action=" + mode;
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
			url: "./" + docID,
			type: "DELETE",

			success: function() {
					callback();
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.deleted"), true);
				},
			error: function(error) {
                    console.log(error);
                    if (error.status == 401) {
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                    }
                    else{
                            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.delete.error"), false);
                    }
				}
		});
	}

    /**
     * @property numFound
     * @property derivateLink
     */
    function getDocLinks(docID) {
        var url = jp.baseURL + "servlets/solr/select?q=id%3A" + docID + "&start=0&rows=10&sort=maintitle+asc&wt=json&indent=true";
        $.getJSON(url, function(search) {
            if (search.response.numFound > 0){
                addLinksToView(search.response.docs[0].derivateLink);
            }
        });
    }

    return {
        //public
        init: function() {

        },

        showDerivateOrDoc: function(docID, path, filename){
            if (docID.search("derivate") != -1){
            	getDerivate(docID, path, filename);
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

        addXML: function(json) {
            addXMLToView(json);
        },

		updateDoc: function(mode) {
			updateDocument(mode);
		},

		cancelEditDoc: function() {
			showDoc(derivateBrowserTools.getCurrentDocID(), "");
		},

		editDoc: function() {
			var docID = derivateBrowserTools.getCurrentDocID();
			getDocEditor(docID.substring(docID.indexOf("_") + 1, docID.lastIndexOf("_")), "update", derivateBrowserTools.getCurrentDocID());
		},

		deleteDoc: function() {
			deleteDocument(derivateBrowserTools.getCurrentDocID(), removeFromView);
		},

		newDoc: function(type) {
			getDocEditor(type, "create", derivateBrowserTools.getCurrentDocID());
		},

        removeLink: function(path) {
            removeLinkFromView(path);
            if ($("#journal-info-linklist").children().length == 0){
                $("#journal-info-linklist").addClass("hidden");
                $("#journal-info-text").addClass("journal-info-text-large");
            }
        }
    };
})();