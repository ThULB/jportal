<?xml version="1.0" encoding="ISO-8859-1"?>
  <!-- ===================================================================================================== -->
  <!-- This stylesheet contains all templates to show an mycore object in the detail view -->
  <!-- ===================================================================================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mcr="http://www.mycore.org/"
    xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xlink mcr i18n acl xalan"
    xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities">


    <xsl:param name="MCR.Module-iview.SupportedContentTypes" />
    <xsl:param name="MCR.Module-iview.markedImageURL" />

    <!-- ===================================================================================================== -->
    <!-- TODO -->
    <!-- ===================================================================================================== -->
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

</xsl:stylesheet>