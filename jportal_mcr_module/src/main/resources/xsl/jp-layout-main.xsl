<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcr="http://www.mycore.org/" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities" xmlns:websiteWriteProtection="xalan://org.mycore.frontend.MCRWebsiteWriteProtection"
  exclude-result-prefixes="xsi mcr acl xalan layoutUtils websiteWriteProtection">

  <xsl:include href="jp-layout-tools.xsl" />
  <xsl:include href="jp-layout-nameOfTemplate.xsl" />
  <xsl:include href="jp-layout-contentArea.xsl" />
  <xsl:include href="jp-layout-contentArea-objectEditing.xsl" />
  <xsl:include href="jp-layout-mcrwebpage.xsl" />
  <xsl:include href="jp-layout-contentArea-searchResults.xsl" />
  <xsl:include href="jp-layout-footer.xsl" />
  <xsl:include href="jp-navigation-top.xsl" />
  <xsl:include href="jp-globalmessage.xsl" />

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

  <xsl:variable name="showSearchBar" select="not(contains('advanced.form laws.form', $mode))" />

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
        <script type="text/javascript" src="{$MCR.Layout.JS.JQueryURI}" />
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/{$jqueryUI.version}/jquery-ui.min.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}ckeditor/ckeditor.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}ckeditor/adapters/jquery.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-layout-controller.js" />
        <!-- TODO: don't init iview2 if no image is available -->
        <xsl:call-template name="initIview2JS" />

        <!-- Piwik -->
        <xsl:if test="$MCR.Piwik.enable = 'true' and $MCR.Piwik.baseurl != ''">
          <script type="text/javascript" src="{$MCR.Piwik.baseurl}piwik.js" />
          <script type="text/javascript">
            var pkBaseURL = '<xsl:value-of select="$MCR.Piwik.baseurl" />';
            var journalID = '<xsl:value-of select="/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID" />';
            try {
              var piwikTracker = Piwik.getTracker(pkBaseURL + "piwik.php", 1);
              if(journalID != "") {
                piwikTracker.setCustomVariable (1, "journal", journalID, scope = "page");
              }
              piwikTracker.trackPageView();
              piwikTracker.enableLinkTracking();
            } catch( err ) {
              console.log(err);
            }
          </script>
        </xsl:if>
        <!-- End Piwik Tracking Code -->

        <xsl:variable name="type" select="substring-before(substring-after(/mycoreobject/@ID,'_'),'_')" />
        <xsl:if test="acl:checkPermission('CRUD',concat('update_',$type))">
          <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-iview2-derivLink.js" />
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
                  <xsl:if test="$qt != '*'">
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
        <xsl:call-template name="jp-layout-footer" />
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
</xsl:stylesheet>