<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xalan mcrxml jpxml solrxml i18n">

  <xsl:param name="returnURL"/>
  <xsl:param name="returnHash"/>
  <xsl:param name="returnID"/>
  <xsl:param name="returnName"/>


  <!-- facets without selected -->
  <xsl:variable name="filteredFacetsXML">
    <xsl:variable name="numFound" select="/response/result/@numFound"/>
    <lst name="facet_fields">
      <xsl:for-each select="/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst">
        <xsl:variable name="facet" select="@name"/>
        <lst name="{@name}">
          <xsl:for-each select="int">
            <xsl:if test="not(jpxml:isFacetSelected($RequestURL, $facet, @name))
                          and ($numFound != text() or $facet = 'journalType')">
              <xsl:if test="not(mcrxml:isCurrentUserGuestUser() and jpxml:isExcludedFacet(@name))">
                <int name="{@name}">
                  <xsl:value-of select="text()"/>
                </int>
              </xsl:if>
            </xsl:if>
          </xsl:for-each>
        </lst>
      </xsl:for-each>
    </lst>
    <lst name="facet_ranges">
      <!-- hard coded published sort -->
      <lst name="published">
        <xsl:for-each select="/response/lst[@name='facet_counts']/lst[@name='facet_ranges']/lst[@name='published']/date">
          <date name="{@name}">
            <xsl:value-of select="text()"/>
          </date>
        </xsl:for-each>
      </lst>
    </lst>
  </xsl:variable>
  <xsl:variable name="filteredFacets" select="xalan:nodeset($filteredFacetsXML)"/>

  <!-- selected facets -->
  <xsl:variable name="selectedFacetsXML">
    <lst name="facet_fields">
      <xsl:for-each select="/response/lst[@name='facet_counts']/lst['facet_fields']/lst">
        <xsl:variable name="facet" select="@name"/>
        <lst name="{@name}">
          <xsl:for-each select="int">
            <xsl:if test="jpxml:isFacetSelected($RequestURL, $facet, @name)">
              <int name="{@name}">
                <xsl:value-of select="text()"/>
              </int>
            </xsl:if>
          </xsl:for-each>
        </lst>
      </xsl:for-each>
    </lst>
  </xsl:variable>
  <xsl:variable name="selectedFacets" select="xalan:nodeset($selectedFacetsXML)"/>

  <xsl:template match="/response">

    <!-- javascript & css imports -->
    <link href="{$WebApplicationBaseURL}webjars/Eonasdan-bootstrap-datetimepicker/4.15.35/css/bootstrap-datetimepicker.min.css" rel="stylesheet" media="screen" type="text/css"/>
    <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/momentjs/2.10.6/min/moment-with-locales.js"></script>
    <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/Eonasdan-bootstrap-datetimepicker/4.15.35/js/bootstrap-datetimepicker.js"/>
    <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/highstock/2.0.4/highstock.src.js"/>
    <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-facetDateQuery.js"/>
    <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-publishedDialog.js"/>
    <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-response-default.js"/>

    <xsl:call-template name="searchBreadcrumb">
      <xsl:with-param name="objID" select="$journalID"/>
      <xsl:with-param name="returnURL" select="$returnURL"/>
      <xsl:with-param name="returnHash" select="$returnHash"/>
      <!-- returnID = is something like jportal_jpjournal_00000024 also id from where you came -->
      <xsl:with-param name="returnID" select="$returnID"/>
      <!-- returnName = if no id then give a name, like advanced search or law search (only i18n format)-->
      <xsl:with-param name="returnName" select="$returnName"/>
    </xsl:call-template>
    <div id="resultListContainer" class="col-md-12 jp-layout-mainContent">
      <xsl:if test="$selectedFacets/lst/lst/int">
        <!-- hack for responsive -->
        <div id="resultListHeader" class="row">
          <div class="list-group jp-list-group-special visible-xs">
            <xsl:apply-templates mode="facetField" select="$selectedFacets/lst/lst/int"/>
          </div>
        </div>
      </xsl:if>
      <div id="resultList" class="row container-fluid">
        <xsl:apply-templates mode="resultList" select="."/>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="searchResultText" match="response">
    <xsl:value-of select="i18n:translate('jp.metadata.search.no_results')"/>
  </xsl:template>

  <xsl:template mode="searchResultText" match="response[result/@numFound &gt; 0]">
    <xsl:variable name="resultInfoXML">
      <xsl:call-template name="jp.pagination.getResultInfoXML">
        <xsl:with-param name="response" select="/response"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="resultInfo" select="xalan:nodeset($resultInfoXML)"/>
    <xsl:value-of select="concat($resultInfo/numFound, ' ', i18n:translate('jp.metadata.search.hits'))"/>
    <xsl:if test="$resultInfo/page > 0">
      <xsl:value-of select="concat(' (', i18n:translate('jp.metadata.search.page'), ' ' , $resultInfo/page + 1, ')')"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="response" mode="jp.response.navigation">
    <xsl:apply-templates mode="jp.response.sort" select="."/>
  </xsl:template>

  <xsl:template name="jp-response-default-noHits">
    <p>
      <xsl:value-of select="i18n:translate('jp.metadata.search.no_results')"/>
      <br/>
      <xsl:value-of select="i18n:translate('jp.metadata.search.sugestion')"/>
      <ul>
        <li>
          <xsl:value-of select="i18n:translate('jp.metadata.search.sugestion1')"/>
        </li>
        <li>
          <xsl:value-of select="i18n:translate('jp.metadata.search.sugestion2')"/>
        </li>
        <li>
          <xsl:value-of select="i18n:translate('jp.metadata.search.sugestion3')"/>
        </li>
        <li>
          <xsl:value-of select="i18n:translate('jp.metadata.search.sugestion4')"/>
        </li>
      </ul>
    </p>
  </xsl:template>

  <xsl:template mode="resultList" match="response">
    <div class="col-sm-3">
      <div class="jp-layout-searchResult-style">
        <xsl:apply-templates mode="searchResultText" select="."/>
        <button type="button" class="navbar-toggle collapsed jp-layout-mynavbarbutton" data-toggle="collapse" data-target="#navbar-collapse-searchResult">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <div class="jp-layout-triangle visible-xs"></div>
        <div class="jp-layout-triangle visible-xs"></div>
      </div>
      <div id="navbar-collapse-searchResult" class="jp-layout-searchList navbar-collapse collapse" role="navigation">
        <xsl:apply-templates select="." mode="getFacetList"/>
      </div>
    </div>
    <div class="col-sm-9 jp-layout-hits">
      <div class="jp-layout-triangle hidden-xs"></div>
      <div class="jp-layout-triangle hidden-xs"></div>
      <div class="jp-objectlist">
        <xsl:choose>
          <xsl:when test="result/doc">
            <xsl:apply-templates mode="searchResults" select="result/doc"/>
            <xsl:apply-templates mode="jp.pagination" select="."/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="jp-response-default-noHits"/>
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="getFacetList" match="response">

    <xsl:apply-templates mode="jp.response.navigation" select="."/>

    <div class="jp-layout-search-sidebar-group">
      <h2 class="jp-layout-resultLCaption">
        <xsl:value-of select="i18n:translate('jp.metadata.search.narrow')"/>
      </h2>

      <div class="list-group jp-list-group-special hidden-xs">
        <xsl:apply-templates mode="facetField" select="$selectedFacets/lst/lst/int"/>
      </div>

      <xsl:apply-templates mode="facetGroup" select="$filteredFacets/lst[@name='facet_fields']/lst"/>
      <xsl:apply-templates mode="facetRanges" select="$filteredFacets/lst[@name='facet_ranges']/lst"/>
    </div>
  </xsl:template>

  <xsl:variable name="searchResultsFields">
    <field name="objectType"/>
    <field name="dateOfBirth">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.person.dateOfBirth')"/>
      </xsl:attribute>
    </field>
    <field name="dateOfDeath">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.person.dateOfDeath')"/>
      </xsl:attribute>
    </field>
    <field name="participant.author">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.author')"/>
      </xsl:attribute>
    </field>
    <field name="date.published">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.jpjournal.date.published')"/>
      </xsl:attribute>
    </field>
    <field name="date.published_from">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.jpjournal.date.published')"/>
      </xsl:attribute>
    </field>
    <field name="date.published_Original">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.coverage')"/>
      </xsl:attribute>
    </field>
    <field name="date.published_Original_From">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.coverage')"/>
      </xsl:attribute>
    </field>
    <field name="size">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.size')"/>
      </xsl:attribute>
    </field>
    <field name="rubric">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.rubric')"/>
      </xsl:attribute>
    </field>
  </xsl:variable>

  <xsl:template mode="searchResults" match="doc">
    <xsl:variable name="mcrId" select="str[@name='id']"/>
    <xsl:choose>
      <xsl:when test="mcrxml:exists($mcrId)">
        <div class="row jp-objectlist-object">
          <div class="jp-objectlist-thumbnail">
            <xsl:variable name="mcrObj" select="document(concat('mcrobject:', $mcrId))/mycoreobject"/>
            <xsl:apply-templates select="$mcrObj" mode="derivateDisplay">
              <xsl:with-param name="mode" select="'preview'"/>
              <xsl:with-param name="editable" select="'false'"/>
              <xsl:with-param name="query" select="../../lst[@name='responseHeader']/lst[@name='params']/str[@name='qry']"/>
            </xsl:apply-templates>
          </div>
          <div class="jp-objectlist-metadata">
            <xsl:apply-templates mode="searchHitLabel" select="."/>
            <ul class="jp-layout-metadaInSearchResults">
              <xsl:variable name="doc" select="."/>
              <xsl:for-each select="xalan:nodeset($searchResultsFields)/field">
                <xsl:variable name="fieldName" select="@name"/>
                <xsl:if test="$doc/*[@name = $fieldName]">
                  <li>
                    <xsl:if test="@label">
                      <span class="jp-layout-label">
                        <xsl:value-of select="@label"/>
                      </span>
                    </xsl:if>
                    <xsl:apply-templates mode="searchHitDataField" select="$doc/*[@name = $fieldName]"/>
                  </li>
                </xsl:if>
              </xsl:for-each>
            </ul>
          </div>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <!-- object doesn't exist in mycore -> delete it in solr -->
        <xsl:value-of select="solrxml:delete($mcrId)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="searchHitLabel" match="doc">
    <h3 class="jp-layout-clickLabel">
      <!-- TODO: do not pass the qry parameter, instead we should pass a generated response id. with this id we could get the response from cache and use it in our 
        metadata object -->
      <xsl:variable name="q" select="../../lst[@name='responseHeader']/lst[@name='params']/str[@name='qry']"/>
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="concat($WebApplicationBaseURL, 'receive/', str[@name='id'])"/>
          <xsl:if test="$q != ''">
            <xsl:value-of select="concat('?XSL.q=', $q)"/>
          </xsl:if>
        </xsl:attribute>
        <xsl:attribute name="title">
          <xsl:apply-templates mode="searchHitLabelText" select="."/>
        </xsl:attribute>
        <xsl:call-template name="shortenString">
          <xsl:with-param name="string">
            <xsl:apply-templates mode="searchHitLabelText" select="."/>
          </xsl:with-param>
          <xsl:with-param name="length" select="190"/>
        </xsl:call-template>
      </a>
    </h3>
  </xsl:template>

  <xsl:template mode="searchHitLabelURL" match="searchHitLabelURL[subselectEnd]">
    <xsl:value-of select="subselectEnd/@value"/>
    <xsl:apply-templates mode="urlParam" select="param"/>
  </xsl:template>

  <xsl:template mode="urlParam" match="param">
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
    <xsl:value-of select="str[@name='maintitle']"/>
  </xsl:template>

  <xsl:template mode="searchHitLabelText" match="doc[contains('person jpinst', str[@name='objectType'])]">
    <xsl:value-of select="str[@name='heading']"/>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str">
    <span class="jp-layout-inList">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='date.published_from']">
    <span class="jp-layout-inList">
      <xsl:value-of select="."/>
      <xsl:if test="../str[@name='date.published_until']">
        <xsl:value-of select="concat(' - ', ../str[@name='date.published_until'])"/>
      </xsl:if>
    </span>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="arr[@name='participant.author']/str">
    <span class="jp-layout-inList">
      <a href="{$WebApplicationBaseURL}receive/{substring-before(., '#')}">
        <xsl:value-of select="substring-after(., '#')"/>
      </a>
    </span>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="arr[@name='rubric']/str">
    <xsl:variable name="category">
      <categ classid="{substring-before(.,'#')}" categid="{substring-after(.,'#')}"/>
    </xsl:variable>
    <span class="jp-layout-inList">
      <xsl:call-template name="printClass">
        <xsl:with-param name="nodes" select="xalan:nodeset($category)/categ"/>
        <xsl:with-param name="host" select="'local'"/>
        <xsl:with-param name="next" select="', '"/>
      </xsl:call-template>
    </span>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'person']">
    <xsl:value-of select="i18n:translate('jp.metadata.facet.objectType.person')"/>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jpinst']">
    <xsl:value-of select="i18n:translate('jp.metadata.facet.objectType.jpinst')"/>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jpjournal']">
    <xsl:value-of select="i18n:translate('jp.metadata.facet.objectType.jpjournal')"/>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jpvolume']">
    <span class="jp-layout-label">
      <xsl:value-of select="i18n:translate('jp.metadata.search.volume_published')"/>
    </span>
    <xsl:call-template name="resultListBreadcrumb">
      <xsl:with-param name="objID" select="../str[@name='id']"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jparticle']">
    <span class="jp-layout-label">
      <xsl:value-of select="i18n:translate('jp.metadata.search.article_published')"/>
    </span>
    <xsl:call-template name="resultListBreadcrumb">
      <xsl:with-param name="objID" select="../str[@name='id']"/>
    </xsl:call-template>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * SORT -->
  <!-- *************************************************** -->
  <xsl:template mode="jp.response.sort" match="response">
  </xsl:template>

  <xsl:template mode="jp.response.sort" match="response[result/@numFound &gt;= 2]">
    <xsl:variable name="sort" select="lst[@name='responseHeader']/lst[@name='params']/str[@name='sort']/text()"/>
    <xsl:variable name="sortOptionsXML">
      <option value="score desc">
        <xsl:value-of select="i18n:translate('jp.metadata.search.score_desc')"/>
      </option>
      <option value="published asc">
        <xsl:value-of select="i18n:translate('jp.metadata.search.published_asc')"/>
      </option>
      <option value="published desc">
        <xsl:value-of select="i18n:translate('jp.metadata.search.published_desc')"/>
      </option>
      <option value="alphabetic_sort asc">
        <xsl:value-of select="i18n:translate('jp.metadata.search.alphabetic_sort_asc')"/>
      </option>
    </xsl:variable>
    <xsl:variable name="sortOptions" select="xalan:nodeset($sortOptionsXML)"/>
    <div class="jp-layout-search-sidebar-group">
      <h2 class="jp-layout-resultLCaption">
        <xsl:value-of select="i18n:translate('jp.metadata.search.sort')"/>
      </h2>
      <select class="sortSelect"> <!-- id="sortSelect" -->
        <xsl:apply-templates select="$sortOptions/option" mode="jp.response.sort.option">
          <xsl:with-param name="selected" select="$sort"/>
        </xsl:apply-templates>
      </select>
    </div>
  </xsl:template>

  <xsl:template match="option" mode="jp.response.sort.option">
    <xsl:param name="selected"/>
    <xsl:element name="option">
      <xsl:attribute name="value">
        <xsl:value-of select="@value"/>
      </xsl:attribute>
      <xsl:if test="@value = $selected">
        <xsl:attribute name="selected">
          <xsl:value-of select="'selected'"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:value-of select="text()"/>
    </xsl:element>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * FACET -->
  <!-- *************************************************** -->
  <xsl:template mode="facetGroup" match="lst">
  </xsl:template>

  <xsl:template mode="filteredFacetGroup" match="lst">
  </xsl:template>

  <xsl:template mode="facetGroup" match="lst[count(*) &gt; 0]">
    <div>
      <a class="dt-collapse" data-toggle="collapse">
        <xsl:attribute name="data-target">
          <xsl:value-of select="concat('#', @name)"/>
        </xsl:attribute>
        <span class="jp-layout-facet-group-head">
          <xsl:value-of select="i18n:translate(concat('jp.metadata.facet.', @name))"/>
        </span>
        <i class="fa fa-sort-asc"></i>
        <i class="fa fa-sort-desc"></i>
      </a>
      <div class="collapse in list-group jp-list-group-special" id="{@name}">
        <xsl:apply-templates select="int" mode="facetField"/>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="facetField" match="int">
    <xsl:variable name="value" select="@name"/>
    <xsl:variable name="count" select="text()"/>
    <xsl:variable name="facet" select="../@name"/>
    <xsl:variable name="requestURLNoStartParam" select="jpxml:rmStartParam($RequestURL)"/>
    <xsl:variable name="selected" select="jpxml:isFacetSelected($requestURLNoStartParam, $facet, $value)"/>

    <xsl:variable name="href">
      <xsl:choose>
        <xsl:when test="$selected">
          <xsl:value-of select="jpxml:removeFacet($requestURLNoStartParam, $facet, $value)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat($requestURLNoStartParam, '&amp;fq=', $facet, ':&quot;', $value, '&quot;')"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="class">
      <xsl:if test="$selected">
        <xsl:value-of select="'selected'"/>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="text">
      <xsl:variable name="facetSettings" select="$settings/facet[@name=$facet]"/>
      <xsl:choose>
        <xsl:when test="$facet = 'journalType'">
          <xsl:value-of select="jpxml:getJournalTypeFacetLabel($value)"/>
        </xsl:when>
        <xsl:when test="$facetSettings/@translate = 'true'">
          <xsl:value-of select="i18n:translate(concat('jp.metadata.facet.', $facet, '.', $value))"/>
        </xsl:when>
        <xsl:when test="$facetSettings/@mcrid = 'true'">
          <xsl:variable name="mcrObj" select="document(concat('mcrobject:', $value))/mycoreobject"/>
          <xsl:apply-templates mode="printTitle"
                               select="$mcrObj/metadata/maintitles/maintitle[@inherited='0']|$mcrObj/metadata/def.heading/heading|$mcrObj/metadata/names[@class='MCRMetaInstitutionName']/name"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$value"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <a class="list-group-item {$class}" href="{$href}">
      <i class="icon {$class}"></i>
      <span class="text">
        <xsl:value-of select="$text"/>
      </span>
      <span class="pull-right">
        <xsl:if test="$selected">
          <xsl:attribute name="class">pull-right hidden-xs</xsl:attribute>
        </xsl:if>
        <xsl:value-of select="$count"/>
      </span>
    </a>
  </xsl:template>

  <!-- FACET RANGE -->
  <xsl:template mode="facetRanges" match="lst">
    <div>
      <a class="dt-collapse" data-toggle="collapse">
        <xsl:attribute name="data-target">
          <xsl:value-of select="concat('#', @name)"/>
        </xsl:attribute>
        <span class="jp-layout-facet-group-head">
          <xsl:value-of select="i18n:translate(concat('jp.metadata.facet.', @name))"/>
        </span>
        <i class="fa fa-sort-asc"></i>
        <i class="fa fa-sort-desc"></i>
      </a>
      <div class="collapse in list-group jp-list-group-special" id="{@name}">
        <div style="display: flex;">
          <div style="display: flex; flex-direction: column; justify-content: space-between; margin-right: 8px;">
            <a id="{@name}_accept_button" href="javascript:void(0)" type="button"
               class="btn btn-xs btn-primary disabled" role="button">
              <i class="fa fa-check"></i>
            </a>
            <a id="{@name}_popup_button" href="javascript:void(0)" type="button"
               class="btn btn-xs btn-default" role="button">
              <i class="fa fa-bar-chart"></i>
            </a>
            <a id="{@name}_cancel_button" href="javascript:void(0)" type="button"
               class="btn btn-xs btn-danger disabled" role="button">
              <i class="fa fa-times"></i>
            </a>
          </div>
          <div>
            <div class='input-group date' id='{@name}_from'>
              <input type='text' class="form-control"/>
              <span
                      class="input-group-addon">
                <span
                        class="glyphicon glyphicon-calendar"></span>
              </span>
            </div>
            <div class="text-center">
              <i class="fa fa-angle-down"></i>
            </div>
            <div class='input-group date' id='{@name}_to'>
              <input type='text' class="form-control"/>
              <span
                      class="input-group-addon">
                <span
                        class="glyphicon glyphicon-calendar"></span>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>