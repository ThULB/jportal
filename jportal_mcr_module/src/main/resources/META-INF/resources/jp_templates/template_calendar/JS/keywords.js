var startKeyword = window.location.hash.substring(1);
function loadKeywords() {
	var mainDiv = $('<div class="keywords"></div>');
	var parents = "keywordIndex";
	$("<h3 id='keywordIndex' class='placeholder expander expand'>Gesamtschlagwortregister</h3>").appendTo(mainDiv).one("click", function() {
		loadKeyword(mainDiv, "", parents);
	}).click(function() {
		toggleKeyword($(this), $(this).attr("id"), $(this).attr("id"));
	});

	mainDiv.appendTo($("#main"));
	
	if(containsKeyword('keywordIndex')){
		$("#keywordIndex").click();
	}
}

function toggleKeyword(element, linkId, parents) {
	element.nextAll("ul").toggle();
	var toggle = element.hasClass("expand");
	element.removeClass(toggle ? "expand" : "collapse");
	element.addClass(toggle ? "collapse" : "expand");
	toggle ? $("#" + linkId).addClass("jp-layout-colorBlue") : $("#" + linkId).removeClass("jp-layout-colorBlue");
	if(toggle)
		window.location.hash = parents;
}

function loadKeyword(element, keyword, parents) {
	if(keyword == "") {
		$.get(jp.baseURL + "rsc/classifications/jportal_class_00000083/", function(data) {
			attachToElement(element, data, parents);
		});
	} else{
		parents = parents + "," + keyword;
		$.get(jp.baseURL + "rsc/classifications/jportal_class_00000083/" + keyword, function(data) {
			attachToElement(element, data, parents);
		});
	}
}

function attachToElement(element, keywords, parents) {
	var ul = $("<ul></ul>").appendTo(element);
	for ( var i = 0; i < keywords.children.length; i++) {
		var keyword = keywords.children[i];
		if (keyword.haslink == true){
			var li = $("<li></li>").appendTo(ul);
			if (keyword.haschildren == true){
				$("<span class='placeholder expander expand' id='" + keyword.id.categid + "' />").appendTo(li).click(function() {
					toggleKeyword($(this), "a_" + $(this).attr("id"), parents + "," + $(this).attr("id"));
				}).one("click", function() {
					loadKeyword($(this).parent(), $(this).attr("id"), parents);
				});
			} else {
				$("<span class='placeholder' id='" + keyword.id.categid + "' />").appendTo(li);
			}
			
			var a = $("<a id='a_"+ keyword.id.categid + "' />");
			var categID = keyword.id.categid.replace(/ /g, "\\ ").replace(
					/[(]/g, "\\(").replace(/[)]/g, "\\)");
			var docUrl = document.URL.split("#");
			docUrl = docUrl[0];
			a.text(keyword.labels[0].text);
			a.attr("href", jp.baseURL + "servlets/solr/select?q=+volContentClassi1:" + categID + "&XSL.returnURL=" + docUrl + "#" + parents); 
			a.appendTo(li);
			
			if(containsKeyword(keyword.id.categid)){
				$("#" + keyword.id.categid).click();
				$("#a_" + keyword.id.categid).addClass("jp-layout-colorBlue");
			}
		}
	}
}

function containsKeyword(toSearch){
	var keywords = startKeyword.split(",");
	for(var i = 0; i<keywords.length; i++){
		if(keywords[i] == toSearch){
			return true;
		}
	}
	return false;
}
