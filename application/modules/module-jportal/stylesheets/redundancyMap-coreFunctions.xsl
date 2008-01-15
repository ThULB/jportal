<?xml version="1.0" encoding="ISO-8859-1" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager">
    <xsl:param name="redunMode" select="'open'" />
    <xsl:variable name="RedunMap" select="'redundancy.xml'" />
    <xsl:variable name="filteredRedunMap">
        <xsl:call-template name="get.filteredRedunMap" />
    </xsl:variable>
    <!-- ===================================================================================== -->

    <xsl:template name="get.filteredRedunMap">
        <xsl:choose>
            <xsl:when test="$redunMode='open'">
                <xsl:copy-of
                    select="document(concat($WebApplicationBaseURL,$RedunMap,'?XSL.Style=xml'))/redundancyMap/redundancyID[not(@status) or @status='open']" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of
                    select="document(concat($WebApplicationBaseURL,$RedunMap,'?XSL.Style=xml'))/redundancyMap/redundancyID[@status='accepted' or @status='denied']" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="get.isDoublet">
        <xsl:param name="id" />
        <xsl:choose>
            <xsl:when test="xalan:nodeset($filteredRedunMap)//redundancyID[@status='accepted' and text()=$id]">
                <xsl:value-of select="'true'" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'false'" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ===================================================================================== -->

</xsl:stylesheet>