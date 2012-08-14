<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <xsl:template name="nameOfTemplate">
    <xsl:apply-templates mode="nameOfTemplate" select="/mycoreobject|/MyCoReWebPage//var|/MyCoReWebPage/section/jpsearch" />
  </xsl:template>

  <xsl:template mode="nameOfTemplate" match="mycoreobject[@xsi:noNamespaceSchemaLocation='datamodel-jpjournal.xsd']">
    <xsl:value-of select="metadata/hidden_templates/hidden_template" />
  </xsl:template>

  <xsl:template mode="nameOfTemplate" match="*">
    <xsl:variable name="journalID">
      <xsl:call-template name="getJournalID"/>
    </xsl:variable>
    <xsl:if test="$journalID != ''">
      <xsl:apply-templates mode="nameOfTemplate" select="document(concat('notnull:mcrobject:',$journalID))/mycoreobject" />
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="getJournalID">
      <xsl:apply-templates mode="journalID" select="/mycoreobject|/MyCoReWebPage//var|/MyCoReWebPage/section/jpsearch" />
  </xsl:template>

  <xsl:template mode="journalID" match="mycoreobject">
    <xsl:value-of select="metadata/hidden_jpjournalsID/hidden_jpjournalID" />
  </xsl:template>

  <xsl:template mode="journalID" match="var[@name='/mycoreobject/@ID']">
    <xsl:value-of select="@value" />
  </xsl:template>

  <xsl:template mode="journalID" match="jpsearch">
    <xsl:value-of select="$searchjournalID" />
  </xsl:template>

</xsl:stylesheet>