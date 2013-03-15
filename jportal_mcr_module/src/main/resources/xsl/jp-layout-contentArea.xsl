<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools" xmlns:mcr="http://www.mycore.org/" exclude-result-prefixes="layoutTools acl mcr">

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
  <xsl:variable name="updatePerm" select="acl:checkPermission($currentObjID,concat('update-',$currentType))" />
  <xsl:variable name="deletePerm" select="acl:checkPermission($currentObjID,concat('delete-',$currentType))" />
  <xsl:variable name="dataModel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
  <xsl:variable name="hasChildren" select="count(/mycoreobject/structure/children) > 0" />
  <xsl:variable name="listType" select="layoutTools:getListType(/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID)" />
  <xsl:variable name="isCalendar" select="$listType = 'calendar'" />
  <xsl:variable name="isOnlineJournal" select="$listType = 'online'" />

  <xsl:variable name="showMetadataAndDerivate" select="not($hasChildren) or $isCalendar or $isOnlineJournal or $updatePerm = 'true' or $deletePerm = 'true' or $dataModel = 'datamodel-jpjournal.xsd'" />

  <xsl:template priority="9" match="/mycoreobject">
  
    <xsl:variable name="objectEditingHTML">
      <editing>
        <xsl:call-template name="objectEditing">
          <xsl:with-param name="id" select="/mycoreobject/@ID" />
          <xsl:with-param name="dataModel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
        </xsl:call-template>
      </editing>
    </xsl:variable>
    <xsl:variable name="objectEditing" select="xalan:nodeset($objectEditingHTML)/editing" />
    <xsl:variable name="contentRColHtml">
      <xsl:choose>
        <xsl:when test="$objectEditing/menu[@id='jp-object-editing']//li/a">
          <div id="jp-content-RColumn" class="jp-layout-content-RCol">
            <xsl:copy-of select="$objectEditing/menu[@id='jp-object-editing' and li]" />
          </div>
          <class for="jp-content-LColumn">jp-layout-content-LCol-RCol</class>
        </xsl:when>
        <xsl:otherwise>
          <class for="jp-content-LColumn">jp-layout-content-LCol-noRCol</class>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="contentRCol" select="xalan:nodeset($contentRColHtml)" />

    <xsl:if test="not($currentType='person' or $currentType='jpinst')">
      <xsl:call-template name="breadcrumb" />
    </xsl:if>

    <xsl:apply-templates mode="printTitle"
      select="metadata/maintitles/maintitle[@inherited='0']|metadata/def.heading/heading|metadata/names[@class='MCRMetaInstitutionName']/name">
      <xsl:with-param name="allowHTML" select="$objSetting/title/@allowHTML" />
    </xsl:apply-templates>
    <xsl:variable name="LColumnHTML">
      <div id="jp-content-LColumn" class="jp-layout-content-LCol {$contentRCol/class[@for='jp-content-LColumn']}">
        <ul>
          <xsl:if test="structure/children">
            <xsl:call-template name="tableOfContent">
              <xsl:with-param name="id" select="./@ID" />
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="@xsi:noNamespaceSchemaLocation='datamodel-jpjournal.xsd'">
            <xsl:apply-templates mode="renderIntroTxt"
              select="document(concat('notnull:journalFile:',@ID,'/intro.xml'))/MyCoReWebPage/section[@xml:lang='de']" />
          </xsl:if>
        </ul>
      </div>
    </xsl:variable>
    <xsl:variable name="LColumn" select="xalan:nodeset($LColumnHTML)" />
    <xsl:if test="$LColumn/div/ul/li">
      <xsl:copy-of select="$LColumn/*" />
    </xsl:if>

    <!-- Edit -->
    <xsl:copy-of select="$contentRCol/div[@id='jp-content-RColumn']"></xsl:copy-of>

    <!-- metadata & derivate -->
    <xsl:if test="$showMetadataAndDerivate">
      <div id="jp-content-Bottom">
        <xsl:if test="metadata/child::node()[not(contains(name(), 'hidden_')) and */@inherited='0']">
        <dl class="jp-layout-metadataList">
          <xsl:variable name="ignore" select="'maintitles def.heading names logo'" />
          <xsl:apply-templates mode="metadataDisplay"
            select="metadata/child::node()[not(contains(name(), 'hidden_')) and not(contains($ignore, name())) and */@inherited='0']" />
          <xsl:if test="contains(@ID, '_person_') or contains(@ID, '_jpinst_')">
            <xsl:apply-templates mode="linkedArticles" select="." />
            <xsl:apply-templates mode="linkedCalendar" select="." />
          </xsl:if>
        </dl>
        </xsl:if>
        <xsl:if test="structure/derobjects or metadata/derivateLinks">
          <div id="derivCol">
            <xsl:call-template name="derivateDisplay">
              <xsl:with-param name="nodes" select="structure/derobjects|metadata/derivateLinks" />
              <xsl:with-param name="journalID" select="metadata/hidden_jpjournalsID/hidden_jpjournalID" />
            </xsl:call-template>
          </div>
        </xsl:if>
      </div>
    </xsl:if>

    <!-- call dynamic template_*.xsl -->
    <xsl:variable name="templateXML">
      <template id="{$template}" mcrID="{@ID}">
      </template>
    </xsl:variable>
    <xsl:apply-templates select="xalan:nodeset($templateXML)" mode="template" >
      <!-- mcrObj is node mycoreobject root -->
      <xsl:with-param name="mcrObj" select="."/>
    </xsl:apply-templates>

  </xsl:template>

  <xsl:template match="/" mode="template">
    <xsl:param name="mcrObj"/>
    <xsl:apply-templates mode="template" select="*">
      <xsl:with-param name="mcrObj" select="$mcrObj"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="renderIntroTxt" match="section[@xml:lang='de']">
    <li>
      <div id="intro" class="jp-layout-intro">
        <xsl:apply-templates mode="renderView" select="*|text()" />
      </div>
    </li>
  </xsl:template>

  <xsl:template mode="printTitle" match="heading[@inherited='0']">
    <div id="jp-maintitle" class="jp-layout-maintitle jp-layout-border">
      <xsl:apply-templates mode="metadataPersName" select="." />
    </div>
  </xsl:template>

  <xsl:template mode="printTitle" match="name">
    <div id="jp-maintitle" class="jp-layout-maintitle jp-layout-border">
      <xsl:value-of select="fullname" />
    </div>
  </xsl:template>

  <xsl:template mode="printTitle" match="maintitle[@inherited='0']">
    <xsl:param name="allowHTML" select="$settings/title/@allowHTML" />

    <div id="jp-maintitle" class="jp-layout-maintitle jp-layout-border">
      <xsl:choose>
        <xsl:when test="$allowHTML='true'">
          <xsl:value-of disable-output-escaping="yes" select="." />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="." />
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

</xsl:stylesheet>