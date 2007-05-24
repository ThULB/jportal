<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
	xmlns:aclObjID="xalan://org.mycore.access.strategies.MCRObjectIDStrategy"
	xmlns:aclObjType="xalan://org.mycore.access.strategies.MCRJPortalStrategy"
	xmlns:mcr="http://www.mycore.org/" xmlns:xlink="http://www.w3.org/1999/xlink"
	exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
	<xsl:param select="'local'" name="objectHost"/>
	<!-- ===================================================================================================== -->
	<xsl:template name="dateConvert">
		<xsl:param name="dateUnconverted"/>
		<xsl:variable name="format">
			<xsl:choose>
				<xsl:when test="string-length(normalize-space(.))=4">
					<xsl:value-of select="i18n:translate('metaData.dateYear')"/>
				</xsl:when>
				<xsl:when test="string-length(normalize-space(.))=7">
					<xsl:value-of select="i18n:translate('metaData.dateYearMonth')"/>
				</xsl:when>
				<xsl:when test="string-length(normalize-space(.))=10">
					<xsl:value-of select="i18n:translate('metaData.dateYearMonthDay')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="i18n:translate('metaData.dateTime')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:call-template name="formatISODate">
			<xsl:with-param name="date" select="."/>
			<xsl:with-param name="format" select="$format"/>
		</xsl:call-template>
	</xsl:template>
	<!-- ===================================================================================================== -->
	<!--Template for result list hit: see results.xsl-->
	<xsl:template match="mcr:hit[contains(@id,'_jpvolume_')]">
		<xsl:param name="mcrobj"/>
		<xsl:param name="mcrobjlink"/>
		<xsl:variable select="100" name="DESCRIPTION_LENGTH"/>
		<xsl:variable select="@host" name="host"/>
		<xsl:variable name="obj_id">
			
			<xsl:value-of select="@id"/>
		</xsl:variable>
		<xsl:variable name="cXML">
			<xsl:copy-of select="document(concat('mcrobject:',@id))"/>
		</xsl:variable>
    <table cellspacing="0" cellpadding="0" id="leaf-all">
      <tr>
        <td id="leaf-front" colspan="1" rowspan="2">
          <img src="{$WebApplicationBaseURL}images/band2.gif"/>
        </td>
        <td id="leaf-linkarea2">
          <xsl:variable name="name">
            <xsl:call-template name="ShortenText">
              <xsl:with-param name="text"
                select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()"/>
              <xsl:with-param name="length" select="75"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:variable name="date">
            <xsl:choose>
              <xsl:when test="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0']">
                <xsl:variable name="date">
                  <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date/text()"/>
                </xsl:variable>
                <xsl:value-of select="concat(' (',$date,')')"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="''"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="label">
            <xsl:value-of select="concat($name,$date)"/>
          </xsl:variable>
          <xsl:call-template name="objectLinking">
            <xsl:with-param name="obj_id" select="@id"/>
            <xsl:with-param name="obj_name" select="$label"/>
            <xsl:with-param name="requestParam"
              select="'XSL.view.objectmetadata.SESSION=false&amp;XSL.toc.pos.SESSION=0'"/>
          </xsl:call-template>
        </td>
      </tr>
      <xsl:call-template name="printDerivates">
        <xsl:with-param name="obj_id" select="@id"/>
        <xsl:with-param name="knoten" select="$cXML"/>
      </xsl:call-template>
    </table>
		<table cellspacing="0" cellpadding="0">
			<tr id="leaf-whitespaces">
				<td>
				</td>
			</tr>
		</table>
	</xsl:template>
  <!-- =============================================================================================== -->
  <xsl:template match="mcr:hit[contains(@id,'_jpvolume_')]" mode="toc">
    <xsl:param name="mcrobj"/>
    <xsl:param name="mcrobjlink"/>
    
    <xsl:variable name="cXML">
      <xsl:copy-of select="document(concat('mcrobject:',@id))"/>
    </xsl:variable>
    
    <table cellspacing="0" cellpadding="0" id="leaf-all">
      <tr>
        <td id="leaf-front" colspan="1" rowspan="2">
          <img src="{$WebApplicationBaseURL}images/band2.gif"/>
        </td>
        <td id="leaf-linkarea2">
          <xsl:variable name="name">
            <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/maintitles/maintitle/text()"/>
          </xsl:variable>
          <xsl:variable name="date">
            <xsl:choose>
              <xsl:when test="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0']">
                <xsl:variable name="date">
                  <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date/text()"/>
                </xsl:variable>
                <xsl:value-of select="concat(' (',$date,')')"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="''"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="label">
            <xsl:value-of select="concat($name,$date)"/>
          </xsl:variable>
          <xsl:variable name="shortlabel">
            <xsl:call-template name="ShortenText">
              <xsl:with-param name="text" select="$label"/>
              <xsl:with-param name="length" select="400"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:variable name="children">
            <xsl:choose>
              <xsl:when test="(xalan:nodeset($cXML)/mycoreobject/structure/children)">
                <xsl:value-of select="'true'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'false'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="(contains(@id,'_jparticle_')) 
              or ($children='false') ">
              <xsl:call-template name="objectLinking">
                <xsl:with-param name="obj_id" select="@id"/>
                <xsl:with-param name="obj_name" select="$shortlabel"/>
                <xsl:with-param name="hoverText" select="$name"/>
                <xsl:with-param name="requestParam" select="'XSL.view.objectmetadata.SESSION=false'"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="objectLinking">
                <xsl:with-param name="obj_id" select="@id"/>
                <xsl:with-param name="obj_name" select="$shortlabel"/>
                <xsl:with-param name="hoverText" select="$name"/>
                <xsl:with-param name="requestParam"
                  select="'XSL.view.objectmetadata.SESSION=true&amp;XSL.toc.pos.SESSION=1'"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
      <xsl:call-template name="printDerivates">
        <xsl:with-param name="obj_id" select="@id"/>
        <xsl:with-param name="knoten" select="$cXML"/>
      </xsl:call-template>
    </table>
    <br/>
  </xsl:template>
<!-- ================================================================================================================= -->  	
	
	<!--Template for generated link names and result titles: see mycoreobject.xsl, results.xsl, MyCoReLayout.xsl-->
	<xsl:template priority="1" mode="resulttitle" match="/mycoreobject[contains(@ID,'_jpvolume_')]">
		<xsl:choose>
			<!--
			you could insert any title-like metadata here, e.g.
			replace "your-tags/here" by something of your metadata
			-->
			<xsl:when test="./metadata/your-tags">
				<xsl:call-template name="printI18N">
					<xsl:with-param select="./metadata/your-tags/here" name="nodes"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				
				<xsl:value-of select="@label"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--Template for title in metadata view: see mycoreobject.xsl-->
	<xsl:template priority="1" mode="title" match="/mycoreobject[contains(@ID,'_jpvolume_')]">
		<xsl:choose>
			<!--
			you could insert any title-like metadata here, e.g.
			replace "your-tags/here" by something of your metadata
			-->
			<xsl:when test="./metadata/your-tags">
				
				<xsl:call-template name="printI18N">
					<xsl:with-param select="./metadata/your-tags/here" name="nodes"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@ID"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- =================================================================================================================================== -->
	<!--Template for metadata view: see mycoreobject.xsl-->
	<xsl:template priority="1" mode="present" match="/mycoreobject[contains(@ID,'_jpvolume_')]">
		<xsl:param select="$objectHost" name="obj_host"/>
		<xsl:param name="accessedit"/>
		<xsl:param name="accessdelete"/>
		
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
		<div id="detailed-frame">
			<table border="0" cellspacing="0">
				<tr>
					<td id="detailed-cube">
						<img src="{$WebApplicationBaseURL}images/band.gif"/>
					</td>
					<td id="detailed-mainheadline">
						<xsl:variable name="maintitle_shorted">
							<xsl:call-template name="ShortenText">
								<xsl:with-param name="text" select="./metadata/maintitles/maintitle[@inherited='0']/text()"/>
								<xsl:with-param name="length" select="75"/>
							</xsl:call-template>
						</xsl:variable>						
						<xsl:value-of select="$maintitle_shorted"/>
					</td>
					<td id="detailed-links" colspan="1" rowspan="3">
						<table id="detailed-contenttable" border="0" cellspacing="0">
							<xsl:call-template name="printDerivates">
								<xsl:with-param name="obj_id" select="@ID"/>
							</xsl:call-template>
						</table>
					</td>
				</tr>
				<tr>
					<td colspan="3" rowspan="1">
						<table cellspacing="0" cellpadding="0" id="detailed-view">
							<tr>
								<td id="detailed-headlines"><xsl:value-of select="i18n:translate('metaData.headlines.formaldiscr')"/></td>
							</tr>
							
							<!--1***maintitle*************************************-->
							
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/maintitles/maintitle[@inherited='0']" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.bibdescript')" name="label"/>
							</xsl:call-template>
							
							<!--2***subtitle*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/subtitles/subtitle" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.subtitle')" name="label"/>
							</xsl:call-template>
							<!--3***participant*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="'right'" name="textalign"/>
								<xsl:with-param select="./metadata/participants/participant" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.participants_label')" name="label"/>
							</xsl:call-template>
							<!--4***date*************************************-->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./metadata/dates/date" name="nodes"/>
								<xsl:with-param select="i18n:translate('editormask.labels.date_label')" name="label"/>
							</xsl:call-template>
							<xsl:if test="./metadata/notes/note">
								<!--5***note*************************************-->
								<xsl:call-template name="printMetaDates">
									<xsl:with-param select="'true'" name="volume-node"/>
									<xsl:with-param select="./metadata/notes/note" name="nodes"/>
									<xsl:with-param select="i18n:translate('editormask.labels.note')" name="label"/>
								</xsl:call-template>
							</xsl:if>
							<tr id="detailed-dividingline">
								<td colspan="2">
									<hr noshade="noshade" style="width: max; min-width: 600px;"/>
								</td>
							</tr>
							<tr>
								<td id="detailed-headlines"><xsl:value-of select="i18n:translate('metaData.headlines.systemdata')"/></td>
							</tr>
							<!--*** Created ************************************* -->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./service/servdates/servdate[@type='createdate']" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.datecr')" name="label"/>
							</xsl:call-template>
							
							<!--*** Last Modified ************************************* -->
							<xsl:call-template name="printMetaDates">
								<xsl:with-param select="./service/servdates/servdate[@type='modifydate']" name="nodes"/>
								<xsl:with-param select="i18n:translate('editor.search.document.datemod')" name="label"/>
								
							</xsl:call-template>
							
							<!--*** MyCoRe-ID ************************************* -->
							<tr>
								<td class="metaname" style="text-align:right; padding-right: 5px;">
									<xsl:value-of select="concat(i18n:translate('metaData.ID'),':')"/>
								</td>
								<td class="metavalue">
									<xsl:value-of select="./@ID"/>
								</td>
								
							</tr>
							<!-- Static URL ************************************************** -->
							<xsl:call-template name="get.staticURL">
								<xsl:with-param name="stURL" select="$staticURL"/>
							</xsl:call-template>
							<xsl:call-template name="emptyRow"/>
							
							<!-- Administration ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
							<xsl:call-template name="showAdminHead"/>
							<!--*** Editor Buttons ************************************* -->
							<xsl:call-template name="editobject_with_der">
								<xsl:with-param select="$accessedit" name="accessedit"/>
								<xsl:with-param select="./@ID" name="id"/>
							</xsl:call-template>
							<xsl:if test="acl:checkPermission(./@ID,'writedb')">
								<xsl:call-template name="addChild2">
									<xsl:with-param name="id" select="./@ID"/>
									<xsl:with-param name="types" select="'jpvolume'"/>
								</xsl:call-template>
							</xsl:if>
							<xsl:variable name="params_dynamicClassis">
								<xsl:call-template name="get.params_dynamicClassis"/>
							</xsl:variable>
							
							<xsl:variable name="journalID">
								<xsl:value-of select="./metadata/hidden_jpjournalsID/hidden_jpjournalID/text()"/>
							</xsl:variable>
							
							<xsl:if test="aclObjType:checkPermissionOfType('jportal_jparticle_xxxxxxxx','writedb') and aclObjID:checkPermission($journalID,'writedb')">
								<xsl:call-template name="addChild2">
									<xsl:with-param name="id" select="./@ID"/>
									<xsl:with-param name="types" select="'jparticle'"/>
									<xsl:with-param select="$params_dynamicClassis" name="layout"/>
								</xsl:call-template>
							</xsl:if>
						</table>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>
	
	
	<!-- =================================================================================================================================== -->
	
	<xsl:template name="addChild2">
		<xsl:param name="id"/>
		<xsl:param name="layout"/>
		<xsl:param name="types"/>
		<xsl:param select="concat('&amp;_xml_structure%2Fparents%2Fparent%2F%40href=',$id)" name="xmltempl"/>
		<xsl:variable name="suffix">
			
			<xsl:if test="string-length($layout)&gt;0">
				<xsl:value-of select="concat('&amp;layout=',$layout)"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="typeToken">
			<xsl:call-template name="Tokenizer">
				<xsl:with-param select="$types" name="string"/>
			</xsl:call-template>
		</xsl:variable>
		
<!--		<xsl:if test="acl:checkPermission($id,'writedb')">-->
			<tr>
				<td class="metaname">
					<xsl:value-of select="concat(i18n:translate('metaData.addChildObject'),':')"/>
				</td>
				<td class="metavalue">
					<ul>
						<xsl:for-each select="xalan:nodeset($typeToken)/token">
							<xsl:variable select="." name="type"/>
							
							<li>
								<a
									href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;step=author&amp;todo=wnewobj{$suffix}{$xmltempl}">
									<xsl:value-of select="i18n:translate(concat('metaData.',$type,'.[singular]'))"/>
								</a>
							</li>
						</xsl:for-each>
					</ul>
				</td>
			</tr>
			
<!--		</xsl:if>-->
	</xsl:template>
	
	<!-- =================================================================================================================================== -->	
	
	<xsl:template name="Derobjects2">
		<xsl:param name="obj_host"/>
		<xsl:param name="staticURL"/>
		<xsl:param name="layout"/>
		<xsl:param name="xmltempl"/>
		<xsl:variable select="substring-before(substring-after(./@ID,'_'),'_')" name="type"/>
		<xsl:variable name="suffix">
			
			<xsl:if test="string-length($layout)&gt;0">
				<xsl:value-of select="concat('&amp;layout=',$layout)"/>
			</xsl:if>
		</xsl:variable>
		<xsl:if test="./structure/derobjects">
			<tr>
				<td style="vertical-align:top;" class="metaname">
					<xsl:value-of select="i18n:translate('metaData.jpvolume.[derivates]')"/>
				</td>
				
				<td class="metavalue">
					<xsl:if test="$objectHost != 'local'">
						<a href="{$staticURL}"><xsl:value-of select="i18n:translate('metaData.origserver')"/></a>
					</xsl:if>
					<xsl:if test="$objectHost = 'local'">
						<xsl:for-each select="./structure/derobjects/derobject">
							<table cellpadding="0" cellspacing="0" border="0" width="100%">
								<tr>
									
									<td valign="top" align="left">
										<div class="derivateBox">
											<xsl:variable select="@xlink:href" name="deriv"/>
											<xsl:variable select="concat('mcrobject:',$deriv)" name="derivlink"/>
											<xsl:variable select="document($derivlink)" name="derivate"/>
											<xsl:apply-templates select="$derivate/mycorederivate/derivate/internals"/>
											<xsl:apply-templates select="$derivate/mycorederivate/derivate/externals"/>
										</div>
									</td>
									
									<xsl:if test="acl:checkPermission(./@ID,'writedb')">
										<td align="right" valign="top">
											<a
												href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;re_mcrid={../../../@ID}&amp;se_mcrid={@xlink:href}&amp;te_mcrid={@xlink:href}&amp;todo=saddfile{$suffix}{$xmltempl}">
												<img title="Datei hinzufügen"
													src="{$WebApplicationBaseURL}images/workflow_deradd.gif"/>
											</a>
											<a
												href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;re_mcrid={../../../@ID}&amp;se_mcrid={@xlink:href}&amp;te_mcrid={@xlink:href}&amp;todo=seditder{$suffix}{$xmltempl}">
												<img title="Derivat bearbeiten"
													src="{$WebApplicationBaseURL}images/workflow_deredit.gif"/>
											</a>
											<a
												href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?type={$type}&amp;re_mcrid={../../../@ID}&amp;se_mcrid={@xlink:href}&amp;te_mcrid={@xlink:href}&amp;todo=sdelder{$suffix}{$xmltempl}">
												
												<img title="Derivat löschen"
													src="{$WebApplicationBaseURL}images/workflow_derdelete.gif"/>
											</a>
										</td>
									</xsl:if>
								</tr>
							</table>
						</xsl:for-each>
					</xsl:if>
				</td>
				
			</tr>
		</xsl:if>
	</xsl:template>
	
	<!-- ===================================================================================================== -->

</xsl:stylesheet>