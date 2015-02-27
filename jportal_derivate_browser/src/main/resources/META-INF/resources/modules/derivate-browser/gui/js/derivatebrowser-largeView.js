var derivateBrowserLargeView = (function () {

    var currentFileList = [],
        fullFileList = [],
        currentFileIndex = 0,
        currentSliderIndex = 0,
        oldPosX = 0,
        oldPosY= 0;

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
        currentFileIndex = $(this).data("id");
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
        oldPosX = evt.pageX;
        oldPosY = evt.pageY;
        $("body").append('<div id="view-large-overlay"><img id="view-large-large" src="' + currentFileList[currentFileIndex].getLargePath() + '"></div>');
    });

    $("body").on("click", "#view-large-overlay", function () {
        $("#view-large-overlay").remove();
    });

    $("body").on("mousemove","#view-large-overlay", function (evt) {
        $("#view-large-large").offset({
            top: $("#view-large-large").offset().top + (oldPosY - evt.pageY),
            left: $("#view-large-large").offset().left + (oldPosX - evt.pageX)
        }, 1);
        oldPosX = evt.pageX;
        oldPosY = evt.pageY;
    });

    $("body").on("click", "#button-view-large-close", function () {
        hideLargeView();
        $("#file-view").removeClass("hidden");
        derivateBrowserTools.setFileName("");
        //derivateBrowserTools.goTo(derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath());
    });

    //private Methods
    function initialize(id){
        //fillThumbSlider();
        if (id == "" || id == null){
            setCurrentFileTo(0);
        }
        else{
            var index = currentFileList.map(function(x) {return x.getID();}).indexOf(id);
            if (index == -1) index = 0;
            currentFileIndex = index;
            setCurrentFileTo(index);
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
        var img = $('<a class="view-large-thump" style="background-image: url(' + currentFileList[id].getThumpPath() + ');"></a>');
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
        fileEntry =  currentFileList[currentFile];
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
        currentFileList[currentFile].setStatusTo( $("#view-large-panel-collapse"));
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
            fullFileList = currentFileList.slice();
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
        }
    };
})();