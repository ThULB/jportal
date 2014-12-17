function TreeEntry(data, pa) {
	this.docID = data.id;
	this.type = data.objectType;
	this.parentID = data.parent;
	this.title = data.maintitle;
	this.childCount = data.childrenCount;
	this.deriCount = data.derivateCount;
	this.journalID = data.journalID;
	if (data.objectType == "derivate"){
		this.parentID = data.derivateOwner;
		this.title = data.id;
		this.journalID = ""
	}
	if (data.objectType == "jpjournal"){
		this.parentID = data.id;
		this.journalID = "";
	}
	if (pa != undefined){
		this.path = pa;
	}
	else{
		this.path = "";
	}
	this.childs = [];
	this.deris = [];
}

TreeEntry.prototype.setChilds = function(cs) {
	this.childs = cs;
}

TreeEntry.prototype.setDeris = function(ds) {
	this.deris = ds;
}

TreeEntry.prototype.getChildsAndDeris = function(callback) {
	if (this.childCount != undefined && this.childCount > 0 && this.childs.length == 0){
		this.getChilds(0, callback);
		return true;
	}
	if (this.deriCount != undefined && this.deriCount > 0 && this.deris.length == 0){
		this.getDeris(0, callback);
		return true;
	}
	callback(this.parentID, this.type);
}

TreeEntry.prototype.draw = function(callback) {
	var entry = this;
	
	var node = $(".folder").filter(function() {
		return $(this).data("docID") == entry.docID;
	});
	if (node.length == 0){
		var li = $("<li class='folder'><div class='folder-name'>" + this.title + "</div></li>");
		if (this.type == "jpjournal"){
			$(li).addClass("journal");
			$(li).prepend("<span class='glyphicon glyphicon-book icon'></span>");
		}
		else{
			if (this.type == "derivate"){
				$(li).addClass("derivat");
				$(li).prepend("<span class='glyphicon glyphicon-picture icon'></span>");
				//evtl. ändern
				$(li).data("deriID", this.docID);
			}
			else{
				$(li).prepend("<span class='glyphicon glyphicon-file icon'></span>");
			}
		}
		if((this.childCount > 0 || this.deriCount > 0) && this.type != "derivate"){
			var ul = $("<ul class='children'></ul>");
			$(li).prepend("<span class='glyphicon glyphicon-minus button button-contract'></span>");
			$.each(entry.deris, function(index, value){
				entry.addChildDerivate(ul, value);
			});
			$.each(entry.childs, function(index, value){
				entry.addChildDocs(ul, value);
			});
			$(li).append(ul);
		}
		else{
			$(li).prepend("<div class='no-button'>&nbsp;</div>");
		}
		$(li).data("docID", this.docID);
//		if (this.path != "" && this.type == "derivate"){
//			$(li).append("<ul class='children'></ul>")
//			var parent = $(li).find("ul.children");
//			var paths = this.path.split("/");
//			var currentPath = "";
//			for (var i = 0; i < paths.length; i++){
//				if (paths[i] != ""){
//					currentPath = currentPath + "/" + paths[i];
//					parent = entry.addFolderToDerivate(parent, currentPath);
//				}
//			}
//			if (highlight){
//				$(".aktiv").removeClass("aktiv");
//				$(li).removeClass("aktiv");
//				$(parent).parent().addClass("aktiv");
//			}
//		}
		if (this.type == "jpjournal"){
			$("#folder-list-ul").append(li);
		}
		else{
			$(".folder").filter(function() {
				return $(this).data("docID") == entry.parentID;
			}).find("ul.children").append(li);
		}
	}
	else{
		expandFolder(node);
	}
	if (this.path != "" && this.type == "derivate"){
		entry.getDeriFolder(node, callback)
	}	
	callback(entry);
}

TreeEntry.prototype.getChilds = function(start, callback) {
	var entry = this;
	$.getJSON("/servlets/solr/select?q=%2Bparent%3A" + entry.docID + "&start=" + start + "&rows=100&wt=json&sort=maintitle%20asc&wt=json", function(search){
		if(search.response.numFound > 0){
			var results = search.response.docs;
			for (result in results){
				var bla = new TreeEntry(results[result])
				entry.childs.push(bla);
			}
			if(search.response.numFound > 100 && search.response.start < search.response.numFound){
				entry.getChilds(start + 100);
			}
			else{
				entry.getChildsAndDeris(callback);
			}
		}
	});
};

TreeEntry.prototype.getDeris = function(start, callback) {
	var entry = this;
	$.getJSON("/servlets/solr/select?q=%2BderivateOwner%3A" + entry.docID + "&start=" + start + "&rows=100&wt=json&sort=maintitle%20asc&wt=json", function(search){
		if(search.response.numFound > 0){
			var results = search.response.docs;
			for (result in results){
				var bla = new TreeEntry(results[result])
				entry.deris.push(bla);
			}
			if(search.response.numFound > 100 && search.response.start < search.response.numFound){
				entry.getChilds(start + 100);
			}
			else{
				entry.getChildsAndDeris(callback);
			}
		}
	});
};

TreeEntry.prototype.addFolderToDerivate = function(parent, data) {
	var entry = this;
	var li = $("<li class='derivat-folder'><div class='folder-name'>" + data.name + "</div></li>");
	$(li).prepend("<span class='glyphicon glyphicon-folder-close icon'></span>");
	$(li).prepend("<div class='no-button'>&nbsp;</div>");
	$(li).append("<ul class='children'></ul>");
	$(li).data("id", this.docID + data.absPath);
	$(li).data("deriID", this.docID);
	$(li).data("path", data.absPath);
	$(parent.children("ul.children")).append(li);
	if (data.hasChildren){
		$.each(data.children, function(index, value) {
			entry.addFolderToDerivate(li, value);
		});
	}
//	return $(li).find("ul.children");
};

TreeEntry.prototype.addChildDerivate = function(parent, deri) {
	var li = $("<li class='folder derivat'><div class='folder-name'>" + deri.id + "</div></li>");
	$(li).prepend("<span class='glyphicon glyphicon-picture icon'></span>");
	$(li).prepend("<div class='no-button'>&nbsp;</div>");
	$(li).append("<ul class='children'></ul>");
	$(li).data("deriID", deri.id);
	$(li).data("docID", deri.id);
	$(parent).append(li);
};

TreeEntry.prototype.addChildDocs = function(parent, doc) {
	var li = $("<li class='folder'><div class='folder-name'>" + doc.title + "</div></li>");
	$(li).prepend("<span class='glyphicon glyphicon-file icon'></span>");
	if(doc.childCount > 0 || doc.deriCount > 0){
		$(li).prepend("<span class='glyphicon glyphicon-plus button button-expand'></span>");
		var ul = $("<ul class='hide-folder children'></ul>");
		$(li).append(ul);
	}
	else{
		$(li).prepend("<div class='no-button'>&nbsp;</div>");
	}
	$(li).data("docID", doc.docID);
	$(parent).append(li);
};

TreeEntry.prototype.getDeriFolder = function(parent, callback) {
	var entry = this;
	$.ajax({
		url: "/rsc/derivatebrowser/folders/" + entry.docID,
		type: "GET",
		dataType: "json",
		success: function(data) {
					if (data.hasChildren){
						$.each(data.children, function(index, value) {
							entry.addFolderToDerivate(parent, value);
						});
					}
					callback(entry);
				},
		error: function(error) {
					alert(error);
				}
	});
}

function expandFolder(node) {
	var button = $(node).children(".button");
	if (button.hasClass("button-expand")){
		button.siblings("ul.children").removeClass("hide-folder");
		button.removeClass("button-expand glyphicon-plus");
		button.addClass("button-contract glyphicon-minus");
	}
}