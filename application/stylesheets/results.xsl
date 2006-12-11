<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 1.19 $ $Date: 2006/11/29 10:49:32 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xlink mcr i18n acl xsl">
  <xsl:include href="MyCoReLayout.xsl" />
  <!-- include custom templates for supported objecttypes -->
  <xsl:include href="objecttypes.xsl" />
  <xsl:variable name="PageTitle"
    select="i18n:translate('titles.pageTitle.results')" />
  <xsl:variable name="Servlet" select="'undefined'" />

  <!-- Trefferliste ausgeben -->
  <xsl:template match="/mcr:results">
    <xsl:variable name="ResultPages">
      <xsl:call-template name="PageGen">
        <xsl:with-param name="i" select="1" />
        <xsl:with-param name="id" select="@id" />
        <xsl:with-param name="size" select="@numPerPage" />
        <xsl:with-param name="currentpage" select="@page" />
        <xsl:with-param name="totalpage" select="@numPages" />
      </xsl:call-template>
    </xsl:variable>
    <table cellpadding="0" cellspacing="0" class="resultHeader">
      <tr>
        <td class="resort">
          <a
            href="{$WebApplicationBaseURL}{@mask}?XSL.editor.source.id={@id}"
            title="{condition[@format='text']/text()}">
            <xsl:value-of
              select="i18n:translate('results.detailedSearch')" />
          </a>
          |
          <a
            href="{$WebApplicationBaseURL}{@mask}?XSL.editor.source.new=true">
            <xsl:value-of select="i18n:translate('results.newSearch')" />
          </a>
        </td>
        <td class="resultPages">
          <xsl:value-of select="concat(i18n:translate('results.size'),' ')" />
          <xsl:copy-of select="$ResultPages" />
        </td>
        <td class="resultCount">
          <strong>
            <xsl:choose>
              <xsl:when test="@numHits=0">
                <xsl:value-of
                  select="i18n:translate('results.noObject')" />
              </xsl:when>
              <xsl:when test="@numHits=1">
                <xsl:value-of
                  select="i18n:translate('results.oneObject')" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of
                  select="i18n:translate('results.nObjects',@numHits)" />
              </xsl:otherwise>
            </xsl:choose>
          </strong>
        </td>
      </tr>
    </table>
    <xsl:comment>RESULT LIST START</xsl:comment>
    <table id="resultList" cellpadding="0" cellspacing="0">
      <xsl:for-each select="mcr:hit">
        <xsl:comment>RESULT ITEM START</xsl:comment>
        <!-- 
          LOCAL REQUEST
        -->
        <xsl:if test="@host = 'local'">
          <xsl:variable name="mcrobj" select="document(concat('mcrobject:',@id))/mycoreobject" />
          <xsl:apply-templates select=".">
            <xsl:with-param name="mcrobj" select="$mcrobj" />
            <xsl:with-param name="mcrobjlink">
              <xsl:call-template name="objectLink">
                <xsl:with-param name="obj_id" select="@id" />
              </xsl:call-template>
            </xsl:with-param>
          </xsl:apply-templates>
        </xsl:if>
        <!-- 
          REMOTE REQUEST
        -->
        <xsl:if test="@host != 'local'">
          <xsl:variable name="mcrobj"
            select="document(concat('mcrws:operation=MCRDoRetrieveObject&amp;host=',@host,'&amp;ID=',@id))/mycoreobject" />
          <xsl:apply-templates select=".">
            <xsl:with-param name="mcrobj" select="$mcrobj" />
            <xsl:with-param name="mcrobjlink">
              <!-- don't call objectLink here: WebService would be queried twice -->
              <a href="{$WebApplicationBaseURL}receive/{@id}{$HttpSession}?host={@host}">
                <xsl:apply-templates select="$mcrobj" mode="resulttitle" />
              </a>
            </xsl:with-param>
          </xsl:apply-templates>
        </xsl:if>
        <xsl:comment>RESULT ITEM END</xsl:comment>
      </xsl:for-each>
    </table>
    <xsl:comment>RESULT LIST END</xsl:comment>
    <div id="pageSelection">
      <xsl:value-of select="concat(i18n:translate('results.size'),' ')" />
      <xsl:copy-of select="$ResultPages" />
    </div>
  </xsl:template>

  <!-- Ein einzelner Treffer -->
  <!-- This is a default template, see document.xsl for a sample of a custom one-->
  <xsl:template match="mcr:hit">
    <xsl:param name="mcrobj" />
    <xsl:param name="mcrobjlink" />
    <xsl:variable name="DESCRIPTION_LENGTH" select="100" />

    <xsl:variable name="obj_id">
      <xsl:value-of select="@id" />
    </xsl:variable>
    <tr>
      <td class="resultTitle" colspan="2">
        <xsl:copy-of select="$mcrobjlink" />
      </td>
    </tr>
    <tr>
      <td class="description" colspan="2">
        <xsl:variable name="date">
          <xsl:call-template name="formatISODate">
            <xsl:with-param name="date"
              select="$mcrobj/service/servdates/servdate[@type='modifydate']" />
            <xsl:with-param name="format"
              select="i18n:translate('metaData.date')" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of
          select="i18n:translate('results.lastChanged',$date)" />
      </td>
    </tr>
  </xsl:template>

  <!-- Bugfix: provide empty template to allow compilation of custom objecttype stylsheets -->
  <xsl:template name="editobject_with_der" />
  <xsl:template name="editobject" />
</xsl:stylesheet>
