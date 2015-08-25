<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xalan i18n">

  <xsl:template match="jpindex">

    <div class="jp-layout-index col-sm-10 col-sm-offset-1">
    <!--<div class="jp-layout-index col-sm-12">-->
      <div class="jp-layout-index-intro">
        <h1>
          <xsl:value-of select="i18n:translate('jp.site.home.header.gwlb')" />
        </h1>
        <p class="greeting">
          <xsl:value-of select="i18n:translate('jp.site.home.greeting1.gwlb')" />
          <a href="http://www.gwlb.de"> Gottfried Wilhelm Leibniz Bibliothek </a>
          <xsl:value-of select="i18n:translate('jp.site.home.greeting2.gwlb')" />
        </p>
        <!--<div class="center-block">-->
          <!--<a class="btn btn-primary jp-layout-index-gwlb-button" href="{$WebApplicationBaseURL}content/main/journalListGWLB.xml#*">-->
            <!--<i class="fa fa-book"></i> weiter zum Zeitschriftenserver der GWLB-->
          <!--</a>-->
        <!--</div>-->
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