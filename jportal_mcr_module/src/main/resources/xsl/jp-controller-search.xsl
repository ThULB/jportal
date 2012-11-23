<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xalan="http://xml.apache.org/xalan">
  <xsl:param name="qt" select="'*'" />
  <xsl:param name="searchjournalID" select="''" />
  <xsl:param name="start" select="'0'" />
  <xsl:param name="rows" select="'10'" />

  <!-- For Subselect -->
  <xsl:param name="subselect.type" select="''" />
  <xsl:param name="subselect.session" select="''" />
  <xsl:param name="subselect.varpath" select="''" />
  <xsl:param name="subselect.webpage" select="''" />

  <xsl:template match="jpsearch" mode="default">
    <xsl:variable name="queryXML">
      <query>
        <queryTerm value="{$qt}" />
        <queryTermField name="+journalID" value="{$searchjournalID}" />
        <queryTermField name="+objectType" value="{$subselect.type}" />
        <param name="qf" value="titles^10 heading^10 dates^5 allMeta^1" />
        <param name="rows" value="{$rows}" />
        <param name="start" value="{$start}" />
        <param name="defType" value="edismax" />
      </query>
    </xsl:variable>
    <xsl:variable name="query">
      <xsl:apply-templates mode="createSolrQuery" select="xalan:nodeset($queryXML)/query" />
    </xsl:variable>

    <xsl:variable name="subselectXML">
      <subselect>
        <param name="subselect.type" value="$subselect.type"/>
        <param name="subselect.session" value="$subselect.session"/>
        <param name="subselect.varpath" value="$subselect.varpath"/>
        <param name="subselect.webpage" value="$subselect.webpage"/>
      </subselect>
    </xsl:variable>
    
    <xsl:variable name="searchResults">
      <solrSearch>
        <xsl:copy-of select="$subselectXML"/>
        <xsl:copy-of select="$queryXML"/>
        <xsl:copy-of select="document($query)"/>
      </solrSearch>
    </xsl:variable>

    <xsl:apply-templates mode="searchResults" select="xalan:nodeset($searchResults)" />
  </xsl:template>

  <xsl:template mode="createSolrQuery" match="query">
    <xsl:value-of select="concat('solr:q=', queryTerm/@value)" />
    <xsl:apply-templates mode="createQueryTermField" select="queryTermField" />
    <xsl:apply-templates mode="createParam" select="param" />
  </xsl:template>

  <xsl:template mode="createQueryTermField" match="queryTermField">
    <xsl:if test="@value != ''">
      <xsl:value-of select="encoder:encode(concat(' ', @name, ':', @value))" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="createParam" match="param">
    <xsl:if test="@value != ''">
      <xsl:value-of select="concat('&amp;', @name, '=', encoder:encode(@value))" />
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>