<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mcr="http://www.mycore.org/"
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
	<xsl:template name="getJPJournalAsXML">
		<xsl:param name="jpjournalID"/>
		<xsl:value-of
			select="document(concat('mcrobject:',$jpjournalID))/mycoreobject/metadata/hidden_pubTypesID/hidden_pubTypeID/text()"/>
	</xsl:template>
	
	<!-- ============================================================================================================================ -->
	<!--  <xsl:template name="appendChild">
	<xsl:param name="accessedit" />
	<xsl:param name="id" />
	<xsl:param name="layout" />
	<xsl:param name="label" />	  
	<xsl:param name="imagePath" />
	<xsl:param name="type" select="'dokument'" />
	<xsl:param name="xmltempl" select="concat('&amp;_xml_structure%2Fparents%2Fparent%2F%40href=',$id)" />
	<xsl:param name="label" />	  
	
	<xsl:variable name="suffix">
	<xsl:if test="string-length($layout)>0">
	<xsl:value-of select="concat('&amp;layout=',$layout)" />
	</xsl:if>
	</xsl:variable>
	<xsl:if test="acl:checkPermission($id,'writedb')">
	<tr>
	<td class="metaname">
	<xsl:value-of select="$label" />:
	</td>
	<td class="metavalue">
	<a
	href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;step=author&amp;todo=wnewobj{$suffix}{$xmltempl}">
	<img src="{concat($WebApplicationBaseURL,$imagePath)}" title="{$label}" />
	</a>
	</td>
	</tr>
	</xsl:if>
	</xsl:template>	-->
	
	<!-- ============================================================================================================================ -->
	
	<xsl:template name="get.params_dynamicClassis">
		
		<xsl:variable name="IDTypes">
			<xsl:value-of
				select="./metadata/hidden_pubTypesID/hidden_pubTypeID/text()"/>
		</xsl:variable>
		<xsl:variable name="IDRubrics">
			<xsl:value-of
				select="./metadata/hidden_rubricsID/hidden_rubricID/text()"/>
		</xsl:variable>
		
		<xsl:variable name="param_types">
			<xsl:value-of
				select="concat('XSL.jportalClassification.types.SESSION=',$IDTypes)"/>
		</xsl:variable>
		<xsl:variable name="param_types_editor">
			<xsl:value-of
				select="concat('_xml_metadata/types/type/@classid=',$IDTypes)"/>
		</xsl:variable>
		
		<xsl:variable name="param_rubrics">
			<xsl:value-of
				select="concat('XSL.jportalClassification.rubrics.SESSION=',$IDRubrics)"/>
		</xsl:variable>
		<xsl:variable name="param_rubrics_editor">
			<xsl:value-of
				select="concat('_xml_metadata/rubrics/rubric/@classid=',$IDRubrics)"/>
		</xsl:variable>
		
		<xsl:value-of
			select="concat('&amp;',$param_types,'&amp;',$param_types_editor,'&amp;',$param_rubrics,'&amp;',$param_rubrics_editor)"/>
		
	</xsl:template>
	
	<!-- ============================================================================================================================ -->
	
	<xsl:template name="get.staticURL">
		<xsl:param name="stURL"/>
		<tr>
			<td id="detailed-staticurl1">
				<xsl:value-of
					select="concat(i18n:translate('metaData.staticURL'),':')"/>
			</td>
			<td>
			</td>
		</tr>
		<tr>
			<td colspan="2" id="detailed-staticurl2">
				<a>
					<xsl:attribute name="href">
						<xsl:copy-of select="$stURL"/>
					</xsl:attribute>
					<xsl:copy-of select="$stURL"/>
				</a>
			</td>
		</tr>
		
	</xsl:template>
	
	<!-- ============================================================================================================================ -->
	<xsl:template name="emptyRow">
		<tr>
			<td>
				<br/>
				<br/>
			</td>
			<td>
				<br/>
				<br/>
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>