<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcr="http://www.mycore.org/"
                xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:dc="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="xalan encoder mcr mcrxml solrxml i18n">

  <xsl:param name="referer" />
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
    <xsl:variable name="sort">
      <!--<xsl:value-of select="'indexPosition%20asc'" />-->
      <xsl:value-of select="'date.published%20desc'" />
      <xsl:value-of select="',maintitle%20asc'" />
    </xsl:variable>
    <xsl:variable name="volumes" select="document(concat('solr:q=', $q, '&amp;sort=', $sort, '&amp;rows=99999'))" />
    <xsl:if test="$volumes/response/result/@numFound &gt; 0">
      <xsl:choose>
        <xsl:when test="$volumes/response/result/doc/arr[@name='volContentClassi1'] != ''">
          <xsl:variable name="classi" select="$volumes/response/result/doc/arr[@name='classification']/str"/>
          <xsl:for-each select="document(concat('classification:metadata:all:children:',$classi))/mycoreclass/categories/category/@ID">
            <xsl:variable name="cat" select="."/>
            <xsl:variable name="catUsed" select="document(concat('solr:q=', $q, '&amp;sort=', $sort, '&amp;rows=99999&amp;fq=', 'volContentClassi1', ':', encoder:encode($cat)))" />
            <xsl:if test="$catUsed/response/result/@numFound &gt; 0">
              <xsl:call-template name="jp.printVolumeListCat">
                <xsl:with-param name="volumes" select="$catUsed/response/result"/>
                <xsl:with-param name="catTxt" select="document(concat('classification:metadata:all:children:',$classi,':',$cat))/mycoreclass/categories/category[@ID=$cat]/label[@xml:lang=$CurrentLang]/@text"/>
              </xsl:call-template>
            </xsl:if>
          </xsl:for-each>
          <xsl:if test="not(mcrxml:isCurrentUserGuestUser())">
            <xsl:variable name="noCat" select="document(concat('solr:q=', $q, '&amp;sort=', $sort, '&amp;rows=99999&amp;fq=', '-volContentClassi1', ':*'))" />
            <xsl:if test="$noCat/response/result/@numFound &gt; 0">
              <xsl:call-template name="jp.printVolumeListCat">
                <xsl:with-param name="volumes" select="$noCat/response/result"/>
                <xsl:with-param name="catTxt" select="'nicht zugewiesen'"/>
              </xsl:call-template>
            </xsl:if>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="jp.printVolumeListCat">
            <xsl:with-param name="volumes" select="$volumes/response/result"/>
            <xsl:with-param name="catTxt" select="'Jahrgang'"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.toc.printArticles">
    <xsl:param name="parentID" />
    <xsl:variable name="q" select="encoder:encode(concat('+parent:', $parentID, ' +objectType:jparticle'))" />
    <xsl:variable name="fq">
      <xsl:choose>
        <xsl:when test="$rubric = 'Artikel'">
          <xsl:value-of select="encoder:encode('-rubricText:*')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="encoder:encode(concat('rubricText:', $rubric))"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="sort" select="'size%20asc,maintitle%20asc'" />
    <xsl:variable name="articles" select="document(concat('solr:q=', $q,'&amp;fq=', $fq, '&amp;sort=', $sort, '&amp;rows=99999'))" />
    <xsl:if test="$articles/response/result/@numFound &gt; 0">
      <div id="jp-tableOfContent" class="jp-layout-tableOfContent container-fluid jp-objectlist jp-content-block row">
        <xsl:apply-templates mode="artList" select="$articles/response/result/doc" />
      </div>
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
              <xsl:value-of select="$link"/>
              <xsl:if test="$link != not('')">
                <xsl:variable name="derivateCount" select="long[@name='derivateCount']" />
                 <xsl:if test="$derivateCount &gt; 0">
                   <xsl:variable name="q" select="encoder:encode(concat('+derivateOwner:', $mcrId, ' +objectType:derivate'))" />
                   <xsl:variable name="derivates" select="document(concat('solr:q=', $q))" />
                     <xsl:if test="$derivates/response/result/@numFound &gt; 0">
                       <xsl:variable name="deri" select="$derivates/response/result/doc"/>
                       <a href="{$WebApplicationBaseURL}servlets/MCRFileNodeServlet/{$deri/str[@name='id']}/{$deri/str[@name='maindoc']}">
                        lesen
                       </a>
                     </xsl:if>
                 </xsl:if>
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
    <xsl:variable name="q" select="encoder:encode(concat('+parent:', $id, ' +objectType:jparticle'))" />
    <xsl:variable name="articles" select="document(concat('solr:q=', $q))" />
    <xsl:if test="$articles/response/result/@numFound &gt; 0">
      <xsl:variable name="rubricCat" select="$articles/response/result/doc/arr[@name='rubric']" />
      <xsl:variable name="classID" select="substring-before($rubricCat,'#')" />
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
              <xsl:when test="$classID != ''">
                <xsl:for-each select="document(concat('classification:metadata:all:children:',$classID))/mycoreclass/categories/category/label[@xml:lang=$CurrentLang]/@text">
                  <xsl:variable name="cat" select="."/>
                  <xsl:variable name="catUsed" select="document(concat('solr:q=', $q, '&amp;rows=99999&amp;fq=', 'rubricText', ':', encoder:encode($cat)))" />
                  <xsl:if test="$catUsed/response/result/@numFound &gt; 0">
                    <xsl:choose>
                      <xsl:when test="$rubric = $cat">
                        <a class="list-group-item active-list-item" href="{$WebApplicationBaseURL}receive/{$id}?XSL.rubric={$cat}">
                          <xsl:value-of select="$cat" />
                        </a>
                      </xsl:when>
                      <xsl:otherwise>
                        <a class="list-group-item" href="{$WebApplicationBaseURL}receive/{$id}?XSL.rubric={$cat}">
                          <xsl:value-of select="$cat" />
                        </a>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </xsl:for-each>
                  <xsl:variable name="noCat" select="document(concat('solr:q=', $q, '&amp;rows=99999&amp;fq=', '-rubricText', ':*'))" />
                  <xsl:if test="$noCat/response/result/@numFound &gt; 0">
                    <xsl:call-template name="jp.printContentList.noCat">
                      <xsl:with-param name="id" select="$id"/>
                    </xsl:call-template>
                </xsl:if>
              </xsl:when>
              <xsl:otherwise>
                  <xsl:call-template name="jp.printContentList.noCat">
                    <xsl:with-param name="id" select="$id"/>
                  </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.printContentList.noCat">
    <xsl:param name="id"/>
    <xsl:choose>
      <xsl:when test="$rubric = 'Artikel'">
        <a class="list-group-item active-list-item" href="{$WebApplicationBaseURL}receive/{$id}?XSL.rubric=Artikel">
          <xsl:value-of select="'Artikel'" />
        </a>
      </xsl:when>
      <xsl:otherwise>
        <a class="list-group-item" href="{$WebApplicationBaseURL}receive/{$id}?XSL.rubric=Artikel">
          <xsl:value-of select="'Artikel'" />
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="jp.printVolumeListCat">
    <xsl:param name="volumes"/>
    <xsl:param name="catTxt"/>
    <div class="list-group">
      <a class="dt-collapse" data-toggle="collapse" data-target="#jp-journal-volume-list">
        <span class="jp-layout-facet-group-head">
          <xsl:value-of select="$catTxt"/>
        </span>
        <i class="fa fa-sort-asc"></i>
        <i class="fa fa-sort-desc"></i>
      </a>
      <div class="collapse in list-group jp-list-group-special" id="jp-journal-volume-list">
        <div id="jp-tableOfContent -vol" class="jp-layout-tableOfContent list-group jp-list-group-special">
          <xsl:apply-templates mode="jp.printListEntryContent" select="$volumes/doc" />
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="jp.toc.buildVolumeSelect">
    <xsl:param name="parentID" />
    <xsl:variable name="qCat" select="encoder:encode(concat('+id:', $currentObjID, ' +objectType:jpvolume'))" />
    <xsl:variable name="q" select="encoder:encode(concat('+parent:', $parentID, ' +objectType:jpvolume'))" />
    <xsl:variable name="fqCat" select="encoder:encode('volContentClassi1:*')"/>
    <xsl:variable name="fq" select="encoder:encode('-volContentClassi1:*')"/>
    <xsl:variable name="sort">
      <!--<xsl:value-of select="'indexPosition%20asc'" />-->
      <xsl:value-of select="'date.published%20desc'" />
      <xsl:value-of select="',maintitle%20asc'" />
    </xsl:variable>

    <xsl:variable name="hasCat" select="document(concat('solr:q=', $qCat, '&amp;fq=', $fqCat, '&amp;sort=', $sort, '&amp;rows=9999'))" />
    <xsl:choose>
      <xsl:when test="$hasCat/response/result/@numFound &gt; 0">
        <xsl:variable name="catID" select="$hasCat/response/result/doc/arr[@name='volContentClassi1']"/>
        <xsl:variable name="classi" select="$hasCat/response/result/doc/arr[@name='classification']/str"/>
        <xsl:variable name="fqCatID" select="encoder:encode(concat('volContentClassi1:', $catID))"/>
        <xsl:variable name="volumesCat" select="document(concat('solr:q=', $q, '&amp;fq=', $fqCatID, '&amp;sort=', $sort, '&amp;rows=9999'))"/>
        <xsl:call-template name="jp.toc.getCatVolumeSelect">
          <xsl:with-param name="catID" select="$catID"/>
          <xsl:with-param name="classi" select="$classi"/>
          <xsl:with-param name="volumesCat" select="$volumesCat/response/result"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="volumes" select="document(concat('solr:q=', $q, '&amp;fq=', $fq, '&amp;sort=', $sort, '&amp;rows=9999'))" />
        <xsl:if test="$volumes/response/result/@numFound &gt; 0">
          <xsl:variable name="noCat" select="document(concat('solr:q=', $q, '&amp;fq=', $fqCat, '&amp;sort=', $sort, '&amp;rows=9999'))" />
          <xsl:variable name="catTxt">
            <xsl:choose>
              <xsl:when test="$noCat/response/result/@numFound &gt; 0">
                <xsl:value-of select="'nicht zugewiesen'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'Jahrgang'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:call-template name="jp.toc.printVolumeSelect">
            <xsl:with-param name="volumes" select="$volumes/response/result"/>
            <xsl:with-param name="catText" select="$catTxt"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="jp.toc.getCatVolumeSelect">
    <xsl:param name="catID"/>
    <xsl:param name="classi"/>
    <xsl:param name="volumesCat"/>
    <xsl:variable name="catTxt" select="document(concat('classification:metadata:all:children:',$classi,':',$catID))/mycoreclass/categories/category[@ID=$catID]/label[@xml:lang=$CurrentLang]/@text"/>
    <xsl:call-template name="jp.toc.printVolumeSelect">
      <xsl:with-param name="volumes" select="$volumesCat"/>
      <xsl:with-param name="catText" select="$catTxt"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="jp.toc.printVolumeSelect">
    <xsl:param name="volumes"/>
    <xsl:param name="catText"/>
    <div class="jp-volume-sidebar-group">
      <h2 class="jp-volume-selectCaption">
        <xsl:value-of select="$catText"/>
      </h2>
      <select class="form-control input-sm" onchange="location = this.options[this.selectedIndex].value;">
        <xsl:apply-templates mode="jp.toc.printVolume" select="$volumes/doc" />
      </select>
    </div>
  </xsl:template>

  <xsl:template mode="jp.toc.printVolume" match="doc">
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
  </xsl:template>

</xsl:stylesheet>
