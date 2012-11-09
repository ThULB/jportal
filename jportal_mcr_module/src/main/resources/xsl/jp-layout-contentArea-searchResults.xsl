<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/"
  xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template mode="searchResults" match="/response" >
    <xsl:variable name="resultInfo">
      <xsl:variable name="start" select="result/@start" />
      <xsl:variable name="rows" select="lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']" />
      <xsl:variable name="numFound" select="result/@numFound" />
      <numFound><xsl:value-of select="$numFound" /></numFound>
      <start><xsl:value-of select="$start" /></start>
      <rows><xsl:value-of select="$rows" /></rows>
      <page><xsl:value-of select="round(($start div $rows) + 0.5)" /></page>
      <pages><xsl:value-of select="round(($numFound div $rows) + 0.5)" /></pages>
    </xsl:variable>

    <div id="searchResults">
      <div id="resultListHeader" class="jp-layout-bottomline jp-layout-border-light">
        <h2>
          Suchergebnisse
        </h2>
        <span>
          <xsl:apply-templates mode="searchResultText" select="." >
            <xsl:with-param name="resultInfo" select="$resultInfo" />
          </xsl:apply-templates>
        </span>
      </div>
      <div id="resultList">
        <xsl:apply-templates mode="resultList" select="." >
          <xsl:with-param name="resultInfo" select="$resultInfo" />
        </xsl:apply-templates>
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

  <xsl:template mode="searchResultText" match="response">
  </xsl:template>

  <xsl:template mode="searchResultText" match="response[result/@numFound = 1]">
    <xsl:value-of select="concat('Ein Ergebniss für &quot;', $qt, '&quot; gefunden.')" />
  </xsl:template>

  <xsl:template mode="searchResultText" match="response[result/@numFound &gt; 1]">
    <xsl:param name="resultInfo" />
    <xsl:value-of select="concat('Etwa ', xalan:nodeset($resultInfo)/numFound, ' Ergebnisse für &quot;', $qt, '&quot; gefunden.')" />
    <xsl:if test="xalan:nodeset($resultInfo)/page > 1">
      <xsl:value-of select="concat(' (Seite ', xalan:nodeset($resultInfo)/page, ')')" />
    </xsl:if>
  </xsl:template>

  <xsl:template name="createResultPages">
    <xsl:param name="resultInfo" />
    <xsl:param name="i" />
    <xsl:variable name="rows" select="xalan:nodeset($resultInfo)/rows" />
    <xsl:if test="$i &lt;= xalan:nodeset($resultInfo)/pages">
      <li>
        <xsl:if test="$i = xalan:nodeset($resultInfo)/page">
          <xsl:attribute name="class">
            <xsl:value-of select="'jp-layout-selected-underline'" />
          </xsl:attribute>
        </xsl:if>
        <a href="/jp-search.xml?XSL.qt={$qt}&amp;XSL.searchjournalID={$searchjournalID}&amp;XSL.start={($i - 1) * $rows}">
          <xsl:value-of select="$i" />
        </a>
      </li>
      <xsl:call-template name="createResultPages">
        <xsl:with-param name="resultInfo" select="$resultInfo" />
        <xsl:with-param name="i" select="$i+1" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="resultList" match="response[result/@numFound = 0]">
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

  <xsl:template mode="resultList" match="response[result/@numFound &gt;= 1]">
    <xsl:param name="resultInfo" />

    <ul class="jp-layout-list-nodecoration">
      <xsl:apply-templates mode="searchResults" select="result/doc" />
    </ul>
    <xsl:if test="xalan:nodeset($resultInfo)/pages &gt; 1">
      <div id="resultPaginator" class="jp-layout-topline jp-layout-border-light">
        <xsl:variable name="start" select="xalan:nodeset($resultInfo)/start" />
        <xsl:variable name="rows" select="xalan:nodeset($resultInfo)/rows" />
        <xsl:variable name="numFound" select="xalan:nodeset($resultInfo)/numFound" />
        <menu class="jp-layout-paginator jp-layout-horiz-menu jp-layout-inline">
          <xsl:if test="($start - $rows) &gt;= 0">
            <li>
              <a href="/jp-search.xml?XSL.qt={$qt}&amp;XSL.searchjournalID={$searchjournalID}&amp;XSL.start={$start - $rows}">&lt; Zurück</a>
            </li>
          </xsl:if>
          <xsl:call-template name="createResultPages">
            <xsl:with-param name="resultInfo" select="$resultInfo" />
            <xsl:with-param name="i" select="1" />
          </xsl:call-template>
          <xsl:if test="($start + $rows) &lt; $numFound">
            <li>
              <a href="/jp-search.xml?XSL.qt={$qt}&amp;XSL.searchjournalID={$searchjournalID}&amp;XSL.start={$start + $rows}">Weiter &gt;</a>
            </li>
          </xsl:if>
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
    <field name="participant" label="Autor" />
    <field name="date.published" label="Erschienen" />
    <field name="date.published_Original" label="Erscheinungsjahr des rez. Werkes" />
    <field name="date.published_Original_From" label="Erscheinungsbeginn der rez. Werke" />
    <field name="date.published_Original_Till" label="Erscheinungsende der rez. Werke" />
    <field name="size" label="Seitenbereich" />
    <field name="rubric" label="Rubrik" />
  </xsl:variable>

  <xsl:template mode="searchResults" match="doc">
    <li class="jp-layout-topline jp-layout-border-light">
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

      <!-- TODO: Derivate Linking -->
      <xsl:call-template name="derivateDisplay">
          <xsl:with-param name="nodes" select="mcr:metaData/mcr:field[@name='linkDeriv']" />
        </xsl:call-template>
    </li>
  </xsl:template>

  <xsl:template mode="searchHitLabel" match="doc">
    <h3 class="jp-layout-clickLabel">
      <a href="/receive/{str[@name='id']}">
        <xsl:apply-templates mode="searchHitLabelText" select="." />
      </a>
    </h3>
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

  <xsl:template mode="searchHitDataField" match="str[@name='participant']">
    <span class="jp-layout-inList">
      <a href="{$WebApplicationBaseURL}receive/{../str[@name='participantID']}">
        <xsl:value-of select="text()" />
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
</xsl:stylesheet>