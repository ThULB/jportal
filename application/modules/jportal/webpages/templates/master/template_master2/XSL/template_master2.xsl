<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_master2">
        <xsl:param name="journalID" />
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>
                <div id="navi_history">
                    <xsl:call-template name="navigation.history" />
                </div>
                <div id="navigation_box">
                    <xsl:call-template name="navigation.row">
                        <xsl:with-param name="rootNode" select="'navi-below'" />
                        <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                        <xsl:with-param name="menuPointHeigth" select="'21'" />
                        <xsl:with-param name="spaceBetweenLinks" select="'12'" />
                    </xsl:call-template>
                </div>
                <a id="transparent-logo" href="{$WebApplicationBaseURL}">
                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/master_new-logo.gif" />
                </a>
                <a id="whatsnew-logo" href="{$WebApplicationBaseURL}content/main/whatsnew.xml">
                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/master_new-button.png" />
                </a>
                <div id="topleft-logo" />
                <div id="topright-logo" />
                <table width="100%" height="30px" border="0" cellspacing="0" cellpadding="0" style="background-color: #646466;">
                    <tr valign="top">
                        <td width="100%">
                            <div id="whitespace"></div>
                            <span id="headline">
                                <a href="http://www.urmel-dl.de/" target="_blank">UrMEL</a>
                                <xsl:copy-of select="'     |     '" />
                                <a href="http://zs.thulb.uni-jena.de/content/below/index.xml" target="_self">Journals@UrMEL</a>
                            </span>
                        </td>
                    </tr>
                </table>
                <table width="100%" height="57" border="0" cellspacing="0" cellpadding="0" style="background-color:transparent;">
                    <tr valign="top">
                        <td rowspan="2" width="1200"
                            style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/master_new-header-left.jpg) no-repeat;">

                        </td>
                        <td colspan="2" width="max" height="57"
                            style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/master_new-header-right.jpg);">
                            <p>
                                <br />
                            </p>
                        </td>
                    </tr>
                </table>
                <table width="100%" height="max" border="0" cellspacing="0" cellpadding="0" style="padding-right: 10px; padding-bottom: 10px;" valign="top">
                    <tr valign="top">
                        <td width="200px" height="100%">
                            <table height="100%" cellspacing="0" cellpadding="0">
                                <xsl:if test="$browserAddress != '/content/main/journalList.xml' and $browserAddress != '/content/main/journals.xml'">
                                    <tr valign="top">
                                        <td width="200px" valign="top"
                                            style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/master_new-navi-top.png) no-repeat;">
                                            <div id="div_navi_main">
                                                <xsl:call-template name="Navigation_main" />
                                            </div>
                                        </td>
                                    </tr>
                                </xsl:if>
                                <tr valign="top" style="height: max;">
                                    <td width="200px" valign="top"
                                        style="height: 100%; background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/master_new-navi-below.jpg) no-repeat;">
                                        <!-- are special logos for current used journal available ? -> if yes, get them -->
                                        <xsl:call-template name="template_logos.getLogos">
                                            <xsl:with-param name="journalsID" select="$journalID" />
                                        </xsl:call-template>
                                        <br />
                                    </td>
                                </tr>
                                <tr>
                                    <td
                                        style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/master_new-navi-filler.jpg); background-repeat: repeat-y; height: 500px;">
                                        <br />
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td width="max" valign="top">
                            <div id="content-whitespace">
                                <div id="contentArea">
                                    <div id="contentWrapper">
                                        <xsl:call-template name="template_master2.write.content" />
                                    </div>
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_master2.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_master2.userInfo">

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
                        <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/login-switch.gif" border="0" />
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