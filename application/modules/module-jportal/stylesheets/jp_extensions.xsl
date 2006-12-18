<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xlink mcr i18n acl xalan">
	<xsl:param name="view.objectmetadata"/>
	
	<!-- ===================================================================================================== -->
	
	<xsl:template
		match="/mycoreobject[contains(@ID,'_jpjournal_')] 
		| /mycoreobject[contains(@ID,'_jpvolume_')] 
		| /mycoreobject[contains(@ID,'_jparticle_')]"
		priority="2">
		
		<xsl:call-template name="printTOCLink"/>
		
		<xsl:call-template name="printHistoryRow"/>
		<br/>
		<br/>
		<br/>
		
		<xsl:choose>
			<!-- metadaten -->
			<xsl:when test="$view.objectmetadata = 'false'">
				<xsl:choose>
					<xsl:when test="($objectHost != 'local') or acl:checkPermission(/mycoreobject/@ID,'read')">
						<xsl:apply-templates select="." mode="present">
							<xsl:with-param name="obj_host" select="$objectHost"/>
						</xsl:apply-templates>
						<hr/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="i18n:translate('metaData.accessDenied')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- inhaltsverzeichnis -->
			<xsl:otherwise>
				<table cellpadding="0" cellspacing="0">
					<tr>
						<td colspan="2" rowspan="1" id="leaf-headline2">
						</td>
						<td colspan="1" rowspan="2" id="leaf-preview">
							<img src="{concat($WebApplicationBaseURL,'preview.png')}"/>
						</td>
					</tr>
					<tr>
						<td id="leaf-leafarea">
							<br/>
							<table cellpadding="0" cellspacing="0" border="0">
								<xsl:variable select="." name="context"/>
								<xsl:for-each select="./structure/children/child">
									<xsl:variable name="childXML">
										<xsl:copy-of select="document(concat('mcrobject:',@xlink:href))"/>
									</xsl:variable>
									
									<tr id="leaf-all">
										<td id="leaf-front">
											<div></div>
										</td>
										<td id="leaf-linkarea">
											
											<xsl:variable name="name">
												<xsl:value-of
													select="xalan:nodeset($childXML)/mycoreobject/metadata/maintitles/maintitle/text()"/>
											</xsl:variable>
											<xsl:variable name="date">
												<xsl:choose>
													<xsl:when
														test="xalan:nodeset($childXML)/mycoreobject/metadata/dates/date[@inherited='0']">
														<xsl:variable name="date">
															<xsl:value-of
																select="xalan:nodeset($childXML)/mycoreobject/metadata/dates/date/text()"/>
														</xsl:variable>
<!--														<xsl:call-template name="formatISODate">
															<xsl:with-param name="date" select="$date"/>
															<xsl:with-param name="format" select="i18n:translate('metaData.date')" />
															<xsl:with-param name="locale" select="$CurrentLang"/>
														</xsl:call-template>-->
														<xsl:value-of select="concat(' (',$date,')')" />
													</xsl:when>
													<xsl:otherwise>
														
														<xsl:value-of select="''"/>
													</xsl:otherwise>
												</xsl:choose>
												
											</xsl:variable>
											
											<xsl:variable name="label">
												
												<xsl:value-of select="concat($name,$date)"/>
											</xsl:variable>
											<xsl:choose>
												<xsl:when test="contains(@xlink:href,'_jparticle_')]">
													
													<xsl:call-template name="objectLinking">
														
														<xsl:with-param name="obj_id" select="@xlink:href"/>
														
														<xsl:with-param name="obj_name" select="$label"/>
														
														<xsl:with-param name="requestParam"
															select="'XSL.view.objectmetadata.SESSION=false'"/>
														
													</xsl:call-template>
												</xsl:when>
												
												<xsl:otherwise>
													
													<xsl:call-template name="objectLinking">
														
														<xsl:with-param name="obj_id" select="@xlink:href"/>
														
														<xsl:with-param name="obj_name" select="$label"/>
														
													</xsl:call-template>
													
												</xsl:otherwise>
											</xsl:choose>
										</td>
									</tr>
									<tr id="leaf-whitespaces">
									</tr>
								</xsl:for-each>
							</table>
						</td>
					</tr>
				</table>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ===================================================================================================== -->
	
	<xsl:template name="appendChild">
		<xsl:param name="accessedit"/>
		<xsl:param name="id"/>
		<xsl:param name="layout"/>
		<xsl:param name="label"/>
		<xsl:param name="imagePath"/>
		<xsl:param name="type" select="'dokument'"/>
		<xsl:param name="xmltempl" select="concat('&amp;_xml_structure%2Fparents%2Fparent%2F%40href=',$id)"/>
		<xsl:param name="label"/>
		
		<xsl:variable name="suffix">
			<xsl:if test="string-length($layout)>0">
				
				<xsl:value-of select="concat('&amp;layout=',$layout)"/>
				
			</xsl:if>
			
		</xsl:variable>
		<xsl:if test="acl:checkPermission($id,'writedb')">
			<tr>
				<td class="metaname">
					
					<xsl:value-of select="$label"/> : </td>
				<td class="metavalue">
					
					<a
						href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;step=author&amp;todo=wnewobj{$suffix}{$xmltempl}">
						
						<img src="{concat($WebApplicationBaseURL,$imagePath)}" title="{$label}"/>
						
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
			
			<xsl:value-of select="./metadata/hidden_pubTypesID/hidden_pubTypeID/text()"/>
			
		</xsl:variable>
		
		<xsl:variable name="IDRubrics">
			
			<xsl:value-of select="./metadata/hidden_rubricsID/hidden_rubricID/text()"/>
			
		</xsl:variable>
		
		<xsl:variable name="param_types">
			
			<xsl:value-of select="concat('XSL.jportalClassification.types.SESSION=',$IDTypes)"/>
			
		</xsl:variable>
		
		<xsl:variable name="param_types_editor">
			
			<xsl:value-of select="concat('_xml_metadata/types/type/@classid=',$IDTypes)"/>
			
		</xsl:variable>
		
		<xsl:variable name="param_rubrics">
			
			<xsl:value-of select="concat('XSL.jportalClassification.rubrics.SESSION=',$IDRubrics)"/>
			
		</xsl:variable>
		
		<xsl:variable name="param_rubrics_editor">
			
			<xsl:value-of select="concat('_xml_metadata/rubrics/rubric/@classid=',$IDRubrics)"/>
			
		</xsl:variable>
		
		<xsl:value-of
			select="concat('&amp;',$param_types,'&amp;',$param_types_editor,'&amp;',$param_rubrics,'&amp;',$param_rubrics_editor)"/>
		
	</xsl:template>
	
	
	<!-- ============================================================================================================================ -->
	
	<xsl:template name="get.staticURL">
		<xsl:param name="stURL"/>
		<tr>
			<td id="detailed-staticurl1">
				
				<xsl:value-of select="concat(i18n:translate('metaData.staticURL'),':')"/>
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
	
	
	<!-- ============================================================================================================================ -->
	<xsl:template name="printHistoryRow">
		<table>
			<tr>
				<td id="leaf-headline2">
					
					<xsl:for-each select="./metadata/maintitles/maintitle">
						
						<xsl:sort select="@inherited" order="descending"/>
						
						<xsl:choose>
							
							<xsl:when test="@inherited='0'">
								
								<span>
									
									<xsl:value-of select="text()"/>
									
								</span>
								
							</xsl:when>
							
							<xsl:when test="@inherited='1' ">
								
								<xsl:if test="/mycoreobject/structure/parents/parent[@xlink:href!='']">
									
									<xsl:variable name="parent_name">
										
										<xsl:value-of
											select="document(concat('mcrobject:',/mycoreobject/structure/parents/parent/@xlink:href))/mycoreobject/metadata/maintitles/maintitle/text()"/>
										
									</xsl:variable>
									
									<xsl:variable name="href">
										
										<xsl:value-of select="/mycoreobject/structure/parents/parent/@xlink:href"/>
										
									</xsl:variable>
									
									<xsl:call-template name="objectLinking">
										
										<xsl:with-param name="obj_id" select="$href"/>
										
										<xsl:with-param name="obj_name" select="$parent_name"/>
										
									</xsl:call-template>
									
									<xsl:value-of select="' &gt; '"/>
									
								</xsl:if>
								
							</xsl:when>
							
							<xsl:otherwise>
								
								<xsl:value-of select="concat(text(),' > ')"/>
								
							</xsl:otherwise>
							
						</xsl:choose>
						
					</xsl:for-each>
					
				</td>
			</tr>
			<tr>
				<td id="leaf-headline1"> _________________________________________________</td>
			</tr>
		</table>
	</xsl:template>
	
	
	<!-- ============================================================================================================================ -->
	
	<xsl:template name="objectLinking">
		<xsl:param name="obj_id"/>
		<xsl:param name="obj_name"/>
		<xsl:param name="requestParam"/>
		<!-- 
		LOCAL REQUEST
		-->
		<xsl:if test="$objectHost = 'local'">
			
			<xsl:variable name="mcrobj" select="document(concat('mcrobject:',$obj_id))/mycoreobject"/>
			
			<xsl:choose>
				
				<xsl:when test="acl:checkPermission($obj_id,'read')">
					
					<a href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}?{$requestParam}">
						
						<xsl:value-of select="$obj_name"/>
						
					</a>
					
				</xsl:when>
				
				<xsl:otherwise>
					
					<!-- Build Login URL for LoginServlet -->
					
					<xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
						select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?url=', encoder:encode( string( $RequestURL ) ) )"/>
					
					<xsl:apply-templates select="$mcrobj" mode="resulttitle"/>
					
					&#160;
					
					<a href="{$LoginURL}">
						
						<img src="{concat($WebApplicationBaseURL,'images/paper_lock.gif')}"/>
						
					</a>
					
				</xsl:otherwise>
				
			</xsl:choose>
		</xsl:if>
		<!-- 
		REMOTE REQUEST
		-->
		<xsl:if test="$objectHost != 'local'">
			
			<xsl:variable name="mcrobj"
				select="document(concat('mcrws:operation=MCRDoRetrieveObject&amp;host=',$objectHost,'&amp;ID=',$obj_id))/mycoreobject"/>
			<a href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}?host={@host}&amp;{$requestParam}">
				
				<xsl:apply-templates select="$mcrobj" mode="resulttitle"/>
			</a>
		</xsl:if>
	</xsl:template>
	
	<!-- ===================================================================================================== -->
	
	<xsl:template name="setParameter">
		<xsl:param name="param"/>
		<xsl:param name="paramValue"/>
		<!-- to verify within template -->
		<xsl:param name="labelOFF"/>
		<xsl:param name="labelON"/>
		
		<xsl:variable name="RequestURL_view-Deleted">
			
			<xsl:call-template name="UrlDelParam">
				
				<xsl:with-param name="url" select="$RequestURL"/>
				
				<!--				<xsl:with-param name="par" select="'XSL.view.objectmetadata'"/>-->
				
				<xsl:with-param name="par" select="concat('XSL.',$param)"/>
				
			</xsl:call-template>
			
		</xsl:variable>
		
		<xsl:variable name="targetURL">
			
			<xsl:call-template name="UrlDelParam">
				
				<xsl:with-param name="url" select="$RequestURL_view-Deleted"/>
				
				<xsl:with-param name="par" select="concat('XSL.',$param,'.SESSION')"/>
				
			</xsl:call-template>
			
		</xsl:variable>
		<table id="switch">
			<tr>
				<td>
					
					<xsl:choose>
						
						<xsl:when test="$paramValue='false'">
							
							<xsl:variable name="targetURL_withParam">
								
								<xsl:call-template name="UrlSetParam">
									
									<xsl:with-param name="url" select="$targetURL"/>
									
									<xsl:with-param name="par" select="concat('XSL.',$param,'.SESSION')"/>
									
									<xsl:with-param name="value" select="'true'"/>
									
								</xsl:call-template>
								
							</xsl:variable>
							
							<a href="{$targetURL_withParam}">
								
								<xsl:value-of select="$labelOFF"/>
								
							</a>
							
						</xsl:when>
						
						<xsl:otherwise>
							
							<xsl:variable name="targetURL_withParam">
								
								<xsl:call-template name="UrlSetParam">
									
									<xsl:with-param name="url" select="$targetURL"/>
									
									<xsl:with-param name="par" select="concat('XSL.',$param,'.SESSION')"/>
									
									<xsl:with-param name="value" select="'false'"/>
									
								</xsl:call-template>
								
							</xsl:variable>
							
							<a href="{$targetURL_withParam}">
								
								<xsl:value-of select="$labelON"/>
								
							</a>
							
						</xsl:otherwise>
						
					</xsl:choose>
					
				</td>
			</tr>
		</table>
	</xsl:template>
	
	<!-- ===================================================================================================== -->
	
	<xsl:template name="printTOCLink">
		<xsl:choose>
			
			<xsl:when test="/mycoreobject[contains(@ID,'_jparticle_')]">
				
				<xsl:call-template name="objectLinking">
					
					<xsl:with-param name="obj_id" select="/mycoreobject/structure/parents/parent/@xlink:href"/>
					
					<xsl:with-param name="obj_name"
						select="concat('Inhaltsverzeichnis ',/mycoreobject/metadata/maintitles/maintitle[@inherited='1']/text(), ' &gt;')"/>
					
					<xsl:with-param name="requestParam" select="'XSL.view.objectmetadata.SESSION=true'"/>
					
				</xsl:call-template>
				
			</xsl:when>
			
			<xsl:otherwise>
				
				<xsl:call-template name="setParameter">
					
					<xsl:with-param name="param" select="'view.objectmetadata'"/>
					
					<xsl:with-param name="paramValue" select="$view.objectmetadata"/>
					
					<xsl:with-param name="labelON" select="'Detailansicht &gt;'"/>
					
					<xsl:with-param name="labelOFF" select="'Inhaltsverzeichnis &gt;'"/>
					
				</xsl:call-template>
				
			</xsl:otherwise>
			
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>