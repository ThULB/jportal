<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
                xmlns:mcr="http://www.mycore.org/" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities" xmlns:websiteWriteProtection="xalan://org.mycore.frontend.MCRWebsiteWriteProtection"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions" exclude-result-prefixes="xsi mcr mcrxml acl xalan layoutUtils websiteWriteProtection jpxml i18n">

  <xsl:include href="jp-layout-tools.xsl"/>
  <xsl:include href="jp-layout-functions.xsl"/>
  <xsl:include href="jp-layout-nameOfTemplate.xsl"/>
  <xsl:include href="jp-layout-contentArea.xsl"/>
  <xsl:include href="jp-layout-contentArea-objectEditing.xsl"/>
  <xsl:include href="jp-layout-contentArea-advancedsearch.xsl"/>
  <xsl:include href="jp-layout-footer.xsl"/>

  <xsl:include href="jp-navigation-top.xsl"/>
  <xsl:include href="jp-globalmessage.xsl"/>

  <xsl:include href="jp-layout-searchbar.xsl"/>

  <xsl:include href="jp_template_utils.xsl"/>
  <xsl:include href="xslInclude:jportal"/>
  <xsl:include href="xslInclude:components"/>
  <xsl:include href="xslInclude:class.templates"/>

  <xsl:param name="JP.Site.label"/>
  <xsl:param name="JP.Site.Parent.label"/>
  <xsl:param name="JP.Site.Parent.url"/>
  <xsl:param name="JP.Site.HTML.Head.Meta.Keywords.de"/>
  <xsl:param name="JP.Site.HTML.Head.Meta.Keywords.en"/>

  <xsl:param name="object"/>
  <xsl:param name="layout"/>
  <xsl:param name="MCR.NameOfProject"/>

  <!-- user -->
  <xsl:variable name="user" select="jpxml:getUserID()"/>

  <xsl:variable name="languages" select="jpxml:getLanguages()/languages"/>
  <xsl:variable name="objSetting" select="xalan:nodeset($objSettingXML)"/>
  <xsl:variable name="ImageBaseURL" select="concat($WebApplicationBaseURL,'images/') "/>
  <xsl:variable name="MainTitle" select="$MCR.NameOfProject"/>

  <xsl:variable name="objSettingXML">
    <title allowHTML="true"/>
  </xsl:variable>
  <xsl:variable name="journalID">
    <xsl:call-template name="jp.getJournalID"/>
  </xsl:variable>
  <xsl:variable name="template">
    <xsl:variable name="tmp">
      <xsl:call-template name="jp.getNameOfTemplate"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$tmp = ''">
        <xsl:value-of select="'template_master'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$tmp"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="searchMode">
    <xsl:call-template name="jp.getSearchMode"/>
  </xsl:variable>

  <xsl:variable name="templateResourcePath" select="concat('jp_templates/', $template, '/')"/>
  <xsl:variable name="templateWebURL" select="concat($WebApplicationBaseURL, 'jp_templates/', $template, '/')"/>

  <!-- TODO: remove this -->
  <xsl:variable name="wcms.useTargets" select="'no'"/>

  <xsl:variable name="objectEditingHTML">
    <editing>
      <xsl:call-template name="jp.object.editing.items"/>
    </editing>
  </xsl:variable>
  <xsl:variable name="objectEditing" select="xalan:nodeset($objectEditingHTML)/editing"/>

  <xsl:template name="renderLayout">
    <html>
      <head>
        <title>
          <xsl:call-template name="HTMLPageTitle"/>
        </title>
        <meta content="Zeitschriften-Portal" lang="de" name="description"/>
        <meta content="Journal-Portal" lang="en" name="description"/>
        <meta content="{$JP.Site.HTML.Head.Meta.Keywords.de}" lang="de" name="keywords"/>
        <meta content="{$JP.Site.HTML.Head.Meta.Keywords.en}" lang="en" name="keywords"/>
        <meta content="MyCoRe" lang="de" name="generator"/>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <!-- add html stuff to head for MyCoReWebPage-->
        <xsl:copy-of select="/MyCoReWebPage/head/top/*"/>
        <link href="{$WebApplicationBaseURL}webjars/font-awesome/4.4.0/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
        <link href="{$WebApplicationBaseURL}webjars/bootstrap/3.3.4/css/bootstrap.min.css" rel="stylesheet" type="text/css" media="screen"/>
        <link href="{$WebApplicationBaseURL}css/jp-default.css" rel="stylesheet" type="text/css"/>
        <link href="{$WebApplicationBaseURL}css/jp-editor.css" rel="stylesheet" type="text/css"/>
        <link href="{$WebApplicationBaseURL}css/jp-local-overrides.css" rel="stylesheet" type="text/css"/>
        <link href="{$WebApplicationBaseURL}css/jp-local-overrides.css" rel="stylesheet" type="text/css"/>
        <link href="{$WebApplicationBaseURL}css/jp-move-obj.css" rel="stylesheet" type="text/css"/>

        <link href='https://fonts.googleapis.com/css?family=Open+Sans:400,700&amp;subset=latin-ext,cyrillic' rel='stylesheet' type='text/css'/>
        <xsl:if test="starts-with($RequestURL, concat($WebApplicationBaseURL, 'content/below/index.xml'))">
          <link href="https://fonts.googleapis.com/css?family=PT+Sans+Narrow&amp;subset=latin-ext,cyrillic" rel="stylesheet" type="text/css"/>
        </xsl:if>

        <xsl:if test="$template != ''">
          <xsl:if test="jpxml:resourceExist(concat($templateResourcePath, 'IMAGES/logo.png'))">
            <style type="text/css">
              #header {
              background-image: url(<xsl:value-of select="concat($templateWebURL, 'IMAGES/logo.png')"/>);
              }
            </style>
          </xsl:if>
          <xsl:if test="jpxml:resourceExist(concat($templateResourcePath, 'CSS/', $template, '.css'))">
            <link href="{$templateWebURL}CSS/{$template}.css" rel="stylesheet" type="text/css"/>
          </xsl:if>
          <xsl:if test="/MyCoReWebPage/section/jpindex and jpxml:resourceExist(concat($templateResourcePath, 'CSS/', 'index.css'))">
            <link href="{$templateWebURL}CSS/index.css" rel="stylesheet" type="text/css"/>
          </xsl:if>
        </xsl:if>
        <script type="text/javascript">
          var jp = jp || {};
          jp.baseURL = '<xsl:value-of select="$WebApplicationBaseURL"/>';
          jp.journalID = '<xsl:value-of select="$journalID"/>';
          jp.journalID = jp.journalID != '' ? jp.journalID : null;
          jp.lang = '<xsl:value-of select="i18n:getCurrentLocale()"/>';
          jp.isGuest ='<xsl:value-of select="mcrxml:isCurrentUserGuestUser()" />' !== "false";
        </script>
        <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/jquery/2.1.4/dist/jquery.min.js"/>
        <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/bootstrap/3.3.4/js/bootstrap.min.js"/>
        <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/bootstrap-dialog/1.34.6/dist/js/bootstrap-dialog.min.js"/>
        <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/mustachejs/0.8.2/mustache.js"></script>
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-layout-controller.js"/>

        <!-- Piwik -->
        <xsl:call-template name="jp.piwik"/>

        <xsl:variable name="type" select="substring-before(substring-after(/mycoreobject/@ID,'_'),'_')"/>

        <!-- add html stuff to head for MyCoReWebPage -->
        <xsl:copy-of select="/MyCoReWebPage/head/bottom/*"/>
      </head>
      <body>
        <div class="background">
          <xsl:apply-templates select="document('getData:config/jp-globalmessage.xml')/globalmessage"/>

          <div id="header" class="container-fluid">

            <div id="globalHeader">
              <xsl:call-template name="jp.navigation.top"/>
            </div>

            <div id="logo"></div>
            <!-- searchbar -->
            <xsl:call-template name="jp.layout.searchbar"/>
          </div>

          <div id="main" class="container-fluid">
            <xsl:apply-templates/>
            <!-- call dynamic template_*.xsl -->
            <xsl:if test="$template != '' and $journalID != ''">
              <xsl:variable name="templateXML">
                <template id="{$template}"/>
              </xsl:variable>
              <xsl:apply-templates select="xalan:nodeset($templateXML)" mode="template">
                <xsl:with-param name="mcrObj" select="/mycoreobject"/>
              </xsl:apply-templates>
            </xsl:if>
          </div>

          <!-- footer -->
          <xsl:call-template name="jp.layout.footer"/>

          <!-- delete -->
          <xsl:call-template name="jp.object.editing.delete.dialog"/>
          <!-- add html stuff to end of body for MyCoReWebPage -->
          <xsl:copy-of select="/MyCoReWebPage/body/*"/>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="HTMLPageTitle">
    <xsl:variable name="titleFront">
      <xsl:choose>
        <xsl:when test="/MyCoReWebPage/journalID != ''">
          <xsl:value-of select="document(concat('mcrobject:',/MyCoReWebPage/journalID))/mycoreobject/metadata/maintitles/maintitle[@inherited='0']"/>
        </xsl:when>
        <xsl:when
                test="contains(/mycoreobject/@ID,'_jpjournal_') or contains(/mycoreobject/@ID,'_jpvolume_') or contains(/mycoreobject/@ID,'_jparticle_')  ">
          <xsl:value-of select="/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text()"/>
        </xsl:when>
        <xsl:when test="contains(/mycoreobject/@ID,'_jpinst_') ">
          <xsl:copy-of select="/mycoreobject/metadata/names/name/fullname/text()"/>
        </xsl:when>
        <xsl:when test="contains(/mycoreobject/@ID,'_person_') ">
          <xsl:copy-of select="/mycoreobject/metadata/def.heading/heading/lastName/text()"/>
          <xsl:if test="/mycoreobject/metadata/def.heading/heading/firstName/text()">
            <xsl:copy-of select="concat(', ',/mycoreobject/metadata/def.heading/heading/firstName/text())"/>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$PageTitle"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="concat($titleFront,' - ',$MainTitle)"/>
  </xsl:template>

  <xsl:template match="noEditor">
    <xsl:copy-of select="*"/>
  </xsl:template>
</xsl:stylesheet>