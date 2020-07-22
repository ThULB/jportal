var jp = jp || {};
jp.editor = {};
jp.editor.dates = {
    range: false,
    date: undefined,
    from: undefined,
    until: undefined
}

jp.editor.journalDates = undefined;

jp.editor.init = function() {
	// create date fields
	createDate();

	function newDate(dateStr) {
	    let date = new Date(dateStr);

	    return isNaN(date) ? undefined : date;
    }
    function getJournalDates(func){
	    if(jp.editor.journalDates === undefined){
            const journalID = document.getElementById("journalID").textContent;
            const getJournalDatesURL = jp.baseURL + "rsc/object/journalDates/" + journalID;

            jp.util.getJSON(getJournalDatesURL)
                .then((d) => {
                    let publishedDate = d.find(date => date.type === "published");
                    jp.editor.journalDates = {
                        startDate: undefined,
                        endDate: undefined,
                        error: undefined
                    };

                    if(publishedDate !== undefined) {
                        if (publishedDate.date) {
                            jp.editor.journalDates.startDate = newDate(publishedDate.date);
                        } else if (publishedDate.from) {
                            jp.editor.journalDates.startDate = newDate(publishedDate.from);
                        }

                        if (publishedDate.until) {
                            jp.editor.journalDates.endDate = newDate(publishedDate.until);
                        }
                    }

                    func(jp.editor.journalDates);
                })
                .catch(error => {
                    jp.editor.journalDates = {error: error};
                    func(jp.editor.journalDates);
                });
        } else {
	        func(jp.editor.journalDates);
        }
    }

    function formatDate(date) {
        let fullYear = date.getFullYear();
        let month = date.getMonth()+1;
        let day = date.getDay();
        return [fullYear, month.toString().padStart(2, '0'), day.toString().padStart(2, '0')].join("-");
    }

    function checkDatesForLogicalErrors(journalDates, errorTargetDiv) {
        if(jp.editor.dates.range){
            if(jp.editor.dates.from !== undefined){
                let dateFrom = jp.editor.dates.from.getTime();
                let journalDateFrom = jp.editor.journalDates.startDate;

                if(journalDateFrom !== undefined && dateFrom < journalDateFrom) {
                    errorOutput("Das Anfangsdatum darf nicht vor dem Erscheinungsdatum der Zeitschrift "
                        + formatDate(journalDateFrom) + " liegen.", errorTargetDiv);
                    return;
                }

                if (jp.editor.dates.until !== undefined) {
                    let dateUntil = jp.editor.dates.until.getTime();

                    if (dateUntil < dateFrom) {
                        errorOutput("Das Enddatum sollte in der Zeit nach dem Anfangsdatum sein.", errorTargetDiv);
                        return;
                    }

                    let journalDateUntil = jp.editor.journalDates.endDate;

                    if (journalDateUntil !== undefined && journalDateUntil < dateUntil) {
                        errorOutput("Das Anfangsdatum darf nicht nach dem Ende des Erscheinungsdatum der Zeitschrift "
                            + formatDate(journalDateUntil) + " liegen.", errorTargetDiv);
                        return;
                    }
                }
            }
        } else {
            if(jp.editor.dates.date !== undefined){
                let journalDateFrom = jp.editor.journalDates.startDate;
                let journalDateUntil = jp.editor.journalDates.endDate;

                if(journalDateFrom != undefined && jp.editor.dates.date < journalDateFrom) {
                    errorOutput("Das Datum darf nicht vor dem Erscheinungsdatum der Zeitschrift "
                        + formatDate(journalDateFrom) + " liegen.", errorTargetDiv);
                    return;
                }

                if(journalDateUntil !== undefined && journalDateUntil < jp.editor.dates.date) {
                    errorOutput("Das Datum darf nicht nach dem Ende des Erscheinungsdatum der Zeitschrift "
                        + formatDate(journalDateUntil) + " liegen.", errorTargetDiv);
                    return;
                }
            }
        }
    }

    document.querySelectorAll("select.dynamicBinding")
        .forEach(select => {
            if(select.onchange !== updateBindings){
                select.onchange = updateBindings;
                select.dispatchEvent(new Event("change"));
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

        document.querySelectorAll(".jpdate-group")
            .forEach(group => {
                // select
                let dateSelect = group.querySelector("input[value=date]");
                let rangeSelect = group.querySelector("input[value=range]");
                dateSelect.onchange = onChangeDateSelect;
                rangeSelect.onchange = onChangeDateSelect;

                // dates
                let inputGroup = group.querySelectorAll(".input-group");
                let dateInputGroup = inputGroup[0];
                let fromInputGroup = inputGroup[1];
                let untilInputGroup = inputGroup[2];
                let dateInput = dateInputGroup.querySelector("input.date-field");
                let fromInput = fromInputGroup.querySelector("input.date-field");
                let untilInput = untilInputGroup.querySelector("input.date-field");
                let dateFrom = fromInput.value;

                // init dates on start
                if (dateFrom !== null && dateFrom !== undefined && dateFrom !== "") {
                    //rangeSelect.prop("checked", true).change();
                    rangeSelect.checked = true;
                    rangeSelect.dispatchEvent(new Event("change"));
                } else {
                    //dateSelect.prop("checked", true).change();
                    dateSelect.checked = true;
                    dateSelect.dispatchEvent(new Event("change"));
                }

                // clear dates before submit
                group.closest("form").onsubmit = () => {
                    if (dateSelect.checked) {
                        fromInput.value = null;
                        untilInput.value = null;
                    } else {
                        dateInput.value = null;
                    }
                    return true;
                };

                function onChangeDateSelect(e) {
                    let value = e.target.value;
                    if (value === "date") {
                        jp.editor.dates.range = false;
                        dateInputGroup.style.display = "table";
                        fromInputGroup.style.display = "none";
                        untilInputGroup.style.display = "none";
                    } else {
                        jp.editor.dates.range = true;
                        dateInputGroup.style.display = "none";
                        fromInputGroup.style.display = "table";
                        untilInputGroup.style.display = "table";
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
            let combineDate = function () {
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
                } else if(year.match(/[^-^\d]/g) != null) {
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
                dateInput.value = newDate.isValid() ? newDate.format(format) : null;
                dateInput.dispatchEvent(new Event("change"));
            };

            let addForm = function (placeHolder, maxlength, value) {
                forms[placeHolder] = Object.assign(document.createElement("input"), inputBase);
                forms[placeHolder].placeholder = placeHolder;
                forms[placeHolder].maxlength = maxlength;
                forms[placeHolder].title = "Eingabe nur als Zahl möglich!";
                forms[placeHolder].value = value !== undefined ? value :  "";
                forms[placeHolder].oninput = e => {
                    //wait 500ms for input then exec combineDate
                    let saveOnInput = e.target.oninput;
                    e.target.oninput = undefined;
                    setTimeout(() => {
                        combineDate();
                        e.target.oninput = saveOnInput;
                    }, 500);
                };
                forms[placeHolder].onblur = e => {
                    //wait 500ms for input then exec combineDate
                    let saveOnInput = e.target.oninput;
                    e.target.oninput = undefined;
                    setTimeout(() => {
                        getJournalDates(d => checkDatesForLogicalErrors(d, forms["Tag"]));
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

	function setDatesFromInput(input) {
        let dateInputNameSplit = input.name.split("@");

        if(dateInputNameSplit.length === 2 && dateInputNameSplit[1] !== ""){
            let dateType = dateInputNameSplit[1]
            let date = newDate(input.value);
            if(date !== undefined) {
                jp.editor.dates[dateType] = date;
            }
        }
    }

    document.querySelectorAll(".date-field")
        .forEach(input => {
            setDatesFromInput(input);
            input.onchange = e => setDatesFromInput(e.target);

            dateCombiner(input)
        })

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

        let saveButton = document.querySelector("input[name='_xed_submit_servlet:UpdateObjectServlet']");
        saveButton.disabled = true;

    }

    function killError(element) {
        element.parentNode.classList.remove("has-error");
        let errorBox = element.parentNode.querySelector(".jp-layout-errorBox");
        if(errorBox !== null && errorBox !== undefined){
            errorBox.remove();
        }

        let saveButton = document.querySelector("input[name^='_xed_submit_servlet:']");
        saveButton.disabled = false;
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
      searchInput.value = id;
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

(function() {
    jp.editor.init();
    jp.editor.articleGNDLocation();
})();