<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcr="http://www.mycore.org/" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities" exclude-result-prefixes="xsi mcr acl xalan layoutUtils">

  <xsl:include href="jp-layout-tools.xsl" />
  <xsl:include href="jp-layout-nameOfTemplate.xsl" />
  <xsl:include href="jp-layout-contentArea.xsl" />
  <xsl:include href="jp-layout-contentArea-objectEditing.xsl" />
  <xsl:include href="jp-layout-mcrwebpage.xsl" />
  <xsl:include href="jp-layout-contentArea-searchResults.xsl" />
  <xsl:include href="jp-navigation-top.xsl" />

  <xsl:param name="object" />
  <xsl:param name="layout" />
  <xsl:param name="MCR.Piwik.baseurl" />
  <xsl:param name="MCR.Piwik.enable" />

  <!-- For Subselect -->
  <xsl:param name="subselect.type" select="''" />
  <xsl:param name="subselect.session" select="''" />
  <xsl:param name="subselect.varpath" select="''" />
  <xsl:param name="subselect.webpage" select="''" />

  <!-- Search modes -->
  <xsl:param name="mode" select="''" />

  <xsl:variable name="objSettingXML">
    <title allowHTML="true" />
  </xsl:variable>
  <xsl:variable name="objSetting" select="xalan:nodeset($objSettingXML)" />

  <xsl:variable name="nameOfTemplate">
    <xsl:call-template name="nameOfTemplate" />
  </xsl:variable>

  <xsl:variable name="showSearchBar" select="not(contains('advanced.form', $mode))" />

  <xsl:template name="renderLayout">
    <xsl:if test="/mycoreobject/@ID">
      <xsl:variable name="setObjIDInSession" select="layoutUtils:setLastValidPageID(/mycoreobject/@ID)" />
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
        <meta content="Zeitschriften,historisch,aktuell,Paper,Forschung,UrMEL,ThULB, FSU Jena,Langzeitarchivierung" lang="de" name="keywords" />
        <meta content="Journals,EJournals,historical,currently,paper,research,UrMEL,ThULB, FSU Jena,long term preservation" lang="en"
          name="keywords" />
        <meta content="MyCoRe" lang="de" name="generator" />
        <link href="{$WebApplicationBaseURL}jp-layout-default.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}jp-layout-editor.css" rel="stylesheet" type="text/css" />
        <xsl:if test="$nameOfTemplate != ''">
          <link href="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/CSS/{$nameOfTemplate}.css" rel="stylesheet" type="text/css" />
        </xsl:if>
        <link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}templates/content/template_logos/CSS/sponsoredlogos.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}style_userManagement.css" rel="stylesheet" type="text/css" />
        <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js" type="text/javascript" />
        <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js" type="text/javascript" />
        <script type="text/javascript" src="{$MCR.Layout.JS.JQueryURI}" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}ckeditor/ckeditor.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}ckeditor/adapters/jquery.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-layout-controller.js" />

        <!-- Piwik -->
        <script type="text/javascript">
          if('<xsl:value-of select="$MCR.Piwik.enable" />' == 'true'){
          var pkBaseURL = '<xsl:value-of select="$MCR.Piwik.baseurl" />';
          document.write(unescape("%3Cscript src='" + pkBaseURL + "piwik.js' type='text/javascript'%3E%3C/script%3E"));
          }
        </script>
        <script type="text/javascript">
          if('<xsl:value-of select="$MCR.Piwik.enable" />'== 'true'){
          var myvar = '<xsl:value-of select="/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID" />';
          try {
          var piwikTracker = Piwik.getTracker(pkBaseURL + "piwik.php", 1);
          if(myvar != ""){
            piwikTracker.setCustomVariable (1, "journal", myvar, scope = "page");
          }
          piwikTracker.trackPageView();
          piwikTracker.enableLinkTracking();
          } catch( err ) {}
          }
        </script>
        <!-- End Piwik Tracking Code -->

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
            <xsl:call-template name="jp.navigation.top" />
          </div>
        </div>
        <div id="logo"></div>

        <xsl:if test="$showSearchBar">
          <div id="searchBar">
            <form id="searchForm" action="/jp-search.xml">
              <xsl:variable name="queryterm">
                <xsl:if test="$qt != '*'">
                  <xsl:value-of select="$qt" />
                </xsl:if>
              </xsl:variable>
              <input id="inputField" name="XSL.qt" value="{$queryterm}"></input>
              <input id="submitButton" type="submit" value="Suche" />
              <xsl:if test="$subselect.type != ''">
                <input type="hidden" name="XSL.subselect.type" value="{$subselect.type}" />
                <input type="hidden" name="XSL.subselect.session.SESSION" value="{$subselect.session}" />
                <input type="hidden" name="XSL.subselect.varpath.SESSION" value="{$subselect.varpath}" />
                <input type="hidden" name="XSL.subselect.webpage.SESSION" value="{$subselect.webpage}" />
              </xsl:if>
              <xsl:variable name="journalID">
                <xsl:call-template name="getJournalID" />
              </xsl:variable>
              <xsl:if test="$journalID != ''">
                <input type="hidden" name="XSL.searchjournalID" value="{$journalID}" />
              </xsl:if>
            </form>
          </div>
        </xsl:if>
        <div id="main">
          <xsl:apply-templates />
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
        <!-- TODO: don't init iview2 if no image is available -->
        <xsl:call-template name="initIview2JS" />
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>