
/**
 * @property objectType
 * @property maintitle
 * @property childrenCount
 * @property derivateOwner
 */
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
};

TreeEntry.prototype.getChilds = function(start, callback) {
	var entry = this;
	$.getJSON(jp.baseURL + "servlets/solr/select?q=%2Bparent%3A" + entry.docID + "&start=" + start + "&rows=100&wt=json&sort=maintitle%20asc&wt=json", function(search){
		if(search.response.numFound > 0){
			var results = search.response.docs;
			$.each(results, function(i, result){
				var bla = new TreeEntry(result, "");
				entry.childs.push(bla);
			});
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
	$.getJSON(jp.baseURL + "servlets/solr/select?q=%2BderivateOwner%3A" + entry.docID + "&start=" + start + "&rows=100&wt=json&sort=maintitle%20asc&wt=json", function(search){
		if(search.response.numFound > 0){
			var results = search.response.docs;
			$.each(results, function(i, result){
				var bla = new TreeEntry(result, "");
				entry.deris.push(bla);
			});
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
};
