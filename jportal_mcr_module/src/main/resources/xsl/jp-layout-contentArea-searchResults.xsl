<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/"
  xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template mode="searchResults" match="/mcr:results">
    <div id="searchResults">
      <div id="resultListHeader" class="jp-layout-bottomline jp-layout-border-light">
        <h2>
          Suchergebnisse
        </h2>
        <span>
          <xsl:apply-templates mode="searchResultText" select="." />
        </span>
      </div>
      <div id="resultList">
        <xsl:apply-templates mode="resultList" select="." />
      </div>
      <div id="searchRightNavi">
        <ul>
          <xsl:if test="$searchjournalID != ''">
            <li>
              <a href="/receive/{$searchjournalID}">
                &gt; Zurück zur Zeitschrift
              </a>
            </li>
          </xsl:if>
          <li>
            &gt; Erweiterte Suche
          </li>
        </ul>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="searchResultText" match="mcr:results[@numHits = 1]">
    <xsl:value-of select="concat('Etwa ', @numHits, ' Ergebniss für &quot;', $qt, '&quot; gefunden.')" />
  </xsl:template>

  <xsl:template mode="searchResultText" match="mcr:results[@numHits > 1]">
    <xsl:value-of select="concat('Etwa ', @numHits, ' Ergebnisse für &quot;', $qt, '&quot; gefunden.')" />
    <xsl:if test="@numPages > 1">
      <xsl:value-of select="concat(' (Seite ', @page, ')')" />
    </xsl:if>
  </xsl:template>

  <xsl:template name="createResultPages">
    <xsl:param name="numPages" />
    <xsl:param name="i" />
    <xsl:if test="$i &lt;= $numPages">
      <li>
        <xsl:if test="$i = /mcr:results/@page">
          <xsl:attribute name="class">
            <xsl:value-of select="'jp-layout-selected-underline'" />
          </xsl:attribute>
        </xsl:if>
        <a href="/jp-search.xml?XSL.qt={$qt}&amp;XSL.searchjournalID={$searchjournalID}&amp;XSL.resultpage={$i}">
          <xsl:value-of select="$i" />
        </a>
      </li>
      <xsl:call-template name="createResultPages">
        <xsl:with-param name="numPages" select="@numPages" />
        <xsl:with-param name="i" select="$i+1" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="resultList" match="mcr:results[@numHits = 0]">
    <p>
      Ihre Suche für
      <b>
        &quot;
        <xsl:value-of select="$qt" />
        &quot;
      </b>
      ergab keine Treffer.
    </p>
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

  <xsl:template mode="resultList" match="mcr:results[@numHits &gt; 1]">
    <ul class="jp-layout-list-nodecoration">
      <xsl:apply-templates mode="searchResults" select="mcr:hit" />
    </ul>
    <xsl:if test="@numPages > 1">
      <div id="resultPaginator" class="jp-layout-topline jp-layout-border-light">
        <menu class="jp-layout-paginator jp-layout-horiz-menu jp-layout-inline">
          <li>
            <a href="/jp-search.xml?XSL.qt={$qt}&amp;XSL.searchjournalID={$searchjournalID}&amp;XSL.resultpage={@page -1}">&lt; Zurück</a>
          </li>
          <xsl:call-template name="createResultPages">
            <xsl:with-param name="numPages" select="@numPages" />
            <xsl:with-param name="i" select="1" />
          </xsl:call-template>
          <li>
            <a href="/jp-search.xml?XSL.qt={$qt}&amp;XSL.searchjournalID={$searchjournalID}&amp;XSL.resultpage={@page +1}">Weiter &gt;</a>
          </li>
        </menu>
      </div>
    </xsl:if>
  </xsl:template>
  
  <xsl:variable name="searchResultsFields">
    <field name="objectType" />
    <field name="published" label="Erschienen"/>
    <field name="published_from" label="Erschienen"/>
    <!-- 
    <field name="participants_xlinkTitle" label="Autor" />
     -->
    <field name="participants_withName" label="Autor" />
    <field name="published_art" label="Erschienen" />
    <field name="published_orig_art" label="Erscheinungsjahr des rez. Werkes" />
    <field name="published_orig_from_art" label="Erscheinungsbeginn der rez. Werke" />
    <field name="published_orig_till_art" label="Erscheinungsende der rez. Werke" />
    <field name="sizes_art" label="Seitenbereich" />
    <field name="rubrics_id" label="Rubrik" />
  </xsl:variable>

  <xsl:template mode="searchResults" match="mcr:hit">
    <li class="jp-layout-topline jp-layout-border-light">
      <xsl:apply-templates mode="searchHitLabel" select="." />

      <ul class="jp-layout-metadaInSearchResults">
        <xsl:variable name="mcrHit" select="." />
        <xsl:for-each select="xalan:nodeset($searchResultsFields)/field">
          <xsl:variable name="fieldName" select="@name" />
          <xsl:if test="$mcrHit/mcr:metaData/mcr:field[@name = $fieldName]">
            <li>
              <xsl:if test="@label">
                <span class="jp-layout-label">
                  <xsl:value-of select="@label" />
                </span>
              </xsl:if>
              <xsl:apply-templates mode="searchHitDataField" select="$mcrHit/mcr:metaData/mcr:field[@name = $fieldName]" />
            </li>
          </xsl:if>
        </xsl:for-each>
      </ul>
      
      <xsl:call-template name="derivateDisplay">
          <xsl:with-param name="nodes" select="mcr:metaData/mcr:field[@name='linkDeriv']" />
        </xsl:call-template>
    </li>
  </xsl:template>

  <xsl:template mode="searchHitLabel" match="mcr:hit">
    <h3 class="jp-layout-clickLabel">
      <a href="/receive/{@id}">
        <xsl:apply-templates mode="searchHitLabelText" select="." />
      </a>
    </h3>
  </xsl:template>

  <xsl:template mode="searchHitLabelText" match="mcr:hit[contains('jpjournal jpvolume jparticle', mcr:metaData/mcr:field[@name='objectType'])]">
    <xsl:value-of select="mcr:metaData/mcr:field[@name='maintitles_plain']" />
  </xsl:template>

  <xsl:template mode="searchHitLabelText" match="mcr:hit[mcr:metaData/mcr:field[@name='objectType'] = 'person']">
    <xsl:value-of select="concat(mcr:metaData/mcr:field[@name='headingFirstName'],' ', mcr:metaData/mcr:field[@name='headingLastName'])" />
  </xsl:template>

  <xsl:template mode="searchHitLabelText" match="mcr:hit[mcr:metaData/mcr:field[@name='objectType'] = 'jpinst']">
    <xsl:value-of select="mcr:metaData/mcr:field[@name='instname']" />
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field">
    <span class="jp-layout-inList">
      <xsl:value-of select="." />
    </span>
  </xsl:template>
  
  <xsl:template mode="searchHitDataField" match="mcr:field[@name='participants_withName']">
    <span class="jp-layout-inList">
      <a href="{$WebApplicationBaseURL}receive/{substring-before(.,'#')}">
        <xsl:value-of select="substring-after(.,'#')" />
      </a>
    </span>
  </xsl:template>
  
  <xsl:template mode="searchHitDataField" match="mcr:field[@name='rubrics_id']">
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

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='objectType' and text() = 'person']">
    Person
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='objectType' and text() = 'jpinst']">
    Institution
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='objectType' and text() = 'jpjournal']">
    Zeitschrift
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='objectType' and text() = 'jpvolume']">
    <span class="jp-layout-label">
      Band erschienen in
    </span>
    <xsl:call-template name="resultListBreadcrumb">
      <xsl:with-param name="objID" select="../../@id" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='objectType' and text() = 'jparticle']">
    <span class="jp-layout-label">
      Artikel erschienen in
    </span>
    <xsl:call-template name="resultListBreadcrumb">
      <xsl:with-param name="objID" select="../../@id" />
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>