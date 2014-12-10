var jp = jp || {};

jp.advancedsearch = {

	onsubmit: function() {
		var contentQuery = null;
		var solrQuery = null;

		for(var i = 1; i <= 3; i++) {
			var field = $("select[name='XSL.field" + i + "']").val();
			var value = $("input[name='XSL.value" + i + "']").val();
			if(value == null || value == "") {
				continue;
			}
			if(field == "content") {
				contentQuery = (contentQuery == null) ? value : contentQuery + " " + value;
			} else {
				var fieldQuery = "+" + field + ":" + value;
				solrQuery = (solrQuery == null) ? fieldQuery : solrQuery + " " + fieldQuery;
			}
		}
        contentQuery = (contentQuery == null) ? null : "({!join from=returnId to=id}" + contentQuery + ")";
        solrQuery = (solrQuery == null && contentQuery == null) ? "*:*" : (contentQuery == null) ? solrQuery : (solrQuery == null) ? contentQuery
                : contentQuery + " AND " + solrQuery;
        $("input[name='q']").val(solrQuery);
		return true;
	},

	changeSearchRadius: function() {
		if($("input:checked").val() == 'globalSearchOption'){
			$("input[name='fq']").remove();
			$("#hiddenJournalId").removeAttr("name");
		}
		
		if($("input:checked").val() == 'journalSearchOption'){
			$("#hiddenJournalId").after(' <input type="hidden" name="fq" value="journalID:' + $("input[name='journalID']").val() + '"/> ')
			$("#hiddenJournalId").attr("name", "journalID");
		}
	}

};