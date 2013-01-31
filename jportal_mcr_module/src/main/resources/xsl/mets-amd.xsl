<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 3554 $ $Date: 2011-09-01 13:02:42 +0200 (Do, 01 Sep 2011) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" exclude-result-prefixes="mcr xalan i18n acl mcrxml">

  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="MCR.OPAC.CATALOG" />
  <xsl:param name="objectID" />

  <xsl:param name="JP.Site.adminMail" />
  <xsl:param name="JP.Site.Parent.logo" />

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

  <xsl:template name="amdSec">
    <xsl:param name="mcrobject" />
    <xsl:param name="derobject" />

    <xsl:variable name="sectionID">
      <xsl:choose>
        <xsl:when test="$mcrobject">
          <xsl:value-of select="$mcrobject" />
        </xsl:when>
        <xsl:when test="$derobject">
          <xsl:value-of select="$derobject" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="derivateOwnerId">
      <xsl:choose>
        <!-- it is derivate -->
        <xsl:when test="$derobject">
          <xsl:value-of select="mcrxml:getMCRObjectID($derobject)" />
        </xsl:when>
        <xsl:otherwise>
          <!--it is a regular mcrobj -->
          <xsl:value-of select="$mcrobject" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:comment>
      Start amdSec - mets-amd.xsl
    </xsl:comment>
    <mets:amdSec ID="amd_{$sectionID}">
      <mets:rightsMD ID="rightsMD_263566811">
        <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVRIGHTS">
          <mets:xmlData>
            <dv:rights xmlns:dv="http://dfg-viewer.de/">
              <dv:owner>
                <xsl:value-of select="$JP.Site.Owner.label" />
              </dv:owner>
              <dv:ownerContact>
                <xsl:value-of select="concat('mailto:', $JP.Site.adminMail)" />
              </dv:ownerContact>
              <dv:ownerLogo>
                <xsl:value-of select="$JP.Site.Parent.logo" />
              </dv:ownerLogo>
              <dv:ownerSiteURL>
                <xsl:value-of select="$JP.Site.Owner.url" />
              </dv:ownerSiteURL>
            </dv:rights>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:rightsMD>
      <mets:digiprovMD>
        <xsl:attribute name="ID">
            <xsl:value-of select="concat('digiprovMD',$sectionID)" />          
          </xsl:attribute>
        <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVLINKS">
          <mets:xmlData>
            <dv:links xmlns:dv="http://dfg-viewer.de/">
              <dv:presentation>
                <xsl:choose>
                  <xsl:when test="$mcrobject">
                    <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',$mcrobject)" />
                  </xsl:when>
                  <xsl:when test="$derobject">
                    <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',mcrxml:getMCRObjectID($derobject))" />
                  </xsl:when>
                </xsl:choose>
              </dv:presentation>
            </dv:links>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:digiprovMD>
    </mets:amdSec>
    <xsl:comment>
      End amdSec - mets-amd.xsl
    </xsl:comment>
  </xsl:template>

</xsl:stylesheet>