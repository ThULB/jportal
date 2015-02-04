$(document)
		.ready(
				function() {
					document.title = $("#xeditor-title").text().trim() + " "
							+ document.title;
					
					if ($("#currentType").text() == 'jparticle'
							|| $("#currentType").text() == 'jpjournal'
							|| $("#currentType").text() == 'jpvolume') {
						createDate();
					}
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
			$("#dateContainer").attr("style", "display:none");
			if (isArticle)
				$("#dateOriginalContainer").attr("style", "display:none");
			$("#fromDateContainer").removeAttr("style");
			$("#untilDateContainer").removeAttr("style");
		} else {
			if (formSelector.val() == "published_original") {
				if (isArticle)
					$("#dateOriginalContainer").removeAttr("style");
				$("#dateContainer").attr("style", "display:none");
			} else {
				$("#dateContainer").removeAttr("style");
				if (isArticle)
					$("#dateOriginalContainer").attr("style", "display:none");
			}
			$("#fromDateContainer").attr("style", "display:none");
			$("#untilDateContainer").attr("style", "display:none");
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

/* mark the div if yyyy-_-dd or _-mm-dd; save data on original input 
 * add some 0  ---> year = 2   now  year = 0002 
 * */
				var combineDate = function() {
					if(forms["Jahr"].val() != "") {
						dateInputJq.val("");
						if(forms["Jahr"].val().length < 4) {
							for(var i = forms["Jahr"].val().length; i < 4; i++){
								dateInputJq.val(dateInputJq.val() + "0");
							}
						}
						dateInputJq.val(dateInputJq.val() + forms["Jahr"].val());
						parent.removeClass("has-error");
						if(forms["Monat"].val() != "") {
							var extraNul = "";
							if(forms["Monat"].val().length < 2) extraNul = "0";
							dateInputJq.val(dateInputJq.val() + "-" + extraNul + forms["Monat"].val());
							if(forms["Tag"].val() != "") {
								extraNul = "";
								if(forms["Tag"].val().length < 2) extraNul = "0";
								dateInputJq.val(dateInputJq.val() + "-" + extraNul + forms["Tag"].val());
							}
						} else {
							if(forms["Tag"].val() != "") {
								parent.addClass("has-error");
							}
						}
					} else {
						if(forms["Monat"].val() != "" || forms["Tag"].val() != "") {
							parent.addClass("has-error");
						} else {
							parent.removeClass("has-error");
						}
						dateInputJq.val("");
					}
				};
				
				$("#dateSelect").on("change keyup", function() {
					forms["Jahr"].val(null);
					forms["Monat"].val(null);
					forms["Tag"].val(null);
				});

				var content = dateInputJq.val().split("-");
				var zaehler = 2; 
/* settings for the input */
				for ( var placeHolder in formsData) {
					var maxlength = formsData[placeHolder].length;
					forms[placeHolder] = jQuery(inputBase);
					forms[placeHolder].attr("placeholder", placeHolder);
					forms[placeHolder].attr("maxlength", maxlength);
					forms[placeHolder].val(content[zaehler]);
					forms[placeHolder].on("keyup change click", combineDate);
					forms[placeHolder].insertAfter(dateInputJq);
					zaehler --; 
				}
			});
};

$(".date-field").dateCombiner();
