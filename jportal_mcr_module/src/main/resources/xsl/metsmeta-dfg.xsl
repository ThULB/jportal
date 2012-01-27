<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xalan="http://xml.apache.org/xalan" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="xalan mcrxml encoder" version="1.0">
  <xsl:output method="xml" encoding="utf-8" />
  <xsl:param name="WebApplicationBaseURL" />
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

  <xsl:template match="mycoreobject[contains(@ID,'_jpjournal_') or contains(@ID,'_jpvolume_') or contains(@ID,'_jparticle_')]" priority="1" mode="metsmeta">

    <!-- Identifier -->
    <mods:identifier type="urn">
      <xsl:choose>
        <xsl:when test="./metadata/identis/identi[@type='urn']">
          <xsl:attribute name="type">urn</xsl:attribute>
          <xsl:value-of select="./metadata/identis/identi[@type='urn']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>

    <!-- Title -->
    <xsl:for-each select="./metadata/maintitles/maintitle[@inherited=0]">
      <mods:titleInfo>
        <xsl:attribute name="type"><xsl:value-of select="'uniform'" /></xsl:attribute>
        <mods:title>
          <!-- remove '@' characters -->
          <xsl:value-of select="translate(.,'@','')" />
        </mods:title>
      </mods:titleInfo>
    </xsl:for-each>

    <!-- Subtitle -->
    <xsl:for-each select="./metadata/subtitles/subtitle[@inherited=0]">
      <mods:titleInfo>
        <xsl:attribute name="type"><xsl:value-of select="'alternative'" /></xsl:attribute>
        <mods:title>
          <!-- remove '@' characters -->
          <xsl:value-of select="translate(.,'@','')" />
        </mods:title>
      </mods:titleInfo>
    </xsl:for-each>

    <!-- Origin date -->
    <xsl:if test="./metadata/dates/date[@type='published']">
      <mods:originInfo>
        <mods:dateIssued>
          <xsl:value-of select="./metadata/dates/date[@type='published']" />
        </mods:dateIssued>
      </mods:originInfo>
    </xsl:if>

    <!-- Note -->
    <xsl:if test="./metadata/notes/note">
      <mods:note>
        <xsl:value-of select="./metadata/notes/note" />
      </mods:note>
    </xsl:if>

    <xsl:apply-templates select="metadata/participants/participant" mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />
  </xsl:template>

  <xsl:template match="mycoreobject" mode="recordIdentifier">
    <mods:location>
      <mods:url access="object in context">
        <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',@ID)" />
      </mods:url>
    </mods:location>
    <mods:identifier type="uri">
      <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',@ID)" />
    </mods:identifier>
    <mods:recordInfo>
      <mods:recordIdentifier>
        <xsl:value-of select="@ID" />
      </mods:recordIdentifier>
      <mods:recordChangeDate>
        <xsl:value-of select="./service/servdates/servdate[@type='modifydate']" />
      </mods:recordChangeDate>
      <mods:recordCreationDate>
        <xsl:value-of select="./service/servdates/servdate[@type='createdate']" />
      </mods:recordCreationDate>
    </mods:recordInfo>
    <xsl:if test="parents/parent/@xlink:href">
      <mods:relatedItem type="host">
        <mods:recordInfo>
          <mods:recordIdentifier type="mcrid">
            <xsl:copy-of select="parents/parent/@xlink:href" />
          </mods:recordIdentifier>
        </mods:recordInfo>
      </mods:relatedItem>
    </xsl:if>
  </xsl:template>

  <xsl:template match="participant" mode="personal">
    <mods:name>
      <xsl:attribute name="type">
        <xsl:choose>
        <xsl:when test="contains(@xlink:href,'_jpinst_')">
          <xsl:value-of select="'corporate'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'personal'" />
        </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:variable name="personObj" select="document(concat('mcrobject:',@xlink:href))" />
      <xsl:choose>
        <xsl:when test="$personObj/mycoreobject/metadata/def.identifier/identifier[@type='pnd']">
          <xsl:attribute name="authority">
            <xsl:value-of select="'pnd'" />
          </xsl:attribute>
          <xsl:attribute name="authorityURI">
            <xsl:value-of select="'http://d-nb.info/gnd/'" />
          </xsl:attribute>
          <xsl:attribute name="valueURI">
            <xsl:value-of select="concat('http://d-nb.info/gnd/',$personObj/mycoreobject/metadata/def.identifier/identifier[@type='pnd'])" />
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="$personObj/mycoreobject/metadata/def.identifier/identifier[@type='ppn']">
          <!-- just kidding -->
          <xsl:attribute name="authority">
            <xsl:value-of select="'gvk-ppn'" />
          </xsl:attribute>
            <xsl:variable name="sourceCatalog">
              <xsl:value-of select="'https://kataloge.thulb.uni-jena.de'"></xsl:value-of>
            </xsl:variable>
            <xsl:attribute name="authorityURI">
              <xsl:value-of select="$sourceCatalog" />
            </xsl:attribute>
            <xsl:attribute name="valueURI">
              <xsl:call-template name="generateCatalogURL">
                <xsl:with-param name="catalog" select="$sourceCatalog" />
                <xsl:with-param name="ppn" select="$personObj/mycoreobject/metadata/def.identifier/identifier[@type='ppn']" />
              </xsl:call-template>
            </xsl:attribute>
        </xsl:when>
      </xsl:choose>
      <xsl:if test="@type">
        <mods:role>
          <mods:roleTerm type="code" authority="marcrelator">
            <xsl:apply-templates select="@type" mode="marcrelator" />
          </mods:roleTerm>
        </mods:role>
      </xsl:if>
      <xsl:variable name="personalName" select="$personObj/mycoreobject/metadata/def.heading/heading/personalName" />
      <xsl:variable name="collocation" select="$personObj/mycoreobject/metadata/def.heading/heading/collocation" />
      <xsl:choose>
        <xsl:when test="$personalName">
          <mods:namePart type="given">
            <xsl:value-of select="$personalName" />
          </mods:namePart>
          <xsl:if test="$collocation">
            <mods:namePart type="termsOfAddress">
              <xsl:value-of select="$collocation" />
            </mods:namePart>
          </xsl:if>
          <mods:displayForm>
            <xsl:value-of select="$personalName" />
          </mods:displayForm>
        </xsl:when>
        <xsl:when test="$personObj/mycoreobject/metadata/def.unittitle/unittitle">
          <mods:displayForm>
            <xsl:value-of select="$personObj/mycoreobject/metadata/def.unittitle/unittitle" />
          </mods:displayForm>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="lastName" select="$personObj/mycoreobject/metadata/def.heading/heading/lastName" />
          <xsl:variable name="firstName" select="$personObj/mycoreobject/metadata/def.heading/heading/firstName" />
          <xsl:variable name="nameAffix" select="$personObj/mycoreobject/metadata/def.heading/heading/nameAffix" />
          <xsl:if test="$lastName">
            <mods:namePart type="family">
              <xsl:value-of select="$lastName" />
            </mods:namePart>
          </xsl:if>
          <xsl:if test="$firstName">
            <mods:namePart type="given">
              <xsl:value-of select="$firstName" />
            </mods:namePart>
          </xsl:if>
          <mods:displayForm>
            <xsl:value-of select="concat($lastName,', ',$firstName,' ', $nameAffix)" />
          </mods:displayForm>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="dateOfBirth" select="$personObj/mycoreobject/metadata/def.dateOfBirth/dateOfBirth" />
      <xsl:variable name="dateOfDeath" select="$personObj/mycoreobject/metadata/def.dateOfDeath/dateOfDeath" />
      <xsl:if test="$dateOfBirth | $dateOfDeath">
        <mods:namePart type="date">
          <xsl:value-of select="concat($dateOfBirth,' - ',$dateOfDeath)" />
        </mods:namePart>
      </xsl:if>
    </mods:name>
  </xsl:template>

  <xsl:template match="participant/@type" mode="marcrelator">
    <!-- http://www.loc.gov/marc/relators/relacode.html -->
    <xsl:choose>
      <xsl:when test=". = 'author'">
        <xsl:value-of select="'aut'" />
      </xsl:when>
      <xsl:when test=". = 'printer'">
        <xsl:value-of select="'prt'" />
      </xsl:when>
      <xsl:when test=". = 'other'">
        <xsl:value-of select="'oth'" />
      </xsl:when>
      <xsl:when test=". = 'employer'">
        <!-- Auftraggeber -> Applicant -->
        <xsl:value-of select="'app'" />
      </xsl:when>
      <xsl:when test=". = 'person_charge'">
        <!-- Bearbeiter -> Editor -->
        <xsl:value-of select="'edt'" />
      </xsl:when>
      <xsl:when test=". = 'owner'">
        <!-- Besitzer -> owner -->
        <xsl:value-of select="'own'" />
      </xsl:when>
      <xsl:when test=". = 'previous_owner'">
        <!-- Besitzer -> former owner -->
        <xsl:value-of select="'fmo'" />
      </xsl:when>
      <xsl:when test=". = 'recipient'">
        <!-- Empfänger -> recipient -->
        <xsl:value-of select="'rcp'" />
      </xsl:when>
      <xsl:when test=". = 'artist'">
        <!-- Künstler -> artist -->
        <xsl:value-of select="'art'" />
      </xsl:when>
      <xsl:when test=". = 'writer'">
        <!-- Schreiber -> inscriber -->
        <xsl:value-of select="'ins'" />
      </xsl:when>
      <xsl:when test=". = 'translator'">
        <!-- Übersetzer -> translator -->
        <xsl:value-of select="'trl'" />
      </xsl:when>
      <xsl:when test=". = 'corporation'">
        <!-- Institution -> owner -->
        <xsl:value-of select="'own'" />
      </xsl:when>
      <xsl:when test=". = 'previous_organisation'">
        <!-- Vorbesitzende Institution -> former owner -->
        <xsl:value-of select="'fmo'" />
      </xsl:when>
      <xsl:when test=". = 'patron'">
        <!-- Förderer -> parton -->
        <xsl:value-of select="'pat'" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'asn'" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="generateCatalogURL">
    <xsl:param name="catalog" />
    <xsl:param name="ppn" />
    <xsl:if test="string-length($catalog) &gt; 5">
      <xsl:value-of select="concat($catalog,'PPN?PPN=',$ppn)" />
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>