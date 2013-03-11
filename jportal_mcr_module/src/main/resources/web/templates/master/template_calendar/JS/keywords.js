function loadKeywords() {
	var mainDiv = $('<div class="keywords"></div>');
	$("<h3 class='placeholder expander expand'>Gesamtschlagwortregister</h3>").appendTo(mainDiv).one("click", function() {
		loadKeyword(mainDiv, "");
	}).click(function() {
		toggleKeyword($(this));
	});
	mainDiv.appendTo($("#jp-content-LColumn > ul"));
}

function toggleKeyword(element) {
	element.nextAll("ul").toggle();
	var toggle = element.hasClass("expand");
	element.removeClass(toggle ? "expand" : "collapse");
	element.addClass(toggle ? "collapse" : "expand");
}

function loadKeyword(element, keyword) {
	if(keyword == "") {
		$.get("/rsc/classifications/jportal_class_00000083/", function(data) {
			attachToElement(element, data);
		});
	} else{
		$.get("/rsc/classifications/jportal_class_00000083/" + keyword, function(data) {
			attachToElement(element, data);
		});
	}
}

function attachToElement(element, keywords) {
	var ul = $("<ul></ul>").appendTo(element);
	for ( var i = 0; i < keywords.children.length; i++) {
		var keyword = keywords.children[i];
		if (keyword.haslink == true){
			var li = $("<li></li>").appendTo(ul);
			if (keyword.haschildren == true){
				$("<span class='placeholder expander expand' id='" + keyword.id.categid + "' />").appendTo(li).click(function() {
					toggleKeyword($(this));
				}).one("click", function() {
					loadKeyword($(this).parent(), $(this).attr("id"));
				});
			} else {
				$("<span class='placeholder' id='" + keyword.id.categid + "' />").appendTo(li);
			}
			var a = $("<a/>");
			var categID = keyword.id.categid.replace(/ /g, "\\ ");
			a.text(keyword.labels[0].text);
			a.attr("href", "../jp-search.xml?XSL.qt=+volContentClassi1:" + categID + "&XSL.mode=hidden");
			a.appendTo(li);
		}
	}
}