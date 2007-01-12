<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	xmlns:mcr="http://www.mycore.org/" xmlns:xlink="http://www.w3.org/1999/xlink"
	exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
	<xsl:param select="'local'" name="objectHost"/>
	<!--	<xsl:include href="mcr-module-startIview.xsl"/>-->
	<!--Template for result list hit: see results.xsl-->
	<xsl:template match="mcr:hit[contains(@id,'_jpjournal_')]">
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
					
					<xsl:value-of select="i18n:translate('metaData.type.journal')"/>
					
				</td>
				<td id="leaf-linkarea2">
					<xsl:variable name="name">
							<xsl:call-template name="ShortenText">
								<xsl:with-param name="text" select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()"/>
								<xsl:with-param name="length" select="25"/>
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
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<xsl:call-template name="printDerivates">
					<xsl:with-param name="obj_id" select="@id"/>
					<xsl:with-param name="knoten" select="$cXML"/>
				</xsl:call-template>
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
	<xsl:template priority="1" mode="resulttitle" match="/mycoreobject[contains(@ID,'_jpjournal_')]">
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
	<xsl:template priority="1" mode="title" match="/mycoreobject[contains(@ID,'_jpjournal_')]">
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
	<xsl:template priority="1" mode="present" match="/mycoreobject[contains(@ID,'_jpjournal_')]">
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
			<table border="0" cellspacing="0">
				<tr>
					<td id="detailed-cube">
						<xsl:value-of select="i18n:translate('metaData.type.journal')"/>
					</td>
					<td id="detailed-mainheadline">
						<xsl:variable name="maintitle_shorted">
							<xsl:call-template name="ShortenText">
								<xsl:with-param name="text" select="./metadata/maintitles/maintitle/text()"/>
								<xsl:with-param name="length" select="25"/>
							</xsl:call-template>
						</xsl:variable>						
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
								<td id="detailed-headlines">Formale Beschreibung</td>
							</tr>
							
							<!--1***maintitle*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/maintitles/maintitle" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.maintitle')" name="label"/>
							</xsl:call-template>
							
							<!--2***subtitle*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/subtitles/subtitle[@type='title_spokenAbout']"
									name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.subtitle')" name="label"/>
								
							</xsl:call-template>
							
							<!--3***participant*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/participants/participant" name="nodes"/>
								<xsl:with-param select="i18n:translate('metaData.jpjournal.participant')" name="label"/>
							</xsl:call-template>
							<!--4***date*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/dates/date[@type='published_from']" name="nodes"/>
								
								<xsl:with-param select="i18n:translate('metaData.jpjournal.date.published_from')"
									name="label"/>
							</xsl:call-template>
							<!--5***date*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/dates/date[@type='published_until']" name="nodes"/>
								<xsl:with-param select="i18n:translate('metaData.jpjournal.date.published_until')"
									name="label"/>
							</xsl:call-template>
							<tr id="detailed-dividingline">
								<td colspan="2">
									<hr noshade="noshade" width="460"/>
								</td>
							</tr>
							<tr>
								<td id="detailed-headlines">Inhaltliche Beschreibung</td>
							</tr>
							
							<!--6***classi*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/classis/classi" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.classi')" name="label"/>
							</xsl:call-template>
							
							<!--7***abstract*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/abstracts/abstract[@type='extend']" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.abstract')" name="label"/>
							</xsl:call-template>
							
							<!--8***identi*************************************-->
							
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/identis/identi[@type='issn']" name="nodes"/>
								<xsl:with-param select="i18n:translate('metaData.jpjournal.identi.issn')" name="label"/>
							</xsl:call-template>
							
							<!--9***identi*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/identis/identi[@type='nbn']" name="nodes"/>
								<xsl:with-param select="i18n:translate('metaData.jpjournal.identi.nbn')" name="label"/>
							</xsl:call-template>
							
							<!--10***language*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/languages/language" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.language')" name="label"/>
							</xsl:call-template>
							
							<!--11***right*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/rights/right" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.right')" name="label"/>
								
							</xsl:call-template>
							
							<!--12***note*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/notes/note" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.note')" name="label"/>
							</xsl:call-template>
							
							<!--<!-#-13***hidden_jpjournalID*************************************-#->
							<xsl:call-template name="printMetaDates">
							<xsl:with-param select="'right'" name="textalign"/>
							<xsl:with-param
							select="./metadata/hidden_jpjournalsID/hidden_jpjournalID"
							name="nodes"/>
							
							<xsl:with-param
							select="i18n:translate('metaData.jpjournal.hidden_jpjournalID')"
							name="label"/>
							</xsl:call-template>
							
							<!-#-14***hidden_rubricID*************************************-#->
							<xsl:call-template name="printMetaDates">
							<xsl:with-param select="'right'" name="textalign"/>
							<xsl:with-param
							select="./metadata/hidden_rubricsID/hidden_rubricID"
							name="nodes"/>
							<xsl:with-param
							select="i18n:translate('metaData.jpjournal.hidden_rubricID')"
							name="label"/>
							</xsl:call-template>
							
							<!-#-15***hidden_pubTypeID*************************************-#->
							<xsl:call-template name="printMetaDates">
							<xsl:with-param select="'right'" name="textalign"/>
							<xsl:with-param
							select="./metadata/hidden_pubTypesID/hidden_pubTypeID"
							name="nodes"/>
							<xsl:with-param
							select="i18n:translate('metaData.jpjournal.hidden_pubTypeID')"
							name="label"/>
							</xsl:call-template>-->
							<tr id="detailed-dividingline">
								<td colspan="2">
									<hr noshade="noshade" width="460"/>
								</td>
							</tr>
							<tr>
								<td id="detailed-headlines">Systemdaten</td>
							</tr>
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
							<xsl:with-param select="'right'" name="textalign"/>
							<xsl:with-param
							select="$context/structure/children/child[contains(@xlink:href, concat('_',$thisObjectType,'_'))]"
							name="nodes"/>
							<xsl:with-param select="$label" name="label"/>
							</xsl:call-template>
							</xsl:for-each>
							<xsl:call-template name="Derobjects">
							<xsl:with-param select="$staticURL"
							name="staticURL"/>
							<xsl:with-param select="$obj_host"
							name="obj_host"/>
							</xsl:call-template>-->
							<!--*** Created ************************************* -->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./service/servdates/servdate[@type='createdate']" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.datecr')" name="label"/>
							</xsl:call-template>
							
							<!--*** Last Modified ************************************* -->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./service/servdates/servdate[@type='modifydate']" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.datemod')" name="label"/>
								
							</xsl:call-template>
							
							<!--*** MyCoRe-ID ************************************* -->
							<tr>
								<td class="metaname" style="text-align:right;  padding-right: 5px;">
									<xsl:value-of select="concat(i18n:translate('metaData.ID'),':')"/>
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
								<xsl:with-param select="$accessedit" name="accessedit"/>
								<xsl:with-param select="./@ID" name="id"/>
							</xsl:call-template>
							<xsl:call-template name="addChild">
								
								<xsl:with-param name="id" select="./@ID"/>
								<xsl:with-param name="types" select="'jpvolume'"/>
							</xsl:call-template>
						</table>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>
	<xsl:template name="addChild">
		<xsl:param name="id"/>
		<xsl:param name="layout"/>
		<xsl:param name="types"/>
		<xsl:param select="concat('&amp;_xml_structure%2Fparents%2Fparent%2F%40href=',$id)" name="xmltempl"/>
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
					<xsl:value-of select="concat(i18n:translate('metaData.addChildObject'),':')"/>
				</td>
				<td class="metavalue">
					<ul>
						<xsl:for-each select="xalan:nodeset($typeToken)/token">
							<xsl:variable select="." name="type"/>
							
							<li>
								<a
									href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;step=author&amp;todo=wnewobj{$suffix}{$xmltempl}">
									<xsl:value-of select="i18n:translate(concat('metaData.',$type,'.[singular]'))"/>
								</a>
							</li>
						</xsl:for-each>
					</ul>
				</td>
			</tr>
			
		</xsl:if>
	</xsl:template>
	<xsl:template name="Derobjects">
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
					<xsl:value-of select="i18n:translate('metaData.jpjournal.[derivates]')"/>
				</td>
				
				<td class="metavalue">
					<xsl:if test="$objectHost != 'local'">
						<a href="{$staticURL}">nur auf original Server</a>
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
			<!-- MCR-IView ..start -->
			<!-- example implementation -->
			<!--			<xsl:if test="$objectHost = 'local'">
			<xsl:for-each select="./structure/derobjects/derobject">
			<xsl:variable select="@xlink:href" name="deriv"/>
			<xsl:variable name="firstSupportedFile">
			<xsl:call-template name="iview.getSupport">
			<xsl:with-param select="$deriv" name="derivID"/>
			
			</xsl:call-template>
			</xsl:variable>
			<xsl:choose>
			<xsl:when test="$firstSupportedFile != ''">
			<tr>
			<td colspan="2" class="metanone">
			<br/>
			<xsl:call-template name="iview">
			<xsl:with-param select="$deriv"
			name="derivID"/>
			
			<xsl:with-param
			select="$firstSupportedFile"
			name="pathOfImage"/>
			<xsl:with-param select="'500'"
			name="height"/>
			<xsl:with-param select="'750'"
			name="width"/>
			<xsl:with-param select="'fitToWidth'"
			name="scaleFactor"/>
			<xsl:with-param select="'normal'"
			name="display"/>
			<xsl:with-param select="'image'"
			name="style"/>
			</xsl:call-template>
			</td>
			</tr>
			
			</xsl:when>
			</xsl:choose>
			</xsl:for-each>
			</xsl:if>-->
			<!-- MCR - IView ..end -->
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>