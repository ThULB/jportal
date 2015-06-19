var derivateBrowserTools = (function () {
	
	var currentDocID = "",
		currentPath = "",
        currentFile = "",
        timeOutID = null,
        i18nKeys = [],
        imgLoadingTimer = 0,
        loadingTimer = 0,
        asyncCount = 0,
        asyncCallback = undefined;

    //binds
    $("body").on("click", "#alert-area-close", function () {
        window.clearTimeout(timeOutID);
        $('#alert-area').removeClass("show-alert");
    });

    //private Methods   
	function getPDFImg(img, deriID, path){
        clearTimeout(imgLoadingTimer);
        imgLoadingTimer = setTimeout(function() {
            $(img).siblings(".img-placeholder").attr( "src", jp.baseURL + "images/adobe-logo.svg");
            $(img).siblings(".img-placeholder").removeClass("hidden");
            $(img).addClass("hidden");
        }, 500);
		$(img).attr( "src", jp.baseURL + "img/pdfthumb/" + deriID + path).on("load", function() {
            clearTimeout(imgLoadingTimer);
			$(img).siblings(".img-placeholder").addClass("hidden");
			$(img).removeClass("hidden");
		});
	}

    function getImgWithPath(img, path) {
        clearTimeout(imgLoadingTimer);
        imgLoadingTimer = setTimeout(function() {
            $(img).siblings(".img-placeholder").attr("src", jp.baseURL + "images/file-logo.svg");
            $(img).siblings(".img-placeholder").removeClass("hidden");
            $(img).addClass("hidden");
        }, 500);
        $(img).attr("src", path).on("load", function () {
            clearTimeout(imgLoadingTimer);
            $(img).siblings(".img-placeholder").addClass("hidden");
            $(img).removeClass("hidden");
        });
    }
	
	function getImg(img, deriID, path){
        getImgWithPath(img, jp.baseURL + "servlets/MCRTileCombineServlet/MIN/" + deriID + path);
        //$(img).siblings(".img-placeholder").attr( "src", jp.baseURL + "images/file-logo.svg");
		//$(img).attr( "src", jp.baseURL + "servlets/MCRTileCombineServlet/MIN/" + deriID + path).on("load", function() {
		//	$(img).siblings(".img-placeholder").addClass("hidden");
		//	$(img).removeClass("hidden");
		//});
//		if (count < 6){
//			$.ajax({
//				url: "/servlets/MCRTileCombineServlet/MIN/" + deriID + path,
//				type: "GET",
//				processData : false,
//				success: function(data, textStatus, xhr) {
//					$(img).attr( "src", "/servlets/MCRTileCombineServlet/MIN/" + deriID + path).on("load", function() {
//						$(img).siblings(".img-placeholder").addClass("hidden");
//						$(img).removeClass("hidden");
//					});
//				},
//				error: function(error) {
//		    		setTimeout(function() {
//		    			getImg(img, deriID, path, count + 1);
//					}, 10000);
//				}
//			});
//		}
	}
	
	function goToDocument(docID, path) {
        hideLoadScreen();
        $("#journal-info-linklist").addClass("hidden");
        $("#journal-info-text").addClass("journal-info-text-large");
		if (docID == "" || docID == undefined || (currentDocID.contains("derivate") && !docID.contains("derivate")) || (!currentDocID.contains("derivate") && docID.contains("derivate"))){
			$("#derivat-panel").addClass("hidden");
			$("#derivate-browser").addClass("hidden");
			$("#journal-info").addClass("hidden");
            if (!$("#file-view-large").hasClass("hidden")){
                derivateBrowserLargeView.destroyLargeView();
                $("#file-view").removeClass("hidden");
            }
		}
        if (docID != "" && docID != undefined) {
            var fileName = path.substr(path.lastIndexOf("/") + 1);
            if(fileName.contains(".")){
                path = path.substring(0, path.lastIndexOf("/"));
            }
            else{
                fileName = "";
            }
            setDocIDs(docID, path, fileName);
            derivateBrowserNavigation.goToDocument(docID, path);
            derivateBrowserFileView.showDerivateOrDoc(docID, path, fileName);
            disableButton();
            $.address.path("/" + currentDocID  + currentPath + "/" + currentFile);
        }
        else{
            setDocIDs("", "", "");
            $.address.path("");
        }
        $("#journal-info-button-delete-labelAll").addClass("hidden");
        $("#journal-info-button-delete-label").removeClass("hidden");
        $("#journal-info-button-goToPage").removeClass("hidden");
        $("#journal-info-button-edit").removeClass("hidden");
	}

    function disableButton() {
        var type = currentDocID.match("_(.*?)_")[1];
        switch (type) {
            case "jparticle":
                $("#folder-list-new-button-article").attr("disabled", "");
                $("#folder-list-new-button-derivate").removeAttr("disabled");
                $("#folder-list-new-button-volume").attr("disabled", "");
                break;
            case "derivate":
                $("#folder-list-new-button-volume").attr("disabled", "");
                $("#folder-list-new-button-article").attr("disabled", "");
                $("#folder-list-new-button-derivate").attr("disabled", "");
                break;
            case "jpjournal":
                $("#folder-list-new-button-volume").removeAttr("disabled");
                $("#folder-list-new-button-derivate").removeAttr("disabled");
                $("#folder-list-new-button-article").attr("disabled", "");
                break;
            default :
                $("#folder-list-new-button-article").removeAttr("disabled");
                $("#folder-list-new-button-volume").removeAttr("disabled");
                $("#folder-list-new-button-derivate").removeAttr("disabled");
        }
    }
	
	function setDocIDs(docID, path, filename) {
		currentDocID = docID;
		currentPath = path;
        currentFile = filename;
	}

    function setFile(filename) {
        currentFile = filename;
        $.address.path("/" + currentDocID  + currentPath + "/" + currentFile);
    }

    function showAlert(text, success) {
        $('#alert-area').removeClass("show-alert");
        $("#alert-area").removeClass("alert-success");
        $("#alert-area").removeClass("alert-danger");
        if (timeOutID != null){
            window.clearTimeout(timeOutID);
        }

        $("#alert-area-text").html(text);
        if (success){
            $("#alert-area").addClass("alert-success");
            $("#alert-area").addClass("show-alert");
        }
        else{
            $("#alert-area").addClass("alert-danger");
            $("#alert-area").addClass("show-alert");
        }
        timeOutID = window.setTimeout(function() {
            $('#alert-area').removeClass("show-alert");
        }, 5000);
    }

    function loadI18nKeys(lang) {
        jQuery.getJSON(jp.baseURL + "servlets/MCRLocaleServlet/" + lang + "/db.*", function(data) {
            i18nKeys = data;
            changeAllI18n();
        });
    }

    function changeAllI18n() {
        $("#folder-list-search-input").attr("placeholder", i18nKeys["db.label.search"]);
        updateI18n($("body"));
    }

    function updateI18n(elm) {
        $(elm).find(".i18n").each(function(i, node) {
            var i18nKey = i18nKeys[$(node).attr("i18n")];
            if (i18nKey != undefined){
                $(node).html(i18nKey);
            }
            else{
                $(node).html($(node).attr("i18n-def"));
            }
        });
        $(elm).find(".i18n-title").each(function(i, node) {
            var i18nKey = i18nKeys[$(node).attr("i18n")];
            if (i18nKey != undefined){
                $(node).attr("title", i18nKey);
            }
        });
    }

    function getI18nKey(key) {
        var string = i18nKeys[key];
        if (string != undefined){
            for (var i = 0; i < arguments.length-1; i++){
                string = string.replace(new RegExp('\\{' + i + '\\}', "g"), arguments[i+1]);
            }
            return string;
        }
        else{
            return "";
        }
    }

    function toReadableSize(size, unit) {
        var conSize = convertSize({number: size, unit: unit});
        var unitString = "";
        switch (conSize.unit){
            case 0:
                unitString = "bytes";
                break;
            case 1:
                unitString = "kB";
                break;
            case 2:
                unitString = "MB";
                break;
            case 3:
                unitString = "GB";
                break;
            default:
                unitString = "GB";
                break;
        }
        return conSize.number + " " + unitString;
    }

    function convertSize(sizeAndUnit) {
        if (sizeAndUnit.unit < 3){
            if (sizeAndUnit.number > 1024){
                var size2 = Math.round((sizeAndUnit.number / 1024) * 100)/ 100;
                return convertSize({number: size2, unit: sizeAndUnit.unit + 1});
            }
        }
        return {number: sizeAndUnit.number, unit: sizeAndUnit.unit};
    }

    function addPopover(elm, content) {
        var timer;
        $(elm).popover({
            content: content,
            html: true,
            trigger: "manual"
        }).on("mouseenter", function() {
            var pop = this;
            clearTimeout(timer);
            $(".popover.in").parent().find(".popover-file").popover("hide");
            timer = setTimeout(function() {
                if ($(".popover:hover").length == 0) {
                    $(pop).popover("show");
                }
            }, 400);
        }).on("mouseleave", function() {
            var pop = this;
            clearTimeout(timer);
            setTimeout(function() {
                if (!$(".popover:hover").length) {
                    if (!$(':hover',this).length) {
                        $(pop).popover('hide');
                    }
                }
            }, 200);
        });
    }

    function showDelAlert(fileList) {
        $("#lightbox-alert-delete-list").html("");
        $.each(fileList, function (i, elm) {
            $("#lightbox-alert-delete-list").append("<p>" + elm + "</p>")
        });
        $("#lightbox-alert-delete").modal('show');
    }
    function showDelAlertDocs(fileList) {
        $("#lightbox-alert-delete-docs-list").html("");
        $.each(fileList, function (i, elm) {
            var name = derivateBrowserNavigation.getDocName(elm);
            if (name == undefined) name = elm;
            $("#lightbox-alert-delete-docs-list").append("<p>" + name + "</p>")
        });
        $("#lightbox-alert-delete-docs").modal('show');
    }

    function showLoadScreen() {
        if ($("#filebrowser-loading").hasClass("hidden")) {
            clearTimeout(loadingTimer);
            loadingTimer = setTimeout(function() {
                $("#filebrowser-loading").removeClass("hidden");
            }, 1000);
        }
    }

    function hideLoadScreen() {
        clearTimeout(loadingTimer);
        $("#filebrowser-loading").addClass("hidden");
    }

    return {
        //public   
    	setImgPath: function(img, deriID, path) {
			if (path.endsWith("pdf")){
				getPDFImg(img, deriID, path);
			}
			else{
				getImg(img, deriID, path, 0);
			}
		},

        setImgPathWithPath: function(img, path) {
            getImgWithPath(img, path);
        },
		
		getCurrentDocID: function() {
			return currentDocID;
		},

		getCurrentPath: function() {
			return currentPath;
		},
		
		goTo: function(docID, path) {
			goToDocument(docID, path);
		},

        goToPath: function(path) {
            goToDocument(path.substring(0, path.indexOf("/")), path.substring(path.indexOf("/")));
        },
		
		setIDs: function(docID, path, filename) {
            if (filename == undefined) filename = "";
			setDocIDs(docID, path, filename);
		},

        setFileName: function(filename) {
            setFile(filename);
        },

        alert: function(text, success) {
            showAlert(text, success);
        },

        loadI18n: function(lang) {
            loadI18nKeys(lang)
        },

        updateI18nForElm: function(elm) {
            updateI18n(elm);
        },

        getI18n: function(args) {
           return getI18nKey.apply(this,arguments);
        },

        getReadableSize: function(size, unit) {
            return toReadableSize(size, unit);
        },

        setupPopover: function(elm, content) {
            addPopover(elm, content);
        },

        showDeleteAlert: function(fileList) {
            showDelAlert(fileList);
        },

        showDeleteAlertDocs: function(fileList) {
            showDelAlertDocs(fileList);
        },

        showLoadingScreen: function() {
            showLoadScreen();
        },

        hideLoadingScreen: function () {
            hideLoadScreen();
        },

        newAsyncMonitor: function (count, callback) {
            asyncCount = count;
            asyncCallback = callback;
        },

        doneAsync: function (para) {
            if (--asyncCount < 1){
                asyncCallback(para, "");
            }
        }
    };
})();

if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
}

if (typeof String.prototype.contains !== 'function') {
    String.prototype.contains = function(it) {
        return this.indexOf(it) !== -1;
    };
}