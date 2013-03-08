<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/"
    exclude-result-prefixes="mcr i18n">

    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:include href="objecttypes.xsl" />

    <xsl:param name="selected" />

    <xsl:variable name="PageTitle" select="'Blättern A - Z'" />


    <!-- =================================================================== -->
    <xsl:template match="journalList[@mode='javascript']">
        <div id="firstLetterTab" additionalQuery="{additionalQuery}">
            <span class="label tab-fonts"><xsl:value-of select="listTitle"/>:</span>
            <ul id="tabNav" class="tab-nav tab-fonts">
            </ul>
            <div id="resultList" class="tab-panel"></div>
        </div>
        <script src="/js/jp-journalList.js"></script>
        <script type="text/javascript">
          $(document).ready(function() {
            jp.az.load();
          });
        </script>
    </xsl:template>

    <!-- =================================================================== -->
    <xsl:template match="journalList[@url]" priority="2">
        <xsl:apply-templates select="document(@url)/journalList" />
    </xsl:template>
</xsl:stylesheet>