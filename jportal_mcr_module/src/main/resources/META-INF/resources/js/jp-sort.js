var jp = jp || {};

jp.sort = {

  id: null,

  object: null,

  selectedSorter: null,

  sorters: [],

  addSorter: function(sortClass) {
    jp.sort.sorters.push(sortClass);
  },

  init: function(id) {
    jp.sort.id = id;
    jp.sort.loadSortContainer();
    jp.sort.loadI18nKeys();
    jp.sort.getObject(id, function(object) {
      jp.sort.object = object;
      var sortBy = object.metadata.autosort != null ? object.metadata.autosort.data[0] : null;
      if(sortBy != null) {
        jp.sort.selectSorter(sortBy.$text);
        $("#jp-sort-order-select").val(sortBy._order);
      }
    });
  },

  show: function() {
    $("#sort-modal").modal("show");
  },

  loadSortContainer: function() {
    var container = $("#autoSortContainer");
    for(var sorter of jp.sort.sorters) {
      var className = sorter.substring(sorter.lastIndexOf(".") + 1, sorter.length);
      var btn = "<a href='#' type='button' class='jp-sort-editor-sorter list-group-item " + className + "' onclick='jp.sort.selectSorter(`" + sorter +"`)'>" +
          "<div class='i18n' i18n='jp.sort.editor." + className + "' style='font-weight: bold;'></div>" +
          "<div class='i18n' i18n='jp.sort.editor." + className + ".description'></div>" +
      	"</a>";
      container.append(btn);
    }
  },

  selectSorter: function(sorter) {
    $(".jp-sort-editor-sorter").removeClass("active");
    if(sorter == null) {
      $(".jp-sort-editor-sorter-no-sorter").addClass("active");
      jp.sort.selectedSorter = null;
      return;
    }
    jp.sort.selectedSorter = sorter;
    var className = sorter.substring(sorter.lastIndexOf(".") + 1, sorter.length);
    $("." + className).addClass("active");
  },

  save: function() {
    before();
    if(jp.sort.selectedSorter != null) {
      var sorterClass = jp.sort.selectedSorter;
      var order = $("#jp-sort-order-select").val();
      $.post(jp.baseURL + "rsc/sorter/" + jp.sort.id + "?sorter=" + sorterClass + "&order=" + order)
        .done(onSuccess)
        .fail(onFail);
    } else {
      $.ajax({
        method: 'DELETE',
        url: jp.baseURL + "rsc/sorter/" + jp.sort.id
      }).done(onSuccess)
        .fail(onFail);
    }

    function before() {
      $("#jp-sort-body-darken").css("display", "block");
      $("#jp-sort-body").css("pointer-events", "none");
      $("#jp-sort-footer-default").css("display", "none");
      $("#jp-sort-footer-saving").css("display", "block");
    }

    function after() {
      $("#jp-sort-body-darken").css("display", "none");
      $("#jp-sort-body").css("pointer-events", "auto");
      $("#jp-sort-footer-default").css("display", "block");
      $("#jp-sort-footer-saving").css("display", "none");
    }

    function onSuccess() {
      after();
      $("#sort-modal").modal("hide");
    }

    function onFail(error) {
      after();
      console.log(error);
      alert("An error occur. Unable to save changes.");
    }
  },

  getObject: function(id, callback) {
    $.getJSON(jp.baseURL + "rsc/object/" + id, function(object) {
      callback(object);
    }, function(error) {
      console.log(error);
      alert("Error while retrieving mycore object " + id);
    });
  },

  loadI18nKeys: function() {
    $.getJSON(jp.baseURL + "rsc/locale/translate/" + jp.lang + "/jp.sort.editor.*", function(data) {
      i18nKeys = data;
      jp.sort.updateI18n($("body"));
    });
  },

  updateI18n: function(elm) {
    $(elm).find(".i18n").each(function(i, node) {
      var key = $(node).attr("i18n");
      var i18nKey = i18nKeys[key];
      if (i18nKey != undefined) {
        $(node).html(i18nKey);
      } else {
        $(node).html($(node).attr("i18n-def"));
      }
    });
  }

}

$(document).ready(function() {
  $("body").on("click", "#sortButton", function() {
    var mcrID = $(this).attr("mcrid");
    if ($("#sortGUIMain").length < 1) {
      $.ajax({
        url: jp.baseURL + "html/jp-sort.html",
        type: "GET",
        dataType: "html",
        statusCode: {
          200: function(data) {
            var html = $("<div></div>").append(data).find("#sortGUIMain");
            $("head").append('<link href="'+ jp.baseURL + 'css/jp-sort.css" rel="stylesheet" type="text/css">');
            $("body").append(html);
            jp.sort.init(mcrID);
            jp.sort.show();
          },
          500: function(error) {
            alert(error);
          }
        }
      });
    } else {
      jp.sort.show();
    }
  });
});
