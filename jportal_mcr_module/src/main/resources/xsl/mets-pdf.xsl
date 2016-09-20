<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mets="http://www.loc.gov/METS/">
  <xsl:param name="ThumbnailBaseURL" select="concat($ServletsBaseURL,'MCRDFGThumbnail/')" />
  <xsl:param name="ImageBaseURL" select="concat($ServletsBaseURL,'MCRTileCombineServlet/')"/>
  <xsl:include href="mets-dfgProfile.xsl" />
  
  <xsl:template match="mets:structMap[@TYPE='PHYSICAL']//mets:div[@TYPE='page']">
    <xsl:copy>
      <xsl:attribute name="ORDER">
        <xsl:number />
      </xsl:attribute>
      <xsl:apply-templates select="@*[not(local-name() = 'ORDER')]|node()" />
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
