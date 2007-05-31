<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	xmlns:mcr="http://www.mycore.org/" xmlns:xlink="http://www.w3.org/1999/xlink"
	exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
	<xsl:param select="'local'" name="objectHost"/>
	<!-- ============================================================================================================ -->
	<!--Template for result list hit: see results.xsl-->
	<xsl:template match="mcr:hit[contains(@id,'_jpinst_')]">
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
				<td id="leaf-front">
					<div>
						<img src="{$WebApplicationBaseURL}images/institut2.gif"/>
					</div>
				</td>
				<td id="leaf-linkarea">
					
					<xsl:variable name="name">
						<xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/names/name/fullname/text()"/>
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
		</table>
		<table cellspacing="0" cellpadding="0">
			<tr id="leaf-whitespaces">
				<td colspan="2">
				</td>
			</tr>
		</table>
	</xsl:template>
	<!--Template for generated link names and result titles: see mycoreobject.xsl, results.xsl, MyCoReLayout.xsl-->
	<!-- ============================================================================================================ -->	
	<xsl:template priority="1" mode="resulttitle" match="/mycoreobject[contains(@ID,'_jpinst_')]">
		<xsl:choose>
			<!--
			you could insert any title-like metadata here, e.g.
			replace "your-tags/here" by something of your metadata
			-->
			<xsl:when test="./metadata/names/name/fullname">
				<xsl:call-template name="printI18N">
					<xsl:with-param select="./metadata/names/name/fullname" name="nodes"/>
				</xsl:call-template>
				
				<xsl:if test="./metadata/addresses/address/city">
					<xsl:variable name="city">
						<xsl:call-template name="printI18N">
							<xsl:with-param select="./metadata/addresses/address/city" name="nodes"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of select="concat(' (',$city,')')"/>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@label"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ============================================================================================================ -->	
	<!--Template for title in metadata view: see mycoreobject.xsl-->
	<xsl:template priority="1" mode="title" match="/mycoreobject[contains(@ID,'_jpinst_')]">
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
	<!-- ============================================================================================================ -->	
	<!--Template for metadata view: see mycoreobject.xsl-->
	<xsl:template priority="1" mode="present" match="/mycoreobject[contains(@ID,'_jpinst_')]">
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
						<img src="{$WebApplicationBaseURL}images/institut.gif"/>
					</td>
					<td id="detailed-mainheadline">
						<xsl:variable name="name_shorted">
							<xsl:call-template name="ShortenText">
								<xsl:with-param name="text" select="./metadata/names/name/fullname"/>
								<xsl:with-param name="length" select="75"/>
							</xsl:call-template>
						</xsl:variable>						
						<xsl:value-of select="$name_shorted"/>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<table cellspacing="0" cellpadding="0" id="detailed-view">
							
							<!--1***name*************************************-->
							
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/names/name/fullname" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.AINameName')" name="label"/>
							</xsl:call-template>
							
							<!--1***nickname*************************************-->
							
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/names/name/nickname" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.AINameNick')" name="label"/>
							</xsl:call-template>
							
							<!--1***property*************************************-->
							
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/names/name/property" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.AINameProp')" name="label"/>
							</xsl:call-template>
							
							<!--2***address*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/addresses/address/street" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.AAddressStreet')" name="label"/>
							</xsl:call-template>
							<!--2***address*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/addresses/address/number" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.AAddressNumber')" name="label"/>
							</xsl:call-template>
							<!--2***address*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/addresses/address/zipcode" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.AAddressZIP')" name="label"/>
							</xsl:call-template>
							<!--2***address*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/addresses/address/city" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.AAddressCity')" name="label"/>
							</xsl:call-template>
							<!--2***address*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/addresses/address/country" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.AAddressCountry')"
									name="label"/>
							</xsl:call-template>
							
							<!--3***phone*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/phones/phone[@type='Telefon']" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.LPhone')" name="label"/>
							</xsl:call-template>
							<!--4***url*************************************-->
							<xsl:if test="./metadata/urls/url/@xlink:href">
								<tr>
									<td valign="top" id="detailed-labels">
										<xsl:value-of select="i18n:translate('editormask.labels.LURL')"/>
									</td>
									<td>
										<a href="{./metadata/urls/url/@xlink:href}">
											<xsl:value-of select="./metadata/urls/url/@xlink:href"/>
										</a>
									</td>
								</tr>
							</xsl:if>
							<!--5***email*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/emails/email" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.LEmail')" name="label"/>
							</xsl:call-template>
							<!--6***note*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/notes/note" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.LNote')" name="label"/>
							</xsl:call-template>
							
							<!-- linked articles-->
							<xsl:call-template name="listLinkedArts"/>
							
							<tr id="detailed-dividingline">
								<td colspan="2">
									<hr noshade="noshade" style="width: 100%;"/>
								</td>
							</tr>
							<tr>
								<td id="detailed-headlines">
									<xsl:value-of select="i18n:translate('metaData.headlines.systemdata')"/>
								</td>
							</tr>
							
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
								<td class="metaname" style="text-align:right; padding-right: 5px;">
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
							<xsl:call-template name="editobject">
								<xsl:with-param select="$accessedit" name="accessedit"/>
								<xsl:with-param select="./@ID" name="id"/>
							</xsl:call-template>
							<!--*** List children per object type ************************************* -->
							<!--
							1.) get a list of objectTypes of all child elements
							2.) remove duplicates from this list
							3.) for-each objectTyp id list child elements
							-->
							
							<xsl:variable name="objectTypes">
								<xsl:for-each select="./structure/children/child/@xlink:href">
									<id>
										<xsl:copy-of select="substring-before(substring-after(.,'_'),'_')"/>
									</id>
								</xsl:for-each>
							</xsl:variable>
							<xsl:variable select="xalan:nodeset($objectTypes)/id[not(.=following::id)]"
								name="unique-ids"/>
							<!--
							the for-each would iterate over <id> with root not beeing /mycoreobject
							so we save the current node in variable context to access needed nodes
							-->
							
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
						</table>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>
	<!-- ============================================================================================================ -->	
</xsl:stylesheet>