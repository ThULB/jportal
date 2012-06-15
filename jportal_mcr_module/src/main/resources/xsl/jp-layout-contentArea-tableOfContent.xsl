<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:mcr="http://www.mycore.org/">
  <xsl:param name="toc.page" select="'1'" />

  <xsl:template name="tableOfContent">
    <xsl:param name="id" />

    <xsl:variable name="mcrsql" select="encoder:encode(concat('parent = ', $id))" />
    <xsl:variable name="numPerPage" select="$settings/numPerPage" />
    <xsl:variable name="childrenObjs"
      select="document(concat('query:term=', $mcrsql, '&amp;sortby=maintitles&amp;order=ascending&amp;numPerPage=', $numPerPage,'&amp;page=', $toc.page))" />
    <xsl:if test="$childrenObjs/mcr:results/@numHits &gt; 0">
      <div id="jp-tableOfContent" class="jp-layout-tableOfContent jp-layout-marginLR">
        <h3>Inhaltsverzeichnis</h3>
        <ul>
          <xsl:apply-templates mode="printListEntry" select="$childrenObjs/mcr:results/mcr:hit" />
        </ul>

        <xsl:if test="$childrenObjs/mcr:results/@numHits &gt; $numPerPage">
          <menu class="jp-layout-horiz-menu">
            <xsl:apply-templates mode="tableOfContentNavi" select="$childrenObjs/mcr:results" />
          </menu>
        </xsl:if>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="printListEntryContent" match="mcr:hit">
    <a href="{$WebApplicationBaseURL}receive/{@id}">
      <xsl:value-of select="mcr:metaData/mcr:field[@name='maintitles_plain']" />
    </a>
  </xsl:template>

  <xsl:template mode="tableOfContentNavi" match="mcr:results">
    <xsl:param name="i" select="1" />

    <xsl:variable name="url">
      <xsl:call-template name="UrlSetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="'XSL.toc.page'" />
        <xsl:with-param name="value" select="$i" />
      </xsl:call-template>
    </xsl:variable>

    <li>
      <a href="{$url}">
        <xsl:value-of select="$i" />
      </a>
    </li>

    <xsl:if test="$i &lt; @numPages">
      <xsl:apply-templates mode="tableOfContentNavi" select=".">
        <xsl:with-param name="i" select="$i +1" />
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>