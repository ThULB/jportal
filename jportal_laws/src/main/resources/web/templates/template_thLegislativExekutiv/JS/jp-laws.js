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

    var fq = "";
    // input conditions
    if(searchTerm.length > 0) {
    	$("#qt").attr("value", "({!join from=returnId to=id}" + searchTerm + ") OR " + searchTerm);
    	fq = '-objectType:data_file';
    }
    if(territory.length > 0) {
    	fq += " +hidden_genhiddenfield1:" + territory;
    }
    if(from.length > 0 && until.length > 0) {
    	fq += " +date.published:[" + from + " TO " + until + "]";
    } else if(from.length > 0) {
    	fq += " +date.published:[" + from + " TO *]";
    } else if(until.length > 0) {
    	fq += " +date.published:[* TO " + until + "]";
    }
    fq += " +contentClassi2:Gesetzesblaetter";
    fq += " +objectType:jpvolume";
    $("#fq").attr("value", fq);
}

function setLogo(baseURL) {
    $('#logo').css('background-image', 'url(' + baseURL + 'templates/template_thLegislativExekutiv/IMAGES/logo.png)');
}

function setMaintitle(maintitle) {
	$('#logo').prepend('<div id="logoTitle">' + maintitle  + '</div>');
}

function setSearchLink(baseURL) {
	$('#searchForm').append('<a href="' + baseURL + 'jp-search.xml?XSL.mode=laws.form">Expertensuche in Gesetzesblättern</a>');
}
