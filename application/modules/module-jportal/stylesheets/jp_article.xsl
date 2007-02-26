<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	xmlns:mcr="http://www.mycore.org/" xmlns:xlink="http://www.w3.org/1999/xlink"
	exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
	<xsl:param select="'local'" name="objectHost"/>
	<!--Template for result list hit: see results.xsl-->
	<xsl:template match="mcr:hit[contains(@id,'_jparticle_')]">
		<xsl:param name="mcrobj"/>
		<xsl:param name="mcrobjlink"/>
		<xsl:variable select="100" name="DESCRIPTION_LENGTH"/>
		<xsl:variable select="@host" name="host"/>
		<xsl:variable name="obj_id">
			
			<xsl:value-of select="@id"/>
		</xsl:variable>
		
		<xsl:variable name="cXML">
			<xsl:copy-of select="document(concat('mcrobject:',@id))"/>
		</xsl:variable>
		<table cellspacing="0" cellpadding="0" id="leaf-all">
			<tr>
				<td id="leaf-front" colspan="1" rowspan="2">
					<!--<xsl:value-of select="i18n:translate('metaData.type.article')"/>-->
					<img src="{$WebApplicationBaseURL}images/artikel2.gif"/>
				</td>
				<td id="leaf-linkarea2">
					<xsl:variable name="name">
							<xsl:call-template name="ShortenText">
								<xsl:with-param name="text" select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()"/>
								<xsl:with-param name="length" select="75"/>
							</xsl:call-template>
					</xsl:variable>
										
					<xsl:variable name="date">
						<xsl:choose>
							<xsl:when test="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0']">
								<xsl:variable name="date">
									<xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date/text()"/>
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
					<xsl:call-template name="objectLinking">
						<xsl:with-param name="obj_id" select="@id"/>
						<xsl:with-param name="obj_name" select="$label"/>
						<xsl:with-param name="requestParam"
							select="'XSL.view.objectmetadata.SESSION=false&amp;XSL.toc.pos.SESSION=0'"/>
						<xsl:with-param name="hoverText" select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()"/>
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<xsl:call-template name="printDerivates">
					<xsl:with-param name="obj_id" select="@id"/>
					<xsl:with-param name="knoten" select="$cXML"/>
				</xsl:call-template>
			</tr>
			<tr>
				<td id="leaf-front"></td>
				<td>
					<span id="leaf-published">
						<xsl:value-of select="i18n:translate('metaData.published')"/>
						<xsl:text>: </xsl:text>
						<xsl:call-template name="printHistoryRow">
							<xsl:with-param name="sortOrder" select="'descending'"/>
							<xsl:with-param name="printCurrent" select="'false'"/>
							<xsl:with-param name="node" select="xalan:nodeset($cXML)"/>
						</xsl:call-template>
					</span>
				</td>
			</tr>
		</table>
		<table cellspacing="0" cellpadding="0">
			<tr id="leaf-whitespaces">
				<td>
				</td>
			</tr>
		</table>
	</xsl:template>
	
	<!--Template for generated link names and result titles: see mycoreobject.xsl, results.xsl, MyCoReLayout.xsl-->
	
	<xsl:template priority="1" mode="resulttitle" match="/mycoreobject[contains(@ID,'_jparticle_')]">
		<xsl:choose>
			<!--
			you could insert any title-like metadata here, e.g.
			replace "your-tags/here" by something of your metadata
			-->
			<xsl:when test="./metadata/your-tags">
				<xsl:call-template name="printI18N">
					<xsl:with-param select="./metadata/your-tags/here" name="nodes"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				
				<xsl:value-of select="@label"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--Template for title in metadata view: see mycoreobject.xsl-->
	<xsl:template priority="1" mode="title" match="/mycoreobject[contains(@ID,'_jparticle_')]">
		<xsl:choose>
			<!--
			you could insert any title-like metadata here, e.g.
			replace "your-tags/here" by something of your metadata
			-->
			<xsl:when test="./metadata/your-tags">
				
				<xsl:call-template name="printI18N">
					<xsl:with-param select="./metadata/your-tags/here" name="nodes"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@ID"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--Template for metadata view: see mycoreobject.xsl-->
	<xsl:template priority="1" mode="present" match="/mycoreobject[contains(@ID,'_jparticle_')]">
		<xsl:param select="$objectHost" name="obj_host"/>
		<xsl:param name="accessedit"/>
		<xsl:param name="accessdelete"/>
		<xsl:variable name="objectBaseURL">
			<xsl:if test="$objectHost != 'local'">
				<xsl:value-of
					select="document('webapp:hosts.xml')/mcr:hosts/mcr:host[@alias=$objectHost]/mcr:url[@type='object']/@href"/>
			</xsl:if>
			
			<xsl:if test="$objectHost = 'local'">
				<xsl:value-of select="concat($WebApplicationBaseURL,'receive/')"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="staticURL">
			<xsl:value-of select="concat($objectBaseURL,@ID)"/>
		</xsl:variable>
		<div id="detailed-frame">
			<xsl:variable name="maintitle_shorted">
				<xsl:call-template name="ShortenText">
					<xsl:with-param name="text" select="./metadata/maintitles/maintitle[@inherited='0']/text()"/>
					<xsl:with-param name="length" select="75"/>
				</xsl:call-template>
			</xsl:variable>					
			<table border="0" cellspacing="0">
				<tr>
					<td id="detailed-cube">
						<!--<xsl:value-of select="i18n:translate('metaData.type.article')"/>-->
						<img src="{$WebApplicationBaseURL}images/artikel.gif"/>
					</td>
					<td id="detailed-mainheadline">
						<xsl:value-of select="$maintitle_shorted"/>
					</td>
					<td id="detailed-links" colspan="1" rowspan="3">
						<table id="detailed-contenttable" border="0" cellspacing="0">
							
							<xsl:call-template name="printDerivates">
								<xsl:with-param name="obj_id" select="@ID"/>
							</xsl:call-template>
							
						</table>
					</td>
				</tr>
				<tr>
					<td colspan="2" rowspan="1">
						<table cellspacing="0" cellpadding="0" id="detailed-view">
							<tr>
								<td id="detailed-headlines"><xsl:value-of select="i18n:translate('metaData.headlines.formaldiscr')"/></td>
							</tr>
							
							<!--1***maintitle*************************************-->	
							<!-- take care on cuted main title-->
							<tr>
								<td valign="top" id="detailed-labels">
									<xsl:value-of select="i18n:translate('editormask.labels.bibdescript')"/>
								</td>
								<td class="metavalue">
									<xsl:variable name="mainTitle">
										<xsl:value-of select="./metadata/maintitles/maintitle[@inherited='0']"/>
									</xsl:variable>
									<xsl:choose>
										<!-- cuted -->
										<xsl:when test="string-length($mainTitle)>75)">
											<xsl:value-of select="concat('...',substring-after($mainTitle, substring-before($maintitle_shorted,'...')))"/>											
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$mainTitle" />
										</xsl:otherwise>
									</xsl:choose>									
								</td>
							</tr>
							
							<!--2***subtitle*************************************-->
							<xsl:call-template name="printMetaDate_typeSensitive">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/subtitles/subtitle" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.subtitle')" name="label"/>
								<xsl:with-param name="typeClassi" select="'jportal_class_00000006'"/>
								<xsl:with-param name="mode" select="'text'"/>
							</xsl:call-template>
							
							<!--3***participant*************************************-->
							<xsl:call-template name="printMetaDate_typeSensitive">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/participants/participant" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.participants_label')" name="label"/>
								<xsl:with-param name="typeClassi" select="'jportal_class_00000007'"/>								
								<xsl:with-param name="mode" select="'xlink'"/>
							</xsl:call-template>
							<!--4***date*************************************-->
							<xsl:call-template name="printMetaDate_typeSensitive">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/dates/date[@inherited='0']" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.date_label')" name="label"/>
								<xsl:with-param name="typeClassi" select="'jportal_class_00000008'"/>								
								<xsl:with-param name="mode" select="'date'"/>								
							</xsl:call-template>
							
							<!--11***size*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/sizes/size" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.size')" name="label"/>
							</xsl:call-template>
							
							<!--7***identi*************************************-->
							<xsl:call-template name="printMetaDate_typeSensitive">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/identis/identi" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.identi')" name="label"/>
								<xsl:with-param name="typeClassi" select="'jportal_class_00000010'"/>								
								<xsl:with-param name="mode" select="'text'"/>
							</xsl:call-template>
							
							<tr id="detailed-dividingline">
								<td colspan="2">
									<hr noshade="noshade" width="460"/>
								</td>
							</tr>
							<tr>
								<td id="detailed-headlines"><xsl:value-of select="i18n:translate('metaData.headlines.contantdiscr')"/></td>
							</tr>
							
							<!--5***keyword*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/keywords/keyword" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.keyword')" name="label"/>
							</xsl:call-template>
							
							<!--6***abstract*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/abstracts/abstract" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.abstract')"
									name="label"/>
							</xsl:call-template>
							<!--8***note*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/notes/note" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.note')" name="label"/>
							</xsl:call-template>
							<!--9***type*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/types/type" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.type')" name="label"/>
							</xsl:call-template>
							
							<!--10***rubric*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/rubrics/rubric" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.rubric')" name="label"/>
							</xsl:call-template>
							<!--10***classipub*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/classispub/classipub" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.classipub')" name="label"/>
							</xsl:call-template>
							<!--10***classipub2*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/classispub2/classipub2" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.classipub2')" name="label"/>
							</xsl:call-template>
							<!--10***classipub3*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/classispub3/classipub3" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.classipub3')" name="label"/>
							</xsl:call-template>							
							
														
							<!--12***ref*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/refs/ref" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.pub_reference')" name="label"/>
							</xsl:call-template>
							
							<tr id="detailed-dividingline">
								<td colspan="2">
									<hr noshade="noshade" width="460"/>
								</td>
							</tr>
							<tr>
								<td id="detailed-headlines"><xsl:value-of select="i18n:translate('metaData.headlines.systemdata')"/></td>
							</tr>
							
							<!--*** Created ************************************* -->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./service/servdates/servdate[@type='createdate']" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.datecr')" name="label"/>
							</xsl:call-template>
							
							<!--*** Last Modified ************************************* -->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./service/servdates/servdate[@type='modifydate']" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.datemod')" name="label"/>
							</xsl:call-template>
							
							<!--*** MyCoRe-ID ************************************* -->
							<tr>
								<td class="metaname" style="text-align:right;  padding-right: 5px;">
									<xsl:value-of select="i18n:translate('metaData.ID')"/>
									
								</td>
								<td class="metavalue">
									<xsl:value-of select="./@ID"/>
								</td>
							</tr>
							
							<!-- Static URL ************************************************** -->
							<xsl:call-template name="get.staticURL">
								<xsl:with-param name="stURL" select="$staticURL"/>
							</xsl:call-template>
							<xsl:call-template name="emptyRow"/>

							<!-- Administration ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
							<xsl:call-template name="showAdminHead"/>														
							<!--*** Editor Buttons ************************************* -->
							<!--      <xsl:call-template name="editobject_with_der">
							<xsl:with-param select="$accessedit" name="accessedit"/>
							<xsl:with-param select="./@ID" name="id"/>
							</xsl:call-template>-->
							<xsl:variable name="params_dynamicClassis">
								<xsl:call-template name="get.params_dynamicClassis"/>
							</xsl:variable>
							<xsl:call-template name="editobject_with_der">
								<xsl:with-param select="$accessedit" name="accessedit"/>
								<xsl:with-param select="./@ID" name="id"/>
								<xsl:with-param select="$params_dynamicClassis" name="layout"/>
							</xsl:call-template>
						</table>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>
	
	<!-- =================================================================================================================== -->
	<xsl:template name="Derobjects3">
		<xsl:param name="obj_host"/>
		
		<xsl:param name="staticURL"/>
		<xsl:param name="layout"/>
		<xsl:param name="xmltempl"/>
		<xsl:variable select="substring-before(substring-after(./@ID,'_'),'_')" name="type"/>
		<xsl:variable name="suffix">
			<xsl:if test="string-length($layout)&gt;0">
				<xsl:value-of select="concat('&amp;layout=',$layout)"/>
			</xsl:if>
		</xsl:variable>
		
		<xsl:if test="./structure/derobjects">
			<tr>
				<td style="vertical-align:top;" class="metaname">
					<xsl:value-of select="i18n:translate('metaData.jparticle.[derivates]')"/>
				</td>
				<td class="metavalue">
					<xsl:if test="$objectHost != 'local'">
						<a href="{$staticURL}"><xsl:value-of select="i18n:translate('metaData.origserver')"/></a>
						
					</xsl:if>
					<xsl:if test="$objectHost = 'local'">
						<xsl:for-each select="./structure/derobjects/derobject">
							<table cellpadding="0" cellspacing="0" border="0" width="100%">
								<tr>
									<td valign="top" align="left">
										<div class="derivateBox">
											<xsl:variable select="@xlink:href" name="deriv"/>
											<xsl:variable select="concat('mcrobject:',$deriv)" name="derivlink"/>
											
											<xsl:variable select="document($derivlink)" name="derivate"/>
											<xsl:apply-templates select="$derivate/mycorederivate/derivate/internals"/>
											<xsl:apply-templates select="$derivate/mycorederivate/derivate/externals"/>
										</div>
									</td>
									<xsl:if test="acl:checkPermission(./@ID,'writedb')">
										<td align="right" valign="top">
											<a
												href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;re_mcrid={../../../@ID}&amp;se_mcrid={@xlink:href}&amp;te_mcrid={@xlink:href}&amp;todo=saddfile{$suffix}{$xmltempl}">
												<img title="Datei hinzufügen"
													src="{$WebApplicationBaseURL}images/workflow_deradd.gif"/>
												
											</a>
											<a
												href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;re_mcrid={../../../@ID}&amp;se_mcrid={@xlink:href}&amp;te_mcrid={@xlink:href}&amp;todo=seditder{$suffix}{$xmltempl}">
												<img title="Derivat bearbeiten"
													src="{$WebApplicationBaseURL}images/workflow_deredit.gif"/>
											</a>
											<a
												href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;re_mcrid={../../../@ID}&amp;se_mcrid={@xlink:href}&amp;te_mcrid={@xlink:href}&amp;todo=sdelder{$suffix}{$xmltempl}">
												<img title="Derivat löschen"
													src="{$WebApplicationBaseURL}images/workflow_derdelete.gif"/>
											</a>
										</td>
									</xsl:if>
									
								</tr>
							</table>
						</xsl:for-each>
					</xsl:if>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>