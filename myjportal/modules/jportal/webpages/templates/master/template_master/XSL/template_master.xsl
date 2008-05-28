<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink">
    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_master">
        <html>
            <head>
                <title>
                    <xsl:call-template name="HTMLPageTitle" />
                </title>
                <meta content="Zeitschriften-Portal" lang="de" name="description" />
                <meta content="Journal-Portal" lang="en" name="description" />
                <meta content="Zeitschriften,historisch,aktuell,Paper,Forschung,UrMEL,ThULB, FSU Jena,Langzeitarchivierung,Andreas Trappe" lang="de"
                    name="keywords" />
                <meta content="Journals,EJournals,historical,currently,paper,research,UrMEL,ThULB, FSU Jena,long term preservation,Andreas Trappe" lang="en"
                    name="keywords" />
                <meta content="MyCoRe" lang="de" name="generator" />
                <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_general.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_navigation.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_content.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}/common.css" rel="stylesheet" type="text/css" />
                <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js" type="text/javascript" />
                <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js" type="text/javascript" />
                <xsl:call-template name="module-broadcasting.getHeader" />
            </head>

            <body>
                <div class="border">
                    <div class="logoBar">

                        <!-- Navigation right top corner -->
                        <div class="navigationBelow">

                            <div class="top">
                                <xsl:call-template name="navigation.row">
                                    <xsl:with-param name="rootNode" select="'navi-below'" />
                                    <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                                    <xsl:with-param name="menuPointHeigth" select="'21'" />
                                    <!-- use pixel values -->
                                    <xsl:with-param name="spaceBetweenLinks" select="'12'" />
                                    <!-- use pixel values -->
                                    <xsl:with-param name="seperatorChar" select="'|'" />
                                </xsl:call-template>
                            </div>
                            <div class="history">
                                <xsl:call-template name="navigation.history" />
                            </div>

                        </div>
                        <!-- navigationBelow / Navigation right top corner -->

                    </div>
                    <!-- logoBar -->

                    <div class="mainSite_MenuArea">

                        <div class="mainMenu">
                            <xsl:call-template name="Navigation_main" />
                        </div>

                    </div>
                    <!-- mainSite_MenuArea -->

                    <div class="mainSite_ContentArea">
                        <xsl:call-template name="getFastWCMS" />
                        <div id="contentArea">

                            <xsl:call-template name="template_master.write.content" />
                        </div>
                    </div>
                    <!-- mainSite_ContentArea -->

                    <div style="clear: both;">
                        <br />
                    </div><!-- a small buffer beetween logo and content -->

                </div><!-- border -->

            </body>
        </html>
    </xsl:template>


    <!-- ======================================================================================================== -->
    <xsl:template name="template_master.write.content">
        <div class="headline">
            <xsl:copy-of select="$PageTitle" />
        </div>
        <xsl:apply-templates />
    </xsl:template>


    <!-- ======================================================================================================== -->
    <xsl:template name="template_master.userInfo"><!-- BEGIN: login values -->
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" />
        <!-- END OF: login values -->
        <text i18n="editor.start.LoginText.label" />
        :
        <a href="{$LoginURL}">
            <xsl:value-of select="$CurrentUser" />
        </a>
    </xsl:template>
</xsl:stylesheet>