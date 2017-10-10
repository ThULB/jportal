<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="mycoreobject">
        <xsl:comment>
            Start mycoreobject (mycoreobject-marc.xsl)
        </xsl:comment>
        <xsl:copy-of select="document(concat('marcxml:', @ID))"/>
    </xsl:template>

</xsl:stylesheet>
