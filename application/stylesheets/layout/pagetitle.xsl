<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


    <!-- ================================================================================= -->
    <xsl:template name="HTMLPageTitle">

        <xsl:variable name="titleFront">
            <xsl:choose>
                <xsl:when
                    test="contains(/mycoreobject/@ID,'_jpjournal_') 
				or contains(/mycoreobject/@ID,'_jpvolume_') 
				or contains(/mycoreobject/@ID,'_jparticle_')  ">
                    <xsl:copy-of select="concat(/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text(),' (')" />
                    <xsl:call-template name="printHistoryRow">
                        <xsl:with-param name="sortOrder" select="'descending'" />
                        <xsl:with-param name="printCurrent" select="'false'" />
                    </xsl:call-template>
                    <xsl:copy-of select="')'" />
                </xsl:when>
                <xsl:when test="contains(/mycoreobject/@ID,'_jpinst_') ">
                    <xsl:copy-of select="/mycoreobject/metadata/names/name/fullname/text()" />
                </xsl:when>
                <xsl:when test="contains(/mycoreobject/@ID,'_person_') ">
                    <xsl:copy-of select="/mycoreobject/metadata/def.heading/heading/lastName/text()" />
                    <xsl:if test="/mycoreobject/metadata/def.heading/heading/firstName/text()">
                        <xsl:copy-of select="concat(', ',/mycoreobject/metadata/def.heading/heading/firstName/text())" />
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$PageTitle" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:value-of select="concat($titleFront,' - ',$MainTitle)" />
    </xsl:template>
    <!-- ================================================================================= -->

</xsl:stylesheet>
