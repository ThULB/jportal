<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="i18n xalan">

  <xsl:template match="laws_search">
    <xsl:call-template name="laws_search.js" />
    <form class="searchbox" action="servlets/MCRSearchServlet" method="post" accept-charset="utf-8" onSubmit="return buildQuery()" style="padding-left: 280px;">

      <input id="query" type="hidden" name="query" />

      <table>
        <tr>
          <td>Text</td>
          <td><input type="text" id="allMeta" size="30" /></td>
        </tr>
        <tr>
          <td>Territorium</td>
          <td>
            <xsl:variable name="selectBox" select="document('classification:editor:-1:children:jportal_laws_territory')" />
            <select id="territory">
              <option><xsl:value-of select="i18n:translate('editor.search.choose')" /></option>
              <xsl:for-each select="$selectBox/items/item">
                <option value="{@value}"><xsl:value-of select="label" /></option>
              </xsl:for-each>
            </select>
          </td>
        </tr>
        <tr>
          <td>Jahrgang</td>
          <td>
            <input id="published_from" type="text" size="4" maxlength="4" />
            <span>-</span>
            <input id="published_until" type="text" size="4" maxlength="4"/>
          </td>
        </tr>
      </table>
      <input type="submit" value="Suchen" />
    </form>
  </xsl:template>

  <xsl:template name="laws_search.js">
    <script type="text/javascript">
      /*$(document).ready(function() {
        
      });*/
      function buildQuery() {
        var allMeta = $("#allMeta").val();
        var territory = $("#territory").val();
        var from = $("#published_from").val();
        var until = $("#published_until").val();

        var query = "";
        // input conditions
        query = addCondition(query, "allMeta", "contains", allMeta);
        // TODO
        query = addCondition(query, "id", "&lt;=", territory);
        query = addCondition(query, "published_from", "&gt;=", from);
        query = addCondition(query, "published_until", "&lt;=", until);

        // hidden conditions
        query = addCondition(query, "contentClassi2", "=", "Gesetzesblaetter");
        query = addCondition(query, "objectType", "=", "jpvolume");

        $("#query").attr("value", query);
      }
      function addCondition(/*String*/ query, /*String*/ field, /*String*/ operation, /*String*/ value) {
        if(value.length &lt;= 0) {
          return query;
        }
        var condition = field + " " + operation + " " + value;
        if(query.length == 0) {
          return condition;
        } else {
          return query + " AND " + condition;
        }
      }
    </script>
  </xsl:template>

</xsl:stylesheet>