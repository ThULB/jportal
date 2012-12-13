<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools"
    xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template match="/template[@id='template_addrBookTh']" mode="template">
    <xsl:apply-templates select="document(concat('mcrobject:',@mcrID))/mycoreobject" mode="template_addrBookTh" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_addrBookTh">
    <xsl:variable name="datesInfo" select="layoutTools:getDatesInfo(/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID)" />

    <script type="text/javascript">
      $(document).ready(function() {		
        $('#logo').prepend('<xsl:apply-templates mode="logoTitle" select="$datesInfo"/>');
      });
    </script>
  </xsl:template>
  
  <xsl:template mode="logoTitle" match="datesInfo">
    <xsl:variable name="cityName">
    	<xsl:apply-templates mode="logoTitle" select="hidden_genhiddenfields1/hidden_genhiddenfield1"/>
    </xsl:variable> 
    <xsl:variable name="dates">
    	<xsl:apply-templates mode="logoTitle" select="dates"/>
    </xsl:variable> 
    <h1 class="logoTitle"><xsl:value-of select="concat($cityName, ' ', $dates)"/></h1>
  </xsl:template>
  
  <xsl:template mode="logoTitle" match="dates">
    <xsl:apply-templates mode="logoTitle" select="date[@type='published']|date[@type='published_from']|date[@type='published_until']"/>
  </xsl:template>
  
  <xsl:template mode="logoTitle" match="*">
    <xsl:value-of select="." />
  </xsl:template>
  
  <xsl:template mode="logoTitle" match="date[@type='published_from']">
    <xsl:value-of select="concat(., ' - ')" />
  </xsl:template>
</xsl:stylesheet>