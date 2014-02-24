<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:imprint="xalan://fsu.jportal.util.ImprintUtil" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools" exclude-result-prefixes="mcrxml i18n imprint">

  <xsl:template name="jp.navigation.top">
    <xsl:variable name="isGuest" select="mcrxml:isCurrentUserGuestUser()" />
    <ul>
      <xsl:if test="not($isGuest)">
        <li class="userName">
          <xsl:value-of select="layoutTools:getUserName()"/>
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
      <xsl:if test="mcrxml:isCurrentUserInRole('admin')">
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
    </ul>
    <xsl:call-template name="jp.navigation.top.language" />
  </xsl:template>

  <xsl:template name="jp.navigation.top.language">
    <span id="languageMenu" class="languageMenu">
      <a id="languageSelect" class="languageSelect">
        <img src="{$WebApplicationBaseURL}images/naviMenu/lang-{$CurrentLang}.png" alt="{$CurrentLang}" />
        <img class="caret" src="{$WebApplicationBaseURL}images/naviMenu/dropdown.png" />
      </a>
      <ul id="languageList" class="hidden languageMenu">
        <xsl:for-each select="$languages/lang">
          <xsl:if test="$CurrentLang != text()">
            <li>
              <a class="changeLang">
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
    </span>
    <xsl:call-template name="jp.navigation.top.language.js" />
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
