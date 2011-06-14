dojo.provide("dojoclasses.SimpleRESTStore");
dojo.require("dojo.data.ItemFileWriteStore");

dojo.declare("dojoclasses.SimpleRESTStore", dojo.data.ItemFileWriteStore, {

	constructor: function(/* object */ keywordParameters){
		this.classBaseURL = keywordParameters.classBaseURL;
	},

	getValues: function(/* item */ parent, /* attribute-name-string */ attribute) {
		var array = this.inherited(arguments);
		if(attribute == "children") {
			if(parent[attribute] && parent[attribute][0] == true) {
				delete(parent[attribute]);
				var url = this.classBaseURL;
				if(parent.$ref) {
					url += parent.$ref[0];
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

});