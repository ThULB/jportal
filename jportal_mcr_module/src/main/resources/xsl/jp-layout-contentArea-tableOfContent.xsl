<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:mcr="http://www.mycore.org/" xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions"
  xmlns:math="xalan://java.lang.Math"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" exclude-result-prefixes="xalan encoder mcr mcrxml solrxml">
  <xsl:param name="vol.start" select="'0'" />
  <xsl:param name="art.start" select="'0'" />

  <xsl:template name="tableOfContent">
    <xsl:param name="id" />

    <xsl:variable name="findVolQuery" select="encoder:encode(concat('+parent:', $id, ' +objectType:jpvolume'))" />
    <xsl:variable name="numPerPage_vol" select="$settings/numPerPage[@for='volume']" />
    <xsl:variable name="sort_vol">
      <xsl:value-of select="'indexPosition%20asc'" />
      <xsl:choose>
        <xsl:when test="$isPartOfOnlineJournal">
          <xsl:value-of select="',date.published%20desc'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="',date.published%20asc'" />
        </xsl:otherwise>
      </xsl:choose>
      <xsl:value-of select="',maintitle%20asc'" />
    </xsl:variable>
    <xsl:variable name="volumes"
      select="document(concat('solr:q=', $findVolQuery, '&amp;sort=', $sort_vol, '&amp;rows=', $numPerPage_vol,'&amp;start=', $vol.start))" />
    <xsl:if test="$volumes/response/result/@numFound &gt; 0">
      <li>
        <div id="jp-tableOfContent" class="jp-layout-tableOfContent">
          <h3>Inhaltsverzeichnis</h3>
          <ul>
            <xsl:apply-templates mode="jp.printListEntryContent" select="$volumes/response/result/doc" />
          </ul>
          <xsl:apply-templates mode="jp.pagination" select="$volumes/response">
            <xsl:with-param name="startParam" select="'XSL.vol.start'" />
          </xsl:apply-templates>
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
          <xsl:apply-templates mode="jp.pagination" select="$articles/response">
            <xsl:with-param name="startParam" select="'XSL.art.start'" />
          </xsl:apply-templates>
        </div>
      </li>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="artList" match="doc">
    <xsl:variable name="mcrId" select="str[@name='id']" />
    <xsl:choose>
      <xsl:when test="mcrxml:exists($mcrId)">
        <xsl:variable name="fields">
          <field name="participant.author" label="Autor" />
          <field name="date.published" label="Erschienen" />
          <field name="date.published_Original" label="Erscheinungsjahr des rez. Werkes" />
          <field name="date.published_Original_From" label="Erscheinungsbeginn der rez. Werke" />
          <field name="date.published_Original_Till" label="Erscheinungsende der rez. Werke" />
          <field name="size" label="Seitenbereich" />
          <field name="rubric" label="Rubrik" />
        </xsl:variable>
        <xsl:variable name="doc" select="." />
        <li>
          <ul class="metadata jp-layout-metadaInSearchResults">
            <li>
              <a href="{$WebApplicationBaseURL}receive/{$mcrId}" class="title">
                <xsl:value-of select="str[@name='maintitle']" />
              </a>
            </li>
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
          <xsl:variable name="mcrObj" select="document(concat('mcrobject:', $mcrId))/mycoreobject" />
          <xsl:call-template name="derivatePreview">
            <xsl:with-param name="mcrObj" select="$mcrObj" />
          </xsl:call-template>
        </li>
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
    <xsl:value-of select="concat(' (', text(), ')')" />
  </xsl:template>
  <xsl:template mode="jp.toc.published" match="str[@name='date.published_from']">
    <xsl:value-of select="concat(' (', text())" />
    <xsl:if test="../str[@name='date.published_until']">
      <xsl:value-of select="concat(' - ', ../str[@name='date.published_until'])" />
    </xsl:if>
    <xsl:value-of select="')'" />
  </xsl:template>

</xsl:stylesheet>
