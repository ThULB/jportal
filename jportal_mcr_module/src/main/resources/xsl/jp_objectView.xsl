<?xml version="1.0" encoding="ISO-8859-1"?>
	<!--
		=====================================================================================================
	-->
	<!--
		This stylesheet contains all templates to show an mycore object in the
		detail and child view
	-->
	<!--
		=====================================================================================================
	-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" 
    xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities"
    xmlns:urn="xalan://org.fsu.jp.urn.URNXalanExtensions"
    exclude-result-prefixes="xlink mcr i18n acl xalan layoutUtils mcrxsl">

	<xsl:param name="view.objectmetadata" select="'false'" />

	<xsl:param name="toc.pageSize" select="25" />
	<xsl:param name="toc.pos" />

	<xsl:param name="toc.sortBy.jpvolume" select="'position'" />
	<xsl:param name="toc.sortBy.jparticle" select="'size'" />
	<xsl:param select="5" name="maxLinkedArts" />
	<xsl:param select="5" name="maxLinkedCals" />

	<xsl:param name="resultListEditorID" />
	<xsl:param name="numPerPage" />
	<xsl:param name="page" />
	<xsl:param name="previousObject" />
	<xsl:param name="previousObjectHost" />
	<xsl:param name="nextObject" />
	<xsl:param name="nextObjectHost" />

	<xsl:variable name="allowHTMLInArticles">
		<xsl:call-template name="get.allowHTMLInArticles" />
	</xsl:variable>

	<!--
		=====================================================================================================
	-->
	<!-- shows the journal, volume and article view -->
	<!--
		=====================================================================================================
	-->
	<xsl:template
		match="/mycoreobject[contains(@ID,'_jpjournal_')] 
        | /mycoreobject[contains(@ID,'_jpvolume_')] 
        | /mycoreobject[contains(@ID,'_jparticle_')]"
		priority="2">

		<xsl:call-template name="jp_objectView_initJS" />
		<xsl:call-template name="printSwitchViewBar" />

		<xsl:choose>
			<!-- metadaten -->
			<xsl:when test="$view.objectmetadata = 'false'">
				<xsl:choose>
					<xsl:when
						test="($objectHost != 'local') or acl:checkPermission(/mycoreobject/@ID,'read')">
						<xsl:apply-templates select="." mode="present">
							<xsl:with-param name="obj_host" select="$objectHost" />
						</xsl:apply-templates>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="i18n:translate('metaData.accessDenied')" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- inhaltsverzeichnis -->
			<xsl:otherwise>
				<xsl:call-template name="printChildren" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!-- shows the person and institution view -->
	<!--
		=====================================================================================================
	-->
	<xsl:template
		match="/mycoreobject[contains(@ID,'_person_')] 
        | /mycoreobject[contains(@ID,'_jpinst_')]"
		priority="2">

		<xsl:call-template name="jp_objectView_initJS" />

		<xsl:choose>
			<xsl:when
				test="($objectHost != 'local') or acl:checkPermission(/mycoreobject/@ID,'read')">

				<table>
					<tr>
						<xsl:call-template name="browseCtrlJP" />
						<td>&#160;&#160;&#160;&#160;&#160;&#160;</td>
						<xsl:call-template name="switchToXMLview" />
					</tr>
				</table>

				<xsl:apply-templates select="." mode="present">
					<xsl:with-param name="obj_host" select="$objectHost" />
				</xsl:apply-templates>
				<hr />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="i18n:translate('metaData.accessDenied')" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
		=================================================================================================================
	-->
	<!-- inits all java scripts functions -->
	<!--
		=================================================================================================================
	-->
	<xsl:template name="jp_objectView_initJS">
		<script type="text/javascript">
			function confirmDelete(delUrl) {
			if
			(confirm("Objekt wirklich löschen?")) {
			document.location = delUrl;
			}
			}
      </script>
		<script type="text/javascript">
			function confirmFormDelete() {
			if (confirm("Objekt
			wirklich löschen?")) {
			return true;
			}
			return false;
			}
      </script>
	</xsl:template>

	<!--
		=================================================================================================================
	-->
	<!--
		shows the xml code of the current mcr object (prints the xml-button)
	-->
	<!--
		=================================================================================================================
	-->
	<xsl:template name="switchToXMLview">
		<xsl:if test="$view.objectmetadata='false'">
			<!--
				sets the XSL.Style parameter of the browser address to xmlexport
			-->
			<xsl:variable name="newurl">
				<xsl:call-template name="UrlSetParam">
					<xsl:with-param name="url" select="$RequestURL" />
					<xsl:with-param name="par" select="'XSL.Style'" />
					<xsl:with-param name="value" select="'xmlexport'" />
				</xsl:call-template>
			</xsl:variable>
			<td id="detailed-xmlbutton">
				<a href="{$newurl}" alt="{i18n:translate('metaData.xmlView')}"
					title="{i18n:translate('metaData.xmlView')}">
					<img src="{$WebApplicationBaseURL}images/xml.png" />
				</a>
			</td>
		</xsl:if>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!--
		Prints a switch bar which contains the children and the detailed view.
		Called by article, volume and journal.
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="printSwitchViewBar">
		<xsl:variable name="children">
			<xsl:choose>
				<xsl:when test="/mycoreobject/structure/children">
					<xsl:value-of select="'true'" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'false'" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<table id="switch" cellspacing="0" cellpadding="0" border="0">
			<tr>
				<!-- oncle buttons :-) -->
				<xsl:if test="/mycoreobject/structure/parents/parent/@xlink:href">
					<xsl:variable name="currentID">
						<xsl:value-of select="/mycoreobject/@ID" />
					</xsl:variable>
					<xsl:variable name="OID">
						<xsl:call-template name="typeOfObjectID">
							<xsl:with-param name="id" select="/mycoreobject/@ID" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="mcrSql" xmlns:encoder="xalan://java.net.URLEncoder">
						<xsl:value-of
							select="encoder:encode(concat('parent = ',/mycoreobject/structure/parents/parent/@xlink:href))" />
					</xsl:variable>
					<xsl:variable name="sort">
						<xsl:call-template name="get.sortKey">
							<xsl:with-param name="kindOfChildren" select="$OID" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="sortOrder">
						<xsl:call-template name="get.sortOrder">
							<xsl:with-param name="kindOfChildren" select="$OID" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="siblings"
						select="document(concat('query:term=',$mcrSql,$sort,'&amp;order=',$sortOrder))" />
					<xsl:variable name="currentNode">
						<xsl:copy-of select="$siblings/mcr:results/mcr:hit[@id=$currentID]" />
					</xsl:variable>

					<xsl:for-each select="$siblings/mcr:results/mcr:hit">
						<xsl:variable name="pos">
							<xsl:value-of select="position()" />
						</xsl:variable>
						<xsl:if test="@id=$currentID">
							<xsl:variable name="pred">
								<xsl:value-of
									select="$siblings/mcr:results/mcr:hit[position()=number($pos)-1]/@id" />
							</xsl:variable>
							<xsl:variable name="suc">
								<xsl:value-of
									select="$siblings/mcr:results/mcr:hit[position()=number($pos)+1]/@id" />
							</xsl:variable>

							<xsl:choose>
								<xsl:when test="$OID='jparticle'">
									<xsl:if test="$pred!=''">
										<td id="detailed-browse">
											<a
												href="{concat($WebApplicationBaseURL,'receive/',$pred,$HttpSession)}?XSL.toc.pos.SESSION=1"
												alt="{i18n:translate('metaData.jparticle.switchleft')}"
												title="{i18n:translate('metaData.jparticle.switchleft')}">
												<img src="{$WebApplicationBaseURL}left.gif" />
											</a>
										</td>
									</xsl:if>
									<xsl:if test="$suc!=''">
										<td id="detailed-browse">
											<a
												href="{concat($WebApplicationBaseURL,'receive/',$suc,$HttpSession)}?XSL.toc.pos.SESSION=1"
												alt="{i18n:translate('metaData.jparticle.switchright')}"
												title="{i18n:translate('metaData.jparticle.switchright')}">
												<img src="{$WebApplicationBaseURL}right.gif" />
											</a>
										</td>
									</xsl:if>
									<td width="20"></td>
								</xsl:when>
								<xsl:otherwise>
									<xsl:if test="$pred!=''">
										<td id="detailed-browse">
											<a
												href="{concat($WebApplicationBaseURL,'receive/',$pred,$HttpSession)}?XSL.toc.pos.SESSION=1"
												alt="{i18n:translate('metaData.jpvolume.switchleft')}"
												title="{i18n:translate('metaData.jpvolume.switchleft')}">
												<img src="{$WebApplicationBaseURL}left.gif" />
											</a>
										</td>
									</xsl:if>
									<xsl:if test="$suc!=''">
										<td id="detailed-browse">
											<a
												href="{concat($WebApplicationBaseURL,'receive/',$suc,$HttpSession)}?XSL.toc.pos.SESSION=1"
												alt="{i18n:translate('metaData.jpvolume.switchright')}"
												title="{i18n:translate('metaData.jpvolume.switchright')}">
												<img src="{$WebApplicationBaseURL}right.gif" />
											</a>
										</td>
									</xsl:if>
									<td width="20"></td>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
					</xsl:for-each>
				</xsl:if>
				<xsl:if
					test="$CurrentUser!='gast' and contains(/mycoreobject/@ID,'jpvolume') or contains(/mycoreobject/@ID,'jpjournal')">
					<td>
						<xsl:choose>
							<xsl:when test="$view.objectmetadata='false'">
								<div id="switch-current">
									<xsl:value-of select="i18n:translate('metadata.navi.detailact')" />
								</div>
							</xsl:when>
							<xsl:otherwise>
								<div id="switch-notcurrent">
									<xsl:variable name="URLDetails">
										<xsl:call-template name="UrlSetParam">
											<xsl:with-param name="url"
												select="concat($WebApplicationBaseURL,'receive/',/mycoreobject/@ID,$HttpSession)" />
											<xsl:with-param name="par"
												select="'XSL.view.objectmetadata.SESSION'" />
											<xsl:with-param name="value" select="'false'" />
										</xsl:call-template>
									</xsl:variable>
									<a href="{$URLDetails}">
										<xsl:value-of select="i18n:translate('metadata.navi.showdetail')" />
									</a>
								</div>
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td width="20"></td>
					<td>
						<xsl:choose>
							<xsl:when test="$view.objectmetadata='true'">
								<div id="switch-current">
									<xsl:value-of select="i18n:translate('metadata.navi.contentact')" />
								</div>
							</xsl:when>
							<xsl:otherwise>
								<div id="switch-notcurrent">
									<xsl:variable name="URLDetails">
										<xsl:call-template name="UrlSetParam">
											<xsl:with-param name="url"
												select="concat($WebApplicationBaseURL,'receive/',/mycoreobject/@ID,$HttpSession)" />
											<xsl:with-param name="par"
												select="'XSL.view.objectmetadata.SESSION'" />
											<xsl:with-param name="value" select="'true'" />
										</xsl:call-template>
									</xsl:variable>
									<a href="{$URLDetails}">
										<xsl:value-of select="i18n:translate('metadata.navi.showcontent')" />
									</a>
								</div>
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</xsl:if>
				<td>&#160;&#160;&#160;&#160;&#160;&#160;</td>
				<xsl:call-template name="browseCtrlJP" />
				<td>&#160;&#160;&#160;&#160;&#160;&#160;</td>
				<xsl:call-template name="switchToXMLview" />
				<xsl:if
					test="/mycoreobject[contains(@ID,'_jpvolume_')] | /mycoreobject[contains(@ID,'_jparticle_')]">
					<xsl:call-template name="linkFile" />
				</xsl:if>
			</tr>
		</table>
		<br />
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!-- prints the children of an mcr object -->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="printChildren">
		<!-- lucene implementation -->
		<xsl:call-template name="printChildren_lucene" />
		<!-- children implementation -->
		<!--    	<xsl:call-template name="printChildren_nolucene"/>-->

	</xsl:template>

	<xsl:template name="printChildren_nolucene">
		<xsl:variable name="numChildren" select="count(./structure/children/child)" />

		<xsl:variable name="toc.pos.verif">
			<xsl:choose>
				<xsl:when test="$toc.pageSize>$numChildren">
					<xsl:value-of select="1" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$toc.pos" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<table>
			<tr>
				<td>
					<xsl:value-of
						select="concat(i18n:translate('metaData.sortbuttons.numberofres'),': ')" />
					<b>
						<xsl:value-of select="$numChildren" />
					</b>
					<xsl:call-template name="printTOCNavi.chooseHitPage">
						<xsl:with-param name="numberOfChildren" select="$numChildren" />
					</xsl:call-template>
				</td>
			</tr>
		</table>

		<table id="resultList" cellpadding="0" cellspacing="0">
			<xsl:for-each
				select="./structure/children/child[(position()>=$toc.pos.verif) and ($toc.pos.verif+$toc.pageSize>position())]">
				<xsl:variable name="mcrobj"
					select="document(concat('mcrobject:',@xlink:href))" />
				<xsl:apply-templates select="$mcrobj/mycoreobject"
					mode="toc" />
			</xsl:for-each>
		</table>
	</xsl:template>

	<xsl:template name="printChildren_lucene">
		<xsl:variable name="kindOfChildren2">
			<xsl:choose>
				<xsl:when test="./structure/children/child[position()=1]/@xlink:href">
					<xsl:call-template name="typeOfObjectID">
						<xsl:with-param name="id"
							select="./structure/children/child[position()=1]/@xlink:href" />
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="''" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="mcrSql" xmlns:encoder="xalan://java.net.URLEncoder">
			<xsl:value-of select="encoder:encode(concat('parent = ',./@ID))" />
		</xsl:variable>
		<xsl:variable name="sort">
			<xsl:call-template name="get.sortKey">
				<xsl:with-param name="kindOfChildren" select="$kindOfChildren2" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="sortOrder">
			<xsl:call-template name="get.sortOrder">
				<xsl:with-param name="kindOfChildren" select="$kindOfChildren2" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="children"
			select="document(concat('query:term=',$mcrSql,$sort,'&amp;order=',$sortOrder))" />
		<xsl:call-template name="printTOCNavi">
			<xsl:with-param name="location" select="'navi'" />
			<xsl:with-param name="childrenKinds" select="$kindOfChildren2" />
			<xsl:with-param name="childrenXML" select="$children" />
			<xsl:with-param name="numChildren"
				select="count($children/mcr:results/mcr:hit)" />
		</xsl:call-template>
		<xsl:variable name="toc.pos.verif">
			<xsl:choose>
				<xsl:when test="$toc.pageSize>count($children/mcr:results/mcr:hit)">
					<xsl:value-of select="1" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$toc.pos" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<table id="resultList" cellpadding="0" cellspacing="0">
			<xsl:for-each
				select="$children/mcr:results/mcr:hit[(position()>=$toc.pos.verif) and ($toc.pos.verif+$toc.pageSize>position())]">
				<xsl:variable name="mcrobj"
					select="document(concat('mcrobject:',@id))/mycoreobject" />
				<xsl:apply-templates select="." mode="toc">
					<xsl:with-param name="mcrobj" select="$mcrobj" />
					<xsl:with-param name="mcrobjlink">
						<xsl:call-template name="objectLink">
							<xsl:with-param name="obj_id" select="@id" />
						</xsl:call-template>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:for-each>
		</table>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!--
		returns the sortkey of a given children (used by printSwitchViewBar
		and printChildren)
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="get.sortKey">
		<xsl:param name="kindOfChildren" />
		<xsl:choose>
			<xsl:when
				test="($kindOfChildren='jpvolume') and ($toc.sortBy.jpvolume='title')">
				<xsl:value-of select="'&amp;sortby=maintitles_vol'" />
			</xsl:when>
			<xsl:when
				test="($kindOfChildren='jpvolume') and ($toc.sortBy.jpvolume='position')">
				<xsl:value-of select="'&amp;sortby=position_vol'" />
			</xsl:when>
			<xsl:when
				test="($kindOfChildren='jpvolume') and ($toc.sortBy.jpvolume='date')">
				<xsl:value-of select="'&amp;sortby=dates_vol'" />
			</xsl:when>
			<xsl:when
				test="($kindOfChildren='jparticle') and ($toc.sortBy.jparticle='title')">
				<xsl:value-of select="'&amp;sortby=maintitles_art'" />
			</xsl:when>
			<xsl:when
				test="($kindOfChildren='jparticle') and ($toc.sortBy.jparticle='size')">
				<xsl:value-of select="'&amp;sortby=sizes_art'" />
			</xsl:when>
			<xsl:when
				test="($kindOfChildren='jparticle') and ($toc.sortBy.jparticle='position')">
				<xsl:value-of select="'&amp;sortby=dates_art'" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="''" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!--
		returns the sort order of a given children (used by printSwitchViewBar
		and printChildren)
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="get.sortOrder">
		<xsl:param name="kindOfChildren" />
		<xsl:choose>
			<xsl:when
				test="($kindOfChildren='jparticle') and ($toc.sortBy.jparticle='position')">
				<xsl:value-of select="'descending'" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'ascending'" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!--
		prints the number of children, a result pages navigation and a form to
		sort them
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="printTOCNavi">
		<xsl:param name="location" />
		<xsl:param name="childrenKinds" />
		<xsl:param name="childrenXML" />
		<xsl:param name="numChildren"
			select="count(/mycoreobject/structure/children//child)" />

		<xsl:variable name="pred">
			<xsl:value-of select="number($toc.pos)-(number($toc.pageSize)+1)" />
		</xsl:variable>
		<xsl:variable name="succ">
			<xsl:value-of select="number($toc.pos)+number($toc.pageSize)+1" />
		</xsl:variable>

		<xsl:choose>
			<xsl:when test="$location='navi'">
				<table>
					<tr>
						<td colspan="2">
							<xsl:value-of
								select="concat(i18n:translate('metaData.sortbuttons.numberofres'),': ')" />
							<b>
								<xsl:value-of select="$numChildren" />
							</b>
							<xsl:call-template name="printTOCNavi.chooseHitPage">
								<xsl:with-param name="numberOfChildren"
									select="count(xalan:nodeset($childrenXML)/mcr:results/mcr:hit)" />
							</xsl:call-template>
						</td>
					</tr>
					<tr>
						<td align="left" colspan="2">
							<form id="sort" target="_self"
								action="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}{$HttpSession}"
								method="post">
								<p>
									<xsl:choose>
										<xsl:when test="$childrenKinds='jparticle'">
											<select onChange="document.getElementById('sort').submit()"
												name="XSL.toc.sortBy.jparticle.SESSION" size="1">
												<option value="title">
													<xsl:call-template name="checkSelection">
														<xsl:with-param name="compVal1"
															select="$toc.sortBy.jparticle" />
														<xsl:with-param name="compVal2" select="'title'" />
													</xsl:call-template>
													<xsl:value-of
														select="i18n:translate('metaData.sortbuttons.aftertitles')" />
												</option>
												<option value="size">
													<xsl:call-template name="checkSelection">
														<xsl:with-param name="compVal1"
															select="$toc.sortBy.jparticle" />
														<xsl:with-param name="compVal2" select="'size'" />
													</xsl:call-template>
													<xsl:value-of
														select="i18n:translate('metaData.sortbuttons.afterpages')" />
												</option>
												<option value="position">
													<xsl:call-template name="checkSelection">
														<xsl:with-param name="compVal1"
															select="$toc.sortBy.jparticle" />
														<xsl:with-param name="compVal2" select="'position'" />
													</xsl:call-template>
													<xsl:value-of
														select="i18n:translate('metaData.sortbuttons.position')" />
												</option>
											</select>
										</xsl:when>
										<xsl:otherwise>
											<select onChange="document.getElementById('sort').submit()"
												name="XSL.toc.sortBy.jpvolume.SESSION" size="1">
												<option value="title">
													<xsl:call-template name="checkSelection">
														<xsl:with-param name="compVal1" select="$toc.sortBy.jpvolume" />
														<xsl:with-param name="compVal2" select="'title'" />
													</xsl:call-template>
													<xsl:value-of
														select="i18n:translate('metaData.sortbuttons.aftertitles')" />
												</option>
												<option value="position">
													<xsl:call-template name="checkSelection">
														<xsl:with-param name="compVal1" select="$toc.sortBy.jpvolume" />
														<xsl:with-param name="compVal2" select="'position'" />
													</xsl:call-template>
													<xsl:value-of
														select="i18n:translate('metaData.sortbuttons.position')" />
												</option>
												<option value="date">
													<xsl:call-template name="checkSelection">
														<xsl:with-param name="compVal1" select="$toc.sortBy.jpvolume" />
														<xsl:with-param name="compVal2" select="'date'" />
													</xsl:call-template>
													<xsl:value-of
														select="i18n:translate('metaData.sortbuttons.chronological')" />
												</option>
											</select>
										</xsl:otherwise>
									</xsl:choose>
								</p>
							</form>
						</td>
					</tr>
				</table>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!-- prints the result list navigation if results > $toc.pageSize -->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="printTOCNavi.chooseHitPage">
		<xsl:param name="numberOfChildren" />
		<xsl:variable name="numberOfHitPages">
			<xsl:value-of
				select="ceiling(number($numberOfChildren) div number($toc.pageSize))" />
		</xsl:variable>
		<xsl:if test="number($numberOfChildren)>number($toc.pageSize)">
			<xsl:value-of select="concat(', ',i18n:translate('metaData.resultpage'))" />
			<xsl:for-each
				select="./structure/children/child[number($numberOfHitPages)>=position()]">
				<xsl:variable name="jumpToPos">
					<xsl:value-of
						select="(position()*number($toc.pageSize))-number($toc.pageSize)" />
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="number($jumpToPos)+1=number($toc.pos)">
						<xsl:value-of select="concat(' [',position(),'] ')" />
					</xsl:when>
					<xsl:otherwise>
						<a
							href="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}{$HttpSession}?XSL.toc.pos.SESSION={$jumpToPos+1}">
							<xsl:value-of select="concat(' ',position(),' ')" />
						</a>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!--
		checks if the two values are equals, if true 'selected' will be
		returned
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="checkSelection">
		<xsl:param name="compVal1" />
		<xsl:param name="compVal2" />
		<xsl:if test="$compVal1=$compVal2">
			<xsl:attribute name="selected">
                <xsl:value-of select="selected" />
            </xsl:attribute>
		</xsl:if>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!--
		shows a navigation control to browse through the result list of a
		search
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="browseCtrlJP">
		<xsl:variable name="hasPrev" select="string-length($previousObject)>0" />
		<xsl:variable name="hasNext" select="string-length($nextObject)>0" />

		<xsl:if test="$hasPrev">
			<xsl:variable name="hostParam">
				<xsl:if test="$previousObjectHost != 'local'">
					<xsl:value-of select="concat('?host=',$previousObjectHost)" />
				</xsl:if>
			</xsl:variable>
			<td id="detailed-browse">
				<a
					href="{$WebApplicationBaseURL}receive/{$previousObject}{$HttpSession}{$hostParam}"
					alt="{i18n:translate('metaData.resultlist.prev')}" title="{i18n:translate('metaData.resultlist.prev')}">
					<img src="{$WebApplicationBaseURL}left.gif" />
				</a>
			</td>
		</xsl:if>
		<xsl:if test="$hasPrev or $hasNext">
			<td>
				<div id="switch-notcurrent">
					<a
						href="{$ServletsBaseURL}MCRSearchServlet{$HttpSession}?mode=results&amp;id={$resultListEditorID}&amp;page={$page}&amp;numPerPage={$numPerPage}">
						<xsl:value-of select="i18n:translate('metaData.resultlist')" />
					</a>
				</div>
			</td>
		</xsl:if>
		<xsl:if test="$hasNext">
			<xsl:variable name="hostParam">
				<xsl:if test="$nextObjectHost != 'local'">
					<xsl:value-of select="concat('?host=',$nextObjectHost)" />
				</xsl:if>
			</xsl:variable>
			<td id="detailed-browse">
				<a
					href="{$WebApplicationBaseURL}receive/{$nextObject}{$HttpSession}{$hostParam}"
					alt="{i18n:translate('metaData.resultlist.next')}" title="{i18n:translate('metaData.resultlist.next')}">
					<img src="{$WebApplicationBaseURL}right.gif" />
				</a>
			</td>
		</xsl:if>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!-- prints the icon line to edit an object -->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="jp_editobject">

		<xsl:param name="accessedit" />
		<xsl:param name="accessdelete" />
		<xsl:param name="id" />

		<xsl:param name="layout" select="'$'" />
		<xsl:variable name="layoutparam">
			<xsl:if test="$layout != '$'">
				<xsl:value-of select="concat('&amp;layout=',$layout)" />
			</xsl:if>
		</xsl:variable>

		<!-- deleted -->
		<xsl:variable name="deleted">
			<xsl:call-template name="isFlagSet">
				<xsl:with-param name="flagName" select="'deleted'" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:if test="$objectHost = 'local'">
			<xsl:choose>
				<xsl:when
					test="acl:checkPermission($id,'writedb') or acl:checkPermission($id,'deletedb')">
					<xsl:variable name="type"
						select="substring-before(substring-after($id,'_'),'_')" />
					<tr>
						<td class="metaname">
							<xsl:value-of select="concat(i18n:translate('metaData.edit'),' :')" />
						</td>
						<td class="metavalue">
							<xsl:choose>
								<!-- deleted -->
								<xsl:when test="$deleted = 'true'">
									<a
										href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=srestoreobj">
										<img src="{$WebApplicationBaseURL}images/workflow_restore.gif"
											title="{i18n:translate('swf.object.restoreObject')}" />
									</a>
									<a id="obj_del"
										href="javascript:confirmDelete('{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=exportAndDelete')">
										<img src="{$WebApplicationBaseURL}images/workflow_objdelete.gif"
											title="{i18n:translate('swf.object.delObject')}" />
									</a>
								</xsl:when>
								<!-- not deleted -->
								<xsl:otherwise>
									<xsl:if test="acl:checkPermission($id,'writedb')">
										<a
											href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=seditobj">
											<img src="{$WebApplicationBaseURL}images/workflow_objedit.gif"
												title="{i18n:translate('swf.object.editObject')}" />
										</a>
									</xsl:if>
									<xsl:if test="acl:checkPermission($id,'deletedb')">
										<a id="obj_del"
											href="javascript:confirmDelete('{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=sdelobj')">
											<img src="{$WebApplicationBaseURL}images/workflow_objdelete.gif"
												title="{i18n:translate('swf.object.delObject')}" />
										</a>
									</xsl:if>
								</xsl:otherwise>
							</xsl:choose>

						</td>
					</tr>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!-- prints the icon line to edit an object with derivates -->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="jp_editobject_with_der">
		<xsl:param name="accessnbn" />
		<xsl:param name="accessedit" />
		<xsl:param name="accessdelete" />
		<xsl:param name="id" />
		<xsl:param name="layout" select="'$'" />
		<xsl:variable name="layoutparam">
			<xsl:if test="$layout != '$'">
				<xsl:value-of select="concat('&amp;layout=',$layout)" />
			</xsl:if>
		</xsl:variable>

		<!-- deleted -->
		<xsl:variable name="deleted">
			<xsl:call-template name="isFlagSet">
				<xsl:with-param name="flagName" select="'deleted'" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:if test="$objectHost = 'local'">
			<xsl:choose>
				<xsl:when
					test="acl:checkPermission($id,'writedb') or acl:checkPermission($id,'deletedb')">
                <xsl:variable name="type">
                <xsl:choose>
                  <xsl:when test="acl:checkPermission('modify-group')">
                    <xsl:value-of select="concat(substring-before(substring-after($id,'_'),'_'),'&amp;layout=admin&amp;')" />
                   </xsl:when>
                   <xsl:otherwise>
                      <xsl:value-of select="substring-before(substring-after($id,'_'),'_')" />
                   </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                                      
					<tr>
						<td class="metaname">
							<xsl:value-of select="concat(i18n:translate('metaData.edit'),' :')" />
						</td>
						<td class="metavalue">
							<xsl:choose>
								<!-- deleted -->
								<xsl:when test="$deleted = 'true'">
									<a
										href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=srestoreobj">
										<img src="{$WebApplicationBaseURL}images/workflow_restore.gif"
											title="{i18n:translate('jportal.object.restoreObject')}" />
									</a>
									<a id="obj_del"
										href="javascript:confirmDelete('{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=exportAndDelete')">
										<img src="{$WebApplicationBaseURL}images/workflow_objdelete.gif"
											title="{i18n:translate('jportal.object.delObject')}" />
									</a>
								</xsl:when>
								<!-- not deleted -->
								<xsl:otherwise>
									<xsl:if test="acl:checkPermission($id,'writedb')">
                                    
                                      <a
                                          href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=seditobj">
                                          <img src="{$WebApplicationBaseURL}images/workflow_objedit.gif"
                                          title="{i18n:translate('swf.object.editObject')}" /> </a>
                                     
										<a
											href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type=acl&amp;step=commit&amp;todo=seditacl">
											<img src="{$WebApplicationBaseURL}images/workflow_acledit.gif"
												title="{i18n:translate('swf.object.editACL')}" />
										</a>
										<xsl:if test="$accessnbn = 'true'">
											<a
												href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=saddnbn">
												<img src="{$WebApplicationBaseURL}images/workflow_addnbn.gif"
													title="{i18n:translate('swf.object.addNBN')}" />
											</a>
										</xsl:if>
										<a
											href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=snewder">
											<img src="{$WebApplicationBaseURL}images/workflow_deradd.gif"
												title="{i18n:translate('swf.derivate.addDerivate')}" />
										</a>
									</xsl:if>
									<xsl:if test="acl:checkPermission($id,'deletedb')">
										<a
											href="javascript:confirmDelete('{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=sdelobj')">
											<img src="{$WebApplicationBaseURL}images/workflow_objdelete.gif"
												title="{i18n:translate('swf.object.delObject')}" />
										</a>
									</xsl:if>
								</xsl:otherwise>
							</xsl:choose>
						</td>
					</tr>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

	<!--
		============================================================================================================================
	-->
	<!-- prints a static url to the selected mcr object -->
	<!--
		============================================================================================================================
	-->
	<xsl:template name="get.staticURL">
		<xsl:param name="stURL" />
		<tr>
			<td id="detailed-staticurl1">
				<xsl:value-of select="i18n:translate('metaData.staticURL')" />
			</td>
			<td colspan="2" id="detailed-staticurl2">
				<a>
					<xsl:attribute name="href">
                        <xsl:copy-of select="$stURL" />
                    </xsl:attribute>
					<xsl:copy-of select="$stURL" />
				</a>
			</td>
		</tr>
	</xsl:template>

	<!--
		============================================================================================================================
	-->
	<!--
		prints a short list of linked articles (used by persons and
		institutions)
	-->
	<!--
		============================================================================================================================
	-->
	<xsl:template name="listLinkedArts">
		<xsl:variable name="mcrSql" xmlns:encoder="xalan://java.net.URLEncoder">
			<xsl:value-of
				select="encoder:encode(concat('(objectType = jparticle) and (link = ', /mycoreobject/@ID, ')'))" />
		</xsl:variable>
		<xsl:variable name="linkedArt"
			select="document(concat('query:term=',$mcrSql))" />
		<xsl:variable name="OID">
			<xsl:call-template name="typeOfObjectID">
				<xsl:with-param name="id" select="/mycoreobject/@ID" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:if test="$linkedArt/mcr:results/mcr:hit">
			<table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
				<tr>
					<td valign="top" id="detailed-labels">
						<br></br>
						<xsl:choose>
							<xsl:when test="$OID='person'">
								<xsl:value-of select="i18n:translate('metaData.person.linked.article')" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="i18n:translate('metaData.jpinst.linked')" />
							</xsl:otherwise>
						</xsl:choose>

					</td>
					<td>
						<ul>
							<xsl:for-each
								select="$linkedArt/mcr:results/mcr:hit[number($maxLinkedArts)>position()-1]">
								<xsl:if test="contains(@id, 'jparticle')">
									<xsl:variable name="art"
										select="document(concat('mcrobject:',@id))" />
									<li>
										<xsl:call-template name="printHistoryRow">
											<xsl:with-param name="sortOrder" select="'ascending'" />
											<xsl:with-param name="printCurrent" select="'true'" />
											<xsl:with-param name="linkCurrent" select="'true'" />
											<xsl:with-param name="layout" select="'false'" />
											<xsl:with-param name="node" select="$art" />
										</xsl:call-template>
									</li>
								</xsl:if>
								<div id="detailed-linkedart"></div>
							</xsl:for-each>
							<xsl:if test="count($linkedArt/mcr:results/mcr:hit)>$maxLinkedArts">
								<li>
									<a xmlns:encoder="xalan://java.net.URLEncoder"
										href="{$ServletsBaseURL}MCRSearchServlet{$HttpSession}?query={$mcrSql}&amp;numPerPage=10">
										<xsl:value-of
											select="concat(' ',i18n:translate('metaData.person.linked.showAll'),' (',count($linkedArt/mcr:results/mcr:hit),') &gt;&gt;')" />
									</a>
								</li>
							</xsl:if>
						</ul>
					</td>
				</tr>
			</table>
		</xsl:if>
	</xsl:template>

	<!--
		============================================================================================================================
	-->
	<!-- prints a short list of linked calenders (used by persons) -->
	<!--
		============================================================================================================================
	-->
	<xsl:template name="listLinkedCals">
		<xsl:variable name="mcrSql" xmlns:encoder="xalan://java.net.URLEncoder">
			<xsl:value-of
				select="encoder:encode(concat('(objectType = jpjournal) and (contentClassi1 = calendar) and (link = ', /mycoreobject/@ID, ')'))" />
		</xsl:variable>
		<xsl:variable name="linkedCal"
			select="document(concat('query:term=',$mcrSql))" />
		<xsl:variable name="OID">
			<xsl:call-template name="typeOfObjectID">
				<xsl:with-param name="id" select="/mycoreobject/@ID" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:if test="$linkedCal/mcr:results/mcr:hit">
			<table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
				<tr>
					<td valign="top" id="detailed-labels">
						<br></br>
						<xsl:choose>
							<xsl:when test="$OID='person'">
								<xsl:value-of
									select="i18n:translate('metaData.person.linked.calendar')" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="i18n:translate('metaData.jpinst.linked')" />
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td>
						<ul>
							<xsl:for-each
								select="$linkedCal/mcr:results/mcr:hit[number($maxLinkedCals)>position()-1]">
								<xsl:if test="contains(@id, 'jpjournal')">
									<xsl:variable name="cal"
										select="document(concat('mcrobject:',@id))" />
									<li>
										<xsl:call-template name="printHistoryRow">
											<xsl:with-param name="sortOrder" select="'ascending'" />
											<xsl:with-param name="printCurrent" select="'true'" />
											<xsl:with-param name="linkCurrent" select="'true'" />
											<xsl:with-param name="layout" select="'false'" />
											<xsl:with-param name="node" select="$cal" />
										</xsl:call-template>
									</li>
								</xsl:if>
								<div id="detailed-linkedart"></div>
							</xsl:for-each>
							<xsl:if test="count($linkedCal/mcr:results/mcr:hit)>$maxLinkedCals">
								<li>
									<a xmlns:encoder="xalan://java.net.URLEncoder"
										href="{$ServletsBaseURL}MCRSearchServlet{$HttpSession}?query={$mcrSql}&amp;numPerPage=10">
										<xsl:value-of
											select="concat(' ',i18n:translate('metaData.person.linked.showAll'),' (',count($linkedCal/mcr:results/mcr:hit),') &gt;&gt;')" />
									</a>
								</li>
							</xsl:if>
						</ul>
					</td>
				</tr>
			</table>
		</xsl:if>
	</xsl:template>

	<!--
		============================================================================================================================
	-->
	<!--
		prints a xalan nodeset as list (used by listLinkedArts and
		listLinkedCals)
	-->
	<!--
		============================================================================================================================
	-->
	<xsl:template name="printHistoryRow">
		<xsl:param name="underline" select="'false'" />
		<xsl:param name="sortOrder" select="'descending'" />
		<xsl:param name="printCurrent" select="'true'" />
		<xsl:param name="linkCurrent" select="'false'" />
		<xsl:param name="layout" select="'false'" />
		<xsl:param name="node" select="." />

		<xsl:variable name="selectPresentLang">
			<xsl:call-template name="selectPresentLang">
				<xsl:with-param name="nodes"
					select="$node/mycoreobject/metadata/maintitles/maintitle[@inherited=0]" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="objectID"
			select="xalan:nodeset($node)/mycoreobject/@ID" />
		<xsl:choose>
			<xsl:when test="$layout='true'">
				<span id="leaf-headline2">
					<xsl:if
						test="contains(/mycoreobject/@ID,'jparticle') or
                                contains(/mycoreobject/@ID,'jpvolume') or
                                contains(/mycoreobject/@ID,'jpjournal') or
                                contains($objectID,'jparticle') or
                                contains($objectID,'jpvolume') or
                                contains($objectID,'jpjournal')">
						<xsl:choose>
							<xsl:when test="$sortOrder='descending'">
								<xsl:for-each
									select="$node/mycoreobject/metadata/maintitles/maintitle[@xml:lang=$CurrentLang]">
									<xsl:sort select="@inherited" order="descending" />
									<xsl:call-template name="printHistoryRow.rows">
										<xsl:with-param name="sortOrder" select="$sortOrder" />
										<xsl:with-param name="printCurrent2" select="$printCurrent" />
										<xsl:with-param name="linkCurrent" select="$linkCurrent" />
									</xsl:call-template>
								</xsl:for-each>
							</xsl:when>
							<xsl:otherwise>
								<xsl:for-each
									select="$node/mycoreobject/metadata/maintitles/maintitle[@xml:lang=$CurrentLang]">
									<xsl:sort select="@inherited" order="ascending" />
									<xsl:call-template name="printHistoryRow.rows">
										<xsl:with-param name="sortOrder" select="$sortOrder" />
										<xsl:with-param name="printCurrent2" select="$printCurrent" />
										<xsl:with-param name="linkCurrent" select="$linkCurrent" />
									</xsl:call-template>
								</xsl:for-each>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:if>
				</span>
				<xsl:if test="$underline='true'">
					<table>
						<tr>
							<td id="leaf-headline1">_________________________________________________</td>
						</tr>
					</table>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<span id="leaf-headline2">
					<xsl:if
						test="contains(/mycoreobject/@ID,'jparticle') or
                                contains(/mycoreobject/@ID,'jpvolume') or
                                contains(/mycoreobject/@ID,'jpjournal') or
                                contains($objectID,'jpjournal') or
                                contains($objectID,'jpvolume') or
                                contains($objectID,'jparticle')">
						<xsl:choose>
							<xsl:when test="$sortOrder='descending'">
								<xsl:for-each
									select="$node/mycoreobject/metadata/maintitles/maintitle[@xml:lang=$CurrentLang or @xml:lang=$selectPresentLang]">
									<xsl:sort select="@inherited" order="descending" />
									<xsl:call-template name="printHistoryRow.rows">
										<xsl:with-param name="sortOrder" select="$sortOrder" />
										<xsl:with-param name="printCurrent2" select="$printCurrent" />
										<xsl:with-param name="linkCurrent" select="$linkCurrent" />
									</xsl:call-template>
								</xsl:for-each>
							</xsl:when>
							<xsl:otherwise>
								<xsl:for-each
									select="$node/mycoreobject/metadata/maintitles/maintitle[@xml:lang=$CurrentLang or @xml:lang=$selectPresentLang]">
									<xsl:sort select="@inherited" order="ascending" />
									<xsl:call-template name="printHistoryRow.rows">
										<xsl:with-param name="sortOrder" select="$sortOrder" />
										<xsl:with-param name="printCurrent2" select="$printCurrent" />
										<xsl:with-param name="linkCurrent" select="$linkCurrent" />
									</xsl:call-template>
								</xsl:for-each>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:if>
				</span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
		============================================================================================================================
	-->
	<!-- prints a single entry of the list (called by printHistoryRow) -->
	<!--
		============================================================================================================================
	-->
	<xsl:template name="printHistoryRow.rows">
		<xsl:param name="sortOrder" />
		<xsl:param name="printCurrent2" />
		<xsl:param name="linkCurrent" />
		<xsl:choose>
			<xsl:when test="@inherited='0' ">
				<xsl:if test="$printCurrent2='true' ">
					<span>
						<xsl:variable name="date">
							<xsl:if
								test="/mycoreobject/metadata/dates/date[@inherited='0']/text()!='' and /mycoreobject/metadata/dates/date[@inherited='0']/text()!=/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text()">
								<xsl:value-of
									select="concat(' (',/mycoreobject/metadata/dates/date[@inherited='0']/text(),')')" />
							</xsl:if>
						</xsl:variable>
						<xsl:variable name="text">
							<xsl:call-template name="ShortenText">
								<xsl:with-param name="text" select="text()" />
								<xsl:with-param name="length" select="25" />
							</xsl:call-template>
						</xsl:variable>
						<xsl:variable name="label">
							<xsl:value-of select="concat($text,$date)" />
						</xsl:variable>
						<xsl:choose>
							<xsl:when test="$linkCurrent='true'">
								<a
									href="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}?XSL.view.objectmetadata.SESSION=false"
									alt="{text()}" title="{text()}">
									<b>
										<xsl:value-of select="$label" />
									</b>
								</a>
							</xsl:when>
							<xsl:otherwise>
								<b>
									<xsl:value-of select="$label" />
								</b>
							</xsl:otherwise>
						</xsl:choose>
					</span>
				</xsl:if>
			</xsl:when>
			<xsl:when test="@inherited='1' ">
				<xsl:if test="/mycoreobject/structure/parents/parent[@xlink:href!='']">
					<xsl:variable name="date">
						<xsl:if
							test="/mycoreobject/metadata/dates/date[@inherited='1'] and /mycoreobject/metadata/dates/date[@inherited='1']!=/mycoreobject/metadata/maintitles/maintitle[@inherited='1'] and position()!=1">
							<xsl:value-of
								select="concat(' (',/mycoreobject/metadata/dates/date[@inherited='1']/text(),')')" />
						</xsl:if>
					</xsl:variable>
					<xsl:variable name="text">
						<xsl:call-template name="ShortenText">
							<xsl:with-param name="text" select="text()" />
							<xsl:with-param name="length" select="25" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="label">
						<xsl:choose>
							<xsl:when test="$sortOrder='descending'">
								<xsl:choose>
									<xsl:when test="position()!=last()">
										<xsl:value-of select="concat($text,$date, ' \ ')" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="concat($text,$date)" />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="concat(' - ',$text,$date)" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:call-template name="objectLinking">
						<xsl:with-param name="obj_id"
							select="/mycoreobject/structure/parents/parent/@xlink:href" />
						<xsl:with-param name="obj_name" select="$label" />
						<xsl:with-param name="hoverText" select="text()" />
						<xsl:with-param name="requestParam"
							select="'XSL.toc.pos.SESSION=1&amp;XSL.view.objectmetadata.SESSION=true'" />
					</xsl:call-template>
				</xsl:if>
			</xsl:when>
			<xsl:when test="@inherited!='1' and @inherited!='0'">
				<xsl:variable name="heritedLevel">
					<xsl:value-of select="@inherited" />
				</xsl:variable>
				<xsl:variable name="date">
					<xsl:if
						test="/mycoreobject/metadata/dates/date[@inherited=$heritedLevel] and position()!=1 and /mycoreobject/metadata/dates/date[@inherited=position()]!=/mycoreobject/metadata/maintitles/maintitle[@inherited=position()]">
						<xsl:value-of
							select="concat(' (',/mycoreobject/metadata/dates/date[@inherited=$heritedLevel]/text(),')')" />
					</xsl:if>
				</xsl:variable>
				<xsl:variable name="text">
					<xsl:call-template name="ShortenText">
						<xsl:with-param name="text" select="text()" />
						<xsl:with-param name="length" select="25" />
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="label">
					<xsl:choose>
						<xsl:when test="$sortOrder='descending'">
							<xsl:choose>
								<xsl:when test="position()!=last()-1">
									<xsl:value-of select="concat($text,$date, ' \ ')" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat($text,$date)" />
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="concat(' - ',$text,$date)" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:value-of select="$label" />
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!--
		============================================================================================================================
	-->
	<!-- prints a link to a given mcr object. -->
	<!--
		============================================================================================================================
	-->
	<xsl:template name="objectLinking">
		<xsl:param name="obj_id" />
		<xsl:param name="obj_name" />
		<xsl:param name="hoverText" />
		<xsl:param name="requestParam" />
		<xsl:param name="requestParam" />
		<xsl:param name="allowHTMLInResultLists" select="'false'" />

		<!--LOCAL REQUEST -->
		<xsl:if test="$objectHost = 'local'">
			<xsl:choose>
				<xsl:when test="acl:checkPermission($obj_id,'read')">
					<a
						href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}?{$requestParam}"
						alt="{$hoverText}" title="{$hoverText}">
						<xsl:choose>
							<xsl:when
								test="$allowHTMLInArticles = 'true' or $allowHTMLInResultLists = 'true'">
								<xsl:value-of disable-output-escaping="yes" select="$obj_name" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$obj_name" />
							</xsl:otherwise>
						</xsl:choose>
					</a>
				</xsl:when>
				<xsl:otherwise>
					<!--
						<xsl:variable name="mcrobj"
						select="document(concat('mcrobject:',$obj_id))/mycoreobject" />
					-->
					<!-- Build Login URL for LoginServlet -->
					<xsl:variable xmlns:encoder="xalan://java.net.URLEncoder"
						name="LoginURL"
						select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?url=', encoder:encode( string( $RequestURL ) ) )" />
					<xsl:value-of select="$obj_name" />
					&#160;
					<a href="{$LoginURL}">
						<img src="{concat($WebApplicationBaseURL,'images/paper_lock.gif')}" />
					</a>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<!--REMOTE REQUEST -->
		<xsl:if test="$objectHost != 'local'">
			<xsl:variable name="mcrobj"
				select="document(concat('mcrws:operation=MCRDoRetrieveObject&amp;host=',$objectHost,'&amp;ID=',$obj_id))/mycoreobject" />
			<a
				href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}?host={@host}&amp;{$requestParam}">
				<xsl:apply-templates select="$mcrobj" mode="resulttitle" />
			</a>
		</xsl:if>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!--
		returns 'true' if its allowed to use html tags, otherwise 'false'
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="get.allowHTMLInArticles">
		<xsl:choose>
			<xsl:when
				test="xalan:nodeset($journalXML)/mycoreobject/metadata and xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_genhiddenfields1/hidden_genhiddenfield1/text() = 'allowHTML'">
				<xsl:value-of select="'true'" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'false'" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!-- prints the derivates of an mcr object -->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="printDerivates">
		<xsl:param name="obj_id" />
		<xsl:param name="knoten" />

		<xsl:choose>
			<!-- metadata view -->
			<xsl:when test="$knoten=''">
				<xsl:if test="(./structure/derobjects)">
					<xsl:if test="$objectHost = 'local'">
						<xsl:for-each select="./structure/derobjects/derobject">
							<xsl:variable name="deriv" select="@xlink:href" />
							<xsl:variable name="deriv" select="@xlink:href" />
							<xsl:variable name="derivlink" select="concat('mcrobject:',$deriv)" />
							<xsl:variable name="derivate" select="document($derivlink)" />

                            <!-- deleted -->
							<xsl:variable name="isDeleted">
								<xsl:call-template name="isFlagSet">
									<xsl:with-param name="path" select="$derivate/mycorederivate" />
									<xsl:with-param name="flagName" select="'deleted'" />
								</xsl:call-template>
							</xsl:variable>
              
                            <!-- urn -->
                            <xsl:variable name="derivateWithURN" select="mcrxsl:hasURNDefined(@xlink:href)" />

							<xsl:if test="$isDeleted != 'true'">
								<tr>
									<td align="left" valign="top" id="detailed-links">
										<table cellpadding="0" cellspacing="0"
											id="detailed-contenttable">
											<tr>
												<td colspan="4">
													<xsl:apply-templates
														select="$derivate/mycorederivate/derivate/internals">
														<xsl:with-param name="objID" select="$obj_id" />
														<xsl:with-param name="objectXML"
															select="document(concat('mcrobject:',$obj_id))" />
													</xsl:apply-templates>
													<xsl:apply-templates
														select="$derivate/mycorederivate/derivate/externals">
														<xsl:with-param name="objID" select="$obj_id" />
													</xsl:apply-templates>
												</td>
											</tr>
											<tr height="30px">
												<xsl:if test="acl:checkPermission($obj_id,'writedb')">
                                                    <xsl:variable name="type">
                                                      <xsl:copy-of select="substring-before(substring-after($obj_id,'_'),'_')" />
                                                    </xsl:variable>
                                                    <xsl:variable name="cellWidth" select="'24'" />
													<td width="{$cellWidth}" valign="center" align="center">
														<form method="get">
															<xsl:attribute name="action">
                                                              <xsl:value-of
																select="concat($WebApplicationBaseURL,'servlets/MCRStartEditorServlet',$JSessionID)" />
                                                            </xsl:attribute>
															<input name="lang" type="hidden" value="{$CurrentLang}" />
															<input name="se_mcrid" type="hidden">
																<xsl:attribute name="value">
                                                                  <xsl:value-of
																	select="@xlink:href" />
                                                              </xsl:attribute>
															</input>
															<input name="te_mcrid" type="hidden">
																<xsl:attribute name="value">
                                                                  <xsl:value-of
																	select="@xlink:href" />
                                                              </xsl:attribute>
															</input>
															<input name="re_mcrid" type="hidden">
																<xsl:attribute name="value">
                                                                  <xsl:value-of
																	select="$obj_id" />
                                                              </xsl:attribute>
															</input>
															<input name="type" type="hidden" value="{$type}" />
															<input name="todo" type="hidden" value="saddfile" />
															<input type="image"
																src="{$WebApplicationBaseURL}images/workflow_deradd.gif"
																title="{i18n:translate('swf.derivate.addFile')}" />
														</form>
													</td>
													<td width="{$cellWidth}" valign="center" align="center">
														<form method="get">
															<xsl:attribute name="action">
                                                              <xsl:value-of
																select="concat($WebApplicationBaseURL,'servlets/MCRStartEditorServlet',$JSessionID)" />
                                                          </xsl:attribute>
															<input name="lang" type="hidden" value="{$CurrentLang}" />
															<input name="se_mcrid" type="hidden">
																<xsl:attribute name="value">
                                                                  <xsl:value-of
																	select="@xlink:href" />
                                                              </xsl:attribute>
															</input>
															<input name="te_mcrid" type="hidden">
																<xsl:attribute name="value">
                                                                  <xsl:value-of
																	select="@xlink:href" />
                                                              </xsl:attribute>
															</input>
															<input name="re_mcrid" type="hidden">
																<xsl:attribute name="value">
                                                                  <xsl:value-of
																	select="$obj_id" />
                                                              </xsl:attribute>
															</input>
															<input name="type" type="hidden" value="{$type}" />
															<input name="todo" type="hidden" value="seditder" />
															<input name="extparm" type="hidden">
																<xsl:variable name="deriv" select="@xlink:href" />
																<xsl:variable name="derivlink"
																	select="concat('mcrobject:',$deriv)" />
																<xsl:attribute name="value">
                                                                  <xsl:value-of
																	select="document($derivlink)/mcr_results/mcr_result/@label" />
                                                              </xsl:attribute>
															</input>
															<input type="image"
																src="{$WebApplicationBaseURL}images/workflow_deredit.gif"
																title="{i18n:translate('swf.derivate.editDerivate')}" />
														</form>
													</td>
                                                    <xsl:if test="$derivateWithURN=false() and urn:isAllowedObjectForURNAssignment($obj_id)">
                                                      <td width="{$cellWidth}" valign="center" align="center">
                                                        <form method="get">
                                                          <xsl:attribute name="action">
                                                          <xsl:value-of select="concat($WebApplicationBaseURL,'servlets/MCRAddURNToObjectServlet',$JSessionID)" />
                                                          </xsl:attribute>
                                                          <input name="lang" type="hidden" value="{$CurrentLang}" />
                                                          <input name="se_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                              <xsl:value-of select="@xlink:href" />
                                                            </xsl:attribute>
                                                          </input>
                                                          <input name="te_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                              <xsl:value-of select="@xlink:href" />
                                                            </xsl:attribute>
                                                          </input>
                                                          <input name="re_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                              <xsl:value-of select="$obj_id" />
                                                            </xsl:attribute>
                                                          </input>
                                                          <input name="type" type="hidden" value="{$type}" />
                                                          <input name="todo" type="hidden" value="addURNToDerivates" />
                                                          <input type="image" src="{$WebApplicationBaseURL}images/workflow_addnbn.gif" title="{i18n:translate('swf.urn.addURN')}" />
                                                        </form>
                                                      </td>
                                                    </xsl:if>
                                                    <xsl:if test="not($derivateWithURN) or ($derivateWithURN and contains($CurrentGroups,'admingroup'))">
                                                      <td width="{$cellWidth}" valign="center" align="center">
                                                        <form method="get">
                                                          <xsl:attribute name="action">
                                                            <xsl:value-of select="concat($WebApplicationBaseURL,'servlets/MCRStartEditorServlet',$JSessionID)" />
                                                          </xsl:attribute>
                                                          <input name="lang" type="hidden" value="{$CurrentLang}" />
                                                          <input name="se_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                              <xsl:value-of select="@xlink:href" />
                                                            </xsl:attribute>
                                                          </input>
                                                          <input name="te_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                              <xsl:value-of select="@xlink:href" />
                                                            </xsl:attribute>
                                                          </input>
                                                          <input name="re_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                              <xsl:value-of select="$obj_id" />
                                                            </xsl:attribute>
                                                          </input>
                                                          <input name="type" type="hidden" value="{$type}" />
                                                          <input name="todo" type="hidden" value="sdelder" />
                                                          <input type="image" src="{$WebApplicationBaseURL}images/workflow_derdelete.gif" title="{i18n:translate('swf.derivate.delDerivate')}" onClick="return confirmFormDelete();" />
                                                        </form>
                                                      </td>
                                                    </xsl:if>
												</xsl:if>
											</tr>
										</table>
									</td>
								</tr>
							</xsl:if>
							<tr id="detailed-whitespaces">
								<td></td>
							</tr>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
			</xsl:when>
			<!-- search result list -->
			<xsl:otherwise>
				<xsl:variable name="isdeletedExt">
					<xsl:for-each
						select="$knoten/mycoreobject/structure/derobjects/derobject">
						<xsl:variable name="derivID"
							select="./@xlink:href" />
						<xsl:variable name="derivateObj"
							select="document(concat('mcrobject:',$derivID))" />
						<xsl:variable name="isDeleted">
							<xsl:call-template name="isFlagSet">
								<xsl:with-param name="path"
									select="$derivateObj/mycorederivate" />
								<xsl:with-param name="flagName"
									select="'deleted'" />
							</xsl:call-template>
						</xsl:variable>
						<xsl:if test="not($isDeleted = 'true')">
							<xsl:value-of select="'false'" />
						</xsl:if>
					</xsl:for-each>
				</xsl:variable>
				<xsl:if test="($knoten/mycoreobject/structure/derobjects) and not($isdeletedExt = '')">
						<tr>
							<td id="leaf-additional">
								<xsl:call-template name="lineSpace" />
								<table cellpadding="0"
									cellspacing="0">
									<xsl:value-of
										select="i18n:translate('metaData.derivate.plural')" />
									<xsl:choose>
										<!-- fulltext hit -->
										<xsl:when test="mcr:metaData">
											<xsl:for-each
												select="mcr:metaData">
												<xsl:apply-templates
													select=".">
													<xsl:with-param
														name="objID" select="$obj_id" />
													<xsl:with-param
														name="objectXML" select="$knoten" />
												</xsl:apply-templates>
												<xsl:copy-of
													select="' (Treffer im Volltext)'" />
												<xsl:if
													test="position() != last()">
													<xsl:copy-of
														select="' '" />
													<xsl:call-template
														name="lineSpace" />
												</xsl:if>
											</xsl:for-each>
										</xsl:when>
										<!-- hit not in fulltext -->
										<xsl:otherwise>
											<xsl:for-each
												select="xalan:nodeset($knoten)/mycoreobject/structure/derobjects/derobject">
												<xsl:variable
													name="deriv" select="@xlink:href" />
												<xsl:variable
													name="derivlink" select="concat('mcrobject:',$deriv)" />
												<xsl:variable
													name="derivate" select="document($derivlink)" />
												<xsl:variable
													name="isDeleted">
													<xsl:call-template
														name="isFlagSet">
														<xsl:with-param
															name="path" select="$derivate/mycorederivate" />
														<xsl:with-param
															name="flagName" select="'deleted'" />
													</xsl:call-template>
												</xsl:variable>
												<xsl:if
													test="$isDeleted != 'true'">
													<xsl:apply-templates
														select="$derivate/mycorederivate/derivate/internals">
														<xsl:with-param
															name="objID" select="$obj_id" />
														<xsl:with-param
															name="objectXML" select="$knoten" />
													</xsl:apply-templates>
													<xsl:apply-templates
														select="$derivate/mycorederivate/derivate/externals">
														<xsl:with-param
															name="objID" select="$obj_id" />
														<xsl:with-param
															name="objectXML" select="$knoten" />
													</xsl:apply-templates>
													<xsl:if
														test="position()!=last()">
														<xsl:copy-of
															select="' '">
														</xsl:copy-of>
														<xsl:call-template
															name="lineSpace" />
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</xsl:otherwise>
									</xsl:choose>
								</table>
							</td>
						</tr>
					</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
		<!-- links  -->
		<xsl:choose>
			<xsl:when test="$knoten != '' and not(mcr:metaData)">
				<xsl:for-each select="$knoten/mycoreobject/metadata/ifsLinks/ifsLink">
					<xsl:variable name="derivateID" select="substring-before(./text(),'/')" />
					<xsl:variable name="derivateObj"
						select="document(concat('mcrobject:',$derivateID))" />
					<xsl:variable name="isDeleted">
						<xsl:call-template name="isFlagSet">
							<xsl:with-param name="path" select="$derivateObj/mycorederivate" />
							<xsl:with-param name="flagName" select="'deleted'" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:if test="$isDeleted != 'true'">
						<tr>
							<td id="leaf-additional">
								<xsl:call-template name="lineSpace" />
								<table cellpadding="0" cellspacing="0">
									<xsl:apply-templates select=".">
										<xsl:with-param name="objID" select="$obj_id" />
									</xsl:apply-templates>
									<xsl:if test="position()!=last()">
										<xsl:copy-of select="', '" />
										<xsl:call-template name="lineSpace" />
									</xsl:if>
								</table>
							</td>
						</tr>
					</xsl:if>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="/mycoreobject/metadata/ifsLinks/ifsLink">
					<xsl:variable name="derivateID" select="substring-before(./text(),'/')" />
					<xsl:variable name="derivateObj"
						select="document(concat('notnull:mcrobject:',$derivateID))" />
					<xsl:variable name="isDeleted">
						<xsl:call-template name="isFlagSet">
							<xsl:with-param name="path" select="$derivateObj/mycorederivate" />
							<xsl:with-param name="flagName" select="'deleted'" />
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="hasPermission"
						select="acl:checkPermission($obj_id,'deletedb')" />
					<xsl:if test="($isDeleted != 'true')">
						<tr>
							<td align="left" valign="top" id="detailed-links">
								<table cellpadding="0" cellspacing="0" id="detailed-contenttable">
									<tr>
										<td colspan="3" style="padding-left: 10px;">
											<xsl:choose>
												<xsl:when test="($derivateObj/null) and $hasPermission">
													<xsl:value-of
														select="i18n:translate('metaData.derivate.link.sourceNotexist')" />
												</xsl:when>
												<xsl:when test="not($derivateObj/null)">
													<xsl:apply-templates select=".">
														<xsl:with-param name="objID" select="$obj_id" />
													</xsl:apply-templates>
												</xsl:when>
											</xsl:choose>
										</td>
									</tr>
								</table>
							</td>
						</tr>
						<xsl:if test="$hasPermission">
							<tr>
								<td colspan="3" width="30" valign="top" align="center">
									<xsl:variable name="url">
										<xsl:value-of
											select="concat($ServletsBaseURL,'MCRJPortalLinkFileServlet?jportalLinkFileServlet.mode=removeLink&amp;jportalLinkFileServlet.from=',$obj_id,'&amp;jportalLinkFileServlet.to=',text())" />
									</xsl:variable>
									<a href="{$url}">
										<img src="{$WebApplicationBaseURL}images/workflow_derdelete.gif"
											title="Diesen Link entfernen" alt="Diesen Link entfernen" />
									</a>
								</td>
							</tr>
						</xsl:if>
					</xsl:if>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--
		=====================================================================================================
	-->
	<!--
		prints the system data (createdate, modifydate and ID) for an mcr
		object
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="get.systemData">
		<xsl:if test="$CurrentUser!='gast'">
			<!--*** Created ************************************* -->
			<table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
				<xsl:call-template name="printMetaDates">
					<xsl:with-param select="'right'" name="textalign" />
					<xsl:with-param select="./service/servdates/servdate[@type='createdate']"
						name="nodes" />
					<xsl:with-param select="i18n:translate('editor.search.document.datecr')"
						name="label" />
				</xsl:call-template>
			</table>
			<!--*** Last Modified ************************************* -->
			<table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
				<xsl:call-template name="printMetaDates">
					<xsl:with-param select="'right'" name="textalign" />
					<xsl:with-param select="./service/servdates/servdate[@type='modifydate']"
						name="nodes" />
					<xsl:with-param select="i18n:translate('editor.search.document.datemod')"
						name="label" />
				</xsl:call-template>
			</table>
			<!--*** MyCoRe-ID ************************************* -->
			<table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
				<tr>
					<td id="detailed-labels" style="text-align:right;  padding-right: 5px;">
						<xsl:value-of select="i18n:translate('metaData.ID')" />
					</td>
					<td class="metavalue">
						<xsl:value-of select="./@ID" />
					</td>
				</tr>
			</table>
		</xsl:if>
	</xsl:template>
	<!--
		=====================================================================================================
	-->
	<!-- returns a string in iso date format -->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="jportalFormatISODate">
		<xsl:param name="date" />
		<xsl:param name="locale" select="$CurrentLang" />
		<xsl:value-of xmlns:mcrxml="xalan://org.mycore.common.xml.MCRJPortalXMLFunctions"
			select="mcrxml:formatISODate( string( $date ),string( $locale ) )" />
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!--
		prints each parameter and his value in a single row of a html table
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="printMetaDates">
		<!-- prints a table row for a given nodeset -->
		<xsl:param name="volume-node" />
		<xsl:param name="nodes" />
		<xsl:param name="label" select="local-name($nodes[1])" />
		<xsl:if test="$nodes">
			<tr>
				<td valign="top" id="detailed-labels">
					<xsl:value-of select="$label" />
				</td>
				<td class="metavalue">
					<xsl:for-each select="$nodes">
						<xsl:choose>
							<xsl:when test="../@class='MCRMetaClassification'">
								<xsl:call-template name="printClass">
									<xsl:with-param name="nodes" select="." />
									<xsl:with-param name="host" select="$objectHost" />
									<xsl:with-param name="next" select="', '" />
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
							<xsl:when test="@class='MCRMetaDerivateLink'">
								<xsl:call-template name="derivateLink" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:choose>
									<xsl:when test="$allowHTMLInArticles = 'true'">
										<xsl:call-template name="printI18N-allowHTML">
											<xsl:with-param name="nodes" select="." />
											<xsl:with-param name="host" select="$objectHost" />
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise>
										<xsl:call-template name="printI18N">
											<xsl:with-param name="nodes" select="." />
											<xsl:with-param name="host" select="$objectHost" />
										</xsl:call-template>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:if test="position()!=last()">
							<br />
						</xsl:if>
					</xsl:for-each>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<!--
		=====================================================================================================
	-->
	<!-- prints enumation of a parameter in a structured or flat layout  -->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="printMetaDate_typeSensitive">
		<xsl:param name="nodes" />
		<xsl:param name="label" />
		<xsl:param name="typeClassi" />
		<xsl:param name="mode" />
		<xsl:param name="layout" select="'structure'" />

		<xsl:if test="$nodes">
			<xsl:variable name="classXML">
				<xsl:copy-of
					select="xalan:nodeset(document(concat('classification:metadata:all:children:',$typeClassi)))" />
			</xsl:variable>
			<tr>
				<xsl:if test="$layout = 'structure'">
					<td valign="top" id="detailed-labels">
						<xsl:value-of select="$label" />
					</td>
				</xsl:if>
				<td>
					<xsl:choose>
						<xsl:when test="$layout='structure'">
							<xsl:attribute name="class">
                              <xsl:value-of select="'leaf-additional'" />
                            </xsl:attribute>
						</xsl:when>
						<xsl:when test="$layout='flat'">
							<xsl:attribute name="id">
                              <xsl:value-of select="'leaf-additional'" />
                            </xsl:attribute>
						</xsl:when>
					</xsl:choose>

					<!-- pass all categories, sorted by labels in $currentlang -->
					<xsl:for-each
						select="xalan:nodeset($classXML)/mycoreclass/categories/category">
						<xsl:sort select="./label[@xml:lang=$CurrentLang]/@text"
							order="ascending" />
						<xsl:variable name="categID">
							<xsl:value-of select="./@ID" />
						</xsl:variable>
						<!-- in mcrobject have been current categID found-->
						<xsl:if test="$nodes[@type=$categID] | $nodes[@xlink:title=$categID]">
							<!-- label of category -->
							<i>
								<xsl:value-of
									select="xalan:nodeset($classXML)/mycoreclass/categories/category[@ID=$categID]/label[@xml:lang=$CurrentLang]/@text" />
								:
							</i>
							<xsl:choose>
								<xsl:when test="$layout='structure'">
									<br />
									<ul>
										<xsl:for-each
											select="$nodes[@type=$categID] | $nodes[@xlink:title=$categID]">
											<xsl:sort order="ascending" select="./text()" />
											<li>
												<xsl:call-template name="printMetaDate_typeSensitive.printEntry">
													<xsl:with-param name="modeIF" select="$mode"></xsl:with-param>
												</xsl:call-template>
											</li>
										</xsl:for-each>
									</ul>
								</xsl:when>
								<xsl:when test="$layout='flat'">
									<xsl:for-each
										select="$nodes[@type=$categID] | $nodes[@xlink:title=$categID]">
										<xsl:sort order="ascending" select="./text()" />
										<xsl:call-template name="printMetaDate_typeSensitive.printEntry">
											<xsl:with-param name="modeIF" select="$mode"></xsl:with-param>
										</xsl:call-template>
										<xsl:if test="position()!=last()">
											<xsl:text>; </xsl:text>
										</xsl:if>
									</xsl:for-each>
								</xsl:when>
							</xsl:choose>
						</xsl:if>
					</xsl:for-each>
					<xsl:if test="$layout='flat'">
						<br />
						<br />
					</xsl:if>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<!--
		=====================================================================================================
	-->
	<!-- prints a single entry of the enumation -->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="printMetaDate_typeSensitive.printEntry">
		<xsl:param name="modeIF" />
		<xsl:choose>
			<xsl:when test="$modeIF='xlink'">
				<xsl:call-template name="objectLink">
					<xsl:with-param name="obj_id" select="@xlink:href" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$modeIF='date'">
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
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$allowHTMLInArticles = 'true'">
						<xsl:call-template name="printI18N-allowHTML">
							<xsl:with-param name="nodes" select="./text()" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="printI18N">
							<xsl:with-param name="nodes" select="./text()" />
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--
		=====================================================================================================
	-->
	<!--
		prints a set of nodes in the current language, html tags will be
		retained and displayed correctly
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="printI18N-allowHTML">
		<xsl:param name="nodes" />
		<xsl:param name="next" />
		<xsl:variable name="selectPresentLang">
			<xsl:call-template name="selectPresentLang">
				<xsl:with-param name="nodes" select="$nodes" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:for-each select="$nodes[lang($selectPresentLang)]">
			<xsl:if test="position() != 1">
				<xsl:value-of select="$next" disable-output-escaping="yes" />
			</xsl:if>
			<xsl:call-template name="lf2br-allowHTML">
				<xsl:with-param name="string" select="." />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>
	<!--
		=====================================================================================================
	-->
	<!--
		convert line feed (lf) to new line (br) -> doesnt touch the html tags
	-->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="lf2br-allowHTML">
		<xsl:param name="string" />
		<xsl:choose>
			<xsl:when test="contains($string,'&#xA;')">
				<xsl:value-of select="substring-before($string,'&#xA;')" />
				<!-- replace line break character by xhtml tag -->
				<br />
				<xsl:call-template name="lf2br">
					<xsl:with-param name="string"
						select="substring-after($string,'&#xA;')" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of disable-output-escaping="yes" select="$string" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
		=====================================================================================================
	-->
	<!-- TODO -->
	<!--
		=====================================================================================================
	-->
	<xsl:template name="showAdminHead">
		<xsl:if
			test="acl:checkPermission(./@ID,'writedb') or acl:checkPermission(./@ID,'deletedb')">
			<table border="0" cellspacing="0" cellpadding="0"
				id="detailed-divlines">
				<tr>
					<td colspan="2" id="detailed-innerdivlines">
						<br />
					</td>
				</tr>
			</table>
			<table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
				<tr>
					<td id="detailed-headlines">
						<xsl:value-of select="i18n:translate('metaData.headlines.admin')" />
					</td>
					<td>
						<br />
					</td>
				</tr>
			</table>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>