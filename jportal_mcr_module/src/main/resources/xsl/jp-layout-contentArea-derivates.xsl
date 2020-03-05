<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:iview2="xalan://org.mycore.iview2.frontend.MCRIView2XSLFunctions"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:mcrurn="xalan://fsu.jportal.xml.LayoutTools"
                xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
                xmlns:derivAccess="xalan://fsu.jportal.access.DerivateAccess"
                xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
                xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
                exclude-result-prefixes="xlink xalan iview2 mcrxml mcrurn acl derivAccess layoutTools jpxml">

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="JP.Site.Logo.Proxy.url"/>

  <xsl:template mode="derivateDisplay" match="/mycoreobject">
    <xsl:param name="mode" select="'metadata'"/>
    <xsl:param name="editable" select="'true'"/>
    <xsl:param name="query"/>

    <xsl:variable name="access" select="derivAccess:checkPermission(@ID)"/>
    <xsl:variable name="derivateLink" select="metadata/derivateLinks/derivateLink"/>
    <xsl:variable name="derivate" select="structure/derobjects/derobject"/>

    <xsl:choose>
      <xsl:when test="$access and ($derivateLink or $derivate)">
        <div class="jp-layout-derivateList">
          <xsl:apply-templates select="metadata/derivateLinks/derivateLink" mode="derivateDisplay">
            <xsl:with-param name="mode" select="$mode"/>
            <xsl:with-param name="editable" select="$editable"/>
            <xsl:with-param name="query" select="$query"/>
          </xsl:apply-templates>

          <xsl:apply-templates select="structure/derobjects/derobject" mode="derivateDisplay">
            <xsl:with-param name="mode" select="$mode"/>
            <xsl:with-param name="editable" select="$editable"/>
            <xsl:with-param name="query" select="$query"/>
          </xsl:apply-templates>
        </div>
      </xsl:when>
      <xsl:when test="contains(@ID, '_jpjournal_')">
        <xsl:variable name="journalImage" select="layoutTools:getJournalIconURL(@ID)"/>
        <xsl:if test="$journalImage != ''">
          <a href="{$WebApplicationBaseURL}receive/{@ID}?&amp;q={$query}" class="thumbnail">
            <img class="jp-journal-thumbnail" src="{$journalImage}"/>
          </a>
        </xsl:if>
      </xsl:when>
      <xsl:when test="contains(@ID, '_person_')">
        <a href="{$WebApplicationBaseURL}receive/{@ID}?&amp;q={$query}" class="thumbnail">
          <img class="jp-journal-thumbnail" src="{$WebApplicationBaseURL}{$JP.Site.Logo.Proxy.url}journal/Personen.svg"/>
        </a>
      </xsl:when>
      <xsl:when test="contains(@ID, '_jpinst_')">
        <a href="{$WebApplicationBaseURL}receive/{@ID}?&amp;q={$query}" class="thumbnail">
          <img class="jp-journal-thumbnail" src="{$WebApplicationBaseURL}{$JP.Site.Logo.Proxy.url}journal/Institutionen.svg"/>
        </a>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="derivateDisplay" match="derivateLink">
    <xsl:param name="editable"/>
    <xsl:param name="mode"/>
    <xsl:param name="query"/>

    <xsl:variable name="objID" select="/mycoreobject/@ID"/>
    <xsl:variable name="derivID" select="substring-before(@xlink:href, '/')"/>
    <xsl:variable name="file" select="substring-after(@xlink:href, '/')"/>
    <xsl:variable name="deleteLink" select="acl:checkPermission('default', 'update-jparticle')"/>

    <xsl:if test="$deleteLink or layoutTools:getDerivateDisplay($derivID) = 'true'">
      <div class="jp-layout-derivate">
        <xsl:call-template name="iview2Entry">
          <xsl:with-param name="ID" select="$objID"/>
          <xsl:with-param name="derivID" select="$derivID"/>
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="query" select="$query"/>
        </xsl:call-template>
        <xsl:if test="$mode = 'metadata' and $editable = 'true' and $deleteLink">
          <div class="objectEditingButton unlinkImage" data-object="{$objID}" data-image="{@xlink:href}">
            <xsl:value-of select="'Verlinkung löschen'"/>
          </div>
        </xsl:if>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="derivateDisplay" match="derobject">
    <xsl:param name="editable"/>
    <xsl:param name="mode"/>
    <xsl:param name="query"/>

    <xsl:variable name="derivate" select="document(concat('mcrobject:', @xlink:href))/mycorederivate"/>
    <xsl:variable name="iviewFile" select="iview2:getSupportedMainFile(@xlink:href)"/>
    <xsl:variable name="objID" select="/mycoreobject/@ID"/>
    <xsl:variable name="updateDerivate" select="acl:checkPermission(@xlink:href, 'update-derivate')"/>
    <xsl:variable name="showDerivate" select="layoutTools:getDerivateDisplay(@xlink:href) = 'true'"/>

    <xsl:if test="$updateDerivate or $showDerivate">
      <div class="jp-layout-derivate">
        <xsl:choose>
          <xsl:when test="$iviewFile != ''">
            <xsl:call-template name="iview2Entry">
              <xsl:with-param name="ID" select="$objID"/>
              <xsl:with-param name="derivID" select="@xlink:href"/>
              <xsl:with-param name="file" select="mcrxml:encodeURIPath($iviewFile)"/>
              <xsl:with-param name="query" select="$query"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="derivEntry">
              <xsl:with-param name="derivate" select="$derivate"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$mode = 'metadata' and $editable = 'true' and not(mcrxml:isCurrentUserGuestUser())">
          <ul class="edit">
            <li>
              <a href="{$WebApplicationBaseURL}rsc/derivatebrowser/compact#/{@xlink:href}/">Details</a>
            </li>
            <xsl:if test="acl:checkPermission(@xlink:href, 'update-derivate')">
              <xsl:choose>
                <xsl:when test="not(mcrurn:hasURNAssigned(@xlink:href))">
                  <li>
                    <a href="#" data-toggle="modal" data-target="#generateURNDialog">URN vergeben</a>
                  </li>
                  <div class="modal fade" id="generateURNDialog" tabindex="-1" role="dialog" data-backdrop="static"
                       data-id="{@xlink:href}">
                    <div class="modal-dialog">
                      <div class="modal-content">
                        <div class="modal-header">
                          <h4 class="modal-title">URN vergeben</h4>
                        </div>
                        <div class="modal-body">
                          <div class="row">
                            <div class="col-md-2" id="generateURNDialogIcon" style="text-align: center;">
                              <i class='fas fa-3x fa-question-circle'></i>
                            </div>
                            <div class="col-md-10" id="generateURNDialogContent">
                              Sind Sie sich sicher das Sie URN vergeben möchten. Diese wäre permanent und kann nicht
                              gelöscht werden.
                            </div>
                          </div>
                        </div>
                        <div class="modal-footer">
                          <button type="button" class="btn btn-default" data-dismiss="modal">Schließen</button>
                          <button type="button" class="btn btn-primary" id="generateURNDialogStart">URN Vergeben
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </xsl:when>
                <xsl:otherwise>
                  <div class="modal fade" id="updateURNDialog" tabindex="-1" role="dialog" data-backdrop="static"
                       data-id="{@xlink:href}">
                    <div class="modal-dialog">
                      <div class="modal-content">
                        <div class="modal-header">
                          <h4 class="modal-title">URN vergeben</h4>
                        </div>
                        <div class="modal-body">
                          <div class="row">
                            <div class="col-md-2" id="updateURNDialogIcon" style="text-align: center;">
                              <i class='fas fa-3x fa-question-circle'></i>
                            </div>
                            <div class="col-md-10" id="updateURNDialogContent">
                              Sind Sie sich sicher das Sie URL der URN aktualisieren möchten. Diese wäre permanent,
                              die alte URL kann nicht wiederhergestellt werden.
                            </div>
                          </div>
                        </div>
                        <div class="modal-footer">
                          <button type="button" class="btn btn-default" data-dismiss="modal">Schließen</button>
                          <button type="button" class="btn btn-primary" id="updateURNDialogStart">Aktualisieren</button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <a href="#" data-toggle="modal" data-target="#updateURNDialog">URL der URN aktualisieren</a>
                </xsl:otherwise>
              </xsl:choose>
              <li>
                <a href="javascript:window.open(&quot;{$WebApplicationBaseURL}rsc/mets/editor/start/{@xlink:href}&quot;,&quot;Mets Editor&quot;,&quot;scrollbars=no,location=no&quot;);void(0);">
                  Mets Editor
                </a>
              </li>
            </xsl:if>
            <xsl:if test="jpxml:isMetsGeneratable(@xlink:href)">
              <li>
                <a href="#" data-toggle="modal" data-target="#generateMetsDialog">METS generieren</a>
              </li>
              <div class="modal fade" id="generateMetsDialog" tabindex="-1" role="dialog" data-backdrop="static"
                   data-id="{@xlink:href}">
                <div class="modal-dialog">
                  <div class="modal-content">
                    <div class="modal-header">
                      <h4 class="modal-title">METS generieren</h4>
                    </div>
                    <div class="modal-body">
                      <div class="row">
                        <div class="col-md-2" id="generateMetsDialogIcon" style="text-align: center;">
                          <i class='fas fa-3x fa-question-circle'></i>
                        </div>
                        <div class="col-md-10" id="generateMetsDialogContent">
                          Sind Sie sich sicher das Sie die aktuelle mets.xml überschreiben wollen?
                        </div>
                      </div>
                    </div>
                    <div class="modal-footer">
                      <button type="button" class="btn btn-default" data-dismiss="modal">Schließen</button>
                      <button type="button" class="btn btn-primary" id="generateMetsDialogStart">Generieren</button>
                    </div>
                  </div>
                </div>
              </div>
            </xsl:if>
            <xsl:if test="jpxml:isMetsImportable(@xlink:href)">
              <li>
                <a href="#" data-toggle="modal" data-target="#importMetsDialog">METS Import</a>
              </li>
              <div class="modal fade" id="importMetsDialog" tabindex="-1" role="dialog" data-backdrop="static"
                   data-id="{@xlink:href}">
                <div class="modal-dialog">
                  <div class="modal-content">
                    <div class="modal-header">
                      <h4 class="modal-title">METS Import</h4>
                    </div>
                    <div class="modal-body">
                      <div class="row">
                        <div class="col-md-2" id="importMetsDialogIcon">
                          <i class='fas fa-3x fa-circle-notch fa-spin'></i>
                        </div>
                        <div class="col-md-10" id="importMetsDialogContent">
                          Derivat wird überprüft. Bitte warten...
                        </div>
                      </div>
                    </div>
                    <div class="modal-footer">
                      <button type="button" class="btn btn-default" data-dismiss="modal" id="importMetsDialogClose">
                        Schließen
                      </button>
                      <button type="button" class="btn btn-primary" disabled="disabled" id="importMetsDialogStart">
                        Importvorgang starten
                      </button>
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

  <xsl:template name="iview2Entry">
    <xsl:param name="ID"/>
    <xsl:param name="derivID"/>
    <xsl:param name="file"/>
    <xsl:param name="query"/>

    <xsl:variable name="href">
      <xsl:value-of select="concat($WebApplicationBaseURL, 'rsc/viewer/', $derivID, '/', $file)"/>
      <xsl:value-of select="concat('?logicalDiv=', $ID)"/>
      <xsl:if test="$query != ''">
        <xsl:value-of select="concat('&amp;q=', $query)"/>
      </xsl:if>
    </xsl:variable>
    <a href="{$href}" class="jp-thumbnail thumbnail"
       data-img="{$WebApplicationBaseURL}servlets/MCRTileCombineServlet/MIN/{$derivID}/{$file}?centerThumb=no">
      <div class="jp-layout-hidden-Button"/>
      <i class='fas fa-circle-notch fa-spin jp-thumbnail-spinner'/>
    </a>
  </xsl:template>

  <xsl:template name="derivEntry">
    <xsl:param name="derivate"/>
    <xsl:variable name="derivID" select="$derivate/@ID"/>
    <xsl:variable name="maindoc" select="$derivate/derivate/internals/internal/@maindoc"/>
    <xsl:variable name="encodedMaindoc" select="mcrxml:encodeURIPath($maindoc)"/>
    <xsl:variable name="derivbase" select="concat($WebApplicationBaseURL,'servlets/MCRFileNodeServlet/',$derivID,'/')"/>
    <xsl:variable name="fileType">
      <xsl:call-template name="uppercase">
        <xsl:with-param name="string" select="mcrxml:regexp($maindoc, '.*\.', '')"/>
      </xsl:call-template>
    </xsl:variable>
    <a href="{$derivbase}{$encodedMaindoc}" class="jp-thumbnail thumbnail">
      <xsl:attribute name="data-img">
        <xsl:choose>
          <xsl:when test="$fileType = 'PDF'">
            <xsl:value-of select="concat($WebApplicationBaseURL, 'img/pdfthumb/', $derivID, '/', $encodedMaindoc)"/>
          </xsl:when>
          <xsl:when test="$fileType = 'XML'">
            <xsl:value-of select="concat($WebApplicationBaseURL, 'images/xml-logo.svg')"/>
          </xsl:when>
          <xsl:when test="$fileType = 'SVG'">
            <xsl:value-of select="concat($derivbase, $encodedMaindoc)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat($WebApplicationBaseURL, 'images/file-logo.svg')"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <div class="jp-layout-hidden-Button"/>
      <i class='fas fa-circle-notch fa-spin jp-thumbnail-spinner'/>
    </a>
  </xsl:template>

</xsl:stylesheet>
