<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:decoder="xalan://java.net.URLDecoder"
  exclude-result-prefixes="xalan decoder">

  <xsl:variable name="qry">
    <xsl:variable name="encodedQuery">
      <xsl:call-template name="UrlGetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="'qry'" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="decoder:decode($encodedQuery, 'UTF-8')" />
  </xsl:variable>
  <xsl:variable name="sort">
    <xsl:variable name="encodedSort">
      <xsl:call-template name="UrlGetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="'sort'" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="decoder:decode($encodedSort, 'UTF-8')" />
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
      <form id="searchForm" action="{$WebApplicationBaseURL}servlets/solr/find" class="container-fluid">
        <div class="row">
          <div class="hidden">
            <span id="globalSearchLabel">Suche im Gesamtbestand</span>
            <span id="journalSearchLabel">Suche innerhalb der Zeitschrift</span>
          </div>
          <div class="col-md-8 input-group">
            <xsl:if test="$journalID != ''">
              <span class="input-group-btn">
                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" id="searchDropDownButton">
                  <span class="caret"></span>
                  <span class="sr-only">Toggle Search</span>
                </button>
                <ul class="dropdown-menu" role="menu" id="searchDropDownMenu">
                  <li>
                    <a href="javascript:void(0)" id="globalSearchOption">
                      <i class="fa fa-fw fa-globe" />
                    </a>
                  </li>
                  <li>
                    <a href="javascript:void(0)" id="journalSearchOption">
                      <i class="fa fa-fw fa-book" />
                    </a>
                  </li>
                </ul>
              </span>
            </xsl:if>
            <input class="form-control" id="inputField" name="qry" value="{$qry}" />
            <xsl:if test="$sort != ''">
              <input type="hidden" name="sort" value="{$sort}" />
            </xsl:if>
            <span class="input-group-btn">
              <input id="submitButton" type="submit" class="btn btn-default" value="Suche" />
            </span>
          </div>
        </div>
      </form>
    </div>
  </xsl:template>

</xsl:stylesheet>
