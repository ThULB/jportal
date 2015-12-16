<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" 
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
  exclude-result-prefixes="xalan xlink jpxml">

  <xsl:include href="coreFunctions.xsl" />
  <xsl:include href="jp-layout-functions.xsl" />

  <xsl:template match="metadata" mode="metadata">
    <xsl:apply-templates mode="jportal.metadata" select="." />
    <xsl:apply-templates mode="jportal.allMeta" select="." />
    <xsl:apply-templates mode="jportal.link" select="*" />
    <xsl:apply-templates mode="jportal.category" select="*" />
  </xsl:template>

  <xsl:template match="text()|@*" mode="jportal.link" />
  <xsl:template match="text()|@*" mode="jportal.category" />
  <xsl:template match="text()|@*" mode="jportal.metadata" />
  <xsl:template match="text()|@*" mode="jportal.allMeta" />
  <xsl:template match="text()|@*" mode="jportal.journal" />

  <!-- *************************************************** -->
  <!-- * Journal, Article, Volume -->
  <!-- *************************************************** -->

  <!-- journalID -->
  <xsl:template match="hidden_jpjournalsID/hidden_jpjournalID[position() = 1]" mode="jportal.metadata">
    <xsl:variable name="objectID" select="../../../@ID" />
    <xsl:variable name="journalID" select="text()" />
    <field name="journalID">
      <xsl:value-of select="$journalID" />
    </field>
    <!-- enhance article and volume with journal data -->
    <xsl:if test="contains($objectID, '_jpvolume_') or contains($objectID, '_jparticle_')">
      <xsl:variable name="journal" select="document(concat('mcrobject:', $journalID))/mycoreobject" />
      <xsl:apply-templates select="$journal/metadata/*" mode="jportal.journal" />
			<!-- participants should only be from the object -->
      <xsl:apply-templates select="$journal/metadata/participants/participant" mode="jportal.participant" />
      <xsl:apply-templates select="$journal/metadata/hidden_genhiddenfields1" mode="jportal.hiddenGenFields" />
      <xsl:apply-templates select="$journal/metadata/hidden_genhiddenfields2" mode="jportal.hiddenGenFields" />
      <xsl:apply-templates select="$journal/metadata/hidden_genhiddenfields3" mode="jportal.hiddenGenFields" />
    </xsl:if>
  </xsl:template>

  <!-- link -->
  <xsl:template match="*[@xlink:href]" mode="jportal.link">
    <field name="link">
      <xsl:value-of select="@xlink:href" />
    </field>
  </xsl:template>

  <!-- category -->
  <xsl:template match="*[@class='MCRMetaClassification']/*" mode="jportal.category">
    <field name="category">
      <xsl:value-of select="concat(@classid, ':', @categid)" />
    </field>
    <field name="classification">
      <xsl:value-of select="@classid" />
    </field>
  </xsl:template>

  <!-- maintitle -->
  <xsl:template match="maintitles" mode="jportal.metadata">
    <xsl:apply-templates mode="jportal.metadata.maintitle" select="maintitle[@inherited='0']" />
    <xsl:apply-templates mode="jportal.metadata.journalTitle"
      select="maintitle[not(preceding-sibling::maintitle/@inherited &gt;= @inherited) and not(following-sibling::maintitle/@inherited &gt; @inherited)]" />
  </xsl:template>

  <xsl:template mode="jportal.metadata.maintitle" match="maintitle[@inherited='0']">
    <field name="maintitle">
      <xsl:value-of select="text()" />
    </field>
    <field name="titles">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <xsl:template mode="jportal.metadata.journalTitle"
    match="maintitle[not(preceding-sibling::maintitle/@inherited &gt;= @inherited) and not(following-sibling::maintitle/@inherited &gt; @inherited)]">
    <field name="journalTitle">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <!-- titles -->
  <xsl:template match="subtitles/subtitle[@inherited='0']" mode="jportal.metadata">
    <field name="titles">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>
  
  <!-- roles -->
  <xsl:template match="def.role/role[@inherited='0']" mode="jportal.metadata">
    <field name="roles">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <!-- dates -->
  <xsl:template match="dates" mode="jportal.metadata">
    <xsl:variable name="published" select="jpxml:getPublishedDate(xalan:nodeset(.))" />
    <xsl:if test="$published">
      <field name="published_sort">
        <xsl:value-of select="$published" />
      </field>
    </xsl:if>
    <xsl:apply-templates select="date[@inherited='0']" mode="jportal.metadata.date" />
  </xsl:template>

  <xsl:template match="date" mode="jportal.metadata.date">
    <field name="dates">
      <xsl:value-of select="text()" />
    </field>
    <field name="date.{@type}">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <!-- rubric -->
  <xsl:template match="rubrics/rubric" mode="jportal.metadata">
    <xsl:if test="@inherited='0'">
      <field name="rubric">
        <xsl:value-of select="concat(@classid, '#', @categid)" />
      </field>
    </xsl:if>
    <!-- resolve text -->
    <xsl:apply-templates mode="jportal.classText"
      select="document(concat('classification:metadata:0:parents:', @classid, ':', @categid))/mycoreclass/categories//category/label">
      <xsl:with-param name="name" select="'rubricText'" />
    </xsl:apply-templates>
  </xsl:template>

  <!-- participant -->
  <xsl:template match="participants/participant" mode="jportal.metadata">
    <xsl:call-template name="jp.index.participants" />
  </xsl:template>

  <xsl:template match="participants/participant" mode="jportal.participant">
    <xsl:call-template name="jp.index.participants" />
  </xsl:template>

  <xsl:template name="jp.index.participants">
    <xsl:variable name="name">
      <xsl:apply-templates select="document(concat('mcrobject:', @xlink:href))/mycoreobject" mode="jp.metadata.name" />
    </xsl:variable>
    <field name="participants">
      <xsl:value-of select="$name" />
    </field>
    <field name="participant.{@type}">
      <xsl:value-of select="concat(@xlink:href, '#', $name)" />
    </field>
  </xsl:template>

  <!-- keywords -->
  <xsl:template match="keywords/keyword" mode="jportal.metadata">
    <field name="keywords">
      <xsl:value-of select="." />
    </field>
  </xsl:template>
  <xsl:template match="field[@name='jportal_class_00000083.top']" mode="jportal.metadata">
    <field name="keywords">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <!-- position -->
  <xsl:template match="hidden_positions/hidden_position" mode="jportal.metadata">
    <field name="indexPosition">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <!-- identifier (gnd, pnd etc.) -->
  <xsl:template match="def.identifier/identifier | identifiers/identifier" mode="jportal.metadata">
    <field name="id.{@type}">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <!-- size -->
  <xsl:template match="sizes/size" mode="jportal.metadata">
    <field name="size">
      <xsl:value-of select="." />
    </field>
    <field name="indexPosition">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <!-- derivateLink -->
  <xsl:template match="derivateLinks/derivateLink" mode="jportal.metadata">
    <field name="derivateLink">
      <xsl:value-of select="@xlink:href" />
    </field>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * Person -->
  <!-- *************************************************** -->
  <xsl:template mode="jportal.metadata" match="def.heading/heading">
    <field name="heading_base">
      <xsl:apply-templates mode="jp.metadata.person.name" select="." />
    </field>
  </xsl:template>

  <xsl:template mode="jportal.metadata" match="def.alternative/alternative">
    <field name="alternatives">
      <xsl:apply-templates mode="jp.metadata.person.name" select="." />
    </field>
  </xsl:template>

  <xsl:template mode="jportal.metadata" match="def.dateOfBirth/dateOfBirth|def.dateOfDeath/dateOfDeath">
    <field name="{name()}">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * Institution -->
  <!-- *************************************************** -->
  <xsl:template mode="jportal.metadata" match="names[@class='MCRMetaInstitutionName']/name/fullname">
    <field name="heading_base">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template mode="jportal.metadata" match="def.doubletOf/doubletOf">
    <field name="{name()}">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * allMeta -->
  <!-- *************************************************** -->
  <xsl:template match="*[translate(normalize-space(text()), ' ', '') != '']" mode="jportal.allMeta">
    <field name="allMeta">
      <xsl:value-of select="." />
    </field>
  </xsl:template>
  <xsl:template match="*[@xlink:title != '']" mode="jportal.allMeta">
    <field name="allMeta">
      <xsl:value-of select="@xlink:title" />
    </field>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * Classifications -->
  <!-- *************************************************** -->

  <xsl:template match="label" mode="jportal.classText">
    <xsl:param name="name" />
    <field name="{$name}">
      <xsl:value-of select="@text" />
    </field>
  </xsl:template>

  <!-- contentClassi and volContentClassi -->
  <xsl:template match="*[contains(name(), 'contentClassis') or contains(name(), 'volContentClassis')]" mode="jportal.metadata">
    <xsl:apply-templates select="*" mode="jportal.classification" />
  </xsl:template>
  <xsl:template match="*[contains(name(), 'contentClassis') or contains(name(), 'volContentClassis')]" mode="jportal.journal">
    <xsl:apply-templates select="*" mode="jportal.classification" />
  </xsl:template>

  <xsl:template match="*" mode="jportal.classification">
    <xsl:apply-templates mode="jportal.contentClassi"
      select="document(concat('classification:metadata:0:parents:', @classid, ':', @categid))/mycoreclass/categories//category">
      <xsl:with-param name="name" select="name()" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*" mode="jportal.contentClassi">
    <xsl:param name="name" />
    <field name="{$name}">
      <xsl:value-of select="@ID" />
    </field>
  </xsl:template>

  <!-- add hidden_genhiddenfields -->
  <xsl:template match="*[contains(name(), 'hidden_genhiddenfields')]/*" mode="jportal.hiddenGenFields">
    <field name="{name()}">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

</xsl:stylesheet>