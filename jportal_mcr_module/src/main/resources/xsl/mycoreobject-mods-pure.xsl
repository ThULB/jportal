<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 3494 $ $Date: 2011-07-12 11:36:36 +0200 (Tue, 12 Jul 2011) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mets="http://www.loc.gov/METS/"
                xmlns:mods="http://www.loc.gov/mods/v3" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
                xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
                exclude-result-prefixes="xalan i18n acl mcrxml jpxml layoutTools">
  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="ThumbnailBaseURL" select="concat($ServletsBaseURL,'MCRDFGThumbnail/')"/>
  <xsl:param name="ImageBaseURL" select="concat($ServletsBaseURL,'MCRDFGServlet/')"/>
  <xsl:param name="MCR.OPAC.CATALOG"/>

  <xsl:include href="mets-dfgProfile.xsl"/>

  <xsl:variable name="ACTUAL.OPAC.CATALOG">
    <xsl:choose>
      <xsl:when test="$MCR.OPAC.CATALOG = '%MCROpacCatalog%'">
        <xsl:value-of select="'http://gso.gbv.de/DB=2.1/'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$MCR.OPAC.CATALOG"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="mycoreobject">
    <xsl:comment>
      Start mycoreobject (mycoreobject-mods-pure.xsl)
    </xsl:comment>
    <mets:mets xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:xlink="http://www.w3.org/1999/xlink"
               xsi:schemaLocation="http://www.loc.gov/METS/ http://www.loc.gov/mets/mets.xsd http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
      <mets:dmdSec ID="dmd_{@ID}">
        <mets:mdWrap MDTYPE="MODS">
          <mets:xmlData>
            <mods:mods>
              <xsl:apply-templates mode="metsmeta" select="."/>
              <xsl:apply-templates mode="jp.mods.extension" select="."/>
            </mods:mods>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:dmdSec>

      <xsl:call-template name="amdSec"/>
      <xsl:call-template name="fileSecSelector"/>

    </mets:mets>
    <xsl:comment>
      End mycoreobject (mycoreobject-mods-pure.xsl)
    </xsl:comment>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
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

  <xsl:template name="fileSecSelector">
    <xsl:choose>
      <xsl:when test="./metadata/derivateLinks/derivateLink or ./structure/derobjects/derobject">
        <xsl:variable name="mainFile" select="jpxml:getMainFile(@ID)"/>
        <xsl:call-template name="fileSec">
          <xsl:with-param name="mimeType" select="jpxml:getMimeTypeForThumbnail($mainFile)"/>
          <xsl:with-param name="url" select="jpxml:getThumbnail(@ID, 'MID')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains(@ID, '_jpjournal_')">
        <xsl:variable name="iconURL" select="layoutTools:getJournalIconURL(@ID)" />
        <xsl:if test="$iconURL != ''">
          <xsl:call-template name="fileSec">
            <xsl:with-param name="mimeType" select="'image/svg+xml'"/>
            <xsl:with-param name="url" select="$iconURL"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="fileSec">
    <xsl:param name="mimeType"/>
    <xsl:param name="url"/>
    <mets:fileSec>
      <mets:fileGrp USE="DEFAULT">
        <mets:file ID="FILE_0000_DEFAULT" MIMETYPE="{$mimeType}">
          <xsl:element name="mets:FLocat">
            <xsl:attribute name="LOCTYPE">URL</xsl:attribute>
            <xsl:attribute name="xlink:href" namespace="http://www.w3.org/1999/xlink">
              <xsl:value-of select="$url"/>
            </xsl:attribute>
          </xsl:element>
        </mets:file>
      </mets:fileGrp>
    </mets:fileSec>
  </xsl:template>

</xsl:stylesheet>
