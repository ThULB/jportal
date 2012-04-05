<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_master2">
        <xsl:param name="journalID" />
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
                    <div class="navi_history">
                        <xsl:call-template name="navigation.history" />
                    </div>
                </div>
                <div id="navi_left" class="jp-layout-left-navi">
                    <div>
                        <xsl:call-template name="navigation.tree" />
                    </div>
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