<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcr="http://www.mycore.org/" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  <xsl:include href="jp-layout-tools.xsl" />
  <xsl:include href="jp-layout-nameOfTemplate.xsl" />
  <xsl:include href="jp-layout-contentArea.xsl" />
  <xsl:include href="jp-layout-contentArea-objectEditing.xsl" />
  <xsl:include href="jp-layout-mcrwebpage.xsl" />
  <xsl:include href="jp-layout-contentArea-searchResults.xsl" />

  <xsl:param name="object" />
  <xsl:param name="layout" />

  <xsl:variable name="objSettingXML">
    <title allowHTML="true" />
  </xsl:variable>
  <xsl:variable name="objSetting" select="xalan:nodeset($objSettingXML)" />

  <xsl:template name="renderLayout">
    <xsl:choose>
      <xsl:when test="$layout = 'old'">
        <xsl:call-template name="renderLayout_old" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="renderLayout_new" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="renderLayout_new">
    <xsl:variable name="nameOfTemplate">
      <xsl:call-template name="nameOfTemplate" />
    </xsl:variable>
    
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
        <meta content="Zeitschriften,historisch,aktuell,Paper,Forschung,UrMEL,ThULB, FSU Jena,Langzeitarchivierung" lang="de" name="keywords" />
        <meta content="Journals,EJournals,historical,currently,paper,research,UrMEL,ThULB, FSU Jena,long term preservation" lang="en"
          name="keywords" />
        <meta content="MyCoRe" lang="de" name="generator" />
        <link href="{$WebApplicationBaseURL}jp-layout-default.css" rel="stylesheet" type="text/css" />
        <xsl:if test="$nameOfTemplate != ''">
          <link href="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/CSS/{$nameOfTemplate}.css" rel="stylesheet" type="text/css" />
        </xsl:if>
        <link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}templates/content/template_logos/CSS/sponsoredlogos.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}style_userManagement.css" rel="stylesheet" type="text/css" />
        <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js" type="text/javascript" />
        <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js" type="text/javascript" />
        <script type="text/javascript" src="{$MCR.Layout.JS.JQueryURI}" />
        
        <xsl:variable name="type" select="substring-before(substring-after(/mycoreobject/@ID,'_'),'_')" />
        <xsl:if test="acl:checkPermission('CRUD',concat('update_',$type))">
          <script type="text/javascript" src="{$WebApplicationBaseURL}iview/js/iview2DerivLink.js" />
        </xsl:if>

        <!-- add IE CSS to head -->
        <xsl:variable name="cssLinked">
          &lt;link href="
          <xsl:value-of select="concat($WebApplicationBaseURL,'templates/master/',$nameOfTemplate,'/CSS/',$nameOfTemplate,'_IE.css')" />"
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
              <a href="http://www.urmel-dl.de/" target="_blank">UrMEL</a>
            </li>
            <li>
              <a href="/content/below/index.xml" target="_self">Journals@UrMEL</a>
            </li>
          </ul>
          <div id="globalMenu">
            <xsl:call-template name="navigation.row" />
          </div>
        </div>
        <div id="logo"></div>
        <div id="searchBar">
          <form id="searchForm" action="/jp-search.xml">
            <input id="inputField" name="XSL.qt"></input>
            <input id="submitButton" type="submit" value="Suche" />
            <xsl:variable name="journalID">
              <xsl:call-template name="getJournalID" />
            </xsl:variable>
            <xsl:if test="$journalID != ''">
              <input type="hidden" name="XSL.searchjournalID" value="{$journalID}" />
            </xsl:if>
          </form>
        </div>
        <div id="main">
          <xsl:apply-templates />
        </div>
        <xsl:if test="$object='delete'">
          <xsl:copy-of select="$objectEditing/deleteMsg" />
        </xsl:if>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="renderLayout_old">
    <xsl:variable name="nameOfTemplate">
      <xsl:call-template name="nameOfTemplate" />
    </xsl:variable>
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
        <meta content="Zeitschriften,historisch,aktuell,Paper,Forschung,UrMEL,ThULB, FSU Jena,Langzeitarchivierung" lang="de" name="keywords" />
        <meta content="Journals,EJournals,historical,currently,paper,research,UrMEL,ThULB, FSU Jena,long term preservation" lang="en"
          name="keywords" />
        <meta content="MyCoRe" lang="de" name="generator" />
        <link href="{$WebApplicationBaseURL}jp-layout-default.css" rel="stylesheet" type="text/css" />
        <xsl:if test="$nameOfTemplate != ''">
          <link href="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/CSS/{$nameOfTemplate}.css" rel="stylesheet" type="text/css" />
        </xsl:if>
        <link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}templates/content/template_logos/CSS/sponsoredlogos.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}style_userManagement.css" rel="stylesheet" type="text/css" />
        <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js" type="text/javascript" />
        <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js" type="text/javascript" />
        <script type="text/javascript" src="{$MCR.Layout.JS.JQueryURI}" />

        <xsl:variable name="type" select="substring-before(substring-after(/mycoreobject/@ID,'_'),'_')" />
        <xsl:if test="acl:checkPermission('CRUD',concat('update_',$type))">
          <script type="text/javascript" src="{$WebApplicationBaseURL}iview/js/iview2DerivLink.js" />
        </xsl:if>

        <xsl:call-template name="module-broadcasting.getHeader" />

        <!-- add IE CSS to head -->
        <xsl:variable name="cssLinked">
          &lt;link href="
          <xsl:value-of select="concat($WebApplicationBaseURL,'templates/master/',$nameOfTemplate,'/CSS/',$nameOfTemplate,'_IE.css')" />"
          rel="stylesheet" type="text/css"/&gt;
        </xsl:variable>
        <xsl:comment>
          <xsl:value-of select="'[if lte IE 8]&gt;'" />
          <xsl:value-of select="$cssLinked" />
          <xsl:value-of select="'&lt;![endif]'" />
        </xsl:comment>
      </head>
      <body>
        <div id="jp-header" class="jp-layout-header">
          <div id="jp-global-navigation" class="jp-global-navigation-bar">
            <div class="jp-home-link">
              <a href="http://www.urmel-dl.de/" target="_blank">UrMEL</a>
              <xsl:copy-of select="'     |     '" />
              <a href="/content/below/index.xml" target="_self">Journals@UrMEL</a>
            </div>
            <div class="jp-login">
              <xsl:call-template name="navigation.row" />
            </div>
          </div>
        </div>

        <div id="jp-journal-navigation">
          <div class="jp-layout-marginLR">
            <form id="searchForm" action="/jp-search.xml">
              <input id="inputField" name="XSL.qt"></input>
              <input id="submitButton" type="submit" value="Suche" />
              <xsl:variable name="journalID">
                <xsl:call-template name="getJournalID" />
              </xsl:variable>
              <xsl:if test="$journalID != ''">
                <input type="hidden" name="XSL.searchjournalID" value="{$journalID}" />
              </xsl:if>
            </form>
          </div>
        </div>

        <div id="jp-main" class="jp-layout-content-area">
          <xsl:apply-templates />
        </div>

        <div id="jp-footer" class="jp-layout-footer">
        </div>
        <xsl:if test="$object='delete'">
          <xsl:copy-of select="$objectEditing/deleteMsg" />
        </xsl:if>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>