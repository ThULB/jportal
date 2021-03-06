
var derivateBrowserDerivateView = (function () {

    var renameRunning = false;

    function bindActions() {
        bindUIActions();
        bindEventActions();
    }

    function bindUIActions() {
        $("body").on("keydown", "td.browser-table-file-name > input", function (event) {
            if (!$(this).hasClass("input-new")) {
                if (event.which == 13) {
                    if ($(this).val() != $(this).parent().data("oldName")) {
                        renameFile($(this).parents(".browser-table-entry").data("path"),
                            $(this).parents(".browser-table-entry").data("deriID"),
                            $(this).val(),
                            $(this).parents(".browser-table-entry").data("startfile"));
                    }
                    else {
                        $(this).parent().html($(this).parent().data("oldName"));
                    }
                }
                if (event.which == 27 && !$(this).parent().hasClass("spinnerInInput")) {
                    $(this).parent().removeClass("has-error");
                    $(this).closest(".browser-table-entry").find(".btn-edit").removeData("edit");
                    $(this).parent().html($(this).parent().data("oldName"));
                }
            }
        });

        $("body").on("click", ".btn-add", function () {
            createTempFolder();
            $("#browser-table-wrapper").animate({
                scrollTop: $("#browser-table").height()
            }, 100);
            $(".input-new").focus();
        });

        $("body").on("click", "#lightbox-alert-delete-confirm", function () {
            let derivateFiles = new DerivateFiles();

            derivateBrowserTools.showLoadingScreen();
            $(".delete").each(function () {
                let derivId = $(this).data("deriID");
                let path = $(this).data("path");
                let file = new File(derivId, path);
                derivateFiles.add(file);
            });
            deleteMultipleFiles(derivateFiles);
            $("#lightbox-alert-delete").modal('hide');
        });

        $("body").on("click", ".btn-check", function (event) {
            var parent = $(this).parents(".browser-table-entry")[0];
            var last = $(".last-selected")[0];
            if (event.shiftKey && last != undefined) {
                var inZone = false;
                $("#browser-table-files").children().each(function(i, elm) {
                    if (elm == last || elm == parent){
                        if (inZone) {
                            inZone = false;
                            checkEntry(true, elm);
                        }
                        else {
                            inZone = true;
                        }
                    }
                    if (inZone) {
                        checkEntry(true, elm);
                    }
                });
            }
            else {
                if ($(parent).data("checked") != true) {
                    checkEntry(true, parent);
                    $(".last-selected").removeClass("last-selected");
                    $(parent).addClass("last-selected");
                }
                else {
                    checkEntry(false, parent);
                }
            }
            checkIfNothingSelected();
        });

        $("body").on("click", ".btn-check-all", function () {
            if ($(this).data("checked") != true) {
                $(this).removeClass("glyphicon-unchecked");
                $(this).addClass("glyphicon-check");
                $(this).data("checked", true);
                $(".browser-table-entry:visible").each(function (i, node) {
                    checkEntry(true, node);
                });
                checkIfNothingSelected();
            }
            else {
                $(this).addClass("glyphicon-unchecked");
                $(this).removeClass("glyphicon-check");
                $(this).removeData("checked");
                $(".browser-table-entry.checked").each(function (i, node) {
                    checkEntry(false, node);
                });
                checkIfNothingSelected();
            }
        });

        $("body").on("keydown", ".input-new", function (event) {
            if (event.which == 13) {
                newFolder($(this).val(), $(this).data("temp"));
            }
            if (event.which == 27) {
                $(this).parents(".browser-table-entry").remove();
                $("#derivat-panel-folder").html(parseInt($("#derivat-panel-folder").html()) - 1);
            }
        });

        $("body").on("mouseenter", ".browser-table-entry", function () {
            $(this).find("span.btn").removeClass("invisible");
            $(this).find("div.no-urn").addClass("hidden");
        });

        $("body").on("mouseleave", ".browser-table-entry", function () {
            var file = $(this);
            $(this).find("span.btn").addClass("invisible");
            $(this).find("div.no-urn").removeClass("hidden");
            $(this).find("span.btn-check").filter(function () {
                return file.data("checked") == true;
            }).removeClass("invisible");
        });

        $("body").on("click", "#collapse-btn", function () {
            if ($(this).hasClass("glyphicon-chevron-up")) {
                $("#browser-table-wrapper").addClass("small-table");
                $(this).removeClass("glyphicon-chevron-up");
                $(this).addClass("glyphicon-chevron-down");
                //$("#derivat-panel-buttons").addClass("hidden");
            }
            else {
                $("#browser-table-wrapper").removeClass("small-table");
                $(this).removeClass("glyphicon-chevron-down");
                $(this).addClass("glyphicon-chevron-up");
            }
        });

        $("body").on("mouseenter", "#panel-img", function () {
            $("#derivat-panel-startfile-overlay-div").removeClass("hidden");
        });

        $("body").on("mouseleave", "#derivat-panel-startfile-overlay-div", function () {
            $("#derivat-panel-startfile-overlay-div").addClass("hidden");
        });

        $("body").on("click", ".lightbox-alert-delete-cancel", function () {
            $("#lightbox-alert-delete-list").html("");
            $(".delete").removeClass("delete");
            $("#lightbox-alert-delete").modal('hide');
        });

        $("body").on("click", "#btn-tileDeri", function () {
            $(this).attr("disabled", true);
            $(this).find("span").addClass("glyphicon-refresh button-spinner-solo");
            $(this).find("span").removeClass("glyphicon-th");
            tileDerivate(derivateBrowserTools.getCurrentDocID())
        });

        $("body").on("click", "#btn-hide", function () {
            var entry = $(this);
            $.get(jp.baseURL + "servlets/MCRDisplayHideDerivateServlet?derivate=" + derivateBrowserTools.getCurrentDocID())
                .done(function() {
                    if (entry.hasClass("derivate-ishidden")) {
                        entry.removeClass("derivate-ishidden");
                        entry.attr("title", "Derivate verstecken");
                        $("#derivate-hidden").addClass("hidden");
                    }
                    else {
                        entry.addClass("derivate-ishidden");
                        entry.attr("title", "Derivate anzeigen");
                        $("#derivate-hidden").removeClass("hidden");
                    }
                });
        });

        $("body").on("click", "#btn-filter-table-input-remove", function () {
            filterTable("");
            $("#btn-filter-table-input-remove").addClass("hidden");
            $("#btn-filter-table-input > input").val("");
            $("#btn-filter-table-input").animate({
                "width": "-10",
                "margin-left": "0"
            }, 1000, function() {
                $("#btn-filter-table-input").addClass("hidden");
                $("#btn-filter-table-input").removeClass("filter-table-input-large");
            });

        });

        $("body").on("input", "#btn-filter-table-input > input", function () {
            filterTable($(this).val());
        });

        $("body").on("click", "#btn-filter-table", function () {
            $("#btn-filter-table-input").toggleClass("filter-table-input-large");
            if ($("#btn-filter-table-input").hasClass("filter-table-input-large")){
                $("#btn-filter-table-input").removeClass("hidden");
                $("#btn-filter-table-input").animate({
                    "width": "200px",
                    "margin-left": "5px"
                }, 1000);
            }
            else {
                $("#btn-filter-table-input").animate({
                    "width": "-10",
                    "margin-left": "0"
                }, 1000, function() {
                    $("#btn-filter-table-input").addClass("hidden");
                });
            }
        });

        $("body").on("click", "#browser-table-sort-click", function () {
            $("#browser-table-sort").click();
        });

        $("body").on("click", "#browser-table-sort-click-lastMod", function () {
            $("#browser-table-sort-last").click();
        });

        $("body").on("click", ".popover-img", function () {
            $("body").trigger("loadViewer", $(this).data("deriID") + $(this).data("path"));
            $("#file-view").addClass("hidden");
        });

        $("body").on("click", "#btn-download-zip", function () {
            window.location.href = jp.baseURL + "servlets/MCRZipServlet/" + derivateBrowserTools.getCurrentDocID();
            $("#download-new-select-area").addClass("hidden");
        });

        $("body").on("click", "#btn-download-tar", function () {
            window.location.href = jp.baseURL + "servlets/MCRTarServlet/" + derivateBrowserTools.getCurrentDocID();
            $("#download-new-select-area").addClass("hidden");
        });

        $("body").on("click", "#btn-download", function () {
            $("#download-new-select-area").toggleClass("hidden");
        });

        $("body").on("click", "#btn-large-view", function () {
            $("#file-view").addClass("hidden");
            $("body").trigger("loadViewer");
        });

        $("body").on("click", "#btn-viewer", function () {
            window.location.href = jp.baseURL + "rsc/viewer/" + derivateBrowserTools.getCurrentDocID() + $("#derivat-panel-startfile").data("startfile");
        });
        
        $("body").on("click", "#btn-deleteDeri", function () {
            derivateBrowserTools.showDeleteDocAlert("derivate");
        });

        $("body").on("mouseenter", ".popover-file", function () {
            if (!$(this).hasClass("popoverAdded")) {
                var parent = $(this).closest(".browser-table-entry");
                var file = [];
                file.lastmodified = $(this).data("lastMod");
                file.size = $(this).data("size");
                var popoverTemplate = $("#popover-template").html();
                var popOverOutput = $(Mustache.render(popoverTemplate, file));
                derivateBrowserTools.updateI18nForElm($(popOverOutput));
                //$(this).popover({content: popOverOutput, html: true});
                derivateBrowserTools.setImgPath($(popOverOutput).find(".popover-img"), $(parent).data("deriID"), $(parent).data("path"));
                derivateBrowserTools.setupPopover($(this), popOverOutput);
                $(this).addClass("popoverAdded");
                $(this).popover("show");
            }
        });

        $("body").on("mouseleave", ".popover", function() {
            $(".popShow").popover("hide");
        });
        
        $("body").on("click", "span.btn-new-urn", function () {
            var entry = $(this).parents(".browser-table-entry");

            let derivId = entry.data("deriID");
            let path = entry.data("path");
            let file = new File(derivId, path);
            addURN(file);
        });

        $("body").on("click", "#btn-urnAll", function () {
            let derivId = derivateBrowserTools.getCurrentDocID();
            addURNAll(derivId);
            //$("#derivat-panel-buttons").addClass("hidden");
        });

        $("body").on("click", ".derivate-browser-breadcrumb-entry", function () {
            derivateBrowserTools.goTo($(this).data("deriID"), $(this).data("path"));
        });

        $("body").on("click", ".btn-folder", function () {
            derivateBrowserTools.goTo($(this).parents(".browser-table-folder").data("deriID"), $(this).parents(".browser-table-folder").data("path"));
        });

        $("body").on("click", ".btn-edit", function () {
            var name = $(this).parents(".browser-table-entry").find(".browser-table-file-name");
            if (!$(this).data("edit")) {
                $(this).data("edit", true);
                name.data("oldName", name.html());
                name.html("<input type='text' value='" + name.data("oldName") + "' class='form-control input-sm'/>");
            }
            else {
                $(this).removeData("edit");
                name.data("oldName", name.html());
                name.html($(name).find("input").val());
            }
        });

        $("body").on("click", ".btn-delete", function () {
            var entry = $(this).parents(".browser-table-entry");
            var startfile = $("#derivat-panel-startfile").data("startfile");
            if ($(entry).data("startfile") != true && startfile != $(entry).data("path")) {
                entry.addClass("delete");
                var fileList = [];
                fileList.push($(entry).data("path"));
                derivateBrowserTools.showDeleteAlert(fileList);
            }
            else {
                derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.delete.startfile"), false);
            }
        });

        $("body").on("click", ".btn-delete-all", function () {
            var canDelete = true;
            var startfile = $("#derivat-panel-startfile").data("startfile");
            var fileList = [];
            if ($(".glyphicon-check").length > 0) {
                var entrys = $(".browser-table-entry").filter(function () {
                    return $(this).data("checked") == true;
                });
                entrys.each(function (i, node) {
                    if ($(this).data("startfile") != true && startfile.indexOf($(this).data("path")) != 0) {
                        $(node).addClass("delete");
                        fileList.push($(node).data("path"));
                    }
                    else {
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.delete.startfile"), false);
                        canDelete = false;

                    }
                });
                if (canDelete) {
                    derivateBrowserTools.showDeleteAlert(fileList);
                }
                else {
                    $(".delete").removeClass("delete");
                }
            }
        });

        $("body").on("click", ".btn-move-all", function () {
            if ($(".glyphicon-check").length > 0) {
                getTargetFolders($("#derivat-panel-name").html());
            }
        });

        $("body").on("click", ".target-folder-entry > .folder-name, .target-folder-entry > span.icon", function () {
            if (!$(this).hasClass("faded")) {
                $(".target-folder-selected").removeClass("target-folder-selected");
                $(this).parent().addClass("target-folder-selected");
            }
        });

        $("body").on("click", "#lightbox-multi-move-confirm", function () {
            if ($(".target-folder-selected").length > 0) {
                let targetDerivId = $("#target-panel-childlist div.folder-name").html();
                let targetPath = $(".target-folder-selected").attr("data-path");
                let moveTo = new File(targetDerivId, targetPath);

                let derivateFiles = new DerivateFiles();
                derivateFiles.setTarget(moveTo)

                if ($(".glyphicon-check").length > 0) {
                    var entrys = $(".browser-table-entry").filter(function () {
                        return $(this).data("checked") == true;
                    });
                    entrys.each(function () {
                        let derivId = $(this).data("deriID");
                        let path = $(this).data("path");
                        let file = new File(derivId, path);

                        let type = "file";
                        if ($(this).hasClass("browser-table-folder")) {
                            type = "folder";
                        }

                        file.setType(type);

                        if (!file.equals(moveTo) && derivateBrowserTools.getCurrentPath() !== moveTo.getPath()) {
                            derivateFiles.add(file);
                        }
                    });
                }
                $(this).attr("disabled", true);
                $(this).append('<span class="glyphicon glyphicon-refresh button-spinner" aria-hidden="true"></span>');
                moveFiles(derivateFiles);
            }
            else {
                derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.move.targetFolder"), false);
            }
        });

        $("body").on("click", "#btn-renameFiles", function () {
            getFileList(derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath(),
                $("#lightbox-multi-rename-filelist"));
        });


        $("body").on("click", "#lightbox-multi-rename-send-button", function () {
            $(this).attr("disabled", true);
            $(this).append('<span class="glyphicon glyphicon-refresh button-spinner" aria-hidden="true"></span>');
            renameMultiple(derivateBrowserTools.getCurrentDocID(), $("#lightbox-multi-rename-pattern").val(),
                $("#lightbox-multi-rename-newname").val());
        });

        $("body").on("keyup", ".lightbox-multi-rename-input", function () {
            if ($(this).val() != "" && $(this).siblings(".lightbox-multi-rename-input").val() != "") {
                renameTest($("#lightbox-multi-rename-filelist").val(), $("#lightbox-multi-rename-pattern").val(),
                    $("#lightbox-multi-rename-newname").val());
            }
        });

        $("body").on("change", "#lightbox-multi-rename-filelist", function () {
            if ($("#lightbox-multi-rename-pattern").val() != "" && $("lightbox-multi-rename-newname").val() != "") {
                renameTest($("#lightbox-multi-rename-filelist").val(), $("#lightbox-multi-rename-pattern").val(),
                    $("#lightbox-multi-rename-newname").val());
            }
        });

        $("body").on("click", ".lightbox-multi-rename-cancel", function () {
            $("#lightbox-multi-rename").modal("hide");
            $("#lightbox-multi-rename-newname").val("");
            $("#lightbox-multi-rename-pattern").val("");
            $("#lightbox-multi-rename-result > ul").html("");
            $("#lightbox-multi-rename-test-input").val("");
            $('#lightbox-multi-rename-send-button').removeAttr("disabled");
            $('#lightbox-multi-rename-send-button').find('.button-spinner').remove();
            getDerivate(derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath());
        });

        $("body").on("click", "#lightbox-multi-rename-copy", function () {
            $("#lightbox-multi-rename-copy-input").val($("#lightbox-multi-rename-filelist").val());
            $("#lightbox-multi-rename-copy-input").removeClass("hidden").focus().select();
            document.execCommand('copy');
            $("#lightbox-multi-rename-copy-input").blur().addClass("hidden");
        });

        $("#browser-table").stupidtable();
        $("#browser-table").on("aftertablesort", function (event, data) {
            $("#browser-table-sort-click").find(".glyphicon").addClass("hidden");
            var dir = $.fn.stupidtable.dir;
            if (data.direction == dir.ASC){
                if (data.column == "2") {
                    $("#browser-table-sort-click-lastMod > span").addClass("hidden");
                    $("body").trigger("sortList", "ASC");
                    $("#browser-table-sort-click").find(".glyphicon-chevron-up").removeClass("hidden");
                }
                else {
                    $("#browser-table-sort-click > span").addClass("hidden");
                    $("#browser-table-sort-click-lastMod > span").addClass("hidden");
                    $("#browser-table-sort-click-lastMod").find(".glyphicon-chevron-up").removeClass("hidden");
                }
            }
            else{
                if (data.column == "2") {
                    $("#browser-table-sort-click-lastMod > span").addClass("hidden");
                    $("body").trigger("sortList", "DESC");
                    $("#browser-table-sort-click").find(".glyphicon-chevron-down").removeClass("hidden");
                }
                else {
                    $("#browser-table-sort-click > span").addClass("hidden");
                    $("#browser-table-sort-click-lastMod > span").addClass("hidden");
                    $("#browser-table-sort-click-lastMod").find(".glyphicon-chevron-down").removeClass("hidden");
                }
            }
        });
        
    }

    function bindEventActions() {
        $("body").on("changeStartFile", function (event, elm) {
            changeStartFile($(elm));
        });

        $("body").on("showDerivate", function (event, deriID, path, filename) {
            getDerivate(deriID, path, filename);
        });

        $("body").on("removeFileWithPath", function (event, path) {
            removeFileWithPath(path);
        });

        $("body").on("addXML", function (event, json) {
            addXMLToView(json);
        });

        $("body").on("addFile", function (event, json) {
            addFileToView(json);
        });

        $("body").on("renameFile", function (event, oldName, deriID, newName, start, callback) {
            renameFile(oldName, deriID, newName, start, callback);
        });
    }

    //private Methods
    /**
     * @property maindocName
     */
    function showDerivatOrFolder(deriID, data, filename) {
        $("#derivat-panel-files").html(0);
        $("#derivat-panel-folder").html(0);
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
        $("body").trigger("resetList");
        if (data.children.length < 1) {
            $("#browser-table-wrapper").addClass("browser-table-empty");
        }
        else {
            $("#browser-table-wrapper").removeClass("browser-table-empty");
        }
        var totalSize = 0;
        $.each(data.children, function(i, file) {
            totalSize = totalSize + file.size;
            file.deriID = deriID;
            file.urnEnabled = data.urnEnabled;
            file.lastmodifiedValue = moment(file.lastmodified, "DD.MM.YYYY HH:mm:ss").format("x");
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
        if (data.size == null || data.size == undefined || data.size == 0) {
			$("#derivat-panel-size").html(derivateBrowserTools.getReadableSize(totalSize, 0));
		}
        $("#derivate-browser").removeClass("hidden");
        $("#browser-table-sort-click").find(".glyphicon").addClass("hidden");
        $("#browser-table-sort").stupidsort('asc');
        createBreadcrumb(deriID, data.absPath);
        if (filename == undefined || filename == "" ) {
            $("#file-view-large").addClass("hidden");
            $("#file-view").removeClass("hidden");
        }
        if (($("#file-view-large").hasClass("hidden") && filename != "" && filename != undefined) || !$("#file-view-large").hasClass("hidden")){
            $("#file-view").addClass("hidden");
            if (data.absPath != "/") filename = "/" + filename;
            $("body").trigger("loadViewer", deriID + data.absPath + filename);
        }
    }

    function addFileToView(file , mainDoc) {
        file.baseURL = jp.baseURL;
        var fileEntryTemplate = $("#file-entry-template").html();
        var fileEntryOutput = $(Mustache.render(fileEntryTemplate, file));
        derivateBrowserTools.updateI18nForElm(fileEntryOutput);
        $(fileEntryOutput).find(".popover-file").data("lastMod", file.lastmodified);
        $(fileEntryOutput).find(".popover-file").data("size", derivateBrowserTools.getReadableSize(file.size,0));
        $(fileEntryOutput).data("path", file.absPath);
        $(fileEntryOutput).data("deriID", file.deriID);
        $(fileEntryOutput).data("docID", file.deriID);
        $(fileEntryOutput).data("md5", file.md5);
        if ((mainDoc == file.absPath) || ("/" + mainDoc == file.absPath)){
            $("body").trigger("addFileToList", new LargeViewEntry(file.deriID, file.absPath, file.size, file.lastmodified, file.urn, true));
            $(fileEntryOutput).data("startfile", true);
        }
        else {
            $("body").trigger("addFileToList", new LargeViewEntry(file.deriID, file.absPath, file.size, file.lastmodified, file.urn, false));
        }
        $("#derivat-panel-files").html(parseInt($("#derivat-panel-files").html()) + 1);
        $(fileEntryOutput).appendTo("#browser-table-files");
        $("#browser-table-wrapper").removeClass("browser-table-empty");
    }

    function addXMLToView(file) {
        file.baseURL = jp.baseURL;
        var xmlEntryTemplate = $("#xml-entry-template").html();
        var xmlEntryOutput = $(Mustache.render(xmlEntryTemplate, file));
        derivateBrowserTools.updateI18nForElm(xmlEntryOutput);
        $(xmlEntryOutput).data("path", file.absPath);
        $(xmlEntryOutput).data("deriID", file.deriID);
        $(xmlEntryOutput).data("docID", file.deriID);
        $("#derivat-panel-files").html(parseInt($("#derivat-panel-files").html()) + 1);
        $(xmlEntryOutput).appendTo("#browser-table-files");
        $("#browser-table-wrapper").removeClass("browser-table-empty");
    }

    function addFolderToView(folder, path) {
        var folderEntryTemplate = $("#folder-entry-template").html();
        var folderEntryOutput = $(Mustache.render(folderEntryTemplate, folder));
        derivateBrowserTools.updateI18nForElm(folderEntryOutput);
        $(folderEntryOutput).data("path", folder.absPath);
        $(folderEntryOutput).data("deriID", folder.deriID);
        $(folderEntryOutput).appendTo("#browser-table-files");
        if (!folder.temp){
            $("body").trigger("addDerivatFolder", [folder.name, folder.absPath, folder.deriID, path]);
        }
        else{
            $(folderEntryOutput).find("input.input-new").data("temp", folder.temp);
        }
        $("#derivat-panel-folder").html(parseInt($("#derivat-panel-folder").html()) + 1);
        $("#browser-table-wrapper").removeClass("browser-table-empty");
    }

    /**
     * @property parentName
     * @property size
     * @property parentLastMod
     * @property hasURN
     */
    function showPanel(data, child) {
        var deriName = "";
        if (child){
            var panelName = $("#derivat-panel-name");
            if ($(panelName).html() != data.parentName){
                $(panelName).html(data.parentName);
                $("#derivat-panel-size").html(derivateBrowserTools.getReadableSize(data.size, 0));
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
        if (!data.display) {
            $("#derivate-hidden").removeClass("hidden");
            $("#btn-hide").addClass("derivate-ishidden");
        }
        else {
            $("#derivate-hidden").addClass("hidden");
            $("#btn-hide").removeClass("derivate-ishidden");
        }
        $("#derivat-panel-startfile-label").html(data.maindocName);
        if (data.hasURN || !data.urnEnabled){
            $("#btn-urnAll").addClass("hidden");
            $("#derivat-panel-urn").html(data.urn);
            $("#derivat-panel-urn").removeClass("hidden");
            $("#derivat-panel-urn").prev("dt").removeClass("hidden");
        }
        else{
            $("#btn-urnAll").removeClass("hidden");
            $("#derivat-panel-urn").addClass("hidden");
            $("#derivat-panel-urn").prev("dt").addClass("hidden");
        }
        $("#btn-urnAll").data("urnEnabled", data.urnEnabled);
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

    function convertTempFolder(input, temp){
        var parent = $("#temp-folder-" + temp).parents(".browser-table-folder");
        parent.data("path", derivateBrowserTools.getCurrentPath() + "/" + input);
        parent.find(".browser-table-file-name").html(input);
        $("body").trigger("addDerivatFolder", [input, derivateBrowserTools.getCurrentPath() + "/" + input, derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath()]);
    }

    function findFile(path) {
        return $(".browser-table-entry").filter(function() {
            return ($(this).data("path") == path);
        });
    }

    function setStartFile(entry, loadImg) {
        $(".startfile").removeClass("startfile");
        $(".browser-table-file").filter(function () {
            return $(this).data("startfile") == true;
        }).removeData("startfile");
        $(entry).data("startfile", true);
        if (loadImg) {
            derivateBrowserTools.setImgPath($("#panel-img"), $(entry).data("deriID"), $(entry).data("path"));
        }
        $("#derivat-panel-startfile-label").html($(entry).data("path"));
    }
    
    function startRenaming(oldName){
        renameRunning = true;
        var entry = findFile(oldName);
        $(entry).find(".browser-table-file-name").append('<span class="glyphicon glyphicon-refresh input-spinner" aria-hidden="true"></span>');
        $(entry).find(".browser-table-file-name").addClass("spinnerInInput");
    }

    function endRenaming(oldName){
        renameRunning = false;
        var entry = findFile(oldName);
        $(entry).find(".browser-table-file-name > .input-spinner").remove();
        $(entry).find(".browser-table-file-name").removeClass("spinnerInInput");
        updateFileDate(entry);
    }

    function updateFileDate(entry) {
        var currentDate = moment();
        $(entry).find(".browser-table-file-lastMod").html(currentDate.format("DD.MM.YYYY HH:mm:ss"));
        $(entry).find(".browser-table-file-lastMod").attr("data-sort-value", currentDate.format("x"));
    }

    function createTempFolder() {
        var folder = {
            "temp": Math.floor((Math.random() * 1000) + 1),
            "deriID": derivateBrowserTools.getCurrentDocID(),
            "absPath": derivateBrowserTools.getCurrentPath() + "/temp"
        };
        addFolderToView(folder, derivateBrowserTools.getCurrentPath());
    }

    function removeFile(node) {
        node.remove();
        if ($(node).hasClass("browser-table-file") || $(node).hasClass("browser-table-file")) {
            $("#derivat-panel-files").html(parseInt($("#derivat-panel-files").html()) - 1);
        }
        if ($(node).hasClass("browser-table-folder")) {
            $("#derivat-panel-folder").html(parseInt($("#derivat-panel-folder").html()) - 1);
        }
        if ($("#browser-table-files").html() == "") {
            $("#browser-table-wrapper").addClass("browser-table-empty");
        }
    }
        
    function removeFileWithPath(path) {
        var node = findFile(path);
        removeFile(node);
    }

    function checkIfNothingSelected() {
        if ($(".browser-table-entry .glyphicon-check").length == 0) {
            $(".btn-delete-all").addClass("faded");
            $(".btn-move-all").addClass("faded");
            $(".btn-check-all").addClass("glyphicon-unchecked");
            $(".btn-check-all").removeClass("glyphicon-check");
            $(".btn-check-all").removeData("checked");
        }
        else {
            $(".btn-delete-all").removeClass("faded");
            $(".btn-move-all").removeClass("faded");
        }
        if ($(".browser-table-entry .glyphicon-check").length == $(".browser-table-entry").length && $(".browser-table-entry").length != 0) {
            $(".btn-check-all").removeClass("glyphicon-unchecked");
            $(".btn-check-all").addClass("glyphicon-check");
            $(".btn-check-all").data("checked", true);
        }
    }

    function checkEntry(check, node){
        if (check) {
            $(node).addClass("checked");
            $(node).data("checked", true);
            $(node).find(".btn-check").removeClass("glyphicon-unchecked");
            $(node).find(".btn-check").removeClass("invisible");
            $(node).find(".btn-check").addClass("glyphicon-check");
            if ($(node).hasClass("browser-table-file")) {
                $("body").trigger("setFileSelected", [$(node).data("docID") + $(node).data("path"), true]);
            }
        }
        else {
            $(node).removeClass("checked");
            $(node).removeData("checked");
            $(node).find(".btn-check").addClass("glyphicon-unchecked");
            if ($(node).find(".invisible").length > 0) {
                $(node).find(".btn-check").addClass("invisible");
            }
            $(node).find(".btn-check").removeClass("glyphicon-check");
            if ($(node).hasClass("browser-table-file")) {
                $("body").trigger("setFileSelected", [$(node).data("docID") + $(node).data("path"), false]);
            }
        }
    }

    function filterTable(filterID) {
        filterID = filterID.replace("*", ".*");
        filterID = filterID.replace("?", ".?");
        filterID = filterID.replace("(", "\\(");
        filterID = filterID.replace(")", "\\)");

        if(filterID != ""){
            $("#btn-filter-table-input-remove").removeClass("hidden");
            $("body").trigger("filterList");
            //$(".browser-table-entry").addClass("hidden");
            var entrys = $(".browser-table-entry")
                .filter(function() {
                    return $(this).find(".browser-table-file-name").html().match(new RegExp("^" + filterID, "i"));
                }).toArray();
            $(".browser-table-entry").each(function(index, entry) {
                if (entrys.indexOf(entry) == -1) {
                    $(entry).addClass("hidden");
                }
                else {
                    $(entry).removeClass("hidden");
                    if (!$(entry).hasClass("browser-table-xml") && !$(entry).hasClass("browser-table-folder")) {
                        $("body").trigger("addFileToFilteredList", $(entry).data("deriID") + $(entry).data("path"));
                    }
                }
            });
        }
        else{
            $(".browser-table-entry").removeClass("hidden");
            $("body").trigger("resetFilteredList");
        }
    }
    
    function listFilesInSelect(data, select) {
        var resultTemplate = $("#multi-rename-select-template").html();
        var resultOutput = $(Mustache.render(resultTemplate, data));
        $(select).html(resultOutput);
    }

    function removeTileSpinner() {
        $("#btn-tileDeri").removeAttr("disabled");
        $("#btn-tileDeri").find("span").addClass("glyphicon-th");
        $("#btn-tileDeri").find("span").removeClass("glyphicon-refresh button-spinner-solo");
    }

    //ajax Methods
    function getDerivate(deriID, path, filename){
		$("#editor-loading").removeClass("hidden");
        $.ajax({
            url: "./" + deriID + path,
            type: "GET",
            dataType: "json",
            success: function(data) {
				$("#editor-loading").addClass("hidden");
                showDerivatOrFolder(deriID, data, filename);
            },
            error: function(error) {
                console.log(error);
				$("#editor-loading").addClass("hidden");
                derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.loadFailed", deriID), false);
            }
        });
    }

    function renameFile(oldName, deriID, newName, start, callback) {
        if (!renameRunning) {
            startRenaming(oldName);
            $.ajax({
                url: "rename?file=" + deriID + ":" + oldName + "&name=" + newName + "&mainFile=" + start,
                type: "POST",
                dataType: "json",
                statusCode: {
                    200: function () {
                        endRenaming(oldName);
                        var entry = findFile(oldName);
                        $(entry).data("path", oldName.substring(0, oldName.lastIndexOf("/") + 1) + newName);
                        $(entry).find(".browser-table-file-name").html(newName);
                        $(entry).find(".browser-table-file-name").removeData("oldName");
                        $(entry).find(".browser-table-file-name").removeClass("has-error");
                        $("body").trigger("renameDoc", [derivateBrowserTools.getCurrentDocID(), oldName, newName]);
                        $("body").trigger("updateName", [deriID + oldName, newName]);
                        if (start) {
                            $("#derivat-panel-startfile").data("startfile", $(entry).data("path"));
                            setStartFile(entry, false);
                        }
                        $(entry).find(".btn-edit").removeData("edit");
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.rename.success", oldName.substr(oldName.lastIndexOf("/") + 1), newName), true);
                        if (callback != undefined) {
                            callback(true, deriID, oldName, newName);
                        }
                    },
                    409: function () {
                        endRenaming(oldName);
                        var entry = findFile(oldName);
                        $(entry).find(".browser-table-file-name").addClass("has-error");
                        if (callback != undefined) {
                            callback(false);
                        }
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.rename.already", newName), false);
                    },
                    500: function () {
                        endRenaming(oldName);
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.rename.error", oldName), false);
                    },
                    401: function () {
                        endRenaming(oldName);
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                    }
                }
            });
        }
    }

    function deleteMultipleFiles(json) {
        $.ajax({
            url: "multiple",
            type: "PUT",
            contentType: 'application/json',
            dataType: "json",
            data: JSON.stringify(json),
            statusCode: {
                200: function (data) {
                    var notAll = 0;
                    $.each(data.files, function (i, file) {
                        if (file.status == 1) {
                            removeFileWithPath(file.path);
                            $("body").trigger("removeDocPerID", [file.deriID, file.path]);
                            $("body").trigger("removeFile", file.deriID + file.path);
                            checkIfNothingSelected();
                        }
                        else {
                            notAll++;
                        }
                    });
                    if (notAll > 0) {
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.delete.notAll", notAll), false);
                    }
                    else {
                        if (data.files.length == 1) {
                            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.delete.success", data.files[0].path.substr(data.files[0].path.lastIndexOf("/") + 1)), true);
                        }
                        else {
                            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.delete.successAll"), true);
                        }

                    }
                    $(".delete").removeClass("delete");
                    derivateBrowserTools.hideLoadingScreen();
                },
                500: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.delete.errorMulti"), false);
                    $(".delete").removeClass("delete");
                    derivateBrowserTools.hideLoadingScreen();
                },
                401: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                    derivateBrowserTools.hideLoadingScreen();
                }
            }
        });
    }

    function newFolder(input, temp) {
        var path = derivateBrowserTools.getCurrentDocID() + derivateBrowserTools.getCurrentPath() + "/" + input;
        $.ajax({
            url: "./" + path,
            type: "POST",
            dataType: "json",
            statusCode: {
                200: function () {
                    convertTempFolder(input, temp);
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.newFolder.success", input), true);
                },
                500: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.newFolder.error", input), false);
                },
                409: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.newFolder.already", input), false);
                },
                401: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                }
            }
        });
    }

    function changeStartFile(entry) {
        $.ajax({
            url: "./" + entry.data("deriID") + entry.data("path") + "/main",
            type: "PUT",
            dataType: "json",
            statusCode: {
                200: function () {
                    $("#derivat-panel-startfile").data("startfile", $(entry).data("path"));
                    setStartFile(entry, true);
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.startfile.success"), true);
                },
                500: function () {
                    var oldStartfile = $("#derivat-panel-startfile").data("startfile");
                    var oldEntry = $(".browser-table-file").filter(function () {
                        return $(this).data("path") == oldStartfile;
                    });
                    setStartFile(oldEntry, false);
                    $("#derivat-panel-startfile").data("startfile", $(oldEntry).data("path"));
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.startfile"), false);
                },
                401: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                }
            }
        });
    }

    function getTargetFolders(deriID) {
        $.ajax({
            url: "folders/" + deriID,
            type: "GET",
            dataType: "json",
            success: function (data) {
                var template = $("#target-folder-entry-template").html();
                var output = Mustache.render(template, data, {recursive: template});
                $("#target-panel-childlist").html(output);
                var path = derivateBrowserTools.getCurrentPath();
                if (path == "") path = "/";
                $("li.target-folder-entry[data-path='" + path + "'] > div").addClass("faded");
                $("li.target-folder-entry[data-path='" + path + "'] > span").addClass("faded");
                $('#lightbox-multi-move').modal('show');
            },
            error: function () {
                derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.move.noTargetFolder"), false);
            }
        });
    }

    function tileDerivate(deriID) {
        $.ajax({
            url: "tileDerivate?deriID=" + deriID,
            type: "POST",
            statusCode: {
                200: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.tile.success"), true);
                    removeTileSpinner();
                },
                500: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.tile.error"), false);
                    removeTileSpinner();
                },
                401: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                    removeTileSpinner();
                }
            }
        });
    }

    /**
     * @property URN
     */
    function addURN(json) {
        $.ajax({
            url: "addURN",
            type: "POST",
            contentType: 'application/json',
            dataType: "json",
            data: JSON.stringify(json),
            statusCode: {
                200: function (data) {
                    let file = Object.setPrototypeOf(data, File.prototype);
                    let derivID = file.getDerivId();
                    let path = file.getPath();
                    let urn = file.getURN();

                    if (urn != "") {
                        $(".browser-table-file").filter(function () {
                            return ($(this).data("deriID") == derivID) && ($(this).data("path") == path);
                        }).find("td.browser-table-file-urn").html(urn);
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.urn.success"), true)
                    } else {
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.urn.error"), false);
                    }
                },
                500: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.urn.error"), false);
                },
                401: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                }
            }
        });
    }

    function addURNAll(derivId) {
        $.ajax({
            url: "addURN/" + derivId,
            type: "POST",
            statusCode: {
                200: function () {
                    getDerivate(derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath());
                },
                500: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.urn.error"), false);
                },
                401: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                }
            }
        });
    }

    function moveFiles(json) {
        $.ajax({
            url: "moveDeriFiles",
            type: "POST",
            contentType: 'application/json',
            dataType: "json",
            data: JSON.stringify(json),
            statusCode: {
                200: function (data) {
                    let notMoved = 0;
                    Object.setPrototypeOf(data.target, File.prototype);
                    let moveToPath = data.target.getPath();

                    $.each(data.files, function (i, file) {
                        Object.setPrototypeOf(file, File.prototype);


                        if (file.getStatus() === 1){
                            let derivId = file.getDerivId();
                            let path = file.getPath();
                            let name = path.replace(/^.*[\\\/]/, '');

                            removeFileWithPath(path);
                            $("body").trigger("removeDocPerID", [derivId, path]);
                            if (file.type == "folder") {
                                $("body").trigger("addChildToDerivat", [name, path, derivId, moveToPath])
                            }
                            else {
                                $("body").trigger("removeFile", derivId + path);
                            }
                        }
                        else {
                            notMoved++;
                        }
                    });
                    $('#lightbox-multi-move').modal('hide');
                    $('#lightbox-multi-move-confirm').attr("disabled", true);
                    $('#lightbox-multi-move-confirm').find('.button-spinner').remove();
                    if (notMoved > 0) {
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.move.notAll", notMoved), false);
                    }
                    else {
                        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.move.success"), true);
                    }
                },
                500: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.move.error"), false);
                },
                401: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                }
            }
        });
    }

    function renameTest(filename, pattern, newname){
        var json = {
            fileName: filename,
            pattern: pattern,
            newName: newname
        };
        $.ajax({
            url: "renameMultiple/test",
            type: "POST",
            contentType: 'application/json',
            data: JSON.stringify(json),
            success: function(data) {
                $("#lightbox-multi-rename-test-input").val(data);
            },
            error: function(error) {
                console.log(error);
            }
        });
    }

    function renameMultiple(deriID, pattern, newname){
        var json = {
            derivId: deriID,
            pattern: pattern,
            newName: newname
        };
        $.ajax({
            url: "renameMultiple",
            type: "POST",
            contentType: 'application/json',
            dataType: "json",
            data: JSON.stringify(json),
            success: function(data) {
                var resultTemplate = $("#multi-rename-template").html();
                var resultOutput = $(Mustache.render(resultTemplate, data));
                derivateBrowserTools.updateI18nForElm($(resultOutput));
                $("#lightbox-multi-rename-result").html(resultOutput);
                if (data.length < 1) {
                    $("#lightbox-multi-rename-result").html("<ul><li>" + derivateBrowserTools.getI18n("db.alert.rename.multi.nothing") + "</li></ul>");
                }
                $('#lightbox-multi-rename-send-button').removeAttr("disabled");
                $('#lightbox-multi-rename-send-button').find('.button-spinner').remove();

            },
            error: function(error) {
                console.log(error);
                $("#lightbox-multi-rename-result").html("<ul><li>" + derivateBrowserTools.getI18n("db.alert.rename.multi.error") + "</li></ul>");
                $('#lightbox-multi-rename-send-button').removeAttr("disabled");
                $('#lightbox-multi-rename-send-button').find('.button-spinner').remove();
            }
        });
    }

    function getFileList(deriID, path, select){
        $.ajax({
            url: "./" + deriID + path,
            type: "GET",
            dataType: "json",
            success: function(data) {
                listFilesInSelect(data, select);
                $("#lightbox-multi-rename").modal("show");
            },
            error: function(error) {
                console.log(error);
                derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.loadFailed", deriID), false);
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
