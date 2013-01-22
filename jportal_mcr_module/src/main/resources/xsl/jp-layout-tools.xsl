<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="">

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

  <xsl:template name="jp.printClass">
    <xsl:param name="nodes" />
    <xsl:for-each select="$nodes">
      <xsl:variable name="label" select="./label[lang($CurrentLang)]/@text" />
      <xsl:choose>
        <xsl:when test="string-length($label) = 0">
          <xsl:call-template name="jp.printClass.fallback">
            <xsl:with-param name="node" select="." />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$label" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="jp.printClass.fallback">
    <xsl:param name="node" />
    <xsl:param name="pos" select="1" />
    <xsl:variable name="classlabel" select="$node/label[lang($languages/lang[$pos]/text())]/@text" />
 	<xsl:choose>
      <xsl:when test="string-length($classlabel) != 0">
        <xsl:value-of select="$classlabel" />
      </xsl:when>
      <xsl:when test="$languages/lang[$pos + 1]">
        <xsl:call-template name="jp.printClass.fallback">
          <xsl:with-param name="node" select="$node" />
          <xsl:with-param name="pos" select="$pos + 1" />
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>