<?xml version="1.0" encoding="ISO-8859-1"?>
	<!-- ============================================== -->
	<!--
		$Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $
	-->
	<!-- ============================================== -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
	exclude-result-prefixes="xlink i18n" xmlns:xalan="http://xml.apache.org/xalan">

	<!-- ============================================== -->
	<!-- the template                                   -->
	<!-- ============================================== -->
	<xsl:template name="template_addrBookTh">
		<xsl:param name="journalsMaintitle" select="'Zeitschriftenname'" />
		<xsl:param name="periodetitle" select="'Erscheinungszeitraum'" />
        <xsl:variable name="journalID">
          <xsl:value-of select="document('jportal_getJournalID:XPathDoesNotExist')/dummyRoot/hidden/@default" />
        </xsl:variable>
        <xsl:variable name="journalXML">
          <xsl:copy-of select="document(concat('mcrobject:',$journalID))" />
        </xsl:variable>
		<xsl:variable name="city">
			<xsl:value-of select="xalan:nodeset($journalXML)//hidden_genhiddenfield1" />
		</xsl:variable>
		<xsl:variable name="published">
			<xsl:value-of select="xalan:nodeset($journalXML)//date[@type='published']" />
		</xsl:variable>
		<xsl:variable name="published_from">
			<xsl:value-of select="xalan:nodeset($journalXML)//date[@type='published_from']" />
		</xsl:variable>
		<xsl:variable name="published_until">
			<xsl:value-of
				select="xalan:nodeset($journalXML)//date[@type='published_until']" />
		</xsl:variable>
		<xsl:variable name="pubYear">
			<xsl:choose>
				<xsl:when test="$published != ''">
					<xsl:value-of select="$published"/>
				</xsl:when>
				<xsl:when test="($published_from != '') and ($published_until != '')">
					<xsl:value-of select="concat($published_from, ' - ', $published_until)"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>
		<html>
			<head>
				<xsl:call-template name="jp.layout.getHTMLHeader">
					<xsl:with-param name="nameOfTemplate" select="'template_addrBookTh'" />
				</xsl:call-template>
			</head>
			<body>
				<!--
					<div id="time"> <xsl:copy-of select="$periodetitle" /> </div> <div
					id="journal-title"> <xsl:copy-of select="$journalsMaintitle" />
					</div>
				-->
				<!-- <div id="transparent-logo" /> -->
				<table width="100%" height="30px" border="0" cellspacing="0"
					cellpadding="0" style="background-color: #646466;">
					<tr valign="top">
						<td width="100%">
							<div id="whitespace"></div>
							<span id="headline">
								<a href="http://www.urmel-dl.de/" target="_blank">UrMEL</a>
								<xsl:copy-of select="'     |     '" />
								<a href="http://zs.thulb.uni-jena.de/content/below/index.xml"
									target="_self">Journals@UrMEL</a>
							</span>
						</td>
					</tr>
				</table>

				<!-- doesnt work 100% with firefox, no idea why (use opera :)) -->
				<div id="navigation_box">
					<xsl:call-template name="navigation.row" />
				</div>

				<div style="height: max; width: max;">
					<table width="100%" border="0" cellspacing="0" cellpadding="0"
						style="background-color:transparent;">
						<tr valign="top">
							<td width="860" height="147"
								style="background:url({$WebApplicationBaseURL}templates/master/template_addrBookTh/IMAGES/abt-logo.png) no-repeat; background-color: #5a5720">
								<div class="city">
									<xsl:value-of select="$city"/>
								</div>
								<div class="pubYear">
									<xsl:value-of select="$pubYear"/>
								</div>
							</td>
						</tr>
						<tr>
							<td>
								<table width="100%" border="0" cellspacing="0"
									cellpadding="0">
									<tr>
										<td width="220" height="58"
											style="background:url({$WebApplicationBaseURL}templates/master/template_addrBookTh/IMAGES/abt-navi-top.png) no-repeat;" />
										<td style="padding-left:35px; padding-right:35px;">
											<table width="100%" border="0" cellspacing="0"
												cellpadding="0">
												<tr>
													<td>
														<div id="navi_history">
															<xsl:call-template name="navigation.history" />
														</div>
													</td>
												</tr>
												<tr>
													<td height="5"
														style="background:url({$WebApplicationBaseURL}templates/master/template_addrBookTh/IMAGES/abt-line.png) repeat-x;" />
												</tr>
											</table>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
					<div class="naviContent">
						<div class="naviColumn">
							<div id="div_navi_main">
								<xsl:call-template name="Navigation_main" />
							</div>
							<!--
								are special logos for current used journal available ? -> if
								yes, get them
							-->
							<xsl:call-template name="template_logos.getLogos">
								<xsl:with-param name="journalsID" select="$journalID" />
							</xsl:call-template>
						</div>
						<div id="contentArea">
							<div id="contentWrapper">
								<xsl:call-template name="template_addrBookTh.write.content" />
							</div>
						</div>
					</div>
				</div>
			</body>
		</html>

	</xsl:template>

	<!--
		Template for Content
		==================================================================================
	-->
	<xsl:template name="template_addrBookTh.write.content">
		<xsl:call-template name="jp.layout.getHTMLContent" />
	</xsl:template>

	<!--
		Template for User info
		================================================================================
	-->
	<xsl:template name="template_addrBookTh.userInfo">

		<!-- BEGIN: login values -->
		<xsl:variable xmlns:encoder="xalan://java.net.URLEncoder"
			name="LoginURL"
			select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?lang=',$CurrentLang,'&amp;amp;url=', encoder:encode( string( $RequestURL ) ) )" />
		<!-- END OF: login values -->
		<table class="login_window" border="0" cellspacing="0"
			cellpadding="0">
			<tr>
				<td class="login_space"></td>
				<td class="login_window">
					<!-- Login-Button / 2 Pfeile =================================== -->
					<a href="{$LoginURL}">
						<img
							src="{$WebApplicationBaseURL}templates/master/template_addrBookTh/IMAGES/login-switch.gif"
							border="0" />
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