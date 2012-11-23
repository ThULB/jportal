<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder">
  <xsl:param name="qt" select="'*'" />
  <xsl:param name="op" select="'contains'" />
  <xsl:param name="searchjournalID" select="''" />
  <xsl:param name="resultpage" select="'1'" />

  <!-- For Subselect -->
  <xsl:param name="subselect.type" select="''" />
  <xsl:param name="subselect.session" select="''"/>
  <xsl:param name="subselect.varpath" select="''"/>
  <xsl:param name="subselect.webpage" select="''"/>
  
  <xsl:template match="jpsearch">
    <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
    
    <xsl:variable name="journalIDTerm">
      <xsl:if test="$searchjournalID != ''">
        <xsl:value-of select="encoder:encode(concat(' and (journalID = &quot;', $searchjournalID,'&quot;)'))" />
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="qt_lowerCase" select="translate($qt,$uppercase, $lowercase)"/>
<!--     <xsl:variable name="queryTerm" select="encoder:encode(concat('(allMetaNotInherited ', $op,' &quot;', $qt_lowerCase,'&quot;)'))" /> -->
    <xsl:variable name="queryTerm" >
      <xsl:variable name="searchField">
        <xsl:choose>
          <xsl:when test="$subselect.type = 'person'">
            <xsl:value-of select="'anyname'"></xsl:value-of>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'allMetaNotInherited'"></xsl:value-of>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      
      <xsl:value-of select="concat('(', $searchField, $op,' &quot;', $qt_lowerCase,'&quot;)')"/>
      <xsl:if test="$subselect.type != ''">
        <xsl:value-of select="concat(' and (objectType = ', $subselect.type, ')')"/>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="sortby">
      <xsl:if test="$subselect.type != ''">
        <xsl:value-of select="'&amp;sortby=sortName&amp;order=ascending'"/>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="searchResults" select="document(concat('query:term=',encoder:encode($queryTerm), $journalIDTerm, '&amp;numPerPage=10&amp;page=',$resultpage, $sortby))"></xsl:variable>
    <xsl:apply-templates mode="searchResults" select="$searchResults" />
  </xsl:template>
</xsl:stylesheet>