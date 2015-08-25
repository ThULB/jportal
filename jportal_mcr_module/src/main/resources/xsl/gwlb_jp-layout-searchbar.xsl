<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:decoder="xalan://java.net.URLDecoder"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xalan decoder i18n">

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

<!--     <xsl:if test="$showSearchBar = 'true'"> -->
    <xsl:variable name="searchBar" select="xalan:nodeset($searchBarMode)" />
    <xsl:choose>
      <xsl:when test="$searchBar/div[@id='searchBar']">
        <xsl:copy-of select="$searchBar/div[@id='searchBar']" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="jp.layout.searchbar.default" />
      </xsl:otherwise>
    </xsl:choose>
<!--     </xsl:if> -->
  </xsl:template>

  <xsl:template name="jp.layout.searchbar.default">
    <script type="text/javascript">
      $(document).ready(function() {
        updateSearchbar();
      });
    </script>
    <div id="searchBar">
      <xsl:if test="$showSearchBar = 'true'">
        <form id="searchForm" action="{$WebApplicationBaseURL}servlets/solr/find">
          <div class="row">
            <xsl:variable name="mobileCol">
              <xsl:choose>
                <xsl:when test="$objectEditing//li/a">
                  <xsl:value-of select="'col-xs-8 col-xs-offset-2'" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="'col-xs-12'" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <div class="col-sm-8 col-sm-offset-2 {$mobileCol}">
              <div class="input-group">
		            
                <div class="input-group-btn">
                  <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" id="searchDropDownButton">
                    <span class="caret"></span>
                    <span class="sr-only">Toggle Search</span>
                  </button>
                  <ul class="dropdown-menu" role="menu" id="searchDropDownMenu">
                    <xsl:if test="$journalID != ''">
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
                    </xsl:if>
                    <li>
                      <xsl:choose>
                        <xsl:when test="$searchMode != 'advanced'">
                          <a>
                            <xsl:attribute name="href">
      	                    <xsl:value-of select="concat($WebApplicationBaseURL, 'jp-advancedsearch.xml')" />
      	                      <xsl:if test="$journalID != ''">
      	                        <xsl:value-of select="concat('?journalID=', $journalID)" />
      	                      </xsl:if>
      	                  </xsl:attribute>
                            <i class="fa fa-fw fa-search-plus" />
                            <xsl:value-of select="i18n:translate('jp.metadata.search.advanced')" />
                          </a>
                        </xsl:when>
                        <xsl:otherwise>
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
                            <i class="fa fa-fw fa-search-plus" />
                            <xsl:value-of select="i18n:translate('jp.metadata.search.advancedEdit')" />
                          </a>
                        </xsl:otherwise>
                      </xsl:choose>
                    </li>
                  </ul>
                </div><!-- end input-group-btn -->
                <input class="form-control" type="text" id="inputField" name="qry" value="{$qry}" />
                  <span class="input-group-addon">
                    <span class="glyphicon glyphicon-search glyphSearchBar"
                      onclick="$('#searchForm').submit()">
                      <xsl:attribute name="title">
                  <xsl:value-of select="i18n:translate('jp.metadata.search.search')" />
                </xsl:attribute>
                    </span>
                  </span>
  
              </div><!-- input-group-btn  -->
                <div class="hidden">
                  <span id="globalSearchLabel"><xsl:value-of select="i18n:translate('jp.metadata.search.entire_inventory')" /></span> 
                  <span id="journalSearchLabel"><xsl:value-of select="i18n:translate('jp.metadata.search.within_journal')" /></span> 
                </div>
  
                <xsl:if test="$sort != ''">
                  <input type="hidden" name="sort" value="{$sort}" />
                </xsl:if>
            </div>
            <!-- edit object -->
            <xsl:if test="$objectEditing//li/a">
              <div class="dropdown dropdown-menu-right pull-right jp-layout-object-editing-container">
                <button class="btn btn-default fa fa-gear dropdown-toggle" type="button" data-toggle="dropdown" />
                <ul class="jp-layout-object-editing-menu dropdown-menu dropdown-menu-right" role="menu">
                  <xsl:copy-of select="$objectEditing/*" />
                </ul>
              </div>
             </xsl:if>
          </div>
        </form>
      </xsl:if>
    </div>
  </xsl:template>
</xsl:stylesheet>