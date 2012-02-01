<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
	xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/"
	exclude-result-prefixes="mcr i18n">

	<xsl:include href="MyCoReLayout.xsl" />
	<xsl:include href="objecttypes.xsl" />

	<xsl:param name="selected" />

	<xsl:variable name="PageTitle" select="'Blättern A - Z'" />

	<!-- =================================================================== -->
	<xsl:template match="journalList[@url]" priority="2">
		<xsl:apply-templates select="document(@url)/journalList" />
	</xsl:template>

	<xsl:template match="journalList[@mode='alphabetical'] | journallist[@mode='alphabetical']">

		<xsl:variable name="objectCount" select="count(section/journal)" />

		<!-- do layout -->
		<xsl:choose>
			<xsl:when test="$objectCount > 0">
				<p>
					<xsl:choose>
						<xsl:when test="@type = 'journal'">
							<xsl:value-of select="i18n:translate('jportal.a-z.journal.introduction')" />
						</xsl:when>
						<xsl:when test="@type = 'calendar'">
							<xsl:value-of select="i18n:translate('jportal.a-z.calendar.introduction')" />
						</xsl:when>
					</xsl:choose>
				</p>
				<xsl:call-template name="journalList.printShortcuts">
					<xsl:with-param name="objectCount" select="$objectCount" />
				</xsl:call-template>
				<br />
				<br />
				<xsl:call-template name="journalList.printEntries">
					<xsl:with-param name="objectCount" select="$objectCount" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<p>
					<b>
						<xsl:choose>
							<xsl:when test="@type = 'journal'">
								<xsl:value-of select="i18n:translate('jportal.a-z.journal.emptyList')" />
							</xsl:when>
							<xsl:when test="@type = 'calendar'">
								<xsl:value-of select="i18n:translate('jportal.a-z.calendar.emptyList')" />
							</xsl:when>
						</xsl:choose>
					</b>
				</p>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template name="journalList.printShortcuts">
		<xsl:param name="objectCount" />

		<xsl:call-template name="journalList.seperator" />
		<xsl:for-each select="section">
    		<a href="?XSL.selected={@name}">
    			<b>
    				<xsl:value-of select="@name" />
    			</b>
    		</a>
			<xsl:call-template name="journalList.seperator" />
		</xsl:for-each>
	</xsl:template>

	<!-- =========================================================================== -->

	<xsl:template name="journalList.printEntries">
		<xsl:param name="objectCount" />
		<xsl:choose>
			<xsl:when test="$selected = ''">
				<p>
					<a style="font-weight:bold">
						<xsl:value-of select="section[position() = 1]/@name" />
					</a>
				</p>
				<xsl:for-each select="section[position() = 1]/journal">
					<xsl:call-template name="jpjournal.printResultListEntry">
						<xsl:with-param name="cXML" select="document(concat('mcrobject:',text()))" />
					</xsl:call-template>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<p>
					<a style="font-weight:bold">
						<xsl:value-of select="$selected" />
					</a>
				</p>
				<xsl:for-each select="section[@name = $selected]/journal">
					<xsl:call-template name="jpjournal.printResultListEntry">
						<xsl:with-param name="cXML" select="document(concat('mcrobject:',text()))" />
					</xsl:call-template>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- =========================================================================== -->

	<xsl:template name="journalList.seperator">
		<xsl:value-of select="'&#160;&#160;&#160;|&#160;&#160;&#160;'" />
	</xsl:template>

</xsl:stylesheet>