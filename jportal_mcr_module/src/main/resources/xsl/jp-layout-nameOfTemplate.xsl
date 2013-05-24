<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xalan="http://xml.apache.org/xalan">
  <xsl:variable name="editorForm" select="'editor-jpjournal editor-jpvolume editor-jparticle'"></xsl:variable>
  <xsl:variable name="tagsWithTemplateInfo" select="/mycoreobject|/MyCoReWebPage/section/jpsearch|/MyCoReWebPage/section/jpadvancedsearch|/MyCoReWebPage/journalID"></xsl:variable>
  <xsl:template name="nameOfTemplate">
    <xsl:apply-templates mode="nameOfTemplate"
      select="$tagsWithTemplateInfo" />
  </xsl:template>

  <xsl:template mode="nameOfTemplate" match="mycoreobject[@xsi:noNamespaceSchemaLocation='datamodel-jpjournal.xsd']">
    <xsl:value-of select="metadata/hidden_templates/hidden_template" />
  </xsl:template>

  <xsl:template mode="nameOfTemplate" match="*">
    <xsl:variable name="journalID">
      <xsl:apply-templates mode="journalID" select="." />
    </xsl:variable>
    
    <xsl:if test="$journalID != ''">
      <!-- using URI resolver nameOfTemplate: is pretty ugly, should find a better solution -->
      <!-- stream will not close while using URI resolver notnull:mcrobject: which leads to "Too many open files" exception -->
      <xsl:value-of select="layoutTools:getNameOfTemplate($journalID)"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="getJournalID">
    <xsl:apply-templates mode="journalID"
      select="$tagsWithTemplateInfo" />
  </xsl:template>

  <xsl:template mode="journalID" match="mycoreobject">
    <xsl:value-of select="metadata/hidden_jpjournalsID/hidden_jpjournalID" />
  </xsl:template>

  <xsl:template mode="journalID" match="jpsearch | jpadvancedsearch">
    <xsl:value-of select="$searchjournalID" />
  </xsl:template>
  
  <xsl:template mode="journalID" match="journalID">
    <xsl:value-of select="." />
  </xsl:template>
</xsl:stylesheet>