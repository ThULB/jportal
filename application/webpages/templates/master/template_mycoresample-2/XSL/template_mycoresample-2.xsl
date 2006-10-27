<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xlink">

<!-- any XML elements defined here will go into the head -->
<!-- other stylesheets may override this variable -->
<xsl:variable name="head.additional" /> 

<!-- ============================================== -->
<!-- the template                                   -->
<!-- ============================================== -->
<xsl:template name="template_mycoresample-2">
  <html>
      <head>
            <title>
                  <xsl:call-template name="PageTitle"/>
            </title>
            <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_general.css" 
                  rel="stylesheet" type="text/css"/>
            <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_navigation.css" 
                  rel="stylesheet" type="text/css"/>
            <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_content.css" 
                  rel="stylesheet" type="text/css"/>
		    <link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" 
				  rel="stylesheet" type="text/css"/>
            <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js"
				  type="text/javascript"/>
		    <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js"
				  type="text/javascript"/>
            <xsl:copy-of select="$head.additional" />
      </head>

    <body>
      <table id="maintable" cellspacing="0" cellpadding="0">
      <tr class="max">
      <!-- general column left -->
      <td id="mainLeftColumn">
        <a>
            <xsl:attribute name="href">
                <xsl:call-template name="UrlAddSession">
                    <xsl:with-param name="url" select="concat($WebApplicationBaseURL,'content/below/index.xml')"/>
                </xsl:call-template>
            </xsl:attribute>
            <img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/logo.gif" alt="Logo" id="logo"/></a>
        <div class="navi_main"><xsl:call-template name="Navigation_main"/></div>
      </td>
      <!-- END OF: general column left -->

      <!-- BEGIN: general column right -->
      <td class="max autowidth">
        <table class="max" cellspacing="0" cellpadding="0">

          <!-- BEGIN: menu above -->
          <tr class="minheight">
            <td id="navi_below_cell">
               <xsl:call-template name="navigation.row">
                  <xsl:with-param name="rootNode" select="$loaded_navigation_xml/navi-below" />
                  <xsl:with-param name="CSSLayoutClass" select="'navi_below'"/>
                  <xsl:with-param name="menuPointHeigth" select="'21'" /> <!-- use pixel values -->
                  <xsl:with-param name="spaceBetweenLinks" select="'12'" />  <!-- use pixel values -->
               </xsl:call-template>
            </td>
          </tr>
          <!-- END OF: menu above -->

          <!-- BEGIN: history navigation area -->
          <tr class="minheight">
            <td>
              <table class="navi_history">
                <tr>
                  <td class="navi_history">
                    <xsl:call-template name="navigation.history" />
                  </td>
                  <td class="navi_history_user"><xsl:call-template name="template_mycoresample-2.userInfo"/></td>
                </tr>
              </table>
            </td>
          </tr>
          <!-- END OF: history navigation area -->

          <!-- BEGIN content area -->
          <tr>
            <td id="contentArea">
              <!-- IE Fix: contentWrapper needed :o( -->
              <div id="contentWrapper">
                 <xsl:call-template name="template_mycoresample-2.write.content"/>
              </div>
            </td>
          </tr>
          <!-- END OF: content area -->

          <!-- footer right -->
          <tr class="minheight">
            <td id="footer" >
              <xsl:call-template name="footer" />
            </td>
          </tr>
          <!-- END OF: footer right -->

        </table>
      </td>

    <!-- END OF: general column right -->
    </tr>
      </table>
    </body>
  </html>
</xsl:template>
<!-- ======================================================================================================== -->
<xsl:template name="template_mycoresample-2.write.content">
   <div class="headline">
           <xsl:value-of select="$PageTitle"/>
   </div>
	
   <xsl:call-template name="getFastWCMS" />		
	
   <xsl:apply-templates/>
</xsl:template>
<!-- ======================================================================================================== -->	
<xsl:template name="template_mycoresample-2.userInfo">

  <!-- BEGIN: login values -->
  <xsl:variable 
    xmlns:encoder="xalan://java.net.URLEncoder" 
    name="LoginURL" 
    select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" 
/> 
  <!-- END OF: login values -->

        <xsl:value-of select="concat(i18n:translate('users.user'),': ')" />
        <a href="{$LoginURL}">
        <xsl:value-of select="$CurrentUser" />
        </a>

</xsl:template>

</xsl:stylesheet>
