let ImprintEditor = function (objID, type) {
    let currentObjID = objID,
		currentType = type,
		i18nKeys =[];
	return {
		init: function () {
			loadDefaultButtons();

			$("#imprint-modal").modal("show");

			$('#imprint-modal').on('shown.bs.modal', function () {
				scrollToCurrentElm();
			});

			loadDefault();

			CKEDITOR.replace("imprint-editor", {
				resize_enabled: false,
				entities: false,
				enterMode: CKEDITOR.ENTER_BR,
				entities_processNumerical: 'force',
				tabSpaces: 4,
				fillEmptyBlocks: false,
				height: '265px',
                toolbar: [
                    { name: 'clipboard', items: [ 'Undo', 'Redo' ] },
                    { name: 'basicstyles', items: [ 'Bold', 'Italic', 'Underline' ] },
                    { name: 'styles', items: [ 'Format' ] },
                    { name: 'colors', items: [ 'TextColor', 'BGColor' ] },
                    { name: 'paragraph', items: [ 'NumberedList', 'BulletedList'] },
                    { name: 'links', items: [ 'Link', 'Unlink' ] },
                    { name: 'document', items: [ 'Source' ] }]
			});

			$("#imprintGUIMain").on("click", ".imprint-list-link", function () {
				if (!$(this).parent().hasClass("standard-elm")){
					$("#imprint-preview-edit").removeClass("hidden");
				}
				else {
					$("#imprint-preview-edit").addClass("hidden");
				}
				selectImprint($(this).parent());
			});

			$("#imprintGUIMain").on("click", "#imprint-new-btn", function () {
				showEditor(true);
			});

			$("#imprintGUIMain").on("click", "#imprint-delete-btn", function () {
				var imprintName = getImprintNameWithElm($(".imprint-list-elm.active"));
				$("#imprint-alert-delete-title").html(getI18nKey("jp.imprintEditor.delete", imprintName));
				$("#imprint-alert-delete-text").html(getI18nKey("jp.imprintEditor.fsType.delete", imprintName));
				$("#imprint-alert-delete").modal("show");
				$('#imprint-alert-delete').data('bs.modal').$backdrop.css('z-index','1060');
			});

			$("#imprintGUIMain").on("click", "#imprint-alert-delete-btn", function () {
				var imprintName = getImprintNameWithElm($(".imprint-list-elm.active"));
				deleteImprint(imprintName);
				$("#imprint-alert-delete").modal("hide");
			});

			$("#imprintGUIMain").on("click", "#imprint-preview-edit", function () {
				showEditor();
				$("#imprint-editor-panel").removeClass("new-imprint");
			});

			$("#imprintGUIMain").on("click", "#imprint-preview-cancel", function () {
				closeImprintEditor();
			});

			$("#imprintGUIMain").on("click", ".imprint-link-close", function () {
				closeImprintEditor();
			});

			$("#imprintGUIMain").on("click", "#imprint-editor-cancel", function () {
				hideEditor();
			});

			$("#imprintGUIMain").on("click", "#imprint-editor-save", function () {
				saveEditor();
			});

			$("#imprintGUIMain").on("click", "#imprint-preview-save", function () {
				savePreview();
			});

			//Fix: CKEDITOR URL input doesn't work when embedded in a bootstrap modal
			$.fn.modal.Constructor.prototype.enforceFocus = function() {};
		},

		open: function (objID, type) {
			currentObjID = objID;
			currentType = type;

			loadDefaultButtons();
			$("#imprint-modal").modal("show");
			loadDefault();
		}
	};

	function loadImprintList() {
		$.ajax({
			url: jp.baseURL + "rsc/fs/" + currentType +  "/list" + "?objID=" + currentObjID,
			type: "GET",
			dataType: "json",
			success: function(data) {
				if (data.length > 0) {
					$.each(data , function(i, elm){
						appendImprintToList(elm);
					});
					if (currentType != "link") {
						selectCurrentImprint(currentObjID);
					}
				}
				else {
					$("#imprint-no-imprint").removeClass("hidden");
					selectImprint($(".standard-elm"));
					$(".standard-elm").append("<i class='fa fa-2x fa-check current-imprint'></i>");
				}
			},
			error: function() {
				alert("Error while getting imprint list.");
			}
		});
	}

	function saveImprint(imprintName, imprintText) {
		$.ajax({
			url: jp.baseURL + "rsc/fs/" + currentType +  "/save",
			data: JSON.stringify({objID: currentObjID, imprintID: imprintName, content: imprintText}),
			contentType: 'application/json',
			dataType: "json",
			type: "POST",
			statusCode: {
				200: function () {
					appendImprintToList(imprintName);
					hideEditor();
					selectImprint(getImprintWithName(imprintName));
					scrollToCurrentElm();
				},
				500: function () {
					alert("Error while saving imprint.");
				}
			}
		});
	}

	function deleteImprint(imprintName) {
		$.ajax({
			url: jp.baseURL + "rsc/fs/" + currentType +  "/delete/" + imprintName  + "?objID=" + currentObjID,
			type: "DELETE",
			statusCode: {
				200: function () {
					var listElm = getImprintWithName(imprintName);
					listElm.remove();
					if ($(".current-imprint").length == 1) {
						selectImprint($(".current-imprint").parent());
					}
					else {
						if ($(".imprint-list-elm").length > 0) {
							selectImprint($(".imprint-list-elm").first());
						}
						else {
							$("#imprint-no-imprint").removeClass("hidden");
							$("#imprint-preview").html("");
						}
					}
				},
				500: function () {
					alert("Error while deleting imprint.");
				}
			}
		});
	}

	function removeImprintLink() {
		$.ajax({
			url: jp.baseURL + "rsc/fs/" + currentType +  "/removeLink/" + currentObjID,
			type: "DELETE",
			success: function(){
				location.reload();
			},
			error: function(){
				alert("Error while linking imprint.");
			}
		});
	}

	function editImprint(oldImprintName, newImprintName, oldImprintText, newImprintText) {
		$.ajax({
			url: jp.baseURL + "rsc/fs/" + currentType +  "/edit",
			data: JSON.stringify({objID: currentObjID, oldImprintID: oldImprintName, newImprintID: newImprintName,
				oldContent: oldImprintText, newContent: newImprintText}),
			contentType: 'application/json',
			dataType: "json",
			type: "POST",
			statusCode: {
				200: function () {
					getImprintWithName(oldImprintName).find(".imprint-list-link").html(newImprintName);
					hideEditor();
					if (currentType != "greeting") {
						selectImprint(getImprintWithName(newImprintName));
					}
					else {
						selectImprint(getImprintWithName("Begrüßung"));
					}
				},
				500: function () {
					alert("Error while editing imprint.");
				}
			}
		});
	}

	function linkImprint(docID, imprintName) {
		$.ajax({
			url: jp.baseURL + "rsc/fs/" + currentType +  "/set/?objID=" + docID + "&imprintID=" + imprintName,
			type: "POST",
			success: function(){
				location.reload();
			},
			error: function(){
				alert("Error while linking imprint.");
			}
		});
	}

	function selectCurrentImprint(docID) {
		$.ajax({
			url: jp.baseURL + "rsc/fs/" + currentType +  "/get/" + docID,
			type: "GET",
			statusCode: {
				200: function (data) {
					if (data == "") {
						$("#imprint-no-link").removeClass("hidden");
						selectImprint($(".standard-elm"));
						$(".standard-elm").append("<i class='fa fa-2x fa-check current-imprint'></i>");
					}
					else {
						if (currentType == "greeting") {
							data = "Begrüßung";
						}
						selectImprint(getImprintWithName(data));
						getImprintWithName(data).append("<i class='fa fa-2x fa-check current-imprint'></i>");
					}
				},
				500: function () {
					alert("Error while getting linked imprint.");
				}
			}
		});
	}

	function getImprint(imprintName) {
		$.ajax({
			url: jp.baseURL + "rsc/fs/" + currentType +  "/retrieve/" + imprintName + "?objID=" + currentObjID,
			type: "GET",
			success: function(data){
				$("#imprint-preview").html(data);
				if (data != "<div />" && data != "") {
					$("#imprint-no-imprint").addClass("hidden");
				}
				else {
					$("#imprint-no-imprint").removeClass("hidden");
				}
			},
			error: function(){
				alert("Error while getting imprint content.");
			}
		});
	}

	function appendImprintToList(imprint) {
		var link = $("<div class='imprint-list-link'>" + imprint + "</div>");
		var listElm = $("<div class='list-group-item imprint-list-elm'>");
		$(listElm).append(link);
		$("#imprintSelBox").append(listElm);
		return listElm;
	}

	function getImprintWithName(imprintName){
		return $(".imprint-list-elm").filter(function() {
			return ($(this).find(".imprint-list-link").html() == imprintName);
		});
	}

	function getImprintNameWithElm(elm) {
		var parent = $(elm).closest(".imprint-list-elm");
		return parent.find(".imprint-list-link").html();
	}

	function hideEditor() {
		hideEditorAlert();
		$("#imprint-editor-panel").removeClass("has-error");
		$("#imprint-preview-title").removeClass("hidden");
		$("#imprint-preview-panel").removeClass("hidden");
		if (currentType == "link") {
			$("#imprint-link-button").removeClass("hidden");
		}
		else {
			$("#imprint-preview-button").removeClass("hidden");
		}
		$("#imprint-new-title").addClass("hidden");
		$("#imprint-editor-title").addClass("hidden");
		$("#imprint-editor-panel").addClass("hidden");
		$("#imprint-editor-button").addClass("hidden");
		$("#imprint-no-imprint").addClass("hidden");
		$("#imprint-no-link").addClass("hidden");
		$("button.imprint-link-close").removeClass("hidden");
		if (currentType != "link" && ($("#imprint-preview").html() == "<div />" || $("#imprint-preview").html() == "")) {
			if($("#imprintSelBox").is(":empty")){
				$("#imprint-no-imprint").removeClass("hidden");
			} else {
				$("#imprint-no-link").removeClass("hidden");
			}
		}
		$("#imprint-editor-input").removeAttr("readonly");
	}

	function showEditor(newImprint) {
		if (newImprint == undefined || !newImprint) {
			CKEDITOR.instances['imprint-editor'].setData($("#imprint-preview").html());
			$("#imprint-editor-input").val(getImprintNameWithElm($(".imprint-list-elm.active")));
			$("#imprint-new-title").addClass("hidden");
			$("#imprint-editor-title").removeClass("hidden");
		}
		else {
			$("#imprint-new-title").removeClass("hidden");
			$("#imprint-editor-title").addClass("hidden");
			$("#imprint-editor-panel").addClass("new-imprint");
			CKEDITOR.instances['imprint-editor'].setData("");
			$("#imprint-editor-input").val("");
		}
		$("#imprint-preview-title").addClass("hidden");
		$("#imprint-preview-panel").addClass("hidden");
		$("#imprint-preview-button").addClass("hidden");
		$("#imprint-link-button").addClass("hidden");
		$("#imprint-editor-panel").removeClass("hidden");
		$("#imprint-editor-button").removeClass("hidden");
		$("button.imprint-link-close").addClass("hidden");
		if (currentType == "greeting") {
			$("#imprint-editor-input").attr("readonly", true);
		}
	}

	function selectImprint(imprintElm) {
		$("#imprint-no-link").addClass("hidden");
		$(".active").removeClass("active");
		$(imprintElm).addClass("active");
		if (!$(imprintElm).hasClass("standard-elm")) {
			if (currentType != "greeting") {
				getImprint($(imprintElm).find(".imprint-list-link").html());
				$("#imprint-delete-btn").removeAttr("disabled");
			}
			else {
				getImprint("intro");
			}
		}
		else {
			$("#imprint-preview-edit").addClass("hidden");
			if (currentType == "imprint" || (currentType == "greeting" && currentObjID == "index")) {
				getImprint("master");
			}
			else {
				$("#imprint-preview").html("");
			}

			if (currentType == "imprint" || currentType == "partner") {
				$("#imprint-delete-btn").attr("disabled", true);
			}
		}
	}

	function closeImprintEditor() {
		if (currentType == "link") {
			location.reload();
		}
		$("#imprintSelBox").html("");
		$("#imprint-preview").html("");
		$("#imprintGUIMain > .modal").modal("hide");
		$("#imprint-preview-edit").addClass("hidden");
		$("#imprint-no-link").addClass("hidden");
		$("#imprint-no-imprint").addClass("hidden");
		$("#imprint-link-button").addClass("hidden");
		$("#imprint-preview-button").removeClass("hidden");
	}

	function saveEditor() {
		hideEditorAlert();
		var imprintName = $("#imprint-editor-input").val();
		var imprintText = CKEDITOR.instances['imprint-editor'].getData();
		if (imprintName == "") {
			showEditorAlert("name");
			return;
		}
		if (imprintText == "") {
			showEditorAlert("content");
			return;
		}
		if ($("#imprint-editor-panel").hasClass("new-imprint")) {
			if (getImprintWithName(imprintName).length < 1) {
				if (currentType != "greeting") {
					saveImprint(imprintName, imprintText);
				}
				else {
					saveImprint("intro", imprintText);
				}
			}
			else {
				showEditorAlert("name");
			}
		}
		else {
			var oldImprintName = getImprintNameWithElm($(".imprint-list-elm.active"));
			if (getImprintWithName(imprintName).length < 1 || imprintName == oldImprintName) {
				if (currentType != "greeting") {
					editImprint(oldImprintName, imprintName, $("#imprint-preview").html(), imprintText);
				}
				else {
					editImprint("intro", "intro", $("#imprint-preview").html(), imprintText);
				}
			}
			else {
				showEditorAlert("name");
			}
		}
	}

	function showEditorAlert(type) {
		if (type == "name") {
			$("#imprint-editor-panel").addClass("has-error");
		}
		if (type == "content") {
			$("#imprint-editor-wrapper").addClass("imprint-alert");
		}
		$("#imprint-editor-error").removeClass("hidden");
	}

	function hideEditorAlert() {
		$("#imprint-editor-panel").removeClass("has-error");
		$("#imprint-editor-wrapper").removeClass("imprint-alert");
		$("#imprint-editor-error").addClass("hidden");
	}

	function scrollToCurrentElm() {
		if ($(".imprint-list-elm.active").length == 1){
			$("#imprintSelBox").animate({
				scrollTop: $('#imprintSelBox > .active').index() * $('#imprintSelBox > .active').outerHeight()
			}, 1);
			if (!$(".imprint-list-elm.active").hasClass("standard-elm")){
				$("#imprint-preview-edit").removeClass("hidden");
			}
		}
	}

	function loadI18nKeys() {
		jQuery.getJSON(jp.baseURL + "rsc/locale/translate/" + jp.lang + "/jp.imprintEditor.*", function(data) {
			i18nKeys = data;
			updateI18n($("body"));
		});
	}

	function updateI18n(elm) {
		$(elm).find(".i18n").each(function(i, node) {
			var key = $(node).attr("i18n");
			key = key.replace("fsType", currentType);
			var i18nKey = i18nKeys[key];
			if (i18nKey != undefined){
				$(node).html(i18nKey);
			}
			else{
				$(node).html($(node).attr("i18n-def"));
			}
		});
	}

	function getI18nKey(key) {
		key = key.replace("fsType", currentType);
		var string = i18nKeys[key];
		if (string != undefined){
			for (var i = 0; i < arguments.length-1; i++){
				string = string.replace(new RegExp('\\{' + i + '\\}', "g"), arguments[i+1]);
			}
			return string;
		}
		else{
			return "";
		}
	}
	
	function loadDefaultButtons() {
		if (currentType == "imprint") {
			$("#imprintSelBox").append('<div class="list-group-item imprint-list-elm active imprint-standard standard-elm"><div class="imprint-list-link">standart Impressum</div></div>');
		}

		if (currentType == "partner") {
			$("#imprintSelBox").append('<div class="list-group-item imprint-list-elm active partner-standard standard-elm"><div class="imprint-list-link">kein Partner</div></div>');
		}

		if (currentType == "greeting" && $(".greeting-standard").length < 1) {
			if (currentObjID == "index") {
				$("#imprintSelBox").append('<div class="list-group-item imprint-list-elm active greeting-standard standard-elm"><div class="imprint-list-link">standart Begrüßung</div></div>');
			}
			else {
				$("#imprintSelBox").append('<div class="list-group-item imprint-list-elm active greeting-standard standard-elm"><div class="imprint-list-link">keine Begrüßung</div></div>');
			}
			$("#imprintSelBox").append('<div class="list-group-item imprint-list-elm active"><div class="imprint-list-link">Begrüßung</div></div>');
			$("#imprint-new-btn").parent().addClass("hidden");
		}
		else {
			$("#imprint-new-btn").parent().removeClass("hidden");
		}
	}

	function loadDefault() {
		loadI18nKeys();
		if (currentType != "greeting") {
			loadImprintList();
		}
		else {
			selectCurrentImprint(currentObjID);
		}
		if (currentType == "link") {
			$("#imprint-preview-button").addClass("hidden");
			$("#imprint-link-button").removeClass("hidden");
		}
	}

	function savePreview() {
		if (!$(".imprint-list-elm.active").hasClass("standard-elm")) {
			if (currentType != "greeting") {
				linkImprint(currentObjID, getImprintNameWithElm($(".imprint-list-elm.active")));
			}
			else {
				linkImprint(currentObjID, "intro");
			}

		}
		else {
			removeImprintLink();
		}
	}

};

$(document).ready(function() {
	var imprintEditorInstance;
	$("body").on("click", ".jp-infoFiles-button", function () {
		var objID = $(this).attr("journalid");
		var type = $(this).attr("type");
		if ($("#imprintGUIMain").length < 1){
			$.ajax({
				url: jp.baseURL + "html/jp-imprint.html",
				type: "GET",
				dataType: "html",
				statusCode: {
					200: function(data) {
						var html = $("<div></div>").append(data).find("#imprintGUIMain");
						$("head").append('<link href="'+ jp.baseURL + 'css/jp-imprint.css" rel="stylesheet" type="text/css">');
						$("body").append(html);
						imprintEditorInstance = new ImprintEditor(objID, type);
						imprintEditorInstance.init();
					},
					500: function(error) {
						alert(error);
					}
				}
			});
		}
		else {
			imprintEditorInstance.open(objID, type);
		}
	});
});