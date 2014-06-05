<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 3494 $ $Date: 2011-07-12 11:36:36 +0200 (Tue, 12 Jul 2011) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:iview2="iview2://org.mycore.iview2.frontend.MCRIView2XSLFunctions" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  exclude-result-prefixes="xalan i18n acl mcrxml iview2">
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="ThumbnailBaseURL" select="concat($ServletsBaseURL,'MCRDFGThumbnail/')" />
  <xsl:param name="ImageBaseURL" select="concat($ServletsBaseURL,'MCRDFGServlet/')"/>
  <xsl:param name="MCR.OPAC.CATALOG" />

  <xsl:include href="mets-dfgProfile.xsl" />

  <xsl:variable name="ACTUAL.OPAC.CATALOG">
    <xsl:choose>
      <xsl:when test="$MCR.OPAC.CATALOG = '%MCROpacCatalog%'">
        <xsl:value-of select="'http://gso.gbv.de/DB=2.1/'" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$MCR.OPAC.CATALOG" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="mycoreobject">
    <xsl:comment>
      Start mycoreobject (mycoreobject-mods-pure.xsl)
    </xsl:comment>
    <mets:mets xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema"
      xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:zvdd="http://zvdd.gdz-cms.de/"
      xsi:schemaLocation="http://www.loc.gov/METS/ http://www.loc.gov/mets/mets.xsd http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
      <mets:dmdSec ID="dmd_{@ID}">
        <mets:mdWrap MDTYPE="MODS">
          <mets:xmlData>
            <mods:mods>
              <xsl:apply-templates mode="metsmeta" select="." />
              <xsl:apply-templates mode="jp.mods.extension" select="." />
            </mods:mods>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:dmdSec>

      <xsl:call-template name="amdSec" />

      <xsl:if test="./metadata/derivateLinks/derivateLink or ./structure/derobjects/derobject">
        <mets:fileSec>
          <mets:fileGrp USE="DEFAULT">
            <xsl:variable name="derivateID">
              <xsl:choose>
                <xsl:when test="./metadata/derivateLinks/derivateLink">
                  <xsl:value-of select="substring-before(./metadata/derivateLinks/derivateLink/@xlink:href, '/')" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="./structure/derobjects/derobject/@xlink:href" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:variable name="file">
              <xsl:choose>
                <xsl:when test="./metadata/derivateLinks/derivateLink">
                  <xsl:value-of select="./metadata/derivateLinks/derivateLink/@xlink:href" />
                </xsl:when>
                <xsl:when test="./structure/derobjects/derobject and iview2:getSupportedMainFile($derivateID) != ''">
                  <xsl:value-of select="iview2:getSupportedMainFileByOwner(@ID)" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="document(concat('mcrobject:',$derivateID))/mycorederivate/derivate/internals/internal/@maindoc" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:variable name="fileMimeType" select="mcrxml:getMimeType($file)" />
            <xsl:variable name="mimeType">
              <xsl:choose>
                <!-- we use the tile combine servlet which gives us always jpegs -->
                <xsl:when test="starts-with($fileMimeType, 'image/')">
                  <xsl:value-of select="'image/jpeg'" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$fileMimeType" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:variable name="url">
              <xsl:choose>
                <xsl:when test="starts-with($mimeType, 'image/')">
                  <xsl:value-of select="concat($WebApplicationBaseURL, 'servlets/MCRTileCombineServlet/MID/', $file)" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="concat($WebApplicationBaseURL,'servlets/MCRFileNodeServlet/',$derivateID, '/',$file)" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <mets:file ID="FILE_0000_DEFAULT" MIMETYPE="{$mimeType}">
              <xsl:element name="mets:FLocat">
                <xsl:attribute name="LOCTYPE">URL</xsl:attribute>
                <xsl:attribute name="xlink:href" namespace="http://www.w3.org/1999/xlink">
                <xsl:value-of select="$url" />
              </xsl:attribute>
              </xsl:element>
            </mets:file>
          </mets:fileGrp>
        </mets:fileSec>
      </xsl:if>
    </mets:mets>
    <xsl:comment>
      End mycoreobject (mycoreobject-mods-pure.xsl)
    </xsl:comment>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mycoreobject" priority="0" mode="metsmeta" xmlns:mods="http://www.loc.gov/mods/v3">
    <mets:mdWrap MDTYPE="MODS">
      <mets:xmlData>
        <mods:mods>
          <xsl:comment>
            Fallback implementation.
            Add a template in "metsmeta-dfg.xsl" matching the desired object type.
          </xsl:comment>
        </mods:mods>
      </mets:xmlData>
    </mets:mdWrap>
  </xsl:template>

</xsl:stylesheet>
