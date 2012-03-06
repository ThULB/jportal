<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 529 $ $Date: 2008-07-02 16:01:39 +0200 (Mi, 02 Jul 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_akruetzel">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body
                style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/bg_body_akruetzel.jpg) right; background-repeat: repeat-y;">
                <table style="padding: 0px; width: 100%" border="0" cellpadding="0" cellspacing="0">
                    <tr>
                        <td colspan="1" rowspan="2" width="256" height="166" id="header_bg_left">
                            <br />
                        </td>
                        <td colspan="6" rowspan="1" height="66">
                            <table border="0" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td id="dummy">
                                        <br />
                                    </td>
                                    <td>
                                        <div id="navi_box">
                                            <xsl:call-template name="navigation.row" />
                                        </div>
                                    </td>
                                    <td>
                                        <div id="thulb_logo">
                                            <a href="http://www.thulb.uni-jena.de">
                                                <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb_logo.gif" border="0"></img>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td colspan="1" rowspan="2" width="132" height="166" id="header_bg_right">
                            <br />
                        </td>
                    </tr>
                    <tr>
                        <td width="87" height="100">
                            <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_akruetzel_top_2.jpg" alt="text" border="0" />
                        </td>
                        <td id="header_filler">
                            <br />
                        </td>
                        <td width="160" height="100">
                            <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_akruetzel_top_3.jpg" alt="text" border="0" />
                        </td>
                        <td id="header_filler">
                            <br />
                        </td>
                        <td width="160" height="100">
                            <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_akruetzel_top_3.jpg" alt="text" border="0" />
                        </td>
                        <td id="header_filler">
                            <br />
                        </td>
                    </tr>
                </table>
                <table style="padding-bottom: 5px; height: 15px; vertical-align: top;" border="0" cellpadding="0" cellspacing="0">
                    <tr>
                        <td>
                            <div id="login_div">
                                <div id="login_div_inside">
                                    <xsl:call-template name="template_akruetzel.userInfo" />
                                </div>
                            </div>
                        </td>
                        <td>
                            <div id="navi_history">
                                <div id="navi_history_inside">
                                    <xsl:call-template name="navigation.history" />
                                </div>
                            </div>
                        </td>
                        <td>
                            <div id="end">
                                <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/end_akruetzel.gif" alt="text" border="0"></img>

                            </div>
                        </td>
                    </tr>
                </table>
                <table style="padding-top: 5px; width: 100%; vertical-align: top;" border="0" cellpadding="0" cellspacing="0">
                    <tr>
                        <td width="255px" style="vertical-align: top;">
                            <div id="area_left">
                                <div id="left_space" style="width:256px; height: 42px; background-color:#C25454;"></div>
                                <div id="div_navi_main">
                                    <xsl:call-template name="navigation.tree" />
                                </div>
                                <div id="footer_bottom">
                                    <a href="http://www.akruetzel.de">
                                        <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/footer.jpg" border="0"></img>
                                    </a>
                                </div>
                            </div>
                        </td>
                        <td style="vertical-align: top; text-align: left;">
                            <div id="contentArea">
                                <div id="contentWrapper">
                                    
                                    <xsl:call-template name="template_akruetzel.write.content" />
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>


            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_akruetzel.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_akruetzel.userInfo">

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