<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mcr="http://www.mycore.org/"
    xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
    <xsl:param select="'local'" name="objectHost" />
    <!-- =============================================================================================== -->
    <!--Template for result list hit: see results.xsl-->
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
                    </xsl:call-template>
                </td>
            </tr>
            <xsl:call-template name="printDerivates">
                <xsl:with-param name="obj_id" select="@id" />
                <xsl:with-param name="knoten" select="$cXML" />
            </xsl:call-template>
            <tr>
                <td>
                    <span id="leaf-published">
                        <xsl:value-of select="i18n:translate('metaData.published')" />
                        <xsl:text>: </xsl:text>
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
    <xsl:template match="mcr:hit[contains(@id,'_jparticle_')]" mode="toc">
        <xsl:param name="mcrobj" />
        <xsl:param name="mcrobjlink" />

        <xsl:variable name="cXML">
            <xsl:copy-of select="document(concat('mcrobject:',@id))" />
        </xsl:variable>
        <table cellspacing="0" cellpadding="0" id="leaf-all">
            <!-- title -->
            <tr>
                <td id="leaf-front" colspan="1" rowspan="6">
                    <img src="{$WebApplicationBaseURL}images/artikel2.gif" />
                </td>
                <td id="leaf-linkarea2">
                    <xsl:variable name="name">
                        <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
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
                    <xsl:variable name="shortlabel">
                        <xsl:call-template name="ShortenText">
                            <xsl:with-param name="text" select="$label" />
                            <xsl:with-param name="length" select="400" />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="children">
                        <xsl:choose>
                            <xsl:when test="(xalan:nodeset($cXML)/mycoreobject/structure/children)">
                                <xsl:value-of select="'true'" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="'false'" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="(contains(@id,'_jparticle_')) 
              or ($children='false') ">
                            <xsl:call-template name="objectLinking">
                                <xsl:with-param name="obj_id" select="@id" />
                                <xsl:with-param name="obj_name" select="$shortlabel" />
                                <xsl:with-param name="hoverText" select="$name" />
                                <xsl:with-param name="requestParam" select="'XSL.view.objectmetadata.SESSION=false'" />
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="objectLinking">
                                <xsl:with-param name="obj_id" select="@id" />
                                <xsl:with-param name="obj_name" select="$shortlabel" />
                                <xsl:with-param name="hoverText" select="$name" />
                                <xsl:with-param name="requestParam" select="'XSL.view.objectmetadata.SESSION=true&amp;XSL.toc.pos.SESSION=1'" />
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
            <!-- date -->
            <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0' and @type='published']">
                <tr>
                    <td id="leaf-additional">
                        <xsl:call-template name="lineSpace" />
                        <xsl:value-of select="concat(i18n:translate('editormask.labels.date_label'),': ')" />
                        <xsl:variable name="format">
                            <xsl:choose>
                                <xsl:when test="string-length(normalize-space(xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0']))=4">
                                    <xsl:value-of select="i18n:translate('metaData.dateYear')" />
                                </xsl:when>
                                <xsl:when test="string-length(normalize-space(xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0']))=7">
                                    <xsl:value-of select="i18n:translate('metaData.dateYearMonth')" />
                                </xsl:when>
                                <xsl:when test="string-length(normalize-space(xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0']))=10">
                                    <xsl:value-of select="i18n:translate('metaData.dateYearMonthDay')" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="i18n:translate('metaData.dateTime')" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:for-each select="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0' and @type='published']">
                            <xsl:call-template name="formatISODate">
                                <xsl:with-param name="date"
                                    select="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0' and @type='published']/text()" />
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
                        <xsl:with-param name="objectXML" select="xalan:nodeset($cXML)" />
                        <xsl:with-param name="listLength" select="5" />
                    </xsl:call-template>
                </td>
            </tr>

            <!-- page area -->
            <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/sizes/size">
                <tr>
                    <td id="leaf-additional">
                        <xsl:call-template name="lineSpace" />
                        <i>
                            <xsl:value-of select="concat(i18n:translate('editormask.labels.size'),': ')" />
                        </i>
                        <xsl:copy-of select="xalan:nodeset($cXML)/mycoreobject/metadata/sizes/size/text()" />
                    </td>
                </tr>
            </xsl:if>

            <!-- rubric -->
            <tr>
                <td id="leaf-additional">
                    <xsl:call-template name="lineSpace" />
                    <i>
                        <xsl:value-of select="i18n:translate('editormask.labels.rubric')" />
                        :
                    </i>
                    <xsl:call-template name="printClass">
                        <xsl:with-param name="nodes" select="xalan:nodeset($cXML)/mycoreobject/metadata/rubrics/rubric" />
                        <xsl:with-param name="host" select="'local'" />
                    </xsl:call-template>
                </td>
            </tr>

            <!-- derivates -->
            <xsl:call-template name="printDerivates">
                <xsl:with-param name="obj_id" select="@id" />
                <xsl:with-param name="knoten" select="$cXML" />
            </xsl:call-template>
        </table>
        <br />
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
    
    <!--Template for metadata view: see mycoreobject.xsl-->
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
        <div id="detailed-frame">
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
                            <xsl:when test="string-length($mainTitle)>150)">
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
                        <!--1***maintitle*************************************-->
                        <!-- only if headline cut -->
                        <xsl:if test="string-length($mainTitle)>150)">
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
                        <!--2***subtitle*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDate_typeSensitive">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/subtitles/subtitle" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.subtitle')" name="label" />
                                <xsl:with-param name="typeClassi" select="'jportal_class_00000006'" />
                                <xsl:with-param name="mode" select="'text'" />
                            </xsl:call-template>
                        </table>
                        <!--3***participant*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDate_typeSensitive">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/participants/participant" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.participants_label')" name="label" />
                                <xsl:with-param name="typeClassi" select="'jportal_class_00000007'" />
                                <xsl:with-param name="mode" select="'xlink'" />
                            </xsl:call-template>
                        </table>
                        <!--4***date*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDate_typeSensitive">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/dates/date[@inherited='0']" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.date_label')" name="label" />
                                <xsl:with-param name="typeClassi" select="'jportal_class_00000008'" />
                                <xsl:with-param name="mode" select="'date'" />
                            </xsl:call-template>
                        </table>
                        <!--5***size*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/sizes/size" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.size')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--6***identi*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDate_typeSensitive">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/identis/identi" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.identi')" name="label" />
                                <xsl:with-param name="typeClassi" select="'jportal_class_00000010'" />
                                <xsl:with-param name="mode" select="'text'" />
                            </xsl:call-template>
                        </table>
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-divlines">
                            <xsl:choose>
                                <xsl:when
                                    test="(./metadata/identis/identi | ./metadata/sizes/size 
                                | ./metadata/dates/date[@inherited='0']/text()
                                | ./metadata/participants/participant | ./metadata/subtitles/subtitle)">
                                    <tr>
                                        <td colspan="2" id="detailed-innerdivlines">
                                            <br />
                                        </td>
                                    </tr>
                                </xsl:when>
                                <xsl:when test="string-length($mainTitle)>150)">
                                    <tr>
                                        <td colspan="2" id="detailed-innerdivlines">
                                            <br />
                                        </td>
                                    </tr>
                                </xsl:when>
                            </xsl:choose>
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
                        <!--7***keyword*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/keywords/keyword" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.keyword')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--8***abstract*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/abstracts/abstract" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.abstract')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--9***note*************************************-->
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
                        <!--10***type*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/types/type" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.type')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--11***rubric*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/rubrics/rubric" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.rubric')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--12***classipub*************************************-->
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
                        <!--13***classipub2*************************************-->
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
                        <!--14***classipub3*************************************-->
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
                        <!--15***classipub4*************************************-->
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
                        <!--16***ref*************************************-->
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
                        <xsl:if test="$CurrentUser!='gast'">
                            <!--*** Created ************************************* -->
                            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                                <xsl:call-template name="printMetaDates">
                                    <xsl:with-param select="'right'" name="textalign" />
                                    <xsl:with-param select="./service/servdates/servdate[@type='createdate']" name="nodes" />
                                    <xsl:with-param select="i18n:translate('editor.search.document.datecr')" name="label" />
                                </xsl:call-template>
                            </table>
                            <!--*** Last Modified ************************************* -->
                            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                                <xsl:call-template name="printMetaDates">
                                    <xsl:with-param select="'right'" name="textalign" />
                                    <xsl:with-param select="./service/servdates/servdate[@type='modifydate']" name="nodes" />
                                    <xsl:with-param select="i18n:translate('editor.search.document.datemod')" name="label" />
                                </xsl:call-template>
                            </table>
                            <!--*** MyCoRe-ID ************************************* -->
                            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                                <tr>
                                    <td id="detailed-labels" style="text-align:right;  padding-right: 5px;">
                                        <xsl:value-of select="i18n:translate('metaData.ID')" />
                                    </td>
                                    <td class="metavalue">
                                        <xsl:value-of select="./@ID" />
                                    </td>
                                </tr>
                            </table>
                        </xsl:if>

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
                        <!--      <xsl:call-template name="editobject_with_der">
                            <xsl:with-param select="$accessedit" name="accessedit"/>
                            <xsl:with-param select="./@ID" name="id"/>
                            </xsl:call-template>-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">

                            <xsl:call-template name="editobject_with_der">
                                <xsl:with-param select="$accessedit" name="accessedit" />
                                <xsl:with-param select="./@ID" name="id" />
                            </xsl:call-template>

                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>

    <!-- =================================================================================================================== -->
    <xsl:template name="Derobjects3">
        <xsl:param name="obj_host" />

        <xsl:param name="staticURL" />
        <xsl:param name="layout" />
        <xsl:param name="xmltempl" />
        <xsl:variable select="substring-before(substring-after(./@ID,'_'),'_')" name="type" />
        <xsl:variable name="suffix">
            <xsl:if test="string-length($layout)&gt;0">
                <xsl:value-of select="concat('&amp;layout=',$layout)" />
            </xsl:if>
        </xsl:variable>

        <xsl:if test="./structure/derobjects">
            <tr>
                <td style="vertical-align:top;" class="metaname">
                    <xsl:value-of select="i18n:translate('metaData.jparticle.[derivates]')" />
                </td>
                <td class="metavalue">
                    <xsl:if test="$objectHost != 'local'">
                        <a href="{$staticURL}">
                            <xsl:value-of select="i18n:translate('metaData.origserver')" />
                        </a>

                    </xsl:if>
                    <xsl:if test="$objectHost = 'local'">
                        <xsl:for-each select="./structure/derobjects/derobject">
                            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td valign="top" align="left">
                                        <div class="derivateBox">
                                            <xsl:variable select="@xlink:href" name="deriv" />
                                            <xsl:variable select="concat('mcrobject:',$deriv)" name="derivlink" />

                                            <xsl:variable select="document($derivlink)" name="derivate" />
                                            <xsl:apply-templates select="$derivate/mycorederivate/derivate/internals" />
                                            <xsl:apply-templates select="$derivate/mycorederivate/derivate/externals" />
                                        </div>
                                    </td>
                                    <xsl:if test="acl:checkPermission(./@ID,'writedb')">
                                        <td align="right" valign="top">
                                            <a
                                                href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;re_mcrid={../../../@ID}&amp;se_mcrid={@xlink:href}&amp;te_mcrid={@xlink:href}&amp;todo=saddfile{$suffix}{$xmltempl}">
                                                <img title="Datei hinzufügen" src="{$WebApplicationBaseURL}images/workflow_deradd.gif" />

                                            </a>
                                            <!--<a
                                                href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;re_mcrid={../../../@ID}&amp;se_mcrid={@xlink:href}&amp;te_mcrid={@xlink:href}&amp;todo=seditder{$suffix}{$xmltempl}">
                                                <img title="Derivat bearbeiten"
                                                src="{$WebApplicationBaseURL}images/workflow_deredit.gif"/>
                                                </a>-->
                                            <a
                                                href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;re_mcrid={../../../@ID}&amp;se_mcrid={@xlink:href}&amp;te_mcrid={@xlink:href}&amp;todo=sdelder{$suffix}{$xmltempl}">
                                                <img title="Derivat löschen" src="{$WebApplicationBaseURL}images/workflow_derdelete.gif" />
                                            </a>
                                        </td>
                                    </xsl:if>

                                </tr>
                            </table>
                        </xsl:for-each>
                    </xsl:if>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <!-- =============================================================================================== -->
</xsl:stylesheet>