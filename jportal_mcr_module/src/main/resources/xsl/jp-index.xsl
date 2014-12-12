<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xalan i18n">

  <xsl:template match="jpindex">

    <div class="jp-layout-index">
      <div class="jp-layout-index-intro">
        <h1>
          <xsl:value-of select="i18n:translate('jp.site.home.header')" />
        </h1>
        <p class="greeting">
          <xsl:value-of select="i18n:translate('jp.site.home.greeting1')" />
          <a href="http://www.thulb.uni-jena.de">Thüringer Universitäts- und Landesbibliothek Jena</a>
          (ThULB)
          <xsl:value-of select="i18n:translate('jp.site.home.greeting2')" />
        </p>
        <p>
          <xsl:value-of select="i18n:translate('jp.site.home.intro')" />
          <a href="http://www.urmel-dl.de">Universal Multimedia Electronic Library</a>
          (UrMEL).
        </p>
      </div>
      <div class="jp-layout-index-list">
        <ul class="list-inline text-center">
          <li>
            <a href="{$WebApplicationBaseURL}content/main/journalList.xml#A" class="entry journal">
              <xsl:variable name="text" select="i18n:translate('jp.site.home.journal')" />
              <div class="title">
                <span class="initial">
                  <xsl:value-of select="substring($text, 1, 1)" />
                </span>
                <span class="text">
                  <xsl:value-of select="$text" />
                </span>
              </div>
              <p class="description">
                <xsl:value-of select="i18n:translate('jp.site.home.journal.description')" />
              </p>
            </a>
          </li>
          <li>
            <a href="{$WebApplicationBaseURL}content/main/calendarList.xml#A" class="entry calendar">
              <xsl:variable name="text" select="i18n:translate('jp.site.home.calendar')" />
              <div class="title">
                <span class="initial">
                  <xsl:value-of select="substring($text, 1, 1)" />
                </span>
                <span class="text">
                  <xsl:value-of select="$text" />
                </span>
              </div>
              <p class="description">
                <xsl:value-of select="i18n:translate('jp.site.home.calendar.description')" />
              </p>
            </a>
          </li>
        </ul>
      </div>
      <div class="jp-layout-index-latestArticles">
        <h1>
          <xsl:value-of select="i18n:translate('jp.site.home.currentArticle')" />
        </h1>
        <xsl:call-template name="jp.layout.printLatestArticles" />
      </div>
    </div>
  </xsl:template>

  <xsl:template name="jp.layout.printLatestArticles">
    <xsl:param name="sortField" select="'created'" />
    <xsl:param name="maxResults" select="'3'" />
    <xsl:param name="objectType" select="'jparticle'" />

    <xsl:variable name="searchTerm" select="concat('objectType:', $objectType)" />
    <xsl:variable name="queryURI" select="concat('solr:q=',$searchTerm,'&amp;sort=',$sortField, '%20desc&amp;rows=',$maxResults)" />
    <div class="row">
      <xsl:apply-templates mode="jp.layout.printLatestArticles" select="document($queryURI)/response/result/doc" />
    </div>
  </xsl:template>

  <xsl:template mode="jp.layout.printLatestArticles" match="doc">
    <div class="col-sm-4">
      <div class="jp-layout-titlewrap">
        <a class="title" href="{$WebApplicationBaseURL}receive/{str[@name='id']}">
          <xsl:call-template name="shortenString">
            <xsl:with-param name="string" select="str[@name='maintitle']" />
            <xsl:with-param name="length" select="200" />
          </xsl:call-template>
        </a>
      </div>
      <div class="journal">
        Erschienen in
        <a href="{$WebApplicationBaseURL}receive/{str[@name='journalID']}">
          <xsl:call-template name="shortenString">
            <xsl:with-param name="string" select="str[@name='journalTitle']" />
            <xsl:with-param name="length" select="60" />
          </xsl:call-template>
        </a>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>