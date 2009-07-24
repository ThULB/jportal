<?xml version="1.0" encoding="UTF-8"?>

  <!-- ============================================== -->
  <!-- $Revision: 1.25 $ $Date: 2007-12-06 16:17:45 $ -->
  <!-- ============================================== -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink xalan i18n">

  <xsl:include href="MyCoReLayout.xsl" />
  <xsl:include href="editor.xsl" />
  <xsl:include href="classificationBrowser.xsl" />
  <xsl:include href="workflow.xsl" />
  <xsl:include href="fileupload.xsl" />
  <xsl:include href="objecttypes.xsl" />
  <xsl:include href="sitemap.xsl" />
  <xsl:include href="search-website.xsl" />

  <xsl:variable name="PageTitle">
    <xsl:choose>
      <xsl:when test="/MyCoReWebPage/section/@i18n">
        <xsl:value-of select="i18n:translate(/MyCoReWebPage/section/@i18n)" />
      </xsl:when>
      <xsl:otherwise>
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
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="Servlet" select="'undefined'" />

  <!-- =============================================================================== -->

  <xsl:template match="/MyCoReWebPage">
    <xsl:choose>
      <xsl:when test="section[lang($CurrentLang)]">
        <xsl:for-each select="section">
          <xsl:choose>
            <xsl:when test="@direction = $direction">
              <xsl:apply-templates select="self::*[@direction = $direction]"/>
            </xsl:when>
            <xsl:when test="self::*[lang($CurrentLang) or lang('all') or contains(@alt,$CurrentLang)] != '' ">
              <xsl:apply-templates select="self::*[lang($CurrentLang) or lang('all') or contains(@alt,$CurrentLang)]" />
            </xsl:when>
          </xsl:choose>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="section">
          <xsl:choose>
            <xsl:when test="@direction = $direction">
              <xsl:apply-templates select="self::*[@direction = $direction]"/>
            </xsl:when>
            <xsl:when test="self::*[lang($DefaultLang) or lang('all') or contains(@alt,$DefaultLang)] != '' ">
              <xsl:apply-templates select="self::*[lang($DefaultLang) or lang('all') or contains(@alt,$DefaultLang)]"/>
            </xsl:when>
          </xsl:choose>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- =============================================================================== -->

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

  <xsl:template match="i18n">
    <xsl:value-of select="i18n:translate(@key)" />
  </xsl:template>

  <!-- =============================================================================== -->

</xsl:stylesheet>