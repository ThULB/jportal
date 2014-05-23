<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Contains jportal specific layout functions.
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:math="xalan://java.lang.Math" exclude-result-prefixes="xalan math">

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
        var piwikURL = '<xsl:value-of select="$MCR.Piwik.baseurl" />';
        var journalID = '<xsl:value-of select="/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID" />';
        var pageID = '<xsl:value-of select="$MCR.Piwik.id" />';
        trackPageView(piwikURL, journalID, pageID);
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
  <!-- * NAMES -->
  <!-- *************************************************** -->
  <xsl:template mode="jp.metadata.title" match="mycoreobject">
    <!-- person/jpinst -->
    <xsl:apply-templates select="." mode="jp.metadata.name"/>
    <!-- journal/volume/article -->
    <xsl:apply-templates select="." mode="jp.metadata.maintitle"/>
  </xsl:template>

  <xsl:template mode="jp.metadata.maintitle" match="mycoreobject">
    <xsl:value-of select="metadata/maintitles/maintitle[@inherited='0']" />
  </xsl:template>

  <xsl:template mode="jp.metadata.name" match="mycoreobject">
    <xsl:if test="contains(@ID, '_person_')"> 
      <xsl:apply-templates mode="jp.metadata.person.name" select="metadata/def.heading/heading" />
    </xsl:if>
    <xsl:if test="contains(@ID, '_jpinst_')"> 
      <xsl:apply-templates mode="jp.metadata.jpinst.name" select="metadata/names" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="jp.metadata.jpinst.name" match="names[@class='MCRMetaInstitutionName']">
    <xsl:value-of select="name/fullname" />
  </xsl:template>

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
      <xsl:when test="lastName and collocation">
        <xsl:value-of select="concat(lastName, ' &lt;',collocation,'&gt;')" />
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
      <xsl:variable name="start" select="$resultInfo/start" />
      <xsl:variable name="rows" select="$resultInfo/rows" />

      <xsl:variable name="numFound" select="$resultInfo/numFound" />
      <xsl:variable name="pageStart" select="math:min(math:max($resultInfo/page - 2, 1), math:max($resultInfo/pages - 6, 1))" />
      <xsl:variable name="pageEnd" select="math:max(math:min($resultInfo/page + 4, $resultInfo/pages), math:min($resultInfo/pages, 7))" />

      <div class="center-block" id="resultPaginator">
        <ul class="pagination">
          <xsl:if test="($start - $rows) &gt;= 0">
            <xsl:call-template name="jp.pagination.entry">
              <xsl:with-param name="startParam" select="$startParam" />
              <xsl:with-param name="text" select="'&lt;'" />
              <xsl:with-param name="value" select="$start - $rows" />
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="$pageStart &gt; 1">
            <xsl:call-template name="jp.pagination.entry">
              <xsl:with-param name="startParam" select="$startParam" />
              <xsl:with-param name="text" select="'1'" />
              <xsl:with-param name="value" select="0" />
            </xsl:call-template>
            <li class="plain"><span>...</span></li>
          </xsl:if>
          <xsl:call-template name="jp.pagination.createResultPages">
            <xsl:with-param name="resultInfo" select="$resultInfo" />
            <xsl:with-param name="startParam" select="$startParam" />
            <xsl:with-param name="i" select="$pageStart" />
            <xsl:with-param name="pageEnd" select="$pageEnd" />
          </xsl:call-template>
          <xsl:if test="$pageEnd &lt; $resultInfo/pages">
            <li class="plain"><span>...</span></li>
            <xsl:call-template name="jp.pagination.entry">
              <xsl:with-param name="startParam" select="$startParam" />
              <xsl:with-param name="text" select="$resultInfo/pages" />
              <xsl:with-param name="value" select="$rows * ($resultInfo/pages - 1)" />
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="($start + $rows) &lt; $numFound">
             <xsl:call-template name="jp.pagination.entry">
              <xsl:with-param name="startParam" select="$startParam" />
              <xsl:with-param name="text" select="'&gt;'" />
              <xsl:with-param name="value" select="$start + $rows" />
            </xsl:call-template>
          </xsl:if>
        </ul>
        <xsl:if test="$resultInfo/pages &gt; 7">
          <div class="pagination pagination-jumper">
            <span class="input-group input-group-sm">
              <form class="pagination-jumper-form">
                <input class="form-control" placeholder="Seite" id="pagination-{$startParam}" />
                <span class="input-group-btn">
                  <input class="btn btn-default pagination-jump-submit" data-param="{$startParam}" data-rows="{$rows}"
                  data-pages="{$resultInfo/pages}" value="Go" type="submit" />
                </span>
              </form>
            </span>
          </div>
        </xsl:if>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.pagination.createResultPages">
    <xsl:param name="resultInfo" />
    <xsl:param name="startParam" select="'start'" />
    <xsl:param name="pageEnd" />
    <xsl:param name="i" />

    <xsl:if test="$i &lt;= $resultInfo/pages and $i &lt;= $pageEnd">
      <xsl:choose>
        <xsl:when test="$i = $resultInfo/page + 1">
          <li class="active"><span><xsl:value-of select="$i" /></span></li>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="jp.pagination.entry">
            <xsl:with-param name="startParam" select="$startParam" />
            <xsl:with-param name="text" select="$i" />
            <xsl:with-param name="value" select="($i - 1) * $resultInfo/rows" />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:call-template name="jp.pagination.createResultPages">
        <xsl:with-param name="resultInfo" select="$resultInfo" />
        <xsl:with-param name="startParam" select="$startParam" />
        <xsl:with-param name="pageEnd" select="$pageEnd" />
        <xsl:with-param name="i" select="$i+1" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.pagination.entry">
    <xsl:param name="startParam" select="'start'" />
    <xsl:param name="value" />
    <xsl:param name="text" />
    <li>
      <a>
        <xsl:attribute name="href">
          <xsl:call-template name="UrlSetParam">
            <xsl:with-param name="url" select="$RequestURL" />
            <xsl:with-param name="par" select="$startParam" />
            <xsl:with-param name="value" select="$value" />
          </xsl:call-template>
        </xsl:attribute>
        <xsl:value-of select="$text" />
      </a>
    </li>
  </xsl:template>

</xsl:stylesheet>