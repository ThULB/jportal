function UploadEntry(docID, deriID, path, file) {
    this.uploadID = null;
    this.docID = docID;
    this.deriID = deriID;
    this.path = path;
    this.name = file.name;
    this.size = derivateBrowserTools.getReadableSize(file.size, 0);
    this.rawSize = file.size;
    this.type = file.type;
    this.lastmodified = file.lastModified !== undefined ?
        moment(file.lastModified).format("DD.MM.YYYY HH:mm:ss") : undefined;
    this.file = file;
    this.exists = undefined;
    this.img = undefined;
    this.statusbar = undefined;
    //noinspection JSUnusedGlobalSymbols
    this.inFolder = false;
    this.checkedFile = undefined;
    this.md5 = "";
}

UploadEntry.prototype.setUploadID = function (uploadID) {
    this.uploadID = uploadID;
};

UploadEntry.prototype.getStatus = function () {
    var template = $("#upload-entry-template").html();
    var status = $(Mustache.render(template, this));
    readImg(this.file, $(status).find("img.upload-preview-image"), this);
    return status;
};

UploadEntry.prototype.getFormData = function() {
	if (this.exists === undefined) return undefined;
    const data = new FormData();
    data.append("uploadID", this.uploadID);
    data.append("documentID", this.docID);
    data.append("derivateID", this.deriID);
    data.append("path", this.path);
    data.append("filename", this.name);
    data.append("size", this.rawSize);
    data.append("file", this.file);
    data.append("overwrite", this.exists);
    data.append("type", this.type);
    return data;
};

UploadEntry.prototype.getID = function() {
	if (this.path === ""){
		return this.docID + "/" + this.name;
	}
    return this.docID + this.path + "/" + this.name;
};

UploadEntry.prototype.getCompletePath = function() {
    return this.path.trim() + "/" + this.name;
};

UploadEntry.prototype.isInFolder = function() {
    //noinspection JSUnusedGlobalSymbols
    this.inFolder = true;
};

UploadEntry.prototype.getCheckJson = function () {
    return {
        file: this.name,
        id: this.getID(),
        fileType: this.file.type
    };
};

UploadEntry.prototype.getaddToBrowserJson = function () {
    const currentDate = moment();
    return {
        name: this.name,
        size: this.rawSize,
        lastmodified: currentDate.format("DD.MM.YYYY HH:mm:ss"),
        absPath: this.getCompletePath(),
        deriID: this.docID,
        md5: this.md5,
        urnEnabled: $("#btn-urnAll").data("urnEnabled")
    };
};

UploadEntry.prototype.askOverwrite = function(existingFile, deriID, path) {
	existingFile.deriID = deriID;
	existingFile.path = path;
	const uploadOverwriteTemplate = $("#upload-overwrite-template").html();
	const originalFileOutput = $(Mustache.render(uploadOverwriteTemplate, existingFile));
	derivateBrowserTools.setImgPath($(originalFileOutput).find(".overwrite-img"), deriID, path + "/" + existingFile.name);
	$(originalFileOutput).find(".img-size").html(derivateBrowserTools.getReadableSize($(originalFileOutput).find(".img-size").html(),0));
	$("#lightbox-upload-overwrite-original-file").html(originalFileOutput);
	
	var newFileOutput = $(Mustache.render(uploadOverwriteTemplate, this));
	readImg(this.file, $(newFileOutput).find("img.overwrite-img"), this);
	$("#lightbox-upload-overwrite-new-file").html(newFileOutput);
	$("#lightbox-upload-overwrite-filename").html(existingFile.name);
	showModalWhenReady();
};

function readImg(file, display, upload) {
    var supportedImg = ["jpeg", "png", "tiff", "gif", "bmp"];
    var type = upload.type.substr(upload.type.lastIndexOf("/")+1);
	if (!upload.type.endsWith("pdf")){
		if ((file.size < 2097152) && supportedImg.indexOf(type) > -1){
			if (upload.img !== undefined){
				display.attr("src", upload.img);
				$(display).siblings(".img-placeholder").addClass("hidden");
				$(display).removeClass("hidden");
			} else{
				var reader = new FileReader();
				reader.onload =  function() {
					display.attr("src", reader.result);
					upload.img = reader.result;
					$(display).siblings(".img-placeholder").addClass("hidden");
					$(display).removeClass("hidden");
				};
				reader.readAsDataURL(file);
			}
		} else {
            display.attr("src", jp.baseURL + "images/file-logo.svg");
            $(display).siblings(".img-placeholder").addClass("hidden");
            $(display).removeClass("hidden");
        }
	} else {
		display.attr("src", jp.baseURL + "images/adobe-logo.svg");
		$(display).siblings(".img-placeholder").addClass("hidden");
		$(display).removeClass("hidden");
	}
}

function showModalWhenReady() {
    const uploadOverwrite = $('#lightbox-upload-overwrite');
    if (uploadOverwrite.data("open")) {
        uploadOverwrite.data("openagain", true);
    } else {
        uploadOverwrite.modal('show');
    }
}
