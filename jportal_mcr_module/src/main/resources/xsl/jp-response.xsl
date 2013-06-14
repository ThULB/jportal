<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY html-output SYSTEM "xsl/xsl-output-html.fragment">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan i18n encoder">
  &html-output;

  <xsl:include href="MyCoReLayout.xsl" />
  <xsl:include href="jp-response-default.xsl" />
  <xsl:include href="jp-response-subselect.xsl" />

  <xsl:variable name="PageTitle" select="'Result'" />

  <xsl:template match="/response[lst/@name='error']">
    <b><xsl:value-of select="lst[@name='error']/int" /></b><br />
    <xsl:value-of select="lst[@name='error']/str" />
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * PAGINATION -->
  <!-- *************************************************** -->
  <xsl:variable name="resultInfoXML">
    <xsl:variable name="start" select="/response/result/@start" />
    <xsl:variable name="rows">
      <xsl:choose>
        <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']">
          <xsl:value-of select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'10'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="numFound" select="/response/result/@numFound" />
    <numFound>
      <xsl:value-of select="$numFound" />
    </numFound>
    <start>
      <xsl:value-of select="$start" />
    </start>
    <rows>
      <xsl:value-of select="$rows" />
    </rows>
    <page>
      <xsl:value-of select="ceiling($start div $rows)" />
    </page>
    <pages>
      <xsl:value-of select="ceiling($numFound div $rows)" />
    </pages>
  </xsl:variable>
  <xsl:variable name="resultInfo" select="xalan:nodeset($resultInfoXML)" />

  <xsl:template match="response" mode="pagination">
    <xsl:if test="$resultInfo/pages &gt; 1">
      <div id="resultPaginator" class="jp-layout-topline jp-layout-border-light">
        <xsl:variable name="start" select="$resultInfo/start" />
        <xsl:variable name="rows" select="$resultInfo/rows" />
        <xsl:variable name="numFound" select="$resultInfo/numFound" />
        <menu class="jp-layout-paginator jp-layout-horiz-menu jp-layout-inline">
          <xsl:if test="($start - $rows) &gt;= 0">
            <li>
              <a>
                <xsl:attribute name="href">
                  <xsl:call-template name="UrlSetParam">
                    <xsl:with-param name="url" select="$RequestURL" />
                    <xsl:with-param name="par" select="'start'" />
                    <xsl:with-param name="value" select="$start - $rows" />
                  </xsl:call-template> 
                </xsl:attribute>
                <xsl:value-of select="'&lt; Zurück'" />
              </a>
            </li>
          </xsl:if>
          <xsl:variable name="pageStart">
            <xsl:choose>
              <xsl:when test="($resultInfo/page - 4) &gt; 1">
                <xsl:value-of select="$resultInfo/page - 4" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="1" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="pageEnd">
            <xsl:choose>
              <xsl:when test="($resultInfo/page + 5) &gt; 10">
                <xsl:value-of select="$resultInfo/page + 5" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="10" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <xsl:call-template name="createResultPages">
            <xsl:with-param name="resultInfo" select="$resultInfo" />
            <xsl:with-param name="i" select="$pageStart" />
            <xsl:with-param name="pageEnd" select="$pageEnd" />
          </xsl:call-template>
          <xsl:if test="($start + $rows) &lt; $numFound">
            <li>
              <a>
                <xsl:attribute name="href">
                  <xsl:call-template name="UrlSetParam">
                    <xsl:with-param name="url" select="$RequestURL" />
                    <xsl:with-param name="par" select="'start'" />
                    <xsl:with-param name="value" select="$start + $rows" />
                  </xsl:call-template>
                </xsl:attribute>
                <xsl:value-of select="'Weiter &gt;'" />
              </a>
            </li>
          </xsl:if>
        </menu>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="createResultPages">
    <xsl:param name="pageEnd" />
    <xsl:param name="i" />

    <xsl:if test="$i &lt;= $resultInfo/pages and $i &lt;= $pageEnd">
      <li>
        <xsl:if test="$i = $resultInfo/page + 1">
          <xsl:attribute name="class">
            <xsl:value-of select="'jp-layout-selected-underline'" />
          </xsl:attribute>
        </xsl:if>
        <a>
          <xsl:attribute name="href">
             <xsl:call-template name="UrlSetParam">
              <xsl:with-param name="url" select="$RequestURL" />
              <xsl:with-param name="par" select="'start'" />
              <xsl:with-param name="value" select="($i - 1) * $resultInfo/rows" />
            </xsl:call-template> 
          </xsl:attribute>
          <xsl:value-of select="$i" />
        </a>
      </li>
      <xsl:call-template name="createResultPages">
        <xsl:with-param name="pageEnd" select="$pageEnd" />
        <xsl:with-param name="i" select="$i+1" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>