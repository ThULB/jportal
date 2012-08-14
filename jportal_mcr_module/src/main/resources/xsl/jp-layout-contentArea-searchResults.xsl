<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/">

  <xsl:template mode="searchResults" match="/mcr:results">
    <div class="jp-layout-marginLR">
      <div id="resultListHeader" class="jp-layout-bottomline jp-layout-border-light">
        <h2>
          Suchergebnisse
        </h2>
        <span>
          <xsl:apply-templates mode="searchResultText" select="." />
        </span>
      </div>
      <ul class="jp-layout-list-nodecoration">
        <xsl:apply-templates mode="searchResults" select="mcr:hit" />
      </ul>
      <xsl:if test="@numPages > 1">
        <div id="resultPaginator" class="jp-layout-topline jp-layout-border-light">
          <span class="jp-layout-label">
            Ergebnisseiten:
          </span>
          <menu class="jp-layout-paginator jp-layout-horiz-menu jp-layout-inline">
            <xsl:call-template name="createResultPages">
              <xsl:with-param name="numPages" select="@numPages" />
              <xsl:with-param name="i" select="1" />
            </xsl:call-template>
          </menu>
        </div>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template mode="searchResultText" match="mcr:results[@numHits = 0]">
    <xsl:message>
      num hits 1
    </xsl:message>
  </xsl:template>

  <xsl:template mode="searchResultText" match="mcr:results[@numHits = 1]">
    <xsl:value-of select="concat('Etwa ', @numHits, ' Ergebniss für &quot;', $qt, '&quot; gefunden.')" />
  </xsl:template>

  <xsl:template mode="searchResultText" match="mcr:results[@numHits > 1]">
    <xsl:value-of select="concat('Etwa ', @numHits, ' Ergebnisse für &quot;', $qt, '&quot; gefunden. (Seite ', @page, ')')" />
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
    <li>
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
      <xsl:call-template name="searchBreadcrumb">
        <xsl:with-param name="objID" select="../../@id" />
      </xsl:call-template>
    </div>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="mcr:field[@name='objectType' and text() = 'jparticle']">
    <div>
      Artikel erschienen in:
      <xsl:call-template name="searchBreadcrumb">
        <xsl:with-param name="objID" select="../../@id" />
      </xsl:call-template>
    </div>
  </xsl:template>
</xsl:stylesheet>