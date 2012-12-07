<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n" xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:template match="/template[@id='template_thueringen']" mode="template">
    <xsl:apply-templates select="document(concat('mcrobject:',@mcrID))/mycoreobject" mode="template_thueringen" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_thueringen">
    <script type="text/javascript">
      $(document).ready(function() {		
        var name = '<xsl:value-of select="document(concat('mcrobject:',/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID))/mycoreobject/metadata/maintitles/maintitle" />';
        $('#logo').prepend('<h1 class="logoTitle">' + name  + '</h1>');
      });
    </script>
  </xsl:template>

</xsl:stylesheet>
