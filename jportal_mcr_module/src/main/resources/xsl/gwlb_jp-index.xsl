<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:imprint="xalan://fsu.jportal.util.ImprintUtil" exclude-result-prefixes="xalan i18n imprint">

  <xsl:template match="jpindex">

    <div class="jp-layout-index col-sm-10 col-sm-offset-1">
      <div class="jp-layout-index-intro">
        <xsl:variable name="currentLang" select="i18n:getCurrentLocale()" />
        <xsl:choose>
          <xsl:when test="imprint:has('index', 'greeting')">
            <xsl:apply-templates select="imprint:getImprintContent('index', 'greeting', $currentLang)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="imprint:getDefaultGreetingXSL('index', $currentLang)" />
          </xsl:otherwise>
        </xsl:choose>
      </div>
      <div id="gwlb-layout-index-list">
        <h2>verfügbare Zeitschriften</h2>
        <ul>
          <xsl:variable name="journalList" select="document('solr:q=+objectType:jpjournal&amp;rows=99999&amp;sort=maintitle_sort%20asc')" />
          <xsl:if test="$journalList/response/result/@numFound &gt; 0">
            <xsl:for-each select="$journalList/response/result/doc">
              <li>
                <h3>
                  <a href="{$WebApplicationBaseURL}receive/{str[@name='id']}"><xsl:value-of select="str[@name='maintitle']"/></a>
                </h3>
              </li>
            </xsl:for-each>
          </xsl:if>
        </ul>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>