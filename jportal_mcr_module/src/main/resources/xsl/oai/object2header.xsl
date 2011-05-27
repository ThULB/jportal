<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" />

  <xsl:param name="identifier" />

  <xsl:template match="/">
    <header>
      <xsl:apply-templates select="mycoreobject | mycorederivate" mode="header" />
    </header>
  </xsl:template>

  <xsl:template match="mycoreobject | mycorederivate" mode="header">
    <xsl:apply-templates select="@ID" />
    <xsl:apply-templates select="service/servdates/servdate[@type='modifydate']" />
    <xsl:apply-templates select="metadata/def.component/component" />
  </xsl:template>

  <xsl:template match="@ID">
    <identifier>
      <xsl:value-of select="concat('oai:',$identifier,':',.)" />
    </identifier>
  </xsl:template>

  <xsl:template match="component">
    <setSpec>
      <xsl:value-of select="concat(@classid,':',@categid)" />
    </setSpec>
  </xsl:template>

  <xsl:template match="servdate">
    <datestamp>
      <xsl:value-of select="substring(text(),1,10)" />
    </datestamp>
  </xsl:template>

  <xsl:template match="*" />

</xsl:stylesheet>
