function Upload(docID, deriID, path, file) {
	this.docID = docID;
	this.deriID = deriID;
	this.path = path;
	this.name = file.name;
	this.size = getReadableSize(file.size, 0);
	this.rawSize = file.size;
	this.lastmodified = file.lastModifiedDate.toLocaleDateString() + " " + file.lastModifiedDate.toLocaleTimeString();
	this.file = file;
	this.exists = undefined;
	this.img = undefined;
	this.statusbar = undefined;
}

Upload.prototype.getStatus = function() {
	var template = $("#upload-entry-template").html();
	var status = $(Mustache.render(template, this));
	if (this.file.type.match(/image.*/)){
		readImg(this.file, $(status).find("img.upload-preview-image"));
	}
	return status;
}

Upload.prototype.getFormData = function() {
	if (this.exists == undefined) return undefined;
    var data = new FormData();
    data.append("documentID", this.docID);
    data.append("derivateID", this.deriID);
    data.append("path", this.path);
    data.append("size", this.rawSize);
    data.append("file", this.file);
	data.append("overwrite", this.exists);
    return data;
}

Upload.prototype.getID = function() {
	if (this.path == ""){
		return this.deriID + "/" + this.name;
	}
    return this.deriID + this.path + "/" + this.name;
}

Upload.prototype.getCompletePath = function() {
    return this.path + "/" + this.name;
}

Upload.prototype.getCheckJson = function() {
	var json ={
			file: this.name,
			id: this.getID(),
			fileType: this.file.type
	}
	return json;
}

Upload.prototype.getaddToBrowserJson = function() {
	var currentDate = new Date()
	var json ={
			name: this.name,
			size: this.rawSize,
			lastmodified: currentDate.toLocaleDateString() + " " + currentDate.toLocaleTimeString(),
			absPath: this.getCompletePath(),
			deriID: this.deriID
	}
	return json;
}

Upload.prototype.askOverwrite = function(existingFile, deriID, path) {
	existingFile.deriID = deriID;
	existingFile.path = path;
	var uploadOverwriteTemplate = $("#upload-overwrite-template").html();
	var originalFileOutput = $(Mustache.render(uploadOverwriteTemplate, existingFile));
	//var newFileOutput = $(Mustache.render(fileEntryTemplate, file));
	$(originalFileOutput).find(".img-size").html(getReadableSize($(originalFileOutput).find(".img-size").html(),0));
	$("#lightbox-upload-overwrite-original-file").html(originalFileOutput);
	
	var newFileOutput = $(Mustache.render(uploadOverwriteTemplate, this));
	readImg(this.file, $(newFileOutput).find("img.overwrite-img"));
	$("#lightbox-upload-overwrite-new-file").html(newFileOutput);
	$("#lightbox-upload-overwrite-filename").html(existingFile.name);
	showModalWhenReady();
}


function getReadableSize(size, unit) {
	var conSize = convertSize({number: size, unit: unit});
	var unitString = "";
	switch (conSize.unit){
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

function convertSize(sizeAndUnit){
	if (sizeAndUnit.unit < 3){
		if (sizeAndUnit.number > 1024){
			var size2 = Math.round((sizeAndUnit.number / 1024) * 100)/ 100;
			return convertSize({number: size2, unit: sizeAndUnit.unit + 1});
		}
	}
	return {number: sizeAndUnit.number, unit: sizeAndUnit.unit};
}

function readImg(file, display) {
	if (this.img != undefined){
		display.attr("src", this.img);
	}
	else{
		var reader = new FileReader();
		reader.onload =  function(e) {
			display.attr("src", reader.result);
			this.img = reader.result;
		}
		reader.readAsDataURL(file);
	}
}

function showModalWhenReady(){
	if ($('#lightbox-upload-overwrite').data("open")){
		$('#lightbox-upload-overwrite').data("openagain", true);
	}
	else{
		$('#lightbox-upload-overwrite').modal('show');
	}
}