<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mcr="http://www.mycore.org/"
	xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xlink mcr i18n acl xalan" xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities">
	<xsl:include href="jp_layout-commons.xsl" />
	<xsl:param name="view.objectmetadata" select="'false'" />

	<xsl:param name="toc.pageSize" select="100" />
	<xsl:param name="toc.pos" />

	<xsl:param name="toc.sortBy.jpvolume" select="'position'" />
	<xsl:param name="toc.sortBy.jparticle" select="'size'" />
	<xsl:param select="5" name="maxLinkedArts" />
    <xsl:param select="5" name="maxLinkedCals" />

	<xsl:param name="MCR.Module-iview.SupportedContentTypes" />

	<xsl:param name="resultListEditorID" />
	<xsl:param name="numPerPage" />
	<xsl:param name="page" />
	<xsl:param name="previousObject" />
	<xsl:param name="previousObjectHost" />
	<xsl:param name="nextObject" />
	<xsl:param name="nextObjectHost" />

	<xsl:variable name="thumbnail">
		<xsl:call-template name="get.thumbnailSupport" />
	</xsl:variable>
	<xsl:variable name="JPID_zfbbHack">
		<xsl:call-template name="get.zfbbSupport" />
	</xsl:variable>
	<xsl:variable name="journalID">
		<xsl:call-template name="get.journalID" />
	</xsl:variable>
	<xsl:variable name="journalXML">
		<xsl:call-template name="get.journalXML" />
	</xsl:variable>
	<xsl:variable name="allowHTMLInArticles">
		<xsl:call-template name="get.allowHTMLInArticles" />
	</xsl:variable>
	<xsl:variable name="readAccessForDerivates">
		<xsl:call-template name="get.readAccessForDerivates">
			<xsl:with-param name="jID" select="$journalID" />
		</xsl:call-template>
	</xsl:variable>
	<!-- ===================================================================================================== -->
	<xsl:template
		match="/mycoreobject[contains(@ID,'_jpjournal_')] 
        | /mycoreobject[contains(@ID,'_jpvolume_')] 
        | /mycoreobject[contains(@ID,'_jparticle_')]"
		priority="2">

		<xsl:call-template name="printSwitchViewBar" />

		<xsl:choose>
			<!-- metadaten -->
			<xsl:when test="$view.objectmetadata = 'false'">
				<xsl:choose>
					<xsl:when test="($objectHost != 'local') or acl:checkPermission(/mycoreobject/@ID,'read')">
						<xsl:apply-templates select="." mode="present">
							<xsl:with-param name="obj_host" select="$objectHost" />
						</xsl:apply-templates>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="i18n:translate('metaData.accessDenied')" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- inhaltsverzeichnis -->
			<xsl:otherwise>
				<xsl:call-template name="printChildren" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ===================================================================================================== -->
	<xsl:template match="/mycoreobject[contains(@ID,'_person_')] 
        | /mycoreobject[contains(@ID,'_jpinst_')]" priority="2">

		<xsl:choose>
			<xsl:when test="($objectHost != 'local') or acl:checkPermission(/mycoreobject/@ID,'read')">

				<table>
					<tr>
						<xsl:call-template name="browseCtrlJP" />
						<td>&#160;&#160;&#160;&#160;&#160;&#160;</td>
                        <xsl:call-template name="SwitchToXMLview" />
                    </tr>
                </table>

                <xsl:apply-templates select="." mode="present">
                    <xsl:with-param name="obj_host" select="$objectHost" />
                </xsl:apply-templates>
                <hr />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="i18n:translate('metaData.accessDenied')" />
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="appendChild">
        <xsl:param name="accessedit" />
        <xsl:param name="id" />
        <xsl:param name="layout" />
        <xsl:param name="label" />
        <xsl:param name="imagePath" />
        <xsl:param name="type" select="'dokument'" />
        <xsl:param name="xmltempl" select="concat('&amp;_xml_structure%2Fparents%2Fparent%2F%40href=',$id)" />
        <xsl:param name="label" />

        <xsl:variable name="suffix">
            <xsl:if test="string-length($layout)>0">

                <xsl:value-of select="concat('&amp;layout=',$layout)" />

            </xsl:if>

        </xsl:variable>
        <xsl:if test="acl:checkPermission($id,'writedb')">
            <tr>
                <td class="metaname">

                    <xsl:value-of select="$label" />
                    :
                </td>
                <td class="metavalue">

                    <a href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;step=author&amp;todo=wnewobj{$suffix}{$xmltempl}">

                        <img src="{concat($WebApplicationBaseURL,$imagePath)}" title="{$label}" />

                    </a>

                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <!-- ===================================================================================================== -->
    <xsl:template name="getJournalXML">
        <xsl:param name="id" />
        <xsl:copy-of select="document(concat('mcrobject:',$id))" />
    </xsl:template>

    <!-- ============================================================================================================================ -->
    <!-- 
        <xsl:template name="get.params_dynamicClassis">
        
        <xsl:variable name="journalXML">
        <xsl:call-template name="getJournalXML">
        <xsl:with-param name="id" select="./metadata/hidden_jpjournalsID/hidden_jpjournalID/text()" />
        </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="IDTypes">
        <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_pubTypesID/hidden_pubTypeID/text()" />
        </xsl:variable>
        <xsl:variable name="param_types">
        <xsl:value-of select="concat('XSL.jportalClassification.types.SESSION=',$IDTypes)" />
        </xsl:variable>
        <xsl:variable name="param_types_editor">
        <xsl:value-of select="concat('_xml_metadata/types/type/@classid=',$IDTypes)" />
        </xsl:variable>
        <xsl:variable name="IDRubrics">
        <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_rubricsID/hidden_rubricID/text()" />
        </xsl:variable>
        <xsl:variable name="param_rubrics">
        <xsl:value-of select="concat('XSL.jportalClassification.rubrics.SESSION=',$IDRubrics)" />
        </xsl:variable>
        <xsl:variable name="param_rubrics_editor">
        <xsl:value-of select="concat('_xml_metadata/rubrics/rubric/@classid=',$IDRubrics)" />
        </xsl:variable>
        <xsl:variable name="IDclassipub">
        <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_classispub/hidden_classipub/text()" />
        </xsl:variable>
        <xsl:variable name="param_classipub">
        <xsl:value-of select="concat('XSL.jportalClassification.classipub.SESSION=',$IDclassipub)" />
        </xsl:variable>
        <xsl:variable name="param_classipub_editor">
        <xsl:value-of select="concat('_xml_metadata/classispub/classipub/@classid=',$IDclassipub)" />
        </xsl:variable>
        
        <xsl:variable name="IDclassipub2">
        <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_classispub2/hidden_classipub2/text()" />
        </xsl:variable>
        <xsl:variable name="param_classipub2">
        <xsl:value-of select="concat('XSL.jportalClassification.classipub2.SESSION=',$IDclassipub2)" />
        </xsl:variable>
        <xsl:variable name="param_classipub2_editor">
        <xsl:value-of select="concat('_xml_metadata/classispub2/classipub2/@classid=',$IDclassipub2)" />
        </xsl:variable>
        
        <xsl:variable name="IDclassipub3">
        <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_classispub3/hidden_classipub3/text()" />
        </xsl:variable>
        <xsl:variable name="param_classipub3">
        <xsl:value-of select="concat('XSL.jportalClassification.classipub3.SESSION=',$IDclassipub3)" />
        </xsl:variable>
        <xsl:variable name="param_classipub3_editor">
        <xsl:value-of select="concat('_xml_metadata/classispub3/classipub3/@classid=',$IDclassipub3)" />
        </xsl:variable>
        
        <xsl:value-of
        select="concat('&amp;',$param_types,'&amp;',$param_types_editor,'&amp;',$param_rubrics,'&amp;',$param_rubrics_editor
        ,'&amp;',$param_classipub,'&amp;',$param_classipub_editor
        ,'&amp;',$param_classipub2,'&amp;',$param_classipub2_editor
        ,'&amp;',$param_classipub3,'&amp;',$param_classipub3_editor)" />
        
        </xsl:template>
    -->
    <!-- ============================================================================================================================ -->

    <xsl:template name="get.staticURL">
        <xsl:param name="stURL" />
        <tr>
            <td id="detailed-staticurl1">

                <xsl:value-of select="i18n:translate('metaData.staticURL')" />
            </td>
            <td colspan="2" id="detailed-staticurl2">
                <a>
                    <xsl:attribute name="href">
                        
                        <xsl:copy-of select="$stURL" />
                        
                    </xsl:attribute>

                    <xsl:copy-of select="$stURL" />
                </a>
            </td>
        </tr>
    </xsl:template>

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

    <!-- ============================================================================================================================ -->
    <xsl:template name="printHistoryRow">
        <xsl:param name="underline" select="'false'" />
        <xsl:param name="sortOrder" select="'descending'" />
        <xsl:param name="printCurrent" select="'true'" />
        <xsl:param name="linkCurrent" select="'false'" />
        <xsl:param name="layout" select="'false'" />
        <xsl:param name="node" select="." />
        
        <xsl:variable name="selectPresentLang">
           <xsl:call-template name="selectPresentLang">
               <xsl:with-param name="nodes" select="$node/mycoreobject/metadata/maintitles/maintitle[@inherited=0]" />
           </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="objectID" select="xalan:nodeset($node)/mycoreobject/@ID" />
        <xsl:choose>
            <xsl:when test="$layout='true'">
                <span id="leaf-headline2">
                    <xsl:if test="contains(/mycoreobject/@ID,'jparticle') or
                                contains(/mycoreobject/@ID,'jpvolume') or
                                contains(/mycoreobject/@ID,'jpjournal') or
                                contains($objectID,'jparticle') or
                                contains($objectID,'jpvolume') or
                                contains($objectID,'jpjournal')">
                        <xsl:choose>
                            <xsl:when test="$sortOrder='descending'">
                                <xsl:for-each select="$node/mycoreobject/metadata/maintitles/maintitle[@xml:lang=$CurrentLang]">
                                    <xsl:sort select="@inherited" order="descending" />
                                    <xsl:call-template name="printHistoryRow.rows">
                                        <xsl:with-param name="sortOrder" select="$sortOrder" />
                                        <xsl:with-param name="printCurrent2" select="$printCurrent" />
                                        <xsl:with-param name="linkCurrent" select="$linkCurrent" />
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:for-each select="$node/mycoreobject/metadata/maintitles/maintitle[@xml:lang=$CurrentLang]">
                                    <xsl:sort select="@inherited" order="ascending" />
                                    <xsl:call-template name="printHistoryRow.rows">
                                        <xsl:with-param name="sortOrder" select="$sortOrder" />
                                        <xsl:with-param name="printCurrent2" select="$printCurrent" />
                                        <xsl:with-param name="linkCurrent" select="$linkCurrent" />
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </span>
                <xsl:if test="$underline='true'">
                    <table>
                        <tr>
                            <td id="leaf-headline1">_________________________________________________</td>
                        </tr>
                    </table>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                 <span id="leaf-headline2">
                    <xsl:if test="contains(/mycoreobject/@ID,'jparticle') or
                                contains(/mycoreobject/@ID,'jpvolume') or
                                contains(/mycoreobject/@ID,'jpjournal') or
                                contains($objectID,'jpjournal') or
                                contains($objectID,'jpvolume') or
                                contains($objectID,'jparticle')">
                        <xsl:choose>
                            <xsl:when test="$sortOrder='descending'">
                                <xsl:for-each select="$node/mycoreobject/metadata/maintitles/maintitle[@xml:lang=$CurrentLang or @xml:lang=$selectPresentLang]">
                                    <xsl:sort select="@inherited" order="descending" />
                                    <xsl:call-template name="printHistoryRow.rows">
                                        <xsl:with-param name="sortOrder" select="$sortOrder" />
                                        <xsl:with-param name="printCurrent2" select="$printCurrent" />
                                        <xsl:with-param name="linkCurrent" select="$linkCurrent" />
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:for-each select="$node/mycoreobject/metadata/maintitles/maintitle[@xml:lang=$CurrentLang or @xml:lang=$selectPresentLang]">
                                    <xsl:sort select="@inherited" order="ascending" />
                                    <xsl:call-template name="printHistoryRow.rows">
                                        <xsl:with-param name="sortOrder" select="$sortOrder" />
                                        <xsl:with-param name="printCurrent2" select="$printCurrent" />
                                        <xsl:with-param name="linkCurrent" select="$linkCurrent" />
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose> 
                      </xsl:if>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ============================================================================================================================ -->

    <xsl:template name="printHistoryRow.rows">
        <xsl:param name="sortOrder" />
        <xsl:param name="printCurrent2" />
        <xsl:param name="linkCurrent" />
        <xsl:choose>
            <xsl:when test="@inherited='0' ">
                <xsl:if test="$printCurrent2='true' ">
                    <span>
                        <xsl:variable name="date">
                            <xsl:if
                                test="/mycoreobject/metadata/dates/date[@inherited='0']/text()!='' and /mycoreobject/metadata/dates/date[@inherited='0']/text()!=/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text()">
                                <xsl:value-of select="concat(' (',/mycoreobject/metadata/dates/date[@inherited='0']/text(),')')" />
                            </xsl:if>
                        </xsl:variable>
                        <xsl:variable name="text">
                            <xsl:call-template name="ShortenText">
                                <xsl:with-param name="text" select="text()" />
                                <xsl:with-param name="length" select="25" />
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:variable name="label">
                            <xsl:value-of select="concat($text,$date)" />
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="$linkCurrent='true'">
                                <a href="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}?XSL.view.objectmetadata.SESSION=false" alt="{text()}"
                                    title="{text()}">
                                    <b>
                                        <xsl:value-of select="$label" />
                                    </b>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <b>
                                    <xsl:value-of select="$label" />
                                </b>
                            </xsl:otherwise>
                        </xsl:choose>
                    </span>
                </xsl:if>
            </xsl:when>
            <xsl:when test="@inherited='1' ">
                <xsl:if test="/mycoreobject/structure/parents/parent[@xlink:href!='']">
                    <xsl:variable name="date">
                        <xsl:if
                            test="/mycoreobject/metadata/dates/date[@inherited='1'] and /mycoreobject/metadata/dates/date[@inherited='1']!=/mycoreobject/metadata/maintitles/maintitle[@inherited='1'] and position()!=1">
                            <xsl:value-of select="concat(' (',/mycoreobject/metadata/dates/date[@inherited='1']/text(),')')" />
                        </xsl:if>
                    </xsl:variable>
                    <xsl:variable name="text">
                        <xsl:call-template name="ShortenText">
                            <xsl:with-param name="text" select="text()" />
                            <xsl:with-param name="length" select="25" />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="label">
                        <xsl:choose>
                            <xsl:when test="$sortOrder='descending'">
                                <xsl:choose>
                                    <xsl:when test="position()!=last() and $view.objectmetadata='true'">
                                        <xsl:value-of select="concat($text,$date, ' \ ')" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="concat($text,$date)" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="concat(' - ',$text,$date)" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:call-template name="objectLinking">
                        <xsl:with-param name="obj_id" select="/mycoreobject/structure/parents/parent/@xlink:href" />
                        <xsl:with-param name="obj_name" select="$label" />
                        <xsl:with-param name="hoverText" select="text()" />
                        <xsl:with-param name="requestParam" select="'XSL.toc.pos.SESSION=1&amp;XSL.view.objectmetadata.SESSION=true'" />
                    </xsl:call-template>
                </xsl:if>
            </xsl:when>
            <xsl:when test="@inherited!='1' and @inherited!='0'">
                <xsl:variable name="heritedLevel">
                    <xsl:value-of select="@inherited" />
                </xsl:variable>
                <xsl:variable name="date">
                    <xsl:if
                        test="/mycoreobject/metadata/dates/date[@inherited=$heritedLevel] and position()!=1 and /mycoreobject/metadata/dates/date[@inherited=position()]!=/mycoreobject/metadata/maintitles/maintitle[@inherited=position()]">
                        <xsl:value-of select="concat(' (',/mycoreobject/metadata/dates/date[@inherited=$heritedLevel]/text(),')')" />
                    </xsl:if>
                </xsl:variable>
                <xsl:variable name="text">
                    <xsl:call-template name="ShortenText">
                        <xsl:with-param name="text" select="text()" />
                        <xsl:with-param name="length" select="25" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="label">
                    <xsl:choose>
                        <xsl:when test="$sortOrder='descending'">
                            <xsl:choose>
                                <xsl:when test="position()!=last()-1">
                                    <xsl:value-of select="concat($text,$date, ' \ ')" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="concat($text,$date)" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat(' - ',$text,$date)" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:value-of select="$label" />
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- ============================================================================================================================ -->

    <xsl:template name="objectLinking">
        <xsl:param name="obj_id" />
        <xsl:param name="obj_name" />
        <xsl:param name="hoverText" />
        <xsl:param name="requestParam" />
        <xsl:param name="requestParam" />
        <xsl:param name="allowHTMLInResultLists" select="'false'" />

        <!-- 
            LOCAL REQUEST
        -->

        <xsl:if test="$objectHost = 'local'">

            <xsl:variable name="mcrobj" select="document(concat('mcrobject:',$obj_id))/mycoreobject" />

            <xsl:choose>

                <xsl:when test="acl:checkPermission($obj_id,'read')">

                    <a href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}?{$requestParam}" alt="{$hoverText}" title="{$hoverText}">
                        <xsl:choose>
                            <xsl:when test="$allowHTMLInArticles = 'true' or $allowHTMLInResultLists = 'true'">
                                <xsl:value-of disable-output-escaping="yes" select="$obj_name" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$obj_name" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </a>

                </xsl:when>

                <xsl:otherwise>

                    <!-- Build Login URL for LoginServlet -->

                    <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
                        select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?url=', encoder:encode( string( $RequestURL ) ) )" />

                    <xsl:apply-templates select="$mcrobj" mode="resulttitle" />

                    &#160;

                    <a href="{$LoginURL}">

                        <img src="{concat($WebApplicationBaseURL,'images/paper_lock.gif')}" />

                    </a>

                </xsl:otherwise>

            </xsl:choose>
        </xsl:if>
        <!-- 
            REMOTE REQUEST
        -->
        <xsl:if test="$objectHost != 'local'">

            <xsl:variable name="mcrobj" select="document(concat('mcrws:operation=MCRDoRetrieveObject&amp;host=',$objectHost,'&amp;ID=',$obj_id))/mycoreobject" />
            <a href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}?host={@host}&amp;{$requestParam}">

                <xsl:apply-templates select="$mcrobj" mode="resulttitle" />
            </a>
        </xsl:if>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="setParameter">
        <xsl:param name="param" />
        <xsl:param name="paramValue" />
        <!-- to verify within template -->
        <xsl:param name="labelOFF" />
        <xsl:param name="labelON" />

        <xsl:variable name="RequestURL_view-Deleted">
            <xsl:call-template name="UrlDelParam">
                <xsl:with-param name="url" select="$RequestURL" />
                <xsl:with-param name="par" select="concat('XSL.',$param)" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="targetURL">
            <xsl:call-template name="UrlDelParam">
                <xsl:with-param name="url" select="$RequestURL_view-Deleted" />
                <xsl:with-param name="par" select="concat('XSL.',$param,'.SESSION')" />
            </xsl:call-template>
        </xsl:variable>
        <table id="switch" cellspacing="0" cellpadding="0" border="0">
            <tr>
                <xsl:choose>
                    <xsl:when test="$paramValue='false'">
                        <td>
                            <div id="switch-current">
                                <xsl:value-of select="$labelON" />
                            </div>
                        </td>
                        <td width="20"></td>
                        <td>
                            <div id="switch-notcurrent">
                                <xsl:variable name="targetURL_withParam">
                                    <xsl:call-template name="UrlSetParam">
                                        <xsl:with-param name="url" select="$targetURL" />
                                        <xsl:with-param name="par" select="concat('XSL.',$param,'.SESSION')" />
                                        <xsl:with-param name="value" select="'true'" />
                                    </xsl:call-template>
                                </xsl:variable>
                                <a href="{$targetURL_withParam}">
                                    <xsl:value-of select="$labelOFF" />
                                </a>
                            </div>
                        </td>

                    </xsl:when>
                    <xsl:otherwise>
                        <td>
                            <div id="switch-notcurrent">
                                <xsl:variable name="targetURL_withParam">
                                    <xsl:call-template name="UrlSetParam">
                                        <xsl:with-param name="url" select="$targetURL" />
                                        <xsl:with-param name="par" select="concat('XSL.',$param,'.SESSION')" />
                                        <xsl:with-param name="value" select="'false'" />
                                    </xsl:call-template>
                                </xsl:variable>
                                <a href="{$targetURL_withParam}">
                                    <xsl:value-of select="concat($labelON,' anzeigen')" />
                                </a>
                            </div>
                        </td>
                        <td width="20"></td>
                        <td>
                            <div id="switch-current">
                                <xsl:value-of select="$labelOFF" />
                            </div>
                        </td>
                    </xsl:otherwise>
                </xsl:choose>
            </tr>
        </table>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="printSwitchViewBar">
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

        <table id="switch" cellspacing="0" cellpadding="0" border="0">
            <tr>
                <!-- oncle buttons :-) -->
                <xsl:if test="/mycoreobject/structure/parents/parent/@xlink:href">
                    <xsl:variable name="currentID">
                        <xsl:value-of select="/mycoreobject/@ID" />
                    </xsl:variable>
                    <xsl:variable name="OID">
                        <xsl:call-template name="typeOfObjectID">
                            <xsl:with-param name="id" select="/mycoreobject/@ID" />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="mcrSql" xmlns:encoder="xalan://java.net.URLEncoder">
                        <xsl:value-of select="encoder:encode(concat('parent = ',/mycoreobject/structure/parents/parent/@xlink:href))" />
                    </xsl:variable>
                    <xsl:variable name="sort">
                        <xsl:call-template name="get.sortKey">
                            <xsl:with-param name="kindOfChildren" select="$OID" />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="sortOrder">
                        <xsl:call-template name="get.sortOrder">
                            <xsl:with-param name="kindOfChildren" select="$OID" />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="siblings">
                        <xsl:copy-of select="xalan:nodeset(document(concat('query:term=',$mcrSql,$sort,'&amp;order=',$sortOrder)))" />
                    </xsl:variable>
                    <xsl:variable name="currentNode">
                        <xsl:copy-of select="xalan:nodeset($siblings)/mcr:results/mcr:hit[@id=$currentID]" />
                    </xsl:variable>

                    <xsl:for-each select="xalan:nodeset($siblings)/mcr:results/mcr:hit">
                        <xsl:variable name="pos">
                            <xsl:value-of select="position()" />
                        </xsl:variable>
                        <xsl:if test="@id=$currentID">
                            <xsl:variable name="pred">
                                <xsl:value-of select="xalan:nodeset($siblings)/mcr:results/mcr:hit[position()=number($pos)-1]/@id" />
                            </xsl:variable>
                            <xsl:variable name="suc">
                                <xsl:value-of select="xalan:nodeset($siblings)/mcr:results/mcr:hit[position()=number($pos)+1]/@id" />
                            </xsl:variable>

                            <xsl:choose>
                                <xsl:when test="$OID='jparticle'">
                                    <xsl:if test="$pred!=''">
                                        <td id="detailed-browse">
                                            <a href="{concat($WebApplicationBaseURL,'receive/',$pred,$HttpSession)}?XSL.toc.pos.SESSION=1"
                                                alt="{i18n:translate('metaData.jparticle.switchleft')}"
                                                title="{i18n:translate('metaData.jparticle.switchleft')}">
                                                <img src="{$WebApplicationBaseURL}left.gif" />
                                            </a>
                                        </td>
                                    </xsl:if>
                                    <xsl:if test="$suc!=''">
                                        <td id="detailed-browse">
                                            <a href="{concat($WebApplicationBaseURL,'receive/',$suc,$HttpSession)}?XSL.toc.pos.SESSION=1"
                                                alt="{i18n:translate('metaData.jparticle.switchright')}"
                                                title="{i18n:translate('metaData.jparticle.switchright')}">
                                                <img src="{$WebApplicationBaseURL}right.gif" />
                                            </a>
                                        </td>
                                    </xsl:if>
                                    <td width="20"></td>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:if test="$pred!=''">
                                        <td id="detailed-browse">
                                            <a href="{concat($WebApplicationBaseURL,'receive/',$pred,$HttpSession)}?XSL.toc.pos.SESSION=1"
                                                alt="{i18n:translate('metaData.jpvolume.switchleft')}"
                                                title="{i18n:translate('metaData.jpvolume.switchleft')}">
                                                <img src="{$WebApplicationBaseURL}left.gif" />
                                            </a>
                                        </td>
                                    </xsl:if>
                                    <xsl:if test="$suc!=''">
                                        <td id="detailed-browse">
                                            <a href="{concat($WebApplicationBaseURL,'receive/',$suc,$HttpSession)}?XSL.toc.pos.SESSION=1"
                                                alt="{i18n:translate('metaData.jpvolume.switchright')}"
                                                title="{i18n:translate('metaData.jpvolume.switchright')}">
                                                <img src="{$WebApplicationBaseURL}right.gif" />
                                            </a>
                                        </td>
                                    </xsl:if>
                                    <td width="20"></td>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="$CurrentUser!='gast' and contains(/mycoreobject/@ID,'jpvolume') or contains(/mycoreobject/@ID,'jpjournal')">
                    <td>
                        <xsl:choose>
                            <xsl:when test="$view.objectmetadata='false'">
                                <div id="switch-current">
                                    <xsl:value-of select="i18n:translate('metadata.navi.detailact')" />
                                </div>
                            </xsl:when>
                            <xsl:otherwise>
                                <div id="switch-notcurrent">
                                    <xsl:variable name="URLDetails">
                                        <xsl:call-template name="UrlSetParam">
                                            <xsl:with-param name="url" select="concat($WebApplicationBaseURL,'receive/',/mycoreobject/@ID,$HttpSession)" />
                                            <xsl:with-param name="par" select="'XSL.view.objectmetadata.SESSION'" />
                                            <xsl:with-param name="value" select="'false'" />
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <a href="{$URLDetails}">
                                        <xsl:value-of select="i18n:translate('metadata.navi.showdetail')" />
                                    </a>
                                </div>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                    <td width="20"></td>
                    <td>
                        <xsl:choose>
                            <xsl:when test="$view.objectmetadata='true'">
                                <div id="switch-current">
                                    <xsl:value-of select="i18n:translate('metadata.navi.contentact')" />
                                </div>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="/mycoreobject[contains(@ID,'_jparticle_')]
                                        or  $children='false'">
                                        <div id="switch-notcurrent">
                                            <xsl:variable name="URLDetails">
                                                <xsl:call-template name="UrlSetParam">
                                                    <xsl:with-param name="url"
                                                        select="concat($WebApplicationBaseURL,'receive/',/mycoreobject/structure/parents/parent/@xlink:href,$HttpSession)" />
                                                    <xsl:with-param name="par" select="'XSL.view.objectmetadata.SESSION'" />
                                                    <xsl:with-param name="value" select="'true'" />
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <a href="{$URLDetails}">
                                                <xsl:value-of
                                                    select="concat(i18n:translate('metadata.navi.content'),' ',/mycoreobject/metadata/maintitles/maintitle[@inherited='1']/text(),' ',i18n:translate('metadata.navi.show'))" />
                                            </a>
                                        </div>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <div id="switch-notcurrent">
                                            <xsl:variable name="URLDetails">
                                                <xsl:call-template name="UrlSetParam">
                                                    <xsl:with-param name="url"
                                                        select="concat($WebApplicationBaseURL,'receive/',/mycoreobject/@ID,$HttpSession)" />
                                                    <xsl:with-param name="par" select="'XSL.view.objectmetadata.SESSION'" />
                                                    <xsl:with-param name="value" select="'true'" />
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <a href="{$URLDetails}">
                                                <xsl:value-of select="i18n:translate('metadata.navi.showcontent')" />
                                            </a>
                                        </div>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>

                </xsl:if>
                <td>&#160;&#160;&#160;&#160;&#160;&#160;</td>
                <xsl:call-template name="browseCtrlJP" />
                <td>&#160;&#160;&#160;&#160;&#160;&#160;</td>
                <xsl:call-template name="SwitchToXMLview" />
                <xsl:if test="/mycoreobject[contains(@ID,'_jpvolume_')] 
        | /mycoreobject[contains(@ID,'_jparticle_')]">
                    <xsl:call-template name="linkFile" />
                </xsl:if>
            </tr>
        </table>
        <br />
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="get.sortKey">
        <xsl:param name="kindOfChildren" />
        <xsl:choose>
            <xsl:when test="($kindOfChildren='jpvolume') and ($toc.sortBy.jpvolume='title')">
                <xsl:value-of select="'&amp;sortby=maintitles_vol'" />
            </xsl:when>
            <xsl:when test="($kindOfChildren='jpvolume') and ($toc.sortBy.jpvolume='position')">
                <xsl:value-of select="'&amp;sortby=position_vol'" />
            </xsl:when>
            <xsl:when test="($kindOfChildren='jparticle') and ($toc.sortBy.jparticle='title')">
                <xsl:value-of select="'&amp;sortby=maintitles_art'" />
            </xsl:when>
            <xsl:when test="($kindOfChildren='jparticle') and ($toc.sortBy.jparticle='size')">
                <xsl:value-of select="'&amp;sortby=sizes_art'" />
            </xsl:when>
            <xsl:when test="($kindOfChildren='jparticle') and ($toc.sortBy.jparticle='position')">
                <xsl:value-of select="'&amp;sortby=dates_art'" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="''" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="get.sortOrder">
        <xsl:param name="kindOfChildren" />
        <xsl:choose>
            <xsl:when test="($kindOfChildren='jparticle') and ($toc.sortBy.jparticle='position')">
                <xsl:value-of select="'descending'" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'ascending'" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="printDerivates">
        <xsl:param name="obj_id" />
        <xsl:param name="knoten" />

        <xsl:choose>
            <!-- metadata view -->
            <xsl:when test="$knoten=''">
                <xsl:if test="./structure/derobjects">
                    <xsl:if test="$objectHost = 'local'">
                        <xsl:for-each select="./structure/derobjects/derobject">
                            <xsl:variable name="deriv" select="@xlink:href" />
                            <tr>
                                <td align="left" valign="top" id="detailed-links">
                                    <table cellpadding="0" cellspacing="0" id="detailed-contenttable">
                                        <xsl:variable name="deriv" select="@xlink:href" />
                                        <xsl:variable name="derivlink" select="concat('mcrobject:',$deriv)" />
                                        <xsl:variable name="derivate" select="document($derivlink)" />
                                        <tr>
                                            <td colspan="3" style="padding-left: 10px;">
                                                <xsl:apply-templates select="$derivate/mycorederivate/derivate/internals">
                                                    <xsl:with-param name="objID" select="$obj_id" />
                                                    <xsl:with-param name="objectXML" select="document(concat('mcrobject:',$obj_id))" />
                                                </xsl:apply-templates>
                                                <xsl:apply-templates select="$derivate/mycorederivate/derivate/externals">
                                                    <xsl:with-param name="objID" select="$obj_id" />
                                                </xsl:apply-templates>
                                            </td>
                                        </tr>
                                        <tr>
                                            <xsl:if test="acl:checkPermission($obj_id,'writedb')">
                                                <td width="30" valign="top" align="center">
                                                    <form method="get">
                                                        <xsl:attribute name="action">
                                                            <xsl:value-of select="concat($WebApplicationBaseURL,'servlets/MCRStartEditorServlet',$JSessionID)" />
                                                        </xsl:attribute>
                                                        <input name="lang" type="hidden" value="{$CurrentLang}" />
                                                        <input name="se_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="@xlink:href" />
                                                            </xsl:attribute>
                                                        </input>
                                                        <input name="te_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="@xlink:href" />
                                                            </xsl:attribute>
                                                        </input>
                                                        <input name="re_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="$obj_id" />
                                                            </xsl:attribute>
                                                        </input>
                                                        <xsl:variable name="type">
                                                            <xsl:copy-of select="substring-before(substring-after($obj_id,'_'),'_')" />
                                                        </xsl:variable>
                                                        <input name="type" type="hidden" value="{$type}" />
                                                        <input name="todo" type="hidden" value="saddfile" />
                                                        <input type="image" src="{$WebApplicationBaseURL}images/workflow_deradd.gif"
                                                            title="{i18n:translate('swf.derivate.addFile')}" />
                                                    </form>
                                                </td>
                                                <td width="30" valign="top" align="center">
                                                    <form method="get">
                                                        <xsl:attribute name="action">
                                                            <xsl:value-of select="concat($WebApplicationBaseURL,'servlets/MCRStartEditorServlet',$JSessionID)" />
                                                        </xsl:attribute>
                                                        <input name="lang" type="hidden" value="{$CurrentLang}" />
                                                        <input name="se_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="@xlink:href" />
                                                            </xsl:attribute>
                                                        </input>
                                                        <input name="te_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="@xlink:href" />
                                                            </xsl:attribute>
                                                        </input>
                                                        <input name="re_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="$obj_id" />
                                                            </xsl:attribute>
                                                        </input>
                                                        <xsl:variable name="type">
                                                            <xsl:copy-of select="substring-before(substring-after($obj_id,'_'),'_')" />
                                                        </xsl:variable>
                                                        <input name="type" type="hidden" value="{$type}" />
                                                        <input name="todo" type="hidden" value="seditder" />
                                                        <input name="extparm" type="hidden">
                                                            <xsl:variable name="deriv" select="@xlink:href" />
                                                            <xsl:variable name="derivlink" select="concat('mcrobject:',$deriv)" />
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="document($derivlink)/mcr_results/mcr_result/@label" />
                                                            </xsl:attribute>
                                                        </input>
                                                        <input type="image" src="{$WebApplicationBaseURL}images/workflow_deredit.gif"
                                                            title="{i18n:translate('swf.derivate.editDerivate')}" />
                                                    </form>
                                                </td>
                                                <td width="30" valign="top" align="center">
                                                    <form method="get">
                                                        <xsl:attribute name="action">
                                                            <xsl:value-of select="concat($WebApplicationBaseURL,'servlets/MCRStartEditorServlet',$JSessionID)" />
                                                        </xsl:attribute>
                                                        <input name="lang" type="hidden" value="{$CurrentLang}" />
                                                        <input name="se_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="@xlink:href" />
                                                            </xsl:attribute>
                                                        </input>
                                                        <input name="te_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="@xlink:href" />
                                                            </xsl:attribute>
                                                        </input>
                                                        <input name="re_mcrid" type="hidden">
                                                            <xsl:attribute name="value">
                                                                <xsl:value-of select="$obj_id" />
                                                            </xsl:attribute>
                                                        </input>
                                                        <xsl:variable name="type">
                                                            <xsl:copy-of select="substring-before(substring-after($obj_id,'_'),'_')" />
                                                        </xsl:variable>
                                                        <input name="type" type="hidden" value="{$type}" />
                                                        <input name="todo" type="hidden" value="sdelder" />
                                                        <input type="image" src="{$WebApplicationBaseURL}images/workflow_derdelete.gif"
                                                            title="{i18n:translate('swf.derivate.delDerivate')}" />
                                                    </form>
                                                </td>
                                            </xsl:if>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr id="detailed-whitespaces">
                                <td></td>
                            </tr>
                        </xsl:for-each>
                    </xsl:if>
                </xsl:if>
            </xsl:when>
            <!-- search result list -->
            <xsl:otherwise>
                <xsl:if test="xalan:nodeset($knoten)/mycoreobject/structure/derobjects">
                    <tr>
                        <td id="leaf-additional">
                            <xsl:call-template name="lineSpace" />
                            <table cellpadding="0" cellspacing="0">
                                <xsl:call-template name="get.labelOfDigitalMedias" />
                                <xsl:choose>
                                    <!-- fulltext hit -->
                                    <xsl:when test="mcr:metaData">
                                        <xsl:for-each select="mcr:metaData">
                                            <xsl:apply-templates select=".">
                                                <xsl:with-param name="objID" select="$obj_id" />
                                                <xsl:with-param name="objectXML" select="$knoten" />                                                
                                            </xsl:apply-templates>
                                            <xsl:copy-of select="' (Treffer im Volltext)'" />
                                            <xsl:if test="position() != last()">
                                                <xsl:copy-of select="' '" />
                                                <xsl:call-template name="lineSpace" />
                                            </xsl:if>
                                        </xsl:for-each>
                                    </xsl:when>
                                    <!-- hit not in fulltext -->
                                    <xsl:otherwise>
                                        <xsl:for-each select="xalan:nodeset($knoten)/mycoreobject/structure/derobjects/derobject">
                                            <xsl:variable name="deriv" select="@xlink:href" />
                                            <xsl:variable name="derivlink" select="concat('mcrobject:',$deriv)" />
                                            <xsl:variable name="derivate" select="document($derivlink)" />
                                            <xsl:apply-templates select="$derivate/mycorederivate/derivate/internals">
                                                <xsl:with-param name="objID" select="$obj_id" />
                                                <xsl:with-param name="objectXML" select="$knoten" />
                                            </xsl:apply-templates>
                                            <xsl:apply-templates select="$derivate/mycorederivate/derivate/externals">
                                                <xsl:with-param name="objID" select="$obj_id" />
                                                <xsl:with-param name="objectXML" select="$knoten" />
                                            </xsl:apply-templates>
                                            <xsl:if test="position()!=last()">
                                                <xsl:copy-of select="' '"></xsl:copy-of>
                                                <xsl:call-template name="lineSpace" />
                                            </xsl:if>
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </table>
                        </td>
                    </tr>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
        <!-- links  -->
        <xsl:choose>
            <xsl:when test="$knoten != '' and not(mcr:metaData)">
                <xsl:if test="xalan:nodeset($knoten)/mycoreobject/metadata/ifsLinks/ifsLink">
                    <tr>
                        <td id="leaf-additional">
                            <xsl:call-template name="lineSpace" />
                            <table cellpadding="0" cellspacing="0">
                                <xsl:for-each select="xalan:nodeset($knoten)/mycoreobject/metadata/ifsLinks/ifsLink">
                                    <xsl:apply-templates select=".">
                                        <xsl:with-param name="objID" select="$obj_id" />
                                    </xsl:apply-templates>
                                    <xsl:if test="position()!=last()">
                                        <xsl:copy-of select="', '" />
                                        <xsl:call-template name="lineSpace" />
                                    </xsl:if>
                                </xsl:for-each>
                            </table>
                        </td>
                    </tr>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="/mycoreobject/metadata/ifsLinks/ifsLink">
                    <tr>
                        <td align="left" valign="top" id="detailed-links">
                            <table cellpadding="0" cellspacing="0" id="detailed-contenttable">
                                <tr>
                                    <td colspan="3" style="padding-left: 10px;">
                                        <xsl:apply-templates select=".">
                                            <xsl:with-param name="objID" select="$obj_id" />
                                        </xsl:apply-templates>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <xsl:if test="acl:checkPermission($obj_id,'deletedb')">
                        <tr>
                            <td colspan="3" width="30" valign="top" align="center">
                                <xsl:variable name="url">
                                    <xsl:value-of
                                        select="concat($ServletsBaseURL,'MCRJPortalLinkFileServlet?jportalLinkFileServlet.mode=removeLink&amp;jportalLinkFileServlet.from=',$obj_id,'&amp;jportalLinkFileServlet.to=',text())" />
                                </xsl:variable>
                                <a href="{$url}">
                                    <img src="{$WebApplicationBaseURL}images/workflow_derdelete.gif" title="Diesen Link entfernen" alt="Diesen Link entfernen" />
                                </a>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="isFlagSet">
      <xsl:param name="flagName" />
      <xsl:for-each select="./service/servflags/servflag">
        <xsl:if test="text() = $flagName" >
          <xsl:value-of select="'true'" />
        </xsl:if>
      </xsl:for-each>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="isFlagTypeSet">
      <xsl:param name="flagName" />
      <xsl:for-each select="./service/servflags/servflag">
        <xsl:if test="@type = $flagName" >
          <xsl:value-of select="'true'" />
        </xsl:if>
      </xsl:for-each>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="get.labelOfDigitalMedias">
        <i>Digitalisat(e):</i>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template match="internals | ifsLink | mcr:metaData" priority="2">
        <xsl:param name="objID" />
        <xsl:param name="objectXML" />
        <xsl:param name="detailed-view" />
        <xsl:if test="$objectHost = 'local'">
            <xsl:variable name="derivid">
                <xsl:choose>
                    <!-- links -->
                    <xsl:when test="name() = 'ifsLink'">
                        <xsl:value-of select="substring-before(./text(),'/')" />
                    </xsl:when>
                    <!-- full text hit -->
                    <xsl:when test="name() = 'mcr:metaData'">
                        <xsl:value-of select="mcr:field[@name='DerivateID']/text()" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="../../@ID" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            
            <xsl:variable name="derivmain">
               	<xsl:choose>
                   	<!-- links -->
                   	<xsl:when test="name() = 'ifsLink'">
                        <xsl:value-of select="substring-after(./text(),'/')" />
                   	</xsl:when>
                   	<!-- full text hit -->
                   	<xsl:when test="name() = 'mcr:metaData'">
                        <!-- <xsl:value-of select="mcr:field[@name='filePath']/text()" /> -->
                        <!-- check if a file mapping has to be done -->
                        <xsl:call-template name="mappFile">
           					<xsl:with-param name="derivid-if" select="$derivid"/>
           					<xsl:with-param name="file" select="mcr:field[@name='filePath']/text()" />
           				</xsl:call-template>
                   	</xsl:when>
                    <xsl:otherwise>
                       	<xsl:value-of select="internal/@maindoc" />
                   	</xsl:otherwise>
               	</xsl:choose>
            </xsl:variable>
            
            <xsl:variable name="derivbase">
                <xsl:value-of select="concat($ServletsBaseURL,'MCRFileNodeServlet/',$derivid,'/')" />
            </xsl:variable>
            <xsl:variable name="fileType">
                <xsl:call-template name="getFileType">
                    <xsl:with-param name="fileName" select="$derivmain" />
                </xsl:call-template>
            </xsl:variable>
            <!-- IView available ? -->
            <xsl:variable name="supportedMainFile">
                <xsl:call-template name="iview.getSupport.hack">
                    <xsl:with-param name="derivid_2" select="$derivid" />
                    <xsl:with-param name="mainFile" select="$derivmain" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="href">
                <xsl:choose>
                    <xsl:when test="$supportedMainFile != ''">
                        <xsl:value-of select="$supportedMainFile" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <!-- remove double slash if exist -->
                            <xsl:when test="substring($derivbase,string-length($derivbase),1) = '/' and substring($derivmain,1,1) = '/'">
                                <xsl:value-of select="concat($derivbase,substring-after($derivmain,'/'))" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="concat($derivbase,$derivmain)" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="fileLabel">
                <xsl:call-template name="getFileLabel">
                    <xsl:with-param name="typeOfFile" select="$fileType" />
                </xsl:call-template>
            </xsl:variable>
            <!-- access to edit ? -->
            <xsl:variable name="editAccess">
                <!--  <xsl:value-of select="acl:checkPermission($objID,'writedb') or acl:checkPermission($objID,'deletedb')" />-->
                <xsl:choose>
                    <xsl:when test="acl:checkPermission($objID,'writedb') or acl:checkPermission($objID,'deletedb')">
                        <xsl:value-of select="'true'"></xsl:value-of>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="'false'"></xsl:value-of>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <!-- read access ? -->
            <xsl:variable name="readAccess4Derivates">
                <xsl:choose>
                    <xsl:when test="$readAccessForDerivates = ''">
                        <xsl:variable name="jourID">
                            <xsl:value-of select="xalan:nodeset($objectXML)/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()" />
                        </xsl:variable>
                        <xsl:call-template name="get.readAccessForDerivates">
                            <xsl:with-param name="jID" select="$jourID" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="$readAccessForDerivates = 'true' or $editAccess = 'true'">
                        <xsl:value-of select="'true'"></xsl:value-of>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="'false'"></xsl:value-of>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:choose>
                <xsl:when test="$thumbnail='true'">
                    <table cellpadding="0" cellspacing="0" id="detailed-contenttable">
                        <tr id="detailed-contentsimg">
                            <td id="detailed-contentsimgpadd">
                                <xsl:choose>
                                    <xsl:when test="($supportedMainFile!='')">
                                        <xsl:call-template name="iview.getEmbedded.thumbnail">
                                            <xsl:with-param name="derivID" select="$derivid" />
                                            <xsl:with-param name="pathOfImage" select="concat('/',$derivmain)" />
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <img src="{concat($WebApplicationBaseURL,'/images/dummyPreview.png')}" />
                                    </xsl:otherwise>
                                </xsl:choose>
                                <br />
                            </td>
                        </tr>
                        <tr id="detailed-contents">
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$readAccess4Derivates = 'true'">
                                        <xsl:variable name="label">
                                            <xsl:choose>
                                                <xsl:when test="name() = 'ifsLink'">
                                                    <xsl:value-of select="concat($fileLabel, '(~)')" />
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$fileLabel" />
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>
                                        <a href="{$href}">
                                            <xsl:value-of select="$label" />
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>Zugriff gesperrt!</xsl:otherwise>
                                </xsl:choose>
                                <xsl:text>
                                </xsl:text>
                                <xsl:if test="$editAccess = 'true'">
                                    <a href="{$derivbase}">
                                        <xsl:value-of select="', Details &gt;&gt; '" />
                                    </a>
                                </xsl:if>
                            </td>
                        </tr>
                    </table>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="$readAccess4Derivates = 'true'">
                            <xsl:variable name="label">
                                <xsl:choose>
                                    <xsl:when test="name()='ifsLink'">
                                        <xsl:value-of select="concat($fileLabel, '(~)')" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$fileLabel" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <a href="{$href}">
                                <xsl:value-of select="$label" />
                            </a>
                        </xsl:when>
                        <xsl:otherwise>Zugriff gesperrt !</xsl:otherwise>
                    </xsl:choose>

                    <xsl:if test="$editAccess = 'true'">
                        <a href="{$derivbase}">
                            <xsl:value-of select="', Details &gt;&gt; '" />
                        </a>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
    
    <!-- ===================================================================================================== -->
   
    <xsl:template name="mappFile">
        <xsl:param name="derivid-if" />
        <xsl:param name="file" />
        <xsl:param name="fileExistVerification" select="'true'"/>
        
        <xsl:variable name="fileMappings">
            <xsl:copy-of select="document('webapp:fileMappings.xml')" />
        </xsl:variable>
        <xsl:choose>
            <!-- file mapping(s) available ? -->
            <xsl:when test="xalan:nodeset($fileMappings)/fileMappings/fileMapping" >        
                <xsl:variable name="fileName">
                    <xsl:value-of select="substring-after(substring-before($file,'.'),'/')" />
                </xsl:variable>
                <xsl:variable name="fileExt">
                    <xsl:call-template name="getFileType">
                        <xsl:with-param name="fileName" select="$file" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="fileContentTypes">
                    <xsl:copy-of select="document('webapp:FileContentTypes.xml')" />
                </xsl:variable>
                <xsl:variable name="idOfFileType">
                    <xsl:value-of select="xalan:nodeset($fileContentTypes)/FileContentTypes/type/rules/extension[text()=$fileExt]/../../@ID" />
                </xsl:variable>
                <xsl:variable name="transFileList">
                    <!-- file type exist AND file type must be mapped -->
                    <xsl:if test="$idOfFileType != '' and xalan:nodeset($fileMappings)/fileMappings/fileMapping/type[@ID=$idOfFileType]">
                       <!-- make a difference between real file verification and blind guessing of mappable file -->
                       <xsl:choose>
                            <!-- file existing will be verified -->
                            <xsl:when test="$fileExistVerification = 'true'">
                                <xsl:variable name="derivXML" select="document(concat('jportal_getDerDirXML:',$derivid-if))" />
                                <!-- go through all mappable file extension id's -->
                                <xsl:for-each select="xalan:nodeset($fileMappings)/fileMappings/fileMapping/type[@ID=$idOfFileType]/../mappTo/type">
                                    <xsl:variable name="mappID" select="@ID" />
                                    <!--  go throug all file extensions belonging to extension id's -->
                                    <xsl:for-each select="xalan:nodeset($fileContentTypes)/FileContentTypes/type[@ID=$mappID]/rules/extension">
                                        <!-- is current extension id in derivate xml ? -->
                                        <xsl:variable name="fileNameTmp" select="concat($fileName,'.',text())" />
                                        <xsl:variable name="mappableNode" select="xalan:nodeset($derivXML)/mcr_directory/children/child/name[text()=$fileNameTmp]" />
                                        <xsl:if test="$mappableNode">
                                            <xsl:value-of select="concat($mappableNode,$derivid-if)" />
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:for-each>
                            </xsl:when>
                            <!-- NO file existing won't be verified -> blind taking of first file in FileContentTypes.xml -->
                            <xsl:otherwise>
                               <xsl:variable name="mappExtID" select="xalan:nodeset($fileMappings)/fileMappings/fileMapping/type[@ID=$idOfFileType]/../mappTo/type[position()=1]/@ID"/>
                               <xsl:variable name="mappFileExt" select="xalan:nodeset($fileContentTypes)/FileContentTypes/type[@ID=$mappExtID]/rules/extension[position()=1]/text()" />
                               <xsl:value-of select="concat($fileName,'.',$mappFileExt,$derivid-if)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </xsl:variable>
                <!-- return translated file -->
                <xsl:choose>
                    <xsl:when test="contains($transFileList,$derivid-if)">
                        <xsl:value-of select="substring-before($transFileList,$derivid-if)" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$file" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$file" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
	<!-- ===================================================================================================== -->
	
    <xsl:template name="getFileLabel">
        <xsl:param name="typeOfFile" />
        <xsl:variable name="label">
            <xsl:value-of select="document('webapp:FileContentTypes.xml')/FileContentTypes/type[rules/extension/text()=$typeOfFile]/label/text()" />
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$label = ''">
                <xsl:value-of select="concat(' ',i18n:translate('metaData.digitalisat'),' (',$typeOfFile,') ')" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$CurrentLang='de'">
                        <xsl:value-of select="concat(' ',$label,' ',i18n:translate('metaData.digitalisat.show'),' ')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat(' ',i18n:translate('metaData.digitalisat.show'),' ',$label,' ')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="iview.getSupport.hack">
        <xsl:param name="derivid_2" />
        <xsl:param name="mainFile" />

        <xsl:variable name="fileType">
            <xsl:call-template name="getFileType">
                <xsl:with-param name="fileName" select="$mainFile" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$fileType!=''">
                <xsl:choose>
                    <xsl:when test="contains($MCR.Module-iview.SupportedContentTypes,$fileType)">
                        <xsl:call-template name="iview.getAddress.hack">
                            <xsl:with-param name="fullPathOfImage" select="concat($derivid_2,'/',$mainFile)" />
                            <xsl:with-param name="height" select="'510'" />
                            <xsl:with-param name="width" select="'605'" />
                            <xsl:with-param name="scaleFactor" select="'fitToWidth'" />
                            <xsl:with-param name="display" select="'extended'" />
                            <xsl:with-param name="style" select="'image'" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="''" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="''" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="getFileType">
        <xsl:param name="fileName" />
        <xsl:value-of select="substring($fileName,number(string-length($fileName)-2))" />
    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="iview.getAddress.hack">
        <xsl:param name="fullPathOfImage" />
        <xsl:param name="height" />
        <xsl:param name="width" />
        <xsl:param name="scaleFactor" />
        <xsl:param name="display" />
        <xsl:param name="style" />
        <xsl:value-of
            select="concat($iview.home,$fullPathOfImage,$HttpSession,'?mode=generateLayout&amp;XSL.MCR.Module-iview.navi.zoom.SESSION=',$scaleFactor,'&amp;XSL.MCR.Module-iview.display.SESSION=',$display,'&amp;XSL.MCR.Module-iview.style.SESSION=',$style,'&amp;XSL.MCR.Module-iview.lastEmbeddedURL.SESSION=',$lastEmbeddedURL,'&amp;XSL.MCR.Module-iview.embedded.SESSION=false&amp;XSL.MCR.Module-iview.move=reset')" />
    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="printChildren">

        <xsl:variable name="kindOfChildren2">
            <xsl:choose>
                <xsl:when test="./structure/children/child[position()=1]/@xlink:href">
                    <xsl:call-template name="typeOfObjectID">
                        <xsl:with-param name="id" select="./structure/children/child[position()=1]/@xlink:href" />
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="''" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="mcrSql" xmlns:encoder="xalan://java.net.URLEncoder">
            <xsl:value-of select="encoder:encode(concat('parent = ',./@ID))" />
        </xsl:variable>
        <xsl:variable name="sort">
            <xsl:call-template name="get.sortKey">
                <xsl:with-param name="kindOfChildren" select="$kindOfChildren2" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="sortOrder">
            <xsl:call-template name="get.sortOrder">
                <xsl:with-param name="kindOfChildren" select="$kindOfChildren2" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="children">
            <xsl:copy-of select="xalan:nodeset(document(concat('query:term=',$mcrSql,$sort,'&amp;order=',$sortOrder)))" />
        </xsl:variable>
        <xsl:call-template name="printTOCNavi">
            <xsl:with-param name="location" select="'navi'" />
            <xsl:with-param name="childrenKinds" select="$kindOfChildren2" />
            <xsl:with-param name="childrenXML" select="xalan:nodeset($children)" />
        </xsl:call-template>
        <xsl:variable name="toc.pos.verif">
            <xsl:choose>
                <xsl:when test="$toc.pageSize>count(xalan:nodeset($children)/mcr:results/mcr:hit)">
                    <xsl:value-of select="1" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$toc.pos" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <table id="resultList" cellpadding="0" cellspacing="0">
            <xsl:for-each select="xalan:nodeset($children)/mcr:results/mcr:hit[(position()>=$toc.pos.verif) and ($toc.pos.verif+$toc.pageSize>position())]">
                <xsl:variable name="mcrobj" select="document(concat('mcrobject:',@id))/mycoreobject" />
                <xsl:apply-templates select="." mode="toc">
                    <xsl:with-param name="mcrobj" select="$mcrobj" />
                    <xsl:with-param name="mcrobjlink">
                        <xsl:call-template name="objectLink">
                            <xsl:with-param name="obj_id" select="@id" />
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:apply-templates>
            </xsl:for-each>
        </table>

    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="printTOCNavi">
        <xsl:param name="location" />
        <xsl:param name="childrenKinds" />
        <xsl:param name="childrenXML" />

        <xsl:variable name="pred">
            <xsl:value-of select="number($toc.pos)-(number($toc.pageSize)+1)" />
        </xsl:variable>
        <xsl:variable name="succ">
            <xsl:value-of select="number($toc.pos)+number($toc.pageSize)+1" />
        </xsl:variable>
        <xsl:variable name="numChildren">
            <xsl:value-of select="count(/mycoreobject/structure/children//child)" />
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$location='navi'">
                <table>
                    <tr>
                        <td colspan="2">
                            <xsl:value-of select="concat(i18n:translate('metaData.sortbuttons.numberofres'),': ')" />
                            <b>
                                <xsl:value-of select="$numChildren" />
                            </b>
                            <xsl:call-template name="printTOCNavi.chooseHitPage">
                                <xsl:with-param name="children" select="xalan:nodeset($childrenXML)" />
                            </xsl:call-template>
                        </td>
                    </tr>
                    <tr>
                        <td align="left" colspan="2">
                            <form id="sort" target="_self" action="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}{$HttpSession}" method="post">
                                <p>
                                    <xsl:choose>
                                        <xsl:when test="$childrenKinds='jparticle'">
                                            <select onChange="document.getElementById('sort').submit()" name="XSL.toc.sortBy.jparticle.SESSION" size="1">
                                                <option value="title">
                                                    <xsl:call-template name="checkSelection">
                                                        <xsl:with-param name="compVal1" select="$toc.sortBy.jparticle" />
                                                        <xsl:with-param name="compVal2" select="'title'" />
                                                    </xsl:call-template>
                                                    <xsl:value-of select="i18n:translate('metaData.sortbuttons.aftertitles')" />
                                                </option>
                                                <option value="size">
                                                    <xsl:call-template name="checkSelection">
                                                        <xsl:with-param name="compVal1" select="$toc.sortBy.jparticle" />
                                                        <xsl:with-param name="compVal2" select="'size'" />
                                                    </xsl:call-template>
                                                    <xsl:value-of select="i18n:translate('metaData.sortbuttons.afterpages')" />
                                                </option>
                                                <option value="position">
                                                    <xsl:call-template name="checkSelection">
                                                        <xsl:with-param name="compVal1" select="$toc.sortBy.jparticle" />
                                                        <xsl:with-param name="compVal2" select="'position'" />
                                                    </xsl:call-template>
                                                    <xsl:value-of select="i18n:translate('metaData.sortbuttons.position')" />
                                                </option>
                                            </select>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <select onChange="document.getElementById('sort').submit()" name="XSL.toc.sortBy.jpvolume.SESSION" size="1">
                                                <option value="title">
                                                    <xsl:call-template name="checkSelection">
                                                        <xsl:with-param name="compVal1" select="$toc.sortBy.jpvolume" />
                                                        <xsl:with-param name="compVal2" select="'title'" />
                                                    </xsl:call-template>
                                                    <xsl:value-of select="i18n:translate('metaData.sortbuttons.aftertitles')" />
                                                </option>
                                                <option value="position">
                                                    <xsl:call-template name="checkSelection">
                                                        <xsl:with-param name="compVal1" select="$toc.sortBy.jpvolume" />
                                                        <xsl:with-param name="compVal2" select="'position'" />
                                                    </xsl:call-template>
                                                    <xsl:value-of select="i18n:translate('metaData.sortbuttons.position')" />
                                                </option>
                                            </select>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </p>
                            </form>
                        </td>
                    </tr>
                </table>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="checkSelection">
        <xsl:param name="compVal1" />
        <xsl:param name="compVal2" />
        <xsl:if test="$compVal1=$compVal2">
            <xsl:attribute name="selected">
                <xsl:value-of select="selected" />
            </xsl:attribute>
        </xsl:if>
    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="printTOCNavi.chooseHitPage">
        <xsl:param name="children" />

        <xsl:variable name="numberOfChildren">
            <xsl:value-of select="count(xalan:nodeset($children)/mcr:results/mcr:hit)" />
        </xsl:variable>
        <xsl:variable name="numberOfHitPages">
            <xsl:value-of select="ceiling(number($numberOfChildren) div number($toc.pageSize))" />
        </xsl:variable>
        <xsl:if test="number($numberOfChildren)>number($toc.pageSize)">
            <xsl:value-of select="concat(', ',i18n:translate('metaData.resultpage'))" />
            <xsl:for-each select="./structure/children/child[number($numberOfHitPages)>=position()]">
                <xsl:variable name="jumpToPos">
                    <xsl:value-of select="(position()*number($toc.pageSize))-number($toc.pageSize)" />
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="number($jumpToPos)+1=number($toc.pos)">
                        <xsl:value-of select="concat(' [',position(),'] ')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <a href="{$WebApplicationBaseURL}receive/{/mycoreobject/@ID}{$HttpSession}?XSL.toc.pos.SESSION={$jumpToPos+1}">
                            <xsl:value-of select="concat(' ',position(),' ')" />
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:if>

    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="haveChildren">
        <xsl:param name="object" />
        <xsl:choose>
            <xsl:when test="xalan:nodeset($object)/mycoreobject/structure/children">
                <xsl:value-of select="'true'" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'false'" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="showAdminHead">
        <xsl:if test="acl:checkPermission(./@ID,'writedb') or acl:checkPermission(./@ID,'deletedb')">
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
                        <xsl:value-of select="i18n:translate('metaData.headlines.admin')" />
                    </td>
                    <td>
                        <br />
                    </td>
                </tr>
            </table>
        </xsl:if>
    </xsl:template>
    <!-- ===================================================================================================== -->
    <!--    <xsl:template name="get.toc.pos">
        <xsl:value-of select="'hallo'"/>
        <!-#-
        <xsl:if test="number($toc.pageSize)=1000000">
        <xsl:value-of select="1"/>
        </xsl:if>-#->
        </xsl:template>-->
    <!-- ===================================================================================================== -->
    
    <xsl:template name="jportalFormatISODate">
        <xsl:param name="date" />
        <xsl:param name="locale" select="$CurrentLang" />
        <xsl:value-of xmlns:mcrxml="xalan://org.mycore.common.xml.MCRJPortalXMLFunctions"
            select="mcrxml:formatISODate( string( $date ),string( $locale ) )" />
    </xsl:template>

    <xsl:template name="printMetaDates">
        <!-- prints a table row for a given nodeset -->
        <xsl:param name="volume-node" />
        <xsl:param name="nodes" />
        <xsl:param name="label" select="local-name($nodes[1])" />
        <xsl:if test="$nodes">
            <tr>
                <td valign="top" id="detailed-labels">
                    <xsl:value-of select="$label" />
                </td>
                <td class="metavalue">
                    <xsl:for-each select="$nodes">
                        <xsl:choose>
                            <xsl:when test="../@class='MCRMetaClassification'">
                                <xsl:call-template name="printClass">
                                    <xsl:with-param name="nodes" select="." />
                                    <xsl:with-param name="host" select="$objectHost" />
                                </xsl:call-template>
                                <xsl:call-template name="printClassInfo">
                                    <xsl:with-param name="nodes" select="." />
                                    <xsl:with-param name="host" select="$objectHost" />
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:when test="../@class='MCRMetaISO8601Date'">
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
                            </xsl:when>
                            <xsl:when test="../@class='MCRMetaLinkID'">
                                <xsl:call-template name="objectLink">
                                    <xsl:with-param name="obj_id" select="@xlink:href" />
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="$allowHTMLInArticles = 'true'">
                                        <xsl:call-template name="printI18N-allowHTML">
                                            <xsl:with-param name="nodes" select="." />
                                            <xsl:with-param name="host" select="$objectHost" />
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:call-template name="printI18N">
                                            <xsl:with-param name="nodes" select="." />
                                            <xsl:with-param name="host" select="$objectHost" />
                                        </xsl:call-template>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="position()!=last()">
                            <br />
                        </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="printMetaDate_typeSensitive">
        <xsl:param name="nodes" />
        <xsl:param name="label" />
        <xsl:param name="typeClassi" />
        <xsl:param name="mode" />
        <xsl:param name="layout" select="'structure'" />

        <xsl:if test="$nodes">
            <xsl:variable name="classXML">
                <xsl:copy-of select="xalan:nodeset(document(concat('classification:metadata:all:children:',$typeClassi)))" />
            </xsl:variable>
            <tr>
                <xsl:if test="$layout = 'structure'">
                    <td valign="top" id="detailed-labels">
                        <xsl:value-of select="$label" />
                    </td>
                </xsl:if>
                <td>
                    <xsl:choose>
                        <xsl:when test="$layout='structure'">
                            <xsl:attribute name="class"><xsl:value-of select="'leaf-additional'" />
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$layout='flat'">
                            <xsl:attribute name="id"> <xsl:value-of select="'leaf-additional'" />
                            </xsl:attribute>
                        </xsl:when>
                    </xsl:choose>

                    <!-- pass all categories, sorted by labels in $currentlang -->
                    <xsl:for-each select="xalan:nodeset($classXML)/mycoreclass/categories/category">
                        <xsl:sort select="./label[@xml:lang=$CurrentLang]/@text" order="ascending" />
                        <xsl:variable name="categID">
                            <xsl:value-of select="./@ID" />
                        </xsl:variable>
                        <!-- in mcrobject have been current categID found-->
                        <xsl:if test="$nodes[@type=$categID] | $nodes[@xlink:title=$categID]">
                            <!-- label of category -->
                            <i>
                                <xsl:value-of
                                    select="xalan:nodeset($classXML)/mycoreclass/categories/category[@ID=$categID]/label[@xml:lang=$CurrentLang]/@text" />
                                :
                            </i>
                            <xsl:choose>
                                <xsl:when test="$layout='structure'">
                                    <br />
                                    <ul>
                                        <xsl:for-each select="$nodes[@type=$categID] | $nodes[@xlink:title=$categID]">
                                            <xsl:sort order="ascending" select="./text()" />
                                            <li>
                                                <xsl:call-template name="printMetaDate_typeSensitive.printEntry">
                                                    <xsl:with-param name="modeIF" select="$mode"></xsl:with-param>
                                                </xsl:call-template>
                                            </li>
                                        </xsl:for-each>
                                    </ul>
                                </xsl:when>
                                <xsl:when test="$layout='flat'">
                                    <xsl:for-each select="$nodes[@type=$categID] | $nodes[@xlink:title=$categID]">
                                        <xsl:sort order="ascending" select="./text()" />
                                        <xsl:call-template name="printMetaDate_typeSensitive.printEntry">
                                            <xsl:with-param name="modeIF" select="$mode"></xsl:with-param>
                                        </xsl:call-template>
                                        <xsl:if test="position()!=last()">
                                            <xsl:text>; </xsl:text>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:when>
                            </xsl:choose>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:if test="$layout='flat'">
                        <br />
                        <br />
                    </xsl:if>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="printMetaDate_typeSensitive.printEntry">
        <xsl:param name="modeIF" />
        <xsl:choose>
            <xsl:when test="$modeIF='xlink'">
                <xsl:call-template name="objectLink">
                    <xsl:with-param name="obj_id" select="@xlink:href" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$modeIF='date'">
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
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$allowHTMLInArticles = 'true'">
                        <xsl:call-template name="printI18N-allowHTML">
                            <xsl:with-param name="nodes" select="./text()" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="printI18N">
                            <xsl:with-param name="nodes" select="./text()" />
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

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

        <xsl:choose>
            <!-- jpjournal or jpvolume or jparticle with own webcontext called -->
            <!-- webcontext is not empty AND $navigation.xml contains webcontext -->
            <xsl:when
                test="xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext/text()
                and ($loaded_navigation_xml//item[@href=xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext/text()])">
                <xsl:variable name="object_webContext">
                    <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext/text()" />
                </xsl:variable>
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
    <xsl:template name="listLinkedArts">
        <xsl:variable name="mcrSql" xmlns:encoder="xalan://java.net.URLEncoder">
            <xsl:value-of select="encoder:encode(concat('(objectType = jparticle) and (deletedFlag = false) and (link = ', /mycoreobject/@ID, ')'))"/>
        </xsl:variable>
        <xsl:variable name="linkedArt">
            <xsl:copy-of select="xalan:nodeset(document(concat('query:term=',$mcrSql)))" />
        </xsl:variable>
        <xsl:variable name="OID">
            <xsl:call-template name="typeOfObjectID">
                <xsl:with-param name="id" select="/mycoreobject/@ID" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:if test="xalan:nodeset($linkedArt)/mcr:results/mcr:hit">
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                <tr>
                    <td valign="top" id="detailed-labels">
                        <br></br>
                        <xsl:choose>
                            <xsl:when test="$OID='person'">
                                <xsl:value-of select="i18n:translate('metaData.person.linked.article')" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="i18n:translate('metaData.jpinst.linked')" />
                            </xsl:otherwise>
                        </xsl:choose>

                    </td>
                    <td>
                        <ul>
                            <xsl:for-each select="xalan:nodeset($linkedArt)/mcr:results/mcr:hit[number($maxLinkedArts)>position()-1]">
                                <xsl:variable name="art">
                                    <xsl:copy-of select="document(concat('mcrobject:',@id))" />
                                </xsl:variable>
                                <xsl:variable name="linkID">
                                    <xsl:call-template name="objectLink">
                                        <xsl:with-param name="obj_id" select="@id" />
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:if test="contains($linkID, 'jparticle')">
                                    <li>
                                        <xsl:call-template name="printHistoryRow">
                                            <xsl:with-param name="sortOrder" select="'ascending'" />
                                            <xsl:with-param name="printCurrent" select="'true'" />
                                            <xsl:with-param name="linkCurrent" select="'true'" />
                                            <xsl:with-param name="layout" select="'false'" />
                                            <xsl:with-param name="node" select="xalan:nodeset($art)" />
                                        </xsl:call-template>
                                    </li>
                                </xsl:if>
                                <div id="detailed-linkedart"></div>
                            </xsl:for-each>
                            <xsl:if test="count(xalan:nodeset($linkedArt)/mcr:results/mcr:hit)>$maxLinkedArts">
                                <li>
                                    <a xmlns:encoder="xalan://java.net.URLEncoder"
                                        href="{$ServletsBaseURL}MCRSearchServlet{$HttpSession}?query={$mcrSql}&amp;numPerPage=10">
                                        <xsl:value-of
                                            select="concat(' ',i18n:translate('metaData.person.linked.showAll'),' (',count(xalan:nodeset($linkedArt)/mcr:results/mcr:hit),') &gt;&gt;')" />
                                    </a>
                                </li>
                            </xsl:if>
                        </ul>
                    </td>
                </tr>
            </table>
        </xsl:if>
    </xsl:template>
    
    <!-- ===================================================================================================== -->
    <xsl:template name="listLinkedCals">
        <xsl:variable name="mcrSql" xmlns:encoder="xalan://java.net.URLEncoder">
            <xsl:value-of select="encoder:encode(concat('(objectType = jpjournal) and (deletedFlag = false) and (contentClassi1 = calendar) and (link = ', /mycoreobject/@ID, ')'))"/>
        </xsl:variable>
         <xsl:variable name="linkedCal">
            <xsl:copy-of select="xalan:nodeset(document(concat('query:term=',$mcrSql)))" />
        </xsl:variable>
        <xsl:variable name="OID">
            <xsl:call-template name="typeOfObjectID">
                <xsl:with-param name="id" select="/mycoreobject/@ID" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:if test="xalan:nodeset($linkedCal)/mcr:results/mcr:hit">
            <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                <tr>
                    <td valign="top" id="detailed-labels">
                        <br></br>
                        <xsl:choose>
                            <xsl:when test="$OID='person'">
                                <xsl:value-of select="i18n:translate('metaData.person.linked.calendar')" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="i18n:translate('metaData.jpinst.linked')" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                    <td>
                        <ul>
                            <xsl:for-each select="xalan:nodeset($linkedCal)/mcr:results/mcr:hit[number($maxLinkedCals)>position()-1]">
                                <xsl:variable name="cal">
                                    <xsl:copy-of select="document(concat('mcrobject:',@id))" />
                                </xsl:variable>
                                <xsl:variable name="linkID">
                                    <xsl:call-template name="objectLink">
                                        <xsl:with-param name="obj_id" select="@id" />
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:if test="contains($linkID, 'jpjournal')">
                                    <li>
                                        <xsl:call-template name="printHistoryRow">
                                            <xsl:with-param name="sortOrder" select="'ascending'" />
                                            <xsl:with-param name="printCurrent" select="'true'" />
                                            <xsl:with-param name="linkCurrent" select="'true'" />
                                            <xsl:with-param name="layout" select="'false'" />
                                            <xsl:with-param name="node" select="xalan:nodeset($cal)" />
                                        </xsl:call-template>
                                    </li>
                                </xsl:if>
                                <div id="detailed-linkedart"></div>
                            </xsl:for-each>
                            <xsl:if test="count(xalan:nodeset($linkedCal)/mcr:results/mcr:hit)>$maxLinkedCals">
                                <li>
                                    <a xmlns:encoder="xalan://java.net.URLEncoder"
                                        href="{$ServletsBaseURL}MCRSearchServlet{$HttpSession}?query={$mcrSql}&amp;numPerPage=10">
                                        <xsl:value-of
                                            select="concat(' ',i18n:translate('metaData.person.linked.showAll'),' (',count(xalan:nodeset($linkedCal)/mcr:results/mcr:hit),') &gt;&gt;')" />
                                    </a>
                                </li>
                            </xsl:if>
                        </ul>
                    </td>
                </tr>
            </table>
        </xsl:if>
    </xsl:template>
    
    
    
    <!-- ===================================================================================================== -->
    <xsl:template name="browseCtrlJP">

        <xsl:if test="string-length($previousObject)>0">

            <xsl:variable name="hostParam">
                <xsl:if test="$previousObjectHost != 'local'">
                    <xsl:value-of select="concat('?host=',$previousObjectHost)" />
                </xsl:if>
            </xsl:variable>
            <td id="detailed-browse">
                <a href="{$WebApplicationBaseURL}receive/{$previousObject}{$HttpSession}{$hostParam}" alt="{i18n:translate('metaData.resultlist.prev')}"
                    title="{i18n:translate('metaData.resultlist.prev')}">
                    <img src="{$WebApplicationBaseURL}left.gif" />
                </a>
            </td>

        </xsl:if>

        <xsl:if test="string-length($numPerPage)>0">
            <td>
                <div id="switch-notcurrent">
                    <a
                        href="{$ServletsBaseURL}MCRSearchServlet{$HttpSession}?mode=results&amp;id={$resultListEditorID}&amp;page={$page}&amp;numPerPage={$numPerPage}">
                        <xsl:value-of select="i18n:translate('metaData.resultlist')" />
                    </a>
                </div>
            </td>

        </xsl:if>

        <xsl:if test="string-length($nextObject)>0">
            <xsl:variable name="hostParam">
                <xsl:if test="$nextObjectHost != 'local'">
                    <xsl:value-of select="concat('?host=',$nextObjectHost)" />
                </xsl:if>
            </xsl:variable>
            <td id="detailed-browse">
                <a href="{$WebApplicationBaseURL}receive/{$nextObject}{$HttpSession}{$hostParam}" alt="{i18n:translate('metaData.resultlist.next')}"
                    title="{i18n:translate('metaData.resultlist.next')}">
                    <img src="{$WebApplicationBaseURL}right.gif" />
                </a>
            </td>

        </xsl:if>

    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="get.thumbnailSupport">
        <xsl:choose>
            <xsl:when test="/mycoreobject and $view.objectmetadata='false'">
                <xsl:value-of select="'true'" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'false'" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="get.zfbbSupport">
        <xsl:choose>
            <xsl:when test="$template='template_jstzfbb'">
                <xsl:value-of select="'true'" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'false'" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- ===================================================================================================== -->
    <xsl:template name="getAuthorList">
        <xsl:param name="objectXML" />
        <xsl:param name="listLength" />

        <xsl:for-each select="xalan:nodeset($objectXML)/mycoreobject/metadata/participants/participant[number($listLength)+1>position()]">
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

    <!-- ================================================================================================================= -->

    <xsl:template name="lineSpace">
        <div style="height:0.7em;" />
    </xsl:template>

    <!-- ================================================================================================================= -->

    <xsl:template name="SwitchToXMLview">
        <xsl:if test="$view.objectmetadata='false'">
            <xsl:variable name="newurl">
                <xsl:call-template name="UrlSetParam">
                    <xsl:with-param name="url" select="$RequestURL" />
                    <xsl:with-param name="par" select="'XSL.Style'" />
                    <xsl:with-param name="value" select="'xmlexport'" />
                </xsl:call-template>
            </xsl:variable>
            <td id="detailed-xmlbutton">
                <a href="{$newurl}" alt="{i18n:translate('metaData.xmlView')}" title="{i18n:translate('metaData.xmlView')}">
                    <img src="{$WebApplicationBaseURL}images/xml.png" />
                </a>
            </td>
        </xsl:if>
    </xsl:template>

    <!-- ================================================================================================================= -->

    <xsl:template name="linkFile">
        <xsl:variable name="linkExist">
            <xsl:value-of select="/mycoreobject/metadata/ifsLinks/ifsLink[text() = $MCR.Module-iview.markedImageURL]" />
        </xsl:variable>
        <xsl:if test="acl:checkPermission(./@ID,'writedb') and $MCR.Module-iview.markedImageURL != '' and not($linkExist)">
            <xsl:variable name="url">
                <xsl:value-of
                    select="concat($ServletsBaseURL,'MCRJPortalLinkFileServlet?jportalLinkFileServlet.mode=setLink&amp;jportalLinkFileServlet.from=',./@ID)" />
            </xsl:variable>
            <td width="30px" />
            <td id="detailed-xmlbutton">
                <a href="{$url}" alt="{i18n:translate('metaData.xmlView')}" title="Bild {$MCR.Module-iview.markedImageURL} mit diesem Dokument verlinken">
                    <img src="{$WebApplicationBaseURL}/images/paperClip.jpeg" />
                </a>
            </td>
        </xsl:if>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="mcr_directory.saveDerivate">
        <xsl:if test="$CurrentUser!='gast'">
            <xsl:variable name="saveAddress">
                <xsl:value-of select="concat($ServletsBaseURL,'MCRZipServlet?id=',./path/text())" />
            </xsl:variable>
            <table id="metaData" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr align="right">
                    <td align="right">
                        <a href="{$saveAddress}">
                            <img src="{$WebApplicationBaseURL}images/save.gif" />
                        </a>
                    </td>
                </tr>
            </table>
        </xsl:if>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="get.journalXML">
        <xsl:if test="$journalID != ''">
            <xsl:copy-of select="document(concat('mcrobject:',$journalID))" />
        </xsl:if>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="get.journalID">
        <!-- 
            <xsl:if test="xalan:nodeset($journalXML)/mycoreobject">
            <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/@ID" />
            </xsl:if> -->
        <xsl:value-of select="document('jportal_getJournalID:noXPath')/dummyRoot/hidden/@default" />
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="get.allowHTMLInArticles">
        <xsl:choose>
            <xsl:when
                test="xalan:nodeset($journalXML)/mycoreobject/metadata and xalan:nodeset($journalXML)/mycoreobject/metadata/hidden_genhiddenfields1/hidden_genhiddenfield1/text() = 'allowHTML'">
                <xsl:value-of select="'true'" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'false'" />
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="printI18N-allowHTML">
        <xsl:param name="nodes" />
        <xsl:param name="next" />
        <xsl:variable name="selectPresentLang">
            <xsl:call-template name="selectPresentLang">
                <xsl:with-param name="nodes" select="$nodes" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:for-each select="$nodes[lang($selectPresentLang)]">
            <xsl:if test="position() != 1">
                <xsl:value-of select="$next" disable-output-escaping="yes" />
            </xsl:if>
            <xsl:call-template name="lf2br-allowHTML">
                <xsl:with-param name="string" select="." />
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="lf2br-allowHTML">
        <xsl:param name="string" />
        <xsl:choose>
            <xsl:when test="contains($string,'&#xA;')">
                <xsl:value-of select="substring-before($string,'&#xA;')" />
                <!-- replace line break character by xhtml tag -->
                <br />
                <xsl:call-template name="lf2br">
                    <xsl:with-param name="string" select="substring-after($string,'&#xA;')" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of disable-output-escaping="yes" select="$string" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="get.readAccessForDerivates">
        <xsl:param name="jID" />
        <xsl:if test="$jID != ''">
            <xsl:value-of select="acl:checkPermission($jID,'read-derivates')" />
        </xsl:if>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="get.systemData">
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
    </xsl:template>

</xsl:stylesheet>