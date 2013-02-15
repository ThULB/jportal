<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n layoutTools" xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template match="/template[@id='template_goethe']" mode="template">
    <xsl:param name="mcrObj"/>
    <xsl:apply-templates select="$mcrObj" mode="template_goethe" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_goethe">
    <!-- get template ID from java -->
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
        var name = '<xsl:value-of select="layoutTools:getMaintitle(/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID)" />';
        $('#logo').prepend('<h1 class="logoTitle">' + truncate(name, 70)  + '</h1>');
        $('#logo').prepend('<div class="logoDate"><xsl:value-of select="$pubYear"/></div>');
      });
    </script>
  </xsl:template>
</xsl:stylesheet>
