<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Contains jportal specific layout functions.
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="MCR.Piwik.baseurl" />
  <xsl:param name="MCR.Piwik.enable" />
  <xsl:param name="MCR.Piwik.id" select="'1'" />

  <xsl:template mode="jp.printListEntry" match="*">
    <li>
      <xsl:apply-templates mode="jp.printListEntryContent" select="." />
    </li>
  </xsl:template>

  <xsl:template name="jp.piwik">
    <xsl:if test="$MCR.Piwik.enable = 'true' and $MCR.Piwik.baseurl != ''">
      <script type="text/javascript">
        var _paq = _paq || [];
        _paq.push(["trackPageView"]);
        _paq.push(["enableLinkTracking"]);
        (function() {
          var u = '<xsl:value-of select="$MCR.Piwik.baseurl" />';
          var journalID = '<xsl:value-of select="/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID" />';
          if(journalID != "") {
            _paq.push(['setCustomVariable', 1, "journal", journalID, "page"]);
          }
          _paq.push(["setTrackerUrl", u+"piwik.php"]);
          _paq.push(["setSiteId", '<xsl:value-of select="$MCR.Piwik.id" />']);
          var d=document, g=d.createElement("script"), s=d.getElementsByTagName("script")[0]; g.type="text/javascript";
          g.defer=true; g.async=true; g.src=u+"piwik.js"; s.parentNode.insertBefore(g,s);
        })();
      </script>
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.printClass">
    <xsl:param name="nodes" />
    <xsl:for-each select="$nodes">
      <xsl:variable name="label" select="./label[lang($CurrentLang)]/@text" />
      <xsl:choose>
        <xsl:when test="string-length($label) = 0">
          <xsl:call-template name="jp.printClass.fallback">
            <xsl:with-param name="node" select="." />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$label" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="jp.printClass.fallback">
    <xsl:param name="node" />
    <xsl:param name="pos" select="1" />
    <xsl:variable name="classlabel" select="$node/label[lang($languages/lang[$pos]/text())]/@text" />
 	<xsl:choose>
      <xsl:when test="string-length($classlabel) != 0">
        <xsl:value-of select="$classlabel" />
      </xsl:when>
      <xsl:when test="$languages/lang[$pos + 1]">
        <xsl:call-template name="jp.printClass.fallback">
          <xsl:with-param name="node" select="$node" />
          <xsl:with-param name="pos" select="$pos + 1" />
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>