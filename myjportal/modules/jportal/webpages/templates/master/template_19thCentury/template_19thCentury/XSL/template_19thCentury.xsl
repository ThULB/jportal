<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_19thCentury">
        <xsl:param name="journalsMaintitle" select="'Zeitschriftenname'" />
        <xsl:param name="periodetitle" select="'Erscheinungszeitraum'" />
        <xsl:param name="journalID" />
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader">
                    <xsl:with-param name="nameOfTemplate" select="'template_19thCentury'" />
                </xsl:call-template>
            </head>
            <body>
                <div id="time">
                    <xsl:copy-of select="$periodetitle" />
                </div>
                <div id="journal-title">
                    <xsl:copy-of select="$journalsMaintitle" />
                </div>
                <div id="transparent-logo" />
                <table width="100%" height="30px" border="0" cellspacing="0" cellpadding="0" style="background-color: #646466;">
                    <tr valign="top">
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
                <div style="min-height: 800px; width: max; margin-left: 10px; margin-right: 10px; margin-bottom: 10px; background-color:#e6dece;">
                    <table width="100%" height="162" border="0" cellspacing="0" cellpadding="0" style="background-color:transparent;">
                        <tr valign="top">
                            <td rowspan="2" width="1235"
                                style="background:url({$WebApplicationBaseURL}templates/master/template_19thCentury/IMAGES/19th-header-left.jpg) no-repeat;">
                                <div id="navigation_box">
                                    <xsl:call-template name="navigation.row">
                                        <xsl:with-param name="rootNode" select="'navi-below'" />
                                        <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                                        <xsl:with-param name="menuPointHeigth" select="'21'" />
                                        <xsl:with-param name="spaceBetweenLinks" select="'12'" />
                                    </xsl:call-template>
                                </div>
                            </td>
                            <td colspan="2" width="max" height="127"
                                style="background:url({$WebApplicationBaseURL}templates/master/template_19thCentury/IMAGES/19th-header-right.jpg) no-repeat;">
                                <p>
                                    <br />
                                </p>
                            </td>
                        </tr>
                        <tr>
                            <td width="max" height="35"
                                style="background:url({$WebApplicationBaseURL}templates/master/template_19thCentury/IMAGES/19th-header-right2.jpg) no-repeat;">
                                <p>
                                    <br />
                                </p>
                            </td>
                            <td width="11" height="35"
                                style="background:url({$WebApplicationBaseURL}templates/master/template_19thCentury/IMAGES/19th-header-right3.jpg) no-repeat;">
                                <p>
                                    <br />
                                </p>
                            </td>
                        </tr>
                    </table>
                    <table width="100%" height="max" border="0" cellspacing="0" cellpadding="0" style="padding-right: 10px; padding-bottom: 10px;"
                        valign="top">
                        <tr valign="top">
                            <td width="200px" valign="top" rowspan="2">
                                <div id="div_navi_main">
                                    <xsl:call-template name="Navigation_main" />
                                </div>
                                <!-- are special logos for current used journal available ? -> if yes, get them -->
                                <xsl:call-template name="template_logos.getLogos">
                                    <xsl:with-param name="journalsID" select="$journalID" />
                                </xsl:call-template>
                            </td>
                            <td width="max" valign="top" style="height: 16px; padding-right: 5px;">
                                <xsl:text> </xsl:text>
                            </td>
                            <td width="5" rowspan="2" valign="top" style="height: 500px;">
                                <xsl:text> </xsl:text>
                            </td>
                        </tr>
                        <tr valign="top">
                            <td width="max" valign="top" style="background-color:#d4c9b0; height: 500px;">
                                <div id="navi_history">
                                    <xsl:call-template name="navigation.history" />
                                </div>
                                <div id="contentArea">
                                    <div id="contentWrapper">

                                        <xsl:call-template name="template_19thCentury.write.content" />
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_19thCentury.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_19thCentury.userInfo">

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
                        <img src="{$WebApplicationBaseURL}templates/master/template_19thCentury/IMAGES/login-switch.gif" border="0" />
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