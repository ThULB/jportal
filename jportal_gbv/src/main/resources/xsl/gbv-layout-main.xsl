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
  <xsl:include href="jp-layout-mcrwebpage.xsl" />
  <xsl:include href="jp-layout-contentArea-searchResults.xsl" />
  <xsl:include href="jp-layout-footer.xsl" />
  <xsl:include href="jp-navigation-top.xsl" />
  <xsl:include href="jp-globalmessage.xsl" />

  <xsl:include href="gbv-searchbar.xsl" />
  <xsl:include href="gbv-journalList.xsl" />
  <xsl:include href="gbv-breadcrumb.xsl" />
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
  <xsl:variable name="showSearchBar" select="not(contains('advanced.form laws.form', $mode))" />
  <xsl:variable name="ImageBaseURL" select="concat($WebApplicationBaseURL,'images/') " />
  <xsl:variable name="MainTitle" select="$MCR.NameOfProject" />

  <xsl:variable name="objSettingXML">
    <title allowHTML="true" />
  </xsl:variable>
  <xsl:variable name="template">
    <xsl:call-template name="nameOfTemplate" />
  </xsl:variable>
  <xsl:variable name="templateResourcePath" select="concat('templates/master/', $template, '/')" />
  <xsl:variable name="templateWebURL" select="concat($WebApplicationBaseURL, 'templates/master/', $template, '/')" />

    <!-- TODO: remove this -->
  <xsl:variable name="wcms.useTargets" select="'no'" />

  <xsl:variable name="webPath" select="substring-after($RequestURL, $WebApplicationBaseURL)" />

  <xsl:variable name="pageListXML">
    <page name="content/below/index.xml" />
    <page name="gbv-journalList.xml" />
  </xsl:variable>
  <xsl:variable name="pageList" select="xalan:nodeset($pageListXML)" />

  <xsl:variable name="maxWidthPageListXML">
    <page name="servlets/MCRACLEditorServlet" />
    <page name="jp-classeditor.xml" />
    <page name="authorization/roles-editor.xml" />
    <page name="editor_form_" />
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
        <link href="{$WebApplicationBaseURL}css/jp-default.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-gbv.less" rel="stylesheet/less" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/jp-editor.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}css/font-awesome.min.css" rel="stylesheet" type="text/css" />
        <script type="text/javascript" src="{$MCR.Layout.JS.JQueryURI}" />
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/{$jqueryUI.version}/jquery-ui.min.js" />
        <script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/ckeditor/4.0.1/ckeditor.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}ckeditor/adapters/jquery.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-layout-controller.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/less-1.3.3.min.js" />
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
                        <xsl:call-template name="gbv-searchbar" />
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

            <!-- 
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
                            <xsl:copy-of select="$contentRCol/div[@id='jp-content-RColumn']"></xsl:copy-of>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates />
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
                <xsl:call-template name="jp.layout.footer" />
                <xsl:if test="$object='delete'">
                    <xsl:copy-of select="$objectEditing/deleteMsg" />
                </xsl:if>
                <div id="viewerContainerWrapper" />
                <div id="ckeditorContainer">
                    <div class="jp-layout-message-background"></div>
                    <div id="ckeditorframe">
                        <textarea id="ckeditor"></textarea>
                    </div>
                </div> -->
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