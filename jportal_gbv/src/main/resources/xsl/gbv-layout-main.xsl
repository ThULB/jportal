<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mcr="http://www.mycore.org/"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities"
  xmlns:websiteWriteProtection="xalan://org.mycore.frontend.MCRWebsiteWriteProtection" xmlns:jpxml="xalan://org.mycore.common.xml.MCRJPortalXMLFunctions"
  exclude-result-prefixes="xsi mcr acl xalan layoutUtils websiteWriteProtection jpxml">

  <xsl:include href="jp-layout-tools.xsl" />
  <xsl:include href="jp-layout-functions.xsl" />
  <xsl:include href="jp-layout-nameOfTemplate.xsl" />
  <xsl:include href="jp-layout-contentArea.xsl" />
  <xsl:include href="jp-layout-contentArea-objectEditing.xsl" />
  <xsl:include href="jp-layout-contentArea-advancedsearch.xsl" />
  <xsl:include href="jp-layout-mcrwebpage.xsl" />
  <xsl:include href="jp-layout-footer.xsl" />
  <xsl:include href="jp-navigation-top.xsl" />
  <xsl:include href="jp-layout-searchbar.xsl" />
  <xsl:include href="jp-globalmessage.xsl" />

  <xsl:include href="gbv-journalList.xsl" />
  <xsl:include href="gbv-breadcrumb.xsl" />
  <xsl:include href="gbv-response-default.xsl" />
  <xsl:include href="gbv-editMenu.xsl" />
  <xsl:include href="gbv-latestArticles.xsl" />

  <xsl:include href="xslInclude:modules" />
  <xsl:include href="xslInclude:templates" />

  <xsl:param name="JP.Site.label" />
  <xsl:param name="JP.Site.Parent.label" />
  <xsl:param name="JP.Site.Parent.url" />
  <xsl:param name="JP.Site.HTML.Head.Meta.Keywords.de" />
  <xsl:param name="JP.Site.HTML.Head.Meta.Keywords.en" />

  <xsl:param name="object" />
  <xsl:param name="layout" />
  <xsl:param name="MCR.NameOfProject" />
    <!-- For Subselect -->
  <xsl:param name="subselect.type" select="''" />
  <xsl:param name="subselect.session" select="''" />
  <xsl:param name="subselect.varpath" select="''" />
  <xsl:param name="subselect.webpage" select="''" />
    <!-- Search modes -->
  <xsl:param name="mode" select="'default'" />
    <!-- user -->
  <xsl:variable name="user" select="jpxml:getUserID()" />

  <xsl:variable name="languages" select="jpxml:getLanguages()/languages" />
  <xsl:variable name="objSetting" select="xalan:nodeset($objSettingXML)" />
  <xsl:variable name="ImageBaseURL" select="concat($WebApplicationBaseURL,'images/') " />
  <xsl:variable name="MainTitle" select="$MCR.NameOfProject" />

  <xsl:variable name="objSettingXML">
    <title allowHTML="true" />
  </xsl:variable>
  <xsl:variable name="journalID">
    <xsl:call-template name="jp.getJournalID" />
  </xsl:variable>
  <xsl:variable name="template">
    <xsl:call-template name="jp.getNameOfTemplate" />
  </xsl:variable>
  <xsl:variable name="searchMode">
    <xsl:call-template name="jp.getSearchMode" />
  </xsl:variable>
  <xsl:variable name="templateResourcePath" select="concat('templates/', $template, '/')" />
  <xsl:variable name="templateWebURL" select="concat($WebApplicationBaseURL, 'templates/', $template, '/')" />

    <!-- TODO: remove this -->
  <xsl:variable name="wcms.useTargets" select="'no'" />

  <xsl:variable name="webPath" select="substring-after($RequestURL, $WebApplicationBaseURL)" />

  <xsl:variable name="pageListXML">
    <page name="content/below/index.xml" />
    <page name="gbv-journalList.xml" />
  </xsl:variable>
  <xsl:variable name="pageList" select="xalan:nodeset($pageListXML)" />

  <xsl:variable name="maxWidthPageListXML">
    <page name="rsc/ACLE/start" />
    <page name="jp-classeditor.xml" />
    <page name="authorization/roles-editor.xml" />
    <page name="rsc/editor" />
    <page name="fileupload_commit.xml" />
    <page name="servlets/MCRFileNodeServlet" />
  </xsl:variable>
  <xsl:variable name="maxWidthPageList" select="xalan:nodeset($maxWidthPageListXML)" />

  <xsl:variable name="pageTitle">
    <xsl:call-template name="HTMLPageTitle" />
  </xsl:variable>

  <xsl:template name="renderLayout">
    <xsl:if test="/mycoreobject/@ID">
      <xsl:variable name="setObjIDInSession" select="jpxml:setLastValidPageID(/mycoreobject/@ID)" />
    </xsl:if>
    <xsl:variable name="objectEditingHTML">
      <editing>
        <xsl:call-template name="objectEditing">
          <xsl:with-param name="id" select="/mycoreobject/@ID" />
          <xsl:with-param name="dataModel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
        </xsl:call-template>
      </editing>
    </xsl:variable>
    <xsl:variable name="objectEditing" select="xalan:nodeset($objectEditingHTML)/editing" />

    <html>
      <head>
        <title>
          <xsl:value-of select="$pageTitle" />
        </title>
        <meta content="Zeitschriften-Portal" lang="de" name="description" />
        <meta content="Journal-Portal" lang="en" name="description" />
        <meta content="{$JP.Site.HTML.Head.Meta.Keywords.de}" lang="de" name="keywords" />
        <meta content="{$JP.Site.HTML.Head.Meta.Keywords.en}" lang="en" name="keywords" />
        <meta content="MyCoRe" lang="de" name="generator" />
        <!-- add html stuff to head for MyCoReWebPage-->
        <xsl:copy-of select="/MyCoReWebPage/head/top/*"/>
        <link href="{$WebApplicationBaseURL}css/bootstrap.min.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="{$WebApplicationBaseURL}css/jp-default.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-gbv.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-editor.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-move-obj.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/font-awesome.min.css" rel="stylesheet" type="text/css" />
        <xsl:if test="$template != ''">
          <xsl:if test="jpxml:resourceExist(concat($templateResourcePath, 'IMAGES/logo.png'))">
            <style type="text/css">
              #logo {
                background-image: url(<xsl:value-of select="concat($templateWebURL, 'IMAGES/logo.png')" />);
              }
            </style>
          </xsl:if>
          <xsl:if test="jpxml:resourceExist(concat($templateResourcePath, '/CSS/', $template, '.css'))">
            <link href="{$templateWebURL}/CSS/{$template}.css" rel="stylesheet" type="text/css" />
          </xsl:if>
        </xsl:if>
        <script type="text/javascript" src="{$MCR.Layout.JS.JQueryURI}" />
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/{$jqueryUI.version}/jquery-ui.min.js" />
        <script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/ckeditor/4.0.1/ckeditor.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}ckeditor/adapters/jquery.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-layout-controller.js" />
        <!-- TODO: don't init iview2 if no image is available -->
        <xsl:call-template name="initIview2JS" />

        <!-- Piwik -->
        <xsl:call-template name="jp.piwik" />

        <xsl:variable name="type" select="substring-before(substring-after(/mycoreobject/@ID,'_'),'_')" />
        <xsl:if test="acl:checkPermission('POOLPRIVILEGE',concat('update-',$type))">
          <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-iview2-derivLink.js" />
        </xsl:if>
      </head>
      <body>
        <div class="pageHolder">
          <div class="header">
            <div class="topBlock">
              <div id="globalMenu" class="globalMenu">
                <xsl:call-template name="jp.navigation.top" />
              </div>
              <a href="http://www.gbv.de" class="gbv-logo" />
              <a href="{$WebApplicationBaseURL}" class="title">Digitale Bibliothek</a>
            </div>
            <xsl:apply-templates select="document('webapp:config/jp-globalmessage.xml')/globalmessage" />
            <div class="bottomBlock">
              <xsl:if test="/mycoreobject/@ID">
                <xsl:call-template name="gbv-breadcrumb" />
              </xsl:if>
              <xsl:if test="$objectEditing/menu[@id='jp-object-editing']//li/a">
                <xsl:call-template name="gbv-editMenu" />
              </xsl:if>
            </div>
          </div>
          <div class="content">
            <xsl:choose>
              <xsl:when test="$pageList/page[starts-with($webPath, @name)]">
                <xsl:apply-templates />
              </xsl:when>
              <xsl:when test="$maxWidthPageList/page[starts-with($webPath, @name)]">
                <div class="maxWidthBlock">
                  <xsl:apply-templates />
                </div>
              </xsl:when>
              <xsl:otherwise>
                <div class="unknownContent">
                  <div class="horizontalContainer">
                    <div class="leftBlock">
                      <div class="contentBlock">
                        <xsl:call-template name="jp.layout.searchbar" />
                        <div class="caption">
                          <xsl:value-of select="$pageTitle" />
                        </div>
                      </div>
                    </div>
                    <div class="rightBlock">
                      <div class="contentBlock">
                        <xsl:apply-templates />
                      </div>
                    </div>
                  </div>
                </div>
              </xsl:otherwise>
            </xsl:choose>
          </div>
          <div class="footer">
            <xsl:if test="not($maxWidthPageList/page[starts-with($webPath, @name)])">
              <div class="horizontalContainer topBlock">
                <div class="leftBlock" />
                <div class="rightBlock" />
              </div>
            </xsl:if>
          </div>
        </div>
        <xsl:if test="$object='delete'">
          <xsl:copy-of select="$objectEditing/deleteMsg" />
        </xsl:if>
        <div id="viewerContainerWrapper" />
        <div id="ckeditorContainer">
          <div class="jp-layout-message-background"></div>
          <div id="ckeditorframe">
            <textarea id="ckeditor"></textarea>
          </div>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="HTMLPageTitle">
    <xsl:variable name="titleFront">
      <xsl:choose>
        <xsl:when test="contains(/mycoreobject/@ID,'_jpjournal_') or contains(/mycoreobject/@ID,'_jpvolume_') or contains(/mycoreobject/@ID,'_jparticle_')  ">
          <xsl:value-of select="/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text()" />
        </xsl:when>
        <xsl:when test="contains(/mycoreobject/@ID,'_jpinst_') ">
          <xsl:copy-of select="/mycoreobject/metadata/names/name/fullname/text()" />
        </xsl:when>
        <xsl:when test="contains(/mycoreobject/@ID,'_person_') ">
          <xsl:copy-of select="/mycoreobject/metadata/def.heading/heading/lastName/text()" />
          <xsl:if test="/mycoreobject/metadata/def.heading/heading/firstName/text()">
            <xsl:copy-of select="concat(', ',/mycoreobject/metadata/def.heading/heading/firstName/text())" />
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$PageTitle" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$titleFront" />
  </xsl:template>
</xsl:stylesheet>