<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
	exclude-result-prefixes="xlink i18n">
	
	<!-- ============================================== -->
	<!-- the template                                   -->
	<!-- ============================================== -->
	<xsl:template name="template_suz">
		<html>
			<head>
				<meta http-equiv="content-type" content="text/html;charset=UTF-8"/>
    <title>
	<xsl:call-template name="HTMLPageTitle"/>
    </title>
    <meta content="Zeitschriften-Portal" lang="de" name="description"/>
    <meta content="Journal-Portal" lang="en" name="description"/>
    <meta content="Zeitschriften,historisch,aktuell,Paper,Forschung,UrMEL,ThULB, FSU Jena,Langzeitarchivierung,Andreas Trappe" lang="de" name="keywords"/>
    <meta content="Journals,EJournals,historical,currently,paper,research,UrMEL,ThULB, FSU Jena,long term preservation,Andreas Trappe" lang="en" name="keywords"/>
    <meta content="MyCoRe" lang="de" name="generator"/>
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
				<div id="border">
					<div id="footer1">
						<!-- footer 1 fertig -->
						<div id="login_div"
							style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/bg_login.gif) no-repeat;">
							<xsl:call-template name="template_suz.userInfo"/>
						</div>
						<div id="navigation">
							<div id="navigation_box">
								<xsl:call-template name="navigation.row">
									<xsl:with-param name="rootNode"
										select="document($navigationBase) /navigation/navi-below"/>
									<xsl:with-param name="CSSLayoutClass" select="'navi_below'"/>
									<xsl:with-param name="menuPointHeigth" select="'21'"/>
									<!-- use pixel values -->
									<xsl:with-param name="spaceBetweenLinks" select="'12'"/>
									<!-- use pixel values -->
								</xsl:call-template>
							</div>
							<div id="thulb_logo"> <a href="http://www.thulb.uni-jena.de"><img
									src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb_logo.gif"
									border="0"></img></a>
							</div>
						</div>
						
					</div>
					
					<div id="banner"
						style="background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/suz_logo.gif) no-repeat;">
						<br></br>
					</div>
					
					<table id="footer2" border="0" cellspacing="0" cellpadding="0">
						<tr>
							<td id="footer2_left">
								<div id="div_navi_main"
									style="background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/suz_bg_nav2.gif); background-repeat: repeat-y;">
									<xsl:call-template name="Navigation_main"/>
								</div>
								<div id="footer_bottom"
									style="background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/suz_bg_nav3.gif); background-repeat: repeat-x;">
								</div>
							</td>
							<td valign="top" id="footer2_right">
								<div id="navi_history">
									<xsl:call-template name="navigation.history"/>
								</div>
								<br/>
								<div id="contentArea">
									<div id="contentWrapper">
										<xsl:call-template name="getFastWCMS"/>
										<xsl:call-template name="template_suz.write.content"/>
									</div>
								</div>
							</td>
						</tr>
					</table>
					
				</div>
			</body>
		</html>
		
	</xsl:template>
	
	<!-- Template for Content ================================================================================== -->
	<xsl:template name="template_suz.write.content">
		<div class="headline">
			<xsl:copy-of select="$PageTitle"/>
		</div>
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- Template for User info ================================================================================ -->
	<xsl:template name="template_suz.userInfo">
		
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
						<img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/login-switch.gif" border="0"/>
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