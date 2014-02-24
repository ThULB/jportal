<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:jpxml="xalan://org.mycore.common.xml.MCRJPortalXMLFunctions" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager"  exclude-result-prefixes="mcrxml jpxml i18n acl">

  <xsl:template match="jpaccount">
    <div class="jp-layout-account">
      <h1>Konto</h1>
      <xsl:choose>
        <xsl:when test="not(mcrxml:isCurrentUserGuestUser())">
          <p>Sie sind angemeldet als <xsl:value-of select="$user" />.</p>
          <xsl:call-template name="jp.account.show" />
        </xsl:when>
        <xsl:otherwise>
          <p>Sie sind nicht angemeldet.</p>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template name="jp.account.show">
    <ul>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRUserServlet?action=changeMyPassword">Passwort ändern</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRUserServlet?action=show&amp;id={$user}">Nutzerdaten anzeigen</a>
      </li>
    </ul>
  </xsl:template>

</xsl:stylesheet>
