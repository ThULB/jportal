<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder">

  <xsl:template match="/template[@id='template_calendar']" mode="template">
    <xsl:apply-templates select="document(concat('mcrobject:',@mcrID))/mycoreobject" mode="template_calendar" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_calendar">
    <script type="text/javascript" src="../templates/master/template_calendar/JS/keywords.js" />
    <script>
		$(document).ready(function() {
			loadKeywords();		
		});
    </script>
  </xsl:template>
</xsl:stylesheet>

<!-- <?xml version="1.0" encoding="ISO-8859-1"?> -->
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<!-- <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:i18n="xalan:org.mycore.services.i18n.MCRTranslation" -->
<!--     xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xlink i18n"> -->
    <!-- ============================================== -->
    <!-- the template                                   -->
    <!-- ============================================== -->
<!--     <xsl:variable name="journalsID"> -->
<!--         <xsl:value-of select="document('jportal_getJournalID:XPathDoesNotExist')/dummyRoot/hidden/@default" /> -->
<!--     </xsl:variable> -->
<!--     <xsl:template name="template_calendar"> -->
<!--         <xsl:variable name="journalXML"> -->
<!--             <xsl:copy-of select="document(concat('mcrobject:',$journalsID))" /> -->
<!--         </xsl:variable> -->
                <!-- get name of journal -->
<!--         <xsl:variable name="journalMaintitle"> -->
<!--             <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/maintitles/maintitle/text()" /> -->
<!--         </xsl:variable> -->
<!--         <html> -->
<!--             <head> -->
<!--                 <xsl:call-template name="jp.layout.getHTMLHeader"> -->
                    <!--<xsl:with-param name="nameOfTemplate" select="'template_calendar'" />-->
<!--                 </xsl:call-template> -->
<!--             </head> -->
<!--             <body> -->
<!--                 <div class="navigationBar"> -->
<!--                     <table id="headline"> -->
<!--                         <tr> -->
<!--                             <td> -->
<!--                                 <a href="http://www.urmel-dl.de/" target="_blank">UrMEL</a> -->
<!--                             </td> -->
<!--                             <td>|</td> -->
<!--                             <td> -->
<!--                                 <a href="http://zs.thulb.uni-jena.de/content/below/index.xml" target="_self">Journals@UrMEL</a> -->
<!--                             </td> -->
<!--                         </tr> -->
<!--                     </table> -->
                    
                    <!-- top rigth menu with Sitemap, Login etc. -->
<!--                     <div id="navigation_box"> -->
<!--                         <xsl:call-template name="navigation.row" /> -->
<!--                     </div> -->
                    <!-- the img is necessary because min-width is not supported by IE6-->
<!--                     <img src="../../../images/emtyDot1Pix.gif" width="1024px" height="0px" border="0" /> -->
<!--                 </div> -->
                
                <!-- the second bar with image and logo for each journal -->
<!--                 <div class="bannerBar"> -->
<!--                     the images for logo and banner are defined in style_general.css -->
<!--                     <div id="mainBanner" /> -->
<!--                     the right logo -->
<!--                     <div id="transparent-logo" /> -->
<!--                     the img is necessary because min-width is not supported by IE6 -->
<!--                     <img src="../../../images/emtyDot1Pix.gif" width="1024px" height="0px" border="0" /> -->
<!--                 </div> -->
<!--                 <div class="mainLayer"> -->
<!--                     <div id="navi_history"> -->
<!--                         <div class="line"> -->
<!--                             <xsl:call-template name="navigation.history" /> -->
<!--                         </div> -->
<!--                     </div> -->
<!--                     <div class="titleString"> -->
<!--                         <table> -->
<!--                             <tr> -->
<!--                                 <td class="decoration"> -->
<!--                                     <div class="ornamentUp" /> -->
<!--                                 </td> -->
<!--                                 <td class="title"> -->
<!--                                     <xsl:value-of select="$journalMaintitle" /> -->
<!--                                 </td> -->
<!--                             </tr> -->
<!--                         </table> -->
<!--                     </div> -->
<!--                     <div class="naviContent"> -->
<!--                         <div class="naviColumn"> -->
<!--                             <div class="ornamentDown" /> -->
<!--                             <div id="div_navi_main"> -->
<!--                                 <xsl:call-template name="navigation.tree" /> -->
<!--                             </div> -->
<!--                             <div class="sponsorLogo" usemap="#logos"> -->
<!--                                 <img src="../../../images/emtyDot1Pix.gif" width="112px" height="125px" border="0" usemap="#logos"> -->
<!--                                     <map name="logos"> -->
<!--                                         <area target="_blank" shape="rect" coords="0,0,112,46" href="http://www.dfg.de/" alt="DFG" title="DFG" /> -->
<!--                                         <area target="_blank" shape="rect" coords="0,46,112,73" href="http://www.thulb.uni-jena.de/" alt="ThULB" title="ThULB" /> -->
<!--                                         <area target="_blank" shape="rect" coords="0,73,112,96" -->
<!--                                             href="http://www.archive-in-thueringen.de/index.php?major=archiv&amp;action=detail&amp;object=archiv&amp;id=29" alt="Stadtarchiv Altenburg" -->
<!--                                             title="Stadtarchiv Altenburg" /> -->
<!--                                         <area target="_blank" shape="rect" coords="0,96,112,125" href="http://www.presseforschung.uni-bremen.de/" alt="Deutsche Presseforschung Bremen" -->
<!--                                             title="Deutsche Presseforschung Bremen" /> -->
<!--                                     </map> -->
<!--                                 </img> -->
<!--                             </div> -->
<!--                         </div> -->
<!--                         <div id="contentArea"> -->
<!--                             <div id="contentWrapper"> -->
<!--                                 <xsl:call-template name="template_calendar.write.content" /> -->
<!--                             </div> -->
<!--                         </div> -->
<!--                         <div class="footer" /> -->
<!--                     </div> -->
<!--                 </div> -->
                <!-- the img is necessary because min-width is not supported by IE6-->
<!--                 <img src="../../../images/emtyDot1Pix.gif" width="1024px" height="0px" border="0" /> -->
<!--             </body> -->
<!--         </html> -->
<!--     </xsl:template> -->

    <!-- Template for Content ================================================================================== -->
<!--     <xsl:template name="template_calendar.write.content"> -->
<!--         <xsl:call-template name="jp.layout.getHTMLContent" /> -->
<!--     </xsl:template> -->

    <!-- Template for User info ================================================================================ -->
<!--     <xsl:template name="template_calendar.userInfo"> -->

        <!-- BEGIN: login values -->
<!--         <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL" -->
<!--             select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" /> -->
        <!-- END OF: login values -->
<!--         <table class="login_window" border="0" cellspacing="0" cellpadding="0"> -->
<!--             <tr> -->
<!--                 <td class="login_space"></td> -->
<!--                 <td class="login_window"> -->
                    <!-- Login-Button / 2 Pfeile =================================== -->
<!--                     <a href="{$LoginURL}"> -->
<!--                         <img src="{$WebApplicationBaseURL}templates/master/template_calendar/IMAGES/login-switch.gif" border="0" /> -->
<!--                     </a> -->
<!--                 </td> -->
<!--                 <td class="login_text"> -->
<!--                     <text i18n="editor.start.LoginText.label" /> -->
<!--                     : -->
<!--                 </td> -->
<!--                 <td class="user_id"> -->
<!--                     <p class="whitebox"> -->
<!--                         <xsl:value-of select="$CurrentUser" /> -->
<!--                     </p> -->
<!--                 </td> -->
<!--             </tr> -->
<!--         </table> -->
<!--     </xsl:template> -->
<!-- </xsl:stylesheet> -->