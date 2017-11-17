<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcrurn="xalan://fsu.jportal.urn.URNTools"
                exclude-result-prefixes="xsl xlink mets mods xalan mcrurn" version="1.0">
  <xsl:output method="xml" encoding="utf-8" />
  <xsl:param name="MCR.Module-iview2.SupportedContentTypes" />
  <xsl:param name="WebApplicationBaseURL" />

  <xsl:param name="derivateID" select="substring-after(/mets:mets/mets:dmdSec/@ID,'_')" />
  <xsl:param name="objectID" />

  <!-- this is where the master file group is located (files that are referenced by a relative URL) -->
  <xsl:variable name="masterFileGrp"
    select="/mets:mets/mets:fileSec/mets:fileGrp[@USE = 'MASTER']" />

  <xsl:variable name="urns" select="mcrurn:getURNsForMCRID($derivateID)" />

  <!-- - - - - - - - - Identity Transformation - - - - - - - - - -->
  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mets:fileSec">
    <!-- <xsl:copy-of select="mcrurn:getURNsForMCRID($derivateID)"/> -->
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:call-template name="generateIViewURLS">
        <xsl:with-param name="use" select="'MAX'" />
        <xsl:with-param name="zoom" select="'MAX'" />
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="generateIViewURLS">
    <xsl:param name="use" />
    <xsl:param name="zoom" select="''" />
    <mets:fileGrp USE="{$use}">
      <xsl:for-each select="$masterFileGrp/mets:file">
        <xsl:variable name="ncName">
          <xsl:choose>
            <xsl:when test="contains(@ID,'_')">
              <xsl:value-of select="substring-after(@ID,'_')" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@ID" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <mets:file ID="{concat($use,'_',$ncName)}" MIMETYPE="image/jpeg">
          <mets:FLocat LOCTYPE="URL" xlink:href="{concat($ImageBaseURL,$zoom,'/',$derivateID,'/',mets:FLocat/@xlink:href)}" />
        </mets:file>
      </xsl:for-each>
    </mets:fileGrp>
  </xsl:template>

  <xsl:template match="mets:div[$masterFileGrp/mets:file/@ID=mets:fptr/@FILEID]">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:attribute name="ORDER">
        <xsl:value-of select="count(preceding-sibling::mets:div)+1" />
      </xsl:attribute>
      <xsl:variable name="fileID" select="mets:fptr[$masterFileGrp/mets:file/@ID=@FILEID]/@FILEID" />
      <xsl:variable name="file" select="$masterFileGrp/mets:file[@ID=$fileID]/mets:FLocat/@xlink:href" />
      <xsl:variable name="filePath">
        <!-- remove leading "./" from relative URL if present -->
        <xsl:choose>
          <xsl:when test="substring($file, 1, 2) = './'">
            <xsl:value-of select="substring($file, 3)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$file" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="urn" select="$urns/file[@name=$filePath]" />
      <xsl:if test="$urn">
        <!-- merge urn in PHYSICAL structMap -->
        <xsl:attribute name="CONTENTIDS">
          <xsl:value-of select="$urn/@urn" />
        </xsl:attribute>
      </xsl:if>
      <xsl:variable name="ncName">
        <xsl:choose>
          <xsl:when test="contains($fileID,'_')">
            <xsl:value-of select="substring-after($fileID,'_')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$fileID" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <mets:fptr FILEID="{concat('MAX_',$ncName)}" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>