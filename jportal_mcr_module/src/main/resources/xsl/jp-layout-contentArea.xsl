<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  exclude-result-prefixes="encoder">

  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="RequestURL" />

  <xsl:include href="jp-layout-contentArea-breadcrumb.xsl" />
  <xsl:include href="jp-layout-contentArea-tableOfContent.xsl" />
  <xsl:include href="jp-layout-contentArea-derivates.xsl" />
  <xsl:include href="jp-layout-contentArea-metadata.xsl" />

  <xsl:variable name="settings" select="document('../xml/layoutDefaultSettings.xml')/layoutSettings" />

  <xsl:template priority="9" match="/mycoreobject">
    <!-- Refactoring with more apply-templates -->

    <xsl:apply-templates mode="printTitle" select="metadata/maintitles/maintitle[@inherited='0']">
      <xsl:with-param name="allowHTML" select="$objSetting/title/@allowHTML" />
    </xsl:apply-templates>

    <xsl:call-template name="breadcrumb" />

    <xsl:call-template name="tableOfContent">
      <xsl:with-param name="id" select="./@ID" />
    </xsl:call-template>

    <!-- End Refactoring with more apply-templates -->

    <div id="jp-content-container" class="jp-layout-marginLR">
      <xsl:if test="structure/derobjects|metadata/derivateLinks">
        <div id="jp-derivate-container" class="jp-layout-derivates">
          <p>Digitalisate</p>
          <div>
            <xsl:apply-templates mode="derivateDisplay" select="structure/derobjects|metadata/derivateLinks" />
          </div>
        </div>
      </xsl:if>
      <div id="jp-metadata-container" class="jp-layout-metadata">
        <xsl:if test="@xsi:noNamespaceSchemaLocation='datamodel-jpjournal.xsd'">
          <div id="intro" class="jp-layout-intro jp-layout-border-light">
            <xsl:variable name="journalIntroText" select="document(concat('notnull:webapp:/journalContext/',@ID,'/intro.xml'))/intro" />
            <xsl:copy-of select="$journalIntroText/*" />
          </div>
        </xsl:if>
        <dl class="jp-layout-metadataList">
          <xsl:apply-templates mode="metadataDisplay"
            select="metadata/child::node()[name() != 'maintitles' and not(contains(name(), 'hidden_'))]" />
        </dl>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="printTitle" match="maintitle[@inherited='0']">
    <xsl:param name="allowHTML" select="$settings/title/@allowHTML" />

    <div id="jp-maintitle" class="jp-layout-maintitle jp-layout-marginLR">
      <xsl:choose>
        <xsl:when test="$allowHTML='true'">
          <xsl:value-of disable-output-escaping="yes" select="." />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="." />
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template match="printLatestArticles">
    <ul id="latestArticles" class="latestArticles"></ul>
  </xsl:template>
</xsl:stylesheet>