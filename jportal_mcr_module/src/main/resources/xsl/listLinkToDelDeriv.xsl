<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="http://www.mycore.org/"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan"
	xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions">
	<xsl:param name="WebApplicationBaseURL" />
	<xsl:output method="html" />
	<xsl:template match="/listLinkToDelDeriv">
		<html>
			<head>
			</head>
			<body>
				<xsl:variable name="searchQuery" xmlns:encoder="xalan://java.net.URLEncoder">
					<xsl:value-of select="encoder:encode('linkDerivExist = true')" />
				</xsl:variable>
				<xsl:variable name="resultXML" select="document(concat('query:term=',$searchQuery))" />
				<xsl:apply-templates select="$resultXML" />
			</body>
		</html>
	</xsl:template>

	<xsl:template match="/mcr:results">
		<table>
			<tr>
				<th>Object with dead Link</th>
				<th>linked to</th>
			</tr>
			<xsl:for-each select="mcr:hit">
				<xsl:variable name="mcrObjXML" select="document(concat('mcrobject:',@id))" />
				<xsl:variable name="deadLinks">
					<table>
						<xsl:for-each select="$mcrObjXML/mycoreobject/metadata/derivateLinks/derivateLink">
							<xsl:variable name="derivID" select="substring-before(@xlink:href,'/')" />
							<xsl:variable name="isDeadLink">
								<xsl:choose>
									<xsl:when test="not(mcrxml:exists($derivID))">
										<xsl:value-of select="concat(@xlink:href, ' dead link')" />
									</xsl:when>
									<xsl:when test="document(concat('mcrobject:',$derivID))/mycorederivate/service/servflags/servflag[text() = 'deleted']">
										<a href="{concat($WebApplicationBaseURL,'servlets/MCRFileNodeServlet/',$derivID)}">
											<xsl:value-of select="@xlink:href" />
										</a>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="'false'" />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:if test="$isDeadLink != 'false'">
								<tr>
									<td>
										<xsl:copy-of select="$isDeadLink" />
									</td>
								</tr>
							</xsl:if>
						</xsl:for-each>
					</table>
				</xsl:variable>
				<xsl:if test="xalan:nodeset($deadLinks)/table/tr">
					<tr>
						<td>
							<a href="{concat($WebApplicationBaseURL,'receive/',@id)}">
								<xsl:value-of select="@id" />
							</a>
						</td>
						<td>
							<xsl:copy-of select="$deadLinks" />
						</td>
					</tr>
				</xsl:if>
			</xsl:for-each>
		</table>
	</xsl:template>
</xsl:stylesheet>