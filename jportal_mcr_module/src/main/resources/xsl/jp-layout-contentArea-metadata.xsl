<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink">

  <xsl:key name="subtitles" match="subtitle" use="@type" />
  <xsl:key name="identis" match="identi" use="@type" />
  <xsl:key name="notes" match="note" use="@type" />
  <xsl:key name="participants" match="participant" use="@type" />
  <xsl:key name="dates" match="date[@inherited='0']" use="@type" />
  <xsl:key name="traditions" match="tradition" use="@type" />
  <xsl:key name="collationNotes" match="collationNote" use="@type" />

  <xsl:template mode="metadataDisplay" match="metadata/*[@class='MCRMetaLangText']">
    <xsl:call-template name="metadataField">
      <xsl:with-param name="fields" select="*"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template mode="metadataDisplay" match="metadata/*[*/@type]">
    <xsl:variable name="currentTagName" select="name()" />
    <xsl:for-each select="*[generate-id(.)=generate-id(key($currentTagName, @type)[1])]">
      <xsl:call-template name="metadataField">
        <xsl:with-param name="fields" select="key($currentTagName, @type)" />
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="metadataField">
    <xsl:param name="fields"/>
    <dt>
      <xsl:apply-templates mode="metadataFieldLabel" select="$fields[1]" />
    </dt>
    <dd>
      <xsl:apply-templates mode="metadataFieldValue" select="$fields">
        <xsl:sort select="text()" />
        <xsl:sort select="@xlink:title" />
      </xsl:apply-templates>
    </dd>
  </xsl:template>

  <xsl:template mode="metadataFieldLabel" match="*[../@class='MCRMetaLangText']">
    <xsl:variable name="tagName" select="name()"/>
    <xsl:value-of select="concat(i18n:translate($settings/i18n[@tag=$tagName]), ': ')" />
  </xsl:template>
  
  <xsl:template mode="metadataFieldLabel" match="*[@type]">
    <xsl:variable name="currentTagName" select="name()" />
    <xsl:variable name="datamodel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
    <xsl:variable name="classID" select="$settings/datamodel[contains(@type, $datamodel)]/class[@tag=$currentTagName]" />
    <xsl:variable name="categID" select="@type" />
      <xsl:variable name="label"
        select="document(concat('classification:metadata:all:children:',$classID,':',$categID))/mycoreclass/categories/category[@ID=$categID]/label[@xml:lang=$CurrentLang]/@text" />
    <xsl:value-of select="concat($label, ': ')" />
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaLangText']|*[../@class='MCRMetaISO8601Date']">
    <xsl:value-of select="text()" />
    <xsl:if test="position() != last()">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaLinkID']">
    <a href="{$WebApplicationBaseURL}receive/{@xlink:href}">
      <xsl:value-of select="@xlink:title" />
    </a>
    <xsl:if test="position() != last()">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>