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