<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  exclude-result-prefixes="xalan mcrxml solrxml">

  <!-- subselect param -->
  <xsl:param name="subselect.type" select="''" />
  <xsl:param name="subselect.session" />
  <xsl:param name="subselect.varpath" />
  <xsl:param name="subselect.webpage" />

  <xsl:variable name="subselectXML">
    <subselect>
      <param name="subselect.type" value="{$subselect.type}" />
      <param name="subselect.session" value="{$subselect.session}" />
      <param name="subselect.varpath" value="{$subselect.varpath}" />
      <param name="subselect.webpage" value="{$subselect.webpage}" />
    </subselect>
  </xsl:variable>
  <xsl:variable name="subselectParam" select="xalan:nodeset($subselectXML)" />

  <xsl:variable name="subselectTypeLabel">
    <xsl:choose>
      <xsl:when test="$subselect.type = 'person'">
        <xsl:value-of select="'Person'" />
      </xsl:when>
      <xsl:when test="$subselect.type = 'jpinst'">
        <xsl:value-of select="'Institution'" />
      </xsl:when>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="subselectView" select="document('../xml/views/subselectView.xml')/view" />

  <xsl:template mode="renderView" match="@*|node()">
    <xsl:param name="data" />
    <xsl:copy>
      <xsl:apply-templates mode="renderView" select="@*|node()">
        <xsl:with-param name="data" select="$data" />
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="jpsearch" mode="subselect.form">
    <xsl:apply-templates mode="renderView" select="$subselectView/component[@name='subelectForm']/*" />
  </xsl:template>

  <xsl:template mode="controllerHook" match="/jpsearchBar[@mode='subselect.form' or @mode='subselect.result']">
    <xsl:apply-templates mode="renderView" select="$subselectView/component[@name='subelectFormInput']/*">
      <xsl:with-param name="data" select="$subselectParam/subselect" />
    </xsl:apply-templates>
  </xsl:template>

  <!-- Rendering search form view in subselect -->
  <xsl:template mode="renderView" match="@value[contains(.,'{subselect.type.label}')]">
    <xsl:attribute name="value">
      <xsl:value-of select="concat($subselectTypeLabel,'enname', substring-after(.,'{subselect.type.label}'))" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="renderView" match="@getData[.='subselect.search.form.qt']">
    <xsl:attribute name="placeholder">
      <xsl:choose>
        <xsl:when test="$subselect.type = 'person'">
          <xsl:value-of select="'Personenname'" />
        </xsl:when>
        <xsl:when test="$subselect.type = 'jpinst'">
          <xsl:value-of select="'Institutionsname'" />
        </xsl:when>
      </xsl:choose>
      <xsl:value-of select="' eingeben'" />
    </xsl:attribute>
    <xsl:if test="$qt != '' and $qt != '*'">
      <xsl:attribute name="value">
        <xsl:value-of select="$qt" />
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="renderView" match="@getData[.='subselect.search.form.sort']">
    <xsl:if test="../@value = $sort">
      <xsl:attribute name="selected">
        <xsl:value-of select="'selected'" />
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="renderView" match="h2[.='{subselect.type.label}']">
    <xsl:copy>
      <xsl:value-of select="concat($subselectTypeLabel, 'enauswahl')" />
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="renderView" match="@value[contains(.,'{subselect.type}')]">
    <xsl:attribute name="value">
      <xsl:value-of select="$subselect.type" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="renderView" match="@value[contains(.,'{subselect.session}')]">
    <xsl:attribute name="value">
      <xsl:value-of select="$subselect.session" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="renderView" match="@value[contains(.,'{subselect.varpath}')]">
    <xsl:attribute name="value">
      <xsl:value-of select="$subselect.varpath" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="renderView" match="@value[contains(.,'{subselect.webpage}')]">
    <xsl:attribute name="value">
      <xsl:value-of select="$subselect.webpage" />
    </xsl:attribute>
  </xsl:template>
  <!-- ############################################################################## -->

  <!-- create search query -->
  <xsl:template match="jpsearch" mode="subselect.result">
    <xsl:variable name="queryXML">
      <query>
        <queryTerm value="{{!q.op=AND}}{$qt}" />
        <queryTermField name="objectType" value="{$subselect.type}" />
        <param name="qf" value="heading^10 dates^10 alternatives^5 heading_de^5 alternatives_de^3 allMeta^1" />
        <param name="rows" value="{$rows}" />
        <param name="start" value="{$start}" />
        <param name="defType" value="edismax" />
        <xsl:if test="$sort = 'alphabetical'">
          <param name="sort" value="heading_lowercase asc" />
        </xsl:if>
      </query>
    </xsl:variable>
    <xsl:variable name="query">
      <xsl:apply-templates mode="createSolrQuery" select="xalan:nodeset($queryXML)/query" />
    </xsl:variable>
    <xsl:apply-templates mode="renderView" select="$subselectView/component[@name='resultList']/*">
      <xsl:with-param name="data" select="document($query)/response" />
    </xsl:apply-templates>
    <xsl:apply-templates mode="renderView" select="$subselectView/component[@name='objectPreview']/*" />
  </xsl:template>

  <!-- Rendering view for result list -->
  <xsl:template mode="renderView" match="getData[@id='search.numFound']">
    <xsl:param name="data" />
    <xsl:value-of select="$data/result/@numFound" />
  </xsl:template>

  <xsl:template mode="renderView" match="getData[@id='search.query']">
    <xsl:param name="data" />
    <xsl:value-of select="concat(@pre,substring-before($data/lst[@name='responseHeader']/lst[@name='params']/str[@name='q'],' +'),@post)" />
  </xsl:template>

  <!-- Rendering view for result list entry -->
  <xsl:template mode="renderView" match="component[@id='resultListEntry']">
    <xsl:param name="data" />
    <xsl:apply-templates mode="listEntryView" select="$data/result/doc">
      <xsl:with-param name="view" select="li[contains(@class,'resultListEntry')]" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="listEntryView" match="doc">
    <xsl:param name="view" />
    <xsl:variable name="mcrId" select="str[@name='id']" />
    <xsl:choose>
      <xsl:when test="mcrxml:exists($mcrId)">
        <xsl:apply-templates mode="renderView" select="$view">
          <xsl:with-param name="data" select="." />
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <!-- object doesn't exist in mycore -> delete it in solr -->
        <xsl:value-of select="solrxml:delete($mcrId)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="renderView" match="getData[@id='result.hit.heading']">
    <xsl:param name="data" />
    <xsl:value-of select="$data/str[@name='heading']" />
  </xsl:template>

  <xsl:template mode="renderView" match="getData[@id='result.hit.dateOfBirth']">
    <xsl:param name="data" />
    <xsl:if test="$data/str[@name='dateOfBirth']">
      <xsl:value-of select="concat('Geburtsdatum: ', $data/str[@name='dateOfBirth'])" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="renderView" match="getData[@id='result.hit.dateOfDeath']">
    <xsl:param name="data" />
    <xsl:if test="$data/str[@name='dateOfDeath']">
      <xsl:value-of select="concat('Sterbedatum: ', $data/str[@name='dateOfDeath'])" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="renderView" match="@data-jp-mcrid[contains(.,'{result.hit.id}')]">
    <xsl:param name="data" />
    <xsl:attribute name="data-jp-mcrid">
      <xsl:value-of select="$data/str[@name='id']" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="renderView" match="@href[contains(.,'{result.hit.id}')]">
    <xsl:param name="data" />
    <xsl:variable name="url">
      <url>
        <base>/servlets/XMLEditor</base>
        <param name="_action" value="end.subselect" />
        <xsl:copy-of select="$subselectParam/subselect/param" />
        <param name="mode" value="prefix" />
        <param name="_var_@xlink:href" value="{$data/str[@name='id']}" />
        <param name="_var_@xlink:title" value="{$data/str[@name='heading']}" />
      </url>
    </xsl:variable>

    <xsl:attribute name="href">
      <xsl:apply-templates mode="createURL" select="xalan:nodeset($url)/url">
        <xsl:with-param name="encode" select="'false'" />
      </xsl:apply-templates>
    </xsl:attribute>
  </xsl:template>

  <!-- Rendering view for result list paginator -->
  <xsl:template mode="renderView" match="component[@id='resultPaginator']">
    <xsl:param name="data" />

    <xsl:variable name="resultPaginatorParam">
      <result>
        <rows>
          <xsl:value-of select="$data/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']" />
        </rows>
        <numFound>
          <xsl:value-of select="$data/result[@name='response']/@numFound" />
        </numFound>
        <start>
          <xsl:value-of select="$data/result[@name='response']/@start" />
        </start>
      </result>
    </xsl:variable>

    <xsl:variable name="paginatorStart">
      <xsl:choose>
        <xsl:when test="($start - 4*$rows) &gt; $rows">
          <xsl:value-of select="$start - 4*$rows" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="0" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="($start - $rows) &gt;= 0">
      <xsl:variable name="viewData">
        <label>
          <xsl:value-of select="'&lt; Zurück'" />
        </label>
        <href>
          <xsl:call-template name="UrlSetParam">
            <xsl:with-param name="url" select="$RequestURL" />
            <xsl:with-param name="par" select="'XSL.start'" />
            <xsl:with-param name="value" select="$start - $rows" />
          </xsl:call-template>
        </href>
      </xsl:variable>
      <xsl:apply-templates mode="renderView" select="li[contains(@class,'{resultpage}')]">
        <xsl:with-param name="data" select="xalan:nodeset($viewData)" />
      </xsl:apply-templates>
    </xsl:if>

    <xsl:call-template name="createResultPaginator">
      <xsl:with-param name="numEntry" select="$rows" />
      <xsl:with-param name="numFound" select="$data/result[@name='response']/@numFound" />
      <xsl:with-param name="startPage" select="$paginatorStart" />
      <xsl:with-param name="loopCount" select="10" />
      <xsl:with-param name="view" select="li[contains(@class,'{resultpage}')]" />
    </xsl:call-template>

    <xsl:if test="($start + $rows) &lt; $data/result[@name='response']/@numFound">
      <xsl:variable name="viewData">
        <label>
          <xsl:value-of select="'Weiter &gt;'" />
        </label>
        <href>
          <xsl:call-template name="UrlSetParam">
            <xsl:with-param name="url" select="$RequestURL" />
            <xsl:with-param name="par" select="'XSL.start'" />
            <xsl:with-param name="value" select="$start + $rows" />
          </xsl:call-template>
        </href>
      </xsl:variable>
      <xsl:apply-templates mode="renderView" select="li[contains(@class,'{resultpage}')]">
        <xsl:with-param name="data" select="xalan:nodeset($viewData)" />
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

  <xsl:template name="createResultPaginator">
    <xsl:param name="numEntry" />
    <xsl:param name="numFound" />
    <xsl:param name="startPage" />
    <xsl:param name="loopCount" />
    <xsl:param name="view" />

    <xsl:variable name="viewData">
      <label>
        <xsl:value-of select="ceiling($startPage div $rows) + 1" />
      </label>
      <href>
        <xsl:call-template name="UrlSetParam">
          <xsl:with-param name="url" select="$RequestURL" />
          <xsl:with-param name="par" select="'XSL.start'" />
          <xsl:with-param name="value" select="$startPage" />
        </xsl:call-template>
      </href>
      <xsl:if test="$startPage = $start">
        <selected />
      </xsl:if>
    </xsl:variable>

    <xsl:apply-templates mode="renderView" select="$view">
      <xsl:with-param name="data" select="xalan:nodeset($viewData)" />
    </xsl:apply-templates>

    <xsl:if test="$loopCount &gt; 1 and $startPage + $rows &lt; $numFound">
      <xsl:call-template name="createResultPaginator">
        <xsl:with-param name="numEntry" select="$numEntry" />
        <xsl:with-param name="numFound" select="$numFound" />
        <xsl:with-param name="startPage" select="$startPage + $rows" />
        <xsl:with-param name="loopCount" select="$loopCount - 1" />
        <xsl:with-param name="view" select="$view" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="renderView" match="@class[contains(.,'{resultpage}')]">
    <xsl:param name="data" />

    <xsl:if test="$data/selected">
      <xsl:attribute name="class">
        <xsl:value-of select="substring-after(.,'{resultpage} ')" />
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="renderView" match="getData[@id='resultpage.label']">
    <xsl:param name="data" />

    <xsl:value-of select="$data/label" />
  </xsl:template>

  <xsl:template mode="renderView" match="@href[.='{link.to.resultpage}']">
    <xsl:param name="data" />

    <xsl:attribute name="href">
        <xsl:value-of select="$data/href" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="renderView" match="@href[.='{subselect.cancel.link}']">
    <xsl:param name="data" />
    <xsl:attribute name="href">
      <xsl:choose>
        <xsl:when test="contains($subselect.webpage, 'XSL.editor.session.id')">
          <xsl:value-of select="concat('/',$subselect.webpage)" />
        </xsl:when>
        <xsl:otherwise>
         <xsl:value-of select="concat('/',$subselect.webpage,'XSL.editor.session.id=',$subselect.session)" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

</xsl:stylesheet>