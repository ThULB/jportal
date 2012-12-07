<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template match="/template[@id='template_addrBookTh']" mode="template">
    <xsl:apply-templates select="document(concat('mcrobject:',@mcrID))/mycoreobject" mode="template_addrBookTh" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_addrBookTh">
    <xsl:variable name="journalXML" select="document(concat('mcrobject:',/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID))/mycoreobject" />

	<xsl:variable name="published">
		<xsl:value-of select="$journalXML//date[@type='published']" />
	</xsl:variable>
	<xsl:variable name="published_from">
		<xsl:value-of select="$journalXML//date[@type='published_from']" />
	</xsl:variable>
	<xsl:variable name="published_until">
		<xsl:value-of select="$journalXML//date[@type='published_until']" />
	</xsl:variable>		
	<xsl:variable name="pubYear">
		<xsl:choose>
			<xsl:when test="$published != ''">
				<xsl:value-of select="$published"/>
			</xsl:when>
			<xsl:when test="$published_from != ''">
				<xsl:value-of select="concat($published_from, ' - ', $published_until)"/>
			</xsl:when>
		</xsl:choose>
	</xsl:variable>

    <script type="text/javascript">
      $(document).ready(function() {		
        var journalName = '<xsl:value-of select="$journalXML/metadata/hidden_genhiddenfields1/hidden_genhiddenfield1" />';
        var journalDate = '<xsl:value-of select="$pubYear" />'
        $('#logo').prepend('<h1 class="logoTitle">' + journalName + ' ' + journalDate  + '</h1>');
      });
    </script>
  </xsl:template>
</xsl:stylesheet>