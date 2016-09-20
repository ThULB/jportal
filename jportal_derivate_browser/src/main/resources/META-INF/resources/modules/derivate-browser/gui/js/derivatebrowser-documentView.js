
var derivateBrowserDocumentView = (function () {

    var docDeleteList = [];

    //private Methods
    $("body").on("changed", "#main", function(){
        $("#main").find("form").attr("id", "doc-editor-form");
        $("#main").find("input[name='_xed_submit_servlet:UpdateObjectServlet']").remove();
        $("#main").find("input[name='_xed_submit_servlet:CreateObjectServlet']").remove();
        $("#main").find("input[name='_xed_submit_cancel']").remove();
    });

    function bindActions() {
        bindUIActions();
        bindEventActions();
    }
    
    function bindUIActions() {
        $("body").on("click", "#folder-list-new-choose", function () {
            $("#folder-list-new-select-area").toggleClass("hidden");
        });

        $("body").on("click", "#folder-list-new-button-journal", function () {
            getDocEditor("jpjournal", "create", derivateBrowserTools.getCurrentDocID());
            $("#folder-list-new-select-area").addClass("hidden");
        });

        $("body").on("click", "#folder-list-new-button-volume", function () {
            getDocEditor("jpvolume", "create", derivateBrowserTools.getCurrentDocID());
            $("#folder-list-new-select-area").addClass("hidden");
        });

        $("body").on("click", "#folder-list-new-button-article", function () {
            getDocEditor("jparticle", "create", derivateBrowserTools.getCurrentDocID());
            $("#folder-list-new-select-area").addClass("hidden");
        });
        
        $("body").on("click", "#journal-info-button-cancel", function () {
            showDoc(derivateBrowserTools.getCurrentDocID(), "");
        });

        $("body").on("click", "#journal-info-button-save", function () {
            updateDocument($(this).data("mode"));
        });

        $("body").on("click", "#journal-info-button-edit", function () {
            var docID = derivateBrowserTools.getCurrentDocID();
            getDocEditor(docID.substring(docID.indexOf("_") + 1, docID.lastIndexOf("_")), "update", derivateBrowserTools.getCurrentDocID());
        });

        $("body").on("click", "#lightbox-alert-delete-docs-confirm", function () {
            var json = [];
            $.each(docDeleteList, function (i, elm) {
                json.push({"objId": elm});
            });
            derivateBrowserTools.showLoadingScreen();
            deleteDocument(json, removeFromView);
            $("#lightbox-alert-delete-docs").modal('hide');
        });

        $("body").on("click", ".lightbox-alert-delete-docs-cancel", function () {
            $("#lightbox-alert-delete-docs-list").html("");
            $("#lightbox-alert-delete-docs").modal('hide');
            docDeleteList = [];
        });

        $("body").on("click", ".link-preview", function () {
            derivateBrowserTools.goToPath($(this).data("path"));
        });

        $("body").on("click", ".btn-remove-link", function (event) {
            event.stopPropagation();
            removeLink(derivateBrowserTools.getCurrentDocID(), $(this).closest(".link-preview").data("path"));
        });

        $("body").on("click", "#journal-info-button-goToPage", function () {
            window.location.href = jp.baseURL + "receive/" + derivateBrowserTools.getCurrentDocID();
        });

        $("body").on("click", "#journal-info-button-delete", function () {
            if ($(".aktiv").length == 1) {
                $("body").trigger("gotDerivateChilds", [derivateBrowserTools.getCurrentDocID(), checkForChilds]);
            }
            if ($(".aktiv").length > 1) {
                docDeleteList = [];
                var selectedDocs =[];
                $(".aktiv").each(function (i, elm) {
                    selectedDocs.push($(elm).data("docID"));
                });
                deleteMultipleDocs(selectedDocs);
            }
        });
    }

    function bindEventActions() {
        $("body").on("showSelectedDocs", function () {
            showDocs();
        });

        $("body").on("showDoc", function (event, docID) {
            showDoc(docID);
        });
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
                var img = $("<div class='link-preview'><img class='link-preview-img' src=''><img class='img-placeholder-link-preview img-placeholder' src=''></div>");
                $(img).append("<div class='link-info'><h6 class='mightOverflow'>" + link + "</h6><span class='btn-remove-link glyphicon glyphicon-remove'></span></div>");
                $(img).data("path", link);
                var deriID = link.substring(0,link.indexOf("/"));
                var path = link.substring(link.indexOf("/"));
                derivateBrowserTools.setImgPath($(img).find("img.link-preview-img"), deriID, path);
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
        if ($("#journal-info-linklist").children().length == 0){
            $("#journal-info-linklist").addClass("hidden");
            $("#journal-info-text").addClass("journal-info-text-large");
        }
    }

    function showDocs() {
        if ($(".aktiv").length > 1) {
            $("#journal-info-button-delete-labelAll").removeClass("hidden");
            $("#journal-info-button-delete-label").addClass("hidden");
            $("#journal-info-text").html("");
            $("#journal-info-text").addClass("journal-info-text-large");
            $("#journal-info-button-goToPage").addClass("hidden");
            $("#journal-info-button-edit").addClass("hidden");
            $("#journal-info-linklist").addClass("hidden");
            $("#journal-info-text").append("<strong>" + derivateBrowserTools.getI18n("db.label.selected.label") + "</strong>");
            var ul = $("<ul></ul>");
            $(".aktiv").each(function (i, elm) {
                var newElm = $(elm).clone();
                $(newElm).children().first().remove();
                ul.append("<li>" + $(newElm).html() + "</li>");
            });
            $("#journal-info-text").append(ul);

        }
        else {
            derivateBrowserTools.goTo(derivateBrowserTools.getCurrentDocID(), "");
        }
    }

    /**
     * @property docs
     * @property derivateCount
     */
    function checkForChilds(data, docs, docID) {
        if (data.docs[0].derivateCount > 0) {
            var name = data.docs[0].maintitle;
            if (name == undefined) name = docID;
            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.delete.digi", name), false);
            docDeleteList = [];
        }
        else {
            if (docs != undefined){
                docDeleteList.push(data.docs[0].maintitle);
                if (docs.length > 0) {
                    deleteMultipleDocs(docs);
                }
                else {
                    derivateBrowserTools.showDeleteAlertDocs(docDeleteList)
                }
            }
            else {
                derivateBrowserTools.showDeleteDocAlert("journal");
            }
        }
    }

    function deleteMultipleDocs(docs) {
        $("body").trigger("gotDerivateChilds", [docs.pop(), checkForChilds, docs]);
    }
    
    function removeFromView(json){
        $.each(json, function (i, elm) {
            if (elm.status == "1") {
                $("body").trigger("removeDocPerID", [elm.objId, ""])
            }
            else {
                derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.delete.notAllDocs"), false);
            }
        });
        derivateBrowserTools.hideLoadingScreen();
        derivateBrowserTools.goToParent(derivateBrowserTools.getCurrentDocID());
    }

    //ajax Methods
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
                        $("body").trigger("renameDoc", [derivateBrowserTools.getCurrentDocID(), "", title]);
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.edited"), true);
                    }
                    else{
                        var name = $("#doc-editor-form textarea[name='/mycoreobject/metadata/maintitles/maintitle']").val();
                        $("#journal-info-text").html("");
                        $("body").trigger("addTempDoc", [docID, name, type, derivateBrowserTools.getCurrentDocID()]);
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

    function deleteDocument(json, callback) {
        $.ajax({
            url: "docs",
            type: "DELETE",
            data: JSON.stringify(json),
            dataType: "json",
            success: function(data) {
                callback(data);
                derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.deleted"), true);
            },
            error: function(error) {
                derivateBrowserTools.hideLoadingScreen();
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

    function removeLink(docID, imgPath) {
        $.ajax({
            url: "link?docID=" + docID + "&imgPath=" + imgPath,
            type: "DELETE",
            statusCode: {
                200: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.link.remove.success"), true);
                    removeLinkFromView(imgPath);
                },
                500: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.link.remove.error"), false);
                },
                401: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                }
            }
        });
    }


    return {
        //public
        init: function() {
            bindActions();
        }
    };
})();