<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.7 $ $Date: 2007-12-13 09:00:58 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xlink" >

<xsl:include href="MyCoReLayout.xsl" />

<xsl:variable name="PageTitle" select="i18n:translate('titles.pageTitle.robots')" />
<xsl:variable name="Servlet" select="'undefined'"/>

<!-- ======== page start ======== -->
<xsl:template match="/indexpage">
      <h1><xsl:value-of select="i18n:translate('titles.pageTitle.robots')"/></h1>
      <p>
        <xsl:value-of select="i18n:translate('indexpage.robots.text')"/>
        <br/><br/><a href="{$WebApplicationBaseURL}"><xsl:value-of select="i18n:translate('indexpage.robots.back')"/></a>
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
      <xsl:value-of select="i18n:translate('indexpage.robots.from')"/>
      <xsl:value-of select="from/@short"/>
      <xsl:value-of select="i18n:translate('indexpage.robots.to')"/>
      <xsl:value-of select="to/@short" />
    </a>
  </li>
</xsl:template>

</xsl:stylesheet>
