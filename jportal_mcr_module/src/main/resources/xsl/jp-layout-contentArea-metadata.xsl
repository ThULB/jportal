<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:ext="xalan://org.mycore.services.fieldquery.data2fields.MCRXSLBuilder" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  exclude-result-prefixes="i18n xsi xlink ext mcrxml">

  <xsl:key name="subtitles" match="subtitle[@inherited='0']" use="@type" />
  <xsl:key name="identis" match="identi[@inherited='0']" use="@type" />
  <xsl:key name="notes" match="note[@inherited='0']" use="@type" />
  <xsl:key name="participants" match="participant[@inherited='0']" use="@type" />
  <xsl:key name="dates" match="date[@inherited='0']" use="@type" />
  <xsl:key name="traditions" match="tradition[@inherited='0']" use="@type" />
  <xsl:key name="collationNotes" match="collationNote[@inherited='0']" use="@type" />
  <xsl:key name="def.alternative" match="alternative[@inherited='0']" use="@type" />
  <xsl:key name="def.contact" match="contact[@inherited='0']" use="@type" />
  <xsl:key name="def.identifier" match="identifier[@inherited='0']" use="@type" />
  <xsl:key name="def.note" match="note[@inherited='0']" use="@type" />
  <xsl:key name="phones" match="phone[@inherited='0']" use="@type" />
  <xsl:key name="identifiers" match="identifier[@inherited='0']" use="@type" />

  <xsl:variable name="simpleType"
    select="'MCRMetaLangText MCRMetaClassification MCRMetaXML MCRMetaInstitutionName MCRMetaISO8601Date MCRMetaAddress MCRMetaLink'" />

  <xsl:template mode="metadataDisplay" match="metadata/*">
  </xsl:template>

  <xsl:template mode="metadataDisplay" match="metadata/*[contains($simpleType, @class)]">
    <xsl:call-template name="metadataField">
      <xsl:with-param name="fields" select="*" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="metadataDisplay" match="metadata/*[*/@type]">
    <xsl:variable name="currentTagName" select="name()" />
    <xsl:variable name="isGuest" select="mcrxml:isCurrentUserGuestUser()" />
    <xsl:for-each select="*[generate-id(.)=generate-id(key($currentTagName, @type)[1])]">
      <xsl:if test="not($currentTagName='def.note' and @type='hidden' and $isGuest)">
        <xsl:call-template name="metadataField">
          <xsl:with-param name="fields" select="key($currentTagName, @type)" />
        </xsl:call-template>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="metadataField">
    <xsl:param name="fields" />
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

  <xsl:template mode="metadataFieldLabel"
    match="*[../@class='MCRMetaLangText' or ../@class='MCRMetaXML' or ../@class='MCRMetaISO8601Date' or ../@class='MCRMetaLink']">
    <xsl:variable name="tagName" select="name()" />
    <xsl:value-of select="i18n:translate($settings/i18n[@tag=$tagName])" />
  </xsl:template>

  <xsl:template mode="metadataFieldLabel" match="names[@class='MCRMetaInstitutionName']/name/*">
  </xsl:template>
  <xsl:template mode="metadataFieldLabel" match="names[@class='MCRMetaInstitutionName']/name/*[name() != 'fullname']">
    <xsl:variable name="tagName" select="name()" />
    <xsl:value-of select="i18n:translate($settings/i18n[@tag=$tagName])" />
    <xsl:if test="position() != (last() - 1)">
      <xsl:value-of select="' &amp; '" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldLabel" match="addresses[@class='MCRMetaAddress']/address">
    <xsl:value-of select="i18n:translate($settings/i18n[@tag='address'])" />
  </xsl:template>

  <xsl:template mode="metadataFieldLabel" match="*[@type and not(../@class='MCRMetaXML') and name()!='identifier']">
    <xsl:variable name="currentTagName" select="name()" />
    <xsl:variable name="datamodel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
    <xsl:variable name="classID" select="$settings/datamodel[contains(@type, $datamodel)]/class[@tag=$currentTagName]" />
    <xsl:variable name="categID" select="@type" />
    <xsl:choose>
      <xsl:when test="$classID and $categID">
        <xsl:value-of
          select="document(concat('classification:metadata:all:children:',$classID,':',$categID))/mycoreclass/categories/category[@ID=$categID]/label[@xml:lang=$CurrentLang]/@text" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="tagName" select="concat(name(), '.', @type)" />
        <xsl:value-of select="i18n:translate($settings/i18n[@tag=$tagName])" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaLangText']">
    <xsl:if test="@inherited='0'">
      <p class="jp-layout-metadata-list">
        <xsl:value-of select="text()" />
      </p>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaLangText' and @type = 'doi']">
    <xsl:if test="@inherited='0'">
      <p class="jp-layout-metadata-list">
        <a href="{text()}">
          <xsl:value-of select="text()" />
        </a>
      </p>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaLinkID']">
    <p class="jp-layout-metadata-list">
      <a href="{$WebApplicationBaseURL}receive/{@xlink:href}">
        <xsl:value-of select="@xlink:title" />
      </a>
    </p>
  </xsl:template>

  <xsl:template mode="metadataFieldLabel" match="identifier[@type]">
    <xsl:value-of select="@type" />
  </xsl:template>

  <xsl:template mode="metadataFieldLabel" match="*[../@class='MCRMetaClassification']">
    <xsl:variable name="tagName" select="name()" />

    <xsl:choose>
      <xsl:when test="$settings/i18n[@tag=$tagName]">
        <xsl:value-of select="i18n:translate($settings/i18n[@tag=$tagName])" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="document(concat('jportal_getClassLabel:getDirectely:',@classid))//label/text()" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaClassification']">
    <xsl:variable name="classlink" select="concat('classification:metadata:0:children:',@classid,':',@categid)" />
    <xsl:call-template name="jp.printClass">
      <xsl:with-param name="nodes" select="document($classlink)/mycoreclass/categories/category" />
    </xsl:call-template>
    <xsl:if test="position() != last()">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaISO8601Date']">
    <xsl:value-of select="ext:normalizeDate(string(text()))" />
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaLink']">
    <a href="{@xlink:href}">
      <xsl:value-of select="concat(@xlink:href, ' (', @xlink:title, ')')" />
    </a>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaXML']">
    <xsl:apply-templates mode="metadataPersName" select="." />
    <xsl:if test="position() != last()">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="names[@class='MCRMetaInstitutionName']/name/fullname">
  </xsl:template>
  <xsl:template mode="metadataFieldValue" match="names[@class='MCRMetaInstitutionName']/name/*[not(name() = 'fullname')]">
    <xsl:variable name="tagName" select="name()" />
    <xsl:value-of select="." />
    <xsl:if test="position() != (last() - 1)">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="addresses[@class='MCRMetaAddress']/address">
    <dl class="address">
      <xsl:apply-templates select="*" mode="addressValue" />
    </dl>
  </xsl:template>
  <xsl:template match="*" mode="addressValue">
    <xsl:variable name="tagName" select="name()" />
    <dt>
      <xsl:value-of select="i18n:translate($settings/i18n[@tag=$tagName])" />
    </dt>
    <dd>
      <xsl:value-of select="." />
    </dd>
  </xsl:template>

  <xsl:template mode="metadataPersName" match="heading">
    <xsl:value-of select="concat(firstName,' ', lastName)" />
  </xsl:template>

  <xsl:template mode="metadataPersName" match="alternative">
    <xsl:choose>
      <xsl:when test="@type='complete'">
        <xsl:value-of select="concat(firstName,' ', lastName)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="name" />
        <xsl:value-of select="personalName" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="position() != last()">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
