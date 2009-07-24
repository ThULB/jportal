<?xml version="1.0" encoding="iso-8859-1"?>
  <!-- ============================================== -->
  <!-- $Revision: 1398 $ $Date: 2008-10-07 11:35:52 +0200 (Di, 07 Okt 2008) $-->
  <!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" />
  <xsl:param name="objectType" />
  <xsl:param name="step" />
  <xsl:param name="titleuri" />
  <xsl:variable name="titles" select="document($titleuri)/titles" />

  <xsl:template name="getTitle">
    <xsl:param name="lang" />
    <xsl:choose>
      <xsl:when test="$titles/title[@step=$step and @type=$objectType and lang($lang)]">
        <xsl:value-of select="$titles/title[@step=$step and @type=$objectType and lang($lang)]" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat('Title(',$lang,'):',$step,'-',$objectType)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@title[parent::section]" priority="2">
    <xsl:attribute name="{local-name()}">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="lang" select="parent::*/@xml:lang" />
      </xsl:call-template>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="editor/@id|components/@root|panel/@id">
    <xsl:attribute name="{local-name()}">
      <xsl:value-of select="concat('editor-',$objectType)" />
	 </xsl:attribute>
  </xsl:template>

  <xsl:template match="cell/@ref">
    <xsl:attribute name="{local-name()}">
      <xsl:value-of select="concat('edit-',$objectType)" />
   </xsl:attribute>
  </xsl:template>

  <xsl:template match="include/@uri">
    <xsl:attribute name="{local-name()}">
      <xsl:value-of select="concat('webapp:editor/editor-',$objectType,'.xml')" />
	</xsl:attribute>
  </xsl:template>

  <xsl:template match="target/@name">
    <xsl:attribute name="{local-name()}">
      <xsl:choose>
        <xsl:when test="$step='author'">
          <xsl:value-of select="'MCRJPortalCheckCommitDataServlet'" />
        </xsl:when>
        <xsl:when test="$step='commit'">
          <xsl:value-of select="'MCRJPortalCheckCommitDataServlet'" />
        </xsl:when>
        <xsl:when test="$step='editor'">
          <xsl:value-of select="'MCRCheckEditDataServlet'" />
        </xsl:when>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

</xsl:stylesheet>