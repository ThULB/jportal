<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 1.7 $ $Date: 2006-09-06 12:20:05 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink"
  xmlns:encoder="xalan://java.net.URLEncoder">

  <xsl:variable name="PageTitle" select="'Die Detailliste des Objektes'" />
  <xsl:include href="MyCoReLayout.xsl" />
  <!-- include custom templates for supported objecttypes -->
  <xsl:include href="objecttypes.xsl" />

  <xsl:param name="subselect.session"/>
  <xsl:param name="subselect.varpath"/>
  <xsl:param name="subselect.webpage"/>

  <xsl:variable name="editorValue">
    <xsl:variable name="pValue">
      <xsl:call-template name="UrlGetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="'editorValue'" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string-length($pValue) &gt; 1">
        <xsl:value-of select="$pValue" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'@xlink:href'" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="editorValueTitle">
    <xsl:variable name="pValue">
      <xsl:call-template name="UrlGetParam">
        <xsl:with-param name="url" select="$RequestURL" />
        <xsl:with-param name="par" select="'editorValueTitle'" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string-length($pValue) &gt; 1">
        <xsl:value-of select="$pValue" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'@editor.output'" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="/mcr_directory">
    <xsl:variable name="obj_host" select="'local'" />
    <xsl:variable name="derivlink" select="concat('mcrobject:',ownerID)" />
    <xsl:variable name="derivdoc" select="document($derivlink)" />
    <xsl:variable name="sourcelink"
      select="concat('mcrobject:',$derivdoc/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href)" />
    <xsl:variable name="sourcedoc" select="document($sourcelink)" />
    <xsl:variable name="accesseditvalue">
      <xsl:choose>
        <!-- if source object and derivate allows writing -->
        <xsl:when test="acl:checkPermission($derivdoc/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href,'writedb') and acl:checkPermission(ownerID,'writedb')">
          <xsl:value-of select="'true'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'false'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="accessdeletevalue">
      <xsl:choose>
        <!-- if source object and derivate allows deleting -->
        <xsl:when test="acl:checkPermission($derivdoc/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href,'deletedb') and acl:checkPermission(ownerID,'deletedb')">
          <xsl:value-of select="'true'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'false'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- check the access rights -->
    <table id="metaData" cellpadding="0" cellspacing="0">
      <tr>
        <th class="metahead" colspan="2">
          <xsl:value-of select="i18n:translate('IFS.commonData')" />
        </th>
      </tr>
      <tr>
        <td class="metaname">
          <xsl:value-of select="concat(i18n:translate('IFS.id'),' :')" />
        </td>
        <td class="metavalue">
          <xsl:value-of select="$derivdoc/mycorederivate/@ID" />
        </td>
      </tr>
      <tr>
        <td class="metaname">
          <xsl:value-of select="concat(i18n:translate('IFS.location'),' :')" />
        </td>
        <td class="metavalue">
          <xsl:value-of select="$obj_host" />
        </td>
      </tr>
      <tr>
        <td class="metaname">
          <xsl:value-of select="concat(i18n:translate('IFS.size'),' :')" />
        </td>
        <td class="metavalue">
          <xsl:value-of select="concat(size,' ',i18n:translate('IFS.bytes'))" />
        </td>
      </tr>
      <tr>
        <td class="metaname">
          <xsl:value-of select="concat(i18n:translate('IFS.total'),' :')" />
        </td>
        <td class="metavalue">
          <xsl:value-of select="concat( numChildren/total/files, ' / ', numChildren/total/directories )" />
        </td>
      </tr>
      <tr>
        <td class="metaname">
          <xsl:value-of select="concat(i18n:translate('IFS.startFile'),' :')" />
        </td>
        <td class="metavalue">
          <xsl:variable name="maindoc" select="$derivdoc/mycorederivate/derivate/internals/internal/@maindoc" />
          <xsl:variable name="derivifs"
            select="concat($ServletsBaseURL,'MCRFileNodeServlet/',$derivdoc/mycorederivate/@ID,'/',$maindoc,$HttpSession,'?hosts=',$obj_host)" />
          <a href="{$derivifs}" target="_blank">
            <xsl:value-of select="$maindoc" />
          </a>
        </td>
      </tr>
      <tr>
        <td class="metaname">
          <xsl:value-of select="concat(i18n:translate('metaData.lastChanged'),' :')" />
        </td>
        <td class="metavalue">
          <xsl:value-of select="date" />
        </td>
      </tr>
      <tr>
        <td class="metaname">
          <xsl:value-of select="concat(i18n:translate('IFS.docTitle'),' :')" />
        </td>
        <td class="metavalue">
          <xsl:call-template name="objectLink">
            <xsl:with-param name="obj_id" select="$sourcedoc/mycoreobject/@ID" />
          </xsl:call-template>
        </td>
      </tr>
    </table>
    <table id="files" cellpadding="0" cellspacing="0">
      <tr>
        <th class="metahead">
          <xsl:value-of select="'Auswahl'" />
        </th>
        <th class="metahead">
          <xsl:value-of select="'Preview'" />
        </th>
        <th class="metahead">
          <xsl:value-of select="i18n:translate('IFS.fileSize')" />
        </th>
        <th class="metahead">
          <xsl:value-of select="i18n:translate('IFS.fileType')" />
        </th>
        <th class="metahead">
          <xsl:value-of select="i18n:translate('metaData.lastChanged')" />
        </th>
      </tr>
      <xsl:apply-templates select="path" />
      <xsl:apply-templates select="children">
        <xsl:with-param name="accesseditvalue" select="$accesseditvalue" />
        <xsl:with-param name="accessdeletevalue" select="$accessdeletevalue" />
        <xsl:with-param name="maindoc" select="$derivdoc/mycorederivate/derivate/internals/internal/@maindoc" />
        <xsl:with-param name="se_mcrid" select="$derivdoc/mycorederivate/@ID" />
        <xsl:with-param name="re_mcrid" select="$sourcedoc/mycoreobject/@ID" />
      </xsl:apply-templates>
    </table>
    <hr />
    <p>
      <table>
        <tr>
          <td style="padding-right:8px">
            <form method="post">
              <xsl:attribute name="action">
                    <xsl:value-of select="concat($WebApplicationBaseURL,'servlets/MCRDerivateLinkServlet?')" />
                    <xsl:value-of select="concat('subselect.session=', $subselect.session)" />
                    <xsl:value-of select="concat('&amp;subselect.varpath=', $subselect.varpath)" />
                    <xsl:value-of select="concat('&amp;subselect.webpage=', encoder:encode($subselect.webpage))" />
                    <xsl:value-of select="concat('&amp;XSL.editor.session.id=', $subselect.session)" />
                  </xsl:attribute>
              <input type="submit" class="submit" value="{i18n:translate('indexpage.sub.select.back')}" />
            </form>
          </td>
          <td>
            <form method="post">
              <xsl:attribute name="action">
                  <xsl:value-of select="concat($WebApplicationBaseURL,$subselect.webpage)" />
                  <xsl:if test="not(contains(@webpage,@session))">
                    <xsl:value-of select="concat('XSL.editor.session.id=',$subselect.session)" />
                  </xsl:if>
                </xsl:attribute>
              <input type="submit" class="submit" value="{i18n:translate('indexpage.sub.select.cancel')}" />
            </form>
          </td>
        </tr>
      </table>
    </p>

    
  </xsl:template>

  <!-- parent directory ********************************************** -->
  <xsl:template match="path">
    <xsl:if test="contains(.,'/')">
      <xsl:variable name="host" select="../../@host" />
      <xsl:variable name="parent">
        <xsl:call-template name="parent">
          <xsl:with-param name="path" select="." />
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="parentdoc" select="document(concat('ifs:',$parent,'?hosts=',$host))" />
      <xsl:variable name="derivifs"
        select="concat($ServletsBaseURL,'MCRFileNodeServlet/',$parent,$HttpSession,'?hosts=',$host)" />
      <tr>
        <td class="metavalue"></td>
        <td class="metavalue">
          <a href="{$derivifs}">..</a>
        </td>
        <td class="metavalue">
          <xsl:value-of select="$parentdoc/mcr_directory/size" />
          <xsl:value-of select="concat(i18n:translate('IFS.bytes'),' :')" />
        </td>
        <td class="metavalue">
          <xsl:value-of select="i18n:translate('IFS.directory')" />
        </td>
        <td class="metavalue">
          <xsl:value-of select="$parentdoc/mcr_directory/date[@type='lastModified']" />
        </td>
        <td class="metavalue"></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="parent">
    <xsl:param name="path" />
    <xsl:param name="position" select="string-length($path)-1" />
    <xsl:choose>
      <xsl:when test="contains(substring($path,$position),'/')">
        <!--found the last element -->
        <xsl:choose>
          <!--Workaround for FilenodeServlet-Bug: trailing slashes MUST be ommited for directory
            but are REQUIRED if at Level of OwnerID
          -->
          <xsl:when test="contains(substring($path,0,$position),'/')">
            <xsl:value-of select="substring($path,0,$position)" />
          </xsl:when>
          <xsl:otherwise>
            <!-- return ownerID with trailing '/' -->
            <xsl:value-of select="concat(substring($path,0,$position),'/')" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="parent">
          <xsl:with-param name="path" select="$path" />
          <xsl:with-param name="position" select="$position - 1" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Child componets ********************************************** -->
  <xsl:template match="children">
    <xsl:param name="accesseditvalue" />
    <xsl:param name="accessdeletevalue" />
    <xsl:param name="maindoc" />
    <xsl:param name="se_mcrid" />
    <xsl:param name="re_mcrid" />
    <xsl:variable name="host" select="../../@host" />
    <xsl:variable name="type" select="substring-before(substring-after($re_mcrid,'_'),'_')" />
    <xsl:variable name="allfiles" select="../numChildren/total/files" />
    <xsl:variable name="path" select="substring-after(../path,../ownerID)" />
    <xsl:variable name="contentTypes" select="document('webapp:FileContentTypes.xml')" />
    <xsl:for-each select="child">
      <xsl:sort select="@type" />
      <xsl:sort select="contentType" />
      <xsl:sort select="name" />
      <tr>
        <td>
          <xsl:variable name="derivid" select="../../path" />
          <xsl:variable name="derivmain" select="name" />
          <xsl:if test="@type = 'directory'">
            <xsl:variable name="derivifs"
              select="concat($ServletsBaseURL,'MCRFileNodeServlet/',$derivid,'/',$derivmain,$HttpSession,'?hosts=',$host)" />
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="$derivifs" />
              </xsl:attribute>
              <xsl:copy-of select="name" />
            </a>
          </xsl:if>
          <xsl:if test="@type = 'file'">
            <xsl:variable name="url"
              select="concat($ServletsBaseURL,'XMLEditor',$HttpSession,
              '?_action=end.subselect&amp;subselect.session=',$subselect.session,
              '&amp;subselect.varpath=', $subselect.varpath,
              '&amp;subselect.webpage=', encoder:encode($subselect.webpage))" />
            <xsl:variable name="path" select="concat($derivid, '/', $derivmain)" />   
            <a
              href="{$url}&amp;_var_{$editorValue}={$path}&amp;_var_@xlink:title={$path}&amp;_var_{$editorValueTitle}={$path}">
              <xsl:value-of select="substring-after($path, '/')" />
            </a>
          </xsl:if>
        </td>
        <td class="metavalue">
          <xsl:call-template name="selectFile.printPreview">
            <xsl:with-param name="deriv" select="../../path"/>
            <xsl:with-param name="firstSupportedFile" select="concat('/', name)"/>
          </xsl:call-template>
        </td>
        <td class="metavalue">
          <xsl:value-of select="concat(size,' ',i18n:translate('IFS.bytes'))" />
        </td>
        <xsl:if test="@type = 'directory'">
          <td class="metavalue">
            <xsl:value-of select="i18n:translate('IFS.directory')" />
          </td>
        </xsl:if>
        <xsl:if test="@type = 'file'">
          <xsl:variable name="ctype" select="contentType" />
          <td class="metavalue">
            <xsl:value-of select="$contentTypes/FileContentTypes/type/label[../@ID=$ctype]" />
          </td>
        </xsl:if>
        <td class="metavalue">
          <xsl:value-of select="date" />
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>
  
  
  <!-- print preview pic -->
  <xsl:template name="selectFile.printPreview">
    <xsl:param name="deriv" />
    <xsl:param name="firstSupportedFile" />

    <xsl:if test="$objectHost = 'local'">
      <xsl:variable name="mayWriteDerivate" select="acl:checkPermission($deriv,'writedb')" />
      <!-- MCR-IView ..start -->
      <xsl:if test="$firstSupportedFile != ''">
        <a>
          <xsl:attribute name="href">
            <xsl:call-template name="iview.getAddress">
              <xsl:with-param select="$deriv" name="derivID" />
              <xsl:with-param select="$firstSupportedFile" name="pathOfImage" />
              <xsl:with-param select="'500'" name="height" />
              <xsl:with-param select="'750'" name="width" />
              <xsl:with-param select="'fitToWidth'" name="scaleFactor" />
              <xsl:with-param select="'extended'" name="display" />
              <xsl:with-param select="'image'" name="style" />
              </xsl:call-template>
            </xsl:attribute>
          <xsl:attribute name="title">
            <xsl:value-of select="i18n:translate('metaData.iView')" />
          </xsl:attribute>
          <xsl:call-template name="iview">
            <xsl:with-param select="$deriv" name="derivID" />
            <xsl:with-param select="$firstSupportedFile" name="pathOfImage" />
            <xsl:with-param select="'thumbnail'" name="display" />
          </xsl:call-template>
        </a>
      </xsl:if>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>