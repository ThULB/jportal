<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:mcr="http://www.mycore.org/">
  <xsl:param name="vol.page" select="'1'" />
  <xsl:param name="art.page" select="'1'" />

  <xsl:template name="tableOfContent">
    <xsl:param name="id" />

    <xsl:variable name="findVolQuery" select="encoder:encode(concat('parent = ', $id, ' and objectType = jpvolume'))" />
    <xsl:variable name="numPerPage_vol" select="$settings/numPerPage[@for='volume']" />
    <xsl:variable name="volumes"
      select="document(concat('query:term=', $findVolQuery, '&amp;sortby=maintitles&amp;order=ascending&amp;numPerPage=', $numPerPage_vol,'&amp;page=', $vol.page))" />
    <xsl:if test="$volumes/mcr:results/@numHits &gt; 0">
      <li>
        <div id="jp-tableOfContent" class="jp-layout-tableOfContent">
          <h3>Inhaltsverzeichnis</h3>
          <ul>
            <xsl:apply-templates mode="printListEntryContent" select="$volumes/mcr:results/mcr:hit" />
          </ul>

          <xsl:if test="$volumes/mcr:results/@numHits &gt; $numPerPage_vol">
            <div class="resultPaginator">
              <span>Seite: </span>
              <menu class="jp-layout-paginator jp-layout-horiz-menu jp-layout-inline">
                <xsl:apply-templates mode="tableOfContentNavi" select="$volumes/mcr:results" >
                  <xsl:with-param name="tocName" select="'XSL.vol.page'"/>
                </xsl:apply-templates>
              </menu>
            </div>
          </xsl:if>
        </div>
      </li>
    </xsl:if>
    <xsl:variable name="findArtQuery" select="encoder:encode(concat('parent = ', $id, ' and objectType = jparticle'))" />
    <xsl:variable name="numPerPage_art" select="$settings/numPerPage[@for='article']" />
    <xsl:variable name="articles"
      select="document(concat('query:term=', $findArtQuery, '&amp;sortby=maintitles&amp;order=ascending&amp;numPerPage=', $numPerPage_art,'&amp;page=', $art.page))" />
    <xsl:if test="$articles/mcr:results/@numHits &gt; 0">
      <li>
        <div id="jp-tableOfContent" class="jp-layout-tableOfContent">
          <h3>Artikel</h3>
          <ul id="artList">
            <xsl:apply-templates mode="artList" select="$articles/mcr:results/mcr:hit" />
            <!-- <xsl:apply-templates mode="artListEntry" select="$articles/mcr:results/mcr:hit" /> -->
          </ul>

          <xsl:if test="$articles/mcr:results/@numHits &gt; $numPerPage_art">
            <div class="resultPaginator">
              <span>Seite: </span>
              <menu class="jp-layout-paginator jp-layout-horiz-menu jp-layout-inline">
                <xsl:apply-templates mode="tableOfContentNavi" select="$articles/mcr:results" >
                  <xsl:with-param name="tocName" select="'XSL.art.page'"/>
                </xsl:apply-templates>
              </menu>
            </div>
          </xsl:if>
        </div>
      </li>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="artList" match="mcr:hit">
    <xsl:variable name="fields">
      <field name="participants_withName" label="Autor" />
      <field name="published_art" label="Erschienen" />
      <field name="published_orig_art" label="Erscheinungsjahr des rez. Werkes" />
      <field name="published_orig_from_art" label="Erscheinungsbeginn der rez. Werke" />
      <field name="published_orig_till_art" label="Erscheinungsende der rez. Werke" />
      <field name="sizes_art" label="Seitenbereich" />
      <field name="rubrics_id" label="Rubrik" />
    </xsl:variable>
    <xsl:variable name="mcrHit" select="." />

    <li>
      <a href="{$WebApplicationBaseURL}receive/{@id}">
        <xsl:value-of select="mcr:metaData/mcr:field[@name='maintitles_plain']" />
      </a>
      <p>
        <ul class="jp-layout-metadaInSearchResults">
          <xsl:for-each select="xalan:nodeset($fields)/field">
            <xsl:variable name="fieldName" select="@name" />
            <xsl:if test="$mcrHit/mcr:metaData/mcr:field[@name = $fieldName]">
              <li>
                <span class="jp-layout-label">
                  <xsl:value-of select="@label" />
                </span>
                <xsl:apply-templates mode="artEntryFields" select="$mcrHit/mcr:metaData/mcr:field[@name = $fieldName]" />
              </li>
            </xsl:if>
          </xsl:for-each>
        </ul>
        
        <xsl:call-template name="derivateDisplay">
          <xsl:with-param name="nodes" select="mcr:metaData/mcr:field[@name='linkDeriv']" />
        </xsl:call-template>
      </p>
    </li>
  </xsl:template>

  <xsl:template mode="artEntryFields" match="mcr:field">
    <xsl:value-of select="." />
  </xsl:template>

  <xsl:template mode="artEntryFields" match="mcr:field[@name='rubrics_id']">
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

  <xsl:template mode="artEntryFields" match="mcr:field[@name='participants_withName']">
    <span class="jp-layout-inList">
      <a href="{$WebApplicationBaseURL}receive/{substring-before(.,'#')}">
        <xsl:value-of select="substring-after(.,'#')" />
      </a>
    </span>
  </xsl:template>

  <xsl:template mode="printListEntryContent" match="mcr:hit">
    <li>
      <a href="{$WebApplicationBaseURL}receive/{@id}">
        <xsl:value-of select="mcr:metaData/mcr:field[@name='maintitles_plain']" />
      </a>
    </li>
  </xsl:template>

  <xsl:template mode="tableOfContentNavi" match="mcr:results">
    <xsl:param name="i" select="1" />
    <xsl:param name="tocName"/>

    <xsl:variable name="url">
      <xsl:call-template name="UrlSetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="$tocName" />
        <xsl:with-param name="value" select="$i" />
      </xsl:call-template>
    </xsl:variable>

    <li>
      <xsl:if test="$i = /mcr:results/@page">
        <xsl:attribute name="class">
            <xsl:value-of select="'jp-layout-selected-underline'" />
          </xsl:attribute>
      </xsl:if>
      <a href="{$url}">
        <xsl:value-of select="$i" />
      </a>
    </li>

    <xsl:if test="$i &lt; @numPages">
      <xsl:apply-templates mode="tableOfContentNavi" select=".">
        <xsl:with-param name="i" select="$i +1" />
        <xsl:with-param name="tocName" select="$tocName" />
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>