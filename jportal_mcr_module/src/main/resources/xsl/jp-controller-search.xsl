<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder">
  <xsl:param name="qt" select="'*'" />
  <xsl:param name="searchjournalID" select="''" />
  <xsl:param name="start" select="'0'" />
  <xsl:param name="rows" select="'10'" />

  <xsl:template match="jpsearch">
    <xsl:variable name="journalIDTerm">
      <xsl:if test="$searchjournalID != ''">
        <xsl:value-of select="concat(' journalID:', $searchjournalID)" />
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="queryTerm" select="encoder:encode(concat($qt, $journalIDTerm))" />
    <xsl:variable name="searchResults" select="document(concat('solr:q=', $queryTerm ,'&amp;rows=',$rows,'&amp;start=',$start,'&amp;defType=edismax'))"></xsl:variable>
    <xsl:apply-templates mode="searchResults" select="$searchResults" />
  </xsl:template>
</xsl:stylesheet>
