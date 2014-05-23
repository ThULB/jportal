$(document).ready(function() {

	var objectID = null;
	var objectTitle = null;
	var deletable = false;
	var referer = null;
	var parent = null;

	var dialog = null;
	var title = null;
	var body = null;
	var footer = null;
	var imageDiv = null;
	var infoDiv = null;
	var submitButton = null;
	var closeButton = null;

	$("#deleteDocButton").on("click", function() {
		if(dialog == null) {
			setup();
		}
		render();
		$('#delete-dialog').modal();
	});

	function setup() {
		dialog = $("#delete-dialog");
		title = $("#delete-dialog-title");
		body = $("#delete-dialog-body");
		footer = $("#delete-dialog-footer");
		imageDiv = $("#delete-dialog-image");
		infoDiv = $("#delete-dialog-info");
		submitButton = $("#delete-dialog-submit");
		submitButton.on("click", onSubmitButtonClicked);
		closeButton = $("#delete-dialog-close");

		objectID = dialog.attr("data-id");
		objectTitle = dialog.attr("data-title");
		deletable = dialog.attr("data-deletable") === "true";
		referer = dialog.attr("data-referer");
		parent = dialog.attr("data-parent");
	}

	function render() {
		title.text("Dokument löschen");
		dialog.removeClass("text-danger");

		if(deletable) {
			imageDiv.html("<i class='fa fa-warning fa-5x'></i>");
			infoDiv.html("Sind Sie sicher, daß Sie <b>" + objectTitle + "</b> löschen wollen?");
			submitButton.removeClass("hidden");
			closeButton.text("Abbrechen");
		} else {
			imageDiv.html("<i class='fa fa-ban fa-5x'></i>");
			infoDiv.html("Dieses Dokument enthält Digitalisate. Löschen nicht möglich!");
		}
	}

	function onSubmitButtonClicked() {
		closeButton.text("Schließen");
		submitButton.addClass("hidden");

		imageDiv.html("<i class='fa fa-circle-o-notch fa-spin fa-5x'></i>");
		infoDiv.html("Dokument wird gelöscht. Bitte warten...");

		$.ajax({
			url: "/rsc/object/" + objectID,
			type: "delete",
			dataType: 'text'
		}).done(function() {
			imageDiv.html("<i class='fa fa-check fa-5x'></i>");
			infoDiv.html("Löschen erfolgreich!");
			closeButton.on("click", onCloseClicked);
		}).error(function(error, xhr) {
			title.text("Es ist ein Fehler beim Löschen aufgetreten");
			dialog.addClass("text-danger");
			imageDiv.html("<i class='fa fa-exclamation fa-5x'></i>");
			if(error.status == 404) {
				infoDiv.html("<p class='text-danger'>Das Dokument wurde bereits gelöscht.</p>");
			} else if(error.status == 401) {
				infoDiv.html("<p class='text-danger'>Sie haben nicht die notwendigen Rechte das Dokument zu löschen.</p>");
			} else if(error.status == 403 && error.responseText != null && error.responseText != "") {
				infoDiv.html("<p class='text-danger'>" + error.responseText + "</p>");
			} else {
				infoDiv.html("<p class='text-danger'>Das Dokument mit der ID <b>" + objectID + "</b> konnte nicht gelöscht werden." +
						" Bitte wenden Sie sich an den Administrator!</p>" +
						"<p>" + error.status + ": " + error.statusText + "</p><p>" + error.responseText + "</p>");
			}
		});
	}

	function onCloseClicked() {
		var gotoURL = null;
		if(referer != null && referer != "") {
			gotoURL = referer;
		} else if(parent != null && parent != "") {
			gotoURL = jp.baseURL + "receive/" + parent;
		} else {
			gotoURL = jp.baseURL;
		}
		window.location = gotoURL;
	}

});