<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:layoutDetector="xalan://org.mycore.frontend.MCRJPortalLayoutTemplateDetector" xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools" xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template match="/template[@id='template_DynamicLayoutTemplates']" mode="template">
    <xsl:apply-templates select="document(concat('mcrobject:',@mcrID))/mycoreobject" mode="template_DynamicLayoutTemplates" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_DynamicLayoutTemplates">
    <!-- get template ID from java -->
    <xsl:variable name="template_DynamicLayoutTemplates" select="layoutDetector:getTemplateID(@ID)" />
    <xsl:variable name="journal" select="document(concat('mcrobject:', metadata/hidden_jpjournalsID/hidden_jpjournalID))/mycoreobject" />

    <xsl:variable name="published">
		<xsl:value-of select="$journal//date[@type='published']" />
	</xsl:variable>
	<xsl:variable name="published_from">
		<xsl:value-of select="$journal//date[@type='published_from']" />
	</xsl:variable>
	<xsl:variable name="published_until">
		<xsl:value-of select="$journal//date[@type='published_until']" />
	</xsl:variable>
	<xsl:variable name="pubYear">
		<xsl:choose>
			<xsl:when test="$published != ''">
				<xsl:value-of select="$published"/>
			</xsl:when>
			<xsl:when test="($published_from != '') and ($published_until != '')">
				<xsl:value-of select="concat($published_from, ' - ', $published_until)"/>
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
        $('#logo').css('background-image', 'url(' + baseURL + 'templates/master/' + template + '/IMAGES/logo.png)');
        var maintitle = '<xsl:value-of select="layoutTools:getMaintitle(/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID)" />';
        $('#logo').prepend('<div id="logoDate"><xsl:value-of select="$pubYear"/></div>');
        $('#logoDate').after('<div id="logoTitle">' + truncate(maintitle, 72)  + '</div>');
        if (name.length > 40){
        	$('#logoTitle').css('font-size', 'large');
        }
        if (name.length > 80){
        	$('#logoTitle').css('top', '8px');
        	$('#logoTitle').css('height', '70px');
        	$('#logoTitle').css('line-height', '24px');
        	$('#logoTitle').css('overflow', 'hidden');
        	$('#logoTitle').css('text-overflow', 'ellipsis');
        }
      });
    </script>
  </xsl:template>
</xsl:stylesheet>