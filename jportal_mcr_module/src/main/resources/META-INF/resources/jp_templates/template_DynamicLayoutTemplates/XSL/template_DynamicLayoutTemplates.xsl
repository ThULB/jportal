<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
                xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
                xmlns:escapeUtils="org.apache.commons.lang.StringEscapeUtils" xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="jpxml xalan layoutTools escapeUtils">

    <xsl:template match="/template[@id='template_DynamicLayoutTemplates']" mode="template">
        <xsl:param name="mcrObj"/>
        <xsl:variable name="journal" select="document(concat('mcrobject:', $journalID))/mycoreobject"/>
        <xsl:apply-templates select="$journal" mode="template_DynamicLayoutTemplates"/>
        <xsl:call-template name="template_date">
            <xsl:with-param name="mcrObj" select="$mcrObj"/>
        </xsl:call-template>
        <xsl:call-template name="template_maintitle">
            <xsl:with-param name="mcrObj" select="$mcrObj"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/mycoreobject" mode="template_DynamicLayoutTemplates">
        <xsl:variable name="published" select="jpxml:getPublishedISODate(@ID)"/>
        <xsl:variable name="century" select="jpxml:getCentury($published)"/>

        <script type="text/javascript">
            $(document).ready(function() {
              var baseURL = '<xsl:value-of select="$WebApplicationBaseURL"/>';
              var century = <xsl:value-of select="$century"/>;
              if(century > 19){
                $('#header').css('background-image', 'url(' + baseURL + 'jp_templates/template_DynamicLayoutTemplates/IMAGES/logo19.png)');
              }
            });
        </script>
    </xsl:template>
</xsl:stylesheet>
