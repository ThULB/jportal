<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:xalan="http://xml.apache.org/xalan">
  <xsl:template name="objectEditing">
    <xsl:param name="id" />
    <xsl:param name="dataModel" />

    <xsl:variable name="type" select="substring-before(substring-after($id,'_'),'_')" />
    <xsl:variable name="editPropXML">
      <access update="{acl:checkPermission($id,concat('update_',$type))}" delete="{acl:checkPermission($id,concat('delete_',$type))}" />
      <editorServlet url="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}" />
      <param name="tf_mcrid" value="{$id}" />
      <param name="re_mcrid" value="{$id}" />
      <param name="se_mcrid" value="{$id}" />
      <param name="type" value="{$type}" />
      <param name="step" value="commit" />
      <link label="Dokument bearbeiten">
        <param name="todo" value="seditobj" />
      </link>
      <link label="Datei hochladen">
        <param name="todo" value="snewder" />
      </link>
    </xsl:variable>
    <xsl:variable name="editProp" select="xalan:nodeset($editPropXML)" />

    <xsl:if test="$editProp/access/@update = 'true'">
      <menu id="jp-object-editing" class="jp-layout-horiz-menu">
        <xsl:apply-templates mode="editLink" select="$editProp/link" />

        <xsl:apply-templates mode="newObjLink" select="$settings/newObj[contains(@parent, $dataModel)]">
          <xsl:with-param name="parentID" select="$id" />
        </xsl:apply-templates>

        <xsl:if test="/mycoreobject[contains(@ID,'_jpjournal_')]">
          <li>
            <span id="diagButton">Rubrik bearbeiten</span>
          </li>
          <xsl:call-template name="classificationEditor" />
        </xsl:if>
      </menu>
    </xsl:if>

    <xsl:if test="$editProp/access/@delete = 'true'">
      <menu id="jp-delete-objects" class="jp-layout-horiz-menu">
        <li>
          <a href="">Zeitschrift Löschen</a>
        </li>
        <li>
          <a href="">Bilder Löschen</a>
        </li>
      </menu>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="editLink" match="link">
    <xsl:variable name="params">
      <xsl:apply-templates mode="urlParams" select="../param | ./param" />
    </xsl:variable>
    <li>
      <a href="{concat(../editorServlet/@url,'?',$params)}">
        <xsl:value-of select="@label" />
      </a>
    </li>
  </xsl:template>

  <xsl:template mode="urlParams" match="param">
    <xsl:value-of select="concat(@name,'=',@value)" />
    <xsl:if test="position() != last()">
      <xsl:value-of select="'&amp;'" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="newObjLink" match="newObj">
    <xsl:param name="parentID" />
    <li>
      <a href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={.}&amp;step=author&amp;todo=wnewobj&amp;parentID={$parentID}">
        <xsl:value-of select="@label" />
      </a>
    </li>
  </xsl:template>
</xsl:stylesheet>