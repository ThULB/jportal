var derivateBrowserTools = (function() {

  var currentDocID = "",
    currentPath = "",
    currentFile = "",
    currentMode = "full",
    timeOutID = null,
    i18nKeys = [],
    imgLoadingTimer = 0,
    loadingTimer = 0,
    asyncCount = 0,
    asyncCallback = undefined,
    qpara = [], hash;

  //binds
  $("body").on("click", "#alert-area-close", function() {
    window.clearTimeout(timeOutID);
    $('#alert-area').removeClass("show-alert");
  });

  $("body").on("mouseenter", ".mightOverflow", function() {
    var $this = $(this);

    if (this.offsetWidth < this.scrollWidth && !$this.attr('title')) {
      $this.attr('title', $this.text());
    }
  });

  $("body").on("click", "#lightbox-alert-deleteDoc-confirm", function() {
    var json = [];
    json.push({"objId": derivateBrowserTools.getCurrentDocID()});
    derivateBrowserTools.showLoadingScreen();
    if (currentMode == "compact") {
      $("#lightbox-alert-deleteDoc").modal('hide');
      getParentID(derivateBrowserTools.getCurrentDocID(), deleteDocAndGoToParent);
    }
    else {
      deleteDocument(json, removeFromView);
      $("#lightbox-alert-deleteDoc").modal('hide');
    }
  });

  $("body").on("click", "#btn-close", function() {
    var docID = derivateBrowserTools.getCurrentDocID();
    if (docID.contains("derivate")) {
      getParentID(docID, redirectToParent);
    }
    else {
      redirectToParent(docID);
    }
  });

  //private Methods
  function getPDFImg(img, deriID, path) {
    clearTimeout(imgLoadingTimer);
    imgLoadingTimer = setTimeout(function() {
      $(img).siblings(".img-placeholder").attr("src", jp.baseURL + "images/adobe-logo.svg");
      $(img).siblings(".img-placeholder").removeClass("hidden");
      $(img).addClass("hidden");
    }, 500);
    $(img).attr("src", jp.baseURL + "img/pdfthumb/" + deriID + path).on("load", function() {
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
    $(img).attr("src", path).on("load", function() {
      clearTimeout(imgLoadingTimer);
      $(img).siblings(".img-placeholder").addClass("hidden");
      $(img).removeClass("hidden");
    });
  }

  function getImg(img, deriID, path) {
    getImgWithPath(img, jp.baseURL + "servlets/MCRTileCombineServlet/MIN/" + deriID + path);
    // getImgWithPath(img, jp.baseURL + "rsc/iiif/image/Iview/" + deriID + path.replace("/", "%2F") + "/full/400,/0/color.jpg");
    $(img).data("deriID", deriID);
    $(img).data("path", path);
  }

  function goToDocument(docID, path) {
    hideLoadScreen();
    $("#journal-info-linklist").addClass("hidden");
    $("#btn-close").removeClass("hidden");
    $("#journal-info-text").addClass("journal-info-text-large");
    if (docID == "" || docID == undefined || (currentDocID.contains("derivate") && !docID.contains("derivate")) || (!currentDocID.contains("derivate") && docID.contains("derivate"))) {
      $("#derivat-panel").addClass("hidden");
      $("#derivate-browser").addClass("hidden");
      $("#journal-info").addClass("hidden");
      if (!$("#file-view-large").hasClass("hidden")) {
        $("body").trigger("destroyLargeView");
        $("#file-view").removeClass("hidden");
      }
    }
    if (docID != "" && docID != undefined) {
        if (docID.contains("derivate")) {
            checkFileType(docID, path, setAndLoadDoc)
        }
        else {
            setAndLoadDoc(docID, path, "");
        }
    }
    else {
      setDocIDs("", "", "");
      $.address.path("");
    }
    $("#journal-info-button-delete-labelAll").addClass("hidden");
    $("#journal-info-button-delete-label").removeClass("hidden");
    $("#journal-info-button-goToPage").removeClass("hidden");
    $("#journal-info-button-edit").removeClass("hidden");
  }

  function setAndLoadDoc(docID, path, fileName) {
      setDocIDs(docID, path, fileName);
      $("body").trigger("goToDocument", [docID, path]);
      if (docID.contains("derivate")) {
          $("body").trigger("showDerivate", [docID, path, fileName]);
      }
      else {
          $("body").trigger("showDoc", docID);
      }
      disableButton();
      $.address.path("/" + currentDocID + currentPath + "/" + currentFile);
  }

  function disableButton() {
    let type = currentDocID.match("_(.*?)_")[1];
    let newArticle = $("#folder-list-new-button-article");
    let newVolume = $("#folder-list-new-button-volume");
    let newDerivate = $("#folder-list-new-button-derivate");
    switch (type) {
      case "jparticle":
        newArticle.attr("disabled", "");
        newDerivate.removeAttr("disabled");
        newVolume.attr("disabled", "");
        break;
      case "derivate":
        newVolume.attr("disabled", "");
        newArticle.attr("disabled", "");
        newDerivate.attr("disabled", "");
        break;
      case "jpjournal":
        newVolume.removeAttr("disabled");
        newDerivate.removeAttr("disabled");
        newArticle.attr("disabled", "");
        break;
      default :
        newArticle.removeAttr("disabled");
        newVolume.removeAttr("disabled");
        newDerivate.removeAttr("disabled");
    }
  }

  function setDocIDs(docID, path, filename) {
    currentDocID = docID;
    currentPath = path;
    currentFile = filename;
  }

  function setFile(filename) {
    currentFile = filename;
    $.address.path("/" + currentDocID + currentPath + "/" + currentFile);
  }

  function showAlert(text, success) {
    let alertArea = $('#alert-area');
    alertArea.removeClass("show-alert");
    alertArea.removeClass("alert-success");
    alertArea.removeClass("alert-danger");
    if (timeOutID != null) {
      window.clearTimeout(timeOutID);
    }

    $("#alert-area-text").html(text);
    if (success) {
      alertArea.addClass("alert-success");
      alertArea.addClass("show-alert");
    }
    else {
      alertArea.addClass("alert-danger");
      alertArea.addClass("show-alert");
    }
    timeOutID = window.setTimeout(function() {
      alertArea.removeClass("show-alert");
    }, 5000);
  }

  function loadI18nKeys(lang) {
    jQuery.getJSON(jp.baseURL + "rsc/locale/translate/" + lang + "/db.*", function(data) {
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
      let i18nKey = i18nKeys[$(node).attr("i18n")];
      if (i18nKey != undefined) {
        $(node).html(i18nKey);
      }
      else {
        $(node).html($(node).attr("i18n-def"));
      }
    });
    $(elm).find(".i18n-title").each(function(i, node) {
      let i18nKey = i18nKeys[$(node).attr("i18n")];
      if (i18nKey != undefined) {
        $(node).attr("title", i18nKey);
      }
    });
    $(elm).find(".i18n-placeholder").each(function(i, node) {
      let i18nKey = i18nKeys[$(node).attr("i18n")];
      if (i18nKey != undefined) {
          $(node).attr("placeholder", i18nKey);
      }
    });
  }

  function getI18nKey(key) {
    let string = i18nKeys[key];
    if (string != undefined) {
      for (var i = 0; i < arguments.length - 1; i++) {
        string = string.replace(new RegExp('\\{' + i + '\\}', "g"), arguments[i + 1]);
      }
      return string;
    }
    else {
      return "";
    }
  }

  function toReadableSize(size, unit) {
    let conSize = convertSize({number: size, unit: unit});
    let unitString = "";
    switch (conSize.unit) {
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
    if (sizeAndUnit.unit < 3) {
      if (sizeAndUnit.number > 1024) {
        var size2 = Math.round((sizeAndUnit.number / 1024) * 100) / 100;
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
      trigger: "manual",
      container: '#files'
    }).on("mouseenter", function() {
      var pop = this;
      clearTimeout(timer);
      $(".popShow").popover("hide");
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
          if (!$(':hover', this).length) {
            $(pop).popover('hide');
          }
        }
      }, 200);
    }).on("hide.bs.popover", function() {
      $(this).removeClass("popShow");
    }).on("show.bs.popover", function() {
      $(this).addClass("popShow");
    });
  }

  function showDelAlert(fileList) {
    $("#lightbox-alert-delete-list").html("");
    $.each(fileList, function(i, elm) {
      $("#lightbox-alert-delete-list").append("<p>" + elm + "</p>")
    });
    $("#lightbox-alert-delete").modal('show');
  }

  function showDelAlertDocs(fileList) {
    $("#lightbox-alert-delete-docs-list").html("");
    $.each(fileList, function(i, doc) {
      $("#lightbox-alert-delete-docs-list").append("<p>" + doc.maintitle + "</p>")
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

  function showDeleteDocAlert(docType) {
    $("#lightbox-alert-deleteDoc-label").html(derivateBrowserTools.getI18n("db.alert.document.delete." + docType + ".title"));
    $("#lightbox-alert-deleteDoc-text").html(derivateBrowserTools.getI18n("db.alert.document.delete." + docType + ".text", derivateBrowserTools.getCurrentDocID()));
    $("#lightbox-alert-deleteDoc").modal('show');
  }

  function readQueryParameter() {
    var q = document.URL.split(/\?(.+)?/)[1];
    if (q != undefined) {
      q = q.split('#')[0];
      q = q.split('&');
      for (var i = 0; i < q.length; i++) {
        hash = q[i].split(/=(.+)?/);
        qpara.push(hash[1]);
        qpara[hash[0]] = hash[1];
      }
    }
  }

  function getPath() {
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
  }

  function deleteDocAndGoToParent(parentID) {
    var json = [];
    json.push({"objId": derivateBrowserTools.getCurrentDocID()});
    deleteDocument(json, checkDeleteAndRedirect, parentID);
  }

  function checkDeleteAndRedirect(json, parentID) {
    if (json[0].status == "0") {
      derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.delete.error"), false);
      return false;
    }
    if (json[0].status == "1") {
      redirectToParent(parentID);
    }
  }

  function redirectToParent(parentID) {
    if (parentID != undefined && parentID != "") {
      window.location.href = jp.baseURL + "receive/" + parentID;
      return false;
    }
    if (window.referrer != undefined) {
      window.location.href = window.referrer;
      return false;
    }
    window.location.href = jp.baseURL;
  }

  function removeFromView(json) {
    $.each(json, function(i, elm) {
      if (elm.status == "1") {
        $("body").trigger("removeDocPerID", [elm.objId, ""]);
      }
      else {
        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.delete.notAllDocs"), false);
      }
    });
    derivateBrowserTools.hideLoadingScreen();
    derivateBrowserTools.goToParent(derivateBrowserTools.getCurrentDocID());
  }

  function getParentID(docID, callback) {
    var url = jp.baseURL + "servlets/solr/select?q=id%3A" + docID + "&start=0&rows=10&sort=maintitle+asc&wt=json&indent=true";
    $.getJSON(url, function(search) {
      if (search.response.numFound > 0) {
        if (search.response.docs[0].parent) {
          callback(search.response.docs[0].parent, "");
          return;
        }
        if (search.response.docs[0].derivateOwner) {
          callback(search.response.docs[0].derivateOwner, "");
          return;
        }
        callback("", "");
      }
      else {
        callback("", "");
      }
    });
  }

  function deleteDocument(json, callback, parentID) {
    $.ajax({
      url: "docs",
      type: "DELETE",
      data: JSON.stringify(json),
      dataType: "json",
      success: function(data) {
        callback(data, parentID);
        derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.deleted"), true);
      },
      error: function(error) {
        derivateBrowserTools.hideLoadingScreen();
        console.log(error);
        if (error.status == 401) {
          derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.noPermission"), false);
        }
        else {
          derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.document.delete.error"), false);
        }
      }
    });
  }

  function checkFileType(deriID, path, callback){
      $.ajax({
          url: "./checkFileType/" + deriID + path ,
          type: "GET",
          success: function(data) {
              if (data === "directory") {
                  callback(deriID, path, "");
              }
              else {
                  callback(deriID, path.substring(0, path.lastIndexOf("/")), path.substr(path.lastIndexOf("/") + 1));
              }
          },
          error: function(error) {
              console.log(error);
              derivateBrowserTools.alert(derivateBrowserTools.getI18n("db.alert.loadFailed", deriID), false);
          }
      });
  }

  return {
    //public
    init: function(mode) {
      currentMode = mode ? mode : "full";

      $.address.externalChange(function() {
        getPath();
      });

      readQueryParameter();
      var lang = qpara["lang"];
      if (lang == undefined) lang = "de";
      loadI18nKeys(lang);

      if (currentMode == "compact") {
        getPath();
      }
    },

    initStandalone: function() {
      readQueryParameter();
      var lang = qpara["lang"];
      if (lang == undefined) lang = "de";
      loadI18nKeys(lang);
    },

    setImgPath: function(img, deriID, path) {
      if (path.endsWith("pdf")) {
        getPDFImg(img, deriID, path);
      }
      else {
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

    goToParent: function(docID) {
      getParentID(docID, goToDocument);
    },

    goToPath: function(path) {
      goToDocument(path.substring(0, path.indexOf("/")), path.substring(path.indexOf("/")));
    },

    setFileName: function(filename) {
      setFile(filename);
    },

    alert: function(text, success) {
      showAlert(text, success);
    },

    updateI18nForElm: function(elm) {
      updateI18n(elm);
    },

    getI18n: function(args) {
      return getI18nKey.apply(this, arguments);
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

    hideLoadingScreen: function() {
      hideLoadScreen();
    },

    newAsyncMonitor: function(count, callback) {
      asyncCount = count;
      asyncCallback = callback;
    },

    doneAsync: function(para) {
      if (--asyncCount < 1) {
        asyncCallback(para, "");
      }
    },

    showDeleteDocAlert: function(docType) {
      showDeleteDocAlert(docType);
    },

    setDocID: function(docID) {
      currentDocID = docID;
    },

    setPath: function(path) {
      currentPath = path;
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
