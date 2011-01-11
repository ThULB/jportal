<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ===================================================================================================== -->
<!-- This stylesheet contains all templates to print derivates -->
<!-- ===================================================================================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
	xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xlink mcr i18n acl xalan" xmlns:layoutUtils="xalan://org.mycore.frontend.MCRLayoutUtilities">

	<xsl:variable name="readAccessForDerivates">
		<xsl:call-template name="get.readAccessForDerivates">
			<xsl:with-param name="jID" select="$journalID" />
		</xsl:call-template>
	</xsl:variable>

	<xsl:variable name="thumbnail">
		<xsl:call-template name="get.thumbnailSupport" />
	</xsl:variable>

	<!-- ===================================================================================================== -->

	<xsl:template match="internals | ifsLink | mcr:metaData" priority="2">
		<xsl:param name="objID" />
		<xsl:param name="objectXML" />
		<xsl:param name="detailed-view" />
		<xsl:if test="$objectHost = 'local'">
			<xsl:variable name="derivid">
				<xsl:choose>
					<!-- links -->
					<xsl:when test="name() = 'ifsLink'">
						<xsl:value-of select="substring-before(./text(),'/')" />
					</xsl:when>
					<!-- full text hit -->
					<xsl:when test="name() = 'mcr:metaData'">
						<xsl:value-of select="mcr:field[@name='DerivateID']/text()" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="../../@ID" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>

			<xsl:variable name="derivmain">
				<xsl:choose>
					<!-- links -->
					<xsl:when test="name() = 'ifsLink'">
						<xsl:value-of select="substring-after(./text(),'/')" />
					</xsl:when>
					<!-- full text hit -->
					<xsl:when test="name() = 'mcr:metaData'">
						<!-- <xsl:value-of select="mcr:field[@name='filePath']/text()" /> -->
						<!-- check if a file mapping has to be done -->
						<xsl:call-template name="mappFile">
							<xsl:with-param name="derivid-if" select="$derivid" />
							<xsl:with-param name="filePath" select="mcr:field[@name='filePath']/text()" />
							<xsl:with-param name="fileName" select="mcr:field[@name='fileName']/text()" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="internal/@maindoc" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>

			<xsl:variable name="derivbase">
				<xsl:value-of select="concat($ServletsBaseURL,'MCRFileNodeServlet/',$derivid,'/')" />
			</xsl:variable>
			<xsl:variable name="fileType">
				<xsl:call-template name="getFileType">
					<xsl:with-param name="fileName" select="$derivmain" />
				</xsl:call-template>
			</xsl:variable>
			<!-- IView available ? -->
			<xsl:variable name="supportedMainFile">
				<xsl:call-template name="iview.getSupport.hack">
					<xsl:with-param name="derivid_2" select="$derivid" />
					<xsl:with-param name="mainFile" select="$derivmain" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="href">
				<xsl:choose>
					<xsl:when test="$supportedMainFile != ''">
						<xsl:value-of select="$supportedMainFile" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<!-- remove double slash if exist -->
							<xsl:when test="substring($derivbase,string-length($derivbase),1) = '/' and substring($derivmain,1,1) = '/'">
								<xsl:value-of select="concat($derivbase,substring-after($derivmain,'/'))" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="concat($derivbase,$derivmain)" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="fileLabel">
				<xsl:call-template name="getFileLabel">
					<xsl:with-param name="typeOfFile" select="$fileType" />
				</xsl:call-template>
			</xsl:variable>
			<!-- access to edit ? -->
			<xsl:variable name="editAccess">
				<!-- <xsl:value-of select="acl:checkPermission($objID,'writedb') or acl:checkPermission($objID,'deletedb')" /> -->
				<xsl:choose>
					<xsl:when test="acl:checkPermission($objID,'writedb') or acl:checkPermission($objID,'deletedb')">
						<xsl:value-of select="'true'"></xsl:value-of>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="'false'"></xsl:value-of>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<!-- read access ? -->
			<xsl:variable name="readAccess4Derivates">
				<xsl:choose>
					<xsl:when test="$readAccessForDerivates = ''">
						<xsl:variable name="jourID">
							<xsl:value-of select="xalan:nodeset($objectXML)/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()" />
						</xsl:variable>
						<xsl:call-template name="get.readAccessForDerivates">
							<xsl:with-param name="jID" select="$jourID" />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$readAccessForDerivates = 'true' or $editAccess = 'true'">
						<xsl:value-of select="'true'"></xsl:value-of>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="'false'"></xsl:value-of>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>

			<xsl:choose>
				<xsl:when test="$thumbnail='true'">
					<table cellpadding="0" cellspacing="0" id="detailed-contenttable">
						<tr id="detailed-contentsimg1">
							<td id="detailed-contentsimgpadd">
								<xsl:choose>
									<xsl:when test="($supportedMainFile!='')">
										<xsl:call-template name="derivateView">
											<xsl:with-param name="derivateID" select="../../@ID" />
										</xsl:call-template>
										<!-- <xsl:call-template name="iview.getEmbedded.thumbnail"> <xsl:with-param name="derivID" select="$derivid" /> <xsl:with-param name="pathOfImage" 
											select="concat('/',$derivmain)" /> </xsl:call-template> -->
									</xsl:when>
									<xsl:otherwise>
										<img src="{concat($WebApplicationBaseURL,'images/dummyPreview.png')}" />
									</xsl:otherwise>
								</xsl:choose>
								<br />
							</td>
						</tr>
						<xsl:if test="$editAccess = 'true'">
							<tr id="detailed-contents">
								<td>
									<xsl:choose>
										<xsl:when test="$readAccess4Derivates = 'true'">
											<xsl:variable name="label">
												<xsl:choose>
													<xsl:when test="name() = 'ifsLink'">
														<xsl:value-of select="concat($fileLabel, '(~)')" />
													</xsl:when>
													<xsl:otherwise>
														<xsl:value-of select="$fileLabel" />
													</xsl:otherwise>
												</xsl:choose>
											</xsl:variable>
										</xsl:when>
										<xsl:otherwise>
											Zugriff gesperrt!
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text>
                                </xsl:text>
									<a href="{$derivbase}">
										<xsl:value-of select="'Details &gt;&gt; '" />
									</a>
								</td>
							</tr>
						</xsl:if>
					</table>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="$readAccess4Derivates = 'true'">
							<xsl:variable name="label">
								<xsl:choose>
									<xsl:when test="name()='ifsLink'">
										<xsl:value-of select="concat($fileLabel, '(~)')" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$fileLabel" />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<a href="{$href}">
								<xsl:value-of select="$label" />
							</a>
						</xsl:when>
						<xsl:otherwise>
							Zugriff gesperrt !
						</xsl:otherwise>
					</xsl:choose>

					<xsl:if test="$editAccess = 'true'">
						<a href="{$derivbase}">
							<xsl:value-of select="', Details &gt;&gt; '" />
						</a>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

	<!-- ===================================================================================================== -->

	<xsl:template name="mappFile">
		<xsl:param name="derivid-if" />
		<xsl:param name="filePath" />
		<xsl:param name="fileName" />

		<xsl:variable name="fileMappings" select="document('webapp:fileMappings.xml')" />

		<xsl:choose>
			<!-- file mapping(s) available ? -->
			<xsl:when test="$fileMappings/fileMappings/fileMapping">
				<!-- contains the mapped files separated by comma -->
				<xsl:variable name="transFileList">
					<!-- derivate root path -->
					<xsl:variable name="rootPath" select="substring-before($filePath,$fileName)" />
					<!-- file name without extension -->
					<xsl:variable name="fileNameWithoutExt" select="substring-before($fileName,'.')" />
					<xsl:variable name="derivXML" select="document(concat('ifs:',$derivid-if, $rootPath))" />
					<xsl:variable name="contentTypeId">
						<xsl:value-of select="$derivXML/mcr_directory/children/child/name[text()=$fileName]/../contentType" />
					</xsl:variable>
					<xsl:variable name="fileContentTypes" select="document('webapp:FileContentTypes.xml')" />
					<!-- file type exist AND file type must be mapped -->
					<xsl:if test="$fileMappings/fileMappings/fileMapping/type[@ID=$contentTypeId]">
						<!-- go through all mappable file extension id's -->
						<xsl:for-each select="$fileMappings/fileMappings/fileMapping[type/@ID=$contentTypeId]/mappTo/type">
							<xsl:variable name="mapId" select="@ID" />
							<xsl:for-each select="$derivXML/mcr_directory/children/child[starts-with(name, $fileNameWithoutExt)]">
								<xsl:if test="contentType = $mapId">
									<xsl:value-of select="concat(name, ',')" />
								</xsl:if>
							</xsl:for-each>
						</xsl:for-each>
					</xsl:if>
				</xsl:variable>

				<!-- return translated file -->
				<xsl:choose>
					<xsl:when test="$transFileList">
						<xsl:value-of select="substring-before($transFileList, ',')" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$filePath" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$filePath" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ===================================================================================================== -->

	<xsl:template name="getFileLabel">
		<xsl:param name="typeOfFile" />
		<xsl:variable name="label">
			<xsl:value-of select="document('webapp:FileContentTypes.xml')/FileContentTypes/type[rules/extension/text()=$typeOfFile]/label/text()" />
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$label = ''">
				<xsl:value-of select="concat(' ',i18n:translate('metaData.digitalisat'),' (',$typeOfFile,') ')" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$CurrentLang='de'">
						<xsl:value-of select="concat(' ',$label,' ',i18n:translate('metaData.digitalisat.show'),' ')" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(' ',i18n:translate('metaData.digitalisat.show'),' ',$label,' ')" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ===================================================================================================== -->
	<xsl:template name="iview.getSupport.hack">
		<xsl:param name="derivid_2" />
		<xsl:param name="mainFile" />

		<xsl:variable name="fileType">
			<xsl:call-template name="getFileType">
				<xsl:with-param name="fileName" select="$mainFile" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$fileType!=''">
				<xsl:choose>
					<xsl:when test="contains($MCR.Module-iview.SupportedContentTypes,$fileType)">
						<xsl:call-template name="iview.getAddress.hack">
							<xsl:with-param name="fullPathOfImage" select="concat($derivid_2,'/',$mainFile)" />
							<xsl:with-param name="height" select="'510'" />
							<xsl:with-param name="width" select="'605'" />
							<xsl:with-param name="scaleFactor" select="'fitToWidth'" />
							<xsl:with-param name="display" select="'extended'" />
							<xsl:with-param name="style" select="'image'" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="''" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="''" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ===================================================================================================== -->
	<xsl:template name="getFileType">
		<xsl:param name="fileName" />
		<xsl:value-of select="substring($fileName,number(string-length($fileName)-2))" />
	</xsl:template>
	<!-- ===================================================================================================== -->
	<xsl:template name="iview.getAddress.hack">
		<xsl:param name="fullPathOfImage" />
		<xsl:param name="height" />
		<xsl:param name="width" />
		<xsl:param name="scaleFactor" />
		<xsl:param name="display" />
		<xsl:param name="style" />
		<xsl:value-of select="'no more iview1'" />
		<!-- select="concat($iview.home,$fullPathOfImage,$HttpSession,'?mode=generateLayout&amp;XSL.MCR.Module-iview.navi.zoom.SESSION=',$scaleFactor,'&amp;XSL.MCR.Module-iview.display.SESSION=',$display,'&amp;XSL.MCR.Module-iview.style.SESSION=',$style,'&amp;XSL.MCR.Module-iview.lastEmbeddedURL.SESSION=',$lastEmbeddedURL,'&amp;XSL.MCR.Module-iview.embedded.SESSION=false&amp;XSL.MCR.Module-iview.move=reset')" 
			/> -->
	</xsl:template>


	<!-- ===================================================================================================== -->
	<xsl:template name="get.thumbnailSupport">
		<xsl:choose>
			<xsl:when test="/mycoreobject and $view.objectmetadata='false'">
				<xsl:value-of select="'true'" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'false'" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ===================================================================================================== -->

	<xsl:template name="get.readAccessForDerivates">
		<xsl:param name="jID" />
		<xsl:if test="$jID != ''">
			<xsl:value-of select="acl:checkPermission($jID,'read-derivates')" />
		</xsl:if>
	</xsl:template>


</xsl:stylesheet>