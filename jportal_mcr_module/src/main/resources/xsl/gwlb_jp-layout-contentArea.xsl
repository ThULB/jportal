<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
                xmlns:mcr="http://www.mycore.org/" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:imprint="xalan://fsu.jportal.util.ImprintUtil"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="layoutTools acl mcrxml mcr xlink imprint i18n">

  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="RequestURL" />
  <xsl:param name="rubric" />
  <xsl:param name="JP.GWLB.Author.Portal.GFA.Journal.Ids"/>
  <xsl:param name="JP.GWLB.Author.Portal.CMA.Journal.Ids"/>

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


    <div class="jp-content container-fluid col-sm-12">
      <!-- left side -->
      <div class="row">
      <xsl:call-template name="leftSide">
        <xsl:with-param name="id" select="@ID"/>
      </xsl:call-template>

      <!-- right side-->
    <xsl:choose>
      <xsl:when test="$currentType != 'jparticle' and not(contains($currentType,'person'))  and $currentType !='jpinst'">
        <div id="jp-journal-content" class="col-md-9 col-xs-6">
          <xsl:call-template name="jp.journal.content" />
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div id="jp-journal-content">
          <xsl:call-template name="jp.journal.content" />
        </div>
      </xsl:otherwise>
      </xsl:choose>
    </div>
    </div>
  </xsl:template>

  <xsl:template name="leftSide">
    <xsl:param name="id"/>

    <xsl:variable name="currentType" select="substring-before(substring-after($id,'_'),'_')"/>
    <xsl:variable name="obj" select="document(concat('mcrobject:',$id))/mycoreobject"/>

    <xsl:if test="$currentType != 'jparticle' and not(contains($currentType, 'person'))">
      <div id="jp-journal-childs" class="col-md-3 col-xs-4">
        <xsl:if test="$currentType = 'jpvolume'">
          <xsl:call-template name="jp.backToJournal"/>
        </xsl:if>
        <!-- children -->
        <xsl:if test="$obj/structure/children">
          <xsl:if test="$currentType = 'jpvolume'">
            <xsl:call-template name="jp.toc.buildVolumeSelect">
              <xsl:with-param name="parentID" select="$obj/structure/parents/parent/@xlink:href"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:call-template name="tableOfContent">
            <xsl:with-param name="id" select="$id"/>
          </xsl:call-template>
          <xsl:if test="$currentType = 'jpvolume'">
            <xsl:call-template name="jp.printContentList">
              <xsl:with-param name="id" select="$id"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="$currentType = 'jpjournal'">
            <xsl:call-template name="jp.volumeLinks">
              <xsl:with-param name="id" select="$id"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:if>


        <xsl:if test="$journalID !=''">

          <xsl:variable name="journal" select="document(concat('mcrobject:', $journalID))/mycoreobject" />
          <xsl:variable name="template" select="$journal/metadata/hidden_templates/hidden_template/text()" />
        </xsl:if>
         <!-- <xsl:if test="contains($template, 'template_gfa' or 'template_cma')">-->
            <xsl:if test="$template ='template_gfa' and not(contains($currentType, 'person'))">
              <div class="template-collapse">
              <a class="dt-collapse collapsed" data-toggle="collapse" data-target="#collapse1" >
                <span class="jp-layout-facet-group-head" id="portal">
                Autorenportal
                <i class="fa fa-sort-asc" />
                <i class="fa fa-sort-desc" />
                </span>
              </a>
              <div id="collapse1" class="collapse">
               <p id="portal">
                  <a href="{concat($WebApplicationBaseURL,'jp_templates/template_gfa/XML/becomeAutor.xml?journalID=', $id)}">
                    Wie werde
                    ich Autor
                  </a>
              </p>
                <p id="portal">
                  <a href="{concat($WebApplicationBaseURL,'jp_templates/template_gfa/XML/guideline.xml?journalID=', $id)}">
                    Richtlinien
                  </a>
                </p>
                <p id="portal">
                <a href="{concat($WebApplicationBaseURL,'jp_templates/template_gfa/XML/recessionOffer.xml?journalID=', $id)}">
                    Rezensionsangebote
                </a>
                </p>
              </div>
              </div>
            </xsl:if>
            <xsl:if test="$template = 'template_cma' and not(contains($currentType, 'person'))">
              <div class="template-collapse">
              <a class="dt-collapse collapsed" data-toggle="collapse" data-target="#collapse1">
                <span class="jp-layout-facet-group-head" id="portal">
                  Autorenportal
                  <i class="fa fa-sort-asc" />
                  <i class="fa fa-sort-desc" />
                </span>
              </a>
              <div id="collapse1" class="collapse">
                <p id="portal">
                <a href="{concat($WebApplicationBaseURL,'jp_templates/template_cma/XML/becomeAutor.xml?journalID=', $id)}">
                    Wie werde
                    ich Autor
                  </a>
                </p>
                <p id="portal">
                <a href="{concat($WebApplicationBaseURL,'jp_templates/template_cma/XML/guideline.xml?journalID=', $id)}">
                    Richtlinien
                  </a>
                </p>
              </div>
              </div>
            </xsl:if>
          <xsl:if test="$template != 'template_gwlb' and not(contains($currentType, 'person'))">
            <div class="imprint">
              <xsl:call-template name="imprint"/>
            </div>
          </xsl:if>
          <!--</xsl:if>-->
        <xsl:if test="$template = 'template_cma'">
          <div class="imprint" id="newsletter">
            <a href="{concat($WebApplicationBaseURL,'jp_templates/template_cma/XML/cmaNewsletter.xed?journalID=', $id)}">
              Newsletter
            </a>
          </div>
        </xsl:if>
        <xsl:if test="$template = 'template_gfa'">
          <div class="imprint" id="newsletter">
            <a href="{concat($WebApplicationBaseURL,'jp_templates/template_gfa/XML/gfaNewsletter.xed?journalID=', $id)}">
              Newsletter
            </a>
          </div>
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


  <!--to match the journalID at the webpage otherwise it will show the ID under the main div -->
  <xsl:template match="journalID" mode="webpage">
  </xsl:template>

  <!--structure for imprint and autorenportal -->
  <xsl:template match="section" mode="webpage">
    <xsl:choose>
      <xsl:when test="$journalID and $journalID != ''">
        <div class="jp-content container-fluid col-sm-12">
          <!-- left side -->
          <div class="row">
            <xsl:call-template name="leftSide">
              <xsl:with-param name="id" select="$journalID"/>
            </xsl:call-template>
            <div id="jp-journal-content" class="col-md-9 col-xs-6">
              <xsl:for-each select="node()">
                <xsl:apply-templates select="."/>
              </xsl:for-each>
            </div>
          </div>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <span class="section">
          <xsl:for-each select="node()">
            <xsl:apply-templates select="."/>
          </xsl:for-each>
        </span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="renderIntroTxt" match="section[@xml:lang]">
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

    <div id="jp-maintitle">
      <xsl:choose>
        <xsl:when test="contains(@ID,'journal')">
          <xsl:attribute name="class">
            <xsl:value-of select="'jp-layout-maintitle-big jp-layout-border'"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(/mycoreobject/@ID,'_jpvolume_')">
          <xsl:attribute name="class">
            <xsl:value-of select="'jp-layout-maintitle-big jp-layout-border volume'"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="class">
            <xsl:value-of select="'jp-layout-maintitle jp-layout-border'"/>
          </xsl:attribute>
        </xsl:otherwise>

      </xsl:choose>
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
          <xsl:variable name="journalIntro"
                        select="document(concat('notnull:journalFile:',@ID,'/intro.xml'))/MyCoReWebPage/section[@xml:lang='de']"/>
          <xsl:comment>
            <xsl:value-of select="$journalIntro"/>
          </xsl:comment>
          <xsl:choose>
            <xsl:when test="$journalIntro = not('')">
              <xsl:apply-templates mode="renderIntroTxt" select="$journalIntro"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates mode="renderIntroTxt"
                                   select="document(concat('notnull:journalFile:',@ID,'/intro.xml'))/MyCoReWebPage/section[@xml:lang='all']"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </div>
    </xsl:if>

    <!-- metadata & derivate -->
    <xsl:if test="$showMetadataAndDerivate and ($currentType = 'jpvolume' and $rubric = '') or not($currentType = 'jpvolume') ">
      <div class="jp-content-block">
        <!--<div class="row">-->
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
                    maintitles|participants|traditions|identis|subtitles|dates|languages|rights|predeces|successors|ddcs|abstracts|notes|contentClassis1|contentClassis2|contentClassis3|contentClassis4|contentClassis5|contentClassis6|contentClassis7|maintitlesForSorting
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
        <!--</div>-->
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
      <div class="list-group" id="vintage">
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

  <xsl:template name="imprint">
    <xsl:variable name="imprintHref">
      <xsl:choose>
        <xsl:when test="$journalID != '' and imprint:has($journalID, 'imprint')">
          <xsl:value-of select="concat($WebApplicationBaseURL, 'rsc/fs/imprint/webpage/', $journalID)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="imprint:has('index', 'imprint')">
              <xsl:value-of select="concat($WebApplicationBaseURL, 'rsc/fs/imprint/webpage/index')" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="concat($WebApplicationBaseURL, 'jp-imprint.xml')" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <a href="{$imprintHref}">
      <xsl:value-of select="i18n:translate('jp.site.imprint.gwlb')" />
    </a>
  </xsl:template>

</xsl:stylesheet>