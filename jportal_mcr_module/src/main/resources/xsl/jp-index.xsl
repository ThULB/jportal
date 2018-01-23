<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xalan i18n">

  <xsl:template match="jpindex">
    <style type="text/css">
      #logo {
      background-image: none;
      }
    </style>

    <div class="jp-layout-index col-md-12">
      <div class="logo container"><img src="{$templateWebURL}IMAGES/logo.svg" alt="logo"/></div>
      <div class="info container">
        Das von der Thüringer Universitäts- und Landesbibliothek Jena (ThULB) betriebene Internetportal journals@UrMEL
        bietet Zugang zu wissenschaftlichen Zeitschriften in digitaler Form.
      </div>
      <div id="portfolio" class="container">
        <nav>
          <a class="btn btn-default" href="{$WebApplicationBaseURL}content/main/journalList.xml#A" role="button">ZUM BESTAND</a>
        </nav>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>