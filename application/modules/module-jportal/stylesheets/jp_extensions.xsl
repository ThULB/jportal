<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xlink mcr i18n acl xalan">
	
	<xsl:include href="mcr-module-startIview.xsl"/>
	
	<xsl:param name="view.objectmetadata"/>
	<xsl:param name="toc.pos" select="0"/>
	<xsl:param name="toc.pageSize" select="10"/>
	
	<!-- ===================================================================================================== -->
	
	<xsl:template
		match="/mycoreobject[contains(@ID,'_jpjournal_')] 
		| /mycoreobject[contains(@ID,'_jpvolume_')] 
		| /mycoreobject[contains(@ID,'_jparticle_')]"
		priority="2">
		
		<xsl:call-template name="printTOCLink"/>
		
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
					<!--					<tr>
					<td colspan="2" rowspan="1" id="leaf-headline2">
					</td>
					<td colspan="1" rowspan="2" id="leaf-preview">
					<img src="{concat($WebApplicationBaseURL,'preview.png')}"/>
					</td>
					</tr>-->
					<tr>
						<td id="leaf-leafarea">
							<br/>
							<xsl:call-template name="printChildren"/>
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
		<xsl:param name="underline" select="'false'"/>
		<xsl:param name="sortOrder" select="'descending'"/>
		
		<table>
			<tr>
				<td id="leaf-headline2">
					<xsl:choose>
						<xsl:when test="$sortOrder='descending'">
							<xsl:for-each select="mycoreobject/metadata/maintitles/maintitle">
								<xsl:sort select="@inherited" order="descending"/>
								<xsl:call-template name="printHistoryRow.rows">
									<xsl:with-param name="sortOrder" select="$sortOrder"/>
								</xsl:call-template>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<xsl:for-each select="mycoreobject/metadata/maintitles/maintitle">
								<xsl:sort select="@inherited" order="ascending"/>
								<xsl:call-template name="printHistoryRow.rows">
									<xsl:with-param name="sortOrder" select="$sortOrder"/>
								</xsl:call-template>
							</xsl:for-each>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</tr>
			<xsl:if test="$underline='true'">
				<tr>
					<td id="leaf-headline1"> _________________________________________________</td>
				</tr>
			</xsl:if>
		</table>
	</xsl:template>
	
	
	<!-- ============================================================================================================================ -->
	
	<xsl:template name="printHistoryRow.rows">
		<xsl:param name="sortOrder"/>
		<xsl:choose>
			<xsl:when test="@inherited='0'">
				<span>
					<xsl:variable name="date">
						<xsl:if test="/mycoreobject/metadata/dates/date[@inherited='0']/text()!=''">
							<xsl:value-of
								select="concat(' (',/mycoreobject/metadata/dates/date[@inherited='0']/text(),')')"/>
						</xsl:if>
					</xsl:variable>
					<xsl:variable name="text">
						<xsl:call-template name="ShortenText">
							<xsl:with-param name="text" select="text()"/>
							<xsl:with-param name="length" select="25"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="label">
						<xsl:value-of select="concat($text,$date)"/>
					</xsl:variable>
					<xsl:value-of select="$label"/>
				</span>
			</xsl:when>
			<xsl:when test="@inherited='1' ">
				<xsl:if test="/mycoreobject/structure/parents/parent[@xlink:href!='']">
					<xsl:variable name="date">
						<xsl:if test="/mycoreobject/metadata/dates/date[@inherited='1']">
							<xsl:value-of
								select="concat(' (',/mycoreobject/metadata/dates/date[@inherited='1']/text(),')')"/>
						</xsl:if>
					</xsl:variable>
					<xsl:variable name="text">
						<xsl:call-template name="ShortenText">
							<xsl:with-param name="text" select="text()"/>
							<xsl:with-param name="length" select="25"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="label">
						<xsl:choose>
							<xsl:when test="$sortOrder='descending'">
								<xsl:value-of select="concat($text,$date, ' > ')"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="concat(' > ',$text,$date)"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:call-template name="objectLinking">
						<xsl:with-param name="obj_id" select="/mycoreobject/structure/parents/parent/@xlink:href"/>
						<xsl:with-param name="obj_name" select="$label"/>
						<xsl:with-param name="requestParam"
							select=" concat('XSL.toc.pos.SESSION=0&amp;XSL.view.objectmetadata.SESSION=',$view.objectmetadata)"/>
					</xsl:call-template>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="heritedLevel">
					<xsl:value-of select="@inherited"/>
				</xsl:variable>
				<xsl:variable name="date">
					<xsl:if test="/mycoreobject/metadata/dates/date[@inherited=$heritedLevel]">
						<xsl:value-of
							select="concat(' (',/mycoreobject/metadata/dates/date[@inherited=$heritedLevel]/text(),')')"/>
					</xsl:if>
				</xsl:variable>
				<xsl:variable name="text">
					<xsl:call-template name="ShortenText">
						<xsl:with-param name="text" select="text()"/>
						<xsl:with-param name="length" select="25"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="label">
					<xsl:choose>
						<xsl:when test="$sortOrder='descending'">
							<xsl:value-of select="concat($text,$date, ' > ')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="concat(' > ',$text,$date)"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:value-of select="$label"/>
			</xsl:otherwise>
		</xsl:choose>
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
				<xsl:with-param name="par" select="concat('XSL.',$param)"/>				
			</xsl:call-template>			
		</xsl:variable>		
		<xsl:variable name="targetURL">			
			<xsl:call-template name="UrlDelParam">				
				<xsl:with-param name="url" select="$RequestURL_view-Deleted"/>				
				<xsl:with-param name="par" select="concat('XSL.',$param,'.SESSION')"/>				
			</xsl:call-template>			
		</xsl:variable>
		<table id="switch" cellspacing="0" cellpadding="0" border="0">
			<tr>
				<xsl:choose>
					<xsl:when test="$paramValue='false'">
						<td>
							<div id="switch-current">						
								<xsl:value-of select="$labelON"/> 
							</div>
						</td>						
						<td width="20"></td>						
						<td>
							<div id="switch-notcurrent">
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
							</div>
						</td>

					</xsl:when>
					<xsl:otherwise>
						<td>
							<div id="switch-notcurrent">
								<xsl:variable name="targetURL_withParam">
									<xsl:call-template name="UrlSetParam">
										<xsl:with-param name="url" select="$targetURL"/>
										<xsl:with-param name="par" select="concat('XSL.',$param,'.SESSION')"/>
										<xsl:with-param name="value" select="'false'"/>
									</xsl:call-template>
								</xsl:variable>
								<a href="{$targetURL_withParam}">
									<xsl:value-of select="concat($labelON,' anzeigen')"/> 
								</a>
							</div>
						</td>						
						<td width="20"></td>						
						<td>
							<div id="switch-current">
								<xsl:value-of select="$labelOFF"/> 
							</div>
						</td>
					</xsl:otherwise>
				</xsl:choose>
			</tr>
		</table>
	</xsl:template>
	
	<!-- ===================================================================================================== -->
	
	<xsl:template name="printTOCLink">
		<xsl:variable name="children">
			<xsl:choose>
				<xsl:when test="/mycoreobject/structure/children)">
					<xsl:value-of select="'true'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'false'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
<!--		<xsl:choose>
			<xsl:when test="/mycoreobject[contains(@ID,'_jparticle_')]
				or  $children='false'">
					<xsl:call-template name="objectLinking">
						<xsl:with-param name="obj_id" select="/mycoreobject/structure/parents/parent/@xlink:href"/>
						<xsl:with-param name="obj_name"
							select="concat('Zeige Inhaltsverzeichnis ',/mycoreobject/metadata/maintitles/maintitle[@inherited='1']/text(), ' &gt;')"/>
						<xsl:with-param name="requestParam" select="'XSL.view.objectmetadata.SESSION=true'"/>
					</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>-->
				<xsl:call-template name="setParameter">
					<xsl:with-param name="param" select="'view.objectmetadata'"/>
					<xsl:with-param name="labelON" select="'Detailansicht'"/>
					<xsl:with-param name="paramValue" select="$view.objectmetadata"/>
					<xsl:with-param name="labelOFF" select="'Inhaltsverzeichnis'"/>
				</xsl:call-template>
				<br></br>
<!--			</xsl:otherwise>
		</xsl:choose>-->
		<br/>
	</xsl:template>
	
	<!-- ===================================================================================================== -->
	
	<xsl:template name="printDerivates">
		<xsl:param name="obj_id"/>
		<xsl:param name="knoten"/>
		
		<xsl:choose>
			<xsl:when test="$knoten=''">
				<xsl:if test="./structure/derobjects">
					<!--					<xsl:if test="$objectHost != 'local'">
					<a href="{$staticURL}">nur auf original Server</a>
					</xsl:if>-->
					<xsl:if test="$objectHost = 'local'">
						<xsl:for-each select="./structure/derobjects/derobject">
							<xsl:variable name="deriv" select="@xlink:href"/>
							<tr>
								<xsl:if test="acl:checkPermission($obj_id,'writedb')">
									<td width="10"/>
									<td width="30" valign="top" align="center">
										<form method="get">
											<xsl:attribute name="action">
												<xsl:value-of
													select="concat($WebApplicationBaseURL,'servlets/MCRStartEditorServlet',$JSessionID)"/>
											</xsl:attribute>
											<input name="lang" type="hidden" value="{$CurrentLang}"/>
											<input name="se_mcrid" type="hidden">
												<xsl:attribute name="value">
													<xsl:value-of select="@xlink:href"/>
												</xsl:attribute>
											</input>
											<input name="te_mcrid" type="hidden">
												<xsl:attribute name="value">
													<xsl:value-of select="@xlink:href"/>
												</xsl:attribute>
											</input>
											<input name="re_mcrid" type="hidden">
												<xsl:attribute name="value">
													<xsl:value-of select="$obj_id"/>
												</xsl:attribute>
											</input>
											<xsl:variable name="type">
												<xsl:copy-of
													select="substring-before(substring-after($obj_id,'_'),'_')"/>
											</xsl:variable>
											<input name="type" type="hidden" value="{$type}"/>
											<input name="todo" type="hidden" value="saddfile"/>
											<input type="image" src="{$WebApplicationBaseURL}images/workflow_deradd.gif"
												title="{i18n:translate('swf.derivate.addFile')}"/>
										</form>
									</td>
									<td width="10"/>
									<td width="30" valign="top" align="center">
										<form method="get">
											<xsl:attribute name="action">
												<xsl:value-of
													select="concat($WebApplicationBaseURL,'servlets/MCRStartEditorServlet',$JSessionID)"/>
											</xsl:attribute>
											<input name="lang" type="hidden" value="{$CurrentLang}"/>
											<input name="se_mcrid" type="hidden">
												<xsl:attribute name="value">
													<xsl:value-of select="@xlink:href"/>
												</xsl:attribute>
											</input>
											<input name="te_mcrid" type="hidden">
												<xsl:attribute name="value">
													<xsl:value-of select="@xlink:href"/>
												</xsl:attribute>
											</input>
											<input name="re_mcrid" type="hidden">
												<xsl:attribute name="value">
													<xsl:value-of select="$obj_id"/>
												</xsl:attribute>
											</input>
											<xsl:variable name="type">
												<xsl:copy-of
													select="substring-before(substring-after($obj_id,'_'),'_')"/>
											</xsl:variable>
											<input name="type" type="hidden" value="{$type}"/>
											<input name="todo" type="hidden" value="seditder"/>
											<input name="extparm" type="hidden">
												<xsl:variable name="deriv" select="@xlink:href"/>
												<xsl:variable name="derivlink" select="concat('mcrobject:',$deriv)"/>
												<xsl:attribute name="value">
													<xsl:value-of
														select="document($derivlink)/mcr_results/mcr_result/@label"/>
												</xsl:attribute>
											</input>
											<input type="image" src="{$WebApplicationBaseURL}images/workflow_deredit.gif"
												title="{i18n:translate('swf.derivate.editDerivate')}"/>
										</form>
									</td>
									<td width="10"/>
									<td width="30" valign="top" align="center">
										<form method="get">
											<xsl:attribute name="action">
												<xsl:value-of
													select="concat($WebApplicationBaseURL,'servlets/MCRStartEditorServlet',$JSessionID)"/>
											</xsl:attribute>
											<input name="lang" type="hidden" value="{$CurrentLang}"/>
											<input name="se_mcrid" type="hidden">
												<xsl:attribute name="value">
													<xsl:value-of select="@xlink:href"/>
												</xsl:attribute>
											</input>
											<input name="te_mcrid" type="hidden">
												<xsl:attribute name="value">
													<xsl:value-of select="@xlink:href"/>
												</xsl:attribute>
											</input>
											<input name="re_mcrid" type="hidden">
												<xsl:attribute name="value">
													<xsl:value-of select="$obj_id"/>
												</xsl:attribute>
											</input>
											<xsl:variable name="type">
												<xsl:copy-of
													select="substring-before(substring-after($obj_id,'_'),'_')"/>
											</xsl:variable>
											<input name="type" type="hidden" value="{$type}"/>
											<input name="todo" type="hidden" value="sdelder"/>
											<input type="image"
												src="{$WebApplicationBaseURL}images/workflow_derdelete.gif"
												title="{i18n:translate('swf.derivate.delDerivate')}"/>
										</form>
									</td>
									<td width="10"/>
								</xsl:if>
								<td align="left" valign="top" id="detailed-links">
									<table id="detailed-contenttable" border="0" cellspacing="0">
										<xsl:variable name="deriv" select="@xlink:href"/>
										<xsl:variable name="derivlink" select="concat('mcrobject:',$deriv)"/>
										<xsl:variable name="derivate" select="document($derivlink)"/>
										<tr id="detailed-contents">
											<td>
												<xsl:apply-templates
													select="$derivate/mycorederivate/derivate/internals"/>
												<xsl:apply-templates
													select="$derivate/mycorederivate/derivate/externals"/>
											</td>
										</tr>
									</table>
								</td>
							</tr>
							<tr id="detailed-whitespaces">
								<td></td>
							</tr>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="xalan:nodeset($knoten)/mycoreobject/structure/derobjects">
					<td id="searchmask-contentlinkarea">
						<xsl:if test="$objectHost = 'local'">
							<table border="0" cellspacing="0" id="searchmask-contentlinks">
								<xsl:for-each
									select="xalan:nodeset($knoten)/mycoreobject/structure/derobjects/derobject">
									<xsl:variable name="deriv" select="@xlink:href"/>
									<xsl:variable name="deriv" select="@xlink:href"/>
									<xsl:variable name="derivlink" select="concat('mcrobject:',$deriv)"/>
									<xsl:variable name="derivate" select="document($derivlink)"/>
									<tr>
										<td>
											<xsl:apply-templates select="$derivate/mycorederivate/derivate/internals"/>
											<xsl:apply-templates select="$derivate/mycorederivate/derivate/externals"/>
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</xsl:if>
					</td>
				</xsl:if>
				
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	<!-- ===================================================================================================== -->
	<xsl:template match="internals" priority="2">
		<xsl:if test="$objectHost = 'local'">
			<xsl:variable name="obj_host" select="../../../@host"/>
			<xsl:variable name="derivid" select="../../@ID"/>
			<xsl:variable name="derivlabel" select="../../@label"/>
			<xsl:variable name="derivmain" select="internal/@maindoc"/>
			<xsl:variable name="derivbase" select="concat($ServletsBaseURL,'MCRFileNodeServlet/',$derivid,'/')"/>
			<xsl:variable name="derivifs" select="concat($derivbase,$derivmain,$HttpSession,'?hosts=',$obj_host)"/>
			<xsl:variable name="derivdir" select="concat($derivbase,$HttpSession,'?hosts=',$obj_host)"/>
			<xsl:variable name="derivxml" select="concat('ifs:/',$derivid,'?hosts=',$obj_host)"/>
			<xsl:variable name="details" select="document($derivxml)"/>
			<xsl:variable name="ctype" select="$details/mcr_directory/children/child[name=$derivmain]/contentType"/>
			<xsl:variable name="ftype"
				select="document('webapp:FileContentTypes.xml')/FileContentTypes/type[@ID=$ctype]/label"/>
			<xsl:variable name="size" select="$details/mcr_directory/size"/>
			
			<xsl:variable name="href">
				<!-- IView available ? -->
				<xsl:variable name="supportedMainFile">
					<xsl:call-template name="iview.getSupport">
						<xsl:with-param name="derivID" select="$derivid"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$supportedMainFile != ''">
						<xsl:call-template name="iview.getAddress">
							<xsl:with-param name="derivID" select="$derivid"/>
							<xsl:with-param name="pathOfImage" select="$supportedMainFile"/>
							<xsl:with-param name="height" select="'510'"/>
							<xsl:with-param name="width" select="'605'"/>
							<xsl:with-param name="scaleFactor" select="'fitToWidth'"/>
							<xsl:with-param name="display" select="'extended'"/>
							<xsl:with-param name="style" select="'image'"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat($derivbase,$derivmain)"/>
					</xsl:otherwise>
				</xsl:choose>
				
			</xsl:variable>
			
			<a href="{$href}">
				<xsl:value-of select="concat($ctype,' ansehen &gt;&gt;')"/>
			</a>
			
			<br/>
			<xsl:if test="$CurrentUser!='gast'">
				<a href="{$derivbase}">
					<xsl:value-of select="'Details &gt;&gt;'"/>
				</a>
			</xsl:if>
			<!--				<xsl:variable name="ziplink"
			select="concat($ServletsBaseURL,'MCRZipServlet',$JSessionID,'?id=',$derivid)"/>
			<a class="linkButton" href="{$ziplink}">
			<xsl:value-of select="i18n:translate('buttons.zipGen')"/>
			</a>-->
			<!--				&#160;
			<a href="{$derivdir}">
			<xsl:value-of select="i18n:translate('buttons.details')"/>
			</a>-->
		</xsl:if>
	</xsl:template>
	<!-- ===================================================================================================== -->
	
	<xsl:template name="printChildren">
		<table>
			<tr>
				<td colspan="1" rowspan="2">
					<table cellpadding="0" cellspacing="0">
						<tr>
							<td>
								<xsl:call-template name="printTOCNavi">
									<xsl:with-param name="location" select="'navi'"/>
								</xsl:call-template>
							</td>
						</tr>
						<xsl:for-each select="./structure/children/child">
							<!-- take care on children result list lenght -->
							<xsl:if test="(position()>=$toc.pos) and ($toc.pos+$toc.pageSize>=position())">
								<tr id="leaf-whitespaces">
									<td colspan="2">
										<xsl:variable name="cXML">
											<xsl:copy-of select="document(concat('mcrobject:',@xlink:href))"/>
										</xsl:variable>
										<table cellspacing="0" cellpadding="0" id="leaf-all">
											<tr>
												<td id="leaf-front" colspan="1" rowspan="2">
													<xsl:variable name="OID">
														<xsl:call-template name="typeOfObjectID">
															<xsl:with-param name="id" select="@xlink:href"/>
														</xsl:call-template>
													</xsl:variable>
													<xsl:choose>
														<xsl:when test="$OID='jpvolume'">
															<xsl:value-of
																select="i18n:translate('metaData.type.volume')"/>
														</xsl:when>
														<xsl:when test="$OID='jpjournal'">
															<xsl:value-of
																select="i18n:translate('metaData.type.journal')"/>
														</xsl:when>
														<xsl:when test="$OID='jparticle'">
															<xsl:value-of
																select="i18n:translate('metaData.type.article')"/>
														</xsl:when>
														<xsl:otherwise>
														</xsl:otherwise>
													</xsl:choose>
												</td>
												<td id="leaf-linkarea">
													<xsl:variable name="name">
														<xsl:value-of
															select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()"/>
													</xsl:variable>
													<xsl:variable name="date">
														<xsl:choose>
															<xsl:when
																test="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0']">
																<xsl:variable name="date">
																	<xsl:value-of
																		select="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date/text()"/>
																</xsl:variable>
																<xsl:value-of select="concat(' (',$date,')')"/>
															</xsl:when>
															<xsl:otherwise>
																<xsl:value-of select="''"/>
															</xsl:otherwise>
														</xsl:choose>
													</xsl:variable>
													<xsl:variable name="label">
														<xsl:value-of select="concat($name,$date)"/>
													</xsl:variable>
													<xsl:variable name="shortlabel">
														<xsl:call-template name="ShortenText">
															<xsl:with-param name="text" select="$label"/>
															<xsl:with-param name="length" select="40"/>
														</xsl:call-template>
													</xsl:variable>
													<xsl:variable name="children">
														<xsl:choose>
															<xsl:when
																test="(xalan:nodeset($cXML)/mycoreobject/structure/children)">
																<xsl:value-of select="'true'"/>
															</xsl:when>
															<xsl:otherwise>
																<xsl:value-of select="'false'"/>
															</xsl:otherwise>
														</xsl:choose>
													</xsl:variable>
													<xsl:choose>
														<xsl:when
															test="(contains(@xlink:href,'_jparticle_')) 
												or ($children='false') ">
															<xsl:call-template name="objectLinking">
																<xsl:with-param name="obj_id" select="@xlink:href"/>
																<xsl:with-param name="obj_name" select="$shortlabel"/>
																<xsl:with-param name="requestParam"
																	select="'XSL.view.objectmetadata.SESSION=false&amp;XSL.toc.pos.SESSION=0'"/>
															</xsl:call-template>
														</xsl:when>
														<xsl:otherwise>
															<xsl:call-template name="objectLinking">
																<xsl:with-param name="obj_id" select="@xlink:href"/>
																<xsl:with-param name="obj_name" select="$shortlabel"/>
																<xsl:with-param name="requestParam"
																	select="'XSL.view.objectmetadata.SESSION=true&amp;XSL.toc.pos.SESSION=0'"/>
															</xsl:call-template>
														</xsl:otherwise>
													</xsl:choose>
												</td>
											</tr>
											<tr>
												<xsl:call-template name="printDerivates">
													<xsl:with-param name="obj_id" select="@xlink:href"/>
													<xsl:with-param name="knoten" select="$cXML"/>
												</xsl:call-template>
											</tr>
										</table>
									</td>
								</tr>
								<tr>
									<td>
										<br/>
									</td>
								</tr>
							</xsl:if>
						</xsl:for-each>
					</table>
				</td>
				<td id="leaf-horizontalnaviup">
					<xsl:call-template name="printTOCNavi">
						<xsl:with-param name="location" select="'upper'"/>
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td id="leaf-horizontalnavidown">
					<xsl:call-template name="printTOCNavi">
						<xsl:with-param name="location" select="'lower'"/>
					</xsl:call-template>
				</td>
			</tr>
		</table>
		
	</xsl:template>
	<!-- ===================================================================================================== -->
	<xsl:template name="printTOCNavi">
		<xsl:param name="location"/>
		
		<xsl:variable name="pred">
			<xsl:value-of select="number($toc.pos)-(number($toc.pageSize)+1)"/>
		</xsl:variable>
		<xsl:variable name="succ">
			<xsl:value-of select="number($toc.pos)+number($toc.pageSize)+1"/>
		</xsl:variable>
		<xsl:variable name="numChildren">
			<xsl:value-of select="count(/mycoreobject/structure/children//child)"/>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="$location='navi'">
				<table>
					<tr>
						<td align="center">
							<form id="pageSize" target="_self"
								action="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}{$HttpSession}" method="post">
								<p>
									<select onChange="document.getElementById('pageSize').submit()"
										name="XSL.toc.pageSize.SESSION" size="1">
										<option value="" selected="selected">Inhaltsverzeichnis unterteilen</option>
										<option value="5">in 5'er Gruppen</option>
										<option value="10">in 10'er Gruppen</option>
										<option value="15">in 15'er Gruppen</option>
										<option value="20">in 20'er Gruppen</option>
										<option value="30">in 30'er Gruppen</option>
										<option value="40">in 40'er Gruppen</option>
										<option value="50">in 50'er Gruppen</option>
										<option value="1000000">keine Unterteilung</option>
									</select>
								</p>
							</form>
						</td>
						<td align="center">
							<form id="sort" target="_self"
								action="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}{$HttpSession}" method="post">
								<p>
									<select onChange="document.getElementById('pageSize').submit()"
										name="XSL.toc.pageSize.SESSION" size="1">
										<option value="" selected="selected">sortieren</option>
										<option value="ascending">aufsteigend</option>
										<option value="descending">absteigend</option>
									</select>
								</p>
							</form>
						</td>
					</tr>
				</table>
			</xsl:when>
			<xsl:otherwise>
				<table>
					<tr>
						<xsl:if test="$location='upper'">
							
							<xsl:if test="$pred>=0">
								<td>
									<a
										href="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}{$HttpSession}?XSL.toc.pos.SESSION={$pred}">
										&lt;&lt;
									</a>
								</td>
								<xsl:variable name="to">
									<xsl:choose>
										<xsl:when test="(number($toc.pageSize)+number($toc.pos))>number($numChildren)">
											<xsl:value-of select="number($numChildren)"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="number($toc.pos)+number($toc.pageSize)"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
								<td align="center">
									<xsl:if test="$pred>=0 or number($numChildren)>= $succ">
										<xsl:value-of
											select="concat($toc.pos,'-',number($to),' von ',number($numChildren))"/>
									</xsl:if>
								</td>
							</xsl:if>
						</xsl:if>
						
						<xsl:if test="$location='lower'">
							
							<xsl:if test="number($numChildren)>= $succ">
								<td>
									<a
										href="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}{$HttpSession}?XSL.toc.pos.SESSION={$succ}">
										&gt;&gt;
									</a>
								</td>
								<xsl:variable name="to">
									<xsl:choose>
										<xsl:when test="(number($toc.pageSize)+number($toc.pos))>number($numChildren)">
											<xsl:value-of select="number($numChildren)"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="number($toc.pos)+number($toc.pageSize)"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
								<td align="center">
									<xsl:if test="$pred>=0 or number($numChildren)>= $succ">
										<xsl:value-of
											select="concat($toc.pos,'-',number($to),' von ',number($numChildren))"/>
									</xsl:if>
								</td>
							</xsl:if>
							
						</xsl:if>
					</tr>
				</table>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ===================================================================================================== -->
	<xsl:template name="haveChildren">
		<xsl:param name="object"/>
		<xsl:choose>
			<xsl:when test="xalan:nodeset($object)/mycoreobject/structure/children)">
				<xsl:value-of select="'true'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'false'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ===================================================================================================== -->
	<xsl:template name="showAdminHead">
		<xsl:if test="acl:checkPermission(./@ID,'writedb') or acl:checkPermission(./@ID,'deletedb')">
			<tr id="detailed-dividingline">
				<td colspan="2">
					<hr noshade="noshade" width="460"/>
				</td>
			</tr>
			<tr>
				<td id="detailed-headlines">
					<xsl:value-of select="concat(i18n:translate('metaData.admin'),':')"/>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>