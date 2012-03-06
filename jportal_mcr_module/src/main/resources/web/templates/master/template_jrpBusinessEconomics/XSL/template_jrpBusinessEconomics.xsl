<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 529 $ $Date: 2008-07-02 15:01:39 +0100 (Mi, 02 Jul 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_jrpBusinessEconomics">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>
                <table cellspacing="0" cellpadding="0" id="footer">
                    <tr>
                        <td style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jrpbe_top_left.jpg) no-repeat;">
                            <table cellspacing="0" cellpadding="0" width="600px">
                                <tr>
                                    <td>
                                        <div id="fsu-logo">
                                            <a href="http://www.uni-jena.de" target="_blank">
                                                <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/fsu-logo.png" border="0" />
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div id="login_div">
                                            <div id="login_div_inside">
                                                <xsl:call-template name="template_jrpbe.userInfo" />
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td width="300px"
                            style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jrpbe_top_middle.jpg); repeat x;">
                        </td>
                        <td style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jrpbe_top_right.jpg) no-repeat;">
                            <table cellspacing="0" cellpadding="0" width="414px">
                                <tr>
                                <td style="width: 346px; padding-bottom:40px; text-align:right;">
                                        <table id="navi_box">
                                            <tr>
                                                <td>
                                                    <xsl:call-template name="navigation.row">
                                                        <xsl:with-param name="padding-right" select="'0'" />
                                                    </xsl:call-template>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td style="vertical-align: top; padding-right: 4px; text-align: center; padding-top:4px">
                                        <a href="http://www.thulb.uni-jena.de" target="_blank">
                                            <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb_logo.png" border="0" />
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <table border="0" cellspacing="0" cellpadding="0" id="second-line">
                    <tr>
                        <td style="width: 160px; vertical-align: top; text-align: left;">
                            <div id="div_navi_main"
                                style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jrpbe_left.jpg) no-repeat;">
                                <br />
                                <xsl:call-template name="navigation.tree" />
                            </div>
                        </td>
                        <td style="vertical-align: top; text-align: left; height: 100%;">
                            <div id="navi_history">
                                <div id="navi_history_inside">
                                    <xsl:call-template name="navigation.history" />
                                </div>
                            </div>
                            <div id="contentArea">
                                <div id="contentWrapper">
                                    <xsl:call-template name="template_jrpbe.write.content" />
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_jrpbe.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_jrpbe.userInfo">

        <!-- BEGIN: login values -->
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" />
        <!-- END OF: login values -->
        <table class="login_window" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td>
                    <xsl:value-of select="i18n:translate('editor.start.LoginText.label')" />
                    &#8226;
                    <xsl:value-of select="$CurrentUser" />
                </td>
            </tr>
        </table>

    </xsl:template>
</xsl:stylesheet>