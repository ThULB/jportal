<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:imprint="xalan://fsu.jportal.util.ImprintUtil" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager" exclude-result-prefixes="mcrxml i18n imprint acl">

  <xsl:template name="jp.navigation.top">
    <xsl:variable name="isGuest" select="mcrxml:isCurrentUserGuestUser()" />
      <ul class="list-inline jp-navigation-topHeader-UlRightWraper nav navbar-nav">
       
        <xsl:if test="not($isGuest)">
          <li class="userName jp-layout-mainHeader-SeperatorRight">
            <a> <xsl:value-of select="layoutTools:getUserName()" /></a>
          </li>
        </xsl:if>
        <li>
          <xsl:variable name="imprintHref">
            <xsl:choose>
              <xsl:when test="$journalID != '' and imprint:has($journalID, 'imprint')">
                <xsl:value-of select="concat($WebApplicationBaseURL, 'rsc/fs/imprint/webpage/', $journalID)" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="concat($WebApplicationBaseURL, 'jp-imprint.xml')" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <a href="{$imprintHref}">
            <xsl:value-of select="i18n:translate('jp.site.imprint')" />
          </a>
        </li>
        <xsl:if test="$journalID != '' and imprint:has($journalID, 'partner')">
          <li>
            <a href="{concat($WebApplicationBaseURL, 'rsc/fs/partner/webpage/', $journalID)}">
              <xsl:value-of select="i18n:translate('jp.site.partner')" />
            </a>
          </li>
        </xsl:if>
        <xsl:if test="acl:checkPermission('administrate-jportal')">
          <li>
            <a href="{$WebApplicationBaseURL}jp-admin.xml">Admin</a>
          </li>
        </xsl:if>
        <xsl:if test="not($isGuest)">
          <li>
            <a href="{$WebApplicationBaseURL}jp-account.xml">
              <xsl:value-of select="i18n:translate('jp.site.account')" />
            </a>
          </li>
        </xsl:if>
        <li>
          <a>
            <xsl:attribute name="href">
            <xsl:if test="$isGuest">
              <xsl:value-of select="concat($WebApplicationBaseURL, 'servlets/MCRLoginServlet?url=', $RequestURL)" />
            </xsl:if>
            <xsl:if test="not($isGuest)">
              <xsl:value-of select="concat($WebApplicationBaseURL, 'servlets/logout')" />
            </xsl:if>
          </xsl:attribute>
            <xsl:if test="$isGuest">
              <xsl:value-of select="i18n:translate('jp.site.login')" />
            </xsl:if>
            <xsl:if test="not($isGuest)">
              <xsl:value-of select="i18n:translate('jp.site.logout')" />
            </xsl:if>
          </a>
        </li>
    <xsl:call-template name="jp.navigation.top.language" />
    </ul>
  </xsl:template>

  <xsl:template name="jp.navigation.top.language">
      <li id="languageMenu" class="dropdown-toggle"> 
        <a data-toggle="dropdown" class="btn btn-default dropdown-toggle jp-navigation-topHeader-DropdownBorder" type="button">
          <img src="{$WebApplicationBaseURL}images/naviMenu/lang-{$CurrentLang}.png" alt="{$CurrentLang}" class="jp-navigation-topHeader-ImgPush" />
          <span class="caret"></span>
        </a>

        <ul role="menu" class="dropdown-menu jp-navigation-topHeader-DropdownMenu">
          <xsl:for-each select="$languages/lang">
            <xsl:if test="$CurrentLang != text()">
              <li>
                <a class="changeLang text-center">
                  <xsl:attribute name="href">
	                  <xsl:variable name="newurl">
	                    <xsl:call-template name="UrlSetParam">
	                      <xsl:with-param name="url" select="$RequestURL" />
	                      <xsl:with-param name="par" select="'lang'" />
	                      <xsl:with-param name="value" select="text()" />
	                    </xsl:call-template>
	                  </xsl:variable>
	                  <xsl:call-template name="UrlAddSession">
	                    <xsl:with-param name="url" select="$newurl" />
	                   </xsl:call-template>
	                </xsl:attribute>
                  <img src="{$WebApplicationBaseURL}images/naviMenu/lang-{text()}.png" alt="{text()}" />
                </a>
              </li>
            </xsl:if>
          </xsl:for-each>
        </ul>
        <xsl:call-template name="jp.navigation.top.language.js" />
      </li>    
  </xsl:template>

  <xsl:template name="jp.navigation.top.language.js">
    <script type="text/javascript">
      $(document).ready(function() {
      var languageList = $('#languageList');
      $('#languageSelect').click(function() {
      $(this).toggleClass('clicked');
      languageList.toggleClass('hidden');
      });
      });
    </script>
  </xsl:template>

</xsl:stylesheet>
