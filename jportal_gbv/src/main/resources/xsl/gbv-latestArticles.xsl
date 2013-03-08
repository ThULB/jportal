<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="gbv-latestArticles">
    <script type="text/javascript" src="/js/gbv-latestArticles.js"/>
    <script type="text/javascript">
      $(document).ready(function() {
        gbv.latestArticles.load('<xsl:value-of select="@query" />');
      });
    </script>
    <div id="latestArticles" class="latestArticles">
    </div>
  </xsl:template>

</xsl:stylesheet>