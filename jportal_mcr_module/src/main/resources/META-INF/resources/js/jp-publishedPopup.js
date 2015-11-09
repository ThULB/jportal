var jp = jp || {};

/**
 * Creates a new Published Popup.
 * 
 * @param options 
 * <ul>
 *  <li><b>contentNode - required</b>: node where the content is displayed</li>
 *  <li><b>query - required</b>: instance of jp.facet.FacetDateQuery</li>
 * </ul>
 */
jp.PublishedPopup = function(options) {
	options = typeof options !== 'undefined' ?  options : {};
	this.contentNode = options.contentNode;
	this.query = options.query;
	this.chart = null;

	this.chartContainerId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	    var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
	    return v.toString(16);
	});
	this.highchartsDiv = $("<div style='width: 500px;' id='" + this.chartContainerId + "'></div>");
	this.footerDiv = $("<div class='footer'></div>");
	this.fromText = $("<div></div>");
	this.untilText = $("<div></div>");

	if(this.contentNode == null) {
		throw "No contentNode parameter in options. Something like $('#mycontent') is excpected!";
	}
	if(this.query == null) {
		throw "No query parameter in options. An instance of jp.facet.FacetDateQuery is excpected!";
	}
}

/**
 * Executes the facet date query and rerender's the whole popup.
 * 
 * @return promise
 */
jp.PublishedPopup.prototype.executeQuery = function() {
	var that = this;
	return this.query.execute().then(function(data) {
		if(data == null) {
			that.render("Keine Werke in diesem Zeitraum gefunden.");
			return null;
		} else {
			var plotData = that.buildPlotData(data);
			that.renderPlotData(plotData);
			return plotData;
		}
	}).fail(function(e) {
		console.log(e);
		that.render("<div class='alert alert-danger' role='alert'>Error while loading data: " + e.statusText + "</div>");
	});
}

/**
 * Replaces the html of the popup with the given content.
 */
jp.PublishedPopup.prototype.render = function(content) {
	this.contentNode.html(content);
}

/**
 * Converts solr's date facet data to a highchart compatible one.
 */
jp.PublishedPopup.prototype.buildPlotData = function(/*array*/ data) {
	var utcFormat = this.query.utcFormat();
	var timeData = {};
	for(var i = 0; i < data.length; i+=2) {
		var timeStamp = moment.utc(data[i], utcFormat).valueOf();
		// solr returns the utc timestamp but ignores the dst for unknown reason
		// im not sure if this is the best way to fix it but its works (for my test cases)
		timeStamp = moment(timeStamp).startOf("day").valueOf();
		timeData[timeStamp] = data[i + 1];
	}
	var uot = this.query.unitOfTime();
	var plotData = [];
	var runningDate = moment(this.query.getFrom().valueOf());
	while(runningDate.valueOf() < this.query.getUntil().valueOf()) {
		var value = timeData[runningDate.valueOf()];
		plotData.push([runningDate.valueOf(), value != null ? value : 0]);
		runningDate.add(1, uot);
	}
	return plotData;
}

/**
 * Renders the highchart with the given plot data.
 */
jp.PublishedPopup.prototype.renderPlotData = function(plotData) {
	this.zoomInButton = $("<a href='javascript:void(0)' class='btn btn-default disabled'><i class='fa fa-search-plus'></i></a>");
	this.zoomOutButton = $("<a href='javascript:void(0)' class='btn btn-default disabled' style='margin-right: 4px;'><i class='fa fa-search-minus'></i></a>");
	this.filterButton = $("<a href='javascript:void(0)' class='btn btn-primary'>Filter anwenden</a>");

	this.zoomInButton.on("click", $.proxy(this.onZoomIn, this));
	this.zoomOutButton.on("click", $.proxy(this.onZoomOut, this));
	this.filterButton.on("click", $.proxy(this.onFilter, this));

	this.footerDiv.empty();
	this.highchartsDiv.empty();

	// date display
	var dateDisplayDiv = $("<div style='display: flex; justify-content: space-between; font-size: 0.9em;'></div>");
	dateDisplayDiv.append(this.fromText);
	dateDisplayDiv.append(this.untilText);

	// toolbar
	var toolbarDiv = $("<div style='display: flex; justify-content: space-between;'></div>")
	var leftToolbarDiv = $("<div></div>");
	var rightToolbarDiv = $("<div></div>");
	leftToolbarDiv.append(this.zoomOutButton);
	leftToolbarDiv.append(this.zoomInButton);
	rightToolbarDiv.append(this.filterButton);
	toolbarDiv.append(leftToolbarDiv);
	toolbarDiv.append(rightToolbarDiv);

	// footer
	this.footerDiv.append(dateDisplayDiv);
	this.footerDiv.append("<hr style='margin: 4px 0px 12px;' />");
	this.footerDiv.append(toolbarDiv);

	// highcharts
	this.contentNode.html(this.highchartsDiv);
	this.contentNode.append(this.footerDiv);
	this.chart = new Highcharts.StockChart(this.getHighchartOptions(plotData));
}

jp.PublishedPopup.prototype.getHighchartOptions = function(plotData) {
	var uot = this.query.unitOfTime();
	return {
		xAxis: {
            type: 'datetime',
            ordinal: false
        },
        rangeSelector: {
        	enabled: false
        },
        series : [{
            name : 'Erschienen',
            data : plotData,
            type: 'column',
            tooltip: {
                valueDecimals: 0,
                valueSuffix: " Werk(e)",
                xDateFormat: this.toHighChartsDateFormat(uot)
            }
        }],
        tooltip: {
            xDateFormat: this.toHighChartsDateFormat(uot),
            shared: true
        },
        chart: {
        	renderTo: this.chartContainerId,
            events: {
                redraw: $.proxy(this.updateFooter, this)
            }
        },
        credits: {
			enabled: false
		}
    };
}

jp.PublishedPopup.prototype.toHighChartsDateFormat = function(/*string*/ unitOfTime) {
	if(unitOfTime == "year") {
		return "%Y";
	} else if(unitOfTime == "day") {
		return "%Y-%m-%d";
	}
	throw "Unsupported unit of time " + unitOfTime;
}

/**
 * Updates the footer based on the current highchart status.
 */
jp.PublishedPopup.prototype.updateFooter = function() {
	var from = this.query.getFrom().valueOf();
	var until = this.query.getUntil().valueOf();
	if(this.chart != null && this.chart.xAxis != null && this.chart.xAxis[0] != null) {
		var ce = this.chart.xAxis[0].getExtremes();
		// filter button
		// zoom in
		var zoomInDisabled = ce.min == ce.dataMin && ce.max == ce.dataMax;
		if(zoomInDisabled != this.zoomInButton.hasClass('disabled')) {
			this.zoomInButton.toggleClass('disabled');
		}
		// zoom out
		var zoomOutDisabled = this.query.min.valueOf() == from && this.query.max.valueOf() == until;
		if(zoomOutDisabled != this.zoomOutButton.hasClass('disabled')) {
			this.zoomOutButton.toggleClass('disabled');
		}
		// text
		this.fromText.html(moment(ce.min).format("Do MMM YYYY"));
		this.untilText.html(moment(ce.max).endOf(this.query.unitOfTime()).format("Do MMM YYYY"));
	}
}

/**
 * Zoom's and reloads the highchart.
 * 
 * @param from
 * @param until
 */
jp.PublishedPopup.prototype.zoom = function(from, until) {
	this.query.setFrom(from);
	this.query.setUntil(until);
	var that = this;
	this.render("<div class='text-center'><i class='fa fa-spinner fa-spin'></i></div>");
	this.executeQuery().done(function() {
		that.updateFooter();
	});
}

/**
 * Fired when the zoom in button was clicked.
 */
jp.PublishedPopup.prototype.onZoomIn = function() {
	var ce = this.chart.xAxis[0].getExtremes();
	this.zoom(moment(ce.min), moment(ce.max));
}

/**
 * Fired when the zoom out button was clicked.
 */
jp.PublishedPopup.prototype.onZoomOut = function() {
	this.zoom(null, null);
}

/**
 * Fired when the filter button was clicked.
 */
jp.PublishedPopup.prototype.onFilter = function() {
	var ce = this.chart.xAxis[0].getExtremes();
	var from = moment(ce.min);
	var until = moment(ce.max).endOf(this.query.unitOfTime());
	$(this).trigger("filter", [from, until]);
}
