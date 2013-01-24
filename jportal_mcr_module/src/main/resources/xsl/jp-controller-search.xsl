<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template match="jpsearch" mode="default">
    <xsl:variable name="queryXML">
      <query>
        <queryTerm value="+({$qt})" />
        <queryTermField name="+journalID" value="{$searchjournalID}" />
        <param name="qf" value="heading^10 titles^10 participants^10 heading_de^5 participants_de^5 alternatives^5 dates^5 titles_de^5 alternatives_de^3 allMeta^1" />
        <param name="rows" value="{$rows}" />
        <param name="start" value="{$start}" />
        <param name="defType" value="edismax" />
      </query>
    </xsl:variable>
    <xsl:variable name="query">
      <xsl:apply-templates mode="createSolrQuery" select="xalan:nodeset($queryXML)/query" />
    </xsl:variable>
    <xsl:apply-templates mode="searchResults" select="document($query)" />
  </xsl:template>

</xsl:stylesheet>