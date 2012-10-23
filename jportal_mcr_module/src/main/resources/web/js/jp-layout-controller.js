function introEditor(journalID) {
    var createdElem = null;

    var ckEditorMainButtonCtr = function(tmpElem) {
        return function() {
            $('#ckeditorButton').hide();
            var introFrame = $('#intro');
            introFrame.ckeditor({
                resize_enabled : false,
                entities: false,
                enterMode: CKEDITOR.ENTER_BR,
                entities_processNumerical: 'force',
                tabSpaces: 4,
                fillEmptyBlocks: false,
                height : '500px',
                toolbar : [ ['Undo', 'Redo', '-', 'Bold', 'Italic', '-', 'NumberedList', 'BulletedList', '-',
                        'Link', 'Unlink' ] ]
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
                    url : '/rsc/journaFile/'+journalID+'/intro.xml',
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
                    $('<div/>').load('/jpCkeditor/GUI.html #ckEditorCancelMsgContainer', cancelMsgButtonCtr).appendTo('#main');
                } else {
                    cancelNoSave();
                }

            })
        }
    }

    if ($('#intro').length) {
        var tmpElem = $('<div id="#ckEditorTmp"/>')
        tmpElem.load('/jpCkeditor/GUI.html #ckeditorButtons', ckEditorMainButtonCtr(tmpElem)).insertAfter('#intro');
    } else {
        var Lcolum = $('#jp-content-LColumn>ul');
        if (Lcolum.length == 0) {
            var tmpElem = $('<div id="#ckEditorTmp"/>')
            tmpElem.load('/jpCkeditor/GUI.html #jp-content-LColumn', ckEditorMainButtonCtr(tmpElem)).insertAfter('#jp-maintitle');
        } else {
            var tmpElem = $('<li id="#ckEditorTmp"/>')
            tmpElem.load('/jpCkeditor/GUI.html #jp-content-LColumn-List .ckGUI', ckEditorMainButtonCtr(tmpElem)).appendTo(Lcolum);
        }
    }
}
