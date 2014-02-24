<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:include href="copynodes.xsl" />

  <xsl:param name="linkId" />

  <xsl:template match="participant[@xlink:href = $linkId]/@xlink:title">
    <xsl:variable name="linkedObj" select="document(concat('mcrobject:',$linkId))/mycoreobject/metadata" />
    <xsl:choose>
      <xsl:when test="$linkedObj/def.heading/heading">
        <xsl:attribute name="xlink:title">
          <xsl:value-of select="concat($linkedObj/def.heading/heading/lastName,', ', $linkedObj/def.heading/heading/firstName)" />
        </xsl:attribute>
      </xsl:when>
      <xsl:when test="$linkedObj/names/name">
        <xsl:attribute name="xlink:title">
          <xsl:value-of select="$linkedObj/names/name/fullname" />
        </xsl:attribute>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>