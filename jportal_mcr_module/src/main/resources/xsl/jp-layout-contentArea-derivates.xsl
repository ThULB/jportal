<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:template mode="derivateDisplay" match="metadata/derivateLinks">
    <ul class="jp-layout-derivateLinks">
      <xsl:for-each select="derivateLink">
        <xsl:variable name="derivID" select="substring-before(@xlink:href, '/')" />
        <xsl:variable name="derivParentID"
          select="document(concat('notnull:mcrobject:', $derivID))/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href" />
        <xsl:variable name="mainPage" select="substring-after(@xlink:href, '/')" />
        <xsl:variable name="href">
          <xsl:value-of
            select="concat($WebApplicationBaseURL,'receive/',$derivParentID,'?jumpback=true&amp;maximized=true&amp;page=',$mainPage,'&amp;derivate=',$derivID)" />
        </xsl:variable>
        <li>
          <a href="{$href}">
            <img src="{concat($WebApplicationBaseURL,'servlets/MCRThumbnailServlet/',@xlink:href)}" />
          </a>
        </li>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template mode="derivateDisplay" match="structure/derobjects">
    <xsl:message>
      DerObjects
      <xsl:value-of select="@xlink:href"></xsl:value-of>
    </xsl:message>
    <ul class="jp-layout-derivateLinks">
      <xsl:for-each select="derobject">
        <xsl:variable name="derivID" select="@xlink:href" />
        <xsl:variable name="derivParentID" select="/mycoreobject/@ID" />
        <xsl:variable name="mainPage" select="/mycoreobject/@ID" />
        <xsl:variable name="mainDoc"
          select="document(concat('notnull:mcrobject:', $derivID))/mycorederivate/derivate/internals/internal[@inherited='0']/@maindoc" />
        <xsl:variable name="href">
          <xsl:value-of
            select="concat($WebApplicationBaseURL,'receive/',$derivParentID,'?jumpback=true&amp;maximized=true&amp;derivate=',$derivID)" />
        </xsl:variable>
        <li>
        <!-- 
          <a href="{$href}">
            <img src="{concat($WebApplicationBaseURL,'servlets/MCRThumbnailServlet/',$derivID,'/',$mainDoc)}" />
          </a>
         -->
          <xsl:call-template name="derivateView">
            <xsl:with-param name="derivateID" select="$derivID" />
            <xsl:with-param name="extensions" select="'&quot;startWidth&quot;:192, &quot;startHeight&quot;:192'"/>
          </xsl:call-template>
        </li>
      </xsl:for-each>
    </ul>
  </xsl:template>
</xsl:stylesheet>