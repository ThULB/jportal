<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:layoutUtils="xalan:///org.mycore.frontend.MCRLayoutUtilities" exclude-result-prefixes="layoutUtils">

  <xsl:variable name="PageTitle">
    <xsl:value-of select="'Sitemap'" />
  </xsl:variable>
  
  <xsl:template match="/" mode="metaTags">
    <xsl:comment>Start - metaTags (sitemap.xsl)</xsl:comment>
    <meta name="description" content="Übersichtsseite der Archivanwendung"/>
    <meta name="keywords" content="Sitemap"/>
    <xsl:comment>End - metaTags (sitemap.xsl)</xsl:comment>
  </xsl:template>

  <!-- ================================================================================= -->
  <xsl:template match="sitemap">
    <xsl:call-template name="sitemap.index" />
  </xsl:template>
  <!-- ================================================================================= -->
  <xsl:template name="sitemap.index">
    <xsl:comment>Start - sitemap.index (sitemap.xsl)</xsl:comment>
    <table id="sitemap" width="90%" border="0" cellspacing="0" cellpadding="0" align="center">
      <!-- general column widths definition -->
      <colgroup>
        <col width="45%" />
        <col width="10%" />
        <col width="45%" />
      </colgroup>
      <!-- END OF: general column widths definition -->
      <!-- menu left -->
      <!-- @Deprecated remove navi-main in next release -->
      <xsl:if test="$loaded_navigation_xml/navi-main">
        <xsl:apply-templates select="$loaded_navigation_xml/navi-main" mode="createSitemap">
          <xsl:with-param name="typeOfMenu" select="'tree'" />
        </xsl:apply-templates>
      </xsl:if>
      <xsl:if test="$loaded_navigation_xml/menu[@id=navi-main]">
        <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='navi-main']" mode="createSitemap">
          <xsl:with-param name="typeOfMenu" select="'tree'" />
        </xsl:apply-templates>
      </xsl:if>
      <!-- END OF: menu left -->
      <!-- menu on top -->
      <!-- @Deprecated remove navi-below in next release -->
      <xsl:if test="$loaded_navigation_xml/navi-below">
        <xsl:apply-templates select="$loaded_navigation_xml/navi-below" mode="createSitemap">
          <xsl:with-param name="typeOfMenu" select="'horizontal'" />
        </xsl:apply-templates>
      </xsl:if>
      <xsl:if test="$loaded_navigation_xml/menu[@id=navi-below]">
        <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='navi-below']" mode="createSitemap">
          <xsl:with-param name="typeOfMenu" select="'horizontal'" />
        </xsl:apply-templates>
      </xsl:if>
      <!-- END OF: menu on top -->
    </table>
    <xsl:comment>End - sitemap.index (sitemap.xsl)</xsl:comment>
  </xsl:template>
  <!-- ================================================================================= -->
  <xsl:template match="/*/*" mode="createSitemap">
    <xsl:param name="typeOfMenu" />
    <!-- display name of menu -->
    <tr>
      <th colspan="3">
        <xsl:value-of select="label[lang($CurrentLang)]" />
      </th>
    </tr>
    <!-- END OF: display name of menu -->
    <xsl:choose>
      <!-- display tree -->
      <xsl:when test=" $typeOfMenu = 'tree' ">
        <tr>
          <td colspan="3">
            <br />
          </td>
        </tr>
        <xsl:apply-templates select="item[position() mod 2=1]" mode="printRow" />
      </xsl:when>
      <!-- display horizontal version -->
      <xsl:otherwise>
        <!-- sub menu tree -->
        <tr valign="top">
          <td colspan="3" class="horizontal">
            <xsl:apply-templates select="." mode="createTree" />
          </td>
        </tr>
        <!-- END OF: sub menu tree -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- ================================================================================= -->
  <xsl:template match="item" mode="printRow">
    <xsl:variable name="currentNumber" select="count(preceding-sibling::item)+1" />
    <xsl:variable name="numberOfMainEntries" select="count(../item[layoutUtils:readAccess(@href)])" />
    <!-- display main menu name -->
    <tr>
      <td class="mainMenuPoint">
        <xsl:apply-templates select="." mode="printMainMenu" />
      </td>
      <xsl:if test="$currentNumber=1">
        <td rowspan="{$numberOfMainEntries+($numberOfMainEntries mod 2)}" />
      </xsl:if>
      <!-- right column -->
      <td class="mainMenuPoint">
        <xsl:apply-templates select="following-sibling::item[1]" mode="printMainMenu" />
      </td>
    </tr>
    <!-- END OF: display main menu name -->
  </xsl:template>
  <xsl:template match="item" mode="printMainMenu">
    <xsl:variable name="readAccess">
      <xsl:call-template name="get.readAccess">
        <xsl:with-param name="webpage" select="@href" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="$readAccess='true'">
      <h2>
        <xsl:value-of select="concat('[',count(preceding-sibling::item[layoutUtils:readAccess(@href)])+1,'] ')" />
        <a href="{concat($WebApplicationBaseURL,substring-after(@href,'/'))}">
          <xsl:choose>
            <xsl:when test="label[lang($CurrentLang)] != ''">
              <xsl:value-of select="label[lang($CurrentLang)]" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="label[lang($DefaultLang)]" />
            </xsl:otherwise>
          </xsl:choose>
        </a>
      </h2>
      <xsl:apply-templates select="." mode="createTree" />
    </xsl:if>
  </xsl:template>
  <!-- ================================================================================= -->
  <xsl:template match="item|/*/*" mode="createTree">
    <xsl:param name="parentPage" select="@href" />
    <ul>
      <xsl:for-each select="child::item">
        <xsl:variable name="access">
          <xsl:call-template name="get.readAccess">
            <xsl:with-param name="webpage" select="@href" />
            <xsl:with-param name="blockerWebpage" select="$parentPage" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="$access='true'">
          <xsl:variable name="linkKind">
            <xsl:choose>
              <!-- Does the current node have got childrens ? -->
              <xsl:when test=" count(child::item) &gt; 0 ">
                <xsl:value-of select="'poppedUp'" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'normal'" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <li>
            <xsl:call-template name="addMenuRow">
              <xsl:with-param name="linkKind" select="$linkKind" />
            </xsl:call-template>
            <xsl:if test="$linkKind = 'poppedUp'">
              <xsl:apply-templates select="." mode="createTree" />
            </xsl:if>
          </li>
        </xsl:if>
      </xsl:for-each>
    </ul>
  </xsl:template>
</xsl:stylesheet>