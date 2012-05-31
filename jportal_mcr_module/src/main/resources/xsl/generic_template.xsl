<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mcr="http://www.mycore.org/"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  exclude-result-prefixes="encoder">

  <xsl:include href="jp-layout-tools.xsl" />
  <xsl:include href="jp-layout-contentArea.xsl" />
  <xsl:include href="jp-layout-contentArea-objectEditing.xsl" />

  <!-- ============================================== -->
  <!-- the template -->
  <!-- ============================================== -->
  <xsl:variable name="objSettingXML">
    <title allowHTML="true" />
  </xsl:variable>
  <xsl:variable name="objSetting" select="xalan:nodeset($objSettingXML)" />

  <xsl:template name="renderLayout">
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

          <div id="main_navi">
            <xsl:call-template name="navigation.tree" />

            <xsl:call-template name="objectEditing">
              <xsl:with-param name="id" select="/mycoreobject/@ID" />
              <xsl:with-param name="dataModel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
            </xsl:call-template>
          </div>
        </div>

        <div id="content_area" class="jp-layout-content-area">
          <xsl:apply-templates />
        </div>

        <div id="footer" class="footer"></div>
      </body>
    </html>

  </xsl:template>
</xsl:stylesheet>