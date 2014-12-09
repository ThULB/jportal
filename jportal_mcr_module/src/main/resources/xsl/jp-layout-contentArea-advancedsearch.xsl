<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="encoder xalan jpxml i18n">

  <xsl:param name="field1" />
  <xsl:param name="field2" />
  <xsl:param name="field3" />
  <xsl:param name="value1" />
  <xsl:param name="value2" />
  <xsl:param name="value3" />

  <xsl:variable name="searchfields">
    <entry field="allMeta" />
    <entry field="titles_de" />
    <entry field="names_de" />
    <entry field="dates" />
    <entry field="keywords" />
    <entry field="rubricText" />
    <entry field="content" />
  </xsl:variable>

  <xsl:template match="jpadvancedsearch">
    <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-advancedsearch.js" />
    <div>
      <form id="advancedSearchForm" action="{$WebApplicationBaseURL}servlets/solr/advanced" onsubmit="jp.advancedsearch.onsubmit()">
      	<h4 class="col-sm-offset-1 jp-layout-advancedSearchHead">
      		<xsl:value-of select="i18n:translate('jp.metadata.search.advanced')" />
      	</h4>
      	<xsl:if test="$journalID != ''">
      	<div>
      		<span class="col-sm-3 col-sm-offset-1 col-xs-12 jp-layout-advancedSearchLabel">
						<xsl:value-of select="i18n:translate('jp.metadata.search.searchArea')" />
					</span>
	      	<div class="col-sm-8 form-group col-xs-12 jp-layout-advancedSearchRadioGr">
		      	<label class="radio-inline col-sm-6">
		          <input type="radio" name="radioGroup" id="globalSearchOption" value="globalSearchOption" onchange="jp.advancedsearch.changeSearchRadius()" />
		          <xsl:value-of select="i18n:translate('jp.metadata.search.entire_inventory')" />
		        </label>
		        <label class="radio-inline col-sm-6">
		          <input type="radio" name="radioGroup" checked="" id="journalSearchOption" value="journalSearchOption" onchange="jp.advancedsearch.changeSearchRadius()" />
		          <xsl:value-of select="i18n:translate('jp.metadata.search.within_journal')" />
		        </label>
	        </div>
	      </div>
        </xsl:if>
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
          <input type="hidden" name="journalID" value="{$journalID}"  />
          <input type="hidden" name="fq" value="journalID:{$journalID}"/>           
        </xsl:if>
        <input type="hidden" name="q" value="" />
        <div class="col-sm-12">
        	<input id="submitButton" type="submit" class="btn btn-primary pull-right">
      			<xsl:attribute name="value">
                <xsl:value-of select="i18n:translate('jp.metadata.search.search')" />
            </xsl:attribute>
      		</input>
      	</div>
      </form>
    </div>
  </xsl:template>

  <xsl:template name="jpadvancedsearch.printSearchRow">
    <xsl:param name="row" />
    <xsl:param name="field" />
    <xsl:param name="value" />
    <div> 
      <select name="XSL.field{$row}" class="field col-sm-3 col-sm-offset-1 col-xs-12">
        <xsl:for-each select="xalan:nodeset($searchfields)/entry">
          <xsl:choose>
            <xsl:when test="$field = @field">
              <option value="{@field}" selected="selected">
                <xsl:value-of select="i18n:translate(concat('jp.metadata.search.advanced.', @field))" />
              </option>
            </xsl:when>
            <xsl:otherwise>
              <option value="{@field}">
                <xsl:value-of select="i18n:translate(concat('jp.metadata.search.advanced.', @field))" />
              </option>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </select>
      <div class="col-sm-8 form-group col-xs-12">
      	<input id="inputField" name="XSL.value{$row}" value="{$value}" class="form-control jp-layout-advancedSearchInput"></input>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>