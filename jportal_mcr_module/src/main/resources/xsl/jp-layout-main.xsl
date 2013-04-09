<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcr="http://www.mycore.org/" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities" xmlns:websiteWriteProtection="xalan://org.mycore.frontend.MCRWebsiteWriteProtection"
  xmlns:jpxml="xalan://org.mycore.common.xml.MCRJPortalXMLFunctions"
  exclude-result-prefixes="xsi mcr acl xalan layoutUtils websiteWriteProtection jpxml">

  <xsl:include href="jp-layout-tools.xsl" />
  <xsl:include href="jp-layout-functions.xsl" />
  <xsl:include href="jp-layout-nameOfTemplate.xsl" />
  <xsl:include href="jp-layout-contentArea.xsl" />
  <xsl:include href="jp-layout-contentArea-objectEditing.xsl" />
  <xsl:include href="jp-layout-mcrwebpage.xsl" />
  <xsl:include href="jp-layout-contentArea-searchResults.xsl" />
  <xsl:include href="jp-layout-footer.xsl" />
  <xsl:include href="jp-navigation-top.xsl" />
  <xsl:include href="jp-globalmessage.xsl" />
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
  <xsl:variable name="showSearchBar" select="not(contains('advanced.form laws.form', $mode))" />
  <xsl:variable name="ImageBaseURL" select="concat($WebApplicationBaseURL,'images/') " />
  <xsl:variable name="MainTitle" select="$MCR.NameOfProject" />

  <xsl:variable name="objSettingXML">
    <title allowHTML="true" />
  </xsl:variable>
  <xsl:variable name="template">
    <xsl:call-template name="nameOfTemplate" />
  </xsl:variable>
  <xsl:variable name="templateResourcePath" select="concat('templates/', $template, '/')" />
  <xsl:variable name="templateWebURL" select="concat($WebApplicationBaseURL, 'templates/', $template, '/')" />

  <!-- TODO: remove this -->
  <xsl:variable name="wcms.useTargets" select="'no'" />

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
          <xsl:call-template name="HTMLPageTitle" />
        </title>
        <meta content="Zeitschriften-Portal" lang="de" name="description" />
        <meta content="Journal-Portal" lang="en" name="description" />
        <meta content="{$JP.Site.HTML.Head.Meta.Keywords.de}" lang="de" name="keywords" />
        <meta content="{$JP.Site.HTML.Head.Meta.Keywords.en}" lang="en" name="keywords" />
        <meta content="MyCoRe" lang="de" name="generator" />
        <link href="{$WebApplicationBaseURL}css/jp-default.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-editor.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-local-overrides.css" rel="stylesheet" type="text/css" />

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

        <!-- add IE CSS to head -->
        <xsl:variable name="cssLinked">
          &lt;link href="
          <xsl:value-of select="concat($WebApplicationBaseURL,'templates/',$template,'/CSS/',$template,'_IE.css')" />"
          rel="stylesheet" type="text/css"/&gt;
        </xsl:variable>
        <xsl:comment>
          <xsl:value-of select="'[if lte IE 8]&gt;'" />
          <xsl:value-of select="$cssLinked" />
          <xsl:value-of select="'&lt;![endif]'" />
        </xsl:comment>
      </head>
      <body>
        <div id="globalHeader">
          <ul id="globalHomeLink" class="jp-layout-hMenu">
            <li>
              <a href="{$JP.Site.Parent.url}" target="_blank">
                <xsl:value-of select="$JP.Site.Parent.label" />
              </a>
            </li>
            <li>
              <a href="/content/below/index.xml" target="_self">
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
          <div id="globalMenu" class="globalMenu">
            <xsl:call-template name="jp.navigation.top" />
          </div>
        </div>
        <xsl:apply-templates select="document('webapp:config/jp-globalmessage.xml')/globalmessage" />
        <div id="logo"></div>

        <xsl:variable name="searchBarMode">
          <xsl:variable name="controllerHook">
            <jpsearchBar mode="{$mode}"/>
          </xsl:variable>   
          <xsl:apply-templates mode="controllerHook" select="xalan:nodeset($controllerHook)/jpsearchBar"/>
        </xsl:variable>

        <xsl:if test="$showSearchBar">
          <xsl:variable name="searchBar" select="xalan:nodeset($searchBarMode)"/>
          <xsl:choose>
          <xsl:when test="$searchBar/div[@id='searchBar']">
            <xsl:copy-of select="$searchBar/div[@id='searchBar']"/>
          </xsl:when>
          <xsl:otherwise>
            <div id="searchBar">
              <form id="searchForm" action="/jp-search.xml">
                <xsl:variable name="queryterm">
                  <xsl:if test="$qt != '*' and $mode != 'hidden'">
                    <xsl:value-of select="$qt" />
                  </xsl:if>
                </xsl:variable>
                <xsl:variable name="journalID">
                  <xsl:call-template name="getJournalID" />
                </xsl:variable>
                <xsl:choose>
                  <xsl:when test="$journalID != ''">
                    <input id="inputField" name="XSL.qt" value="{$queryterm}" placeholder="Suche innerhalb der Zeitschrift" title="Suche innerhalb der Zeitschrift"></input>
                    <input type="hidden" name="XSL.searchjournalID" value="{$journalID}" />
                  </xsl:when>
                  <xsl:otherwise>
                    <input id="inputField" name="XSL.qt" value="{$queryterm}" placeholder="Suche im Gesamtbestand" title="Suche im Gesamtbestand"></input>
                  </xsl:otherwise>
                </xsl:choose>
                <input id="submitButton" type="submit" value="Suche" />
              </form>
            </div>
          </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
        <div id="main">
          <xsl:choose>
            <xsl:when test="/MyCoReWebPage[//mycoreobject]">
              <xsl:variable name="objectEditingHTML">
                <editing>
                  <xsl:call-template name="objectEditing">
                    <xsl:with-param name="id" select="/mycoreobject/@ID" />
                    <xsl:with-param name="dataModel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
                  </xsl:call-template>
                </editing>
              </xsl:variable>
              <xsl:variable name="objectEditing" select="xalan:nodeset($objectEditingHTML)/editing" />
              <xsl:variable name="contentRColHtml">
                <xsl:choose>
                  <xsl:when test="$objectEditing/menu[@id='jp-object-editing']//li/a">
                    <div id="jp-content-RColumn" class="jp-layout-content-RCol">
                      <xsl:copy-of select="$objectEditing/menu[@id='jp-object-editing' and li]" />
                    </div>
                    <class for="jp-content-LColumn">jp-layout-content-LCol-RCol</class>
                  </xsl:when>
                  <xsl:otherwise>
                    <class for="jp-content-LColumn">jp-layout-content-LCol-noRCol</class>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="contentRCol" select="xalan:nodeset($contentRColHtml)" />
              <div id="jp-content-LColumn" class="jp-layout-content-LCol {$contentRCol/class[@for='jp-content-LColumn']}">
                <xsl:apply-templates />
              </div>
              <!-- Edit -->
              <xsl:copy-of select="$contentRCol/div[@id='jp-content-RColumn']"></xsl:copy-of>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates />
            </xsl:otherwise>
          </xsl:choose>   
        </div>
        <!-- footer -->
        <xsl:call-template name="jp.layout.footer" />
        <!-- delete messages -->
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
    <xsl:value-of select="concat($titleFront,' - ',$MainTitle)" />
  </xsl:template>
</xsl:stylesheet>