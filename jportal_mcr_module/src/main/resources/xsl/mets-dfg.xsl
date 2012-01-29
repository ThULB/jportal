<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mets="http://www.loc.gov/METS/" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xalan="http://xml.apache.org/xalan" xmlns:mcr="xalan://org.mycore.common.xml.MCRXMLFunctions" exclude-result-prefixes="mcr" version="1.0">
  <xsl:include href="metsmeta-dfg.xsl"/>
  <xsl:include href="mets-iview.xsl"/>
  <xsl:output method="xml" encoding="utf-8"/>
  <xsl:param name="MCR.Module-iview2.SupportedContentTypes"/>
  <xsl:param name="ServletsBaseURL"/>
  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="derivateID"/>
  <xsl:param name="objectID"/>
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

  <xsl:variable name="sourcedoc" select="document(concat('mcrobject:',$objectID))"/>

  <xsl:template match="/mycoreobject" priority="0" mode="metsmeta" xmlns:mods="http://www.loc.gov/mods/v3">
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

  <xsl:template match="mets:dmdSec">
    <mets:dmdSec ID="dmd_{$derivateID}">
      <mets:mdWrap MDTYPE="MODS">
        <mets:xmlData>
          <mods:mods>
            <xsl:apply-templates mode="metsmeta" select="$sourcedoc/mycoreobject"/>
            <mods:extension>
				<xsl:variable name="journalID" select="$sourcedoc/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID"></xsl:variable>
                <xsl:variable name="entities" select="document(concat($WebApplicationBaseURL,'rsc/modslogos/',$journalID))"/>
                
                <xsl:if test="count($entities/child::node()) > 0">
                	<xsl:copy-of select="$entities" />
                </xsl:if>
            </mods:extension>
          </mods:mods>
        </mets:xmlData>
      </mets:mdWrap>
    </mets:dmdSec>
  </xsl:template>

  <xsl:template match="mets:amdSec">
    <mets:amdSec ID="amd_{$derivateID}">
      <mets:rightsMD ID="rightsMD_263566811">
        <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVRIGHTS">
          <mets:xmlData>
            <dv:rights xmlns:dv="http://dfg-viewer.de/">
              <dv:owner>Thüringer Universitäts- und Landesbibliothek</dv:owner>
              <dv:ownerContact>mailto:s.hermann@uni-jena.de</dv:ownerContact>
              <dv:ownerLogo>
                <xsl:value-of select="concat($WebApplicationBaseURL,'images/logo-dfg-viewer.png')"/>

              </dv:ownerLogo>
              <dv:ownerSiteURL>http://www.thulb.uni-jena.de/</dv:ownerSiteURL>
            </dv:rights>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:rightsMD>
      <mets:digiprovMD>
        <xsl:attribute name="ID">
            <xsl:value-of select="concat('digiprovMD',$derivateID)"/>          
          </xsl:attribute>
        <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVLINKS">
          <mets:xmlData>
            <dv:links xmlns:dv="http://dfg-viewer.de/">
              <xsl:variable name="ppn" select="$sourcedoc/mycoreobject/metadata/def.identifier/identifier[@type='ppn']"/>
              <xsl:if test="$ppn">
                <dv:reference>
                  <xsl:value-of select="concat($ACTUAL.OPAC.CATALOG,'PPN?PPN=',$ppn)"/>
                </dv:reference>
              </xsl:if>
              <dv:presentation>
                <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',$objectID)"/>
              </dv:presentation>
            </dv:links>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:digiprovMD>
    </mets:amdSec>
  </xsl:template>
</xsl:stylesheet>