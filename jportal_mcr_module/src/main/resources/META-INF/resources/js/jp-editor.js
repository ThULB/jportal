var jp = jp || {};
jp.editor = {};

jp.editor.init = function() {
	// create date fields
	createDate();

	let journalsDates = undefined;

    function getJournalDates(func){
	    if(journalsDates === undefined){
            const journalID = document.getElementById("journalID").textContent;
            const getJournalDatesURL = jp.baseURL + "rsc/object/journalDates/" + journalID;

            jp.util.getJSON(getJournalDatesURL)
                .then((d) => {
                    let publishedDate = d.find(date => date.type === "published");
                    journalsDates = {
                        startDate: undefined,
                        endDate: undefined,
                        error: undefined
                    };

                    if(publishedDate !== undefined) {
                        if (publishedDate.date) {
                            journalsDates.startDate = new Date(publishedDate.date);
                        } else if (publishedDate.from) {
                            journalsDates.startDate = new Date(publishedDate.from);
                        }

                        if (publishedDate.until) {
                            journalsDates.endDate = new Date(publishedDate.until);
                        }
                    }

                    func(journalsDates);
                })
                .catch(error => {
                    journalsDates = {error: error};
                    func(journalsDates);
                });
        } else {
	        func(journalsDates);
        }
    }

    const onChangeEvent = new Event("onchange");
    document.querySelectorAll("select.dynamicBinding")
        .foreach(select => {
            if(select.onchange !== updateBindings){
                select.onchange = updateBindings;
                select.dispatchEvent(onChangeEvent);
            }
        });

	function updateBindings(event) {
	    const currentSelect = event.target;
	    let on = currentSelect.getAttribute("on");
	    if(on == null) {
	      return;
	    }

        let row = currentSelect.closest(".row");
        let classid = on.split(":")[0];
        let categid = on.split(":")[1];
        let dependentBinding = document.querySelector("select.dynamicBinding[data-classid='" + classid + "']");
        let display = (categid === dependentBinding.val()) ? "block" : "none";
        if(display === "none") {
          currentSelect.value = "";
        }
	    row.css("display", display);
	}

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

    function dateCombiner(dateInput) {
            /*
             * error if yyyy-_-dd or _-mm-dd
             * error by letters, day 01 - 31, month 01 - 12
             * save data on original input
             * can add missed 0  ---> year = 2  now complete to  year = 0002  automatically on original input
             *
             * */
            let combineDate = function (pubishedDates) {
                const year = forms["Jahr"].value;
                const month = forms["Monat"].value;
                const day = forms["Tag"].value;

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

                console.log("journalDates: " + JSON.stringify(pubishedDates))


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
                dateInput.value = newDate.isValid() ? newDate.format(format) : null;
            };

            let addForm = function (placeHolder, maxlength, value) {
                forms[placeHolder] = Object.assign(document.createElement("input"), inputBase);
                forms[placeHolder].placeholder = placeHolder;
                forms[placeHolder].maxlength = maxlength;
                forms[placeHolder].title = "Eingabe nur als Zahl möglich!";
                forms[placeHolder].value = value;
                forms[placeHolder].oninput = e => {
                    //wait 500ms for input then exec combineDate
                    let saveOnInput = e.target.oninput;
                    e.target.oninput = undefined;
                    setTimeout(() => {
                        getJournalDates(d => combineDate(d));
                        e.target.oninput = saveOnInput;
                    }, 500);
                };

                //insertAfter
                dateInput.parentNode.insertBefore(forms[placeHolder], dateInput.nextSibling);
            };

            dateInput.style.display = "none";

            //const dateInputJq = jQuery(dateInput);
            /* original input hidden */
            //dateInputJq.css({
            //    "display": "none"
            //});
            const forms = {};
            //const inputBase = '<input class="form-control" style="width: 33.3%;">';
            const inputBase = {className: "form-control", style: "width: 33.3%;"};
            const originalDate = dateInput.value;
            const isBC = originalDate.startsWith("-");
            const splitDate = isBC ? originalDate.substring(1).split("-") : originalDate.split("-");

            addForm("Tag", 2, splitDate[2]);
            addForm("Monat", 2, splitDate[1]);
            addForm("Jahr", 5, (isBC ? "-" : "") + splitDate[0]);
    };

    document.querySelectorAll(".date-field")
        .forEach(dateCombiner)

    function errorOutput(errorText, element) {
        if (element.parentNode.querySelectorAll(".jp-layout-errorBox").length === 0) {
            element.parentNode.classList.add("has-error");
            let alertDiv = document.createElement("div");
            alertDiv.className = "jp-layout-errorBox alert alert-danger";
            alertDiv.role = "alert";
            alertDiv.textContent = errorText;
            //insertAfter
            element.parentNode.insertBefore(alertDiv, element.nextSibling);
        }
    }

    function killError(element) {
        element.parentNode.classList.remove("has-error");
        let errorBox = element.parentNode.querySelector(".jp-layout-errorBox");
        if(errorBox !== null && errorBox !== undefined){
            errorBox.remove();
        }
    }

};

// jparticle - gnd location
jp.editor.articleGNDLocation = function () {
    document.querySelectorAll(".jp-gnd-location-form")
        .forEach((form) => {
            let searchInput = document.createElement("input");
            searchInput.className = "form-control";
            searchInput.type = "text";
            searchInput.placeholder = "GND ID";

            form.insertBefore(searchInput, form.firstChild);
            loadData(form, searchInput);
            form.querySelector("button").onclick = () => {
                const id = searchInput.value;
                const url = jp.baseURL + "rsc/gnd/location/" + id;
                jp.util.getJSON(url).then((data) => {
                    if (data.label == null) {
                        BootstrapDialog.alert('There is no pica+ field "065A a" for this record!');
                        return;
                    }
                    setData(id, data.label, data.latitude, data.longitude, data.areaCode, form);
                }).catch((error) => {
                    BootstrapDialog.alert('Error while getting record data from server. If you think this is an error please' +
                        ' contact your administrator!');
                    console.log(error);
                })
            }
    });

  function loadData(form, searchInput) {
    const inputDiv = form.previousElementSibling;
    const id = inputDiv.querySelector(".jp-gnd-location-input-id").value;
    const label = inputDiv.querySelector(".jp-gnd-location-input-label").value;
    const areaCode = inputDiv.querySelector(".jp-gnd-location-input-areaCode").value;
    const data = inputDiv.querySelector(".jp-gnd-location-input-data").value;
    if (id !== "") {
      searchInput.val(id);
    }
    if(label !== "") {
      let display = label + " (" + data + ")";
      if(areaCode !== "") {
        display += " [" + areaCode +"]"
      }
      inputDiv.querySelector(".jp-gnd-location-input-display").textContent = display;
    }
  }

  function setData(id, label, lat, lng, areaCode, form) {
    const inputDiv = form.previousElementSibling;
    inputDiv.querySelector(".jp-gnd-location-input-id").value = id;
    inputDiv.querySelector(".jp-gnd-location-input-label").value = label;
    inputDiv.querySelector(".jp-gnd-location-input-areaCode").value = areaCode;
    if(lat !== null && lng !== null) {
      inputDiv.querySelector(".jp-gnd-location-input-data").value = lat + "," + lng;
      inputDiv.querySelector(".jp-gnd-location-input-display")
          .textContent = label + " (" + lat + "," + lng + ")";
    } else {
      inputDiv.querySelector(".jp-gnd-location-input-data").value = null;
      inputDiv.querySelector(".jp-gnd-location-input-display")
          .textContent = label + " (keine Koordinaten gefunden!)";
    }
  }

};
