<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink">

    <!-- any XML elements defined here will go into the head -->
    <!-- other stylesheets may override this variable -->
    <xsl:variable name="head.additional" />

    <!-- ============================================== -->
    <!-- Seiten-Layout                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_aaz">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader" />
            </head>

            <body>
                <div id="img_head" style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/2.jpg) no-repeat;">
                    <div id="navi_top">
                        <xsl:call-template name="navigation.row">
                            <xsl:with-param name="rootNode" select="'navi-below'" />
                            <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                            <xsl:with-param name="menuPointHeigth" select="'21'" /><!-- use pixel values -->
                            <xsl:with-param name="spaceBetweenLinks" select="'12'" /><!-- use pixel values -->
                        </xsl:call-template>
                    </div>
                </div>
                <img id="img_body_fading" src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/3.gif" />
                <table id="content-table" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                        <td id="navi_left">

                            <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/link.gif" usemap="#Link" border="0">
                                <map name="Link">
                                    <area shape="rect" coords="0,46,130,92" href="http://zs.thulb.uni-jena.de/content/main/journals/aaz_pilot.xml" />
                                </map>
                            </img>


                            <!--<a href="{$WebApplicationBaseURL}templates/master/template_aaz/XSL/template_aaz.xsl">aaz</a>
                                <br />	
                                <a href="{$WebApplicationBaseURL}templates/master/template_aaz/XSL/template_aaz_pilot.xsl">aaz_pilot</a>-->
                            <xsl:call-template name="Navigation_main" />

                            <a href="http://www.thulb.uni-jena.de" target="_blank" id="thulb_logo">
                                <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb.png" />
                            </a>
                            <br />
                            <a href="http://www.thueringen.de/de/staatsarchive/rudolstadt/content.html" target="_blank" style="margin-left: 8px;">
                                Staatsarchiv Rudolstadt
                            </a>
                        </td>
                        <td id="content">
                            <div style="width:98%;">
                                <div id="navi_history">
                                    <div id="navi_history_inside">
                                        <xsl:call-template name="navigation.history" />
                                    </div>
                                </div>
                                <!--<xsl:choose>
                                    <xsl:when test="$readAccess='true'">-->
                                <xsl:call-template name="template_aaz.write.content" />
                                <!--</xsl:when>
                                    <xsl:otherwise> verboten! </xsl:otherwise>
                                    </xsl:choose>-->
                            </div>
                        </td>
                    </tr>
                </table>
            </body>

        </html>

    </xsl:template>
    <!-- ======================================================================================================== -->
    <xsl:template name="template_aaz.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

</xsl:stylesheet>
