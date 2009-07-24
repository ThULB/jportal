<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.20 $ $Date: 2007-04-04 13:23:09 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="mcr xsl encoder">

<xsl:param name="WebApplicationBaseURL" />

<xsl:template match="/mcr:results">
  <rss version="2.0">
    <channel>
      <title>Suchergebnisse als RSS-Feed</title>
      <description>
        Suche nach
        <xsl:value-of select="condition[@format='text']/text()" />
      </description>
      <link>
        <xsl:value-of select="$WebApplicationBaseURL" />
        <xsl:text>servlets/MCRSearchServlet?XSL.Style=rss&amp;maxResults=30&amp;created.sortField=descending&amp;query=</xsl:text>
        <xsl:value-of select="encoder:encode(condition[@format='text']/text())" />
      </link>
      <xsl:apply-templates select="mcr:hit" />
    </channel>
  </rss>
</xsl:template>

<xsl:template match="mcr:hit">
  <xsl:apply-templates select="document(concat('mcrobject:',@id))/mycoreobject" />
</xsl:template>

<xsl:template match="mycoreobject">
  <item>
    <pubDate>
      <xsl:variable name="date" select="metadata/dates/date[starts-with(@type,'creat')]" />
      <xsl:if test="string-length($date) &gt; 0">
        <xsl:variable name="mm" select="substring($date,6,2)" />
        <xsl:value-of select="substring($date,9,2)" />
        <xsl:text> </xsl:text>
        <xsl:if test="$mm='01'">Jan</xsl:if>
        <xsl:if test="$mm='02'">Feb</xsl:if>
        <xsl:if test="$mm='03'">Mar</xsl:if>
        <xsl:if test="$mm='04'">Apr</xsl:if>
        <xsl:if test="$mm='05'">May</xsl:if>
        <xsl:if test="$mm='06'">Jun</xsl:if>
        <xsl:if test="$mm='07'">Jul</xsl:if>
        <xsl:if test="$mm='08'">Aug</xsl:if>
        <xsl:if test="$mm='09'">Sep</xsl:if>
        <xsl:if test="$mm='10'">Oct</xsl:if>
        <xsl:if test="$mm='11'">Nov</xsl:if>
        <xsl:if test="$mm='12'">Dec</xsl:if>
        <xsl:text> </xsl:text>
        <xsl:value-of select="substring($date,1,4)" />
        <xsl:text> </xsl:text>
        <xsl:value-of select="substring-after($date,' ')" />
        <xsl:text> GMT+0100</xsl:text>
      </xsl:if>
    </pubDate>
    <title>
      <xsl:value-of select="metadata/titles/title" />
    </title>
    <description>
      <xsl:value-of select="metadata/descriptions/description" />
    </description>
    <link>
      <xsl:value-of select="$WebApplicationBaseURL" />
      <xsl:text>receive/</xsl:text>
      <xsl:value-of select="@ID" />
    </link>
  </item>
</xsl:template>

</xsl:stylesheet>
