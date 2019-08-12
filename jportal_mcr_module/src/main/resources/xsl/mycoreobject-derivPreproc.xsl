<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
    <xsl:include href="copynodes.xsl" />

    <xsl:template match="derobjects[@class='MCRMetaLinkID']">
        <derobjects class="MCRMetaEnrichedLinkID">
            <xsl:apply-templates mode="migrate" select="derobject"/>
        </derobjects>
    </xsl:template>

    <xsl:template mode="migrate" match="derobject">
        <xsl:variable name="mainDoc">
            <xsl:apply-templates mode="mainDoc" select="document(concat('mcrobject:',@xlink:href))"/>
        </xsl:variable>

        <derobject inherited="0" xlink:type="locator" xlink:title="{@xlink:title}" xlink:href="{@xlink:href}">
            <order>1</order>
            <mainDoc><xsl:value-of select="$mainDoc"/></mainDoc>
        </derobject>
    </xsl:template>

    <xsl:template mode="mainDoc" match="node()|@*">
        <xsl:apply-templates mode="mainDoc" select="*"/>
    </xsl:template>

    <xsl:template mode="mainDoc" match="internal">
        <xsl:value-of select="@maindoc"/>
    </xsl:template>
</xsl:stylesheet>
