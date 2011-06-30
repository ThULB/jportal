<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
    xmlns:mcr="http://www.mycore.org/" xmlns:xlink="http://www.w3.org/1999/xlink"
    exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
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
    <!--Template for result list hit: see results.xsl -->
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
                    <xsl:with-param name="knoten" select="xalan:nodeset($cXML)" />
                </xsl:call-template>
            </tr>
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

    <!-- ===================================================================================================== -->
    <!--Template for result list hit: see results.xsl -->
    <xsl:template match="/mycoreobject[contains(@ID,'_jpvolume_')]" mode="toc">
        <table cellspacing="0" cellpadding="0" id="leaf-all">
            <tr>
                <td id="leaf-front" colspan="1" rowspan="9">
                    <img src="{$WebApplicationBaseURL}images/band2.gif" />
                </td>
                <td id="leaf-linkarea2">
                    <xsl:variable name="name">
                        <xsl:value-of select="/mycoreobject/metadata/maintitles/maintitle/text()" />
                    </xsl:variable>

                    <xsl:variable name="date">
                        <xsl:choose>
                            <xsl:when
                                test="/mycoreobject/metadata/dates/date[@inherited='0'] and /mycoreobject/metadata/dates/date[@inherited='0'] != $name">
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

                    <xsl:variable name="children">
                        <xsl:choose>
                            <xsl:when test="/mycoreobject/structure/children">
                                <xsl:value-of select="'true'" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="'false'" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                  
                    <xsl:call-template name="objectLinking">
                        <xsl:with-param name="obj_id" select="/mycoreobject/@ID" />
                        <xsl:with-param name="obj_name" select="$shortlabel" />
                        <xsl:with-param name="hoverText" select="$name" />
                        <xsl:with-param name="requestParam" select="concat('XSL.view.objectmetadata.SESSION=', $children)" />
                    </xsl:call-template>

                    <!-- sub title, if exist -->
                    <xsl:if test="/mycoreobject/metadata/subtitles/subtitle/text()">
                        <span style="font-size:90%;">
                            <i>
                                <xsl:value-of select="/mycoreobject/metadata/subtitles/subtitle/text()" />
                            </i>
                        </span>
                    </xsl:if>
                    <!-- abstract, if exist -->
                    <xsl:if test="/mycoreobject/metadata/abstracts/abstract/text()">
                        <xsl:choose>
                            <xsl:when test="/mycoreobject/metadata/subtitles/subtitle">
                                <span style="font-size:90%;">
                                    <i>
                                        <xsl:value-of select="concat(i18n:translate('editormask.labels.abstract'),': ')" />
                                    </i>
                                    <xsl:value-of select="/mycoreobject/metadata/abstracts/abstract/text()" />
                                </span>
                            </xsl:when>
                            <xsl:otherwise>
                                <span style="font-size:90%;">
                                    <i>
                                        <xsl:value-of select="/mycoreobject/metadata/abstracts/abstract/text()" />
                                    </i>
                                </span>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </td>
            </tr>

            <!-- author - only for calendars -->
            <xsl:variable name="journalId" select="/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()" />
            <xsl:if test="$journalId">
                <xsl:variable name="journal" select="document(concat('mcrobject:',$journalId))/mycoreobject" />
                <xsl:variable name="journalType" select="$journal/metadata/contentClassis1/contentClassi1/@categid" />
                <xsl:if test="$journalType = 'calendar'">
                    <xsl:variable name="authorName">
                        <xsl:value-of select="$journal/metadata/participants/participant[@xlink:title='author']/@xlink:label" />
                    </xsl:variable>
                    <xsl:if test="$authorName">
                        <tr>
                            <td id="leaf-additional">
                                <xsl:call-template name="lineSpace" />
                                <i>
                                    <xsl:value-of select="concat(i18n:translate('editormask.labels.author'),': ')" />
                                </i>
                                <xsl:value-of select="$authorName" />
                                <xsl:call-template name="lineSpace" />
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:if>
            </xsl:if>

            <xsl:call-template name="getContentClassis">
                <xsl:with-param name="counter" select="1" />
            </xsl:call-template>

            <!-- note, if exist -->
            <xsl:if test="/mycoreobject/metadata/notes/note/text()">
                <tr>
                    <td id="leaf-additional">
                        <i>
                            <xsl:value-of select="concat(i18n:translate('editormask.labels.note'),': ')" />
                        </i>
                        <xsl:value-of select="/mycoreobject/metadata/notes/note/text()" />
                        <xsl:call-template name="lineSpace" />
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
    <xsl:template match="mcr:hit[contains(@id,'_jpvolume_')]" mode="toc">
        <xsl:param name="mcrobj" select="document(concat('mcrobject:',@id))" />
        <xsl:apply-templates select="$mcrobj" mode="toc" />
    </xsl:template>

    <!-- ================================================================================================================= -->
    <xsl:template name="getContentClassis">
        <xsl:param name="counter" />
        <!-- volContentClassis, if exist -->
        <xsl:if test="/mycoreobject/metadata/*/*[name()=concat('volContentClassi',$counter)]">
            <tr>
                <td id="leaf-additional">
                    <xsl:call-template name="lineSpace" />
                    <i>
                        <xsl:variable name="expression"
                            select="concat('jportal_getClassLabel:getFromJournal:hidden_classiVol',$counter,'/hidden_classiVol',$counter)" />
                        <xsl:value-of select="concat(document($expression)//label/text(),': ')" />
                    </i>
                    <xsl:call-template name="printClass">
                        <xsl:with-param name="nodes" select="/mycoreobject/metadata/*/*[name()=concat('volContentClassi',$counter)]" />
                        <xsl:with-param name="host" select="'local'" />
                        <xsl:with-param name="next" select="', '" />
                    </xsl:call-template>
                    <xsl:call-template name="lineSpace" />
                </td>
            </tr>
        </xsl:if>

        <xsl:if test="number($counter) &lt; 6">
            <xsl:call-template name="getContentClassis">
                <xsl:with-param name="counter" select="$counter + 1" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- ================================================================================================================= -->

    <!--Template for generated link names and result titles: see mycoreobject.xsl, results.xsl, MyCoReLayout.xsl -->
    <xsl:template priority="1" mode="resulttitle" match="/mycoreobject[contains(@ID,'_jpvolume_')]">
        <xsl:choose>
            <xsl:when test="./metadata/maintitles/maintitle">
                <xsl:call-template name="printI18N">
                    <xsl:with-param select="./metadata/maintitles/maintitle" name="nodes" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@label" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--Template for title in metadata view: see mycoreobject.xsl -->
    <xsl:template priority="1" mode="title" match="/mycoreobject[contains(@ID,'_jpvolume_')]">
        <xsl:choose>
            <!-- you could insert any title-like metadata here, e.g. replace "your-tags/here" by something of your metadata -->
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
    <!--Template for metadata view: see mycoreobject.xsl -->
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
                        <!--1***maintitle************************************* -->
                        <table cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/maintitles/maintitle[@inherited='0']" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.bibdescript')" name="label" />
                            </xsl:call-template>
                        </table>
                        <xsl:variable name="formalDescrNodeNames1">
                            <node name="subtitle" label="editormask.labels.bibdescript" />
                            <node name="participant" label="editormask.labels.participants_label" />
                        </xsl:variable>

                        <xsl:call-template name="printMetaNodes">
                            <xsl:with-param name="metaData" select="./metadata" />
                            <xsl:with-param name="nodeNames" select="xalan:nodeset($formalDescrNodeNames1)" />
                        </xsl:call-template>

                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDate_typeSensitive">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/people/person" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.people_label')" name="label" />
                                <xsl:with-param name="typeClassi" select="'jportal_class_00000007'" />
                                <xsl:with-param name="mode" select="'text'" />
                            </xsl:call-template>
                        </table>
                        <!--4***date************************************* -->
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
                        <!--4***tradition************************************* -->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDate_typeSensitive">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/traditions/tradition" name="nodes" />
                                <xsl:with-param name="typeClassi" select="'jportal_class_00000080'" />
                                <xsl:with-param name="mode" select="'text'" />
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
                        <!--5*** collation note ************************************* -->
                        <table cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/collationNotes/collationNote" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.labels.collation_note')" name="label" />
                            </xsl:call-template>
                        </table>

                        <!-- Content Description ################################################### -->
                        <xsl:variable name="contentDescrNodeNames">
                            <node name="note" label="editormask.labels.note" />
                            <node name="person" label="editormask.labels.person_label" />
                            <node name="publicationNote" label="editormask.labels.publicationNote" />
                            <node name="footNote" label="editormask.labels.footNote" />
                            <node name="normedPubLocation" label="editormask.labels.normedPubLocation" />
                            <node name="bibEvidence" label="editormask.labels.bibEvidence" />
                            <node name="indexField" label="editormask.labels.indexField" />
                            <node name="abstract" label="editormask.labels.abstract" />
                        </xsl:variable>

                        <xsl:variable name="contentDescr">
                            <xsl:call-template name="printVolContentClassis">
                                <xsl:with-param name="nodes" select="./metadata/*/*[contains(name(),'volContentClassi')]" />
                                <xsl:with-param name="counter" select="1" />
                            </xsl:call-template>

                            <xsl:call-template name="printMetaNodes">
                                <xsl:with-param name="metaData" select="./metadata" />
                                <xsl:with-param name="nodeNames" select="xalan:nodeset($contentDescrNodeNames)" />
                            </xsl:call-template>
                        </xsl:variable>

                        <xsl:if test="count(xalan:nodeset($contentDescr)/*) > 0">
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
                            <xsl:copy-of select="$contentDescr" />
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
                            <xsl:call-template name="jp_editobject_with_der">
                                <xsl:with-param select="$accessedit" name="accessedit" />
                                <xsl:with-param select="./@ID" name="id" />
                            </xsl:call-template>
                            <xsl:if test="acl:checkPermission(./@ID,'create_jpvolume')">
                                <xsl:call-template name="addChild2">
                                    <xsl:with-param name="id" select="./@ID" />
                                    <xsl:with-param name="types" select="'jpvolume'" />
                                </xsl:call-template>
                            </xsl:if>
                            <xsl:if
                                test="acl:checkPermission(./@ID,'create_jparticle')">
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

    <xsl:template name="printVolContentClassis">
        <xsl:param name="nodes" />
        <xsl:param name="counter" />

        <xsl:variable name="volContentClassiNodes" select="$nodes[name() = concat('volContentClassi',$counter)]" />

        <xsl:if test="count($volContentClassiNodes) > 0">
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                <xsl:variable name="label_classiVol">
                    <xsl:value-of
                        select="document(concat('jportal_getClassLabel:getFromJournal:hidden_classiVol',$counter,'/hidden_classiVol',$counter))//label/text()" />
                </xsl:variable>
                <xsl:call-template name="printMetaDates">
                    <xsl:with-param select="'right'" name="textalign" />
                    <xsl:with-param select="$volContentClassiNodes" name="nodes" />
                    <xsl:with-param select="$label_classiVol" name="label" />
                </xsl:call-template>
            </table>
        </xsl:if>

        <xsl:if test="number($counter) &lt; 6">
            <xsl:call-template name="printVolContentClassis">
                <xsl:with-param name="nodes" select="$nodes" />
                <xsl:with-param name="counter" select="$counter + 1" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- =================================================================================================================================== -->
    <xsl:template name="addChild2">
        <xsl:param name="id" />
        <xsl:param name="layout" />
        <xsl:param name="types" />
        <xsl:param select="concat('&amp;parentID=',$id)" name="xmltempl" />
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

        <!-- <xsl:if test="acl:checkPermission($id,'writedb')"> -->
        <tr>
            <td id="detailed-labels">
                <xsl:value-of select="concat(i18n:translate('metaData.addChildObject'),':')" />
            </td>
            <td class="metavalue">
                <ul>
                    <xsl:for-each select="xalan:nodeset($typeToken)/token">
                        <xsl:variable select="." name="type" />
                        <li>
                            <a
                                href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;step=author&amp;todo=wnewobj{$suffix}{$xmltempl}">
                                <xsl:value-of select="i18n:translate(concat('metaData.',$type,'.[singular]'))" />
                            </a>
                        </li>
                    </xsl:for-each>
                </ul>
            </td>
        </tr>

        <!-- </xsl:if> -->
    </xsl:template>

    <!-- ===================================================================================================== -->
</xsl:stylesheet>