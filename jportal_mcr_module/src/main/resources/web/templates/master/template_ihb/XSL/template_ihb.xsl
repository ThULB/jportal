<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 529 $ $Date: 2008-07-02 16:01:39 +0200 (Mi, 02 Jul 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_ihb">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>

                <div id="footer1">
                    <div id="border">
                        <div id="navigation">
                            <xsl:call-template name="navigation.row" />
                        </div>
                    </div>
                    <div id="banner">
                        <div id="banner_left"
                            style="	background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_top_left.gif) no-repeat;">
                            <div id="login_div">
                                <xsl:call-template name="template_ihb.userInfo" />
                            </div>
                        </div>
                        <div id="banner_right">
                            <map name="uni_ilmenau">
                                <area shape="rect" coords="2,5,183,140"
                                    href="http://zs.thulb.uni-jena.de/content/main/journals/journal_645.xml?XSL.lastPage.SESSION=/content/main/journals/journal_645.xml"
                                    alt="Universitätsnachrichten Ilmenau" />
                                <area shape="rect" coords="187,5,366,140"
                                    href="http://zs.thulb.uni-jena.de/content/main/journals/iun.xml?XSL.lastPage.SESSION=/content/main/journals/iun.xml"
                                    alt="Ilmenauer Uni-Nachrichten" />
                                <area shape="rect" coords="372,5,551,140"
                                    href="http://zs.thulb.uni-jena.de/content/main/journals/ihb.xml?XSL.lastPage.SESSION=/content/main/journals/ihb.xml"
                                    alt="Ilmenauer Hochschulblatt" />
                                <area shape="rect" coords="554,5,735,140"
                                    href="http://zs.thulb.uni-jena.de/content/main/journals/inh.xml?XSL.lastPage.SESSION=/content/main/journals/inh.xml"
                                    alt="Neue Hochschule" />
                            </map>
                            <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_top_right.gif" width="735" height="140"
                                alt="Universitï¿½t Ilmenau" usemap="#uni_ilmenau" border="0" />
                        </div>
                    </div>
                    <table id="footer2" border="0" cellpadding="0" cellspacing="0">
                        <tr>
                            <td valign="top" id="footer2-left">
                                <div id="navi_all">
                                    <div id="div_navi_main">
                                        <xsl:call-template name="navigation.tree" />
                                    </div>
                                    <div id="navi_under"
                                        style="	background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/navi_below.gif) repeat-y;">
                                    </div>
                                </div>
                            </td>
                            <td valign="top" id="footer2-right">
                                <div id="navi_history">
                                    <xsl:call-template name="navigation.history" />
                                </div>
                                <br />
                                <div id="contentArea">
                                    
                                    <xsl:call-template name="template_ihb.write.content" />
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
            </body>
        </html>

    </xsl:template>


    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_ihb.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>


    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_ihb.userInfo">

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