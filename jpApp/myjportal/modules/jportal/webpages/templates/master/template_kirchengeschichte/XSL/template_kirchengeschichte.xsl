<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n" xmlns:xalan="http://xml.apache.org/xalan">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_kirchengeschichte">
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader">
                    <xsl:with-param name="nameOfTemplate" select="'template_kirchengeschichte'" />
                </xsl:call-template>
            </head>
            <body>
                <table width="100%" height="34px" border="0" cellspacing="0" cellpadding="0" style="background-color: #858688;">
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

                <table height="146" width="100%" border="0" cellspacing="0" cellpadding="0">
                  <tr>
                  	<td style="background-image:url({$WebApplicationBaseURL}templates/master/template_kirchengeschichte/IMAGES/header_balken.png);
                               background-repeat:repeat; width:100%">
						<table height="146" width="100%" border="0" cellspacing="0" cellpadding="0">
							<tr>
								<td style="background-image:url({$WebApplicationBaseURL}templates/master/template_kirchengeschichte/IMAGES/header.png);
	                               background-repeat:no-repeat; background-position:center; width:1024px;">
	                          		<div id="navi_history">
										<xsl:call-template name="navigation.history" />
									</div>
								</td>
							</tr>
						</table>
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
                                    <xsl:with-param name="spaceBetweenMainLinks" select="'4'"/>
                                    <!-- use pixel values -->
                                    <xsl:with-param name="borderWidthTopDown" select="'15'"/>
                                    <!-- use pixel values -->
                                    <xsl:with-param name="borderWidthSides" select="'0'"/>
                                    <!-- use percent values -->
                                  </xsl:call-template>
                                </div>

                                <div id="logo-thulb">
                                    <a href="http://www.thulb.uni-jena.de/" 
                                       onmouseover="hover1.src='{$WebApplicationBaseURL}templates/master/template_kirchengeschichte/IMAGES/ThulbBlau.png'"
                                       onmouseout="hover1.src='{$WebApplicationBaseURL}templates/master/template_kirchengeschichte/IMAGES/ThulbGrau.png'" >
                                       <img name="hover1" src="{$WebApplicationBaseURL}templates/master/template_kirchengeschichte/IMAGES/ThulbGrau.png"
                                            width="55" height="19" border="0">
                                       </img>
                                    </a>
                                </div>
                                <div id="logo-dfg">
                                    <a href="http://www.dfg.de" 
                                       onmouseover="hover2.src='{$WebApplicationBaseURL}templates/master/template_kirchengeschichte/IMAGES/DFGSchwarz.png'"
                                       onmouseout="hover2.src='{$WebApplicationBaseURL}templates/master/template_kirchengeschichte/IMAGES/DFGGrau.png'" >
                                       <img name="hover2" src="{$WebApplicationBaseURL}templates/master/template_kirchengeschichte/IMAGES/DFGGrau.png"
                                            width="100" height="37" border="0">
                                       </img>
                                    </a>
                                </div>
                                <div id="logo-kirchengeschichte">
                                    <a href="http://www.uni-jena.de/kirchengeschichte-p-138.html"> 
                                       Lehrstuhl für Kirchengeschichte
                                    </a>
                                </div>
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
                                        <xsl:call-template name="template_kirchengeschichte.write.content" />
                                    </div>
                                </div>
                            </td>
                        </tr>
                </table>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_kirchengeschichte.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_kirchengeschichte.userInfo">

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
                        <img src="{$WebApplicationBaseURL}templates/master/template_kirchengeschichte/IMAGES/login-switch.gif" border="0" />
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
