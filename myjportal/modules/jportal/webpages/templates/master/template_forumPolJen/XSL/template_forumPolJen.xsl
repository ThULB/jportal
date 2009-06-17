<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_forumPolJen">
        <xsl:param name="journalsMaintitle" select="'Zeitschriftenname'" />
        <xsl:param name="periodetitle" select="'Erscheinungszeitraum'" />
        <xsl:param name="journalID" />
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader">
                    <xsl:with-param name="nameOfTemplate" select="'template_forumPolJen'" />
                </xsl:call-template>
            </head>
            <body>
                  <table width="100%" height="30px" border="0" cellspacing="0" cellpadding="0" style="background-color: #868789;">
                      <tr valign="top">
                          <td width="100%">
                              <div id="whitespace"></div>
                              <span id="headline">
                                  <a href="http://www.urmel-dl.de/" target="_blank">UrMEL</a>
                                  <xsl:copy-of select="'     |     '" />
                                  <a href="http://zs.thulb.uni-jena.de/content/below/index.xml" target="_self">Journals@UrMEL</a>
                              </span>
                          </td>
                      </tr>
                  </table>
                  <div id="navigation_box">
                    <xsl:call-template name="navigation.row">
                      <xsl:with-param name="rootNode" select="'navi-below'" />
                      <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                      <xsl:with-param name="menuPointHeigth" select="'21'" />
                      <xsl:with-param name="spaceBetweenLinks" select="'12'" />
                    </xsl:call-template>
                  </div>
  
                  <div style="min-height: 800px; width: max;">
                      <table width="100%" height="163" border="0" cellspacing="0" cellpadding="0" style="background-color:transparent;">
                          <tr valign="top">
                              <td style="background:url({$WebApplicationBaseURL}templates/master/template_forumPolJen/IMAGES/fpj-repeat.png);background-repeat:repeat;" />
                              <td width="679px" align="center">
                                <table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
                                  <tr>
                                    <td height="105px" style="background:url({$WebApplicationBaseURL}templates/master/template_forumPolJen/IMAGES/fpj-logo.png);background-repeat:no-repeat;" />
                                  </tr>
                                  <tr>
                                    <td height="16px" >
                                      <div id="navi_history">
                                        <xsl:call-template name="navigation.history" />
                                      </div>
                                    </td>
                                  </tr>
                                  <tr>
                                    <td height="42px" style="background:url({$WebApplicationBaseURL}templates/master/template_forumPolJen/IMAGES/fpj-line.png) no-repeat;"/>
                                  </tr>
                                </table>
                              </td>
                              <td style="background:url({$WebApplicationBaseURL}templates/master/template_forumPolJen/IMAGES/fpj-repeat.png);background-repeat:repeat;"/> 
                          </tr>
                      </table>
                      <table width="100%" height="max" border="0" cellspacing="0" cellpadding="0" style="padding-right: 10px; padding-bottom: 10px;"
                          valign="top">
                          <tr valign="top">
                              <td width="200" valign="top">
                                  <div id="div_navi_main">
                                    <xsl:call-template name="navigation.tree">
                                      <xsl:with-param name="rootNode" select="'navi-main'"/>
                                      <xsl:with-param name="CSSLayoutClass" select="'navi_main'"/>
                                      <xsl:with-param name="menuPointHeigth" select="'17'"/>
                                      <!-- use pixel values -->
                                      <xsl:with-param name="columnWidthIcon" select="'9'"/>
                                      <!-- use percent values -->
                                      <xsl:with-param name="spaceBetweenMainLinks" select="'10'"/>
                                      <!-- use pixel values -->
                                      <xsl:with-param name="borderWidthTopDown" select="'3'"/>
                                      <!-- use pixel values -->
                                      <xsl:with-param name="borderWidthSides" select="'0'"/>
                                      <!-- use percent values -->
                                      <xsl:with-param name="textIndent" select="'8px'"/>
                                    </xsl:call-template>
                                  </div>
                              </td>
                              <td valign="top" style="height: 500px;" rowspan="2">
                                  <div id="contentArea">
                                      <div id="contentWrapper">
                                          <xsl:call-template name="template_forumPolJen.write.content" />
                                      </div>
                                  </div>
                              </td>
                          </tr>
                          <tr>
                            <td>
                              <!-- are special logos for current used journal available ? -> if yes, get them -->
                              <div id="div_navi_logo">
                                <xsl:call-template name="template_logos.getLogos">
                                  <xsl:with-param name="journalsID" select="$journalID" />
                                </xsl:call-template>
                              </div>
                            </td>
                          </tr>
                      </table>
                  </div>
            </body>
        </html>
    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_forumPolJen.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_forumPolJen.userInfo">

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
                        <img src="{$WebApplicationBaseURL}templates/master/template_forumPolJen/IMAGES/login-switch.gif" border="0" />
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