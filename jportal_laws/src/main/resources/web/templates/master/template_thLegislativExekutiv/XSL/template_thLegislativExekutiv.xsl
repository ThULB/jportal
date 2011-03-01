<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
    exclude-result-prefixes="xlink i18n" xmlns:xalan="http://xml.apache.org/xalan">

    <xsl:variable name="nameOfTemplate" select="'template_thLegislativExekutiv'" />

    <xsl:include href="jp_laws.xsl"/>

    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_thLegislativExekutiv">
        <xsl:variable name="journalsID">
          <xsl:value-of select="document('jportal_getJournalID:XPathDoesNotExist')/dummyRoot/hidden/@default" />
        </xsl:variable>
        <xsl:variable name="journalXML">
          <xsl:copy-of select="document(concat('mcrobject:',$journalsID))" />
        </xsl:variable>
        <xsl:variable name="journalMaintitle">
          <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
        </xsl:variable>
        <xsl:variable name="periodetitle">
          <xsl:copy-of select="concat(xalan:nodeset($journalXML)/mycoreobject/metadata/dates/date[@type='published_from']/text(),' - ',xalan:nodeset($journalXML)/mycoreobject/metadata/dates/date[@type='published_until']/text())" />
        </xsl:variable>
        <xsl:variable name="additionalTitle">
          <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/subtitles/subtitle/text()" />
        </xsl:variable>

        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader">
                    <xsl:with-param name="nameOfTemplate" select="$nameOfTemplate" />
                </xsl:call-template>
                <link href="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/CSS/style_laws.css" rel="stylesheet" type="text/css" />
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

                <table width="100%" height="30px" border="0" cellspacing="0" cellpadding="0" style="background-color: #bd1220;">
                  <tr valign="left">
                    <td style="border-color:#a19794; border-style:solid; border-top-width: 1px; border-bottom-width: 0px; border-left-width: 0px; border-right-width: 0px;">
                      <div id="journal-title">
                        <xsl:copy-of select="concat($journalMaintitle, ' ', $periodetitle, ' ', $additionalTitle)" />
                      </div>
                    </td>
                  </tr>
                </table>

                <table height="112" width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color:transparent;background:#44352e;">
                  <tr>
                    <td style="background-image:url({$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/IMAGES/header.png);
                               background-repeat:no-repeat; width:1024px; 
                               border-color:#a19794; border-style:solid; border-bottom-width: 1px; border-top-width: 0px;
                               border-left-width: 0px; border-right-width: 0px;">
                        <div id="headerBorder">
                        </div>
                    </td>
                  </tr>
                </table>

                <table height="60" width="100%" border="0" cellspacing="0" cellpadding="0">
                  <tr>
                    <td>
                      <div id="navi_history">
                        <xsl:call-template name="navigation.history" />
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <div id="th-logo">
                        <a href="http://www.urmel-dl.de/">
                          <img src="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/IMAGES/thLogo.png" width="117" height="49" border="0" />
                        </a>
                      </div>
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
                                       onmouseover="hover1.src='{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/IMAGES/thulb_logo_hover.png'"
                                       onmouseout="hover1.src='{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/IMAGES/thulb_logo.png'" >
                                       <img name="hover1" src="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/IMAGES/thulb_logo.png"
                                            width="85" height="30" border="0">
                                       </img>
                                    </a>
                                </div>
                                <div id="logo-archive">
                                    <a href="http://www.thueringen.de/de/staatsarchive/" 
                                       onmouseover="hover2.src='{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/IMAGES/archive_logo.png'"
                                       onmouseout="hover2.src='{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/IMAGES/archive_logo.png'" >
                                       <img name="hover2" src="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/IMAGES/archive_logo.png"
                                            width="106" height="30" border="0">
                                       </img>
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
                                        <xsl:call-template name="template_thLegislativExekutiv.write.content" />
                                    </div>
                                </div>
                            </td>
                        </tr>
                </table>
            </body>
        </html>

    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_thLegislativExekutiv.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_thLegislativExekutiv.userInfo">

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
                        <img src="{$WebApplicationBaseURL}templates/master/{$nameOfTemplate}/IMAGES/login-switch.gif" border="0" />
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

    <!-- ====================================================================-->
    <!-- Special handling for volumes to print laws in table form -->
    <xsl:template match="/mycoreobject[contains(@ID,'_jpvolume_')]" priority="3">
      <xsl:call-template name="jp_printVolumeData" />
    </xsl:template>

    <!-- ====================================================================-->

    <xsl:template name="jp_printVolumeData">
      <xsl:call-template name="jp_objectView_initJS" />
      <xsl:call-template name="printMetadataHead" />
      <xsl:choose>
        <!-- metadaten -->
        <xsl:when test="$view.objectmetadata = 'false'">
          <xsl:call-template name="printMetadata" />
        </xsl:when>
        <!-- inhaltsverzeichnis -->
        <xsl:otherwise>
          <xsl:variable name="register" select="metadata/identis/identi[@type='Register' or @type='register']"></xsl:variable>
          <xsl:if test="$register">
            <xsl:variable name="registerUrl" select="concat($WebApplicationBaseURL, 'register/', $register)"/>
            <xsl:apply-templates select="document($registerUrl)/gesetzessammlung" />
          </xsl:if>
          <!-- Print children at the end -->
          <xsl:call-template name="printChildren" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
