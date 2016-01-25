// PUBLISHED SORT
$(document).ready(function() {
	
	// global highchart settings
	Highcharts.setOptions({
		global : {
			useUTC : false
		}
	});
	
	var fromDateInput = $('#published_from');
	var toDateInput = $('#published_to');
	
	if(!fromDateInput.length || !toDateInput.length) {
		return;
	}

	// build facet date query
	var fdq = new jp.solr.FacetDateQuery({
		solrURL : jp.baseURL + "servlets/solr/select",
		min : moment("1500", "YYYY"),
		max : moment()
	});
	if(jp.journalID != null) {
		fdq.fqArray = ["journalID:" + jp.journalID];
		// this should be synchronized, but its not really required
		fdq.fetchEarliest().then(function(/*moment*/ earliestDate) {
			if(earliestDate != null) {
				fdq.min = earliestDate.startOf("day");
			}
		});
		fdq.fetchLatest().then(function(/*moment*/ latestDate) {
			if(latestDate != null) {
				fdq.max = latestDate.endOf("day");
			}
		});
	}

	// build from & until parameter
	var urlParameter = getUrlParameter();
	var published = urlParameter["fq"] == null ? null : urlParameter["fq"].filter(function(p) {
		return p.startsWith("published:[");
	})[0];
	var fromString = published == null ? null : published.slice(published.indexOf("[") + 1, published.indexOf(" TO "));
	var untilString = published == null ? null : published.slice(published.indexOf(" TO ") + 4, published.indexOf("]"));
	fromString = fromString == "*" ? null : fromString;
	untilString = untilString == "*" ? null : untilString;

	var acceptButton = $("#published_accept_button");
	var cancelButton = $("#published_cancel_button");

	var fromDate = fromString == null ? null : moment.utc(fromString, fdq.utcFormat()).local();
	var untilDate = untilString == null ? null : moment.utc(untilString, fdq.utcFormat()).local()

	fromDateInput.datetimepicker({
		locale : 'de',
		format : 'L',
		useCurrent: false,
		defaultDate: fromDate
	});
	toDateInput.datetimepicker({
		locale : 'de',
		format : 'L',
		defaultDate: untilDate
	});
	fdq.setFrom(fromDate);
	fdq.setUntil(untilDate);

	// event handling
	fromDateInput.on("dp.show", function() {
		if(fromDateInput.data("DateTimePicker").date() == null) {
			fromDateInput.data("DateTimePicker").date(moment("1500", "YYYY"));
		}
	});
	fromDateInput.on("dp.change", function() {
		var acceptDisabled = fromDateInput.data("DateTimePicker").date() == null || toDateInput.data("DateTimePicker").date() == null;
		if(acceptDisabled) {
			acceptButton.addClass("disabled");
		} else {
			acceptButton.removeClass("disabled");
		}
		
		
		fdq.setFrom(fromDateInput.data("DateTimePicker").date());
	});
	toDateInput.on("dp.change", function() {
		updateButtons();
	});
	acceptButton.on("click", function() {
		applyFilter();
	});
	cancelButton.on("click", function() {
		window.location.search = removePublishedSortFromSearch();
	});

	// dialog
	var popupButton = $("#published_popup_button");
	var dialog = new jp.PublishedDialog({
		query : fdq,
		title: "Erschienene Werke/Artikel",
		filterButtonText: "Filter anwenden"
	});
	$(dialog).on("filter", function(e, from, until) {
		fromDateInput.data("DateTimePicker").date(from);
		toDateInput.data("DateTimePicker").date(until);
		dialog.close();
		applyFilter();
	});
	popupButton.on("click", function() {
		dialog.open();
	});
	
	updateButtons();

	function removePublishedSortFromSearch() {
		var search = window.location.search;
		var i = search.indexOf("fq=published:");
		if(i != -1) {
			search = search.substring(0, i - 1) + search.substring(search.indexOf("]", i + 1)  + 1, search.length)
		}
		return search;
	}

	function updateButtons() {
		// accept button
		if(fromDateInput.data("DateTimePicker").date() == null || toDateInput.data("DateTimePicker").date() == null) {
			acceptButton.addClass("disabled");
		} else {
			acceptButton.removeClass("disabled");
		}
		fdq.setUntil(toDateInput.data("DateTimePicker").date());
		// cancel button
		if(published == null) {
			cancelButton.addClass("disabled");
		} else {
			cancelButton.removeClass("disabled");
		}
	}
	
	function applyFilter() {
		var from = moment(fromDateInput.data("DateTimePicker").date()).utc().format(fdq.utcFormat());
		var until = moment(toDateInput.data("DateTimePicker").date()).utc().format(fdq.utcFormat());
		var search = removePublishedSortFromSearch();
		search = search + (search.length == 0 ? "?" : "&") + "fq=published:[" + from + " TO " + until + "]";
		window.location.search=search;
	}

});