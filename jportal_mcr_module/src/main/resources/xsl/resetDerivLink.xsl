<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:include href="copynodes.xsl" />

  <xsl:param name="oldID" />
  <xsl:param name="newID" />

  <xsl:template match="derivateLink[contains(@xlink:href,$oldID)]/@xlink:href">
    <xsl:attribute name="xlink:href">
      <xsl:value-of select="concat($newID, substring-after(., $oldID))" />
    </xsl:attribute>
  </xsl:template>
</xsl:stylesheet>