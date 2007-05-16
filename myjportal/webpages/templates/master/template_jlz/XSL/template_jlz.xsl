<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	exclude-result-prefixes="xlink">
	
	<!-- ============================================== -->
	<!-- the template                                   -->
	<!-- ============================================== -->
	<xsl:template name="template_jlz">
		<html>
			<head>
				
				<!--<meta http-equiv="content-type" content="text/html;charset=UTF-8"/>-->
				<title>
					<xsl:call-template name="PageTitle"/>
				</title>
				<link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_general.css" rel="stylesheet"
					type="text/css"/>
				<link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_content.css" rel="stylesheet"
					type="text/css"/>
				<link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_navigation.css"
					rel="stylesheet" type="text/css"/>
				<link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet"
					type="text/css"/>
				<link href="{$WebApplicationBaseURL}/common.css" rel="stylesheet" type="text/css"/>
				<script language="JavaScript"
					src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js"
					type="text/javascript"/>
				<script language="JavaScript"
					src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js"
					type="text/javascript"/>
				<xsl:call-template name="module-broadcasting.getHeader"/>
			</head>
			
			<body>
				
				<!-- Begin: Big fat Maintable =========================================================================== -->
				<table class="main" border="0" cellspacing="0" cellpadding="0">
					
					<!-- Begin: Top margin and define column ================================================== -->
					<tr>
						<td class="left_margin">&#160;
						</td>
						<td class="menu_column">
							<p/>
						</td>
						<td class="conten_column">
							<p/>
						</td>
					</tr>
					<!-- End: Top margin and define column ==================================================== -->
					
					<!-- Begin: Login-row and Navigation-row ================================================== -->
					<tr>
						<td class="login_navi_row"/>
						<!-- Begin: Login ===================================================================== -->
						<td rowspan="3" class="login_row">
							<xsl:call-template name="template_jlz.userInfo"/>
						</td>
						<!-- End: Login ======================================================================= -->
						
						<!-- Begin: Navigation ================================================================ -->
						<td id="navi_below_cell">
							<xsl:call-template name="navigation.row">
								<!--xsl:with-param name="rootNode" select="$loaded_navigation_xml/navi-below" /-->
								<xsl:with-param name="rootNode"
									select="document($navigationBase) /navigation/navi-below"/>
								<xsl:with-param name="CSSLayoutClass" select="'navi_below'"/>
								<xsl:with-param name="menuPointHeigth" select="'21'"/> <!-- use pixel values -->
								<xsl:with-param name="spaceBetweenLinks" select="'12'"/> <!-- use pixel values -->
							</xsl:call-template>
						</td>
						<!-- End: Navigation =================================================================== -->
					</tr>
					<!-- End: Login-row and Navigation-row ===================================================== -->
					
					<!-- Begin: Decoration lines =============================================================== -->
					<tr>
						<!-- td class="decor_line">&#160;</td-->
						<td class="decor_line"></td>
						<td class="decor_line"></td>
						<td class="decor_line"></td>
						<!--td class="decor_line"/>
						<td class="decor_line"/-->
					</tr>
					<tr>
						<td class="decor_spc">&#160;
						</td>
						<td class="decor_spc">&#160;
						</td>
						<td class="decor_spc">&#160;
						</td>
						<!--td class="decor_spc"/>
						<td class="decor_spc"/-->
					</tr>
					<!-- End: Decoration lines ================================================================= -->
					
					<!-- Begin: Main Graphics ================================================================== -->
					<tr>
						<td class="balk1" colspan="2"> <img class="balk1"
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/balk1.gif" alt="balk1"/>
						</td>
						<td class="balk2"> <img class="balk2"
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/balk2.gif"/>
						</td>
					</tr>
					<!-- End: Main Graphics ==================================================================== -->
					
					<!-- Begin: Navigation history ============================================================= -->
					<tr>
						<td class="navi_history"/>
						
						<!-- Begin: Part of Main Graphics / rest of the book =================================== -->
						<td rowspan="4" class="balk_u"> <img class="balk_u"
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/balk_u.gif"/>
						</td>
						<!-- End: Part of Main Graphics / rest of the book ===================================== -->
						
						<td class="navi_history">
							<table class="navi_history" border="0" cellspacing="0" cellpadding="0">
								<tr>
									<td class="navi_history">
										<xsl:call-template name="navigation.history"/>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<!-- End: Navigation history =============================================================== -->
					
					<!-- Begin: Decoration lines =============================================================== -->
					<tr>
						<td class="decor_spc"/>
						<td class="decor_spc"/>
						<td class="decor_spc"/>
					</tr>
					<tr>
						<td class="decor_line"/>
						<td class="decor_line"/>
						<td class="decor_line"/>
					</tr>
					<tr>
						<td class="decor_big_spc"/>
						<td class="decor_big_spc"/>
						<td class="decor_big_spc"/>
					</tr>
					<!-- End: Decoration lines ================================================================= -->
					
					<!-- Begin: Main Menu / Content Area ======================================================= -->
					<tr>
						<td/>
						<!-- Begin: Main Menu  ================================================================= -->
						<!-- Anmerkung: Fuer ein aufgeklapptes Menu wird eine Grafik in form eines "L" verwendet,
						das irgendwo im System liegt. Evtl. kann man dieses "L" durch ein Unicode
						Derivat ersetzen -->
						<td class="navi_main">
							<div class="navi_main">
								<table border="0" cellspacing="0" cellpadding="0">
									<tr>
										<td>
											<xsl:call-template name="Navigation_main"/>
										</td>
									</tr>
									<tr>
										<td> <img class="jahr"
												src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/jahr.gif"
												alt="Seit 1874 bis 1978"/>
										</td>
									</tr>
									<tr>
										<td class="empty_box"/>
									</tr>
								</table>
							</div>
						</td>
						<!-- End: Main Menu  =================================================================== -->
						
						<!-- Begin: Content Area  ============================================================== -->
						<td id="contentArea">
							<div id="contentWrapper">
								<xsl:call-template name="getFastWCMS"/>
								<xsl:call-template name="template_jlz.write.content"/>
							</div>
						</td>
						<!-- End: Content Area  ================================================================ -->
					</tr>
					<!-- End: Main Menu / Content Area ========================================================= -->
					
					<!-- Begin: Thulb-Logo ===================================================================== -->
					<tr>
						<td/>
						<td align="center">
							<div class="logo"> <img class="logo"
									src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/logo.gif"
									alt="Thulb-Logo"/>
							</div>
						</td>
					</tr>
					<!-- End: Thulb-Logo ======================================================================= -->
				</table>
				
				<!-- End: Big fat Maintable ============================================================================= -->
			</body>
		</html>
		
	</xsl:template>
	
	<!-- Template for Content ================================================================================== -->
	<xsl:template name="template_jlz.write.content">
		<div class="headline">
			<xsl:copy-of select="$PageTitle"/>
		</div>
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- Template for User info ================================================================================ -->
	<xsl:template name="template_jlz.userInfo">
		
		<!-- BEGIN: login values -->
		<xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
			select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )"/>
		<!-- END OF: login values -->
		<table class="login_window" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td class="login_text">
					<text i18n="editor.start.LoginText.label"/>: </td>
				<td class="user_id">
					<p class="whitebox">
						<xsl:value-of select="$CurrentUser"/>
					</p>
				</td>
				<td class="login_window">
					<!-- Login-Button / 2 Pfeile =================================== -->
					<a href="{$LoginURL}">
						<div class="buttons">&#x25B6;
							<br/>&#160;&#x25C0;</div>
					</a>
				</td>
			</tr>
		</table>
		
	</xsl:template>
	
</xsl:stylesheet>

<!-- ************************************************ -->
<!-- altes template_mycoresample_2.... ??
<tr class="max">
<!- - general column left - ->
<td id="mainLeftColumn">
<a>
<xsl:attribute name="href">
<xsl:call-template name="UrlAddSession">
<xsl:with-param name="url" select="concat($WebApplicationBaseURL,'content/below/index.xml')"/>
</xsl:call-template>
</xsl:attribute>
<img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/buch.gif" alt="Logo" id="buch"/></a>
<div class="navi_main"><xsl:call-template name="Navigation_main"/></div>
</td>
<!- - END OF: general column left - ->

<!- - BEGIN: general column right - ->
<td class="max autowidth">
<table class="max" cellspacing="0" cellpadding="0">

<!- - BEGIN: menu above - ->
<tr class="minheight">
<td id="navi_below_cell">
<xsl:call-template name="NavigationRow">
<xsl:with-param name="rootNode" select="$loaded_navigation_xml/navi-below" />
<xsl:with-param name="CSSLayoutClass" select="'navi_below'"/>
<xsl:with-param name="menuPointHeigth" select="'21'" /> <!- - use pixel values - ->
<xsl:with-param name="spaceBetweenLinks" select="'12'" />  <!- - use pixel values - ->
</xsl:call-template>
</td>
</tr>
<!- - END OF: menu above - ->

<!- - BEGIN: history navigation area - ->
<tr class="minheight">
<td>
<table class="navi_history">
<tr>
<td class="navi_history">
<xsl:call-template name="navigation.history" />
</td>
<td class="navi_history_user"><xsl:call-template name="template_jlz.userInfo"/></td>
</tr>
</table>
</td>
</tr>
<!- - END OF: history navigation area - ->

<!- - BEGIN content area - ->
<tr>
<td id="contentArea">
<!- - IE Fix: contentWrapper needed :o( - ->
<div id="contentWrapper">
<xsl:call-template name="template_jlz.write.content"/>
</div>
</td>
</tr>
<!- - END OF: content area - ->

<!- - footer right - ->
<tr class="minheight">
<td id="footer" >
<xsl:call-template name="footer" />
</td>
</tr>
<!- - END OF: footer right - ->

</table>
</td>

<!- - END OF: general column right - ->
</tr>
-->