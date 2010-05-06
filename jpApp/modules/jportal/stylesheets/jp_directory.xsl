<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink">

  <xsl:variable name="PageTitle" select="'Die Detailliste des Objektes'" />

  <xsl:template match="/mcr_directory" priority="1">
    <xsl:variable name="obj_host" select="'local'" />
    <xsl:variable name="derivlink" select="concat('mcrobject:',ownerID)" />
    <xsl:variable name="derivdoc" select="document($derivlink)" />
    <xsl:variable name="sourcelink" select="concat('mcrobject:',$derivdoc/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href)" />
    <xsl:variable name="sourcedoc" select="document($sourcelink)" />
  
    <xsl:variable name="accesseditvalue">
      <xsl:choose>
        <!-- if source object and derivate allows writing -->
        <xsl:when test="acl:checkPermission(ownerID,'writedb')">
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
        <xsl:when test="acl:checkPermission(ownerID,'deletedb')">
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
        <xsl:with-param name="accessdeletevalue" select="$accessdeletevalue" />
        <xsl:with-param name="maindoc" select="$derivdoc/mycorederivate/derivate/internals/internal/@maindoc" />
        <xsl:with-param name="se_mcrid" select="$derivdoc/mycorederivate/@ID" />
        <xsl:with-param name="re_mcrid" select="$sourcedoc/mycoreobject/@ID" />
      </xsl:apply-templates>
    </table>
    <hr />
  </xsl:template>

</xsl:stylesheet>