<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.4 $ $Date: 2006/07/05 11:04:27 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink" >

<xsl:include href="MyCoReLayout.xsl" />

<xsl:variable name="PageTitle">Index aller Dokumente - Zugang für Suchmaschinen</xsl:variable>
<xsl:variable name="Servlet" select="'undefined'"/>

<!-- ======== page start ======== -->
<xsl:template match="/indexpage">
      <h1>Index aller Dokumente - Zugang für Suchmaschinen</h1>
      <p>
        Dies ist ein automatisch generierter Index aller Dokumente, geordnet
        nach der MCRObjectID. Dieser Zugang dient der
        Indizierung durch Suchmaschinen (robots).
        <br/><br/><a href="{$WebApplicationBaseURL}">Zurück zur Startseite...</a>
      </p>
      <p>
        <ul>
          <xsl:apply-templates select="results/*" />
        </ul>
      </p>
</xsl:template>

<!-- ========== value ========== -->
<xsl:template match="value">
  <li>
    <a class="metavalue" href="{$WebApplicationBaseURL}receive/{idx}">
      <xsl:value-of select="idx"/>
    </a><br/>
  </li>
</xsl:template>

<!-- ========== range ========== -->
<xsl:template match="range">
  <li>
    <a href="{$WebApplicationBaseURL}{/indexpage/@path}{from/@pos}-{to/@pos}/index.html">
      <xsl:text>Dokumente von </xsl:text>
      <xsl:value-of select="from/@short"/>
      <xsl:text> bis </xsl:text>
      <xsl:value-of select="to/@short" />
    </a>
  </li>
</xsl:template>

</xsl:stylesheet>
