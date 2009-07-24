<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.2 $Date:  -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation">

    <!--  ========================================================================================== -->

    <xsl:template name="getSelectBox">
        <select name="mode" size="1" class="button">
            <option value="prefix">
                <xsl:if test="$mode = 'prefix'">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="i18n:translate('indexpage.contains')" />
            </option>
            <option value="equals">
                <xsl:if test="$mode = 'equals'">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="'='" />
            </option>
        </select>
    </xsl:template>

    <!--  ========================================================================================== -->

</xsl:stylesheet>
