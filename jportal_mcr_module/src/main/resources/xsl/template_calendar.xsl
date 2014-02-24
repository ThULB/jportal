<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder">

  <xsl:template match="/template[@id='template_calendar']" mode="template">
    <xsl:param name="mcrObj"/>
    <xsl:apply-templates select="$mcrObj" mode="template_calendar" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_calendar">
    <script type="text/javascript" src="../templates/template_calendar/JS/keywords.js" />
    <script>
		$(document).ready(function() {
			loadKeywords();
		});
    </script>
  </xsl:template>
</xsl:stylesheet>
