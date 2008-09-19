<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:layoutUtils="xalan:///org.mycore.frontend.MCRLayoutUtilities" exclude-result-prefixes="xalan">
    <xsl:output method="html" indent="yes" encoding="UTF-8" media-type="text/html" xalan:indent-amount="2" doctype-public="-//W3C//DTD HTML 4.01//EN"
        doctype-system="http://www.w3.org/TR/html4/strict.dtd" />

    <!-- ================== get some wcms required global variables ===================================== -->
    <!-- location of navigation base -->
    <xsl:param name="navi" />
    <xsl:variable name="navigationBase">
        <xsl:call-template name="get.naviBase" />
    </xsl:variable>
    <!-- load navigation.xml -->
    <xsl:variable name="loaded_navigation_xml" select="document($navigationBase)/navigation" />

    <!-- base image path -->
    <xsl:variable name="ImageBaseURL" select="concat($WebApplicationBaseURL,'images/') " />
    <!-- main title configured in mycore.properties -->
    <xsl:param name="MCR.NameOfProject" />
    <xsl:variable name="MainTitle">
        <xsl:value-of select="$MCR.NameOfProject" />
    </xsl:variable>

    <xsl:param name="href" />

    <xsl:variable name="browserAddress">
        <xsl:call-template name="getBrowserAddress" />
    </xsl:variable>

    <!-- look for appropriate template entry and assign -> $template -->
    <xsl:param name="template">
        <xsl:call-template name="getTemplate">
            <xsl:with-param name="browserAddress" select="$browserAddress" />
            <xsl:with-param name="navigationBase" select="$navigationBase" />
        </xsl:call-template>
    </xsl:param>

    <!-- set useTarget to 'yes' if you want the target attribute to appear in links
        the wcms controls. This would break HTML 4.01 strict compatiblity but allows
        the browser to open new windows when clicking on certain links.
        To keep standard compliance it's default turned of, as it may annoy some
        people, too.
    -->
    <xsl:variable name="wcms.useTargets" select="'yes'" />

    <xsl:variable name="whiteList">
        <xsl:call-template name="get.whiteList" />
    </xsl:variable>
    <xsl:variable name="readAccess">
        <xsl:choose>
            <xsl:when test="starts-with($RequestURL, $whiteList)">
                <xsl:value-of select="'true'" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="layoutUtils:readAccess($browserAddress)" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:include href="chooseTemplate.xsl" />
    <xsl:include href="pagetitle.xsl" />
    <xsl:include href="navi_main.xsl" />
    <xsl:include href="footer.xsl" />
    <xsl:include href="navigation.xsl" />
    <xsl:include href="wcms_common.xsl" />
    <xsl:include href="modules-includes.xsl" />
    <xsl:include href="jp_extensions.xsl" />

    <!-- =================================================================================================== -->
    <xsl:template name="generatePage">
        <!-- call the appropriate template -->
        <xsl:call-template name="chooseTemplate" />
    </xsl:template>
    <!-- ================================================================================= -->
    <xsl:template name="get.naviBase">
        <xsl:choose>
            <xsl:when test="$navi=''">
                <xsl:value-of select="'webapp:config/navigation.xml'" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat('File:',$navi)" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- ================================================================================= -->
</xsl:stylesheet>