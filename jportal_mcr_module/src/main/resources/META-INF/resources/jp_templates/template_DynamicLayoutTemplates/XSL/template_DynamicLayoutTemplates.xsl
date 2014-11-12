<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
  xmlns:layoutDetector="xalan://org.mycore.frontend.MCRJPortalLayoutTemplateDetector" xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools"
  xmlns:escapeUtils="org.apache.commons.lang.StringEscapeUtils" xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="jpxml xalan layoutTools escapeUtils">

  <xsl:template match="/template[@id='template_DynamicLayoutTemplates']" mode="template">
    <xsl:param name="mcrObj" />
    <xsl:apply-templates select="$mcrObj" mode="template_DynamicLayoutTemplates" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_DynamicLayoutTemplates">
    <!-- get template ID from java -->
    <xsl:variable name="template_DynamicLayoutTemplates" select="layoutDetector:getTemplateID(@ID)" />
    <xsl:variable name="journal" select="document(concat('mcrobject:', $journalID))/mycoreobject" />

    <xsl:variable name="published">
      <xsl:value-of select="$journal//date[@type='published']" />
    </xsl:variable>
    <xsl:variable name="published_from">
      <xsl:value-of select="$journal//date[@type='published_from']" />
    </xsl:variable>

    <xsl:variable name="century">
      <xsl:choose>
        <xsl:when test="$published_from != ''">
          <xsl:value-of select="jpxml:getCentury($published_from)"></xsl:value-of>
        </xsl:when>
        <xsl:when test="$published != ''">
          <xsl:value-of select="jpxml:getCentury($published)"></xsl:value-of>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="published_until">
      <xsl:value-of select="$journal//date[@type='published_until']" />
    </xsl:variable>
    <xsl:variable name="pubYear">
      <xsl:choose>
        <xsl:when test="$published != ''">
          <xsl:value-of select="$published" />
        </xsl:when>
        <xsl:when test="($published_from != '') and ($published_until != '')">
          <xsl:value-of select="concat($published_from, ' - ', $published_until)" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <script type="text/javascript">
      $(document).ready(function() {
        var baseURL = '<xsl:value-of select="$WebApplicationBaseURL" />';
        var template = '<xsl:value-of select="$template_DynamicLayoutTemplates" />';
        if(template == '') {
          console.error("Unable to find template. Maybe there is no valid published or published_from metadata field set.");
          return;
        }
        $('#logo').css('background-image', 'url(' + baseURL + 'jp_templates/template_DynamicLayoutTemplates/IMAGES/logo<xsl:value-of select="$century" />.png)');
        var maintitle = '<xsl:value-of select="escapeUtils:escapeJavaScript(layoutTools:getMaintitle($journalID))" />';
        $('#logo').prepend('<div id="logoDate"><xsl:value-of select="$pubYear" /></div>');
        $('#logoDate').after('<div id="logoTitle">' + truncate(maintitle, 96) + '</div>');
        if (maintitle.length > 40) {
          $('#logoTitle').css('font-size', 'large');
        }
      });
    </script>
  </xsl:template>
</xsl:stylesheet>