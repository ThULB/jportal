<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

  <xsl:template match="/template[@id='template_ilmenau']" mode="template">
    <xsl:param name="mcrObj"/>
    <xsl:apply-templates select="$mcrObj" mode="template_ilmenau" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_ilmenau">
    <script type="text/javascript">
      $(document).ready(function() {
      	$.ajax({
          type: "GET",
          url: "../jp_templates/template_ilmenau/config/linkConfig.xml",
          dataType: "xml",
          success: parseXml
  		});
      });
      function parseXml(xml) {
      	var i = 1;
      	$(xml).find("link").each(function() {
      		var id = $(this).attr("journalId");
      		var img = $(this).attr("img");
      		var alt = $(this).attr("alt");

      		$('#logo').append('<a id="LogoLink' + i + '" href="{$WebApplicationBaseURL}receive/' + id +'"></a>');

      		if (id == '<xsl:value-of select="$journalID" />'){
      			$('#LogoLink' + i).append('<img class="LogoS" src="../jp_templates/template_ilmenau/IMAGES/' + img + '"/>');
      		} else {
	      		$('#LogoLink' + i).append('<img class="LogoS" src="../jp_templates/template_ilmenau/IMAGES/' + alt + '"/>');
      		}
      		i++;
      	});
      }
    </script>
  </xsl:template>
</xsl:stylesheet>