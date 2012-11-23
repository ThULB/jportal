<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="mcrxml i18n">

  <xsl:template match="jpadmin">
    <h1>Administration</h1>
    <xsl:choose>
      <xsl:when test="mcrxml:isCurrentUserInRole('admin') or mcrxml:isCurrentUserInRole('admingroup')">
        <xsl:call-template name="jp.admin.show" />
      </xsl:when>
      <xsl:otherwise>
        <p>Sie haben keine Berechtigung für diesen Bereich.</p>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="jp.admin.show">
    <h2>Editoren</h2>
    <ul>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRACLEditorServlet_v2?mode=getACLEditor">ACL Editor</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}jp-classeditor.xml?XSL.classeditor.showId=true">Klassifikations Editor</a>
      </li>
    </ul>
    <h2>WebCLI</h2>
    <input type="button" value="Start" onclick="window.open('/modules/webcli/start.xml','','width=900,height=900,resizable,locationbar=false,menubar=false,personalbar=false,toolbar=false');"/>
    <h2>Benutzer</h2>
    <ul>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRUserServlet?url=/content/below/index.xml&amp;mode=CreatePwdDialog">Passwort ändern</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRUserServlet?url=/content/below/index.xml&amp;mode=ShowUser">Nutzerdaten anzeigen</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRUserAdminServlet?mode=newuser">Nutzer anlegen</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRUserAdminServlet?mode=newgroup">Gruppe anlegen</a>
      </li>
      <li>
        <a href="{$WebApplicationBaseURL}servlets/MCRUserAjaxServlet">Nutzer- Gruppenverwaltung</a>
      </li>
    </ul>    
  </xsl:template>
</xsl:stylesheet>
