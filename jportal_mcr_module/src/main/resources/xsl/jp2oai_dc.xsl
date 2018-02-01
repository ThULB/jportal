<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions"
                exclude-result-prefixes="xalan jpxml xlink">

  <xsl:param name="WebApplicationBaseURL" />

  <xsl:include href="object2record.xsl" />

  <xsl:template match="mycoreobject" mode="metadata">
    <oai_dc:dc>
      <xsl:call-template name="identifier" />
      <xsl:call-template name="title" />
      <xsl:call-template name="subject"/>
      <xsl:call-template name="description" />
      <xsl:call-template name="type" />
      <xsl:call-template name="format" />
      <xsl:call-template name="creator" />
      <xsl:call-template name="publisher"/>
      <xsl:call-template name="contributor" />
      <xsl:call-template name="published"/>
      <xsl:call-template name="language" />
      <xsl:call-template name="coverage"/>
      <xsl:call-template name="relation"/>
      <xsl:call-template name="rights"/>
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
      <xsl:value-of select="jpxml:getTitle(@ID)"/>
    </dc:title>
  </xsl:template>

  <xsl:template name="subject">
    <!-- keywords -->
    <xsl:for-each select="metadata/keywords/keyword">
      <dc:subject>
        <xsl:value-of select="text()"/>
      </dc:subject>
    </xsl:for-each>
    <!-- ddc -->
    <xsl:for-each select="metadata/types/type[@classid='jportal_class_00000003']">
      <dc:subject>
        <xsl:value-of select="concat('ddc:', @categid)"/>
      </dc:subject>
    </xsl:for-each>
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
      <xsl:variable name="type" select="substring-before(substring-after(@ID,'_'),'_')"/>
      <xsl:choose>
        <xsl:when test="$type='jpjournal'">journal</xsl:when>
        <xsl:when test="$type='jpvolume'">volume</xsl:when>
        <xsl:when test="$type='jparticle'">article</xsl:when>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:template name="format">
    <xsl:variable name="type" select="substring-before(substring-after(@ID,'_'),'_')"/>
    <xsl:if test="$type = 'jparticle'">
      <xsl:element name="dc:format">
        <xsl:value-of select="concat('SizeOrDuration ', metadata/sizes/size)" />
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template name="creator">
    <xsl:variable name="creator" select="jpxml:getCreator(@ID)"/>
    <xsl:if test="$creator">
      <dc:creator>
        <xsl:value-of select="$creator"/>
      </dc:creator>
    </xsl:if>
  </xsl:template>

  <xsl:template name="publisher">
    <xsl:variable name="creator" select="jpxml:getCreator(@ID)"/>
    <xsl:variable name="publisher" select="jpxml:getPublisher(@ID)"/>
    <xsl:if test="$publisher and $creator != $publisher">
      <dc:publisher>
        <xsl:value-of select="$publisher"/>
      </dc:publisher>
    </xsl:if>
  </xsl:template>

  <xsl:template name="contributor">
    <xsl:for-each select="metadata/participants/participant">
      <xsl:if test="not(@type = 'mainAuthor' or @type = 'author' or @type = 'mainPublisher' or @type = 'publisher')">
        <xsl:variable name="name" select="jpxml:getTitle(@xlink:href)" />
        <xsl:variable name="role" select="jpxml:getMarcRelatorText(@type)" />
        <dc:contributor>
          <xsl:value-of select="concat($name, ' (', $role, ')')" />
        </dc:contributor>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="published">
    <xsl:variable name="published" select="jpxml:getPublishedISODate(@ID)"/>
    <xsl:if test="$published">
      <dc:date>
        <xsl:value-of select="$published"/>
      </dc:date>
    </xsl:if>
  </xsl:template>

  <xsl:template name="language">
    <xsl:variable name="language" select="jpxml:getLanguage(@ID)"/>
    <xsl:if test="$language">
      <dc:language>
        <xsl:value-of select="$language"/>
      </dc:language>
    </xsl:if>
  </xsl:template>

  <xsl:template name="coverage">
    <xsl:variable name="type" select="substring-before(substring-after(@ID,'_'),'_')"/>
    <xsl:if test="$type = 'jpjournal' and metadata/dates/date[@type='published_from']">
      <dc:coverage>
        <xsl:value-of select="metadata/dates/date[@type='published_from']"/>
        <xsl:value-of select="'-'"/>
        <xsl:if test="metadata/dates/date[@type='published_until']">
          <xsl:value-of select="metadata/dates/date[@type='published_until']"/>
        </xsl:if>
      </dc:coverage>
    </xsl:if>
  </xsl:template>

  <xsl:template name="relation">
    <xsl:variable name="type" select="substring-before(substring-after(@ID,'_'),'_')"/>
    <xsl:if test="$type != 'jpjournal'">
      <dc:relation>
        <xsl:value-of select="concat('IsPartOf ', concat($WebApplicationBaseURL, 'receive/', metadata/hidden_jpjournalsID/hidden_jpjournalID))"/>
      </dc:relation>
    </xsl:if>
  </xsl:template>

  <xsl:template name="rights">
    <xsl:variable name="rights" select="jpxml:getAccessRights(@ID)"/>
    <xsl:if test="$rights">
      <dc:rights>
        <xsl:value-of select="$rights"/>
      </dc:rights>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
