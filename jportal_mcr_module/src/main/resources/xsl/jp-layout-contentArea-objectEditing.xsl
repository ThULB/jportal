<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:derivateLinkUtil="xalan://org.mycore.frontend.util.DerivateLinkUtil" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  <xsl:variable name="dataModel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
  <xsl:variable name="createJournal" select="acl:checkPermission('CRUD','create_journal')" />
  <xsl:variable name="isAdmin" select="acl:checkPermission('POOLPRIVILEGE','administrate-user')" />
  <xsl:variable name="bookmarkedImage" select="derivateLinkUtil:getBookmarkedImage()" />
  <xsl:variable name="linkExist" select="/mycoreobject/metadata/derivateLinks/derivateLink[@xlink:href = $bookmarkedImage]" />
  <xsl:variable name="hasSourceOfLink" select="/mycoreobject/structure/derobjects/derobject[@xlink:href = substring-before($bookmarkedImage,'/')]" />
  <xsl:variable name="linkImgAllowed" select="$bookmarkedImage != '' and not($linkExist) and not($hasSourceOfLink)" />

  <xsl:variable name="menuXML">
    <menu>
      <var name="dataModel" value="{$dataModel}" />
      <var name="currentType" value="{$currentType}" />
      <var name="currentObjID" value="{$currentObjID}" />
      <var name="updatePerm" value="{$updatePerm}" />
      <var name="deletePerm" value="{$deletePerm}" />
      <var name="createJournal" value="{$createJournal}" />
      <var name="isAdmin" value="{$isAdmin}" />
      <var name="linkImgAllowed" value="{$linkImgAllowed}" />
      <link id="editorServlet" href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}" />
      <link id="linkImgUrl" href="{$ServletsBaseURL}DerivateLinkServlet?mode=setLink&amp;from={$currentObjID}" />
      <params id="editorServlet-editParam">
        <param name="tf_mcrid" select="currentObjID" />
        <param name="re_mcrid" select="currentObjID" />
        <param name="se_mcrid" select="currentObjID" />
        <param name="type" select="currentType" />
        <param name="step" value="commit" />
      </params>
      <item class="jp-layout-menu-dropdown">
        <!-- <label name="Bearbeiten" /> -->
        <restriction name="updatePerm" value="true" />
        <item>
          <label name="Dokument bearbeiten" ref="editorServlet">
            <params>
              <param ref="editorServlet-editParam" />
              <param name="todo" value="seditobj" />
            </params>
          </label>
        </item>
        <item id="ckeditorButton">
          <restriction name="dataModel" value="datamodel-jpjournal.xsd" />
          <label name="Beschreibung bearbeiten" />
        </item>
        <item>
          <label name="Datei hochladen" ref="editorServlet">
            <params>
              <param ref="editorServlet-editParam" />
              <param name="todo" value="snewder" />
            </params>
          </label>
        </item>
        <item>
          <label name="Neue Person" ref="editorServlet">
            <params>
              <param name="type" value="person" />
              <param name="step" value="author" />
              <param name="todo" value="wnewobj" />
            </params>
          </label>
        </item>
        <item>
          <label name="Neue Institution" ref="editorServlet">
            <params>
              <param name="type" value="jpinst" />
              <param name="step" value="author" />
              <param name="todo" value="wnewobj" />
            </params>
          </label>
        </item>
        <item>
          <label name="Neue Zeitschrift" ref="editorServlet">
            <params>
              <param name="type" value="jpjournal" />
              <param name="step" value="author" />
              <param name="todo" value="wnewobj" />
            </params>
          </label>
          <restriction name="createJournal" value="true" />
        </item>
        <item>
          <label name="Neues Band" ref="editorServlet">
            <params>
              <param name="type" value="jpvolume" />
              <param name="step" value="author" />
              <param name="todo" value="wnewobj" />
              <param name="parentID" select="currentObjID" />
            </params>
          </label>
          <restriction name="dataModel" value="datamodel-jpjournal.xsd datamodel-jpvolume.xsd" />
        </item>
        <item>
          <label name="Neuer Artikel" ref="editorServlet">
            <params>
              <param name="type" value="jparticle" />
              <param name="step" value="author" />
              <param name="todo" value="wnewobj" />
              <param name="parentID" select="currentObjID" />
            </params>
          </label>
          <restriction name="dataModel" value="datamodel-jpvolume.xsd" />
        </item>
        <item>
          <label name="Bild verlinken" ref="linkImgUrl" />
          <restriction name="linkImgAllowed" value="true" />
        </item>
        <item id="diagButton">
          <restriction name="dataModel" value="datamodel-jpjournal.xsd" />
          <label name="Rubrik bearbeiten" />
        </item>
      </item>
      <item class="jp-layout-menu-dropdown">
        <item>
          <label name="Passwort ändern" href="/servlets/MCRUserServlet?url=/content/below/index.xml&amp;mode=CreatePwdDialog" />
        </item>
        <item>
          <label name="Nutzerdaten anzeigen" href="/servlets/MCRUserServlet?url=/content/below/index.xml&amp;mode=ShowUser" />
        </item>
        <item>
          <label name="Nutzer anlegen" href="/servlets/MCRUserAdminServlet?mode=newuser" />
        </item>
        <item>
          <label name="Gruppe anlegen" href="/servlets/MCRUserAdminServlet?mode=newgroup" />
        </item>
        <item>
          <label name="Nutzer- Gruppenverwaltung" href="/servlets/MCRUserAjaxServlet" />
        </item>
        <restriction name="isAdmin" value="true" />
      </item>
      <item class="jp-layout-menu-dropdown">
        <!-- Administration -->
        <item>
          <label name="WebCLI" href="/modules/webcli/launchpad.xml" />
        </item>
        <item>
          <label name="ACL Editor" href="/servlets/MCRACLEditorServlet_v2?mode=getACLEditor" />
        </item>
        <restriction name="isAdmin" value="true" />
      </item>
      <item class="jp-layout-menu-dropdown">
        <!-- <label name="Löschen" /> -->
        <restriction name="deletePerm" value="true" />
        <item>
          <label name="Dokument löschen" href="/receive/{/mycoreobject/@ID}?XSL.object=delete" />
        </item>
      </item>
      <link id="delObj" class="jp-layout-message-button" name="Löschen" ref="editorServlet">
        <params>
          <param ref="editorServlet-editParam" />
          <param name="todo" value="sdelobj" />
        </params>
      </link>
    </menu>
  </xsl:variable>
  <xsl:variable name="menu" select="xalan:nodeset($menuXML)/menu" />


  <xsl:template name="classificationEditorDiag">
    <xsl:variable name="journalID" select="/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID" />
    <xsl:variable name="journalRecourceURL" select="concat($classeditor.resourceURL,'jp/',$journalID,'/')" />

    <xsl:call-template name="classeditor.loadSettings">
      <xsl:with-param name="classeditor.class" select="@classId"/>
      <xsl:with-param name="classeditor.categ" select="@categId"/>
      <xsl:with-param name="classeditor.showId" select="@showId='true'"/>
    </xsl:call-template>
    <xsl:call-template name="classeditor.includeDojoJS" />
    <xsl:call-template name="classeditor.includeJS" />

    <script type="text/javascript" src="{$WebApplicationBaseURL}classification/ClassificationEditor.js"></script>

    <script type="text/javascript">
      function loadError(jqxhr, settings, exception) {
        console.log(exception);
        alert(exception);
      }

      $(document).ready(function() {
        $("#diagButton").click(function() {
          classeditor.settings.resourceURL = '<xsl:value-of select="$journalRecourceURL"/>';
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
      $("#ckeditorButton").click(function(){
      introEditor('<xsl:value-of select="$currentObjID"/>')
      })
      })
    </script>
  </xsl:template>

  <xsl:template name="objectEditing">
    <xsl:param name="id" />
    <xsl:param name="dataModel" />

    <menu id="jp-object-editing" class="jp-layout-object-editing">
      <xsl:apply-templates mode="menuItem" select="$menu/item" />

      <xsl:if test="/mycoreobject[contains(@ID,'_jpjournal_')]">
        <xsl:call-template name="classificationEditorDiag" />
        <xsl:call-template name="introEditorDiag" />
      </xsl:if>
    </menu>

    <deleteMsg>
      <xsl:choose>
        <xsl:when test="/mycoreobject/structure/derobjects">
          <xsl:call-template name="deleteFailMessage" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="deleteButton">
            <xsl:apply-templates mode="menuLabel" select="$menu/link[@id='delObj']" />
          </xsl:variable>
          <xsl:call-template name="deleteCheckMessage">
            <xsl:with-param name="delButton" select="$deleteButton" />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </deleteMsg>
  </xsl:template>

  <!-- MENU ##################################################################### -->
  <xsl:template mode="menuItem" match="item">
    <xsl:call-template name="createMenuItem" />
  </xsl:template>

  <xsl:template mode="menuItem" match="item[restriction]">
    <xsl:variable name="name" select="restriction/@name" />
    <xsl:if test="contains(restriction/@value,$menu/var[@name=$name]/@value)">
      <xsl:call-template name="createMenuItem" />
    </xsl:if>
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
    <a href="{@href}">
      <xsl:value-of select="@name" />
    </a>
  </xsl:template>

  <xsl:template mode="menuLabel" match="label[@ref]|link[@ref]">
    <a>
      <xsl:apply-templates mode="copyAttr" select="@*" />
      <xsl:attribute name="href">
          <xsl:apply-templates mode="menuLink" select="@ref" />
          <xsl:apply-templates mode="menuLinkParam" select="params" />
      </xsl:attribute>
      <xsl:value-of select="@name" />
    </a>
  </xsl:template>

  <xsl:template mode="menuLink" match="@ref">
    <xsl:variable name="ref" select="." />
    <xsl:value-of select="$menu/link[@id=$ref]/@href" />
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
    <xsl:value-of select="concat(@name,'=', $menu/var[@name=$selectID]/@value)" />
  </xsl:template>
  <!-- END MENU ##################################################################### -->

  <xsl:template name="deleteFailMessage">
    <div class="jp-layout-message-background" />
    <div id="deleteFail" class="jp-layout-message">
      <div class="jp-layout-message-icon">
        <img src="/images/error.png" width="64px" />
      </div>
      <div class="jp-layout-message-text">
        Dieses Dokument enthält Digitalisate. Löschen nicht möglich!
      </div>
      <a id="okButton" class="jp-layout-message-button" href="/receive/{/mycoreobject/@ID}">
        OK
      </a>
    </div>
  </xsl:template>

  <xsl:template name="deleteCheckMessage">
    <xsl:param name="delButton" />

    <div class="jp-layout-message-background" />
    <div id="deleteCheck" class="jp-layout-message">
      <div class="jp-layout-message-icon">
        <img src="/images/warning.png" width="64px" />
      </div>
      <div class="jp-layout-message-text">
        Sind Sie sicher, daß Sie dieses Dokument löschen wollen?
      </div>
      <ul id="delCheckButtons" class="jp-layout-horiz-menu">
        <li>
          <a class="jp-layout-message-button" href="/receive/{/mycoreobject/@ID}">
            Abbrechen
          </a>
        </li>
        <li>
          <xsl:copy-of select="$delButton" />
        </li>
      </ul>
    </div>
  </xsl:template>
</xsl:stylesheet>