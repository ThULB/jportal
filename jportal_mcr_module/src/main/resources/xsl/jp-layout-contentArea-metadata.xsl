<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:ext="xalan://org.mycore.services.fieldquery.data2fields.MCRXSLBuilder"
  xmlns:encoder="xalan://java.net.URLEncoder" exclude-result-prefixes="i18n xsi xlink ext encoder">

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
  <xsl:variable name="simpleType" select="'MCRMetaLangText MCRMetaClassification MCRMetaXML MCRMetaInstitutionName MCRMetaISO8601Date'" />
  <xsl:variable name="ignore" select="'maintitles def.heading'" />

  <xsl:template mode="metadataDisplay" match="metadata/*[contains($ignore, name())]">
  </xsl:template>

  <xsl:template mode="metadataDisplay" match="metadata/*[contains($simpleType, @class) and not(contains($ignore, name()))]">
    <xsl:call-template name="metadataField">
      <xsl:with-param name="fields" select="*" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="metadataDisplay" match="metadata/*[*/@type and not(contains($ignore, name()))]">
    <xsl:variable name="currentTagName" select="name()" />
    <xsl:for-each select="*[generate-id(.)=generate-id(key($currentTagName, @type)[1])]">
      <xsl:call-template name="metadataField">
        <xsl:with-param name="fields" select="key($currentTagName, @type)" />
      </xsl:call-template>
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

  <xsl:template mode="metadataFieldLabel" match="*[../@class='MCRMetaLangText' or ../@class='MCRMetaXML' or ../@class='MCRMetaISO8601Date']">
    <xsl:variable name="tagName" select="name()" />
    <xsl:value-of select="i18n:translate($settings/i18n[@tag=$tagName])" />
  </xsl:template>

  <xsl:template mode="metadataFieldLabel" match="names[@class='MCRMetaInstitutionName']/name/*">
  </xsl:template>
  <xsl:template mode="metadataFieldLabel" match="names[@class='MCRMetaInstitutionName']/name/*[name() != 'fullname']">
    <xsl:variable name="tagName" select="name()" />
    <xsl:value-of select="i18n:translate($settings/i18n[@tag=$tagName])" />
    
    <xsl:message><xsl:value-of select="position()" /></xsl:message>
    <xsl:message><xsl:value-of select="last()" /></xsl:message>
    <xsl:message><xsl:value-of select="'--------------------'" /></xsl:message>
    
    <xsl:if test="position() != (last() - 1)">
      <xsl:value-of select="' &amp; '" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldLabel" match="*[@type and not(../@class='MCRMetaXML') and name()!='identifier']">
    <xsl:variable name="currentTagName" select="name()" />
    <xsl:variable name="datamodel" select="/mycoreobject/@xsi:noNamespaceSchemaLocation" />
    <xsl:variable name="classID" select="$settings/datamodel[contains(@type, $datamodel)]/class[@tag=$currentTagName]" />
    <xsl:variable name="categID" select="@type" />
    <xsl:choose>
      <xsl:when test="$classID and $categID">
        <xsl:value-of select="document(concat('classification:metadata:all:children:',$classID,':',$categID))/mycoreclass/categories/category[@ID=$categID]/label[@xml:lang=$CurrentLang]/@text" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat('could not be resolved (', name(), ')')" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaLangText']">
    <xsl:if test="@inherited='0'">
      <xsl:value-of select="text()" />
      <xsl:if test="position() != last()">
        <xsl:value-of select="'; '" />
      </xsl:if>
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
    <xsl:call-template name="printClass">
      <xsl:with-param name="nodes" select="." />
      <xsl:with-param name="host" select="'local'" />
      <xsl:with-param name="next" select="', '" />
    </xsl:call-template>
    <xsl:if test="position() != last()">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataFieldValue" match="*[../@class='MCRMetaISO8601Date']">
    <xsl:value-of select="ext:normalizeDate(string(text()))" />
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
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="position() != last()">
      <xsl:value-of select="'; '" />
    </xsl:if>
  </xsl:template>

  <!-- Linked metadata -->
  <xsl:template mode="linkedArticles" match="mycoreobject">
    <xsl:variable name="q" select="encoder:encode(concat('+objectType:jparticle +link:', @ID))" />
    <xsl:apply-templates select="document(concat('solr:q=',$q,'&amp;rows=6&amp;ref=', @ID, '&amp;mode=article'))/response" mode="linkedObjects.result" />
  </xsl:template>

  <xsl:template mode="linkedCalendar" match="mycoreobject">
    <xsl:variable name="q" select="encoder:encode(concat('+objectType:jpjournal +contentClassi1:calendar +link:', @ID))" />
    <xsl:apply-templates select="document(concat('solr:q=',$q,'&amp;rows=6&amp;ref=', @ID, '&amp;mode=calendar'))/response" mode="linkedObjects.result" />
  </xsl:template>

  <xsl:template mode="linkedObjects.result" match="/response[result/@numFound = 0]">
  </xsl:template>

  <xsl:template mode="linkedObjects.result" match="/response[result/@numFound &gt; 0]">
    <dt>
      <xsl:apply-templates mode="linkedObjects.result.label"  select="lst[@name = 'responseHeader']/lst[@name = 'params']" />
    </dt>
    <dd>
      <ul>
        <xsl:apply-templates mode="linkedObjects.result.list" select="result/doc" />
        <xsl:apply-templates mode="linkedObjects.result.more" select="." />
      </ul>
    </dd>
  </xsl:template>

  <xsl:template mode="linkedObjects.result.label" match="lst[contains(str[@name='ref'], '_person_')]">
    <xsl:value-of select="i18n:translate(concat('metaData.person.linked.', str[@name='mode']))" />
  </xsl:template>

  <xsl:template mode="linkedObjects.result.label" match="lst[contains(str[@name='ref'], '_jpinst_')]">
    <xsl:value-of select="i18n:translate('metaData.jpinst.linked')" />
  </xsl:template>

  <xsl:template mode="linkedObjects.result.list" match="doc">
    <xsl:variable name="objID" select="str[@name='id']" />
    <li>
      <a href="{$WebApplicationBaseURL}receive/{$objID}">
        <xsl:call-template name="shortenString">
          <xsl:with-param name="string" select="str[@name='maintitle']" />
          <xsl:with-param name="length" select="50" />
        </xsl:call-template>
      </a>
    </li>
  </xsl:template>

  <xsl:template mode="linkedObjects.result.more" match="response">
  </xsl:template>

  <xsl:template mode="linkedObjects.result.more" match="response[result/@numFound &gt; lst[@name = 'responseHeader']/lst[@name = 'params']/str[@name='rows']]">
    <li>
      <xsl:variable name="q" select="encoder:encode(lst[@name = 'responseHeader']/lst[@name = 'params']/str[@name='q'])" />
      <a href="{$WebApplicationBaseURL}jp-search.xml?XSL.qt={$q}">
        <xsl:value-of select="i18n:translate('metaData.person.linked.showAll')" />
        <xsl:value-of select="concat(' (', result/@numFound, ')')" />
      </a>
    </li>
  </xsl:template>

</xsl:stylesheet>
