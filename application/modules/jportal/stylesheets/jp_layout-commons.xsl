<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mcr="http://www.mycore.org/"
    xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xlink mcr i18n acl xalan"
    xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities">

    <!-- ================================================================================== -->

    <xsl:template name="jp.layout.getHTMLHeader">
        <title>
            <xsl:call-template name="HTMLPageTitle" />
        </title>
        <meta content="Zeitschriften-Portal" lang="de" name="description" />
        <meta content="Journal-Portal" lang="en" name="description" />
        <meta content="Zeitschriften,historisch,aktuell,Paper,Forschung,UrMEL,ThULB, FSU Jena,Langzeitarchivierung,Andreas Trappe" lang="de" name="keywords" />
        <meta content="Journals,EJournals,historical,currently,paper,research,UrMEL,ThULB, FSU Jena,long term preservation,Andreas Trappe" lang="en"
            name="keywords" />
        <meta content="MyCoRe" lang="de" name="generator" />
        <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_general.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_navigation.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}templates/master/{$template}/CSS/style_content.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}templates/master/template_wcms/CSS/style_admin.css" rel="stylesheet" type="text/css" />
        <link href="{$WebApplicationBaseURL}/common.css" rel="stylesheet" type="text/css" />
        <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/menu.js" type="text/javascript" />
        <script language="JavaScript" src="{$WebApplicationBaseURL}templates/master/template_wcms/JAVASCRIPT/WCMSJavaScript.js" type="text/javascript" />
        <xsl:call-template name="module-broadcasting.getHeader" />
    </xsl:template>

    <!-- ================================================================================== -->

    <xsl:template name="jp.layout.getHTMLContent">
        <xsl:call-template name="print.writeProtectionMessage" />
        <xsl:choose>
            <xsl:when test="$readAccess='true'">
                <div class="headline">
                    <xsl:copy-of select="$PageTitle" />
                </div>
                <xsl:call-template name="getFastWCMS" />
                <xsl:apply-templates />
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="printNotLoggedIn" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ================================================================================== -->

</xsl:stylesheet>