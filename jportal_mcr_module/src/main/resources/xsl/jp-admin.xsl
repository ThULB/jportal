<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:acl="xalan://org.mycore.access.MCRAccessManager" exclude-result-prefixes="i18n mcrxml acl">

  <xsl:template match="jpadmin">
    <div class="jp-layout-admin">
      <h1>Administration</h1>
      <xsl:choose>
        <xsl:when test="acl:checkPermission('administrate-jportal')">
          <xsl:call-template name="jp.admin.show"/>
        </xsl:when>
        <xsl:otherwise>
          <p>Sie haben keine Berechtigung für diesen Bereich.</p>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template name="jp.admin.show">
    <xsl:variable name="isAdmin" select="mcrxml:isCurrentUserInRole('admin')"/>
    <div class="panel-primary col-sm-4">
      <div class="panel-heading">
        <h2 class="panel-title">Editoren</h2>
      </div>
      <div class="panel-body">
        <ul>
          <xsl:if test="$isAdmin">
            <li>
              <a href="{$WebApplicationBaseURL}rsc/ACLE/start">ACL-Editor</a>
            </li>
          </xsl:if>
          <li>
            <a href="{$WebApplicationBaseURL}jp-classeditor.xml">Klassifikations Editor</a>
          </li>
          <li>
            <a href="{$WebApplicationBaseURL}rsc/derivatebrowser/start?lang={i18n:getCurrentLocale()}">Objekt Browser</a>
          </li>
        </ul>
      </div>
    </div>
    <xsl:if test="$isAdmin">
      <div class="panel-primary col-sm-4">
        <div class="panel-heading">
          <h2 class="panel-title">WebCLI</h2>
        </div>
        <div class="panel-body">
          <input type="button" value="Start"
                 onclick="window.open(jp.baseURL + 'rsc/WebCLI/','','width=900,height=900,resizable,locationbar=false,menubar=false,personalbar=false,toolbar=false');"/>
        </div>
      </div>
    </xsl:if>
    <div class="panel-primary col-sm-4">
      <div class="panel-heading">
        <h2 class="panel-title">Benutzer</h2>
      </div>
      <div class="panel-body">
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
      </div>
    </div>
    <div class="panel-primary col-sm-4">
      <div class="panel-heading">
        <h2 class="panel-title">Sonstiges</h2>
      </div>
      <div class="panel-body">
        <ul>
          <xsl:if test="$isAdmin">
            <li>
              <a href="{$WebApplicationBaseURL}modules/webtools/session/sessionListing.xml">Aktive Sitzungen</a>
            </li>
            <li>
              <a href="{$WebApplicationBaseURL}modules/webtools/processing/processing.xml">Processing</a>
            </li>
          </xsl:if>
          <li>
            <a href="{$WebApplicationBaseURL}jp-errorMenu.xml">Systemfehler Übersicht</a>
          </li>
          <xsl:if test="acl:checkPermission('default', 'delete-doublets')">
            <li>
              <a href="{$WebApplicationBaseURL}rsc/doublets">Dublettenfinder</a>
            </li>
           </xsl:if>
        </ul>
      </div>
    </div>
    <xsl:if test="$isAdmin">
      <div class="panel-primary col-sm-4">
        <div class="panel-heading">
          <h2 class="panel-title">Einstellungen</h2>
        </div>
        <div class="panel-body">
          <ul>
            <li>
              <a href="{$WebApplicationBaseURL}jp-globalmessage-editor.xml">Globale Nachricht Bearbeiten</a>
            </li>
          </ul>
        </div>
      </div>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
