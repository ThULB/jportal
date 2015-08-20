<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcr="http://www.mycore.org/" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities" xmlns:websiteWriteProtection="xalan://org.mycore.frontend.MCRWebsiteWriteProtection"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions" exclude-result-prefixes="xsi mcr mcrxml acl xalan layoutUtils websiteWriteProtection jpxml">

  <xsl:include href="jp-layout-tools.xsl" />
  <xsl:include href="jp-layout-functions.xsl" />
  <xsl:include href="jp-layout-nameOfTemplate.xsl" />
  <xsl:include href="jp-layout-contentArea.xsl" />
  <xsl:include href="jp-layout-contentArea-objectEditing.xsl" />
  <xsl:include href="jp-layout-contentArea-advancedsearch.xsl" />
  <xsl:include href="jp-layout-footer.xsl" />

  <xsl:include href="jp-navigation-top.xsl" />
  <xsl:include href="jp-globalmessage.xsl" />

  <xsl:include href="jp-layout-searchbar.xsl" />

  <xsl:include href="xslInclude:modules" />
  <xsl:include href="xslInclude:class.templates" />

  <xsl:param name="JP.Site.label" />
  <xsl:param name="JP.Site.Parent.label" />
  <xsl:param name="JP.Site.Parent.url" />
  <xsl:param name="JP.Site.HTML.Head.Meta.Keywords.de" />
  <xsl:param name="JP.Site.HTML.Head.Meta.Keywords.en" />

  <xsl:param name="object" />
  <xsl:param name="layout" />
  <xsl:param name="MCR.NameOfProject" />

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
    <xsl:variable name="tmp">
      <xsl:call-template name="jp.getNameOfTemplate" />
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$tmp = ''">
        <xsl:value-of select="'template_master2'" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$tmp" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="searchMode">
    <xsl:call-template name="jp.getSearchMode" />
  </xsl:variable>

  <xsl:variable name="templateResourcePath" select="concat('jp_templates/', $template, '/')" />
  <xsl:variable name="templateWebURL" select="concat($WebApplicationBaseURL, 'jp_templates/', $template, '/')" />

  <!-- TODO: remove this -->
  <xsl:variable name="wcms.useTargets" select="'no'" />

  <xsl:variable name="objectEditingHTML">
    <editing>
      <xsl:call-template name="jp.object.editing.items" />
    </editing>
  </xsl:variable>
  <xsl:variable name="objectEditing" select="xalan:nodeset($objectEditingHTML)/editing" />

  <xsl:template name="renderLayout">
    <xsl:if test="/mycoreobject/@ID">
      <xsl:variable name="setObjIDInSession" select="jpxml:setLastValidPageID(/mycoreobject/@ID)" />
    </xsl:if>

    <html>
      <head>
        <title>
          <xsl:call-template name="HTMLPageTitle" />
        </title>
        <meta content="Zeitschriften-Portal" lang="de" name="description" />
        <meta content="Journal-Portal" lang="en" name="description" />
        <meta content="{$JP.Site.HTML.Head.Meta.Keywords.de}" lang="de" name="keywords" />
        <meta content="{$JP.Site.HTML.Head.Meta.Keywords.en}" lang="en" name="keywords" />
        <meta content="MyCoRe" lang="de" name="generator" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <!-- add html stuff to head for MyCoReWebPage-->
        <xsl:copy-of select="/MyCoReWebPage/head/top/*" />
        <link href="{$WebApplicationBaseURL}bower_components/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="{$WebApplicationBaseURL}css/jp-default.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-editor.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-local-overrides.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-move-obj.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}bower_components/fontawesome/css/font-awesome.min.css" rel="stylesheet" type="text/css" />


        <link href='http://fonts.googleapis.com/css?family=Open+Sans:400,700&amp;subset=latin-ext,cyrillic' rel='stylesheet' type='text/css' />
        <xsl:if test="starts-with($RequestURL, concat($WebApplicationBaseURL, 'content/below/index.xml'))">
          <link href="http://fonts.googleapis.com/css?family=PT+Sans+Narrow&amp;subset=latin-ext,cyrillic" rel="stylesheet" type="text/css" />
        </xsl:if>
       
        <xsl:if test="$template != ''">
          <xsl:if test="jpxml:resourceExist(concat($templateResourcePath, 'IMAGES/logo.png'))">
            <style type="text/css">
              #logo {
              background-image: url(<xsl:value-of select="concat($templateWebURL, 'IMAGES/logo.png')" />);
              }
            </style>
          </xsl:if>
          <xsl:if test="jpxml:resourceExist(concat($templateResourcePath, 'CSS/', $template, '.css'))">
            <link href="{$templateWebURL}CSS/{$template}.css" rel="stylesheet" type="text/css" />
          </xsl:if>
        </xsl:if>
        <script type="text/javascript">
          var jp = jp || {};
          jp.baseURL = '<xsl:value-of select="$WebApplicationBaseURL" />';
          jp.journalID = '<xsl:value-of select="$journalID" />';
          jp.journalID = jp.journalID != '' ? jp.journalID : null;
        </script>
        <script type="text/javascript" src="{$WebApplicationBaseURL}bower_components/jquery/dist/jquery.min.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-layout-controller.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}bower_components/bootstrap/dist/js/bootstrap.js" />
        <xsl:if test="not(mcrxml:isCurrentUserGuestUser())">
          <script type="text/javascript" src="{$WebApplicationBaseURL}bower_components/bootstrap-dialog/dist/js/bootstrap-dialog.min.js" />
        </xsl:if>

        <!-- Piwik -->
        <xsl:call-template name="jp.piwik" />

        <xsl:variable name="type" select="substring-before(substring-after(/mycoreobject/@ID,'_'),'_')" />

        <!-- add html stuff to head for MyCoReWebPage -->
        <xsl:copy-of select="/MyCoReWebPage/head/bottom/*" />
      </head>
      <body>
        <div id="globalHeader">
          <div class="row">
            <div class="col-md-6 navbar-header">
              <button type="button" class="navbar-toggle collapsed jp-layout-mynavbarbutton" data-toggle="collapse" data-target="#navbar-collapse-globalHeader">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </button>
            
              <ul class="list-inline jp-layout-mainHeader-UlLeft">
                <li class="jp-layout-mainHeader-SeperatorRight">
                  <a href="{$JP.Site.Parent.url}" target="_blank">
                    <xsl:value-of select="$JP.Site.Parent.label" />
                  </a>
                </li>
                <li class="jp-layout-mainHeader-LiPaPushleft">
                  <a href="{$WebApplicationBaseURL}content/below/index.xml" target="_self">
                    <xsl:value-of select="$JP.Site.label" />
                  </a>
                </li>
                <xsl:if test="websiteWriteProtection:isActive() and $CurrentUser != 'gast'">
                  <li>
                    <span class="webWriteProtection">
                      <xsl:value-of select="websiteWriteProtection:getMessage()" />
                    </span>
                  </li>
                </xsl:if>
              </ul>
            </div>
            <div id="navbar-collapse-globalHeader" class="col-md-6 collapse navbar-collapse navbar-right">
              <!-- <ul class="list-inline" style="padding: 10px"> </ul> -->
              <xsl:call-template name="jp.navigation.top" />
            </div>
          </div>
        </div>

        <xsl:apply-templates select="document('getData:config/jp-globalmessage.xml')/globalmessage" />
        <div id="logo"></div>

        <!-- searchbar -->
        <xsl:call-template name="jp.layout.searchbar" />

        <div id="main">
          <xsl:apply-templates />
          <!-- call dynamic template_*.xsl -->
          <xsl:if test="$template != ''">
            <xsl:variable name="templateXML">
              <template id="{$template}" />
            </xsl:variable>
            <xsl:apply-templates select="xalan:nodeset($templateXML)" mode="template">
              <!-- mcrObj is node mycoreobject root -->
              <xsl:with-param name="mcrObj" select="/mycoreobject" />
            </xsl:apply-templates>
          </xsl:if>

        </div>
        <!-- footer -->
        <xsl:call-template name="jp.layout.footer" />

        <!-- delete -->
        <xsl:call-template name="jp.object.editing.delete.dialog" />
        <div id="viewerContainerWrapper" />
        <!-- add html stuff to end of body for MyCoReWebPage -->
        <xsl:copy-of select="/MyCoReWebPage/body/*" />
      </body>
    </html>
  </xsl:template>

  <xsl:template name="HTMLPageTitle">
    <xsl:variable name="titleFront">
      <xsl:choose>
        <xsl:when test="/MyCoReWebPage/journalID != ''">
          <xsl:value-of select="document(concat('mcrobject:',/MyCoReWebPage/journalID))/mycoreobject/metadata/maintitles/maintitle[@inherited='0']" />
        </xsl:when>
        <xsl:when
          test="contains(/mycoreobject/@ID,'_jpjournal_') or contains(/mycoreobject/@ID,'_jpvolume_') or contains(/mycoreobject/@ID,'_jparticle_')  ">
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
    <xsl:value-of select="concat($titleFront,' - ',$MainTitle)" />
  </xsl:template>

  <xsl:template match="noEditor">
    <xsl:copy-of select="*" />
  </xsl:template>
</xsl:stylesheet>