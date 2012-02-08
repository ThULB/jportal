<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/">

  <xsl:include href="object2record.xsl" />
  <xsl:include href="mycoreobject-mods-pure.xsl" />

  <xsl:template match="mycoreobject" mode="metadata">
    <xsl:apply-templates select="." />
  </xsl:template>

</xsl:stylesheet>