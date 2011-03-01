<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_master">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>

            <body>
                <div class="border">
                    <div class="logoBar">

                        <!-- Navigation right top corner -->
                        <div class="navigationBelow">

                            <div class="top">
                                <xsl:call-template name="navigation.row" />
                            </div>
                            <div class="history">
                                <xsl:call-template name="navigation.history" />
                            </div>

                        </div>
                        <!-- navigationBelow / Navigation right top corner -->

                    </div>
                    <!-- logoBar -->

                    <div class="mainSite_MenuArea">

                        <div class="mainMenu">
                            <xsl:call-template name="Navigation_main" />
                        </div>

                    </div>
                    <!-- mainSite_MenuArea -->

                    <div class="mainSite_ContentArea">
                        <div id="contentArea">
                            <xsl:call-template name="template_master.write.content" />
                        </div>
                    </div>
                    <!-- mainSite_ContentArea -->

                    <div style="clear: both;">
                        <br />
                    </div><!-- a small buffer beetween logo and content -->

                </div><!-- border -->

            </body>
        </html>
    </xsl:template>


    <!-- ======================================================================================================== -->
    <xsl:template name="template_master.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>


    <!-- ======================================================================================================== -->
    <xsl:template name="template_master.userInfo"><!-- BEGIN: login values -->
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" />
        <!-- END OF: login values -->
        <text i18n="editor.start.LoginText.label" />
        :
        <a href="{$LoginURL}">
            <xsl:value-of select="$CurrentUser" />
        </a>
    </xsl:template>
</xsl:stylesheet>