<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:derivateLinkUtil="xalan://org.mycore.frontend.util.DerivateLinkUtil" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:solrxml="xalan://org.mycore.solr.common.xml.MCRSolrXMLFunctions" exclude-result-prefixes="i18n derivateLinkUtil xlink acl mcrxml xalan xsi solrxml">

  <xsl:variable name="bookmarkedImage" select="derivateLinkUtil:getBookmarkedImage()" />
  <xsl:variable name="linkExist" select="/mycoreobject/metadata/derivateLinks/derivateLink[@xlink:href = $bookmarkedImage]" />
  <xsl:variable name="hasSourceOfLink" select="/mycoreobject/structure/derobjects/derobject[@xlink:href = substring-before($bookmarkedImage,'/')]" />

  <xsl:variable name="menuVarXML">
    <var name="dataModel" value="{/mycoreobject/@xsi:noNamespaceSchemaLocation}" />
    <var name="createJournal" value="{acl:checkPermission('POOLPRIVILEGE', 'create-jpjournal')}" />
    <var name="createPerson" value="{acl:checkPermission('POOLPRIVILEGE', 'create-person')}" />
    <var name="createInst" value="{acl:checkPermission('POOLPRIVILEGE', 'create-jpinst')}" />
    <var name="createVol" value="{acl:checkPermission('POOLPRIVILEGE', 'create-jpvolume')}" />
    <var name="createArt" value="{acl:checkPermission('POOLPRIVILEGE', 'create-jparticle')}" />
    <var name="deleteDeriv" value="{acl:checkPermission('default_derivate', 'deletedb')}" />
    <var name="currentType" value="{$currentType}" />
    <var name="currentObjID" value="{$currentObjID}" />
    <var name="updatePerm" value="{$updatePerm}" />
    <var name="deletePerm" value="{$deletePerm}" />
    <var name="isGuest" value="{mcrxml:isCurrentUserGuestUser()}" />
    <var name="linkImgAllowed" value="{$bookmarkedImage != '' and not($linkExist) and not($hasSourceOfLink)}" />
    <var name="notJournal" value="{/mycoreobject/@xsi:noNamespaceSchemaLocation != 'datamodel-jpjournal.xsd'}" />
    <var name="hasChildren" value="{count(/mycoreobject/structure/children) &gt; 0}" />
  </xsl:variable>
  <xsl:variable name="menuVar" select="xalan:nodeset($menuVarXML)" />

  <xsl:variable name="menuXML">
    <menu id="jp-main-menu" journalid="{$currentObjID}">
      <link id="editorServlet" href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}" />
      
      <link id="newEditorResource" href="{$WebApplicationBaseURL}editor" />
      
      <link id="moveObjResource" href="{$WebApplicationBaseURL}rsc/moveObj" />
      <item class="jp-layout-menu-dropdown">
        <!-- <label name="Bearbeiten" /> -->
        <restriction name="updatePerm" value="true" />
        <restriction name="dataModel" contains="datamodel-" />
        <item>
          <label name="Dokument bearbeiten" href="{$WebApplicationBaseURL}editor/start.xed?id={$currentObjID}&amp;type={$currentType}&amp;action=update" />
        </item>
        <item>
          <restriction name="hasChildren" value="true" />
          <restriction name="createJournal" value="true" />
          <label name="Kinder verschieben" ref="moveObjResource" path="start?objId={$currentObjID}" />
        </item>
        <item id="ckeditorButton" class="objectEditingButton">
          <restriction name="dataModel" value="datamodel-jpjournal.xsd" />
          <label name="Beschreibung bearbeiten" />
        </item>
        <item id="diagButton" class="objectEditingButton">
          <restriction name="dataModel" value="datamodel-jpjournal.xsd" />
          <label name="Rubrik bearbeiten" />
        </item>
        <item id="imprintButton" class="objectEditingButton jp-infoFiles-button" type="imprint" journalid="{$currentObjID}" containerid="jp-content-LColumn">
          <restriction name="dataModel" value="datamodel-jpjournal.xsd" />
          <label name="Impressum auswählen" />
        </item>
        <item id="partnerButton" class="objectEditingButton jp-infoFiles-button" type="partner" journalid="{$currentObjID}" containerid="jp-content-LColumn">
          <restriction name="dataModel" value="datamodel-jpjournal.xsd" />
          <label name="Partner auswählen" />
        </item>
        <item>
          <restriction name="dataModel" value="datamodel-jpjournal.xsd datamodel-jpvolume.xsd datamodel-jparticle.xsd" />
          <label name="Datei hochladen" href="{$WebApplicationBaseURL}servlets/derivate/create?id={$currentObjID}" />
        </item>
      </item>
      <item class="jp-layout-menu-dropdown">
        <item>
          <label name="Neue Person" ref="newEditorResource" path="start.xed?type=person&amp;action=create" />
          <restriction name="createPerson" value="true" />
        </item>
        <item>
          <label name="Neue Institution" ref="newEditorResource" path="start.xed?type=jpinst&amp;action=create" />
          <restriction name="createInst" value="true" />
        </item>
        <item>
          <label name="Person/Institution importieren" href="{$WebApplicationBaseURL}jp-import-sru.xml" />
          <restriction name="createPerson" value="true" />
          <restriction name="createInst" value="true" />
        </item>
        <item>
          <label name="Neue Zeitschrift" ref="newEditorResource" path="start.xed?type=jpjournal&amp;action=create" />
          <restriction name="createJournal" value="true" />
        </item>
        <item>
          <label name="Neuer Band" ref="newEditorResource" path="start.xed?type=jpvolume&amp;action=create&amp;parent={$currentObjID}" />
          <restriction name="createVol" value="true" />
          <restriction name="dataModel" value="datamodel-jpjournal.xsd datamodel-jpvolume.xsd" />
        </item>
        <item>
         <label name="Neuer Artikel" ref="newEditorResource" path="start.xed?type=jparticle&amp;action=create&amp;parent={$currentObjID}" />
          <restriction name="createArt" value="true" />
          <restriction name="dataModel" value="datamodel-jpvolume.xsd" />
        </item>
      </item>
      <item class="jp-layout-menu-dropdown">
        <restriction name="updatePerm" value="true" />
        <restriction name="createJournal" value="true" />
        <item>
          <label name="Dublettenfinder" href="{$WebApplicationBaseURL}rsc/doublets" />
        </item>
      </item>
      <item class="jp-layout-menu-dropdown">
        <restriction name="updatePerm" value="true" />
        <restriction name="linkImgAllowed" value="true" />
        <restriction name="dataModel" contains="datamodel-" />
        <item class="objectEditingButton" id="linkImage" data-object="{$currentObjID}">
          <label name="Bild verlinken" />
        </item>
      </item>
      <item class="jp-layout-menu-dropdown">
        <restriction name="deletePerm" value="true" />
        <restriction name="dataModel" contains="datamodel-" />
        <item class="objectEditingButton" id="deleteDocButton">
          <label name="Dokument löschen" />
        </item>
      </item>
      <item class="jp-layout-menu-dropdown">
        <restriction name="isGuest" value="false" />
        <restriction name="dataModel" contains="datamodel-" />
        <item>
          <label name="XML" href="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}?XSL.Style=xml" />
        </item>
        <item>
          <label name="Versionsgeschichte" href="{$WebApplicationBaseURL}jp-history.xml?XSL.id={/mycoreobject/@ID}" />
        </item>
      </item>
    </menu>
  </xsl:variable>
  <xsl:variable name="menu" select="xalan:nodeset($menuXML)/menu" />

  <xsl:template name="classificationEditorDiag">
    <xsl:variable name="journalRecourceURL" select="concat($classeditor.resourceURL,'jp/',$journalID,'/')" />

    <xsl:call-template name="classeditor.loadSettings">
      <xsl:with-param name="classeditor.class" select="@classId" />
      <xsl:with-param name="classeditor.categ" select="@categId" />
      <xsl:with-param name="classeditor.showId" select="@showId='true'" />
    </xsl:call-template>
    <xsl:call-template name="classeditor.includeDojoJS" />
    <xsl:call-template name="classeditor.includeJS" />

    <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-classificationEditor.js"></script>

    <script type="text/javascript">
      $(document).ready(function() {
        $("#diagButton").click(function() {
          classeditor.settings.resourceURL = '<xsl:value-of select="$journalRecourceURL" />';
          classeditor.classId = "list";
          classeditor.categoryId = "";
          startClassificationEditor();
        });
      });
    </script>
  </xsl:template>

  <xsl:template name="introEditorDiag">
    <script type="text/javascript">
      $(document).ready(function() {
        $("#ckeditorButton").click(function() {
          introEditor('<xsl:value-of select="$currentObjID" />');
        });
      });
    </script>
  </xsl:template>

  <xsl:template name="initImprint">
    <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-imprint.js" />
    <script type="text/javascript">
      $(document).ready(function() {
      <!-- initFS({ baseURL: '<xsl:value-of select="$WebApplicationBaseURL" />', type: 'imprint', journalID: '<xsl:value-of select="$currentObjID"/>', container: 
        '#jp-content-LColumn', button: '#imprintButton' }); -->
      });
    </script>
  </xsl:template>

  <xsl:template name="objectEditing">
    <xsl:param name="id" />
    <xsl:param name="dataModel" />

    <menu id="jp-object-editing" class="jp-layout-object-editing">
      <xsl:apply-templates mode="menuItem" select="$menu/item" />
      <xsl:if test="/mycoreobject[contains(@ID,'_jpjournal_')]">
        <script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/ckeditor/4.0.1/ckeditor.js" />
        <script type="text/javascript" src="{$WebApplicationBaseURL}ckeditor/adapters/jquery.js" />
        <xsl:call-template name="classificationEditorDiag" />
        <xsl:call-template name="introEditorDiag" />
        <xsl:call-template name="initImprint" />
      </xsl:if>
    </menu>

    <delete>
      <xsl:if test="$deletePerm='true' and /mycoreobject">
        <script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-delete-dialog.js" />
        <xsl:variable name="title">
          <xsl:apply-templates select="." mode="jp.metadata.title" />
        </xsl:variable>
        <div class="modal fade" id="delete-dialog" tabindex="-1" role="dialog" aria-hidden="true" data-id="{/mycoreobject/@ID}" data-deletable="{not(/mycoreobject/structure/derobjects)}"
          data-referer="{$Referer}" data-parent="{/mycoreobject/structure/parents/parent/@xlink:href}" data-title="{$title}" data-backdrop="static">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h4 class="modal-title" id="delete-dialog-title"></h4>
              </div>
              <div class="modal-body" id="delete-dialog-body">
                <div class="row">
                  <div class="col-md-2 text-center" id="delete-dialog-image">
                  </div>
                  <div class="col-md-10" id="delete-dialog-info">
                  </div>
                </div>
              </div>
              <div class="modal-footer" id="delete-dialog-footer">
                <button type="button" class="btn btn-default" id="delete-dialog-close" data-dismiss="modal">Ok</button>
                <button type="button" class="btn btn-primary hidden" id="delete-dialog-submit">Löschen</button>
              </div>
            </div>
          </div>
        </div>
      </xsl:if>
    </delete>
  </xsl:template>

  <!-- MENU ##################################################################### -->
  <xsl:template mode="menuItem" match="item">
    <xsl:call-template name="createMenuItem" />
  </xsl:template>

  <xsl:template mode="menuItem" match="item[restriction]">
    <xsl:variable name="access">
      <xsl:apply-templates select="restriction" mode="menuItem" />
    </xsl:variable>
    <xsl:if test="not(contains($access, 'false'))">
      <xsl:call-template name="createMenuItem" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="restriction[@value]" mode="menuItem">
    <xsl:variable name="name" select="@name" />
    <xsl:variable name="permission" select="$menuVar/var[@name=$name]/@value" />
    <xsl:value-of select="$permission != '' and contains(@value, $permission)" />
  </xsl:template>

  <xsl:template match="restriction[@contains]" mode="menuItem">
    <xsl:variable name="name" select="@name" />
    <xsl:variable name="permission" select="$menuVar/var[@name=$name]/@value" />
    <xsl:value-of select="$permission != '' and contains($permission, @value)" />
  </xsl:template>

  <xsl:template name="createMenuItem">
    <li>
      <xsl:apply-templates mode="copyAttr" select="@*" />
      <xsl:apply-templates mode="menuLabel" select="label" />
      <xsl:apply-templates mode="subMenu" select="." />
    </li>
  </xsl:template>

  <xsl:template mode="copyAttr" match="@*">
    <xsl:variable name="attrName" select="name()" />
    <xsl:attribute name="{$attrName}">
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>

  <xsl:template mode="subMenu" match="item[item]">
    <menu class="jp-layout-submenu-dropdown">
      <xsl:apply-templates mode="menuItem" select="item" />
    </menu>
  </xsl:template>

  <xsl:template mode="menuLabel" match="label">
    <xsl:value-of select="@name" />
  </xsl:template>

  <xsl:template mode="menuLabel" match="label[@href]">
    <a>
      <xsl:copy-of select="@*" />
      <xsl:value-of select="@name" />
    </a>
  </xsl:template>

  <xsl:template mode="menuLabel" match="label[@ref]|link[@ref]">
    <a>
      <xsl:apply-templates mode="copyAttr" select="@*" />
      <xsl:attribute name="href">
          <xsl:apply-templates mode="menuLink" select="@ref" />
          <xsl:apply-templates mode="menuLink" select="@path" />
          <xsl:apply-templates mode="menuLinkParam" select="params" />
      </xsl:attribute>
      <xsl:value-of select="@name" />
    </a>
  </xsl:template>

  <xsl:template mode="menuLink" match="@ref">
    <xsl:variable name="ref" select="." />
    <xsl:value-of select="$menu/link[@id=$ref]/@href" />
  </xsl:template>

  <xsl:template mode="menuLink" match="@path">
    <xsl:value-of select="concat('/',.)" />
  </xsl:template>

  <xsl:template mode="menuLinkParam" match="params">
    <xsl:value-of select="'?'" />
    <xsl:apply-templates mode="menuLinkParam" select="param" />
  </xsl:template>

  <xsl:template mode="menuLinkParam" match="param">
    <xsl:apply-templates mode="menuLinkParamCreate" select="." />
    <xsl:if test="position() != last()">
      <xsl:value-of select="'&amp;'" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="menuLinkParamCreate" match="param[@ref]">
    <xsl:variable name="ref" select="@ref" />
    <xsl:apply-templates mode="menuLinkParam" select="$menu/params[@id=$ref]/param" />
  </xsl:template>

  <xsl:template mode="menuLinkParamCreate" match="param[@value]">
    <xsl:value-of select="concat(@name,'=',@value)" />
  </xsl:template>

  <xsl:template mode="menuLinkParamCreate" match="param[@select]">
    <xsl:variable name="selectID" select="@select" />
    <xsl:value-of select="concat(@name,'=', $menuVar/var[@name=$selectID]/@value)" />
  </xsl:template>
  <!-- END MENU ##################################################################### -->
</xsl:stylesheet>