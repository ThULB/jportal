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
    <xsl:variable name="mcrobjectID" select="$mcrobject" />
    <xsl:variable name="derobjectID" select="$derobject" />

    <xsl:variable name="sectionID">
      <xsl:choose>
        <xsl:when test="$mcrobjectID">
          <xsl:value-of select="$mcrobjectID" />
        </xsl:when>
        <xsl:when test="$derobjectID">
          <xsl:value-of select="$derobjectID" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="derivateOwnerID">
      <xsl:choose>
        <!-- it is derivate -->
        <xsl:when test="$derobjectID">
          <xsl:value-of select="mcrxml:getMCRObjectID($derobjectID)" />
        </xsl:when>
        <xsl:otherwise>
          <!--it is a regular mcrobj -->
          <xsl:value-of select="$mcrobjectID" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="mcrobj" select="document(concat('mcrobject:', $derivateOwnerID))/mycoreobject" />

    <xsl:comment>
      Start amdSec - mets-amd.xsl
    </xsl:comment>
    <mets:amdSec ID="amd_{$sectionID}">
      <mets:rightsMD ID="rightsMD_263566811">
        <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVRIGHTS">
          <mets:xmlData>
            <xsl:call-template name="addDvRights">
              <xsl:with-param name="owner" select="$JP.Site.Owner.label" />
              <xsl:with-param name="contact" select="concat('mailto:', $JP.Site.adminMail)" />
              <xsl:with-param name="logo" select="$JP.Site.Parent.logo" />
              <xsl:with-param name="url" select="$JP.Site.Owner.url" />
            </xsl:call-template>
            <xsl:if test="$mcrobj/metadata/hidden_jpjournalsID/hidden_jpjournalID">
              <xsl:variable name="journal" select="document(concat('mcrobject:', $mcrobj/metadata/hidden_jpjournalsID/hidden_jpjournalID))/mycoreobject" />
              <xsl:apply-templates select="$journal/metadata/participants/participant[@type='partner']" mode="dvRightsPartner" />
            </xsl:if>
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
                  <xsl:when test="$mcrobjectID">
                    <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',$mcrobjectID)" />
                  </xsl:when>
                  <xsl:when test="$derobjectID">
                    <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',mcrxml:getMCRObjectID($derobjectID))" />
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

  <xsl:template match="participant" mode="dvRightsPartner">
    <xsl:variable name="participant" select="document(concat('mcrobject:', @xlink:href))/mycoreobject" />
    <xsl:call-template name="addDvRights">
       <xsl:with-param name="owner" select="$participant/metadata/names/name/fullname" />
	   <xsl:with-param name="contact" select="$participant/metadata/emails/email" />
	   <xsl:with-param name="logo" select="$participant/metadata/logo/url[@type='logoPlain']" />
	   <xsl:with-param name="url" select="$participant/metadata/urls/url/@xlink:href" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="addDvRights">
    <xsl:param name="owner" />
    <xsl:param name="contact" select="''" />
    <xsl:param name="logo" select="''" />
    <xsl:param name="url" select="''" />
    <dv:rights xmlns:dv="http://dfg-viewer.de/">
      <dv:owner>
        <xsl:value-of select="$owner" />
      </dv:owner>
      <xsl:if test="$contact != ''">
        <dv:ownerContact>
          <xsl:value-of select="$contact" />
        </dv:ownerContact>
      </xsl:if>
      <xsl:if test="$logo != ''">
        <dv:ownerLogo>
          <xsl:value-of select="$logo" />
        </dv:ownerLogo>
      </xsl:if>
      <xsl:if test="$url != ''">
        <dv:ownerSiteURL>
          <xsl:value-of select="$url" />
        </dv:ownerSiteURL>
      </xsl:if>
    </dv:rights>
  </xsl:template>

</xsl:stylesheet>