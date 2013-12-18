function introEditor(journalID) {
	var createdElem = null;

	var ckEditorMainButtonCtr = function(tmpElem) {
		return function() {
			$('#ckeditorButton').hide();
			var introFrame = $('#intro');
			introFrame
					.ckeditor({
						resize_enabled : false,
						entities : false,
						enterMode : CKEDITOR.ENTER_BR,
						entities_processNumerical : 'force',
						tabSpaces : 4,
						fillEmptyBlocks : false,
						height : '500px',
						toolbar : [ [ 'Undo', 'Redo', '-', 'Bold', 'Italic', '-', 'NumberedList', 'BulletedList', '-', 'Link', 'Unlink',
								'Source' ] ]
					});

			function cancelNoSave() {
				introFrame.ckeditorGet().destroy(true);
				tmpElem.remove();
				$('#ckeditorButton').show();
			}

			function saveContent() {
				var editor = introFrame.ckeditorGet();
				var editorData = editor.getData();
				$.ajax({
					url : '/rsc/journalFile/' + journalID + '/intro.xml',
					type : 'POST',
					data : editorData,
					contentType : 'application/xhtml+xml'
				});
				editor.destroy();
				$('#ckeditorButton').show();
			}

			$('#ckeditorSaveButton').click(function() {
				saveContent();
				$('#ckeditorButtons').remove();
			})

			$('#ckeditorCancelButton').click(function() {
				var cancelMsgButtonCtr = function() {
					$('#ckEditorCancelNoSave').click(function() {
						$('#ckEditorCancelMsgContainer').parent().remove();
						cancelNoSave();
					});

					$('#ckEditorCancelSave').click(function() {
						saveContent();
						$('#ckEditorCancelMsgContainer').parent().remove();
					});
				}

				$('#ckeditorButtons').remove();
				if (introFrame.ckeditorGet().checkDirty()) {
					$('<div/>').load('/ckeditor/GUI.html #ckEditorCancelMsgContainer', cancelMsgButtonCtr).appendTo('#main');
				} else {
					cancelNoSave();
				}

			})
		}
	}

	if ($('#intro').length) {
		var tmpElem = $('<div id="#ckEditorTmp"/>')
		tmpElem.load('/ckeditor/GUI.html #ckeditorButtons', ckEditorMainButtonCtr(tmpElem)).insertAfter('#intro');
	} else {
		var Lcolum = $('#jp-content-LColumn>ul');
		if (Lcolum.length == 0) {
			var tmpElem = $('<div id="#ckEditorTmp"/>')
			tmpElem.load('/ckeditor/GUI.html #jp-content-LColumn', ckEditorMainButtonCtr(tmpElem)).insertAfter('#jp-maintitle');
		} else {
			var tmpElem = $('<li id="#ckEditorTmp"/>')
			tmpElem.load('/ckeditor/GUI.html #jp-content-LColumn-List .ckGUI', ckEditorMainButtonCtr(tmpElem)).appendTo(Lcolum);
		}
	}
}

function showDeleteDerivateDialog(/* String */id) {
	if (!confirm('Das Derivat wirklich löschen?')) {
		return;
	}
	jQuery.ajax({
		type : 'DELETE',
		url : '/rsc/object/' + id
	}).done(function(msg) {
		location.reload(true);
	}).fail(function(error) {
		if (error.status == 400) {
			alert('Bad request: ' + error.responseText);
		} else if (error.status == 401) {
			alert('Unauthorized: You have no permission to delete this object!');
		} else if (error.status == 403) {
			alert('Forbidden: ' + error.responseText);
		} else if (error.status == 404) {
			alert('Unknown MyCoRe object id ' + id);
		}
	});
}

function mergeDerivates(/* String */objID) {
	if (!confirm('Derivate wirklich zusammenführen?')) {
		return;
	}
	jQuery.ajax({
		type : 'POST',
		url : '/rsc/obj/' + objID + '/mergeDeriv'
	}).done(function(msg) {
		location.reload(true);
	}).fail(function(error) {
		if (error.status == 400) {
			alert('Bad request: ' + error.responseText);
		} else if (error.status == 401) {
			alert('Unauthorized: You have no permission to merge this object!');
		} else if (error.status == 403) {
			alert('Forbidden: ' + error.responseText);
		} else if (error.status == 404) {
			alert('Unknown MyCoRe object id ' + id);
		}
	});
}

$(document).ready(function() {
	function supportHTML5() {
		return !!document.createElement('canvas').getContext;
	}

	if (!supportHTML5()) {
		var searchInput = $('#searchForm #inputField');
		var placeholderTxt = searchInput.attr('placeholder');
		searchInput.attr('value', placeholderTxt);
		searchInput.focus(function() {
			searchInput.removeAttr('value');
		});

		var inputChanged = false;
		searchInput.change(function() {
			inputChanged = true;
		})

		searchInput.blur(function() {
			var currentInputVal = searchInput.attr('value');
			if (!inputChanged || $.trim(currentInputVal) == '') {
				searchInput.attr('value', placeholderTxt);
			}
		})
	}
});

function truncate(/* String */string, /* int */maxCharacters) {
	return string.length > maxCharacters ? jQuery.trim(string).substring(0, maxCharacters).split(" ").slice(0, -1).join(" ") + "..."
			: string;
}

function updateQueryStringParameter(uri, key, value) {
	var re = new RegExp("([?|&])" + key + "=.*?(&|$)", "i");
	separator = uri.indexOf('?') !== -1 ? "&" : "?";
	if (uri.match(re)) {
		return uri.replace(re, '$1' + key + "=" + value + '$2');
	} else {
		return uri + separator + key + "=" + value;
	}
}

$(document).ready(function() {
	$("#sortSelect").on("change", function() {
		// get url
		var url = window.location.href;
		// change/add sort parameter
		var newLocation = updateQueryStringParameter(url, "sort", this.value);
		// go to
		window.location = newLocation;
	});
});