<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.18 $ $Date: 2006/05/26 15:39:08 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" 
	exclude-result-prefixes="xlink" >
    <xsl:include href="MyCoReLayout.xsl" />
	<xsl:include href="editor.xsl" />
	<xsl:include href="workflow.xsl" />
	<xsl:include href="fileupload.xsl" />
	<xsl:variable name="PageTitle">
		<xsl:choose>
			<xsl:when test="/MyCoReWebPage/section[ lang($CurrentLang)]/@title != '' ">
				<xsl:value-of select="/MyCoReWebPage/section[lang($CurrentLang)]/@title"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="/MyCoReWebPage/section[lang($DefaultLang)]/@title"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="Servlet" select="'undefined'"/>
	<!-- =============================================================================== -->
	<xsl:template match="/MyCoReWebPage">
		<xsl:choose>
			<xsl:when test=" section[lang($CurrentLang)] != '' ">
				<xsl:apply-templates select="section[lang($CurrentLang)] | section[lang('all')]"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="section[lang($DefaultLang)]" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- =============================================================================== -->	
	<!-- - - - - - - - - Identity Transformation  - - - - - - - - - -->
	<xsl:template match='@*|node()'>
		<xsl:copy>
			<xsl:apply-templates select='@*|node()'/>
		</xsl:copy>
	</xsl:template>	
	<!-- =============================================================================== -->		
	<xsl:template match="section">
		<xsl:for-each select="node()">
			<xsl:apply-templates select="." />
		</xsl:for-each>
	</xsl:template>
	<!-- =============================================================================== -->		
</xsl:stylesheet>
