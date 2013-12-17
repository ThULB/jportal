<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools"
  xmlns:laws="xalan://fsu.jportal.laws.common.xml.LawsXMLFunctions" xmlns:decoder="xalan://java.net.URLDecoder" xmlns:escapeUtils="org.apache.commons.lang.StringEscapeUtils"
  exclude-result-prefixes="layoutTools laws decoder escapeUtils">

  <xsl:variable name="hl">
    <xsl:variable name="encodedQuery">
      <xsl:call-template name="UrlGetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="'hl'" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="decoder:decode($encodedQuery, 'UTF-8')" />
  </xsl:variable>

  <xsl:template match="/template[@id='template_thLegislativExekutiv']" mode="template">
    <xsl:param name="mcrObj" />
    <xsl:call-template name="jp.laws.js" />
    <xsl:apply-templates select="$mcrObj" mode="template_thLegislativExekutiv" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_thLegislativExekutiv">
    <xsl:if test="contains(@ID, 'jpvolume')">
      <xsl:variable name="xml" select="laws:getXML(@ID)" />
      <xsl:if test="$xml">
        <xsl:variable name="derivateID" select="laws:getImageDerivate(@ID)" />
        <xsl:apply-templates select="$xml/gesetzessammlung" mode="template_thLegislativExekutiv">
          <xsl:with-param name="objId" select="@ID" />
          <xsl:with-param name="derivateId" select="$derivateID" />
        </xsl:apply-templates>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <!-- ================================================================================= -->
  <xsl:template name="jp.laws.js">
    <script type="text/javascript" src="{$WebApplicationBaseURL}templates/template_thLegislativExekutiv/JS/jp-laws.js" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}templates/template_thLegislativExekutiv/JS/jquery.highlight.js" />
    <script type="text/javascript">
      $(document).ready(function() {
        setLogo('<xsl:value-of select="$WebApplicationBaseURL" />');
        setMaintitle('<xsl:value-of select="escapeUtils:escapeJavaScript(layoutTools:getMaintitle($journalID))" />');
        setSearchLink('<xsl:value-of select="$WebApplicationBaseURL" />');
        linkLawsToIview();
        highlightLawsText('<xsl:value-of select="$hl" />');
      });
    </script>
  </xsl:template>
</xsl:stylesheet>
