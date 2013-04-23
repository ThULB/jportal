<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:output method="xml" encoding="UTF-8" media-type="text/xml" />

  <xsl:include href="xslInclude:solr" />

  <xsl:template match="/">
    <add>
      <xsl:apply-templates mode="doc" select="//mycoreobject|//mycorederivate|//file[not(parent::fileset)]" />    
    </add>
  </xsl:template>

  <xsl:template mode="doc" match="mycoreobject|mycorederivate|file">
    <doc>
      <xsl:apply-templates mode="base" select="." />
      <xsl:apply-templates mode="structure" select="structure" />
      <xsl:apply-templates mode="metadata" select="metadata" />
      <xsl:apply-templates mode="derivate" select="derivate" />
      <xsl:apply-templates mode="service" select="service" />
    </doc>
  </xsl:template>

  <xsl:template match="text()|@*" mode="structure" />
  <xsl:template match="text()|@*" mode="metadata" />
  <xsl:template match="text()|@*" mode="derivate" />
  <xsl:template match="text()|@*" mode="service" />

  <!-- MCRObject MCRDerivate -->
  <xsl:template mode="base" match="mycoreobject|mycorederivate">
    <field name="id"><xsl:value-of select="@ID" /></field>
    <field name="objectType"><xsl:value-of select="substring-before(substring-after(@ID, '_'), '_')" /></field>
    <field name="objectProject"><xsl:value-of select="substring-before(@ID, '_')" /></field>
    <xsl:if test="name() = 'mycoreobject'">
      <field name="derivateCount"><xsl:value-of select="count(structure/derobjects/derobject)" /></field>
    </xsl:if>
    <xsl:if test="name() = 'mycorederivate'">
      <field name="display"><xsl:value-of select="derivate/@display" /></field>
    </xsl:if>
  </xsl:template>

  <!-- MCRDerivate -->
  <xsl:template mode="derivate" match="fileset">
    <field name="urn"><xsl:value-of select="@urn" /></field>
    <xsl:for-each select="file/urn">
      <field name="urn"><xsl:value-of select="." /></field>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="derivate" match="linkmetas/linkmeta">
    <field name="derivateOwner"><xsl:value-of select="@xlink:href" /></field>
  </xsl:template>

  <xsl:template mode="derivate" match="internals/internal">
    <field name="maindoc"><xsl:value-of select="@maindoc" /></field>
  </xsl:template>

  <!-- MCRFile -->
  <xsl:template mode="base" match="file">
    <field name="id"><xsl:value-of select="@id" /></field>
    <field name="objectType"><xsl:value-of select="'data_file'" /></field>
    <field name="objectProject"><xsl:value-of select="substring-before(@owner, '_')" /></field>
    <field name="returnId"><xsl:value-of select="@returnId" /></field>
    <field name="DerivateID"><xsl:value-of select="@owner" /></field>
    <field name="fileName"><xsl:value-of select="@name" /></field>
    <field name="filePath"><xsl:value-of select="@path" /></field>
    <xsl:if test="@urn">
      <field name="urn"><xsl:value-of select="@urn" /></field>
    </xsl:if>
    <field name="modified"><xsl:value-of select="@modified" /></field>
    <field name="stream_size"><xsl:value-of select="@size" /></field>
    <field name="extension"><xsl:value-of select="@extension" /></field>
    <field name="stream_content_type"><xsl:value-of select="@contentTypeID" /></field>
    <!-- select all leaves as basis for solr document fields -->
    <xsl:for-each select="*[count(child::*) = 0]">
      <xsl:if test="string-length(.) &gt; 0">
        <field name="{local-name(.)}"><xsl:value-of select="." /></field>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- parent -->
  <xsl:template mode="structure" match="parents/parent">
    <field name="parent"><xsl:value-of select="@xlink:href" /></field>
  </xsl:template>

  <!-- modify date -->
  <xsl:template mode="service" match="servdates/servdate[@type='modifydate']">
    <field name="modified"><xsl:value-of select="." /></field>
  </xsl:template>

  <!-- create date -->
  <xsl:template mode="service" match="servdates/servdate[@type='createdate']">
    <field name="created"><xsl:value-of select="." /></field>
  </xsl:template>

</xsl:stylesheet>