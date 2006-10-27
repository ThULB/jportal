<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- ===================================================================================== -->
	<!-- add here the xsl stylesheet which is your template by: -->
	<!-- adding "	<xsl:include href="template_MYTEMPLATE.xsl" />" -->
	<!-- ===================================================================================== -->
	<xsl:include href="template_wcms.xsl" />
	<xsl:include href="template_mycoresample-1.xsl" />
	<xsl:include href="template_mycoresample-2.xsl" />
	<!-- ===================================================================================== -->
	<xsl:template name="chooseTemplate">
		<xsl:comment>
		chooseTemplate: "<xsl:value-of select="$template"/>"
		</xsl:comment>
		<xsl:choose>
			<xsl:when test=" $template = 'template_wcms' " >
				<xsl:call-template name="template_wcms"/>
			</xsl:when>
			<xsl:when test=" $template = 'template_mycoresample-1' " >
				<xsl:call-template name="template_mycoresample-1"/>
			</xsl:when>
			<xsl:when test=" $template = 'template_mycoresample-2' " >
				<xsl:call-template name="template_mycoresample-2"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="template_mycoresample-1"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================================= -->
</xsl:stylesheet>
