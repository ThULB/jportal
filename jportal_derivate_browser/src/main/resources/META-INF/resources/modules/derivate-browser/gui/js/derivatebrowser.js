var DerivateBrowser = function () {
    var qpara = [], hash;
    var mouseDown = false;
    var mouseDownTimer = undefined;
    var dragElm = null;
    var dragObj = null;
    var docDeleteList = [];

    return {
        init: function () {

            $("body").on("click", "#btn-tileDeri", function () {
                tileDerivate(derivateBrowserTools.getCurrentDocID())
            });

            $("body").on("click", "#btn-hide", function () {
                var entry = $(this);
                $.get(jp.baseURL + "servlets/MCRDisplayHideDerivateServlet?derivate=" + derivateBrowserTools.getCurrentDocID())
                    .done(function() {
                        if (entry.hasClass("derivate-ishidden")) {
                            entry.removeClass("derivate-ishidden");
                            $("#derivate-hidden").addClass("hidden");
                        }
                        else {
                            entry.addClass("derivate-ishidden");
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

            $("body").on("keydown", "#btn-filter-table-input > input", function (key) {
                if (key.which == 13) {
                    filterTable($(this).val());
                }
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

            $("body").on("click", "#browser-table-sort", function () {

            });

            $("body").on("click", ".popover-img", function () {
                var parent = $(this).closest(".browser-table-entry");
                parent.find("popover-file").popover("hide");
                derivateBrowserLargeView.loadViewer(parent.data("docID") + parent.data("path"));
                $("#file-view").addClass("hidden");
            });

            //$("body").on("click", "#btn-derivate-options", function () {
            //    $("#derivat-panel-buttons").toggleClass("hidden");
            //});

            $("body").on("click", "#btn-download-zip", function () {
                window.location.href = jp.baseURL + "servlets/MCRZipServlet/" + derivateBrowserTools.getCurrentDocID();
                $("#download-new-select-area").addClass("hidden");
                //$("#derivat-panel-buttons").addClass("hidden");
            });

            $("body").on("click", "#btn-download-tar", function () {
                window.location.href = jp.baseURL + "servlets/MCRTarServlet/" + derivateBrowserTools.getCurrentDocID();
                $("#download-new-select-area").addClass("hidden");
                //$("#derivat-panel-buttons").addClass("hidden");
            });

            $("body").on("click", "#btn-download", function () {
                $("#download-new-select-area").toggleClass("hidden");
            });

            $("body").on("click", "#btn-list-view", function () {
                $(this).addClass("hidden");
                $("#btn-large-view").removeClass("hidden");
                $("#browser-table-head").removeClass("hidden");
                $("#browser-table-wrapper").removeClass("hidden");
                $("#derivate-browser-footer").removeClass("hidden");
            });

            $("body").on("click", "#btn-large-view", function () {
                $("#file-view").addClass("hidden");
                derivateBrowserLargeView.loadViewer();
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

            $("body").on("click", "#btn-viewer", function () {
                window.location.href = jp.baseURL + "rsc/viewer/" + derivateBrowserTools.getCurrentDocID() + $("#derivat-panel-startfile").data("startfile");
            });

            $("body").on("click", "#folder-list-new-choose", function () {
                $("#folder-list-new-select-area").toggleClass("hidden");
            });

            $("body").on("click", "#folder-list-new-button-journal", function () {
                derivateBrowserFileView.newDoc("jpjournal");
                $("#folder-list-new-select-area").addClass("hidden");
            });

            $("body").on("click", "#folder-list-new-button-volume", function () {
                derivateBrowserFileView.newDoc("jpvolume");
                $("#folder-list-new-select-area").addClass("hidden");
            });

            $("body").on("click", "#folder-list-new-button-article", function () {
                derivateBrowserFileView.newDoc("jparticle");
                $("#folder-list-new-select-area").addClass("hidden");
            });

            $("body").on("click", "#lightbox-alert-deleteDoc-confirm", function () {
                var json = [];
                json.push({"objId": derivateBrowserTools.getCurrentDocID()});
                derivateBrowserFileView.deleteDocs(json);
                $("#lightbox-alert-deleteDoc").modal('hide');
            });

            $("body").on("click", "#journal-info-button-delete", function () {
                if ($(".aktiv").length == 1) {
                    derivateBrowserNavigation.gotDerivateChilds(derivateBrowserTools.getCurrentDocID(), checkForChilds);
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

            $("body").on("click", "#journal-info-button-cancel", function () {
                derivateBrowserFileView.cancelEditDoc();
            });

            $("body").on("click", "#journal-info-button-save", function () {
                derivateBrowserFileView.updateDoc($(this).data("mode"));
            });

            $("body").on("click", "#btn-deleteDeri", function () {
                //$("#derivat-panel-buttons").addClass("hidden");
                showDeleteDocAlert("derivate");
            });

//			$.address.internalChange(function() {
////				console.log("internal");
//			});

            $.address.externalChange(function () {
                $(".modal").modal("hide");
                var paths = $.address.pathNames();
                var path = "";
                if (paths.length > 1) {
                    path = "/" + paths.slice(1).join("/");
                }
                path = path.replace("%20", " ");
                if (paths[0] != undefined) {
                    derivateBrowserTools.goTo(paths[0], path);
                }
            });

            $("body").on("mouseenter", ".popover-file", function () {
                if (!$(this).hasClass("popoverAdded")) {
                    var parent = $(this).closest(".browser-table-entry");
                    var file = [];
                    file.lastmodified = $(this).data("lastMod");
                    file.size = $(this).data("size");
                    var popoverTemplate = $("#popover-template").html();
                    var popOverOutput = $(Mustache.render(popoverTemplate, file));
                    //$(this).popover({content: popOverOutput, html: true});
                    derivateBrowserTools.setImgPath($(popOverOutput).find(".popover-img"), $(parent).data("deriID"), $(parent).data("path"));
                    derivateBrowserTools.setupPopover($(this), popOverOutput);
                    $(this).addClass("popoverAdded");
                    $(this).popover("show");
                }
            });

            $("body").on("mouseleave", ".popover", function() {
                $(this).parent().find(".popover-file").popover("hide");
            });

            $("body").on("mousedown", ".popover-file", function () {
                var entry = $(this);
                clearTimeout(mouseDownTimer);
                mouseDownTimer = setTimeout(function() {
                    mouseDown = true;
                    dragElm = entry.closest(".browser-table-entry");
                }, 500);
            });

            $("body").on("mousedown", "#view-large-normal", function (e) {
                var entry = $(this);
                e.preventDefault();
                e.stopPropagation();
                clearTimeout(mouseDownTimer);
                mouseDownTimer = setTimeout(function() {
                    mouseDown = true;
                    dragElm = entry;
                }, 500);
            });

            $("body").on("mousedown", ".folder:not(.journal) > .folder-name, .folder:not(.journal) > span.icon", function () {
                var entry = $(this);
                clearTimeout(mouseDownTimer);
                mouseDownTimer = setTimeout(function() {
                    mouseDown = true;
                    dragElm = entry.closest(".folder");
                }, 500);
            });

            $("body").on("mousemove", function (e) {
                if (mouseDown) {
                    e.preventDefault();
                    e.stopPropagation();
                    if ($(dragObj)) {
                        $(dragObj).offset({
                            top: e.pageY - 1 /*- ($(dragObj).height() / 2)*/,
                            left: e.pageX + 1 //- ($(dragObj).width() / 2)
                        }, 1);
                        //TODO evtl. flackern
//			        	$(dragObj).addClass("hidden");
                        $(".drag-elm-hover").removeClass("drag-elm-hover");
                        $(".drag-info").addClass("hidden");
                        var hoverObj = document.elementFromPoint(e.pageX, e.pageY).closest(".folder, #derivat-panel-startfile");
                        if ($(hoverObj).closest(".folder").length > 0 && !$(hoverObj).hasClass("derivat") && ($(hoverObj).data("faded") == undefined)) {
                            $("#drag-info-link").removeClass("hidden");
                            $(hoverObj).addClass("drag-elm-hover");
                        }
                        if ($(hoverObj).closest("#derivat-panel-startfile").length > 0) {
                            $("#drag-info-start").removeClass("hidden");
                        }
//		        		$(dragObj).removeClass("hidden");
                    }
                    if ((dragObj == null) /*&& (Math.abs(mouseY - e.pageY) > 20)*/) {
                        $("li.derivat").addClass("faded");
                        if ($(dragElm).hasClass("folder")) {
                            var elm = $("<div id='drag-doc'></div>");
                            $(elm).append($(dragElm).children("span.icon").clone());
                            $(elm).append($(dragElm).children("div.folder-name").clone());
                            $(elm).width($(dragElm).width());
//			        		var elm = dragElm.find("span.icon").clone();
                            $("body").append(elm);
                            if ($(".aktiv").length > 1) {
                                $(elm).append("<div id='drag-doc-count'>" + $(".aktiv").length + "</div>");
                                $(elm).css("padding-right", $("#drag-doc-count").outerWidth() + "px");
                                $(".aktiv").addClass("faded");
                            }
                            $(dragElm).addClass("faded");
                            $(elm).offset($(dragElm).offset());
                            $(elm).animate({
                                "width": "100px"
                            });
                            dragObj = elm;
                        }
                        if ($(dragElm).hasClass("browser-table-file") || $(dragElm).hasClass("view-large-draggable")) {
                            derivateBrowserNavigation.fadeEntry($(".aktiv").closest(".folder:not('.derivat')"));
                            var div = $('<div id="drag-img-div"><img class="img-placeholder img-placeholder-startfile"><img id="drag-img" class="hidden"></div>');
                            div.append('<div id="drag-info-link" class="hidden drag-info">' + derivateBrowserTools.getI18n("db.label.link") + '</div>');
                            div.append('<div id="drag-info-start" class="hidden drag-info">' + derivateBrowserTools.getI18n("db.label.changeStart") + '</div>');
                            $(div).children(".img-placeholder").attr("src", jp.baseURL + "images/file-logo.svg");
                            if (dragElm.data("path") != undefined){
                                derivateBrowserTools.setImgPath($(div).find("#drag-img"), dragElm.data("deriID"), dragElm.data("path"));
                            }
                            else{
                                $(div).find("#drag-img").attr("src", $(dragElm).attr("src"));
                                $(div).find("#drag-img").removeClass("hidden");
                                $(div).find(".img-placeholder").addClass("hidden");
                            }
                            $("body").append(div);
                            dragObj = div;
                        }
                        window.getSelection().removeAllRanges();
                        $("body").attr('unselectable', 'on')
                            .addClass("noSelect").bind('selectstart', function () {
                                return false;
                            });
                    }

                }
            });

            $("body").on("mouseup", function (e) {
                clearTimeout(mouseDownTimer);
                mouseDown = false;
                if (dragObj) {
                    var dropObj = document.elementFromPoint(e.pageX, e.pageY);
                    if (($(dropObj).closest("#derivat-panel-startfile").length > 0) && $(dragElm).hasClass("browser-table-file")) {
                        dragObj.remove();
                        changeStartFile(dragElm);
                    }
                    if (($(dropObj).closest(".folder").length > 0) && ($(dragElm).hasClass("browser-table-file") || $(dragElm).hasClass("view-large-draggable"))) {
                        if (!$(dropObj).closest(".folder").hasClass("derivat") && ($(dropObj).closest(".folder").data("faded") == undefined)) {
                            dragObj.remove();
                            if (dragElm.data("path") != undefined){
                                setLink($(dropObj).closest(".folder").data("docID"), $(dragElm).data("docID") + $(dragElm).data("path"));
                            }
                            else{
                                setLink($(dropObj).closest(".folder").data("docID"), $(dragElm).data("id"));
                            }
                        }
                    }
                    if (($(dropObj).closest(".folder").length > 0) && $(dragElm).hasClass("folder")) {
                        var moveTo = $(dropObj).closest(".folder");
                        if (!$(moveTo).hasClass("faded") && !($(dragElm).parent().closest(".folder")[0] == $(moveTo)[0])) {
                            if ((!$(moveTo).hasClass("derivat") && !$(moveTo).hasClass("article")) || ($(moveTo).hasClass("article") && $(dragElm).hasClass("derivat"))) {
                                if (!($(moveTo).hasClass("journal") && $(dragElm).hasClass("article"))) {
                                    dragObj.remove();
                                    var json = [];
                                    $(".aktiv").each(function () {
                                        json.push({
                                            "objId": $(this).data("docID"),
                                            "newParentId": $(moveTo).data("docID")
                                        });
                                    });
                                    moveDocTo(json);
                                }
                            }
                        }
                    }
                    $(".drag-elm-hover").removeClass("drag-elm-hover");
                    $(dragObj).animate({
                        top: $(dragElm).offset().top,
                        left: $(dragElm).offset().left + 20
                    }, 400, function () {
                        $(dragElm).removeClass("faded");
                        $(".aktiv").removeClass("faded");
                        $("li.derivat").removeClass("faded");
                        derivateBrowserNavigation.unFadeEntry($(".aktiv").closest(".folder:not('.derivat')"));
                        dragObj.remove();
                        dragElm = null;
                        dragObj = null;
                    });
                    $("body").removeAttr('unselectable')
                        .removeClass("noSelect").bind('selectstart', function () {
                            return true;
                        });
                }
            });

            $("body").on("click", "span.btn-new-urn", function () {
                var entry = $(this).parents(".browser-table-entry");
                var json = {
                    "deriID": entry.data("deriID"),
                    "completeDeri": false,
                    "files": []
                };
                var json2 = {
                    "path": entry.data("path")
                };
                json.files.push(json2);
                addURN(json);
            });

            $("body").on("click", "#btn-urnAll", function () {
                var json = {
                    "deriID": derivateBrowserTools.getCurrentDocID(),
                    "completeDeri": true,
                    "files": []
                };
                addURN(json);
                //$("#derivat-panel-buttons").addClass("hidden");
            });

            $("body").on("click", "#journal-info-button-edit", function () {
                derivateBrowserFileView.editDoc();
            });

            $("body").on("keydown", "#folder-list-search-input", function (key) {
                if (key.which == 13) {
                    searchJournals($(this).val());
                }
            });

            $("body").on("click", "#folder-list-search-button", function () {
                searchJournals($("#folder-list-search-input").val());
            });

            $("body").on("click", ".folder:not(.derivat) > .folder-name, .folder:not(.derivat) > span.icon", function (event) {
                if (event.shiftKey || event.ctrlKey) {
                    window.getSelection().removeAllRanges();
                    var current = derivateBrowserNavigation.getCurrentNode();
                    var endNode = $(this).parent(".folder");
                    if (current.parent()[0] == endNode.parent()[0]) {
                        if (event.ctrlKey) {
                            if ($(".aktiv").length > 1){
                                $(this).parent(".folder").toggleClass("aktiv");
                            }
                        }
                        else {
                            derivateBrowserNavigation.selectRange(derivateBrowserNavigation.getCurrentNode(), $(this).parent(".folder"));
                        }
                        derivateBrowserFileView.showSelectedDocs();
                    }
                }
                else {
                    derivateBrowserTools.goTo($(this).parent().data("docID"), "");
                }
            });

            $("body").on("click", ".button-expand", function () {
                derivateBrowserNavigation.expandDoc($(this).closest(".folder").data("docID"));
            });

            $("body").on("click", ".button-contract", function () {
                $(this).siblings("ul.children").addClass("hide-folder");
                $(this).siblings("ul.children").html("");
                $(this).removeClass("button-contract glyphicon-minus");
                $(this).addClass("button-expand glyphicon-plus");
            });

            $("body").on("click", ".derivat > .folder-name, .derivat > span.icon", function (event) {
                if (!event.shiftKey) {
                    derivateBrowserTools.goTo($(this).parent().data("docID"), "");
                }
            });

            $("body").on("click", ".derivat-folder > .folder-name, .derivat-folder > span.icon", function (event) {
                if (!event.shiftKey) {
                    derivateBrowserTools.goTo($(this).parent().data("deriID"), $(this).parent().data("path"));
                }
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
                    name.html("<input type='text' value='" + name.data("oldName") + "' class='form-control input-sm'></input>");
                }
                else {
                    $(this).removeData("edit");
                    name.data("oldName", name.html());
                    name.html($(name).find("input").val());
                }
            });

            $("body").on("keydown", "td.browser-table-file-name > input", function (event) {
                if (!$(this).hasClass("input-new")) {
                    if (event.which == 13) {
                        if ($(this).val() != $(this).parent().data("oldName")) {
                            var main = ($(this).parents(".browser-table-entry").data("startfile") == true ? "true" : "false");
                            derivateBrowserFileView.renameFile($(this).parents(".browser-table-entry").data("path"), $(this).parents(".browser-table-entry").data("deriID"), $(this).val(), main);
                        }
                        else {
                            $(this).parent().html($(this).parent().data("oldName"));
                        }
                    }
                    if (event.which == 27) {
                        $(this).parent().html($(this).parent().data("oldName"));
                        $(this).closest(".browser-table-entry").find(".btn-edit").removeData("edit");
                    }
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

            $("body").on("click", "#lightbox-alert-delete-confirm", function () {
                var json = {
                        files: []
                };
                derivateBrowserTools.showLoadingScreen();
                $(".delete").each(function () {
                    var json2 = {
                        "deriID": $(this).data("deriID"),
                        "path": $(this).data("path")
                    };
                    json.files.push(json2);
                });
                deleteMultipleFiles(json);
                $("#lightbox-alert-delete").modal('hide');
            });

            $("body").on("click", "#lightbox-alert-delete-docs-confirm", function () {
                var json = [];
                $.each(docDeleteList, function (i, elm) {
                    json.push({"objId": elm});
                });
                derivateBrowserFileView.deleteDocs(json);
                $("#lightbox-alert-delete-docs").modal('hide');
            });

            $("body").on("click", ".lightbox-alert-delete-cancel", function () {
                $("#lightbox-alert-delete-list").html("");
                $(".delete").removeClass("delete");
                $("#lightbox-alert-delete").modal('hide');
            });

            $("body").on("click", ".lightbox-alert-delete-docs-cancel", function () {
                $("#lightbox-alert-delete-docs-list").html("");
                $("#lightbox-alert-delete-docs").modal('hide');
                docDeleteList = [];
            });

            $("body").on("click", ".btn-add", function () {
                derivateBrowserFileView.createTempFolder();
                $("#browser-table-wrapper").animate({
                    scrollTop: $("#browser-table").height()
                }, 100);
                $(".input-new").focus();
            });

            $("body").on("keydown", ".input-new", function (event) {
                if (event.which == 13) {
                    newFolder($(this).val(), $(this).data("temp"));
                }
                if (event.which == 27) {
                    $(this).parents(".browser-table-entry").remove();
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
                    var moveTo = $("#target-panel-childlist div.folder-name").html() + ":" + $(".target-folder-selected").attr("data-path");
                    var json = {
                        "moveTo": moveTo,
                        "files": []
                    };

                    if ($(".glyphicon-check").length > 0) {
                        var entrys = $(".browser-table-entry").filter(function () {
                            return $(this).data("checked") == true;
                        });
                        entrys.each(function () {
                            var file = $(this).data("deriID") + ":" + $(this).data("path");
                            var path = "/";
                            var type = "file";
                            if ($(this).hasClass("browser-table-folder")) {
                                type = "folder";
                            }
                            if (file != moveTo && $(this).data("deriID") + ":" + derivateBrowserTools.getCurrentPath() != moveTo) {
                                var json2 = {
                                    "file": file,
                                    "type": type
                                };
                                json.files.push(json2);
                            }
                        });
                    }
                    moveFiles(json);
                }
                else {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.move.targetFolder"), false);
                }
            });

            $("body").on("mouseenter", ".mightOverflow", function () {
                var $this = $(this);

                if (this.offsetWidth < this.scrollWidth && !$this.attr('title')) {
                    $this.attr('title', $this.text());
                }
            });

//			$(derivateBrowserNavigation).on("Test", function() {
//				console.log("TEST");
//			});

            $(derivateBrowserNavigation).on("DerivatFolder", function (e, name, filepath, deriID, absPath) {
                derivateBrowserNavigation.addChildToDerivat(name, filepath, deriID, absPath);
            });

            $("#browser-table").stupidtable();
            $("#browser-table").on("aftertablesort", function (event, data) {
                $("#browser-table-sort-click").find(".glyphicon").addClass("hidden");
                var dir = $.fn.stupidtable.dir;
                if (data.direction == dir.ASC){
                    derivateBrowserLargeView.sortList("ASC");
                    $("#browser-table-sort-click").find(".glyphicon-chevron-up").removeClass("hidden");
                }
                else{
                    derivateBrowserLargeView.sortList("DESC");
                    $("#browser-table-sort-click").find(".glyphicon-chevron-down").removeClass("hidden");
                }
            });
            readQueryParameter();
            var lang = qpara["lang"];
            if (lang == undefined) lang = "de";
            derivateBrowserTools.loadI18n(lang);
        }
    };

    function deleteMultipleFiles(json) {
        $.ajax({
            url: "multiple",
            type: "DELETE",
            contentType: 'application/json',
            dataType: "json",
            data: JSON.stringify(json),
            statusCode: {
                200: function (data) {
                    var notAll = 0;
                    $.each(data.files, function (i, file) {
                        if (file.status == 1) {
                            derivateBrowserFileView.removeFileWithPath(file.path);
                            derivateBrowserNavigation.removeDocPerID(file.deriID, file.path);
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
                            derivateBrowserLargeView.removeFile(data.files[0].deriID + data.files[0].path);
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
                    derivateBrowserFileView.tempToFolder(input, temp);
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
                    derivateBrowserFileView.changeStartFile(entry, true);
                },
                500: function () {
                    var oldStartfile = $("#derivat-panel-startfile").data("startfile");
                    var oldEntry = $(".browser-table-file").filter(function () {
                        return $(this).data("path") == oldStartfile;
                    });
                    derivateBrowserFileView.changeStartFile(oldEntry, false);
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

    function moveFiles(json) {
        $.ajax({
            url: "moveDeriFiles",
            type: "POST",
            contentType: 'application/json',
            dataType: "json",
            data: JSON.stringify(json),
            statusCode: {
                200: function (data) {
                    var notMoved = 0;
                    $.each(data.files, function (i, oneFile) {
                        if (oneFile.status == "1") {
                            var deriID = oneFile.file.substr(0, oneFile.file.indexOf(":"));
                            var path = oneFile.file.substr(oneFile.file.indexOf(":") + 1);
                            var name = oneFile.file.substr(oneFile.file.lastIndexOf("/") + 1);
                            derivateBrowserFileView.removeFileWithPath(path);
                            derivateBrowserNavigation.removeDocPerID(deriID, path);
                            if (oneFile.type == "folder") {
                                derivateBrowserNavigation.addChildToDerivat(name, path, deriID, data.moveTo.substr(oneFile.file.indexOf(":") + 1));
                            }
                        }
                        else {
                            notMoved++;
                        }
                    });
                    $('#lightbox-multi-move').modal('hide');
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
                    if (json.completeDeri) {
                        derivateBrowserFileView.showDerivateOrDoc(derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath());
                    }
                    else {
                        var dID = data.deriID;
                        $.each(data.files, function (i, file) {
                            if (file.URN != "") {
                                $(".browser-table-file").filter(function () {
                                    return ($(this).data("deriID") == dID) && ($(this).data("path") == file.path);
                                }).find("td.browser-table-file-urn").html(file.URN);
                                derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.urn.success"), true)
                            }
                            else {
                                derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.urn.error"), false);
                            }
                        });
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

    function moveDocTo(json) {
        derivateBrowserTools.showLoadingScreen();
        $.ajax({
            url: "moveDocs",
            type: "PUT",
            contentType: 'application/json',
            dataType: "json",
            data: JSON.stringify(json),
            statusCode: {
                200: function (data) {
                    derivateBrowserTools.newAsyncMonitor(data.length, derivateBrowserTools.goTo);
                    $.each(data, function (i , elm) {
                        if (elm.success) {
                            derivateBrowserNavigation.removeDocPerID(elm.objId);
                            derivateBrowserNavigation.addDoc(elm.objId);
                        }
                        else {
                            derivateBrowserTools.doneAsync(elm.newParentId);
                            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.move.notAllDocs"), false);
                        }
                    });
                },
                500: function () {
                    derivateBrowserTools.hideLoadingScreen();
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.move.error"), false);
                },
                401: function () {
                    derivateBrowserTools.hideLoadingScreen();
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                }
            }
        });
    }

    function setLink(docID, imgPath) {
        $.ajax({
            url: "link?docID=" + docID + "&imgPath=" + imgPath,
            type: "POST",
            statusCode: {
                200: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.link.add.success"), true);
                    if (!$("#file-view-large").hasClass("hidden")) {
                        derivateBrowserLargeView.updateLinks(docID);
                    }
                },
                500: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.link.add.error"), false);
                },
                401: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                }
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
                    derivateBrowserFileView.removeLink(imgPath);
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

    function tileDerivate(deriID) {
        $.ajax({
            url: "tileDerivate?deriID=" + deriID,
            type: "POST",
            statusCode: {
                200: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.tile.success"), true);
                },
                500: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.tile.success"), false);
                },
                401: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                }
            }
        });
    }

    function searchJournals(query) {
        $("#folder-list-ul").html("");
        $("#derivate-browser").addClass("hidden");
        $("#derivat-panel").addClass("hidden");
        if (query == "") query = "*";
        derivateBrowserNavigation.searchJournals(query);
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

    function readQueryParameter() {
        var q = document.URL.split(/\?(.+)?/)[1];
        if (q != undefined) {
            q = q.split('&');
            for (var i = 0; i < q.length; i++) {
                hash = q[i].split(/=(.+)?/);
                qpara.push(hash[1]);
                qpara[hash[0]] = hash[1];
            }
        }
    }

    function showDeleteDocAlert(docType) {
        $("#lightbox-alert-deleteDoc-label").html(derivateBrowserTools.getI18n("db.alert.document.delete." + docType + ".title"));
        $("#lightbox-alert-deleteDoc-text").html(derivateBrowserTools.getI18n("db.alert.document.delete." + docType + ".text", derivateBrowserTools.getCurrentDocID()));
        $("#lightbox-alert-deleteDoc").modal('show');
    }

    /**
     * @property docs
     * @property derivateCount
     */
    function checkForChilds(data, docs, docID) {
        if (data.docs[0].derivateCount > 0) {
            var name = derivateBrowserNavigation.getDocName(docID);
            if (name == undefined) name = docID;
            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.delete.digi", name), false);
            docDeleteList = [];
        }
        else {
            if (docs != undefined){
                docDeleteList.push(docID);
                if (docs.length > 0) {
                    deleteMultipleDocs(docs);
                }
                else {
                    derivateBrowserTools.showDeleteAlertDocs(docDeleteList)
                }
            }
            else {
                showDeleteDocAlert("journal");
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
            derivateBrowserLargeView.filterList();
            $(".browser-table-entry").addClass("hidden");
            var entrys = $(".browser-table-entry")
                .filter(function() {
                    return $(this).find(".browser-table-file-name").html().match(new RegExp("^" + filterID, "i"));
                });
            $(entrys).each(function(index, entry) {
                $(entry).removeClass("hidden");
                derivateBrowserLargeView.addFileToFilteredList(($(entry).data("deriID") + $(entry).data("path")));
            });
        }
        else{
            $(".browser-table-entry").removeClass("hidden");
            derivateBrowserLargeView.resetFilteredList();
        }
    }

    function checkEntry(check, node){
        if (check) {
            $(node).addClass("checked");
            $(node).data("checked", true);
            $(node).find(".btn-check").removeClass("glyphicon-unchecked");
            $(node).find(".btn-check").removeClass("invisible");
            $(node).find(".btn-check").addClass("glyphicon-check");
            derivateBrowserLargeView.getFile($(node).data("docID") + $(node).data("path")).selected = true;
        }
        else {
            $(node).removeClass("checked");
            $(node).removeData("checked");
            $(node).find(".btn-check").addClass("glyphicon-unchecked");
            $(node).find(".btn-check").addClass("invisible");
            $(node).find(".btn-check").removeClass("glyphicon-check");
            derivateBrowserLargeView.getFile($(node).data("docID") + $(node).data("path")).selected = false;
        }
    }

    function deleteMultipleDocs(docs) {
        derivateBrowserNavigation.gotDerivateChilds(docs.pop(), checkForChilds, docs);
    }
};

$(document).ready(function() {
	var DerivateBrowserInstance = new DerivateBrowser();
	DerivateBrowserInstance.init();
});