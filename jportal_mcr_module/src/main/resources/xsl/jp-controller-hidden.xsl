<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template match="jpsearch" mode="hidden">
    <xsl:variable name="query" select="encoder:encode($hiddenQt, 'UTF-8')" />
    <xsl:variable name="searchResults" select="document(concat('solr:q=', $query ,'&amp;rows=',$rows,'&amp;start=',$start,'&amp;defType=edismax'))"></xsl:variable>
    <xsl:apply-templates mode="searchResults" select="$searchResults" />
  </xsl:template>

</xsl:stylesheet>
