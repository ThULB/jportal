<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:derivateLinkUtil="xalan://org.mycore.frontend.util.DerivateLinkUtil"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="i18n derivateLinkUtil xlink acl mcrxml xalan xsi">

  <xsl:variable name="bookmarkedImage" select="derivateLinkUtil:getBookmarkedImage()" />
  <xsl:variable name="linkExist" select="/mycoreobject/metadata/derivateLinks/derivateLink[@xlink:href = $bookmarkedImage]" />
  <xsl:variable name="hasSourceOfLink" select="/mycoreobject/structure/derobjects/derobject[@xlink:href = substring-before($bookmarkedImage,'/')]" />
  
  <xsl:variable name="menuVarXML">
    <var name="dataModel" value="{/mycoreobject/@xsi:noNamespaceSchemaLocation}" />
    <var name="createJournal" value="{acl:checkPermission('CRUD','create_jpjournal')}" />
    <var name="createPerson" value="{acl:checkPermission('POOLPRIVILEGE','create-person')}" />
    <var name="createInst" value="{acl:checkPermission('POOLPRIVILEGE','create-jpinst')}" />
    <var name="createVol" value="{acl:checkPermission('POOLPRIVILEGE','create-jpvolume')}" />
    <var name="createArt" value="{acl:checkPermission('POOLPRIVILEGE','create-jparticle')}" />
    <var name="currentType" value="{$currentType}" />
    <var name="currentObjID" value="{$currentObjID}" />
    <var name="updatePerm" value="{$updatePerm}" />
    <var name="deletePerm" value="{$deletePerm}" />
    <var name="isAdmin" value="{acl:checkPermission('POOLPRIVILEGE','administrate-user')}" />
    <var name="isGuest" value="{mcrxml:isCurrentUserGuestUser()}" />
    <var name="linkImgAllowed" value="{$bookmarkedImage != '' and not($linkExist) and not($hasSourceOfLink)}" />
  </xsl:variable>
  <xsl:variable name="menuVar" select="xalan:nodeset($menuVarXML)"/>

  <xsl:variable name="menuXML">
    <menu>
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
        <item id="diagButton">
          <restriction name="dataModel" value="datamodel-jpjournal.xsd" />
          <label name="Rubrik bearbeiten" />
        </item>
        <item>
          <label name="Datei hochladen" ref="editorServlet">
            <params>
              <param ref="editorServlet-editParam" />
              <param name="todo" value="snewder" />
            </params>
          </label>
        </item>
      </item>
      <item class="jp-layout-menu-dropdown">
        <item>
          <label name="Neue Person" ref="editorServlet">
            <params>
              <param name="type" value="person" />
              <param name="step" value="author" />
              <param name="todo" value="wnewobj" />
            </params>
          </label>
          <restriction name="createPerson" value="true" />
        </item>
        <item>
          <label name="Neue Institution" ref="editorServlet">
            <params>
              <param name="type" value="jpinst" />
              <param name="step" value="author" />
              <param name="todo" value="wnewobj" />
            </params>
          </label>
          <restriction name="createInst" value="true" />
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
          <label name="Neuer Band" ref="editorServlet">
            <params>
              <param name="type" value="jpvolume" />
              <param name="step" value="author" />
              <param name="todo" value="wnewobj" />
              <param name="parentID" select="currentObjID" />
            </params>
          </label>
          <restriction name="createVol" value="true" />
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
          <restriction name="createArt" value="true" />
          <restriction name="dataModel" value="datamodel-jpvolume.xsd" />
        </item>
      </item>
      <item class="jp-layout-menu-dropdown">
        <restriction name="updatePerm" value="true" />
        <restriction name="linkImgAllowed" value="true" />
        <item>
          <label name="Bild verlinken" ref="linkImgUrl" />
        </item>
      </item>
      <item class="jp-layout-menu-dropdown">
        <restriction name="deletePerm" value="true" />
        <item>
          <label name="Dokument löschen" href="/receive/{/mycoreobject/@ID}?XSL.object=delete" />
        </item>
      </item>
      <item class="jp-layout-menu-dropdown">
        <restriction name="isGuest" value="false" />
        <restriction name="dataModel" value="datamodel-jpjournal.xsd datamodel-jpvolume.xsd datamodel-jparticle.xsd datamodel-person.xsd datamodel-jpinst.xsd" />
        <item>
          <label name="XML" href="/receive/{/mycoreobject/@ID}?XSL.Style=xml" />
        </item>
        <item>
          <label name="Versionsgeschichte" href="/history.xml?XSL.id={/mycoreobject/@ID}" />
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
    <xsl:variable name="access">
      <xsl:apply-templates select="restriction" mode="menuItem" />
    </xsl:variable>
    <xsl:if test="not(contains($access, 'false'))">
      <xsl:call-template name="createMenuItem" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="restriction" mode="menuItem">
    <xsl:variable name="name" select="@name" />
    <xsl:variable name="permission" select="$menuVar/var[@name=$name]/@value" />
    <xsl:value-of select="$permission != '' and contains(@value, $permission)" />
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
    <xsl:value-of select="concat(@name,'=', $menuVar/var[@name=$selectID]/@value)" />
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