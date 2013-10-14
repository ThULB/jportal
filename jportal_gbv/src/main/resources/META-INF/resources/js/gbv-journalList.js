var gbv = gbv || {};

gbv.az = {

	load: function() {
		$.getJSON(gbv.az.getSearchURL(), function(searchResult) {
			gbv.az.printList(searchResult.response);
		});
	},

	getSearchURL: function() {
		var host = jp.az.getHost();
		var base = '/servlets/solr/select?wt=json&sort=maintitle_lowercase asc&rows=9999&q=';
		var query = "%2BobjectType:jpjournal %2DcontentClassi1:calendar";
		return host + base + query;
	},

	printList: function(response) {
		var mainDiv = $("#gbv-journalList");
		if(response.numFound == 0) {
			mainDiv.append("Keine Zeitschriften gefunden.");
			return;
		}
		var docs = response.docs;
		for(var i = 0; i < docs.length; i++) {
			gbv.az.printJournal(mainDiv, docs[i]);
		}
	},

	printJournal: function(root, journal) {
		var journalLink = $("<a class='journal' href='" + jp.az.getHost() + "/receive/" + journal.id + "'></a>");
		journalLink.append("<div class='title'>" + journal.maintitle + "</div>");
		jp.az.printPublished(journalLink, journal);
		jp.az.printPublisher(journalLink, journal);
		root.append(journalLink);
	}

};
