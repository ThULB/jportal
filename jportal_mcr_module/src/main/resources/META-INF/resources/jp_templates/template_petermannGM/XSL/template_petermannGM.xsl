<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
  xmlns:layoutDetector="xalan://fsu.jportal.frontend.DynamicLayoutTemplateDetector" xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
  xmlns:escapeUtils="org.apache.commons.lang.StringEscapeUtils" xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="jpxml xalan layoutTools escapeUtils">

  <xsl:template match="/template[@id='template_petermannGM']" mode="template">
    <xsl:param name="mcrObj" />
    <xsl:variable name="journal" select="document(concat('mcrobject:', $journalID))/mycoreobject" />
    <xsl:apply-templates select="$journal" mode="template_petermannGM" />
    <xsl:call-template name="template_date">
      <xsl:with-param name="mcrObj" select="$mcrObj"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_petermannGM">
    <xsl:variable name="published_from">
      <xsl:value-of select="//date[@type='published_from']" />
    </xsl:variable>
    <xsl:variable name="published_until">
      <xsl:value-of select="//date[@type='published_until']" />
    </xsl:variable>

    <xsl:variable name="pubYear">
      <xsl:choose>
        <xsl:when test="($published_from != '') and ($published_until != '')">
          <xsl:value-of select="concat($published_from, '_', $published_until)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="''"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <script type="text/javascript">
      $(document).ready(function() {
        var baseURL = '<xsl:value-of select="$WebApplicationBaseURL" />';

        var pubYear = '<xsl:value-of select="$pubYear" />';
        $('#header').css('background-image', 'url(' + baseURL + 'jp_templates/template_petermannGM/IMAGES/logo_' + pubYear +'.png)');
      });
    </script>
  </xsl:template>
</xsl:stylesheet>