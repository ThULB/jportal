<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools"
  xmlns:mcr="http://www.mycore.org/" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:imprint="xalan://fsu.jportal.util.ImprintUtil" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="layoutTools acl mcrxml mcr xlink imprint i18n">

  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="RequestURL" />
  <xsl:param name="rubric" />

  <xsl:include href="gwlb_jp-layout-contentArea-breadcrumb.xsl" />
  <xsl:include href="gwlb_jp-layout-contentArea-tableOfContent.xsl" />
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
  <xsl:variable name="hasRubric" select="($rubric = 'essays') or ($rubric = 'recension')" />

  <xsl:variable name="showMetadataAndDerivate"
    select="($hasChildren and not($hasRubric) and not($isJournal)) or not($hasChildren) or (not($isJournal) and ($isPartOfOnlineJournal or $isPartOfCalendar)) or $updatePerm = 'true' or $deletePerm = 'true'" />

  <xsl:template priority="9" match="/mycoreobject">

    <!-- breadcrumb -->
    <xsl:if test="not($currentType='person' or $currentType='jpinst')">
      <xsl:call-template name="breadcrumb" />
    </xsl:if>
    <div class="jp-content container-fluid col-sm-10 col-sm-offset-1">
      <!-- left side -->
      <xsl:if test="$currentType != 'jparticle'">
        <div id="jp-journal-childs" class="col-sm-3">
          <xsl:if test="$currentType = 'jpvolume'">
            <xsl:call-template name="jp.backToJournal" />
          </xsl:if>
          <!-- children -->
          <xsl:if test="structure/children">
            <xsl:if test="$currentType = 'jpvolume'">
              <xsl:call-template name="jp.toc.buildVolumeSelect">
                <xsl:with-param name="parentID" select="structure/parents/parent/@xlink:href" />
              </xsl:call-template>
            </xsl:if>
            <xsl:call-template name="tableOfContent">
              <xsl:with-param name="id" select="./@ID" />
            </xsl:call-template>
            <xsl:if test="$currentType = 'jpvolume'">
              <xsl:call-template name="jp.printContentList">
                <xsl:with-param name="id" select="./@ID" />
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="$currentType = 'jpjournal'">
              <xsl:call-template name="jp.volumeLinks">
                <xsl:with-param name="id" select="./@ID" />
              </xsl:call-template>
            </xsl:if>
          </xsl:if>
        </div>
      </xsl:if>

      <!-- right side -->
      <xsl:choose>
      <xsl:when test="$currentType != 'jparticle' and $currentType != 'person'  and $currentType !='jpinst'">
        <div id="jp-journal-content" class="col-sm-9">
          <xsl:call-template name="jp.journal.content" />
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div id="jp-journal-content" class="col-sm-12">
          <xsl:call-template name="jp.journal.content" />
        </div>
      </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template match="/" mode="template">
    <xsl:param name="mcrObj" />
    <xsl:apply-templates mode="template" select="*">
      <xsl:with-param name="mcrObj" select="$mcrObj" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="renderIntroTxt" match="section[@xml:lang='de']">
    <xsl:attribute name="class">
      <xsl:value-of select="'jp-layout-intro jp-content-block'" />
    </xsl:attribute>
    <xsl:copy-of select="@*|node()" />
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

  <xsl:template name="jp.journal.content">
    <!-- title -->
    <div id="jp-maintitle" class="jp-layout-maintitle jp-layout-border">
      <xsl:apply-templates mode="printTitle"
                           select="metadata/maintitles/maintitle[@inherited='0']|metadata/def.heading/heading|metadata/names[@class='MCRMetaInstitutionName']/name">
        <xsl:with-param name="allowHTML" select="$objSetting/title/@allowHTML" />
      </xsl:apply-templates>
      <div class="jp-layout-triangle"></div>
      <div class="jp-layout-triangle"></div>
    </div>

    <!-- journal text -->
    <xsl:if test="@xsi:noNamespaceSchemaLocation='datamodel-jpjournal.xsd'">
      <div id="intro">
        <xsl:if test="imprint:has($journalID, 'greeting')">
          <xsl:apply-templates mode="renderIntroTxt" select="document(concat('notnull:journalFile:',@ID,'/intro.xml'))/MyCoReWebPage/section[@xml:lang='de']" />
        </xsl:if>
      </div>
    </xsl:if>

    <!-- metadata & derivate -->
    <xsl:if test="$showMetadataAndDerivate and ($currentType = 'jpvolume' and $rubric = '') or not($currentType = 'jpvolume') ">
      <div class="jp-content-block">
        <div class="row">
          <xsl:if test="structure/derobjects or metadata/derivateLinks">
            <div class="col-sm-4 jp-content-thumbnail">
              <xsl:call-template name="derivateDisplay">
                <xsl:with-param name="nodes" select="structure/derobjects|metadata/derivateLinks" />
                <xsl:with-param name="journalID" select="metadata/hidden_jpjournalsID/hidden_jpjournalID" />
              </xsl:call-template>
            </div>
          </xsl:if>
          <xsl:if test="metadata/child::node()[not(contains(name(), 'hidden_')) and */@inherited='0']">
            <dl class="col-sm-8 jp-layout-metadataList">
              <xsl:if test="not(structure/derobjects or metadata/derivateLinks)">
                <xsl:attribute name="class">col-sm-12 jp-layout-metadataList</xsl:attribute>
              </xsl:if>
              <xsl:variable name="ignore" select="'maintitles def.heading names logo'" />
              <xsl:variable name="elements">
                <xsl:choose>
                  <xsl:when test="$currentType = 'jpinst'">
                    names|alternatives|addresses|phones|urls|emails|notes|identifiers|logo|def.doubletOf
                  </xsl:when>
                  <xsl:when test="$currentType = 'person'">
                    def.heading|def.alternative|def.peerage|def.gender|def.contact|def.role|def.placeOfActivity|def.dateOfBirth|def.placeOfBirth|def.dateOfDeath|def.placeOfDeath|def.note|def.link|def.identifier|def.doubletOf
                  </xsl:when>
                  <xsl:when test="$currentType = 'jpjournal'">
                    maintitles|subtitles|participants|dates|traditions|identis|languages|rights|predeces|successors|ddcs|abstracts|notes|contentClassis1|contentClassis2|contentClassis3|contentClassis4|contentClassis5|contentClassis6|contentClassis7|maintitlesForSorting
                  </xsl:when>
                  <xsl:when test="$currentType = 'jpvolume'">
                    maintitles|subtitles|participants|dates|traditions|identis|collationNotes|volContentClassis1|volContentClassis2|volContentClassis3|volContentClassis4|volContentClassis5|volContentClassis6|abstracts|notes|people|publicationNotes|normedPubLocations|footNotes|bibEvidences|indexFields
                  </xsl:when>
                  <xsl:when test="$currentType = 'jparticle'">
                    maintitles|subtitles|participants|dates|refs|identis|sizes|keywords|abstracts|notes|types|rubrics|classispub|classispub2|classispub3|classispub4
                  </xsl:when>
                </xsl:choose>
              </xsl:variable>
              <xsl:apply-templates mode="metadataDisplay" select="metadata/child::node()[not(contains(name(), 'hidden_')) and not(contains($ignore, name())) and */@inherited='0']" >
                <xsl:sort order="ascending" select="string-length(substring-before($elements, name()))" data-type="number" />
              </xsl:apply-templates>
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
      </div>
    </xsl:if>

    <!--Print Article-->
    <xsl:if test="($currentType = 'jpvolume') and not($rubric = '')">
      <xsl:call-template name="jp.toc.printArticles">
        <xsl:with-param name="parentID" select="./@ID" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template name="jp.backToJournal">
    <div id="jp-volume-back-link">
      <a href="{$WebApplicationBaseURL}receive/{structure/parents/parent/@xlink:href}">
        zurück zur Zeitschrift
      </a>
    </div>
  </xsl:template>

  <xsl:template name="jp.volumeLinks">
    <xsl:param name="id" />
    <xsl:if test="imprint:has($id, 'link')">
      <div class="list-group">
        <a class="dt-collapse" data-toggle="collapse" data-target="#jp-journal-link-list">
          <span class="jp-layout-facet-group-head">
            Links
          </span>
          <i class="fa fa-sort-asc"></i>
          <i class="fa fa-sort-desc"></i>
        </a>
        <div class="list-group jp-list-group-special" id="jp-journal-link-list">
          <div id="jp-tableOfContent" class="jp-layout-tableOfContent list-group jp-list-group-special">
            <xsl:for-each select="imprint:getLinks($id)">
              <a class="list-group-item" href="{./@href}">
                <xsl:value-of select="./@text"></xsl:value-of>
              </a>
            </xsl:for-each>
          </div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>