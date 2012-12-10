<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xalan="http://xml.apache.org/xalan">
  <xsl:variable name="editorForm" select="'editor-jpjournal editor-jpvolume editor-jparticle'"></xsl:variable>
  <xsl:template name="nameOfTemplate">
    <xsl:apply-templates mode="nameOfTemplate"
      select="/mycoreobject|/MyCoReWebPage//editor[contains($editorForm, @id)]|/MyCoReWebPage/section/jpsearch|/MyCoReWebPage/section/jpadvancedsearch" />
  </xsl:template>

  <xsl:template mode="nameOfTemplate" match="mycoreobject[@xsi:noNamespaceSchemaLocation='datamodel-jpjournal.xsd']">
    <xsl:value-of select="metadata/hidden_templates/hidden_template" />
  </xsl:template>

  <xsl:template mode="nameOfTemplate" match="*">
    <xsl:variable name="journalID">
      <xsl:call-template name="getJournalID" />
    </xsl:variable>
    
    <xsl:if test="$journalID != ''">
      <!-- using URI resolver nameOfTemplate: is pretty ugly, should find a better solution -->
      <!-- stream will not close while using URI resolver notnull:mcrobject: which leads to "Too many open files" exception -->
      <xsl:value-of select="document(concat('nameOfTemplate:',$journalID))/nameOfTemplate"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="getJournalID">
    <xsl:apply-templates mode="journalID"
      select="/mycoreobject|/MyCoReWebPage//editor[contains($editorForm, @id)]|/MyCoReWebPage/section/jpsearch|/MyCoReWebPage/section/jpadvancedsearch" />
  </xsl:template>

  <xsl:template mode="journalID" match="mycoreobject">
    <xsl:value-of select="metadata/hidden_jpjournalsID/hidden_jpjournalID" />
  </xsl:template>

  <xsl:template mode="journalID" match="editor[contains($editorForm, @id)]">
    <xsl:variable name="parentID">
      <xsl:choose>
        <xsl:when test="target-parameters/target-parameter[@name='parentID']">
          <xsl:value-of select="target-parameters/target-parameter[@name='parentID']" />
        </xsl:when>
        <xsl:when test="target-parameters/target-parameter[@name='mcrid']">
          <xsl:value-of select="target-parameters/target-parameter[@name='mcrid']" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="parentDoc" select="document(concat('parents:',$parentID))"/>
    <xsl:choose>
      <xsl:when test="$parentDoc/parents/parent[1]">
        <xsl:value-of select="$parentDoc/parents/parent[1]/@xlink:href" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$parentID" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="journalID" match="jpsearch | jpadvancedsearch">
    <xsl:value-of select="$searchjournalID" />
  </xsl:template>

</xsl:stylesheet>