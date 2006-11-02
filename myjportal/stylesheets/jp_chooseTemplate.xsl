<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="template_jalz.xsl" />

	<!-- ===================================================================================== -->
	<xsl:template name="jp.chooseTemplate">
		<xsl:choose>
			<xsl:when test=" $template = 'template_jalz' " >
				<xsl:call-template name="template_jalz"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================================= -->
</xsl:stylesheet>
