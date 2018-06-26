<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 3554 $ $Date: 2011-09-01 13:02:42 +0200 (Do, 01 Sep 2011) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
                xmlns:mets="http://www.loc.gov/METS/" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="xsl xlink mets xalan i18n acl mcrxml">

  <xsl:param name="WebApplicationBaseURL" />

  <xsl:param name="MCR.Mail.Sender" />
  <xsl:param name="JP.Site.Parent.logo" />

  <xsl:template name="amdSec">
    <mets:amdSec ID="amd_{@ID}">
      <mets:rightsMD ID="rightsMD_263566811">
        <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVRIGHTS">
          <mets:xmlData>
            <xsl:call-template name="addDvRights">
              <xsl:with-param name="owner" select="$JP.Site.Owner.label" />
              <xsl:with-param name="contact" select="concat('mailto:', $MCR.Mail.Sender)" />
              <xsl:with-param name="logo" select="concat($WebApplicationBaseURL, $JP.Site.Parent.logo)" />
              <xsl:with-param name="url" select="$JP.Site.Owner.url" />
            </xsl:call-template>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:rightsMD>
      <mets:digiprovMD>
        <xsl:attribute name="ID">
          <xsl:value-of select="concat('digiprovMD', @ID)" />          
        </xsl:attribute>
        <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVLINKS">
          <mets:xmlData>
            <dv:links xmlns:dv="http://dfg-viewer.de/">
              <dv:presentation>
                <xsl:value-of select="concat($WebApplicationBaseURL,'receive/', @ID)" />
              </dv:presentation>
            </dv:links>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:digiprovMD>
    </mets:amdSec>
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