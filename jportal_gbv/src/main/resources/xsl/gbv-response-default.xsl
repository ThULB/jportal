<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- overwrite jp-response-default -> do not show facets in result list -->
  <xsl:template mode="resultList" match="response[result/@numFound &gt;= 1]" priority="1">
    <ul class="jp-layout-list-nodecoration">
      <xsl:apply-templates mode="searchResults" select="result/doc" />
    </ul>
    <div class="clear" />
    <xsl:apply-templates mode="jp.pagination" select="." />
  </xsl:template>

</xsl:stylesheet>