<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:encoder="xalan://java.net.URLEncoder" exclude-result-prefixes="xlink i18n xalan encoder">

  <xsl:param name="mode" />
  <xsl:param name="qt" select="'*'" />
  <xsl:param name="start" select="'0'" />
  <xsl:param name="rows" select="'10'" />

  <xsl:template match="jpsearch" mode="laws.form">
    <xsl:call-template name="jp.laws.search.css" />
    <xsl:call-template name="jp.laws.search.js" />

    <p>
      <xsl:value-of select="i18n:translate('jp.laws.search.intro')" />
    </p>

    <form id="advancedSearchForm" action="/jp-search.xml" onSubmit="return buildQuery()">
      <input type="hidden" name="XSL.mode" value="laws.result"/>
      <input type="hidden" id="qt" name="XSL.qt" />
      <table>
        <tr>
          <th><xsl:value-of select="i18n:translate('jp.laws.search.text')" /></th>
          <td><input type="text" id="searchTerm" size="30" /></td>
        </tr>
        <tr>
          <th><xsl:value-of select="i18n:translate('jp.laws.search.territory')" /></th>
          <td>
            <xsl:variable name="selectBox" select="document('classification:editor:-1:children:jportal_laws_territory')" />
            <select id="territory">
              <option value=""><xsl:value-of select="i18n:translate('editor.search.choose')" /></option>
              <xsl:for-each select="$selectBox/items/item">
                <option value="{@value}"><xsl:value-of select="label" /></option>
              </xsl:for-each>
            </select>
          </td>
        </tr>
        <tr>
          <th><xsl:value-of select="i18n:translate('jp.laws.search.year')" /></th>
          <td>
            <input id="published_from" type="text" size="4" maxlength="4" />
            <span class="seperator">-</span>
            <input id="published_until" type="text" size="4" maxlength="4"/>
          </td>
        </tr>
      </table>
      <input class="submit" type="submit" value="{i18n:translate('jp.laws.search')}" />
    </form>
  </xsl:template>

 <xsl:template match="jpsearch" mode="laws.result">
    <xsl:variable name="q" select="encoder:encode($qt, 'UTF-8')" />
    <xsl:variable name="searchResults" select="document(concat('solr:q=', $q ,'&amp;rows=',$rows,'&amp;start=',$start,'&amp;defType=edismax'))"></xsl:variable>
    <xsl:apply-templates mode="searchResults" select="$searchResults" />
 </xsl:template>

  <xsl:template name="jp.laws.search.css">
    <style type="text/css">
      .searchbox {
      }
      .searchbox table th {
        padding-right: 20px;
        text-align: left;
      }
      .searchbox table tr {
        padding-bottom: 8px;
      }
      .searchbox .seperator {
        padding-right: 3px;
        padding-left: 3px;
      }
      .searchbox .submit {
        margin-top: 12px;
      }
    </style>
  </xsl:template>

  <xsl:template name="jp.laws.search.js">
    <script type="text/javascript">
      function buildQuery() {
        var searchTerm = $("#searchTerm").val();
        var territory = $("#territory").val();
        var from = $("#published_from").val();
        var until = $("#published_until").val();

        // input conditions
        var query = searchTerm;
        if(territory.length &gt; 0) {
          query += " +volContentClassi1:" + territory;
        }
        if(from.length &gt; 0 &amp; until.length &gt; 0) {
          query += " +published:[" + from + " TO " + until + "]";
        } else if(from.length &gt; 0) {
          query += " +published:[" + from + " TO *]";
        } else if(until.length &gt; 0) {
          query += " +published:[* TO " + until + "]";
        }
        query += " +contentClassi2:Gesetzesblaetter";
        query += " +objectType:jpvolume";
        $("#qt").attr("value", query);
      }
    </script>
  </xsl:template>

</xsl:stylesheet>