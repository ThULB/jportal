<?xml version="1.0" encoding="UTF-8"?>
<!--
  This stylesheets handles jportal specific stuff of XSL.Style=solr
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Overwrite mycoreobject-solr.xsl behaviour -->
  <xsl:template match="*[@class='MCRMetaClassification']/*" priority="1">
  </xsl:template>

  <xsl:template match="metadata" mode="user-application" priority="1">
    <xsl:apply-templates select="*" mode="jp-solr-export" />
  </xsl:template>

  <xsl:template match="node()" mode="jp-solr-export">
  </xsl:template>

  <!-- add contentClassi and volContentClassi to the user generated variable -->
  <xsl:template match="*[contains(name(), 'contentClassis') or contains(name(), 'volContentClassis')]" mode="jp-solr-export">
    <xsl:for-each select="*">
      <xsl:apply-templates mode="jp-solr-export-classification"
          select="document(concat('classification:metadata:0:parents:', @classid, ':', @categid))/mycoreclass/categories//category">
        <xsl:with-param name="name" select="name()" />
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>

  <!-- add hidden_genhiddenfields -->
  <xsl:template match="*[contains(name(), 'hidden_genhiddenfields')]" mode="jp-hidden-gen">
    <xsl:apply-templates select="*" mode="jp-solr-export-text" />
  </xsl:template>

  <xsl:template match="*" mode="jp-solr-export-classification">
    <xsl:param name="name" />
    <field name="{$name}">
      <xsl:value-of select="@ID" />
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
  <xsl:template match="*[name() = 'hidden_jpjournalsID' and (contains(../../@ID, '_jpvolume_') or contains(../../@ID, '_jparticle_'))]" mode="jp-solr-export">
    <xsl:variable name="journalID" select="hidden_jpjournalID/text()" />
    <xsl:variable name="journal" select="document(concat('mcrobject:', $journalID))/mycoreobject" />
    <xsl:apply-templates select="$journal/metadata/*" mode="jp-solr-export" />
    <xsl:apply-templates select="$journal/metadata/hidden_genhiddenfields1" mode="jp-hidden-gen" />
    <xsl:apply-templates select="$journal/metadata/hidden_genhiddenfields2" mode="jp-hidden-gen" />
    <xsl:apply-templates select="$journal/metadata/hidden_genhiddenfields3" mode="jp-hidden-gen" />
  </xsl:template>

</xsl:stylesheet>
