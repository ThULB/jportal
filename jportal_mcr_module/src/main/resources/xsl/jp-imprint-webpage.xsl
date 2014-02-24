<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:include href="/xsl/copynodes.xsl" />

  <xsl:param name="journalID" />

  <xsl:template match="/MyCoReWebPage">
    <MyCoReWebPage>
      <journalID>
        <xsl:value-of select="$journalID" />
      </journalID>
      <xsl:apply-templates select="@*|node()" />
    </MyCoReWebPage>
  </xsl:template>

</xsl:stylesheet>