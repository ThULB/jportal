<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 494 $ $Date: 2008-05-29 16:09:29 +0200 (Do, 29 Mai 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_izw">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>

                <div id="komplett">
                    <div id="banner">
                        <!--Navigation oben-->
                        <div id="navigation_row">
                          <xsl:call-template name="navigation.row" />
                        </div>
                    </div>

                    <!--Srednjajy Teil Benutzer und Navigation-->
                    <div id="mitu">
                        <!--Sache mitte-->
                        <div id="login">
                            <xsl:call-template name="template_izw.userInfo" />
                        </div>
                        <div id="navi_history">
                            <xsl:call-template name="navigation.history" />
                        </div>
                    </div>
                    <!--End Srednej Teil-->

                    <!--Text hier-->
                    <div id="text">
                        <div id="contentWrapper">
                            <xsl:call-template name="template_izw.write.content" />
                        </div>
                    </div>
                    <!--end text hier-->

                    <!--Main Navigation-->
                    <div id="nav">
                        <div id="nav_main">
                            <xsl:call-template name="Navigation_main" />
                        </div>
                    </div>
                    <div id="navu"></div>
                    <!--end Main navigation-->
                    <!--<div id="buchbd" style="height:254px; width:237px; float:right; background: URL({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/book_right.jpg);">
                        </div>-->
                </div>


            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_izw.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_izw.userInfo">

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
