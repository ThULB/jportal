var jp = jp || {};

/**
 * Creates a new Published Dialog.
 * 
 * @param options
 * <ul>
 * 	<li><b>title - required</b>: dialog title</li>
 *  <li><b>filterButtonText- required</b> text of the filter button</li>
 *  <li><b>query - required</b>: instance of jp.facet.FacetDateQuery</li>
 * </ul>
 */
jp.PublishedDialog = function(options) {
	this.dialog = new BootstrapDialog({
        title: options.title,
        message: $.proxy(this.render, this),
        onshown: $.proxy(this.onDialogShow, this),
        autodestroy: false
    });

	this.query = options.query;
	this.chart = null;

	this.chartContainerId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	    var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
	    return v.toString(16);
	});
	this.highchartsDiv = $("<div id='" + this.chartContainerId + "' style='height: 450px; display: flex; justify-content: center; align-items: center;'></div>");
	this.fromText = $("<div></div>");
	this.untilText = $("<div></div>");

	this.zoomInButton = $("<a href='javascript:void(0)' class='btn btn-default disabled'><i class='fa fa-search-plus'></i></a>");
	this.zoomOutButton = $("<a href='javascript:void(0)' class='btn btn-default disabled' style='margin-right: 4px;'><i class='fa fa-search-minus'></i></a>");
	this.filterButton = $("<a href='javascript:void(0)' class='btn btn-primary'>" + options.filterButtonText + "</a>");

	this.zoomInButton.on("click", $.proxy(this.onZoomIn, this));
	this.zoomOutButton.on("click", $.proxy(this.onZoomOut, this));
	this.filterButton.on("click", $.proxy(this.onFilter, this));
}

/**
 * Opens the dialog
 */
jp.PublishedDialog.prototype.open = function() {
	this.dialog.open();
}

/**
 * Close the dialog
 */
jp.PublishedDialog.prototype.close = function() {
	this.dialog.close();
}

jp.PublishedDialog.prototype.onDialogShow = function() {
	if(this.chart == null) {
		var that = this;
		this.updateChart().done(function() {
			that.updateFooter();
		});
	}
}

jp.PublishedDialog.prototype.updateChart = function() {
	this.renderContent("<div class='text-center'><i class='fa fa-spinner fa-spin'></i></div>");
	var that = this;
	return this.query.execute().then(function(data) {
		if(data == null) {
			that.renderContent("<div class='text-center'>Keine Werke/Artikel in diesem Zeitraum gefunden.</div>");
			return null;
		} else {
			var plotData = that.buildPlotData(data);
			that.chart = new Highcharts.StockChart(that.getHighchartOptions(plotData));
			return plotData;
		}
	}).fail(function(e) {
		console.log(e);
		that.renderContent("<div class='alert alert-danger' role='alert'>Error while loading data: " + e.statusText + "</div>");
	});
}

jp.PublishedDialog.prototype.renderContent = function(html) {
	
	this.highchartsDiv.html(html);
}

jp.PublishedDialog.prototype.render = function() {
	console.log("render");
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
	var footerDiv = $("<div class='footer'></div>");
	footerDiv.append(dateDisplayDiv);
	footerDiv.append("<hr style='margin: 4px -15px 12px;' />");
	footerDiv.append(toolbarDiv);

	// main div
	var mainDiv = $("<div class='main'></div>");
	mainDiv.append(this.highchartsDiv);
	mainDiv.append(footerDiv);
	return mainDiv;
}

jp.PublishedDialog.prototype.getHighchartOptions = function(plotData) {
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
                valueSuffix: " Werke/Artikel",
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

jp.PublishedDialog.prototype.toHighChartsDateFormat = function(/*string*/ unitOfTime) {
	if(unitOfTime == "year") {
		return "%Y";
	} else if(unitOfTime == "day") {
		return "%Y-%m-%d";
	}
	throw "Unsupported unit of time " + unitOfTime;
}

/**
 * Converts solr's date facet data to a highchart compatible one.
 */
jp.PublishedDialog.prototype.buildPlotData = function(/*array*/ data) {
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
 * Zoom's and reloads the highchart.
 * 
 * @param from
 * @param until
 */
jp.PublishedDialog.prototype.zoom = function(from, until) {
	this.query.setFrom(from);
	this.query.setUntil(until);
	var that = this;
	this.updateChart();
}

/**
 * Fired when the zoom in button was clicked.
 */
jp.PublishedDialog.prototype.onZoomIn = function() {
	var ce = this.chart.xAxis[0].getExtremes();
	this.zoom(moment(ce.min), moment(ce.max));
}

/**
 * Fired when the zoom out button was clicked.
 */
jp.PublishedDialog.prototype.onZoomOut = function() {
	this.zoom(null, null);
}

/**
 * Fired when the filter button was clicked.
 */
jp.PublishedDialog.prototype.onFilter = function() {
	var ce = this.chart.xAxis[0].getExtremes();
	var from = moment(ce.min);
	var until = moment(ce.max).endOf(this.query.unitOfTime());
	$(this).trigger("filter", [from, until]);
}

/**
 * Updates the footer based on the current highchart status.
 */
jp.PublishedDialog.prototype.updateFooter = function() {
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
