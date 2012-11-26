<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="jp-controller-search.xsl" />
  <xsl:include href="jp-controller-advancedsearch.xsl" />

  <xsl:param name="mode" select="'default'"/>

  <xsl:template match="jpsearch">
    <xsl:choose>
      <xsl:when test="$mode = 'default'">
        <xsl:apply-templates select="." mode="default" />
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
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
