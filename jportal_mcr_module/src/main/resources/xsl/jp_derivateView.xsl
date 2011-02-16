<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ===================================================================================================== -->
<!-- This stylesheet contains all templates to print derivates -->
<!-- ===================================================================================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities"
  exclude-result-prefixes="xlink mcr i18n acl xalan mcrxsl encoder layoutUtils">

  <!-- ========================================================== -->
  <!-- derivate  -->
  <!-- ========================================================== -->
  <xsl:template match="internals" priority="2">
    <xsl:param name="objID" />
    <xsl:param name="objectXML" />
    <xsl:if test="$objectHost = 'local'">
      <xsl:variable name="derivID" select="../../@ID" />
      <xsl:choose>
        <xsl:when test="mcrxsl:exists($derivID)">
          <xsl:variable name="mainFile" select="internal/@maindoc" />
          <xsl:call-template name="jp.derivate.print">
            <xsl:with-param name="objID" select="$objID" />
            <xsl:with-param name="objectXML" select="$objectXML" />
            <xsl:with-param name="derivID" select="$derivID" />
            <xsl:with-param name="mainFile" select="$mainFile" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="jp.derivate.doesnotexist" >
            <xsl:with-param name="derivID" select="$derivID"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- ========================================================== -->
  <!-- derivate link  -->
  <!-- ========================================================== -->
  <xsl:template match="derivateLink" priority="2">
    <xsl:param name="objID" />
    <xsl:param name="objectXML" />
    <xsl:if test="$objectHost = 'local'">
      <xsl:variable name="derivID" select="substring-before(@xlink:href,'/')" />
      <xsl:choose>
        <xsl:when test="mcrxsl:exists($derivID)">
          <xsl:variable name="mcrObjId" select="document(concat('mcrobject:',$derivID))/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href" />
          <xsl:variable name="mainFile" select="substring-after(@xlink:href,'/')" />
          <xsl:call-template name="jp.derivate.print">
            <xsl:with-param name="objID" select="$mcrObjId" />
            <xsl:with-param name="objectXML" select="$objectXML" />
            <xsl:with-param name="derivID" select="$derivID" />
            <xsl:with-param name="mainFile" select="$mainFile" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="jp.derivate.doesnotexist" >
            <xsl:with-param name="derivID" select="$derivID"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- ========================================================== -->
  <!-- Hit in text -->
  <!-- ========================================================== -->
  <xsl:template match="mcr:metaData" priority="2">
    <xsl:param name="objID" />
    <xsl:param name="objectXML" />
    
    <xsl:if test="$objectHost = 'local'">
      <xsl:variable name="derivID" select="mcr:field[@name='DerivateID']/text()" />
      <xsl:choose>
        <xsl:when test="mcrxsl:exists($derivID)">
          <xsl:variable name="mainFile" >
            <!-- check if a file mapping has to be done -->
            <xsl:call-template name="jp.derivate.mappFile">
              <xsl:with-param name="derivid-if" select="$derivID" />
              <xsl:with-param name="filePath" select="mcr:field[@name='filePath']/text()" />
              <xsl:with-param name="fileName" select="mcr:field[@name='fileName']/text()" />
            </xsl:call-template>
          </xsl:variable>
          <xsl:call-template name="jp.derivate.print">
            <xsl:with-param name="objID" select="$objID" />
            <xsl:with-param name="objectXML" select="$objectXML" />
            <xsl:with-param name="derivID" select="$derivID" />
            <xsl:with-param name="mainFile" select="$mainFile" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="jp.derivate.doesnotexist" >
            <xsl:with-param name="derivID" select="$derivID"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- ========================================================== -->
  <!-- is called when the derivate doesn't exists -->
  <!-- ========================================================== -->
  <xsl:template name="jp.derivate.doesnotexist" >
    <xsl:param name="derivID" />
    <xsl:value-of select="concat('Derivat ', $derivID,' ist gelöscht!')"></xsl:value-of>      
  </xsl:template>

  <!-- ========================================================== -->
  <!-- Start entry point to print a derivate -->
  <!-- ========================================================== -->
  <xsl:template name="jp.derivate.print">
    <xsl:param name="objID" />
    <xsl:param name="objectXML" />
    <xsl:param name="derivID" />
    <xsl:param name="mainFile" />

    <!-- encoded main file -->
    <xsl:variable name="encodedMainFile" select="encoder:encode($mainFile)" />
    <!-- url to MCRFileNodeServlet -->
    <xsl:variable name="derivbase" select="concat($ServletsBaseURL,'MCRFileNodeServlet/',$derivID,'/')" />
    <!-- is iview2 is used or not -->
    <xsl:variable name="useIview">
      <xsl:call-template name="jp.derivate.iview.isSupportedFile">
        <xsl:with-param name="objID" select="$objID"/>
        <xsl:with-param name="file" select="$mainFile"/>
      </xsl:call-template>
    </xsl:variable>
    <!-- href -->
    <xsl:variable name="href">
      <xsl:choose>
        <xsl:when test="$useIview = 'true'">
          <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',$objID,'?XSL.view.objectmetadata=false&amp;jumpback=true&amp;maximized=true&amp;page=',$encodedMainFile)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <!-- remove double slash if exist -->
            <xsl:when test="substring($derivbase,string-length($derivbase),1) = '/' and substring($encodedMainFile,1,1) = '/'">
              <xsl:value-of select="concat($derivbase,substring-after($encodedMainFile,'/'))" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="concat($derivbase,$encodedMainFile)" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- print -->
    <xsl:choose>
      <xsl:when test="$view.objectmetadata='false'">
        <!-- metadata -->
        <xsl:call-template name="jp.derivate.print.present">
          <xsl:with-param name="objID" select="$objID"/>
          <xsl:with-param name="objectXML" select="$objectXML" />
          <xsl:with-param name="derivID" select="$derivID" />
          <xsl:with-param name="derivbase" select="$derivbase" />
          <xsl:with-param name="useIview" select="$useIview" />
          <xsl:with-param name="encodedMainFile" select="$encodedMainFile" />
          <xsl:with-param name="href" select="$href" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <!-- resultset -->
        <xsl:call-template name="jp.derivate.print.details">
            <xsl:with-param name="objID" select="$objID"/>
            <xsl:with-param name="objectXML" select="$objectXML" />
            <xsl:with-param name="derivbase" select="$derivbase" />
            <xsl:with-param name="encodedMainFile" select="$encodedMainFile" />
          <xsl:with-param name="href" select="$href" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ========================================================== -->
  <!-- Metadataview -->
  <!-- ========================================================== -->
  <xsl:template name="jp.derivate.print.present">
    <xsl:param name="objID" />
    <xsl:param name="objectXML" />
    <xsl:param name="derivID" />
    <xsl:param name="derivbase" />
    <xsl:param name="useIview" />
    <xsl:param name="encodedMainFile" />
    <xsl:param name="href" />

    <table cellpadding="0" cellspacing="0" id="detailed-contenttable">
      <tr id="detailed-contentsimg1">
        <td id="detailed-contentsimgpadd">
          <xsl:choose>
            <xsl:when test="$useIview = 'true'">
              <xsl:choose>
                <!-- links -->
                <xsl:when test="name() = 'derivateLink'">
                  <a href="{$href}">
                    <xsl:call-template name="iview2.getImageElement">
                      <xsl:with-param select="$derivID" name="derivate" />
                      <xsl:with-param select="concat('/', $encodedMainFile)" name="imagePath" />
                    </xsl:call-template>
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="derivateView">
                    <xsl:with-param name="derivateID" select="../../@ID" />
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <a href="{$href}">
                <img src="{concat($WebApplicationBaseURL,'images/dummyPreview.png')}" border="0" />
              </a>
            </xsl:otherwise>
          </xsl:choose>
          <br />
        </td>
      </tr>
      <tr id="detailed-contents">
        <td>
          <xsl:call-template name="jp.derivate.print.details">
            <xsl:with-param name="objID" select="$objID"/>
            <xsl:with-param name="objectXML" select="$objectXML" />
            <xsl:with-param name="derivbase" select="$derivbase" />
            <xsl:with-param name="encodedMainFile" select="$encodedMainFile" />
            <xsl:with-param name="href" select="''" />
          </xsl:call-template>
        </td>
      </tr>
    </table>
  </xsl:template>

  <!-- ========================================================== -->
  <!-- Details & Resultset -->
  <!-- ========================================================== -->
  <xsl:template name="jp.derivate.print.details">
    <xsl:param name="objID" />
    <xsl:param name="objectXML" />
    <xsl:param name="derivbase" />
    <xsl:param name="encodedMainFile" />
    <xsl:param name="href"/>

    <!-- has read access? -->
    <xsl:variable name="readAccess">
      <xsl:call-template name="jp.derivate.readAccess">
        <xsl:with-param name="objectXML" select="$objectXML" />
      </xsl:call-template>
    </xsl:variable>
    <!-- has edit access? -->
    <xsl:variable name="editAccess">
      <xsl:call-template name="jp.derivate.editAccess">
        <xsl:with-param name="objID" select="$objID" />
      </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$readAccess = 'true' or $editAccess = 'true'">
        <!-- get the label -->
        <xsl:variable name="label">
          <xsl:call-template name="jp.derivate.getFileLabel">
            <xsl:with-param name="file" select="$encodedMainFile"/>
          </xsl:call-template>
          <xsl:if test="name() = 'derivateLink'">
            <xsl:value-of select="' (~)'" />
          </xsl:if>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$href != ''">
            <!-- link to file -->
            <a href="{$href}">
              <xsl:value-of select="$label" />
            </a>
          </xsl:when>
          <xsl:otherwise>
            <!-- simple text -->
            <xsl:value-of select="$label" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'Zugriff gesperrt !'" />
      </xsl:otherwise>
    </xsl:choose>

    <xsl:if test="$editAccess = 'true'">
      <a href="{$derivbase}">
        <xsl:value-of select="', Details &gt;&gt; '" />
      </a>
    </xsl:if>
  </xsl:template>

  <!-- ========================================================== -->
  <!-- Checks if file is supported by iview2  -->
  <!-- ========================================================== -->
  <xsl:template name="jp.derivate.iview.isSupportedFile">
    <xsl:param name="objID" />
    <xsl:param name="file" />

    <xsl:variable name="fileType">
      <xsl:call-template name="jp.derivate.getFileType">
        <xsl:with-param name="fileName" select="$file" />
      </xsl:call-template>
    </xsl:variable>

    <xsl:value-of select="$fileType!='' and contains($MCR.Module-iview.SupportedContentTypes, $fileType)" />
  </xsl:template>

  <!-- ========================================================== -->
  <!-- Returns the label of a file  -->
  <!-- ========================================================== -->
  <xsl:template name="jp.derivate.getFileLabel">
    <xsl:param name="file" />
    <xsl:variable name="typeOfFile">
      <xsl:call-template name="jp.derivate.getFileType">
        <xsl:with-param name="fileName" select="$file" />
      </xsl:call-template>
    </xsl:variable>    
    <xsl:variable name="label">
      <xsl:value-of select="document('webapp:FileContentTypes.xml')/FileContentTypes/type[rules/extension/text()=$typeOfFile]/label/text()" />
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$label = ''">
        <xsl:value-of select="concat(' ',i18n:translate('metaData.digitalisat'),' (',$typeOfFile,') ')" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$label" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ========================================================== -->
  <!-- Returns the type of a file  -->
  <!-- ========================================================== -->
  <xsl:template name="jp.derivate.getFileType">
    <xsl:param name="fileName" />
    <xsl:value-of select="substring-after(substring($fileName,string-length($fileName)-4), '.')" />
  </xsl:template>

  <!-- ========================================================== -->
  <!-- Checks for read access -->
  <!-- ========================================================== -->
  <xsl:template name="jp.derivate.readAccess">
    <xsl:param name="objectXML" />
    <!-- get journal ID -->
    <xsl:variable name="jID">
      <xsl:choose>
        <xsl:when test="$journalID != ''">
          <xsl:value-of select="$journalID" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="xalan:nodeset($objectXML)/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="acl:checkPermission($jID,'read-derivates')" />
  </xsl:template>

  <!-- ========================================================== -->
  <!-- Checks for edit access -->
  <!-- ========================================================== -->
  <xsl:template name="jp.derivate.editAccess">
    <xsl:param name="objID" />
    <xsl:value-of select="acl:checkPermission($objID,'writedb') or acl:checkPermission($objID,'deletedb')" />
  </xsl:template>

  <!-- ========================================================== -->
  <!-- mapps a file with fileMappings.xml -->
  <!-- ========================================================== -->
  <xsl:template name="jp.derivate.mappFile">
    <xsl:param name="derivid-if" />
    <xsl:param name="filePath" />
    <xsl:param name="fileName" />

    <xsl:variable name="fileMappings" select="document('webapp:fileMappings.xml')" />

    <xsl:choose>
      <!-- file mapping(s) available ? -->
      <xsl:when test="$fileMappings/fileMappings/fileMapping">
        <!-- contains the mapped files separated by comma -->
        <xsl:variable name="transFileList">
          <!-- derivate root path -->
          <xsl:variable name="rootPath" select="substring-before($filePath,$fileName)" />
          <!-- file name without extension -->
          <xsl:variable name="fileNameWithoutExt" select="substring-before($fileName,'.')" />
          <xsl:variable name="derivXML" select="document(concat('ifs:',$derivid-if, $rootPath))" />
          <xsl:variable name="contentTypeId">
            <xsl:value-of select="$derivXML/mcr_directory/children/child/name[text()=$fileName]/../contentType" />
          </xsl:variable>
          <xsl:variable name="fileContentTypes" select="document('webapp:FileContentTypes.xml')" />
          <!-- file type exist AND file type must be mapped -->
          <xsl:if test="$fileMappings/fileMappings/fileMapping/type[@ID=$contentTypeId]">
            <!-- go through all mappable file extension id's -->
            <xsl:for-each select="$fileMappings/fileMappings/fileMapping[type/@ID=$contentTypeId]/mappTo/type">
              <xsl:variable name="mapId" select="@ID" />
              <xsl:for-each select="$derivXML/mcr_directory/children/child[starts-with(name, $fileNameWithoutExt)]">
                <xsl:if test="contentType = $mapId">
                  <xsl:value-of select="concat(name, ',')" />
                </xsl:if>
              </xsl:for-each>
            </xsl:for-each>
          </xsl:if>
        </xsl:variable>

        <!-- return translated file -->
        <xsl:choose>
          <xsl:when test="$transFileList">
            <xsl:value-of select="substring-before($transFileList, ',')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$filePath" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$filePath" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>