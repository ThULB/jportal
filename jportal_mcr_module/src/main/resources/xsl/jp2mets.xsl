<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns="http://www.openarchives.org/OAI/2.0/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

  <xsl:include href="object2record.xsl" />
  <xsl:include href="mycoreobject-metsmods.xsl" />

  <xsl:template match="mycoreobject" mode="metadata">
    <xsl:apply-templates select="." />
  </xsl:template>

</xsl:stylesheet>
