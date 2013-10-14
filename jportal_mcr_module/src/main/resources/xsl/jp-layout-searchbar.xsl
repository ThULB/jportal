<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:decoder="xalan://java.net.URLDecoder" exclude-result-prefixes="xalan decoder">

  <xsl:variable name="qry">
    <xsl:variable name="encodedQuery">
      <xsl:call-template name="UrlGetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="'qry'" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="decoder:decode($encodedQuery, 'UTF-8')" />
  </xsl:variable>
  <xsl:variable name="showSearchBar">
    <xsl:apply-templates mode="showSearchBar" select="*" />
  </xsl:variable>
  <xsl:template match="*" mode="showSearchBar">
    <xsl:value-of select="'true'" />
  </xsl:template>
  <xsl:template match="MyCoReWebPage[section/jpadvancedsearch]" mode="showSearchBar">
    <xsl:value-of select="'false'" />
  </xsl:template>

  <xsl:template match="jp-searchbar">
    <xsl:call-template name="jp.layout.searchbar" />
  </xsl:template>

  <xsl:template name="jp.layout.searchbar">
    <xsl:variable name="searchBarMode">
      <xsl:variable name="controllerHook">
        <xsl:choose>
          <xsl:when test="/MyCoReWebPage/jp-searchbar/@mode">
            <jpsearchBar mode="{/MyCoReWebPage/jp-searchbar/@mode}" />
          </xsl:when>
          <xsl:otherwise>
            <jpsearchBar mode="{$searchMode}" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:apply-templates mode="controllerHook" select="xalan:nodeset($controllerHook)/jpsearchBar" />
    </xsl:variable>

    <xsl:if test="$showSearchBar = 'true'">
      <xsl:variable name="searchBar" select="xalan:nodeset($searchBarMode)" />
      <xsl:choose>
        <xsl:when test="$searchBar/div[@id='searchBar']">
          <xsl:copy-of select="$searchBar/div[@id='searchBar']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="jp.layout.searchbar.default" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.layout.searchbar.default">
    <div id="searchBar">
      <form id="searchForm" action="/servlets/solr/find">
        <div class="input-group">
          <xsl:choose>
            <xsl:when test="$journalID != ''">
              <input class="form-control" id="inputField" name="qry" value="{$qry}" placeholder="Suche innerhalb der Zeitschrift"
                title="Suche innerhalb der Zeitschrift" />
              <input type="hidden" name="fq" value="journalID:{$journalID}" />
              <input type="hidden" name="journalID" value="{$journalID}" />
            </xsl:when>
            <xsl:otherwise>
              <input class="form-control"  id="inputField" name="qry" value="{$qry}" placeholder="Suche im Gesamtbestand" title="Suche im Gesamtbestand" />
            </xsl:otherwise>
          </xsl:choose>
          <span class="input-group-btn">
<!--             <button id="submitButton" type="submit" class="btn btn-default">Suche</button> -->
            <input id="submitButton" type="submit" class="btn btn-default" value="Suche"/>
          </span>
        </div>
      </form>
    </div>
  </xsl:template>

</xsl:stylesheet>