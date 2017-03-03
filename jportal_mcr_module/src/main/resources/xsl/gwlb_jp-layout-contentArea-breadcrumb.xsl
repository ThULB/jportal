<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
				exclude-result-prefixes="i18n">

	<xsl:param name="rubric" />
	<xsl:param name="JP.GWLB.Author.Portal.Enabled.Journal.Ids"/>
	<xsl:param name="JP.GWLB.Author.Portal.GFA.Journal.Ids"/>
	<xsl:param name="JP.GWLB.Author.Portal.CMA.Journal.Ids"/>

	<xsl:template name="breadcrumb">
	<div id="bread">
	<div class="row">
		<div id="jp-breadcrumb-container" class="col-sm-12">
			<ol class="col-sm-10 breadcrumb jp-layout-mcbreadcrumb">
				<li>
					<a href="{$WebApplicationBaseURL}content/below/index.xml" target="_self">
						<i class="fa fa-reply" aria-hidden="true"></i>
						Zeitschriftenserver der VZG
					</a>
				</li>
				<xsl:variable name="journalID">
					<xsl:call-template name="jp.getJournalID"/>
				</xsl:variable>

				<xsl:if test="$journalID">
					<li>
						<a
								href="{$WebApplicationBaseURL}content/main/journalListGWLB.xml#*">
							<xsl:value-of select="'A-Z'" />
						</a>
					</li>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="/mycoreobject">
						<xsl:variable name="parents"
									  select="document(concat('parents:',/mycoreobject/@ID))/parents"/>
						<xsl:choose>
							<!--<xsl:when test="($rubric = 'essays') or ($rubric = 'recension')">-->
							<xsl:when test="$rubric = not('')">
								<xsl:apply-templates mode="jp.printListEntry"
													 select="$parents/parent"/>
								<xsl:call-template name="jp.printListEntryWithRubric">
									<xsl:with-param name="title"
													select="/mycoreobject/metadata/maintitles/maintitle[@inherited='0']"/>
									<xsl:with-param name="href" select="/mycoreobject/@ID"/>
								</xsl:call-template>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates mode="jp.printListEntry"
													 select="$parents/parent | /mycoreobject/metadata/maintitles/maintitle[@inherited='0'] | /mycoreobject/metadata/def.heading/heading"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="journal" select="document(concat('mcrobject:', $journalID))/mycoreobject"/>
						<xsl:call-template name="jp.printListEntryWithRubric">
							<xsl:with-param name="title"
											select="$journal/metadata/maintitles/maintitle[@inherited='0'] | $journal/metadata/def.heading/heading"/>
							<xsl:with-param name="href" select="$journalID"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</ol>
			<!--<xsl:call-template name="jp-layout-breadcrumb-scroller" />-->
		</div>
	</div>
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
		<div id="bread">
			<div class="row">
				<div id="jp-breadcrumb-container" class="col-sm-10">
					<ol class="breadcrumb jp-layout-mcbreadcrumb">
						<xsl:variable name="hash">
							<xsl:if test="not($objID = '')">
								<xsl:variable name="char"
															select="substring(document(concat('mcrobject:',$objID))/mycoreobject/metadata/maintitles/maintitle[last()]/text(), 1, 1)" />
								<xsl:if test="contains(concat($lcletters, $ucletters), $char)">
									<xsl:value-of select="$char" />
								</xsl:if>
							</xsl:if>
						</xsl:variable>
						<li>
							<a href="{$WebApplicationBaseURL}content/below/index.xml" target="_self">
								<i class="fa fa-reply" aria-hidden="true"></i>
								Zeitschriftenserver der VZG
							</a>
						</li>
						<li>
							<a href="{$WebApplicationBaseURL}content/main/journalListGWLB.xml#{$hash}">
								<xsl:value-of select="'A-Z'" />
							</a>
						</li>
						<xsl:if test="not($objID = '')">
							<li>
								<a href="{$WebApplicationBaseURL}receive/{$objID}">
									<xsl:apply-templates mode="jp.printListEntryContent"
																			 select="document(concat('mcrobject:',$objID))/mycoreobject/metadata/maintitles/maintitle" />
								</a>
							</li>
						</xsl:if>
						<xsl:if test="$returnURL">
							<li>
								<a href="{$returnURL}">
									<xsl:if test="$returnHash">
										<xsl:attribute name="href">
											<xsl:value-of select="concat($returnURL,'#',$returnHash)" />
										</xsl:attribute>
									</xsl:if>
									<xsl:choose>
										<xsl:when test="$returnName">
											<xsl:value-of select="i18n:translate($returnName)" />
										</xsl:when>
										<xsl:when test="$returnID">
											<xsl:apply-templates mode="jp.printListEntryContent"
																					 select="document(concat('mcrobject:',$returnID))/mycoreobject/metadata/maintitles/maintitle" />
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
									<xsl:value-of select="i18n:translate('jp.metadata.search.result')" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="i18n:translate('jp.metadata.search.search')" />
								</xsl:otherwise>
							</xsl:choose>
						</li>
					</ol>
				</div>
			</div>
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
				<xsl:with-param name="length" select="100" />
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
				<xsl:with-param name="length" select="100" />
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
				<xsl:if test="string-length($title) &gt;= 100">
					<xsl:attribute name="title">
						<xsl:value-of select="$title" />
					</xsl:attribute>
				</xsl:if>
				<xsl:call-template name="shortenString">
					<xsl:with-param name="string" select="$title" />
					<xsl:with-param name="length" select="100" />
				</xsl:call-template>
			</a>
		</li>

		<li>
			<span>
				<xsl:call-template name="shortenString">
					<xsl:with-param name="string" select="$rubric" />
					<xsl:with-param name="length" select="100" />
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
