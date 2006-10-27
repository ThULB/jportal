<?xml version="1.0" encoding="iso-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.11 $ $Date: 2006/05/26 15:28:25 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xlink mcr">

  <xsl:variable name="nl"><xsl:text>
</xsl:text></xsl:variable>
	
  <!-- Trefferliste ausgeben -->
  <xsl:template match="/mcr:results">
	  <xsl:copy-of select="$nl" />
Anzahl der Treffer : <xsl:value-of select="@numHits" /><xsl:copy-of select="$nl" />
	<xsl:for-each select="mcr:hit">
        <xsl:apply-templates select="." />
    </xsl:for-each>
	<xsl:copy-of select="$nl" />
	<xsl:copy-of select="$nl" />
  </xsl:template>

  <!-- This is a default template, see document.xsl for a sample of a custom one-->
  <xsl:template match="mcr:hit">
    <xsl:value-of select="@host"/>: <xsl:value-of select="@id"/><xsl:copy-of select="$nl" />

  </xsl:template>

</xsl:stylesheet>