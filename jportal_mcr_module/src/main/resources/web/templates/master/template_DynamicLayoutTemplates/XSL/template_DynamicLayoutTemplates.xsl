<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:layoutDetector="xalan://org.mycore.frontend.MCRJPortalLayoutTemplateDetector" xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template match="/template[@id='template_DynamicLayoutTemplates']" mode="template">
    <xsl:apply-templates select="document(concat('mcrobject:',@mcrID))/mycoreobject" mode="template_DynamicLayoutTemplates" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_DynamicLayoutTemplates">
    <!-- get template ID from java -->
    <xsl:variable name="template_DynamicLayoutTemplates">
      <xsl:value-of select="layoutDetector:getTemplateID()" />
    </xsl:variable>

    <script type="text/javascript">
      $(document).ready(function() {
        var baseURL = '<xsl:value-of select="$WebApplicationBaseURL" />';
        var template = '<xsl:value-of select="$template_DynamicLayoutTemplates" />';
        $('#logo').css('background-image', 'url(' + baseURL + 'templates/master/' + template + '/IMAGES/logo.png)');
      });
    </script>
  </xsl:template>

    <!-- ============================================== -->
<!--     <xsl:template name="template_DynamicLayoutTemplates">
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
                <xsl:variable name="journalMaintitle">
                    <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
                </xsl:variable>
                <xsl:variable name="timeFrame">
                    <xsl:copy-of
                        select="concat(xalan:nodeset($journalXML)/mycoreobject/metadata/dates/date[@type='published_from']/text(),' - ',xalan:nodeset($journalXML)/mycoreobject/metadata/dates/date[@type='published_until']/text())" />
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$template_DynamicLayoutTemplates = 'template_18thCentury'">
                        <xsl:call-template name="template_18thCentury">
                            <xsl:with-param name="journalsMaintitle" select="$journalMaintitle" />
                            <xsl:with-param name="periodetitle" select="$timeFrame" />
                            <xsl:with-param name="journalID" select="$journalsID" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="$template_DynamicLayoutTemplates = 'template_19thCentury'">
                        <xsl:call-template name="template_19thCentury">
                            <xsl:with-param name="journalsMaintitle" select="$journalMaintitle" />
                            <xsl:with-param name="periodetitle" select="$timeFrame" />
                            <xsl:with-param name="journalID" select="$journalsID" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="$template_DynamicLayoutTemplates = 'template_20thCentury'">
                        <xsl:call-template name="template_20thCentury">
                            <xsl:with-param name="journalsMaintitle" select="$journalMaintitle" />
                            <xsl:with-param name="periodetitle" select="$timeFrame" />
                            <xsl:with-param name="journalID" select="$journalsID" />
                        </xsl:call-template>
                    </xsl:when>
                </xsl:choose>

            </xsl:otherwise>
        </xsl:choose>
    </xsl:template> -->
    <!-- ============================================== -->
</xsl:stylesheet>