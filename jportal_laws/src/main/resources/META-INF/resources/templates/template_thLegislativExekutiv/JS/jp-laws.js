function linkLawsToIview() {
	$('.content a').click(function() {
		showIview({
			ID : $(this).attr("data-jp-laws-derivateId"),
			file : "/" + $(this).attr("data-jp-laws-image")
		});
	});
}

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
}

function setLogo(baseURL) {
	var logo = $('#logo');
	// set background
	logo.css('background-image', 'url(' + baseURL + 'templates/template_thLegislativExekutiv/IMAGES/logo.png)');
	// add link
	logo.css("position", "relative");
	logo.wrapInner("<a id='logoLink' href='http://www.urmel-dl.de/Projekte/LegislativundExekutiv.html'></a>");
}

function setMaintitle(maintitle) {
	$('#logo').prepend('<div id="logoTitle">' + maintitle + '</div>');
}

function setSearchLink(baseURL) {
	$('#searchForm').append('<a href="' + baseURL + 'jp-laws-search.xml">Expertensuche in Gesetzesblättern</a>');
}

function highlightLawsText(/* string */hl) {
	if (hl == null || hl == '') {
		return;
	}
	var hlArray = hl.split(/\W+/);
	$('#jp-content-laws').highlight(hlArray);
}
