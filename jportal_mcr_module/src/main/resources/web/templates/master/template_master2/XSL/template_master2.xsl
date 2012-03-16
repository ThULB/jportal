<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_master2">
        <xsl:param name="journalID" />
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>
            <body>
                <div id="header" class="header">
                    <div class="navi_horiz">
                        <div class="home_link">
                            <a href="http://www.urmel-dl.de/" target="_blank">UrMEL</a>
                            <xsl:copy-of select="'     |     '" />
                            <a href="/content/below/index.xml" target="_self">Journals@UrMEL</a>
                        </div>
                        <div>
                            <xsl:call-template name="navigation.row" />
                        </div>
                    </div>
                    <div class="logo">
                    </div>
                    <div class="navi_history">
                        <xsl:call-template name="navigation.history" />
                    </div>
                </div>
                <div id="navi_left" class="navi_left">
                    <div>
                        <xsl:call-template name="navigation.tree" />
                    </div>
                </div>
                <div id="content_area" class="content_area">
                    <xsl:call-template name="template_master2.write.content" />
                </div>
                <div id="footer" class="footer"></div>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_master2.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_master2.userInfo">

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