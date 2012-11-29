<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder">
  <!-- commonly used template for searching -->
  
  <xsl:template mode="createSolrQuery" match="query">
    <xsl:value-of select="concat('solr:q=', encoder:encode(concat('+(', queryTerm/@value, ')'), 'UTF-8'))" />
    <xsl:apply-templates mode="createQueryTermField" select="queryTermField" />
    <xsl:apply-templates mode="createParam" select="param" >
      <xsl:with-param name="sign" select="'&amp;'"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template mode="createURL" match="url">
    <xsl:value-of select="base" />
    <xsl:apply-templates mode="createParam" select="param" />
  </xsl:template>

  <xsl:template mode="createQueryTermField" match="queryTermField">
    <xsl:if test="@value != ''">
      <xsl:value-of select="encoder:encode(concat(' ', @name, ':', @value))" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="createParam" match="param">
    <xsl:param name="sign">
      <xsl:choose>
        <xsl:when test="position()=1">
          <xsl:value-of select="'?'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'&amp;'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>

    <xsl:if test="@value != ''">
      <xsl:value-of select="concat($sign, @name, '=', encoder:encode(@value))" />
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>