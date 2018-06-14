<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:jpxml="xalan://fsu.jportal.xml.JPXMLFunctions" xmlns:urn="xalan://fsu.jportal.urn.URNTools"
  exclude-result-prefixes="i18n xsi xlink mcrxml jpxml">

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
  <xsl:key name="abstracts" match="abstract[@inherited='0']" use="@type" />
  <xsl:key name="people" match="person[@inherited='0']" use="@type" />
  <xsl:key name="links" match="link[@inherited='0']" use="@type" />
  <xsl:key name="geographicCoordinates" match="link[@inherited='0']" use="@type" />

  <xsl:variable name="simpleType"
    select="'MCRMetaLangText MCRMetaClassification MCRMetaXML MCRMetaInstitutionName MCRMetaISO8601Date MCRMetaAddress MCRMetaLink MCRMetaSpatial JPMetaLocation JPMetaDate'" />

  <xsl:template mode="metadataDisplay" match="metadata/*">
  </xsl:template>

  <xsl:template mode="metadataURN" match="derivateLink">
    <xsl:variable name="derivID" select="substring-before(@xlink:href,'/')" />
    <xsl:variable name="filePath" select="concat('/',substring-after(@xlink:href,'/'))" />
    <xsl:variable name="urn" select="urn:getURNForFile($derivID,$filePath)" />
    <xsl:call-template name="metadataDisplayURNItem">
      <xsl:with-param name="urn" select="$urn" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="metadataURN" match="derobject">
    <xsl:variable name="derivID" select="@xlink:href" />
    <xsl:variable name="_filePath"
      select="document(concat('notnull:mcrobject:',$derivID))/mycorederivate/derivate/internals/internal/@maindoc" />
    <xsl:variable name="filePath">
      <xsl:choose>
        <xsl:when test="starts-with($_filePath,'/')">
          <xsl:value-of select="$_filePath" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat('/',$_filePath)" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="$filePath != ''">
      <xsl:variable name="urn" select="urn:getURNForFile($derivID,$filePath)" />
      <xsl:call-template name="metadataDisplayURNItem">
        <xsl:with-param name="urn" select="$urn" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="metadataDisplayURNItem">
    <xsl:param name="urn" />
    <xsl:if test="$urn != ''">
      <dt class="col-sm-3">
          URN
      </dt>
      <dd class="col-sm-9">
        <a href="{concat('http://nbn-resolving.de/urn/resolver.pl?urn=', $urn)}">
          <xsl:value-of select="$urn" />
        </a>
      </dd>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataDisplay" match="metadata/*[contains($simpleType, @class)]">
    <xsl:call-template name="metadataField">
      <xsl:with-param name="fields" select="*" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="metadataDisplay" match="metadata/*[*/@type]">
    <xsl:variable name="currentTagName" select="name()" />
    <xsl:for-each select="*[generate-id(.)=generate-id(key($currentTagName, @type)[1])]">
      <xsl:if test="not(($currentTagName='def.note' or $currentTagName='notes') and (@type='hidden' or @type='internalNote'))">
        <xsl:call-template name="metadataField">
          <xsl:with-param name="fields" select="key($currentTagName, @type)" />
        </xsl:call-template>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="metadataField">
    <xsl:param name="fields" />
    <dt class="col-sm-3 col-xs-4 jp-layout-metadataList-{name()}">
      <xsl:apply-templates mode="metadataFieldLabel" select="$fields[1]" />
    </dt>
    <dd class="col-sm-9 col-xs-8 jp-layout-metadataList-{name()}">
      <xsl:apply-templates mode="metadataFieldValue" select="$fields">
        <xsl:sort select="text()" />
        <xsl:sort select="@xlink:title" />
      </xsl:apply-templates>
    </dd>
  </xsl:template>

  <xsl:template mode="metadataFieldLabel"
                match="*[../@class='MCRMetaLangText' or ../@class='MCRMetaXML' or ../@class='MCRMetaISO8601Date' or
                         ../@class='MCRMetaLink' or ../@class='MCRMetaSpatial' or ../@class='JPMetaLocation']">
    <xsl:variable name="tagName" select="name()"/>
    <xsl:value-of select="i18n:translate($settings/i18n[@tag=$tagName])"/>
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

  <xsl:template mode="metadataFieldLabel" match="dates[@class='JPMetaDate']/date">
    <xsl:value-of select="i18n:translate(concat('metaData.date.', @type))" />
  </xsl:template>

  <xsl:template mode="metadataFieldLabel" match="*[@type and not(../@class='MCRMetaXML' or ../@class='JPMetaDate') and name()!='identifier']">
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
        <xsl:variable name="identifierURL">
          <xsl:apply-templates select="@type" mode="jp.metadata.identis.link" />
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$identifierURL != ''">
            <a href="{$identifierURL}">
              <xsl:value-of select="text()"/>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="text()"/>
          </xsl:otherwise>
        </xsl:choose>
      </p>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaLinkID']">
    <p class="jp-layout-metadata-list">
      <a href="{$WebApplicationBaseURL}receive/{@xlink:href}">
        <xsl:apply-templates mode="jp.metadata.name" select="document(concat('mcrobject:', @xlink:href))/mycoreobject" />
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
        <xsl:value-of select="jpxml:getClassificationLabel(@classid)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaClassification']">
    <xsl:variable name="classlink" select="concat('classification:metadata:0:children:',@classid,':',@categid)" />
    <xsl:call-template name="jp.printClass">
      <xsl:with-param name="nodes" select="document($classlink)/mycoreclass/categories/category" />
      <xsl:with-param name="lang" select="$CurrentLang" />
    </xsl:call-template>
    <xsl:if test="position() != last()">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaISO8601Date']">
    <xsl:call-template name="jp.date.print">
      <xsl:with-param name="date" select="." />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='JPMetaDate']">
    <xsl:value-of select="jpxml:formatJPMetaDate(., $CurrentLang)" />
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaLink' and @xlink:title]">
    <a href="{@xlink:href}" class="jp-layout-metadata-separated-list">
      <xsl:value-of select="@xlink:title" />
    </a>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaLink']">
    <a href="{@xlink:href}" class="jp-layout-metadata-separated-list">
      <xsl:value-of select="@xlink:href" />
    </a>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaXML']">
    <xsl:apply-templates mode="jp.metadata.person.name" select="." />
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

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaSpatial']">
    <xsl:variable name="modalTitle" select="i18n:translate('metaData.headlines.jpinst.geoCoordinates')" />
    <button class="btn btn-default jp-spatial-view-show-modal-button"
            data-modal-title="{$modalTitle}" data-content="{text()}" data-zoom="17" >
      <xsl:value-of select="i18n:translate('metaData.showSpatial')" />
    </button>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='JPMetaLocation']">
    <span class="jp-layout-metadata-separated-list">
      <xsl:choose>
        <xsl:when test="not(string(.))">
          <xsl:value-of select="concat(@label, ' (')" />
          <a href="https://d-nb.info/gnd/{@id}" target="_blank">
            <xsl:value-of select="@id" />
          </a>
          <xsl:value-of select="')'" />
        </xsl:when>
        <xsl:otherwise>
          <a href="javascript:void(0)" class="jp-spatial-view-show-modal-button" data-zoom="13"
             data-modal-title="{concat(@label, ' (', @id, ')')}" data-content="{text()}">
            <xsl:value-of select="@label" />
          </a>
          <xsl:value-of select="' ('" />
          <a href="https://d-nb.info/gnd/{@id}" target="_blank">
            <xsl:value-of select="@id" />
          </a>
          <xsl:value-of select="')'" />
        </xsl:otherwise>
      </xsl:choose>
    </span>
  </xsl:template>

</xsl:stylesheet>
