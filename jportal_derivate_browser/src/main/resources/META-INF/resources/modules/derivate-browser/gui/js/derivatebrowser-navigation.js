
var derivateBrowserNavigation = (function () {

    //private Properties
    var tempTree = [];

    function bindActions() {
        bindUIActions();
        bindEventActions();
    }

    function bindUIActions() {
        $("body").on("keydown", "#folder-list-search-input", function (key) {
            if (key.which == 13) {
                searchJournals($(this).val());
            }
        });

        $("body").on("click", "#folder-list-search-button", function () {
            searchJournals($("#folder-list-search-input").val());
        });

        $("body").on("click", ".folder:not(.derivat) > .folder-name, .folder:not(.derivat) > span.icon", function (event) {
            if (event.shiftKey || event.ctrlKey) {
                window.getSelection().removeAllRanges();
                var current = getCurrentNode();
                var endNode = $(this).parent(".folder");
                if (current.parent()[0] == endNode.parent()[0]) {
                    if (event.ctrlKey) {
                        if ($(".aktiv").length > 1){
                            $(this).parent(".folder").toggleClass("aktiv");
                        }
                    }
                    else {
                        selectRange(getCurrentNode(), $(this).parent(".folder"));
                    }
                    $("body").trigger("showSelectedDocs");
                }
            }
            else {
                derivateBrowserTools.goTo($(this).parent().data("docID"), "");
            }
        });

        $("body").on("click", ".button-expand", function () {
            var docID = $(this).closest(".folder").data("docID");
            findDoc(docID).find("ul.children").html("");
            getDocPerID(docID, getDocChilds);
        });

        $("body").on("click", ".button-contract", function () {
            $(this).siblings("ul.children").addClass("hide-folder");
            $(this).siblings("ul.children").html("");
            $(this).removeClass("button-contract glyphicon-minus");
            $(this).addClass("button-expand glyphicon-plus");
        });

        $("body").on("click", ".derivat > .folder-name, .derivat > span.icon", function (event) {
            if (!event.shiftKey) {
                derivateBrowserTools.goTo($(this).parent().data("docID"), "");
            }
        });

        $("body").on("click", ".derivat-folder > .folder-name, .derivat-folder > span.icon", function (event) {
            if (!event.shiftKey) {
                derivateBrowserTools.goTo($(this).parent().data("deriID"), $(this).parent().data("path"));
            }
        });
    }

    function bindEventActions() {
        $("body").on("addDerivatFolder", function (e, name, filepath, deriID, absPath) {
            addChildToDerivat(name, filepath, deriID, absPath);
        });

        $("body").on("renameDoc", function (event, docID, path, name) {
            renameDoc(docID, path, name);
        });

        $("body").on("removeDocPerID", function (event, docID, path) {
            removeDocPerID(docID, path);
        });

        $("body").on("addChildToDerivat", function (event, childName, childPath, deriID, path) {
            addChildToDerivat(childName, childPath, deriID, path);
        });

        $("body").on("gotDerivateChilds", function (event, docID, callback, docs) {
            gotDerivateChilds(docID, callback, docs);
        });

        $("body").on("addTempDoc", function (event, docID, name, type, parentID) {
            drawTempDoc(docID, name, type, parentID);
        });

        $("body").on("fadeEntry", function (event, node) {
            $(node).children().not(".children").addClass("faded");
            $(node).data("faded", true);
        });

        $("body").on("unFadeEntry", function (event, node) {
            $(node).children().not(".children").removeClass("faded");
            $(node).removeData("faded");
        });

        $("body").on("addDoc", function (event, docID) {
            getDocPerID(docID, addDocToParent);
        });

        $("body").on("goToDocument", function (event, docID, path) {
            goToDocument(docID, path)
        });
    }
    
    //private Methods
    function highlight (node) {
        $(".aktiv").removeClass("aktiv");
        $(node).addClass("aktiv");
    }

    function highlightRange (startNode, endNode) {
        var parent = $(startNode).parent()[0];
        var inZone = false;
        $(".aktiv").removeClass("aktiv");
        if (parent == $(endNode).parent()[0]) {
            $(parent).children().each(function(i, elm) {
                if (elm == startNode[0]) inZone = true;
                if (inZone) {
                    $(elm).addClass("aktiv");
                }
                if (elm == endNode[0]) inZone = false;
            });
        }

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
        var parentID = response.docs[0].parent;
        if (parentID == undefined) parentID = response.docs[0].derivateOwner;
        derivateBrowserTools.doneAsync(parentID);
        if (response.numFound > 0){
            var parent = findDoc(parentID);
            if ($(parent).find("ul.children").children().length > 0) {
                drawDoc(new TreeEntry(response.docs[0], ""));
            }
            else {
                if ($(parent).find(".no-button").length > 0){
                    $(parent).find(".no-button").remove();
                    $(parent).prepend("<span class='glyphicon glyphicon-plus button button-expand'></span>");
                }
            }
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

    function searchJournals(query) {
        $("#folder-list-ul").html("");
        $("#derivate-browser").addClass("hidden");
        $("#derivat-panel").addClass("hidden");
        if (query == "") query = "*";
        getJournals(query,  0, drawDoc);
    }

    function getCurrentNode() {
        return findDoc(derivateBrowserTools.getCurrentDocID(), derivateBrowserTools.getCurrentPath());
    }

    function selectRange(startNode, endNode) {
        if ($(startNode).index() < $(endNode).index()) {
            highlightRange(startNode, endNode);
        }
        else {
            highlightRange(endNode, startNode);
        }
    }

    function addChildToDerivat(childName, childPath, deriID, path) {
        var parent = findDoc(deriID, path);
        drawDeriFolder(childName, childPath, deriID, parent);
    }

    function renameDoc(docID, path, name) {
        var doc = findDoc(docID, path);
        if (doc.length > 0) {
            doc.children("div.folder-name").html(name);
            if (doc.data("path") != undefined){
                var oldPath = doc.data("path");
                doc.data("path", oldPath.substring(0, oldPath.lastIndexOf("/") + 1) + name);
                doc.data("id", doc.data("derID") + doc.data("path"));
            }
        }
    }

    function removeDoc(node) {
        var parent = $(node).parent().closest(".folder");
        node.remove();
        checkForExpandButton(parent);
    }

    function removeDocPerID(docID, path) {
        var node = findDoc(docID, path);
        removeDoc(node);
    }

    function gotDerivateChilds(docID, callback, docs){
        getDocPerID(docID, callback, docs);
    }

    function goToDocument(docID, path){
        var node = findDoc(docID, path);
        if (node.length > 0){
            highlight(node);
        }
        else{
            getDocPerID(docID, pushDocAndGetChilds);
        }
    }

    //ajax Methods
    function getDocPerID(docID, callback, docs, count) {
        var url = jp.baseURL + "servlets/solr/select?q=id%3A" + docID + "&start=0&rows=10&sort=maintitle+asc&wt=json&indent=true";
        $.getJSON(url, function(search) {
            //console.log(search.response.numFound);
            if (search.response.numFound > 0) {
                callback(search.response, docs, docID);
                derivateBrowserTools.hideLoadingScreen();
            }
            else {
                if (count == undefined){
                    count = 1;
                    derivateBrowserTools.showLoadingScreen();
                }
                if (count < 4) {
                    setTimeout(function () {
                        getDocPerID(docID, callback, docs, count+1);
                    }, 1000);
                }
                else {
                    derivateBrowserTools.hideLoadingScreen();
                }
            }
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
            bindActions();
        }
    };
})();