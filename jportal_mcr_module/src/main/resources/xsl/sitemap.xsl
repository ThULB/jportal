<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:layoutUtils="xalan:///org.mycore.frontend.MCRLayoutUtilities" exclude-result-prefixes="layoutUtils">

  <xsl:variable name="PageTitle">
    <xsl:value-of select="'Sitemap'" />
  </xsl:variable>
  
  <xsl:template match="/" mode="metaTags">
    <meta name="description" content="Übersichtsseite des Archivanwendung"/>
    <meta name="keywords" content="Sitemap"/>
  </xsl:template>

  <!-- ================================================================================= -->
  <xsl:template match="sitemap">
    <xsl:call-template name="sitemap.index" />
  </xsl:template>
  <!-- ================================================================================= -->
  <xsl:template name="sitemap.index">
    <table id="sitemap" width="90%" border="0" cellspacing="0" cellpadding="0" align="center">
      <!-- general column widths definition -->
      <colgroup>
        <col width="45%" />
        <col width="10%" />
        <col width="45%" />
      </colgroup>
      <!-- END OF: general column widths definition -->
      <!-- menu left -->
      <xsl:apply-templates select="$loaded_navigation_xml/navi-main" mode="createSitemap">
        <xsl:with-param name="typeOfMenu" select="'tree'" />
      </xsl:apply-templates>
      <!-- END OF: menu left -->
      <!-- menu on top -->
      <xsl:apply-templates select="$loaded_navigation_xml/navi-below" mode="createSitemap">
        <xsl:with-param name="typeOfMenu" select="'horizontal'" />
      </xsl:apply-templates>
      <!-- END OF: menu on top -->
    </table>
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
	<xsl:variable name="readableItems" select="item[layoutUtils:readAccess(@href)]"/>
        <xsl:apply-templates select="$readableItems[position() mod 2=1]" mode="printRow">
          <xsl:with-param name="readableItems" select="$readableItems"/>
	</xsl:apply-templates>
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
    <xsl:param name="readableItems" />
    <xsl:variable name="currentLink" select="@href"/>
    <xsl:variable name="currentNumber">
      <xsl:for-each select="$readableItems">
        <xsl:if test="@href=$currentLink">
          <xsl:value-of select="position()"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="numberOfMainEntries" select="count($readableItems)" />
    <!-- display main menu name -->
    <tr>
      <td class="mainMenuPoint">
        <xsl:apply-templates select="." mode="printMainMenu">
          <xsl:with-param name="currentNumber" select="$currentNumber"/>
        </xsl:apply-templates>
      </td>
      <xsl:if test="$currentNumber=1">
        <td rowspan="{$numberOfMainEntries+($numberOfMainEntries mod 2)}" />
      </xsl:if>
      <!-- right column -->
      <td class="mainMenuPoint">
        <xsl:apply-templates select="$readableItems[$currentNumber+1]" mode="printMainMenu">
          <xsl:with-param name="currentNumber" select="$currentNumber+1"/>
        </xsl:apply-templates>
      </td>
    </tr>
    <!-- END OF: display main menu name -->
  </xsl:template>
  <xsl:template match="item" mode="printMainMenu">
    <xsl:param name="currentNumber"/>
    <h2>
      <xsl:value-of select="concat('[',$currentNumber,'] ')" />
      <xsl:call-template name="addLink">
        <xsl:with-param name="linkKind" select="'normal'" />
      </xsl:call-template>
    </h2>
    <xsl:apply-templates select="." mode="createTree" />
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
            <xsl:call-template name="addLink">
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
