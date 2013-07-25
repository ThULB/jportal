<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:jpxml="xalan://org.mycore.common.xml.MCRJPortalXMLFunctions" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xalan mcrxml jpxml solrxml i18n">

  <xsl:param name="returnURL" />

  <xsl:template match="/response">
    <div id="searchResults">
      <div id="resultListHeader" class="jp-layout-bottomline jp-layout-border-light">
        <h2>Suchergebnisse</h2>
        <div>
          <span>
            <xsl:apply-templates mode="searchResultText" select="." />
          </span>
          <xsl:call-template name="jpsearch.printNavigation" />
        </div>
      </div>
      <div id="resultList">
        <xsl:apply-templates mode="resultList" select="." />
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="searchResultText" match="response">
    <xsl:value-of select="'Ihre Suche ergab keine Treffer.'" />
  </xsl:template>

  <xsl:template mode="searchResultText" match="response[result/@numFound = 1]">
    <xsl:value-of select="'Ein Ergebnis gefunden.'" />
  </xsl:template>

  <xsl:template mode="searchResultText" match="response[result/@numFound &gt; 1]">
    <xsl:value-of select="concat($resultInfo/numFound, ' Ergebnisse gefunden.')" />
    <xsl:if test="$resultInfo/page > 0">
      <xsl:value-of select="concat(' (Seite ', $resultInfo/page + 1, ')')" />
    </xsl:if>
  </xsl:template>

  <xsl:template name="jpsearch.printNavigation">
    <ul class="navigation">
      <xsl:choose>
        <xsl:when test="$searchMode != 'advanced'">
          <li>
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="concat($WebApplicationBaseURL, 'jp-advancedsearch.xml')" />
                <xsl:if test="$journalID != ''">
                  <xsl:value-of select="concat('?journalID=', $journalID)" />
                </xsl:if>
              </xsl:attribute>
              <xsl:value-of select="'Erweiterte Suche'" />
            </a>
          </li>
        </xsl:when>
        <xsl:otherwise>
          <li>
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="concat($WebApplicationBaseURL, 'jp-advancedsearch.xml?')" />
                <xsl:value-of select="concat('XSL.field1=', $field1, '&amp;XSL.value1=', $value1)" />
                <xsl:value-of select="concat('&amp;XSL.field2=', $field2, '&amp;XSL.value2=', $value2)" />
                <xsl:value-of select="concat('&amp;XSL.field3=', $field3, '&amp;XSL.value3=', $value3)" />
                <xsl:if test="$journalID != ''">
                  <xsl:value-of select="concat('&amp;journalID=', $journalID)" />
                </xsl:if>
              </xsl:attribute>
              <xsl:value-of select="'Erweiterte Suche bearbeiten'" />
            </a>
          </li>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="$journalID != ''">
        <li>
          <a href="/receive/{$journalID}">Zurück zur Zeitschrift</a>
        </li>
        <li>
          <a>
            <xsl:attribute name="href">
              <xsl:variable name="requestUrlWithoutJournalID">
                <xsl:call-template name="UrlDelParam">
                  <xsl:with-param name="url" select="$RequestURL" />
                  <xsl:with-param name="par" select="'journalID'" />
                </xsl:call-template>
              </xsl:variable>
              <xsl:call-template name="UrlDelParam">
                <xsl:with-param name="url" select="$requestUrlWithoutJournalID" />
                <xsl:with-param name="par" select="'fq'" />
              </xsl:call-template>
            </xsl:attribute>
            <xsl:value-of select="'Im Gesamtbestand suchen'" />
          </a>
        </li>
      </xsl:if>
      <xsl:if test="$returnURL">
        <li>
          <a href="{$returnURL}">Zurück</a>
        </li>
      </xsl:if>
    </ul>
  </xsl:template>

  <xsl:template mode="resultList" match="response[result/@numFound = 0]">
    <p>
      Vorschläge:
      <ul>
        <li>Achten Sie darauf, dass alle Wörter richtig geschrieben sind.</li>
        <li>Probieren Sie es mit anderen Suchbegriffen.</li>
        <li>Probieren Sie es mit allgemeineren Suchbegriffen.</li>
        <li>Probieren Sie es mit weniger Suchbegriffen.</li>
      </ul>
    </p>
  </xsl:template>

  <xsl:template mode="resultList" match="response[result/@numFound &gt;= 1]">
    <xsl:variable name="showFacet" select="lst[@name='facet_counts']" />
    <xsl:variable name="resultListClass">
      <xsl:if test="not($showFacet)">
        <xsl:value-of select="'max-width'" />
      </xsl:if>
    </xsl:variable>
    <ul class="jp-layout-list-nodecoration {$resultListClass}">
      <xsl:apply-templates mode="searchResults" select="result/doc" />
    </ul>
    <xsl:if test="$showFacet">
      <div class="jp-layout-facet-list">
        <xsl:apply-templates mode="facetList" select="lst[@name='facet_counts']/lst" />
      </div>
    </xsl:if>
    <div class="clear" />
    <xsl:apply-templates mode="pagination" select="." />
  </xsl:template>

  <xsl:variable name="searchResultsFields">
    <field name="objectType" />
    <field name="published" label="Erschienen" />
    <field name="published_from" label="Erschienen" />
    <field name="dateOfBirth" label="Geburtsdatum" />
    <field name="dateOfDeath" label="Sterbedatum" />
    <field name="participant.author" label="Autor" />
    <field name="date.published" label="Erschienen" />
    <field name="date.published_Original" label="Erscheinungsjahr des rez. Werkes" />
    <field name="date.published_Original_From" label="Erscheinungsbeginn der rez. Werke" />
    <field name="date.published_Original_Till" label="Erscheinungsende der rez. Werke" />
    <field name="size" label="Seitenbereich" />
    <field name="rubric" label="Rubrik" />
  </xsl:variable>

  <xsl:template mode="searchResults" match="doc">
    <xsl:variable name="mcrId" select="str[@name='id']" />
    <xsl:choose>
      <xsl:when test="mcrxml:exists($mcrId)">
        <li class="jp-layout-topline jp-layout-border-light">
          <div class="metadata">
            <xsl:apply-templates mode="searchHitLabel" select="." />
            <ul class="jp-layout-metadaInSearchResults">
              <xsl:variable name="doc" select="." />
              <xsl:for-each select="xalan:nodeset($searchResultsFields)/field">
                <xsl:variable name="fieldName" select="@name" />
                <xsl:if test="$doc/*[@name = $fieldName]">
                  <li>
                    <xsl:if test="@label">
                      <span class="jp-layout-label">
                        <xsl:value-of select="@label" />
                      </span>
                    </xsl:if>
                    <xsl:apply-templates mode="searchHitDataField" select="$doc/*[@name = $fieldName]" />
                  </li>
                </xsl:if>
              </xsl:for-each>
            </ul>
          </div>
          <xsl:variable name="mcrObj" select="document(concat('mcrobject:', $mcrId))/mycoreobject" />
          <xsl:call-template name="derivatePreview">
            <xsl:with-param name="mcrObj" select="$mcrObj" />
          </xsl:call-template>
        </li>
      </xsl:when>
      <xsl:otherwise>
        <!-- object doesn't exist in mycore -> delete it in solr -->
        <xsl:value-of select="solrxml:delete($mcrId)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="searchHitLabel" match="doc">
    <h3 class="jp-layout-clickLabel">
      <a href="/receive/{str[@name='id']}">
        <xsl:apply-templates mode="searchHitLabelText" select="." />
      </a>
    </h3>
  </xsl:template>

  <xsl:template mode="searchHitLabelURL" match="searchHitLabelURL[subselectEnd]">
    <xsl:value-of select="subselectEnd/@value" />
    <xsl:apply-templates mode="urlParam" select="param" />
  </xsl:template>
  
  <xsl:template mode="urlParam" match="param" >
    <xsl:choose>
      <xsl:when test="position()=1">
        <xsl:value-of select="'?'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'&amp;'"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="concat(@name,'=',@value)"/>
  </xsl:template>

  <xsl:template mode="searchHitLabelText" match="doc[contains('jpjournal jpvolume jparticle', str[@name='objectType'])]">
    <xsl:value-of select="str[@name='maintitle']" />
  </xsl:template>

  <xsl:template mode="searchHitLabelText" match="doc[contains('person jpinst', str[@name='objectType'])]">
    <xsl:value-of select="str[@name='heading']" />
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str">
    <span class="jp-layout-inList">
      <xsl:value-of select="." />
    </span>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="arr[@name='participant.author']/str">
    <span class="jp-layout-inList">
      <a href="{$WebApplicationBaseURL}receive/{substring-before(., '#')}">
        <xsl:value-of select="substring-after(., '#')" />
      </a>
    </span>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="arr[@name='rubric']/str">
    <xsl:variable name="category">
      <categ classid="{substring-before(.,'#')}" categid="{substring-after(.,'#')}" />
    </xsl:variable>
    <span class="jp-layout-inList">
      <xsl:call-template name="printClass">
        <xsl:with-param name="nodes" select="xalan:nodeset($category)/categ" />
        <xsl:with-param name="host" select="'local'" />
        <xsl:with-param name="next" select="', '" />
      </xsl:call-template>
    </span>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'person']">
    <xsl:value-of select="'Person'" />
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jpinst']">
    <xsl:value-of select="'Institution'" />
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jpjournal']">
    <xsl:value-of select="'Zeitschrift'" />
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jpvolume']">
    <span class="jp-layout-label">
      <xsl:value-of select="'Band erschienen in'" />
    </span>
    <xsl:call-template name="resultListBreadcrumb">
      <xsl:with-param name="objID" select="../str[@name='id']" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jparticle']">
    <span class="jp-layout-label">
      <xsl:value-of select="'Artikel erschienen in'" />
    </span>
    <xsl:call-template name="resultListBreadcrumb">
      <xsl:with-param name="objID" select="../str[@name='id']" />
    </xsl:call-template>
  </xsl:template>
  
  <!-- *************************************************** -->
  <!-- * FACET -->
  <!-- *************************************************** -->
  <xsl:template mode="facetList" match="lst">
  </xsl:template>
  
  <xsl:template mode="facetList" match="lst[@name='facet_fields']">
    <h3><xsl:value-of select="i18n:translate('jp.metadata.facet.intro')" /></h3>
    <xsl:apply-templates mode="facetGroup" select="lst" />
  </xsl:template>

  <xsl:template mode="facetGroup" match="lst">
    <div class="group">
      <span class="groupName">
        <xsl:value-of select="i18n:translate(concat('jp.metadata.facet.', @name))" />
      </span>
      <table>
        <xsl:apply-templates select="int" mode="facetField">
          <xsl:with-param name="facet" select="@name"/>
        </xsl:apply-templates>
      </table>
    </div>
  </xsl:template>

  <xsl:template mode="facetField" match="int">
    <xsl:param name="facet" />
    <xsl:variable name="value" select="@name" />
    <xsl:variable name="count" select="text()" />

    <xsl:variable name="selected" select="jpxml:isFacetSelected($RequestURL, $facet, $value)" />
    <xsl:variable name="href">
      <xsl:choose>
        <xsl:when test="$selected">
          <xsl:value-of select="jpxml:removeFacet($RequestURL, $facet, $value)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat($RequestURL, '&amp;fq=', $facet, ':', $value)" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="class">
      <xsl:if test="$selected">
        <xsl:value-of select="'selected'" />
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="text">
      <xsl:variable name="facetSettings" select="$settings/facet[@name=$facet]" />
      <xsl:choose>
        <xsl:when test="$facetSettings/@translate = 'true'">
          <xsl:value-of select="i18n:translate(concat('jp.metadata.facet.', $facet, '.', $value))" />
        </xsl:when>
        <xsl:when test="$facetSettings/@mcrid = 'true'">
          <xsl:variable name="mcrObj" select="document(concat('mcrobject:', $value))/mycoreobject" />
          <xsl:apply-templates mode="printTitle"
            select="$mcrObj/metadata/maintitles/maintitle[@inherited='0']|$mcrObj/metadata/def.heading/heading|$mcrObj/metadata/names[@class='MCRMetaInstitutionName']/name" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$value" />
        </xsl:otherwise>      
      </xsl:choose>
    </xsl:variable>

    <tr>
      <td class="text">
        <a href="{$href}">
          <i class="icon {$class}"></i>
          <span class="text">
            <xsl:value-of select="$text" />
          </span>
        </a>
      </td>
      <td class="count">
        <xsl:value-of select="concat(' (', $count, ')')" />
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>