<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  exclude-result-prefixes="acl">
	<xsl:template match="jp-ip-editor">
	
    	<div class="jp-ip-editor">
      		<h1>IP Editor</h1>
      		<xsl:choose>
        		<xsl:when test="acl:checkPermission(@objId, @perm)">
         			<xsl:call-template name="jp.ip.editor.show" />
        		</xsl:when>
        		<xsl:otherwise>
          			<p>Sie haben keine Berechtigung für diesen Bereich.</p>
        		</xsl:otherwise>
      		</xsl:choose>
    	</div>
   	</xsl:template>
	<xsl:template name="jp.ip.editor.show">
		<p id="jp-ip-editor-help">Mit dem IP-Editor können Sie alle IP-Adressen sehen, IP-Adressen löschen und neue hinzufügen.<br/>
		Es können * eingegeben werden um ganze IP-Bereiche freizugeben.</p>
		<table>
		  <tr>
	        <td class="jp-ip-editor-label">IP-Adresse hinzufügen</td>
	        <td>
	        	<div id="jp-ip-editor-ips">
					<input id="jp-ip-editor-ip1" class="jp-ip-editor-ip" type="text" maxlength="3"/>.
					<input id="jp-ip-editor-ip2" class="jp-ip-editor-ip" type="text" maxlength="3"/>.
					<input id="jp-ip-editor-ip3" class="jp-ip-editor-ip" type="text" maxlength="3"/>.
					<input id="jp-ip-editor-ip4" class="jp-ip-editor-ip" type="text" maxlength="3"/>
					<div id="jp-ip-editor-button-add" class="jp-ip-editor-button icon-plus icon-large" title="IP hinzufügen"/>
				</div>
	        </td>
	        <td>
				
	        </td>
	      </tr>
	      <tr>
	        <td class="jp-ip-editor-label">IP-Adressen</td>
	        <td>
	        	<div id="jp-ip-editor-ipList-head">
	        		<input id="jp-ip-editor-button-selectAll" type="checkbox"/>
	        		<div id="jp-ip-editor-button-refresh" class="jp-ip-editor-button jp-ip-editor-button-right icon-refresh icon-large" title="Liste neu laden"/>
	        		<div id="jp-ip-editor-button-deleteMulti" class="jp-ip-editor-button jp-ip-editor-button-right icon-trash icon-large" title="Makierte IPs löschen"/>
	        	</div>
				<ul id="jp-ip-editor-ipList"/>
	        </td>
	      </tr>
	    </table>	
		<xsl:call-template name="jp.ip.editor.js" />
	</xsl:template>

	<xsl:template name="jp.ip.editor.js">
		<script type="text/javascript" src="{$WebApplicationBaseURL}js/jp-ip-editor.js" />
   		<script type="text/javascript">
   			var ruleId = '<xsl:value-of select="@ruleId" />';
	    	var defRule = '<xsl:value-of select="@defRule" />';
	    	$(document).ready(function() {
	        	getIPs();
	      	});
    	</script>
	</xsl:template>
</xsl:stylesheet>