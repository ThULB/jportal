<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="i18n xsl xlink">

    <!-- any XML elements defined here will go into the head -->
    <!-- other stylesheets may override this variable -->
    <xsl:variable name="head.additional" />

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_isis">
        <html>
            <head>
                <!--<meta http-equiv="content-type" content="text/html;charset=UTF-8"/>-->
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
                <xsl:copy-of select="$head.additional" />
                <xsl:call-template name="module-broadcasting.getHeader" />
            </head>

            <body>
                <div id="topmenu">

                    <p id="navi_below_cell">

                        <xsl:call-template name="navigation.row">
                            <xsl:with-param name="rootNode" select="'navi-below'" />
                            <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                            <xsl:with-param name="menuPointHeigth" select="'21'" /><!-- use pixel values -->
                            <xsl:with-param name="spaceBetweenLinks" select="'12'" /><!-- use pixel values -->
                        </xsl:call-template>
                    </p>

                </div>

                <div id="logotop">
                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/logo.jpg" alt="Allgemeines - ISIS von Oken"></img>
                </div>

                <table id="menuContentWrapper">
                    <tr>
                        <td id="menu-width">
                            <div id="leftmenu">
                                <img id="blueimg" src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/muh.jpg" alt="Kasten"></img>
                                <div id="navmain">
                                    <xsl:call-template name="Navigation_main" />
                                </div>
                                <a href="http://www.dfg.de" target="_blank" id="logos">
                                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/dfg.png" alt="Logo DFG"></img>
                                </a>
                                <div id="sponsor-line" />
                                <a href="http://www.thulb.uni-jena.de" target="_blank" id="logos">
                                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb.png" alt="Logo ThulB"></img>
                                </a>
                                <div id="sponsor-line" />
                                <a href="http://www2.uni-jena.de/biologie/ehh/haeckel.htm" target="_blank" id="logos">
                                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/ehh.png" alt="Logo Ernst Haeckel Haus"></img>
                                </a>
                            </div>
                        </td>
                        <td id="contentArea">
                            <div id="navi_history">
                                <div id="navi_history_inside">
                                    <xsl:call-template name="navigation.history" />
                                </div>
                            </div>
                            <xsl:call-template name="template_isis.write.content" />
                        </td>
                    </tr>
                </table>

            </body>
        </html>
    </xsl:template>
    <!-- ======================================================================================================== -->
    <xsl:template name="template_isis.write.content">
        <div class="headline">
            <xsl:copy-of select="$PageTitle" />
        </div>

        <xsl:call-template name="getFastWCMS" />

        <xsl:apply-templates />
    </xsl:template>
    <!-- ======================================================================================================== -->
    <xsl:template name="template_isis.userInfo">

        <!-- BEGIN: login values -->
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" />
        <!-- END OF: login values -->

        <xsl:value-of select="concat(i18n:translate('users.user'),': ')" />
        <a href="{$LoginURL}">
            <xsl:value-of select="$CurrentUser" />
        </a>

    </xsl:template>

</xsl:stylesheet>