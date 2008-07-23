<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/">
    
    <xsl:include href="journalList-timeBar.xsl" />
    
    <!-- =================================================================================================== -->

    <xsl:template match="journalList[@mode='alphabetical'] | journallist[@mode='alphabetical']">

        <xsl:variable name="journalIDs">
            <xsl:call-template name="get.allJournalIDs" />
        </xsl:variable>

        <xsl:call-template name="journalList.doLayout">
            <xsl:with-param name="journalIds" select="$journalIDs" />
            <xsl:with-param name="mode" select="'shortcut'" />
        </xsl:call-template>
        <xsl:call-template name="journalList.seperator" />
        <br />
        <br />

        <xsl:call-template name="journalList.doLayout">
            <xsl:with-param name="journalIds" select="$journalIDs" />
            <xsl:with-param name="mode" select="'fully'" />
        </xsl:call-template>

    </xsl:template>


    <!-- =================================================================================================== -->

    <xsl:template name="journalList.doLayout">
        <xsl:param name="journalIds" />
        <xsl:param name="mode" />
        <xsl:for-each select="xalan:nodeset($journalIds)/mcr:results/mcr:hit">
            <xsl:variable name="precTitle">
                <xsl:variable name="pos" select="position()" />
                <xsl:value-of select="substring(xalan:nodeset($journalIds)/mcr:results/mcr:hit[position()=number($pos - 1)]/mcr:sortData/mcr:field/text(),1,1)" />
            </xsl:variable>
            <xsl:variable name="title">
                <xsl:value-of select="substring(./mcr:sortData/mcr:field/text(),1,1)" />
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$mode = 'shortcut'">
                    <xsl:call-template name="journalList.doLayout.shortcuts">
                        <xsl:with-param name="prefixLabel" select="$precTitle = $title" />
                        <xsl:with-param name="titleIF" select="$title" />
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="journalList.doLayout.journals">
                        <xsl:with-param name="prefixLabel" select="$precTitle = $title" />
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.doLayout.journals">
        <xsl:param name="prefixLabel" />
        <xsl:if test="$prefixLabel = true">
            <xsl:variable name="label" select="substring(./mcr:sortData/mcr:field/text(),1,1)" />
            <xsl:variable name="labelUpperCase">
                <xsl:call-template name="journalList.upperCase">
                    <xsl:with-param name="char" select="$label" />
                </xsl:call-template>
            </xsl:variable>
            <br />
            <b>
                <a name="{$labelUpperCase}">
                    <b>
                        <xsl:value-of select="$labelUpperCase" />
                    </b>
                </a>
            </b>
            <br />
            <br />
        </xsl:if>
        <xsl:apply-templates select=".">
            <xsl:with-param name="mcrobj" select="document(concat('mcrobject:',@id))" />
            <xsl:with-param name="mcrobjlink">
                <xsl:call-template name="objectLink">
                    <xsl:with-param name="obj_id" select="@id" />
                </xsl:call-template>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.doLayout.shortcuts">
        <xsl:param name="prefixLabel" />
        <xsl:param name="titleIF" />
        <xsl:if test="$prefixLabel = true">
            <xsl:call-template name="journalList.seperator" />
            <xsl:variable name="char">
                <xsl:call-template name="journalList.upperCase">
                    <xsl:with-param name="char" select="$titleIF" />
                </xsl:call-template>
            </xsl:variable>
            <a href="#{$char}">
                <b>
                    <xsl:value-of select="$char" />
                </b>
            </a>
        </xsl:if>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.seperator">
        <xsl:value-of select="'&#160;&#160;&#160;|&#160;&#160;&#160;'" />
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.allJournalIDs">
        <xsl:variable name="term">
            <xsl:value-of select="encoder:encode('(objectType = jpjournal)')" />
        </xsl:variable>
        <xsl:variable name="queryURI">
            <xsl:value-of select="concat('query:term=',$term,'&amp;sortby=maintitles&amp;order=ascending&amp;maxResults=0')" />
        </xsl:variable>
        <xsl:copy-of select="document($queryURI)" />
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.upperCase">
        <xsl:param name="char" />
        <xsl:choose>
            <xsl:when test="$char='a' or $char='A'">
                <xsl:value-of select="'A'" />
            </xsl:when>
            <xsl:when test="$char='b' or $char='B'">
                <xsl:value-of select="'B'" />
            </xsl:when>
            <xsl:when test="$char='c' or $char='C'">
                <xsl:value-of select="'C'" />
            </xsl:when>
            <xsl:when test="$char='D' or $char='d'">
                <xsl:value-of select="'D'" />
            </xsl:when>
            <xsl:when test="$char='e' or $char='E'">
                <xsl:value-of select="'E'" />
            </xsl:when>
            <xsl:when test="$char='f' or $char='F'">
                <xsl:value-of select="'F'" />
            </xsl:when>
            <xsl:when test="$char='g' or $char='G'">
                <xsl:value-of select="'G'" />
            </xsl:when>
            <xsl:when test="$char='h' or $char='H'">
                <xsl:value-of select="'H'" />
            </xsl:when>
            <xsl:when test="$char='i' or $char='I'">
                <xsl:value-of select="'I'" />
            </xsl:when>
            <xsl:when test="$char='j' or $char='J'">
                <xsl:value-of select="'J'" />
            </xsl:when>
            <xsl:when test="$char='k' or $char='K'">
                <xsl:value-of select="'K'" />
            </xsl:when>
            <xsl:when test="$char='l' or $char='L'">
                <xsl:value-of select="'L'" />
            </xsl:when>
            <xsl:when test="$char='m' or $char='M'">
                <xsl:value-of select="'M'" />
            </xsl:when>
            <xsl:when test="$char='n' or $char='N'">
                <xsl:value-of select="'N'" />
            </xsl:when>
            <xsl:when test="$char='o' or $char='O'">
                <xsl:value-of select="'O'" />
            </xsl:when>
            <xsl:when test="$char='p' or $char='P'">
                <xsl:value-of select="'P'" />
            </xsl:when>
            <xsl:when test="$char='q' or $char='Q'">
                <xsl:value-of select="'Q'" />
            </xsl:when>
            <xsl:when test="$char='r' or $char='R'">
                <xsl:value-of select="'R'" />
            </xsl:when>
            <xsl:when test="$char='s' or $char='S'">
                <xsl:value-of select="'S'" />
            </xsl:when>
            <xsl:when test="$char='t' or $char='T'">
                <xsl:value-of select="'T'" />
            </xsl:when>
            <xsl:when test="$char='u' or $char='U'">
                <xsl:value-of select="'U'" />
            </xsl:when>
            <xsl:when test="$char='v' or $char='V'">
                <xsl:value-of select="'V'" />
            </xsl:when>
            <xsl:when test="$char='w' or $char='W'">
                <xsl:value-of select="'W'" />
            </xsl:when>
            <xsl:when test="$char='x' or $char='X'">
                <xsl:value-of select="'X'" />
            </xsl:when>
            <xsl:when test="$char='y' or $char='Y'">
                <xsl:value-of select="'Y'" />
            </xsl:when>
            <xsl:when test="$char='z' or $char='Z'">
                <xsl:value-of select="'Z'" />
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- =================================================================================================== -->

</xsl:stylesheet>