<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="/xsl/copynodes.xsl" />

  <xsl:param name="paramsXML" />

  <xsl:template match="@*[contains(., '{') and contains(., '}')]">
    <xsl:attribute name="{name()}">
        <xsl:value-of select="substring-before(.,'{')"></xsl:value-of>
        <xsl:variable name="paramName">
          <xsl:variable name="tmp" select="substring-after(.,'{')" />
          <xsl:value-of select="substring-before($tmp, '}')"></xsl:value-of>
        </xsl:variable>
        <xsl:value-of select="$paramsXML/params/param[@name=$paramName]/@value"/>
        <xsl:value-of select="substring-after(.,'}')"></xsl:value-of>
      </xsl:attribute>
  </xsl:template>

  <xsl:template match="placeholder">
    <xsl:variable name="paramName" select="@paramName" />
    <xsl:value-of select="$paramsXML/params/param[@name=$paramName]/@value"/>
  </xsl:template>
  
  <xsl:template match="insert[contains(@for, $paramsXML/params/param[@name='type']/@value)]">
    <xsl:apply-templates select="*"/>
  </xsl:template>
</xsl:stylesheet>