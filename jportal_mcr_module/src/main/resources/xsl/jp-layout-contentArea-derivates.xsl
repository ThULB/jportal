<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:iview2="xalan://org.mycore.iview2.frontend.MCRIView2XSLFunctions"
  xmlns:mcr="http://www.mycore.org/" xmlns:mcrservlet="xalan://org.mycore.frontend.servlets.MCRServlet" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:mcrurn="xalan://org.mycore.urn.MCRXMLFunctions"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:derivAccess="xalan://fsu.jportal.access.DerivateAccess" xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
  xmlns:encoder="xalan://java.net.URLEncoder" xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions" exclude-result-prefixes="xlink iview2 mcr mcrservlet mcrxml jpxml mcrurn acl encoder">

  <xsl:template name="derivatePreview">
    <xsl:param name="mcrObj" />
    <xsl:variable name="objID" select="$mcrObj/@ID" />
    <xsl:variable name="journalID" select="$mcrObj/metadata/hidden_jpjournalsID/hidden_jpjournalID" />
    <xsl:variable name="published" select="$mcrObj/metadata/dates/date[@type='published' and @inherited=0]" />
    <xsl:choose>
      <xsl:when test="$mcrObj/metadata/derivateLinks/derivateLink[1]">
        <xsl:call-template name="derivateDisplay">
          <xsl:with-param name="nodes" select="$mcrObj/metadata/derivateLinks/derivateLink[1]" />
          <xsl:with-param name="objID" select="$journalID" />
          <xsl:with-param name="journalID" select="$journalID" />
          <xsl:with-param name="mode" select="'preview'" />
          <xsl:with-param name="published" select="$published" />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$mcrObj/structure/derobjects/derobject">
        <xsl:call-template name="derivateDisplay">
          <xsl:with-param name="nodes" select="$mcrObj/structure/derobjects/derobject[1]" />
          <xsl:with-param name="objID" select="$objID" />
          <xsl:with-param name="journalID" select="$journalID" />
          <xsl:with-param name="editable" select="'false'" />
          <xsl:with-param name="mode" select="'preview'" />
          <xsl:with-param name="published" select="$published" />
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="derivateDisplay">
    <xsl:param name="nodes" />
    <xsl:param name="objID" />
    <xsl:param name="journalID" />
    <xsl:param name="editable" select="'true'" />
    <xsl:param name="mode" select="'metadata'" />
    <xsl:param name="published" select="/mycoreobject/metadata/dates/date[@type='published' and @inherited=0]" />

    <xsl:if test="derivAccess:checkPermission($objID, $journalID, $published)">
      <xsl:if test="count($nodes) &gt; 0">
        <div class="jp-layout-derivateList">
          <xsl:apply-templates mode="derivateDisplay" select="$nodes">
            <xsl:with-param name="editable" select="$editable" />
            <xsl:with-param name="mode" select="$mode" />
          </xsl:apply-templates>
        </div>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="derivateDisplay" match="derivateLink">
    <xsl:param name="editable" select="'true'" />
    <xsl:param name="mode" select="'metadata'" />
    <xsl:variable name="objID" select="/mycoreobject/@ID" />
    <xsl:variable name="derivID" select="substring-before(@xlink:href, '/')" />
    <xsl:variable name="file" select="substring-after(@xlink:href, '/')" />
    <xsl:variable name="deleteLink" select="acl:checkPermission('default', 'update-jparticle')" />

    <xsl:if test="$deleteLink or layoutTools:getDerivateDisplay($derivID) = 'true'">
      <div class="jp-layout-derivate">
        <xsl:call-template name="iview2Entry">
          <xsl:with-param name="derivID" select="$derivID" />
          <xsl:with-param name="file" select="$file" />
        </xsl:call-template>
        <xsl:if test="$mode = 'metadata'">
          <xsl:call-template name="dfgViewerLink">
            <xsl:with-param name="derivID" select="$derivID" />
            <xsl:with-param name="file" select="$file" />
          </xsl:call-template>
        </xsl:if>
        <xsl:if test="$mode = 'metadata' and $editable = 'true' and $deleteLink">
          <div class="objectEditingButton unlinkImage" data-object="{$objID}" data-image="{@xlink:href}">
            <xsl:value-of select="'Verlinkung löschen'" />
          </div>
        </xsl:if>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="derivateDisplay" match="derobject">
    <xsl:param name="editable" select="'true'" />
    <xsl:param name="mode" select="'metadata'" />

    <xsl:variable name="derivate" select="document(concat('mcrobject:', @xlink:href))/mycorederivate" />
    <xsl:variable name="iviewFile" select="iview2:getSupportedMainFile(@xlink:href)" />
    <xsl:variable name="objID" select="/mycoreobject/@ID" />
    <xsl:variable name="deleteDB" select="acl:checkPermission(@xlink:href, 'deletedb')" />
    <xsl:variable name="showDerivate" select="layoutTools:getDerivateDisplay(@xlink:href) = 'true'" />

    <xsl:if test="$deleteDB or $showDerivate">
      <div class="jp-layout-derivate">
        <xsl:choose>
          <xsl:when test="$iviewFile != ''">
            <xsl:call-template name="iview2Entry">
              <xsl:with-param name="derivID" select="@xlink:href" />
              <xsl:with-param name="file" select="mcrxml:encodeURIPath($iviewFile)" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="derivEntry">
              <xsl:with-param name="derivate" select="$derivate" />
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$mode = 'metadata' and $iviewFile != ''">
          <xsl:call-template name="dfgViewerLink">
            <xsl:with-param name="derivID" select="@xlink:href" />
          </xsl:call-template>
        </xsl:if>
        <xsl:if test="$mode = 'metadata' and $editable = 'true' and not(mcrxml:isCurrentUserGuestUser())">
          <ul class="edit">
            <li>
              <a href="{$WebApplicationBaseURL}servlets/MCRFileNodeServlet/{@xlink:href}/">Details</a>
            </li>
            <li>
              <a href="{$WebApplicationBaseURL}servlets/derivate/update?id={@xlink:href}&amp;objectid={$objID}">Dateien hinzufügen</a>
            </li>
            <xsl:if test="acl:checkPermission(@xlink:href, 'update-derivate')">
              <xsl:if test="not(mcrurn:hasURNDefined(@xlink:href))">
                <li>
                  <a href="{$WebApplicationBaseURL}servlets/MCRAddURNToObjectServlet?object={@xlink:href}">URN vergeben</a>
                </li>
              </xsl:if>
              <li>
                <a href="javascript:window.open(&quot;{$WebApplicationBaseURL}rsc/mets/editor/start/{@xlink:href}&quot;,&quot;Mets Editor&quot;,&quot;scrollbars=no,location=no&quot;);void(0);">Mets Editor</a>
              </li>
            </xsl:if>
            <xsl:if test="$deleteDB">
              <li>
                <a href="{$WebApplicationBaseURL}servlets/MCRDisplayHideDerivateServlet?derivate={@xlink:href}">
                  <xsl:choose>
                    <xsl:when test="$showDerivate">
                      Derivat verstecken
                    </xsl:when>
                    <xsl:otherwise>
                      Derivat anzeigen
                    </xsl:otherwise>
                  </xsl:choose>
                </a>
              </li>
              <li>
                <a href='javascript:;' onclick="showDeleteDerivateDialog('{@xlink:href}');">Derivat löschen</a>
              </li>
            </xsl:if>
            <li>
              <a href="javascript:;" onclick="selectDerivateContext(this, '{@xlink:href}', '{$derivate/derivate/linkmetas/linkmeta/@xlink:role}');">Derivat Kontext</a>
            </li>
            <li>
              <a href="javascript:;" onclick="startProcessTIFF('{@xlink:href}');">TIFF konvertieren</a>
            </li>
            <xsl:if test="jpxml:isMetsImportable(@xlink:href)">
              <li>
                <a href="javascript:;" data-toggle="modal" data-target="#importMetsDialog">METS Import</a>
              </li>
              <div class="modal fade" id="importMetsDialog" tabindex="-1" role="dialog" data-backdrop="static" data-id="{@xlink:href}">
                <div class="modal-dialog">
                  <div class="modal-content">
                    <div class="modal-header">
                      <h4 class="modal-title">METS Import</h4>
                    </div>
                    <div class="modal-body">
                      <div class="row">
                        <div class="col-md-2" id="importMetsDialogIcon">
                          <i class='fa fa-3x fa-circle-o-notch fa-spin'></i>
                        </div>
                        <div class="col-md-10" id="importMetsDialogContent">
                          Derivat wird überprüft. Bitte warten...
                        </div>
                      </div>
                    </div>
                    <div class="modal-footer">
                      <button type="button" class="btn btn-default" data-dismiss="modal" id="importMetsDialogClose">Schließen</button>
                      <button type="button" class="btn btn-primary" disabled="disabled" id="importMetsDialogStart">Importvorgang starten</button>
                    </div>
                  </div>
                </div>
              </div>
            </xsl:if>
          </ul>
        </xsl:if>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="dfgViewerLink">
    <xsl:param name="derivID" />
    <xsl:param name="file" select="''" />
    <div class="dfg-viewer">
      <xsl:variable name="encodedURL">
        <xsl:variable name="url">
          <xsl:value-of select="concat($WebApplicationBaseURL, 'servlets/MCRMETSServlet/', $derivID, '/')" />
          <xsl:if test="$file != ''">
            <xsl:value-of select="$file" />
          </xsl:if>
          <xsl:value-of select="'?XSL.Style=dfg'" />
        </xsl:variable>
        <xsl:value-of select="encoder:encode($url)" />
      </xsl:variable>
      <a href="http://dfg-viewer.de/demo/viewer/?set[mets]={$encodedURL}" target="_blank">
        <xsl:value-of select="'alternativ im DFG-Viewer anzeigen'" />
      </a>
    </div>
  </xsl:template>

  <xsl:template name="iview2Entry">
    <xsl:param name="derivID" />
    <xsl:param name="file" />
    <a href="{$WebApplicationBaseURL}rsc/viewer/{$derivID}/{$file}" class="thumbnail">
      <div class="jp-layout-hidden-Button"></div>
      <img src="{$WebApplicationBaseURL}servlets/MCRTileCombineServlet/MIN/{$derivID}/{$file}?centerThumb=no" />
    </a>
  </xsl:template>

  <xsl:template name="derivEntry">
    <xsl:param name="derivate" />
    <xsl:variable name="derivID" select="$derivate/@ID" />
    <xsl:variable name="maindoc" select="$derivate/derivate/internals/internal/@maindoc" />
    <xsl:variable name="encodedMaindoc" select="jpxml:encodeURL($maindoc)" />
    <xsl:variable name="derivbase" select="concat($WebApplicationBaseURL,'servlets/MCRFileNodeServlet/',$derivID,'/')" />
    <xsl:variable name="fileType">
      <xsl:call-template name="uppercase">
        <xsl:with-param name="string" select="mcrxml:regexp($maindoc, '.*\.', '')" />
      </xsl:call-template>
    </xsl:variable>
    <a href="{$derivbase}{$encodedMaindoc}" class="thumbnail">
      <div class="jp-layout-hidden-Button"></div>
      <xsl:choose>
        <xsl:when test="$fileType = 'PDF'">
          <img src="{$WebApplicationBaseURL}img/pdfthumb/{$derivID}/{$encodedMaindoc}" />
        </xsl:when>
        <xsl:when test="$fileType = 'XML'">
          <img src="{$WebApplicationBaseURL}images/xml-logo.svg" />
        </xsl:when>
        <xsl:when test="$fileType = 'SVG'">
          <img src="{$derivbase}{$encodedMaindoc}" />
        </xsl:when>
        <xsl:otherwise>
          <img src="{$WebApplicationBaseURL}images/file-logo.svg" />
        </xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>

</xsl:stylesheet>