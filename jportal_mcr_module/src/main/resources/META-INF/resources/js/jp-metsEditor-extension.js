$(document).ready(function() {

	// load css stuff
	$('head').append( $('<link rel="stylesheet" type="text/css" />').attr('href', baseURL +  'webjars/font-awesome/4.4.0/css/font-awesome.min.css') );

	// load js stuff
	$.getScript(baseURL + "webjars/bootstrap-dialog/1.34.6/dist/js/bootstrap-dialog.min.js");

	// add sync button when angular has build the html content
	observeElementReady("mets-editor-toolbar-state-controls", onReady);

	function onReady(node) {
		var form = $("<form class='navbar-form navbar-left'></div>");
		var button = $("<button type='button' class='btn btn-default' title='Mets synchronisieren'>" +
				"<span class='glyphicon glyphicon-retweet'></span> " +
		        "Mets synchronisieren</button>");
		form.append(button);
		$(node).after(form);
		button.on("click", onSynchronizeClicked);
	}

	function onSynchronizeClicked() {
		var dialog = new BootstrapDialog({
      closable: false,
      message: function(dialogRef) {

      	function getCloseButton(reload, text) {
      		var close = $("<button class='btn btn-primary btn-lg btn-block'>" + text + "</button>");
      		close.on('click', {dialogRef: dialogRef}, function(event) {
      			if(reload) {
      				document.location = baseURL + "rsc/mets/editor/start/" + derivateID
      			}
      			event.data.dialogRef.close();
      		});
      		return close;
      	}

      	var sruDialogInfo = $(
      		"<div style='text-align: center;'>" +
      		"<p>Die Mets-Datei wird mit der JPortal Objekt Struktur abgeglichen. Bitte warten...</p>" +
      		"<p><i class='fa fa-3x fa-circle-o-notch fa-spin'></i></p>" +
      		"</div>"
      	);
        $.ajax({
    			url: baseURL + "rsc/mets/sync/" + derivateID,
    			dataType: "json"
    		}).done(function(data) {
    			if(data.labelsUpdated == 0 && data.structLinkSynced == false) {
    				sruDialogInfo.html("<p>Synchronisation erfolgreich. Es konnten keine Differenzen festgestellt werden. Die Mets-Datei ist auf dem aktuellen Stand.</p>");
    				sruDialogInfo.append(getCloseButton(false, "Schließen"));
    			} else if(data.errorMsg) {
    				sruDialogInfo.html("<p>Synchronisation fehlgeschlagen: " + data.errorMsg + "</p>");
    				sruDialogInfo.append(getCloseButton(false, "Schließen"));
    			} else {
    			  var msg = "";
    				if(data.labelsUpdated != 0 && data.structLinkSynced == true) {
    				  msg = "<p>Synchronisation erfolgreich. Es wurden " + data.labelsUpdated + " Titel Einträge aktualisiert und die structLink Sektion wurde angepasst." +
              ". Der Editor muss neu geladen werden.</p>";
    				} else if(data.labelsUpdated != 0) {
    				  msg = "<p>Synchronisation erfolgreich. Es wurden " + data.labelsUpdated + " Titel Einträge aktualisiert. Der Editor muss neu geladen werden.</p>";
    				} else {
    				  msg = "<p>Synchronisation erfolgreich. Die structLink Sektion wurde aktualisiert. Der Editor muss neu geladen werden.</p>";
    				}
    				if(data.errors != null && data.errors.length > 0) {
    				  msg += "<div style='color: red; text-align: left;'>Folgende Einträge konnten nicht aktualisiert werden:<ul>";
    				  data.errors.forEach(function(error) {
    				    msg += "<li>" + error.id + ": " + error.reason + "</li>";
    				  });
    				  msg += "</ul></div>";
    				}
    				sruDialogInfo.html(msg);
    			  sruDialogInfo.append(getCloseButton(true, "Editor neu laden"));
    			}
        }).fail(function(jqXHR, textStatus) {
        	var infoText = jqXHR.statusText;
        	sruDialogInfo.html("<p>Anfrage fehlgeschlagen: " + infoText + ".</p>");
        	sruDialogInfo.append(getCloseButton(false, "Schließen"));
        });
        return sruDialogInfo;
      }
		});
        dialog.realize();
        dialog.getModalHeader().hide();
        dialog.getModalFooter().hide();
        dialog.open();
	}

	function observeElementReady(id, callback) {
		var observer = new MutationObserver(function(mutations) {
			mutations.forEach(function(mutation) {
				if(!mutation.addedNodes) {
					return;
				}
				for (var i = 0; i < mutation.addedNodes.length; i++) {
					var node = mutation.addedNodes[i];
					if(node.getAttribute && node.getAttribute("id") == id) {
						observer.disconnect();
						callback(node);
						return;
					}
				}
			});
		});
		observer.observe(document.body, {
			childList : true,
			subtree : true,
			attributes : false,
			characterData : false
		});
	}

});
