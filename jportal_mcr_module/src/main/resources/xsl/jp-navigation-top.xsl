<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:imprint="xalan://fsu.jportal.util.ImprintUtil" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
                xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:websiteWriteProtection="xalan://org.mycore.frontend.MCRWebsiteWriteProtection" exclude-result-prefixes="mcrxml i18n imprint acl websiteWriteProtection">
  <xsl:param name="JP.Site.label"/>
  <xsl:param name="JP.Site.Parent.label"/>
  <xsl:param name="JP.Site.Parent.url"/>

  <xsl:template name="jp.navigation.top">
    <xsl:variable name="isGuest" select="mcrxml:isCurrentUserGuestUser()"/>
    <nav id="jpNavTop" class="navbar navbar-default">
      <div class="container-fluid">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed jp-layout-mynavbarbutton" data-toggle="collapse" data-target="#navbar-collapse-globalHeader">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
        </div>
        <div class="collapse navbar-collapse" id="navbar-collapse-globalHeader">
          <ul class="list-inline nav navbar-nav">
            <li class="jp-layout-mainHeader-LiPaPushleft">
              <a href="{$WebApplicationBaseURL}content/below/index.xml" target="_self">
                <xsl:value-of select="$JP.Site.label"/>
              </a>
            </li>
            <li>
              <a href="{$JP.Site.Parent.url}" target="_blank">
                <xsl:value-of select="$JP.Site.Parent.label"/>
              </a>
            </li>
            <li>
              <xsl:variable name="imprintHref">
                <xsl:choose>
                  <xsl:when test="$journalID != '' and imprint:has($journalID, 'imprint')">
                    <xsl:value-of select="concat($WebApplicationBaseURL, 'rsc/fs/imprint/webpage/', $journalID)"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="concat($WebApplicationBaseURL, 'jp-imprint.xml')"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <a href="{$imprintHref}">
                <xsl:value-of select="i18n:translate('jp.site.imprint')"/>
              </a>
            </li>
            <li>
              <a href="{concat($WebApplicationBaseURL, 'jp-dsgvo.xml')}">
                <xsl:value-of select="i18n:translate('jp.site.privacy')"/>

              </a>
            </li>
            <xsl:if test="$journalID != '' and imprint:has($journalID, 'partner')">
              <li>
                <a href="{concat($WebApplicationBaseURL, 'rsc/fs/partner/webpage/', $journalID)}">
                  <xsl:value-of select="i18n:translate('jp.site.partner')"/>
                </a>
              </li>
            </xsl:if>

            <xsl:choose>
              <xsl:when test="$isGuest">
                <li>
                  <a id="jp.login.button" href="{concat($WebApplicationBaseURL, 'servlets/MCRLoginServlet?action=login')}">
                    <xsl:value-of select="i18n:translate('jp.site.login')"/>
                  </a>
                </li>
              </xsl:when>
              <xsl:when test="not($isGuest)">
                <li>
                  <div class="btn-group jp-navigation-topHeader-Dropdown">
                    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                      <xsl:value-of select="layoutTools:getUserName()"/>
                      <span class="caret"></span>
                    </button>
                    <ul id="userDropdownMenu" role="menu" class="dropdown-menu jp-navigation-topHeader-DropdownMenu">
                      <xsl:if test="acl:checkPermission('administrate-jportal')">
                        <li>
                          <a href="{$WebApplicationBaseURL}jp-admin.xml">Administration</a>
                        </li>
                      </xsl:if>
                      <li>
                        <a href="{$WebApplicationBaseURL}jp-account.xml">
                          <xsl:value-of select="i18n:translate('jp.site.account')"/>
                        </a>
                      </li>
                      <li role="separator" class="divider"></li>
                      <li>
                        <a id="jp.login.button" href="{concat($WebApplicationBaseURL, 'servlets/logout')}">
                          <xsl:value-of select="i18n:translate('jp.site.logout')"/>
                        </a>
                      </li>
                    </ul>
                  </div>
                </li>
              </xsl:when>
            </xsl:choose>
            <xsl:call-template name="jp.navigation.top.language"/>
            <xsl:call-template name="jp.navigation.top.object.editing">
              <xsl:with-param name="isGuest" select="$isGuest"/>
            </xsl:call-template>
          </ul>
        </div>
      </div>
    </nav>
  </xsl:template>

  <xsl:template name="jp.navigation.top.object.editing">
    <xsl:param name="isGuest"/>
    <xsl:if test="not($isGuest) and $objectEditing//li/a">
      <li>
        <!-- edit object -->
        <div class="btn-group jp-navigation-topHeader-Dropdown">
          <button type="button" class="btn btn-default dropdown-toggle fas fa-bars"
                  data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"/>
          <ul class="jp-layout-object-editing-menu dropdown-menu dropdown-menu-right" role="menu">
            <xsl:copy-of select="$objectEditing/*"/>
          </ul>
        </div>
      </li>
    </xsl:if>
  </xsl:template>
  <xsl:template name="jp.navigation.top.language">
    <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'"/>
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>

    <li id="languageMenu" class="dropdown-toggle">
      <div class="btn-group jp-navigation-topHeader-Dropdown">
        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
          <xsl:value-of select="translate($CurrentLang, $smallcase, $uppercase)"/>
          <span class="caret"></span>
        </button>
        <ul id="languageList" role="menu" class="dropdown-menu jp-navigation-topHeader-DropdownMenu">
          <xsl:for-each select="$languages/lang">
            <xsl:if test="$CurrentLang != text() and . != 'ru' and . != 'pl'">
              <li>

                <a class="changeLang text-center">
                  <xsl:attribute name="href">
                    <xsl:variable name="newurl">
                      <xsl:call-template name="UrlSetParam">
                        <xsl:with-param name="url" select="$RequestURL"/>
                        <xsl:with-param name="par" select="'lang'"/>
                        <xsl:with-param name="value" select="text()"/>
                      </xsl:call-template>
                    </xsl:variable>
                    <xsl:call-template name="UrlAddSession">
                      <xsl:with-param name="url" select="$newurl"/>
                    </xsl:call-template>
                  </xsl:attribute>
                  <xsl:value-of select="translate(text(), $smallcase, $uppercase)"/>
                </a>
              </li>
            </xsl:if>
          </xsl:for-each>
        </ul>
      </div>
    </li>
  </xsl:template>

</xsl:stylesheet>
