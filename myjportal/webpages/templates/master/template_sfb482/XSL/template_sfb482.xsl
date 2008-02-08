<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_sfb482">
        <html>
            <head>
                <meta http-equiv="content-type" content="text/html;charset=UTF-8" />
                <title></title>
                <meta content="Zeitschriften-Portal" lang="de" name="description" />
                <meta content="Journal-Portal" lang="en" name="description" />
                <meta content="Zeitschriften,historisch,aktuell,Paper,Forschung,UrMEL,ThULB, FSU Jena,Langzeitarchivierung,Andreas Trappe" lang="de"
                    name="keywords" />
                <meta content="Journals,EJournals,historical,currently,paper,research,UrMEL,ThULB, FSU Jena,long term preservation,Andreas Trappe" lang="en"
                    name="keywords" />
                <meta content="MyCoRe" lang="de" name="generator" />
                <link href="{$WebApplicationBaseURL}templates/master/template_sfb482/CSS/style_general.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}templates/master/template_sfb482/CSS/style_content.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}templates/master/template_sfb482/CSS/style_navigation.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}/common.css" rel="stylesheet" type="text/css" />
                <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js" type="text/javascript" />
                <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js" type="text/javascript" />
                <xsl:call-template name="module-broadcasting.getHeader"/>
            </head>
            <body>
                <div
                    style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/fsu_logo.png) no-repeat; position:absolute; top:60px; right:22px; width: 62px; height: 71px;" />
                <div id="navigation_box">
                    <xsl:call-template name="navigation.row">
                        <xsl:with-param name="rootNode" select="document($navigationBase) /navigation/navi-below" />
                        <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                        <xsl:with-param name="menuPointHeigth" select="'21'" />
                        <xsl:with-param name="spaceBetweenLinks" select="'12'" />
                    </xsl:call-template>
                </div>
                <table width="100%" height="30px" border="0" cellspacing="0" cellpadding="0" style="background-color: #646466;">
                    <tr valign="TOP">
                        <td width="100%">
                            <div id="whitespace"></div>
                            <span id="headline">
                                <a href="http://www.urmel-dl.de/urmel/content/main/content.xml" target="_blank">UrMEL</a>
                                <xsl:copy-of select="'     |     '" />
                                <a href="http://zs.thulb.uni-jena.de/content/below/index.xml" target="_self">Journals@UrMEL</a>
                            </span>
                        </td>
                    </tr>
                </table>
                <table width="100%" height="139" border="0" cellspacing="0" cellpadding="0">
                    <tr valign="TOP">
                        <xsl:choose>
                            <xsl:when test="$CurrentLang = 'de'">
                                <td height="114"
                                    style="background:url({$WebApplicationBaseURL}templates/master/template_sfb482/IMAGES/sfb482-header.png) no-repeat;">
                                    <br />
                                </td>
                            </xsl:when>
                            <xsl:when test="$CurrentLang = 'en'">
                                <td height="114"
                                    style="background:url({$WebApplicationBaseURL}templates/master/template_sfb482/IMAGES/sfb482-header-en.png) no-repeat;">
                                    <br />
                                </td>
                            </xsl:when>
                        </xsl:choose>

                    </tr>
                    <tr>
                        <td style="background-color: #d8d9da; border-bottom: 4px solid #84aed2;">
                            <div id="navi_history">
                                <xsl:call-template name="navigation.history" />
                            </div>
                        </td>
                    </tr>
                </table>
                <table width="100%" height="max" border="0" cellspacing="0" cellpadding="0" style="padding-right: 10px; padding-bottom: 10px;">
                    <tr>
                        <td width="200px" valign="top" rowspan="2">
                            <div id="div_navi_main">
                                <xsl:call-template name="Navigation_main" />
                            </div>
                            <xsl:choose>
                                <xsl:when test="$CurrentLang = 'de'">
                                    <div style="padding-left: 20px;">gefördert durch:</div>
                                </xsl:when>
                                <xsl:when test="$CurrentLang = 'en'">
                                    <div style="padding-left: 20px;">supported by:</div>
                                </xsl:when>
                            </xsl:choose>

                            <div id="thulb_logo">
                                <a href="http://www.thulb.uni-jena.de" target="_blank">
                                    <img src="{$WebApplicationBaseURL}templates/master/template_sfb482/IMAGES/thulb_logo.png" border="0"></img>
                                </a>
                            </div>
                            <div id="dfg_logo">
                                <a href="http://www.dfg.de" target="_blank">
                                    <img src="{$WebApplicationBaseURL}templates/master/template_sfb482/IMAGES/dfg_logo.png" border="0"></img>
                                </a>
                            </div>
                        </td>
                        <td width="max" valign="top" style="height: 16px; padding-right: 5px;">
                            <xsl:text> </xsl:text>
                        </td>
                        <td width="5" rowspan="2" style="height: 500px;">
                            <xsl:text> </xsl:text>
                        </td>
                    </tr>
                    <tr>
                        <td width="max" valign="top">
                            <div id="contentArea">
                                <div id="contentWrapper">
                                    <xsl:call-template name="getFastWCMS" />
                                    <xsl:call-template name="template_sfb482.write.content" />
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_sfb482.write.content">
        <div class="headline">
            <xsl:copy-of select="$PageTitle" />
        </div>
        <xsl:apply-templates />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_sfb482.userInfo">

        <!-- BEGIN: login values -->
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" />
        <!-- END OF: login values -->
        <table class="login_window" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td class="login_space"></td>
                <td class="login_window">
                    <!-- Login-Button / 2 Pfeile =================================== -->
                    <a href="{$LoginURL}">
                        <img src="{$WebApplicationBaseURL}templates/master/template_sfb482/IMAGES/login-switch.gif" border="0" />
                    </a>
                </td>
                <td class="login_text">
                    <text i18n="editor.start.LoginText.label" />
                    :
                </td>
                <td class="user_id">
                    <p class="whitebox">
                        <xsl:value-of select="$CurrentUser" />
                    </p>
                </td>
            </tr>
        </table>

    </xsl:template>

</xsl:stylesheet>