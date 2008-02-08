<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
	exclude-result-prefixes="xlink i18n">
	<!-- ============================================== -->
	<!-- the template                                   -->
	<!-- ============================================== -->
	<xsl:template name="template_jstzfbb">
		<html>
			<head>
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
				<link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_navigation.css"
					rel="stylesheet" type="text/css"/>
				<link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_content.css" rel="stylesheet"
					type="text/css"/>
				<link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet"
					type="text/css"/>
				<link href="{$WebApplicationBaseURL}/common.css" rel="stylesheet" type="text/css"/>
				<script language="JavaScript"
					src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js"
					type="text/javascript"/>
				<script language="JavaScript"
					src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js"
					type="text/javascript"/>
				<!--<xsl:call-template name="module-broadcasting.getHeader"/>-->
			</head>
			<body>
				<table cellspacing="0" cellpadding="0"
					style="width: max; height: 100%; border:1px; border-style:none solid none solid; border-color:#000000; margin: 0px 15px; background-color: #dbdfe7; padding:0px;">
					<tr>
						<td colspan="4" bgcolor="#DBDFE7" height="10"
							style="padding:0px; margin:0px; border:0px; border-style:none"> <img
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/placeholder.gif"
								alt="placeholder" border="0"/>
						</td>
					</tr>
					<tr>
						<td width="20px" bgcolor="#DBDFE7"
							style="border-bottom:1px solid; border-bottom-color:#2D4F97; padding:0px; margin:0px"> <img
								width="20px"
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/placeholder.gif"
								border="0"/>
						</td>
						<td width="217px" valign="top" bgcolor="#2F4E9E">
							<xsl:call-template name="template_jstzfbb.userInfo"/>
						</td>
						<td>
							<table cellspacing="0" cellpadding="0">
								<tr>
									<td width="100%"
										style="border-bottom:1px solid; border-bottom-color:#2D4F97; padding:0px; margin:0px">
											<img
											src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/placeholder.gif"
											border="0"/>
									</td>
									<td align="right">
										<table style="width: 380px; padding:0px; border:0px; margin:0px; border-collapse:collapse">
											<tr>
												<td style="border:none; padding:0px; margin:0px"> <img width="30px"
														src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/table_schraeg1.gif"
														style="border:none; float:left"/>
												</td>
												<td
													style="border-top:1px solid; border-top-color:#2D4F97; padding:0px; margin:0px">
													<xsl:call-template name="navigation.row">
														<xsl:with-param name="rootNode"
															select="document($navigationBase) /navigation/navi-below"/>
														<xsl:with-param name="CSSLayoutClass" select="'navi_below'"/>
														<xsl:with-param name="menuPointHeigth" select="'24'"/>
														<!-- use pixel values -->
														<xsl:with-param name="spaceBetweenLinks" select="'18'"/>
														<!-- use pixel values -->
													</xsl:call-template>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td width="20px" bgcolor="#DBDFE7" height="110" style="border-top:3px solid; border-color:#DBDFE7"
							background="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/bg_table_td3.jpg"
							valign="top"> <img
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/placeholder.gif"
								border="0"/>
						</td>
						<td width="217px" rowspan="3" valign="top" bgcolor="#2F4E9E" style="padding-top:6px"> <a>
							<xsl:attribute name="href">
								<xsl:call-template name="UrlAddSession">
									<xsl:with-param name="url"
										select="concat($WebApplicationBaseURL,'content/below/index.xml')"/>
								</xsl:call-template>
							</xsl:attribute> <img
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/logo1.gif" alt="Logo"
								border="0"/> </a>
						</td>
						<td colspan="2" bgcolor="#DBDFE7" style="width: 100%; border-top:3px solid; border-color:#DBDFE7"
							background="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/bg_table1.jpg"
							valign="top"> <img
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/zfbblogo.gif" alt="Logo"
								id="logo" border="0" align="right"/>
						</td>
					</tr>
					<tr>
						<td width="20px" bgcolor="#BDCBDD" height="25" style="padding:0px; margin:0px"> <img width="20px"
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/placeholder.gif"
								border="0"/>
						</td>
						<td colspan="2" class="navi_history_row">
							<xsl:call-template name="navigation.history"/>
						</td>
					</tr>
					<tr>
						<td width="20px" bgcolor="#DBDFE7" height="20" style="padding:0px; margin:0px"> <img width="20px"
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/placeholder.gif"
								border="0"/>
						</td>
						<td colspan="2" bgcolor="#DBDFE7"> <img
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/placeholder.gif"
								alt="placeholder" border="0"/>
						</td>
					</tr>
					<!-- <tr>
					<td width="20px" bgcolor="#DBDFE7" 
					style="border-top:1px solid; border-top-color:#2D4F97; padding:0px; margin:0px">
					<img 
					src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/placeholder.gif" 
					border="0"/>
					</td>
					<td height="20" width="100%" colspan="3" 
					style="border-top:1px solid; border-top-color:#2D4F97; padding:0px; margin:0px">
					<img 
					src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/placeholder.gif" 
					border="0"/>
					</td>-->
					<!--<td>
					<table width="100%" style="padding:0px; border:0px; margin:0px; border-collapse:collapse">
					<tr>
					<td style="border:none; padding:0px; margin:0px">
					<img width="24px" src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/table_schraeg2.jpg" style="border:none; float:left" />
					</td>
					<td align="left" width="100%" style="border-bottom-color:#2D4F97; border-bottom:1px solid; padding:0px; margin:0px">
					<img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/placeholder.gif" border="0"/>
					</td>
					</tr>
					</table>
					</td>
					</tr>-->
					<!--<tr>
					<td width="20" bgcolor="#DBDFE7"></td>
					<td height="25" bgcolor="#2F4E9E" width="217"></td>
					<td>&#160;</td>
					<td>&#160;</td>
					<td>&#160;</td>
					</tr>-->
					<tr>
						<td width="20" bgcolor="#DBDFE7">&#160;
						</td>
						<td width="217" valign="top">
							<div class="navi_main">
								<xsl:call-template name="Navigation_main"/>
							</div>
						</td>
						<td colspan="2" valign="top" id="contentArea">
							<!-- IE Fix: contentWrapper needed :o( -->
							<div id="contentWrapper">
								<xsl:call-template name="getFastWCMS"/>
								<xsl:call-template name="template_jstzfbb.write.content"/>
							</div>
						</td>
					</tr>
					<tr>
						<td colspan="4" bgcolor="#DBDFE7" height="40"></td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
	<!-- ======================================================================================================== -->
	<xsl:template name="template_jstzfbb.write.content">
		
		<xsl:choose>
			<xsl:when test=" $PageTitle = 'Inhaltsverzeichnis - ' ">
				<xsl:variable name="issue">
					<xsl:call-template name="printClass">
						<xsl:with-param name="recursive" select="'true'"/>
						<xsl:with-param name="nodes"
							select="/mcr_results/mcr_result[1]/mycoreobject/metadata/timebehaviours/timebehaviour"/>
						<xsl:with-param name="host" select="'local'"/>
					</xsl:call-template>
				</xsl:variable>
				
				<div class="headline">
					<xsl:value-of select="concat($PageTitle,$issue)"/>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div class="headline">
					<xsl:copy-of select="$PageTitle"/>
				</div>
			</xsl:otherwise>
		</xsl:choose>
		
		<xsl:apply-templates/>
		
	</xsl:template>
	<!-- ======================================================================================================== -->
	<xsl:template name="template_jstzfbb.userInfo">
		<!-- BEGIN: login values -->
		<xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
			select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )"/>
		<!-- END OF: login values -->
		<table cellspacing="0" cellpadding="0" class="user">
			<tr>
				<td class="user_title">
					<xsl:value-of select="i18n:translate('editor.start.LoginText.label')"/>: </td>
				<td class="user_currentUser">
					<a href="{$LoginURL}">
						<xsl:value-of select="$CurrentUser"/>
					</a>
				</td>
				<td class="user_changeButton">
					<a href="{$LoginURL}">
						<img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/loginDarts.gif" border="0"/>
					</a>
				</td>
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>