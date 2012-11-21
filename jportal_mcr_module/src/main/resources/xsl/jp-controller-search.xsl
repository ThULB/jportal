<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder">
  <xsl:param name="qt" select="'*'" />
  <xsl:param name="searchjournalID" select="''" />
  <xsl:param name="start" select="'0'" />
  <xsl:param name="rows" select="'10'" />

  <!-- For Subselect -->
  <xsl:param name="subselect.type" select="''" />
  <xsl:param name="subselect.session" select="''" />
  <xsl:param name="subselect.varpath" select="''" />
  <xsl:param name="subselect.webpage" select="''" />

  <xsl:template match="jpsearch" mode="default">
    <xsl:variable name="journalIDTerm">
      <xsl:if test="$searchjournalID != ''">
        <xsl:value-of select="concat(' journalID:', $searchjournalID)" />
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="subSelectTerm">
      <xsl:if test="$subselect.type != ''">
        <xsl:value-of select="'+%2BobjectType:person'" />
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="q" select="encoder:encode(concat($qt, $journalIDTerm), 'UTF-8')" />
    <xsl:variable name="qf" select="encoder:encode('titles^10 heading^10 dates^5 allMeta^1')" />
    <xsl:variable name="searchResults"
      select="document(concat('solr:q=', $q ,$subSelectTerm, '&amp;qf=',$qf,'&amp;rows=',$rows,'&amp;start=',$start,'&amp;defType=edismax'))"></xsl:variable>
    <xsl:apply-templates mode="searchResults" select="$searchResults" />
  </xsl:template>
</xsl:stylesheet>
