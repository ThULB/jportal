<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 529 $ $Date: 2008-07-02 16:01:39 +0200 (Wed, 02 Jul 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_jer">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>
                <table cellspacing="0" cellpadding="0" id="footer">
                    <tr>
                        <td style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jer_top_left.jpg) no-repeat;">
                            <table cellspacing="0" cellpadding="0" width="400px">
                                <tr>
                                    <td>
                                        <div id="max-planck-logo">
                                            <a href="http://www.econ.mpg.de" target="_blank">
                                                <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/max-planck-logo.jpg" border="0" />
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div id="login_div">
                                            <div id="login_div_inside">
                                                <xsl:call-template name="template_jer.userInfo" />
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td width="600px"
                            style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jer_top_middle.jpg); repeat x;">
                        </td>
                        <td style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jer_top_right.jpg) no-repeat;">
                            <table cellspacing="0" cellpadding="0" width="570px">
                                <tr>
                                    <td>
                                        <div id="fsu-logo">
                                            <a href="http://www.uni-jena.de" target="_blank">
                                                <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/fsu-logo.jpg" border="0" />
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div id="thulb_logo">
                                            <a href="http://www.thulb.uni-jena.de" target="_blank">
                                                <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb_logo.gif" border="0" />
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <table border="0" cellspacing="0" cellpadding="0" id="second-line">
                    <tr>
                        <td style="width: 297px; vertical-align: top; text-align: left;">
                            <div id="div_navi_main"
                                style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jer_left.jpg) no-repeat;">
                                <br />
                                <xsl:call-template name="Navigation_main" />
                            </div>
                        </td>
                        <td style="vertical-align: top; text-align: left; height: 100%;">
                            <table border="0" cellspacing="0" cellpadding="0" style="width: 100%; height: 20px; text-align: right;">
                                <tr>
                                    <td style="width: 800px;">
                                        <br />
                                    </td>
                                    <td style="width: 500px; text-align: right; padding-right: 20px;">
                                        <table id="navi_box">
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
                            </table>
                            <div id="navi_history">
                                <div id="navi_history_inside">
                                    <xsl:call-template name="navigation.history" />
                                </div>
                            </div>
                            <div id="contentArea">
                                <div id="contentWrapper">
                                    
                                    <xsl:call-template name="template_jer.write.content" />
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_jer.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_jer.userInfo">

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
                    <xsl:value-of select="i18n:translate('editor.start.LoginText.label')" />
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