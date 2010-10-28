<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 529 $ $Date: 2008-07-02 16:01:39 +0200 (Mi, 02 Jul 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_phil_II">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>

                <div id="footer1">
                    <div id="border">
                        <div id="login_div" style="	background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/login.gif) no-repeat;">
                            <xsl:call-template name="template_phil_II.userInfo" />
                        </div>
                        <div id="navigation">
                            <xsl:call-template name="navigation.row">
                                <xsl:with-param name="rootNode" select="'navi-below'" />
                                <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                                <xsl:with-param name="menuPointHeigth" select="'21'" />
                                <!-- use pixel values -->
                                <xsl:with-param name="spaceBetweenLinks" select="'12'" />
                                <!-- use pixel values -->
                            </xsl:call-template>
                        </div>
                    </div>
                    <div id="banner" style="	background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_top.jpg) no-repeat;">
                        <div id="navi_history">
                            <xsl:call-template name="navigation.history" />
                        </div>
                    </div>
                    <div id="navi_all">
                        <div id="div_navi_main" style="	background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/navi.gif) repeat-y;">
                            <xsl:call-template name="Navigation_main" />
                        </div>
                        <div id="navi_under">
                            <map name="navi_under">
                                <area shape="rect" coords="50,9,255,34" href="http://zs.thulb.uni-jena.de/content/main/journals/phil1.xml" alt="xxx" />
                                <area shape="rect" coords="50,36,255,75" href="http://zs.thulb.uni-jena.de/content/main/journals/phil2.xml" alt="xxx" />
                                <area shape="rect" coords="50,77,255,153" href="http://zs.thulb.uni-jena.de/content/main/journals/phil3.xml" alt="xxx" />
                                <area shape="rect" coords="50,155,255,208" href="http://zs.thulb.uni-jena.de/content/main/journals/phil4.xml" alt="xxx" />
                                <area shape="rect" coords="50,210,255,267" href="http://zs.thulb.uni-jena.de/content/main/journals/phil5.xml" alt="xxx" />
                            </map>
                            <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/navi_under.gif" width="255" height="267" alt="sitemap"
                                usemap="#navi_under" border="none" />
                        </div>
                    </div>
                    <br />
                    <div id="contentArea">
                        
                        <xsl:call-template name="template_phil_II.write.content" />
                    </div>
                </div>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_phil_II.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_phil_II.userInfo">

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
                    <!--Login-Button / 2 Pfeile ===================================-->
                    <a href="{$LoginURL}">
                        <div class="buttons">
                            &#x25B6;
                            <br />
                            &#160;&#x25C0;
                        </div>
                    </a>
                </td>
            </tr>
        </table>

    </xsl:template>

</xsl:stylesheet>
