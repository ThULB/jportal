<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
                xmlns:math="http://exslt.org/math" exclude-result-prefixes="xalan xlink jpxml math">

  <xsl:include href="coreFunctions.xsl"/>
  <xsl:include href="jp-layout-functions.xsl"/>

  <xsl:template match="metadata" mode="metadata">
    <xsl:apply-templates mode="jportal.metadata" select="."/>
    <xsl:apply-templates mode="jportal.allMeta" select="."/>
    <xsl:apply-templates mode="jportal.link" select="*"/>
    <xsl:apply-templates mode="jportal.category" select="*"/>
    <xsl:call-template name="jportal.metadata.date.published">
      <xsl:with-param name="ID" select="../@ID"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="text()|@*" mode="jportal.link"/>
  <xsl:template match="text()|@*" mode="jportal.category"/>
  <xsl:template match="text()|@*" mode="jportal.metadata"/>
  <xsl:template match="text()|@*" mode="jportal.allMeta"/>
  <xsl:template match="text()|@*" mode="jportal.journal"/>

  <!-- *************************************************** -->
  <!-- * Journal, Article, Volume -->
  <!-- *************************************************** -->

  <!-- parent, order & ancestorPath -->
  <xsl:template match="parents/parent" mode="structure" priority="1">
    <field name="parent">
      <xsl:value-of select="@xlink:href"/>
    </field>
    <field name="order">
      <xsl:value-of select="jpxml:getOrder(../../../@ID)"/>
    </field>
    <field name="ancestorPath">
      <xsl:value-of select="jpxml:getAncestorPath(../../../@ID)"/>
    </field>
  </xsl:template>

  <!-- enhance article and volume -> hidden_jpjournalID required-->
  <xsl:template match="hidden_jpjournalsID/hidden_jpjournalID[position() = 1]" mode="jportal.metadata">

    <xsl:variable name="objectID" select="../../../@ID"/>
    <xsl:variable name="journalID" select="text()"/>

    <!-- journalID -->
    <field name="journalID">
      <xsl:value-of select="$journalID"/>
    </field>

    <!-- enhance article and volume with journal data -->
    <xsl:if test="contains($objectID, '_jpvolume_') or contains($objectID, '_jparticle_')">
      <xsl:variable name="journal" select="document(concat('mcrobject:', $journalID))/mycoreobject"/>
      <xsl:apply-templates select="$journal/metadata/*" mode="jportal.journal"/>
      <!-- participants should only be from the object -->
      <xsl:apply-templates select="$journal/metadata/participants/participant" mode="jportal.participant"/>
      <xsl:apply-templates select="$journal/metadata/hidden_genhiddenfields1" mode="jportal.hiddenGenFields"/>
      <xsl:apply-templates select="$journal/metadata/hidden_genhiddenfields2" mode="jportal.hiddenGenFields"/>
      <xsl:apply-templates select="$journal/metadata/hidden_genhiddenfields3" mode="jportal.hiddenGenFields"/>
      <xsl:apply-templates select="$journal/metadata/journalTypes" mode="jportal.metadata"/>
    </xsl:if>

    <!-- enhance volume -->
    <xsl:if test="contains($objectID, '_jpvolume_')">
      <xsl:variable name="volumeTypes" select="document(concat('volumeType:', $objectID))/types"/>
      <xsl:for-each select="$volumeTypes/type">
        <field name="volumeType">
          <xsl:value-of select="text()"/>
        </field>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <!-- link -->
  <xsl:template match="*[@xlink:href]" mode="jportal.link">
    <field name="link">
      <xsl:value-of select="@xlink:href"/>
    </field>
  </xsl:template>

  <!-- category -->
  <xsl:template match="*[@class='MCRMetaClassification' and not(name(.) = 'journalTypes')]/*" mode="jportal.category">
    <field name="category">
      <xsl:value-of select="concat(@classid, ':', @categid)"/>
    </field>
    <field name="classification">
      <xsl:value-of select="@classid"/>
    </field>
  </xsl:template>

  <!-- maintitle -->
  <xsl:template match="maintitles/maintitle[@inherited='0']" mode="jportal.metadata">
    <field name="maintitle">
      <xsl:value-of select="text()"/>
    </field>
    <field name="titles">
      <xsl:value-of select="text()"/>
    </field>
  </xsl:template>

  <xsl:template match="maintitle[@inherited='0']" mode="jportal.journal">
    <field name="journalTitle">
      <xsl:value-of select="text()"/>
    </field>
  </xsl:template>

  <!-- titles -->
  <xsl:template match="subtitles/subtitle[@inherited='0']" mode="jportal.metadata">
    <field name="titles">
      <xsl:value-of select="text()"/>
    </field>
  </xsl:template>

  <!-- journal types -->
  <xsl:template match="journalTypes/journalType" mode="jportal.metadata">
    <field name="journalType">
      <xsl:value-of select="concat(@classid, ':', @categid)"/>
    </field>
    <field name="category">
      <xsl:value-of select="concat(@classid, ':', @categid)"/>
    </field>
    <field name="classification">
      <xsl:value-of select="@classid"/>
    </field>
  </xsl:template>

  <!-- roles -->
  <xsl:template match="def.role/role[@inherited='0']" mode="jportal.metadata">
    <field name="roles">
      <xsl:value-of select="text()"/>
    </field>
  </xsl:template>

  <!-- dates -->
  <xsl:template match="dates/date" mode="jportal.metadata">
    <field name="date.{@type}">
      <xsl:choose>
        <xsl:when test="@date != ''">
          <xsl:value-of select="@date"/>
        </xsl:when>
        <xsl:when test="@from != '' and @until != ''">
          <xsl:value-of select="concat('[', @from, ' TO ', @until, ']')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@from"/>
        </xsl:otherwise>
      </xsl:choose>
    </field>
    <xsl:if test="text()">
      <field name="dates">
        <xsl:value-of select="text()"/>
      </field>
    </xsl:if>
  </xsl:template>

  <xsl:template name="jportal.metadata.date.published">
    <xsl:param name="ID"/>
    <xsl:variable name="published" select="jpxml:getPublishedSolrDateRange($ID)"/>
    <xsl:if test="$published">
      <field name="published">
        <xsl:value-of select="$published"/>
      </field>
      <xsl:variable name="published_sort" select="jpxml:getPublishedSolrDate($ID)"/>
      <field name="published_sort">
        <xsl:value-of select="$published_sort"/>
      </field>
    </xsl:if>
  </xsl:template>

  <!-- rubric -->
  <xsl:template match="rubrics/rubric" mode="jportal.metadata">
    <xsl:if test="@inherited='0'">
      <field name="rubric">
        <xsl:value-of select="concat(@classid, '#', @categid)"/>
      </field>
    </xsl:if>
    <!-- resolve text -->
    <xsl:apply-templates mode="jportal.classText"
                         select="document(concat('classification:metadata:0:parents:', @classid, ':', @categid))/mycoreclass/categories//category/label">
      <xsl:with-param name="name" select="'rubricText'"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- participant -->
  <xsl:template match="participants/participant" mode="jportal.metadata">
    <xsl:call-template name="jp.index.participants"/>
  </xsl:template>

  <xsl:template match="participants/participant" mode="jportal.participant">
    <xsl:call-template name="jp.index.participants"/>
  </xsl:template>

  <xsl:template name="jp.index.participants">
    <xsl:variable name="name">
      <xsl:apply-templates select="document(concat('mcrobject:', @xlink:href))/mycoreobject" mode="jp.metadata.name"/>
    </xsl:variable>
    <field name="participants">
      <xsl:value-of select="$name"/>
    </field>
    <field name="participant.{@type}">
      <xsl:value-of select="concat(@xlink:href, '#', $name)"/>
    </field>
  </xsl:template>

  <!-- keywords -->
  <xsl:template match="keywords/keyword" mode="jportal.metadata">
    <field name="keywords">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>
  <xsl:template match="field[@name='jportal_class_00000083.top']" mode="jportal.metadata">
    <field name="keywords">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>

  <!-- position -->
  <xsl:template match="hidden_positions/hidden_position" mode="jportal.metadata">
    <field name="indexPosition">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>

  <!-- identifier (gnd, etc.) -->
  <xsl:template match="def.identifier/identifier | identifiers/identifier" mode="jportal.metadata">
    <field name="id.{@type}">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>

  <!-- size -->
  <xsl:template match="sizes/size" mode="jportal.metadata">
    <field name="size">
      <xsl:value-of select="."/>
    </field>
    <field name="indexPosition">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>

  <!-- derivateLink -->
  <xsl:template match="derivateLinks/derivateLink" mode="jportal.metadata">
    <field name="derivateLink">
      <xsl:value-of select="@xlink:href"/>
    </field>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * Person -->
  <!-- *************************************************** -->
  <xsl:template mode="jportal.metadata" match="def.heading/heading">
    <field name="heading_base">
      <xsl:apply-templates mode="jp.metadata.person.name" select="."/>
    </field>
  </xsl:template>

  <xsl:template mode="jportal.metadata" match="def.alternative/alternative">
    <field name="alternatives">
      <xsl:apply-templates mode="jp.metadata.person.name" select="."/>
    </field>
  </xsl:template>

  <xsl:template mode="jportal.metadata" match="def.dateOfBirth/dateOfBirth|def.dateOfDeath/dateOfDeath">
    <field name="{name()}">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * Institution -->
  <!-- *************************************************** -->
  <xsl:template mode="jportal.metadata" match="names[@class='MCRMetaInstitutionName']/name/fullname">
    <field name="heading_base">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>

  <xsl:template mode="jportal.metadata" match="def.doubletOf/doubletOf">
    <field name="{name()}">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * allMeta -->
  <!-- *************************************************** -->
  <xsl:template match="*[translate(normalize-space(text()), ' ', '') != '']" mode="jportal.allMeta">
    <field name="allMeta">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>
  <xsl:template match="*[@xlink:title != '']" mode="jportal.allMeta">
    <field name="allMeta">
      <xsl:value-of select="@xlink:title"/>
    </field>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * Classifications -->
  <!-- *************************************************** -->

  <xsl:template match="label" mode="jportal.classText">
    <xsl:param name="name"/>
    <field name="{$name}">
      <xsl:value-of select="@text"/>
    </field>
  </xsl:template>

  <!-- contentClassi and volContentClassi -->
  <xsl:template match="*[contains(name(), 'contentClassis') or contains(name(), 'volContentClassis')]"
                mode="jportal.metadata">
    <xsl:apply-templates select="*" mode="jportal.classification"/>
  </xsl:template>
  <xsl:template match="*[contains(name(), 'contentClassis') or contains(name(), 'volContentClassis')]"
                mode="jportal.journal">
    <xsl:apply-templates select="*" mode="jportal.classification"/>
  </xsl:template>

  <xsl:template match="*" mode="jportal.classification">
    <xsl:apply-templates mode="jportal.contentClassi"
                         select="document(concat('classification:metadata:0:parents:', @classid, ':', @categid))/mycoreclass/categories//category">
      <xsl:with-param name="name" select="name()"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*" mode="jportal.contentClassi">
    <xsl:param name="name"/>
    <field name="{$name}">
      <xsl:value-of select="@ID"/>
    </field>
  </xsl:template>

  <!-- add hidden_genhiddenfields -->
  <xsl:template match="*[contains(name(), 'hidden_genhiddenfields')]/*" mode="jportal.hiddenGenFields">
    <field name="{name()}">
      <xsl:value-of select="text()"/>
    </field>
  </xsl:template>

</xsl:stylesheet>
