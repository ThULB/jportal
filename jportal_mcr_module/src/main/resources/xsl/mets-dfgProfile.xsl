<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mets="http://www.loc.gov/METS/"
                xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xsl xlink mets mods xalan" version="1.0">
  <xsl:include href="metsmeta-dfg.xsl" />
  <xsl:include href="mets-iview.xsl" />
  <xsl:include href="mets-amd.xsl" />

  <xsl:output method="xml" encoding="utf-8" />
  <xsl:param name="MCR.Module-iview2.SupportedContentTypes" />
  <xsl:param name="ServletsBaseURL" />
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="derivateID" />
  <xsl:param name="objectID" />
  <xsl:param name="MCR.OPAC.CATALOG" />

  <xsl:param name="JP.Site.Owner.label" />
  <xsl:param name="JP.Site.Owner.url" />
  <xsl:param name="JP.Site.Owner.logo" />

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

  <xsl:variable name="sourcedoc" select="document(concat('mcrobject:',$objectID))" />

  <xsl:template match="/mycoreobject" priority="0" mode="metsmeta" xmlns:mods="http://www.loc.gov/mods/v3">
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

  <xsl:template name="ownerEntity" xmlns:urmel="http://www.urmel-dl.de/ns/mods-entities">
    <xsl:param name="type" select="'owner'" />
    <urmel:entity type="{$type}" xlink:type="extended" xlink:title="{$JP.Site.Owner.label}">
      <urmel:site xlink:type="locator" xlink:href="{$JP.Site.Owner.url}" />
      <urmel:logo xlink:type="resource" xlink:href="{$JP.Site.Owner.logo}" />
    </urmel:entity>
  </xsl:template>

  <xsl:template match="mycoreobject" priority="0" mode="ownerEntity" xmlns:urmel="http://www.urmel-dl.de/ns/mods-entities">
    <xsl:call-template name="ownerEntity" />
  </xsl:template>

  <xsl:template match="mycoreobject" priority="0" mode="sponsorEntity">
    <xsl:comment>
      no sponsor defined
    </xsl:comment>
  </xsl:template>

  <xsl:template match="mycoreobject" priority="0" mode="partnerEntity">
    <xsl:comment>
      no partner defined
    </xsl:comment>
  </xsl:template>

  <xsl:template match="mycoreobject" priority="0" mode="entities">
    <xsl:apply-templates mode="ownerEntity" select="." />
    <xsl:apply-templates mode="sponsorEntity" select="." />
    <xsl:apply-templates mode="partnerEntity" select="." />
  </xsl:template>

  <xsl:template match="mets:mets">
    <mets:mets>
      <xsl:if test="not(mets:amdSec)">
        <xsl:variable name="emptryAMDSec">
          <mets:amdSec />
        </xsl:variable>
        <xsl:apply-templates select="xalan:nodeset($emptryAMDSec)" />
      </xsl:if>
      <xsl:if test="not(mets:dmdSec)">
        <xsl:variable name="emptyDMDSec">
          <mets:dmdSec ID="dmd_{$derivateID}"/>
        </xsl:variable>
        <xsl:apply-templates select="xalan:nodeset($emptyDMDSec)" />
      </xsl:if>
      <xsl:apply-templates />
    </mets:mets>
  </xsl:template>

  <xsl:template match="mets:dmdSec">
    <mets:dmdSec ID="dmd_{$derivateID}">
      <mets:mdWrap MDTYPE="MODS">
        <mets:xmlData>
          <mods:mods>
            <xsl:apply-templates mode="metsmeta" select="$sourcedoc/mycoreobject" />
            <mods:extension>
              <urmel:entities xmlns:urmel="http://www.urmel-dl.de/ns/mods-entities">
                <xsl:call-template name="ownerEntity" />
                <xsl:apply-templates mode="entities" select="$sourcedoc/mycoreobject" />
              </urmel:entities>
            </mods:extension>
          </mods:mods>
        </mets:xmlData>
      </mets:mdWrap>
    </mets:dmdSec>
  </xsl:template>

</xsl:stylesheet>
