function LargeViewEntry(docID, path, size, lastModified, urn, start) {
    this.docID = docID;
    this.path = path;
    this.name = path.substr(path.lastIndexOf("/") + 1);
    this.size = derivateBrowserTools.getReadableSize(size, 0);
    this.lastModified = lastModified;
    this.urn = urn;
    this.linkedDocs = undefined;
    this.selected = false;
    this.start = start;
}

LargeViewEntry.prototype.setStatusTo = function(node) {
    if (this.linkedDocs == undefined){
        return getLinkedDocs(this, node, getTemplate);
    }
    else{
        return getTemplate(this, node);
    }
};

LargeViewEntry.prototype.getLargePath = function() {
    if (this.name.endsWith("pdf")){
        return jp.baseURL + "img/pdfthumb/" + this.docID + this.path;
    }
    else{
        return jp.baseURL + "servlets/MCRTileCombineServlet/MAX/" + this.docID + this.path;
    }
};

LargeViewEntry.prototype.getMidPath = function() {
    if (this.name.endsWith("pdf")){
        return jp.baseURL + "img/pdfthumb/" + this.docID + this.path;
    }
    else{
        return jp.baseURL + "servlets/MCRTileCombineServlet/MID/" + this.docID + this.path;
    }
};
LargeViewEntry.prototype.getThumpPath = function() {
    if (this.name.endsWith("pdf")){
        return jp.baseURL + "img/pdfthumb/" + this.docID + this.path;
    }
    else{
        return jp.baseURL + "servlets/MCRTileCombineServlet//" + this.docID + this.path;
    }
};

LargeViewEntry.prototype.getID = function() {
    return buildID(this);
};

LargeViewEntry.prototype.changeName = function(newName) {
    this.name = newName;
    this.path = this.path.substr(0, this.path.lastIndexOf("/") + 1) + newName;
};

function getTemplate(entry, node) {
    var template = $("#large-view-status-template").html();
    $(node).html(Mustache.render(template, entry));
}

function buildID(entry) {
    return entry.docID + entry.path;
}

function getLinkedDocs(entry, node, callback) {
    entry.linkedDocs = [];
    var url = jp.baseURL + "servlets/solr/select?q=derivateLink%3A" + buildID(entry) + "&start=0&rows=100&sort=maintitle+asc&wt=json&indent=true";
    $.getJSON(url, function(search) {
        if (search.response.numFound > 0){
            $.each(search.response.docs, function(index, value) {
                var doc = {};
                doc.id = value.id;
                doc.name = value.maintitle;
                entry.linkedDocs.push(doc);
            });
        }
        return callback(entry, node);
    });
}