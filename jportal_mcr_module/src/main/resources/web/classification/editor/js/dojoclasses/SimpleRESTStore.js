dojo.provide("dojoclasses.SimpleRESTStore");
dojo.require("dojo.data.ItemFileWriteStore");

dojo.declare("dojoclasses.SimpleRESTStore", dojo.data.ItemFileWriteStore, {

	constructor: function(/* object */ keywordParameters){
		this.classBaseURL = keywordParameters.classBaseURL;
	},

	_fetchChildren: true,

	getValues: function(/* item */ parent, /* attribute-name-string */ attribute) {
		var array = this.inherited(arguments);

		if(this._fetchChildren) {
			if(attribute == "children" && parent[attribute] && parent[attribute][0] == true) {
				delete(parent[attribute]);
				var url = this.classBaseURL;
				if(parent.id) {
					url += parent.id[0];
				}
				var xhrArgs = {
					url :  url,
					sync: true,
					handleAs : "json",
					load : dojo.hitch(this, function(data) {
						array = [];
						var children = data.children;
						dojo.forEach(children, function(child) {
							var newItem = this.newItem(child, {parent: parent , attribute: attribute});
							array.push(newItem);
						}, this);
						this.save();
					}),
					error : function(error) {
						console.log(error);
					}
				};
				dojo.xhrGet(xhrArgs);
			}
		}
		return array;
	},

	deleteItem: function(/* item */ item){
		this._fetchChildren = false;
		var success = this.inherited(arguments);
		this._fetchChildren = true;
		return success;
	}

});