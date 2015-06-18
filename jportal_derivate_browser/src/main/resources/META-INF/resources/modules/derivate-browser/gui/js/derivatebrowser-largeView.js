var derivateBrowserLargeView = (function () {

    var currentFileList = [],
        fullFileList = [],
        currentFileIndex = 0,
        currentSliderIndex = 0;

    //binds
    $("#file-view-large").on("click", "#view-large-link-list a", function (event) {
        event.preventDefault();
        derivateBrowserTools.goTo($(this).attr("data-id"), "");
    });

    $("#file-view-large").on("click", "#view-large-panel-collapse-btn", function () {
        if ($(this).hasClass("glyphicon-chevron-down")) {
            $("#view-large-main").addClass("expended-view");
            $(this).removeClass("glyphicon-chevron-down");
            $(this).addClass("glyphicon-chevron-up");
        }
        else {
            $("#view-large-main").removeClass("expended-view");
            $(this).removeClass("glyphicon-chevron-up");
            $(this).addClass("glyphicon-chevron-down");
        }
    });

    $("#file-view-large").on("click", ".view-large-thump", function () {
        setCurrentFileTo($(this).data("id"));
    });

    $("#file-view-large").on("click", "#view-large-left", function () {
        setPrevFile();
    });

    $("#file-view-large").on("click", "#view-large-right", function () {
        setNextFile();
    });

    $("#file-view-large").on("click", "#view-large-slider-left", function () {
        if (!$(this).hasClass("invisible")){
            setPrevFileSlider();
        }
    });

    $("#file-view-large").on("click", "#view-large-slider-right", function () {
        if (!$(this).hasClass("invisible")){
            setNextFileSlider();
        }
    });

    $("#file-view-large").on("click", ".view-large-resizeable", function (evt) {
        enlargeorHideImage(evt.pageX, evt.pageY);
    });

    $("body").on("click", "#view-large-overlay", function () {
        $("#view-large-overlay").remove();
    });

    $("body").on("mousemove","#view-large-overlay", function (evt) {
        setLargePosition(evt.pageX, evt.pageY);
    });

    $("body").on("click", "#button-view-large-close", function () {
        hideLargeView();
        $("#file-view").removeClass("hidden");
        derivateBrowserTools.setFileName("");
        //derivateBrowserTools.goTo(derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath());
    });

    $("#file-view-large").on("click", ".btn-check-large", function () {
        var file = currentFileList[currentFileIndex];
        var parent = derivateBrowserFileView.getFile(file.path);
        if (!file.selected) {
            $(".last-selected").removeClass("last-selected");
            $(parent).addClass("last-selected");
            $(parent).addClass("checked");
            $(parent).data("checked", true);
            $(parent).find(".btn-check").removeClass("glyphicon-unchecked");
            $(parent).find(".btn-check").removeClass("invisible");
            $(parent).find(".btn-check").addClass("glyphicon-check");
            file.selected = true;
            updatePanelData(currentFileList[currentFileIndex]);
        }
        else {
            $(parent).removeClass("checked");
            $(parent).removeData("checked");
            $(parent).find(".btn-check").addClass("glyphicon-unchecked");
            $(parent).find(".btn-check").addClass("invisible");
            $(parent).find(".btn-check").removeClass("glyphicon-check");
            file.selected = false;
            updatePanelData(currentFileList[currentFileIndex]);
        }
        if ($(".browser-table-entry .glyphicon-check").length == 0) {
            $(".btn-delete-all").addClass("faded");
            $(".btn-move-all").addClass("faded");
        }
        else {
            $(".btn-delete-all").removeClass("faded");
            $(".btn-move-all").removeClass("faded");
        }
    });

    $("#file-view-large").on("click", ".btn-edit-large", function () {
        if ($("#view-large-panel-input").hasClass("hidden")) {
            $("#view-large-panel-input").val($("#view-large-panel-title").html());
            $("#view-large-panel-title").addClass("hidden");
            $("#view-large-panel-input").removeClass("hidden");
        }
        else {
            hidePanelInput();
        }
    });

    $("#file-view-large").on("click", ".btn-delete-large", function () {
        var entry = derivateBrowserFileView.getFile(currentFileList[currentFileIndex].path);
        var startfile = $("#derivat-panel-startfile").data("startfile");
        if ($(entry).data("startfile") != true && startfile.indexOf($(entry).data("path")) != 0) {
            entry.addClass("delete");
            var fileList = [];
            fileList.push($(entry).data("path"));
            derivateBrowserTools.showDeleteAlert(fileList);
        }
        else {
            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.delete.startfile"), false);
        }
    });

    $("#file-view-large").on("keydown", "#view-large-panel-input", function (key) {
        if (key.which == 13) {
            var newName = $("#view-large-panel-input").val();
            var file = currentFileList[currentFileIndex];
            if (newName != file.name){
                derivateBrowserFileView.renameFile(file.path,derivateBrowserTools.getCurrentDocID(), newName, file.start, changeName);
            }
            else {
                hidePanelInput();
            }
        }
        if (key.which == 27) {
            hidePanelInput();
        }
    });

    $("body").on("keydown", function (key) {
        if (!$("#file-view-large").hasClass("hidden") && $("#view-large-panel-input").hasClass("hidden")) {
            if (key.which == 37) {  // <-
                setPrevFile();
            }

            if (key.which == 39) {  // ->
                setNextFile();
            }

            if (key.which == 32) {  // LEER
                enlargeorHideImage($(window).width() / 2, $(window).height() / 2);
            }
        }
    });

    //private Methods
    function initialize(id){
        //fillThumbSlider();
        if (id == "" || id == null){
            setCurrentFileTo(0);
        }
        else{
            var index = getIndexFromID(id);
            if (index != -1) {
                setCurrentFileTo(index);
            }
            else {
                setCurrentFileTo(0);
            }
        }
        $("#file-view-large").removeClass("hidden");
    }

    function setNextFile() {
        if (currentFileIndex < currentFileList.length - 1){
            setCurrentFileTo(++currentFileIndex);
        }
    }

    function setPrevFile() {
        if (currentFileIndex > 0){
            setCurrentFileTo(--currentFileIndex);
        }
    }

    function setNextFileSlider() {
        if (currentSliderIndex + 1 < currentFileList.length){
            $("#view-large-slider-content > a").first().remove();
            addThumbFile(++currentSliderIndex, true,currentFileIndex);
        }
    }

    function setPrevFileSlider() {
        if (currentSliderIndex - 5 > -1){
            $("#view-large-slider-content > a").last().remove();
            addThumbFile((--currentSliderIndex - 4), false, currentFileIndex);
        }
    }

    function setThumbFileTo(currentFile) {
        $("#view-large-slider-content").html("");
        for (var i = currentFile - 2; i <= currentFile + 2; i++){
            if (i >= 0 && i < currentFileList.length){
                addThumbFile(i, true, currentFile);
            }
            else{
                var img = $('<a class="view-large-thump-empty">');
                $("#view-large-slider-content").append(img);
            }
            currentSliderIndex = i;
        }

    }

    function addThumbFile(id, append, currentFile){
        var img = $('<a class="view-large-thump" style="background-image: url(\'' + currentFileList[id].getThumpPath() + '\');"></a>');
        img.data("id", id);
        if (id == currentFile){
            img.addClass("view-large-slider-current");
        }
        if (append){
            $("#view-large-slider-content").append(img);
        }
        else{
            $("#view-large-slider-content").prepend(img);
        }

        if ((id < 5 && append) || (id <= 0 && !append)){
            $("#view-large-slider-left").addClass("invisible");
        }
        else{
            $("#view-large-slider-left").removeClass("invisible");
        }

        if ((id == currentFileList.length - 1 && append) ||(id + 4 >= currentFileList.length - 1 && !append)) {
            $("#view-large-slider-right").addClass("invisible");
        }
        else{
            $("#view-large-slider-right").removeClass("invisible");
        }
    }

    function setCurrentFileTo(currentFile) {
        if (currentFileList.length > 0) {
            showAlert(false);
            currentFileIndex = currentFile;
            var fileEntry =  currentFileList[currentFileIndex];
            $("#view-large-normal").attr( "src", fileEntry.getMidPath());
            $("#view-large-normal").data("id", fileEntry.getID());
            if (!fileEntry.name.endsWith("pdf")){
                $("#view-large-normal").addClass("view-large-resizeable");
            }
            else{
                $("#view-large-normal").removeClass("view-large-resizeable");
            }
            $("#view-large-normal").data("id", fileEntry.getID());
            derivateBrowserTools.setFileName(fileEntry.name);
            updatePanelData(fileEntry);
            if (currentFile == 0){
                $("#view-large-left").addClass("hidden");
            }
            else{
                $("#view-large-left").removeClass("hidden");
            }

            if (currentFile == currentFileList.length - 1 ) {
                $("#view-large-right").addClass("hidden");
            }
            else{
                $("#view-large-right").removeClass("hidden");
            }
            setThumbFileTo(currentFile);
        }
        else{
            showAlert(true);
        }
    }

    function updatePanelData(fileEntry) {
        fileEntry.setStatusTo( $("#view-large-panel-collapse"));
        if (fileEntry.selected) {
            $("#view-large-panel").children().addClass("checked");
            $("#view-large-panel").find(".btn-check-large").removeClass("glyphicon-unchecked");
            $("#view-large-panel").find(".btn-check-large").addClass("glyphicon-check");
        }
        else {
            $("#view-large-panel").children().removeClass("checked");
            $("#view-large-panel").find(".btn-check-large").addClass("glyphicon-unchecked");
            $("#view-large-panel").find(".btn-check-large").removeClass("glyphicon-check");
        }
    }

    function changeName(success, deriID, oldname, newName) {
        if (success) {
            var path = oldname.substr(0, oldname.lastIndexOf("/") + 1) + newName;
            var index = getIndexFromID(deriID + path);
            if (currentFileIndex == index) {
                hidePanelInput();
                $("#view-large-panel-title").html(newName);
            }
        }
        else{
            if (!$("#view-large-panel-input").hasClass("hidden")) {
                $("#view-large-panel-input").parent().addClass("has-error");
            }
        }
    }

    function resetLargeView() {
        currentFileList = [];
        fullFileList = [];
        hideLargeView();
    }

    function hideLargeView() {
        currentFileIndex = 0;
        currentSliderIndex = 0;
        $("#view-large-slider-content").html("");
        $("#view-large-normal").attr("src", "");
        $("#view-large-normal").removeData("id");
        $("#file-view-large").addClass("hidden");
    }

    function sortCurrentList(dir){
        currentFileList.sort(function(a, b) {
            var sort = 0;
            if (a.name.toLowerCase() < b.name.toLowerCase()) sort = -1;
            if (a.name.toLowerCase() > b.name.toLowerCase()) sort = 1;
            if (dir == "DESC") sort = sort * -1;
            return sort;
        });
        if (fullFileList != []){
            fullFileList.sort(function(a, b) {
                var sort = 0;
                if (a.name.toLowerCase() < b.name.toLowerCase()) sort = -1;
                if (a.name.toLowerCase() > b.name.toLowerCase()) sort = 1;
                if (dir == "DESC") sort = sort * -1;
                return sort;
            });
        }
    }

    function enlargeorHideImage(x, y) {
        if ($("#view-large-overlay").length > 0){
            $("#view-large-overlay").remove();
        }
        else{
            var img = $('<div id="view-large-overlay"><img id="view-large-large" src=""></div>');
            $("body").append(img);
            $(img).find("#view-large-large").attr( "src", currentFileList[currentFileIndex].getLargePath()).on("load", function() {
                setLargePosition(x, y);
            });
        }
    }

    function setLargePosition(x, y) {
        var top = - ($(window).height() / 2) + ($("#view-large-large").outerHeight() / 2);
        var left = - ($(window).width() / 2) + ($("#view-large-large").outerWidth() / 2);

        if ($("#view-large-large").height() > $(window).height()){
            top = (y  * ($("#view-large-large").outerHeight() / $(window).height())) - (y);
        }
        if ($("#view-large-large").width() > $(window).width()){
            left = (x  * ($("#view-large-large").outerWidth() / $(window).width())) - (x);
        }
        $("#view-large-large").offset({
            top: -top,
            left: -left
        });
    }

    function getIndexFromID(id) {
        //var index = currentFileList.map(function(x) {return x.getID();}).indexOf(id);
        //if (index == -1) index = 0;
        return currentFileList.map(function(x) {return x.getID();}).indexOf(id);
    }

    function hidePanelInput() {
        $("#view-large-panel-input").val("");
        $("#view-large-panel-title").removeClass("hidden");
        $("#view-large-panel-input").addClass("hidden");
        $("#view-large-panel-input").parent().removeClass("has-error");
    }

    function removeFromList(id) {
        var index = getIndexFromID(id);
        if (index != -1){
            currentFileList.splice(index, 1);
            if (!$("#file-view-large").hasClass("hidden")) {
                setCurrentFileTo(0);
            }
        }
    }

    function showAlert(show) {
        if (show){
            $("#view-large-main").addClass("expended-view");
            $("#view-large-bottom").addClass("hidden");
            $("#view-large-normal-wrapper").addClass("hidden");
            $("#view-large-left").addClass("hidden");
            $("#view-large-right").addClass("hidden");
            $("#view-large-panel").addClass("hidden");
            $("#view-large-alert").removeClass("hidden");
        }
        else {
            $("#view-large-main").removeClass("expended-view");
            $("#view-large-bottom").removeClass("hidden");
            $("#view-large-normal-wrapper").removeClass("hidden");
            $("#view-large-left").removeClass("hidden");
            $("#view-large-right").removeClass("hidden");
            $("#view-large-panel").removeClass("hidden");
            $("#view-large-alert").addClass("hidden");
        }
    }

    //ajax Methods


    return {
        //public
        loadViewer: function(id) {
            initialize(id);
        },

        addFileToList: function(largeFileEntry) {
            currentFileList.push(largeFileEntry);
        },

        destroyLargeView: function() {
            resetLargeView();
        },

        resetList: function() {
            currentFileList = [];
            fullFileList = [];
            currentFileIndex = 0;
            currentSliderIndex = 0;
        },

        sortList: function(dir) {
            sortCurrentList(dir);
        },

        filterList: function() {
            if (currentFileList.length > fullFileList.length) {
                fullFileList = currentFileList.slice();
            }
            currentFileList = [];
        },

        addFileToFilteredList: function(fileEntry) {
            currentFileList.push(fullFileList[fullFileList.map(function(x) {return x.getID();}).indexOf(fileEntry)]);
        },

        resetFilteredList: function() {
            if (fullFileList.length > 0){
                currentFileList = fullFileList.slice();
                fullFileList = [];
            }
        },

        getFile: function(id) {
            var index = getIndexFromID(id);
            if (index != -1) {
                return currentFileList[index];
            }
        },

        updateLinks: function(docID) {
            currentFileList[currentFileIndex].linkedDocs.push({"id": docID, "name": docID });
            updatePanelData(currentFileList[currentFileIndex]);
        },

        updateName: function(id, newName) {
            var index = getIndexFromID(id);
            if (index != -1) {
                currentFileList[index].changeName(newName);
            }
        },

        removeFile: function(id) {
            removeFromList(id)
        }
    };
})();