<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:template name="breadcrumb">
    <div id="jp-breadcrumb-container" class="jp-layout-marginLR">
      <menu class="jp-layout-breadcrumb">
        <li>
          <a href="foo.de">Start</a>
        </li>
        <xsl:apply-templates mode="printListEntry"
          select="document(concat('parents:',/mycoreobject/@ID))/parents/parent | metadata/maintitles/maintitle[@inherited='0']" />
      </menu>
    </div>
  </xsl:template>

  <xsl:template mode="printListEntryContent" match="parent">
    <a href="{$WebApplicationBaseURL}receive/{@xlink:href}">
      <xsl:value-of select="@xlink:title" />
    </a>
  </xsl:template>

  <xsl:template mode="printListEntryContent" match="maintitle[@inherited='0']">
    <span>
      <xsl:call-template name="shortenString">
        <xsl:with-param name="string" select="." />
        <xsl:with-param name="length" select="20" />
      </xsl:call-template>
    </span>
  </xsl:template>
</xsl:stylesheet>