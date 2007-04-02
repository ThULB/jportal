<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	xmlns:mcr="http://www.mycore.org/" xmlns:xlink="http://www.w3.org/1999/xlink"
	exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
	<xsl:param select="'local'" name="objectHost"/>
	<!--Template for result list hit: see results.xsl-->
	<xsl:template match="mcr:hit[contains(@id,'_person_')]">
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
						<!--img src="{$WebApplicationBaseURL}images/person2.gif"/-->
						<img src="{$WebApplicationBaseURL}images/person-f-15x15.gif"/>
					</div>
				</td>
				<td id="leaf-linkarea">
					<xsl:variable name="lastName_shorted">
						<xsl:call-template name="ShortenText">
							<xsl:with-param name="text"
								select="xalan:nodeset($cXML)/mycoreobject/metadata/def.heading/heading/lastName/text()"/>
							<xsl:with-param name="length" select="50"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="firstName_shorted">
						<xsl:call-template name="ShortenText">
							<xsl:with-param name="text"
								select="xalan:nodeset($cXML)/mycoreobject/metadata/def.heading/heading/firstName/text()"/>
							<xsl:with-param name="length" select="50"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="name">
						<xsl:value-of select="concat($lastName_shorted,', ',$firstName_shorted)"/>
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
	<xsl:template priority="1" mode="resulttitle" match="/mycoreobject[contains(@ID,'_person_')]">
		<xsl:apply-templates select="." mode="title"/>
	</xsl:template>
	<!--Template for title in metadata view: see mycoreobject.xsl-->
	<xsl:template priority="1" mode="title" match="/mycoreobject[contains(@ID,'_person_')]">
		<xsl:choose>
			<xsl:when test="./metadata/def.heading/heading">
				<xsl:apply-templates select="./metadata/def.heading/heading"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@ID"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--Template for metadata view: see mycoreobject.xsl-->
	<xsl:template priority="1" mode="present" match="/mycoreobject[contains(@ID,'_person_')]">
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
						<img src="{$WebApplicationBaseURL}images/person-f-25x25.gif"/>
					</td>
					<td id="detailed-mainheadline">
						<xsl:variable name="lastName_shorted">
							<xsl:value-of select="./metadata/def.heading/heading/lastName/text()"/>
						</xsl:variable>
						<xsl:variable name="firstName_shorted">
							<xsl:value-of select="./metadata/def.heading/heading/firstName/text()"/>
						</xsl:variable>
						<xsl:value-of select="concat($lastName_shorted,', ',$firstName_shorted)"/>
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
							<!--1***heading*************************************-->
							<!--<xsl:call-template name="printPersonName">
							<xsl:with-param select="./metadata/def.heading/heading" name="nodes"/>
							<xsl:with-param select="i18n:translate('metaData.person.heading')" name="label"/>
							</xsl:call-template>-->
							<!--2***alternative*************************************-->
							<xsl:call-template name="printPersonName">
								<xsl:with-param select="./metadata/def.alternative/alternative" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.person.Lalternative')" name="label"/>
							</xsl:call-template>
							<!--3***peerage*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/def.peerage/peerage" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.person.Lpeerage')" name="label"/>
							</xsl:call-template>
							<!--3a*** gender  *************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/def.gender/gender" name="nodes"/>
								<xsl:with-param select="i18n:translate('metaData.author.gender')" name="label"/>
							</xsl:call-template>
							<!--3b*** contact *************************************-->
							<xsl:call-template name="printMetaDate_typeSensitive">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/def.contact/contact" name="nodes"/>
								<xsl:with-param select="i18n:translate('contactData.address')" name="label"/>
								<xsl:with-param name="typeClassi" select="'urmel_class_002'"/>
								<xsl:with-param name="mode" select="'text'"/>
							</xsl:call-template>
							<!--4***role*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/def.role/role" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.person.Lrole')" name="label"/>
							</xsl:call-template>
							<!--5***placeOfActivity*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/def.placeOfActivity/placeOfActivity" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.person.LplaceOfActivity')"
									name="label"/>
							</xsl:call-template>
							<!--6***dateOfBirth*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/def.dateOfBirth/dateOfBirth" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.person.LdateOfBirth')" name="label"/>
							</xsl:call-template>
							<!--7***placeOfBirth*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/def.placeOfBirth/placeOfBirth" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.person.LplaceOfBirth')" name="label"/>
							</xsl:call-template>
							<!--8***dateOfDeath*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/def.dateOfDeath/dateOfDeath" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.person.LdateOfDeath')" name="label"/>
							</xsl:call-template>
							<!--9***placeOfDeath*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/def.placeOfDeath/placeOfDeath" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.person.LplaceOfDeath')" name="label"/>
							</xsl:call-template>
							<!--10***note*************************************-->
							<xsl:if test="$CurrentUser!='gast'">
								<xsl:call-template name="printMetaDates">
									<xsl:with-param select="./metadata/def.note/note" name="nodes"/>
									<xsl:with-param select="i18n:translate('editormask.labels.note')" name="label"/>
								</xsl:call-template>
							</xsl:if>
							<!-- linked articles-->
							<xsl:call-template name="listLinkedArts"/>
							
							<tr id="detailed-dividingline">
								<td colspan="2">
									<hr noshade="noshade" width="460"/>
								</td>
							</tr>
							<tr>
								<td id="detailed-headlines">
									<xsl:value-of select="i18n:translate('metaData.headlines.systemdata')"/>
								</td>
							</tr>
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
							<!--*** Created ************************************* -->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./service/servdates/servdate[@type='createdate']" name="nodes"/>
								<xsl:with-param select="i18n:translate('metaData.createdAt')" name="label"/>
							</xsl:call-template>
							<!--*** Last Modified ************************************* -->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./service/servdates/servdate[@type='modifydate']" name="nodes"/>
								<xsl:with-param select="i18n:translate('metaData.lastChanged')" name="label"/>
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
							<!--*** Editor Buttons ************************************* -->
							<xsl:call-template name="editobject">
								<xsl:with-param select="./@ID" name="id"/>
							</xsl:call-template>
						</table>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>
	
	<xsl:template name="printPersonName">
		<xsl:param name="nodes"/>
		<xsl:param name="label" select="local-name($nodes[1])"/>
		<xsl:if test="$nodes">
			<tr>
				<td id="detailed-labels">
					<xsl:value-of select="$label"/>
				</td>
				<td class="metavalue">
					<xsl:choose>
						<xsl:when test="count($nodes)>1">
							<ul>
								<xsl:for-each select="$nodes">
									<li>
										<xsl:apply-templates select="."/>
									</li>
								</xsl:for-each>
							</ul>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="$nodes"/>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<xsl:template match="*[lastName | name]">
		<xsl:choose>
			<xsl:when test="lastName and firstName">
				<xsl:value-of select="concat(lastName,', ',firstName)"/>
			</xsl:when>
			<xsl:when test="lastName">
				<xsl:value-of select="lastName"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="name"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="*[../@class='MCRMetaISO8601Date']">
		<xsl:variable name="format">
			<xsl:choose>
				<xsl:when test="string-length(normalize-space(.))=4">
					<xsl:value-of select="i18n:translate('metaData.dateYear')"/>
				</xsl:when>
				<xsl:when test="string-length(normalize-space(.))=7">
					<xsl:value-of select="i18n:translate('metaData.dateYearMonth')"/>
				</xsl:when>
				<xsl:when test="string-length(normalize-space(.))=10">
					<xsl:value-of select="i18n:translate('metaData.dateYearMonthDay')"/>
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
</xsl:stylesheet>
