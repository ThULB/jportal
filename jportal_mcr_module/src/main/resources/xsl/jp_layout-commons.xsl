<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mcr="http://www.mycore.org/"
    xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" 
    xmlns:file="java.io.File"
    exclude-result-prefixes="xlink mcr i18n acl xalan file"
    xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities">

    <!-- ================================================================================== -->

    <xsl:template name="jp.layout.getHTMLHeader">
        <xsl:param name="nameOfTemplate" select="$template" />
        <title>
            <xsl:call-template name="HTMLPageTitle" />
        </title>
        <meta content="Zeitschriften-Portal" lang="de" name="description" />
        <meta content="Journal-Portal" lang="en" name="description" />
        <meta content="Zeitschriften,historisch,aktuell,Paper,Forschung,UrMEL,ThULB, FSU Jena,Langzeitarchivierung,Andreas Trappe" lang="de" name="keywords" />
        <meta content="Journals,EJournals,historical,currently,paper,research,UrMEL,ThULB, FSU Jena,long term preservation,Andreas Trappe" lang="en"
            name="keywords" />
        <meta content="MyCoRe" lang="de" name="generator" />
        <link href="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/CSS/style_general.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/CSS/style_navigation.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/CSS/style_content.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}templates/content/template_logos/CSS/sponsoredlogos.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}style_userManagement.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}common.css" rel="stylesheet" type="text/css" />
        <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js" type="text/javascript" />
        <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js" type="text/javascript" />

        <script type="text/javascript" src="http://www.google.com/jsapi"></script>
		<script type="text/javascript">google.load("jquery", "1");</script>
        <xsl:if test="acl:checkPermission(/mycoreobject/@ID,'writedb')">
    	  <script type="text/javascript">
            var baseUrl = "<xsl:value-of select="$WebApplicationBaseURL" />";
            var runid = [];
            jQuery(document).bind("toolbarloaded", function(e) {
              //just run if a given viewType is created
              if (e.model.id != "mainTb" || runid[e.viewer.viewID]) {
                return;
              }
              runid[e.viewer.viewID] = true;
              var toolbarModel=e.model;
              var i=toolbarModel.getElementIndex('spring');
              var buttonSet = new ToolbarButtonsetModel("softLink");
              var button = new ToolbarButtonModel(buttonSet.elementName, {'type': 'buttonDefault'}, {'label': "softLink", 'text': false, 'icons': {primary : 'paperClip-icon'}}, "Verlinken", true, false);
              toolbarModel.addElement(buttonSet,i);
              buttonSet.addButton(button);
              //attach to events of view
              jQuery.each(e.getViews(), function(index, view) {
                jQuery(view).bind("press", function(sender, args) {
                  if (args.parentName == buttonSet.elementName) {
                    if (args.elementName == buttonSet.elementName) {
                      var file = decodeURI(e.viewer.curImage);
                      var chapterParent = e.viewer.chapterParent;
                      var derivId = chapterParent.substring(16, chapterParent.length); 
                      var servletPath = baseUrl + "servlets/DerivateLinkServlet";
                      jQuery.post(servletPath, {mode: "setImage", derivateId: derivId, file: file});
                    }
                  }
                })
              });
            });
    	  </script>
        </xsl:if>
        <xsl:variable name="activeLinkFile" select="file:new(concat($WebApplicationBaseURL,'templates/master/',$nameOfTemplate,'/JS/activelink.js'))" />
        
        <xsl:if test="file:exists($activeLinkFile)">
        	<script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/JS/activelink.js"
        		type="text/javascript" />
        </xsl:if>
        <xsl:call-template name="module-broadcasting.getHeader" />
    </xsl:template>

    <!-- ================================================================================== -->

    <xsl:template name="jp.layout.getHTMLContent">
        <xsl:call-template name="print.writeProtectionMessage" />
        <xsl:choose>
            <xsl:when test="$readAccess='true'">
                <div class="headline">
                    <xsl:copy-of select="$PageTitle" />
                </div>
                <xsl:call-template name="getFastWCMS" />
                <xsl:apply-templates />
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="printNotLoggedIn" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ================================================================================== -->

</xsl:stylesheet>