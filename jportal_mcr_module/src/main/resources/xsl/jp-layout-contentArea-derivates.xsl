<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:iview2="xalan://org.mycore.iview2.frontend.MCRIView2XSLFunctions">

  <xsl:param name="iview2.debug" select="'false'" />

  <xsl:template mode="derivateDisplay" match="metadata/derivateLinks|structure/derobjects">
    <ul class="jp-layout-derivateLinks">
      <xsl:apply-templates mode="derivateDisplay" select="*" />
    </ul>
    <div id="viewerContainerWrapper" />
    <xsl:call-template name="initIview2JS" />
  </xsl:template>

  <xsl:template mode="derivateDisplay" match="derivateLink">
    <xsl:call-template name="derivListEntry">
      <xsl:with-param name="derivID" select="substring-before(@xlink:href, '/')" />
      <xsl:with-param name="file" select="concat('/',substring-after(@xlink:href, '/'))" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="derivateDisplay" match="derobject">
    <xsl:call-template name="derivListEntry">
      <xsl:with-param name="derivID" select="@xlink:href" />
      <xsl:with-param name="file" select="iview2:getSupportedMainFile(@xlink:href)" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="derivListEntry">
    <xsl:param name="derivID" />
    <xsl:param name="file" />

    <li>
      <div class="jp-layout-derivateWrapper">
        <div class="jp-layout-hidden-Button"></div>
        <img src="{concat($WebApplicationBaseURL,'servlets/MCRThumbnailServlet/',$derivID, $file,'?centerThumb=no')}" />
      </div>
    </li>
  </xsl:template>

  <xsl:template name="initIview2JS">
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/{$jqueryUI.version}/jquery-ui.min.js" />
    <xsl:choose>
      <xsl:when test="$iview2.debug ='true'">
        <script type="text/javascript" src="{$WebApplicationBaseURL}modules/iview2/js/iview2.js" />
      </xsl:when>
      <xsl:otherwise>
        <script type="text/javascript" src="{$WebApplicationBaseURL}modules/iview2/js/iview2.min.js" />
      </xsl:otherwise>
    </xsl:choose>
    <script type="text/javascript" src="{$WebApplicationBaseURL}iview/js/iview2Init.js" />
  </xsl:template>
</xsl:stylesheet>