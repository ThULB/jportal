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
          <div id="atozFilterContainer" class="input-group">
            <input id="atozFilter" class="form-control jp-input-with-groupaddon" type="text" placeholder="Filter"/>
            <div id="atozFilterRemoveButton" class="input-group-addon">
              <span id="atozFilterRemoveIcon" class="glyphicon glyphicon-remove form-control-feedback" aria-hidden="true"></span>
            </div>
          </div>
        </div>
        <ul id="tabNav" class="nav nav-tabs tab-nav col-md-9 col-md-offset-1">

        </ul>
      </div>
        <div class="row container-fluid" id="resultList">
            <div id="atozFacets" class="col-sm-3">
                <h5 id="document_hits"></h5>
                <div id="document_type"></div>
            </div>
            <div class="col-sm-9 jp-layout-hits">
                <div class="jp-layout-triangle hidden-xs"></div>
                <div class="jp-layout-triangle hidden-xs"></div>
                <div id="objectList" class="tab-panel"></div>
            </div>
        </div>
    </div>

    <script src="https://unpkg.com/rxjs@5.4.3/bundles/Rx.min.js"></script>
    <script src="{$WebApplicationBaseURL}js/jp-journalList-facetsModel.js"></script>
    <script src="{$WebApplicationBaseURL}js/jp-journalList-facetsView.js"></script>
    <script src="{$WebApplicationBaseURL}js/jp-journalList-resultListModelView.js"></script>
    <script src="{$WebApplicationBaseURL}js/jp-journalList-tabsModelView.js"></script>
    <script src="{$WebApplicationBaseURL}js/jp-journalList-main.js"></script>
      <script type="text/javascript">
          $(document).ready(function() {
          importCSS();
          });
      </script>
  </xsl:template>

  <!-- =================================================================== -->
  <xsl:template match="journalList[@url]" priority="2">
    <xsl:apply-templates select="document(@url)/journalList"/>
  </xsl:template>

</xsl:stylesheet>
