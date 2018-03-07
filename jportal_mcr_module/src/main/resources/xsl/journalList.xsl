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
          <li>#</li>
          <li>A</li>
          <li>B</li>
          <li>C</li>
          <li>D</li>
          <li>E</li>
          <li>F</li>
          <li>G</li>
          <li>H</li>
          <li>I</li>
          <li>J</li>
          <li>K</li>
          <li>L</li>
          <li>M</li>
          <li>N</li>
          <li>O</li>
          <li>P</li>
          <li>Q</li>
          <li>R</li>
          <li>S</li>
          <li>T</li>
          <li>U</li>
          <li>V</li>
          <li>W</li>
          <li>X</li>
          <li>Y</li>
          <li>Z</li>
        </ul>
      </div>
      <div class="row container-fluid" id="resultList">
        <div id="atozFacets" class="col-sm-3">
          <h5 id="document_hits"></h5>
          <div id="document_type">
            <i class="fa fa-circle-o-notch fa-spin fa-2x jp-journalList-spinner"></i>
          </div>
        </div>
        <div class="col-sm-9 jp-layout-hits">
          <div class="jp-layout-triangle hidden-xs"></div>
          <div class="jp-layout-triangle hidden-xs"></div>
          <div id="objectList" class="tab-panel">
            <i class="fa fa-circle-o-notch fa-spin fa-2x jp-journalList-spinner"></i>
          </div>
        </div>
      </div>
    </div>

    <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/momentjs/2.10.6/min/moment-with-locales.js" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-util.js" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-journalList-main.js" />
  </xsl:template>

  <!-- =================================================================== -->
  <xsl:template match="journalList[@url]" priority="2">
    <xsl:apply-templates select="document(@url)/journalList"/>
  </xsl:template>

</xsl:stylesheet>
