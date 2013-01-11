<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:mcr="http://www.mycore.org/">
  <xsl:param name="vol.start" select="'0'" />
  <xsl:param name="art.start" select="'0'" />

  <xsl:template name="tableOfContent">
    <xsl:param name="id" />

    <xsl:variable name="findVolQuery" select="encoder:encode(concat('+parent:', $id, ' +objectType:jpvolume'))" />
    <xsl:variable name="numPerPage_vol" select="$settings/numPerPage[@for='volume']" />
    <xsl:variable name="volumes"
      select="document(concat('solr:q=', $findVolQuery, '&amp;sort=position%20asc,maintitle%20asc&amp;rows=', $numPerPage_vol,'&amp;start=', $vol.start))" />
    <xsl:if test="$volumes/response/result/@numFound &gt; 0">
      <li>
        <div id="jp-tableOfContent" class="jp-layout-tableOfContent">
          <h3>Inhaltsverzeichnis</h3>
          <ul>
            <xsl:apply-templates mode="printListEntryContent" select="$volumes/response/result/doc" />
          </ul>

          <xsl:if test="$volumes/response/result/@numFound &gt; $numPerPage_vol">
            <div class="resultPaginator">
              <span>Seite: </span>
              <menu class="jp-layout-paginator jp-layout-horiz-menu jp-layout-inline">
                <xsl:apply-templates mode="tableOfContentNavi" select="$volumes/response" >
                  <xsl:with-param name="tocName" select="'XSL.vol.start'"/>
                </xsl:apply-templates>
              </menu>
            </div>
          </xsl:if>
        </div>
      </li>
    </xsl:if>
    <xsl:variable name="findArtQuery" select="encoder:encode(concat('+parent:', $id, ' +objectType:jparticle'))" />
    <xsl:variable name="numPerPage_art" select="$settings/numPerPage[@for='article']" />
    <xsl:variable name="articles"
      select="document(concat('solr:q=', $findArtQuery, '&amp;sort=size%20asc,maintitle%20asc&amp;rows=', $numPerPage_art,'&amp;start=', $art.start))" />
    <xsl:if test="$articles/response/result/@numFound &gt; 0">
      <li>
        <div id="jp-tableOfContent" class="jp-layout-tableOfContent">
          <h3>Artikel</h3>
          <ul id="artList">
            <xsl:apply-templates mode="artList" select="$articles/response/result/doc" />
          </ul>
          <xsl:if test="$articles/response/result/@numFound &gt; $numPerPage_art">
            <div class="resultPaginator">
              <span>Seite: </span>
              <menu class="jp-layout-paginator jp-layout-horiz-menu jp-layout-inline">
                <xsl:apply-templates mode="tableOfContentNavi" select="$articles/response" >
                  <xsl:with-param name="tocName" select="'XSL.art.start'"/>
                </xsl:apply-templates>
              </menu>
            </div>
          </xsl:if>
        </div>
      </li>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="artList" match="doc">
    <xsl:variable name="fields">
      <field name="participants_withName" label="Autor" />
      <field name="date.published" label="Erschienen" />
      <field name="date.published_Original" label="Erscheinungsjahr des rez. Werkes" />
      <field name="date.published_Original_From" label="Erscheinungsbeginn der rez. Werke" />
      <field name="date.published_Original_Till" label="Erscheinungsende der rez. Werke" />
      <field name="size" label="Seitenbereich" />
      <field name="rubric" label="Rubrik" />
    </xsl:variable>
    <xsl:variable name="doc" select="." />
    <xsl:variable name="mcrId" select="str[@name='id']" />
    <li>
      <div class="metadata">
        <a href="{$WebApplicationBaseURL}receive/{$mcrId}">
          <xsl:value-of select="str[@name='maintitle']" />
        </a>
        <p>
          <ul class="jp-layout-metadaInSearchResults">
            <xsl:for-each select="xalan:nodeset($fields)/field">
              <xsl:variable name="fieldName" select="@name" />
              <xsl:if test="$doc/*[@name = $fieldName]">
                <li>
                  <span class="jp-layout-label">
                    <xsl:value-of select="@label" />
                  </span>
                  <xsl:apply-templates mode="artEntryFields" select="$doc/*[@name = $fieldName]" />
                </li>
              </xsl:if>
            </xsl:for-each>
          </ul>
        </p>
      </div>
      <!-- TODO: link derivate -->
      <xsl:variable name="mcrObj" select="document(concat('mcrobject:', $mcrId))/mycoreobject" />
      <xsl:call-template name="derivateDisplay">
        <xsl:with-param name="nodes" select="$mcrObj/metadata/derivateLinks/derivateLink[1]" />
        <xsl:with-param name="journalID" select="$mcrObj//metadata/hidden_jpjournalsID/hidden_jpjournalID" />
      </xsl:call-template>
    </li>
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

  <xsl:template mode="artEntryFields" match="str[@name='participant']">
    <span class="jp-layout-inList">
      <a href="{$WebApplicationBaseURL}receive/{../str[@name='participantID']}">
        <xsl:value-of select="text()" />
      </a>
    </span>
  </xsl:template>

  <xsl:template mode="printListEntryContent" match="doc">
    <li>
      <a href="{$WebApplicationBaseURL}receive/{str[@name='id']}">
        <xsl:value-of select="str[@name='maintitle']" />
        <xsl:if test="str[@name='date.published']">
          <xsl:value-of select="concat(' (', str[@name='date.published'], ')')" />
        </xsl:if>
      </a>
    </li>
  </xsl:template>

  <xsl:template mode="tableOfContentNavi" match="response">
    <xsl:param name="i" select="1" />
    <xsl:param name="tocName"/>

    <xsl:variable name="start" select="result/@start" />
    <xsl:variable name="rows" select="lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']" />
    <xsl:variable name="numFound" select="result/@numFound" />
    <xsl:variable name="page" select="round(($start div $rows) + 0.5)" />
    <xsl:variable name="pages" select="round(($numFound div $rows) + 0.5)" />

    <xsl:variable name="url">
      <xsl:call-template name="UrlSetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="$tocName" />
        <xsl:with-param name="value" select="($i - 1) * $rows" />
      </xsl:call-template>
    </xsl:variable>

    <li>
      <xsl:if test="$i = $page">
        <xsl:attribute name="class">
            <xsl:value-of select="'jp-layout-selected-underline'" />
          </xsl:attribute>
      </xsl:if>
      <a href="{$url}">
        <xsl:value-of select="$i" />
      </a>
    </li>

    <xsl:if test="$i &lt; $pages">
      <xsl:apply-templates mode="tableOfContentNavi" select=".">
        <xsl:with-param name="i" select="$i +1" />
        <xsl:with-param name="tocName" select="$tocName" />
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>