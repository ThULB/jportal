<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 3508 $ $Date: 2011-07-14 15:49:40 +0200 (Do, 14 Jul 2011) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="mcr xalan i18n acl">
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="ThumbnailBaseURL" select="concat($ServletsBaseURL,'MCRDFGThumbnail/')" />
  <xsl:param name="ImageBaseURL" select="concat($ServletsBaseURL,'MCRDFGServlet/')"/>

  <xsl:include href="mets-dfgProfile.xsl" />

  <xsl:param name="MCR.OPAC.CATALOG"/>

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
    <xsl:variable name="derMets" select="document(concat('xslStyle:mets-dfg?objectID=',@ID,':mets:',@ID))" />
    <xsl:comment>Start mycoreobject - mycoreobject-metsmods.xsl</xsl:comment>
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
        <xsl:with-param name="mcrobject" select="@ID"/>
      </xsl:call-template>

      <xsl:copy-of select="$derMets/mets:mets/mets:fileSec" />
      <xsl:copy-of select="$derMets/mets:mets/mets:structMap" />
      <xsl:copy-of select="$derMets/mets:mets/mets:structLink" />
    </mets:mets>
    <xsl:comment>End mycoreobject - mycoreobject-metsmods.xsl</xsl:comment>
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
            Add a template in "mods-dfg.xsl" matching the desired object type.
          </xsl:comment>
        </mods:mods>
      </mets:xmlData>
    </mets:mdWrap>
  </xsl:template>

</xsl:stylesheet>