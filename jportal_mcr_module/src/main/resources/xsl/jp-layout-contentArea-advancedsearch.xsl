<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:jpxml="xalan://org.mycore.common.xml.MCRJPortalXMLFunctions" exclude-result-prefixes="encoder xalan jpxml">

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

  <xsl:template match="jpadvancedsearch">
    <script type="text/javascript" src="/js/jp-advancedsearch.js" />
    <div>
      <h2>Erweiterte Suche</h2>
      <form id="advancedSearchForm" action="/servlets/solr/advanced" onsubmit="jp.advancedsearch.onsubmit()">
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
        <xsl:if test="$journalID != ''">
          <input type="hidden" name="journalID" value="{$journalID}" />
          <input type="hidden" name="fq" value="journalID:{$journalID}" />
        </xsl:if>
        <input type="hidden" name="q" value="" />
        <input id="submitButton" type="submit" value="Suche" class="submit" />
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

</xsl:stylesheet>
