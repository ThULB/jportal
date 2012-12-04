<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template match="/template[@id='template_addrBookTh']" mode="template">
    <xsl:apply-templates select="document(concat('mcrobject:',@mcrID))/mycoreobject" mode="template_addrBookTh" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_addrBookTh">

	<xsl:variable name="published">
		<xsl:value-of select="xalan:nodeset($journalXML)//date[@type='published']" />
	</xsl:variable>
	<xsl:variable name="published_from">
		<xsl:value-of select="xalan:nodeset($journalXML)//date[@type='published_from']" />
	</xsl:variable>
	<xsl:variable name="published_until">
		<xsl:value-of
			select="xalan:nodeset($journalXML)//date[@type='published_until']" />
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
        var name = '<xsl:value-of select="document(concat('mcrobject:',/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID))/mycoreobject/metadata/hidden_genhiddenfields1/hidden_genhiddenfield1" />';
        $('#logo').prepend('<div id="logoTitle">' + name  + '</div>');
        $('#logoTitle').after('<div id="logoDate"><xsl:value-of select="$pubYear"/></div>');
      });
    </script>
  </xsl:template>  
</xsl:stylesheet>