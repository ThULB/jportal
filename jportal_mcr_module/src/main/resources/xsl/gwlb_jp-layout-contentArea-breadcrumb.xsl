<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="i18n">

	<xsl:param name="rubric" />
	<xsl:template name="breadcrumb">
		<div id="jp-breadcrumb-container" class="col-sm-10 col-sm-offset-1">
			<ol class="col-sm-10 breadcrumb jp-layout-mcbreadcrumb">
				<xsl:if
					test="contains(/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID, 'jpjournal')">
					<xsl:variable name="hash">
						<xsl:variable name="char"
							select="substring(/mycoreobject/metadata/maintitles/maintitle[last()]/text(), 1, 1)" />
						<xsl:if test="contains(concat($lcletters, $ucletters), $char)">
							<xsl:value-of select="$char" />
						</xsl:if>
					</xsl:variable>
					<xsl:variable name="azList">
						<xsl:variable name="listType"
							select="layoutTools:getListType(/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID)" />
						<xsl:choose>
							<xsl:when test="$listType = 'calendar'">
								<xsl:value-of select="'calendar'" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="'journal'" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<li>
						<a
							href="{$WebApplicationBaseURL}content/main/{$azList}ListGWLB.xml#*">
							<xsl:value-of select="'A-Z'" />
						</a>
					</li>
				</xsl:if>
				<xsl:variable name="parents"
					select="document(concat('parents:',/mycoreobject/@ID))/parents" />
				<xsl:choose>
					<!--<xsl:when test="($rubric = 'essays') or ($rubric = 'recension')">-->
					<xsl:when test="$rubric = not('')">
						<xsl:apply-templates mode="jp.printListEntry"
											 select="$parents/parent" />
						<xsl:call-template name="jp.printListEntryWithRubric">
							<xsl:with-param name="title" select="metadata/maintitles/maintitle[@inherited='0']" />
							<xsl:with-param name="href" select="/mycoreobject/@ID" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates mode="jp.printListEntry"
											 select="$parents/parent | metadata/maintitles/maintitle[@inherited='0'] | metadata/def.heading/heading" />
					</xsl:otherwise>
				</xsl:choose>

			</ol>
			<!--<xsl:call-template name="jp-layout-breadcrumb-scroller" />-->
		</div>
	</xsl:template>

	<xsl:template name="resultListBreadcrumb">
		<xsl:param name="objID" />
		<ol class="jp-layout-hit-breadcrumb breadcrumb">
			<xsl:apply-templates mode="jp.printListEntry"
				select="document(concat('parents:',$objID))/parents/parent" />
		</ol>
	</xsl:template>

	<xsl:template name="searchBreadcrumb">
		<xsl:param name="objID" />
		<xsl:param name="returnURL" />
		<xsl:param name="returnHash" />
		<xsl:param name="returnID" />
		<xsl:param name="returnName" />
		<div id="jp-breadcrumb-container" class="col-sm-10 col-sm-offset-1">
			<ol class="breadcrumb jp-layout-mcbreadcrumb">
				<xsl:variable name="hash">
					<xsl:if test="not($objID = '')">	
						<xsl:variable name="char" select="substring(document(concat('mcrobject:',$objID))/mycoreobject/metadata/maintitles/maintitle[last()]/text(), 1, 1)" />
							<xsl:if test="contains(concat($lcletters, $ucletters), $char)">
								<xsl:value-of select="$char" />
							</xsl:if>
					</xsl:if>
				</xsl:variable>
				<xsl:variable name="azList">
					<xsl:variable name="listType">
						<xsl:if test="not($objID = '')">
							 <xsl:value-of select="layoutTools:getListType(document(concat('mcrobject:',$objID))/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID)" />
						</xsl:if>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="$listType = 'calendar'">
							<xsl:value-of select="'calendar'" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="'journal'" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<li>
					<a href="{$WebApplicationBaseURL}content/main/{$azList}ListGWLB.xml#{$hash}">
						<xsl:value-of select="'A-Z'" />
					</a>
				</li>
				<xsl:if test="not($objID = '')">
					<li>
						<a href="{$WebApplicationBaseURL}receive/{$objID}">
							<xsl:apply-templates mode="jp.printListEntryContent" select="document(concat('mcrobject:',$objID))/mycoreobject/metadata/maintitles/maintitle" />
						</a>
					</li>
				</xsl:if>
				<xsl:if test="$returnURL">
					<li>
						<a href="{$returnURL}">
							<xsl:if test="$returnHash"><xsl:attribute name="href"><xsl:value-of select="concat($returnURL,'#',$returnHash)"/></xsl:attribute></xsl:if>
							<xsl:choose>
								<xsl:when test="$returnName">
									<xsl:value-of select="i18n:translate($returnName)"/>
								</xsl:when>
								<xsl:when test="$returnID">
									<xsl:apply-templates mode="jp.printListEntryContent" select="document(concat('mcrobject:',$returnID))/mycoreobject/metadata/maintitles/maintitle" />
								</xsl:when>
								<xsl:otherwise>
									no data for name
								</xsl:otherwise>
							</xsl:choose>
						</a>
					</li>
				</xsl:if>
				<li>
					<xsl:choose>
						<xsl:when test="$returnURL">
							<xsl:value-of select="i18n:translate('jp.metadata.search.result')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="i18n:translate('jp.metadata.search.search')"/>
						</xsl:otherwise>
					</xsl:choose>
				</li>
			</ol>
		</div>
	</xsl:template>

	<xsl:template mode="jp.printListEntryContent" match="parent">
		<a
			href="{$WebApplicationBaseURL}receive/{@xlink:href}?XSL.referer={@referer}"
			alt="{@xlink:title}">
			<xsl:if test="string-length(@xlink:title) &gt;= 20">
				<xsl:attribute name="title">
          <xsl:value-of select="@xlink:title" />
        </xsl:attribute>
			</xsl:if>
			<xsl:call-template name="shortenString">
				<xsl:with-param name="string" select="@xlink:title" />
				<xsl:with-param name="length" select="20" />
			</xsl:call-template>
		</a>
	</xsl:template>

	<xsl:template mode="jp.printListEntryContent" match="parent[@error!='']">
		<span style="color: red">
			<xsl:value-of select="concat(@error, ' (', @xlink:href, ')')" />
		</span>
	</xsl:template>

	<xsl:template mode="jp.printListEntryContent" match="maintitle[@inherited='0']">
		<span>
			<xsl:call-template name="shortenString">
				<xsl:with-param name="string" select="." />
				<xsl:with-param name="length" select="20" />
			</xsl:call-template>
		</span>
	</xsl:template>

	<xsl:template mode="jp.printListEntryContent" match="heading">
		<span>
			<xsl:apply-templates mode="jp.metadata.person.name"
				select="." />
		</span>
	</xsl:template>

	<xsl:template name="jp.printListEntryWithRubric">
		<xsl:param name="title"/>
		<xsl:param name="href"/>
		<li>
			<a
					href="{$WebApplicationBaseURL}receive/{$href}"
					alt="{$title}">
				<xsl:if test="string-length($title) &gt;= 20">
					<xsl:attribute name="title">
						<xsl:value-of select="$title" />
					</xsl:attribute>
				</xsl:if>
				<xsl:call-template name="shortenString">
					<xsl:with-param name="string" select="$title" />
					<xsl:with-param name="length" select="20" />
				</xsl:call-template>
			</a>
		</li>

		<li>
			<span>
				<xsl:call-template name="shortenString">
					<xsl:with-param name="string" select="$rubric" />
					<xsl:with-param name="length" select="20" />
				</xsl:call-template>
			</span>
		</li>
	</xsl:template>

	<xsl:template name="jp-layout-breadcrumb-scroller">
		<xsl:variable name="objectScroll"
			select="document(concat('objectScroll:', /mycoreobject/@ID))/scroll" />
		<ol class="col-sm-2 breadcrumb jp-layout-scroller hidden-xs">
			<xsl:if test="$objectScroll/previous/@id">
				<li>
					<a href="{$objectScroll/previous/@id}">
						<xsl:value-of select="'&#171; Zurück'" />
					</a>
				</li>
			</xsl:if>
			<xsl:if test="$objectScroll/next/@id">
				<li>
					<a href="{$objectScroll/next/@id}">
						<xsl:value-of select="'Weiter &#187; '" />
					</a>
				</li>
			</xsl:if>
		</ol>
	</xsl:template>

</xsl:stylesheet>
