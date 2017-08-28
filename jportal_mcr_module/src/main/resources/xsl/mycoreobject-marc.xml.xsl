<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
                xmlns:marc="http://www.loc.gov/MARC21/slim" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="mcr xalan i18n acl marc">

    <xsl:param name="WebApplicationBaseURL"/>
    <xsl:param name="ThumbnailBaseURL" select="concat($ServletsBaseURL,'MCRDFGThumbnail/')"/>
    <xsl:param name="ImageBaseURL" select="concat($ServletsBaseURL,'MCRDFGServlet/')"/>

    <xsl:template match="mycoreobject">
        <xsl:comment>
            Start mycoreobject (mycoreobject-marc.xsl)
        </xsl:comment>
        <marc:record xmlns:marc="http://www.loc.gov/MARC21/slim" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd">
            <marc:leader></marc:leader>
            <marc:controlfield tag="001">12149120</marc:controlfield>
            <marc:datafield tag="906" ind1=" " ind2=" ">
                <marc:subfield code="a">0</marc:subfield>
            </marc:datafield>
        </marc:record>
    </xsl:template>

</xsl:stylesheet>