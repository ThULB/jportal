<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan" xmlns:escapeUtils="org.apache.commons.lang.StringEscapeUtils"
    exclude-result-prefixes="xlink i18n layoutTools">

  <xsl:template name="template_date">
    <xsl:param name="mcrObj"/>
    <script type="text/javascript">
      $(document).ready(function() {
      var pubYear = '<xsl:value-of select="escapeUtils:escapeJavaScript(layoutTools:getJournalPublished($journalID))" />'
      $('#logo').prepend('<div class="logoDate">' + pubYear + '</div>');
      });
    </script>
  </xsl:template>

  <xsl:template name="template_maintitle">
    <xsl:param name="mcrObj"/>
    <script type="text/javascript">
      $(document).ready(function() {
      var maintitle = '<xsl:value-of select="escapeUtils:escapeJavaScript(layoutTools:getMaintitle($journalID))" />';

      $('#logo').append('<div class="logoTitle">' + truncate(maintitle, 96) + '</div>');

      if (maintitle.length > 40) {
      $('#logoTitle').css('font-size', 'large');
      }
      });
    </script>
  </xsl:template>
</xsl:stylesheet>
