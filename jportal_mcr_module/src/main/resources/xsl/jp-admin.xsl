<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager" exclude-result-prefixes="i18n mcrxml acl">

  <xsl:template match="jpadmin">
    <div class="jp-layout-admin">
      <h1>Administration</h1>
      <xsl:choose>
        <xsl:when test="acl:checkPermission('administrate-jportal')">
          <xsl:call-template name="jp.admin.show" />
        </xsl:when>
        <xsl:otherwise>
          <p>Sie haben keine Berechtigung für diesen Bereich.</p>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template name="jp.admin.show">
    <xsl:variable name="isAdmin" select="mcrxml:isCurrentUserInRole('admin')" />
    <h2>Editoren</h2>
    <ul>
      <xsl:if test="$isAdmin">
        <li>
          <a href="{$WebApplicationBaseURL}rsc/ACLE/start">ACL-Editor</a>
        </li>
      </xsl:if>
      <li>
        <a href="{$WebApplicationBaseURL}jp-classeditor.xml">Klassifikations Editor</a>
      </li>
    </ul>
    <xsl:if test="$isAdmin">
      <h2>WebCLI</h2>
      <input type="button" value="Start" onclick="window.open(jp.baseURL + 'modules/webcli/start.xml','','width=900,height=900,resizable,locationbar=false,menubar=false,personalbar=false,toolbar=false');"/>
    </xsl:if>
    <h2>Benutzer</h2>
    <ul>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRUserServlet">Suchen und verwalten</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}authorization/new-user.xed?action=save">Nutzer anlegen</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}authorization/roles-editor.xml">Gruppen verwalten</a>
      </li>
    </ul>
    <xsl:if test="$isAdmin">
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
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
