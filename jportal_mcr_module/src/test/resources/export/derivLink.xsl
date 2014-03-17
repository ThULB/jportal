<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:output method="text"/>
	<xsl:template match="node()|@*">
      <xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="derobject">
		<xsl:value-of select="concat('export derivate ',@xlink:href,' to directory foo with foo')"></xsl:value-of>
	</xsl:template>
</xsl:stylesheet>