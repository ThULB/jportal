<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mets="http://www.loc.gov/METS/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xalan="http://xml.apache.org/xalan" xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
  xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
  exclude-result-prefixes="xsi xalan layoutTools jpxml" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd" version="1.0">
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

    <!-- Origin Info -->
    <xsl:if test="./metadata/dates/date">
      <mods:originInfo eventType="publication">
        <xsl:for-each select="./metadata/dates/date[@inherited=0]">
            <mods:dateIssued>
              <xsl:if test="@type='published_from'">
                <xsl:attribute name="point"><xsl:value-of select="'start'" /></xsl:attribute>
              </xsl:if>
              <xsl:if test="@type='published_until'">
                <xsl:attribute name="point"><xsl:value-of select="'end'" /></xsl:attribute>
              </xsl:if>
              <xsl:value-of select="text()" />
            </mods:dateIssued>
        </xsl:for-each>
        <xsl:if test="not(./metadata/dates/date[@inherited=0])">
          <mods:dateIssued>
            <xsl:value-of select="jpxml:getPublishedISODate(@ID)" />
          </mods:dateIssued>
        </xsl:if>
      </mods:originInfo>
    </xsl:if>

    <!-- Note -->
    <xsl:for-each select="./metadata/notes/note[@type='annotation' and @inherited=0]">
      <mods:note>
        <xsl:value-of select="text()" />
      </mods:note>
    </xsl:for-each>

    <!-- Language -->
    <xsl:if test="./metadata/languages/language">
      <mods:language>
        <xsl:for-each select="./metadata/languages/language[@inherited=0]">
          <mods:languageTerm authority="rfc3066"><xsl:value-of select="@categid" /></mods:languageTerm>
        </xsl:for-each>
      </mods:language>
    </xsl:if>

    <!-- Part -->
    <xsl:if test="./metadata/sizes/size">
      <mods:part>
        <mods:detail type="pages">
          <mods:number><xsl:value-of select="./metadata/sizes/size/text()" /></mods:number>
        </mods:detail>
      </mods:part>
    </xsl:if>

    <!-- Keywords -->
    <xsl:if test="./metadata/keywords/keyword">
      <mods:subject>
        <xsl:for-each select="./metadata/keywords/keyword">
          <mods:topic>
            <xsl:value-of select="text()" />
          </mods:topic>
        </xsl:for-each>
      </mods:subject>
    </xsl:if>

    <!-- genre -->
    <xsl:if test="contains(@ID,'_jpjournal_') or contains(@ID,'_jparticle_')">
      <genre authority="marcgt">
        <xsl:if test="contains(@ID,'_jpjournal_')">
          <xsl:apply-templates select="./metadata/journalTypes/journalType[@classid='jportal_class_00000200']/@categid" mode="marcgt" />
        </xsl:if>
        <xsl:if test="contains(@ID,'_jparticle_')">
          <xsl:value-of select="'article'" />
        </xsl:if>
        <!-- the genre for volumes is applied at the related item part -->
      </genre>
    </xsl:if>

    <!-- related item -> parent journal id -->
    <xsl:if test="./metadata/hidden_jpjournalsID/hidden_jpjournalID and not(contains(@ID,'_jpjournal_'))">
      <xsl:variable name="journalID" select="./metadata/hidden_jpjournalsID/hidden_jpjournalID/text()" />
      <xsl:variable name="journal" select="document(concat('mcrobject:', $journalID))/mycoreobject" />
      <mods:relatedItem type="host">
        <xsl:attribute name="ID">
          <xsl:value-of select="$journalID" />
        </xsl:attribute>
        <mods:identifier type="uri"><xsl:value-of select="concat($WebApplicationBaseURL,'receive/',$journalID)" /></mods:identifier>
        <mods:titleInfo>
          <mods:title><xsl:value-of select="$journal/metadata/maintitles/maintitle" /></mods:title>
        </mods:titleInfo>
        <xsl:for-each select="document(concat('parents:',@ID))/parents/parent[contains(@xlink:href, '_jpvolume_')]">
          <mods:part ID="{@xlink:href}" type="volume" order="{position()}">
            <text><xsl:value-of select="@xlink:title" /></text>
          </mods:part>
        </xsl:for-each>
      </mods:relatedItem>

      <!-- apply genre for volumes -->
      <xsl:if test="contains(@ID,'_jpvolume_')">
        <genre authority="marcgt">
          <xsl:apply-templates select="$journal/metadata/journalTypes/journalType[@classid='jportal_class_00000200']/@categid" mode="marcgt" />
        </genre>
      </xsl:if>
    </xsl:if>
    <xsl:apply-templates select="metadata/participants/participant" mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />

    <!-- physical location -->
    <xsl:if test="./metadata/contentClassis3/contentClassi3">
      <mods:location>
        <mods:physicalLocation><xsl:value-of select="./metadata/contentClassis3/contentClassi3/@categid" /></mods:physicalLocation>
      </mods:location>
    </xsl:if>

    <!-- journal type extension -->
    <xsl:if test="contains(@ID,'_jpjournal_')">
      <mods:extension>
        <jportal>
          <xsl:for-each select="./metadata/*[contains(name(), 'contentClassis') or contains(name(), 'volContentClassis')]/*">
            <xsl:element name="{name(.)}">
              <xsl:attribute name="classid">
                <xsl:value-of select="@classid" />
              </xsl:attribute>
              <xsl:attribute name="categid">
                <xsl:value-of select="@categid" />
              </xsl:attribute>
            </xsl:element>
          </xsl:for-each>
          <xsl:for-each select="./metadata/*[contains(name(), 'hidden_genhiddenfields')]/*">
            <xsl:element name="{name(.)}">
              <xsl:value-of select="text()" />
            </xsl:element>
          </xsl:for-each>
        </jportal>
      </mods:extension>
    </xsl:if>
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
      <xsl:variable name="participant" select="document(concat('mcrobject:',@xlink:href))/mycoreobject" />
      
      <!-- identifier -->
      <xsl:variable name="gnd" select="layoutTools:getIdentifier(@xlink:href, 'gnd')" />
      <xsl:variable name="pnd" select="layoutTools:getIdentifier(@xlink:href, 'pnd')" />
      <xsl:variable name="ppn" select="layoutTools:getIdentifier(@xlink:href, 'ppn')" />
      <xsl:choose>
        <xsl:when test="$gnd">
          <xsl:attribute name="authority">
            <xsl:value-of select="'gnd'" />
          </xsl:attribute>
          <xsl:attribute name="authorityURI">
            <xsl:value-of select="'http://d-nb.info/gnd/'" />
          </xsl:attribute>
          <xsl:attribute name="valueURI">
            <xsl:value-of select="concat('http://d-nb.info/gnd/',$gnd)" />
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="$pnd">
          <xsl:attribute name="authority">
            <xsl:value-of select="'pnd'" />
          </xsl:attribute>
          <xsl:attribute name="authorityURI">
            <xsl:value-of select="'http://d-nb.info/gnd/'" />
          </xsl:attribute>
          <xsl:attribute name="valueURI">
            <xsl:value-of select="concat('http://d-nb.info/gnd/',$pnd)" />
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="$ppn">
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
              <xsl:with-param name="ppn" select="$ppn" />
            </xsl:call-template>
          </xsl:attribute>
        </xsl:when>
      </xsl:choose>

      <!-- role -->
      <xsl:if test="@type">
        <mods:role>
          <mods:roleTerm type="code" authority="marcrelator">
            <xsl:apply-templates select="@type" mode="marcrelator" />
          </mods:roleTerm>
        </mods:role>
      </xsl:if>

      <!-- name -->
      <xsl:variable name="name" select="$participant/metadata/def.heading/heading/name" />
      <xsl:variable name="collocation" select="$participant/metadata/def.heading/heading/collocation" />
      <xsl:choose>
        <xsl:when test="$name">
          <mods:namePart type="given">
            <xsl:value-of select="$name" />
          </mods:namePart>
          <xsl:if test="$collocation">
            <mods:namePart type="termsOfAddress">
              <xsl:value-of select="$collocation" />
            </mods:namePart>
          </xsl:if>
          <mods:displayForm>
            <xsl:value-of select="$name" />
          </mods:displayForm>
        </xsl:when>
        <xsl:when test="$participant/metadata/names/name/fullname">
          <mods:displayForm>
            <xsl:value-of select="$participant/metadata/names/name/fullname" />
          </mods:displayForm>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="lastName" select="$participant/metadata/def.heading/heading/lastName" />
          <xsl:variable name="firstName" select="$participant/metadata/def.heading/heading/firstName" />
          <xsl:variable name="nameAffix" select="$participant/metadata/def.heading/heading/nameAffix" />
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
      <xsl:variable name="dateOfBirth" select="$participant/metadata/def.dateOfBirth/dateOfBirth" />
      <xsl:variable name="dateOfDeath" select="$participant/metadata/def.dateOfDeath/dateOfDeath" />
      <xsl:if test="$dateOfBirth | $dateOfDeath">
        <mods:namePart type="date">
          <xsl:value-of select="concat($dateOfBirth,' - ',$dateOfDeath)" />
        </mods:namePart>
      </xsl:if>
    </mods:name>
  </xsl:template>

  <xsl:template match="journalType/@categid" mode="marcgt">
    <xsl:choose>
      <xsl:when test=". = 'newspapers'">
        <xsl:value-of select="'newspaper'" />
      </xsl:when>
      <xsl:when test=". = 'addressBooks'">
        <xsl:value-of select="'directory'" />
      </xsl:when>
      <xsl:when test=". = 'parliamentDocuments'">
        <xsl:value-of select="'legislation'" />
      </xsl:when>
      <xsl:when test=". = 'calendars'">
        <xsl:value-of select="'calendar'" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'journal'" />
      </xsl:otherwise>
    </xsl:choose>
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

  <!-- PDF Stuff -->

  <xsl:template match="/mycoreobject" mode="entities" priority="1">
    <xsl:variable name="journalID" select="metadata/hidden_jpjournalsID/hidden_jpjournalID" />
    <!-- TODO: don't do http calls via xsl -->
    <xsl:variable name="entities" select="document(concat('logo:', $journalID))" />
    <xsl:for-each select="$entities/*">
      <xsl:copy-of select="*" />
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="mets:dmdSec" priority="1">
    <mets:dmdSec ID="dmd_{$derivateID}">
      <mets:mdWrap MDTYPE="MODS">
        <mets:xmlData>
          <mods:mods>
            <xsl:apply-templates mode="metsmeta" select="$sourcedoc/mycoreobject" />
            <xsl:apply-templates mode="jp.mods.extension" select="$sourcedoc/mycoreobject" />
          </mods:mods>
        </mets:xmlData>
      </mets:mdWrap>
    </mets:dmdSec>
  </xsl:template>

  <xsl:template mode="jp.mods.extension" match="mycoreobject">
    <xsl:param name="mcrobject" />
    <mods:extension>
      <urmel:entities xmlns:urmel="http://www.urmel-dl.de/ns/mods-entities">
        <xsl:call-template name="ownerEntity">
          <xsl:with-param name="type" select="'operator'" />
        </xsl:call-template>
        <xsl:apply-templates mode="entities" select="." />
      </urmel:entities>
    </mods:extension>
  </xsl:template>

</xsl:stylesheet>
