<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns="http://datacite.org/schema/kernel-3"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>
    <xsl:output method="xml" encoding="utf-8" />

    <xsl:template match="/mycoreobject">
        <resource xsi:schemaLocation="http://datacite.org/schema/kernel-3 http://schema.datacite.org/meta/kernel-3.1/metadata.xsd">
            <xsl:apply-templates select="@*|node()" mode="datacite"/>
        </resource>
    </xsl:template>

    <xsl:template match="@*|node()" mode="datacite">
        <xsl:apply-templates select="@*|node()" mode="datacite"/>
    </xsl:template>

    <xsl:template match="metadata/identis/identi[@type='doi']" mode="datacite">
        <identifier identifierType="DOI"><xsl:value-of select="."/></identifier>

        <!-- 1 -->
        <resourceType resourceTypeGeneral="Dataset">Dataset</resourceType>
    </xsl:template>

    <xsl:template match="metadata/maintitles/maintitle[@inherited='0']" mode="datacite">
        <titles>
            <title xml:lang="en"><xsl:value-of select="."/></title>
        </titles>
    </xsl:template>

    <xsl:template match="metadata/participants" mode="datacite">
        <creators>
            <xsl:apply-templates select="participant[@type='author']" mode="datacite"/>
        </creators>
        <xsl:apply-templates select="participant[@type='mainPublisher']" mode="datacite"/>
    </xsl:template>

    <xsl:template match="participant[@type='author']" mode="datacite">
        <creator>
            <creatorName><xsl:value-of select="@xlink:title"/></creatorName>
        </creator>
    </xsl:template>

    <xsl:template match="participant[@type='mainPublisher']" mode="datacite">
        <publisher><xsl:value-of select="@xlink:title"/></publisher>
    </xsl:template>

    <xsl:template match="metadata/dates/date[@type='published']" mode="datacite">
        <publicationYear><xsl:value-of select="@date"/></publicationYear>
    </xsl:template>
</xsl:stylesheet>