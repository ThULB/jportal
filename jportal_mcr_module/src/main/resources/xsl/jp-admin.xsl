<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  exclude-result-prefixes="i18n mcrxml">

  <xsl:template match="jpadmin">
    <div class="jp-layout-admin">
      <h1>Administration</h1>
      <xsl:choose>
        <xsl:when test="mcrxml:isCurrentUserInRole('admin')">
          <xsl:call-template name="jp.admin.show" />
        </xsl:when>
        <xsl:otherwise>
          <p>Sie haben keine Berechtigung für diesen Bereich.</p>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template name="jp.admin.show">
    <h2>Editoren</h2>
    <ul>
      <li>
        <a href="{$WebApplicationBaseURL}rsc/ACLE/start">ACL-Editor</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}jp-classeditor.xml">Klassifikations Editor</a>
      </li>
    </ul>
    <h2>WebCLI</h2>
    <input type="button" value="Start" onclick="window.open('/modules/webcli/start.xml','','width=900,height=900,resizable,locationbar=false,menubar=false,personalbar=false,toolbar=false');"/>
    <h2>Benutzer</h2>
    <ul>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRUserServlet">Suchen und verwalten</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}authorization/new-user.xml?action=save">Nutzer anlegen</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}authorization/roles-editor.xml">Gruppen verwalten</a>
      </li>
    </ul>
    <h2>Sitzungen</h2>
    <ul>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRSessionListingServlet">Aktive Sitzungen</a>
      </li>
    </ul>
    <h2>Einstellungen</h2>
    <ul>
      <li>
        <a href="{$WebApplicationBaseURL}jp-globalmessage-editor.xml">Globale Nachricht Bearbeiten</a>
      </li>
    </ul>
  </xsl:template>
</xsl:stylesheet>
