var gbv = gbv || {};

gbv.latestArticles = {

	getHost: function() {
		return 'http://' + $(location).attr('host');
	},

	load: function(query) {
		$.getJSON(gbv.latestArticles.getSearchURL(query), function(searchResult) {
			gbv.latestArticles.printLatestArticles(searchResult.response);
		});
	},

	getSearchURL: function(query) {
		var host = gbv.latestArticles.getHost();
		var base = '/servlets/solr/select?wt=json&q=';
		return host + base + query;
	},

	printLatestArticles: function(response) {
		if(response.numFound == 0) {
			return;
		}
		var mainDiv = $("#latestArticles");
		var docs = response.docs;
		for(var i = 0; i < docs.length; i++) {
			gbv.latestArticles.printArticle(mainDiv, docs[i], i == 0);
		}
	},

	printArticle: function(node, article, first) {
		var row = $("<div class='horizontalContainer'></div>");
		var leftBlock = $("<div class='leftBlock'></div>").appendTo(row);
		if(first) {
			leftBlock.append("<div class='contentBlock'>Neuste Artikel</div>");
		}
		var href = gbv.latestArticles.getHost() + "/receive/" + article.id;
		row.append("<a class='rightBlock' href='" + href + "'><div class='contentBlock'>" + article.maintitle + "</div></a>");
		node.append(row);
	}

};
