<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mcr="xalan://org.mycore.common.xml.MCRXMLFunctions">

  <xsl:include href="oai/object2header.xsl" />

  <xsl:param name="WebApplicationBaseURL" />

  <xsl:template match="/">
    <record>
      <header>
        <xsl:apply-templates select="mycoreobject | mycorederivate" mode="header" />
      </header>

      <xsl:if test="mycoreobject/@ID">
        <xsl:if test="mcr:exists(mycoreobject/@ID)">
          <metadata>
            <xsl:apply-templates select="mycoreobject" mode="metadata" />
          </metadata>
        </xsl:if>
      </xsl:if>

      <xsl:if test="mycorederivate/@ID">
        <xsl:if test="mcr:exists(mycorederivate/@ID)">
          <metadata>
            <xsl:apply-templates select="mycorederivate" mode="metadata" />
          </metadata>
        </xsl:if>
      </xsl:if>


    </record>
  </xsl:template>

</xsl:stylesheet>
