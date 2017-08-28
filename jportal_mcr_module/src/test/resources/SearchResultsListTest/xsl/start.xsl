<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="coreFunctions.xsl"/>
  <xsl:include href="jp-layout-contentArea-searchResults.xsl"/>
  <xsl:include href="jp-layout-contentArea-derivates.xsl"/>
  
  <xsl:variable name="qt" select="/query/queryTerm/@value"/>
  <xsl:variable name="WebApplicationBaseURL" select="'http://localhost:8291'"/>
  <xsl:variable name="searchjournalID" select="'jportal_jpjournal_000000001'"/>
  <xsl:variable name="RequestURL" select="'http://foo.de/bar'"/>
  <xsl:variable name="rows" select="1"/>
  
  <xsl:template match="/">
    <xsl:apply-templates mode="searchResults" select="."/>
  </xsl:template>
  
<!--   <xsl:template name="jpsearch.getResultInfo"> -->
<!--     <xsl:param name="response" /> -->
<!--     <xsl:variable name="start" select="result/@start" /> -->
<!--     <xsl:variable name="rows" select="lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']" /> -->
<!--     <xsl:variable name="numFound" select="result/@numFound" /> -->
<!--     <numFound> -->
<!--       <xsl:value-of select="$numFound" /> -->
<!--     </numFound> -->
<!--     <start> -->
<!--       <xsl:value-of select="$start" /> -->
<!--     </start> -->
<!--     <rows> -->
<!--       <xsl:value-of select="$rows" /> -->
<!--     </rows> -->
<!--     <page> -->
<!--       <xsl:value-of select="ceiling($start div $rows)" /> -->
<!--     </page> -->
<!--     <pages> -->
<!--       <xsl:value-of select="ceiling($numFound div $rows)" /> -->
<!--     </pages> -->
<!--   </xsl:template> -->
  
<!--   <xsl:template mode="searchResults" match="/"> -->
<!--     <xsl:apply-templates mode="searchResults" select="solrSearch/response" /> -->
<!--   </xsl:template> -->

<!--   <xsl:template mode="searchResults" match="response"> -->
<!--     <xsl:variable name="resultInfoXML"> -->
<!--       <xsl:call-template name="jpsearch.getResultInfo"> -->
<!--         <xsl:with-param name="repsonse" select="." /> -->
<!--       </xsl:call-template> -->
<!--     </xsl:variable> -->
<!--     <xsl:variable name="resultInfo" select="xalan:nodeset($resultInfoXML)" /> -->

<!--     <div id="searchResults"> -->
<!--       <div id="resultListHeader" class="jp-layout-bottomline jp-layout-border-light"> -->
<!--         <h2>Suchergebnisse</h2> -->
<!--         <div> -->
<!--           <xsl:apply-templates mode="searchResultText" select="."> -->
<!--             <xsl:with-param name="resultInfo" select="$resultInfo" /> -->
<!--           </xsl:apply-templates> -->
<!--         </div> -->
<!--       </div> -->
<!--       <div id="resultList"> -->
<!--         <xsl:apply-templates mode="resultList" select="."> -->
<!--           <xsl:with-param name="resultInfo" select="$resultInfo" /> -->
<!--         </xsl:apply-templates> -->
<!--       </div> -->
<!--     </div> -->
<!--   </xsl:template> -->
</xsl:stylesheet>