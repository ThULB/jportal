<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mods="http://www.loc.gov/mods/v3"
  xmlns:mets="http://www.loc.gov/METS/" xmlns:mix="http://www.loc.gov/mix/v20" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/version191/mets.xsd http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/mods.xsd http://www.loc.gov/mix/v20 http://www.loc.gov/standards/mix/mix20/mix20.xsd"
  exclude-result-prefixes="xlink">

  <xsl:template match="mets:mets">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
      <mets:structLink>
        <xsl:for-each select="mets:structMap//mets:div[@ID != '']">
          <xsl:variable name="FILEID" select=".//mets:fptr/mets:area/@FILEID" />
          <xsl:if test="$FILEID != ''">
            <xsl:variable name="to" select="/mets:mets/mets:structMap//mets:div[mets:fptr/@FILEID = $FILEID]/@ID" />
            <mets:smLink xmlns:xlink="http://www.w3.org/1999/xlink" xlink:from="{@ID}" xlink:to="{$to}" />
          </xsl:if>
        </xsl:for-each>
      </mets:structLink>
    </xsl:copy>
  </xsl:template>

  <!-- copy all -->
  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' />
    </xsl:copy>
  </xsl:template>

  <!-- ignore existing structLink -->
  <xsl:template match="mets:structLink">
  </xsl:template>

</xsl:stylesheet>
