<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:layoutUtils="xalan:///org.mycore.frontend.MCRLayoutUtilities" exclude-result-prefixes="xalan">
    <xsl:output method="html" indent="yes" encoding="UTF-8" media-type="text/html" xalan:indent-amount="2" doctype-public="-//W3C//DTD HTML 4.01//EN"
        doctype-system="http://www.w3.org/TR/html4/strict.dtd" />

    <xsl:include href="jp-layout-main.xsl" />
    <xsl:include href="pagetitle.xsl" />
    <xsl:include href="jp_navigation.xsl" />
    <xsl:include href="footer.xsl" />
    <xsl:include href="navigation.xsl" />

    <!-- includes the stylesheets which are set in the mycore.properties file -->
    <xsl:include href="xslInclude:modules" />
    <xsl:include href="xslInclude:templates" />

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

    <xsl:variable name="template">
      <xsl:call-template name="nameOfTemplate" />
    </xsl:variable>

    <!-- TODO: remove this -->
    <xsl:variable name="wcms.useTargets" select="'no'" />

    <!-- =================================================================================================== -->
    <xsl:template name="generatePage">
        <!-- call the appropriate template -->
        <xsl:call-template name="renderLayout" />
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
