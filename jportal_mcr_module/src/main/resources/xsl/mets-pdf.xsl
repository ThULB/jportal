<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mets="http://www.loc.gov/METS/"
>

  <xsl:param name="ThumbnailBaseURL" select="concat($ServletsBaseURL,'MCRDFGThumbnail/')" />
  <xsl:param name="ImageBaseURL" select="concat($ServletsBaseURL,'MCRTileCombineServlet/')" />
  <xsl:include href="mets-dfgProfile.xsl" />

  <xsl:template match="mets:div[$masterFileGrp/mets:file/@ID=mets:fptr/@FILEID]">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:variable name="fileID" select="mets:fptr[$masterFileGrp/mets:file/@ID=@FILEID]/@FILEID" />
      <xsl:variable name="file" select="$masterFileGrp/mets:file[@ID=$fileID]/mets:FLocat/@xlink:href" />
      <xsl:variable name="filePath">
        <!-- remove leading "./" from relative URL if present -->
        <xsl:choose>
          <xsl:when test="substring($file, 1, 2) = './'">
            <xsl:value-of select="substring($file, 3)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$file" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="urn" select="$urns/file[@name=$filePath]" />
      <xsl:if test="$urn">
        <!-- merge urn in PHYSICAL structMap -->
        <xsl:attribute name="CONTENTIDS">
          <xsl:value-of select="$urn/@urn" />
        </xsl:attribute>
      </xsl:if>
      <xsl:variable name="ncName">
        <xsl:choose>
          <xsl:when test="contains($fileID,'_')">
            <xsl:value-of select="substring-after($fileID,'_')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$fileID" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:attribute name="ORDER">
        <xsl:number />
      </xsl:attribute>
      <mets:fptr FILEID="{concat('MAX_',$ncName)}" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
