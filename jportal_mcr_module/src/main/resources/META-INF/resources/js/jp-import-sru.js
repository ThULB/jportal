var jp = jp || {};

jp.importSRU = {
	xml: null
}

function querySRU(/*string*/ query) {
	clearResults();
	clearDubletCheck();
	showLoading();
	$.ajax({
		url: jp.baseURL + "rsc/sru/search?q=" + query,
		success: function(data) {
			clearResults();
			var sruContainer = data.documentElement;
			if(sruContainer != null && sruContainer.children.length > 0) {
				// just print one result, should be enough and is much
				// easier to link with import button
				var xml = sruContainer.children[0];
				jp.importSRU.xml = xmlToString(xml);

				// doublet check
				var gnd = $(xml).find("identifier[type='gnd']").text();
				var importable = true;
				if(gnd != null) {
					importable = doubletCheck(gnd);
				} else {
					showDubletCheck("Datensatz enthält keine GND-ID.");
				}
				renderHit(jp.importSRU.xml, importable);
			} else {
				$("#result").html("Kein Treffer.");
			}
		},
		dataType: "xml"
	});
}

function renderHit(/*string*/ xml, /*boolean*/ importable) {
	$.ajax({
		type: "POST",
		url: jp.baseURL + "rsc/render/xml",
		data: xml,
		success: function(html) {
			appendHit(html, importable);
		},
		error: function(error) {
			console.log(error);
			printInternalError();
		},
		dataType: "html",
		contentType: "text/xml; charset=UTF-8"
	});
}

function printInternalError() {
	$("#result").html("Es ist ein interner Fehler aufgetreten. Bitte informieren Sie " + 
						"den Administrator welchen GND-Datensatz Sie importieren wollten.");
}

function appendHit(html, importable) {
	$("#result").append(html);
	if(importable) {
		var link = $("<a href='javascript:void(0)'>Datensatz importieren</a>");
		$("<p></p>").append(link).appendTo("#result");
		link.click(function() {
			clearDubletCheck();
			$.ajax({
				type: "POST",
				url: jp.baseURL + "rsc/object/import",
				data: jp.importSRU.xml,
				success: function(id) {
					$("#result").html("<p>Datensatz erfolgreich importiert. <a href='" + jp.baseURL + "receive/" + id +
							"'>Link zum Objekt</a></p>");
				},
				error: function(error) {
					if(error.status == 401) {
						$("#result").html("Sie haben nicht die Rechte einen Datensatz anzulegen.");
					} else {
						console.log(error);
						printInternalError();
						$("#result").append(" Versuchen Sie nicht den Datensatz ein zweites mal "+
								"zu importieren, da dies zu Dubletten führen kann.");
					}
				},
				contentType: "text/xml; charset=UTF-8"
			});
		});
	}
}

function doubletCheck(/*string*/ gnd) {
	var json = $.ajax({
		type: "GET",
		url: jp.baseURL + "servlets/solr/select?rows=1&fl=id&q=id.gnd:" + gnd + " id.pnd:" + gnd + "&wt=json",
		async: false,
		error: function(error) {
			console.log(error);
		}
	}).responseText;
	var response = $.parseJSON(json).response;
	if(response.numFound > 0) {
		var id = response.docs[0].id;
		showDubletCheck("<span style='color:red'>Datensatz existiert bereits!</span> <a href='" + jp.baseURL + "receive/" + id + "'>Link zum Objekt</a>");
		return false;
	} else {
		showDubletCheck("Datensatz kann importiert werden. Keine Dubletten (identische GND-ID) gefunden.");
		return true;
	}
}

function showDubletCheck(/*string*/ msg) {
	$(".doubletCheck").removeClass("hidden");
	$("#doubletCheck").html(msg);
}

function clearResults() {
	$("#result").empty();
}

function clearDubletCheck() {
	$(".doubletCheck").addClass("hidden");
	$("#doubletCheck").empty();
}

function showLoading() {
	$("#result").append("<i class='fa fa-spinner fa-spin'></i>");
}

function xmlToString(xmlData) {
	return window.ActiveXObject ? xmlData.xml : (new XMLSerializer()).serializeToString(xmlData);
}
