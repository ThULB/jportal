<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="gbv-journalList">
    <script type="text/javascript" src="/js/jp-journalList.js"/>
    <script type="text/javascript" src="/js/gbv-journalList.js"/>
    <script type="text/javascript">
      $(document).ready(function() {
        gbv.az.load();
      });
    </script>
  </xsl:template>

</xsl:stylesheet>