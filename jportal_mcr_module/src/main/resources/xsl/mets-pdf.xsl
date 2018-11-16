<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:dv="http://dfg-viewer.de/"
                xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
>

  <xsl:param name="ThumbnailBaseURL" select="concat($ServletsBaseURL,'MCRDFGThumbnail/')" />
  <xsl:param name="ImageBaseURL" select="concat($ServletsBaseURL,'MCRTileCombineServlet/')" />
  <xsl:param name="CCImageURL" select="concat($WebApplicationBaseURL,'images/cc/')" />
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

  <xsl:template match="mets:amdSec">
    <xsl:copy>
      <xsl:attribute name="ID">
        <xsl:value-of select="@ID"/>
      </xsl:attribute>
      <xsl:variable name="id" select="substring-after(@ID, 'amd_')"/>
      <xsl:variable name="cc-licence" select="document('../xml/cc-licence.xml')/licence"/>
      <xsl:variable name="licence" select="jpxml:getLicence($id)"/>
      <mets:rightsMD ID="{concat('RIGHTS_', $id)}">
          <mets:mdWrap MDTYPE="OTHER" OTHERMDTYPE="DVRIGHTS">
              <mets:xmlData>
                  <dv:rights>
                    <dv:licence>
                      <xsl:if test="$cc-licence/type[@name=$licence]">
                        <xsl:attribute name="img">
                          <xsl:value-of select="concat($CCImageURL, $cc-licence/type[@name=$licence]/@img)"/>
                        </xsl:attribute>
                        <xsl:attribute name="url">
                          <xsl:value-of select="$cc-licence/type[@name=$licence]/@url"/>
                        </xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="$licence"/>
                    </dv:licence>
                  </dv:rights>
              </mets:xmlData>
          </mets:mdWrap>
      </mets:rightsMD>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
