<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<xsl:variable name="searchQuery" xmlns:encoder="xalan://java.net.URLEncoder">
			<xsl:value-of select="encoder:encode('linkDerivExist = true')" />
		</xsl:variable>
		<xsl:copy-of select="document(concat('query:term=',$searchQuery))"></xsl:copy-of>
	</xsl:template>
</xsl:stylesheet>