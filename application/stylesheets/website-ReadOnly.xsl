<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:variable name="PageTitle" select="/website-ReadOnly/pageTitle[lang($CurrentLang)]/text()" />

    <xsl:template match="/website-ReadOnly">
        <xsl:copy-of select="$writeProtectionMessage" /> 
    </xsl:template>

</xsl:stylesheet>
