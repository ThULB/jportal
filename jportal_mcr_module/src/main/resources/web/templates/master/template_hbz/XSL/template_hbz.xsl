<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 529 $ $Date: 2008-07-02 16:01:39 +0200 (Mi, 02 Jul 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_hbz">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>
                <table cellspacing="0" cellpadding="0" border="0" id="footer">
                    <tr>
                        <td id="banner-left"
                            style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_hbz_top_left.gif) no-repeat;">
                            <table cellspacing="0" cellpadding="0" border="0" style="width: 100%; text-align: right;">
                                <tr>
                                    <td id="navi_box">
                                        <table cellspacing="0" cellpadding="0" border="0" align="right">
                                            <tr>
                                                <td>
                                                    <xsl:call-template name="navigation.row">
                                                        <xsl:with-param name="rootNode" select="'navi-below'" />
                                                        <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                                                        <xsl:with-param name="menuPointHeigth" select="'21'" />
                                                        <!-- use pixel values -->
                                                        <xsl:with-param name="spaceBetweenLinks" select="'12'" />
                                                        <!-- use pixel values -->
                                                    </xsl:call-template>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td id="navi_history">
                                        <div id="navi_history_inside">
                                            <xsl:call-template name="navigation.history" />
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td id="banner-right"
                            style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_hbz_top_right.gif) no-repeat;">
                            <div id="thulb_logo">
                                <a href="http://www.thulb.uni-jena.de" target="_blank">
                                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb_logo.gif" border="0" />
                                </a>
                            </div>
                        </td>
                    </tr>
                </table>
                <table id="second-line" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td valign="top" halign="left" id="footer2-left">
                            <table cellspacing="0" cellpadding="0" border="0" style="height:90%;">
                                <tr>
                                    <td id="div_navi_main_top"
                                        style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_hbz_navi_top.gif) no-repeat;">
                                        <div id="login_div">
                                            <div id="login_div_inside">
                                                <xsl:call-template name="template_hbz.userInfo" />
                                            </div>
                                        </div>
                                        <br />
                                        <xsl:call-template name="Navigation_main" />
                                    </td>
                                </tr>
                                <tr>
                                    <td id="div_navi_main_middlea"
                                        style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_hbz_navi_middle1.gif) no-repeat;">
                                        <br />
                                    </td>
                                </tr>
                                <tr>
                                    <td id="div_navi_main_middleb">
                                        <a href="http://zs.thulb.uni-jena.de/content/main/journals/suhler.xml" target="_self">
                                            <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_hbz_navi_middle2.gif" border="0" />
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td id="div_navi_main_bottom"
                                        style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_hbz_navi_bottom.gif); background-repeat: repeat-y;">
                                        <br />
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td valign="top" halign="left" id="footer2-right">
                            <div id="contentArea">
                                
                                <xsl:call-template name="template_hbz.write.content" />
                            </div>
                        </td>
                    </tr>
                </table>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_hbz.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_hbz.userInfo">

        <!-- BEGIN: login values -->
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" />
        <!-- END OF: login values -->
        <table class="login_window" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td class="login_text">
                    <xsl:value-of select="i18n:translate('editor.start.LoginText.label')" />
                    :
                </td>
                <td class="user_id">
                    <p class="whitebox">
                        <xsl:value-of select="$CurrentUser" />
                    </p>
                </td>
                <td id="login_window">
                    <!-- Login-Button / 2 Pfeile =================================== -->
                    <a href="{$LoginURL}">
                        <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/login-switch.gif" border="0" />
                    </a>
                </td>
            </tr>
        </table>

    </xsl:template>

</xsl:stylesheet>