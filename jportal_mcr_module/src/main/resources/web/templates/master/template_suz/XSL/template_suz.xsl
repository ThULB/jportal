<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 529 $ $Date: 2008-07-02 16:01:39 +0200 (Mi, 02 Jul 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_suz">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>
                <div id="border">
                    <div id="footer1">
                        <!-- footer 1 fertig -->
                        <div id="login_div" style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/bg_login.gif) no-repeat;">
                            <xsl:call-template name="template_suz.userInfo" />
                        </div>
                        <div id="navigation">
                            <div id="navigation_box">
                                <xsl:call-template name="navigation.row" />
                            </div>
                            <div id="thulb_logo">
                                <a href="http://www.thulb.uni-jena.de">
                                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb_logo.gif" border="0"></img>
                                </a>
                            </div>
                        </div>

                    </div>

                    <div id="banner" style="background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/suz_logo.gif) no-repeat;">
                        <br></br>
                    </div>

                    <table id="footer2" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td id="footer2_left">
                                <div id="div_navi_main"
                                    style="background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/suz_bg_nav2.gif); background-repeat: repeat-y;">
                                    <xsl:call-template name="Navigation_main" />
                                </div>
                                <div id="footer_bottom"
                                    style="background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/suz_bg_nav3.gif); background-repeat: repeat-x;">
                                </div>
                            </td>
                            <td valign="top" id="footer2_right">
                                <div id="navi_history">
                                    <xsl:call-template name="navigation.history" />
                                </div>
                                <br />
                                <div id="contentArea">
                                    <div id="contentWrapper">
                                        
                                        <xsl:call-template name="template_suz.write.content" />
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
    <xsl:template name="template_suz.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_suz.userInfo">

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