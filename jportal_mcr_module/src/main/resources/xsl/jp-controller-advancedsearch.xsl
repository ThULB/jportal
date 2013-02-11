<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:jpxml="xalan://org.mycore.common.xml.MCRJPortalXMLFunctions" exclude-result-prefixes="encoder xalan jpxml">
  <xsl:param name="qt" select="'*'" />
  <xsl:param name="searchjournalID" select="''" />
  <xsl:param name="start" select="'0'" />
  <xsl:param name="rows" select="'10'" />

  <xsl:param name="field1" />
  <xsl:param name="field2" />
  <xsl:param name="field3" />
  <xsl:param name="value1" />
  <xsl:param name="value2" />
  <xsl:param name="value3" />

  <xsl:variable name="searchfields">
    <entry label="alle Wörter" field="allMeta" />
    <entry label="Titel" field="titles_de" />
    <entry label="Person/Institution" field="names_de" />
    <entry label="Jahr" field="dates" />
    <entry label="Schlagwörter" field="keywords" />
    <entry label="Rubrik" field="rubricText" />
    <entry label="Volltext" field="content" />
  </xsl:variable>

  <xsl:template match="jpsearch" mode="advanced.form">
    <xsl:variable name="journalIDTerm">
      <xsl:if test="$searchjournalID != ''">
        <xsl:value-of select="concat(' journalID:', $searchjournalID)" />
      </xsl:if>
    </xsl:variable>

    <div>
      <h2>Erweiterte Suche</h2>
      <form id="advancedSearchForm" action="/jp-search.xml">
        <xsl:call-template name="jpadvancedsearch.printSearchRow">
          <xsl:with-param name="row" select="'1'" />
          <xsl:with-param name="field" select="$field1" />
          <xsl:with-param name="value" select="$value1" />
        </xsl:call-template>
        <xsl:call-template name="jpadvancedsearch.printSearchRow">
          <xsl:with-param name="row" select="'2'" />
          <xsl:with-param name="field" select="$field2" />
          <xsl:with-param name="value" select="$value2" />
        </xsl:call-template>
        <xsl:call-template name="jpadvancedsearch.printSearchRow">
          <xsl:with-param name="row" select="'3'" />
          <xsl:with-param name="field" select="$field3" />
          <xsl:with-param name="value" select="$value3" />
        </xsl:call-template>
        <input id="submitButton" type="submit" value="Suche" class="submit"/>
        <xsl:if test="$searchjournalID != ''">
          <input type="hidden" name="XSL.searchjournalID" value="{$searchjournalID}" />
        </xsl:if>
        <input type="hidden" name="XSL.mode" value="advanced.result"/>
      </form>
    </div>
  </xsl:template>

  <xsl:template name="jpadvancedsearch.printSearchRow">
    <xsl:param name="row" />
    <xsl:param name="field" />
    <xsl:param name="value" />
    <div class="row">
      <select name="XSL.field{$row}" class="field">
        <xsl:for-each select="xalan:nodeset($searchfields)/entry">
          <xsl:choose>
            <xsl:when test="$field = @field">
              <option value="{@field}" selected="selected">
                <xsl:value-of select="@label" />
              </option>
            </xsl:when>
            <xsl:otherwise>
              <option value="{@field}">
                <xsl:value-of select="@label" />
              </option>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </select>
      <input id="inputField" name="XSL.value{$row}" value="{$value}" class="value"></input>
    </div>
  </xsl:template>

  <xsl:template match="jpsearch" mode="advanced.result">
    <xsl:variable name="qt" select="jpxml:toSolrQuery(concat($field1, '=', $value1, '#', $field2, '=', $value2, '#', $field3, '=', $value3))" />
    <xsl:variable name="fq">
      <xsl:value-of select="'-objectType:data_file'" />
      <xsl:value-of select="jpxml:resolveText('[ journalID:{journalID}]', concat('journalID=', $searchjournalID))" />
    </xsl:variable>
    <xsl:variable name="queryXML">
      <query>
        <queryTerm value="{$qt}" />
        <param name="fq" value="{$fq}" />
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
