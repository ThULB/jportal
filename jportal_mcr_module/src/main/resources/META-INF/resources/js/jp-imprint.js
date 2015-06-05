var ImprintEditor = function (objID, type) {
	var currentObjID = objID,
		currentType = type,
		i18nKeys =[];
	return {
		init: function () {
			$("#imprint-modal").modal("show");

			$('#imprint-modal').on('shown.bs.modal', function () {
				scrollToCurrentElm();
			});

			//noinspection JSUnresolvedVariable
			loadI18nKeys(currentLang);
			loadImprintList();

			CKEDITOR.replace("imprint-editor", {
				resize_enabled: false,
				entities: false,
				enterMode: CKEDITOR.ENTER_BR,
				entities_processNumerical: 'force',
				tabSpaces: 4,
				fillEmptyBlocks: false,
				height: '265px',
				toolbar : [ ['Undo', 'Redo', '-', 'Bold', 'Italic', '-', 'NumberedList', 'BulletedList', '-',
					'Link', 'Unlink', 'Source' ] ]
			});

			$("#imprintGUIMain").on("click", ".imprint-list-link", function () {
				$("#imprint-preview-edit").removeClass("hidden");
				selectImprint($(this).html());
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

			$("#imprintGUIMain").on("click", "#imprint-editor-cancel", function () {
				hideEditor();
			});

			$("#imprintGUIMain").on("click", "#imprint-editor-save", function () {
				saveEditor();
			});

			$("#imprintGUIMain").on("click", "#imprint-preview-save", function () {
				linkImprint(currentObjID, getImprintNameWithElm($(".imprint-list-elm.active")));
			});
		},

		open: function (objID, type) {
			currentObjID = objID;
			currentType = type;
			//noinspection JSUnresolvedVariable
			loadI18nKeys(currentLang);
			loadImprintList();
			$("#imprint-modal").modal("show");
		}
	};

	function loadImprintList() {
		$.ajax({
			url: jp.baseURL + "rsc/fs/" + currentType +  "/list",
			type: "GET",
			dataType: "json",
			success: function(data) {
				if (data.length > 0) {
					$.each(data , function(i, elm){
						appendImprintToList(elm);
					});
					selectCurrentImprint(currentObjID);
				}
				else {
					$("#imprint-no-imprint").removeClass("hidden");
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
			data: JSON.stringify({imprintID: imprintName, content: imprintText}),
			contentType: 'application/json',
			dataType: "json",
			type: "POST",
			statusCode: {
				200: function () {
					appendImprintToList(imprintName);
					hideEditor();
					selectImprint(imprintName);
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
			url: jp.baseURL + "rsc/fs/" + currentType +  "/delete/" + imprintName,
			type: "DELETE",
			statusCode: {
				200: function () {
					var listElm = getImprintWithName(imprintName);
					listElm.remove();
					if ($(".current-imprint").length == 1) {
						selectImprint(getImprintNameWithElm($(".current-imprint")));
					}
					else {
						if ($(".imprint-list-elm").length > 0) {
							selectImprint(getImprintNameWithElm($(".imprint-list-elm").first()));
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

	function editImprint(oldImprintName, newImprintName, oldImprintText, newImprintText) {
		$.ajax({
			url: jp.baseURL + "rsc/fs/" + currentType +  "/edit",
			data: JSON.stringify({oldImprintID: oldImprintName, newImprintID: newImprintName,
				oldContent: oldImprintText, newContent: newImprintText}),
			contentType: 'application/json',
			dataType: "json",
			type: "POST",
			statusCode: {
				200: function () {
					getImprintWithName(oldImprintName).find(".imprint-list-link").html(newImprintName);
					hideEditor();
					selectImprint(newImprintName);
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
				closeImprintEditor();
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
					}
					else {
						selectImprint(data);
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
			url: jp.baseURL + "rsc/fs/" + currentType +  "/retrieve/" + imprintName,
			type: "GET",
			success: function(data){
				$("#imprint-preview").html(data);
				if (data != "<div />") {
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
		$("#imprint-preview-button").removeClass("hidden");
		$("#imprint-new-title").addClass("hidden");
		$("#imprint-editor-title").addClass("hidden");
		$("#imprint-editor-panel").addClass("hidden");
		$("#imprint-editor-button").addClass("hidden");
		$("#imprint-no-imprint").addClass("hidden");
		$("#imprint-no-link").addClass("hidden");
		if ($("#imprint-preview").html() == "<div />" || $("#imprint-preview").html() == "") {
			if($("#imprintSelBox").is(":empty")){
				$("#imprint-no-imprint").removeClass("hidden");
			} else {
				$("#imprint-no-link").removeClass("hidden");
			}
		}
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
		$("#imprint-editor-panel").removeClass("hidden");
		$("#imprint-editor-button").removeClass("hidden");
	}

	function selectImprint(imprintName) {
		$("#imprint-no-link").addClass("hidden");
		var elm = getImprintWithName(imprintName);
		$(".active").removeClass("active");
		$(elm).addClass("active");
		getImprint(imprintName);
	}

	function closeImprintEditor() {
		$("#imprintSelBox").html("");
		$("#imprint-preview").html("");
		$("#imprintGUIMain > .modal").modal("hide");
		$("#imprint-preview-edit").addClass("hidden");
		$("#imprint-no-link").addClass("hidden");
		$("#imprint-no-imprint").addClass("hidden");
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
				saveImprint(imprintName, imprintText);
			}
			else {
				showEditorAlert("name");
			}
		}
		else {
			var oldImprintName = getImprintNameWithElm($(".imprint-list-elm.active"));
			if (getImprintWithName(imprintName).length < 1 || imprintName == oldImprintName) {
				editImprint(oldImprintName, imprintName, $("#imprint-preview").html(), imprintText);
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
			$("#imprint-preview-edit").removeClass("hidden");
		}
	}

	function loadI18nKeys(lang) {
		jQuery.getJSON(jp.baseURL + "servlets/MCRLocaleServlet/" + lang + "/jp.imprintEditor.*", function(data) {
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