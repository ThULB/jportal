<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:mcr="http://www.mycore.org/">
  <xsl:template priority="3" match="printLatestArticles">
    <xsl:variable name="searchTerm" select="encoder:encode(concat('(objectType = ',@objectType,')') )" />
    <xsl:variable name="queryURI"
      select="concat('query:term=',$searchTerm,'&amp;sortby=',@sortField,
            '&amp;order=descending&amp;maxResults=',@maxResults)" />
    <ul id="latestArticlesList">
      <xsl:apply-templates mode="searchResultsList" select="document($queryURI)/mcr:results/mcr:hit" />
    </ul>
  </xsl:template>

  <xsl:template mode="searchResultsList" match="mcr:hit">
    <li>
      <h3>
        <a href="{$WebApplicationBaseURL}receive/{@id}">
          <xsl:value-of select="mcr:metaData/mcr:field[@name='maintitles_plain']" />
        </a>
      </h3>
      Erschienen in <xsl:value-of select="mcr:metaData/mcr:field[@name='journalTitle']" />
    </li>
  </xsl:template>
</xsl:stylesheet>