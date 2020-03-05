var jp = jp || {};

$(document).ready(function() {
  jp.history.init();
});

jp.history = jp.history || {

  id: null,

  init: function() {
    jp.history.id = $("#historyTable").data("id");

    $(".versionRow").mouseenter(function() {
      var rev = $(this).data("rev");
      if (rev == null) {
        return;
      }
      var td = $(this).find('.userAction');
      td.append("<i class='fas fa-undo' style='cursor: pointer;' onclick='jp.history.onClickRestore(" + rev + ")'></i>");
    });
    $(".versionRow").mouseleave(function() {
      $(this).find('.userAction').empty();
    });
  },

  onClickRestore: function(rev) {
    var msg = 'Sind Sie sich sicher das Sie die Revision ' + rev + ' wiederherstellen wollen?';
    msg += ' <p>Bitte beachten Sie das sich weder Derivate noch Kinddokumente zurücksetzen lassen.';
    msg += ' Es werden ausschließtlich die Metadaten und die Sortierung der Kinder geändert.</p>';

    new BootstrapDialog({
      title: 'Zurücksetzen?',
      message: msg,
      buttons: [ {
        label: 'Nein',
        action: function(dialog) {
          dialog.close();
        }
      }, {
        label: 'Ja',
        cssClass: 'btn-primary',
        action: function(dialog) {
          jp.history.restore(rev, function() {
            alert("Revision erfolgreich wiederhergestellt.");
            dialog.close();
          }, function(e) {
            if (e.status == "401") {
              alert("Sie haben nicht die notwendige Berechtigung die Revision wiederherzustellen!");
            } else {
              console.log(e);
              alert("Es ist ein Fehler aufgetreten. Bitte wenden Sie sich an den Administrator.");
            }
            dialog.close();
          });
        }
      } ]
    }).open();
  },

  restore: function(rev, done, failed) {
    var url = jp.baseURL + 'rsc/object/restore/' + jp.history.id + "/" + rev;
    $.get(url).done(done).fail(failed);
  }

}
