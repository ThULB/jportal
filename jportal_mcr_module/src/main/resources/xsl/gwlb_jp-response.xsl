<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY html-output SYSTEM "xsl/xsl-output-html.fragment">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  &html-output;

  <xsl:include href="MyCoReLayout.xsl" />
  <xsl:include href="gwlb_jp-response-default.xsl" />
  <xsl:include href="jp-response-subselect.xsl" />

  <xsl:variable name="PageTitle" select="'Result'" />

  <xsl:template match="/response[lst/@name='error']">
    <b><xsl:value-of select="lst[@name='error']/int" /></b><br />
    <xsl:value-of select="lst[@name='error']/str" />
  </xsl:template>

</xsl:stylesheet>
