<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink"
	xmlns:encoder="xalan://java.net.URLEncoder" >
	
	<!-- any XML elements defined here will go into the head -->
	<!-- other stylesheets may override this variable -->
	<xsl:variable name="head.additional"/>
	
	<!-- ============================================== -->
	<!-- the template                                   -->
	<!-- ============================================== -->
	<xsl:template name="template_pamietnik">
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
				<link
					href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_general.css"
					rel="stylesheet" type="text/css"/>
				<link
					href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_navigation.css"
					rel="stylesheet" type="text/css"/>
				<link
					href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_content.css"
					rel="stylesheet" type="text/css"/>
				<link
					href="{$WebApplicationBaseURL}common.css"
					rel="stylesheet" type="text/css"/>
				<link
					href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css"
					rel="stylesheet" type="text/css"/>
				<script language="JavaScript"
					src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js"
					type="text/javascript"/>
				<script language="JavaScript"
					src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js"
					type="text/javascript"/>
				<xsl:copy-of select="$head.additional"/>
                <xsl:call-template name="module-broadcasting.getHeader"/>	
			</head>
			
			<body>
				<table id="maintable" cellspacing="0" cellpadding="0">
					<tr class="max">
						<!-- general column left -->
						<td id="mainLeftColumn"> <a>
							<xsl:attribute name="href">
								<xsl:call-template name="UrlAddSession">
									<xsl:with-param name="url"
										select="concat($WebApplicationBaseURL,'content/below/index.xml')"/>
								</xsl:call-template>
							</xsl:attribute> <img
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/logo1.jpg"
								alt="Logo" id="logo"/></a>
							<table id="navi_table" cellspacing="0" cellpadding="0">
    							<tr>
    							  <td>
									  <div class="navi_history_user">    
							          <xsl:call-template name="template_pamietnik.userInfo"/>
							          </div>
								  </td>
      							  <td>
									<div class="user_change">
									<a>
							         <xsl:attribute name="href">
								     		 <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
			                                 select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,
											'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )"/>
										    <xsl:value-of select="$LoginURL"/>:
		                             </xsl:attribute><img
								     src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/changer.gif"
								    alt="Benutzer wechseln" id="changer"/></a>
									</div>	  	  
								  </td>
    							</tr>
							</table>	
							<div class="navi_main">
								<xsl:call-template name="Navigation_main"/>	
								<img
								src="{$WebApplicationBaseURL}templates/master/{$template}/IMAGES/project.gif"
								alt="Projektbeteiligte:" id="project"/>
							</div>
						</td>
						<!-- END OF: general column left -->
						
						<!-- BEGIN: general column right -->
						<td class="max autowidth">
							<table class="max" cellspacing="0" cellpadding="0" style="background: url({$WebApplicationBaseURL}templates/master/{$template}/IMAGES/logo2.jpg) no-repeat;">
								
								<!-- BEGIN: menu above -->
								<tr class="menueheight">
									<td id="navi_below_cell">
										<xsl:call-template name="navigation.row">
											<xsl:with-param name="rootNode"
												select="$loaded_navigation_xml/navi-below"/>
											<xsl:with-param name="CSSLayoutClass"
												select="'navi_below'"/>
											<xsl:with-param
												name="menuPointHeigth"
												select="'21'"/>
											<!-- use pixel values -->
											<xsl:with-param
												name="spaceBetweenLinks"
												select="'12'"/>
											<!-- use pixel values -->
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
													<xsl:call-template
														name="navigation.history"/>
												</td>
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
											<xsl:call-template name="getFastWCMS" />
											<xsl:call-template
												name="template_pamietnik.write.content"/>
										</div>
									</td>
								</tr>
								<!-- END OF: content area -->
								
								<!-- footer right -->
								<tr class="minheight">
									<td id="footer">
										<xsl:call-template name="footer"/>
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
	<xsl:template name="template_pamietnik.write.content">
		<div class="headline">
			<xsl:copy-of select="$PageTitle"/>
		</div>
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- ======================================================================================================== -->
	<xsl:template name="template_pamietnik.userInfo">
		
		<!-- BEGIN: login values -->
		<xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
			select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )"/>
		<!-- END OF: login values -->
		
        <text i18n="editor.start.LoginText.label"/>:       
		<a href="{$LoginURL}">
			<xsl:value-of select="$CurrentUser"/>
		</a>
		</xsl:template>
	
</xsl:stylesheet>