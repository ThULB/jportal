$(document).ready(function(){
	$("#moveDocButton").on("click", function(){
		var overlay = new Overlay();
		var id = $(this).attr("objID");
		$.getJSON("/servlets/solr/find?qry=%2Bid%3A" + id + "&wt=json", function(search){
			if(search.response.numFound == 1){
				var selectTree = new SelectTree(search.response.docs[0]);
				overlay.setContent(selectTree);
				
				selectTree.onclose(function(){
					overlay.remove();
				})
			}
		});
		$('body').append(overlay);
	});
	
	var SelectTree = function(toMoveObj){
		var selectTreeFrame = $("<div class='jp-moveObjGUI'/>");
		var selectBreadcrumb = $("<div class='breadcrumbContainer' />").appendTo(selectTreeFrame);
		var selectTree = $("<div class='jp-tree-view'/>").appendTo(selectTreeFrame);
		var selectForm = $("<div class='form-actions'/>").appendTo(selectTreeFrame);
		var selectFormText = $("<div class='formText'/>").appendTo(selectForm);
		var selectFormMove = $("<button class='btn disabled'>Verschieben</button>").appendTo(selectForm);
		var selectFormCancel = $("<button class='btn'>Abbrechen</button>").appendTo(selectForm);
		var errorUnauthorized = $('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button></div>');
		
		errorUnauthorized.css({
			position: "absolute",
			width: "567px",
			height: "52px"
		})
		
		function buildListItems(search, iconClass){
			var resultList = search.response.docs;
			var listHtml = $("<ul/>");
			var idNameMap = {};
			
			for ( var int = 0; int < resultList.length; int++) {
				var resultHit = resultList[int];
				var listItem = $("<li/>", {
					'id' : resultHit.id,
					html : resultHit.maintitle
				});
				
				idNameMap[resultHit.id] = resultHit.maintitle;
				
				if(resultHit.objectType == 'jpvolume' && resultHit.id != toMoveObj.id){
					var span = $('<span class="folder">' + listItem.html() + '</span>');
					span.data('id', resultHit.id);
					span.data('parent', resultHit.parent);
					listItem.empty();
					listItem.append('<i class="' + iconClass + '"></i>', span);
				}
				
				listItem.appendTo(listHtml);
			}
			
			listHtml.data("idNameMap", idNameMap);
			
			return listHtml;
		}
		
		function buildList(id){
			$.getJSON("/rsc/obj/" + id + "/parents", function(parents){
				var breadcrumb = $("<ul class='breadcrumb'/>")
				for ( var i = parents.length - 1 ; i >= 0; i--) {
					
					var link = $('<a class="breadcrumbLink" href="#">' + parents[i].title + '</a>');
					link.data("id", parents[i].id);
					var entry = $('<li/>').append(link).append('<span class="divider">/</span>');
					breadcrumb.append(entry);
				}
				
				selectBreadcrumb.html(breadcrumb);
			});
			
			$.getJSON("/servlets/solr/find?qry=%2Bid%3A" + id + "&wt=json", function(search){
				var listItems = buildListItems(search, 'icon-arrow-up').appendTo(selectTree);
				var idNameMap = listItems.data("idNameMap");
				console.log("get ID: " + id + " name: " + idNameMap[id]);
				selectForm.trigger('setText', idNameMap[id]);
			}).done(function(){
				$.getJSON("/servlets/solr/find?qry=%2Bparent%3A" + id + "&wt=json", function(search){
					buildListItems(search, 'icon-caret-right').appendTo('li#' + id);
				});
			});
		}
		
		function setClickEvent(selector, idName){
			selectTreeFrame.on("click", selector, function(){
				var id = $(this).data(idName);
				selectTree.empty();
				buildList(id);
				
				console.log("Click ID: " + id);
				
				selectForm.one('setText', function(evt, destName){
					selectFormText.html('Verschieben von "'+ toMoveObj.maintitle +'" nach "' + destName + '".');
					selectFormMove.toggleClass('disabled', false);
					selectFormMove.toggleClass('btn-primary', true);
					selectFormMove.data('destID', id);
				})
				
			})
		}
		
		setClickEvent("i.icon-caret-right+span", "id");
		setClickEvent("i.icon-arrow-up+span", "parent");
		setClickEvent("a.breadcrumbLink", "id");
		
		selectTreeFrame.onclose = function(fn){
			selectTreeFrame.on("close", fn);
		}
		
		selectFormCancel.on("click", function(){
			selectTreeFrame.trigger("close");
			selectTreeFrame.remove();
		});
		
		selectFormMove.on("click", function(){
			var destID = $(this).data("destID");
			$.ajax({
				url: "/rsc/obj/" + toMoveObj.id + "/moveTo/" + destID,
				type: "PUT"
			}).done(function(){
				selectTree.empty();
				selectFormMove.remove();
				selectFormCancel.toggleClass('btn-primary', true).html("Beenden");
				selectFormText.html('Verschieben von "'+ toMoveObj.maintitle +'" nach "' + $(this).text() + '" war erfolgreich.');
				selectForm.addClass("alert alert-success");
				selectTreeFrame.on("close", function(){
					location.reload();
				});
			}).fail(function(jqXHR, textStatus, error){
				if(jqXHR.status == 401){
					errorUnauthorized.append("Sie haben nicht genügend Rechte für diese Operation. Bitte melden Sie sich an.").insertBefore(selectFormText);
				}
			});
			
		});
		
		var parentID = toMoveObj.parent;
		selectFormText.html('Bitte wählen Sie ein neues Band aus, um "' + toMoveObj.maintitle + '" zu verschieben.');
		buildList(parentID);
		
		return selectTreeFrame;
	}
	
	var Overlay = function(){
		var overlayFrame = $("<div/>");
		var overlay = $("<div/>").appendTo(overlayFrame);
		var overlayCenter = $("<div/>").appendTo(overlayFrame);
		var overlayContent = $("<div class='well'/>").appendTo(overlayCenter);
	    
	    overlay.css({
	    	width: '100%',
	    	height: '100%',
	    	position: 'fixed',
	    	top: '0',
	    	left: '0',
	    	'z-index': '3000',
	    	background: 'black',
	    	 filter: 'alpha(opacity=50)',
	        '-moz-opacity': '0.5',
	        '-khtml-opacity': '0.5', 
	    	opacity: '0.5'
	    });
	    
	    overlayCenter.css({
	    	position: 'absolute',
	    	top: '50%',
	    	left: '50%',
	    	'z-index': '5000'
	    });
	    
	    overlayContent.css({
	    	position: 'relative',
	    	'margin-left': '-329px',
	    	'margin-top': '-300px',
	    	width: '658px',
	    	height: '428px',
	    	background: 'white',
	    	padding: '20px'
	    });
	    
	    overlayFrame.setContent = function(content){
	    	overlayContent.html(content);
	    }
	    
	    return overlayFrame;
	}
});