$(document).ready(function() {			
	createDate();
});

function createDate() {
	var formSelector = $("#dateSelect");
	var isArticle = $("#currentType").text() == 'jparticle';

	if ($("#publshDate").val() != '')
		formSelector.val("published");
	if (isArticle)
		if ($("#publshOriginalDate").val() != '')
			formSelector.val("published_original");
	if ($("#fromDate").val() != '' || $("#untilDate").val() != '')
		formSelector.val("published_from");

	var lastValue = formSelector.val();

	var appear = function() {
		lastValue = formSelector.val();
		if (formSelector.val() == 'published_from') {
			$("#dateContainer").hide();
			if (isArticle)
				$("#dateOriginalContainer").hide();
			$("#fromDateContainer").show();
			$("#untilDateContainer").show();
		} else {
			if (formSelector.val() == "published_original") {
				if (isArticle)
					$("#dateOriginalContainer").show();
				$("#dateContainer").hide();
			} else {
				$("#dateContainer").show();
				if (isArticle)
					$("#dateOriginalContainer").hide();
			}
			$("#fromDateContainer").hide();
			$("#untilDateContainer").hide();
		}
	};

	var deleteVal = function() {
		if (formSelector.val() == 'published_from') {
			$("#publshDate").val(null);
			if (isArticle)
				$("#publshOriginalDate").val(null);
		} else {
			if (formSelector.val() == "published_original")
				$("#publshDate").val(null);
			else if (isArticle)
				$("#publshOriginalDate").val(null);
			$("#fromDate").val(null);
			$("#untilDate").val(null);
		}
	};

	formSelector.change(function() {
		appear();
		deleteVal();
	}).keyup(function() {
		if (lastValue != formSelector.val()) {
			appear();
			deleteVal();
		}
	});

	appear();
	$("#dateSeperator").off();
};

$.fn.dateCombiner = function() {
	var elements = this;

	elements.each(function(index, dateInput) {

				var dateInputJq = jQuery(dateInput);

/* original input hidden */
				dateInputJq.css({
					"display" : "none"
				});

				var formsData = {
					"Tag" : "dd",
					"Monat" : "MM",
					"Jahr" : "yyyy"
				};

				var forms = {};

				var inputBase = '<input class="form-control" style="width: 33.3%;">';

				var parent = dateInputJq.parent();

/* settings for datetimepicker */
				parent.datetimepicker({
					language : $("#hiddenLanguage").text(),
					pickTime : false,
					minDate : '0001-01-01',
					startDate: '+0d',
//					format: 'YYYY-MM-DD',
//					forceParse: false 
				}).on("dp.hide", function() {
					dateInputJq.val(dateInputJq.val().replace(/\./g, "-"));
					var token = dateInputJq.val().split("-");
					forms["Jahr"].val(token[2]);
					forms["Monat"].val(token[1]);
					forms["Tag"].val(token[0]);
					if(forms["Jahr"].val().length < forms["Tag"].val().length) {
						forms["Jahr"].val(token[0]);
						forms["Tag"].val(token[2]);
					}
					combineDate();
				});

/* 
 * error if yyyy-_-dd or _-mm-dd
 * error by letters, day 01 - 31, month 01 - 12 
 * save data on original input 
 * can add missed 0  ---> year = 2  now complete to  year = 0002  automatically on original input
 * 
 * */
				var combineDate = function() {
					if(forms["Jahr"].val() != "") {
						dateInputJq.val("");
						if(forms["Jahr"].val().match(/[^0-9]/g) != null) {
							errorOutput("Bitte Achten Sie darauf das sie nur Zahlen eingeben. Bsp: 1990, 1500, 0100, 100, 10", forms["Tag"]);
						} else {
							if(forms["Jahr"].val().length < 4) {
								for(var i = forms["Jahr"].val().length; i < 4; i++){
									dateInputJq.val(dateInputJq.val() + "0");
								}
							}
							dateInputJq.val(dateInputJq.val() + forms["Jahr"].val());
							killError(forms["Jahr"]);
							
							if(forms["Monat"].val() != "") {
								// check right month input
								if(forms["Monat"].val() > "12" || forms["Monat"].val() < "01") {
									errorOutput("Es gibt max. 12 Monate im Jahr, min. 01 Monat. Achten Sie bitte auch darauf, dass die Eingabe als Zahl erfolgt. Bsp: 01, 02, 03, 08, 11", forms["Tag"]);
								} else {
									var extraNul = "";
									if(forms["Monat"].val().length < 2) extraNul = "0";
									dateInputJq.val(dateInputJq.val() + "-" + extraNul + forms["Monat"].val());
									
									if(forms["Tag"].val() != "") {
										// check right day input
										if(forms["Tag"].val() > "31" || forms["Tag"].val() < "01") {
											errorOutput("Ein Monat hat max. 31 Tage und min 01 Tag. Achten Sie bitte auch darauf, dass die Eingabe als Zahl erfolgt. Bsp: 01, 02, 08, 15, 20, 25", forms["Tag"]);
										} else {
											extraNul = "";
											if(forms["Tag"].val().length < 2) extraNul = "0";
											dateInputJq.val(dateInputJq.val() + "-" + extraNul + forms["Tag"].val());
										}
									}
								}
							} else {
								if(forms["Tag"].val() != "") {
									errorOutput("Die Monats Eingabe fehlt!", forms["Tag"]);
								}
							}
						}
					} else {
						if(forms["Monat"].val() != "" || forms["Tag"].val() != "") {
							errorOutput("Die Jahres Eingabe fehlt!", forms["Tag"]);
						} else {
							killError(forms["Monat"]);
						}
						dateInputJq.val("");
					}
				};
				
				var setDateTimerPickerVal = function() {
					combineDate();
					//set Date on picker only if jahr and mounth and day not null
					if(forms["Jahr"].val() != "" && forms["Monat"].val() != "" && forms["Tag"].val() != "") {
						parent.data("DateTimePicker").setDate(forms["Tag"].val() + "." + forms["Monat"].val() + "." + forms["Jahr"].val());
					}
				};

				
				$("#dateSelect").on("change keyup", function() {
					forms["Jahr"].val(null);
					forms["Monat"].val(null);
					forms["Tag"].val(null);
					killError(forms["Jahr"]);
				});

				var content = dateInputJq.val().split("-");
				var zaehler = 2; 
				/* settings for the input */
				for ( var placeHolder in formsData) {
					var maxlength = formsData[placeHolder].length;
					forms[placeHolder] = jQuery(inputBase);
					forms[placeHolder].attr("placeholder", placeHolder);
					forms[placeHolder].attr("maxlength", maxlength);
					forms[placeHolder].attr("title", "Eingabe nur als Zahl möglich!");
					forms[placeHolder].val(content[zaehler]);
//					forms[placeHolder].on("keyup change click", combineDate);
					forms[placeHolder].on("keyup click", combineDate);
					forms[placeHolder].on("change", setDateTimerPickerVal);
					forms[placeHolder].insertAfter(dateInputJq);
					zaehler --; 
				}
			});
};

$(".date-field").dateCombiner();

function errorOutput(errorText, element) {
	if(element.parent().find(".jp-layout-errorBox").length == 0) {
		element.parent().addClass("has-error");
		$('<div role="alert" class="jp-layout-errorBox alert alert-danger">' + errorText + '</div>').insertAfter(element);
	}
}

function killError(element){
	element.parent().removeClass("has-error");
	element.parent().find(".jp-layout-errorBox").remove();
}
