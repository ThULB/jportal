<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="/xsl/copynodes.xsl" />

  <xsl:param name="paramsXML" />

  <xsl:template match="@*[contains(., '{') and contains(., '}')]">
    <xsl:attribute name="{name()}">
        <xsl:call-template name="replacePlaceholder">
          <xsl:with-param name="str" select="." />
        </xsl:call-template>
    </xsl:attribute>
  </xsl:template>

  <xsl:template name="replacePlaceholder">
    <xsl:param name="str" />

    <xsl:variable name="editedStr">
      <xsl:value-of select="substring-before($str,'{')"></xsl:value-of>
      <xsl:variable name="paramName">
        <xsl:variable name="tmp" select="substring-after($str,'{')" />
        <xsl:value-of select="substring-before($tmp, '}')"></xsl:value-of>
      </xsl:variable>
      <xsl:value-of select="$paramsXML/params/param[@name=$paramName]/@value" />
      <xsl:value-of select="substring-after($str,'}')"></xsl:value-of>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="contains($editedStr, '{') and contains($editedStr, '}')">
        <xsl:call-template name="replacePlaceholder">
          <xsl:with-param name="str" select="$editedStr" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$editedStr"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="placeholder">
    <xsl:variable name="paramName" select="@paramName" />
    <xsl:value-of select="$paramsXML/params/param[@name=$paramName]/@value" />
  </xsl:template>
</xsl:stylesheet>