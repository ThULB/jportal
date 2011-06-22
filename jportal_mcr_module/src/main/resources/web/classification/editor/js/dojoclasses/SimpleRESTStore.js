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
				var classId = parent.id[0].rootid;
				var categId = parent.id[0].categid;
				array = this.load(classId, categId, parent);
			}
		}
		return array;
	},

	deleteItem: function(/* item */ item){
		this._fetchChildren = false;
		var success = this.inherited(arguments);
		this._fetchChildren = true;
		return success;
	},

	load: function(/*String*/ classId, /*String*/ categId, /*dojo.data.item*/ parent) {
		var url = this.classBaseURL + classId;
		if(categId != null && categId != "") {
			url += "/" + categId;
		}
		var newItems = [];
		var xhrArgs = {
			url :  url,
			sync: true,
			handleAs : "json",
			load : dojo.hitch(this, function(data) {
				var items = data;
				if(!dojo.isArray(items)) {
					items = data.children;
				}
				dojo.forEach(items, function(child) {
					if(parent != null) {
						newItems.push(this.newItem(child, {parent: parent, attribute: "children"}));
					} else {
						newItems.push(this.newItem(child));
					}
				}, this);
				this.save();
			}),
			error : function(error) {
				console.log(error);
			}
		};
		dojo.xhrGet(xhrArgs);
		return newItems;
	}

});