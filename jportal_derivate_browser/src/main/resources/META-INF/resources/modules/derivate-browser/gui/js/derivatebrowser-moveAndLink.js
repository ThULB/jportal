
var derivateBrowserMoveAndLink = (function () {

    var mouseDown = false;
    var mouseDownTimer = undefined;
    var dragElm = null;
    var dragObj = null;

    function bindActions() {
        bindUIActions();
        bindEventActions();
    }

    function bindUIActions() {
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
                    var hoverObj = document.elementFromPoint(e.pageX - window.pageXOffset, e.pageY - window.pageYOffset).closest(".folder, #derivat-panel-startfile");
                    if ($(hoverObj).closest(".folder").length > 0 && !$(hoverObj).hasClass("derivat") && ($(hoverObj).data("faded") == undefined)) {
                        $("#drag-info-link").removeClass("hidden");
                        $(hoverObj).addClass("drag-elm-hover");
                    }
                    if ($(hoverObj).closest("#derivat-panel-startfile").length > 0) {
                        $("#drag-info-start").removeClass("hidden");
                    }
                }
                if ((dragObj == null) /*&& (Math.abs(mouseY - e.pageY) > 20)*/) {
                    $("li.derivat").addClass("faded");
                    if ($(dragElm).hasClass("folder")) {
                        var elm = $("<div id='drag-doc'></div>");
                        $(elm).append($(dragElm).children("span.icon").clone());
                        $(elm).append($(dragElm).children("div.folder-name").clone());
                        $(elm).width($(dragElm).width());
                        $("body").append(elm);
                        if ($(".aktiv").length > 1 && $(dragElm).hasClass("aktiv")) {
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
                        $("body").trigger("fadeEntry", $(".aktiv").closest(".folder:not('.derivat')"));
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
                var dropObj = document.elementFromPoint(e.pageX - window.pageXOffset, e.pageY - window.pageYOffset);
                if (($(dropObj).closest("#derivat-panel-startfile").length > 0) && $(dragElm).hasClass("browser-table-file")) {
                    dragObj.remove();
                    $("body").trigger("changeStartFile", dragElm);
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
                                if ($(dragElm).hasClass("aktiv")) {
                                    $(".aktiv").each(function () {
                                        json.push({
                                            "objId": $(this).data("docID"),
                                            "newParentId": $(moveTo).data("docID")
                                        });
                                    });
                                }
                                else {
                                    json.push({
                                        "objId": $(dragElm).data("docID"),
                                        "newParentId": $(moveTo).data("docID")
                                    });
                                }
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
                    $("body").trigger("unFadeEntry", $(".aktiv").closest(".folder:not('.derivat')"));
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
    }

    function bindEventActions() {

    }

    //private Methods


    //ajax Methods
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
                            $("body").trigger("removeDocPerID", elm.objId);
                            $("body").trigger("addDoc", elm.objId);
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
                        $("body").trigger("updateLinks", docID);
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
    
    return {
        //public
        init: function() {
            bindActions();
        }
    };
})();