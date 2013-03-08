<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan">

  <xsl:template match="gbv-searchbar">
    <xsl:call-template name="gbv-searchbar" />
  </xsl:template>

  <xsl:template name="gbv-searchbar">
    <xsl:variable name="searchBarMode">
      <xsl:variable name="controllerHook">
        <jpsearchBar mode="{$mode}" />
      </xsl:variable>
      <xsl:apply-templates mode="controllerHook" select="xalan:nodeset($controllerHook)/jpsearchBar" />
    </xsl:variable>

    <xsl:if test="$showSearchBar">
      <xsl:variable name="searchBar" select="xalan:nodeset($searchBarMode)" />
      <xsl:choose>
        <xsl:when test="$searchBar/div[@id='searchBar']">
          <xsl:copy-of select="$searchBar/div[@id='searchBar']" />
        </xsl:when>
        <xsl:otherwise>
          <form id="searchForm" class="searchbarContainer" action="/jp-search.xml">
            <xsl:variable name="queryterm">
              <xsl:if test="$qt != '*' and $mode != 'hidden'">
                <xsl:value-of select="$qt" />
              </xsl:if>
            </xsl:variable>
            <xsl:variable name="journalID">
              <xsl:call-template name="getJournalID" />
            </xsl:variable>
            <xsl:choose>
              <xsl:when test="$journalID != ''">
                <input id="inputField" name="XSL.qt" value="{$queryterm}" class="searchbar" placeholder="Zeitschrift durchsuchen" title="Suche innerhalb der Zeitschrift"></input>
                <input type="hidden" name="XSL.searchjournalID" value="{$journalID}" />
              </xsl:when>
              <xsl:otherwise>
                <input id="inputField" name="XSL.qt" value="{$queryterm}" class="searchbar" placeholder="Suche im Gesamtbestand" title="Suche im Gesamtbestand"></input>
              </xsl:otherwise>
            </xsl:choose>
            <div class="submitContainer">
              <input type="submit" name="" value="" />
            </div>
          </form>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>