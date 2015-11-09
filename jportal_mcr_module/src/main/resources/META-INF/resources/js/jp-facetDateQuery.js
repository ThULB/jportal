var jp = jp || {};
jp.solr = jp.solr || {};

/**
 * Creates a new DateFilter.
 * 
 * @param options 
 * <ul>
 *  <li><b>solrURL - required</b>: URL to the solr server</li>
 *  <li><b>from - optional</b>: start date</li>
 *  <li><b>until - optional</b>: end date</li>
 *  <li><b>solrField - optional</b>: date field which is queried by solr. the default value is 'published_sort'</li>
 * </ul>
 */
jp.solr.FacetDateQuery = function(options) {
	options = typeof options !== 'undefined' ?  options : {};
	/*string*/ this.solrURL = options.solrURL;
	/*string*/ this.solrField = options.solrField;
	/*moment.date*/ this.from = options.from;
	/*moment.date*/ this.until = options.until;
	/*moment.date*/ this.min = options.min;
	/*moment.date*/ this.max = options.max;

	if(this.solrURL == null) {
		throw "No solrURL attribute in options parameter";
	}
	if(this.solrField == null) {
		this.solrField = "published_sort";
	}
}

/**
 * Sets the from date.
 * 
 * @param newFrom a moment date
 */
jp.solr.FacetDateQuery.prototype.setFrom = function(/*moment.date*/ newFrom) {
	this.from = newFrom;
}

/**
 * Sets the until date.
 * 
 * @param newUntil a moment date
 */
jp.solr.FacetDateQuery.prototype.setUntil = function(/*moment.date*/ newUntil) {
	this.until = newUntil;
}

/**
 * Return the from date.
 * 
 * @return moment date
 */
jp.solr.FacetDateQuery.prototype.getFrom = function() {
	var from = null;
	if(this.until != null && this.from != null) {
		from = moment(this.from.valueOf());
		from.startOf(this.unitOfTime());
	}
	return (from == null || this.min.valueOf() > from.valueOf()) ? moment(this.min.valueOf()) : from;
}

jp.solr.FacetDateQuery.prototype.getOrFetchFrom = function() {
	if(this.from != null) {
		return $.when(this.from);
	}
	return this.fetchEarliest().then(function(date) {
		return date;
	});
}

/**
 * Return the until date.
 * 
 * @return moment date
 */
jp.solr.FacetDateQuery.prototype.getUntil = function() {
	var until = null;
	if(this.until != null && this.from != null) {
		until = moment(this.until.valueOf());
		until.endOf(this.unitOfTime());
	}
	return (until == null || this.max.valueOf() < until.valueOf()) ? moment(this.max.valueOf()) : until;
}

jp.solr.FacetDateQuery.prototype.getOrFetchUntil = function() {
	if(this.until != null) {
		return $.when(this.until);
	}
	return this.fetchLatest().then(function(date) {
		return date;
	});
}

/**
 * Returns the earliest date.
 * 
 * @return jquery promise (@see https://api.jquery.com/promise)
 *   the done(date) method contains the earliest date or null if there is none
 */
jp.solr.FacetDateQuery.prototype.fetchEarliest = function() {
	return this.fetchDate(this.solrField + " asc");
}

/**
 * Returns the latest date.
 * 
 * @return jquery promise (@see https://api.jquery.com/promise)
 *   the done(date) method contains the latest date or null if there is none
 */
jp.solr.FacetDateQuery.prototype.fetchLatest = function() {
	return this.fetchDate(this.solrField + " desc");
}

/**
 * Fetches a single date from solr based on the sorting.
 * 
 * @param sort the field and order (e.g. 'published_sort asc')
 * @return jquery promise (@see https://api.jquery.com/promise)
 *   the done(date) method contains the requested date or null if there is none
 */
jp.solr.FacetDateQuery.prototype.fetchDate = function(sort) {
	var url = this.solrURL + "?q=" + this.solrField + ":*&sort=" + sort + "&rows=1&fl=date.published&wt=json";
	return $.ajax({
		url: url,
		dataType: "json",
	}).then(function(data) {
		try {
			return moment.utc(data.response.docs[0]["date.published"], "YYYY-MM-DD");
		} catch(e) {
			return null;
		}
	})
}

/**
 * Return the utc format in moment style.
 * 
 * @return the utc format
 */
jp.solr.FacetDateQuery.prototype.utcFormat = function() {
	return "YYYY-MM-DD[T]HH:mm:ss[Z]";
}

/**
 * Returns the best matching unit of time for the passed time between
 * from and until. Possible return values are "year", "month" and
 * "day".
 * 
 * @return "year", "month" or "day"
 */
jp.solr.FacetDateQuery.prototype.unitOfTime = function() {
	return this.unitOfTimeFunction(this.from, this.until);
}

jp.solr.FacetDateQuery.prototype.unitOfTimeFunction = function(/*moment.date*/ from, /*moment.date*/ until) {
	if(from == null || until == null) {
		return "year";
	}
	var duration = moment.duration(until.valueOf() - from.valueOf());
	return duration.years() > 20  ? "year" : "day";
}

/**
 * Returns the facet date query url.
 * 
 * @return jquery promise (@see https://api.jquery.com/promise)
 */
jp.solr.FacetDateQuery.prototype.getFacetRangeURL = function() {
	var utcFormat = this.utcFormat();
	var solrURL = this.solrURL;
	solrURL += "?q=*:*&rows=0&wt=json&facet=true&facet.range=" + this.solrField;
	solrURL += "&facet.range.start=" + moment(this.getFrom().valueOf()).utc().format(utcFormat);
	solrURL += "&facet.range.end=" + moment(this.getUntil().valueOf()).utc().format(utcFormat);
	solrURL += "&facet.range.gap=%2B1" + this.unitOfTime().toUpperCase();
	return solrURL;
}

/**
 * Executes the query and fetches the data from solr.
 * 
 * @return jquery promise (@see https://api.jquery.com/promise)
 *   the done(data) method contains the requested solr data
 */
jp.solr.FacetDateQuery.prototype.execute = function() {
	var that = this;
	return $.ajax({
		url: this.getFacetRangeURL(),
		dataType: "json"
	})
	.then(function(data) {
		if(data.facet_counts == null || data.facet_counts.facet_ranges == null
				|| data.facet_counts.facet_ranges[that.solrField] == null ||
				data.facet_counts.facet_ranges[that.solrField].counts == null ||
				data.facet_counts.facet_ranges[that.solrField].counts.length == 0) {
			return null;
		}
		return data.facet_counts.facet_ranges.published_sort.counts;
	});
}