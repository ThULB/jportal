var jp = jp || {};
jp.sort = jp.sort || {

  sorters: {},

  i18nKeys: null,

  addSorter: function(sortClass, defaultOrder) {
    jp.sort.sorters[sortClass] = {
        className: sortClass,
        defaultOrder: defaultOrder
    };
  },

  loadI18nKeys: function() {
    $.getJSON(jp.baseURL + "rsc/locale/translate/" + jp.lang + "/jp.sort.*", function(data) {
      jp.sort.i18nKeys = data;
      jp.sort.updateI18n($("body"));
    });
  },

  updateI18n: function(elm) {
    if(jp.sort.i18nKeys == null) {
      return;
    }
    $(elm).find(".i18n").each(function(i, node) {
      var key = $(node).attr("i18n");
      var i18nKey = jp.sort.i18nKeys[key];
      if (i18nKey != undefined) {
        $(node).html(i18nKey);
      } else {
        $(node).html($(node).attr("i18n-def"));
      }
    });
  },

  beforeSaving: function() {
    $(".jp-sort-body-darken").css("display", "block");
    $(".jp-sort-body").css("pointer-events", "none");
    $(".jp-sort-footer-default").css("display", "none");
    $(".jp-sort-footer-saving").css("display", "block");
  },

  afterSaving: function() {
    $(".jp-sort-body-darken").css("display", "none");
    $(".jp-sort-body").css("pointer-events", "auto");
    $(".jp-sort-footer-default").css("display", "block");
    $(".jp-sort-footer-saving").css("display", "none");
  },

  guid: function() {
    function s4() {
      return Math.floor((1 + Math.random()) * 0x10000)
        .toString(16)
        .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
      s4() + '-' + s4() + s4() + s4();
  }
  
};

jp.sort.object = {

  id: null,

  selectedChildId: null,

  children: [],

  oldChildren: [],

  childrenPage: 1,

  childrenPerPage: 10,

  selectedSorter: null,

  init: function(id) {
    jp.sort.object.id = id;
    jp.sort.object.loadSortContainer();
    jp.sort.loadI18nKeys();
    jp.sort.object.getObject(id, function(object) {
      var sortBy = object.metadata.autosort != null ? object.metadata.autosort.data[0] : null;
      if(sortBy != null) {
        jp.sort.object.selectSorter(sortBy.$text);
        $("#jp-sort-order-select").val(sortBy._order);
      }
    });
    jp.sort.object.getChildren(id, function(data) {
      jp.sort.object.reloadChildren(data.response);
    });
  },

  show: function() {
    $("#jp-sort-object-dialog").modal("show");
  },

  loadSortContainer: function() {
    var container = $("#autoSortContainer");
    for(let key in jp.sort.sorters) {
      var className = key.substring(key.lastIndexOf(".") + 1, key.length);
      var btn = "<a href='#' type='button' class='jp-sort-object-sorter list-group-item " + className + "' onclick='jp.sort.object.selectSorter(`" + key + "`)'>" +
          "<div class='i18n' i18n='jp.sort." + className + "' style='font-weight: bold;'></div>" +
          "<div class='i18n' i18n='jp.sort." + className + ".description'></div>" +
      	"</a>";
      container.append(btn);
    }
  },

  selectSorter: function(sorterName) {
    $(".jp-sort-object-sorter").removeClass("active");
    if(sorterName == null) {
      $(".jp-sort-object-sorter-no-sorter").addClass("active");
      jp.sort.object.selectedSorter = null;
      jp.sort.object.updateChildrenToolbar();
      return;
    }
    jp.sort.object.selectedSorter = sorterName;
    var className = sorterName.substring(sorterName.lastIndexOf(".") + 1, sorterName.length);
    $("." + className).addClass("active");
    jp.sort.object.updateChildrenToolbar();
  },

  reloadChildren: function(solrResponse) {
    var docs = solrResponse.docs;
    jp.sort.object.children = [];
    jp.sort.object.oldChildren = [];
    for(var doc of docs) {
      jp.sort.object.oldChildren.push(doc);
      jp.sort.object.children.push(doc);
    }
    jp.sort.object.childrenPage = 1;
    jp.sort.object.renderChildren();
  },

  renderChildren: function() {
    var container = $("#childrenContainer");
    var paginator = $("#childrenPaginator");
    container.empty();
    paginator.empty();

    var children = jp.sort.object.children;
    if(children.length == 0) {
      container.html("Keine Kinder gefunden.");
      return;
    }

    var currentPage = jp.sort.object.childrenPage;

    updateChildren(container, children, currentPage);
    updatePaginator(paginator, children, currentPage);
    updateToolbar();

    function updateChildren(container, children, currentPage) {
      var start = (jp.sort.object.childrenPerPage * (currentPage - 1));
      var end = Math.min(start + jp.sort.object.childrenPerPage, children.length);
      // add children
      for(var i = start; i < end; i++) {
        addChild(container, children[i]);
      }
    }

    function updatePaginator(paginator, children, currentPage) {
      var numPages = Math.ceil(children.length / jp.sort.object.childrenPerPage);
      var startPage = Math.max(currentPage - (Math.max(currentPage - numPages + 3, 1)), 2);
      var endPage = Math.min(currentPage + (Math.max(4 - currentPage, 1)), numPages - 1);
      
      addPaginatorPage(paginator, 1, currentPage);
      if(endPage > 1) {
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
    }

    function updateToolbar() {
      var child = jp.sort.object.getChild(jp.sort.object.selectedChildId);
      var value = child != null ? jp.sort.object.children.indexOf(child) : null;
      $("#childOrderPosition").val(value);
    }

    function addChild(container, child) {
      var activeClass = "class='jp-sort-object-child list-group-item ";
      activeClass += child.id;
      var selected = jp.sort.object.selectedChildId;
      activeClass += (selected == child.id) ? " active" : "";
      activeClass += "'";
      var onclick = "onclick='jp.sort.object.selectChild(`" + child.id + "`)'";
      container.append("<a href='#' " + activeClass + " " + onclick + ">" + child.maintitle + "</div>");
    }

    function addPaginatorPage(paginator, page, currentPage) {
      var activeClass = (page == currentPage ? "class='active'" : "");
      var onclick = "onclick='jp.sort.object.changePage(" + page + ")'";
      paginator.append("<li " + activeClass + "><a href='#' " + onclick + ">" + page + "</a></li>");
    }
  },

  selectChild: function(id) {
    $(".jp-sort-object-child").removeClass("active");
    $("." + id).addClass("active");
    var child = jp.sort.object.getChild(id);
    jp.sort.object.selectedChildId = null;
    $("#childOrderPosition").val(jp.sort.object.children.indexOf(child));
    jp.sort.object.selectedChildId = id;
  },

  getChild: function(id) {
    if(id == null) {
      return null;
    }
    for(child of jp.sort.object.children) {
      if(child.id == id) {
        return child;
      }
    }
    return null;
  },

  updateChildrenToolbar: function() {
    var disabled = jp.sort.object.selectedSorter != null ? "disabled" : null;
    $("#childrenToolbar button").attr("disabled", disabled);
    $("#childrenToolbar input").attr("disabled", disabled);
  },

  changePage: function(page) {
    jp.sort.object.childrenPage = page;
    jp.sort.object.renderChildren();
  },

  decrementChildOrder: function() {
    var pos = jp.sort.object.getPositionOfSelectedChild();
    if(jp.sort.object.swapChildren(pos, pos - 1)) {
      jp.sort.object.jumpToChild(pos - 1);
    }
  },

  incrementChildOrder: function() {
    var pos = jp.sort.object.getPositionOfSelectedChild();
    if(jp.sort.object.swapChildren(pos, pos + 1)) {
      jp.sort.object.jumpToChild(pos + 1);
    }
  },

  onChildOrderChange: function() {
    var pos = jp.sort.object.getPositionOfSelectedChild();
    var newPos = jp.sort.object.getPositionOfInput();
    if(jp.sort.object.swapChildren(pos, newPos)) {
      jp.sort.object.jumpToChild(newPos);
    }
  },

  jumpToChild: function(pos) {
    var oldPage = jp.sort.object.childrenPage;
    var newPage = Math.ceil((pos + 1) / jp.sort.object.childrenPerPage);
    if(oldPage != newPage) {
      jp.sort.object.changePage(newPage);
    } else {
      jp.sort.object.renderChildren();
    }
  },

  swapChildren: function(pos1, pos2) {
    if(pos1 == null || pos2 == null || (typeof pos1 != "number") || (typeof pos2 != "number")) {
      return false;
    }
    var size = jp.sort.object.children.length;
    if(pos1 < 0 || pos1 >= size || pos2 < 0 || pos2 >= size) {
      return false;
    }
    jp.sort.object.swapArrayPosition(jp.sort.object.children, pos1, pos2);
    return true;
  },

  getPositionOfSelectedChild: function() {
    if(jp.sort.object.selectedChildId == null) {
      return;
    }
    var child = jp.sort.object.getChild(jp.sort.object.selectedChildId);
    return jp.sort.object.children.indexOf(child);
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
    jp.sort.beforeSaving();
    if(jp.sort.object.selectedSorter != null) {
      var sorterClass = jp.sort.object.selectedSorter;
      var order = $("#jp-sort-order-select").val();
      $.post(jp.baseURL + "rsc/sort/sortby/" + jp.sort.object.id + "?sorter=" + sorterClass + "&order=" + order)
        .done(onSuccess)
        .fail(onFail);
    } else {
      // first remove sorter
      $.ajax({
        method: 'DELETE',
        url: jp.baseURL + "rsc/sort/sortby/" + jp.sort.object.id
      }).done(function() {
        // check if the child order has changed
        if(childOrderChanged()) {
          $.ajax({
            method: 'POST',
            url: jp.baseURL + "rsc/sort/resort/" + jp.sort.object.id,
            contentType: "application/json; charset=UTF-8",
            data: JSON.stringify(jp.sort.object.children)
          }).done(onSuccess).fail(onFail);
        }
      }).fail(onFail);
    }

    function childOrderChanged() {
      for(var i = 0; i < jp.sort.object.children.length; i++) {
        var child = jp.sort.object.children[i];
        var oldChild = jp.sort.object.oldChildren[i];
        if(child.id != oldChild.id) {
          return true;
        }
      }
      return false;
    }

    function onSuccess() {
      jp.sort.afterSaving();
      $("#jp-sort-object-dialog").modal("hide");
    }

    function onFail(error) {
      jp.sort.afterSaving();
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

}

jp.sort.level = {

  id: null,
  
  model: [],
  
  index: 0,

  init: function(id) {
    jp.sort.level.id = id;
    jp.sort.loadI18nKeys();
    jp.sort.level.getLevelSorting(id, function(rsp) {
      if(rsp.isNew) {
        $(".jp-sort-level-info").css("display", "block");
      }
      jp.sort.level.model = jp.sort.level.buildFromResponse(rsp);
      jp.sort.level.render();
    });
  },

  buildFromResponse: function(rsp) {
    var model = [];
    for(var level of rsp.levels) {
      model.push(level);
      level.id = jp.sort.guid();
      jp.sort.level.updateOrder(level);
    }
    return model;
  },

  show: function() {
    $("#jp-sort-level-dialog").modal("show");
  },

  getLevelSorting: function(id, callback) {
    $.getJSON(jp.baseURL + "rsc/sort/level/" + id, function(data) {
      callback(data);
    }, function(error) {
      console.log(error);
      alert("Error while loading level sorting object of " + id);
    });
  },
  
  addRow: function(name) {
    jp.sort.level.model.push({
      id: jp.sort.guid(),
      name: name
    });
    jp.sort.level.render();
  },

  render: function() {
    $(".jp-sort-level-row").remove();
    for(var row of jp.sort.level.model) {
      jp.sort.level.renderRow(row);
    }
  },

  renderRow: function(row) {
    var container = $("#jp-sort-level-table-body");
    var tr = "<tr class='jp-sort-level-row'>";
    tr += "<td><input class='form-control' type='text' value='" + row.name + "'></input></td>";
    tr += "<td><select class='form-control' id='jp-sort-level-classSelect-" + row.id + "'";
    tr += "onchange='jp.sort.level.onChangeClass(`" + row.id + "`)'>";
    tr += "<option class='i18n' i18n='jp.sort.object.manualSort' value=''></option>";
    for(let sorter in jp.sort.sorters) {
      var className = sorter.substring(sorter.lastIndexOf(".") + 1, sorter.length);
      tr += "<option class='i18n' i18n='jp.sort." + className + "' value='" + sorter + "'";
      tr += sorter == row.sorter ? " selected='selected'" : "";
      tr += "></option>";
    }
    tr += "</select></td>";
    tr += "<td>";
    if(row.order != null) {
      tr += "<select class='form-control' id='jp-sort-level-orderSelect-" + row.id + "'";
      tr += "onchange='jp.sort.level.onChangeOrder(`" + row.id + "`)'>";
      for(var order of ["ascending", "descending"]) {
        tr += "<option class='i18n' i18n='jp.sort." + order + "' value='" + order + "'";
        tr += order == row.order ? " selected='selected'" : "";
        tr += "></option>";
      }
    }
    tr += "</td>";
    tr += "<td><button type='button' class='btn btn-default btn-sm' onclick='jp.sort.level.removeRow(`" + row.id + "`)'>";
    tr += "<i class='fa fa-minus' aria-hidden='true'></i>";
    tr += "</button></td>";
    tr += "</tr>";
    container.append(tr);
    jp.sort.updateI18n(container);
  },

  getRow: function(id) {
    for(var row of jp.sort.level.model) {
      if(row.id == id) {
        return row;
      }
    }
    return null;
  },

  removeRow: function(id) {
    var row = jp.sort.level.getRow(id);
    var index = jp.sort.level.model.indexOf(row);
    jp.sort.level.model.splice(index, 1);
    jp.sort.level.render();
  },

  onChangeClass: function(id) {
    var row = jp.sort.level.getRow(id);
    var value = $("#jp-sort-level-classSelect-" + id).val();
    row.sorter = value != "" ? value : null;
    jp.sort.level.updateOrder(row);
    jp.sort.level.render();
  },

  onChangeOrder: function(id) {
    var row = jp.sort.level.getRow(id);
    var value = $("#jp-sort-level-orderSelect-" + id).val();
    row.order = value;
    jp.sort.level.render();
  },

  updateOrder: function(row) {
    var sorterClass = row.sorter;
    if(sorterClass == null) {
      row.order = null;
      return;
    }
    var sorter = jp.sort.sorters[sorterClass];
    var defaultOrder = sorter.defaultOrder;
    if(defaultOrder == null || defaultOrder == "none") {
      row.order = null;
      return;
    }
    if(row.order == null) {
      row.order = defaultOrder;
    }
  },

  buildLevels: function() {
    var levels = [];
    jp.sort.level.model.forEach(function(row, arrayIndex) {
      var level = {
        index: arrayIndex,
        name: row.name
      };
      if(row.sorter && row.sorter != '') {
        level.sorter = row.sorter;
      }
      if(row.order && row.order != '') {
        level.order = row.order;
      }
      levels.push(level);
    });
    return levels;
  },

  save: function(apply) {
    var levels = jp.sort.level.buildLevels();
    jp.sort.beforeSaving();
    $.ajax({
      method: 'POST',
      url: jp.baseURL + "rsc/sort/level/" + jp.sort.level.id + "?apply=" + apply,
      contentType: "application/json; charset=UTF-8",
      data: JSON.stringify(levels)
    }).done(function() {
      jp.sort.afterSaving();
      $("#jp-sort-level-dialog").modal("hide");
    }).fail(function(error) {
      jp.sort.afterSaving();
      console.log(error);
      alert("An error occur. Unable to save changes.");
    });
  },

  saveAndApply: function() {
    var title = jp.sort.i18nKeys["jp.sort.level.reviewDialog.title"];
    var message = jp.sort.i18nKeys["jp.sort.level.reviewDialog.message"];
    var yes = jp.sort.i18nKeys["jp.sort.button.yes"];
    var no = jp.sort.i18nKeys["jp.sort.button.no"];

    new BootstrapDialog({
      title: title,
      message: message,
      buttons: [{
        label: no,
        action: function(dialog) {
          dialog.close();
        }
      }, {
        label: yes,
        cssClass: 'btn-warning',
        action: function(dialog) {
          dialog.close();
          jp.sort.level.save(true);
        }
      }]
    }).open();
  }

}

$(document).ready(function() {

  var dialogs = ["object", "level"];

  for(var dialog of dialogs) {
    $("body").on("click", "#jp-sort-" + dialog + "-show-button", function() {
      var dialog = $(this).attr("dialog");
      var mcrid = $(this).attr("mcrid");
      showAndOrLoadDialog(dialog, mcrid);
    });
  }

  function showAndOrLoadDialog(dialog, mcrID) {
    if ($("#jp-sort-" + dialog + "-dialog").length < 1) {
      $.ajax({
        url: jp.baseURL + "html/jp-sort-" + dialog + ".html",
        type: "GET",
        dataType: "html"
      }).done(function(data) {
        loadDialog(dialog, mcrID, data);
        showDialog(dialog, mcrID);
      }).fail(function(error) {
        alert(error);
      });
    } else {
      showDialog(dialog, mcrID);
    }
  }

  function loadDialog(dialog, mcrID, data) {
    var html = $("<div></div>").append(data).find("#jp-sort-" + dialog + "-dialog");
    loadCSS();
    $("body").append(html);
    jp.sort[dialog].init(mcrID);
  }

  function showDialog(dialog, mcrID) {
    jp.sort[dialog].show(mcrID);
  }

  function loadCSS() {
    var path = jp.baseURL + "css/jp-sort.css";
    if (!$("link[href='" + path + "']").length) {
      $("head").append("<link href='"+ path + "' rel='stylesheet' type='text/css'>");
    }
  }

});
