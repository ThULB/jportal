<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:mcr="http://www.mycore.org/">
  <xsl:template priority="3" match="printLatestArticles">
    <xsl:variable name="searchTerm" select="concat('objectType:', @objectType)" />
    <xsl:variable name="queryURI" select="concat('solr:q=',$searchTerm,'&amp;sort=',@sortField, '%20desc&amp;rows=',@maxResults)" />
    <ul id="latestArticlesList">
      <xsl:apply-templates mode="searchResultsList" select="document($queryURI)/response/result/doc" />
    </ul>
  </xsl:template>
  <xsl:template mode="searchResultsList" match="doc">
    <li>
      <h3>
        <a href="{$WebApplicationBaseURL}receive/{str[@name='id']}">
          <xsl:value-of select="str[@name='maintitle']" />
        </a>
      </h3>
      <span>Erschienen in <a href="{$WebApplicationBaseURL}receive/{str[@name='journalID']}">
        <xsl:value-of select="str[@name='journalTitle']" />
      </a></span>
    </li>
  </xsl:template>
</xsl:stylesheet>
