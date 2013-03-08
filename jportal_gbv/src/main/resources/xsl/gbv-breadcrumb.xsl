<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template name="gbv-breadcrumb">
    <div id="jp-breadcrumb-container">
      <menu class="jp-layout-breadcrumb">
        <li>
          <a href="{$WebApplicationBaseURL}gbv-journalList.xml">
            <xsl:value-of select="'A-Z'" />
          </a>
        </li>
        <xsl:apply-templates mode="jp.printListEntry"
          select="document(concat('parents:',/mycoreobject/@ID))/parents/parent | metadata/maintitles/maintitle[@inherited='0'] | metadata/def.heading/heading" />
      </menu>
    </div>
    <xsl:call-template name="jp-layout-breadcrumb-scroller" />
  </xsl:template>

</xsl:stylesheet>