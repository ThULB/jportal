<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xalan="http://xml.apache.org/xalan" xmlns:mcr="http://www.mycore.org/"
  exclude-result-prefixes="encoder">

  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="RequestURL" />

  <xsl:include href="jp-layout-contentArea-breadcrumb.xsl" />
  <xsl:include href="jp-layout-contentArea-tableOfContent.xsl" />
  <xsl:include href="jp-layout-contentArea-derivates.xsl" />
  <xsl:include href="jp-layout-contentArea-metadata.xsl" />

  <xsl:variable name="settings" select="document('../xml/layoutDefaultSettings.xml')/layoutSettings" />

  <xsl:template priority="9" match="/mycoreobject">
    <xsl:variable name="objectEditingHTML">
      <editing>
        <xsl:call-template name="objectEditing">
          <xsl:with-param name="id" select="/mycoreobject/@ID" />
          <xsl:with-param name="dataModel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
        </xsl:call-template>
      </editing>
    </xsl:variable>
    <xsl:variable name="objectEditing" select="xalan:nodeset($objectEditingHTML)/editing" />
    <xsl:variable name="contentRColHtml">
      <xsl:choose>
        <xsl:when test="$objectEditing/menu[@id='jp-object-editing']/li">
          <div id="jp-content-RColumn" class="jp-layout-content-RCol">
            <xsl:copy-of select="$objectEditing/menu[@id='jp-object-editing' and li]" />
          </div>
          <class for="jp-content-LColumn">jp-layout-content-LCol-RCol</class>
        </xsl:when>
        <xsl:otherwise>
          <class for="jp-content-LColumn">jp-layout-content-LCol-noRCol</class>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="contentRCol" select="xalan:nodeset($contentRColHtml)" />

    <xsl:apply-templates mode="printTitle" select="metadata/maintitles/maintitle[@inherited='0']">
      <xsl:with-param name="allowHTML" select="$objSetting/title/@allowHTML" />
    </xsl:apply-templates>

    <xsl:call-template name="breadcrumb" />
    <xsl:if test="structure/children">
      <div id="jp-content-LColumn" class="jp-layout-content-LCol {$contentRCol/class[@for='jp-content-LColumn']}">
        <ul>
          <xsl:call-template name="tableOfContent">
            <xsl:with-param name="id" select="./@ID" />
          </xsl:call-template>
          <xsl:if test="@xsi:noNamespaceSchemaLocation='datamodel-jpjournal.xsd'">
            <xsl:variable name="journalIntroText" select="document(concat('notnull:webapp:/journalContext/',@ID,'/intro.xml'))/intro" />
            <xsl:if test="$journalIntroText">
              <li>
                <div id="intro" class="jp-layout-intro">
                  <xsl:copy-of select="$journalIntroText/*" />
                </div>
              </li>
            </xsl:if>
          </xsl:if>
        </ul>
      </div>
    </xsl:if>

    <xsl:copy-of select="$contentRCol/div[@id='jp-content-RColumn']"></xsl:copy-of>
    <div id="jp-content-Bottom">
      <xsl:if test="metadata/child::node()[not(contains(name(), 'hidden_')) and */@inherited='0']">
        <dl class="jp-layout-metadataList">
          <xsl:apply-templates mode="metadataDisplay" select="metadata/child::node()[not(contains(name(), 'hidden_')) and */@inherited='0']" />
        </dl>
      </xsl:if>
      <xsl:if test="structure/derobjects or metadata/derivateLinks">
        <div id="derivCol">
          <h4>Digitalisate</h4>
          <xsl:call-template name="derivateDisplay">
            <xsl:with-param name="nodes" select="structure/derobjects|metadata/derivateLinks" />
          </xsl:call-template>
        </div>
      </xsl:if>
    </div>

    <div id="viewerContainerWrapper" />
  </xsl:template>

  <xsl:template mode="printTitle" match="maintitle[@inherited='0']">
    <xsl:param name="allowHTML" select="$settings/title/@allowHTML" />

    <div id="jp-maintitle" class="jp-layout-maintitle">
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