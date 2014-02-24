<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Contains useful xsl helper tools.
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="encoder">

  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <xsl:template name="shortenString">
    <xsl:param name="string" />
    <xsl:param name="length" />
    <xsl:param name="remainder" select="'...'" />

    <xsl:choose>
      <xsl:when test="string-length($string) > $length">
        <xsl:value-of select="concat(substring($string,0,$length), $remainder)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="uppercase">
    <xsl:param name="string" />
    <xsl:value-of select="translate($string,$lcletters,$ucletters)" />
  </xsl:template>

  <xsl:template name="lowercase">
    <xsl:param name="string" />
    <xsl:value-of select="translate($string,$ucletters, $lcletters)" />
  </xsl:template>

  <xsl:template mode="createURL" match="url">
    <xsl:value-of select="base" />
    <xsl:apply-templates mode="createParam" select="param" />
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
      <xsl:choose>
        <xsl:when test="@encode = 'false'">
          <xsl:value-of select="concat($sign, @name, '=', @value)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat($sign, @name, '=', encoder:encode(@value))" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>