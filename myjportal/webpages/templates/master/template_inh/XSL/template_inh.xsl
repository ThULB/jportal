<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink">
	
	<!-- ============================================== -->
	<!-- the template                                   -->
	<!-- ============================================== -->
	<xsl:template name="template_inh">
		<html>
			<head>
				<meta http-equiv="content-type"
					content="text/html;charset=UTF-8"/>
				<title>
					
					<xsl:call-template name="PageTitle"/>
				</title>
				<link
					href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_general.css"
					rel="stylesheet" type="text/css"/>
				<link
					href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_content.css"
					rel="stylesheet" type="text/css"/>
				<link
					href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_navigation.css"
					rel="stylesheet" type="text/css"/>
				<link
					href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css"
					rel="stylesheet" type="text/css"/>
				<link href="{$WebApplicationBaseURL}/common.css" rel="stylesheet"
					type="text/css"/>
				<script language="JavaScript"
					src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js"
					type="text/javascript"/>
				<script language="JavaScript"
					src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js"
					type="text/javascript"/>
				<xsl:call-template name="module-broadcasting.getHeader"/>				
			</head>
			<body>
				
				<div id="footer1">
					<div id="border">
						<div id="navigation">
							<xsl:call-template name="navigation.row">
								<xsl:with-param name="rootNode"
									select="document($navigationBase) /navigation/navi-below"/>
								<xsl:with-param name="CSSLayoutClass"
									select="'navi_below'"/>
								<xsl:with-param name="menuPointHeigth"
									select="'21'"/>
								<!-- use pixel values -->
								<xsl:with-param name="spaceBetweenLinks"
									select="'12'"/>
								<!-- use pixel values -->
							</xsl:call-template>
						</div>
					</div>
					<div id="banner">
						<div id="banner_left"
							style="	background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_top_left.gif) no-repeat;">
							<div id="login_div">
								<xsl:call-template name="template_inh.userInfo"/>
							</div>
						</div>
						<div id="banner_right">
							<map name="uni_ilmenau">
								<area shape="rect" coords="8,5,243,140"
									href="http://zs.thulb.uni-jena.de/content/main/journals/iun.xml?XSL.lastPage.SESSION=/content/main/journals/iun.xml"
									alt="Ilmenauer Uni-Nachrichten"/>
								<area shape="rect" coords="252,5,489,140"
									href="http://zs.thulb.uni-jena.de/content/main/journals/ihb.xml?XSL.lastPage.SESSION=/content/main/journals/ihb.xml"
									alt="Ilmenauer Hochschulblatt"/>
								<area shape="rect" coords="498,5,735,140"
									href="http://zs.thulb.uni-jena.de/content/main/journals/inh.xml?XSL.lastPage.SESSION=/content/main/journals/inh.xml"
									alt="Neue Hochschule"/>
							</map> <img
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/banner_top_right.gif"
								width="735" height="140" alt="Universität Ilmenau" usemap="#uni_ilmenau" border="0"/>
						</div>
					</div>
					<div id="navi_all">
						<div id="div_navi_main">
							<xsl:call-template name="Navigation_main"/>
						</div>
						<div id="navi_under"
							style="	background : url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/navi_below.gif) repeat-y;">
						</div>
					</div>
					<div id="navi_history">
						<xsl:call-template name="navigation.history"/>
					</div>
					<br/>
					<div id="contentArea">
						<xsl:call-template name="template_inh.write.content"/>
					</div>
				</div>
			</body>
		</html>
		
	</xsl:template>
	
	
	<!-- Template for Content ================================================================================== -->
	<xsl:template name="template_inh.write.content">
		<div class="headline">
			<xsl:call-template name="getFastWCMS" />
			<xsl:copy-of select="$PageTitle"/>
		</div>
		<xsl:apply-templates/>
	</xsl:template>
	
	
	<!-- Template for User info ================================================================================ -->
	<xsl:template name="template_inh.userInfo">
		
		<!-- BEGIN: login values -->
		<xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
			select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )"/>
		<!-- END OF: login values -->
		<table class="login_window" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td class="login_text">
					<text i18n="editor.start.LoginText.label"/>:
				</td>
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