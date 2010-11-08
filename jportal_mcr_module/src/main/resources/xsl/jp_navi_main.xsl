<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- ================================================================================= -->
  <xsl:template name="Navigation_main">
    <xsl:call-template name="navigation.tree">
      <xsl:with-param name="rootNode" select="'navi-main'"/>
      <xsl:with-param name="CSSLayoutClass" select="'navi_main'"/>
      <xsl:with-param name="menuPointHeigth" select="'17'"/>
      <!-- use pixel values -->
      <xsl:with-param name="columnWidthIcon" select="'9'"/>
      <!-- use percent values -->
      <xsl:with-param name="spaceBetweenMainLinks" select="'10'"/>
      <!-- use pixel values -->
      <xsl:with-param name="borderWidthTopDown" select="'15'"/>
      <!-- use pixel values -->
      <xsl:with-param name="borderWidthSides" select="'7'"/>
      <!-- use percent values -->
    </xsl:call-template>
  </xsl:template>
  <!-- ================================================================================= -->
</xsl:stylesheet>