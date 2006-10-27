<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2005/03/30 13:38:27 $ -->
<!-- ============================================== --> 

<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:java="http://xml.apache.org/xslt/java"
  extension-element-prefixes="java"
  >

<xsl:output 
  method="xml" 
  encoding="UTF-8"
  />

<xsl:template match="/">
  <lucene ID="{./mycoreobject/@ID}">
    <xsl:for-each select="mycoreobject/metadata/*">
      <xsl:for-each select="./*">
        <xsl:if test="../@class='MCRMetaLangText' and ../@parasearch='true' and ../@textsearch='true'">
          <xsl:call-template name="handletext">
            <xsl:with-param name="var"     select="."     />
          </xsl:call-template>
        </xsl:if> 
 
<!--        <xsl:if test="../@class='MCRMetaClassification' and ../@parasearch='true' and ../@textsearch='false'"> -->
        <xsl:if test="../@class='MCRMetaClassification' and ../@parasearch='true'">
          <xsl:call-template name="handleclassification">
            <xsl:with-param name="var"     select="."     />
          </xsl:call-template>
        </xsl:if>  

        <xsl:if test="../@class='MCRMetaDate' and ../@parasearch='true' and ../@textsearch='false'">
          <xsl:call-template name="handledate">
            <xsl:with-param name="var"     select="."     />
          </xsl:call-template>
        </xsl:if> 
 
        <xsl:if test="../@class='MCRMetaLinkID' and ../@parasearch='true' and ../@textsearch='false'">
          <xsl:call-template name="handlelink">
            <xsl:with-param name="var"     select="."     />
          </xsl:call-template>
        </xsl:if> 
 
        <xsl:if test="(../@class='MCRMetaPersonName' or ../@class='MCRMetaInstitutionName') and ../@textsearch='true'">
          <xsl:call-template name="handleperson">
            <xsl:with-param name="var"     select="."     />
          </xsl:call-template>
        </xsl:if> 

      </xsl:for-each> 
    </xsl:for-each> 
  </lucene>
</xsl:template>

<xsl:template name="handletext">
  <xsl:param name="var" />

  <xsl:for-each select="$var">
    <field name="{name(.)}" type="UnStored" xml:lang="{@xml:lang}">
      <xsl:value-of select="." />
    </field>
    <xsl:if test="name(.) != 'keyword'">
      <field name="keywords" type="UnStored" xml:lang="{@xml:lang}">
        <xsl:value-of select="." />
      </field>
    </xsl:if>
  </xsl:for-each> 

</xsl:template>

<xsl:template name="handleclassification">
  <xsl:param name="var" />

  <xsl:for-each select="$var">
    <field name="{name(.)}classid" type="Keyword">
      <xsl:value-of select="./@classid" />
    </field>
    <field name="{name(.)}categid" type="Keyword">
      <xsl:value-of select="./@categid" />
    </field>
  </xsl:for-each> 

</xsl:template>

<xsl:template name="handledate">
  <xsl:param name="var" />

  <xsl:for-each select="$var">
    <field name="{@type}" type="date" xml:lang="{@xml:lang}">
      <xsl:value-of select="." />
    </field>
  </xsl:for-each> 

</xsl:template>

<xsl:template name="handlelink">
  <xsl:param name="var" />

  <xsl:for-each select="$var">
    <field name="{name(.)}" type="Keyword">
      <xsl:value-of select="./@xlink:href" />
    </field>
  </xsl:for-each> 

</xsl:template>

<xsl:template name="handleperson">
  <xsl:param name="var" />

  <xsl:for-each select="$var/*">
    <field name="{name(.)}" type="UnStored" xml:lang="{@xml:lang}">
      <xsl:value-of select="." />
    </field>
<!--
    <xsl:if test="name(.) != 'keyword'">
      <field name="keywords" type="UnStored" xml:lang="{@xml:lang}">
        <xsl:value-of select="." />
      </field>
    </xsl:if>
-->
  </xsl:for-each> 

</xsl:template>


</xsl:stylesheet>

