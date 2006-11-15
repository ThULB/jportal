<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xlink mcr i18n acl">
	
<!-- ===================================================================================================== -->	
	<xsl:template name="appendChild">
		<xsl:param name="accessedit"/>
		<xsl:param name="id"/>
		<xsl:param name="layout"/>
		<xsl:param name="label"/>
		<xsl:param name="imagePath"/>
		<xsl:param name="type" select="'dokument'"/>
		<xsl:param name="xmltempl"
			select="concat('&amp;_xml_structure%2Fparents%2Fparent%2F%40href=',$id)"/>
		<xsl:param name="label"/>
		
		<xsl:variable name="suffix">
			<xsl:if test="string-length($layout)>0">
				<xsl:value-of select="concat('&amp;layout=',$layout)"/>
			</xsl:if>
		</xsl:variable>
		<xsl:if test="acl:checkPermission($id,'writedb')">
			<tr>
				<td class="metaname">
					<xsl:value-of select="$label"/>: </td>
				<td class="metavalue">
					<a
						href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;step=author&amp;todo=wnewobj{$suffix}{$xmltempl}">
						<img src="{concat($WebApplicationBaseURL,$imagePath)}"
							title="{$label}"/>
					</a>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
<!-- ===================================================================================================== -->	
	<xsl:template name="getJPJournalAsXML" >
		<xsl:param name="jpjournalID"/>		
		<xsl:value-of select="document(concat('http:localhost:8291/receive/',$jpjournalID,''?XSL.Style=xml))" />
	</xsl:template>
</xsl:stylesheet>