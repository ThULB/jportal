<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan" xmlns:escapeUtils="org.apache.commons.lang.StringEscapeUtils"
    exclude-result-prefixes="xlink i18n layoutTools escapeUtils">

  <xsl:template match="template[@id='template_ulb']" mode="template">
    <xsl:param name="mcrObj"/>
    <xsl:call-template name="template_date">
      <xsl:with-param name="mcrObj" select="$mcrObj"/>
    </xsl:call-template>
    <xsl:call-template name="template_maintitle">
      <xsl:with-param name="mcrObj" select="$mcrObj"/>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
