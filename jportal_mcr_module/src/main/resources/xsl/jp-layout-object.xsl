<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="i18n">
  <xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes" media-type="text/html" />

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="CurrentLang"/>
  <xsl:param name="languages"/>

  <xsl:variable name="settings" select="document('../xml/layoutDefaultSettings.xml')/layoutSettings" />
  <xsl:param name="ignore" select="'logo'" />

  <xsl:include href="jp-layout-contentArea-metadata.xsl" />
  <xsl:include href="jp-layout-functions.xsl" />
  <xsl:include href="jp-layout-tools.xsl" />
  <xsl:include href="coreFunctions.xsl" />

  <xsl:template match="/mycoreobject">
    <div class="jp-metadata" data-jp-id="{@ID}">
      <xsl:variable name="ignore" select="'logo'" />
      <xsl:apply-templates mode="metadataDisplay"
        select="metadata/child::node()[not(contains(name(), 'hidden_')) and not(contains($ignore, name())) and */@inherited='0']" />
      <div class="jp-layout-clear" />
    </div>
  </xsl:template>

  <!-- Overwrite metadataDisply to handle own stuff -->
  <xsl:template mode="metadataDisplay" match="metadata/*[contains($simpleType, @class)]" priority="1">
    <!-- own stuff -->
    <xsl:apply-templates mode="jp.layout.object" select="*" />
    <!-- default call -->
    <xsl:call-template name="metadataField">
      <xsl:with-param name="fields" select="*" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="jp.layout.object" match="*">
  </xsl:template>

  <!-- full name of jpinst -->
  <xsl:template mode="jp.layout.object" match="name[@inherited='0' and fullname]">
    <dt><xsl:value-of select="i18n:translate($settings/i18n[@tag='fullname'])" /></dt>
    <dd><xsl:value-of select="fullname/text()" /></dd>
  </xsl:template>

</xsl:stylesheet>
