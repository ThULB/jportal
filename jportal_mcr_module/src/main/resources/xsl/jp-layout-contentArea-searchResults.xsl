<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/">

  <xsl:template mode="searchResults" match="/mcr:results">
    <div id="searchResults" >
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

  <xsl:template mode="resultList" match="mcr:results[@numHits = 0]">
    <p>
    Ihre Suche für <b>&quot;<xsl:value-of select="$qt"/>&quot;</b> ergab keine Treffer.
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

  <xsl:template mode="searchResults" match="mcr:hit">
    <li class="jp-layout-topline jp-layout-border-light">
      <xsl:apply-templates mode="searchHitDataField" select="mcr:metaData/mcr:field" />
    </li>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field" />

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='maintitles_plain']">
    <h3 class="jp-layout-clickLabel">
      <a href="/receive/{../../@id}">
        <xsl:value-of select="text()" />
      </a>
    </h3>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='published' or @name='published_from']">
    <div>
      Erschienen:
      <xsl:value-of select="text()" />
    </div>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='participants_xlinkTitle']">
    <div>
      Autor:
      <xsl:value-of select="text()" />
    </div>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='objectType' and text() = 'jpjournal']">
    <div>
      Zeitschrift
    </div>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='objectType' and text() = 'jpvolume']">
    <div>
      Band erschienen in:
      <xsl:call-template name="resultListBreadcrumb">
        <xsl:with-param name="objID" select="../../@id" />
      </xsl:call-template>
    </div>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='objectType' and text() = 'jparticle']">
    <div>
      Artikel erschienen in:
      <xsl:call-template name="resultListBreadcrumb">
        <xsl:with-param name="objID" select="../../@id" />
      </xsl:call-template>
    </div>
  </xsl:template>
</xsl:stylesheet>