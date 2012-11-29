<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="mcrxml i18n">

  <xsl:template name="jp.navigation.top">
    <ul>
      <li>
        <a href="{$WebApplicationBaseURL}content/below/index.xml">Start</a>
      </li>
      <xsl:if test="acl:checkPermission('CRUD', 'admin')">
        <li>
          <a href="{$WebApplicationBaseURL}jp-admin.xml">Admin</a>
        </li>
      </xsl:if>
      <li>
        <xsl:variable name="isGuest" select="mcrxml:isCurrentUserGuestUser()" />
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
            <xsl:value-of select="i18n:translate('jportal.navigation.top.login')" />
          </xsl:if>
          <xsl:if test="not($isGuest)">
            <xsl:value-of select="i18n:translate('jportal.navigation.top.logout')" />
          </xsl:if>
        </a>
      </li>
    </ul>
    <xsl:call-template name="jp.navigation.top.language" />
  </xsl:template>

  <xsl:template name="jp.navigation.top.language">
    <xsl:variable name="languages" select="i18n:getAvailableLanguagesAsXML()" />

    <span id="languageMenu" class="languageMenu">
      <a id="languageSelect">
        <img src="{$WebApplicationBaseURL}images/naviMenu/lang-{$CurrentLang}.png" alt="{$CurrentLang}" />
        <img src="{$WebApplicationBaseURL}images/naviMenu/dropdown.png" style="padding: 0 3px 6px;" />
      </a>
  
      <ul id="languageList" class="languageList_hidden">
        <xsl:for-each select="$languages/i18n/lang">
          <xsl:if test="$CurrentLang != text()">
            <li>
              <xsl:variable name="flag">
                <img
                  src="{$WebApplicationBaseURL}images/naviMenu/lang-{text()}.png"
                  alt="{text()}" />
              </xsl:variable>
              <xsl:call-template name="FlagPrinter">
                <xsl:with-param name="flag" select="$flag" />
                <xsl:with-param name="lang" select="text()" />
                <xsl:with-param name="url" select="$RequestURL" />
                <xsl:with-param name="alternative" select="concat($RequestURL, '?lang=', text())" />
              </xsl:call-template>
            </li>  
          </xsl:if>
        </xsl:for-each>
      </ul>
    </span>
    <xsl:call-template name="jp.navigation.top.language.js" />
  </xsl:template>

  <xsl:template name="jp.navigation.top.language.js">
    <script type="text/javascript">
      $(document).ready(function() {
        var languageList = $('#languageList');
        $('#languageSelect').click(function() {
            $(this).toggleClass('languageSelect');
            languageList.toggleClass('languageList_hidden');
            languageList.toggleClass('languageList_show');
        });
      });
    </script>
  </xsl:template>
  
</xsl:stylesheet>
