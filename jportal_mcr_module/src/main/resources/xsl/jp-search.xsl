<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:param name="qt" select="'*'" />
  <xsl:param name="hiddenQt" select="''" />
  <xsl:param name="searchjournalID" select="''" />
  <xsl:param name="start" select="'0'" />
  <xsl:param name="rows" select="'10'" />
  <xsl:param name="returnURL" />

  <xsl:include href="jp-controller-search-common.xsl" />
  <xsl:include href="jp-controller-search.xsl" />
  <xsl:include href="jp-controller-advancedsearch.xsl" />
  <xsl:include href="jp-controller-subselect.xsl" />
  <xsl:include href="jp-controller-hidden.xsl" />

  <xsl:param name="mode" select="'default'"/>

  <xsl:template match="jpsearch">
    <xsl:choose>
      <xsl:when test="$mode = 'default'">
        <xsl:apply-templates select="." mode="default" />
      </xsl:when>
      <xsl:when test="$mode = 'hidden'">
        <xsl:apply-templates select="." mode="hidden" />
      </xsl:when>
      <xsl:when test="$mode = 'advanced.form'">
        <xsl:apply-templates select="." mode="advanced.form" />
      </xsl:when>
      <xsl:when test="$mode = 'advanced.result'">
        <xsl:apply-templates select="." mode="advanced.result" />
      </xsl:when>
      <xsl:when test="$mode = 'laws.form'">
        <xsl:apply-templates select="." mode="laws.form" />
      </xsl:when>
      <xsl:when test="$mode = 'laws.result'">
        <xsl:apply-templates select="." mode="laws.result" />
      </xsl:when>
      <xsl:when test="$mode = 'subselect.form'">
        <xsl:apply-templates select="." mode="subselect.form" />
      </xsl:when>
      <xsl:when test="$mode = 'subselect.result'">
        <xsl:apply-templates select="." mode="subselect.result" />
      </xsl:when>
    </xsl:choose>

    <!-- call dynamic template_*.xsl -->
    <xsl:if test="$searchjournalID != ''">
      <xsl:variable name="templateXML">
        <template id="{$template}" mcrID="{$searchjournalID}"></template>
      </xsl:variable>
      <xsl:apply-templates select="xalan:nodeset($templateXML)/template" mode="template" />
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
