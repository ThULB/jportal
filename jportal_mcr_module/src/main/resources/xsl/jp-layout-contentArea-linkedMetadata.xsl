<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="encoder i18n">

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
      <xsl:apply-templates mode="linkedObjects.result.label" select="lst[@name = 'responseHeader']/lst[@name = 'params']" />
    </dt>
    <dd class="linked">
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
      <a href="{$WebApplicationBaseURL}receive/{$objID}" class="jp-layout-clickLabel">
        <xsl:call-template name="shortenString">
          <xsl:with-param name="string" select="str[@name='maintitle']" />
          <xsl:with-param name="length" select="50" />
        </xsl:call-template>
      </a>
      <xsl:call-template name="resultListBreadcrumb">
        <xsl:with-param name="objID" select="$objID" />
      </xsl:call-template>
    </li>
  </xsl:template>

  <xsl:template mode="linkedObjects.result.more" match="response">
  </xsl:template>

  <xsl:template mode="linkedObjects.result.more" match="response[result/@numFound &gt; lst[@name = 'responseHeader']/lst[@name = 'params']/str[@name='rows']]">
    <li>
      <xsl:variable name="q" select="encoder:encode(lst[@name = 'responseHeader']/lst[@name = 'params']/str[@name='q'])" />
      <a href="{$WebApplicationBaseURL}servlets/solr/select?q={$q}&amp;XSL.returnURL={$RequestURL}">
        <xsl:value-of select="i18n:translate('metaData.person.linked.showAll')" />
        <xsl:value-of select="concat(' (', result/@numFound, ')')" />
      </a>
    </li>
  </xsl:template>

</xsl:stylesheet>