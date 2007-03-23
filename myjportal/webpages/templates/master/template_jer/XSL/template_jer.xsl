<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	exclude-result-prefixes="xlink">
	
	<!-- ============================================== -->
	<!-- the template                                   -->
	<!-- ============================================== -->
	<xsl:template name="template_jer">
		<html>
			<head>
				<meta http-equiv="content-type" content="text/html;charset=UTF-8"/>
				<title>
					<xsl:call-template name="PageTitle"/>
				</title>
				<link href="{$WebApplicationBaseURL}/common.css" rel="stylesheet" type="text/css"/>
				<link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_general.css" rel="stylesheet"
					type="text/css"/>
				<link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_content.css" rel="stylesheet"
					type="text/css"/>
				<link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_navigation.css"
					rel="stylesheet" type="text/css"/>
				<link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet"
					type="text/css"/>
				<script language="JavaScript"
					src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js"
					type="text/javascript"/>
				<script language="JavaScript"
					src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js"
					type="text/javascript"/>
			</head>
			<body>
						<table cellspacing="0" cellpadding="0" id="footer">
							<tr>
								<td
									style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jer_top_left.jpg) no-repeat;">
									<table cellspacing="0" cellpadding="0" width="400px">
										<tr>
											<td>
												<div id="max-planck-logo"> 
													<a href="http://www.bgc-jena.mpg.de" target="_blank">
														<img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/max-planck-logo.jpg" border="0"/>
													</a>
												</div>
											</td>
										</tr>
										<tr>
											<td>
												<div id="login_div">
													<div id="login_div_inside">
														<xsl:call-template name="template_jer.userInfo"/>
													</div>
												</div>
											</td>
										</tr>
									</table>
								</td>
								<td	width="600px" style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jer_top_middle.jpg); repeat x;">
								</td>
								<td
									style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jer_top_right.jpg) no-repeat;">
									<table cellspacing="0" cellpadding="0" width="641px">
										<tr>
											<td>
												<div id="fsu-logo"> 
													<a href="http://www.uni-jena.de" target="_blank">
													<img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/fsu-logo.jpg" border="0"/>
													</a>
												</div>
											</td>
										</tr>
										<tr>
											<td>
												<div id="thulb_logo"> 
													<a href="http://www.thulb.uni-jena.de" target="_blank">
														<img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb_logo.gif" border="0"/>
													</a>
												</div>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					<div id="second-line">
					<div id="div_navi_main"	style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_jer_left.jpg) no-repeat;">
						<br/>
						<xsl:call-template name="Navigation_main"/>
					</div>
					<div id="navi_box">
						<xsl:call-template name="navigation.row">
							<xsl:with-param name="rootNode" select="document($navigationBase) /navigation/navi-below"/>
							<xsl:with-param name="CSSLayoutClass" select="'navi_below'"/>
							<xsl:with-param name="menuPointHeigth" select="'21'"/>
							<!-- use pixel values -->
							<xsl:with-param name="spaceBetweenLinks" select="'12'"/>
							<!-- use pixel values -->
						</xsl:call-template>
					</div>
					<div id="navi_history">
						<div id="navi_history_inside">
							<xsl:call-template name="navigation.history"/>
						</div>
					</div>
					<div id="contentArea">
						<div id="contentWrapper">
							<xsl:call-template name="getFastWCMS"/>
							<xsl:call-template name="template_jer.write.content"/>
						</div>
					</div>
				</div>	
			</body>
		</html>
		
	</xsl:template>
	
	<!-- Template for Content ================================================================================== -->
	<xsl:template name="template_jer.write.content">
		<div class="headline">
			<xsl:copy-of select="$PageTitle"/>
		</div>
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- Template for User info ================================================================================ -->
	<xsl:template name="template_jer.userInfo">
		
		<!-- BEGIN: login values -->
		<xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
			select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )"/>
		<!-- END OF: login values -->
		<table class="login_window" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td class="login_space"></td>
				<td class="login_window">
					<!-- Login-Button / 2 Pfeile =================================== -->
					<a href="{$LoginURL}">
						<div class="buttons">&#x25B6;
							<br/>&#160;&#x25C0;</div>
					</a>
				</td>
				<td class="login_text">
					<text i18n="editor.start.LoginText.label"/>: </td>
				<td class="user_id">
					<p class="whitebox">
						<xsl:value-of select="$CurrentUser"/>
					</p>
				</td>
			</tr>
		</table>
		
	</xsl:template>
	
</xsl:stylesheet>