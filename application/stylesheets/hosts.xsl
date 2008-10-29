<?xml version="1.0" encoding="iso-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2006/05/19 12:33:14 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mcr="http://www.mycore.org/"
>

<xsl:output method="xml" encoding="UTF-8" />

<xsl:template match="/mcr:hosts">
  <items>
    <xsl:for-each select="mcr:host">
      <item value="{@alias}">
        <xsl:copy-of select="@checked" />
        <xsl:for-each select="mcr:label">
          <label>
            <xsl:copy-of select="@*|text()|*" />
          </label>
        </xsl:for-each>
      </item>
    </xsl:for-each>
  </items>
</xsl:template>

</xsl:stylesheet>