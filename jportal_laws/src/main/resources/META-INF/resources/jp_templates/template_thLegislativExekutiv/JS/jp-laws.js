
function buildQuery() {
	var searchTerm = $("#searchTerm").val();
	var territory = $("#territory").val();
	var from = $("#published_from").val();
	var until = $("#published_until").val();

	// input conditions
	$("#qry").attr("value", searchTerm);
	var fq = "";
	if (territory.length > 0) {
		fq += "+hidden_genhiddenfield1:" + territory;
	}
	if (from.length > 0 && until.length > 0) {
		fq += " +date.published:[" + from + " TO " + until + "]";
	} else if (from.length > 0) {
		fq += " +date.published:[" + from + " TO *]";
	} else if (until.length > 0) {
		fq += " +date.published:[* TO " + until + "]";
	}
	$("#fq").attr("value", fq);
	
	var returnURl = $('<input id="XSL.returnURL" type="hidden" name="XSL.returnURL"></input>');
	var construct = document.URL.split(/[#?]+/)[0] + "?XSL.qry=" + searchTerm + "&XSL.fq=" + territory + "&XSL.date_from=" + from + "&XSL.date_to=" + until;
	returnURl.attr("value", construct);
	returnURl.insertAfter($("#fq"));
}

function setLogo(baseURL) {
	var logo = $('#logo');
	// set background
	// add link
	logo.css("position", "relative");
	logo.wrapInner("<a id='logoLink' href='http://www.urmel-dl.de/Projekte/LegislativundExekutiv.html'></a>");
}

function setMaintitle(maintitle) {
	$('#logo').prepend('<div id="logoTitle">' + maintitle + '</div>');
}

function setSearchLink(baseURL) {
	$('#searchDropDownMenu').append('<li><a href="' + baseURL + 'jp-laws-search.xml"><i class="fas fa-fw fa-file-text-o" />Expertensuche in Gesetzesblättern</a></li>');
}

function highlightLawsText(/* string */hl) {
	if (hl == null || hl == '') {
		return;
	}
	var hlArray = hl.split(/[^\wÄÖÜäöüß]+/);
	$('#jp-content-laws').highlight(hlArray);
}
