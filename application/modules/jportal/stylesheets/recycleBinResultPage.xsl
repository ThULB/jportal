<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/" >

    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:include href="objecttypes.xsl" />

    <xsl:variable name="PageTitle" select="'Papierkorb Ergebnisseite'" />

    <!-- =================================================================================================== -->
    <!-- =========== result page ============ -->
    <!-- =================================================================================================== -->
    <xsl:template match="recycleBinResultPage">

      <xsl:if test="count(deleted/entry) != 0">
        <p>
          <strong><xsl:value-of select="'Gelöschte Objekte:'" /></strong>
          <br />
          <xsl:for-each select="deleted/entry">
            <xsl:value-of select="@id" />
            <br/>
          </xsl:for-each>
        </p>
      </xsl:if>
      <xsl:if test="count(linked/entry) != 0">
        <p>
          <span style="font-weight: bolder; color:#992222;">
            <xsl:value-of select="'nicht gelöschte Objekte (noch verlinkt):'" />
          </span>
          <br />
          <xsl:for-each select="linked/entry">
            <xsl:value-of select="@id" />
            <br/>
          </xsl:for-each>
        </p>
      </xsl:if>
      <xsl:if test="count(error/entry) != 0">
        <p>
          <span style="font-weight: bolder; color:#ff0000;">
            <xsl:value-of select="'nicht gelöschte Objekte (Exception aufgetreten!):'" />
          </span>
          <br />
          <xsl:for-each select="error/entry">
            <xsl:value-of select="@id" />
            <br/>
          </xsl:for-each>
        </p>
      </xsl:if>
      <p>
        <a href="{$WebApplicationBaseURL}recycleBin.xml">zurück zum Papierkorb</a>
      </p>
    </xsl:template>
</xsl:stylesheet>