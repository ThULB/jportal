<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
	xmlns:dc="http://purl.org/dc/elements/1.1/">

  <xsl:param name="WebApplicationBaseURL" />

  <xsl:include href="coreFunctions.xsl" />
  <xsl:include href="oai/object2record.xsl" />

  <xsl:template match="mycoreobject" mode="metadata">
    <oai_dc:dc>
      <xsl:call-template name="identifier" />
      <xsl:call-template name="title" />
      <xsl:call-template name="description" />
      <xsl:call-template name="type" />
      <xsl:call-template name="creator" />
      <xsl:call-template name="created" />
      <xsl:call-template name="language" />
    </oai_dc:dc>
  </xsl:template>

  <xsl:template name="identifier">
    <xsl:choose>
      <xsl:when test="metadata/identis/identi">
        <xsl:for-each select="metadata/identis/identi">
          <dc:identifier>
            <xsl:if test="@type">
              <xsl:value-of select="concat(@type, ' : ')" />
            </xsl:if>
            <xsl:value-of select="." />
          </dc:identifier>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <dc:identifier>
          <xsl:value-of select="concat($WebApplicationBaseURL, 'receive/', @ID)" />
        </dc:identifier>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="title">
    <dc:title>
      <xsl:value-of select="metadata/maintitles/maintitle[@inherited=0]" />
    </dc:title>
  </xsl:template>

  <xsl:template name="description">
    <xsl:for-each select="metadata/abstracts/abstract">
      <dc:description>
        <xsl:value-of select="."></xsl:value-of>
      </dc:description>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="type">
    <xsl:element name="dc:type">
      <xsl:variable name="type">
        <xsl:call-template name="typeOfObjectID">
          <xsl:with-param name="id" select="@ID" />
        </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$type='jpjournal'">journal</xsl:when>
        <xsl:when test="$type='jpvolume'">volume</xsl:when>
        <xsl:when test="$type='jparticle'">article</xsl:when>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:template name="creator">
    <dc:creator>
      <xsl:value-of select="'Thüringer Universitäts- und Landesbibliothek'" />
    </dc:creator>
  </xsl:template>

  <xsl:template name="created">
    <xsl:for-each select="metadata/dates/date[@type='published' or @type='published_from']">
      <dc:date>
        <xsl:value-of select="." />
      </dc:date>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="language">
    <xsl:for-each select="metadata/languages/language">
      <dc:language>
        <xsl:value-of select="@categid" />
      </dc:language>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
