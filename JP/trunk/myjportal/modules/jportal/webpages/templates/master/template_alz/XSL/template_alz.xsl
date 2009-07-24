<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 529 $ $Date: 2008-07-02 16:01:39 +0200 (Wed, 02 Jul 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_alz">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>

            <body>

                <!-- Begin: Big fat Maintable =========================================================================== -->
                <table class="main" border="0" cellspacing="0" cellpadding="0">

                    <!-- Begin: Top margin and define column ================================================== -->
                    <tr>
                        <td class="left_margin">&#160;</td>
                        <td class="menu_column">
                            <p />
                        </td>
                        <td class="conten_column">
                            <p />
                        </td>
                    </tr>
                    <!-- End: Top margin and define column ==================================================== -->

                    <!-- Begin: Login-row and Navigation-row ================================================== -->
                    <tr>
                        <td class="login_navi_row" />
                        <!-- Begin: Login ===================================================================== -->
                        <td rowspan="3" class="login_row">
                            <xsl:call-template name="template_alz.userInfo" />
                        </td>
                        <!-- End: Login ======================================================================= -->

                        <!-- Begin: Navigation ================================================================ -->
                        <td id="navi_below_cell">
                            <xsl:call-template name="navigation.row">
                                <!--xsl:with-param name="rootNode" select="$loaded_navigation_xml/navi-below" /-->
                                <xsl:with-param name="rootNode" select="'navi-below'" />
                                <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                                <xsl:with-param name="menuPointHeigth" select="'21'" /><!-- use pixel values -->
                                <xsl:with-param name="spaceBetweenLinks" select="'12'" /><!-- use pixel values -->
                            </xsl:call-template>
                        </td>
                        <!-- End: Navigation =================================================================== -->
                    </tr>
                    <!-- End: Login-row and Navigation-row ===================================================== -->

                    <!-- Begin: Decoration lines =============================================================== -->
                    <tr>
                        <!-- td class="decor_line">&#160;</td-->
                        <td class="decor_line"></td>
                        <td class="decor_line"></td>
                        <td class="decor_line"></td>
                        <!--td class="decor_line"/>
                            <td class="decor_line"/-->
                    </tr>
                    <tr>
                        <td class="decor_spc">&#160;</td>
                        <td class="decor_spc">&#160;</td>
                        <td class="decor_spc">&#160;</td>
                        <!--td class="decor_spc"/>
                            <td class="decor_spc"/-->
                    </tr>
                    <!-- End: Decoration lines ================================================================= -->

                    <!-- Begin: Main Graphics ================================================================== -->
                    <tr>
                        <td class="balk1" colspan="2">
                            <img class="balk1" src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/balk1.gif" alt="balk1" />
                        </td>
                        <td class="balk2">
                            <img class="balk2" src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/balk2.gif" />
                        </td>
                    </tr>
                    <!-- End: Main Graphics ==================================================================== -->

                    <!-- Begin: Navigation history ============================================================= -->
                    <tr>
                        <td class="navi_history" />

                        <!-- Begin: Part of Main Graphics / rest of the book =================================== -->
                        <td rowspan="4" class="balk_u">
                            <img class="balk_u" src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/balk_u.gif" />
                        </td>
                        <!-- End: Part of Main Graphics / rest of the book ===================================== -->

                        <td class="navi_history">
                            <table class="navi_history" border="0" cellspacing="0" cellpadding="0">
                                <tr>
                                    <td class="navi_history">
                                        <xsl:call-template name="navigation.history" />
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <!-- End: Navigation history =============================================================== -->

                    <!-- Begin: Decoration lines =============================================================== -->
                    <tr>
                        <td class="decor_spc" />
                        <td class="decor_spc" />
                        <td class="decor_spc" />
                    </tr>
                    <tr>
                        <td class="decor_line" />
                        <td class="decor_line" />
                        <td class="decor_line" />
                    </tr>
                    <tr>
                        <td class="decor_big_spc" />
                        <td class="decor_big_spc" />
                        <td class="decor_big_spc" />
                    </tr>
                    <!-- End: Decoration lines ================================================================= -->

                    <!-- Begin: Main Menu / Content Area ======================================================= -->
                    <tr>
                        <td />
                        <!-- Begin: Main Menu  ================================================================= -->
                        <!-- Anmerkung: Fuer ein aufgeklapptes Menu wird eine Grafik in form eines "L" verwendet,
                            das irgendwo im System liegt. Evtl. kann man dieses "L" durch ein Unicode
                            Derivat ersetzen -->
                        <td class="navi_main">
                            <div class="navi_main">
                                <table border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td>
                                            <xsl:call-template name="Navigation_main" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <img class="jahr" src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/jahr.gif"
                                                alt="Seit 1874 bis 1978" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="empty_box" />
                                    </tr>
                                </table>
                            </div>
                        </td>
                        <!-- End: Main Menu  =================================================================== -->

                        <!-- Begin: Content Area  ============================================================== -->
                        <td id="contentArea">
                            <div id="contentWrapper">
                                
                                <xsl:call-template name="template_alz.write.content" />
                            </div>
                        </td>
                        <!-- End: Content Area  ================================================================ -->
                    </tr>
                    <!-- End: Main Menu / Content Area ========================================================= -->

                    <!-- Begin: Thulb-Logo ===================================================================== -->
                    <tr>
                        <td />
                        <td align="center">
                            <div class="logo">
                                <img class="logo" src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/logo.gif" alt="Thulb-Logo" />
                            </div>
                        </td>
                    </tr>
                    <!-- End: Thulb-Logo ======================================================================= -->
                </table>

                <!-- End: Big fat Maintable ============================================================================= -->
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_alz.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_alz.userInfo">

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