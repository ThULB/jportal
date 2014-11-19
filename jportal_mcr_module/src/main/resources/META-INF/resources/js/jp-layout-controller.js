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
					url : jp.baseURL + 'rsc/journalFile/' + journalID + '/intro.xml',
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
					$('<div/>').load(jp.baseURL + 'ckeditor/GUI.html #ckEditorCancelMsgContainer', cancelMsgButtonCtr).appendTo('#main');
				} else {
					cancelNoSave();
				}

			})
		}
	}

	if ($('#intro').length) {
		var tmpElem = $('<div id="#ckEditorTmp"/>')
		tmpElem.load(jp.baseURL + 'ckeditor/GUI.html #ckeditorButtons', ckEditorMainButtonCtr(tmpElem)).insertAfter('#intro');
	} else {
		var Lcolum = $('#jp-content-LColumn>ul');
		if (Lcolum.length == 0) {
			var tmpElem = $('<div id="#ckEditorTmp"/>')
			tmpElem.load(jp.baseURL + 'ckeditor/GUI.html #jp-content-LColumn', ckEditorMainButtonCtr(tmpElem)).insertAfter('#jp-maintitle');
		} else {
			var tmpElem = $('<li id="#ckEditorTmp"/>')
			tmpElem.load(jp.baseURL + 'ckeditor/GUI.html #jp-content-LColumn-List .ckGUI', ckEditorMainButtonCtr(tmpElem)).appendTo(Lcolum);
		}
	}
}

function showDeleteDerivateDialog(/* String */id) {
	if (!confirm('Das Derivat wirklich löschen?')) {
		return;
	}
	jQuery.ajax({
		type : 'DELETE',
		url : jp.baseURL + 'rsc/object/' + id
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

// SEARCHBAR
function updateSearchbar() {
	var searchForm = $("#searchForm");
	var searchField = $("#inputField");
	var searchDropDownButton = $("#searchDropDownButton");
	var journalSearchOption = $("#journalSearchOption");
	var globalSearchOption = $("#globalSearchOption");
	var journalSearchLabel = $("#journalSearchLabel").text();
	var globalSearchLabel = $("#globalSearchLabel").text();

	journalSearchOption.on("click", activateJournalSearch);
	globalSearchOption.on("click", activateGlobalSearch);

	journalSearchOption.append(journalSearchLabel);
	globalSearchOption.append(globalSearchLabel);

	if(jp.journalID != null) {
		activateJournalSearch();
	} else {
		activateGlobalSearch();
	}

	function activateGlobalSearch() {
		searchField.attr("placeholder", globalSearchLabel);
		searchField.attr("title", globalSearchLabel);
		setDrowDownIcon("fa-globe");
		removeHiddenFields();
	}

	function activateJournalSearch() {
		searchField.attr("placeholder", journalSearchLabel);
		searchField.attr("title", journalSearchLabel);
		setDrowDownIcon("fa-book");
		removeHiddenFields();
		appendHiddenFields();
	}

	function removeHiddenFields() {
		$("#searchForm input[name='fq']").remove();
		$("#searchForm input[name='journalID']").remove();
	}

	function appendHiddenFields() {
		searchForm.append("<input type='hidden' name='fq' value='journalID:" + jp.journalID + "' />");
		searchForm.append("<input type='hidden' name='journalID' value='" + jp.journalID + "' />");
	}

	function setDrowDownIcon(iconClass) {
		$("#searchDropDownButton i").remove();
		searchDropDownButton.prepend("<i class='fa fa-fw " + iconClass + "'></i>");
	}

}

// IVIEW 2
$(document).ready(function() {
	$('div.jp-layout-derivateWrapper .image').on({
		mouseenter: function() {
			jQuery(this).find('div.jp-layout-hidden-Button').show();
		},
		mouseleave: function() {
			jQuery(this).find('div.jp-layout-hidden-Button').hide();
		}
	});
});

// SORT
$(document).ready(function() {
	$(".sortSelect").on("change", function() {
		// get url
		var url = window.location.href;
		// change/add sort parameter
		var newLocation = updateQueryStringParameter(url, "sort", this.value);
		// go to
		window.location = newLocation;
	});
});

// PIWIK
function trackPageView(piwikURL, journalID, pageID) {
	window._paq = [];
	(function() {
		var u = piwikURL;
		if(journalID != "") {
			_paq.push(['setCustomVariable', 1, "journal", journalID, "page"]);
		}
		_paq.push(['setDownloadExtensions', "pdf"]);
		_paq.push(["setTrackerUrl", u+"piwik.php"]);
		_paq.push(["setSiteId", pageID]);
		_paq.push(["trackPageView"]);
		_paq.push(["enableLinkTracking"]);
		var d=document, g=d.createElement("script"), s=d.getElementsByTagName("script")[0]; g.type="text/javascript";
		g.defer=true; g.async=true; g.src=u+"piwik.js"; s.parentNode.insertBefore(g,s);
	})();
}

// LINK IMAGE
$(document).ready(function() {

	$("#linkImage").one("click", function() {
		appendSpinner($(this));
		var objectID = $(this).attr("data-object");
		$.post(jp.baseURL + "rsc/derivate/link/set/" + objectID).done(function() {
			window.location.reload();
		}).fail(function(err) {
			alert(err.responseText);
			window.location.reload();
		});
	});

	$(".unlinkImage").one("click", function() {
		appendSpinner($(this));
		var objectID = $(this).attr("data-object");
		var image = $(this).attr("data-image");
		$.post(jp.baseURL + "rsc/derivate/link/remove/" + objectID + "?image=" + encodeURIComponent(image)).done(function() {
			window.location.reload();
		}).fail(function(err) {
			alert(err.responseText);
			window.location.reload();
		});
	});

	function appendSpinner(node) {
		node.append("<i class='fa fa-circle-o-notch fa-spin'></i>");
	}

});

// PAGINATION JUMP
$(document).ready(function() {
	$(".pagination-jump-submit").on("click", function() {
		jump($(this));
	});
	$(".pagination-jumper-form").submit(function(event) {
		jump($("input[type=submit]", $(this)));
		event.preventDefault();
	});

	function jump(submitButton) {
		var param = submitButton.attr("data-param");
		if(param == null) {
			console.log("data-param of pagination-jump-submit not set!");
			return;
		}
		var input = $("input[id='pagination-" + param + "']");
		if(input == null || input.length == 0) {
			console.log("cannot find associated input text of " + param);
			return;
		}
		var page = parseInt(input.val());
		var pages = parseInt(submitButton.attr("data-pages"));
		if(isNaN(pages)) {
			console.log("cannot find data-pages attribute");
			pages = 99999;
		}
		if(isNaN(page) || page <= 0 || page > pages) {
			$(input).stop().animate({ borderColor: "#CD3700" }, 'fast');
			return;
		}
		var rows = parseInt(submitButton.attr("data-rows"));
		if(isNaN(rows)) {
			rows = 5;
		}
		setGetParameter(param, (page - 1) * rows);
	}

	// http://stackoverflow.com/questions/13063838/add-change-parameter-of-url-and-redirect-to-the-new-url
	function setGetParameter(paramName, paramValue) {
		var url = window.location.href;
		if (url.indexOf(paramName + "=") >= 0) {
			var prefix = url.substring(0, url.indexOf(paramName));
			var suffix = url.substring(url.indexOf(paramName));
			suffix = suffix.substring(suffix.indexOf("=") + 1);
			suffix = (suffix.indexOf("&") >= 0) ? suffix.substring(suffix.indexOf("&")) : "";
			url = prefix + paramName + "=" + paramValue + suffix;
		} else {
			if (url.indexOf("?") < 0) {
				url += "?" + paramName + "=" + paramValue;
			} else {
				url += "&" + paramName + "=" + paramValue;
			}
		}
		window.location = url;
	}
});
