dojo.provide("dojoclasses.SimpleRESTStore");
dojo.require("dojo.data.ItemFileWriteStore");

dojo.declare("dojoclasses.SimpleRESTStore", dojo.data.ItemFileWriteStore, {

	constructor: function(/* object */ keywordParameters){
		this.targetURL = keywordParameters.targetURL;
	},

	getValues: function(/* item */ parent, /* attribute-name-string */ attribute) {
		var array = this.inherited(arguments);

		if(array[0] == true) {
			var url = this.targetURL;
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
						var newItem = this.newItem(child, {parent: parent});
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
		return array;
	},

});