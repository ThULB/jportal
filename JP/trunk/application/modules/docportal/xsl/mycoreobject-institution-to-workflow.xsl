<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2007-09-07 12:22:09 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation">
<xsl:output method="xml" encoding="UTF-8" />

<xsl:template match="/mycoreobject">
 <item>
  <xsl:attribute name="ID">
   <xsl:value-of select="@ID" />
  </xsl:attribute>
  <!-- Name -->
  <label>
  <xsl:if test="metadata/names/name">
   <xsl:value-of select="metadata/names/name/fullname" />
  </xsl:if>
  </label>
  <!-- Date -->
  <xsl:if test="metadata/addresses/address">
   <xsl:for-each select="metadata/addresses/address">
    <data>
     <xsl:value-of select="city" />
    </data>
   </xsl:for-each>
  </xsl:if>
  <!-- Create Date -->
  <xsl:if test="service/servdates/servdate">
   <data>
   <xsl:for-each select="service/servdates/servdate">
    <xsl:if test="@type = 'modifydate'">
     <xsl:value-of select="i18n:translate('component.swf.converter.modifydate')" /> <xsl:value-of select="text()|*" />
    </xsl:if>
   </xsl:for-each>
   </data>
  </xsl:if>
 </item>
</xsl:template>

</xsl:stylesheet>

