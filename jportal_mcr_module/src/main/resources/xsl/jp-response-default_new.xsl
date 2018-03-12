<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/"
                xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="xalan">

  <xsl:variable name="facetSettings"
                select="document('../xml/layoutDefaultSettings.xml')/layoutSettings/editor/jpjournal/bind/row"/>

  <xsl:variable name="facetsWithParent">
    <xsl:apply-templates mode="addFacetParent"
                         select="/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst"/>
  </xsl:variable>

  <xsl:variable name="usedFacet"
                select="/response/lst[@name='responseHeader']/lst[@name='params']/arr[@name='fq']/str"/>

  <xsl:template mode="addFacetParent" match="lst">
    <xsl:copy>
      <xsl:apply-templates mode="addFacetParent" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="addFacetParent" match="int">
    <xsl:variable name="groupName" select="../@name"/>
    <xsl:variable name="classID" select="substring-before(@name, ':')"/>
    <xsl:copy>
      <xsl:if test="$facetSettings[@xpath=$groupName and @class=$classID and @on]">
        <xsl:attribute name="parent">
          <xsl:value-of select="$facetSettings[@xpath=$groupName and @class=$classID]/@on"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates mode="addFacetParent" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="addFacetParent" match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates mode="addFacetParent" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:variable name="facetTree">
    <tree>
      <xsl:apply-templates mode="facetTree" select="xalan:nodeset($facetsWithParent)/lst"/>
    </tree>
  </xsl:variable>

  <xsl:template mode="facetTree" match="lst">
    <div group="{@name}">
      <!-- select int nodes where parent attribute is not present-->
      <xsl:apply-templates mode="facetTree" select="int[not(@parent)]"/>
    </div>
  </xsl:template>

  <xsl:template mode="facetTree" match="int">
    <xsl:variable name="group" select=".."/>
    <xsl:variable name="groupName" select="$group/@name"/>
    <xsl:variable name="id" select="@name"/>

    <xsl:variable name="facetCheckboxClass">
      <xsl:choose>
        <xsl:when test="$usedFacet[starts-with(.,$groupName) and contains(., $id)]">
          <xsl:value-of select="'jp-facet-checkbox checked'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'jp-facet-checkbox'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="facetHrefLink">
      <xsl:choose>
        <xsl:when test="$usedFacet[starts-with(.,$groupName) and contains(., $id)]">
          <xsl:apply-templates mode="facetHrefLink" select="$usedFacet[not(contains(., $id))]/text()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates mode="facetHrefLink" select="$usedFacet|$id"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <div class="jp-journalList-facet-row" data-id="{$id}" data-parent="{@parent}">
      <a href="{$facetHrefLink}">
        <div class="jp-journalList-facet-entry">
          <div class="jp-journalList-facet-linkContainer">
            <!--<input class="jp-journalList-facet-checkbox" type="checkbox">-->
            <!--<xsl:if test="$usedFacet[starts-with(.,$group/@name) and contains(., $id)]">-->
            <!--<xsl:attribute name="checked">-->
            <!--<xsl:value-of select="'true'"/>-->
            <!--</xsl:attribute>-->
            <!--</xsl:if>-->
            <!--</input>-->
            <label class="{$facetCheckboxClass}"/>
            <div class="jp-journalList-facet-label">
              <xsl:choose>
                <xsl:when test="$groupName = 'journalType'">
                  <xsl:value-of select="$id"/>
                </xsl:when>
                <xsl:when test="$facetSettings/@translate = 'true'">
                  <xsl:value-of select="concat('jp.metadata.facet.', $groupName, '.', $id)"/>
                </xsl:when>
                <xsl:when test="$facetSettings/@mcrid = 'true'">
                  <xsl:value-of select="concat('mcrobject:', $id)"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$id"/>
                </xsl:otherwise>
              </xsl:choose>
            </div>
          </div>
          <div class="jp-journalList-facet-count">
            <xsl:value-of select="."/>
          </div>
        </div>
      </a>
      <xsl:if test="$group/int[@parent=$id]">
        <xsl:apply-templates mode="facetTree" select="$group/int[@parent=$id]"/>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template match="/response">
    <html>
      <facets>
        <xsl:copy-of select="$facetsWithParent"/>
      </facets>
      <!--<xsl:apply-templates mode="foo" select="xalan:nodeset($facetPath)"/>-->
      <!--<settings>-->
      <!--<xsl:copy-of select="$facetSettings"/>-->
      <!--</settings>-->
      <!--<tree>-->
      <xsl:copy-of select="$facetTree"/>
      <!--</tree>-->
      <!--<used>-->
      <!--<xsl:copy-of select="$usedFacets"/>-->
      <!--</used>-->
    </html>
  </xsl:template>
  
  <xsl:template mode="facetHrefLink" match="text()">
    <xsl:value-of select="concat('&amp;fq=',.)"/>
  </xsl:template>
</xsl:stylesheet>