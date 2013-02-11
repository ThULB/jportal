<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- This stylesheets handles jportal specific stuff of XSL.Style=solr -->

  <xsl:template match="metadata" mode="user-application" priority="1">
    <xsl:apply-templates select="*" mode="jp-solr-export" />
  </xsl:template>

  <xsl:template match="node()" mode="jp-solr-export">
  </xsl:template>

  <!-- add contentClassi to the user generated variable -->
  <xsl:template match="*[contains(name(), 'contentClassis')]" mode="jp-solr-export">
    <xsl:apply-templates select="*" mode="jp-solr-export-classification" />
  </xsl:template>

  <!-- add volContentClassi -->
  <xsl:template match="*[contains(name(), 'volContentClassis')]" mode="jp-solr-export">
    <xsl:apply-templates select="*" mode="jp-solr-export-classification" />
  </xsl:template>

  <!-- add hidden_genhiddenfields -->
  <xsl:template match="*[contains(name(), 'hidden_genhiddenfields')]" mode="jp-solr-export">
    <xsl:apply-templates select="*" mode="jp-solr-export-text" />
  </xsl:template>

  <xsl:template match="*" mode="jp-solr-export-classification">
    <field name="{name()}">
      <xsl:value-of select="@categid" />
    </field>
  </xsl:template>

  <xsl:template match="*" mode="jp-solr-export-text">
    <field name="{name()}">
      <xsl:value-of select="text()" />
    </field>
  </xsl:template>

  <!-- text of rubric -->
  <xsl:template match="rubrics" mode="jp-solr-export">
    <xsl:apply-templates select="rubric" mode="jp-solr-export" />
  </xsl:template>

  <xsl:template match="rubric" mode="jp-solr-export">
    <xsl:apply-templates mode="jp-solr-export-rubric2fields" select="document(concat('classification:metadata:0:parents:', @classid, ':', @categid))/mycoreclass/categories//category/label" />
  </xsl:template>

  <xsl:template match="label" mode="jp-solr-export-rubric2fields">
    <field name="rubricText">
      <xsl:value-of select="@text" />
    </field>
  </xsl:template>

  <!-- enhance article and volume with journal data -->
  <xsl:template match="*[not(contains(/mycoreobject/@ID, 'jpjournal')) and contains(name(), 'hidden_jpjournalsID')]" mode="jp-solr-export">
    <xsl:variable name="journalID" select="hidden_jpjournalID/text()" />
    <xsl:apply-templates select="document(concat('mcrobject:', $journalID))/mycoreobject/metadata/*" mode="jp-solr-export" />
  </xsl:template>

</xsl:stylesheet>
