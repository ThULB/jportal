<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xlink xalan">
	
	<xsl:param name="lastpage" />
	<xsl:param name="basedir" />	
	<xsl:variable name="loaded_navigation_xml" >
		<xsl:copy-of select="document(concat($basedir,'/build/webapps/config/navigation.xml'))" />
	</xsl:variable>
	
	<!-- =================================================================================================== -->	
	<xsl:template match="/root">
		
		<xsl:variable name="template_tmp">
			<!-- point to rigth item -->
			<xsl:for-each select="xalan:nodeset($loaded_navigation_xml)/navigation//item[@href = $lastpage]">
				<!-- collect @template !='' entries along the choosen axis -->
				<xsl:if test="position()=last()">
					<xsl:for-each select="ancestor-or-self::*[@template!='']">
						<xsl:if test="position()=last()">
							<xsl:value-of select="@href"/>
						</xsl:if>
					</xsl:for-each>
				</xsl:if>
				<!-- END OF: collect @template !='' entries along the choosen axis -->
			</xsl:for-each>
			<!-- END OF: point to rigth item -->
		</xsl:variable>
		
		<xsl:choose>
			<!-- assign appropriate template -->
			<xsl:when test="$template_tmp != ''">
				<webSiteContext>
					<xsl:value-of select="$template_tmp"/>
				</webSiteContext>
			</xsl:when>
			<!-- default template -->
			<xsl:otherwise>
				<webSiteContext>root</webSiteContext>				
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	<!-- =================================================================================================== -->	
</xsl:stylesheet>