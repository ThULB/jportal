<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcr="http://www.mycore.org/" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities" xmlns:websiteWriteProtection="xalan://org.mycore.frontend.MCRWebsiteWriteProtection"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:imprint="xalan://fsu.jportal.util.ImprintUtil"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions" exclude-result-prefixes="i18n imprint xsi mcr mcrxml acl xalan layoutUtils websiteWriteProtection jpxml">

  <xsl:include href="jp-layout-tools.xsl" />
 <!-- <xsl:include href="gwlb_jp-layout-functions.xsl" />-->
  <xsl:include href="jp-layout-functions.xsl" />
  <xsl:include href="jp-layout-nameOfTemplate.xsl" />
  <xsl:include href="gwlb_jp-layout-contentArea.xsl" />
  <xsl:include href="gwlb_jp-layout-contentArea-objectEditing.xsl" />
  <xsl:include href="jp-layout-contentArea-advancedsearch.xsl" />
  <xsl:include href="gwlb_jp-layout-footer.xsl" />
  <xsl:include href="gwlb_jp-layout-contentArea-breadcrumb.xsl" />

  <xsl:include href="gwlb_jp-navigation-top.xsl" />
  <xsl:include href="jp-globalmessage.xsl" />

  <xsl:include href="gwlb_jp-layout-searchbar.xsl" />

  <!--<xsl:include href="xslInclude:index" />-->
  <xsl:include href="jp_template_utils.xsl"/>
  <xsl:include href="xslInclude:jportal" />
  <xsl:include href="xslInclude:components" />
  <xsl:include href="xslInclude:class.templates" />

  <xsl:param name="JP.Site.label" />
  <xsl:param name="JP.Site.Parent.label" />
  <xsl:param name="JP.Site.Parent.url" />
  <xsl:param name="JP.Site.HTML.Head.Meta.Keywords.de" />
  <xsl:param name="JP.Site.HTML.Head.Meta.Keywords.en" />

  <xsl:param name="object" />
  <xsl:param name="layout" />
  <xsl:param name="MCR.NameOfProject" />

  <xsl:param name="returnURL" />
  <xsl:param name="returnHash" />
  <xsl:param name="returnID" />
  <xsl:param name="returnName" />

  <!-- user -->
  <xsl:variable name="user" select="jpxml:getUserID()" />

  <xsl:variable name="languages" select="jpxml:getLanguages()/languages" />
  <xsl:variable name="objSetting" select="xalan:nodeset($objSettingXML)" />
  <xsl:variable name="ImageBaseURL" select="concat($WebApplicationBaseURL,'images/') " />
  <xsl:variable name="MainTitle" select="$MCR.NameOfProject" />

  <xsl:variable name="PageTitleGWLB" select="i18n:translate('jp.site.home.title.gwlb')" />

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
        <xsl:value-of select="'template_gwlb'" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$tmp" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="searchMode">
    <xsl:call-template name="jp.getSearchMode" />
  </xsl:variable>

  <xsl:variable name="templateResourcePath" select="concat('jp_templates/',$template, '/')" />
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
        <link href="{$WebApplicationBaseURL}webjars/bootstrap/3.3.4/css/bootstrap.min.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="{$WebApplicationBaseURL}css/gwlb/jp-default.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-editor.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-local-overrides.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-move-obj.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}webjars/font-awesome/5.12.0/css/fontawesome.min.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}webjars/font-awesome/5.12.0/css/solid.min.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}webjars/font-awesome/5.12.0/css/regular.min.css" rel="stylesheet" type="text/css" />


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

          <xsl:if test="jpxml:resourceExist(concat($templateResourcePath, 'IMAGES/Pixel_Left.png'))">
            <style type="text/css">
              #pixelLeft {
              background-image: url(<xsl:value-of select="concat($templateWebURL, 'IMAGES/Pixel_Left.png')" />);
              }
            </style>
          </xsl:if>

          <xsl:if test="jpxml:resourceExist(concat($templateResourcePath, 'IMAGES/Pixel_Right.png'))">
            <style type="text/css">
              #pixelRight {
              background-image: url(<xsl:value-of select="concat($templateWebURL, 'IMAGES/Pixel_Right.png')" />);
              }
            </style>
          </xsl:if>
        </xsl:if>
        <script type="text/javascript">
          var jp = jp || {};
          jp.baseURL = '<xsl:value-of select="$WebApplicationBaseURL" />';
          jp.journalID = '<xsl:value-of select="$journalID" />';
          jp.journalID = jp.journalID != '' ? jp.journalID : null;
          jp.lang = '<xsl:value-of select="i18n:getCurrentLocale()" />';

          window.addEventListener("resize", function(){
            var content= jQuery("#jp-journal-content");
            content.css({"min-height":""});
            content.css({"min-height": jQuery("#footer").offset().top-content.offset().top+"px" });
          });
        </script>
        <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/jquery/2.1.4/dist/jquery.min.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-layout-controller.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/bootstrap/3.3.4/js/bootstrap.min.js" />
        <xsl:if test="not(mcrxml:isCurrentUserGuestUser())">
          <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/bootstrap3-dialog/1.35.4/dist/js/bootstrap-dialog.min.js" />
        </xsl:if>

        <!-- Piwik -->
        <!--<xsl:call-template name="jp.piwik" />-->

        <xsl:variable name="type" select="substring-before(substring-after(/mycoreobject/@ID,'_'),'_')" />

        <!-- add html stuff to head for MyCoReWebPage -->
        <xsl:copy-of select="/MyCoReWebPage/head/bottom/*" />
      </head>
      <body>
        <xsl:if test="not($template = 'template_gwlb')">
            <div id="pixelLeft"></div>
            <div id="pixelRight"></div>
        </xsl:if>
        <div id="border">
        <div id="mainWrapper">
        <xsl:choose>
          <xsl:when test="$template = 'template_gwlb'">
      <div id="globalHeader">
        <div class="row">
          <div class="col-md-6 navbar-header">
            <button type="button" class="navbar-toggle collapsed jp-layout-mynavbarbutton" data-toggle="collapse" data-target="#navbar-collapse-globalHeader">
              <span class="sr-only">Toggle navigation</span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
            </button>
            <img class="GBV" src="{concat($templateWebURL, 'IMAGES/VZG.png')}"></img>
            <ul class="list-inline jp-layout-mainHeader-UlLeft">
              <!--<li class="jp-layout-mainHeader-SeperatorRight">-->
                <!--<xsl:variable name="imprintHref">-->
                  <!--<xsl:choose>-->
                    <!--<xsl:when test="$journalID != '' and imprint:has($journalID, 'imprint')">-->
                      <!--<xsl:value-of select="concat($WebApplicationBaseURL, 'rsc/fs/imprint/webpage/', $journalID)" />-->
                    <!--</xsl:when>-->
                    <!--<xsl:otherwise>-->
                      <!--<xsl:value-of select="concat($WebApplicationBaseURL, 'jp-imprint.xml')" />-->
                    <!--</xsl:otherwise>-->
                  <!--</xsl:choose>-->
                <!--</xsl:variable>-->
                <!--<a href="{$imprintHref}">-->
                  <!--<xsl:value-of select="i18n:translate('jp.site.imprint')" />-->
                <!--</a>-->
              <!--</li>-->
              <!--<xsl:if test="$journalID != '' and imprint:has($journalID, 'partner')">-->
                <!--<li class="jp-layout-mainHeader-SeperatorRight">-->
                  <!--<a href="{concat($WebApplicationBaseURL, 'rsc/fs/partner/webpage/', $journalID)}">-->
                    <!--<xsl:value-of select="i18n:translate('jp.site.partner')" />-->
                  <!--</a>-->
                <!--</li>-->
              <!--</xsl:if>-->
              <!--<li class="jp-layout-mainHeader-LiPaPushleft">-->
                <!--<a href="{$WebApplicationBaseURL}content/below/index.xml" target="_self">-->
                  <!--Kontakt-->
                <!--</a>-->
              <!--</li>-->

              <li class="jp-layout-mainHeader-LiPaPushleft">
                <a href="{$WebApplicationBaseURL}content/below/index.xml" target="_self">
                  Zeitschriftenserver der VZG
                </a>
              </li>
             <!-- <li class="jp-layout-mainheader-LiPaPushright">Digitale Bibliothek
              </li> -->
            </ul>
          </div>
          <div id="navbar-collapse-globalHeader" class="col-md-6 collapse navbar-collapse navbar-right">
            <!-- <ul class="list-inline" style="padding: 10px"> </ul> -->
            <xsl:call-template name="jp.navigation.top" />
            <p class="jp-layout-mainheader-LiPaPushright">Digitale Bibliothek</p>
          </div>
        </div>
      </div>

      <xsl:apply-templates select="document('getData:config/jp-globalmessage.xml')/globalmessage" />
      <div id="logo">
      </div>
            <xsl:if test="response/result/@name = 'response'">
              <xsl:call-template name="searchBreadcrumb" >
                <xsl:with-param name="objID" select="$journalID" />
                <xsl:with-param name="returnURL" select="$returnURL" />
                <xsl:with-param name="returnHash" select="$returnHash" />
                <!-- returnID = is something like jportal_jpjournal_00000024 also id from where you came -->
                <xsl:with-param name="returnID" select="$returnID" />
                <!-- returnName = if no id then give a name, like advanced search or law search (only i18n format)-->
                <xsl:with-param name="returnName" select="$returnName" />
              </xsl:call-template>
            </xsl:if>
        </xsl:when>
          <xsl:otherwise>
            <div id="globalHeader">
                <div id="logo">
                <div class="col-md-6 navbar-header">
                  <button type="button" class="navbar-toggle collapsed jp-layout-mynavbarbutton" data-toggle="collapse" data-target="#navbar-collapse-globalHeader">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                  </button>
                </div>
                <div id="navbar-collapse-globalHeader" class="col-md-6 collapse navbar-collapse navbar-right">
                  <!-- <ul class="list-inline" style="padding: 10px"> </ul> -->
                  <xsl:call-template name="jp.navigation.top" />
                </div>
              </div>
            </div>
            <xsl:apply-templates select="document('getData:config/jp-globalmessage.xml')/globalmessage" />

          <!-- breadcrumb -->
          <xsl:if test="not($currentType='person' or $currentType='jpinst')">
            <xsl:call-template name="breadcrumb" />
          </xsl:if>
          </xsl:otherwise>
          </xsl:choose>

        <!-- searchbar -->
        <xsl:call-template name="jp.layout.searchbar" />

        <div id="main">
          <xsl:choose>
            <xsl:when test="MyCoReWebPage">
              <xsl:apply-templates mode="webpage" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates />
            </xsl:otherwise>
          </xsl:choose>

          <!-- call dynamic template_*.xsl -->
          <xsl:if test="$template != ''">
            <xsl:variable name="templateXML">
              <template id="{$template}" />
            </xsl:variable>
            <xsl:apply-templates select="xalan:nodeset($templateXML)" mode="template">
              <!-- mcrObj is node mycoreobject root -->
              <xsl:with-param name="mcrObj" select="/mycoreobject" />
            </xsl:apply-templates>
            <xsl:apply-templates select="xalan:nodeset($templateXML)" mode="template">
              <!-- mcrObj is node mycoreobject root -->
              <xsl:with-param name="mcrObj" select="/mycoreobject" />
            </xsl:apply-templates>
          </xsl:if>

        </div>
        </div>
        <!-- delete -->
        <xsl:call-template name="jp.object.editing.delete.dialog" />
        <!-- add html stuff to end of body for MyCoReWebPage -->
        <xsl:copy-of select="/MyCoReWebPage/body/*" />
        </div>
        <!-- footer -->
        <xsl:call-template name="gwlb_jp.layout.footer" />
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
          <xsl:choose>
            <xsl:when test="/MyCoReWebPage/section/jpindex">
              <!--<xsl:copy-of select="$PageTitleGWLB" />-->
              <xsl:copy-of select="$JP.Site.label" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="$PageTitle" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="concat($titleFront,' - ',$MainTitle)" />
  </xsl:template>

  <xsl:template match="noEditor">
    <xsl:copy-of select="*" />
  </xsl:template>
</xsl:stylesheet>