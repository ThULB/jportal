<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="xalan mcrxml jpxml solrxml i18n">

  <xsl:param name="returnURL"/>
  <xsl:param name="returnHash"/>
  <xsl:param name="returnID"/>
  <xsl:param name="returnName"/>

  <!-- facets without selected -->
  <xsl:variable name="filteredFacetsXML">
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

  <xsl:template match="/response">

    <!-- javascript & css imports -->
    <link href="{$WebApplicationBaseURL}webjars/Eonasdan-bootstrap-datetimepicker/4.15.35/css/bootstrap-datetimepicker.min.css" rel="stylesheet" media="screen" type="text/css"/>
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
      <!--<xsl:if test="$selectedFacets/lst/lst/int">-->
      <xsl:if test="$facetsWithParentNodes/lst/lst/int">
        <!-- hack for responsive -->
        <div id="resultListHeader" class="row">
          <div class="list-group jp-list-group-special visible-xs">
            <xsl:apply-templates mode="facetTree" select="$facetsWithParentNodes/lst"/>
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
      <xsl:apply-templates mode="facetTree" select="$facetsWithParentNodes/lst"/>
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
        <xsl:value-of select="i18n:translate('metaData.date.published')"/>
      </xsl:attribute>
    </field>
    <field name="date.reviewedWork">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.date.reviewedWork')"/>
      </xsl:attribute>
    </field>
    <field name="date.reportingPeriod">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.date.reportingPeriod')"/>
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

  <xsl:template mode="searchHitDataField" match="str[contains(@name, 'date.')]">
    <span class="jp-layout-inList">
      <xsl:value-of select="jpxml:formatSolrDate(text(), $CurrentLang)"/>
    </span>
  </xsl:template>

  <!--
    <xsl:template mode="searchHitDataField" match="str[@name='date.published_from']">
      <span class="jp-layout-inList">
        <xsl:value-of select="."/>
        <xsl:if test="../str[@name='date.published_until']">
          <xsl:value-of select="concat(' - ', ../str[@name='date.published_until'])"/>
        </xsl:if>
      </span>
    </xsl:template>
  -->

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
    <xsl:variable name="journalType" select="../arr[@name='journalType']/str/text()"/>
    <xsl:if test="$journalType != ''">
      <xsl:variable name="classlink" select="concat('classification:metadata:0:children:',$journalType)"/>
      <xsl:call-template name="jp.printClass">
        <xsl:with-param name="nodes" select="document($classlink)/mycoreclass/categories/category"/>
        <xsl:with-param name="lang" select="concat($CurrentLang, '-singular')"/>
      </xsl:call-template>
    </xsl:if>
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
      <option value="published_sort asc">
        <xsl:value-of select="i18n:translate('jp.metadata.search.published_asc')"/>
      </option>
      <option value="published_sort desc">
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
  <!-- FACET RANGE -->
  <xsl:template mode="facetRanges" match="lst">
    <a class="dt-collapse jp-facet-header" data-toggle="collapse">
      <xsl:attribute name="data-target">
        <xsl:value-of select="concat('#', @name)"/>
      </xsl:attribute>
      <span class="jp-layout-facet-group-head">
        <xsl:value-of select="i18n:translate(concat('jp.metadata.facet.', @name))"/>
      </span>
      <i class="fas fa-sort-up"></i>
      <i class="fas fa-sort-down"></i>
    </a>
    <div class="collapse in list-group jp-list-group-special" id="{@name}">
      <div style="display: flex;">
        <div style="display: flex; flex-direction: column; justify-content: space-between; margin-right: 8px;">
          <a id="{@name}_accept_button" href="javascript:void(0)" type="button"
             class="btn btn-xs btn-primary disabled" role="button">
            <i class="fas fa-check"></i>
          </a>
          <a id="{@name}_popup_button" href="javascript:void(0)" type="button"
             class="btn btn-xs btn-default" role="button">
            <i class="fas fa-chart-bar"></i>
          </a>
          <a id="{@name}_cancel_button" href="javascript:void(0)" type="button"
             class="btn btn-xs btn-danger disabled" role="button">
            <i class="fas fa-times"></i>
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
            <i class="fas fa-angle-down"></i>
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
  </xsl:template>

  <!-- Start tree view facet -->
  <xsl:variable name="facetParentSettings"
                select="$settings/editor/jpjournal/bind/row"/>

  <xsl:variable name="facetsWithParent">
    <xsl:apply-templates mode="addFacetParent"
                         select="/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[int]"/>
  </xsl:variable>
  <xsl:variable name="facetsWithParentNodes" select="xalan:nodeset($facetsWithParent)"/>

  <xsl:variable name="usedFacets"
                select="/response/lst[@name='responseHeader']/lst[@name='params']/arr[@name='fq']/str
                |/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='fq']"/>

  <xsl:variable name="journalIDParam"
                select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='journalID']"/>

  <xsl:template mode="addFacetParent" match="lst">
    <xsl:copy>
      <xsl:apply-templates mode="addFacetParent" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="addFacetParent" match="int">
    <xsl:variable name="groupName" select="../@name"/>
    <xsl:variable name="classID" select="substring-before(@name, ':')"/>
    <xsl:copy>
      <xsl:if test="$facetParentSettings[@xpath=$groupName and @class=$classID and @on]">
        <xsl:attribute name="parent">
          <xsl:value-of select="$facetParentSettings[@xpath=$groupName and @class=$classID]/@on"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates mode="addFacetParent" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="addFacetParent" match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates mode="addFacetParent" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="facetTree" match="lst">
    <a class="dt-collapse jp-facet-header" data-toggle="collapse">
      <xsl:attribute name="data-target">
        <xsl:value-of select="concat('#', @name)"/>
      </xsl:attribute>
      <span class="jp-layout-facet-group-head">
        <xsl:value-of select="i18n:translate(concat('jp.metadata.facet.', @name))"/>
      </span>
      <i class="fas fa-sort-up"></i>
      <i class="fas fa-sort-down"></i>
    </a>
    <div class="collapse in list-group jp-list-group-special jp-facet-list" id="{@name}">
      <!-- select int nodes where parent attribute is not present-->
      <xsl:apply-templates mode="facetTree" select="int[not(@parent)]"/>
    </div>
  </xsl:template>

  <xsl:template mode="facetTree" match="int">
    <xsl:variable name="group" select=".."/>
    <xsl:variable name="groupName" select="$group/@name"/>
    <xsl:variable name="id" select="@name"/>

    <xsl:variable name="facetCheckboxClass">
      <xsl:choose>
        <xsl:when test="$usedFacets[starts-with(.,$groupName) and contains(., $id)]">
          <xsl:value-of select="'jp-facet-checkbox checked'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'jp-facet-checkbox'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="facetHrefLink">
      <xsl:choose>
        <xsl:when test="$usedFacets[starts-with(.,$groupName) and contains(., $id)]">
          <xsl:apply-templates mode="facetHrefLink" select="$usedFacets[not(contains(., $id))]/text()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="addFacet" select="xalan:nodeset(concat($groupName,':&quot;',$id,'&quot;'))"/>
          <xsl:apply-templates mode="facetHrefLink" select="$usedFacets|$addFacet"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="$journalIDParam">
        <xsl:value-of select="concat('&amp;journalID=',$journalIDParam)"/>
      </xsl:if>
    </xsl:variable>

    <div class="jp-facet-row" data-id="{$id}" data-type="{$groupName}">
      <xsl:variable name="_requsetURL" select="concat($RequestURL, '&amp;fq')"/>
      <a href="{concat(substring-before($_requsetURL, '&amp;fq'),$facetHrefLink)}">
        <div class="jp-facet-entry">
          <div class="jp-facet-linkContainer">
            <label class="{$facetCheckboxClass}"/>
            <div class="jp-facet-label">
              <xsl:variable name="facetSettings" select="$settings/facet[@name=$groupName]"/>
              <xsl:choose>
                <xsl:when test="$group/@name = 'journalType'">
                  <xsl:value-of select="jpxml:getJournalTypeFacetLabel($id)"/>
                </xsl:when>
                <xsl:when test="$facetSettings/@translate = 'true'">
                  <xsl:value-of select="i18n:translate(concat('jp.metadata.facet.', $groupName, '.', $id))"/>
                </xsl:when>
                <xsl:when test="$facetSettings/@mcrid = 'true'">
                  <xsl:variable name="mcrObj" select="document(concat('mcrobject:', $id))/mycoreobject"/>
                  <xsl:apply-templates mode="printTitle"
                                       select="$mcrObj/metadata/maintitles/maintitle[@inherited='0']|$mcrObj/metadata/def.heading/heading|$mcrObj/metadata/names[@class='MCRMetaInstitutionName']/name"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$id"/>
                </xsl:otherwise>
              </xsl:choose>
            </div>
          </div>
          <div class="jp-facet-count">
            <xsl:value-of select="."/>
          </div>
        </div>
      </a>
      <xsl:if test="$group/int[@parent=$id]">
        <xsl:apply-templates mode="facetTree" select="$group/int[@parent=$id]"/>
      </xsl:if>
    </div>
  </xsl:template>
  <xsl:template mode="facetHrefLink" match="text()">
    <xsl:value-of select="concat('&amp;fq=',.)"/>
  </xsl:template>
  <!-- End tree view facet -->

</xsl:stylesheet>
