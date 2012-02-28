<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n" xmlns:xalan="http://xml.apache.org/xalan">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_fes">

        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader">
                    <xsl:with-param name="nameOfTemplate" select="'template_fes'" />
                </xsl:call-template>
            </head>
            <body>
                <table width="100%" height="24px" border="0" cellspacing="0" cellpadding="0" style="background-color: #f5f5f5;">
                    <tr valign="center">
                        <td align="right">
                          <div id="navigation_box">
                            <xsl:call-template name="navigation.row" />
                          </div>
                        </td>
                    </tr>
                </table>

                <table height="142" width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color:transparent;">
                  <tr>
                    <td style="width:144px; background-image:url({$WebApplicationBaseURL}templates/master/template_fes/IMAGES/header_left.png); background-repeat:no-repeat;">
                    </td>
                    <td>
                      <table width="100%" height="142" cellspacing="0" >
                        <tr><td
                          style="background-image:url({$WebApplicationBaseURL}templates/master/template_fes/IMAGES/header_x.png); background-repeat:repeat-x;">
                        </td></tr>
                      </table>
                    </td>
                    <td
                      style="width:499px; background-image:url({$WebApplicationBaseURL}templates/master/template_fes/IMAGES/header_center.png); background-repeat:no-repeat;">
                    </td>
                    <td>
                      <table width="100%" height="142" cellspacing="0" >
                        <tr><td
                          style="background-image:url({$WebApplicationBaseURL}templates/master/template_fes/IMAGES/header_x.png); background-repeat:repeat-x;">
                        </td></tr>
                      </table>
                    </td>
                    <td
                      style="width:181px; background-image:url({$WebApplicationBaseURL}templates/master/template_fes/IMAGES/header_right.png); background-repeat:no-repeat;">
                    </td>
                  </tr>
                </table>

	            <div id="navi_history">
                  <xsl:call-template name="navigation.history" />
                </div>

                <table width="100%" height="max" border="0" cellspacing="0" cellpadding="0" style="padding-bottom: 10px;padding-top: 40px" valign="top">
                  <tr valign="top">
                    <td width="200px" valign="top" rowspan="2">
                      <div id="div_navi_main">
                        <xsl:call-template name="Navigation_main">
                          <!-- use percent values -->
                          <xsl:with-param name="spaceBetweenMainLinks" select="'3'" />
                          <!-- use pixel values -->
                          <xsl:with-param name="borderWidthSides" select="'0'" />
                          <!-- use percent values -->
                        </xsl:call-template>
                      </div>
                    </td>
                  </tr>
                  <tr valign="top">
                    <td width="max" valign="top" style="height: 500px;">
                      <div id="contentArea">
                        <div id="contentWrapper">
                          <xsl:call-template name="template_fes.write.content" />
                        </div>
                      </div>
                    </td>
                  </tr>
                </table>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_fes.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>


</xsl:stylesheet>
