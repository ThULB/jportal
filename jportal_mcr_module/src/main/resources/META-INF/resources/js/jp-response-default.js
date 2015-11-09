// PUBLISHED SORT
$(document).ready(function() {
	// global highchart settings
	Highcharts.setOptions({
		global : {
			useUTC : false
		}
	});

	var fdq = new jp.solr.FacetDateQuery({
		solrURL : jp.baseURL + "servlets/solr/select",
		min : moment("1500", "YYYY"),
		max : moment()
	});

	var urlParameter = getUrlParameter();
	var published_sort = urlParameter["fq"] == null ? null : urlParameter["fq"].filter(function(p) {
		return p.startsWith("published_sort[");
	})[0];
	var fromString = published_sort == null ? null : published_sort.slice(published_sort.indexOf("[") + 1, published_sort.indexOf(" TO "));
	var untilString = published_sort == null ? null : published_sort.slice(published_sort.indexOf(" TO ") + 4, published_sort.indexOf("]"));
	fromString = fromString == "*" ? null : fromString;
	untilString = untilString == "*" ? null : untilString;

	var acceptButton = $("#published_sort_accept_button");
	var cancelButton = $("#published_sort_cancel_button");

	var fromDate = $('#published_sort_from');
	var toDate = $('#published_sort_to');
	fromDate.datetimepicker({
		locale : 'de',
		format : 'L',
		useCurrent: false,
		defaultDate: fromString == null ? null : moment.utc(fromString, fdq.utcFormat()).local()
	});
	toDate.datetimepicker({
		locale : 'de',
		format : 'L',
		defaultDate: untilString == null ? null : moment.utc(untilString, fdq.utcFormat()).local()
	});

	var popupButton = $("#published_sort_popup_button");
	popupButton.popover({
		content : function() {
			return "<div class='text-center'><i class='fa fa-spinner fa-spin'></i></div>";
		},
		title : 'Erschienene Werke',
		html : true,
		placement: "right"
	});
	var popup = new jp.PublishedPopup({
		contentNode : popupButton.data('bs.popover').tip().find(".popover-content"),
		query : fdq
	});
	$(popup).on("filter", function(e, from, until) {
		fromDate.data("DateTimePicker").date(from);
		toDate.data("DateTimePicker").date(until);
		popupButton.trigger("click");
	});

	popupButton.on("show.bs.popover", function() {
		fromDate.data("DateTimePicker").disable();
		toDate.data("DateTimePicker").disable();
		popupButton.find("i").toggleClass("fa-angle-right").toggleClass("fa-angle-left");
		popup.query.setFrom(fromDate.data("DateTimePicker").date());
		popup.query.setUntil(toDate.data("DateTimePicker").date());
		popup.executeQuery().done(function() {
			popup.updateFooter();
			var rect = popupButton[0].getClientRects();
			popupButton.data('bs.popover').tip().css("top", rect.top);
		});
	});
	popupButton.on("hide.bs.popover", function() {
		fromDate.data("DateTimePicker").enable();
		toDate.data("DateTimePicker").enable();
		popupButton.find("i").toggleClass("fa-angle-right").toggleClass("fa-angle-left");
	});

	// event handling
	fromDate.on("dp.show", function() {
		if(fromDate.data("DateTimePicker").date() == null) {
			fromDate.data("DateTimePicker").date(moment("1500", "YYYY"));
		}
	});
	fromDate.on("dp.change", function() {
		var acceptDisabled = fromDate.data("DateTimePicker").date() == null || toDate.data("DateTimePicker").date() == null;
		if(acceptDisabled) {
			acceptButton.addClass("disabled");
		} else {
			acceptButton.removeClass("disabled");
		}
		
		
		fdq.setFrom(fromDate.data("DateTimePicker").date());
	});
	toDate.on("dp.change", function() {
		updateButtons();
	});
	acceptButton.on("click", function() {
		var from = moment(fromDate.data("DateTimePicker").date()).utc().format(fdq.utcFormat());
		var until = moment(toDate.data("DateTimePicker").date()).utc().format(fdq.utcFormat());
		var search = removePublishedSortFromSearch();
		search = search + (search.length == 0 ? "?" : "&") + "fq=published_sort[" + from + " TO " + until + "]";
		window.location.search=search;
	});
	cancelButton.on("click", function() {
		window.location.search = removePublishedSortFromSearch();
	});

	updateButtons();

	function removePublishedSortFromSearch() {
		var search = window.location.search;
		var i = search.indexOf("fq=published_sort");
		if(i != -1) {
			search = search.substring(0, i - 1) + search.substring(search.indexOf("]", i + 1)  + 1, search.length)
		}
		return search;
	}

	function updateButtons() {
		// accept button
		if(fromDate.data("DateTimePicker").date() == null || toDate.data("DateTimePicker").date() == null) {
			acceptButton.addClass("disabled");
		} else {
			acceptButton.removeClass("disabled");
		}
		fdq.setUntil(toDate.data("DateTimePicker").date());
		// cancel button
		if(published_sort == null) {
			cancelButton.addClass("disabled");
		} else {
			cancelButton.removeClass("disabled");
		}
	}

});