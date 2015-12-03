<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan layoutTools mcrxml xlink">

  <xsl:variable name="tagsWithTemplateInfo" select="/mycoreobject|/MyCoReWebPage|/response|/journalList|/mcr_directory" />
<!--   <xsl:variable name="tagsWithTemplateInfo" select="/mycoreobject|/MyCoReWebPage|/response" /> -->
  <xsl:variable name="tagsWithSearchModeInfo" select="/response|/MyCoReWebPage" />

  <!-- *************************************************** -->
  <!-- * Template -->
  <!-- *************************************************** -->
  <xsl:template name="jp.getNameOfTemplate">
    <xsl:apply-templates mode="nameOfTemplate" select="$tagsWithTemplateInfo" />
  </xsl:template>

  <xsl:template mode="nameOfTemplate" match="journalList">
    <xsl:value-of select="template" />
  </xsl:template>
  
  <xsl:template mode="nameOfTemplate" match="mycoreobject[@xsi:noNamespaceSchemaLocation='datamodel-jpjournal.xsd']">
    <xsl:value-of select="metadata/hidden_templates/hidden_template" />
  </xsl:template>

  <xsl:template mode="nameOfTemplate" match="*">
    <xsl:variable name="journalID">
      <xsl:call-template name="jp.getJournalID" />
    </xsl:variable>
    <xsl:if test="$journalID != ''">
      <!-- using URI resolver nameOfTemplate: is pretty ugly, should find a better solution -->
      <!-- stream will not close while using URI resolver notnull:mcrobject: which leads to "Too many open files" exception -->
      <xsl:value-of select="layoutTools:getNameOfTemplate($journalID)" />
    </xsl:if>

  </xsl:template>

  <!-- *************************************************** -->
  <!-- * Journal ID -->
  <!-- *************************************************** -->
  <xsl:template name="jp.getJournalID">
    <xsl:variable name="urlJournalID">
      <xsl:call-template name="UrlGetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="'journalID'" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$urlJournalID != ''">
        <xsl:value-of select="$urlJournalID" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="journalID" select="$tagsWithTemplateInfo" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="journalID" match="mycoreobject">
    <xsl:value-of select="metadata/hidden_jpjournalsID/hidden_jpjournalID" />
  </xsl:template>

  <xsl:template mode="journalID" match="mcr_directory">
    <xsl:variable name="ownerID" select="ownerID"/>
    <xsl:variable name="derivateParent" select="document(concat('mcrobject:',$ownerID))/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href"/>
    <xsl:value-of select="layoutTools:getJournalID($derivateParent)"/>
  </xsl:template>

  <xsl:template mode="journalID" match="journalList">
        <xsl:value-of select="''" />
  </xsl:template>
  
  <xsl:template mode="journalID" match="MyCoReWebPage">
        <xsl:apply-templates mode="journalID" select="journalID|section/form/span[@id='journalID']" />
  </xsl:template>
  
  <xsl:template mode="journalID" match="journalID|span[@id='journalID']">
    <xsl:value-of select="." />
  </xsl:template>

  <xsl:template mode="journalID" match="response">
    <xsl:value-of select="lst[@name='responseHeader']/lst[@name='params']/str[@name='journalID']" />
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * Search Mode -->
  <!-- *************************************************** -->
  <xsl:template name="jp.getSearchMode">
    <xsl:apply-templates mode="searchMode" select="$tagsWithTemplateInfo" />
  </xsl:template>

  <xsl:template mode="searchMode" match="text()|@*" />

  <xsl:template mode="searchMode" match="response">
    <xsl:value-of
      select="substring-before(mcrxml:regexp($RequestURL, concat($WebApplicationBaseURL, 'servlets/solr/') , ''), '?')" />
  </xsl:template>

  <xsl:template mode="searchMode" match="MyCoReWebPage[section/jpadvancedsearch]">
    <xsl:value-of select="'advanced'" />
  </xsl:template>

</xsl:stylesheet>
