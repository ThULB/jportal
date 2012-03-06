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
    <xsl:template name="template_isis">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>

            <body>
                <div id="topmenu">

                    <p id="navi_below_cell">
                      <xsl:call-template name="navigation.row" />
                    </p>

                </div>

                <div id="logotop">
                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/logo.jpg" alt="Allgemeines - ISIS von Oken"></img>
                </div>

                <table id="menuContentWrapper">
                    <tr>
                        <td id="menu-width">
                            <div id="leftmenu">
                                <img id="blueimg" src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/muh.jpg" alt="Kasten"></img>
                                <div id="navmain">
                                    <xsl:call-template name="navigation.tree" />
                                </div>
                                <a href="http://www.dfg.de" target="_blank" id="logos">
                                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/dfg.png" alt="Logo DFG"></img>
                                </a>
                                <div id="sponsor-line" />
                                <a href="http://www.thulb.uni-jena.de" target="_blank" id="logos">
                                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb.png" alt="Logo ThulB"></img>
                                </a>
                                <div id="sponsor-line" />
                                <a href="http://www2.uni-jena.de/biologie/ehh/haeckel.htm" target="_blank" id="logos">
                                    <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/ehh.png" alt="Logo Ernst Haeckel Haus"></img>
                                </a>
                            </div>
                        </td>
                        <td id="contentArea">
                            <div id="navi_history">
                                <div id="navi_history_inside">
                                    <xsl:call-template name="navigation.history" />
                                </div>
                            </div>
                            <xsl:call-template name="template_isis.write.content" />
                        </td>
                    </tr>
                </table>

            </body>
        </html>
    </xsl:template>
    <!-- ======================================================================================================== -->
    <xsl:template name="template_isis.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>
    <!-- ======================================================================================================== -->
    <xsl:template name="template_isis.userInfo">

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