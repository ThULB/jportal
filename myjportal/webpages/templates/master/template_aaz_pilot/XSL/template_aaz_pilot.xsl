<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink">
	
	<!-- any XML elements defined here will go into the head -->
	<!-- other stylesheets may override this variable -->
	<xsl:variable name="head.additional"/>
	
	<!-- ============================================== -->
	<!-- Seiten-Layout                                   -->
	<!-- ============================================== -->
	<xsl:template name="template_aaz_pilot">
		<html>
			<head>
				<!--<meta http-equiv="content-type" content="text/html;charset=UTF-8"/>-->
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
				<xsl:copy-of select="$head.additional"/>
			</head>
			
			<body>			
				<div id="img_head" style="background:url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/2.png) no-repeat;">
					<div id="navi_top">
						<xsl:call-template name="navigation.row">
							<xsl:with-param name="rootNode" select="$loaded_navigation_xml/navi-below"/>
							<xsl:with-param name="CSSLayoutClass" select="'navi_below'"/>
							<xsl:with-param name="menuPointHeigth" select="'21'"/> <!-- use pixel values -->
							<xsl:with-param name="spaceBetweenLinks" select="'12'"/> <!-- use pixel values -->
						</xsl:call-template>
					</div>
				</div> <img id="img_body_fading" src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/3.gif"/>
				<table id="content-table" cellspacing="0" cellpadding="0" border="0">
					<tr>
					<td id="navi_left">

						<img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/Link1.gif" usemap="#Link" border="0">
							<map name="Link">
								<area shape="rect" coords="0,0,130,46" href="http://zs.thulb.uni-jena.de/content/main/journals/aaz.xml" />
							</map>
						</img> 

						<!--<a href="{$WebApplicationBaseURL}templates/master/template_aaz_pilot/XSL/template_aaz_pilot.xsl">aaz</a>
						<br />	
						<a href="{$WebApplicationBaseURL}templates/master/template_aaz_pilot/XSL/template_aaz_pilot_pilot.xsl">aaz_pilot</a>-->
						<xsl:call-template name="Navigation_main"/>
						<a href="http://www.thulb.uni-jena.de" target="_blank" id="thulb_logo">
							<img src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/thulb.png" />
						</a>
						<br/>
						<a href="http://www.thueringen.de/de/staatsarchive/rudolstadt/content.html" target="_blank" style="margin-left: 8px;">
							Staatsarchiv Rudolstadt
						</a>
					</td>
					<td id="content">
						<div style="width:98%;">
						<div id="navi_history">
							<div id="navi_history_inside">
								<xsl:call-template name="navigation.history"/>
							</div>
						</div>	
						<!--<xsl:choose>
						<xsl:when test="$readAccess='true'">-->
						<xsl:call-template name="template_aaz_pilot.write.content"/>
						<!--</xsl:when>
						<xsl:otherwise> verboten! </xsl:otherwise>
						</xsl:choose>-->
						</div>	
					</td>
					</tr>	
				</table>
			</body>
			
		</html>
		
	</xsl:template>
	<!-- ======================================================================================================== -->
	<xsl:template name="template_aaz_pilot.write.content">
		<div class="headline">
			<xsl:copy-of select="$PageTitle"/>
		</div>
		
		<xsl:call-template name="getFastWCMS"/>
		
		<xsl:apply-templates/>
	</xsl:template>
	
</xsl:stylesheet>
