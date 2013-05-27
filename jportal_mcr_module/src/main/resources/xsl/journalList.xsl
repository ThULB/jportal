<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/"
    exclude-result-prefixes="mcr i18n">

    <xsl:include href="MyCoReLayout.xsl" />

    <xsl:param name="selected" />

    <xsl:variable name="PageTitle" select="'Blättern A - Z'" />

    <!-- =================================================================== -->
    <xsl:template match="journalList[@mode='javascript']">
        <div id="firstLetterTab" class="journalList" additionalQuery="{additionalQuery}">
          <div class="atoz">
            <span class="label"><xsl:value-of select="i18n:translate(listTitle/text())"/>:</span>
            <ul id="tabNav" class="tab-nav" />
          </div>
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