
var derivateBrowserNavigation = (function () {

    //private Properties
    var    tempTree = [];


    //private Methods
    function highlight (node) {
        $(".aktiv").removeClass("aktiv");
        $(node).addClass("aktiv");
    }
    
    function highlightCurrent () {
    	highlight(findDoc(derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath()));
    }

    function pushDocAndGetChilds (response) {
        if (response.numFound > 0){
            var doc = response.docs[0],
                entry = new TreeEntry(doc, derivateBrowserTools.getCurrentPath());
            tempTree.push(entry);
            entry.getChildsAndDeris(getNextDoc);
        }
    }

    function getNextDoc(docID, docType) {
       if (docType !== "jpjournal") {
           getDocPerID(docID, pushDocAndGetChilds);
       }
       else {
           $("#folder-list-ul").html("");
           buildTree();
       }
    }

    function buildTree() {
        var entry = tempTree.pop();
        if (tempTree.length > 0){
            var node = findDoc(entry.docID);
            if (node.length > 0) {
            	expandEntry(node);
            	drawChildren(entry, node);
            	buildTree();
            }
            else{
            	drawDoc(entry);
            	buildTree();
            }
        }
        else{
            switch (entry.type) {
            case "derivate":
            	getDeriFolders(entry.docID, highlightCurrent);
                break;
            case "jpjournal":
            	drawDoc(entry);
            	highlightCurrent();
                break;
            default :
            	highlightCurrent();
            }
        }
    }

    function drawTempDoc(docID, name, type, parentID){
        var json = {
            "docID": docID,
            "title": name,
            "type": type,
            "parentID": parentID
        };
        drawDoc(json);
    }

    function drawDoc(doc) {
        //create Document
        var li = $("<li class='folder'><div class='folder-name'>" + doc.title + "</div></li>");
        switch (doc.type) {
            case "derivate":
                $(li).addClass("derivat");
                $(li).prepend("<span class='glyphicon glyphicon-picture icon'></span>");
                break;
            case "jpjournal":
                $(li).addClass("journal");
                $(li).prepend("<span class='glyphicon glyphicon-book icon'></span>");
                break;   
            case "jpvolume":
                $(li).addClass("volume");
                $(li).prepend("<span class='glyphicon glyphicon-folder-close icon'></span>");
                break;   
            case "jparticle":
                $(li).addClass("article");
                $(li).prepend("<span class='glyphicon glyphicon-file icon'></span>");
                break;
            default :
                $(li).prepend("<span class='glyphicon glyphicon-file icon'></span>");
        }
        $(li).data("docID", doc.docID);
        if (doc.type == "derivate"){
        	$(li).data("deriID", doc.docID);
        }
        //append Document
        if (doc.type == "jpjournal"){
            $("#folder-list-ul").append(li);
        }
        else{
        	var parent = findDoc(doc.parentID);
        	checkForChildren(parent).append(li);
        	checkForExpandButton(parent);
        }
        //get Childs
        if((doc.childCount > 0 || doc.deriCount > 0) && doc.type != "derivate"){
            $(li).append($("<ul class='children'></ul>"));
            if (doc.childs.length > 0 || doc.deris.length > 0){
            	drawChildren(doc, li);
                $(li).prepend("<span class='glyphicon glyphicon-minus button button-contract'></span>");
            }
            else{
                $(li).prepend("<span class='glyphicon glyphicon-plus button button-expand'></span>");
            }
        }
        else {
            $(li).prepend("<div class='no-button'>&nbsp;</div>");
        }
    }

    function findDoc(docID, path) {
    	if (path != undefined && path != "" && path != "/"){
    		return $(".derivat-folder").filter(function() {
						return ($(this).data("deriID") == docID) && ($(this).data("path") == path);
					});
    	}
        return $(".folder").filter(function() {
                    return $(this).data("docID") == docID;
                });
    }
    
    function expandEntry(entry) {
		$(entry).children("span.glyphicon").removeClass("glyphicon-plus button-expand");
		$(entry).children("span.glyphicon").addClass("glyphicon-minus button-contract");
	}
    
    function drawChildren(doc, parent){
    	checkForChildren(parent);
        $.each(doc.deris, function(index, doc) {
            drawDoc(doc);
        });
        $.each(doc.childs, function(index, deri) {
        	drawDoc(deri);
        });
    }

    /**
     * @property hasChildren
     */
    function drawDeriFolders(deriID, data, parent) {
    	if (parent == undefined){
    		parent = findDoc(deriID)
    	}
    	$.each(data.children, function(i, child) {
        	var li = drawDeriFolder(child.name, child.absPath, deriID, parent);
        	if (child.hasChildren) {
        		drawDeriFolders(deriID, child, li);
        	}
    	});
	}
    
    function drawDeriFolder(childName, childPath, deriID, parent) {
    	var ul = checkForChildren(parent);
    	var li = findDoc(deriID, childPath);
    	if (li.length == 0){
    		li = $("<li class='derivat-folder'><div class='no-button'>&nbsp;</div><span class='glyphicon glyphicon-folder-close icon'></span><div class='folder-name'>" + childName + "</div></li>").appendTo(ul);
    		$(li).data("deriID", deriID);
    		$(li).data("path", childPath);
    		$(li).data("id", deriID + childPath);
    	}
    	return li;
	}
    
    function drawDocChilds() {
		var entry = tempTree.pop();
		var node = findDoc(entry.docID);
		drawChildren(entry, node);
		expandFolder(node);
	}
    
    function getDocChilds(data) {
		var entry = new TreeEntry(data.docs[0], "");
		tempTree.push(entry);
		entry.getChildsAndDeris(drawDocChilds);
	}
    
	function expandFolder(node) {
		var button = $(node).children(".button");
		if (button.hasClass("button-expand")){
			button.siblings("ul.children").removeClass("hide-folder");
			button.removeClass("button-expand glyphicon-plus");
			button.addClass("button-contract glyphicon-minus");
		}
	}

	function addDocToParent(response) {
		if (response.numFound > 0){
           drawDoc(new TreeEntry(response.docs[0], ""));
		}
	}
	
	function checkForChildren(entry) {
    	var ul = $(entry).children("ul");
    	if (ul.length == 0){
    		ul = $("<ul class='children'></ul>").appendTo(entry);
    	}
    	return ul;
	}
	
	function checkForExpandButton(parent) {
    	if (($(parent).children("ul").length != 0) && ($(parent).children("div.no-button").length != 0)){
    		$(parent).children("div.no-button").remove();
    		$(parent).prepend("<span class='glyphicon glyphicon-minus button button-contract'></span>");
    	}
    	if ($(parent).children("ul").children().length == 0){
    		$(parent).children("ul").remove();
    		if ($(parent).children("span.button").length != 0){
    			$(parent).children("span.button").remove();
    			$(parent).prepend("<div class='no-button'></div>");
    		}
    	}
    	
	}

    //ajax Methods
    function getDocPerID(docID, callback) {
        var url = jp.baseURL + "servlets/solr/select?q=id%3A" + docID + "&start=0&rows=10&sort=maintitle+asc&wt=json&indent=true";
        $.getJSON(url, function(search) {
            callback(search.response);
        });
    }

	function getDeriFolders(deriID, callback){
		$.ajax({
			url: "folders/" + deriID,
			type: "GET",
			dataType: "json",
			success: function(data) {
						if (data.hasChildren != false){
							drawDeriFolders(deriID, data);
						}						
						callback();
					},
			error: function(error) {
						alert(error);
					}
		});
	}
	
	function getJournals(query, start, callback) {
		var url = jp.baseURL + "servlets/solr/select?q=" + query + "&start=" + start +  "&rows=10&fq=objectType%3Ajpjournal&sort=maintitle+asc&wt=json&indent=true";
		$.getJSON(url, function(search){
			if(search.response.numFound > 0){
				var results = search.response.docs;
				$.each(results, function(i, result){
					callback(new TreeEntry(result, ""));
				});
			}
			if(search.response.numFound > start + 10){
				getJournals(query, start + 10, callback);
			}
		});		
	}
    
    return {
        //public
        init: function() {

        },

        goToDocument: function(docID, path){
            var node = findDoc(docID, path);
            if (node.length > 0){
            	highlight(node);
            }
            else{
            	 getDocPerID(docID, pushDocAndGetChilds);
            }
        },
        
        selectDocument: function(node){
        	highlight(node);
        },
        
        selectDocumentPerID: function(docID, path){
        	this.selectDocument(findDoc(docID, path));
        },
        
        addChildToDerivat: function(childName, childPath, deriID, path) {
        	var parent = findDoc(deriID, path);
        	drawDeriFolder(childName, childPath, deriID, parent);
		},
		
		expandDoc: function(docID){
			findDoc(docID).find("ul.children").html("");
			getDocPerID(docID, getDocChilds);
		},
		
		searchJournals: function(query) {
			getJournals(query, 0, drawDoc);
		},
		
		removeDocPerID: function(docID, path) {
			var node = findDoc(docID, path);
			this.removeDoc(node);
		},
		
		removeDoc: function(node) {
			var parent = $(node).parent().closest(".folder");
			node.remove();
			checkForExpandButton(parent);
		},
		
		addDoc: function(docID) {
			getDocPerID(docID, addDocToParent);
		},
		
		getParentDocID: function(childDocID) {
			var node = findDoc(childDocID, "");
			return $(node).parent().closest('li.folder').data("docID");
		},

		gotDerivateChilds: function(docID, callback){
			return getDocPerID(docID, callback);
		},

        addTempDoc: function(docID, name, type, parentID){
            drawTempDoc(docID, name, type, parentID);
        },

        renameDoc: function(docID, path, name) {
            var doc = findDoc(docID, path);
            if (doc.length > 0) {
                doc.children("div.folder-name").html(name);
                if (doc.data("path") != undefined){
                    var oldPath = doc.data("path");
                    doc.data("path", oldPath.substring(0, oldPath.lastIndexOf("/") + 1) + name);
                    doc.data("id", doc.data("derID") + doc.data("path"));
                }
            }
        },

        fadeEntry: function(node) {
            $(node).children().not(".children").addClass("faded");
            $(node).data("faded", true);
        },

        unFadeEntry: function(node) {
            $(node).children().not(".children").removeClass("faded");
            $(node).removeData("faded");
        }
    };
})();