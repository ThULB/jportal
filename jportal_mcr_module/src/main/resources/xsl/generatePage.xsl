<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="xalan">
    <xsl:output method="html" indent="yes" encoding="UTF-8" media-type="text/html" xalan:indent-amount="2" doctype-public="-//W3C//DTD HTML 4.01//EN"
        doctype-system="http://www.w3.org/TR/html4/strict.dtd" />

    <xsl:include href="xslInclude:main" />

    <xsl:template name="generatePage">
        <xsl:call-template name="renderLayout" />
    </xsl:template>

</xsl:stylesheet>
