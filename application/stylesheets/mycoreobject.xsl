<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 1.18 $ $Date: 2006/11/14 10:49:45 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xlink mcr xalan i18n acl">
	
	<xsl:include href="MyCoReLayout.xsl"/>
	<!-- include custom templates for supported objecttypes -->
	<xsl:include href="objecttypes.xsl"/>
	
	<xsl:variable name="PageTitle">
		<xsl:call-template name="getPageTitle"/>
	</xsl:variable>
	
	<xsl:variable name="Servlet" select="'undefined'"/>
	<xsl:param name="resultListEditorID"/>
	<xsl:param name="numPerPage"/>
	<xsl:param name="page"/>
	<xsl:param name="previousObject"/>
	<xsl:param name="previousObjectHost"/>
	<xsl:param name="nextObject"/>
	<xsl:param name="nextObjectHost"/>
	
	<xsl:template match="/mycoreobject">
		<xsl:variable name="obj_host">
			<xsl:value-of select="$objectHost"/>
		</xsl:variable>
		
		<!-- Here put in dynamic resultlist -->
		<xsl:if test="./structure/parents">
			<div id="parent">
				<!-- Pay a little attention to this !!! -->
				
				<xsl:apply-templates select="./structure/parents">
					<xsl:with-param name="obj_host" select="$obj_host"/>
					<xsl:with-param name="obj_type" select="'this'"/>
				</xsl:apply-templates>
				&#160;&#160;
				<xsl:apply-templates select="./structure/parents">
					<xsl:with-param name="obj_host" select="$obj_host"/>
					<xsl:with-param name="obj_type" select="'before'"/>
					
				</xsl:apply-templates>
				
				&#160;&#160;
				<xsl:apply-templates select="./structure/parents">
					<xsl:with-param name="obj_host" select="$obj_host"/>
					<xsl:with-param name="obj_type" select="'after'"/>
				</xsl:apply-templates>
			</div>
		</xsl:if>
		
		<xsl:call-template name="resultsub"/>
		<xsl:choose>
			
			<xsl:when test="($obj_host != 'local') or acl:checkPermission(/mycoreobject/@ID,'read')">
				<!-- if access granted: print metadata -->
				<xsl:apply-templates select="." mode="present"/>
				<!-- IE Fix for padding and border -->
				<hr/>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:value-of select="i18n:translate('metaData.accessDenied')"/>
			</xsl:otherwise>
			
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="/mycoreobject" mode="present" priority="0">
		<xsl:variable name="objectType" select="substring-before(substring-after(@ID,'_'),'_')"/>
		<xsl:value-of select="i18n:translate('metaData.noTemplate')"/>
		
		<form method="get" style="padding:20px;background-color:yellow">
			<fieldset>
				<legend>Automatisches Erzeugen von Vorlagen</legend>
				
				<table>
					<tr>
						<td>
							<label>Was erzeugen?</label>
						</td>
						<td>
							
							<select name="XSL.Style">
								<option value="generateMessages">messages_*.properties Vorlage</option>
								<option value="generateStylesheet">
									<xsl:value-of select="concat($objectType,'.xsl Vorlage')"/>
								</option>
							</select>
							
						</td>
					</tr>
				</table>
			</fieldset>
			
			<fieldset>
				<legend>
					<xsl:value-of select="concat('Optionen für Erzeugen von ',$objectType,'.xsl')"/>
				</legend>
				<table>
					<tr>
						<td>
							<label>Objekt kann Derivate enthalten</label>
						</td>
						
						<td>
							<input type="checkbox" name="XSL.withDerivates"/>
						</td>
						
					</tr>
					<tr>
						<td>
							<label>Unterstützung für IView</label>
						</td>
						<td>
							<input type="checkbox" name="XSL.useIView"/>
							
						</td>
					</tr>
					<tr>
						
						<td>
							<label>Kindobjekttypen</label>
						</td>
						<td>
							<input name="XSL.childObjectTypes"/> (durch Leerzeichen getrennt) </td>
					</tr>
					
				</table>
			</fieldset>
			
			<input type="submit" value="erstellen"/>
		</form>
		<xsl:variable name="objectBaseURL">
			<xsl:if test="$objectHost != 'local'">
				<xsl:value-of
					select="document('webapp:hosts.xml')/mcr:hosts/mcr:host[@alias=$objectHost]/mcr:url[@type='object']/@href"/>
			</xsl:if>
			<xsl:if test="$objectHost = 'local'">
				
				<xsl:value-of select="concat($WebApplicationBaseURL,'receive/')"/>
				
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="staticURL">
			<xsl:value-of select="concat($objectBaseURL,@ID)"/>
		</xsl:variable>
		<table id="metaData" cellpadding="0" cellspacing="0">
			<!-- display all metadata -->
			<xsl:for-each select="./metadata/*">
				
				<xsl:call-template name="printMetaDate">
					<xsl:with-param name="nodes" select="./*"/>
				</xsl:call-template>
			</xsl:for-each>
			<!-- Created ***************************************************** -->
			<xsl:call-template name="printMetaDate">
				<xsl:with-param name="nodes" select="./service/servdates/servdate[@type='createdate']"/>
				<xsl:with-param name="label" select="i18n:translate('metaData.createdAt')"/>
			</xsl:call-template>
			
			<!-- Last Change ************************************************* -->
			<xsl:call-template name="printMetaDate">
				<xsl:with-param name="nodes" select="./service/servdates/servdate[@type='modifydate']"/>
				<xsl:with-param name="label" select="i18n:translate('metaData.lastChanged')"/>
			</xsl:call-template>
			<!-- MyCoRe ID *************************************************** -->
			<tr>
				<td class="metaname">
					
					<xsl:value-of select="concat(i18n:translate('metaData.ID'),' :')"/>
					
				</td>
				<td class="metavalue">
					<xsl:value-of select="./@ID"/>
				</td>
			</tr>
		</table>
	</xsl:template>
	
	<!-- Generates a header for the metadata output -->
	
	<xsl:template name="resultsub">
		<table id="metaHeading" cellpadding="0" cellspacing="0">
			<tr>
				<td class="titles">
					<xsl:apply-templates select="." mode="title"/>
				</td>
				<td class="browseCtrl">
					
					<xsl:call-template name="browseCtrl"/>
				</td>
				
			</tr>
		</table>
		<!-- IE Fix for padding and border -->
		<hr/>
	</xsl:template>
	
	<xsl:template name="browseCtrl">
		
		<xsl:if test="string-length($previousObject)>0">
			<xsl:variable name="hostParam">
				
				<xsl:if test="$previousObjectHost != 'local'">
					<xsl:value-of select="concat('?host=',$previousObjectHost)"/>
				</xsl:if>
			</xsl:variable>
			<a href="{$WebApplicationBaseURL}receive/{$previousObject}{$HttpSession}{$hostParam}">&lt;&lt;
			</a>
			&#160;&#160;
			
		</xsl:if>
		<xsl:if test="string-length($numPerPage)>0">
			<a
				href="{$ServletsBaseURL}MCRSearchServlet{$HttpSession}?mode=results&amp;id={$resultListEditorID}&amp;page={$page}&amp;numPerPage={$numPerPage}">
				^ </a>
		</xsl:if>
		<xsl:if test="string-length($nextObject)>0">
			<xsl:variable name="hostParam">
				<xsl:if test="$nextObjectHost != 'local'">
					
					<xsl:value-of select="concat('?host=',$nextObjectHost)"/>
				</xsl:if>
			</xsl:variable>
			
			&#160;&#160;
			<a href="{$WebApplicationBaseURL}receive/{$nextObject}{$HttpSession}{$hostParam}">&gt;&gt;
			</a>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="/mycoreobject" mode="title" priority="0">
		
		<xsl:value-of select="@ID"/>
	</xsl:template>
	
	<!-- Internal link from Derivate ********************************* -->
	
	<xsl:template match="internals">
		<xsl:if test="$objectHost = 'local'">
			<xsl:variable name="obj_host" select="../../../@host"/>
			<xsl:variable name="derivid" select="../../@ID"/>
			<xsl:variable name="derivlabel" select="../../@label"/>
			
			<xsl:variable name="derivmain" select="internal/@maindoc"/>
			<xsl:variable name="derivbase" select="concat($ServletsBaseURL,'MCRFileNodeServlet/',$derivid,'/')"/>
			<xsl:variable name="derivifs" select="concat($derivbase,$derivmain,$HttpSession,'?hosts=',$obj_host)"/>
			<xsl:variable name="derivdir" select="concat($derivbase,$HttpSession,'?hosts=',$obj_host)"/>
			
			<xsl:variable name="derivxml" select="concat('ifs:/',$derivid,'?hosts=',$obj_host)"/>
			<xsl:variable name="details" select="document($derivxml)"/>
			<xsl:variable name="ctype" select="$details/mcr_directory/children/child[name=$derivmain]/contentType"/>
			<xsl:variable name="ftype"
				select="document('webapp:FileContentTypes.xml')/FileContentTypes/type[@ID=$ctype]/label"/>
			
			<xsl:variable name="size" select="$details/mcr_directory/size"/>
			<div class="derivateHeading">
				<xsl:value-of select="$derivlabel"/>
			</div>
			<div class="derivate">
				
				<a href="{$derivifs}">
					<xsl:value-of select="$derivmain"/>
				</a> (
				<xsl:value-of select="ceiling(number($size) div 1024)"/>
				&#160;kB) &#160;&#160;
				<xsl:variable name="ziplink"
					select="concat($ServletsBaseURL,'MCRZipServlet',$JSessionID,'?id=',$derivid)"/>
				<a class="linkButton" href="{$ziplink}">
					<xsl:value-of select="i18n:translate('buttons.zipGen')"/>
					
				</a>
				&#160;
				
				<a href="{$derivdir}">
					<xsl:value-of select="i18n:translate('buttons.details')"/>
				</a> </div>
		</xsl:if>
	</xsl:template>
	
	<!-- External link from Derivate ********************************* -->
	<xsl:template match="externals">
		
		<div class="derivateHeading">
			<xsl:value-of select="i18n:translate('metaData.link')"/>
		</div>
		<div class="derivate">
			<xsl:call-template name="webLink">
				<xsl:with-param name="nodes" select="external"/>
			</xsl:call-template>
		</div>
	</xsl:template>
	
	<!-- Link to the parent ****************************************** -->
	
	<xsl:template match="parents">
		<xsl:param name="obj_host" select="$objectHost"/>
		<xsl:param name="obj_type"/>
		<xsl:variable name="hostParam">
			<xsl:if test="$obj_host != 'local'">
				<xsl:value-of select="concat('?host=',$obj_host)"/>
			</xsl:if>
			
		</xsl:variable>
		
		<xsl:variable name="thisid">
			<xsl:value-of select="../../@ID"/>
		</xsl:variable>
		<xsl:variable name="parent">
			<xsl:if test="$obj_host = 'local'">
				<xsl:copy-of select="document(concat('mcrobject:',parent/@xlink:href))/mycoreobject"/>
			</xsl:if>
			<xsl:if test="$obj_host != 'local'">
				
				<xsl:copy-of
					select="document(concat('mcrws:operation=MCRDoRetrieveObject&amp;host=',$obj_host,'&amp;ID=',parent/@xlink:href))/mycoreobject"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="parent" select="xalan:nodeset($parent)"/>
		<xsl:choose>
			<xsl:when test="$obj_type = 'this'">
				<xsl:call-template name="objectLink">
					<xsl:with-param name="obj_id" select="parent/@xlink:href"/>
				</xsl:call-template>
				
			</xsl:when>
			<xsl:when test="$obj_type = 'before'">
				<xsl:variable name="pos">
					<xsl:for-each select="$parent/structure/children/child">
						<xsl:sort select="."/>
						<xsl:variable name="child">
							<xsl:value-of select="@xlink:href"/>
						</xsl:variable>
						
						<xsl:if test="$thisid = $child">
							
							<xsl:value-of select="position()"/>
						</xsl:if>
					</xsl:for-each>
				</xsl:variable>
				<xsl:for-each select="$parent/structure/children/child">
					<xsl:sort select="."/>
					<xsl:variable name="child">
						
						<xsl:value-of select="@xlink:href"/>
					</xsl:variable>
					
					<xsl:if test="position() = $pos - 1">
						<a href="{$WebApplicationBaseURL}receive/{$child}{$HttpSession}{$hostParam}">
							&#60;-- </a>
					</xsl:if>
				</xsl:for-each>
			</xsl:when>
			
			<xsl:when test="$obj_type = 'after'">
				<xsl:variable name="pos">
					
					<xsl:for-each select="$parent/structure/children/child">
						<xsl:sort select="."/>
						<xsl:variable name="child">
							<xsl:value-of select="@xlink:href"/>
						</xsl:variable>
						<xsl:if test="$thisid = $child">
							
							<xsl:value-of select="position()"/>
						</xsl:if>
					</xsl:for-each>
					
				</xsl:variable>
				<xsl:for-each select="$parent/structure/children/child">
					<xsl:sort select="."/>
					<xsl:variable name="child">
						<xsl:value-of select="@xlink:href"/>
						
					</xsl:variable>
					<xsl:if test="position() = $pos + 1">
						<a href="{$WebApplicationBaseURL}receive/{$child}{$HttpSession}{$hostParam}"> --&#62; </a>
					</xsl:if>
				</xsl:for-each>
			</xsl:when>
			
		</xsl:choose>
	</xsl:template>
	
	<!-- Work with the children ************************************** -->
	<xsl:template match="children">
		
		<ul>
			<xsl:for-each select="child">
				<xsl:sort select="@xlink:label"/>
				<li>
					
					<xsl:apply-templates select="."/>
				</li>
			</xsl:for-each>
		</ul>
	</xsl:template>
	
	<!-- Link to the child ******************************************* -->
	<xsl:template match="child">
		<xsl:call-template name="objectLink">
			
			<xsl:with-param name="obj_id" select="@xlink:href"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- The template write the icon line to edit an object -->
	<xsl:template name="editobject">
		
		<xsl:param name="accessedit"/>
		<xsl:param name="accessdelete"/>
		<xsl:param name="id"/>
		
		<xsl:param name="layout" select="'$'"/>
		<xsl:variable name="layoutparam">
			<xsl:if test="$layout != '$'">
				<xsl:value-of select="concat('&amp;layout=',$layout)"/>
			</xsl:if>
		</xsl:variable>
		
		<xsl:if test="$objectHost = 'local'">
			<xsl:choose>
				
				<xsl:when test="acl:checkPermission($id,'writedb') or acl:checkPermission($id,'deletedb')">
					<xsl:variable name="type" select="substring-before(substring-after($id,'_'),'_')"/>
					<tr>
						<td class="metaname">
							<xsl:value-of select="concat(i18n:translate('metaData.edit'),' :')"/>
						</td>
						<td class="metavalue">
							
							<xsl:if test="acl:checkPermission($id,'writedb')">
								
								<a
									href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=seditobj">
									<img src="{$WebApplicationBaseURL}images/workflow_objedit.gif"
										title="{i18n:translate('swf.object.editObject')}"/>
								</a>
								<a
									href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type=acl&amp;step=commit&amp;todo=seditacl">
									<img src="{$WebApplicationBaseURL}images/workflow_acledit.gif"
										title="{i18n:translate('swf.object.editACL')}"/>
								</a>
							</xsl:if>
							<xsl:if test="acl:checkPermission($id,'deletedb')">
								
								<a
									href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=sdelobj">
									<img src="{$WebApplicationBaseURL}images/workflow_objdelete.gif"
										title="{i18n:translate('swf.object.delObject')}"/>
								</a>
							</xsl:if>
						</td>
					</tr>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
		
	</xsl:template>
	
	<!-- The template write the icon line to edit an object with derivate -->
	<xsl:template name="editobject_with_der">
		<xsl:param name="accessedit"/>
		<xsl:param name="accessdelete"/>
		<xsl:param name="id"/>
		<xsl:param name="layout" select="'$'"/>
		<xsl:variable name="layoutparam">
			
			<xsl:if test="$layout != '$'">
				<xsl:value-of select="concat('&amp;layout=',$layout)"/>
			</xsl:if>
		</xsl:variable>
		<xsl:if test="$objectHost = 'local'">
			<xsl:choose>
				<xsl:when test="acl:checkPermission($id,'writedb') or acl:checkPermission($id,'deletedb')">
					<xsl:variable name="type" select="substring-before(substring-after($id,'_'),'_')"/>
					
					<tr>
						
						<td class="metaname">
							<xsl:value-of select="concat(i18n:translate('metaData.edit'),' :')"/>
						</td>
						<td class="metavalue">
							<xsl:if test="acl:checkPermission($id,'writedb')">
								<a
									href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=seditobj">
									<img src="{$WebApplicationBaseURL}images/workflow_objedit.gif"
										title="{i18n:translate('swf.object.editObject')}"/>
									
								</a>
								<a
									href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type=acl&amp;step=commit&amp;todo=seditacl">
									
									<img src="{$WebApplicationBaseURL}images/workflow_acledit.gif"
										title="{i18n:translate('swf.object.editACL')}"/>
								</a>
								<a
									href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=snewder">
									<img src="{$WebApplicationBaseURL}images/workflow_deradd.gif"
										title="{i18n:translate('swf.derivate.addDerivate')}"/>
								</a>
							</xsl:if>
							
							<xsl:if test="acl:checkPermission($id,'deletedb')">
								<xsl:choose>
									<xsl:when test="contains($id,'_jpjournal_')"/>
									<xsl:otherwise>
										<a
											href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=sdelobj">
											<img src="{$WebApplicationBaseURL}images/workflow_objdelete.gif"
												title="{i18n:translate('swf.object.delObject')}"/>
											
										</a>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:if>
						</td>
					</tr>
				</xsl:when>
				
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="getPageTitle">
		<xsl:choose>
			<xsl:when
				test="contains(/mycoreobject/@ID,'_jpjournal_') 
				or contains(/mycoreobject/@ID,'_jpvolume_') 
				or contains(/mycoreobject/@ID,'_jparticle_')  ">
				
				<xsl:variable name="title">
					<xsl:call-template name="ShortenText">
						<xsl:with-param name="text"
							select="mycoreobject/metadata/maintitles/maintitle[@inherited=0]/text()"/>
						<xsl:with-param name="length" select="75"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$view.objectmetadata='false'">
						<xsl:call-template name="printHistoryRow">
							<xsl:with-param name="sortOrder" select="'descending'"/>
							<xsl:with-param name="printCurrent" select="'false'"/>
						</xsl:call-template>						
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="printHistoryRow">
							<xsl:with-param name="sortOrder" select="'descending'"/>
							<xsl:with-param name="printCurrent" select="'true'"/>
						</xsl:call-template>						
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'Metadaten'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>