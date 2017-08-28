<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
                version="1.0">
  <xsl:output method="xml" encoding="UTF-8" media-type="text/xml" />

  <xsl:include href="config.xsl" />

  <xsl:template match="text()"/>
  <xsl:template match="/solr-document-container">
    <add>
      <xsl:apply-templates select="source" />
    </add>
  </xsl:template>

  <xsl:template match="source">
    <doc>
      <xsl:variable name="obj">
        <obj name="type">
          <xsl:value-of select="substring-before(substring-after(*/@ID, '_'), '_')" />
        </obj>
        <obj name="project">
          <xsl:value-of select="substring-before(*/@ID, '_')" />
        </obj>
        <xsl:if test="mycoreobject|mycorederivate">
          <obj name="id">
            <xsl:value-of select="*/@ID" />
          </obj>
        </xsl:if>
        <xsl:if test="mycoreobject">
          <obj name="derivate.count">
            <xsl:value-of select="count(mycoreobject/structure/derobjects/derobject)" />
          </obj>
        </xsl:if>
      </xsl:variable>

      <xsl:apply-templates mode="objValues" select="xalan:nodeset($obj)/obj" />

      <xsl:apply-templates />
<!--       <xsl:apply-templates mode="entity" /> -->
    </doc>
  </xsl:template>

  <xsl:template mode="objValues" match="obj[@name='id']">
    <!-- mycoreobject/mycorederivate id is key in solr -->
    <field name="id">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template mode="objValues" match="obj[@name='derivate.count']">
    <field name="derivate.count">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <!-- select all leaves as basis for solr document fields -->
  <xsl:template match="*[count(child::*) = 0]">

    <xsl:variable name="element" select="local-name(.)" />

    <xsl:if test="string-length(.) &gt; 0">
      <field name="{$element}">
        <xsl:value-of select="." />
      </field>

      <xsl:for-each select="./@*">
        <!-- <elementName>.<attribute.name>.<attrVal> -->
        <field name="{concat($element, '.', local-name(.), '.', .)}">
          <xsl:value-of select="../." />
        </field>
      </xsl:for-each>

    </xsl:if>

    <xsl:for-each select="./@*">
      <field name="{concat($element, '.', local-name(.))}">
        <xsl:value-of select="." />
      </field>
    </xsl:for-each>

  </xsl:template>

  <!-- matches the fileset element of mycorederivate -->
  <xsl:template match="fileset">
    <field name="urn">
      <xsl:value-of select="@urn" />
    </field>

    <xsl:for-each select="file/urn">
      <field name="urn">
        <xsl:value-of select="." />
      </field>
    </xsl:for-each>

  </xsl:template>

  <xsl:template match="/file">
    <!-- file id is key in solr -->
    <field name="id">
      <xsl:value-of select="@ID" />
    </field>

    <field name="object_type">
      <xsl:value-of select="'file'" />
    </field>

    <field name="owner">
      <xsl:value-of select="@owner" />
    </field>

    <field name="file_name">
      <xsl:value-of select="@name" />
    </field>

    <!-- select all leaves as basis for solr document fields -->
    <xsl:for-each select="*[count(child::*) = 0]">

      <xsl:if test="string-length(.) &gt; 0">
        <field name="{local-name(.)}">
          <xsl:value-of select="." />
        </field>
      </xsl:if>

    </xsl:for-each>
  </xsl:template>

  <xsl:template match="/solr-document-container/source/user">
    <xsl:copy-of select="field" />
  </xsl:template>

  <!-- support raw solr documents -->
  <xsl:template match="/doc">
    <xsl:copy-of select="field" />
  </xsl:template>

</xsl:stylesheet>