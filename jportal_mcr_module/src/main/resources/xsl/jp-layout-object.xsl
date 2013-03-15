<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes" media-type="text/html" />

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="CurrentLang"/>
  <xsl:param name="languages"/>

  <xsl:variable name="settings" select="document('../xml/layoutDefaultSettings.xml')/layoutSettings" />
  <xsl:param name="ignore" select="'logo'" />

  <xsl:include href="jp-layout-contentArea-metadata.xsl" />
  <xsl:include href="jp-layout-functions.xsl" />
  <xsl:include href="jp-layout-tools.xsl" />

  <xsl:template match="/mycoreobject">
    <div class="jp-metadata">
      <xsl:variable name="ignore" select="'logo'" />
      <xsl:apply-templates mode="metadataDisplay"
        select="metadata/child::node()[not(contains(name(), 'hidden_')) and not(contains($ignore, name())) and */@inherited='0']" />
    </div>
  </xsl:template>

</xsl:stylesheet>
