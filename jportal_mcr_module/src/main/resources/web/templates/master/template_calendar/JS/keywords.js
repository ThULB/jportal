var journalID;
var dropDown = $('<img class="dropDownArrow" src="/images/naviMenu/dropdown.png">');
function loadKeywords(jID) {
    journalID = jID;
	var mainDiv = $('<div id="keywords"></div>');
	mainDiv.append("<h3>Schlagwörter</h3>")
	mainDiv.append(dropDown);
	mainDiv.click(function() {
		if($(this).parent().hasClass("open")){
			$(this).nextAll("ul").toggle();
		}
		else{
			$(this).parent().addClass("open");
			loadKeyword($(this).parent(), "");
		}
	});
	mainDiv.appendTo($("#jp-content-LColumn > ul"));
}

function loadKeyword(element, keyword) {
	if(keyword == ""){
		$.get("/rsc/classifications/jportal_class_00000083/", function(data){attachToElement(element, data)});
	}
	else{
		$.get("/rsc/classifications/jportal_class_00000083/" + keyword, function(data){attachToElement(element, data)});
	}
}

function attachToElement(element, keywords) {
	var ul = $("<ul></ul>");
	ul.appendTo(element);
	for ( var i = 0; i < keywords.children.length; i++) {
		var li = $("<li></li>");
		li.appendTo(ul);
		if (keywords.children[i].haschildren == true){
			var div = $("<div></div>");
			div.append('<img class="dropDownArrow" src="/images/naviMenu/dropdown.png">');
			div.attr("id", keywords.children[i].id.categid);
			div.addClass("keyWordDiv");
			div.appendTo(li);
			div.click(function() {
				if($(this).parent().hasClass("open")){
					$(this).nextAll("ul").toggle();
				}
				else{
					$(this).parent().addClass("open");
					loadKeyword($(this).parent(), $(this).attr("id"));
				}
			});
		}
		var a = $("<a></a>");
		a.text(keywords.children[i].labels[0].text);
		var categID = keywords.children[i].id.categid;
		categID = categID.replace(/ /g, "\\ ");
		a.attr("href", "../jp-search.xml?XSL.hiddenQt=+volContentClassi1:" + categID + "+journalID:" + journalID  + "&XSL.mode=hidden");
		a.addClass("keyWordA");
		a.appendTo(li);
	}
}