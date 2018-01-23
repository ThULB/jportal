<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/"
                exclude-result-prefixes="mcr i18n">

  <xsl:include href="MyCoReLayout.xsl"/>

  <xsl:param name="selected"/>

  <xsl:variable name="PageTitle" select="'Blättern A - Z'"/>

  <!-- =================================================================== -->
  <xsl:template match="journalList[@mode='javascript']">
    <div id="firstLetterTab" class="journalList" additionalQuery="{additionalQuery}">
      <div class="atoz col-md-12">
        <div class="form-group col-md-2 jp-layout-atozilter">
          <div class="input-group">
            <input id="atozFilter" class="form-control jp-input-with-groupaddon" type="text" placeholder="Filter"/>
            <div id="atozFilterRemoveButton" class="input-group-addon">
              <span id="atozFilterRemoveIcon" class="glyphicon glyphicon-remove form-control-feedback" aria-hidden="true"></span>
            </div>
          </div>
        </div>
        <ul id="tabNav" class="nav nav-tabs tab-nav col-md-9 col-md-offset-1"/>
      </div>
      <div class="row container-fluid">
        <div id="atozFacets" class="col-sm-2"></div>
        <div class="col-sm-9 col-md-offset-1">
          <div id="resultList" class="tab-panel"></div>
        </div>
      </div>
    </div>
    <script src="{$WebApplicationBaseURL}js/lib/rx.all.js"></script>
    <script src="{$WebApplicationBaseURL}js/jp-journalList.js"></script>
    <script type="text/javascript">
      $(document).ready(function() {
      jp.az.load();
      });
    </script>
  </xsl:template>

  <!-- =================================================================== -->
  <xsl:template match="journalList[@url]" priority="2">
    <xsl:apply-templates select="document(@url)/journalList"/>
  </xsl:template>

</xsl:stylesheet>