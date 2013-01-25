<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:iview2="xalan://org.mycore.iview2.frontend.MCRIView2XSLFunctions" xmlns:mcr="http://www.mycore.org/" xmlns:mcrservlet="xalan://org.mycore.frontend.servlets.MCRServlet"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:layoutTools="xalan://fsu.jportal.xsl.LayoutTools" xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="xlink iview2 mcr mcrservlet mcrxml acl encoder">

  <xsl:param name="iview2.debug" select="'false'" />
  <xsl:param name="CurrentLang" select="'de'"/>

  <xsl:template name="derivatePreview">
    <xsl:param name="mcrObj" />
    <xsl:variable name="journalID" select="$mcrObj//metadata/hidden_jpjournalsID/hidden_jpjournalID" />
    <xsl:choose>
      <xsl:when test="$mcrObj/metadata/derivateLinks/derivateLink[1]">
        <xsl:call-template name="derivateDisplay">
          <xsl:with-param name="nodes" select="$mcrObj/metadata/derivateLinks/derivateLink[1]" />
          <xsl:with-param name="journalID" select="$journalID" />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$mcrObj/structure/derobjects/derobject">
        <xsl:call-template name="derivateDisplay">
          <xsl:with-param name="nodes" select="$mcrObj/structure/derobjects/derobject[1]" />
          <xsl:with-param name="journalID" select="$journalID" />
          <xsl:with-param name="editable" select="'false'" />
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="derivateDisplay">
    <xsl:param name="nodes" />
    <xsl:param name="journalID" />
    <xsl:param name="editable" select="'true'" />
    <xsl:if test="acl:checkPermission($journalID,'read_derivate')">
      <xsl:if test="count($nodes) &gt; 0">
        <div class="jp-layout-derivateList">
          <xsl:apply-templates mode="derivateDisplay" select="$nodes">
            <xsl:with-param name="editable" select="$editable" />
          </xsl:apply-templates>
        </div>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="derivateDisplay" match="derivateLink">
    <xsl:param name="editable" select="'true'" />
    <xsl:variable name="objID" select="/mycoreobject/@ID" />
    <xsl:variable name="derivID" select="substring-before(@xlink:href, '/')" />
    <xsl:variable name="deleteLink" select="acl:checkPermission($derivID, 'delete_derlink')" />

    <xsl:if test="$deleteLink or layoutTools:getDerivateDisplay($derivID) = 'true'">
      <div class="jp-layout-derivateWrapper">
        <div class="image">
          <xsl:call-template name="iview2Entry">
            <xsl:with-param name="derivID" select="substring-before(@xlink:href, '/')" />
            <xsl:with-param name="file" select="concat('/', substring-after(@xlink:href, '/'))" />
          </xsl:call-template>
        </div>
        <xsl:if test="$editable = 'true' and $deleteLink">
          <ul class="edit">
            <li>
              <a href="{$WebApplicationBaseURL}servlets/DerivateLinkServlet?mode=removeLink&amp;from={$objID}&amp;to={@xlink:href}">
                <xsl:value-of select="'Verlinkung löschen'" />
              </a>
            </li>
          </ul>
        </xsl:if>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="derivateDisplay" match="derobject">
    <xsl:param name="editable" select="'true'" />
    <xsl:variable name="iviewFile" select="iview2:getSupportedMainFile(@xlink:href)" />
    <xsl:variable name="objID" select="/mycoreobject/@ID" />
    <xsl:variable name="derivID" select="@xlink:href" />
    <xsl:variable name="deleteDB" select="acl:checkPermission(@xlink:href, 'deletedb')" />
    <xsl:variable name="showDerivate" select="layoutTools:getDerivateDisplay($derivID) = 'true'" />

    <xsl:if test="$deleteDB or $showDerivate">
    <div class="jp-layout-derivateWrapper">
      <div class="image">
        <xsl:choose>
          <xsl:when test="$iviewFile != ''">
            <xsl:call-template name="iview2Entry">
              <xsl:with-param name="derivID" select="@xlink:href" />
              <xsl:with-param name="file" select="$iviewFile" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="derivEntry">
              <xsl:with-param name="derivID" select="@xlink:href" />
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </div>
      <xsl:if test="$editable = 'true' and not(mcrxml:isCurrentUserGuestUser())">
        <ul class="edit">
          <li>
            <a href="{$WebApplicationBaseURL}servlets/MCRFileNodeServlet/{@xlink:href}">Details</a>
          </li>
          <li>
            <a
              href="{$WebApplicationBaseURL}servlets/MCRStartEditorServlet?se_mcrid={@xlink:href}&amp;te_mcrid={@xlink:href}&amp;re_mcrid={$objID}&amp;todo=saddfile">Dateien hinzufügen</a>
          </li>
          <xsl:if test="acl:checkPermission(@xlink:href, 'update_derivate')">
            <xsl:if test="not(mcrxml:hasURNDefined(@xlink:href))">
              <li>
                <a href="{$WebApplicationBaseURL}servlets/MCRAddURNToObjectServlet?object={@xlink:href}">URN vergeben</a>
              </li>
            </xsl:if>
            <li>
              <a href="{$WebApplicationBaseURL}metseditor/start_mets_editor.xml?derivate={@xlink:href}&amp;useExistingMets=true">Mets Editor</a>
            </li>
          </xsl:if>
          <xsl:if test="$deleteDB">
            <li>
              <a href="{$WebApplicationBaseURL}servlets/MCRDisplayHideDerivateServlet?derivate={@xlink:href}">
                <xsl:choose>
                  <xsl:when test="$showDerivate">Derivat verstecken</xsl:when>
                  <xsl:otherwise>Derivat anzeigen</xsl:otherwise>
                </xsl:choose>
              </a>
            </li>
            <li>
              <a href='javascript:;' onclick="showDeleteDerivateDialog('{@xlink:href}');">Derivat löschen</a>
            </li>
          </xsl:if>
        </ul>
      </xsl:if>
    </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="iview2Entry">
    <xsl:param name="derivID" />
    <xsl:param name="file" />
    <div class="jp-layout-hidden-Button"></div>
    <img src="{concat($WebApplicationBaseURL,'servlets/MCRThumbnailServlet/',$derivID, $file,'?centerThumb=no')}" />
    <script type="text/javascript">
      $(document).ready(function() {
        jpAddDefaultOptions({
          id: '<xsl:value-of select="$derivID"/>',
          options: <xsl:value-of select="iview2:getOptions($derivID, '')" />
        });
      });
    </script>
  </xsl:template>

  <xsl:template name="derivEntry">
    <xsl:param name="derivID" />
    <xsl:variable name="derivate" select="document(concat('mcrobject:', $derivID))/mycorederivate" />
    <xsl:variable name="maindoc" select="$derivate/derivate/internals/internal/@maindoc" />
    <xsl:variable name="encodedMaindoc" select="mcrservlet:encodeURL($maindoc)" />
    <xsl:variable name="derivbase" select="concat($WebApplicationBaseURL,'servlets/MCRFileNodeServlet/',$derivID,'/')" />
    <xsl:variable name="fileType">
      <xsl:call-template name="uppercase">
        <xsl:with-param name="string" select="mcrxml:regexp($maindoc, '.*\.', '')" />
      </xsl:call-template>
    </xsl:variable>
    <a href="{$derivbase}{$encodedMaindoc}">
      <div class="jp-layout-hidden-Button"></div>
      <xsl:choose>
        <xsl:when test="$fileType = 'PDF'">
          <img src="{concat($WebApplicationBaseURL,'images/adobe-logo.svg')}" border="0" class="logo" />  
        </xsl:when>
        <xsl:when test="$fileType = 'XML'">
          <img src="{concat($WebApplicationBaseURL,'images/xml-logo.svg')}" border="0" class="logo" />  
        </xsl:when>
        <xsl:otherwise>
          <img src="{concat($WebApplicationBaseURL,'images/file-logo.svg')}" border="0" class="logo" />
          <span style="display: inline-block; text-align: center; width: 100%;">
            <xsl:value-of select="$fileType" />
          </span>
        </xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>

  <xsl:template name="initIview2JS">
    <xsl:choose>
      <xsl:when test="$iview2.debug ='true'">
        <script type="text/javascript" src="{$WebApplicationBaseURL}modules/iview2/js/iview2.js" />
      </xsl:when>
      <xsl:otherwise>
        <script type="text/javascript" src="{$WebApplicationBaseURL}modules/iview2/js/iview2.min.js" />
      </xsl:otherwise>
    </xsl:choose>
    <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-iview2.js" />
    <script type="text/javascript">
      $(document).ready(function() {
        jpInitIview2({
          lang: '<xsl:value-of select="$CurrentLang"/>'
        });
      });
    </script>
  </xsl:template>
</xsl:stylesheet>