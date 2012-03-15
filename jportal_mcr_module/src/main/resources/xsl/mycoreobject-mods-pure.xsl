<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 3494 $ $Date: 2011-07-12 11:36:36 +0200 (Tue, 12 Jul 2011) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:iview2="iview2://org.mycore.iview2.frontend.MCRIView2XSLFunctions" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  exclude-result-prefixes="mcr xalan i18n acl mcrxml iview2">
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:include href="metsmeta-dfg.xsl" />
  <xsl:include href="mets-amd.xsl" />

  <xsl:param name="MCR.OPAC.CATALOG" />

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
            </mods:mods>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:dmdSec>

      <xsl:call-template name="amdSec">
        <xsl:with-param name="mcrobject" select="@ID" />
      </xsl:call-template>
      <xsl:if test="./structure/derobjects/derobject">
        <mets:fileSec>
          <mets:fileGrp USE="DEFAULT">
            <xsl:variable name="derivateID" select="./structure/derobjects/derobject/@xlink:href" />
            <xsl:choose>
              <xsl:when test="iview2:getSupportedMainFile($derivateID) != ''">
                <xsl:variable name="supportedMainFile" select="iview2:getSupportedMainFileByOwner(@ID)" />
                <mets:file ID="FILE_0000_DEFAULT" MIMETYPE="{mcrxml:getMimeType($supportedMainFile)}">
                  <xsl:element name="mets:FLocat">
                    <xsl:attribute name="LOCTYPE">URL</xsl:attribute>
                    <xsl:attribute name="xlink:href" namespace="http://www.w3.org/1999/xlink">
                    <xsl:value-of select="concat($WebApplicationBaseURL, 'servlets/MCRTileCombineServlet/MID/', $supportedMainFile)" />
                  </xsl:attribute>
                  </xsl:element>
                </mets:file>
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="file" select="document(concat('mcrobject:',$derivateID))/mycorederivate/derivate/internals/internal/@maindoc" />
                <mets:file ID="FILE_0000_DEFAULT">
                  <xsl:element name="mets:FLocat">
                    <xsl:attribute name="LOCTYPE">URL</xsl:attribute>
                    <xsl:attribute name="xlink:href" namespace="http://www.w3.org/1999/xlink">
                      <xsl:value-of select="concat($WebApplicationBaseURL,'servlets/MCRFileNodeServlet/',$derivateID, '/',$file)" />
                    </xsl:attribute>
                  </xsl:element>
                </mets:file>
              </xsl:otherwise>
            </xsl:choose>
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
