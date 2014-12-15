<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools"
  xmlns:mcr="http://www.mycore.org/" exclude-result-prefixes="layoutTools acl mcrxml mcr">

  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="RequestURL" />

  <xsl:include href="jp-layout-contentArea-breadcrumb.xsl" />
  <xsl:include href="jp-layout-contentArea-tableOfContent.xsl" />
  <xsl:include href="jp-layout-contentArea-derivates.xsl" />
  <xsl:include href="jp-layout-contentArea-metadata.xsl" />
  <xsl:include href="jp-layout-contentArea-linkedMetadata.xsl" />
  <xsl:include href="jp-history.xsl" />

  <xsl:variable name="settings" select="document('../xml/layoutDefaultSettings.xml')/layoutSettings" />
  <xsl:variable name="currentObjID" select="/mycoreobject/@ID" />
  <xsl:variable name="currentType" select="substring-before(substring-after(/mycoreobject/@ID,'_'),'_')" />
  <xsl:variable name="updatePerm" select="acl:checkPermission($currentObjID,'writedb')" />
  <xsl:variable name="deletePerm" select="acl:checkPermission($currentObjID,'deletedb')" />
  <xsl:variable name="dataModel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
  <xsl:variable name="hasChildren" select="count(/mycoreobject/structure/children) > 0" />
  <xsl:variable name="listType" select="layoutTools:getListType(/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID)" />
  <xsl:variable name="isPartOfCalendar" select="$listType = 'calendar'" />
  <xsl:variable name="isPartOfOnlineJournal" select="$listType = 'online'" />
  <xsl:variable name="isJournal" select="$currentType = 'jpjournal'" />

  <xsl:variable name="showMetadataAndDerivate"
    select="not($hasChildren) or (not($isJournal) and ($isPartOfOnlineJournal or $isPartOfCalendar)) or $updatePerm = 'true' or $deletePerm = 'true'" />

  <xsl:template priority="9" match="/mycoreobject">

    <!-- breadcrumb -->
    <xsl:if test="not($currentType='person' or $currentType='jpinst')">
      <xsl:call-template name="breadcrumb" />
    </xsl:if>

    <!-- title -->
    <div id="jp-maintitle" class="jp-layout-maintitle jp-layout-border">
      <xsl:apply-templates mode="printTitle"
        select="metadata/maintitles/maintitle[@inherited='0']|metadata/def.heading/heading|metadata/names[@class='MCRMetaInstitutionName']/name">
        <xsl:with-param name="allowHTML" select="$objSetting/title/@allowHTML" />
      </xsl:apply-templates>
      <div class="jp-layout-triangle"></div>
      <div class="jp-layout-triangle"></div>
    </div>

    <!-- children -->
    <xsl:if test="structure/children">
      <xsl:call-template name="tableOfContent">
        <xsl:with-param name="id" select="./@ID" />
      </xsl:call-template>
    </xsl:if>

    <!-- journal text -->
    <xsl:if test="@xsi:noNamespaceSchemaLocation='datamodel-jpjournal.xsd'">
      <xsl:apply-templates mode="renderIntroTxt" select="document(concat('notnull:journalFile:',@ID,'/intro.xml'))/MyCoReWebPage/section[@xml:lang='de']" />
    </xsl:if>

    <!-- metadata & derivate -->
    <xsl:if test="$showMetadataAndDerivate">
      <div id="jp-content-Bottom">
        <xsl:if test="structure/derobjects or metadata/derivateLinks">
          <div id="derivCol" class="col-sm-4">
            <xsl:call-template name="derivateDisplay">
              <xsl:with-param name="nodes" select="structure/derobjects|metadata/derivateLinks" />
              <xsl:with-param name="journalID" select="metadata/hidden_jpjournalsID/hidden_jpjournalID" />
            </xsl:call-template>
          </div>
        </xsl:if>
        <xsl:if test="metadata/child::node()[not(contains(name(), 'hidden_')) and */@inherited='0']">
          <dl class="col-sm-8 jp-layout-metadataList">
            <xsl:if test="not(structure/derobjects or metadata/derivateLinks)">
              <xsl:attribute name="class">col-sm-8 col-sm-offset-2 jp-layout-metadataList</xsl:attribute>
            </xsl:if>
            <xsl:variable name="ignore" select="'maintitles def.heading names logo'" />
            <xsl:apply-templates mode="metadataDisplay"
              select="metadata/child::node()[not(contains(name(), 'hidden_')) and not(contains($ignore, name())) and */@inherited='0']" />
            <xsl:if test="contains(@ID, '_person_') or contains(@ID, '_jpinst_')">
              <xsl:apply-templates mode="linkedArticles" select="." />
              <xsl:apply-templates mode="linkedCalendar" select="." />
            </xsl:if>
            <xsl:choose>
              <xsl:when test="metadata/derivateLinks/derivateLink">
                <xsl:apply-templates mode="metadataURN" select="metadata/derivateLinks/derivateLink" />
              </xsl:when>
              <xsl:when test="structure/derobjects/derobject">
                <xsl:apply-templates mode="metadataURN" select="structure/derobjects/derobject" />
              </xsl:when>
            </xsl:choose>
          </dl>
        </xsl:if>
      </div>
    </xsl:if>

  </xsl:template>

  <xsl:template match="/" mode="template">
    <xsl:param name="mcrObj" />
    <xsl:apply-templates mode="template" select="*">
      <xsl:with-param name="mcrObj" select="$mcrObj" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="renderIntroTxt" match="section[@xml:lang='de']">
    <div id="intro" class="jp-layout-intro">
      <xsl:copy-of select="@*|node()" />
    </div>
  </xsl:template>

  <xsl:template mode="printTitle" match="heading[@inherited='0']">
    <xsl:apply-templates mode="jp.metadata.person.name" select="." />
  </xsl:template>

  <xsl:template mode="printTitle" match="name">
    <xsl:value-of select="fullname" />
  </xsl:template>

  <xsl:template mode="printTitle" match="maintitle[@inherited='0']">
    <xsl:param name="allowHTML" select="$settings/title/@allowHTML" />
    <xsl:choose>
      <xsl:when test="$allowHTML='true'">
        <xsl:value-of disable-output-escaping="yes" select="." />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="." />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>