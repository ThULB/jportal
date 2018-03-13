<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcr="http://www.mycore.org/"
                xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:decoder="xalan://java.net.URLDecoder"
                xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
                exclude-result-prefixes="xalan encoder mcr mcrxml solrxml i18n decoder jpxml">

<xsl:param name="referer" />
  <xsl:param name="vol.start" />
  <xsl:param name="art.start" />
  <xsl:param name="q" />

  <xsl:template name="tableOfContent">
    <xsl:param name="id" />
    <xsl:call-template name="jp.toc.printVolumes">
      <xsl:with-param name="parentID" select="$id" />
    </xsl:call-template>
    <xsl:call-template name="jp.toc.printArticles">
      <xsl:with-param name="parentID" select="$id" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="jp.toc.printVolumes">
    <xsl:param name="parentID" />
    <xsl:variable name="rows" select="$settings/numPerPage[@for='volume']" />
    <xsl:variable name="start">
      <xsl:choose>
        <xsl:when test="$vol.start">
          <xsl:value-of select="$vol.start" />
        </xsl:when>
        <xsl:when test="$referer and contains($referer, '_jpvolume_')">
          <xsl:value-of select="concat('ref=', $referer)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'0'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="volumes" select="document(concat('toc:', $parentID, ':jpvolume:', $rows, ':', $start))" />

    <xsl:if test="$volumes/response/result/@numFound &gt; 0">
      <div id="jp-tableOfContent" class="jp-layout-tableOfContent jp-content-block row">
        <ul>
          <xsl:variable name="docSize" select="count($volumes/response/result/doc)" />
          <xsl:variable name="maxCharacters">
            <xsl:for-each select="$volumes/response/result/doc/str[@name='maintitle']">
              <xsl:sort select="string-length(text())" data-type="number" order="descending"/>
              <xsl:if test="position() = 1">
                <xsl:value-of select="string-length(text())" />
              </xsl:if>
            </xsl:for-each>
          </xsl:variable>
          <xsl:attribute name="style">
            <xsl:value-of select="'column-count: '" />
            <xsl:choose>
              <xsl:when test="$docSize &gt; 31 and $maxCharacters &lt; 35">
                <xsl:value-of select="'4'" />
              </xsl:when>
              <xsl:when test="$docSize &gt; 21 and $maxCharacters &lt; 50">
                <xsl:value-of select="'3'" />
              </xsl:when>
              <xsl:when test="$docSize &gt; 11">
                <xsl:value-of select="'2'" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'1'" />
              </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="';'" />
          </xsl:attribute>
          <xsl:apply-templates mode="jp.printListEntryContent" select="$volumes/response/result/doc" />
        </ul>
        <xsl:apply-templates mode="jp.pagination" select="$volumes/response">
          <xsl:with-param name="startParam" select="'XSL.vol.start'" />
        </xsl:apply-templates>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.toc.printArticles">
    <xsl:param name="parentID" />
    <xsl:variable name="rows" select="$settings/numPerPage[@for='article']" />
    <xsl:variable name="start">
      <xsl:choose>
        <xsl:when test="$art.start">
          <xsl:value-of select="$art.start" />
        </xsl:when>
        <xsl:when test="$referer and contains($referer, '_jparticle_')">
          <xsl:value-of select="concat('ref=', $referer)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'0'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="articles" select="document(concat('toc:', $parentID, ':jparticle:', $rows, ':', $start))" />

    <xsl:if test="$articles/response/result/@numFound &gt; 0">
      <div id="jp-tableOfContent" class="jp-layout-tableOfContent container-fluid jp-objectlist jp-content-block row">
        <xsl:apply-templates mode="artList" select="$articles/response/result/doc" />
        <xsl:apply-templates mode="jp.pagination" select="$articles/response">
          <xsl:with-param name="startParam" select="'XSL.art.start'" />
        </xsl:apply-templates>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.toc.getRefererStart">
    <xsl:param name="q" />
    <xsl:param name="sort" />
    <xsl:param name="rows" />
    <xsl:variable name="xml" select="document(concat('solr:q=', $q, '&amp;sort=', $sort, '&amp;rows=99999&amp;fl=id'))" />
    <xsl:variable name="positionInParent" select="count($xml/response/result/doc[str[@name] = $referer]/preceding-sibling::*)" />
    <xsl:value-of select="floor($positionInParent div $rows) * $rows" />
  </xsl:template>

  <xsl:template mode="artList" match="doc">
    <xsl:variable name="mcrId" select="str[@name='id']" />
    <xsl:choose>
      <xsl:when test="mcrxml:exists($mcrId)">
        <xsl:variable name="fields">
          <field name="participant.author" i18n="editormask.labels.author" />
          <field name="date.published" i18n="metaData.date.published" />
          <field name="date.reviewedWork" i18n="metaData.date.reviewedWork" />
          <field name="date.reportingPeriod" i18n="metaData.date.reportingPeriod" />
          <field name="size" i18n="editormask.labels.size" />
          <field name="rubric" i18n="editormask.labels.rubric" />
        </xsl:variable>
        <xsl:variable name="doc" select="." />
        <div class="row jp-objectlist-object">
          <div class="jp-objectlist-thumbnail">
            <xsl:variable name="mcrObj" select="document(concat('mcrobject:', $mcrId))/mycoreobject" />
            <xsl:apply-templates select="$mcrObj" mode="derivateDisplay">
              <xsl:with-param name="mode" select="'preview'" />
              <xsl:with-param name="editable" select="'false'" />
              <xsl:with-param name="query" select="$q" />
            </xsl:apply-templates>
          </div>
          <div class="jp-objectlist-metadata">
            <h3 class="jp-layout-clickLabel">
              <a href="{$WebApplicationBaseURL}receive/{$mcrId}" class="title">
                <xsl:value-of select="str[@name='maintitle']" />
              </a>
            </h3>
            <ul class="jp-layout-metadaInSearchResults">
              <xsl:for-each select="xalan:nodeset($fields)/field">
                <xsl:variable name="fieldName" select="@name" />
                <xsl:if test="$doc/*[@name = $fieldName]">
                  <li>
                    <span class="jp-layout-label">
                      <xsl:value-of select="i18n:translate(@i18n)"/>
                    </span>
                    <xsl:apply-templates mode="artEntryFields" select="$doc/*[@name = $fieldName]" />
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

  <xsl:template mode="artEntryFields" match="str">
    <xsl:value-of select="." />
  </xsl:template>

  <xsl:template mode="artEntryFields" match="arr[@name='rubric']/str">
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

  <xsl:template mode="artEntryFields" match="arr[@name='participant.author']/str">
    <span class="jp-layout-inList">
      <a href="{$WebApplicationBaseURL}receive/{substring-before(., '#')}">
        <xsl:value-of select="substring-after(., '#')" />
      </a>
    </span>
  </xsl:template>

  <xsl:template mode="artEntryFields" match="str[contains(@name, 'date.')]">
    <span class="jp-layout-inList">
      <xsl:value-of select="jpxml:formatSolrDate(text(), $CurrentLang)" />
    </span>
  </xsl:template>

  <xsl:template mode="jp.printListEntryContent" match="doc">
    <xsl:variable name="mcrId" select="str[@name='id']" />
    <xsl:choose>
      <xsl:when test="mcrxml:exists($mcrId)">
        <li>
          <a href="{$WebApplicationBaseURL}receive/{$mcrId}">
            <xsl:value-of select="str[@name='maintitle']" />
            <xsl:apply-templates mode="jp.toc.published" select="str" />
          </a>
        </li>
      </xsl:when>
      <xsl:otherwise>
        <!-- object doesn't exist in mycore -> delete it in solr -->
        <xsl:value-of select="solrxml:delete($mcrId)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="jp.toc.published" match="str">
  </xsl:template>

  <xsl:template mode="jp.toc.published" match="str[@name='date.published']">
    <!-- only apply date when the main title is not equal this date -->
    <xsl:if test="../str[@name='maintitle']/text() != text()">
      <xsl:value-of select="concat(' (', jpxml:formatSolrDate(text(), $CurrentLang), ')')" />
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
