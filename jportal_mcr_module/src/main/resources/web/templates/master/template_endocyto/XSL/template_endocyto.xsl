<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 529 $ $Date: 2008-07-02 16:01:39 +0200 (Mi, 02 Jul 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_endocyto">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>

                <div id="footer1">
                    <table style="width: 100%;" cellspacing="0" cellpadding="0">
                        <tr>
                            <td id="banner-top" colspan="2">
                                <div id="navigation">
                                    <xsl:call-template name="navigation.row" />
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td id="banner" style="background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_top.gif) no-repeat;">
                                <div id="login_div">
                                    <xsl:call-template name="template_endocyto.userInfo" />
                                </div>
                                <div id="navi_history">
                                    <xsl:call-template name="navigation.history" />
                                </div>
                            </td>
                            <td
                                style="background: url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_top_bg.gif); width: max; height: 139px;">
                                <br />
                            </td>
                        </tr>
                    </table>
                    <!--<div id="banner"
                        style="	background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_top.gif) no-repeat;">
                        <div id="login_div">
                        <xsl:call-template name="template_endocyto.userInfo"/>
                        </div>
                        
                        <div style="background: url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_top_bg.gif); width: 100%; height: 139px; float:right;" />	
                        </div>-->
                    <div id="navi_all">
                        <div id="div_navi_main" style="	background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/navi_bg.gif) repeat-y;">
                            <xsl:call-template name="Navigation_main" />
                        </div>
                        <div id="navi_under" style="	background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/navi_under.gif);"></div>
                    </div>
                    <br />
                    <div id="contentArea">
                        
                        <xsl:call-template name="template_endocyto.write.content" />
                    </div>
                </div>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_endocyto.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_endocyto.userInfo">

        <!-- BEGIN: login values -->
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" />
        <!-- END OF: login values -->
        <table class="login_window" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td class="login_text">
                    <text i18n="editor.start.LoginText.label" />
                    :
                </td>
                <td class="user_id">
                    <p class="whitebox">
                        <xsl:value-of select="$CurrentUser" />
                    </p>
                </td>
                <td class="login_window">
                    <!-- Login-Button / 2 Pfeile =================================== -->
                    <a href="{$LoginURL}">
                        <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/login-switch.gif" border="0" />
                    </a>
                </td>
            </tr>
        </table>

    </xsl:template>

</xsl:stylesheet>