<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 529 $ $Date: 2008-07-02 16:01:39 +0200 (Mi, 02 Jul 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_dkp">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>
                <table cellspacing="0" cellpadding="0" id="footer">
                    <tr>
                        <td
                            style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_dkp_top_left.gif) no-repeat; vertical-align: top;">
                            <table cellspacing="0" cellpadding="0" width="350px" height="150px">
                                <tr>
                                    <td id="login">
                                        <div id="login_div">
                                            <xsl:call-template name="template_dkp.userInfo" />
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td width="800px"
                            style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_dkp_top_middle.gif) repeat-x;">
                        </td>
                        <td style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_dkp_top_right.gif) no-repeat;">
                            <table cellspacing="0" cellpadding="0" width="620px">
                                <tr>
                                    <td>
                                        <div id="navi_box">
                                            <xsl:call-template name="navigation.row" />
                                        </div>
                                    </td>
                                    <td width="80px">
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
                <div id="second-line">
                    <div id="div_navi_main"
                        style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_dkp_navi_top.gif) no-repeat;">
                        <br />
                        <xsl:call-template name="navigation.tree" />
                    </div>
                    <div id="navi_history">
                        <div id="navi_history_inside">
                            <xsl:call-template name="navigation.history" />
                        </div>
                    </div>
                    <div id="contentArea">
                        <div id="contentWrapper">
                            
                            <xsl:call-template name="template_dkp.write.content" />
                        </div>
                    </div>
                </div>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_dkp.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_dkp.userInfo">

        <!-- BEGIN: login values -->
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" />
        <!-- END OF: login values -->
        <table class="login_window" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td id="login_hover">
                    <!-- Login-Button / 2 Pfeile =================================== -->
                    <a href="{$LoginURL}">

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