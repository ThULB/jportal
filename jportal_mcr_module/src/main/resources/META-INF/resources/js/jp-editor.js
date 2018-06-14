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

            let dateInputJq = jQuery(dateInput);

            /* original input hidden */
            dateInputJq.css({
                "display": "none"
            });

            let formsData = {
                "Tag": "dd",
                "Monat": "MM",
                "Jahr": "yyyy"
            };

            let forms = {};

            let inputBase = '<input class="form-control" style="width: 33.3%;">';

            /*
             * error if yyyy-_-dd or _-mm-dd
             * error by letters, day 01 - 31, month 01 - 12
             * save data on original input
             * can add missed 0  ---> year = 2  now complete to  year = 0002  automatically on original input
             *
             * */
            let combineDate = function () {
                if (forms["Jahr"].val() !== "") {
                    dateInputJq.val("");
                    if (forms["Jahr"].val().match(/[^0-9]/g) != null) {
                        errorOutput("Bitte Achten Sie darauf das sie nur Zahlen eingeben. Bsp: 1990, 1500, 0100, 100, 10", forms["Tag"]);
                    } else {
                        if (forms["Jahr"].val().length < 4) {
                            for (let i = forms["Jahr"].val().length; i < 4; i++) {
                                dateInputJq.val(dateInputJq.val() + "0");
                            }
                        }
                        dateInputJq.val(dateInputJq.val() + forms["Jahr"].val());
                        killError(forms["Jahr"]);

                        if (forms["Monat"].val() !== "") {
                            // check right month input
                            if (forms["Monat"].val() > "12" || forms["Monat"].val() < "01") {
                                errorOutput("Die Monatsangabe muss zwischen 01 und 12 liegen.", forms["Tag"]);
                            } else {
                                let extraNul = "";
                                if (forms["Monat"].val().length < 2) extraNul = "0";
                                dateInputJq.val(dateInputJq.val() + "-" + extraNul + forms["Monat"].val());

                                if (forms["Tag"].val() !== "") {
                                    // check right day input
                                    if (forms["Tag"].val() > "31" || forms["Tag"].val() < "01") {
                                        errorOutput("Die Tagesangabe muss zwischen 01 und 31 liegen.", forms["Tag"]);
                                    } else {
                                        extraNul = "";
                                        if (forms["Tag"].val().length < 2) extraNul = "0";
                                        dateInputJq.val(dateInputJq.val() + "-" + extraNul + forms["Tag"].val());
                                    }
                                }
                            }
                        } else {
                            if (forms["Tag"].val() !== "") {
                                errorOutput("Die Monatsangabe fehlt!", forms["Tag"]);
                            }
                        }
                    }
                } else {
                    if (forms["Monat"].val() !== "" || forms["Tag"].val() !== "") {
                        errorOutput("Die Jahresangabe fehlt!", forms["Tag"]);
                    } else {
                        killError(forms["Monat"]);
                    }
                    dateInputJq.val("");
                }
            };

            let content = dateInputJq.val().split("-");
            let zaehler = 2;

            /* settings for the input */
            for (let placeHolder in formsData) {
                let maxlength = formsData[placeHolder].length;
                forms[placeHolder] = jQuery(inputBase);
                forms[placeHolder].attr("placeholder", placeHolder);
                forms[placeHolder].attr("maxlength", maxlength);
                forms[placeHolder].attr("title", "Eingabe nur als Zahl möglich!");
                forms[placeHolder].val(content[zaehler]);
                forms[placeHolder].on("keyup click", combineDate);
                forms[placeHolder].insertAfter(dateInputJq);
                zaehler--;
            }
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
      const url = jp.baseURL + "rsc/sru/get/" + id + "?fields=065A";
      jp.util.getJSON(url).then((data) => {
        const label = getValue("065A", "a", null, null, data.fields);
        if(label == null) {
          BootstrapDialog.alert('There is no pica+ field "065A a" for this record!');
          return;
        }
        let lat = getValue("037H", "f", "A", "dgx", data.fields);
        let lng = getValue("037H", "d", "A", "dgx", data.fields);
        if(lat !== null && lng !== null) {
          lat = parseFloat(lat.substring(1));
          lng = parseFloat(lng.substring(1));
        }
        setData(id, label, lat, lng, form);
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
    const data = inputDiv.find(".jp-gnd-location-input-data").val();
    if (id !== "") {
      searchInput.val(id);
    }
    if(label !== "") {
      inputDiv.find(".jp-gnd-location-input-display").html(label + " (" + data + ")");
    }
  }

  function setData(id, label, lat, lng, form) {
    const inputDiv = form.prev(".jp-gnd-location-input");
    inputDiv.find(".jp-gnd-location-input-id").val(id);
    inputDiv.find(".jp-gnd-location-input-label").val(label);
    if(lat !== null && lng !== null) {
      inputDiv.find(".jp-gnd-location-input-data").val(lat + "," + lng);
      inputDiv.find(".jp-gnd-location-input-display").html(label + " (" + lat + "," + lng + ")");
    } else {
      inputDiv.find(".jp-gnd-location-input-data").val(null);
      inputDiv.find(".jp-gnd-location-input-display").html(label + " (keine Koordinaten gefunden!)");
    }
  }

  function getValue(name, code, reqCode, reqValue, fields) {
    for (let i = 0; i < fields.length; i++) {
      if (fields[i].name === name) {
        let subfields = fields[i].subfields;
        if(!checkSubfieldCode(reqCode, reqValue, subfields)) {
          continue;
        }
        for (let j = 0; j < subfields.length; j++) {
          if (subfields[j].code === code) {
            return subfields[j].value;
          }
        }
      }
    }
    return null;
  }

  function checkSubfieldCode(reqCode, reqValue, subfields) {
    if (reqCode == null) {
      return true;
    }
    for (let i = 0; i < subfields.length; i++) {
      if (subfields[i].code === reqCode) {
        if (reqValue == null || reqValue === subfields[i].value) {
          return true;
        }
      }
    }
    return false;
  }

});
