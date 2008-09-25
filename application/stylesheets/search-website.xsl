<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" 
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xlink xalan">
    <!-- =================================================================================================== -->
    <xsl:template match="search-website">
        <xsl:variable name="docRoot" select="$loaded_navigation_xml/@dir" />
        <xsl:variable name="docRoot_clean" select="substring-after($docRoot,'/')" />
        <xsl:variable name="site" select="concat('site:',$WebApplicationBaseURL,$docRoot_clean)" />
        <xsl:variable name="files" select="'xml'" />
        <xsl:variable name="filesFilter" select="concat('filetype:',$files)" />
        <xsl:variable name="filter" select="concat(' ',$site,' ',$filesFilter)" />
        <table>
            <tr>
                <td>
                    <script type="text/javascript">
                        function append () { document.getElementById("searchQuery").value = document.getElementById("searchQuery").value +
                        <xsl:value-of select="concat('&#34;',$filter,'&#34;')" />
                        ; }
                    </script>
                    <form method="get" action="http://www.google.de/search" onsubmit="append()">
                        <input type="search" id="searchQuery" name="q" size="60" results="10" />
                        <input type="submit" class="button" value="{i18n:translate('buttons.startSearch')}" />
                    </form>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!-- =================================================================================================== -->
</xsl:stylesheet>