<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mcr="http://www.mycore.org/" xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xalan encoder mcr mcrxml solrxml i18n">

  <xsl:param name="referer" />
  <xsl:param name="vol.start" />
  <xsl:param name="art.start" />
  <xsl:param name="rubric" />

  <xsl:template name="tableOfContent">
    <xsl:param name="id" />
    <xsl:call-template name="jp.toc.printVolumes">
      <xsl:with-param name="parentID" select="$id" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="jp.toc.printVolumes">
    <xsl:param name="parentID" />
    <xsl:variable name="q" select="encoder:encode(concat('+parent:', $parentID, ' +objectType:jpvolume'))" />
    <xsl:variable name="rows" select="$settings/numPerPage[@for='volume']" />
    <xsl:variable name="sort">
      <!--<xsl:value-of select="'indexPosition%20asc'" />-->
      <xsl:value-of select="'date.published%20desc'" />
      <xsl:value-of select="',maintitle%20asc'" />
    </xsl:variable>
    <xsl:variable name="start">
      <xsl:choose>
        <xsl:when test="$vol.start">
          <xsl:value-of select="$vol.start" />
        </xsl:when>
        <xsl:when test="$referer">
          <xsl:call-template name="jp.toc.getRefererStart">
            <xsl:with-param name="q" select="$q" />
            <xsl:with-param name="sort" select="$sort" />
            <xsl:with-param name="rows" select="$rows" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'0'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="volumes" select="document(concat('solr:q=', $q, '&amp;sort=', $sort, '&amp;rows=99999&amp;fq=volContentClassi1:volume','&amp;start=', $start))" />
    <xsl:if test="$volumes/response/result/@numFound &gt; 0">
      <xsl:apply-templates mode="jp.printVolumeList" select="$volumes/response/result" />
      <!--<xsl:apply-templates mode="jp.printMonographList" select="$volumes/response/result" />-->
    </xsl:if>

    <xsl:variable name="monograph" select="document(concat('solr:q=', $q, '&amp;sort=', $sort, '&amp;rows=99999&amp;fq=volContentClassi1:monograph','&amp;start=', $start))" />
    <xsl:if test="$monograph/response/result/@numFound &gt; 0">
      <!--<xsl:apply-templates mode="jp.printVolumeList" select="$volumes/response/result" />-->
      <xsl:apply-templates mode="jp.printMonographList" select="$monograph/response/result" />
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.toc.printArticles">
    <xsl:param name="parentID" />
    <xsl:variable name="q" select="encoder:encode(concat('+parent:', $parentID, ' +objectType:jparticle'))" />
    <xsl:variable name="fq" select="encoder:encode(concat('rubricText:', $rubric))" />
    <xsl:variable name="rows" select="$settings/numPerPage[@for='article']" />
    <xsl:variable name="sort" select="'size%20asc,maintitle%20asc'" />
    <xsl:variable name="start">
      <xsl:choose>
        <xsl:when test="$art.start">
          <xsl:value-of select="$art.start" />
        </xsl:when>
        <xsl:when test="$referer">
          <xsl:call-template name="jp.toc.getRefererStart">
            <xsl:with-param name="q" select="$q" />
            <xsl:with-param name="sort" select="$sort" />
            <xsl:with-param name="rows" select="$rows" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'0'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="articles" select="document(concat('solr:q=', $q,'&amp;fq=', $fq, '&amp;sort=', $sort, '&amp;rows=99999','&amp;start=', $start))" />
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
            <field name="participant.author" label="Autor" />
            <field name="date.published" label="Erschienen" />
            <field name="date.published_Original" label="Erscheinungsjahr des rez. Werkes" />
            <field name="date.published_Original_From" label="Erscheinungsbeginn der rez. Werke" />
            <field name="date.published_Original_Till" label="Erscheinungsende der rez. Werke" />
            <field name="size" label="Seitenbereich" />
            <field name="rubric" label="Rubrik" />
          </xsl:variable>
          <xsl:variable name="doc" select="." />
          <div class="jp-objectlist-object">
            <div class="jp-objectlist-metadata">
              <div class="jp-objectlist-title">
                <xsl:choose>
                  <xsl:when test="not(mcrxml:isCurrentUserGuestUser())">
                    <a href="{$WebApplicationBaseURL}receive/{$mcrId}">
                      <xsl:value-of select="str[@name='maintitle']" />
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                      <xsl:value-of select="str[@name='maintitle']" />
                  </xsl:otherwise>
                </xsl:choose>

              </div>
              <xsl:variable name="link" select="arr[@name='derivateLink']" />
              <xsl:if test="$link != ''">
                <a href="{$WebApplicationBaseURL}servlets/MCRFileNodeServlet/{$link}">
                  lesen
                </a>
              </xsl:if>
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

  <xsl:template mode="jp.printListEntryContent" match="doc">
    <xsl:variable name="mcrId" select="str[@name='id']" />
    <xsl:variable name="cat" select="arr[@name='category']" />
    <xsl:variable name="category" select="substring-after($cat, ':')" />
    <xsl:choose>
      <xsl:when test="mcrxml:exists($mcrId)">
        <!--<li>-->
          <a class="list-group-item" href="{$WebApplicationBaseURL}receive/{$mcrId}">
            <xsl:value-of select="str[@name='maintitle']" />
            <xsl:apply-templates mode="jp.toc.published" select="str" />
          </a>
        <!--</li>-->
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

  <xsl:template name="jp.printContentList">
    <xsl:param name="id" />
    <div>
      <a class="dt-collapse" data-toggle="collapse" data-target="#jp-journal-child-list">
        <span class="jp-layout-facet-group-head">
          Inhalt
        </span>
        <i class="fa fa-sort-asc"></i>
        <i class="fa fa-sort-desc"></i>
      </a>
      <div class="collapse in list-group jp-list-group-special" id="jp-journal-child-list">
        <div class="jp-layout-tableOfContent list-group jp-list-group-special">
          <xsl:choose>
            <xsl:when test="$rubric = 'essays'">
              <a class="list-group-item active-list-item" href="{$WebApplicationBaseURL}receive/{$id}?XSL.rubric=essays">
                Aufsätze
              </a>
            </xsl:when>
            <xsl:otherwise>
              <a class="list-group-item" href="{$WebApplicationBaseURL}receive/{$id}?XSL.rubric=essays">
                Aufsätze
              </a>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:choose>
            <xsl:when test="$rubric = 'recension'">
              <a class="list-group-item active-list-item" href="{$WebApplicationBaseURL}receive/{$id}?XSL.rubric=recension">
                Rezensionen
              </a>
            </xsl:when>
            <xsl:otherwise>
              <a class="list-group-item" href="{$WebApplicationBaseURL}receive/{$id}?XSL.rubric=recension">
                Rezensionen
              </a>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="jp.printVolumeList" match="result">
    <div class="list-group">
      <a class="dt-collapse" data-toggle="collapse" data-target="#jp-journal-volume-list">
        <span class="jp-layout-facet-group-head">
          Jahrgänge
        </span>
        <i class="fa fa-sort-asc"></i>
        <i class="fa fa-sort-desc"></i>
      </a>
      <div class="collapse in list-group jp-list-group-special" id="jp-journal-volume-list">
        <div id="jp-tableOfContent -vol" class="jp-layout-tableOfContent list-group jp-list-group-special">
          <xsl:apply-templates mode="jp.printVolume" select="./doc" />
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="jp.printVolume" match="doc">
    <xsl:variable name="cat" select="arr[@name='category']" />
    <xsl:variable name="category" select="substring-after($cat, ':')" />
    <xsl:if test="$category = 'volume'">
      <xsl:apply-templates mode="jp.printListEntryContent" select="." />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="jp.printMonographList" match="result">
    <div class="list-group">
      <a class="dt-collapse" data-toggle="collapse" data-target="#jp-journal-monograph-list">
        <span class="jp-layout-facet-group-head">
          Monographien
        </span>
        <i class="fa fa-sort-asc"></i>
        <i class="fa fa-sort-desc"></i>
      </a>
      <div class="collapse in list-group jp-list-group-special" id="jp-journal-monograph-list">
        <div id="jp-tableOfContent-mono" class="jp-layout-tableOfContent list-group jp-list-group-special">
          <xsl:apply-templates mode="jp.printMonograph" select="./doc" />
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="jp.printMonograph" match="doc">
    <xsl:variable name="cat" select="arr[@name='category']" />
    <xsl:variable name="category" select="substring-after($cat, ':')" />
    <xsl:if test="$category = 'monograph'">
      <xsl:apply-templates mode="jp.printListEntryContent" select="." />
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.toc.buildVolumeSelect">
    <xsl:param name="parentID" />
    <xsl:variable name="q" select="encoder:encode(concat('+parent:', $parentID, ' +objectType:jpvolume'))" />
    <xsl:variable name="rows" select="$settings/numPerPage[@for='volume']" />
    <xsl:variable name="sort">
      <!--<xsl:value-of select="'indexPosition%20asc'" />-->
      <xsl:value-of select="'date.published%20desc'" />
      <xsl:value-of select="',maintitle%20asc'" />
    </xsl:variable>
    <xsl:variable name="start">
      <xsl:choose>
        <xsl:when test="$vol.start">
          <xsl:value-of select="$vol.start" />
        </xsl:when>
        <xsl:when test="$referer">
          <xsl:call-template name="jp.toc.getRefererStart">
            <xsl:with-param name="q" select="$q" />
            <xsl:with-param name="sort" select="$sort" />
            <xsl:with-param name="rows" select="$rows" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'0'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="volumes" select="document(concat('solr:q=', $q, '&amp;sort=', $sort, '&amp;rows=9999','&amp;start=', $start))" />
    <xsl:if test="$volumes/response/result/@numFound &gt; 0">
      <xsl:apply-templates mode="jp.toc.printVolumeSelect" select="$volumes/response/result" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="jp.toc.printVolumeSelect" match="result">
    <div class="jp-volume-sidebar-group">
      <h2 class="jp-volume-selectCaption">
        Jahrgang wechseln
      </h2>
      <select id="jp-volume-select" onchange="location = this.options[this.selectedIndex].value;">
        <xsl:apply-templates mode="jp.toc.printVolume" select="./doc" />
      </select>
    </div>
  </xsl:template>

  <xsl:template mode="jp.toc.printVolume" match="doc">
    <xsl:variable name="cat" select="arr[@name='category']" />
    <xsl:variable name="category" select="substring-after($cat, ':')" />
    <xsl:if test="$category = 'volume'">
      <xsl:variable name="maintitle" select="str[@name='maintitle']" />
      <xsl:variable name="currentID" select="str[@name='id']" />
      <xsl:choose>
        <xsl:when test="$currentID = $currentObjID">
          <option value="{$WebApplicationBaseURL}receive/{$currentID}" selected="true">
            <xsl:value-of select="$maintitle" />
            <xsl:apply-templates mode="jp.toc.published" select="str" />
          </option>
        </xsl:when>
        <xsl:otherwise>
          <option value="{$WebApplicationBaseURL}receive/{$currentID}">
            <xsl:value-of select="$maintitle" />
            <xsl:apply-templates mode="jp.toc.published" select="str" />
          </option>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
