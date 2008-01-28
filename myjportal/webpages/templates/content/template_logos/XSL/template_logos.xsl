<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

    <!-- ======================================================================================== -->

    <xsl:template name="template_logos.getLogos">
        <xsl:variable name="journalsID" />
        <xsl:apply-templates
            select="document('webapp:/templates/content/template_logos/CONFIG/template_logos.xml')/templates/template[journalID[@value=$journalsID]]/*" />
    </xsl:template>

    <!-- ======================================================================================== -->

    <xsl:template match="img/@src">
        <xsl:attribute name="src">
            <xsl:value-of select="concat($WebApplicationBaseURL,.)" />
        </xsl:attribute>
    </xsl:template>

    <!-- ======================================================================================== -->

    <!-- - - - - - - - - Identity Transformation  - - - - - - - - - -->
    <xsl:template match='@*|node()'>
        <xsl:copy>
            <xsl:apply-templates select='@*|node()' />
        </xsl:copy>
    </xsl:template>

    <!-- ======================================================================================== -->

</xsl:stylesheet>