var jp = jp || {};

jp.sort = {

  id: null,

  object: null,

  selectedChildId: null,

  children: [],

  oldChildren: [],

  childrenPage: 1,

  childrenPerPage: 10,

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
    jp.sort.getChildren(id, function(data) {
      jp.sort.reloadChildren(data.response);
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
      jp.sort.updateChildrenToolbar();
      return;
    }
    jp.sort.selectedSorter = sorter;
    var className = sorter.substring(sorter.lastIndexOf(".") + 1, sorter.length);
    $("." + className).addClass("active");
    jp.sort.updateChildrenToolbar();
  },

  reloadChildren: function(solrResponse) {
    var docs = solrResponse.docs;
    jp.sort.children = [];
    jp.sort.oldChildren = [];
    for(var doc of docs) {
      jp.sort.oldChildren.push(doc);
      jp.sort.children.push(doc);
    }
    jp.sort.childrenPage = 1;
    jp.sort.renderChildren();
  },

  renderChildren: function() {
    var container = $("#childrenContainer");
    var paginator = $("#childrenPaginator");
    container.empty();
    paginator.empty();

    var children = jp.sort.children;
    if(children.length == 0) {
      container.html("Keine Kinder gefunden.");
      return;
    }

    var currentPage = jp.sort.childrenPage;

    updateChildren(container, children, currentPage);
    updatePaginator(paginator, children, currentPage);
    updateToolbar();

    function updateChildren(container, children, currentPage) {
      var start = (jp.sort.childrenPerPage * (currentPage - 1));
      var end = Math.min(start + jp.sort.childrenPerPage, children.length);
      // add children
      for(var i = start; i < end; i++) {
        addChild(container, children[i]);
      }
    }

    function updatePaginator(paginator, children, currentPage) {
      var numPages = Math.ceil(children.length / jp.sort.childrenPerPage);
      var startPage = Math.max(currentPage - (Math.max(currentPage - numPages + 3, 1)), 2);
      var endPage = Math.min(currentPage + (Math.max(4 - currentPage, 1)), numPages - 1);
      
      addPaginatorPage(paginator, 1, currentPage);
      if(currentPage >= 4) {
        paginator.append("<li class='plain'><span>...</span></li>");
      }
      for(var pageNumber = startPage; pageNumber <= endPage; pageNumber++) {
        addPaginatorPage(paginator, pageNumber, currentPage);
      }
      if(numPages - currentPage >= 3) {
        paginator.append("<li class='plain'><span>...</span></li>");
      }
      addPaginatorPage(paginator, numPages, currentPage);
    }

    function updateToolbar() {
      var child = jp.sort.getChild(jp.sort.selectedChildId);
      var value = child != null ? jp.sort.children.indexOf(child) : null;
      $("#childOrderPosition").val(value);
    }

    function addChild(container, child) {
      var activeClass = "class='jp-sort-editor-child list-group-item ";
      activeClass += child.id;
      var selected = jp.sort.selectedChildId;
      activeClass += (selected == child.id) ? " active" : "";
      activeClass += "'";
      var onclick = "onclick='jp.sort.selectChild(`" + child.id + "`)'";
      container.append("<a href='#' " + activeClass + " " + onclick + ">" + child.maintitle + "</div>");
    }

    function addPaginatorPage(paginator, page, currentPage) {
      var activeClass = (page == currentPage ? "class='active'" : "");
      var onclick = "onclick='jp.sort.changePage(" + page + ")'";
      paginator.append("<li " + activeClass + "><a href='#' " + onclick + ">" + page + "</a></li>");
    }
  },

  selectChild: function(id) {
    $(".jp-sort-editor-child").removeClass("active");
    $("." + id).addClass("active");
    var child = jp.sort.getChild(id);
    jp.sort.selectedChildId = null;
    $("#childOrderPosition").val(jp.sort.children.indexOf(child));
    jp.sort.selectedChildId = id;
  },

  getChild: function(id) {
    if(id == null) {
      return null;
    }
    for(child of jp.sort.children) {
      if(child.id == id) {
        return child;
      }
    }
    return null;
  },

  updateChildrenToolbar: function() {
    var disabled = jp.sort.selectedSorter != null ? "disabled" : null;
    $("#childrenToolbar button").attr("disabled", disabled);
    $("#childrenToolbar input").attr("disabled", disabled);
  },

  changePage: function(page) {
    jp.sort.childrenPage = page;
    jp.sort.renderChildren();
  },

  decrementChildOrder: function() {
    var pos = jp.sort.getPositionOfSelectedChild();
    if(jp.sort.swapChildren(pos, pos - 1)) {
      jp.sort.jumpToChild(pos - 1);
    }
  },

  incrementChildOrder: function() {
    var pos = jp.sort.getPositionOfSelectedChild();
    if(jp.sort.swapChildren(pos, pos + 1)) {
      jp.sort.jumpToChild(pos + 1);
    }
  },

  onChildOrderChange: function() {
    var pos = jp.sort.getPositionOfSelectedChild();
    var newPos = jp.sort.getPositionOfInput();
    if(jp.sort.swapChildren(pos, newPos)) {
      jp.sort.jumpToChild(newPos);
    }
  },

  jumpToChild: function(pos) {
    var oldPage = jp.sort.childrenPage;
    var newPage = Math.ceil((pos + 1) / jp.sort.childrenPerPage);
    if(oldPage != newPage) {
      jp.sort.changePage(newPage);
    } else {
      jp.sort.renderChildren();
    }
  },

  swapChildren: function(pos1, pos2) {
    if(pos1 == null || pos2 == null || (typeof pos1 != "number") || (typeof pos2 != "number")) {
      return false;
    }
    var size = jp.sort.children.length;
    if(pos1 < 0 || pos1 >= size || pos2 < 0 || pos2 >= size) {
      return false;
    }
    jp.sort.swapArrayPosition(jp.sort.children, pos1, pos2);
    return true;
  },

  getPositionOfSelectedChild: function() {
    if(jp.sort.selectedChildId == null) {
      return;
    }
    var child = jp.sort.getChild(jp.sort.selectedChildId);
    return jp.sort.children.indexOf(child);
  },

  getPositionOfInput: function() {
    var pos = $("#childOrderPosition").val();
    if(pos == null) {
      return;
    }
    return parseInt(pos);
  },

  swapArrayPosition: function(list, pos1, pos2) {
    var temp = list[pos1];
    list[pos1] = list[pos2];
    list[pos2] = temp;
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
      // first remove sorter
      $.ajax({
        method: 'DELETE',
        url: jp.baseURL + "rsc/sorter/" + jp.sort.id
      }).done(function() {
        // check if the child order has changed
        if(childOrderChanged()) {
          $.ajax({
            method: 'POST',
            url: jp.baseURL + "rsc/sorter/" + jp.sort.id + "/sort",
            contentType: "application/json; charset=UTF-8",
            data: JSON.stringify(jp.sort.children)
          }).done(onSuccess).fail(onFail);
        }
      }).fail(onFail);
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

    function childOrderChanged() {
      for(var i = 0; i < jp.sort.children.length; i++) {
        var child = jp.sort.children[i];
        var oldChild = jp.sort.oldChildren[i];
        if(child.id != oldChild.id) {
          return true;
        }
      }
      return false;
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

  getChildren: function(id, callback) {
    var q = "parent:" + id;
    var fl = "id,maintitle";
    var sort = "order+asc";
    var rows = "9999";
    $.getJSON(jp.baseURL + "servlets/search?q=" + q + "&fl=" + fl + "&sort=" + sort + "&rows=" + rows + "&wt=json", function(response) {
      callback(response);
    }, function(error) {
      console.log(error);
      alert("Error while retrieving children of mycore object " + id);
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
