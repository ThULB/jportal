<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager" exclude-result-prefixes="i18n acl">

  <xsl:template mode="controllerHook" match="/jpsearchBar[@mode='import.sru']">
    <div id='searchBar'>
      <div class="input-group">
        <input class="form-control" id="inputField" placeholder="GND" />
        <span class="input-group-btn">
          <button class="btn btn-default" id="search" type="submit">Suche</button>
        </span>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="jp-import-sru">
    <xsl:choose>
      <xsl:when test="acl:checkPermission('POOLPRIVILEGE', 'create-person') and acl:checkPermission('POOLPRIVILEGE', 'create-jpinst')">
        <xsl:call-template name="jp.import.sru.search" />
      </xsl:when>
      <xsl:otherwise>
        <p>Sie haben keine Berechtigung Personen oder Institutionen zu importieren.</p>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="jp.import.sru.search">
    <xsl:call-template name="jp.import.sru.js" />
    <div class="jp-import-sru">
      <p class="doubletCheck hidden">
        <span class="title">Dublettencheck: </span>
        <span id="doubletCheck"></span>
      </p>
      <div id="result" class="result"></div>
    </div>
  </xsl:template>

  <xsl:template name="jp.import.sru.js">
    <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-import-sru.js" />
    <script type="text/javascript">
      $(document).ready(function() {
        $("#search").click(function() {
            querySRU($("#inputField").val());
        });
        $(document).keypress(function(e) {
          if(e.which == 13) {
            querySRU($("#inputField").val());
          }
        });
      });
    </script>
  </xsl:template>

</xsl:stylesheet>
