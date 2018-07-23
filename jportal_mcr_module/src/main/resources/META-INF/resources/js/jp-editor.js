$(document).ready(function() {
	// create date fields
	createDate();

    $("select.dynamicBinding").change(function() {
	  updateBindings();
    });

	function updateBindings() {
	  $("select.dynamicBinding").each(function() {
	    let on = $(this).attr("on");
	    if(on == null) {
	      return;
	    }
        let row = $(this).closest(".row");
        let classid = on.split(":")[0];
        let categid = on.split(":")[1];
        let dependentBinding = $("select.dynamicBinding[data-classid='" + classid + "']");
        let display = (categid === dependentBinding.val()) ? "block" : "none";
        if(display === "none") {
          $(this).val("");
        }
	    row.css("display", display);
	  });
	}
	updateBindings();

    function createDate() {

        $(".jpdate-group").each(function() {
            let group = $( this );

            // select
            let dateSelect = group.find("input[value=date]");
            let rangeSelect = group.find("input[value=range]");
            dateSelect.on("change", onChangeDateSelect);
            rangeSelect.on("change", onChangeDateSelect);

            // dates
            let dateInputGroup = $(group.find(".input-group")[0]);
            let fromInputGroup = $(group.find(".input-group")[1]);
            let untilInputGroup = $(group.find(".input-group")[2]);
            let dateInput = dateInputGroup.find("input.date-field");
            let fromInput = fromInputGroup.find("input.date-field");
            let untilInput = untilInputGroup.find("input.date-field");
            let dateFrom = fromInput.val();

            // init dates on start
            if(dateFrom !== null && dateFrom !== undefined && dateFrom !== "") {
                rangeSelect.prop("checked", true).change();
            } else {
                dateSelect.prop("checked", true).change();
            }

            // clear dates before submit
            group.closest("form").submit(function() {
                if(dateSelect.prop("checked")) {
                    fromInput.val(null);
                    untilInput.val(null);
                } else {
                    dateInput.val(null);
                }
                return true;
            });

            function onChangeDateSelect(e) {
                let value = e.target.value;
                if(value === "date") {
                    dateInputGroup.show();
                    fromInputGroup.hide();
                    untilInputGroup.hide();
                } else {
                    dateInputGroup.hide();
                    fromInputGroup.show();
                    untilInputGroup.show();
                }
            }

        });

    }

    $.fn.dateCombiner = function () {
        let elements = this;

        elements.each(function (index, dateInput) {

            /*
             * error if yyyy-_-dd or _-mm-dd
             * error by letters, day 01 - 31, month 01 - 12
             * save data on original input
             * can add missed 0  ---> year = 2  now complete to  year = 0002  automatically on original input
             *
             * */
            let combineDate = function () {
                const year = forms["Jahr"].val();
                const month = forms["Monat"].val();
                const day = forms["Tag"].val();

                // error handling
                const errorTargetDiv = forms["Tag"];
                killError(errorTargetDiv);
                if(year === "" && (month !== "" || day !== "")) {
                    errorOutput("Die Jahresangabe fehlt!", errorTargetDiv);
                    return;
                } else if(month === "" && day !== "") {
                    errorOutput("Die Monatsangabe fehlt!", errorTargetDiv);
                    return;
                } else if(year.match(/[^-][^\d]/g) != null) {
                    errorOutput("Bitte Achten Sie darauf das sie nur Zahlen eingeben. Bsp: 1990, 1500, 50, -50", errorTargetDiv);
                    return;
                } else if(parseInt(month) < 1 || parseInt(month) > 12) {
                    errorOutput("Die Monatsangabe muss zwischen 1 und 12 liegen.", errorTargetDiv);
                    return;
                } else if(parseInt(day) < 1 || parseInt(day) > 31) {
                    errorOutput("Die Tagesangabe muss zwischen 1 und 31 liegen.", errorTargetDiv);
                    return;
                }

                // set date
                let format = "YYYY";
                const newDate = moment({
                    year: parseInt(year)
                });
                if(month !== "") {
                    newDate.month(parseInt(month) - 1);
                    format += "-MM";
                }
                if(day !== "") {
                    newDate.date(parseInt(day));
                    format += "-DD";
                }
                dateInputJq.val(newDate.isValid() ? newDate.format(format) : null);
            };

            let addForm = function (placeHolder, maxlength, value) {
                forms[placeHolder] = jQuery(inputBase);
                forms[placeHolder].attr("placeholder", placeHolder);
                forms[placeHolder].attr("maxlength", maxlength);
                forms[placeHolder].attr("title", "Eingabe nur als Zahl möglich!");
                forms[placeHolder].val(value);
                forms[placeHolder].on("keyup click", combineDate);
                forms[placeHolder].insertAfter(dateInputJq);
            };

            const dateInputJq = jQuery(dateInput);
            /* original input hidden */
            dateInputJq.css({
                "display": "none"
            });
            const forms = {};
            const inputBase = '<input class="form-control" style="width: 33.3%;">';
            const originalDate = dateInputJq.val();
            const isBC = originalDate.startsWith("-");
            const splitDate = isBC ? originalDate.substring(1).split("-") : originalDate.split("-");

            addForm("Tag", 2, splitDate[2]);
            addForm("Monat", 2, splitDate[1]);
            addForm("Jahr", 5, (isBC ? "-" : "") + splitDate[0]);
        });
    };

    $(".date-field").dateCombiner();

    function errorOutput(errorText, element) {
        if (element.parent().find(".jp-layout-errorBox").length === 0) {
            element.parent().addClass("has-error");
            $('<div role="alert" class="jp-layout-errorBox alert alert-danger">' + errorText + '</div>').insertAfter(element);
        }
    }

    function killError(element) {
        element.parent().removeClass("has-error");
        element.parent().find(".jp-layout-errorBox").remove();
    }

});

// jparticle - gnd location
$(document).ready(function () {
  $(".jp-gnd-location-form").each(function () {
    const form = $(this);
    const searchInput = $("<input type='text' class='form-control' maxlength='32' placeholder='GND ID' />");
    form.prepend(searchInput);
    loadData(form, searchInput);
    form.find("button").on("click", () => {
      let id = searchInput.val();
      const url = jp.baseURL + "rsc/gnd/location/" + id;
      jp.util.getJSON(url).then((data) => {
        if(data.label == null) {
          BootstrapDialog.alert('There is no pica+ field "065A a" for this record!');
          return;
        }
        setData(id, data.label, data.latitude, data.longitude, data.areaCode, form);
      }).catch((error) => {
        BootstrapDialog.alert('Error while getting record data from server. If you think this is an error please' +
          ' contact your administrator!');
        console.log(error);
      })
    });
  });

  function loadData(form, searchInput) {
    const inputDiv = form.prev(".jp-gnd-location-input");
    const id = inputDiv.find(".jp-gnd-location-input-id").val();
    const label = inputDiv.find(".jp-gnd-location-input-label").val();
    const areaCode = inputDiv.find(".jp-gnd-location-input-areaCode").val();
    const data = inputDiv.find(".jp-gnd-location-input-data").val();
    if (id !== "") {
      searchInput.val(id);
    }
    if(label !== "") {
      let display = label + " (" + data + ")";
      if(areaCode !== "") {
        display += " [" + areaCode +"]"
      }
      inputDiv.find(".jp-gnd-location-input-display").html(display);
    }
  }

  function setData(id, label, lat, lng, areaCode, form) {
    const inputDiv = form.prev(".jp-gnd-location-input");
    inputDiv.find(".jp-gnd-location-input-id").val(id);
    inputDiv.find(".jp-gnd-location-input-label").val(label);
    inputDiv.find(".jp-gnd-location-input-areaCode").val(areaCode);
    if(lat !== null && lng !== null) {
      inputDiv.find(".jp-gnd-location-input-data").val(lat + "," + lng);
      inputDiv.find(".jp-gnd-location-input-display").html(label + " (" + lat + "," + lng + ")");
    } else {
      inputDiv.find(".jp-gnd-location-input-data").val(null);
      inputDiv.find(".jp-gnd-location-input-display").html(label + " (keine Koordinaten gefunden!)");
    }
  }

});
