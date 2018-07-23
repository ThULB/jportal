<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:decoder="java.net.URLDecoder"
                xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="jpxml xalan mcrxml solrxml i18n decoder">

  <xsl:variable name="_xed_subselect_session">
    <xsl:call-template name="UrlGetParam">
      <xsl:with-param name="url" select="$RequestURL" />
      <xsl:with-param name="par" select="'_xed_subselect_session'" />
    </xsl:call-template>
  </xsl:variable>

  <!-- subselect param -->
  <xsl:param name="subselect.type" select="''" />
  <xsl:param name="subselect.session" />
  <xsl:param name="subselect.varpath" />
  <xsl:param name="subselect.webpage" />

  <xsl:variable name="subselectXML">
    <subselect>
      <param name="subselect.type" value="{$subselect.type}" />
      <param name="subselect.session" value="{$subselect.session}" />
      <param name="_xed_subselect_session" value="{$_xed_subselect_session}" />
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
    <xsl:variable name="qry">
      <xsl:call-template name="UrlGetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="'qry'" />
      </xsl:call-template>
    </xsl:variable>

    <xsl:if test="$qry != ''">
      <xsl:attribute name="value">
        <xsl:value-of select="$qry" />
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="renderView" match="@placeholder[contains(.,'{subselect.search.input.placeholder}')]">
    <xsl:attribute name="placeholder">
      <xsl:value-of select="i18n:translate(concat('subselect.search.input.placeholder.', $subselect.type))" />
    </xsl:attribute>
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
      <xsl:value-of select="i18n:translate(concat('subselect.type.label.', $subselect.type))" />
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="renderView" match="@value[contains(.,'{subselect.type}')]">
    <xsl:attribute name="value">
      <xsl:value-of select="$subselect.type" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="renderView" match="@value[contains(.,'{_xed_subselect_session}')]">
    <xsl:attribute name="value">
        <xsl:value-of select="$_xed_subselect_session" />
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
      <xsl:with-param name="view" select="a[contains(@class,'list-group-item')]" />
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

  <xsl:template mode="renderView" match="getData/ref"/>
  <xsl:template mode="renderView" match="getData/ref[contains(@objectType, $subselect.type) and @name]">
    <xsl:param name="data" />
    <xsl:variable name="name" select="@name"/>

    <xsl:apply-templates mode="getData" select="$data/node()[@name=$name]">
      <xsl:with-param name="ref" select="."/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="getData" match="str[@name = 'ancestorPath']">
    <xsl:apply-templates mode="ancestorPath" select="."/>
  </xsl:template>

  <xsl:template mode="ancestorPath" match="text()[contains(., '/')]">
    <xsl:apply-templates mode="ancestorPath" select="xalan:nodeset(substring-before(.,'/'))"/>
    <xsl:value-of select="' / '"/>
    <xsl:apply-templates mode="ancestorPath" select="xalan:nodeset(substring-after(.,'/'))"/>
  </xsl:template>

  <xsl:template mode="ancestorPath" match="text()[not(contains(., '/'))]">
    <xsl:if test=". != ''">
      <xsl:call-template name="shortenString">
        <xsl:with-param name="string"
                        select="document(concat('mcrobject:', .))/mycoreobject/metadata/maintitles/maintitle[@inherited='0']"/>
        <xsl:with-param name="length" select="20"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="getData" match="str[@name and contains('published|dateOfBirth|dateOfDeath', @name)]">
    <xsl:param name="ref"/>

    <xsl:apply-templates mode="prePost" select="$ref">
      <xsl:with-param name="value" select="jpxml:formatSolrDate(., $CurrentLang)"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="getData" match="str">
    <xsl:param name="ref"/>

    <xsl:apply-templates mode="prePost" select="$ref">
      <xsl:with-param name="value" select="."/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="prePost" match="ref">
    <xsl:param name="value"/>

    <xsl:apply-templates mode="getData" select="pre"/>
    <xsl:apply-templates mode="getData" select="pre/@i18n"/>
    <xsl:apply-templates mode="getData" select="pre/@separator"/>
    <xsl:value-of select="$value" />
    <xsl:apply-templates mode="getData" select="post/@i18n"/>
    <xsl:apply-templates mode="getData" select="post/@separator"/>
    <xsl:apply-templates mode="getData" select="post"/>
  </xsl:template>

  <xsl:template mode="getData" match="@i18n">
    <xsl:value-of select="i18n:translate(.)"/>
  </xsl:template>

  <xsl:template mode="getData" match="@*|node()">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template mode="renderView" match="@data-jp-mcrid[contains(.,'{result.hit.id}')]">
    <xsl:param name="data" />
    <xsl:attribute name="data-jp-mcrid">
      <xsl:value-of select="$data/str[@name='id']" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="renderView" match="@data-submit-url[contains(.,'{result.hit.id}')]">
    <xsl:param name="data" />
    <xsl:variable name="url">
      <url>
        <xsl:choose>
          <xsl:when test="$subselect.session = ''">
            <base>
              <xsl:value-of select="concat($WebApplicationBaseURL, 'servlets/XEditor')" />
            </base>
            <param name="_xed_submit_return" value="submit" />
            <param name="_xed_session" value="{$_xed_subselect_session}" />
            <param name="@xlink:href" value="{$data/str[@name='id']}" /> <!-- _var_@xlink:href -->
            <param name="@xlink:title" value="{$data/str[@name='heading']}" encode="false" /> <!-- _var_@xlink:title -->
          </xsl:when>
          <xsl:otherwise>
            <base>
              <xsl:value-of select="concat($WebApplicationBaseURL, 'servlets/XMLEditor')" />
            </base>
            <param name="_action" value="end.subselect" />
            <xsl:copy-of select="$subselectParam/subselect/param" />
            <param name="mode" value="prefix" />
            <param name="_var_@xlink:href" value="{$data/str[@name='id']}" /> <!-- _var_@xlink:href -->
            <param name="_var_@xlink:title" value="{$data/str[@name='heading']}" encode="false" /> <!-- _var_@xlink:title -->
          </xsl:otherwise>
        </xsl:choose>
      </url>
    </xsl:variable>

    <xsl:attribute name="data-submit-url">
      <xsl:apply-templates mode="createURL" select="xalan:nodeset($url)/url" />
    </xsl:attribute>
  </xsl:template>

  <!-- Rendering view for result list paginator -->
  <xsl:template mode="renderView" match="component[@id='resultPaginator']">
    <xsl:param name="data" />
    <xsl:apply-templates select="$data" mode="jp.pagination" />
  </xsl:template>

  <xsl:template mode="renderView" match="@src[contains(., '{WebApplicationBaseURL}')]">
    <xsl:attribute name="src">
      <xsl:value-of select="concat($WebApplicationBaseURL, substring-after(.,'{WebApplicationBaseURL}'))" />
    </xsl:attribute>
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

  <xsl:template mode="renderView" match="@action[.='{subselect.url}']">
    <xsl:attribute name="action">
        <xsl:value-of select="concat($WebApplicationBaseURL,'servlets/solr/subselect')" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="renderView" match="@href[.='{subselect.cancel.link}']">
    <xsl:param name="data" />
    <xsl:attribute name="href">
      <xsl:value-of select="concat($WebApplicationBaseURL, 'servlets/XEditor?_xed_submit_return=cancel&amp;_xed_session=', $_xed_subselect_session)" />
    </xsl:attribute>
  </xsl:template>

</xsl:stylesheet>