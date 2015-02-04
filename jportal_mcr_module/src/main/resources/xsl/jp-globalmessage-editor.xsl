<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
	xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	exclude-result-prefixes="xalan mcrxml acl i18n">

	<xsl:template match="globalmessage-editor">
		<div id="jp-layout-globalmessage-editor">
			<xsl:choose>
				<xsl:when test="acl:checkPermission('administrate-jportal')">
					<div class="panel panel-default" style="margin-top: 1em">
						<div class="panel-heading text-center">
						  <h1 class="panel-title">
						  	<xsl:value-of select="i18n:translate('jp.site.globalMsgEditor.title')" />
						  </h1>
						</div>
						<div class="panel-body">
							<xsl:call-template name="jp.globalmessage.editor" />
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="alert alert-danger">
						<xsl:value-of select="i18n:translate('jp.site.globalMsgEditor.error')" />
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</div>
	</xsl:template>

	<xsl:template name="jp.globalmessage.editor">
		<xsl:call-template name="jp.globalmessage.editor.js" />
		<xsl:variable name="gm"
			select="document('webapp:config/jp-globalmessage.xml')/globalmessage" />
		<div class="form-group col-sm-12">
			<label class="col-sm-2" for="visibility">
				<xsl:value-of select="i18n:translate('jp.site.globalMsgEditor.visibility')" />
			</label>
			<div class="col-sm-10">
				<select id="visibility" class="form-control">
					<option value="hidden">
						<xsl:value-of select="i18n:translate('jp.site.globalMsgEditor.visibility_hidden')" />
					</option>
					<option value="visible">
						<xsl:value-of select="i18n:translate('jp.site.globalMsgEditor.visibility_visible')" />
					</option>
					<option value="user">
						<xsl:value-of select="i18n:translate('jp.site.globalMsgEditor.visibility_user')" />
					</option>
					<option value="admin">
						<xsl:value-of select="i18n:translate('jp.site.globalMsgEditor.visibility_admin')" />
					</option>
				</select>
			</div>
		</div>
		<div class="form-group col-sm-12">
			<label class="col-sm-2" for="head">
				<xsl:value-of select="i18n:translate('jp.site.globalMsgEditor.title_label')" />
			</label>
			<div class="col-sm-10">
				<input class="form-control" type="text" id="head" value="{$gm/head}" />
			</div>
		</div>
		<div class="form-group col-sm-12">
			<label class="col-sm-2" for="message">
				<xsl:value-of select="i18n:translate('jp.site.globalMsgEditor.msg')" />
			</label>
			<div id="testCkeditor" class="col-sm-10">
<!-- 				<textarea id="message" class="form-control" > -->
				<textarea id="message" class="ckeditor" >
					<xsl:value-of select="$gm/message" />
				</textarea>
				<xsl:call-template name="jp.globalmessage.ckeditor.settings" />
			</div>
		</div>
		<div class="col-sm-12 text-right row">
			<button class="btn btn-primary" type="submit" id="submit" >
				<xsl:value-of select="i18n:translate('common.button.save')" />
			</button>
		</div>
	</xsl:template>

	<xsl:template name="jp.globalmessage.editor.js">
		<script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/ckeditor/4.0.1/ckeditor.js" />
		<script type="text/javascript">
		  $(document).ready(function() {
			$('#submit').on('click', function () {
			  var visibility = $('#visibility').val();
			  var head = $('#head').val();
<!-- 			  var message = $('#message').val(); -->
				var message = CKEDITOR.instances.message.getData();  
			  $.ajax({
				url: jp.baseURL + "rsc/globalMessage/save",
			    type:"POST",
			    data: JSON.stringify({
				  visibility: visibility,
				  head: head,
				  message: message
				}),
				contentType: "application/json; charset=utf-8",
				success: function() {
				  $('<div style="font-size:120%;color:green;text-align:center">Speichern erfolgreich</div>').appendTo('#jp-layout-globalmessage-editor').fadeOut(2500);
				},
				error: function(error) {
				  alert(error);
				}
			  });
			});
		  });
		</script>
	</xsl:template>

	<xsl:template name="jp.globalmessage.ckeditor.settings">
		<script type="text/javascript">
		  CKEDITOR.replace('message',{
    		resize_enabled : false,
    		entities: false,
    		enterMode: CKEDITOR.ENTER_BR,
    		entities_processNumerical: 'force',
    		tabSpaces: 4,
    		fillEmptyBlocks: false,
    		height : '200px',
    		toolbar : [ [ 'Undo', 'Redo', '-', 'Bold', 'Italic', '-', 'NumberedList', 'BulletedList', '-',
    		             'Link', 'Unlink', 'Source', 'Save' ] ]
    	});
		</script>
	</xsl:template>

</xsl:stylesheet>
