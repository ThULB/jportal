<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.25 $ $Date: 2007-12-06 16:17:45 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:include href="editor.xsl" />
    <xsl:include href="workflow.xsl" />
    <xsl:include href="fileupload.xsl" />
    <xsl:include href="objecttypes.xsl" />
    <xsl:include href="sitemap.xsl" />
    <xsl:include href="search-website.xsl" />
    <xsl:include href="journalList.xsl" />
    <xsl:variable name="PageTitle">
        <xsl:choose>
            <xsl:when test="/MyCoReWebPage/section[ lang($CurrentLang)]/@title != '' ">
                <xsl:value-of select="/MyCoReWebPage/section[lang($CurrentLang)]/@title" />
            </xsl:when>
            <xsl:when test="/MyCoReWebPage/section[@alt and contains(@alt,$CurrentLang)]/@title != '' ">
                <xsl:value-of select="/MyCoReWebPage/section[contains(@alt,$CurrentLang)]/@title" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="/MyCoReWebPage/section[lang($DefaultLang)]/@title" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="Servlet" select="'undefined'" />
    <!-- =============================================================================== -->
    <xsl:template match="/MyCoReWebPage">
        <xsl:choose>
            <xsl:when test="section[lang($CurrentLang) or lang('all')]">
                <xsl:apply-templates select="section[lang($CurrentLang) or lang('all')]" />
            </xsl:when>
            <xsl:when test="section[@alt and contains(@alt,$CurrentLang)]">
                <xsl:apply-templates select="section[contains(@alt,$CurrentLang)]" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="section[lang($DefaultLang)]" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- =============================================================================== -->
    <!-- - - - - - - - - Identity Transformation  - - - - - - - - - -->
    <xsl:template match='@*|node()'>
        <xsl:copy>
            <xsl:apply-templates select='@*|node()' />
        </xsl:copy>
    </xsl:template>
    <!-- =============================================================================== -->
    <xsl:template match="section">
        <xsl:for-each select="node()">
            <xsl:apply-templates select="." />
        </xsl:for-each>
    </xsl:template>
    <!-- =============================================================================== -->
</xsl:stylesheet>