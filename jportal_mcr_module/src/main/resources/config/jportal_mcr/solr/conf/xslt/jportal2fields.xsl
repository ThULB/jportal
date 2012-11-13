<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/1999/xhtml"
  version="1.0">
  <xsl:output method="html" encoding="UTF-8" media-type="text/html" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" />

  <xsl:include href="object2fields.xsl" />

  <xsl:template match="/" priority="1">
    <xsl:message><xsl:value-of select="'JPORTAL'" /></xsl:message>
    <add>
      <doc>
        <field name="objectType">
          <xsl:value-of select="substring-before(substring-after(/solr-document-container/source/*/@ID, '_'), '_')" />
        </field>
        <field name="objectProject">
          <xsl:value-of select="substring-before(/solr-document-container/source/*/@ID, '_')" />
        </field>

        <xsl:apply-templates />
        <xsl:apply-templates mode="entity" />
        <xsl:apply-templates mode="jportal.metadata" />
        <xsl:apply-templates mode="jportal.allMeta" />
      </doc>
    </add>
  </xsl:template>

  <!-- journalID -->
  <xsl:template match="/solr-document-container/source/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID[position() = 1]" priority="1">
    <field name="journalID">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <!-- maintitle -->
  <xsl:template match="/solr-document-container/source/mycoreobject/metadata/maintitles/maintitle" priority="1">
    <xsl:if test="@inherited='0'">
      <field name="maintitle">
        <xsl:value-of select="text()" />
      </field>
    </xsl:if>
    <!-- journalTitle -->
    <xsl:variable name="inheritedMax">
      <xsl:for-each select="../maintitle/@inherited">
        <xsl:sort data-type="number" />
        <xsl:if test="position() = last()">
          <xsl:value-of select="." />
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:if test="@inherited = $inheritedMax">
      <field name="journalTitle">
        <xsl:value-of select="text()" />
      </field>
    </xsl:if>
  </xsl:template>

  <!-- dates -->
  <xsl:template match="/solr-document-container/source/mycoreobject/metadata/dates/date[@inherited='0']" priority="1">
    <field name="date">
      <xsl:value-of select="text()" />
    </field>
    <field name="date.{@type}">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <!-- rubric -->
  <xsl:template match="/solr-document-container/source/mycoreobject/metadata/rubrics/rubric[@inherited='0']" priority="1">
    <field name="rubric">
      <xsl:value-of select="concat(@classid, '#', @categid)" />
    </field>
  </xsl:template>

  <!-- publisher & first participant-->
  <xsl:template match="/solr-document-container/source/mycoreobject/metadata/participants/participant" mode="jportal.metadata">
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
  </xsl:template>

  <!-- jpinst heading -->
  <xsl:template match="/solr-document-container/source/mycoreobject[contains(@ID, '_jpinst_')]" mode="jportal.metadata">
    <field name="heading">
      <xsl:value-of select="metadata/names/name/fullname" />
    </field>
  </xsl:template>

  <!-- allMeta -->
  <xsl:variable name="ignore" select="'servdates'"/>
  <xsl:template match="*" mode="jportal.allMeta">
    <xsl:if test="text()">
      <field name="allMeta">
        <xsl:value-of select="text()" />
      </field>
    </xsl:if>
    <xsl:if test="@xlink:title">
      <field name="allMeta">
        <xsl:value-of select="@xlink:title" />
      </field>
    </xsl:if>
    <xsl:if test="not(contains(name(), $ignore))">
      <xsl:apply-templates select="child::node()" mode="jportal.allMeta"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
