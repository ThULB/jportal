<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder">
  <xsl:param name="qt" select="'*'" />
  <xsl:param name="op" select="'contains'" />
  <xsl:param name="searchjournalID" select="''" />
  <xsl:param name="resultpage" select="'1'" />

  <xsl:template match="jpsearch">
    <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

    <xsl:variable name="journalIDTerm">
      <xsl:if test="$searchjournalID != ''">
        <xsl:value-of select="encoder:encode(concat(' and (journalID = &quot;', $searchjournalID,'&quot;)'))" />
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="qt_lowerCase" select="translate($qt,$uppercase, $lowercase)"/>
    <xsl:variable name="queryTerm" select="encoder:encode(concat('(allMetaNotInherited ', $op,' &quot;', $qt_lowerCase,'&quot;)'))" />
    <xsl:variable name="searchResults" select="document(concat('query:term=',$queryTerm, $journalIDTerm, '&amp;numPerPage=2&amp;page=',$resultpage))"></xsl:variable>
    <xsl:apply-templates mode="searchResults" select="$searchResults" />
  </xsl:template>
</xsl:stylesheet>