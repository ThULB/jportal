<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xalan mcrxml jpxml solrxml i18n">

  <xsl:param name="returnURL" />
  
  <xsl:template match="/response">
    <div id="searchResults">
      <div id="resultListHeader" class="row col-sm-12"> <!-- jp-layout-border-light navbar-header -->
        <button type="button" class="navbar-toggle collapsed jp-layout-mynavbarbutton" data-toggle="collapse" data-target="#navbar-collapse-searchResult">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        
        <div class="form-group list-group jp-list-group-special visible-xs">
          <xsl:apply-templates mode="facetList" select="lst[@name='facet_counts']/lst" >
            <xsl:with-param name="isSelected" select="true()" />
          </xsl:apply-templates>
        </div>
<!--         <h2 class="col-sm-6 jp-layout-resultListHeadLeft">  -->
<!--           <xsl:value-of select="i18n:translate('jp.metadata.search.result')" /> -->
<!--         </h2> -->
      </div>
      <div id="resultList" class="col-sm-12 container-fluid">
        <xsl:apply-templates mode="resultList" select="." />
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="searchResultText" match="response">
    <xsl:value-of select="i18n:translate('jp.metadata.search.no_results')" />
  </xsl:template>

  <xsl:template mode="searchResultText" match="response[result/@numFound = 1]">
    <xsl:value-of select="i18n:translate('jp.metadata.search.object')" />
  </xsl:template>

  <xsl:template mode="searchResultText" match="response[result/@numFound &gt; 1]">
    <xsl:variable name="resultInfoXML">
      <xsl:call-template name="jp.pagination.getResultInfoXML">
        <xsl:with-param name="response" select="/response" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="resultInfo" select="xalan:nodeset($resultInfoXML)" />
    <xsl:value-of select="concat($resultInfo/numFound, ' ', i18n:translate('jp.metadata.search.objects'))" />
    <xsl:if test="$resultInfo/page > 0">
      <xsl:value-of select="concat(' (', i18n:translate('jp.metadata.search.page'), ' ' , $resultInfo/page + 1, ')')" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="response" mode="jp.response.navigation">
<!--     <xsl:if test="$journalID != ''"> -->
<!--       <a href="{$WebApplicationBaseURL}receive/{$journalID}">Zurück zur Zeitschrift</a> -->
<!--     </xsl:if> -->
<!--     <xsl:if test="$returnURL"> -->
<!--       <a href="{$returnURL}">Zurück</a> -->
<!--     </xsl:if> -->
    <xsl:apply-templates mode="jp.response.sort" select="." />
  </xsl:template>

  <xsl:template mode="resultList" match="response[result/@numFound = 0]">
    <p>
      <xsl:value-of select="i18n:translate('jp.metadata.search.no_results')" />
      <br />
      <xsl:value-of select="i18n:translate('jp.metadata.search.sugestion')" />
      <ul>
        <li>
          <xsl:value-of select="i18n:translate('jp.metadata.search.sugestion1')" />
        </li>
        <li>
          <xsl:value-of select="i18n:translate('jp.metadata.search.sugestion2')" />
        </li>
        <li>
          <xsl:value-of select="i18n:translate('jp.metadata.search.sugestion3')" />
        </li>
        <li>
          <xsl:value-of select="i18n:translate('jp.metadata.search.sugestion4')" />
        </li>
      </ul>
    </p>
  </xsl:template>

  <xsl:template mode="resultList" match="response[result/@numFound &gt;= 1]">
    <xsl:variable name="showFacet" select="lst[@name='facet_counts']" />
    <xsl:variable name="resultListClass">
      <xsl:if test="$showFacet">
        <xsl:value-of select="'facet'" />
      </xsl:if>
    </xsl:variable>
    <xsl:if test="$showFacet">
      <div id="navbar-collapse-searchResult" class="col-sm-3 jp-layout-searchList navbar-collapse collapse" role="navigation">
        <div class="jp-layout-searchResult-style form-group">
          <xsl:apply-templates mode="searchResultText" select="." />
        </div>
        
        <xsl:apply-templates mode="jp.response.navigation" select="." />
        
        <div>
          <h2 class="jp-layout-resultLCaption">
            <xsl:value-of select="i18n:translate('jp.metadata.search.narrow')" />
          </h2>
          
        <div class="form-group list-group jp-list-group-special hidden-xs">
          <xsl:apply-templates mode="facetList" select="lst[@name='facet_counts']/lst" >
            <xsl:with-param name="isSelected" select="true()" />
          </xsl:apply-templates>
        </div>
        
          <xsl:apply-templates mode="facetList" select="lst[@name='facet_counts']/lst" >
            <xsl:with-param name="isSelected" select="false()" />
          </xsl:apply-templates>
        </div>
        
        <xsl:if test="$journalID != '' or $returnURL">
          <div class="form-group">
            <h2 class="jp-layout-resultLCaption">
              Optionen
            </h2>
            <xsl:if test="$journalID != ''">
              <a href="{$WebApplicationBaseURL}receive/{$journalID}">
                <xsl:value-of select="i18n:translate('jp.metadata.search.back_journal')" />
              </a>
            </xsl:if>
            <xsl:if test="$returnURL">
              <a href="{$returnURL}">
                <xsl:value-of select="i18n:translate('jp.metadata.search.back')" />
              </a>
            </xsl:if>
          </div>
        </xsl:if>
      </div>
    </xsl:if>
    <div class="col-sm-9 jp-layout-resultlistBorder">
      <div id="jp-layout-triangle"></div>
      <div id="jp-layout-triangle"></div>
      <xsl:apply-templates mode="searchResults" select="result/doc" />
      <xsl:apply-templates mode="jp.pagination" select="." />
    </div>
  </xsl:template>

  <xsl:variable name="searchResultsFields">
    <field name="objectType" />
    <field name="published">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.jpjournal.date.published')" />
      </xsl:attribute>
    </field>
    <field name="published_from">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.jpjournal.date.published')" />
      </xsl:attribute>
    </field>
    <field name="date.published_from">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.jpjournal.date.published')" />
      </xsl:attribute>
    </field>
    <field name="dateOfBirth">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.person.dateOfBirth')" />
      </xsl:attribute>
    </field>
    <field name="dateOfDeath">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.person.dateOfDeath')" />
      </xsl:attribute>
    </field>
    <field name="participant.author">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.author')" />
      </xsl:attribute>
    </field>
    <field name="date.published">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('metaData.jpjournal.date.published')" />
      </xsl:attribute>
    </field>
    <field name="date.published_Original">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.coverage')" />
      </xsl:attribute>
    </field>
    <field name="date.published_Original_From">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.coverage')" />
      </xsl:attribute>
    </field>
    <field name="date.published_Original_Till">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.coverage')" />
      </xsl:attribute>
    </field>
    <field name="size">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.size')" />
      </xsl:attribute>
    </field>
    <field name="rubric">
      <xsl:attribute name="label">
        <xsl:value-of select="i18n:translate('editormask.labels.rubric')" />
      </xsl:attribute>
    </field>
  </xsl:variable>

  <xsl:template mode="searchResults" match="doc">
    <xsl:variable name="mcrId" select="str[@name='id']" />
    <xsl:choose>
      <xsl:when test="mcrxml:exists($mcrId)">

        <div class="metadata form-group">

          <div class="col-md-2">
            <xsl:variable name="mcrObj" select="document(concat('mcrobject:', $mcrId))/mycoreobject" />
            <xsl:call-template name="derivatePreview">
              <xsl:with-param name="mcrObj" select="$mcrObj" />
            </xsl:call-template>
          </div>

          <div class="col-md-10">
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
          </div>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <!-- object doesn't exist in mycore -> delete it in solr -->
        <xsl:value-of select="solrxml:delete($mcrId)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="searchHitLabel" match="doc">
    <h3 class="jp-layout-clickLabel">
      <!-- TODO: do not pass the hl parameter, instead we should pass a generated response id. with this id we could get the response from cache 
        and use it in our metadata object -->
      <xsl:variable name="hl" select="../../lst[@name='responseHeader']/lst[@name='params']/str[@name='qry']" />
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="concat($WebApplicationBaseURL, 'receive/', str[@name='id'])" />
          <xsl:if test="$hl != ''">
            <xsl:value-of select="concat('?hl=', $hl)" />   
          </xsl:if>
        </xsl:attribute>
        <xsl:attribute name="title">
          <xsl:apply-templates mode="searchHitLabelText" select="." />
        </xsl:attribute>
        <xsl:call-template name="shortenString">
          <xsl:with-param name="string">
            <xsl:apply-templates mode="searchHitLabelText" select="." />
          </xsl:with-param>
          <xsl:with-param name="length" select="190" />
        </xsl:call-template>
      </a>
    </h3>
  </xsl:template>

  <xsl:template mode="searchHitLabelURL" match="searchHitLabelURL[subselectEnd]">
    <xsl:value-of select="subselectEnd/@value" />
    <xsl:apply-templates mode="urlParam" select="param" />
  </xsl:template>

  <xsl:template mode="urlParam" match="param">
    <xsl:choose>
      <xsl:when test="position()=1">
        <xsl:value-of select="'?'" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'&amp;'" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="concat(@name,'=',@value)" />
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

  <xsl:template mode="searchHitDataField" match="str[@name='date.published_from']">
    <span class="jp-layout-inList">
      <xsl:value-of select="." />
      <xsl:if test="../str[@name='date.published_until']">
        <xsl:value-of select="concat(' - ', ../str[@name='date.published_until'])" />
      </xsl:if>
    </span>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="arr[@name='participant.author']/str">
    <span class="jp-layout-inList">
      <a href="{$WebApplicationBaseURL}receive/{substring-before(., '#')}">
        <xsl:value-of select="substring-after(., '#')" />
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
    <xsl:value-of select="i18n:translate('jp.metadata.facet.objectType.person')" />
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jpinst']">
    <xsl:value-of select="i18n:translate('jp.metadata.facet.objectType.jpinst')" />
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jpjournal']">
    <xsl:value-of select="i18n:translate('jp.metadata.facet.objectType.jpjournal')" />
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jpvolume']">
    <span class="jp-layout-label">
      <xsl:value-of select="i18n:translate('jp.metadata.search.volume_published')" />
    </span>
    <xsl:call-template name="resultListBreadcrumb">
      <xsl:with-param name="objID" select="../str[@name='id']" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="searchHitDataField" match="str[@name='objectType' and text() = 'jparticle']">
    <span class="jp-layout-label">
      <xsl:value-of select="i18n:translate('jp.metadata.search.article_published')" />
    </span>
    <xsl:call-template name="resultListBreadcrumb">
      <xsl:with-param name="objID" select="../str[@name='id']" />
    </xsl:call-template>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * SORT -->
  <!-- *************************************************** -->
  <xsl:template mode="jp.response.sort" match="response">
    <xsl:variable name="sort" select="lst[@name='responseHeader']/lst[@name='params']/str[@name='sort']/text()" />
    <xsl:variable name="sortOptionsXML">
      <option value="score desc">
        <xsl:value-of select="i18n:translate('jp.metadata.search.score_desc')" />
      </option>
      <option value="published_sort asc">
        <xsl:value-of select="i18n:translate('jp.metadata.search.published_sort_asc')" />
      </option>
      <option value="published_sort desc">
        <xsl:value-of select="i18n:translate('jp.metadata.search.published_sort desc')" />
      </option>
      <option value="alphabetic_sort asc">
        <xsl:value-of select="i18n:translate('jp.metadata.search.alphabetic_sort_asc')" />
      </option>
    </xsl:variable>
    <xsl:variable name="sortOptions" select="xalan:nodeset($sortOptionsXML)" />
    <div class="form-group">
      <h2 class="jp-layout-resultLCaption">
        <xsl:value-of select="i18n:translate('jp.metadata.search.sort')" />
      </h2>
      <select id="sortSelect">
        <xsl:apply-templates select="$sortOptions/option" mode="jp.response.sort.option">
          <xsl:with-param name="selected" select="$sort" />
        </xsl:apply-templates>
      </select>
    </div>
  </xsl:template>

  <xsl:template match="option" mode="jp.response.sort.option">
    <xsl:param name="selected" />
    <xsl:element name="option">
      <xsl:attribute name="value">
        <xsl:value-of select="@value" />
      </xsl:attribute>
      <xsl:if test="@value = $selected">
        <xsl:attribute name="selected">
          <xsl:value-of select="'selected'" />
        </xsl:attribute>
      </xsl:if>
      <xsl:value-of select="text()" />
    </xsl:element>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * FACET -->
  <!-- *************************************************** -->
  <xsl:template mode="facetList" match="lst">
  </xsl:template>
  <xsl:template mode="facetGroup" match="lst">
  </xsl:template>

  <xsl:template mode="facetList" match="lst[@name='facet_fields']">
    <xsl:param name="isSelected" />
    <xsl:apply-templates mode="facetGroup" select="lst" >
      <xsl:with-param name="isSelected" select="$isSelected" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="facetGroup" match="lst[count(*) &gt; 0]">
    <xsl:param name="isSelected" />
    
    <xsl:choose>
      <xsl:when test="$isSelected = false()"> 
        <div class="form-group">
          <a class="dt-collapse" data-toggle="collapse">
              <xsl:attribute name="data-target">
                  <xsl:value-of select="concat('#', @name)" />
              </xsl:attribute>
            <span>
              <xsl:value-of select="i18n:translate(concat('jp.metadata.facet.', @name))" />
            </span>
            <i class="fa fa-sort-asc"></i>
            <i class="fa fa-sort-desc"></i>
          </a>
          <div class="collapse in list-group jp-list-group-special" id="{@name}">
            <xsl:apply-templates select="int" mode="facetField">
              <xsl:with-param name="facet" select="@name" />
              <xsl:with-param name="isSelected" select="$isSelected" />
            </xsl:apply-templates>
          </div>
        </div>
      </xsl:when>
      <xsl:when test="$isSelected = true()">
        <xsl:apply-templates select="int" mode="facetField">
            <xsl:with-param name="facet" select="@name" />
            <xsl:with-param name="isSelected" select="$isSelected" />
         </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="facetField" match="int">
    <xsl:param name="facet" />
    <xsl:param name="isSelected" />
    <xsl:variable name="value" select="@name" />
    <xsl:variable name="count" select="text()" />

    <xsl:variable name="selected" select="jpxml:isFacetSelected($RequestURL, $facet, $value)" />
    
    <xsl:if test="$selected = $isSelected">
      <xsl:variable name="href">
        <xsl:choose>
          <xsl:when test="$selected">
            <xsl:value-of select="jpxml:removeFacet($RequestURL, $facet, $value)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat($RequestURL, '&amp;fq=', $facet, ':', $value)" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="class">
        <xsl:if test="$selected">
          <xsl:value-of select="'selected'" />
        </xsl:if>
      </xsl:variable>
      <xsl:variable name="text">
        <xsl:variable name="facetSettings" select="$settings/facet[@name=$facet]" />
        <xsl:choose>
          <xsl:when test="$facetSettings/@translate = 'true'">
            <xsl:value-of select="i18n:translate(concat('jp.metadata.facet.', $facet, '.', $value))" />
          </xsl:when>
          <xsl:when test="$facetSettings/@mcrid = 'true'">
            <xsl:variable name="mcrObj" select="document(concat('mcrobject:', $value))/mycoreobject" />
            <xsl:apply-templates mode="printTitle"
              select="$mcrObj/metadata/maintitles/maintitle[@inherited='0']|$mcrObj/metadata/def.heading/heading|$mcrObj/metadata/names[@class='MCRMetaInstitutionName']/name" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$value" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
  
      <a class="list-group-item {$class}" href="{$href}">
        <i class="icon {$class}"></i>
        <span class="text">
          <xsl:value-of select="$text" />
        </span>
        <span class="pull-right">
          <xsl:value-of select="$count" />
        </span>
      </a>
    </xsl:if>    
  </xsl:template>

</xsl:stylesheet>