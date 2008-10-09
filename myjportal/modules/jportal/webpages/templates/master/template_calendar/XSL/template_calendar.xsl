<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:i18n="xalan:org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xlink i18n">
    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
    <xsl:template name="template_calendar">
        <xsl:variable name="journalsID">
            <xsl:value-of select="document('jportal_getJournalID:XPathDoesNotExist')/dummyRoot/hidden/@default" />
        </xsl:variable>
        <xsl:variable name="journalXML">
            <xsl:copy-of select="document(concat('mcrobject:',$journalsID))" />
        </xsl:variable>
                <!-- get name of journal -->
        <xsl:variable name="journalMaintitle">
            <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
        </xsl:variable>
        <html>
            <head>
                <xsl:call-template name="jp.layout.getHTMLHeader">
                    <!--<xsl:with-param name="nameOfTemplate" select="'template_calendar'" />-->
                </xsl:call-template>
            </head>
            <body>
                <div id="contentArea">
                    <div id="contentWrapper">
                        <div class="content">
                            <xsl:call-template name="template_calendar.write.content" />
                        </div>
                    </div>
                </div>
                
                <!-- the most top gray navigation bar -->
                <div class="navigationBar">
                    <table id="headline">
                        <tr>
                            <td>
                                <a href="http://www.urmel-dl.de/urmel/content/main/content.xml" target="_blank">UrMEL</a>
                            </td>
                            <td>|</td>
                            <td>
                                <a href="http://zs.thulb.uni-jena.de/content/below/index.xml" target="_self">Journals@UrMEL</a>
                            </td>
                        </tr>
                    </table>
                    <!-- rigth menu with Sitemap, Login etc. -->
                    <div id="navigation_box">
                        <xsl:call-template name="navigation.row">
                            <xsl:with-param name="rootNode" select="'navi-below'" />
                            <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
                            <xsl:with-param name="menuPointHeigth" select="'21'" />
                            <xsl:with-param name="spaceBetweenLinks" select="'12'" />
                        </xsl:call-template>
                    </div>
                </div>
                
                <!-- the second bar with image and logo for each journal -->
                <div class="bannerBar">
                    <!-- the images for logo and banner are defined in style_general.css -->
                    <div id="bannerExtention" />
                    <div id="mainBanner" />
                    <!-- the right logo -->
                    <div id="transparent-logo" />
                </div>
                
                <div id="navi_history">
                    <div class="line">
                        <xsl:call-template name="navigation.history" />
                    </div>
                </div>
                
                <!-- right main navigation column -->
                <div id="div_navi_main">
                    <div class="calenderTitle">
                        <div class="titleString">
                            <xsl:value-of select="$journalMaintitle" />
                        </div>
                    </div>
                    <xsl:call-template name="Navigation_main" />
                </div>
                
            </body>
        </html>
    </xsl:template>

    <!-- Template for Content ================================================================================== -->
    <xsl:template name="template_calendar.write.content">
        <xsl:call-template name="jp.layout.getHTMLContent" />
    </xsl:template>

    <!-- Template for User info ================================================================================ -->
    <xsl:template name="template_calendar.userInfo">

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
                        <img src="{$WebApplicationBaseURL}templates/master/template_calendar/IMAGES/login-switch.gif" border="0" />
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