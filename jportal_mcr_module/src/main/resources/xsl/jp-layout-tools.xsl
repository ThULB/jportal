<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <xsl:template mode="printListEntry" match="*">
    <li>
      <xsl:apply-templates mode="printListEntryContent" select="." />
    </li>
  </xsl:template>

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

</xsl:stylesheet>