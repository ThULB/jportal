var derivateBrowserTools = (function () {
	
	var currentDocID = "",
		currentPath = "",
        timeOutID = null,
        i18nKeys =[];


    //private Methods   
	function getPDFImg(img, deriID, path){
		$(img).siblings(".img-placeholder").attr( "src", jp.baseURL + "images/adobe-logo.svg");
		$(img).attr( "src", jp.baseURL + "img/pdfthumb/" + deriID + path).on("load", function() {
			$(img).siblings(".img-placeholder").addClass("hidden");
			$(img).removeClass("hidden");
		});
	}
	
	function getImg(img, deriID, path){
        $(img).siblings(".img-placeholder").attr( "src", jp.baseURL + "images/file-logo.svg");
		$(img).attr( "src", jp.baseURL + "servlets/MCRTileCombineServlet/MIN/" + deriID + path).on("load", function() {
			$(img).siblings(".img-placeholder").addClass("hidden");
			$(img).removeClass("hidden");
		});
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
        $("#journal-info-linklist").addClass("invisible");
		if (docID == "" || docID == undefined || (currentDocID.contains("derivate") && !docID.contains("derivate")) || (!currentDocID.contains("derivate") && docID.contains("derivate"))){
			$("#derivat-panel").addClass("hidden");
			$("#derivate-browser").addClass("hidden");
			$("#journal-info").addClass("hidden");
		}
        if (docID != "" && docID != undefined) {
            setDocIDs(docID, path);
            derivateBrowserNavigation.goToDocument(docID, path);
            derivateBrowserFileView.showDerivateOrDoc(docID, path);
            disableButton();
            $.address.path("/" + currentDocID  + currentPath + "/");
        }
        else{
            setDocIDs("", "");
            $.address.path("");
        }
	}

    function disableButton() {
        var type = currentDocID.match("_(.*?)_")[1];
        switch (type) {
            case "jparticle":
                $("#folder-list-new-button-article").removeAttr("disabled");
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
	
	function setDocIDs(docID, path) {
		currentDocID = docID;
		currentPath = path;
	}

    function showAlert(text, success) {
        $('#alert-area').removeClass("show-alert");
        $("#alert-area").removeClass("alert-success");
        $("#alert-area").removeClass("alert-danger");
        if (timeOutID != null){
            window.clearTimeout(timeOutID);
        }

        $("#alert-area").html(text);
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
        $(".i18n").each(function(i, elm) {
            var i18nKey = i18nKeys[$(elm).attr("i18n")];
            if (i18nKey != undefined){
                $(elm).html(i18nKey);
            }
            else{
                $(elm).html($(elm).attr("i18n-def"));
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
		
		getCurrentDocID: function() {
			return currentDocID;
		},

		getCurrentPath: function() {
			return currentPath;
		},
		
		goTo: function(docID, path) {
			goToDocument(docID, path);
		},
		
		setIDs: function(docID, path) {
			setDocIDs(docID, path);
		},

        alert: function(text, success) {
            showAlert(text, success);
        },

        loadI18n: function(lang) {
            loadI18nKeys(lang)
        },

        getI18n: function(args) {
           return  getI18nKey.apply(this,arguments);
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