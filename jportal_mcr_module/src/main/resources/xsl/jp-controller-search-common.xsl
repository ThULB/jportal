<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder">
  <!-- commonly used template for searching -->
  
  <xsl:template mode="createSolrQuery" match="query">
    <xsl:value-of select="concat('solr:q=', queryTerm/@value)" />
    <xsl:apply-templates mode="createQueryTermField" select="queryTermField" />
    <xsl:apply-templates mode="createParam" select="param" />
  </xsl:template>

  <xsl:template mode="createQueryTermField" match="queryTermField">
    <xsl:if test="@value != ''">
      <xsl:value-of select="encoder:encode(concat(' ', @name, ':', @value))" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="createParam" match="param">
    <xsl:if test="@value != ''">
      <xsl:value-of select="concat('&amp;', @name, '=', encoder:encode(@value))" />
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>