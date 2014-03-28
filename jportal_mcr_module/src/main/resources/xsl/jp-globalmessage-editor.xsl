<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" exclude-result-prefixes="xalan mcrxml">

  <xsl:template match="globalmessage-editor">
    <div id="jp-layout-globalmessage-editor" class="jp-layout-globalmessage-editor">
      <h1>Globale Nachricht bearbeiten</h1>
      <xsl:choose>
        <xsl:when test="mcrxml:isCurrentUserInRole('admin')">
          <xsl:call-template name="jp.globalmessage.editor" />
        </xsl:when>
        <xsl:otherwise>
          <p>Sie haben keine Berechtigung für diesen Bereich.</p>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template name="jp.globalmessage.editor" >
    <xsl:call-template name="jp.globalmessage.editor.js" />
    <xsl:variable name="gm" select="document('webapp:config/jp-globalmessage.xml')/globalmessage" />
    <table>
      <tr>
        <td>Sichtbarkeit</td>
        <td>
          <select id="visibility">
            <option value="hidden">Versteckt</option>
            <option value="visible">Sichtbar</option>
            <option value="user">Benutzer</option>
            <option value="admin">Administrator</option>
          </select>
        </td>
      </tr>
      <tr>
        <td>Überschrift</td>
        <td>
          <input type="text" id="head" value="{$gm/head}" />
        </td>
      </tr>
      <tr>
        <td>Nachricht</td>
        <td>
          <textarea id="message">
            <xsl:value-of select="$gm/message" />
          </textarea>
        </td>
      </tr>
    </table>
    <input type="submit" id="submit" value="Speichern" />
  </xsl:template>

  <xsl:template name="jp.globalmessage.editor.js" >
    <script type="text/javascript">
      $(document).ready(function() {
        $('#submit').on('click', function () {
          var visibility = $('#visibility').attr("value");
          var head = $('#head').attr("value");
          var message = $('#message').attr("value");
          $.ajax({
            url: jp.baseURL + "rsc/globalMessage/save",
            type:"POST",
            data: JSON.stringify({
              visibility: visibility,
              head: head,
              message: message
            }),
            contentType: "application/json; charset=utf-8",
            success: function() {
              $('<div style="font-size:120%;color:green;text-align:center">Speichern erfolgreich</div>').appendTo('#jp-layout-globalmessage-editor').fadeOut(2500);
            },
            error: function(error) {
              alert(error);
            }
          });
        });
      });
    </script>
  </xsl:template>

</xsl:stylesheet>
