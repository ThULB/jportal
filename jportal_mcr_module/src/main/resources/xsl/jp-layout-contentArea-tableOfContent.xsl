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

  <xsl:variable name="settings" select="document('../xml/layoutDefaultSettings.xml')/layoutSettings" />

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

    <xsl:variable name="volumes" select="document(concat('toc:', $parentID, ':jpvolume:', $rows, ':', $start))/results" />

    <xsl:if test="$volumes/@total &gt; 0">
      <div id="jp-tableOfContent" class="jp-layout-tableOfContent jp-content-block row">
        <ul>
          <xsl:attribute name="style">
            <xsl:value-of select="concat('column-count: ', $volumes/@columns, ';')" />
          </xsl:attribute>
          <xsl:apply-templates mode="jp.printListEntryContent" select="$volumes/result" />
        </ul>

        <xsl:apply-templates mode="jp.pagination" select="$volumes">
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

    <xsl:variable name="articles" select="document(concat('toc:', $parentID, ':jparticle:', $rows, ':', $start))/results" />

    <xsl:if test="$articles/@total &gt; 0">
      <div id="jp-tableOfContent" class="jp-layout-tableOfContent container-fluid jp-objectlist jp-content-block row">
        <xsl:apply-templates mode="artList" select="$articles/result" />

        <xsl:apply-templates mode="jp.pagination" select="$articles">
          <xsl:with-param name="startParam" select="'XSL.art.start'" />
        </xsl:apply-templates>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="artList" match="result">
    <div class="row jp-objectlist-object">
      <div class="jp-objectlist-thumbnail">
        <xsl:variable name="mcrObj" select="document(concat('mcrobject:', @id))/mycoreobject" />
        <xsl:apply-templates select="$mcrObj" mode="derivateDisplay">
          <xsl:with-param name="mode" select="'preview'" />
          <xsl:with-param name="editable" select="'false'" />
          <xsl:with-param name="query" select="$q" />
        </xsl:apply-templates>
      </div>
      <div class="jp-objectlist-metadata">
        <h3 class="jp-layout-clickLabel">
          <a href="{$WebApplicationBaseURL}receive/{@id}" class="title">
            <xsl:value-of select="title" />
          </a>
        </h3>
        <ul class="jp-layout-metadaInSearchResults">
          <xsl:for-each select="author | size | date | rubric">
            <li>
              <span class="jp-layout-label">
                <xsl:variable name="i18n">
                  <xsl:choose>
                    <xsl:when test="@type">
                      <xsl:value-of select="@type" />
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="name()" />
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <xsl:value-of select="i18n:translate($settings/i18n[@tag=$i18n])" />
              </span>
              <span class="jp-layout-inList">
                <xsl:apply-templates select="." mode="artEntryValue" />
              </span>
            </li>
          </xsl:for-each>
        </ul>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="artEntryValue" match="size | date | rubric">
    <xsl:value-of select="." />
  </xsl:template>

  <xsl:template mode="artEntryValue" match="author">
    <a href="{$WebApplicationBaseURL}receive/{@id}">
      <xsl:value-of select="." />
    </a>
  </xsl:template>

  <xsl:template mode="jp.printListEntryContent" match="result">
    <li>
      <a href="{$WebApplicationBaseURL}receive/{@id}">
        <xsl:value-of select="title" />
        <xsl:if test="date[@type = 'published']">
          <xsl:value-of select="concat(' (', date[@type = 'published']/text(), ')')" />
        </xsl:if>
      </a>
    </li>
  </xsl:template>

</xsl:stylesheet>
