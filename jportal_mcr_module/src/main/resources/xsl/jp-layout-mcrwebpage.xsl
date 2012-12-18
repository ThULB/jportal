<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mcr="http://www.mycore.org/" xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="xsi mcr encoder xalan">

  <xsl:template match="printLatestArticles">
    <xsl:variable name="searchTerm" select="concat('objectType:', @objectType)" />
    <xsl:variable name="queryURI" select="concat('solr:q=',$searchTerm,'&amp;sort=',@sortField, '%20desc&amp;rows=',@maxResults)" />
    <ul>
      <xsl:apply-templates mode="searchResultsList" select="document($queryURI)/response/result/doc" />
    </ul>
  </xsl:template>

  <xsl:template match="printObjectEditing">
    <xsl:variable name="objectEditingHTML">
      <editing>
        <xsl:call-template name="objectEditing">
          <xsl:with-param name="id" select="/mycoreobject/@ID" />
          <xsl:with-param name="dataModel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
        </xsl:call-template>
      </editing>
    </xsl:variable>
    <xsl:variable name="objectEditing" select="xalan:nodeset($objectEditingHTML)/editing" />
    <xsl:if test="$objectEditing/menu[@id='jp-object-editing']//li/a">
      <div class="jp-layout-index-objectEditing">
        <h1>Erstellen</h1>
        <xsl:copy-of select="$objectEditing/menu[@id='jp-object-editing' and li]" />
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="searchResultsList" match="doc">
    <li>
      <div class="wrapper">
        <p class="title">
          <a href="{$WebApplicationBaseURL}receive/{str[@name='id']}">
            <xsl:call-template name="shortenString">
              <xsl:with-param name="string" select="str[@name='maintitle']" />
              <xsl:with-param name="length" select="250" />
            </xsl:call-template>
          </a>
        </p>
        <div class="journal">
          Erschienen in
          <a href="{$WebApplicationBaseURL}receive/{str[@name='journalID']}">
            <xsl:value-of select="str[@name='journalTitle']" />
          </a>
        </div>
      </div>
    </li>
  </xsl:template>
  
</xsl:stylesheet>
