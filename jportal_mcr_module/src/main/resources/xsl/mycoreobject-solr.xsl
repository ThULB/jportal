<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:output method="xml" encoding="UTF-8" media-type="text/xml" />

  <xsl:include href="xslInclude:solr" />

  <xsl:template match="/">
    <add>
      <xsl:apply-templates mode="doc" select="//mycoreobject|//mycorederivate" />    
    </add>
  </xsl:template>

  <xsl:template mode="doc" match="mycoreobject|mycorederivate">
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
    <xsl:variable name="objectType" select="substring-before(substring-after(@ID, '_'), '_')" />
    <field name="id"><xsl:value-of select="@ID" /></field>
    <field name="objectType"><xsl:value-of select="$objectType" /></field>
    <field name="objectProject"><xsl:value-of select="substring-before(@ID, '_')" /></field>
    <xsl:if test="name() = 'mycoreobject'">
      <field name="derivateCount"><xsl:value-of select="count(structure/derobjects/derobject)" /></field>
    </xsl:if>
    <xsl:if test="name() = 'mycoreobject' and $objectType != 'person' and $objectType != 'jpinst'">
      <field name="childrenCount"><xsl:value-of select="count(structure/children/child)" /></field>
    </xsl:if>
    <xsl:if test="name() = 'mycorederivate'">
      <field name="display"><xsl:value-of select="derivate/@display" /></field>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="derivate" match="linkmetas/linkmeta">
    <field name="derivateOwner"><xsl:value-of select="@xlink:href" /></field>
  </xsl:template>

  <xsl:template mode="derivate" match="internals/internal">
    <field name="maindoc"><xsl:value-of select="@maindoc" /></field>
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