var jp = jp || {};

jp.session = {

  url: jp.baseURL + "rsc/echo/ping",
  minutes: 10,

  init: function () {
    var delay = (jp.session.minutes * 60000) - 30000;
    window.setInterval(jp.session.exec, delay);
  },

  exec: function () {
    $.get(jp.session.url).done(function () {
      console.log("session update");
    }).fail(function (err) {
      console.log(err);
    });
  }

};
jp.session.init();

// CKEDITOR
function introEditor(journalID) {
  let createdElem = null;
  let ckEditorMainButtonCtr = function (tmpElem) {
    $('#ckeditorButton').hide();
    var introFrame = $('#intro');
    introFrame.removeClass("hidden");
    introFrame
        .ckeditor({
          resize_enabled: false,
          entities: false,
          enterMode: CKEDITOR.ENTER_BR,
          entities_processNumerical: 'force',
          tabSpaces: 4,
          fillEmptyBlocks: false,
          height: '500px',
          toolbar: [
            {name: 'clipboard', items: ['Undo', 'Redo']},
            {name: 'basicstyles', items: ['Bold', 'Italic', 'Underline']},
            {name: 'styles', items: ['Format']},
            {name: 'colors', items: ['TextColor', 'BGColor']},
            {name: 'paragraph', items: ['NumberedList', 'BulletedList']},
            {name: 'links', items: ['Link', 'Unlink']},
            {name: 'document', items: ['Source']}]
        });
    var ckeditorButtons = $("<ul id='ckeditorButtons' class='ckeditorButtons'></ul>");
    var ckeditorCancelButton = $("<button id='ckeditorCancelButton'>Abbrechen</button>");
    var ckeditorSaveButton = $("<button id='ckeditorSaveButton'>Speichern</button>");
    $("<li />").appendTo(ckeditorButtons).append(ckeditorCancelButton);
    $("<li />").appendTo(ckeditorButtons).append(ckeditorSaveButton);
    tmpElem.append(ckeditorButtons);

    function cancelNoSave() {
      introFrame.ckeditorGet().destroy(true);
      tmpElem.remove();
      $('#ckeditorButton').show();
    }

    function saveContent() {
      var editor = introFrame.ckeditorGet();
      var editorData = editor.getData();
      $.ajax({
        url: jp.baseURL + 'rsc/journalFile/' + journalID + '/intro.xml',
        type: 'POST',
        data: editorData,
        contentType: 'application/xhtml+xml'
      });
      editor.destroy();
      $('#ckeditorButton').show();
    }

    ckeditorSaveButton.click(function () {
      saveContent();
      ckeditorButtons.remove();
    });

    ckeditorCancelButton.click(function () {
      ckeditorButtons.remove();
      if (introFrame.ckeditorGet().checkDirty()) {
        new BootstrapDialog({
          title: 'Änderungen speichern',
          message: 'Es wurden Änderungen vorgenommen! Wollen Sie diese Änderungen Speichern?',
          buttons: [{
            label: 'Nein',
            action: function (dialog) {
              cancelNoSave();
              dialog.close();
            }
          }, {
            label: 'Ja',
            cssClass: 'btn-primary',
            action: function (dialog) {
              saveContent();
              dialog.close();
            }
          }]
        }).open();
      } else {
        cancelNoSave();
      }
    });
  }
  var tmpElem = $('<div id="#ckEditorTmp"/>');
  ckEditorMainButtonCtr(tmpElem);
  tmpElem.insertAfter('#intro');
}

// DERIVATE DELETE DIALOG
function showDeleteDerivateDialog(/*String*/ id) {
  new BootstrapDialog({
    title: 'Derivat löschen',
    message: 'Wollen Sie das Derivat endgültig löschen?',
    buttons: [{
      label: 'Nein',
      action: function (dialog) {
        dialog.close();
      }
    }, {
      label: 'Ja',
      cssClass: 'btn-primary',
      action: function (dialog) {
        jQuery.ajax({
          type: 'DELETE',
          url: jp.baseURL + 'rsc/object/' + id
        }).done(function (msg) {
          location.reload(true);
        }).fail(function (error) {
          if (error.status == 400) {
            alert('Bad request: ' + error.responseText);
          } else if (error.status == 401) {
            alert('Unauthorized: You have no permission to delete this object!');
          } else if (error.status == 403) {
            alert('Forbidden: ' + error.responseText);
          } else if (error.status == 404) {
            alert('Unknown MyCoRe object id ' + id);
          }
        });
        dialog.close();
      }
    }]
  }).open();
}

$(document).ready(function () {
  function supportHTML5() {
    return !!document.createElement('canvas').getContext;
  }

  if (!supportHTML5()) {
    var searchInput = $('#searchForm #inputField');
    var placeholderTxt = searchInput.attr('placeholder');
    searchInput.attr('value', placeholderTxt);
    searchInput.focus(function () {
      searchInput.removeAttr('value');
    });

    var inputChanged = false;
    searchInput.change(function () {
      inputChanged = true;
    })

    searchInput.blur(function () {
      var currentInputVal = searchInput.attr('value');
      if (!inputChanged || $.trim(currentInputVal) == '') {
        searchInput.attr('value', placeholderTxt);
      }
    })
  }
});

function truncate(/* String */string, /* int */maxCharacters) {
  return string.length > maxCharacters ? jQuery.trim(string).substring(0, maxCharacters).split(" ").slice(0, -1).join(" ") + "..."
      : string;
}

function updateQueryStringParameter(uri, key, value) {
  const re = new RegExp("([?|&])" + key + "=.*?(&|$)", "i");
  const separator = uri.indexOf('?') !== -1 ? "&" : "?";
  if (uri.match(re)) {
    return uri.replace(re, '$1' + key + "=" + value + '$2');
  } else {
    return uri + separator + key + "=" + value;
  }
}

// http://stackoverflow.com/questions/901115/how-can-i-get-query-string-values-in-javascript/21152762#21152762
function getUrlParameter() {
  const qd = {};
  location.search.substr(1).split("&").forEach(function (item) {
    const s = item.split("="),
        k = s[0],
        v = s[1] && decodeURIComponent(s[1]);
    (k in qd) ? qd[k].push(v) : qd[k] = [v]
  });
  return qd;
}

// SEARCHBAR
function updateSearchbar() {
  const searchForm = $("#searchForm");
  const searchField = $("#inputField");
  const searchDropDownButton = $("#searchDropDownButton");
  const journalSearchOption = $("#journalSearchOption");
  const volumeSearchOption = $("#volumeSearchOption");
  const globalSearchOption = $("#globalSearchOption");
  const journalSearchLabel = $("#journalSearchLabel").text();
  const volumeSearchLabel = $("#volumeSearchLabel").text();
  const globalSearchLabel = $("#globalSearchLabel").text();
  const isInSearchResults = location.pathname.endsWith("solr/find");

  journalSearchOption.on("click", activateJournalSearch);
  volumeSearchOption.on("click", activateVolumeSearch);
  globalSearchOption.on("click", activateGlobalSearch);

  journalSearchOption.append(journalSearchLabel);
  volumeSearchOption.append(volumeSearchLabel);
  globalSearchOption.append(globalSearchLabel);

  if (jp.journalID != null) {
    if(jp.objectID !== null && isInSearchResults) {
      activateVolumeSearch();
    } else {
      activateJournalSearch();
    }
  } else {
    activateGlobalSearch();
  }

  function activateGlobalSearch() {
    searchField.attr("placeholder", globalSearchLabel);
    searchField.attr("title", globalSearchLabel);
    setDrowDownIcon("fa-globe");
    removeHiddenFields();
  }

  function activateJournalSearch() {
    searchField.attr("placeholder", journalSearchLabel);
    searchField.attr("title", journalSearchLabel);
    setDrowDownIcon("fa-newspaper-o");
    removeHiddenFields();
    appendJournalSearchFields();
  }

  function activateVolumeSearch() {
    searchField.attr("placeholder", volumeSearchLabel);
    searchField.attr("title", volumeSearchLabel);
    setDrowDownIcon("fa-book");
    removeHiddenFields();
    appendVolumeSearchFields();
  }

  function removeHiddenFields() {
    searchForm.find("input[name='fq']").remove();
    searchForm.find("input[name='journalID']").remove();
    searchForm.find("input[name='objectID']").remove();
  }

  function appendJournalSearchFields() {
    searchForm.append("<input type='hidden' name='fq' value='journalID:" + jp.journalID + "' />");
    searchForm.append("<input type='hidden' name='journalID' value='" + jp.journalID + "' />");
  }

  function appendVolumeSearchFields() {
    searchForm.append("<input type='hidden' name='fq' value='ancestorPath:*/" + jp.objectID + "/*' />");
    searchForm.append("<input type='hidden' name='journalID' value='" + jp.journalID + "' />");
    searchForm.append("<input type='hidden' name='objectID' value='" + jp.objectID + "' />");
  }

  function setDrowDownIcon(iconClass) {
    searchDropDownButton.find("i").remove();
    searchDropDownButton.prepend("<i class='fa fa-fw " + iconClass + "'></i>");
  }

}

// LOAD IMAGES
$(document).ready(function () {
  $(".jp-thumbnail").each((index, link) => {
    let img = new Image();
    img.onload = () => {
      for(let spinner of link.querySelectorAll(".jp-thumbnail-spinner")) {
        spinner.remove();
      }
      link.appendChild(img);
      img.style.display = "none";
      $(img).fadeIn(200);
    };
    img.src = link.dataset.img;
  });
});

// IVIEW 2
$(document).ready(function () {
  $('div.jp-layout-derivate .thumbnail').on({
    mouseenter: function () {
      jQuery(this).find('div.jp-layout-hidden-Button').show();
    },
    mouseleave: function () {
      jQuery(this).find('div.jp-layout-hidden-Button').hide();
    }
  });
});

// SORT
$(document).ready(function () {
  $(".sortSelect").on("change", function () {
    // get url
    var url = window.location.href;
    // change/add sort parameter
    var newLocation = updateQueryStringParameter(url, "sort", this.value);
    // go to
    window.location = newLocation;
  });
});

// PIWIK
function trackPageView(piwikURL, journalID, pageID) {
  window._paq = [];
  (function () {
    var u = piwikURL;
    if (journalID != "") {
      _paq.push(['setCustomVariable', 1, "journal", journalID, "page"]);
    }
    _paq.push(['setDownloadExtensions', "pdf"]);
    _paq.push(["setTrackerUrl", u + "piwik.php"]);
    _paq.push(["setSiteId", pageID]);
    _paq.push(["trackPageView"]);
    _paq.push(["enableLinkTracking"]);
    var d = document, g = d.createElement("script"), s = d.getElementsByTagName("script")[0];
    g.type = "text/javascript";
    g.defer = true;
    g.async = true;
    g.src = u + "piwik.js";
    s.parentNode.insertBefore(g, s);
  })();
}

// LINK IMAGE
$(document).ready(function () {

  $("#linkImage").one("click", function () {
    appendSpinner($(this));
    var objectID = $(this).attr("data-object");
    $.post(jp.baseURL + "rsc/derivate/link/set/" + objectID).done(function () {
      window.location.reload();
    }).fail(function (err) {
      alert(err.responseText);
      window.location.reload();
    });
  });

  $(".unlinkImage").one("click", function () {
    appendSpinner($(this));
    var objectID = $(this).attr("data-object");
    var image = $(this).attr("data-image");
    $.post(jp.baseURL + "rsc/derivate/link/remove/" + objectID + "?image=" + encodeURIComponent(image)).done(function () {
      window.location.reload();
    }).fail(function (err) {
      alert(err.responseText);
      window.location.reload();
    });
  });

  function appendSpinner(node) {
    node.append("<i class='fa fa-circle-o-notch fa-spin'></i>");
  }

});

// PAGINATION JUMP
$(document).ready(function () {
  $(".pagination-jump-submit").on("click", function () {
    jump($(this));
  });
  $(".pagination-jump-input").on("keypress", function (keyEvent) {
    if (keyEvent.which == 13) {
      jump($(this));
    }
  });
  $(".pagination-jumper-form").submit(function (event) {
    jump($("input[type=submit]", $(this)));
    event.preventDefault();
  });

  function jump(submitButton) {
    var param = submitButton.attr("data-param");
    if (param == null) {
      console.log("data-param of pagination-jump-submit not set!");
      return;
    }
    var input = $("input[id='pagination-" + param + "']");
    if (input == null || input.length == 0) {
      console.log("cannot find associated input text of " + param);
      return;
    }
    var page = parseInt(input.val());
    var pages = parseInt(submitButton.attr("data-pages"));
    if (isNaN(pages)) {
      console.log("cannot find data-pages attribute");
      pages = 99999;
    }
    if (isNaN(page) || page <= 0 || page > pages) {
      $(input).stop().animate({borderColor: "#CD3700"}, 'fast');
      return;
    }
    var rows = parseInt(submitButton.attr("data-rows"));
    if (isNaN(rows)) {
      rows = 5;
    }
    setGetParameter(param, (page - 1) * rows);
  }

  // http://stackoverflow.com/questions/13063838/add-change-parameter-of-url-and-redirect-to-the-new-url
  function setGetParameter(paramName, paramValue) {
    var url = window.location.href;
    if (url.indexOf(paramName + "=") >= 0) {
      var prefix = url.substring(0, url.indexOf(paramName));
      var suffix = url.substring(url.indexOf(paramName));
      suffix = suffix.substring(suffix.indexOf("=") + 1);
      suffix = (suffix.indexOf("&") >= 0) ? suffix.substring(suffix.indexOf("&")) : "";
      url = prefix + paramName + "=" + paramValue + suffix;
    } else {
      if (url.indexOf("?") < 0) {
        url += "?" + paramName + "=" + paramValue;
      } else {
        url += "&" + paramName + "=" + paramValue;
      }
    }
    window.location = url;
  }
});

//METS GENERATE
$(document).ready(function () {
  var dialog = $("#generateMetsDialog");
  var generateButton = $("#generateMetsDialogStart");
  var dialogIcon = $("#generateMetsDialogIcon");
  var dialogContent = $("#generateMetsDialogContent");

  generateButton.click(function () {
    dialogIcon.html("<i class='fa fa-3x fa-circle-o-notch fa-spin' />");
    dialogContent.html("mets.xml wird generiert. Bitte warten...");
    generateButton.attr("disabled", "disabled");
    $.get(jp.baseURL + "rsc/mets/base/generate/" + dialog.attr("data-id")).done(function (e) {
      if (e.error) {
        failed(e);
        return;
      }
      success();
    }).fail(function (e) {
      console.log(e);
      failed(e);
    });
  });

  function failed(e) {
    dialogIcon.html("<i class='fa fa-3x fa-ban' />");
    if (e.status == "401") {
      dialogContent.html("Sie haben nicht die notwendige Berechtigung um die mets.xml neu zu generieren!");
    } else {
      dialogContent.html("Es ist ein Fehler bei der Generierung aufgetreten. Bitte wenden Sie sich an den Administrator.");
    }
  }

  function success() {
    dialogIcon.html("<i class='fa fa-3x fa-check' />");
    dialogContent.html("Die mets.xml wurde erfolgreich generiert!");
  }

});

//URN GENERATE
$(document).ready(function () {
  var dialog = $("#generateURNDialog");
  var generateButton = $("#generateURNDialogStart");
  var dialogIcon = $("#generateURNDialogIcon");
  var dialogContent = $("#generateURNDialogContent");


  generateButton.click(function () {
    dialogIcon.html("<i class='fa fa-3x fa-circle-o-notch fa-spin' />");
    dialogContent.html("URN wird vergeben. Bitte warten...");
    generateButton.attr("disabled", "disabled");
    let derivID = dialog.attr("data-id");

    $.post(jp.baseURL + "rsc/pi/registration/service/DNBURNGranular/" + derivID).done(function (e) {
      if (e.error) {
        failed(e);
        return;
      }
      $.post(jp.baseURL + "rsc/urn/update/" + derivID).done(function (e) {
        if (e.error) {
          failed(e);
          return;
        }
        success();
      }).fail(function (e) {
        console.log(e);
        failed(e);
      });
    }).fail(function (e) {
      console.log(e);
      failed(e);
    });
  });

  dialog.on("hidden.bs.modal", function () {
    location.reload();
  });

  function failed(e) {
    dialogIcon.html("<i class='fa fa-3x fa-ban' />");
    if (e.status == "401") {
      dialogContent.html("Sie haben nicht die notwendige Berechtigung, um die URN zu vergeben!");
    } else {
      dialogContent.html("Es ist ein Fehler bei der Generierung aufgetreten. Bitte wenden Sie sich an den Administrator.");
    }
  }

  function success() {
    dialogIcon.html("<i class='fa fa-3x fa-check' />");
    dialogContent.html("Die URN wurde erfolgreich vergeben!");
  }

});

$(document).ready(function () {
  var dialog = $("#updateURNDialog");
  var generateButton = $("#updateURNDialogStart");
  var dialogIcon = $("#updateURNDialogIcon");
  var dialogContent = $("#updateURNDialogContent");

  generateButton.click(function () {
    dialogIcon.html("<i class='fa fa-3x fa-circle-o-notch fa-spin' />");
    dialogContent.html("URL der URN wird aktulisiert. Bitte warten...");
    generateButton.attr("disabled", "disabled");
    $.post(jp.baseURL + "rsc/urn/update/" + dialog.attr("data-id")).done(function (e) {
      if (e.error) {
        failed(e);
        return;
      }
      success();
    }).fail(function (e) {
      console.log(e);
      failed(e);
    });
  });

  dialog.on("hidden.bs.modal", function () {
    location.reload();
  });

  function failed(e) {
    dialogIcon.html("<i class='fa fa-3x fa-ban' />");
    if (e.status == "401") {
      dialogContent.html("Sie haben nicht die notwendige Berechtigung, um die URL der URN zu aktualisieren!");
    } else {
      dialogContent.html("Es ist ein Fehler bei der Generierung aufgetreten. Bitte wenden Sie sich an den Administrator.");
    }
  }

  function success() {
    dialogIcon.html("<i class='fa fa-3x fa-check' />");
    dialogContent.html("Die URN wurde erfolgreich vergeben!");
  }

});

// METS IMPORT & CONVERT
$(document).ready(function () {

  // IMPORT
  var dialog = $("#importMetsDialog");
  var dialogIcon = $("#importMetsDialogIcon");
  var dialogContent = $("#importMetsDialogContent");
  var startImportButton = $("#importMetsDialogStart");
  var closeButton = $("#importMetsDialogClose");

  dialog.on("show.bs.modal", function (e) {
    $.get(jp.baseURL + "rsc/mets/import/check/" + dialog.attr("data-id")).done(function (e) {
      if (e.type == "unknown") {
        failed({
          msg: "Unbekanntes mets.xml Dokument. Bitte wenden Sie sich an den Administrator."
        });
      } else if (e.error) {
        failed(e);
      } else if (e.type == "llz" || e.type == 'jvb' || e.type == 'perthes') {
        importable();
      } else {
        failed(e);
      }
    }).fail(function (e) {
      console.log(e);
      failed(e);
    });
  });

  function importable() {
    dialogIcon.html("<i class='fa fa-3x fa-check' />");
    dialogContent.html("Überprüfung erfolgreich. Sie können den Importvorgang jetzt starten.");
    startImportButton.removeAttr("disabled");
    startImportButton.on("click", startImport);
  }

  function failed(e) {
    dialogIcon.html("<i class='fa fa-3x fa-ban' />");
    if (typeof e.error === 'string') {
      dialogContent.html(e.error);
    } else if (e.error != null && typeof e.error === 'object') {
      if (e.error.appearance == null) {
        dialogContent.html(e.error.message);
        return;
      }
      var html = "<div><p>Es ist ein Fehler bei der Paragraphen Referenzierung aufgetreten. Die folgenden" +
          " Fehler müssen im Structify behoben werden:</p><ul style='margin: 8px 0 16px; font-weight: bold;'>";
      e.error.appearance.forEach(function (error) {
        html += "<li>Seite: " + error.image + "; Artikel; '" + error.label + "'; Paragraph Nummer: " + error.paragraph + "</li>";
      });
      html += "</ul><p>Dieser Fehler tritt auf, wenn einem Paragraph kein ALTO-Block (show paragraph) zugeordnet werden konnte." +
          " Um den Fehler zu beheben muss entweder der Paragraph gelöscht, oder das umschließende Rechteck vergrößert" +
          " werden.</div>";
      html += "<p>Sie können den Importvorgan trotzdem starten. Bitte tun Sie dies nur, wenn Sie sich der Konsequenzen bewusst" +
          " sind!</p> <label><input type='checkbox' id='importMetsDialogImportAnyway'> Trotz Fehler importieren</label>";
      dialogContent.html(html);

      $("#importMetsDialogImportAnyway").on("click", function () {
        startImportButton.removeAttr("disabled");
        startImportButton.on("click", startImport);
      });
    } else if (e.status == "401") {
      dialogContent.html("Sie haben nicht die notwendige Berechtigung um den Importvorgang zu starten!");
    } else {
      dialogContent.html("Dieses Derivat kann nicht importiert werden!" +
          " Bitte wenden Sie sich an den Administrator wenn Sie denken das dies ein Fehler ist.");
    }
  }

  function startImport() {
    dialogIcon.html("<i class='fa fa-3x fa-circle-o-notch fa-spin'></i>");
    dialogContent.html("Importiere. Bitte warten...");
    startImportButton.remove();
    $.post(jp.baseURL + "rsc/mets/import/import/" + dialog.attr("data-id")).done(function (data) {
      dialogIcon.html("<i class='fa fa-3x fa-check' />");
      dialogContent.html("Import erfolgreich!");
      console.log(data);
      closeButton.on("click", function () {
        location.reload();
      });
    }).fail(function (e) {
      console.log(e);
      dialogIcon.html("<i class='fa fa-3x fa-ban' />");
      var msg = "Es ist ein Fehler während des Importvorgangs aufgetreten. " +
          "Bitte wenden Sie sich an den Adminstrator!"
      if (e.status == "401") {
        msg = "Sie haben nicht die notwendigen Rechte.";
      }
      dialogContent.html(msg);
    });
  }

});

// SRU IMPORT
$(document).ready(function () {
  var id = $("#updateSRU").attr("mcrid");
  var gnd = $("#updateSRU").attr("gnd");

  $("#updateSRU").on("click", function () {
    var dialog = new BootstrapDialog({
      closable: false,
      message: function (dialogRef) {
        var html = $(
            "<div style='text-align: center'>" +
            "<p>Katalogdatenbank wird angefragt. Bitte warten...</p>" +
            "<p><i class='fa fa-3x fa-circle-o-notch fa-spin'></i></p>" +
            "</div>"
        );
        return handleMessage(html, dialogRef);
      }
    });
    dialog.realize();
    dialog.getModalHeader().hide();
    dialog.getModalFooter().hide();
    dialog.open();
  });

  function handleMessage(html, dialogRef) {
    var doubletCheckResponse = doubletCheck(gnd);
    if (doubletCheckResponse.numFound > 1) {
      var doublets = doubletCheckResponse.docs.map(function (doc) {
        return doc.id;
      }).filter(function (doubletId) {
        return id != doubletId;
      });
      html.html("<p style='color:red'>Es sind Doubletten vorhanden:</p>");
      doublets.forEach(function (doubletId) {
        html.append("<li><a href='" + jp.baseURL + "receive/" + doubletId + "'>" + doubletId + "</a></li>");
      });
      addCloseButton(html, dialogRef);
      return html;
    }

    fetchData(id, gnd).fail(function (jqXHR, textStatus) {
      var infoText = jqXHR.statusText;
      if (jqXHR.status == 404) {
        infoText = "Katalogeintrag mit GND-Nummer " + gnd + " wurde nicht gefunden.";
      }
      html.html("<p>Anfrage fehlgeschlagen: " + infoText + ".</p>");
      addCloseButton(html, dialogRef);
    });
    return html;
  }

  function addCloseButton(html, dialogRef) {
    var close = $("<button class='btn btn-primary btn-lg btn-block' style='margin-top: 8px;'>Schließen</button>");
    close.on('click', {dialogRef: dialogRef}, function (event) {
      event.data.dialogRef.close();
    });
    html.append(close);
  }

  function fetchData(id, gnd) {
    return $.ajax({
      url: jp.baseURL + "rsc/sru/check/" + gnd
    }).done(function () {
      window.location = jp.baseURL + "editor/start.xed?action=update&id=" + id + "&gnd=" + gnd + "&type=" + id.split("_")[1];
    });
  }

  function doubletCheck(/*string*/ gnd) {
    var json = $.ajax({
      type: "GET",
      url: jp.baseURL + "servlets/solr/select?rows=5&fl=id&q=id.gnd:" + gnd + "&wt=json",
      async: false,
      error: function (error) {
        console.log(error);
      }
    }).responseText;
    return $.parseJSON(json).response;
  }

});

// SPATIAL MODAL
$(document).ready(function () {
  const button = $(".jp-spatial-view-show-modal-button");

  let modal = null;
  let map = null;
  let marker = null;

  button.on("click", (e) => {
    const title = e.target.dataset.modalTitle;
    const content = e.target.dataset.content;
    const split = content.split(",");
    const lat = parseFloat(split[0]);
    const lng = parseFloat(split[1]);
    const zoom = parseInt(e.target.dataset.zoom);
    if (modal == null) {
      modal = initModal(title, lat, lng, zoom);
    } else {
      $(modal.$modalHeader).find(".bootstrap-dialog-title").html(title);
      if(map != null) {
        map.setView([lat, lng], zoom);
        setMarker(lat, lng);
      }
    }
    modal.open();
  });

  function initModal(title, lat, lng, zoom) {
    return new BootstrapDialog({
      title: title,
      message: function (dialog) {
        return $("<div class='jp-modal-map-container'></div>");
      },
      onshow: function (dialog) {
        if (map != null) {
          return;
        }
        jp.util.initLeaflet().then(() => {
          const mapContainer = dialog.$modal.find(".jp-modal-map-container")[0];
          // create map
          map = L.map(mapContainer).setView([lat, lng], zoom);
          L.tileLayer('https://{s}.tile.openstreetmap.de/tiles/osmde/{z}/{x}/{y}.png', {
            attribution: 'Daten von <a href="http://www.openstreetmap.org/">OpenStreetMap</a> -' +
            ' Veröffentlicht unter <a href="http://opendatacommons.org/licenses/odbl/">ODbL</a>',
            maxZoom: 18
          }).addTo(map);
          // set marker
          setMarker(lat, lng);
        });
      },
      buttons: [{
        label: 'Schließen',
        action: function (dialog) {
          dialog.close();
        }
      }],
      autodestroy: false
    });
  }

  function setMarker(lat, lng) {
    if (marker != null) {
      map.removeLayer(marker);
    }
    marker = L.marker([parseFloat(lat), parseFloat(lng)]).addTo(map);
  }

});
