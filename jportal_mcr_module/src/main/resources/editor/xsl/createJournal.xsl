<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="/xsl/copynodes.xsl" />
  <xsl:param name="journalID" />

  <xsl:template match="/mycoreobject/metadata">
    <metadata>
      <xsl:apply-templates />
      <xsl:if test="not(hidden_jpjournalsID)">
        <hidden_jpjournalsID class="MCRMetaLangText" heritable="true" notinherit="false">
          <hidden_jpjournalID inherited="0" form="plain">
            <xsl:value-of select="$journalID" />
          </hidden_jpjournalID>
        </hidden_jpjournalsID>
      </xsl:if>
    </metadata>
  </xsl:template>
</xsl:stylesheet>