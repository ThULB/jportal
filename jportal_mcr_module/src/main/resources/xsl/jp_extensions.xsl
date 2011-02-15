<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xlink mcr i18n acl xalan" xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities">

  <xsl:include href="jp_layout-commons.xsl" />
  <xsl:include href="jp_objectView.xsl" />
  <xsl:include href="jp_derivateView.xsl" />
  <xsl:include href="history.xsl" />

  <xsl:variable name="journalID">
    <xsl:call-template name="get.journalID" />
  </xsl:variable>
  <xsl:variable name="journalXML">
    <xsl:call-template name="get.journalXML" />
  </xsl:variable>


  <!-- ===================================================================================================== -->
  <!-- returns the page title of the current mcr object : TODO i18n -->
  <!-- ===================================================================================================== -->
  <xsl:template match="/mycoreobject" mode="pageTitle" priority="1">
    <xsl:choose>
      <xsl:when test="contains(/mycoreobject/@ID,'_jpjournal_') 
                or contains(/mycoreobject/@ID,'_jpvolume_') 
                or contains(/mycoreobject/@ID,'_jparticle_')  ">
        <xsl:call-template name="printHistoryRow">
          <xsl:with-param name="sortOrder" select="'descending'" />
          <xsl:with-param name="printCurrent" select="'true'" />
          <xsl:with-param name="node" select="./.." />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains(/mycoreobject/@ID,'_person_') ">
        <xsl:value-of select="'Person - Metadaten'" />
      </xsl:when>
      <xsl:when test="contains(/mycoreobject/@ID,'_jpinst_') ">
        <xsl:value-of select="'Institution - Metadaten'" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'Metadaten'" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


   <!-- =================================================================================================== -->
    <!--
        Template: getBrowserAddress
        synopsis: The template will be used to identify the currently selected menu entry and the belonging element item/@href in the navigationBase
        These strategies are embarked on:
        1. RequestURL - lang ?= @href - lang
        2. RequestURL - $WebApplicationBaseURL - lang ?= @href - lang
        3. Root element ?= item//dynamicContentBinding/rootTag
    -->

    <xsl:template name="jp_getBrowserAddress">
        <xsl:variable name="RequestURL.sessionRemoved">
            <xsl:call-template name="UrlDeleteSession">
                <xsl:with-param name="url" select="$RequestURL" />
            </xsl:call-template>
        </xsl:variable>
        <!--remove $lastPage-->
        <xsl:variable name="RequestURL.lastPageDel">
            <xsl:call-template name="UrlDelParam">
                <xsl:with-param name="url" select="$RequestURL.sessionRemoved" />
                <xsl:with-param name="par" select="'XSL.lastPage.SESSION'" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="RequestURL.WebURLDel">
            <xsl:value-of select="concat('/',substring-after($RequestURL.lastPageDel,$WebApplicationBaseURL))" />
        </xsl:variable>
        <!--remove $lang -->
        <xsl:variable name="cleanURL">
            <xsl:call-template name="UrlDelParam">
                <xsl:with-param name="url" select="$RequestURL.lastPageDel" />
                <xsl:with-param name="par" select="'lang'" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="cleanURL2">
            <xsl:call-template name="UrlDelParam">
                <xsl:with-param name="url" select="$RequestURL.WebURLDel" />
                <xsl:with-param name="par" select="'lang'" />
            </xsl:call-template>
        </xsl:variable>

        <!-- 1. case -->
        <!-- test if navigation.xml contains the current browser address -->
        <xsl:variable name="browserAddress_href">
            <xsl:value-of select="$loaded_navigation_xml//item[(@href=$cleanURL2) or (@href=$cleanURL)]/@href" />
        </xsl:variable>
        <!-- 2. case -->
        <!-- TODO: -->
        <!-- remove this code and remove tag(s) <dynamicContentBinding/> from navigation.xml -->
        <!-- look for $browserAddress_dynamicContentBinding -->
        <xsl:variable name="browserAddress_dynamicContentBinding">
            <xsl:if test="  ($browserAddress_href = '') ">
                <!-- assign name of rootTag -> $rootTag -->
                <xsl:variable name="rootTag" select="name(*)" />
                <xsl:for-each select="$loaded_navigation_xml//dynamicContentBinding/rootTag">
                    <xsl:if test=" current() = $rootTag ">
                        <xsl:for-each select="ancestor-or-self::*[@href]">
                            <xsl:if test="position()=last()">
                                <xsl:value-of select="@href" />
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>
        </xsl:variable>
        <!-- 3. case -->
        <!-- nothing look for $lastPage -->
        <xsl:variable name="browserAddress_lastPage">
            <xsl:if test=" ($browserAddress_href = '') and ($browserAddress_dynamicContentBinding = '') ">
                <xsl:call-template name="get.rightPage" />
            </xsl:if>
        </xsl:variable>

        <!-- assign right browser address -->
        <xsl:choose>
            <xsl:when test=" $browserAddress_href != '' ">
                <xsl:value-of select="$browserAddress_href" />
                <!-- store in session -->
                <xsl:variable name="dummy" select="layoutUtils:setLastValidPageID($browserAddress_href)" />
            </xsl:when>
            <xsl:when test=" $browserAddress_dynamicContentBinding != '' ">
                <xsl:value-of select="$browserAddress_dynamicContentBinding" />
                <!-- store in session -->
                <xsl:variable name="dummy" select="layoutUtils:setLastValidPageID($browserAddress_dynamicContentBinding)" />
            </xsl:when>
            <xsl:when test=" $browserAddress_lastPage != '' ">
                <xsl:value-of select="$browserAddress_lastPage" />
                <xsl:variable name="dummy" select="layoutUtils:setLastValidPageID($browserAddress_lastPage)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$loaded_navigation_xml/@hrefStartingPage" />
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

  <!-- ===================================================================================================== -->
  <!-- returns the correct browser address. trys to resolve the address from the 
       navigation.xml and the hidden websitecontext -->
  <!-- ===================================================================================================== -->
  <xsl:template name="get.rightPage">
    <xsl:variable name="journalXML">
      <xsl:if test="/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()">
        <xsl:call-template name="getJournalXML">
          <xsl:with-param name="id" select="/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()" />
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="lastPage">
      <!-- get from session -->
      <xsl:variable name="lastPageID" select="layoutUtils:getLastValidPageID()" />
      <xsl:value-of xmlns:decoder="xalan://java.net.URLDecoder" select="decoder:decode($lastPageID,'UTF-8')" />
    </xsl:variable>

    <xsl:variable name="object_webContext">
      <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext/text()" />
    </xsl:variable>

    <xsl:choose>
      <!-- jpjournal or jpvolume or jparticle with own webcontext called -->
      <!-- webcontext is not empty AND $navigation.xml contains webcontext -->
      <xsl:when test="($object_webContext!='') and ($loaded_navigation_xml//item[@href=$object_webContext])">
        <!-- does $lastPage exist? -->
        <xsl:choose>
          <xsl:when test="($lastPage!='') and ($loaded_navigation_xml//item[@href=$lastPage])">
            <xsl:for-each select="$loaded_navigation_xml//item[@href=$lastPage]">
              <xsl:choose>
                <!-- $webcontext within ancestor axis ? -> choose $lastPage -->
                <xsl:when test="ancestor::item[@href=$object_webContext]">
                  <xsl:value-of select="$lastPage" />
                </xsl:when>
                <!-- $webcontext NOT within ancestor axis ? -> choose $webcontext -->
                <xsl:otherwise>
                  <xsl:value-of select="$object_webContext" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$object_webContext" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of xmlns:decoder="xalan://java.net.URLDecoder" select="decoder:decode($lastPage,'UTF-8')" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ===================================================================================================== -->
  <!-- returns the xml document of an given id -->
  <!-- ===================================================================================================== -->
  <xsl:template name="getJournalXML">
    <xsl:param name="id" />
    <xsl:copy-of select="document(concat('mcrobject:',$id))" />
  </xsl:template>

  <!-- ===================================================================================================== -->
  <!-- returns 'true' if the given flag of an mcrobject is set -->
  <!-- ===================================================================================================== -->
  <xsl:template name="isFlagSet">
    <xsl:param name="flagName" />
    <xsl:param name="path" select="." />

    <xsl:for-each select="$path/service/servflags/servflag">
      <xsl:if test="text() = $flagName" >
        <xsl:value-of select="'true'" />
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- ===================================================================================================== -->
  <!-- returns 'true' if the type of a flag is equals flagType -->
  <!-- ===================================================================================================== -->
  <xsl:template name="isFlagTypeSet">
    <xsl:param name="flagType" />
    <xsl:for-each select="./service/servflags/servflag">
      <xsl:if test="@type = $flagType" >
        <xsl:value-of select="'true'" />
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- ================================================================================================================= -->
  <!-- prints a simple div line -->
  <!-- ================================================================================================================= -->
  <xsl:template name="lineSpace">
    <div style="height:0.7em;" />
  </xsl:template>

  <!-- ============================================================================================================================ -->
  <!-- prints an empty row -->
  <!-- ============================================================================================================================ -->
  <xsl:template name="emptyRow">
    <tr>
      <td>
        <br />
        <br />
      </td>
      <td>
        <br />
        <br />
      </td>
    </tr>
  </xsl:template>

  <!-- ================================================================================================================= -->
  <!-- returns the xml document of journal object -->
  <!-- ================================================================================================================= -->
  <xsl:template name="get.journalXML">
    <xsl:if test="$journalID != ''">
      <xsl:copy-of select="document(concat('mcrobject:',$journalID))" />
    </xsl:if>
  </xsl:template>

  <!-- ===================================================================================================== -->
  <!-- returns the mcr id of the current journal -->
  <!-- ===================================================================================================== -->
  <xsl:template name="get.journalID">
    <xsl:value-of select="document('jportal_getJournalID:noXPath')/dummyRoot/hidden/@default" />
  </xsl:template>

	<!-- ================================================================================= -->
    <xsl:template name="HTMLPageTitle">

        <xsl:variable name="titleFront">
            <xsl:choose>
                <xsl:when
                    test="contains(/mycoreobject/@ID,'_jpjournal_') 
				or contains(/mycoreobject/@ID,'_jpvolume_') 
				or contains(/mycoreobject/@ID,'_jparticle_')  ">
                    <xsl:copy-of select="concat(/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text(),' (')" />
                    <xsl:call-template name="printHistoryRow">
                        <xsl:with-param name="sortOrder" select="'descending'" />
                        <xsl:with-param name="printCurrent" select="'false'" />
                    </xsl:call-template>
                    <xsl:copy-of select="')'" />
                </xsl:when>
                <xsl:when test="contains(/mycoreobject/@ID,'_jpinst_') ">
                    <xsl:copy-of select="/mycoreobject/metadata/names/name/fullname/text()" />
                </xsl:when>
                <xsl:when test="contains(/mycoreobject/@ID,'_person_') ">
                    <xsl:copy-of select="/mycoreobject/metadata/def.heading/heading/lastName/text()" />
                    <xsl:if test="/mycoreobject/metadata/def.heading/heading/firstName/text()">
                        <xsl:copy-of select="concat(', ',/mycoreobject/metadata/def.heading/heading/firstName/text())" />
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$PageTitle" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:value-of select="concat($titleFront,' - ',$MainTitle)" />
    </xsl:template>
    <!-- ================================================================================= -->

</xsl:stylesheet>