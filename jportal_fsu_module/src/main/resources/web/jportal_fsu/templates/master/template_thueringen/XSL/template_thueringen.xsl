<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n" xmlns:xalan="http://xml.apache.org/xalan">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_thueringen">
        <xsl:variable name="journalsID">
          <xsl:value-of select="document('jportal_getJournalID:XPathDoesNotExist')/dummyRoot/hidden/@default" />
        </xsl:variable>
        <xsl:variable name="journalXML">
          <xsl:copy-of select="document(concat('mcrobject:',$journalsID))" />
        </xsl:variable>
        <xsl:variable name="journalMaintitle">
          <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
        </xsl:variable>

        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader">
                    <xsl:with-param name="nameOfTemplate" select="'template_thueringen'" />
                </xsl:call-template>
            </head>
            <body>
                <table width="100%" height="30px" border="0" cellspacing="0" cellpadding="0" style="background-color: #858688;">
                    <tr valign="center">
                        <td style="padding-left: 10px;">
                            <div id="headline">
                                <a href="http://www.urmel-dl.de/" target="_blank">UrMEL</a>
                                <xsl:copy-of select="'     |     '" />
                                <a href="http://zs.thulb.uni-jena.de/content/below/index.xml" target="_self">Journals@UrMEL</a>
                            </div>
                        </td>
                        <td align="right">
                          <div id="navigation_box">
                            <xsl:call-template name="navigation.row">
                              <xsl:with-param name="rootNode" select="'navi-below'" />
                              <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                              <xsl:with-param name="menuPointHeigth" select="'21'" />
                              <xsl:with-param name="spaceBetweenLinks" select="'12'" />
                            </xsl:call-template>
                          </div>
                        </td>
                    </tr>
                </table>

                <table height="179" width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color:transparent;">
                  <tr>
                    <td style="width:550px; background-image:url({$WebApplicationBaseURL}templates/master/template_thueringen/IMAGES/th_header_left.jpg); background-repeat:no-repeat;">
                      <div id="journal-title">
                        <xsl:copy-of select="$journalMaintitle" />
                      </div>
                      <div id="navi_history">
                        <xsl:call-template name="navigation.history" />
                      </div>
                    </td>
                    <td>
                      <table width="100%" height="179" cellspacing="0" >
                        <tr><td
                          style="background-image:url({$WebApplicationBaseURL}templates/master/template_thueringen/IMAGES/th_header_center.jpg); background-repeat:repeat-x;">
                        </td></tr>
                      </table>
                    </td>
                    <td
                      style="width:150px; background-image:url({$WebApplicationBaseURL}templates/master/template_thueringen/IMAGES/th_header_right.jpg); background-repeat:no-repeat;">
                    </td>
                  </tr>
                </table>

                <table width="100%" height="max" border="0" cellspacing="0" cellpadding="0" style="padding-bottom: 10px;"
                        valign="top">
                        <tr valign="top">
                            <td width="200px" valign="top" rowspan="2">
                                <div id="div_navi_main">
                                  <xsl:call-template name="navigation.tree">
                                    <xsl:with-param name="rootNode" select="'navi-main'"/>
                                    <xsl:with-param name="CSSLayoutClass" select="'navi_main'"/>
                                    <xsl:with-param name="menuPointHeigth" select="'17'"/>
                                    <!-- use pixel values -->
                                    <xsl:with-param name="columnWidthIcon" select="'9'"/>
                                    <!-- use percent values -->
                                    <xsl:with-param name="spaceBetweenMainLinks" select="'2'"/>
                                    <!-- use pixel values -->
                                    <xsl:with-param name="borderWidthTopDown" select="'15'"/>
                                    <!-- use pixel values -->
                                    <xsl:with-param name="borderWidthSides" select="'0'"/>
                                    <!-- use percent values -->
                                  </xsl:call-template>
                                </div>
                                <!-- are special logos for current used journal available ? -> if yes, get them -->
                                <xsl:call-template name="template_logos.getLogos">
                                    <xsl:with-param name="journalsID" select="$journalID" />
                                </xsl:call-template>
                            </td>
                            <td width="max" valign="top" style="height: 12px">
                                <xsl:text> </xsl:text>
                            </td>
                            <td width="135px" rowspan="2" valign="top" style="height: 500px;">
                                <xsl:text> </xsl:text>
                            </td>
                        </tr>
                        <tr valign="top">
                            <td width="max" valign="top" style="height: 500px;">
                                <div id="contentArea">
                                    <div id="contentWrapper">
                                        <xsl:call-template name="template_thueringen.write.content" />
                                    </div>
                                </div>
                            </td>
                        </tr>
                </table>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_thueringen.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

</xsl:stylesheet>
