<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="template[@id='template_addrBookTh']" mode="template">
    <xsl:param name="mcrObj"/>
    <xsl:call-template name="template_date">
      <xsl:with-param name="mcrObj" select="$mcrObj"/>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>