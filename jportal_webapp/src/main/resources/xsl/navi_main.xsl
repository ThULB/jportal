<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template name="Navigation_main">
    <xsl:if test="$loaded_navigation_xml/navi-main">
      <xsl:call-template name="NavigationTree">
        <xsl:with-param name="rootNode" select="$loaded_navigation_xml/navi-main" />
        <xsl:with-param name="CSSLayoutClass" select="'menu'" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$loaded_navigation_xml/menu[@id='navi-main']">
      <xsl:call-template name="NavigationTree">
        <xsl:with-param name="rootNode" select="$loaded_navigation_xml/menu[@id='navi-main']" />
        <xsl:with-param name="CSSLayoutClass" select="'menu'" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
