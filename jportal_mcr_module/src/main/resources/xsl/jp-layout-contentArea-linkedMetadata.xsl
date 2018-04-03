<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="encoder i18n">

  <!-- mode parameter in solr query is used for i18n in linkedObjects.result.label templates -->
  <xsl:template mode="linkedArticles" match="mycoreobject">
    <xsl:variable name="q" select="encoder:encode(concat('+objectType:jparticle +link:', @ID))" />
    <xsl:apply-templates select="document(concat('solr:q=',$q,'&amp;rows=6&amp;ref=', @ID, '&amp;mode=article'))/response" mode="linkedObjects.result" />
  </xsl:template>

  <xsl:template mode="linkedCalendar" match="mycoreobject">
    <xsl:variable name="q" select="encoder:encode(concat('+objectType:jpjournal +contentClassi1:calendar +link:', @ID))" />
    <xsl:apply-templates select="document(concat('solr:q=',$q,'&amp;rows=6&amp;ref=', @ID, '&amp;mode=calendar'))/response" mode="linkedObjects.result" />
  </xsl:template>

  <xsl:template mode="linkedJournals" match="mycoreobject">
    <xsl:variable name="q" select="encoder:encode(concat('+objectType:jpjournal -contentClassi1:calendar +link:', @ID))" />
    <xsl:apply-templates select="document(concat('solr:q=',$q,'&amp;rows=6&amp;ref=', @ID, '&amp;mode=journal'))/response" mode="linkedObjects.result" />
  </xsl:template>

  <xsl:template mode="linkedObjects.result" match="/response[result/@numFound = 0]">
  </xsl:template>

  <xsl:template mode="linkedObjects.result" match="/response[result/@numFound &gt; 0]">
    <dt class="col-sm-3">
      <xsl:apply-templates mode="linkedObjects.result.label" select="lst[@name = 'responseHeader']/lst[@name = 'params']" />
    </dt>
    <dd class="col-sm-9 linked">
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
    <xsl:value-of select="i18n:translate(concat('metaData.jpinst.linked.', str[@name='mode']))" />
  </xsl:template>

  <xsl:template mode="linkedObjects.result.list" match="doc">
    <xsl:variable name="objID" select="str[@name='id']" />
    <li>
      <ol class="jp-layout-hit-breadcrumb breadcrumb">
        <li>
          <b>
            <a href="{$WebApplicationBaseURL}receive/{$objID}">
              <xsl:call-template name="shortenString">
                <xsl:with-param name="string" select="str[@name='maintitle']" />
                <xsl:with-param name="length" select="50" />
              </xsl:call-template>
            </a>
          </b>
        </li>
        <xsl:apply-templates mode="jp.printListEntry" select="document(concat('parents:',$objID))/parents/parent" />
      </ol>
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