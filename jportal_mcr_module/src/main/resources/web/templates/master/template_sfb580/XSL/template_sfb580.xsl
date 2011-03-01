<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 494 $ $Date: 2008-05-29 16:09:29 +0200 (Do, 29 Mai 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="i18n xsl xlink">

    <!-- any XML elements defined here will go into the head -->
    <!-- other stylesheets may override this variable -->
    <xsl:variable name="head.additional" />

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_sfb580">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>

            <body>
                <table id="topmenu" cellspacing="0" cellpadding="0" border="0">
                    <tr id="head-top">
                        <td id="head-top">
                            <div id="navi_below_cell">
                                <xsl:call-template name="navigation.row" />
                            </div>
                        </td>
                    </tr>
                    <tr id="head-middle">
                        <td id="sfb-logo">
                            <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/logo_sfb_final.png" alt="SFB-Logo"></img>
                        </td>
                    </tr>
                    <tr id="head-bottom">
                        <td>
                            <div id="navi_history">
                                <div id="navi_history_inside">
                                    <xsl:call-template name="navigation.history" />
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>
                <table id="menuContentWrapper" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                        <td id="menu-width">
                            <div id="leftmenu">
                                <div id="navmain">
                                    <xsl:call-template name="Navigation_main" />
                                </div>
                                <div id="sponsor-line" />
                                <a href="http://www.thulb.uni-jena.de" target="_blank" id="logos">
                                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb.png" alt="Logo ThulB"></img>
                                </a>
                            </div>
                        </td>
                        <td id="contentArea">
                            <xsl:call-template name="template_sfb580.write.content" />
                        </td>
                    </tr>
                </table>

            </body>
        </html>
    </xsl:template>
    <!-- ======================================================================================================== -->
    <xsl:template name="template_sfb580.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>
    <!-- ======================================================================================================== -->
    <xsl:template name="template_sfb580.userInfo">

        <!-- BEGIN: login values -->
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" />
        <!-- END OF: login values -->

        <xsl:value-of select="concat(i18n:translate('users.user'),': ')" />
        <a href="{$LoginURL}">
            <xsl:value-of select="$CurrentUser" />
        </a>

    </xsl:template>

</xsl:stylesheet>