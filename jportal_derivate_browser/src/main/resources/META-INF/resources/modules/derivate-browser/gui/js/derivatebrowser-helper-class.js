var DerivateFiles = function(){
    this.target = {};
    this.files = [];
}

DerivateFiles.prototype.getTarget = function(){
    return this.target;
}

DerivateFiles.prototype.setTarget = function(target){
    this.target = target;
}

DerivateFiles.prototype.getFiles = function(){
    return this.files;
}

DerivateFiles.prototype.setFiles = function(files){
    this.files = files;
}

DerivateFiles.prototype.add = function(file){
    this.getFiles().push(file);
}

var File = function(derivId, path){
    this.derivId = derivId;
    this.path = path;
    this.type = "";
    this.status = 0;
    this.exists = 0;
    this.lastModifiedTime = "";
    this.size = 0;
    this.URN = "";
}

File.prototype.getDerivId = function(){
    return this.derivId;
}

File.prototype.getPath = function () {
    return this.path;
}

File.prototype.getType = function(){
    return this.type;
}

File.prototype.setType = function(type){
    this.type = type;
}

File.prototype.getStatus = function(){
    return this.status;
}

File.prototype.setStatus = function(status){
    this.status = status;
}

File.prototype.getExists = function(){
    return this.exists;
}

File.prototype.setExists = function(exists){
    this.status = exists;
}

File.prototype.getLastModifiedTime = function(){
    return this.lastModifiedTime;
}

File.prototype.setLastModifiedTime = function(time){
    this.lastModifiedTime = time;
}

File.prototype.getSize = function(){
    return this.size;
}

File.prototype.setSize = function(size){
    this.status = size;
}

File.prototype.getURN = function(){
    return this.URN;
}

File.prototype.setURN = function(urn){
    this.URN = urn;
}

File.prototype.toString = function() {
    return this.derivId + ":" + this.path;
}

File.prototype.equals = function(file) {
    if(file instanceof File){
        return this.derivId === file.getDerivId && this.path === file.getPath
    }

    return false;
}

var MoveDocs = function() {
    this.docs = []
}

MoveDocs.prototype.getDocs = function () {
    return this.docs;
}

MoveDocs.prototype.addDoc = function(objId, newParentId){
    this.getDocs().push(new MoveDoc(objId, newParentId));
}

var MoveDoc = function(objId, newParentId){
    this.objId = objId;
    this.newParentId = newParentId;
    this.success = false;
}

MoveDoc.prototype.getObjId = function () {
    return this.objId;
}

MoveDoc.prototype.getNewParentId = function () {
    return this.newParentId;
}

MoveDoc.prototype.isSuccessful = function () {
    return this.success;
}