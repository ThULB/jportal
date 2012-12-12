<?xml version="1.0" encoding="UTF-8"?>
<!-- Indexing Jportal object -->
<!-- This is pretty cool -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.0">

  <xsl:include href="object2fields.xsl" />

  <xsl:template mode="objValues" match="obj[@name='type']" priority="1">
    <field name="objectType">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template mode="objValues" match="obj[@name='project']" priority="1">
    <field name="objectProject">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="/solr-document-container/source/user" priority="1">
    <xsl:copy-of select="field" />
    <xsl:apply-templates mode="jportal.metadata" select="field" />
    <xsl:apply-templates mode="jportal.allMeta" select="field" />
  </xsl:template>

  <xsl:template match="mycoreobject|mycorederivate">
    <xsl:apply-templates select="structure" />
    <xsl:if test="contains(@ID, '_jpinst_')">
      <xsl:apply-templates mode="jportal.jpinst.metadata" select="metadata" />
    </xsl:if>
    <xsl:if test="contains(@ID, '_person_')">
      <xsl:apply-templates mode="jportal.person.metadata" select="metadata" />
    </xsl:if>
    <xsl:apply-templates mode="jportal.metadata" select="metadata" />
    <xsl:apply-templates mode="jportal.allMeta" select="metadata/*//*" />
    <xsl:apply-templates mode="jportal.link" select="*/*/*[@xlink:href]" />
    <xsl:apply-templates select="service" />
  </xsl:template>

  <xsl:template mode="jportal.metadata" match="text()">
  </xsl:template>

  <!-- journalID -->
  <xsl:template match="hidden_jpjournalsID/hidden_jpjournalID[position() = 1]" mode="jportal.metadata">
    <field name="journalID">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <!-- link -->
  <xsl:template match="*[@xlink:href]" mode="jportal.link">
    <field name="link">
      <xsl:value-of select="@xlink:href" />
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

  <!-- dates -->
  <xsl:template match="dates/date[@inherited='0']" mode="jportal.metadata">
    <field name="dates">
      <xsl:value-of select="text()" />
    </field>
    <field name="date.{@type}">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <!-- rubric -->
  <xsl:template match="rubrics/rubric[@inherited='0']" mode="jportal.metadata">
    <field name="rubric">
      <xsl:value-of select="concat(@classid, '#', @categid)" />
    </field>
  </xsl:template>

  <!-- participant -->
  <xsl:template match="participants/participant" mode="jportal.metadata">
    <xsl:if test="@type='mainPublisher'">
      <field name="publisher">
        <xsl:value-of select="concat(@xlink:href, '#', @xlink:title)" />
      </field>
    </xsl:if>
    <xsl:if test="position() = 1">
      <field name="participant">
        <xsl:value-of select="@xlink:title" />
      </field>
      <field name="participantID">
        <xsl:value-of select="@xlink:href" />
      </field>
    </xsl:if>
    <field name="participants">
      <xsl:value-of select="@xlink:title" />
    </field>
  </xsl:template>

  <!-- jpinst heading -->
  <xsl:template match="names/name/fullname" mode="jportal.jpinst.metadata">
    <field name="heading">
      <xsl:value-of select="." />
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
    <field name="position">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <!-- size -->
  <xsl:template match="sizes/size" mode="jportal.metadata">
    <field name="size">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <!-- allMeta -->
  <xsl:template match="*[translate(normalize-space(text()), ' ', '')!='']" mode="jportal.allMeta">
    <field name="allMeta">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="*[@xlink:title!='']" mode="jportal.allMeta">
    <field name="allMeta">
      <xsl:value-of select="@xlink:title" />
    </field>
  </xsl:template>

  <!-- person metadata -->
  <xsl:template mode="jportal.person.metadata" match="metadata" >
    <xsl:apply-templates mode="jportal.person.name" select="def.heading/heading|def.alternative/alternative"/>
  </xsl:template>

  <xsl:template mode="jportal.person.name" match="heading">
    <field name="heading">
      <xsl:apply-templates mode="jportal.person.heading" select="*" />     
    </field>
  </xsl:template>

  <xsl:template mode="jportal.person.name" match="alternative">
    <field name="alternatives">
      <xsl:apply-templates mode="jportal.person.heading" select="*" />
    </field>
  </xsl:template>

  <xsl:template mode="jportal.person.heading" match="lastName|personalName">
    <xsl:value-of select="."/>
  </xsl:template>
  
  <xsl:template mode="jportal.person.heading" match="firstName">
    <xsl:if test="preceding-sibling::lastName">
      <xsl:value-of select="', '"/>
    </xsl:if>
    <xsl:value-of select="."/>
  </xsl:template>
  
  <xsl:template mode="jportal.person.heading" match="collocation">
    <xsl:value-of select="concat(' &lt;',.,'&gt;')"/>
  </xsl:template>
  
  <xsl:template mode="jportal.person.heading" match="nameAffix">
    <xsl:value-of select="concat(' ',.)"/>
  </xsl:template>
</xsl:stylesheet>
