<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:include href="/xsl/copynodes.xsl" />

  <xsl:param name="journalID" />
  <xsl:param name="url" />

  <xsl:template match="/MyCoReWebPage/journalID">
    <journalID>
      <xsl:value-of select="$journalID" />
    </journalID>
  </xsl:template>

  <xsl:template match="div[@id='jportal_acl_ip_editor_module']/@url">
    <xsl:attribute name="url">
      <xsl:value-of select="$url" />
    </xsl:attribute>
  </xsl:template>
</xsl:stylesheet>