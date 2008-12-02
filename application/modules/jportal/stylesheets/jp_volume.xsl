<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:aclObjID="xalan://org.mycore.access.strategies.MCRObjectIDStrategy" xmlns:aclObjType="xalan://org.mycore.access.strategies.MCRJPortalStrategy"
    xmlns:mcr="http://www.mycore.org/" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
    <xsl:param select="'local'" name="objectHost" />
    <!-- ===================================================================================================== -->
    <xsl:template name="dateConvert">
        <xsl:param name="dateUnconverted" />
        <xsl:variable name="format">
            <xsl:choose>
                <xsl:when test="string-length(normalize-space(.))=4">
                    <xsl:value-of select="i18n:translate('metaData.dateYear')" />
                </xsl:when>
                <xsl:when test="string-length(normalize-space(.))=7">
                    <xsl:value-of select="i18n:translate('metaData.dateYearMonth')" />
                </xsl:when>
                <xsl:when test="string-length(normalize-space(.))=10">
                    <xsl:value-of select="i18n:translate('metaData.dateYearMonthDay')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="i18n:translate('metaData.dateTime')" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:call-template name="formatISODate">
            <xsl:with-param name="date" select="." />
            <xsl:with-param name="format" select="$format" />
        </xsl:call-template>
    </xsl:template>
    <!-- ===================================================================================================== -->
    <!--Template for result list hit: see results.xsl-->
    <xsl:template match="mcr:hit[contains(@id,'_jpvolume_')]">
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
        <table cellspacing="0" cellpadding="0" id="leaf-all">
            <tr>
                <td id="leaf-front" colspan="1" rowspan="9">
                    <img src="{$WebApplicationBaseURL}images/band2.gif" />
                </td>
                <td id="leaf-linkarea2">
                    <xsl:variable name="name">
                        <xsl:call-template name="ShortenText">
                            <xsl:with-param name="text" select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
                            <xsl:with-param name="length" select="75" />
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
                    </xsl:call-template>
                </td>
            </tr>
            <tr>
                 <!-- authors -->
                <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/participants/participant">
                    <xsl:call-template name="printMetaDate_typeSensitive">
                        <xsl:with-param select="'right'" name="textalign" />
                        <xsl:with-param select="xalan:nodeset($cXML)/mycoreobject/metadata/participants/participant" name="nodes" />
                        <xsl:with-param select="i18n:translate('editormask.labels.participants_label')" name="label" />
                        <xsl:with-param name="typeClassi" select="'jportal_class_00000007'" />
                        <xsl:with-param name="mode" select="'xlink'" />
                        <xsl:with-param name="layout" select="'flat'" />
                    </xsl:call-template>
                </xsl:if>
                <xsl:call-template name="printDerivates">
                    <xsl:with-param name="obj_id" select="@id" />
                    <xsl:with-param name="knoten" select="$cXML" />
                </xsl:call-template>
            </tr>
        </table>
        <table cellspacing="0" cellpadding="0">
            <tr id="leaf-whitespaces">
                <td></td>
            </tr>
        </table>
    </xsl:template>
    <!-- =============================================================================================== -->
    <xsl:template match="mcr:hit[contains(@id,'_jpvolume_')]" mode="toc">
        <xsl:param name="mcrobj" />
        <xsl:param name="mcrobjlink" />
        <xsl:variable name="cXML">
            <xsl:copy-of select="document(concat('mcrobject:',@id))" />
        </xsl:variable>
        <table cellspacing="0" cellpadding="0" id="leaf-all">
            <tr>
                <td id="leaf-front" colspan="1" rowspan="9">
                    <img src="{$WebApplicationBaseURL}images/band2.gif" />
                </td>
                <td id="leaf-linkarea2">
                    <xsl:variable name="name">
                        <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
                    </xsl:variable>
                    <xsl:variable name="date">
                        <xsl:choose>
                            <xsl:when
                                test="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0'] and xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0'] != $name">
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
              or ($children='false' and $CurrentUser!='gast') ">
                            <xsl:call-template name="objectLinking">
                                <xsl:with-param name="obj_id" select="@id" />
                                <xsl:with-param name="obj_name" select="$shortlabel" />
                                <xsl:with-param name="hoverText" select="$name" />
                                <xsl:with-param name="requestParam" select="'XSL.view.objectmetadata.SESSION=false'" />
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:when test="($children='false' and $CurrentUser='gast')">
                            <xsl:value-of select="$shortlabel" />
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
                    <!-- sub title, if exist  -->
                    <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/subtitles/subtitle/text()">
                        <span style="font-size:90%;">
                            <i>
                                <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/subtitles/subtitle/text()" />
                            </i>
                        </span>
                    </xsl:if>
                    <!-- abstract, if exist  -->
                    <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/abstracts/abstract/text()">
                        <xsl:choose>
                            <xsl:when test="xalan:nodeset($cXML)/mycoreobject/metadata/subtitles/subtitle">
                                <span style="font-size:90%;">
                                    <i>
                                        <xsl:value-of select="concat(i18n:translate('editormask.labels.abstract'),': ')" />
                                    </i>
                                    <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/abstracts/abstract/text()" />
                                </span>
                            </xsl:when>
                            <xsl:otherwise>
                                <span style="font-size:90%;">
                                    <i>
                                        <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/abstracts/abstract/text()" />
                                    </i>
                                </span>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </td>
            </tr>
            <xsl:call-template name="getContentClassis">
                <xsl:with-param name="cXML" select="$cXML" />
            </xsl:call-template>

            <!-- note, if exist  -->
            <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/notes/note/text()">
                <tr>
                    <td id="leaf-additional">
                        <i>
                            <xsl:value-of select="concat(i18n:translate('editormask.labels.note'),': ')" />
                        </i>
                        <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/notes/note/text()" />
                        <xsl:call-template name="lineSpace" />
                    </td>
                </tr>
            </xsl:if>
            <xsl:call-template name="printDerivates">
                <xsl:with-param name="obj_id" select="@id" />
                <xsl:with-param name="knoten" select="$cXML" />
            </xsl:call-template>
        </table>
        <br />
    </xsl:template>

    <!-- ================================================================================================================= -->
    <xsl:template name="getContentClassis">
        <xsl:param name="cXML" />
        <!-- volContentClassis, if exist  -->
        <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis1/volContentClassi1">
            <tr>
                <td id="leaf-additional">
                    <xsl:call-template name="lineSpace" />
                    <i>
                        <xsl:value-of select="concat(document('jportal_getClassLabel:getFromJournal:hidden_classiVol1/hidden_classiVol1')//label/text(),': ')" />
                    </i>
                    <xsl:call-template name="printClass">
                        <xsl:with-param name="nodes" select="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis1/volContentClassi1" />
                        <xsl:with-param name="host" select="'local'" />
                        <xsl:with-param name="next" select="', '" />
                    </xsl:call-template>
                    <xsl:call-template name="lineSpace" />
                </td>
            </tr>
        </xsl:if>
            <!-- volContentClassis, if exist  -->
        <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis2/volContentClassi2">
            <tr>
                <td id="leaf-additional">
                    <xsl:call-template name="lineSpace" />
                    <i>
                        <xsl:value-of select="concat(document('jportal_getClassLabel:getFromJournal:hidden_classiVol2/hidden_classiVol2')//label/text(),': ')" />
                    </i>
                    <xsl:call-template name="printClass">
                        <xsl:with-param name="nodes" select="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis2/volContentClassi2" />
                        <xsl:with-param name="host" select="'local'" />
                        <xsl:with-param name="next" select="', '" />
                    </xsl:call-template>
                    <xsl:call-template name="lineSpace" />
                </td>
            </tr>
        </xsl:if>
            <!-- volContentClassis, if exist  -->
        <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis3/volContentClassi3">
            <tr>
                <td id="leaf-additional">
                    <xsl:call-template name="lineSpace" />
                    <i>
                        <xsl:value-of select="concat(document('jportal_getClassLabel:getFromJournal:hidden_classiVol3/hidden_classiVol3')//label/text(),': ')" />
                    </i>
                    <xsl:call-template name="printClass">
                        <xsl:with-param name="nodes" select="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis3/volContentClassi3" />
                        <xsl:with-param name="host" select="'local'" />
                        <xsl:with-param name="next" select="', '" />
                    </xsl:call-template>
                    <xsl:call-template name="lineSpace" />
                </td>
            </tr>
        </xsl:if>
            <!-- volContentClassis, if exist  -->
        <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis4/volContentClassi4">
            <tr>
                <td id="leaf-additional">
                    <xsl:call-template name="lineSpace" />
                    <i>
                        <xsl:value-of select="concat(document('jportal_getClassLabel:getFromJournal:hidden_classiVol4/hidden_classiVol4')//label/text(),': ')" />
                    </i>
                    <xsl:call-template name="printClass">
                        <xsl:with-param name="nodes" select="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis4/volContentClassi4" />
                        <xsl:with-param name="host" select="'local'" />
                        <xsl:with-param name="next" select="', '" />
                    </xsl:call-template>
                    <xsl:call-template name="lineSpace" />
                </td>
            </tr>
        </xsl:if>
            <!-- volContentClassis, if exist  -->
        <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis5/volContentClassi5">
            <tr>
                <td id="leaf-additional">
                    <xsl:call-template name="lineSpace" />
                    <i>
                        <xsl:value-of select="concat(document('jportal_getClassLabel:getFromJournal:hidden_classiVol5/hidden_classiVol5')//label/text(),': ')" />
                    </i>
                    <xsl:call-template name="printClass">
                        <xsl:with-param name="nodes" select="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis5/volContentClassi5" />
                        <xsl:with-param name="host" select="'local'" />
                        <xsl:with-param name="next" select="', '" />
                    </xsl:call-template>
                    <xsl:call-template name="lineSpace" />
                </td>
            </tr>
        </xsl:if>
            <!-- volContentClassis, if exist  -->
        <xsl:if test="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis6/volContentClassi6">
            <tr>
                <td id="leaf-additional">
                    <xsl:call-template name="lineSpace" />
                    <i>
                        <xsl:value-of select="concat(document('jportal_getClassLabel:getFromJournal:hidden_classiVol6/hidden_classiVol6')//label/text(),': ')" />
                    </i>
                    <xsl:call-template name="printClass">
                        <xsl:with-param name="nodes" select="xalan:nodeset($cXML)/mycoreobject/metadata/volContentClassis6/volContentClassi6" />
                        <xsl:with-param name="host" select="'local'" />
                        <xsl:with-param name="next" select="', '" />
                    </xsl:call-template>
                    <xsl:call-template name="lineSpace" />
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <!-- ================================================================================================================= -->

    <!--Template for generated link names and result titles: see mycoreobject.xsl, results.xsl, MyCoReLayout.xsl-->
    <xsl:template priority="1" mode="resulttitle" match="/mycoreobject[contains(@ID,'_jpvolume_')]">
        <xsl:choose>
            <!--
                you could insert any title-like metadata here, e.g.
                replace "your-tags/here" by something of your metadata
            -->
            <xsl:when test="./metadata/your-tags">
                <xsl:call-template name="printI18N">
                    <xsl:with-param select="./metadata/your-tags/here" name="nodes" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@label" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--Template for title in metadata view: see mycoreobject.xsl-->
    <xsl:template priority="1" mode="title" match="/mycoreobject[contains(@ID,'_jpvolume_')]">
        <xsl:choose>
            <!--
                you could insert any title-like metadata here, e.g.
                replace "your-tags/here" by something of your metadata
            -->
            <xsl:when test="./metadata/your-tags">
                <xsl:call-template name="printI18N">
                    <xsl:with-param select="./metadata/your-tags/here" name="nodes" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@ID" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- =================================================================================================================================== -->
    <!--Template for metadata view: see mycoreobject.xsl-->
    <xsl:template priority="1" mode="present" match="/mycoreobject[contains(@ID,'_jpvolume_')]">
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
            <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td id="detailed-cube">
                        <img src="{$WebApplicationBaseURL}images/band.gif" />
                    </td>
                    <td id="detailed-mainheadline">
                        <xsl:variable name="maintitle_shorted">
                            <xsl:call-template name="ShortenText">
                                <xsl:with-param name="text" select="./metadata/maintitles/maintitle[@inherited='0']/text()" />
                                <xsl:with-param name="length" select="75" />
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:value-of select="$maintitle_shorted" />
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
                        <table cellspacing="0" cellpadding="0" id="detailed-view">
                            <tr>
                                <td id="detailed-headlines">
                                    <xsl:value-of select="i18n:translate('metaData.headlines.formaldiscr')" />
                                </td>
                                <td>
                                    <br />
                                </td>
                            </tr>
                        </table>
                        <!--1***maintitle*************************************-->
                        <table cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/maintitles/maintitle[@inherited='0']" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.bibdescript')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--2***subtitle*************************************-->
                        <table cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/subtitles/subtitle" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.subtitle')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--3***participant*************************************-->
                        <table cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/participants/participant" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.participants_label')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--4***date*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:variable name="nodes" select="./metadata/dates/date[@inherited='0']" />
                            <xsl:if test="$nodes">
                                <tr>
                                    <td valign="top" id="detailed-labels">
                                        <xsl:value-of select="i18n:translate('editormask.labels.date_label')" />
                                    </td>
                                    <td>
                                        <xsl:attribute name="class">
                                            <xsl:value-of select="'metavalue'" />
                                        </xsl:attribute>
                                        <xsl:choose>
                                            <xsl:when test="$nodes[@type='published_from']">
                                                <xsl:value-of select="concat($nodes[@type='published_from'],' - ',$nodes[@type='published_until'])" />
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$nodes[@type='published']" />
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </xsl:if>
                        </table>
                        <!--4***tradition*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDate_typeSensitive">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/traditions/tradition" name="nodes" />
                                <xsl:with-param name="typeClassi" select="'jportal_class_00000080'" />
                                <xsl:with-param name="mode" select="'text'" />
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
                        <!--5*** collation note *************************************-->
                        <table cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/collationNotes/collationNote" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.collation_note')" name="label" />
                            </xsl:call-template>
                        </table>
                        
                        <!-- Content Description ################################################### -->
                        <xsl:if
                            test="./metadata/volContentClassis1/volContentClassi1 | 
                            ./metadata/volContentClassis2/volContentClassi2 | 
                            ./metadata/volContentClassis3/volContentClassi3 | 
                            ./metadata/volContentClassis4/volContentClassi4 | 
                            ./metadata/volContentClassis5/volContentClassi5 | 
                            ./metadata/volContentClassis6/volContentClassi6 | 
                            ./metadata/notes/note | ./metadata/abstracts/abstract">
                            <table border="0" cellspacing="0" cellpadding="0" id="detailed-divlines">
                                <tr>
                                    <td colspan="2" id="detailed-innerdivlines">
                                        <br />
                                    </td>
                                </tr>
                            </table>
                            <table cellspacing="0" cellpadding="0" id="detailed-view">
                                <tr>
                                    <td id="detailed-headlines">
                                        <xsl:value-of select="i18n:translate('metaData.headlines.contantdiscr')" />
                                    </td>
                                    <td>
                                        <br />
                                    </td>
                                </tr>
                            </table>
                            <!--12***classiVol*************************************-->
                            <xsl:if test="./metadata/volContentClassis1/volContentClassi1">
                                <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                                    <xsl:variable name="label_classiVol">
                                        <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_classiVol1/hidden_classiVol1')//label/text()" />
                                    </xsl:variable>
                                    <xsl:call-template name="printMetaDates">
                                        <xsl:with-param select="'right'" name="textalign" />
                                        <xsl:with-param select="./metadata/volContentClassis1/volContentClassi1" name="nodes" />
                                        <xsl:with-param select="$label_classiVol" name="label" />
                                    </xsl:call-template>
                                </table>
                            </xsl:if>
                            <!--12***classiVol*************************************-->
                            <xsl:if test="./metadata/volContentClassis2/volContentClassi2">
                                <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                                    <xsl:variable name="label_classiVol">
                                        <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_classiVol2/hidden_classiVol2')//label/text()" />
                                    </xsl:variable>
                                    <xsl:call-template name="printMetaDates">
                                        <xsl:with-param select="'right'" name="textalign" />
                                        <xsl:with-param select="./metadata/volContentClassis2/volContentClassi2" name="nodes" />
                                        <xsl:with-param select="$label_classiVol" name="label" />
                                    </xsl:call-template>
                                </table>
                            </xsl:if>
                            <!--12***classiVol*************************************-->
                            <xsl:if test="./metadata/volContentClassis3/volContentClassi3">
                                <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                                    <xsl:variable name="label_classiVol">
                                        <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_classiVol3/hidden_classiVol3')//label/text()" />
                                    </xsl:variable>
                                    <xsl:call-template name="printMetaDates">
                                        <xsl:with-param select="'right'" name="textalign" />
                                        <xsl:with-param select="./metadata/volContentClassis3/volContentClassi3" name="nodes" />
                                        <xsl:with-param select="$label_classiVol" name="label" />
                                    </xsl:call-template>
                                </table>
                            </xsl:if>
                            <!--12***classiVol*************************************-->
                            <xsl:if test="./metadata/volContentClassis4/volContentClassi4">
                                <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                                    <xsl:variable name="label_classiVol">
                                        <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_classiVol4/hidden_classiVol4')//label/text()" />
                                    </xsl:variable>
                                    <xsl:call-template name="printMetaDates">
                                        <xsl:with-param select="'right'" name="textalign" />
                                        <xsl:with-param select="./metadata/volContentClassis4/volContentClassi4" name="nodes" />
                                        <xsl:with-param select="$label_classiVol" name="label" />
                                    </xsl:call-template>
                                </table>
                            </xsl:if>
                            <!--15***classiVol*************************************-->
                            <xsl:if test="./metadata/volContentClassis5/volContentClassi5">
                                <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                                    <xsl:variable name="label_classiVol">
                                        <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_classiVol5/hidden_classiVol5')//label/text()" />
                                    </xsl:variable>
                                    <xsl:call-template name="printMetaDates">
                                        <xsl:with-param select="'right'" name="textalign" />
                                        <xsl:with-param select="./metadata/volContentClassis5/volContentClassi5" name="nodes" />
                                        <xsl:with-param select="$label_classiVol" name="label" />
                                    </xsl:call-template>
                                </table>
                            </xsl:if>
                            <!--16***classiVol*************************************-->
                            <xsl:if test="./metadata/volContentClassis6/volContentClassi6">
                                <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                                    <xsl:variable name="label_classiVol">
                                        <xsl:value-of select="document('jportal_getClassLabel:getFromJournal:hidden_classiVol6/hidden_classiVol6')//label/text()" />
                                    </xsl:variable>
                                    <xsl:call-template name="printMetaDates">
                                        <xsl:with-param select="'right'" name="textalign" />
                                        <xsl:with-param select="./metadata/volContentClassis6/volContentClassi6" name="nodes" />
                                        <xsl:with-param select="$label_classiVol" name="label" />
                                    </xsl:call-template>
                                </table>
                            </xsl:if>
                            
                            <!--5*** note *************************************-->
                            <xsl:if test="./metadata/notes/note">
                                <table cellspacing="0" cellpadding="0" id="detailed-view">
                                    <xsl:call-template name="printMetaDates">
                                        <xsl:with-param select="./metadata/notes/note" name="nodes" />
                                        <xsl:with-param select="i18n:translate('editormask.labels.note')" name="label" />
                                    </xsl:call-template>
                                </table>
                            </xsl:if>
                            <!--6*** abstract *************************************-->
                            <xsl:if test="./metadata/abstracts/abstract">
                                <table cellspacing="0" cellpadding="0" id="detailed-view">
                                    <xsl:call-template name="printMetaDates">
                                        <xsl:with-param select="./metadata/abstracts/abstract" name="nodes" />
                                        <xsl:with-param select="i18n:translate('editormask.labels.abstract')" name="label" />
                                    </xsl:call-template>
                                </table>
                            </xsl:if>
                        </xsl:if>
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
                        <!-- System data ##################################### -->
                        <xsl:call-template name="get.systemData" />

                        <!-- Static URL ************************************************** -->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="get.staticURL">
                                <xsl:with-param name="stURL" select="$staticURL" />
                            </xsl:call-template>
                            <xsl:call-template name="emptyRow" />
                        </table>

                        <!-- Administration ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
                        <table cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="showAdminHead" />
                        </table>
                        <!--*** Editor Buttons ************************************* -->
                        <table cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="editobject_with_der">
                                <xsl:with-param select="$accessedit" name="accessedit" />
                                <xsl:with-param select="./@ID" name="id" />
                            </xsl:call-template>
                            <xsl:if test="acl:checkPermission(./@ID,'writedb')">
                                <xsl:call-template name="addChild2">
                                    <xsl:with-param name="id" select="./@ID" />
                                    <xsl:with-param name="types" select="'jpvolume'" />
                                </xsl:call-template>
                            </xsl:if>
                            <xsl:variable name="journalID">
                                <xsl:value-of select="./metadata/hidden_jpjournalsID/hidden_jpjournalID/text()" />
                            </xsl:variable>
                            <xsl:if
                                test="aclObjType:checkPermissionOfType('jportal_jparticle_xxxxxxxx','writedb') and aclObjID:checkPermission($journalID,'writedb')">
                                <xsl:call-template name="addChild2">
                                    <xsl:with-param name="id" select="./@ID" />
                                    <xsl:with-param name="types" select="'jparticle'" />
                                </xsl:call-template>
                            </xsl:if>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>

    <!-- =================================================================================================================================== -->
    <xsl:template name="addChild2">
        <xsl:param name="id" />
        <xsl:param name="layout" />
        <xsl:param name="types" />
        <xsl:param select="concat('&amp;_xml_structure%2Fparents%2Fparent%2F%40href=',$id)" name="xmltempl" />
        <xsl:variable name="suffix">
            <xsl:if test="string-length($layout)&gt;0">
                <xsl:value-of select="concat('&amp;layout=',$layout)" />
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="typeToken">
            <xsl:call-template name="Tokenizer">
                <xsl:with-param select="$types" name="string" />
            </xsl:call-template>
        </xsl:variable>

        <!--        <xsl:if test="acl:checkPermission($id,'writedb')">-->
        <tr>
            <td id="detailed-labels">
                <xsl:value-of select="concat(i18n:translate('metaData.addChildObject'),':')" />
            </td>
            <td class="metavalue">
                <ul>
                    <xsl:for-each select="xalan:nodeset($typeToken)/token">
                        <xsl:variable select="." name="type" />
                        <li>
                            <a href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;step=author&amp;todo=wnewobj{$suffix}{$xmltempl}">
                                <xsl:value-of select="i18n:translate(concat('metaData.',$type,'.[singular]'))" />
                            </a>
                        </li>
                    </xsl:for-each>
                </ul>
            </td>
        </tr>

        <!--        </xsl:if>-->
    </xsl:template>

    <!-- =================================================================================================================================== -->
    <xsl:template name="Derobjects2">
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
                    <xsl:value-of select="i18n:translate('metaData.jpvolume.[derivates]')" />
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

    <!-- ===================================================================================================== -->
</xsl:stylesheet>