<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan" xmlns:escapeUtils="org.apache.commons.lang.StringEscapeUtils"
    exclude-result-prefixes="xlink i18n layoutTools">

  <xsl:template match="template[@id='template_altertumErfurt']" mode="template">
    <xsl:param name="mcrObj"/>
    <script type="text/javascript">
      $(document).ready(function() {
        var name = '<xsl:value-of select="escapeUtils:escapeJavaScript(layoutTools:getMaintitle($journalID))" />';
        $('#logo').prepend('<h1 class="logoTitle">' + truncate(name, 120)  + '</h1>');
      });
    </script>
  </xsl:template>
</xsl:stylesheet>
