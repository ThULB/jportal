<?xml version="1.0" encoding="ISO-8859-1"?>

  <!--
    XSL to transform XML output from MCRClassificationBrowser servlet to
    HTML for client browser, which is loaded by AJAX. The browser sends
    data of all child categories of the requested node.
  -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan i18n">
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="ServletsBaseURL" />
  <xsl:param name="template" />

  <xsl:output method="xml" omit-xml-declaration="yes" />


  <xsl:template match="/classificationBrowserData">
    <xsl:variable name="folder.closed"
      select="concat($WebApplicationBaseURL,'templates/master/',$template,'/IMAGES/folder_plus.gif')" />
    <xsl:variable name="folder.open"
      select="concat($WebApplicationBaseURL,'templates/master/',$template,'/IMAGES/folder_minus.gif')" />
    <xsl:variable name="folder.leaf"
      select="concat($WebApplicationBaseURL,'templates/master/',$template,'/IMAGES/folder_plain.gif')" />
    <xsl:variable name="maxLinks">
      <xsl:value-of
        select="category[not(@numLinks &lt; following-sibling::category/@numLinks)]/@numLinks" />
    </xsl:variable>
    <xsl:variable name="maxResults">
      <xsl:value-of
        select="category[not(@numResults &lt; following-sibling::category/@numResults)]/@numResults" />
    </xsl:variable>

    <ul class="cbList">
      <xsl:for-each select="category">
        <xsl:variable name="id"
          select="translate(concat(../@classification,'_',@id),'+/()[]','ABCDEF')" />
        <li>
          <xsl:choose>
            <xsl:when test="@children = 'true'">
              <input id="cbButton_{$id}" type="image" value="+" src="{$folder.closed}"
                onclick="toogle('{@id}','{$folder.closed}','{$folder.open}');" />
            </xsl:when>
            <xsl:otherwise>
              <img src="{$folder.leaf}" id="cbButton_{$id}" />
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="@numResults">
            <span class="cbNum">
              <xsl:call-template name="docportal.classBrowser.FormatCount">
                <xsl:with-param name="curCount" select="@numResults" />
                <xsl:with-param name="maxCount" select="$maxResults" />
              </xsl:call-template>
            </span>
          </xsl:if>
          <xsl:if test="@numLinks">
            <span class="cbNum">
              <xsl:call-template name="docportal.classBrowser.FormatCount">
                <xsl:with-param name="curCount" select="@numLinks" />
                <xsl:with-param name="maxCount" select="$maxLinks" />
              </xsl:call-template>
            </span>
          </xsl:if>
          <a
            href="{$ServletsBaseURL}MCRSearchServlet?query={@query}&amp;mask={../@webpage}&amp;{../@parameters}">
            <xsl:value-of select="label" />
          </a>
          <xsl:if test="uri">
            <xsl:text> </xsl:text>
            <a href="{uri}" class="cbURI">
              <xsl:value-of select="uri" />
            </a>
          </xsl:if>
          <xsl:if test="description">
            <p class="cbDescription">
              <xsl:value-of select="description" />
            </p>
          </xsl:if>
          <xsl:if test="@children = 'true'">
            <div id="cbChildren_{$id}" class="cbHidden" />
          </xsl:if>
        </li>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template name="docportal.classBrowser.FormatCount">
    <xsl:param name="maxCount" />
    <xsl:param name="curCount" />
    <!-- placeholder for numberOfFiles: 10 times &nbsp; -->
    <xsl:variable name="numberMask"
      select="'&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;'" />
    <xsl:variable name="maxLength" select="string-length($maxCount)" />
    <xsl:variable name="curLength" select="string-length($curCount)" />
    <xsl:variable name="lengthDiff" select="$maxLength - $curLength" />
    <xsl:variable name="cntString">
      <xsl:if test="$lengthDiff &gt; 0">
        <xsl:value-of select="substring($numberMask, 1, $lengthDiff)" />
      </xsl:if>
      <xsl:value-of select="$curCount" />
    </xsl:variable>
    <xsl:value-of select="i18n:translate('browse.document.count',$cntString)" />
  </xsl:template>

</xsl:stylesheet>
