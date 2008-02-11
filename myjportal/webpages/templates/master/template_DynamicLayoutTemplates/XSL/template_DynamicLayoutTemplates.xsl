<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:layoutDetector="xalan://org.mycore.frontend.MCRJPortalLayoutTemplateDetector" xmlns:xalan="http://xml.apache.org/xalan">
    <!-- ============================================== -->
    <xsl:template name="template_DynamicLayoutTemplates">
        <!-- get template ID from java -->
        <xsl:variable name="template_DynamicLayoutTemplates">
            <xsl:value-of select="layoutDetector:getTemplateID()" />
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$template_DynamicLayoutTemplates = ''">
                <xsl:call-template name="template_master" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="journalsID">
                    <xsl:value-of select="document('jportal_getJournalID:XPathDoesNotExist')/dummyRoot/hidden/@default" />
                </xsl:variable>
                <xsl:variable name="journalXML">
                    <xsl:copy-of select="document(concat('mcrobject:',$journalsID))" />
                </xsl:variable>
                <!-- get name of journal -->
                <xsl:variable name="journalMaintitle">
                    <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
                </xsl:variable>
                <!-- get time window -->
                <xsl:variable name="timeFrame">
                    <xsl:copy-of
                        select="concat(xalan:nodeset($journalXML)/mycoreobject/metadata/dates/date[@type='published_from']/text(),' - ',xalan:nodeset($journalXML)/mycoreobject/metadata/dates/date[@type='published_until']/text())" />
                </xsl:variable>
                <!-- TODO: generate this by ant -->
                <xsl:choose>
                    <xsl:when test="$template_DynamicLayoutTemplates = 'template_18thCentury'">
                        <xsl:call-template name="template_18thCentury">
                            <xsl:with-param name="journalsMaintitle" select="$journalMaintitle" />
                            <xsl:with-param name="periodetitle" select="$timeFrame" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="$template_DynamicLayoutTemplates = 'template_19thCentury'">
                        <xsl:call-template name="template_19thCentury">
                            <xsl:with-param name="journalsMaintitle" select="$journalMaintitle" />
                            <xsl:with-param name="periodetitle" select="$timeFrame" />
                        </xsl:call-template>
                    </xsl:when>                    
                </xsl:choose>

            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- ============================================== -->
</xsl:stylesheet>