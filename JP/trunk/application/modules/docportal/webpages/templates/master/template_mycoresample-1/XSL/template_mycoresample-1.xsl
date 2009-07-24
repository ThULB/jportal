<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink">

    <!-- any XML elements defined here will go into the head -->
    <!-- other stylesheets may override this variable -->
    <xsl:variable name="head.additional" />

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_mycoresample-1">
        <html>
            <head>
                <!--<meta http-equiv="content-type" content="text/html;charset=UTF-8"/>-->
                <title>
                    <xsl:call-template name="PageTitle" />
                </title>
                <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_general.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_navigation.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_content.css" rel="stylesheet" type="text/css" />
                <link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet" type="text/css" />
                <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js" type="text/javascript" />
                <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js" type="text/javascript" />
                <xsl:copy-of select="$head.additional" />
                <xsl:call-template name="module-broadcasting.getHeader" />
            </head>

            <body>
                <table id="maintable" cellspacing="0" cellpadding="0">
                    <tr class="max">
                        <!-- general column left -->
                        <td id="mainLeftColumn">
                            <a>
                                <xsl:attribute name="href">
                                <xsl:call-template name="UrlAddSession">
                                    <xsl:with-param name="url" select="concat($WebApplicationBaseURL,'content/below/index.xml')" />
                                </xsl:call-template>
                            </xsl:attribute>
                                <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/logo.gif" alt="Logo" id="logo" />
                            </a>
                            <div class="navi_main">
                                <xsl:call-template name="Navigation_main" />
                            </div>
                        </td>
                        <!-- END OF: general column left -->

                        <!-- BEGIN: general column right -->
                        <td class="max autowidth">
                            <table class="max" cellspacing="0" cellpadding="0">

                                <!-- BEGIN: menu above -->
                                <tr class="minheight">
                                    <td id="navi_below_cell">
                                        <xsl:call-template name="navigation.row">
                                            <xsl:with-param name="rootNode" select="'navi-below'" />
                                            <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                                            <xsl:with-param name="menuPointHeigth" select="'21'" /><!-- use pixel values -->
                                            <xsl:with-param name="spaceBetweenLinks" select="'12'" /><!-- use pixel values -->
                                            <xsl:with-param name="seperatorChar" select="'|'" />
                                        </xsl:call-template>
                                    </td>
                                </tr>
                                <!-- END OF: menu above -->

                                <!-- BEGIN: history navigation area -->
                                <tr class="minheight">
                                    <td>
                                        <table class="navi_history">
                                            <tr>
                                                <td class="navi_history">
                                                    <xsl:call-template name="navigation.history" />
                                                </td>
                                                <td class="navi_history_user">
                                                    <xsl:call-template name="userInfo" />
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <!-- END OF: history navigation area -->

                                <!-- BEGIN content area -->
                                <tr>
                                    <td id="contentArea">
                                        <!-- IE Fix: contentWrapper needed :o( -->
                                        <div id="contentWrapper">
                                            <xsl:call-template name="template_mycoresample-1.write.content" />
                                        </div>
                                    </td>
                                </tr>
                                <!-- END OF: content area -->

                                <!-- footer right -->
                                <tr class="minheight">
                                    <td id="footer">
                                        <xsl:call-template name="footer" />
                                    </td>
                                </tr>
                                <!-- END OF: footer right -->

                            </table>
                        </td>

                        <!-- END OF: general column right -->
                    </tr>
                </table>
            </body>
        </html>
    </xsl:template>
    <!-- ======================================================================================================== -->
    <xsl:template name="template_mycoresample-1.write.content">
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
    <!-- ======================================================================================================== -->
</xsl:stylesheet>