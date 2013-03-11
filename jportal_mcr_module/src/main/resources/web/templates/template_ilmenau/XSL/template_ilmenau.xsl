<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink i18n">

  <xsl:template match="/template[@id='template_ilmenau']" mode="template">
    <xsl:apply-templates select="document(concat('mcrobject:',@mcrID))/mycoreobject" mode="template_ilmenau" />
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="template_ilmenau">
  	<xsl:variable name="journalId">
		<xsl:value-of select="document(concat('mcrobject:',/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID))/mycoreobject/@ID" />
	</xsl:variable>
    <script type="text/javascript">
      $(document).ready(function() {
      	$.ajax({
    			type: "GET",
    			url: "../templates/template_ilmenau/config/linkConfig.xml",
    			dataType: "xml",
    			success: parseXml
  		});
      });
      function parseXml(xml){
      	var i = 1;
      	$(xml).find("link").each(function()
      	{
      		var id = $(this).attr("journalId");
      		var img = $(this).attr("img");
      		var alt = $(this).attr("alt");
      		
      		$('#logo').append('<a id="LogoLink' + i + '" href="{$WebApplicationBaseURL}receive/' + id +'"></a>');
      		
      		if (id == '<xsl:value-of select="$journalId" />'){
      			$('#LogoLink' + i).append('<img class="LogoS" src="../templates/template_ilmenau/IMAGES/' + img + '"/>');
      		}
      		else{
	      		$('#LogoLink' + i).append('<img class="LogoS" src="../templates/template_ilmenau/IMAGES/' + alt + '"/>');
      		}
      		
      		i++;
      	});
      }
    </script>
  </xsl:template>
</xsl:stylesheet>