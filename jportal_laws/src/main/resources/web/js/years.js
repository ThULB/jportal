/*########################################################################
#	Y E A R I N T E R V A L
########################################################################*/
function createYears(/*int*/ count){
	var instance = this;
	this.domNode = dojo.create("div");
	var table = dojo.create("table");
	var tbody = dojo.create("tbody");
	var tr1 = dojo.create("tr");
	var tr2 = dojo.create("tr");
	var td1 = dojo.create("td");
	var td2 = dojo.create("td");
	var td3 = dojo.create("td");
	var td4 = dojo.create("td");

	var von = new dijit.form.DateTextBox({name: "yearFrom"+count, onChange: function(){dijit.byId('yearUntil'+count).constraints.min = arguments[0];} });
	td2.appendChild(von.domNode);
	dojo.create("label", { innerHTML: "von" }, td1);
	tr1.appendChild(td1);
	tr1.appendChild(td2);
	var bis = new dijit.form.DateTextBox({name: "yearUntil"+count, onChange: function(){dijit.byId('yearFrom'+count).constraints.min = arguments[0];} });
	td4.appendChild(bis.domNode);
	dojo.create("label", { innerHTML: "bis" }, td3);
	tr2.appendChild(td3);
	tr2.appendChild(td4);

	tbody.appendChild(tr1);
	tbody.appendChild(tr2);
	table.appendChild(tbody);
	this.domNode.appendChild(table);
	
};
( function() {
	// falls "alle Jahrgänge" angewählt wird, sollen die Datumsfelder verschwinden und leer sein.
	function hideYearInterval(checked) {
		if ( checked ) {
			dojo.attr("YearInterval", "style", {
	            display: "none"
	        });
			var w = dijit.byId('yearFrom');
	        w.attr('value', '');
			w = dijit.byId('yearUntil');
	        w.attr('value', '');
		} else {
			dojo.attr("YearInterval", "style", {
	            display: "block"
	        });
			var w = dijit.byId('yearFrom');
	        w.attr('value', '1805-01-01');
			w = dijit.byId('yearUntil');
	        w.attr('value', '1805-12-30');
		}
	}
	createYears.prototype.hideYearInterval = hideYearInterval;
})();