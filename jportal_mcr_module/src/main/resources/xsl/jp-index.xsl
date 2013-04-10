<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="i18n">

  <xsl:template match="jpindex">

    <div class="jp-layout-index">
      <div class="jp-layout-index-intro">
        <h1><xsl:value-of select="i18n:translate('jp.site.home.header')" /></h1>
        <p class="greeting">
          <xsl:value-of select="i18n:translate('jp.site.home.greeting1')" />
          <a href="http://www.thulb.uni-jena.de">Thüringer Universitäts- und Landesbibliothek Jena</a> (ThULB)
          <xsl:value-of select="i18n:translate('jp.site.home.greeting2')" />
        </p>
        <p>
          <xsl:value-of select="i18n:translate('jp.site.home.intro')" />
          <a href="http://www.urmel-dl.de">Universal Multimedia Electronic Library</a> (UrMEL).
        </p>
      </div>
      <div class="jp-layout-index-list">
        <ul>
          <li>
            <a href="/content/main/journalList.xml#A" class="entry journal">
              <xsl:variable name="text" select="i18n:translate('jp.site.home.journal')" />
              <div class="title">
                <span class="initial"><xsl:value-of select="substring($text, 1, 1)" /></span>
                <span class="text"><xsl:value-of select="$text" /></span>
              </div>
              <p class="description"><xsl:value-of select="i18n:translate('jp.site.home.journal.description')" /></p>
            </a>
          </li>
          <li>
            <a href="/content/main/calendarList.xml#A" class="entry calendar">
              <xsl:variable name="text" select="i18n:translate('jp.site.home.calendar')" />
              <div class="title">
                <span class="initial"><xsl:value-of select="substring($text, 1, 1)" /></span>
                <span class="text"><xsl:value-of select="$text" /></span>
              </div>
              <p class="description"><xsl:value-of select="i18n:translate('jp.site.home.calendar.description')" /></p>
            </a>
          </li>
        </ul>
      </div>

      <xsl:call-template name="jp.layout.printObjectEditing" />

      <div class="jp-layout-index-latestArticles">
        <h1><xsl:value-of select="i18n:translate('jp.site.home.currentArticle')" /></h1>
        <xsl:call-template name="jp.layout.printLatestArticles" />
      </div>
    </div>

  </xsl:template>
</xsl:stylesheet>