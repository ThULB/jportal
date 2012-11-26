<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan">
  <!-- search query param -->
  <xsl:param name="qt" select="'*'" />
  <xsl:param name="searchjournalID" select="''" />
  <xsl:param name="start" select="'0'" />
  <xsl:param name="rows" select="'10'" />
  
  <!-- subselect param -->
  <xsl:param name="subselect.type" select="''" />
  <xsl:param name="subselect.session" />
  <xsl:param name="subselect.varpath" />
  <xsl:param name="subselect.webpage" />
  
  <xsl:template match="jpsearch" mode="subselect.form">
    <xsl:variable name="queryXML">
      <query>
        <queryTerm value="{$qt}" />
        <queryTermField name="+objectType" value="{$subselect.type}" />
        <param name="qf" value="titles^10 heading^10 dates^5 allMeta^1" />
        <param name="rows" value="{$rows}" />
        <param name="start" value="{$start}" />
        <param name="defType" value="edismax" />
      </query>
    </xsl:variable>
    <xsl:variable name="query">
      <xsl:apply-templates mode="createSolrQuery" select="xalan:nodeset($queryXML)/query" />
    </xsl:variable>

    <xsl:variable name="subselectXML">
      <subselect>
        <param name="subselect.type" value="$subselect.type"/>
        <param name="subselect.session" value="$subselect.session"/>
        <param name="subselect.varpath" value="$subselect.varpath"/>
        <param name="subselect.webpage" value="$subselect.webpage"/>
      </subselect>
    </xsl:variable>
    
    <xsl:variable name="searchResults">
      <solrSearch>
        <xsl:copy-of select="$subselectXML"/>
        <xsl:copy-of select="$queryXML"/>
        <xsl:copy-of select="document($query)"/>
      </solrSearch>
    </xsl:variable>

    <xsl:variable name="searchResultView" select="document('../xml/views/searchResultView.xml')/view" />
    <!-- 
    <xsl:apply-templates mode="searchResults.subselect" select="document($query)/response/result/doc" />
     -->
    <xsl:apply-templates mode="renderView" select="$searchResultView/component[@name='resultList']/*">
      <xsl:with-param name="data" select="document($query)"/>
    </xsl:apply-templates>
    
  </xsl:template>
  
  <xsl:template mode="renderView" match="@*|node()">
    <xsl:param name="data"/>
    <xsl:copy>
        <xsl:apply-templates mode="renderView" select="@*|node()">
          <xsl:with-param name="data" select="$data"/>
        </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template mode="renderView" match="getData[@id='search.numFound']">
    <xsl:param name="data"/>
    <xsl:value-of select="$data/response/result/@numFound"/>    
  </xsl:template>
  
  <xsl:template mode="renderView" match="getData[@id='search.query']">
    <xsl:param name="data"/>
    <xsl:value-of select="substring-before($data/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='q'],' +')"/>    
  </xsl:template>
  
  <xsl:template mode="searchResults.subselect" match="doc">
      <li>
        <xsl:value-of select="str[@name='id']"/>
      </li>
  </xsl:template>

  <xsl:template match="jpsubselect">
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="$subselect.type = 'person'">
          <xsl:value-of select="'Person'" />
        </xsl:when>
        <xsl:when test="$subselect.type = 'institution'">
          <xsl:value-of select="'Institution'" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:value-of select="concat('Bitte benutzen Sie das Suchfeld, um eine ', $type, ' auszuwählen.')"></xsl:value-of>
    <!-- http://localhost:18101/servlets/XMLEditor?
    _action=end.subselect
    &subselect.session=9xvpggvoyi
    &subselect.varpath=/mycoreobject/metadata/participants/participant
    &subselect.webpage=editor_form_commit-jpjournal.xml%3Ftype%3Djpjournal%26step%3Dcommit%26cancelUrl%3Dhttp%253A%252F%252Flocalhost%253A18101%252Freceive%252Fjportal_jpjournal_00000761%26sourceUri%3DxslStyle%253Amycoreobject-editor%253Amcrobject%253Ajportal_jpjournal_00000761%26mcrid%3Djportal_jpjournal_00000761%26
    &mode=prefix
    &_var_@xlink:href=jportal_person_00061171
    &_var_@xlink:title=Gleichen-Ru%C3%9Fwurm,%20Alexander%20von%20(1865-11-06%20-%201947-10-25,%20Schriftsteller;%20Herausgeber;%20%C3%9Cbersetzer;%20Kulturphilosoph)%20%20%20%20%20%20%20%20%20%20%20%20%20
    &_var_@field=participants_art
    &_var_@operator==
    &_var_@value=jportal_person_00061171 -->
  </xsl:template>
</xsl:stylesheet>