<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
 exclude-result-prefixes="xalan i18n">

  <!-- ================================================================================= -->
  <xsl:template name="navigation.tree">
    <xsl:param name="CSSLayoutClass" select="'navi_main'" />
    <xsl:call-template name="NavigationTree">
      <xsl:with-param name="rootNode" select="document($navigationBase)/navigation/menu[@id='navi-main']" />
      <xsl:with-param name="CSSLayoutClass" select="$CSSLayoutClass"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="navigation.history">
    <xsl:copy-of select="'Navigation: '" />
    <xsl:call-template name="HistoryNavigationRow" />
  </xsl:template>

  <xsl:template name="navigation.row">
      <xsl:call-template name="NavigationRow">
        <xsl:with-param name="rootNode" select="document($navigationBase)/navigation/menu[@id='navi-below']" />
        <xsl:with-param name="CSSLayoutClass" select="'navi_below'" />
      </xsl:call-template>
      <!-- show languages -->
      <span>
        <xsl:call-template name="navigation.language" />
      </span>
  </xsl:template>

  <xsl:template name="navigation.language">
    <xsl:call-template name="navigation.js" />

    <xsl:variable name="languages" select="i18n:getAvailableLanguagesAsXML()" />

    <span class="language">
      <a id="languageSelect">
        <img src="{$WebApplicationBaseURL}images/naviMenu/lang-{$CurrentLang}.png" alt="{$CurrentLang}" />
        <img src="{$WebApplicationBaseURL}images/naviMenu/dropdown.png" style="padding: 0 3px 6px;" />
      </a>
  
      <ul id="languageMenu">
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
  </xsl:template>

  <xsl:template name="navigation.js">
    <script type="text/javascript">
      $(document).ready(function() {
        var languageSelect = $('#languageSelect');
        var languageMenu = $('#languageMenu');
        languageSelect.click(function() {
          var display = languageMenu.css('display');
          if(display == 'none') {
            languageMenu.css('display', 'block')
            languageSelect.css('background', '#7D7E7D');
          } else {
            languageMenu.css('display', 'none')
            languageSelect.css('background', '');
          }
        });
      });
    </script>
  </xsl:template>
  <!-- ================================================================================= -->
</xsl:stylesheet>
