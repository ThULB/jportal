<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/">

    <xsl:param name="spreading" select="2" />

    <xsl:variable name="today" select="2009" />
    <xsl:variable name="barHeight" select="20" />
    <xsl:variable name="smallLineSpace" select="25" />
    <xsl:variable name="fatLineSpace" select="100" />

    <!-- =================================================================================================== -->

    <xsl:template match="journalList[@mode='chronological'] | journallist[@mode='chronological']">

        <xsl:variable name="journalIDs">
            <xsl:call-template name="get.allJournalIDs" />
        </xsl:variable>
        <!-- all journals with begin date -->
        <xsl:variable name="journalXMLs">
            <xsl:call-template name="get.journalXMLs.timeBar">
                <xsl:with-param name="journalIDsIF" select="$journalIDs" />
            </xsl:call-template>
        </xsl:variable>
        <!-- get earliest year -->
        <xsl:variable name="journalList.earliestYear">
            <xsl:call-template name="get.earliestYear">
                <xsl:with-param name="journalXMLsIF" select="$journalXMLs" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="journalList.timeTotal">
            <xsl:value-of select="($today - $journalList.earliestYear)" />
        </xsl:variable>

        <p>
            <xsl:call-template name="journalList.chooseZoom" />
            <br />
            <br />
        </p>

        <div style="position:relative;height:100%;">
            <!-- legend bar -->
            <div
                style="position:absolute;top:0px;left:0px;width:{$journalList.timeTotal * $spreading}px; height:{$barHeight div 2}px;border:1px solid #000000;background-color:#0F0FFF;" />
            <xsl:call-template name="createLabels">
                <xsl:with-param name="journalList.earliestYearIF" select="$journalList.earliestYear" />
                <xsl:with-param name="today" select="$today" />
                <xsl:with-param name="journalsTotal" select="count(xalan:nodeset($journalIDs)/mcr:results/mcr:hit)" />
            </xsl:call-template>

            <!-- journals -->
            <xsl:for-each select="xalan:nodeset($journalXMLs)/journalXMLs/mycoreobject">
                <xsl:sort select="metadata/dates/date[@type = 'published_from']/text()" data-type="number" order="ascending" />
                <xsl:variable name="start">
                    <xsl:value-of select="(metadata/dates/date[@type = 'published_from']/text() - $journalList.earliestYear) * $spreading" />
                </xsl:variable>
                <xsl:variable name="width">
                    <xsl:choose>
                        <xsl:when test="metadata/dates/date[@type = 'published_until']/text() != ''">
                            <xsl:value-of
                                select="(metadata/dates/date[@type = 'published_until']/text() - metadata/dates/date[@type = 'published_from']/text()) * $spreading" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="($today - metadata/dates/date[@type = 'published_from']/text()) * $spreading" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="top">
                    <xsl:value-of select="(position()* $barHeight div 13) * $barHeight" />
                </xsl:variable>
                <!-- draw -->
                <div style="position:absolute;top:{$top}px;left:{$start}px;width:{$width}px;height:20px;border:2px solid #FF0000;" />
                <div style="position:absolute;top:{$top+3}px;left:{$start+3}px;width:100%;">
                    <!-- label -->
                    <a style="color:#777777;" href="{$WebApplicationBaseURL}{substring-after(metadata/hidden_websitecontexts/hidden_websitecontext/text(),'/')}">
                        <xsl:value-of
                            select="concat(substring(metadata/maintitles/maintitle/text(),1,30),'... (',metadata/dates/date[@type = 'published_from']/text(),' - ',metadata/dates/date[@type = 'published_until']/text(),')')" />
                    </a>
                </div>
            </xsl:for-each>
        </div>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.preselect">
        <xsl:param name="value" />
        <xsl:if test="$value = $spreading">
            <xsl:attribute name="selected"><xsl:value-of select="selected" />
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="createLabels">
        <xsl:param name="journalList.earliestYearIF" />
        <xsl:param name="journalList.earliestYearIncremented" select="$journalList.earliestYearIF" />
        <xsl:param name="today" />
        <xsl:param name="journalsTotal" />

        <xsl:if test="$journalList.earliestYearIncremented &lt;= $today">
            <!-- content -->
            <xsl:variable name="leftPos" select="($journalList.earliestYearIncremented - $journalList.earliestYearIF) * $spreading" />
            <xsl:variable name="height" select="(($journalsTotal * $barHeight div 13) * $barHeight) + ($barHeight + $barHeight div 13)" />
            <!-- draw -->
            <xsl:if test="($journalList.earliestYearIncremented div $smallLineSpace) = round($journalList.earliestYearIncremented div $smallLineSpace)">
                <div style="position:absolute;top:0px;left:{$leftPos}px;width:1px; height:{$height}px;border-left:1px solid #DDDDDD;" />
                <div style="position:absolute;top:-15px;left:{$leftPos}px;">
                    <xsl:value-of select="$journalList.earliestYearIncremented" />
                </div>
            </xsl:if>
            <xsl:if test="($journalList.earliestYearIncremented div $fatLineSpace) = round($journalList.earliestYearIncremented div $fatLineSpace)">
                <div style="position:absolute;top:0px;left:{$leftPos}px;width:3px; height:{$height}px;border-left:3px solid #DDDDDD;" />
                <div style="position:absolute;top:-15px;left:{$leftPos}px;">
                    <xsl:value-of select="$journalList.earliestYearIncremented" />
                </div>
            </xsl:if>
        </xsl:if>

        <xsl:if test="$journalList.earliestYearIncremented &lt;= $today">
            <xsl:call-template name="createLabels">
                <xsl:with-param name="journalList.earliestYearIF" select="$journalList.earliestYearIF" />
                <xsl:with-param name="journalList.earliestYearIncremented" select="$journalList.earliestYearIncremented + 1" />
                <xsl:with-param name="today" select="$today" />
                <xsl:with-param name="journalsTotal" select="$journalsTotal" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.journalXMLs.timeBar">
        <xsl:param name="journalIDsIF" />
        <journalXMLs>
            <xsl:for-each select="xalan:nodeset($journalIDsIF)/mcr:results/mcr:hit">
                <xsl:variable name="mcrobj">
                    <xsl:copy-of select="document(concat('mcrobject:',@id))" />
                </xsl:variable>
                <xsl:if test="xalan:nodeset($mcrobj)/mycoreobject/metadata/dates/date[@type = 'published_from']/text() != ''">
                    <xsl:copy-of select="$mcrobj" />
                </xsl:if>
            </xsl:for-each>
        </journalXMLs>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.earliestYear">
        <xsl:param name="journalXMLsIF" />
        <xsl:for-each select="xalan:nodeset($journalXMLsIF)/journalXMLs/mycoreobject">
            <xsl:sort select="metadata/dates/date[@type = 'published_from']/text()" data-type="number" order="ascending" />
            <xsl:if test="position() = 1">
                <xsl:copy-of select="metadata/dates/date[@type = 'published_from']/text()" />
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.chooseZoom">
        <form id="journalList.zoom" action="{$RequestURL}" method="post">
            Wählen Sie die Vergrößerungsfaktor :
            <select name="XSL.spreading.SESSION" size="1" onChange="document.getElementById('journalList.zoom').submit()">
                <option value="1">
                    <xsl:call-template name="journalList.preselect">
                        <xsl:with-param name="value" select="1" />
                    </xsl:call-template>
                    nicht vergrößern
                </option>
                <option value="2">
                    <xsl:call-template name="journalList.preselect">
                        <xsl:with-param name="value" select="2" />
                    </xsl:call-template>
                    2 fach
                </option>
                <option value="3">
                    <xsl:call-template name="journalList.preselect">
                        <xsl:with-param name="value" select="3" />
                    </xsl:call-template>
                    3 fach
                </option>
                <option value="4">
                    <xsl:call-template name="journalList.preselect">
                        <xsl:with-param name="value" select="4" />
                    </xsl:call-template>
                    4 fach
                </option>
                <option value="5">
                    <xsl:call-template name="journalList.preselect">
                        <xsl:with-param name="value" select="5" />
                    </xsl:call-template>
                    5 fach
                </option>
                <option value="6">
                    <xsl:call-template name="journalList.preselect">
                        <xsl:with-param name="value" select="6" />
                    </xsl:call-template>
                    6 fach
                </option>
                <option value="7">
                    <xsl:call-template name="journalList.preselect">
                        <xsl:with-param name="value" select="7" />
                    </xsl:call-template>
                    7 fach
                </option>
                <option value="8">
                    <xsl:call-template name="journalList.preselect">
                        <xsl:with-param name="value" select="8" />
                    </xsl:call-template>
                    8 fach
                </option>
                <option value="9">
                    <xsl:call-template name="journalList.preselect">
                        <xsl:with-param name="value" select="9" />
                    </xsl:call-template>
                    9 fach
                </option>
                <option value="10">
                    <xsl:call-template name="journalList.preselect">
                        <xsl:with-param name="value" select="10" />
                    </xsl:call-template>
                    10 fach
                </option>
            </select>
        </form>
    </xsl:template>

    <!-- =================================================================================================== -->

</xsl:stylesheet>