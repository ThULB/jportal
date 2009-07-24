<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2007-03-26 06:15:29 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
> 

<xsl:output method="xml" encoding="UTF-8"/>

<xsl:variable name="newline">
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:attribute-set name="tag">
  <xsl:attribute name="class"><xsl:value-of select="./@class" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="subtag">
  <xsl:attribute name="sourcepath"><xsl:value-of select="/mycorederivate/@ID"/></xsl:attribute>
   <xsl:attribute name="maindoc"><xsl:value-of select="@maindoc"/></xsl:attribute>
   <xsl:attribute name="ifsid"><xsl:value-of select="@ifsid"/></xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="tagisodate">
  <xsl:attribute name="class"><xsl:value-of select="'MCRMetaISO8601Date'" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
  <xsl:attribute name="notinherit"><xsl:value-of select="./@notinherit" /></xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="subtagisodate">
  <xsl:attribute name="type"><xsl:value-of select="./@type" /></xsl:attribute>
  <xsl:attribute name="inherited"><xsl:value-of select="./@inherited" /></xsl:attribute>
</xsl:attribute-set>

<xsl:template match="/">
  <mycorederivate>
    <xsl:copy-of select="mycorederivate/@ID"/>
    <xsl:copy-of select="mycorederivate/@label"/>
    <xsl:choose>
      <xsl:when test="mycoreobject/@version">
        <xsl:copy-of select="mycoreobject/@version"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="version">1.3</xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:copy-of select="mycorederivate/@xsi:noNamespaceSchemaLocation"/>
    <xsl:value-of select="$newline"/>
    <derivate>
      <xsl:value-of select="$newline"/>
<!--      <xsl:copy-of select="mycorederivate/derivate/linkmetas"/> -->
      <xsl:apply-templates select="mycorederivate/derivate/linkmetas"/>
      <xsl:value-of select="$newline"/>
      <xsl:for-each select="mycorederivate/derivate/internals">
<!--        <xsl:copy use-attribute-sets="tag">-->
          <xsl:copy> 
  <xsl:attribute name="class"><xsl:value-of select="./@class" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
          <xsl:for-each select="internal">
<!--            <xsl:copy use-attribute-sets="subtag" /> -->
            <xsl:copy>
  <xsl:attribute name="sourcepath"><xsl:value-of select="/mycorederivate/@ID"/></xsl:attribute>
   <xsl:attribute name="maindoc"><xsl:value-of select="@maindoc"/></xsl:attribute>
   <xsl:attribute name="ifsid"><xsl:value-of select="@ifsid"/></xsl:attribute>
            </xsl:copy>
          </xsl:for-each>
        </xsl:copy>
      </xsl:for-each>
      <xsl:value-of select="$newline"/>
    </derivate>
    <xsl:value-of select="$newline"/>
    <service>
      <xsl:value-of select="$newline"/>
      <xsl:for-each select="mycorederivate/service/servdates">
        <xsl:choose>
          <xsl:when test="@class = 'MCRMetaDate'">
            <xsl:if test="./*/@inherited = '0'">
<!--              <xsl:copy use-attribute-sets="tagisodate"> -->
              <xsl:copy>
  <xsl:attribute name="class"><xsl:value-of select="'MCRMetaISO8601Date'" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
  <xsl:attribute name="notinherit"><xsl:value-of select="./@notinherit" /></xsl:attribute>
                <xsl:for-each select="*" >
                  <xsl:if test="@inherited = '0'">
<!--                    <xsl:copy use-attribute-sets="subtagisodate" >-->
                    <xsl:copy>
  <xsl:attribute name="type"><xsl:value-of select="./@type" /></xsl:attribute>
  <xsl:attribute name="inherited"><xsl:value-of select="./@inherited" /></xsl:attribute>
                      <xsl:for-each select="." >
                        <xsl:value-of select="."/>
                      </xsl:for-each>
                    </xsl:copy>
                    <xsl:value-of select="$newline"/>
                  </xsl:if>
                </xsl:for-each>
              </xsl:copy>
              <xsl:value-of select="$newline"/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="./*/@inherited = '0'">
<!--              <xsl:copy use-attribute-sets="tag">-->
              <xsl:copy>
  <xsl:attribute name="class"><xsl:value-of select="./@class" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
                <xsl:for-each select="*" >
                  <xsl:if test="@inherited = '0'">
                    <xsl:copy-of select="."/>
                    <xsl:value-of select="$newline"/>
                  </xsl:if>
                </xsl:for-each>
              </xsl:copy>
              <xsl:value-of select="$newline"/>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:value-of select="$newline"/>
      <servacls class="MCRMetaAccessRule" heritable="false" notinherit="false">
        <!-- READ rule for web application -->
        <xsl:value-of select="$newline"/>
        <servacl inherited="0" permission="read">
          <condition format="xml">
            <boolean operator="true"/>
          </condition>
        </servacl>
        <!-- WRITEDB rule for web application -->
        <xsl:value-of select="$newline"/>
        <servacl inherited="0" permission="writedb">
          <condition format="xml">
            <boolean operator="true"/>
          </condition>
        </servacl>
        <!-- DELETEDB rule for web application -->
        <xsl:value-of select="$newline"/>
        <servacl inherited="0" permission="deletedb">
          <condition format="xml">
            <boolean operator="true"/>
          </condition>
        </servacl>
        <xsl:value-of select="$newline"/>
      </servacls>
      <xsl:value-of select="$newline"/>
    </service>
    <xsl:value-of select="$newline"/>
  </mycorederivate>
</xsl:template>

<xsl:template match='@*|node()'>
  <xsl:copy>
    <xsl:apply-templates select='@*|node()'/>
  </xsl:copy>
</xsl:template>

<xsl:template match='@parasearch | @textsearch' />

</xsl:stylesheet>

