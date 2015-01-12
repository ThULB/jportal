<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:encoder="xalan://java.net.URLEncoder" exclude-result-prefixes="xlink i18n xalan encoder">

  <xsl:template match="jp-laws-search">
    <xsl:call-template name="jp.laws.search.js" />
    <link href="{$WebApplicationBaseURL}jp_templates/template_thLegislativExekutiv/CSS/template_thLegislativExekutiv.css" rel="stylesheet" type="text/css" />
    <div class="jp-laws-expertsearchHeader">
	    <span class="form-control alert alert-warning">
	      <xsl:value-of select="i18n:translate('jp.laws.search.intro')" />
	    </span>
		</div>

    <form id="advancedSearchForm" action="{$WebApplicationBaseURL}servlets/solr/laws" onSubmit="return buildQuery()" class="jp-laws-expertsearch">
      <input type="hidden" name="XSL.returnURL" value="{$RequestURL}" />
      <input type="hidden" name="qry" id="qry" />
      <input type="hidden" name="fq" id="fq" />
        <div class="form-group col-sm-12">
          <label class="col-sm-2"><xsl:value-of select="i18n:translate('jp.laws.search.text')" /></label>
          <div class="col-sm-10"><input class="form-control" type="text" id="searchTerm" size="30" /></div>
        </div>
        <div class="form-group col-sm-12">
          <label class="col-sm-2"><xsl:value-of select="i18n:translate('jp.laws.search.territory')" /></label>
          <div class="col-sm-10">
            <xsl:variable name="selectBox" select="document('classification:editor:-1:children:jportal_laws_territory')" />
            <select id="territory" class="form-control">
              <option value=""><xsl:value-of select="i18n:translate('editor.search.all')" /></option>
              <xsl:for-each select="$selectBox/items/item">
                <option value="{@value}"><xsl:value-of select="label" /></option>
              </xsl:for-each>
            </select>
          </div>
        </div>
        <div class="form-group col-sm-12">
          <label class="col-sm-2"><xsl:value-of select="i18n:translate('jp.laws.search.year')" /></label>
          <div class="col-sm-10">
          	<div class="input-group">
	            <input class="form-control" id="published_from" type="text" size="4" maxlength="4" />
	            <span class="seperator input-group-addon">-</span>
	            <input class="form-control" id="published_until" type="text" size="4" maxlength="4"/>
            </div>
          </div>
        </div>
      	<div class="col-sm-12 row">
      		<input class="btn btn-primary pull-right" type="submit" value="{i18n:translate('jp.laws.search')}" />
      	</div>
    </form>
  </xsl:template>

  <xsl:template match="MyCoReWebPage[section/jp-laws-search]" mode="showSearchBar">
    <xsl:value-of select="'false'" />
  </xsl:template>

  <xsl:template name="jp.laws.search.js">
    <script type="text/javascript" src="{$WebApplicationBaseURL}jp_templates/template_thLegislativExekutiv/JS/jp-laws.js" />
    <script type="text/javascript">
      $(document).ready(function() {
        setLogo('<xsl:value-of select="$WebApplicationBaseURL" />');
      });
    </script>
  </xsl:template>

</xsl:stylesheet>