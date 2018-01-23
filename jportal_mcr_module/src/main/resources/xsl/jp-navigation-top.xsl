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
            <li>
              <a href="{$JP.Site.Parent.url}" target="_blank">
                <xsl:value-of select="$JP.Site.Parent.label"/>
              </a>
            </li>
            <li class="jp-layout-mainHeader-LiPaPushleft">
              <a href="{$WebApplicationBaseURL}content/below/index.xml" target="_self">
                <xsl:value-of select="$JP.Site.label"/>
              </a>
            </li>
            <xsl:if test="websiteWriteProtection:isActive() and $CurrentUser != 'gast'">
              <li>
                <span class="webWriteProtection">
                  <xsl:value-of select="websiteWriteProtection:getMessage()"/>
                </span>
              </li>
            </xsl:if>

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
                  <a data-toggle="dropdown" class="btn btn-default dropdown-toggle jp-navigation-topHeader-DropdownBorder" type="button">
                    <xsl:value-of select="layoutTools:getUserName()"/>
                    <span class="caret"></span>
                  </a>

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
        <div class="col-sm-4 col-xs-2">
          <div class="dropdown dropdown-menu-left pull-left jp-layout-object-editing-container">
            <button id="jp-edit-menu-button" class="btn btn-default fa fa-gear dropdown-toggle" type="button" data-toggle="dropdown"/>
            <ul class="jp-layout-object-editing-menu dropdown-menu dropdown-menu-right" role="menu">
              <xsl:copy-of select="$objectEditing/*"/>
            </ul>
          </div>
        </div>
      </li>
    </xsl:if>
  </xsl:template>
  <xsl:template name="jp.navigation.top.language">
    <li id="languageMenu" class="dropdown-toggle">
      <a data-toggle="dropdown" class="btn btn-default dropdown-toggle jp-navigation-topHeader-DropdownBorder" type="button">
        <!--<img src="{$WebApplicationBaseURL}images/naviMenu/lang-{$CurrentLang}.png" alt="{$CurrentLang}" class="jp-navigation-topHeader-ImgPush"/>-->
        <xsl:value-of select="$CurrentLang"/>
        <span class="caret"></span>
      </a>

      <ul id="languageList" role="menu" class="dropdown-menu jp-navigation-topHeader-DropdownMenu">
        <xsl:for-each select="$languages/lang">
          <xsl:if test="$CurrentLang != text()">
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
                <!--<img src="{$WebApplicationBaseURL}images/naviMenu/lang-{text()}.png" alt="{text()}"/>-->
                <xsl:value-of select="text()"/>
              </a>
            </li>
          </xsl:if>
        </xsl:for-each>
      </ul>
    </li>
  </xsl:template>

</xsl:stylesheet>
