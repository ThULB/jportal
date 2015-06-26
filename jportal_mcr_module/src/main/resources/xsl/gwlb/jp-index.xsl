<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xalan i18n">

  <xsl:template match="jpindex">

    <div class="jp-layout-index col-sm-8 col-sm-offset-2">
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
        <div class="center-block">
          <a class="btn btn-primary jp-layout-index-gwlb-button" href="{$WebApplicationBaseURL}content/main/journalListGWLB.xml#*">
            <i class="fa fa-book"></i> weiter zum Zeitschriftenserver der GWLB
          </a>
        </div>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>