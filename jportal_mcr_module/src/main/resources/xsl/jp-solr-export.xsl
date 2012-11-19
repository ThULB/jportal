<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:template match="metadata" mode="user-application" priority="1">
    <xsl:apply-templates select="*" mode="user-application" />
  </xsl:template>

  <!-- Content Classifications -->
  <xsl:template match="*[contains(name(), 'contentClassis')]" mode="user-application" priority="1">
    <xsl:apply-templates select="*" mode="content-classification" />
  </xsl:template>

  <xsl:template match="*[position()=1]" mode="content-classification">
    <field name="{name()}">
      <xsl:value-of select="@categid" />
    </field>
  </xsl:template>

</xsl:stylesheet>