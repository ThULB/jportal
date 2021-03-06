var derivateBrowserUpload = (function () {

    var uploadID = null,
        currentUploadList = [],
        currentUploadID = 0,
        currentDeriID = "",
        currentPath = "",
        uploadedSize = 0,
        uploadFileCount = 0,
        dragcounter = 0,
        overwriteAll = false,
        skipAll = false,
        uploadRunning = false,
        xhr,
        currentTarget,
        derivateBaseURL;

    //binds
    function bindActions() {
        bindUIActions();
        bindEventActions();
    }

    function bindUIActions() {
        $(currentTarget).on("drop", "#lightbox-new-derivate-main", function (event) {
            event.preventDefault();
            event.stopPropagation();
            if (!$("#lightbox-new-derivate-main").hasClass("new-derivate-noDrop")){
                $("#lightbox-new-derivate-hint").addClass("hidden");
                $("#lightbox-new-derivate-loading").removeClass("hidden");
                createDerivate(event);
            }
        });

        $("body").on("drop", "#files", function (event) {
            event.preventDefault();
            if (!$("#upload-overlay").hasClass("hidden")) {
                if (!$("#derivate-browser").hasClass("hidden")){
                    addToDerivate(event);
                }
                else {
                    if (!$("#journal-info-text").hasClass("hidden")){
                        if (!$("#lightbox-new-derivate-main").hasClass("new-derivate-noDrop")) {
                            $("#lightbox-new-derivate-hint").addClass("hidden");
                            $("#lightbox-new-derivate-loading").removeClass("hidden");
                            $(currentTarget).modal("show");
                            createDerivate(event);
                        }
                    }
                }
            }
            dragcounter = 0;
            $("#upload-overlay").addClass("hidden");
        });

        $("body").on("drop", function (event) {
            event.preventDefault();
        });

        $("body").on("dragenter", "#files", function () {
            if (dragcounter == 0 && !$("#derivate-browser").hasClass("hidden")) {
                $("#upload-overlay").removeClass("hidden");
            }
            if (dragcounter == 0 && !$("#journal-info").hasClass("hidden")) {
                $("#upload-overlay-text-add").addClass("hidden");
                $("#upload-overlay-text-new").removeClass("hidden");
                $("#upload-overlay").removeClass("hidden");
            }
            dragcounter++;
        });

        $("body").on("dragleave", "#files", function () {
            dragcounter--;
            if (dragcounter == 0) {
                $("#upload-overlay").addClass("hidden");
                $("#upload-overlay-text-add").removeClass("hidden");
                $("#upload-overlay-text-new").addClass("hidden");
            }
        });

        $(currentTarget).on("click", "#lightbox-new-derivate-confirm", function () {
            if (currentUploadList.length > 0) {
                startUpload("new");
                $("#lightbox-new-derivate-confirm").addClass("hidden");
                $(".lightbox-new-derivate-cancel").addClass("hidden");
                $("#lightbox-new-derivate-message").removeClass("hidden");
                $("#lightbox-new-derivate-main").addClass("new-derivate-noDrop");
            }
            else {
                $("#lightbox-new-derivate-hint").addClass("alert-danger-text");
            }
        });

        $("body").on("click", "#lightbox-new-derivate-done", function () {
            var deriID = $(this).data("deriID");
            if (deriID != "" && deriID != undefined) {
                $("body").trigger("addTempDoc", [deriID, deriID, "derivate", $(this).data("docID")]);
                $("body").trigger("derivateCreated", deriID);
                finishUpload().done(function() {
                    derivateBrowserTools.goTo(deriID, "");  
                });
            }
            resetUpload();
            hideNewDeri();
        });

        $("body").on("click", ".lightbox-new-derivate-cancel", function () {
            resetUpload();
            hideNewDeri();
        });

        $("body").on("click", "#btn-upload-cancel", function () {
            $('#lightbox-upload-overwrite').modal('hide');
            cancelUpload();
        });

        $("body").on("click", "#btn-upload-skip", function () {
            $('#lightbox-upload-overwrite').modal('hide');
            if ($(".btn-upload-all").data("check") == true) {
                skipAll = true;
            }
            skipFile(currentUploadList[currentUploadID]);
        });

        $("body").on("click", ".btn-upload-all", function () {
            if ($(this).data("check") == true) {
                $(this).removeData("check");
                $(this).addClass("glyphicon-unchecked");
                $(this).removeClass("glyphicon-check");
            }
            else {
                $(this).data("check", true);
                $(this).removeClass("glyphicon-unchecked");
                $(this).addClass("glyphicon-check");
            }
        });

        $("body").on("click", "#btn-upload-overwrite", function () {
            var upload = currentUploadList[currentUploadID];
            $('#lightbox-upload-overwrite').modal('hide');
            if ($(".btn-upload-all").data("check") == true) {
                overwriteAll = true;
            }
            uploadFile(upload);
        });

        $('#lightbox-upload-overwrite').on('hidden.bs.modal', function () {
            $(this).data("open", false);
            if ($(this).data("openagain") != undefined) {
                $(this).removeData("openagain");
                $('#lightbox-upload-overwrite').modal("show");
            }
        });

        $('#lightbox-upload-overwrite').on('shown.bs.modal', function () {
            $(this).data("open", true);
        });

        $("body").on("click", "#upload-status-bar-cancel", function () {
            xhr.abort();
            cancelUpload();
        });

        $("body").on("click", ".btn-close-usb", function () {
            $("#upload-status-bar").animate({'height': '0px'}, 500, function () {
                $("#upload-status-bar").addClass("hidden");
                $("#upload-status-bar-table").html("");
                $("#upload-status-bar-header-error").addClass("hidden");
                $("#upload-status-bar-header-type").addClass("hidden");
                $("#upload-complete-status-current-number").html(0);
                $("#upload-complete-status-file-count").html(0);
                $(".statusbar-complete-progress-status").attr("arial-now", 0);
                $(".statusbar-complete-progress-status").css("width", 0 + "%");
                $(".upload-statusbar-text").addClass("hidden");
                $("#upload-complete-status-text").removeClass("hidden");
                resetUpload();
            });
        });

        $("body").on("click", ".btn-mini-usb", function () {
            if ($(this).data("status") == "maxi") {
                $(this).data("status", "mini");
                $("#upload-status-bar").animate({'height': '32px'}, 500, function () {
                    $("#upload-status-bar-table").addClass("hidden");
                });
            }
            else {
                $(this).data("status", "maxi");
                $("#upload-status-bar-table").removeClass("hidden");
                $("#upload-status-bar").animate({'height': '300px'}, 500);
            }
        });

        $("body").on("click", "#folder-list-new-button-derivate", function () {
            $(currentTarget).modal("show");
            $("#folder-list-new-select-area").addClass("hidden");
        });

        $(window).on('beforeunload', function(){
            if(uploadRunning) {
                return 'If you leave this page, your upload will not complete! Continue?';
            }
        });
    }

    function bindEventActions() {
        $("body").on("openUploadModal", function () {
            $(currentTarget).modal("show");
        });
    }
    
    //private Methods
    /**
    * @property isFile
    * @property isDirectory
    */
    function getWebkitFiles(item, path) {
        path = path || "";
        if (item.isFile) {
            item.file(function (file) {
                addFileToUplodlist(file, path);
            });
        }
        if (item.isDirectory) {
            //noinspection JSUnresolvedFunction
            var dirReader = item.createReader();
            //noinspection JSUnresolvedFunction
            dirReader.readEntries(function (entries) {
                for (var i = 0; i < entries.length; i++) {
                    getWebkitFiles(entries[i], path + "/" + item.name);
                }
            });
        }
    }

    function addFileToUplodlist(file, path) {
        uploadFileCount--;
        if (file.type != "") {
            var upload = new UploadEntry(derivateBrowserTools.getCurrentDocID(), "", derivateBrowserTools.getCurrentPath() + path, file);
            if (path != "") upload.isInFolder();
            if (!isAlreadyInList(upload)) {
                upload.exists = false;
                currentUploadList.push(upload);
            }
            else {
                console.log("already in there");
            }
            if (uploadFileCount == 0){
                showStatus();
            }
        }
        else {
            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.filetype.file"), false);
        }
    }

    function isAlreadyInList(entry) {
        for (var i = 0, len = currentUploadList.length; i < len; i++) {
            if (currentUploadList[i].getID() == entry.getID()) return true
        }
        return false;
    }

    function getFileFromArray(id) {
        for (var i = 0, len = currentUploadList.length; i < len; i++) {
            if (currentUploadList[i].getID() == id) return currentUploadList[i];
        }
        return undefined
    }

    function updateStatus(){
        $("#lightbox-new-derivate-current-number").html(currentUploadID + 1);
        $("#lightbox-new-derivate-file-count").html(currentUploadList.length);
        $("#lightbox-new-derivate-current-name").html(currentUploadList[currentUploadID].getCompletePath());
        $("#lightbox-new-derivate-complete-single-size").html(currentUploadList[currentUploadID].size);
        $(".statusbar-progress-single-status").addClass('noAnimation');
        $(".statusbar-progress-single-status").attr("arial-now", 0);
        $(".statusbar-progress-single-status").css("width", 0 + "%");
    }

    function showStatus(){
        var template = $("#upload-entry-template3").html();
        var status = $(Mustache.render(template, this));
        derivateBrowserTools.updateI18nForElm(status);
        $("#lightbox-new-derivate-status").html(status);
        $("#lightbox-new-derivate-complete-size").html(getCompleteSize());
        $("#lightbox-new-derivate-current-size").html(0);
        $("#lightbox-new-derivate-current-single-size").html(0);
        updateStatus();
        $("#lightbox-new-derivate-loading").addClass("hidden");
        $("#lightbox-new-derivate-status").removeClass("hidden");
    }


    function getCompleteSize() {
        var size = 0;
        for (var i=0, len=currentUploadList.length; i<len; i++) {
            size = size + currentUploadList[i].rawSize;
        }
        return derivateBrowserTools.getReadableSize(size, 0);
    }

    function resetUpload() {
      finishUpload().done(function() {
        uploadID = null;
        currentUploadList = [];
        currentUploadID = 0;
        currentDeriID= "";
        currentPath = "";
        uploadedSize = 0;
        uploadFileCount = 0;
        overwriteAll = false;
        skipAll = false;
        uploadRunning = false;
      }).fail(function(err) {
        console.log(err);
      });
    }

    function hideNewDeri() {
        $(currentTarget).modal("hide");
        $("#lightbox-new-derivate-hint").removeClass("hidden");
        $("#lightbox-new-derivate-hint").removeClass("alert-danger-text");
        $("#lightbox-new-derivate-status").html("");
        $("#lightbox-new-derivate-status").addClass("hidden");
        $("#lightbox-new-derivate-main").removeClass("new-derivate-noDrop");
        $("#lightbox-new-derivate-confirm").removeClass("hidden");
        $(".lightbox-new-derivate-cancel").removeClass("hidden");
        $("#lightbox-new-derivate-done").addClass("hidden");
        $("#lightbox-new-derivate-done").removeData();
        $("#lightbox-new-derivate-error").addClass("hidden");
        $("#lightbox-new-derivate-message").addClass("hidden");
    }

    function existsCheck() {
        let derivId = derivateBrowserTools.getCurrentDocID();
        let path = derivateBrowserTools.getCurrentPath();
        let derivateFiles = new DerivateFiles();

        for (var i=0, len=currentUploadList.length; i<len; i++) {
            /* currentUploadList
            [
                {
                    "uploadID":null,
                    "docID":"jportal_derivate_00000004",
                    "deriID":"jportal_derivate_00000004",
                    "path":"",
                    "name":"TG571023.JPG",
                    "size":"6.96 MB",
                    "rawSize":7301024,
                    "type":"image/jpeg",
                    "lastmodified":"07.03.2020 23:42:32",
                    "file":{},
                    "statusbar":{"0":{},"length":1},"inFolder":false,"md5":""
                 }
            ]
            * */
            if (currentUploadList[i].exists == undefined) {
                let fileName = currentUploadList[i].getCheckJson().file;
                let currentFile = new File(derivId, path + "/" + fileName);
                derivateFiles.add(currentFile);
            }
        }
        if (derivateFiles.getFiles().length > 0) {
            doExistsCheck(derivateFiles);
        }
        else {
            if (!uploadRunning) {
                cancelUpload();
            }
        }
    }

    function addDataToFileList(data) {
        /* data
        [
            {
                "file":"TG571023.JPG",
                "id":"jportal_derivate_00000004/TG571023.JPG",
                "fileType":"image/jpeg",
                "exists":"1",
                "existingFile":{"name":"TG571023.JPG","size":7301024,"lastmodified":"11.03.2020 09:09:17"}
            }
        ]
        * */

        /* new
        [
            {
                "derivId":"jportal_derivate_00000004",
                "exists":1,
                "lastModifiedTime":"11.03.2020 09:09:17",
                "path":"/TG571023.JPG",
                "size":7301024,
                "status":0,
                "type":""
             }
        ]
        * */
        for (var i = 0, len = data.length; i < len; i++) {
            var checkedFile = data[i];
            let fileArrayID = checkedFile.derivId + checkedFile.path;
            var file = getFileFromArray(fileArrayID);
            /*
            {"uploadID":null,
            "docID":"jportal_derivate_00000004",
            "deriID":"jportal_derivate_00000004",
            "path":"","name":"TG571023.JPG","size":"6.96 MB",
            "rawSize":7301024,"type":"image/jpeg",
            "lastmodified":"07.03.2020 23:42:32","file":{},
            "statusbar":{"0":{},"length":1},"inFolder":false,"md5":""}
            * */
            if (file != undefined){
                file.exists = checkedFile.exists;
                file.checkedFile = checkedFile;
            }
        }
    }

    function uploadFilesAndAsk() {
        if (currentUploadID < currentUploadList.length) {
            var upload = currentUploadList[currentUploadID];
            if (upload.exists == "1" && skipAll) {
                skipFile(upload);
                return false;
            }
            if (upload.exists == "1" && !overwriteAll) {
                upload.askOverwrite(upload.checkedFile, currentDeriID, currentPath);
            }
            if (upload.exists == "0" || overwriteAll) {
                uploadFile(upload);
            }
        }
        else {
            scrollToElement($(".upload-entry").last());
            $(".upload-statusbar-text").addClass("hidden");
            $("#upload-complete-status-done").removeClass("hidden");
            uploadRunning = false;
            toggleProgress();
            resetUpload();
            $(".btn-close-usb").removeClass("hidden");
        }
    }

    function showUploadBar() {
        $("#upload-status-bar-table").removeClass("hidden");
        $("#upload-status-bar").removeClass("hidden");
        $("#upload-status-bar").animate({'height': '300px'}, 500);
        $(".btn-mini-usb").data("status", "maxi");
    }

    function cancelUpload(from) {
        if (from == undefined || from < currentUploadID){
            from = currentUploadID;
        }
        for (var i = from, len = currentUploadList.length; i < len; i++) {
            var percentComplete = Math.floor((i + 1) * (100 / currentUploadList.length));
            $(currentUploadList[i].statusbar).find(".upload-preview-status").html(derivateBrowserTools.getI18n("db.label.upload.canceled"));
            $(".statusbar-complete-progress-status").attr("arial-now", percentComplete);
            $(".statusbar-complete-progress-status").css("width", percentComplete + "%");
            $("#upload-complete-status-current-number").html(i + 1);
            $("#upload-complete-status-file-count").html(currentUploadList.length);
        }
        $("#upload-status-bar-cancel").addClass("hidden");
        $(".upload-statusbar-text").addClass("hidden");
        $("#upload-complete-status-canceled").removeClass("hidden");
        $(".btn-close-usb").removeClass("hidden");
        resetUpload();
    }

    function skipFile(upload) {
        currentUploadID++;
        var percentComplete = Math.floor((currentUploadID) * (100 / currentUploadList.length));
        $(".statusbar-complete-progress-status").attr("arial-now", percentComplete);
        $(".statusbar-complete-progress-status").css("width", percentComplete + "%");
        $("#upload-complete-status-current-number").html(currentUploadID);
        $("#upload-complete-status-file-count").html(currentUploadList.length);
        $(upload.statusbar).find(".upload-preview-status").html(derivateBrowserTools.getI18n("db.label.upload.skipped"));
        uploadFilesAndAsk();
    }

    function toggleProgress() {
        $("#upload-status-bar-cancel").toggleClass("hidden");
    }

    function resetProgressBar() {
        $(".statusbar-complete-progress-status").addClass('noAnimation');
        $(".statusbar-complete-progress-status").attr("arial-now", 0);
        $(".statusbar-complete-progress-status").css("width", 0 + "%");
        $(".upload-statusbar-text").addClass("hidden");
        $("#upload-complete-status-text").removeClass("hidden");
    }

    function scrollToElement(elm) {
        var animationTime = 100;
        if ($("#upload-status-bar-body").queue( "fx").length > 1) {
            $("#upload-status-bar-body").stop(true);
            animationTime = 1;
        }
        $("#upload-status-bar-body").animate({
            scrollTop: $(elm).index() * $(elm).outerHeight()
        }, animationTime);
    }

    /**
    * @property dataTransfer
    */
    function addToDerivate(dropEvent) {
        currentDeriID = derivateBrowserTools.getCurrentDocID();
        currentPath = derivateBrowserTools.getCurrentPath();
        var files = dropEvent.originalEvent.dataTransfer.files;
        showUploadBar();
        $.each(files, function (i, file) {
            if (file.type != "") {
                var upload = new UploadEntry(derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath(), file);
                if (!isAlreadyInList(upload)){
                    upload.statusbar = upload.getStatus();
                    $("#upload-status-bar-table").append(upload.statusbar);
                    currentUploadList.push(upload);
                }
            }
            else {
                $("#upload-status-bar-header-type").removeClass("hidden");
                $("#upload-status-bar-header-type-file").addClass("hidden");
                $("#upload-status-bar-header-type-folder").removeClass("hidden");
                derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.filetypeFolder"), false);
            }
        });
        if (!uploadRunning) {
            toggleProgress();
            resetProgressBar();
            $(".btn-close-usb").addClass("hidden");
        }
        else {
            $("#upload-complete-status-current-number").html(currentUploadID);
            $("#upload-complete-status-file-count").html(currentUploadList.length);
        }
        existsCheck();
    }

    function createDerivate(dropEvent) {
        var files = dropEvent.originalEvent.dataTransfer.items;
        if (files != undefined) {
            uploadFileCount = files.length;
            for (var i = 0; i < files.length; ++i) {
                //noinspection JSUnresolvedFunction
                var entry = files[i].webkitGetAsEntry();
                if (entry != null) {
                    getWebkitFiles(entry);
                }
            }
        }
        else {
            files = dropEvent.originalEvent.dataTransfer.files;
            uploadFileCount = files.length;
            $.each(files, function (i, file) {
                addFileToUplodlist(file, "");
            });
        }
    }

    function showUploadAlert(upload, mode, error) {
        $(upload.statusbar).find("lightbox-new-derivate-error").removeClass("hidden");
        $(upload.statusbar).find(".upload-error").attr("title", derivateBrowserTools.getI18n("db.label.upload.error"));
        $(upload.statusbar).find(".upload-preview-status").html(derivateBrowserTools.getI18n("db.label.upload.error"));
        $(upload.statusbar).addClass("progress-bar-danger");
        if (mode == "new") {
            $("#lightbox-new-derivate-error").removeClass("hidden");
            if (error == 401) {
                $(".lightbox-new-derivate-error-permission").removeClass("hidden");
            }
            else {
                $(".lightbox-new-derivate-error-default").removeClass("hidden");
            }
            $("#lightbox-new-derivate-done").removeClass("hidden");
            $("#lightbox-new-derivate-message").addClass("hidden");
            $("#lightbox-new-derivate-done").data("deriID", currentDeriID);
            $("#lightbox-new-derivate-done").data("docID", "");
            $(".statusbar-progress-single-status").addClass("progress-bar-danger");
            $(".statusbar-progress-complete-status").addClass("progress-bar-danger");
            $(".statusbar-progress-single-status").html(derivateBrowserTools.getI18n("db.label.upload.error"));
            $(".statusbar-progress-complete-status").html(derivateBrowserTools.getI18n("db.label.upload.error"));
        }
        if (mode != "new") {
            $("#upload-status-bar-header-type").addClass("hidden");
            $("#upload-status-bar-header-error").removeClass("hidden");
            cancelUpload(currentUploadID + 1);
        }
    }

    function startUpload(mode) {
      var docID = derivateBrowserTools.getCurrentDocID();
      var derID = currentDeriID;
      var num = currentUploadList.length;
      var url = derivateBaseURL + "startUpload" +
      		"?documentID=" + docID +
      		"&derivateID=" + derID +
      		"&num=" + num;
      $.post(url).done(function(res) {
        uploadID = res.uploadID;
        $(window).unload(function() {
          finishUpload();
        });
        if(mode == "new") {
          uploadFile(currentUploadList[0], mode);
        } else {
          uploadFilesAndAsk();
        }
      }).fail(function(err) {
        console.log(err);
      });
    }

    function finishUpload() {
      if(uploadID != null) {
        var url = derivateBaseURL + "finishUpload?uploadID=" + uploadID
        return $.post(url);        
      }
      return $.Deferred().resolve();
    }

    //ajax Methods
    /**
     * @property lengthComputable
     * @property loaded
     * @property total
     */
    function uploadFile(upload, mode) {
        upload.setUploadID(uploadID);
        if (mode == "new") {
            updateStatus();
        } else {
            scrollToElement(upload.statusbar);
            $("#upload-complete-status-current-number").html(currentUploadID + 1);
            $("#upload-complete-status-file-count").html(currentUploadList.length);
            $(upload.statusbar).find(".statusbar-progress").removeClass("hidden");
            $(upload.statusbar).find(".upload-preview-status-text").addClass("hidden");
        }
        $.ajax({
            url: derivateBaseURL + "upload",
            type: "POST",
            processData: false,
            contentType: false,
            data: upload.getFormData(),
            xhr: function () {
                xhr = new window.XMLHttpRequest();
                xhr.upload.addEventListener("progress", function (evt) {
                    if (evt.lengthComputable) {
                        $(".statusbar-progress-single-status").removeClass('noAnimation');
                        var percentSingle = Math.floor((evt.loaded / evt.total) * 100);
                        $("#lightbox-new-derivate-current-single-size").html(derivateBrowserTools.getReadableSize(evt.loaded, 0));
                        $(".statusbar-progress-single-status").attr("arial-now", percentSingle);
                        $(".statusbar-progress-single-status").css("width", percentSingle + "%");
                        $(".statusbar-progress-single-status").html(percentSingle + '%');

                        var percentComplete = Math.floor((currentUploadID) * (100 / currentUploadList.length) + (percentSingle / currentUploadList.length));
                        $("#lightbox-new-derivate-current-size").html(derivateBrowserTools.getReadableSize(uploadedSize + evt.loaded, 0));
                        $(".statusbar-progress-complete-status").attr("arial-now", percentComplete);
                        $(".statusbar-progress-complete-status").css("width", percentComplete + "%");
                        $(".statusbar-progress-complete-status").html(percentComplete + '%');


                        $(upload.statusbar).find(".statusbar-progress-status").attr("arial-now", percentSingle);
                        $(upload.statusbar).find(".statusbar-progress-status").css("width", percentSingle + "%");
                        $(upload.statusbar).find(".statusbar-progress-status").html(percentSingle + '%');

                        $(".statusbar-complete-progress-status").attr("arial-now", percentComplete);
                        $(".statusbar-complete-progress-status").css("width", percentComplete + "%");
                    }
                }, false);
                xhr.upload.addEventListener("load", function (evt) {
                    if (evt.lengthComputable) {
                        uploadedSize = uploadedSize + evt.total;
                    }
                }, false);
                return xhr;
            },
            success: function (data) {
                upload.md5 = data.md5;
                $(upload.statusbar).find(".upload-success").removeClass("hidden");
                $(upload.statusbar).find(".upload-success").attr("title", derivateBrowserTools.getI18n("db.alert.upload.success"));
                $(upload.statusbar).find(".upload-preview-status").html(derivateBrowserTools.getI18n("db.alert.upload.success"));
                currentUploadID++;
                if (mode != "new") {
                    if (upload.exists == "1") {
                        $("body").trigger("removeFileWithPath", upload.getCompletePath());
                    }
                    if (upload.type.endsWith("xml")){
                        $("body").trigger("addXML", upload.getaddToBrowserJson());
                    }
                    else{
                        $("body").trigger("addFile", upload.getaddToBrowserJson());
                    }
                    uploadFilesAndAsk();
                }
                if (mode == "new") {
                    currentDeriID = data.derivateID;
                    if (currentUploadID < currentUploadList.length){
                        var nextUpload = currentUploadList[currentUploadID];
                        nextUpload.deriID = currentDeriID;
                        uploadFile(nextUpload, "new");
                    }
                    else{
                        $("#lightbox-new-derivate-done").removeClass("hidden");
                        $("#lightbox-new-derivate-message").addClass("hidden");
                        $("#lightbox-new-derivate-done").data("deriID", currentDeriID);
                        $("#lightbox-new-derivate-done").data("docID", upload.docID);
                    }
                }
            },
            error: function (error) {
                if (error.status == 0){
                    $(upload.statusbar).find(".upload-preview-status").html(derivateBrowserTools.getI18n("db.label.upload.canceled"));
                }
                else {
                    if (error.status == 415) {
                        var msg = derivateBrowserTools.getI18n("db.alert.filetype.file");
                        derivateBrowserTools.alert(msg, false);
                        $(upload.statusbar).find(".upload-type").removeClass("hidden");
                        $(upload.statusbar).find(".upload-type").attr("title", msg);
                        $(upload.statusbar).find(".upload-preview-status").html(msg);
                        $(upload.statusbar).addClass("progress-bar-warning");
                        if (mode == "new") {
                            $("#lightbox-new-derivate-error").removeClass("hidden");
                            $("#lightbox-new-derivate-done").removeClass("hidden");
                            $("#lightbox-new-derivate-message").addClass("hidden");
                            $("#lightbox-new-derivate-done").data("deriID", currentDeriID);
                            $("#lightbox-new-derivate-done").data("docID", "");
                            $(".statusbar-progress-single-status").addClass("progress-bar-warning");
                            $(".statusbar-progress-complete-status").addClass("progress-bar-warning");
                            $(".statusbar-progress-single-status").html(msg);
                            $(".statusbar-progress-complete-status").html(msg);
                        }
                        if (mode != "new") {
                            if ($("#upload-status-bar-header-error").hasClass("hidden")) {
                                $("#upload-status-bar-header-type").removeClass("hidden");
                                $("#upload-status-bar-header-type-file").removeClass("hidden");
                                $("#upload-status-bar-header-type-folder").addClass("hidden");
                            }
                            cancelUpload(currentUploadID + 1);
                        }
                    }
                    else {
                        if (error.status == 401) {
                            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
                            showUploadAlert(upload, mode, error.status);
                        }
                        else {
                            derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.upload.error"), false);
                            showUploadAlert(upload, mode, error.status);
                        }
                    }
                }
            }
        });
    }

    function doExistsCheck(json) {
        $.ajax({
            url: derivateBaseURL + "exists",
            type: "POST",
            contentType: 'application/json',
            dataType: "json",
            data: JSON.stringify(json),
            statusCode: {
                200: function (data) {
                    /*
                    {"deriID":"jportal_derivate_00000004","path":"","files":[{"file":"TG571023.JPG","id":"jportal_derivate_00000004/TG571023.JPG","fileType":"image/jpeg","exists":"1","existingFile":{"name":"TG571023.JPG","size":7301024,"lastmodified":"11.03.2020 09:09:17"}}]}
                    * */
                    addDataToFileList(data.files);
                    if (!uploadRunning) {
                        uploadRunning = true;
                        startUpload();
                    }
                },
                500: function () {
                    derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.upload.error"), false);
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
            currentTarget = $("#lightbox-new-derivate");
            derivateBaseURL = "";
            bindActions();
        },

        initStandalone: function(target, baseURL) {
            $.get(baseURL + "rsc/derivatebrowser/gui/derivatebrowser.html", function (template) {
                $(target).html($(template).filter("#lightbox-new-derivate").html());
                derivateBrowserTools.updateI18nForElm($(target));
                currentTarget = target;
                var currentDocID = $(currentTarget).attr("data-docid");
                var currentPath = $(currentTarget).attr("data-path") ? (currentTarget).attr("data-path") : "";
                derivateBrowserTools.setDocID(currentDocID);
                derivateBrowserTools.setPath(currentPath);
                derivateBaseURL = baseURL + "rsc/derivatebrowser/";
                if (currentDocID != undefined && currentDocID != "") {
                    bindActions();
                }
            });
        }
    };
})();
