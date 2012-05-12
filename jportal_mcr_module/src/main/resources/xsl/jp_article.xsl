<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mcr="http://www.mycore.org/"
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
  <xsl:param select="'local'" name="objectHost" />
  <!-- =============================================================================================== -->
  <!--Template for result list hit: see results.xsl -->
  <xsl:template match="mcr:hit[contains(@id,'_jparticle_')]">
    <xsl:param name="mcrobjlink" />
    <xsl:param name="overwriteLayout" />
    <xsl:variable select="100" name="DESCRIPTION_LENGTH" />
    <xsl:variable select="@host" name="host" />
    <xsl:variable name="obj_id">
      <xsl:value-of select="@id" />
    </xsl:variable>

    <xsl:variable name="cXML">
      <xsl:copy-of select="document(concat('mcrobject:',@id))" />
    </xsl:variable>

    <xsl:variable name="allowHTMLInResultLists-IF">
      <xsl:choose>
        <xsl:when
          test="document(concat('mcrobject:',xalan:nodeset($cXML)/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()))/mycoreobject/metadata/hidden_genhiddenfields1/hidden_genhiddenfield1/text() = 'allowHTML'">
          <xsl:value-of select="'true'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'false'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <table cellspacing="0" cellpadding="0" id="leaf-all">
      <tr>
        <td id="leaf-front" colspan="1" rowspan="4">
          <img src="{$WebApplicationBaseURL}images/artikel2.gif" />
        </td>
        <td id="leaf-linkarea2">
          <xsl:variable name="name">
            <xsl:call-template name="ShortenText">
              <xsl:with-param name="text" select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
              <xsl:with-param name="length" select="125" />
            </xsl:call-template>
          </xsl:variable>

          <xsl:variable name="date">
            <xsl:choose>
              <xsl:when test="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0']">
                <xsl:variable name="date">
                  <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date/text()" />
                </xsl:variable>
                <xsl:value-of select="concat(' (',$date,')')" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="''" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="label">
            <xsl:value-of select="concat($name,$date)" />
          </xsl:variable>
          <xsl:call-template name="objectLinking">
            <xsl:with-param name="obj_id" select="@id" />
            <xsl:with-param name="obj_name" select="$label" />
            <xsl:with-param name="requestParam" select="'XSL.view.objectmetadata.SESSION=false&amp;XSL.toc.pos.SESSION=0'" />
            <xsl:with-param name="hoverText" select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
            <xsl:with-param name="allowHTMLInResultLists" select="$allowHTMLInResultLists-IF" />
          </xsl:call-template>
        </td>
      </tr>
      <xsl:call-template name="printDerivates">
        <xsl:with-param name="obj_id" select="@id" />
        <xsl:with-param name="knoten" select="xalan:nodeset($cXML)" />
      </xsl:call-template>
      <tr>
        <td>
          <xsl:call-template name="lineSpace" />
          <span id="leaf-published">
            <i>
              <xsl:value-of select="i18n:translate('metaData.published')" />
              <xsl:text>: </xsl:text>
            </i>
            <xsl:call-template name="printHistoryRow">
              <xsl:with-param name="sortOrder" select="'descending'" />
              <xsl:with-param name="printCurrent" select="'false'" />
              <xsl:with-param name="node" select="xalan:nodeset($cXML)" />
            </xsl:call-template>
          </span>
        </td>
      </tr>
    </table>
    <table cellspacing="0" cellpadding="0">
      <tr id="leaf-whitespaces">
        <td></td>
      </tr>
    </table>

  </xsl:template>

  <!-- =============================================================================================== -->
  <xsl:template match="/mycoreobject[contains(@ID,'_jparticle_')]" mode="toc">
    <table cellspacing="0" cellpadding="0" id="leaf-all">
      <!-- title -->
      <tr>
        <td id="leaf-front" colspan="1" rowspan="6">
          <img src="{$WebApplicationBaseURL}images/artikel2.gif" />
        </td>
        <td id="leaf-linkarea2">
          <xsl:variable name="name">
            <xsl:value-of select="/mycoreobject/metadata/maintitles/maintitle/text()" />
          </xsl:variable>
          <xsl:variable name="date">
            <xsl:choose>
              <xsl:when test="/mycoreobject/metadata/dates/date[@inherited='0']">
                <xsl:variable name="date">
                  <xsl:value-of select="/mycoreobject/metadata/dates/date/text()" />
                </xsl:variable>
                <xsl:value-of select="concat(' (',$date,')')" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="''" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="label">
            <xsl:value-of select="concat($name,$date)" />
          </xsl:variable>
          <xsl:variable name="shortlabel">
            <xsl:call-template name="ShortenText">
              <xsl:with-param name="text" select="$label" />
              <xsl:with-param name="length" select="400" />
            </xsl:call-template>
          </xsl:variable>
          <xsl:call-template name="objectLinking">
            <xsl:with-param name="obj_id" select="@ID" />
            <xsl:with-param name="obj_name" select="$shortlabel" />
            <xsl:with-param name="hoverText" select="$name" />
            <xsl:with-param name="requestParam" select="'XSL.view.objectmetadata.SESSION=false'" />
          </xsl:call-template>
        </td>
      </tr>
      <!-- date -->
      <xsl:if test="/mycoreobject/metadata/dates/date[@inherited='0' and @type='published']">
        <tr>
          <td id="leaf-additional">
            <xsl:call-template name="lineSpace" />
            <xsl:value-of select="concat(i18n:translate('editormask.labels.date_label'),': ')" />
            <xsl:variable name="format">
              <xsl:choose>
                <xsl:when test="string-length(normalize-space(/mycoreobject/metadata/dates/date[@inherited='0']))=4">
                  <xsl:value-of select="i18n:translate('metaData.dateYear')" />
                </xsl:when>
                <xsl:when test="string-length(normalize-space(/mycoreobject/metadata/dates/date[@inherited='0']))=7">
                  <xsl:value-of select="i18n:translate('metaData.dateYearMonth')" />
                </xsl:when>
                <xsl:when test="string-length(normalize-space(/mycoreobject/metadata/dates/date[@inherited='0']))=10">
                  <xsl:value-of select="i18n:translate('metaData.dateYearMonthDay')" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="i18n:translate('metaData.dateTime')" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:for-each select="/mycoreobject/metadata/dates/date[@inherited='0' and @type='published']">
              <xsl:call-template name="formatISODate">
                <xsl:with-param name="date" select="/mycoreobject/metadata/dates/date[@inherited='0' and @type='published']/text()" />
                <xsl:with-param name="format" select="$format" />
              </xsl:call-template>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>
      <!-- authors -->
      <tr>
        <td id="leaf-additional">
          <xsl:call-template name="getAuthorList">
            <xsl:with-param name="objectXML" select="." />
            <xsl:with-param name="listLength" select="5" />
          </xsl:call-template>
        </td>
      </tr>

      <!-- page area -->
      <xsl:if test="/mycoreobject/metadata/sizes/size">
        <tr>
          <td id="leaf-additional">
            <xsl:call-template name="lineSpace" />
            <i>
              <xsl:value-of select="concat(i18n:translate('editormask.labels.size'),': ')" />
            </i>
            <xsl:copy-of select="/mycoreobject/metadata/sizes/size/text()" />
          </td>
        </tr>
      </xsl:if>

      <!-- rubric -->
      <xsl:if test="/mycoreobject/metadata/rubrics/rubric">
        <tr>
          <td id="leaf-additional">
            <xsl:call-template name="lineSpace" />
            <i>
              <xsl:value-of select="i18n:translate('editormask.labels.rubric')" />
              :
            </i>
            <xsl:call-template name="printClass">
              <xsl:with-param name="nodes" select="/mycoreobject/metadata/rubrics/rubric" />
              <xsl:with-param name="host" select="'local'" />
              <xsl:with-param name="next" select="', '" />
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>

      <!-- derivates -->
      <tr>
        <td>
          <table border="0" cellspacing="0" cellpadding="0">
            <xsl:call-template name="printDerivates">
              <xsl:with-param name="obj_id" select="@ID" />
              <xsl:with-param name="knoten" select="./.." />
            </xsl:call-template>
          </table>
        </td>
      </tr>
    </table>
    <br />
  </xsl:template>

  <!-- =============================================================================================== -->
  <xsl:template match="mcr:hit[contains(@id,'_jparticle_')]" mode="toc">
    <xsl:param name="mcrobj" select="document(concat('mcrobject:',@id))" />
    <xsl:apply-templates select="$mcrobj" mode="toc" />
  </xsl:template>

  <!-- ================================================================================================================= -->
  <!-- Latest objects -->
  <xsl:template match="mcr:hit[contains(@id,'_jparticle_')]" mode="latestObjects">
    <xsl:param name="mcrobj" />
    <xsl:param name="mcrobjlink" />
    <xsl:variable select="100" name="DESCRIPTION_LENGTH" />
    <xsl:variable select="@host" name="host" />
    <xsl:variable name="obj_id">

      <xsl:value-of select="@id" />
    </xsl:variable>

    <xsl:variable name="cXML">
      <xsl:copy-of select="document(concat('mcrobject:',@id))" />
    </xsl:variable>
    <table id="horizontal">
      <tr>
        <td>
          <table cellspacing="0" cellpadding="0" id="leaf-all">
            <tr>
              <td id="leaf-front" colspan="1" rowspan="3">
                <img src="{$WebApplicationBaseURL}images/artikel2.gif" />
              </td>
              <td id="leaf-linkarea2">
                <xsl:variable name="name">
                  <xsl:call-template name="ShortenText">
                    <xsl:with-param name="text" select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
                    <xsl:with-param name="length" select="125" />
                  </xsl:call-template>
                </xsl:variable>

                <xsl:variable name="author-temp">
                  <authors>
                    <xsl:for-each
                      select="xalan:nodeset($cXML)/mycoreobject/metadata/participants/participant[@inherited='0' and contains(@xlink:href,'person')]">
                      <author>
                        <xsl:value-of select="./@xlink:href" />
                      </author>
                    </xsl:for-each>
                  </authors>
                </xsl:variable>

                <xsl:variable name="author-count">
                  <xsl:value-of select="count(xalan:nodeset($author-temp)/authors/author)" />
                </xsl:variable>

                <xsl:variable name="author-list">
                  <xsl:for-each select="xalan:nodeset($author-temp)/authors/author[position() &lt; 4]">
                    <xsl:variable name="temp">
                      <xsl:call-template name="objectLink">
                        <xsl:with-param name="obj_id" select="." />
                      </xsl:call-template>
                    </xsl:variable>
                    <xsl:value-of select="concat('; ',$temp)" />
                  </xsl:for-each>
                  <xsl:if test='$author-count &gt; 3'>
                    <xsl:value-of select="' et al.'" />
                  </xsl:if>
                </xsl:variable>
                <xsl:variable name="author">
                  <xsl:choose>
                    <xsl:when test="$author-list!=''">
                      <xsl:value-of select="concat(substring($author-list,3),': ')" />
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="''" />
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <xsl:variable name="label">
                  <xsl:value-of select="concat($author,$name)" />
                </xsl:variable>
                <xsl:call-template name="objectLinking">
                  <xsl:with-param name="obj_id" select="@id" />
                  <xsl:with-param name="obj_name" select="$label" />
                  <xsl:with-param name="requestParam" select="'XSL.view.objectmetadata.SESSION=false&amp;XSL.toc.pos.SESSION=0'" />
                  <xsl:with-param name="hoverText" select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
                </xsl:call-template>
              </td>
            </tr>
            <tr>
              <td id="leaf-additional2">
                <xsl:variable name="size-temp">
                  <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/sizes/size[@inherited='0']/text()" />
                </xsl:variable>
                <xsl:variable name="size">
                  <xsl:if test="$size-temp!=''">
                    <xsl:value-of select="concat(', ',i18n:translate('editormask.labels.size'),': ',$size-temp)" />
                  </xsl:if>
                </xsl:variable>

                <xsl:variable name="journal-info">
                  <xsl:call-template name="printHistoryRow">
                    <xsl:with-param name="sortOrder" select="'descending'" />
                    <xsl:with-param name="printCurrent" select="'false'" />
                    <xsl:with-param name="node" select="xalan:nodeset($cXML)" />
                  </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="label2">
                  <xsl:value-of select="concat('in: ',substring($journal-info,1,string-length($journal-info)-2),$size)" />
                </xsl:variable>
                <xsl:copy-of select="$label2" />
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </xsl:template>

  <!-- =============================================================================================== -->
  <xsl:key name="subtitles" match="subtitle" use="@type" />
  <xsl:key name="identis" match="identi" use="@type" />
  <xsl:key name="notes" match="note" use="@type" />
  <xsl:key name="participants" match="participant" use="@type" />
  <xsl:variable name="classificationXML">
    <class tag="participants">jportal_class_00000007</class>
    <class tag="subtitles">jportal_class_00000006</class>
    <class tag="identis">jportal_class_00000010</class>
    <class tag="notes">jportal_class_00000060</class>
  </xsl:variable>
  <xsl:variable name="classification" select="xalan:nodeset($classificationXML)" />

  <xsl:template mode="metadataDisplay" match="metadata/child::node()[@class='MCRMetaLangText' and child::node()[@type!='']]">
    <xsl:variable name="currentTagName" select="name()" />
    <xsl:variable name="classID" select="$classification/class[@tag=$currentTagName]" />
    <xsl:variable name="mcrClass" select="@class" />
    <dl>
      <xsl:for-each select="child::node()[generate-id(.)=generate-id(key($currentTagName, @type)[1])]">
        <xsl:variable name="categID" select="@type" />
        <xsl:variable name="label"
          select="document(concat('classification:metadata:all:children:',$classID,':',$categID))/mycoreclass/categories/category[@ID=$categID]/label[@xml:lang=$CurrentLang]/@text" />

        <dt>
          <xsl:value-of select="concat($label, ': ')" />
        </dt>
        <dd>
          <xsl:for-each select="key($currentTagName, @type)">
            <xsl:sort select="./text()" />
            <xsl:value-of select="text()" />
            <xsl:if test="position() != last()">
              <xsl:value-of select="'; '" />
            </xsl:if>
            <!-- <xsl:call-template name="$mcrClass" select="."/> -->
          </xsl:for-each>
        </dd>
      </xsl:for-each>
    </dl>
  </xsl:template>

  <xsl:template name="MCRMetaLangText">
    <xsl:value-of select="text()" />
    <xsl:if test="position() != last()">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataDisplay" match="metadata/child::node()[@class='MCRMetaLinkID' and child::node()[@type!='']]">
    <xsl:variable name="currentTagName" select="name()" />
    <xsl:variable name="classID" select="$classification/class[@tag=$currentTagName]" />
    <dl>
      <xsl:for-each select="child::node()[generate-id(.)=generate-id(key($currentTagName, @type)[1])]">
        <xsl:variable name="categID" select="@type" />
        <xsl:variable name="label"
          select="document(concat('classification:metadata:all:children:',$classID,':',$categID))/mycoreclass/categories/category[@ID=$categID]/label[@xml:lang=$CurrentLang]/@text" />

        <dt>
          <xsl:value-of select="concat($label, ': ')" />
        </dt>
        <dd>
          <xsl:for-each select="key($currentTagName, @type)">
            <xsl:sort select="./@xlink:title" />
            <a href="{$WebApplicationBaseURL}receive/{@xlink:href}">
              <xsl:value-of select="@xlink:title" />
            </a>
            <xsl:if test="position() != last()">
              <xsl:value-of select="'; '" />
            </xsl:if>
          </xsl:for-each>
        </dd>
      </xsl:for-each>
    </dl>
  </xsl:template>

  <!--Template for metadata view: see mycoreobject.xsl -->
  <xsl:template priority="1" mode="present" match="/mycoreobject[contains(@ID,'_jparticle_')]">
    <xsl:param select="$objectHost" name="obj_host" />
    <xsl:param name="accessedit" />
    <xsl:param name="accessdelete" />

    <xsl:variable name="objectBaseURL">
      <xsl:if test="$objectHost != 'local'">
        <xsl:value-of select="document('webapp:hosts.xml')/mcr:hosts/mcr:host[@alias=$objectHost]/mcr:url[@type='object']/@href" />
      </xsl:if>
      <xsl:if test="$objectHost = 'local'">
        <xsl:value-of select="concat($WebApplicationBaseURL,'receive/')" />
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="staticURL">
      <xsl:value-of select="concat($objectBaseURL,@ID)" />
    </xsl:variable>

    <xsl:message>
      maintitles:
      <xsl:value-of select="count(/mycoreobject[contains(@ID,'_jparticle_')]/metadata/child::node()[@class='MCRMetaLangText'])" />
    </xsl:message>
    <div id="detailed-frame" class="jp-layout-metadata">
      <h3>
        <xsl:choose>
          <xsl:when test="$allowHTMLInArticles = 'true'">
            <xsl:value-of disable-output-escaping="yes" select="./metadata/maintitles/maintitle[@inherited='0']/text()" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="./metadata/maintitles/maintitle[@inherited='0']/text()" />
          </xsl:otherwise>
        </xsl:choose>
      </h3>

      <xsl:apply-templates mode="metadataDisplay" select="metadata/child::node()[name() != 'maintitles' and not(contains(name(), 'hidden_'))]" />

      <!-- #################################### -->
      <xsl:variable name="mainTitle">
        <xsl:choose>
          <xsl:when test="$allowHTMLInArticles = 'true'">
            <xsl:value-of disable-output-escaping="yes" select="./metadata/maintitles/maintitle[@inherited='0']/text()" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="./metadata/maintitles/maintitle[@inherited='0']/text()" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="maintitle_shorted">
        <xsl:call-template name="ShortenText">
          <xsl:with-param name="text" select="./metadata/maintitles/maintitle[@inherited='0']/text()" />
          <xsl:with-param name="length" select="150" />
        </xsl:call-template>
      </xsl:variable>
      <table border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td id="detailed-cube">
            <img src="{$WebApplicationBaseURL}images/artikel.gif" />
          </td>
          <td id="detailed-mainheadline">
            <div id="detailed-headline-frame">
              <xsl:choose>
                <xsl:when test="$allowHTMLInArticles = 'true'">
                  <xsl:value-of disable-output-escaping="yes" select="$maintitle_shorted" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$maintitle_shorted" />
                </xsl:otherwise>
              </xsl:choose>
            </div>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <div id="detailed-derivate-div">
              <table id="detailed-contenttable" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td>
                    <table border="0" cellspacing="0" cellpadding="0">
                      <xsl:call-template name="printDerivates">
                        <xsl:with-param name="obj_id" select="@ID" />
                      </xsl:call-template>
                    </table>
                  </td>
                </tr>
              </table>
            </div>
            <xsl:choose>
              <xsl:when
                test="(./metadata/identis/identi | ./metadata/sizes/size 
                                | ./metadata/dates/date[@inherited='0']/text()
                                | ./metadata/participants/participant | ./metadata/subtitles/subtitle)">
                <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                  <tr>
                    <td id="detailed-headlines">
                      <xsl:value-of select="i18n:translate('metaData.headlines.formaldiscr')" />
                    </td>
                    <td>
                      <br />
                    </td>
                  </tr>
                </table>
              </xsl:when>
              <xsl:when test="string-length($mainTitle)>150">
                <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                  <tr>
                    <td id="detailed-headlines">
                      <xsl:value-of select="i18n:translate('metaData.headlines.formaldiscr')" />
                    </td>
                    <td>
                      <br />
                    </td>
                  </tr>
                </table>
              </xsl:when>
            </xsl:choose>
            <!--1***maintitle************************************* -->
            <!-- only if headline cut -->
            <xsl:if test="string-length($mainTitle)>150">
              <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                <tr>
                  <td valign="top" id="detailed-labels">
                    <xsl:value-of select="i18n:translate('editormask.labels.bibdescript')" />
                  </td>
                  <td class="metavalue">
                    <xsl:choose>
                      <xsl:when test="$allowHTMLInArticles = 'true'">
                        <xsl:call-template name="printI18N-allowHTML">
                          <xsl:with-param name="nodes" select="./metadata/maintitles/maintitle[@inherited='0']/text()" />
                        </xsl:call-template>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:call-template name="printI18N">
                          <xsl:with-param name="nodes" select="./metadata/maintitles/maintitle[@inherited='0']/text()" />
                        </xsl:call-template>
                      </xsl:otherwise>
                    </xsl:choose>
                  </td>
                </tr>
              </table>
            </xsl:if>
            <!--2***subtitle************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="printMetaDate_typeSensitive">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/subtitles/subtitle" name="nodes" />
                <xsl:with-param select="i18n:translate('editormask.labels.subtitle')" name="label" />
                <xsl:with-param name="typeClassi" select="'jportal_class_00000006'" />
                <xsl:with-param name="mode" select="'text'" />
              </xsl:call-template>
            </table>
            <!--3***participant************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="printMetaDate_typeSensitive">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/participants/participant" name="nodes" />
                <xsl:with-param select="i18n:translate('editormask.labels.participants_label')" name="label" />
                <xsl:with-param name="typeClassi" select="'jportal_class_00000007'" />
                <xsl:with-param name="mode" select="'xlink'" />
              </xsl:call-template>
            </table>
            <!--4***date************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="printMetaDate_typeSensitive">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/dates/date[@inherited='0']" name="nodes" />
                <xsl:with-param select="i18n:translate('editormask.labels.date_label')" name="label" />
                <xsl:with-param name="typeClassi" select="'jportal_class_00000008'" />
                <xsl:with-param name="mode" select="'date'" />
              </xsl:call-template>
            </table>
            <!--5***size************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="printMetaDates">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/sizes/size" name="nodes" />
                <xsl:with-param select="i18n:translate('editormask.labels.size')" name="label" />
              </xsl:call-template>
            </table>
            <!--6***identi************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="printMetaDate_typeSensitive">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/identis/identi" name="nodes" />
                <xsl:with-param select="i18n:translate('editormask.labels.identi')" name="label" />
                <xsl:with-param name="typeClassi" select="'jportal_class_00000010'" />
                <xsl:with-param name="mode" select="'text'" />
              </xsl:call-template>
            </table>
            <xsl:if
              test="./metadata/keywords/keyword
                                | ./metadata/abstracts/abstract 
                                | ./metadata/notes/note[@type='annotation']
                                | ./metadata/rubrics/rubric
                                | ./metadata/classispub/classipub
                                | ./metadata/classispub2/classipub2
                                | ./metadata/classispub3/classipub3
                                | ./metadata/classispub4/classipub4
                                | ./metadata/types/type
                                | ./metadata/refs/ref
                                | ./metadata/subtitles/subtitle ">
              <table border="0" cellspacing="0" cellpadding="0" id="detailed-divlines">
                <tr>
                  <td colspan="2" id="detailed-innerdivlines">
                    <br />
                  </td>
                </tr>
              </table>
              <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                <tr>
                  <td id="detailed-headlines">
                    <xsl:value-of select="i18n:translate('metaData.headlines.contantdiscr')" />
                  </td>
                  <td>
                    <br />
                  </td>
                </tr>
              </table>
            </xsl:if>
            <!--7***keyword************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="printMetaDates">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/keywords/keyword" name="nodes" />
                <xsl:with-param select="i18n:translate('editormask.labels.keyword')" name="label" />
              </xsl:call-template>
            </table>
            <!--8***abstract************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="printMetaDates">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/abstracts/abstract" name="nodes" />
                <xsl:with-param select="i18n:translate('editormask.labels.abstract')" name="label" />
              </xsl:call-template>
            </table>
            <!--9***note************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="printMetaDate_typeSensitive">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/notes/note[@type='annotation']" name="nodes" />
                <xsl:with-param select="i18n:translate('editormask.labels.note')" name="label" />
                <xsl:with-param name="typeClassi" select="'jportal_class_00000060'" />
                <xsl:with-param name="mode" select="'text'" />
              </xsl:call-template>
            </table>
            <xsl:if test="$CurrentUser!='gast' and ./metadata/notes/note[@type!='annotation']">
              <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                <xsl:call-template name="printMetaDate_typeSensitive">
                  <xsl:with-param select="'right'" name="textalign" />
                  <xsl:with-param select="./metadata/notes/note[@type='internalNote']" name="nodes" />
                  <xsl:with-param select="i18n:translate('editormask.labels.note')" name="label" />
                  <xsl:with-param name="typeClassi" select="'jportal_class_00000060'" />
                  <xsl:with-param name="mode" select="'text'" />
                </xsl:call-template>
              </table>
            </xsl:if>

            <!--11***rubric************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="printMetaDates">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/rubrics/rubric" name="nodes" />
                <xsl:with-param select="i18n:translate('editormask.labels.rubric')" name="label" />
              </xsl:call-template>
            </table>
            <!--12***classipub************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:variable name="label_classipub">
                <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_classispub/hidden_classipub')//label/text()" />
              </xsl:variable>
              <xsl:call-template name="printMetaDates">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/classispub/classipub" name="nodes" />
                <xsl:with-param select="$label_classipub" name="label" />
              </xsl:call-template>
            </table>
            <!--13***classipub2************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:variable name="label_classipub2">
                <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_classispub2/hidden_classipub2')//label/text()" />
              </xsl:variable>
              <xsl:call-template name="printMetaDates">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/classispub2/classipub2" name="nodes" />
                <xsl:with-param select="$label_classipub2" name="label" />
              </xsl:call-template>
            </table>
            <!--14***classipub3************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:variable name="label_classipub3">
                <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_classispub3/hidden_classipub3')//label/text()" />
              </xsl:variable>
              <xsl:call-template name="printMetaDates">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/classispub3/classipub3" name="nodes" />
                <xsl:with-param select="$label_classipub3" name="label" />
              </xsl:call-template>
            </table>
            <!--15***classipub4************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:variable name="label_classipub4">
                <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_classispub4/hidden_classipub4')//label/text()" />
              </xsl:variable>
              <xsl:call-template name="printMetaDates">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/classispub4/classipub4" name="nodes" />
                <xsl:with-param select="$label_classipub4" name="label" />
              </xsl:call-template>
            </table>
            <!--10***type************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:variable name="label_type">
                <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_pubTypesID/hidden_pubTypeID')//label/text()" />
              </xsl:variable>
              <xsl:call-template name="printMetaDates">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/types/type" name="nodes" />
                <xsl:with-param select="$label_type" name="label" />
              </xsl:call-template>
            </table>
            <!--16***ref************************************* -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="printMetaDates">
                <xsl:with-param select="'right'" name="textalign" />
                <xsl:with-param select="./metadata/refs/ref" name="nodes" />
                <xsl:with-param select="i18n:translate('editormask.labels.pub_reference')" name="label" />
              </xsl:call-template>
            </table>

            <table border="0" cellspacing="0" cellpadding="0" id="detailed-divlines">
              <tr>
                <td colspan="2" id="detailed-innerdivlines">
                  <br />
                </td>
              </tr>
            </table>
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <tr>
                <td id="detailed-headlines">
                  <xsl:value-of select="i18n:translate('metaData.headlines.systemdata')" />
                </td>
                <td>
                  <br />
                </td>
              </tr>
            </table>
            <!-- System data ###################################################### -->
            <xsl:call-template name="get.systemData" />

            <!-- Static URL ************************************************** -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
              <xsl:call-template name="get.staticURL">
                <xsl:with-param name="stURL" select="$staticURL" />
              </xsl:call-template>
              <xsl:call-template name="emptyRow" />
            </table>
            <!-- Administration ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
            <xsl:call-template name="showAdminHead" />
            <!--*** Editor Buttons ************************************* -->
            <!-- <xsl:call-template name="jp_editobject_with_der"> <xsl:with-param select="$accessedit" name="accessedit"/> <xsl:with-param select="./@ID" 
              name="id"/> </xsl:call-template> -->
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">

              <xsl:call-template name="jp_editobject_with_der">
                <xsl:with-param select="$accessedit" name="accessedit" />
                <xsl:with-param select="./@ID" name="id" />
              </xsl:call-template>

            </table>
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <!-- ===================================================================================================== -->
  <!-- prints the list of authors -->
  <!-- ===================================================================================================== -->
  <xsl:template name="getAuthorList">
    <xsl:param name="objectXML" />
    <xsl:param name="listLength" />

    <xsl:for-each select="xalan:nodeset($objectXML)/metadata/participants/participant">
      <xsl:if test="position()=1">
        <xsl:call-template name="lineSpace" />
        <i>
          <xsl:value-of select="i18n:translate('editormask.labels.participants_label')" />
        </i>
        :
      </xsl:if>
      <xsl:if test="position()>1">
        <xsl:copy-of select="';  '" />
      </xsl:if>
      <xsl:variable name="mcrobj" select="document(concat('mcrobject:',@xlink:href))/mycoreobject" />
      <xsl:call-template name="objectLink">
        <xsl:with-param name="obj_id" select="@xlink:href" />
      </xsl:call-template>
    </xsl:for-each>
    <xsl:if test="number(count($objectXML/mycoreobject/metadata/participants/participant))>number($listLength)">
      <xsl:copy-of select="';   ...'" />
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>