<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xalan="http://xml.apache.org/xalan" xmlns:metsGen="xalan://fsu.jportal.mets.DFGOAIMetsXalan">

  <xsl:include href="object2record.xsl" />

  <xsl:template match="mycoreobject" mode="metadata">
      <xsl:if test="structure/derobjects">
        <xsl:copy-of select="metsGen:getMets(@ID, structure/derobjects/derobject[1]/@xlink:href)" />
      </xsl:if>
  </xsl:template>

</xsl:stylesheet>
