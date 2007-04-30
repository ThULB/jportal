<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 1.7 $ $Date: 2006/09/06 12:20:05 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink">

  <xsl:variable name="PageTitle" select="'Die Detailliste des Objektes'" />
  <xsl:include href="MyCoReLayout.xsl" />

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
        <xsl:when
          test="acl:checkPermission($derivdoc/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href,'writedb') ">
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
          <a href="{$derivifs}">
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
          <xsl:variable name="titles" select="$sourcedoc/mycoreobject/metadata/titles" />
          <a href="{$WebApplicationBaseURL}receive/{$sourcedoc/mycoreobject/@ID}{$HttpSession}">
            <xsl:choose>
              <xsl:when test="$titles/title[lang($CurrentLang) and @inherited = '0']">
                <xsl:for-each select="$titles/title[lang($CurrentLang) and @inherited = '0']">
                  <xsl:if test="position() != 1">
                    <br />
                  </xsl:if>
                  <xsl:value-of select="." />
                </xsl:for-each>
              </xsl:when>
              <xsl:otherwise>
                <xsl:for-each select="$titles/title[lang($DefaultLang) and @inherited = '0']">
                  <xsl:if test="position() != 1">
                    <br />
                  </xsl:if>
                  <xsl:value-of select="." />
                </xsl:for-each>
              </xsl:otherwise>
            </xsl:choose>
          </a>
        </td>
      </tr>
    </table>
    <table id="files" cellpadding="0" cellspacing="0">
      <tr>
        <th class="metahead"></th>
        <th class="metahead">
          <xsl:value-of select="i18n:translate('IFS.fileName')" />
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
        <th class="metahead"></th>
      </tr>
      <xsl:apply-templates select="path" />
      <xsl:apply-templates select="children">
        <xsl:with-param name="accesseditvalue" select="$accesseditvalue" />
        <xsl:with-param name="maindoc" select="$derivdoc/mycorederivate/derivate/internals/internal/@maindoc" />
        <xsl:with-param name="se_mcrid" select="$derivdoc/mycorederivate/@ID" />
        <xsl:with-param name="re_mcrid" select="$sourcedoc/mycoreobject/@ID" />
      </xsl:apply-templates>
    </table>
    <hr />
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
        <td class="metavalue">
          <xsl:choose>
            <xsl:when test="$maindoc = name">
              <img src="{$WebApplicationBaseURL}images/button_green.gif" alt="{i18n:translate('IFS.mainFile')}"
                border="0" />
            </xsl:when>
            <xsl:when test="concat('/',$maindoc) = concat($path,'/',name)">
              <img src="{$WebApplicationBaseURL}images/button_green.gif" alt="{i18n:translate('IFS.mainFile')}"
                border="0" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:if test="$accesseditvalue = 'true' and @type = 'file'">
                <form action="{$WebApplicationBaseURL}servlets/MCRStartEditorServlet{$HttpSession}" method="get">
                  <input name="lang" type="hidden" value="{$CurrentLang}" />
                  <input name="se_mcrid" type="hidden">
                    <xsl:attribute name="value">
                      <xsl:value-of select="$se_mcrid" />
                    </xsl:attribute>
                  </input>
                  <input name="re_mcrid" type="hidden">
                    <xsl:attribute name="value">
                      <xsl:value-of select="$re_mcrid" />
                    </xsl:attribute>
                  </input>
                  <input name="type" type="hidden" value="{$type}" />
                  <input name="step" type="hidden" value="commit" />
                  <input name="todo" type="hidden" value="ssetfile" />
                  <input name="extparm" type="hidden">
                    <xsl:attribute name="value">
                      <xsl:value-of select="substring-after(concat($path,'/',name),'/')"/>
                    </xsl:attribute>
                  </input>
                  <input type="image" src="{$WebApplicationBaseURL}images/button_light.gif"
                    title="{i18n:translate('IFS.mainFile')}" border="0" />
                </form>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </td>
        <td class="metavalue">
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
            <xsl:variable name="derivifs"
              select="concat($ServletsBaseURL,'MCRFileNodeServlet/',$derivid,'/',$derivmain,$HttpSession,'?hosts=',$host)" />
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="$derivifs" />
              </xsl:attribute>
              <xsl:copy-of select="name" />
            </a>
          </xsl:if>
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
        <td class="metavalue">
          <xsl:if test="$accesseditvalue = 'true'">
            <form action="{$WebApplicationBaseURL}servlets/MCRStartEditorServlet{$HttpSession}" method="get">
              <input name="lang" type="hidden" value="{$CurrentLang}" />
              <input name="se_mcrid" type="hidden">
                <xsl:attribute name="value">
                  <xsl:value-of select="$se_mcrid" />
                </xsl:attribute>
              </input>
              <input name="re_mcrid" type="hidden">
                <xsl:attribute name="value">
                  <xsl:value-of select="$re_mcrid" />
                </xsl:attribute>
              </input>
              <input name="type" type="hidden" value="{$type}" />
              <input name="step" type="hidden" value="commit" />
              <input name="todo" type="hidden" value="sdelfile" />
              <input name="extparm" type="hidden">
                <xsl:attribute name="value">
                  <xsl:value-of
                    select="concat('####nrall####',$allfiles,'####nrthe####1####filename####',$path,'/',name)" />
                </xsl:attribute>
              </input>
              <input type="image" src="{$WebApplicationBaseURL}images/button_delete.gif"
                title="{i18n:translate('IFS.fileDelete')}" border="0" />
            </form>
          </xsl:if>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
