$(document).ready(function(){
	var searchConf
	$.getJSON("/rsc/moveObj/conf", function(conf){
		searchConf = conf;
	});
	
	var subselect = {};

	subselect = {

		cache: [],

		init: function(/*String*/ hoverSelector, /*String*/ previewSelector) {
			var div = $(previewSelector);
			$(hoverSelector).hover(function() {
				var mcrid = $(this).attr("data-jp-mcrid");
				var pos = $(this).position();
				div.css("top", pos.top + 30);
				div.css("left", pos.left - 8);
				
				if(searchConf.url){
					subselect.get(mcrid, function(html) {
						div.html(html);
					});
					div.removeClass("hidden");
					div.stop().animate({opacity: 1}, 500);
				}
			},
			function () {
				div.stop().animate({opacity: 0}, 500, function() {
					div.css("opacity", "0");
					div.addClass("hidden");
				});
			});
		},

		get: function(/*String*/ mcrid, /*function*/ onSuccess) {
			if(subselect.cache[mcrid]) {
				onSuccess(subselect.cache[mcrid]);
				return;
			}
			$.ajax({
				url: searchConf.url + mcrid,
				dataType: "html"
			}).done(function(html) {
				subselect.cache[mcrid] = html;
				onSuccess(html);
			});
		}

	};
	
	var qpara = [], hash;
	readQueryParameter();
	var objID =  qpara["objId"];
	getObject(objID);
	var objToMove = [];
	$("#mom_checkbox_childlist_all").prop('checked', false);

	$("body").on("click", "#mom_search_button", function() {
		getMoveList($("#mom_search").val(), 0);
	});

	$("body").on("keydown", "#mom_search", function(key) {
		if(key.which == 13) {
			getMoveList($("#mom_search").val(), 0);
		}
	});

	$("body").on("click", "#mom_button_move", function() {
		var elm = $(".mom_checkbox_result:checked").length;
		if (elm > 0 && objToMove.length != 0){
			var newparentID = $("#mom_resultlist input:checked").parent().attr("data-objId");
			
			var json = [];
			$.each(objToMove, function(i, val) {
				json.push({"objId": val, "newParentId": newparentID});
			});
			$("#mom_button_move").addClass("disabled");
			$("#mom_button_move > i").removeClass("fa-arrow-right");
			$("#mom_button_move > i").addClass("fa-spinner fa-spin");
			moveTo(json, newparentID);
		}
		else{
			alert("Bitte wählen Sie erst ein Kindelement aus.")
		}
	});

	$("body").on("click", ".mom_checkbox_result", function(event) {
		if($(this).is(":checked")){
			$(".mom_checkbox_result").prop('checked', false);
			$(this).prop('checked', true);
			$("#mom_button_move").removeClass("disabled");
		}
		else{
			$("#mom_button_move").addClass("disabled");
		}
	});
	
	$("body").on("click", ".mom_checkbox_childlist", function() {
		var id = $(this).parent().attr("data-objId");
		if($(this).is(":checked")){
			objToMove.push(id);
		}
		else{
			var pos = $.inArray(id, objToMove);
			if (~pos) objToMove.splice(pos, 1);
		}
	});
	
	$("body").on("click", "#mom_checkbox_childlist_all", function() {
		selectAll();
	});
	
	$("body").on("click", "#mom_button_move_cancel", function() {
		if (!window.location.origin) window.location.origin = window.location.protocol+"//"+window.location.host;
		window.location.replace(window.location.origin + "/receive/" +  objID);
	});
		
	function moveTo(json, newparentID) {
		$.ajax({
			url: "move",
			type: "PUT",
			contentType: 'application/json',
			dataType: "json",
			data: JSON.stringify(json),
			statusCode: {
				200: function() {
					$("#mom_move_popup").removeClass("hidden");
					$("#mom_move_popup").css("left", ($("#mom_middle").outerWidth() / 2) - ($("#mom_move_popup").outerWidth() / 2));
					$("#mom_move_popup").css("top", ($("#mom_move_popup_arrow").outerHeight() + $("#mom_move_popup").outerHeight()) * -1);
					if (!window.location.origin) window.location.origin = window.location.protocol+"//"+window.location.host;
					setTimeout(function(){window.location.replace(window.location.origin + "/receive/" +  newparentID);},5000);
				},
				500: function(error) {
					$("#mom_button_move").removeClass("disabled");
					$("#mom_button_move > i").addClass("icon-arrow-right");
					$("#mom_button_move > i").removeClass("icon-spinner icon-spin");
					console.log("error in function moveTo");
				}
			}
		});	
	}
	
	function addSearchResult(result) {
		var li = $("<li></li>");
		li.append('<div class="mom_text" data-objId="' + result.id +  '"><input class="mom_checkbox_result" type="checkbox"/><p data-jp-mcrid="' + result.id + '" class="mom_resultlist_entry">' + result.maintitle + '</p></div>');
		if (result.objectType != "jpjournal"){
			li.find("p").append( " (" + result.journalTitle + ")");
		}
		li.appendTo("#mom_resultlist");
	}
	
	function getObject(id) {
		$.getJSON("/servlets/solr/select?q=%2Bid%3A" + id + "&wt=json", function(search){
			if(search.response.numFound == 1){
				var parent = search.response.docs[0];
				if (parent.maintitle != ""){
					$("#mom_parent h4").prepend(parent.maintitle);
					$("#mom_parent h4").attr("data-jp-mcrid", id);
					$("#mom_radio_search_filter1").parent().append(parent.maintitle);
					$("#mom_radio_search_filter1").parent().attr("title", parent.maintitle);
				}
				getChildren(id, 0);
			}
		});
	}
	
	function getChildren(id, start){
		$.getJSON("/servlets/solr/select?q=%2Bparent%3A" + id + "&start=" + start + "&rows=10&wt=json&sort="+searchConf.sort+"%20asc&wt=json", function(search){
			if(search.response.numFound > 0){
				buildTree(search.response.docs);
				buildPaginator(id,search.response,"#mom_childlist_paginator");
				subselect.init(".mom_childlist_entry", "#mom_childlist_objectPreviewContainer");
			}
		});
	}
	
	function getMoveList(id, start){
		$("#mom_resultlist").removeClass("hidden");
		$("#mom_resultlist_nothing").addClass("hidden");
		if (id == "") id = "*";
		if ($("#mom_radio_search_filter1").is(':checked')){
			var url = "/servlets/solr/select?q=" + id + "&start=" + start +  "&rows=10&fq=parent%3A" + objID +"&fq=objectType%3Ajpvolume+objectType%3Ajpjournal&sort="+searchConf.sort+"+asc&wt=json&indent=true";
		}
		else{
			var url = "/servlets/solr/select?q=" + id + "&start=" + start +  "&rows=10&fq=objectType%3Ajpvolume+objectType%3Ajpjournal&sort="+searchConf.sort+"+asc&wt=json&indent=true";
		}
		
		$.getJSON(url, function(search){
			$("#mom_resultlist").html("");
			if(search.response.numFound > 0){
				var results = search.response.docs;
				for (result in results){
					addSearchResult(results[result]);
				}
				buildPaginator(id,search.response,"#mom_movelist_paginator");
				subselect.init(".mom_resultlist_entry", "#mom_resultlist_objectPreviewContainer");
			}
			else{
				$("#mom_resultlist").addClass("hidden");
				$("#mom_resultlist_nothing").removeClass("hidden");
				$("#mom_movelist_paginator > .pagination").html("");
			}
		});
	}
	
	function buildPaginator(id, response, elm) {
		$(elm).find(".pagination").html("");
		var numPerPage = 10;
		var pagecount = Math.ceil(response.numFound / numPerPage);
		var page = Math.ceil(response.start / numPerPage);
				
		if(pagecount > 3){
			if(page > 2){
				if (page < pagecount - 1){
					//e.g. first ... 2 3 4 ... last
					addPageToPaginator(0, 1, "", id, elm);
					addPageToPaginator(0, "...", "disabled", id, elm);
					for(var i = page -1 ; i <= page + 1; i++){
						if(i == page){
							addPageToPaginator((i-1)*numPerPage, i, "active", id, elm);
						}
						else{
							addPageToPaginator((i-1)*numPerPage, i, "", id, elm);
						}
					}
					addPageToPaginator(0, "...", "disabled", id, elm);
					addPageToPaginator((pagecount-1)*numPerPage , pagecount, "", id,elm);
				}
				else{
					//e.g. first ... 4 5 6
					addPageToPaginator(0, 1, "", id, elm);
					addPageToPaginator(0, "...", "disabled", id, elm);
					for(var i = pagecount - 2 ; i <= pagecount; i++){
						if(i == page){
							addPageToPaginator((i-1)*numPerPage, i, "active", id, elm);
						}
						else{
							addPageToPaginator((i-1)*numPerPage, i, "", id, elm);
						}
					}
				}
			}
			else{
				//e.g. 1 2 3 ... last
				for(var i = 0; i < 3; i++){
					if(i == page){
						addPageToPaginator(i*numPerPage, i+1, "active", id, elm);
					}
					else{
						addPageToPaginator(i*numPerPage, i+1, "", id, elm);
					}
				}
				addPageToPaginator(0, "...", "disabled", id, elm);
				addPageToPaginator((pagecount-1)*numPerPage, pagecount, "", id, elm);
			}
		}
		else{
			if(pagecount != 1){
				//e.g. 1 2 3
				for(var i = 0; i < pagecount; i++){
					if(i == page){
						addPageToPaginator(i*numPerPage, i+1, "active", id, elm);
					}
					else{
						addPageToPaginator(i*numPerPage, i+1, "", id, elm);
					}
					
				}
			}
			else{
				addPageToPaginator(0, 1, "active", id, elm);
			}
		}
	}
	
	function addPageToPaginator(start, name, state, id, elm) {
		if (elm == "#mom_childlist_paginator"){
			if (state == ""){
				var pageButton = $('<a href="#" onclick="return false;">' + name + '</a>');
				pageButton.bind("click", function() {
					getChildren(id, start);
				});
			}
			else{
				var pageButton = $('<span>' + name + '</span>');
			}
		}
		else{
			if (state == ""){
				var pageButton = $('<a href="#" onclick="return false;">' + name + '</a>');
				pageButton.bind("click", function() {
					getMoveList(id, start);
				});
			}
			else{
				var pageButton = $('<span>' + name + '</span>');
			}
		}
		
		$("<li></li>").append(pageButton).addClass(state).appendTo($(elm).find(".pagination"));
	}
	
	function buildTree(childs) {
		$("#mom_childlist").html("");
		for (child in childs){
			var li = $('<li class="mom_text" data-objId="' + childs[child].id +  '"></li>')
			li.append('<input class="mom_checkbox_childlist" type="checkbox"/>');
			li.append('<p class="mom_childlist_entry" data-jp-mcrid="' + childs[child].id + '">' + childs[child].maintitle + '</p>')
			var pos = $.inArray(childs[child].id, objToMove);
			if (~pos) $(li).find(".mom_checkbox_childlist").prop('checked', true);
			$("#mom_childlist").append(li);
		}
	}
	
	function readQueryParameter() {
		var q = document.URL.split(/\?(.+)?/)[1];
		if(q != undefined){
	        q = q.split('&');
	        for(var i = 0; i < q.length; i++){
	            hash = q[i].split(/=(.+)?/);
	            qpara.push(hash[1]);
	            qpara[hash[0]] = hash[1];
	        }
		}
	}
	function selectAll() {
		if($("#mom_checkbox_childlist_all").is(":checked")){
			$.getJSON("/servlets/solr/select?q=%2Bparent%3A" + objID + "&start=0&wt=json&sort="+searchConf.sort+"%20asc&wt=json", function(search){
				if(search.response.numFound > 0){
					$.getJSON("/servlets/solr/select?q=%2Bparent%3A" + objID + "&start=0&rows=" + search.response.numFound + "&wt=json&sort="+searchConf.sort+"%20asc&wt=json", function(search2){
						if(search2.response.numFound > 0){
							var childs = search2.response.docs;
							for (child in childs){
								objToMove.push(childs[child].id);
							}
							$(".mom_checkbox_childlist").prop('checked', true);
						}
					});
				}
			});
		}
		else{
			objToMove.length = 0;
			$(".mom_checkbox_childlist").prop('checked', false);
		}
	}
})