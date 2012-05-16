<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <!-- <xsl:param name="WebApplicationBaseURL" /> -->
  <!-- ============================================== -->
  <!-- the template -->
  <!-- ============================================== -->
  <xsl:template name="renderLayout">
    <xsl:param name="journalID" />
    <!-- -->
    <html>
      <head>
        <xsl:call-template name="jp.layout.getHTMLHeader" />
      </head>
      <body>
        <div id="header" class="jp-layout-header">
          <div class="jp-top-navigation-bar">
            <div class="jp-home-link">
              <a href="http://www.urmel-dl.de/" target="_blank">UrMEL</a>
              <xsl:copy-of select="'     |     '" />
              <a href="/content/below/index.xml" target="_self">Journals@UrMEL</a>
            </div>
            <div class="jp-login">
              <xsl:call-template name="navigation.row" />
            </div>
          </div>
          <div class="logo">
          </div>

          <div class="jp-layout-horiz-menu">
            <xsl:call-template name="navigation.tree" />
          </div>
        </div>
        <div class="navi_history">
          <xsl:if test="/mycoreobject/@ID!=''">
            <xsl:variable name="parents" select="document(concat('parents:',/mycoreobject/@ID))/parents/parent" />
            <menu class="jp-layout-breadcrumb">
              <xsl:for-each select="$parents">
                <xsl:sort select="@inherited" order="descending" />
                <li>
                  <b>\ </b>
                  <a href="{$WebApplicationBaseURL}receive/{@xlink:href}">
                    <xsl:value-of select="@xlink:title" />
                  </a>
                </li>
              </xsl:for-each>
              <li>
                <b>\ </b>
                <xsl:variable name="maintitle">
                  <xsl:value-of select="/mycoreobject/metadata/maintitles/maintitle[@inherited='0']" />
                </xsl:variable>

                <xsl:choose>
                  <xsl:when test="string-length($maintitle) > 20">
                    <xsl:value-of select="concat(substring($maintitle,0,20), '...')"></xsl:value-of>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$maintitle" />
                  </xsl:otherwise>
                </xsl:choose>
              </li>
            </menu>
          </xsl:if>
        </div>
        <div id="content_area" class="jp-layout-content-area">
          <xsl:call-template name="jp.layout.getHTMLContent" />
        </div>
        <div id="footer" class="footer"></div>
      </body>
    </html>

  </xsl:template>

  <xsl:template match="printLatestArticles">
    <ul id="latestArticles" class="latestArticles"></ul>
  </xsl:template>
</xsl:stylesheet>