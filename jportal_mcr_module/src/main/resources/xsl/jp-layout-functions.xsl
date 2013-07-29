<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Contains jportal specific layout functions.
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="xalan">

  <xsl:param name="MCR.Piwik.baseurl" />
  <xsl:param name="MCR.Piwik.enable" />
  <xsl:param name="MCR.Piwik.id" select="'1'" />

  <xsl:template mode="jp.printListEntry" match="*">
    <li>
      <xsl:apply-templates mode="jp.printListEntryContent" select="." />
    </li>
  </xsl:template>

  <xsl:template name="jp.piwik">
    <xsl:if test="$MCR.Piwik.enable = 'true' and $MCR.Piwik.baseurl != ''">
      <script type="text/javascript">
        var _paq = _paq || [];
        _paq.push(["trackPageView"]);
        _paq.push(["enableLinkTracking"]);
        (function() {
          var u = '<xsl:value-of select="$MCR.Piwik.baseurl" />';
          var journalID = '<xsl:value-of select="/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID" />';
          if(journalID != "") {
            _paq.push(['setCustomVariable', 1, "journal", journalID, "page"]);
          }
          _paq.push(["setTrackerUrl", u+"piwik.php"]);
          _paq.push(["setSiteId", '<xsl:value-of select="$MCR.Piwik.id" />']);
          var d=document, g=d.createElement("script"), s=d.getElementsByTagName("script")[0]; g.type="text/javascript";
          g.defer=true; g.async=true; g.src=u+"piwik.js"; s.parentNode.insertBefore(g,s);
        })();
      </script>
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.printClass">
    <xsl:param name="nodes" />
    <xsl:param name="languages" />
    <xsl:param name="lang" />
    
    <xsl:for-each select="$nodes">
      <xsl:variable name="label" select="./label[lang($lang)]/@text" />
      <xsl:choose>
        <xsl:when test="string-length($label) = 0">
          <xsl:call-template name="jp.printClass.fallback">
            <xsl:with-param name="node" select="." />
            <xsl:with-param name="languages" select="$languages" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$label" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="jp.printClass.fallback">
    <xsl:param name="node" />
    <xsl:param name="pos" select="1" />
    <xsl:param name="languages" />
    <xsl:variable name="classlabel" select="$node/label[lang($languages/lang[$pos]/text())]/@text" />
 	<xsl:choose>
      <xsl:when test="string-length($classlabel) != 0">
        <xsl:value-of select="$classlabel" />
      </xsl:when>
      <xsl:when test="$languages/lang[$pos + 1]">
        <xsl:call-template name="jp.printClass.fallback">
          <xsl:with-param name="node" select="$node" />
          <xsl:with-param name="pos" select="$pos + 1" />
          <xsl:with-param name="languages" select="$languages" />
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * PERSON NAME -->
  <!-- *************************************************** -->
  <xsl:template mode="jp.metadata.person.name" match="heading | alternative">
    <xsl:choose>
      <xsl:when test="name">
        <xsl:value-of select="name" />
        <xsl:if test="collocation">
          <xsl:value-of select="concat(' &lt;',collocation,'&gt;')" />
        </xsl:if>
      </xsl:when>
      <xsl:when test="firstName and lastName and collocation">
        <xsl:value-of select="concat(lastName,', ',firstName,' &lt;',collocation,'&gt;')" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="firstName and lastName and nameAffix">
          <xsl:value-of select="concat(lastName,', ',firstName,' ',nameAffix)" />
        </xsl:if>
        <xsl:if test="firstName and lastName and not(nameAffix)">
          <xsl:value-of select="concat(lastName,', ',firstName)" />
        </xsl:if>
        <xsl:if test="firstName and not(lastName or nameAffix)">
          <xsl:value-of select="firstName" />
        </xsl:if>
        <xsl:if test="not (firstName) and lastName">
          <xsl:value-of select="lastName" />
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="position() != last()">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>

  <!-- *************************************************** -->
  <!-- * PAGINATION -->
  <!-- *************************************************** -->
  <xsl:template name="jp.pagination.getResultInfoXML">
    <xsl:param name="response" />
    <xsl:variable name="start" select="$response/result/@start" />
    <xsl:variable name="rows">
      <xsl:choose>
        <xsl:when test="$response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']">
          <xsl:value-of select="$response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'10'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="numFound" select="$response/result/@numFound" />
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
  </xsl:template>

  <xsl:template match="response" mode="jp.pagination">
    <xsl:param name="startParam" select="'start'" />
  
    <xsl:variable name="resultInfoXML">
      <xsl:call-template name="jp.pagination.getResultInfoXML">
        <xsl:with-param name="response" select="/response" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="resultInfo" select="xalan:nodeset($resultInfoXML)" />

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
                    <xsl:with-param name="par" select="$startParam" />
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

          <xsl:call-template name="jp.pagination.createResultPages">
            <xsl:with-param name="resultInfo" select="$resultInfo" />
            <xsl:with-param name="startParam" select="$startParam" />
            <xsl:with-param name="i" select="$pageStart" />
            <xsl:with-param name="pageEnd" select="$pageEnd" />
          </xsl:call-template>
          <xsl:if test="($start + $rows) &lt; $numFound">
            <li>
              <a>
                <xsl:attribute name="href">
                  <xsl:call-template name="UrlSetParam">
                    <xsl:with-param name="url" select="$RequestURL" />
                    <xsl:with-param name="par" select="$startParam" />
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

  <xsl:template name="jp.pagination.createResultPages">
    <xsl:param name="resultInfo" />
    <xsl:param name="startParam" select="'start'" />
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
              <xsl:with-param name="par" select="$startParam" />
              <xsl:with-param name="value" select="($i - 1) * $resultInfo/rows" />
            </xsl:call-template> 
          </xsl:attribute>
          <xsl:value-of select="$i" />
        </a>
      </li>
      <xsl:call-template name="jp.pagination.createResultPages">
        <xsl:with-param name="resultInfo" select="$resultInfo" />
        <xsl:with-param name="startParam" select="$startParam" />
        <xsl:with-param name="pageEnd" select="$pageEnd" />
        <xsl:with-param name="i" select="$i+1" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>