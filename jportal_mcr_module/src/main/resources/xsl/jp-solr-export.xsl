<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- This stylesheets handles jportal specific stuff of XSL.Style=solr -->

  <xsl:template match="metadata" mode="user-application" priority="1">
    <xsl:apply-templates select="*" mode="user-application" />
  </xsl:template>

  <!-- add contentClassi1 to the user generated variable -->
  <xsl:template match="*[contains(name(), 'contentClassis')]" mode="user-application" priority="1">
    <xsl:apply-templates select="*" mode="content-classification" />
  </xsl:template>

  <!-- add volContentClassi -->
  <xsl:template match="*[contains(name(), 'volContentClassis')]" mode="user-application" priority="1">
    <xsl:apply-templates select="*" mode="content-classification" />
  </xsl:template>

  <xsl:template match="*" mode="content-classification">
    <field name="{name()}">
      <xsl:value-of select="@categid" />
    </field>
  </xsl:template>

  <!-- text of rubric -->
  <xsl:template match="rubrics" mode="user-application" priority="1">
    <xsl:apply-templates select="rubric" mode="user-application" />
  </xsl:template>

  <xsl:template match="rubric" mode="user-application" priority="1">
    <xsl:apply-templates mode="rubric2fields" select="document(concat('classification:metadata:0:parents:', @classid, ':', @categid))/mycoreclass/categories//category/label" />
  </xsl:template>

  <xsl:template match="label" mode="rubric2fields">
    <field name="rubricText">
      <xsl:value-of select="@text" />
    </field>
  </xsl:template>

</xsl:stylesheet>
