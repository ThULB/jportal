<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/"
  xmlns:xalan="http://xml.apache.org/xalan">

  <!-- For Subselect -->
  <xsl:param name="subselect.type" select="''" />
  <xsl:param name="subselect.session" select="''" />
  <xsl:param name="subselect.varpath" select="''" />
  <xsl:param name="subselect.webpage" select="''" />

  <xsl:template name="jpsearch.getResultInfo">
    <xsl:param name="response" />
    <xsl:variable name="start" select="result/@start" />
    <xsl:variable name="rows" select="lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']" />
    <xsl:variable name="numFound" select="result/@numFound" />
    <numFound>
      <xsl:value-of select="$numFound" />
    </numFound>
    <start>
      <xsl:value-of select="$start" />
    </start>
    <rows>
      <xsl:value-of select="$rows" />
    </rows>
    <page>
      <xsl:value-of select="ceiling($start div $rows)" />
    </page>
    <pages>
      <xsl:value-of select="ceiling($numFound div $rows)" />
    </pages>
  </xsl:template>

  <xsl:template mode="searchResults" match="/response">
    <xsl:variable name="resultInfoXML">
      <xsl:call-template name="jpsearch.getResultInfo">
        <xsl:with-param name="repsonse" select="." />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="resultInfo" select="xalan:nodeset($resultInfoXML)" />

    <div id="searchResults">
      <div id="resultListHeader" class="jp-layout-bottomline jp-layout-border-light">
        <h2>Suchergebnisse</h2>
        <div>
          <xsl:apply-templates mode="searchResultText" select=".">
            <xsl:with-param name="resultInfo" select="$resultInfo" />
          </xsl:apply-templates>
        </div>
      </div>
      <div id="resultList">
        <xsl:apply-templates mode="resultList" select=".">
          <xsl:with-param name="resultInfo" select="$resultInfo" />
        </xsl:apply-templates>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="advancedSearchResults" match="/response">
    <xsl:variable name="resultInfoXML">
      <xsl:call-template name="jpsearch.getResultInfo">
        <xsl:with-param name="repsonse" select="." />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="resultInfo" select="$resultInfoXML" />

    <div id="searchResults">
      <div id="resultListHeader" class="jp-layout-bottomline jp-layout-border-light">
        <h2>Suchergebnisse</h2>
        <div>
          <xsl:apply-templates mode="advancedSearchResultText" select=".">
            <xsl:with-param name="resultInfo" select="$resultInfo" />
          </xsl:apply-templates>
        </div>
      </div>
      <div id="resultList">
        <xsl:apply-templates mode="resultList" select=".">
          <xsl:with-param name="resultInfo" select="$resultInfo" />
        </xsl:apply-templates>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="searchResultText" match="response">
    <span>
      Ihre Suche für
      <b>
        &quot;
        <xsl:value-of select="$qt" />
        &quot;
      </b>
      ergab keine Treffer.
    </span>
    <xsl:call-template name="jpsearch.printNavigation" />
  </xsl:template>

  <xsl:template mode="searchResultText" match="response[result/@numFound = 1]">
    <span>
      <xsl:value-of select="concat('Ein Ergebnis für &quot;', $qt, '&quot; gefunden.')" />
    </span>
    <xsl:call-template name="jpsearch.printNavigation" />
  </xsl:template>

  <xsl:template mode="searchResultText" match="response[result/@numFound &gt; 1]">
    <xsl:param name="resultInfo" />
    <span>
      <xsl:value-of select="concat('Etwa ', $resultInfo/numFound, ' Ergebnisse für &quot;', $qt, '&quot; gefunden.')" />
      <xsl:if test="$resultInfo/page > 1">
        <xsl:value-of select="concat(' (Seite ', $resultInfo/page, ')')" />
      </xsl:if>
    </span>
    <xsl:call-template name="jpsearch.printNavigation" />
  </xsl:template>

  <xsl:template mode="advancedSearchResultText" match="response">
    <span>Ihre Suche ergab keine Treffer.</span>
    <xsl:call-template name="jpsearch.printNavigation">
      <xsl:with-param name="adv" select="true()" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="advancedSearchResultText" match="response[result/@numFound = 1]">
    <span>
      <xsl:value-of select="'Ein Ergebnis gefunden.'" />
    </span>
    <xsl:call-template name="jpsearch.printNavigation">
      <xsl:with-param name="adv" select="true()" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="advancedSearchResultText" match="response[result/@numFound &gt; 1]">
    <xsl:param name="resultInfo" />
    <span>
      <xsl:value-of select="concat($resultInfo/numFound, ' Ergebnisse gefunden.')" />
      <xsl:if test="$resultInfo/page > 1">
        <xsl:value-of select="concat(' (Seite ', $resultInfo/page, ')')" />
      </xsl:if>
    </span>
    <xsl:call-template name="jpsearch.printNavigation">
      <xsl:with-param name="adv" select="true()" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="jpsearch.printNavigation">
    <xsl:param name="adv" select="false()" />
    <ul class="navigation">
      <xsl:if test="not($adv)">
        <li>
          <a href="{concat($WebApplicationBaseURL, 'jp-advancedsearch.xml?XSL.searchjournalID=', $searchjournalID)}">Erweiterte Suche</a>
        </li>
      </xsl:if>
      <xsl:if test="$adv">
        <xsl:variable name="editAdvSearchURL">
          <xsl:call-template name="UrlDelParam">
            <xsl:with-param name="url" select="$RequestURL" />
            <xsl:with-param name="par" select="'XSL.mode'" />
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$editAdvSearchURL}">Erweiterte Suche bearbeiten</a>
        </li>
      </xsl:if>
      <xsl:if test="$searchjournalID != ''">
        <li>
          <a href="/receive/{$searchjournalID}">Zurück zur Zeitschrift</a>
        </li>
        <xsl:variable name="searchAll">
          <xsl:call-template name="UrlDelParam">
            <xsl:with-param name="url" select="$RequestURL" />
            <xsl:with-param name="par" select="'XSL.searchjournalID'" />
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$searchAll}">Im Gesamtbestand suchen</a>
        </li>
      </xsl:if>
    </ul>
  </xsl:template>

  <xsl:template name="createResultPages">
    <xsl:param name="resultInfo" />
    <xsl:param name="pageEnd" />
    <xsl:param name="i" />
    
    <xsl:if test="$i &lt;= $resultInfo/pages and $i &lt;= $pageEnd">
      <li>
        <xsl:if test="$i = $resultInfo/page + 1">
          <xsl:attribute name="class">
            <xsl:value-of select="'jp-layout-selected-underline'" />
          </xsl:attribute>
        </xsl:if>
        <xsl:variable name="subSelectParam">
          <xsl:if test="$subselect.type != ''">
            <xsl:value-of select="concat('&amp;XSL.subselect.type=',$subselect.type)" />
            <xsl:value-of select="concat('&amp;XSL.subselect.session.SESION=',$subselect.session)" />
            <xsl:value-of select="concat('&amp;XSL.subselect.varpath.SESION=',$subselect.varpath)" />
            <xsl:value-of select="concat('&amp;XSL.subselect.webpage.SESION=',$subselect.webpage)" />
          </xsl:if>
        </xsl:variable>
        <a href="/jp-search.xml?XSL.qt={$qt}&amp;XSL.searchjournalID={$searchjournalID}&amp;XSL.start={($i - 1) * $rows}{$subSelectParam}">
          <xsl:value-of select="$i" />
        </a>
      </li>
      <xsl:call-template name="createResultPages">
        <xsl:with-param name="resultInfo" select="$resultInfo" />
        <xsl:with-param name="pageEnd" select="$pageEnd" />
        <xsl:with-param name="i" select="$i+1" />
      </xsl:call-template>
    </xsl:if>
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
    <xsl:param name="resultInfo" />

    <ul class="jp-layout-list-nodecoration">
      <xsl:apply-templates mode="searchResults" select="result/doc" />
    </ul>
    <xsl:if test="$resultInfo/pages &gt; 1">
      <div id="resultPaginator" class="jp-layout-topline jp-layout-border-light">
        <xsl:variable name="start" select="$resultInfo/start" />
        <xsl:variable name="rows" select="$resultInfo/rows" />
        <xsl:variable name="numFound" select="$resultInfo/numFound" />
        <menu class="jp-layout-paginator jp-layout-horiz-menu jp-layout-inline">
          <xsl:if test="($start - $rows) &gt;= 0">
            <li>
              <a href="/jp-search.xml?XSL.qt={$qt}&amp;XSL.searchjournalID={$searchjournalID}&amp;XSL.start={$start - $rows}">&lt; Zurück</a>
            </li>
          </xsl:if>

          <xsl:variable name="pageStart">
            <xsl:choose>
              <xsl:when test="($resultInfo/page - 4) &gt; 1">
                <xsl:value-of select="$resultInfo/page - 4" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="1" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="pageEnd">
            <xsl:choose>
              <xsl:when test="($resultInfo/page + 5) &gt; 10">
                <xsl:value-of select="$resultInfo/page + 5" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="10" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <xsl:call-template name="createResultPages">
            <xsl:with-param name="resultInfo" select="$resultInfo" />
            <xsl:with-param name="i" select="$pageStart" />
            <xsl:with-param name="pageEnd" select="$pageEnd" />
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
    <field name="published" label="Erschienen" />
    <field name="published_from" label="Erschienen" />
    <!-- <field name="participants_xlinkTitle" label="Autor" /> -->
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