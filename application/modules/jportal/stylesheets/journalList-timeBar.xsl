<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/">

    <xsl:variable name="today" select="2008" />
    <xsl:variable name="spreading" select="3" />
    <xsl:variable name="barHeight" select="20" />

    <!-- =================================================================================================== -->

    <xsl:template match="journalList[@mode='chronological'] | journallist[@mode='chronological']">

        <xsl:variable name="journalIDs">
            <xsl:call-template name="get.allJournalIDs" />
        </xsl:variable>
        <!-- all journals with begin date -->
        <xsl:variable name="journalXMLs">
            <xsl:call-template name="get.journalXMLs">
                <xsl:with-param name="journalIDsIF" select="$journalIDs"></xsl:with-param>
            </xsl:call-template>
        </xsl:variable>
        <!-- get earliest year -->
        <xsl:variable name="journalList.earliestYear">
            <xsl:call-template name="get.earliestYear">
                <xsl:with-param name="journalXMLsIF" select="$journalXMLs"></xsl:with-param>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="journalList.timeTotal">
            <xsl:value-of select="($today - $journalList.earliestYear)" />
        </xsl:variable>

        <div style="position:relative;">
            <!-- legend bar -->
            <div
                style="position:absolute;top:0px;left:0px;width:{$journalList.timeTotal * $spreading}px; height:{$barHeight}px;border:1px solid #000000;background-color:#FF7070;" />
            <!-- journals -->
            <xsl:for-each select="xalan:nodeset($journalXMLs)/journalXMLs/mycoreobject">
                <xsl:variable name="start">
                    <xsl:value-of select="(metadata/dates/date[@type = 'published_from']/text() - $journalList.earliestYear) * $spreading" />
                </xsl:variable>
                <xsl:variable name="end">
                    <xsl:choose>
                        <xsl:when test="metadata/dates/date[@type = 'published_until']/text() != ''">
                            <xsl:value-of
                                select="(metadata/dates/date[@type = 'published_until']/text() - metadata/dates/date[@type = 'published_from']/text()) * $spreading" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$today * $spreading" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="top">
                    <xsl:value-of select="(position()* $barHeight div 13) * $barHeight" />
                </xsl:variable>
                <!-- draw -->
                <div style="position:absolute;top:{$top}px;left:{$start}px;width:{$end}px;height:20px;border:1px solid #000000;">
                    <xsl:value-of select="concat(substring(metadata/maintitles/maintitle/text(),1,8),'...')" />
                </div>
            </xsl:for-each>
        </div>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.journalXMLs">
        <xsl:param name="journalIDsIF" />
        <journalXMLs>
            <xsl:for-each select="xalan:nodeset($journalIDsIF)/mcr:results/mcr:hit">
                <xsl:variable name="mcrobj">
                    <xsl:copy-of select="document(concat('mcrobject:',@id))" />
                </xsl:variable>
                <xsl:if test="xalan:nodeset($mcrobj)/mycoreobject/metadata/dates/date[@type = 'published_from']/text() != ''">
                    <xsl:copy-of select="$mcrobj" />
                </xsl:if>
            </xsl:for-each>
        </journalXMLs>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.earliestYear">
        <xsl:param name="journalXMLsIF" />
        <xsl:for-each select="xalan:nodeset($journalXMLsIF)/journalXMLs/mycoreobject">
            <xsl:sort select="metadata/dates/date[@type = 'published_from']/text()" data-type="number" order="ascending" />
            <xsl:if test="position() = 1">
                <xsl:copy-of select="metadata/dates/date[@type = 'published_from']/text()" />
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <!-- =================================================================================================== -->

</xsl:stylesheet>