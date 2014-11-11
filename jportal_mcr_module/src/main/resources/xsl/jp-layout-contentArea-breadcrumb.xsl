<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink">

  <xsl:template name="breadcrumb">
    <div id="jp-breadcrumb-container" class="col-sm-12">
      <ol class="col-sm-10 breadcrumb jp-layout-mcbreadcrumb"> <!-- jp-layout-breadcrumb -->
        <xsl:if test="contains(/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID, 'jpjournal')">
          <xsl:variable name="hash">
            <xsl:variable name="char" select="substring(/mycoreobject/metadata/maintitles/maintitle[last()]/text(), 1, 1)" />
            <xsl:if test="contains(concat($lcletters, $ucletters), $char)">
              <xsl:value-of select="$char" />
            </xsl:if>
          </xsl:variable>
          <xsl:variable name="azList">
            <xsl:variable name="listType" select="layoutTools:getListType(/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID)" />
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
            <a href="{$WebApplicationBaseURL}content/main/{$azList}List.xml#{$hash}">
              <xsl:value-of select="'A-Z'" />
            </a>
          </li>
        </xsl:if>
        <xsl:variable name="parents" select="document(concat('parents:',/mycoreobject/@ID))/parents" />
        <xsl:apply-templates mode="jp.printListEntry" select="$parents/parent | metadata/maintitles/maintitle[@inherited='0'] | metadata/def.heading/heading" />
      </ol>
      <xsl:call-template name="jp-layout-breadcrumb-scroller" />
    </div>
  </xsl:template>

  <xsl:template name="resultListBreadcrumb">
    <xsl:param name="objID" />
    <menu class="jp-layout-searchBreadcrumb">
      <xsl:apply-templates mode="jp.printListEntry" select="document(concat('parents:',$objID))/parents/parent" />
    </menu>
  </xsl:template>

  <xsl:template name="searchBreadcrumb">
    <xsl:param name="objID" />
    <xsl:param name="currentPageName" />
    <ul class="jp-layout-searchBreadcrumb">
      <li>
        <a href="{$WebApplicationBaseURL}receive/$objID">
          <xsl:apply-templates mode="jp.printListEntryContent" select="document(concat('mcrobject:',$objID))/mycoreobject/metadata/maintitles/maintitle" />
        </a>
      </li>
      <li>
        <xsl:value-of select="$currentPageName" />
      </li>
    </ul>
  </xsl:template>

  <xsl:template mode="jp.printListEntryContent" match="parent">
    <a href="{$WebApplicationBaseURL}receive/{@xlink:href}?XSL.referer={@referer}" alt="{@xlink:title}">
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
      <xsl:apply-templates mode="jp.metadata.person.name" select="." />
    </span>
  </xsl:template>

  <xsl:template name="jp-layout-breadcrumb-scroller">
    <xsl:variable name="objectScroll" select="document(concat('objectScroll:', /mycoreobject/@ID))/scroll" />
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
