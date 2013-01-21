function linkLawsToIview() {
	$('.content a').click(function() {
		showIview({
			ID: $(this).attr("data-jp-laws-derivateId"),
			file: "/" + $(this).attr("data-jp-laws-image")
		});
	});
}

function buildQuery() {
    var searchTerm = $("#searchTerm").val();
    var territory = $("#territory").val();
    var from = $("#published_from").val();
    var until = $("#published_until").val();

    // input conditions
    var query = searchTerm;
    if(territory.length > 0) {
      query += " +volContentClassi1:" + territory;
    }
    if(from.length > 0 && until.length > 0) {
      query += " +date.published:[" + from + " TO " + until + "]";
    } else if(from.length > 0) {
      query += " +date.published:[" + from + " TO *]";
    } else if(until.length > 0) {
      query += " +date.published:[* TO " + until + "]";
    }
    query += " +contentClassi2:Gesetzesblaetter";
    query += " +objectType:jpvolume";
    $("#qt").attr("value", query);
}

function setLogo(baseURL) {
    $('#logo').css('background-image', 'url(' + baseURL + 'templates/master/template_thLegislativExekutiv/IMAGES/logo.png)');
}

function setMaintitle(maintitle) {
	$('#logo').prepend('<div id="logoTitle">' + maintitle  + '</div>');
}

function setSearchLink(baseURL) {
	$('#searchForm').append('<a href="' + baseURL + 'jp-search.xml?XSL.mode=laws.form">Expertensuche in Gesetzesblättern</a>');
}
