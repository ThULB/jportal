<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
	xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	xmlns:mcr="http://www.mycore.org/" xmlns:xlink="http://www.w3.org/1999/xlink"
	exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
	<xsl:param select="'local'" name="objectHost"/>
	<xsl:param name="view.objectmetadata"/>
	
	<xsl:template match="/mycoreobject" priority="2">

		<!--<xsl:variable name="test">
			<xsl:value-of select="./structure/children/child/@xlink:href"/>
		</xsl:variable>
		
		<xsl:if test="$test=''">
			<xsl:variable name="view.objectmetadata">
				<xsl:value-of select="'false'"/>
			</xsl:variable>
		</xsl:if>-->		
		<xsl:call-template name="setParameter">
			<xsl:with-param name="param" select="'view.objectmetadata'"/>
			<xsl:with-param name="paramValue" select="$view.objectmetadata"/>
			<xsl:with-param name="labelON" select="'&gt; Detailansicht'"/>
			<xsl:with-param name="labelOFF" select="'&gt; Blätteransicht'"/>
		</xsl:call-template>
		
		<xsl:choose>
			<xsl:when test="$view.objectmetadata = 'false'">
				
				<xsl:choose>
					<!--
					<xsl:when test="document($accessurl)/mycoreaccesscheck/accesscheck/@return = 'true'">
					-->
					<xsl:when
						test="($objectHost != 'local') or acl:checkPermission(/mycoreobject/@ID,'read')">
						<!-- if access granted: print metadata -->
						<xsl:apply-templates select="." mode="present">
							<xsl:with-param name="obj_host" select="$objectHost"/>
						</xsl:apply-templates>
						<!-- IE Fix for padding and border -->
						<hr/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of
							select="i18n:translate('metaData.accessDenied')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<table cellpadding="0" cellspacing="0">
					<tr>
						
						<td id="leaf-headline2">
							
							<xsl:if test="./structure/parents/parent/@xlink:href!=''">
							<xsl:variable name="parent_name">
								<xsl:value-of
									select="document(concat('mcrobject:',./structure/parents/parent/@xlink:href))/mycoreobject/metadata/maintitles/maintitle/text()"/>
							</xsl:variable>
							<xsl:call-template name="objectLinking">
								<xsl:with-param name="obj_id" select="./structure/parents/parent/@xlink:href"/>
								<xsl:with-param name="obj_name" select="$parent_name"/>
							</xsl:call-template>
							</xsl:if>
							>
							<span>
							<xsl:value-of
								select="/mycoreobject/metadata/maintitles/maintitle/text()"/>
							</span>
						</td>
					</tr>
					<tr>
						<td id="leaf-headline1">
							_________________________________________________</td>
						
					</tr>
					<tr>
						<td colspan="2" rowspan="1" id="leaf-headline2"> 
						</td>
						<td colspan="1" rowspan="2" id="leaf-preview">
							<img
								src="{concat($WebApplicationBaseURL,'preview.png')}"/>
						</td>
					</tr>
					<tr>
						<td id="leaf-leafarea">
							<br/>
							<table cellpadding="0" cellspacing="0" border="0">
								<xsl:variable select="." name="context"/>
								<xsl:for-each
									select="./structure/children/child">
									<tr id="leaf-all">
										<td id="leaf-front">
											<div></div>
										</td>
										<td id="leaf-linkarea">
											
											<xsl:variable name="name">
												<xsl:value-of
													select="document(concat('mcrobject:',@xlink:href))/mycoreobject/metadata/maintitles/maintitle/text()"/>
											</xsl:variable>
											<!--<xsl:variable name="date">
											<xsl:call-template name="dateConvert">
											<xsl:with-param name="dateUnconverted"
											select="document(concat('mcrobject:',@xlink:href))/mycoreobject/metadata/dates/date[@type='published']"/>
											</xsl:call-template>
											</xsl:variable>-->
											<xsl:call-template
												name="objectLinking">
												<xsl:with-param name="obj_id"
													select="@xlink:href"/>
												<xsl:with-param name="obj_name"
													select="$name"/>
											</xsl:call-template>
											
										</td>
									</tr>
									<tr id="leaf-whitespaces"></tr>
								</xsl:for-each>
							</table>
						</td>
					</tr>
				</table>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="dateConvert">
		<xsl:param name="dateUnconverted"/>
		<xsl:variable name="format">
			<xsl:choose>
				<xsl:when test="string-length(normalize-space(.))=4">
					<xsl:value-of select="i18n:translate('metaData.dateYear')"/>
				</xsl:when>
				<xsl:when test="string-length(normalize-space(.))=7">
					<xsl:value-of
						select="i18n:translate('metaData.dateYearMonth')"/>
				</xsl:when>
				<xsl:when test="string-length(normalize-space(.))=10">
					<xsl:value-of
						select="i18n:translate('metaData.dateYearMonthDay')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="i18n:translate('metaData.dateTime')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:call-template name="formatISODate">
			<xsl:with-param name="date" select="."/>
			<xsl:with-param name="format" select="$format"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="objectLinking">
		<xsl:param name="obj_id"/>
		<xsl:param name="obj_name"/>
		<!-- 
		LOCAL REQUEST
		-->
		<xsl:if test="$objectHost = 'local'">
			<xsl:variable name="mcrobj"
				select="document(concat('mcrobject:',$obj_id))/mycoreobject"/>
			<xsl:choose>
				<xsl:when test="acl:checkPermission($obj_id,'read')">
					<a
						href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}">
						<xsl:value-of select="$obj_name"/>
					</a>
				</xsl:when>
				<xsl:otherwise>
					<!-- Build Login URL for LoginServlet -->
					<xsl:variable xmlns:encoder="xalan://java.net.URLEncoder"
						name="LoginURL"
						select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?url=', encoder:encode( string( $RequestURL ) ) )"/>
					<xsl:apply-templates select="$mcrobj" mode="resulttitle"/>
					&#160;
					<a href="{$LoginURL}">
						<img
							src="{concat($WebApplicationBaseURL,'images/paper_lock.gif')}"/>
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
			<a
				href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}?host={@host}">
				<xsl:apply-templates select="$mcrobj" mode="resulttitle"/>
			</a>
		</xsl:if>
	</xsl:template>
	
	<!--Template for result list hit: see results.xsl-->
	<xsl:template match="mcr:hit[contains(@id,'_jpvolume_')]">
		<xsl:param name="mcrobj"/>
		<xsl:param name="mcrobjlink"/>
		<xsl:variable select="100" name="DESCRIPTION_LENGTH"/>
		<xsl:variable select="@host" name="host"/>
		<xsl:variable name="obj_id">
			
			<xsl:value-of select="@id"/>
		</xsl:variable>
		<tr>
			<td colspan="2" class="resultTitle">
				<xsl:copy-of select="$mcrobjlink"/>
			</td>
		</tr>
		<tr>
			<td colspan="2" class="description">
				
				<div>please edit &lt;template
					match=mcr:hit[contains(@id,'_jpvolume_')]&gt; for object type:
					jpvolume</div>
				<!--
				you could insert here a preview for your metadata, e.g.
				uncomment the next block and replace "your-tags/here"
				by something of your metadata
				-->
				<!--
				<div>D
				short description:
				<xsl:call-template name="printI18N">
				<xsl:with-param name="nodes" select="$mcrobj/metadata/your-tags/here" />
				</xsl:call-template>
				</div>
				-->
				<span class="properties">
					<xsl:variable name="date">
						<xsl:call-template name="formatISODate">
							<xsl:with-param
								select="$mcrobj/service/servdates/servdate[@type='modifydate']"
								name="date"/>
							
							<xsl:with-param
								select="i18n:translate('metaData.date')"
								name="format"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of
						select="i18n:translate('results.lastChanged',$date)"/>
				</span>
			</td>
		</tr>
	</xsl:template>
	
	
	<!--Template for generated link names and result titles: see mycoreobject.xsl, results.xsl, MyCoReLayout.xsl-->
	
	<xsl:template priority="1" mode="resulttitle"
		match="/mycoreobject[contains(@ID,'_jpvolume_')]">
		<xsl:choose>
			<!--
			you could insert any title-like metadata here, e.g.
			replace "your-tags/here" by something of your metadata
			-->
			<xsl:when test="./metadata/your-tags">
				<xsl:call-template name="printI18N">
					<xsl:with-param select="./metadata/your-tags/here"
						name="nodes"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				
				<xsl:value-of select="@label"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--Template for title in metadata view: see mycoreobject.xsl-->
	<xsl:template priority="1" mode="title"
		match="/mycoreobject[contains(@ID,'_jpvolume_')]">
		<xsl:choose>
			<!--
			you could insert any title-like metadata here, e.g.
			replace "your-tags/here" by something of your metadata
			-->
			<xsl:when test="./metadata/your-tags">
				
				<xsl:call-template name="printI18N">
					<xsl:with-param select="./metadata/your-tags/here"
						name="nodes"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@ID"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<!-- =================================================================================================================================== -->
	<!--Template for metadata view: see mycoreobject.xsl-->
	<xsl:template priority="1" mode="present"
		match="/mycoreobject[contains(@ID,'_jpvolume_')]">
		<xsl:param select="$objectHost" name="obj_host"/>
		<xsl:param name="accessedit"/>
		<xsl:param name="accessdelete"/>
		<xsl:variable name="objectBaseURL">
			<xsl:if test="$objectHost != 'local'">
				<xsl:value-of
					select="document('webapp:hosts.xml')/mcr:hosts/mcr:host[@alias=$objectHost]/mcr:url[@type='object']/@href"/>
			</xsl:if>
			
			<xsl:if test="$objectHost = 'local'">
				<xsl:value-of
					select="concat($WebApplicationBaseURL,'receive/')"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="staticURL">
			<xsl:value-of select="concat($objectBaseURL,@ID)"/>
		</xsl:variable>
		<div id="detailed-frame">
			<table border="0" cellspacing="0">
				<tr>
					<td id="detailed-cube">
						<xsl:value-of select="i18n:translate('metaData.type.volume')"/>
					</td>
					<td id="detailed-mainheadline">
						<xsl:value-of select="./metadata/maintitles/maintitle"/>
					</td>
					<td id="detailed-links" colspan="1" rowspan="3">
						<table id="detailed-contenttable" border="0"
							cellspacing="0">
							<tr id="detailed-contents">
								<td>
									<div>PDF ansehen >></div>
								</td>
							</tr>
							<tr id="detailed-whitespaces">
								<td></td>
							</tr>
							<tr id="detailed-contents">
								<td>
									<div>PDF ansehen >></div>
								</td>
							</tr>
							<tr id="detailed-whitespaces">
								<td></td>
							</tr>
							<tr id="detailed-contents">
								<td>
									<div>JPEG ansehen >></div>
								</td>
							</tr>
							<tr id="detailed-whitespaces">
								<td></td>
							</tr>
							<tr id="detailed-contents">
								<td>
									<div>TIFF ansehen >></div>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td colspan="2" rowspan="1">
						<table cellspacing="0" cellpadding="0" id="detailed-view">
							<tr>
								<td id="detailed-headlines">Formale
									Beschreibung</td>
							</tr>
							
							
							<!--1***maintitle*************************************-->
							
							<xsl:call-template name="printMetaDates">
								<xsl:with-param
									select="./metadata/maintitles/maintitle"
									name="nodes"/>
								<xsl:with-param
									select="i18n:translate('editor.search.document.maintitle')"
									name="label"/>
							</xsl:call-template>
							
							
							<!--2***subtitle*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param
									select="./metadata/subtitles/subtitle[@type='short']"
									name="nodes"/>
								<xsl:with-param
									select="i18n:translate('editor.search.document.subtitle')"
									name="label"/>
							</xsl:call-template>
							
							
							<!--3***subtitle*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param
									select="./metadata/subtitles/subtitle[@type='title_rezensation']"
									name="nodes"/>
								<xsl:with-param
									select="i18n:translate('metaData.jpvolume.subtitle.title_rezensation')"
									name="label"/>
							</xsl:call-template>
							<!--4***date*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param
									select="./metadata/dates/date[@type='published']"
									name="nodes"/>
								<xsl:with-param
									select="i18n:translate('editor.search.document.date')"
									name="label"/>
								
							</xsl:call-template>
							<!--5***note*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'true'"
									name="volume-node"/>
								<xsl:with-param select="./metadata/notes/note"
									name="nodes"/>
								<xsl:with-param
									select="i18n:translate('editor.search.document.note')"
									name="label"/>
							</xsl:call-template>
							<tr id="detailed-dividingline">
								<td style="text-align:right;">
									_________________________________</td>
								<td>_________________________________</td>
							</tr>
							<tr>
								<td id="detailed-headlines">Systemdaten</td>
							</tr>
							<!--<!-#-6***hidden_jpjournalID*************************************-#->
							<xsl:call-template name="printMetaDates">
							<xsl:with-param select="'right'" name="textalign"/>
							<xsl:with-param select="./metadata/hidden_jpjournalsID/hidden_jpjournalID" name="nodes"/>
							<xsl:with-param select="i18n:translate('metaData.jpvolume.hidden_jpjournalID')" name="label"/>
							</xsl:call-template>
							<!-#-7***hidden_rubricID*************************************-#->
							<xsl:call-template name="printMetaDates">
							<xsl:with-param select="'right'" name="textalign"/>
							<xsl:with-param select="./metadata/hidden_rubricsID/hidden_rubricID" name="nodes"/>
							<xsl:with-param select="i18n:translate('metaData.jpvolume.hidden_rubricID')" name="label"/>
							</xsl:call-template>
							<!-#-8***hidden_pubTypeID*************************************-#->
							<xsl:call-template name="printMetaDates">
							<xsl:with-param select="'right'" name="textalign"/>
							<xsl:with-param select="./metadata/hidden_pubTypesID/hidden_pubTypeID" name="nodes"/>
							<xsl:with-param select="i18n:translate('metaData.jpvolume.hidden_pubTypeID')" name="label"/>
							</xsl:call-template>-->
							
							
							<!--<!-#-*** List children per object type ************************************* -#->
							<!-#-
							1.) get a list of objectTypes of all child elements
							2.) remove duplicates from this list
							3.) for-each objectTyp id list child elements
							-#->
							<xsl:variable name="objectTypes">
							<xsl:for-each
							select="./structure/children/child/@xlink:href">
							<id>
							<xsl:copy-of
							select="substring-before(substring-after(.,'_'),'_')"/>
							
							</id>
							</xsl:for-each>
							</xsl:variable>
							<xsl:variable
							select="xalan:nodeset($objectTypes)/id[not(.=following::id)]"
							name="unique-ids"/>
							<!-#-
							the for-each would iterate over <id> with root not beeing /mycoreobject
							so we save the current node in variable context to access needed nodes
							-#->
							<xsl:variable select="." name="context"/>
							<xsl:for-each select="$unique-ids">
							<xsl:variable select="." name="thisObjectType"/>
							<xsl:variable name="label">
							
							<xsl:choose>
							<xsl:when
							test="count($context/structure/children/child[contains(@xlink:href,$thisObjectType)])=1">
							<xsl:value-of
							select="i18n:translate(concat('metaData.',$thisObjectType,'.[singular]'))"/>
							</xsl:when>
							<xsl:otherwise>
							<xsl:value-of
							select="i18n:translate(concat('metaData.',$thisObjectType,'.[plural]'))"/>
							</xsl:otherwise>
							</xsl:choose>
							</xsl:variable>
							
							<xsl:call-template name="printMetaDates">
							<xsl:with-param
							select="$context/structure/children/child[contains(@xlink:href, concat('_',$thisObjectType,'_'))]"
							name="nodes"/>
							<xsl:with-param select="$label" name="label"/>
							</xsl:call-template>
							</xsl:for-each>
							<xsl:call-template name="Derobjects2">
							<xsl:with-param select="$staticURL"
							name="staticURL"/>
							<xsl:with-param select="$obj_host"
							name="obj_host"/>
							</xsl:call-template>-->
							
							
							<!--*** Created ************************************* -->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param
									select="./service/servdates/servdate[@type='createdate']"
									name="nodes"/>
								<xsl:with-param
									select="i18n:translate('editor.search.document.datecr')"
									name="label"/>
							</xsl:call-template>
							
							
							<!--*** Last Modified ************************************* -->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param
									select="./service/servdates/servdate[@type='modifydate']"
									name="nodes"/>
								<xsl:with-param
									select="i18n:translate('editor.search.document.datemod')"
									name="label"/>
								
							</xsl:call-template>
							
							
							<!--*** MyCoRe-ID ************************************* -->
							<tr>
								<td class="metaname"
									style="text-align:right; padding-right: 5px;">
									<xsl:value-of
										select="concat(i18n:translate('metaData.ID'),':')"/>
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
							<!--*** Editor Buttons ************************************* -->
							<xsl:call-template name="editobject_with_der">
								<xsl:with-param select="$accessedit"
									name="accessedit"/>
								<xsl:with-param select="./@ID" name="id"/>
							</xsl:call-template>
							<xsl:call-template name="addChild2">
								
								<xsl:with-param name="id" select="./@ID"/>
								<xsl:with-param name="types"
									select="'jpvolume jparticle'"/>
							</xsl:call-template>
							<xsl:variable name="params_dynamicClassis">
								<xsl:call-template
									name="get.params_dynamicClassis"/>
							</xsl:variable>
							<xsl:call-template name="addChild2">
								<xsl:with-param name="id" select="./@ID"/>
								<xsl:with-param name="types"
									select="'jparticle'"/>
								<xsl:with-param select="$params_dynamicClassis"
									name="layout"/>
							</xsl:call-template>
						</table>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>
	
	
	<!-- =================================================================================================================================== -->
	
	<xsl:template name="addChild2">
		<xsl:param name="id"/>
		<xsl:param name="layout"/>
		<xsl:param name="types"/>
		<xsl:param
			select="concat('&amp;_xml_structure%2Fparents%2Fparent%2F%40href=',$id)"
			name="xmltempl"/>
		<xsl:variable name="suffix">
			
			<xsl:if test="string-length($layout)&gt;0">
				<xsl:value-of select="concat('&amp;layout=',$layout)"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="typeToken">
			<xsl:call-template name="Tokenizer">
				<xsl:with-param select="$types" name="string"/>
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:if test="acl:checkPermission($id,'writedb')">
			<tr>
				<td class="metaname">
					<xsl:value-of
						select="concat(i18n:translate('metaData.addChildObject'),':')"/>
				</td>
				<td class="metavalue">
					<ul>
						<xsl:for-each select="xalan:nodeset($typeToken)/token">
							<xsl:variable select="." name="type"/>
							
							<li>
								<a
									href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;step=author&amp;todo=wnewobj{$suffix}{$xmltempl}">
									<xsl:value-of
										select="i18n:translate(concat('metaData.',$type,'.[singular]'))"/>
								</a>
							</li>
						</xsl:for-each>
					</ul>
				</td>
			</tr>
			
		</xsl:if>
	</xsl:template>
	<xsl:template name="Derobjects2">
		<xsl:param name="obj_host"/>
		<xsl:param name="staticURL"/>
		<xsl:param name="layout"/>
		<xsl:param name="xmltempl"/>
		<xsl:variable select="substring-before(substring-after(./@ID,'_'),'_')"
			name="type"/>
		<xsl:variable name="suffix">
			
			<xsl:if test="string-length($layout)&gt;0">
				<xsl:value-of select="concat('&amp;layout=',$layout)"/>
			</xsl:if>
		</xsl:variable>
		<xsl:if test="./structure/derobjects">
			<tr>
				<td style="vertical-align:top;" class="metaname">
					<xsl:value-of
						select="i18n:translate('metaData.jpvolume.[derivates]')"/>
				</td>
				
				<td class="metavalue">
					<xsl:if test="$objectHost != 'local'">
						<a href="{$staticURL}">nur auf original Server</a>
					</xsl:if>
					<xsl:if test="$objectHost = 'local'">
						<xsl:for-each select="./structure/derobjects/derobject">
							<table cellpadding="0" cellspacing="0" border="0"
								width="100%">
								<tr>
									
									<td valign="top" align="left">
										<div class="derivateBox">
											<xsl:variable select="@xlink:href"
												name="deriv"/>
											<xsl:variable
												select="concat('mcrobject:',$deriv)"
												name="derivlink"/>
											<xsl:variable
												select="document($derivlink)"
												name="derivate"/>
											<xsl:apply-templates
												select="$derivate/mycorederivate/derivate/internals"/>
											<xsl:apply-templates
												select="$derivate/mycorederivate/derivate/externals"/>
										</div>
									</td>
									
									<xsl:if
										test="acl:checkPermission(./@ID,'writedb')">
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
	
	<!-- ************************ -->
	<xsl:template name="setParameter">
		<xsl:param name="param"/>
		<xsl:param name="paramValue"/> <!-- to verify within template -->
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
				<xsl:with-param name="par"
					select="concat('XSL.',$param,'.SESSION')"/>
			</xsl:call-template>
		</xsl:variable>
		<table id="switch">
			<tr>
				<td>
					<xsl:choose>
						<xsl:when test="$paramValue='false'">
							<xsl:variable name="targetURL_withParam">
								<xsl:call-template name="UrlSetParam">
									<xsl:with-param name="url"
										select="$targetURL"/>
									<xsl:with-param name="par"
										select="concat('XSL.',$param,'.SESSION')"/>
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
									<xsl:with-param name="url"
										select="$targetURL"/>
									<xsl:with-param name="par"
										select="concat('XSL.',$param,'.SESSION')"/>
									<xsl:with-param name="value"
										select="'false'"/>
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
	
	<xsl:template name="printMetaDates">
  <!-- prints a table row for a given nodeset -->
	<xsl:param name="volume-node" />  
	<xsl:param name="nodes" />
    <xsl:param name="label" select="local-name($nodes[1])" />
    <xsl:if test="$nodes">
			<xsl:if test="$volume-node='true'">
				<tr id="detailed-dividingline">
					<td style="text-align:right;">_________________________________</td>
					<td>_________________________________</td>
				</tr>
				<tr>
					<td id="detailed-headlines">Inhaltliche Beschreibung</td>
				</tr>
			</xsl:if>
	  <tr>
		<td valign="top" id="detailed-labels">
			<xsl:value-of select="$label"/>
		</td>
        <td class="metavalue">
          <xsl:for-each select="$nodes">
            <xsl:choose>
              <xsl:when test="../@class='MCRMetaClassification'">
                <xsl:call-template name="printClass">
                  <xsl:with-param name="nodes" select="." />
                  <xsl:with-param name="host" select="$objectHost" />
                </xsl:call-template>
                <xsl:call-template name="printClassInfo">
                  <xsl:with-param name="nodes" select="." />
                  <xsl:with-param name="host" select="$objectHost" />
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="../@class='MCRMetaISO8601Date'">
                <xsl:variable name="format">
                  <xsl:choose>
                    <xsl:when test="string-length(normalize-space(.))=4">
                      <xsl:value-of select="i18n:translate('metaData.dateYear')" />
                    </xsl:when>
                    <xsl:when test="string-length(normalize-space(.))=7">
                      <xsl:value-of select="i18n:translate('metaData.dateYearMonth')" />
                    </xsl:when>
                    <xsl:when test="string-length(normalize-space(.))=10">
                      <xsl:value-of select="i18n:translate('metaData.dateYearMonthDay')" />
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="i18n:translate('metaData.dateTime')" />
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <xsl:call-template name="formatISODate">
                  <xsl:with-param name="date" select="." />
                  <xsl:with-param name="format" select="$format" />
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="../@class='MCRMetaLinkID'">
                <xsl:call-template name="objectLink">
                  <xsl:with-param name="obj_id" select="@xlink:href" />
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="printI18N">
                  <xsl:with-param name="nodes" select="." />
                  <xsl:with-param name="host" select="$objectHost" />
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="position()!=last()"><br/></xsl:if>
          </xsl:for-each>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>
	
</xsl:stylesheet>