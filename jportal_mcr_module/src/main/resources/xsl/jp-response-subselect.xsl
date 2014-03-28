<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:decoder="java.net.URLDecoder"
  exclude-result-prefixes="xalan mcrxml solrxml i18n decoder">

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

  <xsl:template match="/response[lst[@name='responseHeader']/lst[@name='params']/str[@name='XSL.subselect.type']]" priority="1">
    <xsl:apply-templates mode="renderView" select="$subselectView/component[@name='resultList']/*">
      <xsl:with-param name="data" select="." />
    </xsl:apply-templates>
    <xsl:apply-templates mode="renderView" select="$subselectView/component[@name='objectPreview']/*" />
  </xsl:template>

  <xsl:template mode="controllerHook" match="/jpsearchBar[@mode='subselect']">
    <xsl:apply-templates mode="renderView" select="$subselectView/component[@name='subelectFormInput']/*">
      <xsl:with-param name="data" select="$subselectParam/subselect" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="renderView" match="getData[@id='subselect.type.label']">
    <xsl:value-of select="i18n:translate(concat('metaData.', $subselect.type, '.[plural]'))" />
  </xsl:template>

  <xsl:template mode="renderView" match="@getData[.='subselect.search.form.qry']">
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
    <xsl:if test="$qry != ''">
      <xsl:attribute name="value">
        <xsl:value-of select="$qry" />
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="renderView" match="@getData[.='subselect.search.form.sort']">
    <xsl:variable name="sort">
      <xsl:variable name="encodedSort">
        <xsl:call-template name="UrlGetParam">
          <xsl:with-param name="url" select="$RequestURL" />
          <xsl:with-param name="par" select="'sort'" />
        </xsl:call-template>
      </xsl:variable>
      <xsl:value-of select="decoder:decode($encodedSort, 'UTF-8')" />
    </xsl:variable>
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

  <!-- Rendering view for result list -->
  <xsl:template mode="renderView" match="getData[@id='search.numFound']">
    <xsl:param name="data" />
    <xsl:value-of select="$data/result/@numFound" />
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
        <base><xsl:value-of select="concat($WebApplicationBaseURL, 'servlets/XMLEditor')" /></base>
        <param name="_action" value="end.subselect" />
        <xsl:copy-of select="$subselectParam/subselect/param" />
        <param name="mode" value="prefix" />
        <param name="_var_@xlink:href" value="{$data/str[@name='id']}" />
        <param name="_var_@xlink:title" value="{$data/str[@name='heading']}" encode="false"/>
      </url>
    </xsl:variable>

    <xsl:attribute name="href">
      <xsl:apply-templates mode="createURL" select="xalan:nodeset($url)/url" />
    </xsl:attribute>
  </xsl:template>

  <!-- Rendering view for result list paginator -->
  <xsl:template mode="renderView" match="component[@id='resultPaginator']">
    <xsl:param name="data" />
    <xsl:apply-templates select="$data" mode="jp.pagination" />
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