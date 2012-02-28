<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- ================================================================================= -->
  <xsl:template name="Navigation_main">
    <xsl:param name="rootNode" select="'navi-main'" />
    <xsl:param name="CSSLayoutClass" select="'navi_main'" />
    <xsl:param name="menuPointHeigth" select="'17'" />
    <xsl:param name="columnWidthIcon" select="'9'" />
    <xsl:param name="spaceBetweenMainLinks" select="'10'" />
    <xsl:param name="borderWidthTopDown" select="'15'" />
    <xsl:param name="borderWidthSides" select="'7'" />

    <xsl:call-template name="navigation.tree">
      <xsl:with-param name="rootNode" select="$rootNode"/>
      <xsl:with-param name="CSSLayoutClass" select="$CSSLayoutClass"/>
      <xsl:with-param name="menuPointHeigth" select="$menuPointHeigth"/>
      <!-- use pixel values -->
      <xsl:with-param name="columnWidthIcon" select="$columnWidthIcon"/>
      <!-- use percent values -->
      <xsl:with-param name="spaceBetweenMainLinks" select="$spaceBetweenMainLinks"/>
      <!-- use pixel values -->
      <xsl:with-param name="borderWidthTopDown" select="$borderWidthTopDown"/>
      <!-- use pixel values -->
      <xsl:with-param name="borderWidthSides" select="$borderWidthSides"/>
      <!-- use percent values -->
    </xsl:call-template>
  </xsl:template>

  <!-- ================================================================================= -->
</xsl:stylesheet>
