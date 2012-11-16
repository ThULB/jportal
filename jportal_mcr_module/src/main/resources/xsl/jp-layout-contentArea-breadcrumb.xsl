<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:template name="breadcrumb">
    <div id="jp-breadcrumb-container">
      <menu class="jp-layout-breadcrumb">
        <xsl:if test="contains(/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID, 'jpjournal')">
          <xsl:variable name="hash" select="substring(/mycoreobject/metadata/maintitles/maintitle[last()]/text(), 1, 1)" />
          <xsl:variable name="listType">
            <xsl:choose>
              <xsl:when test="/mycoreobject/metadata/contentClassis1/contentClassi1/@categid ='calendar'">
                <xsl:value-of select="'calendar'" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'journal'" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <li>
            <a href="{$WebApplicationBaseURL}content/main/{$listType}List.xml#{$hash}">
              <xsl:value-of select="'A-Z'"/>
            </a>
          </li>
        </xsl:if>
        <xsl:apply-templates mode="printListEntry"
          select="document(concat('parents:',/mycoreobject/@ID))/parents/parent | metadata/maintitles/maintitle[@inherited='0'] | metadata/def.heading/heading" />
      </menu>
    </div>
  </xsl:template>

  <xsl:template name="resultListBreadcrumb">
    <xsl:param name="objID" />
    <menu class="jp-layout-searchBreadcrumb">
      <xsl:apply-templates mode="printListEntry" select="document(concat('parents:',$objID))/parents/parent" />
    </menu>
  </xsl:template>

  <xsl:template name="searchBreadcrumb">
    <xsl:param name="objID" />
    <xsl:param name="currentPageName" />

    <ul class="jp-layout-searchBreadcrumb">
      <li>
        <a href="{$WebApplicationBaseURL}receive/$objID">
          <xsl:apply-templates mode="printListEntryContent" select="document(concat('mcrobject:',$objID))/mycoreobject/metadata/maintitles/maintitle" />
        </a>
      </li>
      <li>
        <xsl:value-of select="$currentPageName" />
      </li>
    </ul>
  </xsl:template>

  <xsl:template mode="printListEntryContent" match="parent">
    <a href="{$WebApplicationBaseURL}receive/{@xlink:href}">
      <xsl:value-of select="@xlink:title" />
    </a>
  </xsl:template>

  <xsl:template mode="printListEntryContent" match="parent[@error!='']">
    <span style="color: red">
      <xsl:value-of select="concat(@error, '( ', @xlink:href, ')')" />
    </span>
  </xsl:template>

  <xsl:template mode="printListEntryContent" match="maintitle[@inherited='0']">
    <span>
      <xsl:call-template name="shortenString">
        <xsl:with-param name="string" select="." />
        <xsl:with-param name="length" select="20" />
      </xsl:call-template>
    </span>
  </xsl:template>
  
  <xsl:template mode="printListEntryContent" match="heading">
    <span>
      <xsl:apply-templates mode="metadataPersName" select="."/> 
    </span>
  </xsl:template>
</xsl:stylesheet>