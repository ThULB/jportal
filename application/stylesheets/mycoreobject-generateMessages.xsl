<?xml version="1.0" encoding="iso-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.2 $ $Date: 2006/11/15 16:22:45 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- 
    This stylesheet can be used to generate templates for parts of
    messages_*.properties. These parts are those beginning with
    "metaData.<ObjectType>.*"
  -->
  <xsl:output encoding="iso-8859-1" media-type="text/plain" method="text" />
  <xsl:param name="propPrefix" select="'metaData.'" />
  <xsl:variable name="objectType">
    <xsl:apply-templates select="/*" mode="getObjectType" />
  </xsl:variable>

  <xsl:template match="/mycoreobject">
    <xsl:value-of select="'# [singular] and [plural] entries are used if objects&#10;'" />
    <xsl:value-of select="concat('# of type ',$objectType,' appear e.g. as children&#10;')" />
    <xsl:value-of select="concat($propPrefix,$objectType,'.[singular] = ',$objectType,'&#10;')" />
    <xsl:value-of select="concat($propPrefix,$objectType,'.[plural] = ',$objectType,'s&#10;')" />
    <xsl:value-of select="concat($propPrefix,$objectType,'.[derivates] = ',$objectType,' full text&#10;')" />
    <!--
      For messages_*.properties we must filter out
      1.) tags with the same parent and same type attribute
      2.) tags with the same parent an no type attribut
      
      We define a helper variable to perform this filtering now.
    -->
    <xsl:variable name="unique-list"
      select="./metadata/*/*[not(local-name()=local-name(following-sibling::*) and ((@type=following-sibling::*/@type) or (not(@type) and (following-sibling::*[not(@type)]))))]" />
    <xsl:for-each select="$unique-list">
      <xsl:sort select="local-name()" />
      <xsl:sort select="@type" />
      <!-- Here come the actual filtering of doubles -->
      <xsl:apply-templates select="." mode="messageProperty" />
      <xsl:value-of select="'&#10;'" /><!-- New Line -->
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="*" mode="messageProperty">
    <!-- generates single message_*.properties line -->
    <xsl:variable name="suffix">
      <xsl:if test="@type">
        <xsl:value-of select="concat('.',translate(@type,' ','_'))" />
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="value">
      <xsl:if test="@type">
        <xsl:value-of select="concat(@type,' ')" />
      </xsl:if>
      <xsl:value-of select="local-name()" />
    </xsl:variable>
    <xsl:value-of select="concat($propPrefix,$objectType,'.',local-name(),$suffix,' = ',$value)" />
  </xsl:template>

  <xsl:template match="/*" mode="getObjectType">
    <xsl:choose>
      <xsl:when test="@ID">
        <xsl:value-of select="substring-before(substring-after(@ID,'_'),'_')" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="local-name()" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>