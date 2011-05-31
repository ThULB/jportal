<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="xalan mcrxml encoder" version="1.0">
  <xsl:output method="xml" encoding="utf-8" />
  <xsl:param name="WebApplicationBaseURL" />
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

  <xsl:template match="mycoreobject[contains(@ID,'_jpjournal_') or contains(@ID,'_jpvolume_') or contains(@ID,'_jparticle_')]" priority="1" mode="metsmeta">
    <mods:identifier type="urn">
      <xsl:choose>
        <xsl:when test="./metadata/def.identifier/identifier[@type='urn']">
          <xsl:attribute name="type">urn</xsl:attribute>
          <xsl:value-of select="./metadata/def.identifier/identifier[@type='urn']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>
    <xsl:if test="./metadata/def.shelfmark/shelfmark">
      <mods:location>
        <mods:shelfLocator>
          <xsl:value-of select="./metadata/def.shelfmark/shelfmark[1]" />
        </mods:shelfLocator>
      </mods:location>
    </xsl:if>

    <xsl:variable name="sourceCatalog">
      <xsl:choose>
        <xsl:when test="../../def.source/source[@type='catalog']">
          <xsl:value-of select="../../def.source/source[@type='catalog']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$ACTUAL.OPAC.CATALOG" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:for-each select="./metadata/def.title/title">
      <mods:titleInfo>
        <xsl:attribute name="type">
          <xsl:choose>
            <xsl:when test="@type ='main_title'">
              <xsl:value-of select="'uniform'" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'alternative'" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:if test="metadata/def.identifier/identifier[@type='ppn']">
          <!-- just kidding -->
          <xsl:attribute name="authority">
            <xsl:value-of select="'gbv'" />
          </xsl:attribute>
          <xsl:if test="string-length($sourceCatalog)>5">
            <xsl:attribute name="authorityURI">
              <xsl:value-of select="$sourceCatalog" />
            </xsl:attribute>
            <xsl:attribute name="valueURI">
              <xsl:call-template name="generateCatalogURL">
                <xsl:with-param name="catalog" select="$sourceCatalog" />
                <xsl:with-param name="ppn" select="metadata/def.identifier/identifier[@type='ppn']" />
              </xsl:call-template>
            </xsl:attribute>
          </xsl:if>
        </xsl:if>
        <mods:title>
          <!-- remove '@' characters -->
          <xsl:value-of select="translate(.,'@','')" />
        </mods:title>
      </mods:titleInfo>
    </xsl:for-each>
    <mods:originInfo>
      <mods:place>
        <mods:placeTerm type="text">
          <xsl:value-of select="./metadata/def.place/place[@type='printing_original_item']" />
          <xsl:value-of select="./metadata/def.place/place[@type='pointOfOrigin']" />
        </mods:placeTerm>
      </mods:place>
      <mods:dateIssued>
        <xsl:value-of select="./metadata/def.date/date" />
      </mods:dateIssued>
    </mods:originInfo>
    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
      <xsl:if test="./metadata/def.extendOfItem/extendOfItem">
        <mods:extent>
          <xsl:value-of select="./metadata/def.extendOfItem/extendOfItem" />
        </mods:extent>
      </xsl:if>
      <xsl:if test="./metadata/def.format/format">
        <mods:extent>
          <xsl:value-of select="./metadata/def.format/format" />
        </mods:extent>
      </xsl:if>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.note/note">
      <mods:note>
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>
    <xsl:apply-templates select="metadata/def.entitylink/entitylink" mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_certificate_')]" priority="1" mode="metsmeta">
    <mods:identifier type="urn">
      <xsl:choose>
        <xsl:when test="./metadata/def.identifier/identifier[@type='urn']">
          <xsl:attribute name="type">urn</xsl:attribute>
          <xsl:value-of select="./metadata/def.identifier/identifier[@type='urn']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>
    <xsl:if test="./metadata/def.shelfmark/shelfmark">
      <mods:location>
        <mods:shelfLocator>
          <xsl:value-of select="./metadata/def.shelfmark/shelfmark" />
        </mods:shelfLocator>
      </mods:location>
    </xsl:if>
    <xsl:if test="./metadata/def.register/register">
      <mods:titleInfo>
        <mods:title>
          <xsl:value-of select="./metadata/def.register/register"/>
        </mods:title>
      </mods:titleInfo>
    </xsl:if>
    <mods:originInfo>
      <xsl:if test="./metadata/def.date/date">
        <mods:dateIssued>
          <xsl:value-of select="./metadata/def.date/date"/>
        </mods:dateIssued>
        <mods:issuance>
          <xsl:value-of select="'single unit'" />
        </mods:issuance>
      </xsl:if>
    </mods:originInfo>
    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.note/note">
      <mods:note>
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>
    <xsl:apply-templates select="." mode="recordIdentifier" />
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_file_')]" priority="1" mode="metsmeta">
    <mods:identifier>
      <xsl:choose>
        <xsl:when test="./metadata/def.unitid/unitid">
          <xsl:attribute name="type">unitid</xsl:attribute>
          <xsl:value-of select="./metadata/def.identifier/identifier[1]" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>
    <mods:titleInfo>
      <mods:title>
        <xsl:value-of select="./metadata/def.unittitle/unittitle[@type='proper']" />
      </mods:title>
    </mods:titleInfo>
    <mods:originInfo>
      <xsl:if test="./metadata/def.provenance/provenance">
        <mods:place>
          <mods:placeTerm type="text">
            <xsl:value-of select="./metadata/def.provenance/provenance" />
          </mods:placeTerm>
        </mods:place>
      </xsl:if>
      <xsl:if test="./metadata/def.unitdate/unitdate">
        <mods:dateIssued>
          <xsl:value-of select="./metadata/def.unitdate/unitdate" />
        </mods:dateIssued>
      </xsl:if>
    </mods:originInfo>
    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.note/note">
      <mods:note>
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>
    <xsl:apply-templates select="." mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_imgitem_')]" priority="1" mode="metsmeta">
    <mods:identifier>
      <xsl:choose>
        <xsl:when test="./metadata/def.shelfmark/shelfmark">
          <xsl:attribute name="type">shelfmark</xsl:attribute>
          <xsl:value-of select="./metadata/def.shelfmark/shelfmark[1]" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>
    <xsl:for-each select="./metadata/def.unittitle/unittitle">
      <mods:titleInfo>
        <xsl:attribute name="type">
          <xsl:choose>
            <xsl:when test="@type ='proper'">
              <xsl:value-of select="'uniform'" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'alternative'" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <mods:title>
          <xsl:value-of select="." />
        </mods:title>
      </mods:titleInfo>
    </xsl:for-each>
    <mods:originInfo>
      <xsl:if test="./metadata/def.provenance/provenance">
        <mods:place>
          <mods:placeTerm type="text">
            <xsl:value-of select="./metadata/def.provenance/provenance" />
          </mods:placeTerm>
        </mods:place>
      </xsl:if>
      <xsl:if test="./metadata/def.date/date">
        <mods:dateIssued>
          <xsl:value-of select="./metadata/def.date/date" />
        </mods:dateIssued>
      </xsl:if>
    </mods:originInfo>
    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
      <xsl:if test="./metadata/def.dimensions/dimensions">
        <mods:extent>
          <xsl:value-of select="./metadata/def.dimensions/dimensions" />
        </mods:extent>
      </xsl:if>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.iconography/iconography">
      <mods:abstract>
        <xsl:value-of select="./metadata/def.iconography/iconography" />
      </mods:abstract>
    </xsl:if>
    <xsl:if test="./metadata/def.note/note">
      <mods:note>
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>
    <xsl:apply-templates select="." mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_archivesource_')]" priority="1" mode="metsmeta">
  	<!-- identifier is the unitid or the MCR ID -->
    <mods:identifier>
      <xsl:choose>
        <xsl:when test="./metadata/def.unitid/unitid">
          <xsl:attribute name="type">unitid</xsl:attribute>
          <xsl:value-of select="./metadata/def.unitid/unitid[1]" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose> 
    </mods:identifier>
   
   <!-- the place is the resolved label of the component -->
   <mods:originInfoDefinition>
		<xsl:if test="./metadata/def.component/component">
		  <mods:place>
		  	<xsl:variable name="nodes" select="./metadata/def.component/component"/>
		  	<xsl:call-template name="resolveLabel">
		  		<xsl:with-param name="nodes" select="$nodes" />
		  	</xsl:call-template>
		  </mods:place>
		</xsl:if>
   </mods:originInfoDefinition>
   
   <mods:typeOfResource>
   
   </mods:typeOfResource>
   
    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
      
      	<xsl:if test="./metadata/def.type/type">
		  <mods:form>
		  	<xsl:variable name="nodes" select="./metadata/def.type/type"/>
		  	<xsl:call-template name="resolveLabel">
		  		<xsl:with-param name="nodes" select="$nodes" />
		  	</xsl:call-template>
		  </mods:form>
		</xsl:if>
    </mods:physicalDescription>
    
    
    <!-- if the note is not empty -->
    <xsl:if test="./metadata/def.note/note">
      <mods:note>
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>
    
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

  <xsl:template match="entitylink" mode="personal">
    <xsl:variable name="sourceCatalog">
      <xsl:choose>
        <xsl:when test="../../def.source/source[@type='catalog']">
          <xsl:value-of select="../../def.source/source[@type='catalog']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$ACTUAL.OPAC.CATALOG" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <mods:name>
      <xsl:attribute name="type">
        <xsl:choose>
        <xsl:when test="contains(@xlink:href,'_corporation_')">
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
          <xsl:if test="string-length($sourceCatalog)>5">
            <xsl:attribute name="authorityURI">
              <xsl:value-of select="$sourceCatalog" />
            </xsl:attribute>
            <xsl:attribute name="valueURI">
              <xsl:call-template name="generateCatalogURL">
                <xsl:with-param name="catalog" select="$sourceCatalog" />
                <xsl:with-param name="ppn" select="$personObj/mycoreobject/metadata/def.identifier/identifier[@type='ppn']" />
              </xsl:call-template>
            </xsl:attribute>
          </xsl:if>
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

  <xsl:template match="entitylink/@type" mode="marcrelator">
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
    <xsl:param name="catalog" select="$ACTUAL.OPAC.CATALOG" />
    <xsl:param name="ppn" />
    <xsl:if test="string-length($catalog) &gt; 5">
      <xsl:value-of select="concat($catalog,'PPN?PPN=',$ppn)" />
    </xsl:if>
  </xsl:template>

  <xsl:template name="resolveLabel">
  	<xsl:param name="nodes" />
  	<xsl:for-each select="$nodes">
		    <!-- for the moment only local classification supported -->
		    <xsl:variable name="classLink" select="concat('classification:metadata:0:parents:',@classid,':',@categid)"/>
		    <xsl:variable name="classInfo" select="document($classLink)"/>
		    <xsl:for-each select="$classInfo/descendant::category">
		    	<xsl:choose>
		      <xsl:when test="label/@description">
		      	<xsl:value-of select="label/@description"/>
		      </xsl:when>
		      <xsl:otherwise>
		      	<xsl:value-of select="label/@text"/>
		      </xsl:otherwise>
		    	</xsl:choose>
		    	<xsl:if test="position() != last()">
		      <xsl:value-of select="'&#x00BB;'"/>
		    	</xsl:if>
		    </xsl:for-each>
	</xsl:for-each>
  </xsl:template>

</xsl:stylesheet>