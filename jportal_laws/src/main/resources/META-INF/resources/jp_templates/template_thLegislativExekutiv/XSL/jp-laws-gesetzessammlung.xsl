<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:laws="xalan://fsu.jportal.laws.common.xml.LawsXMLFunctions"
  exclude-result-prefixes="i18n laws">

  <!-- Entry point to print all laws of a register -->
  <xsl:template match="/gesetzessammlung" mode="template_thLegislativExekutiv">
    <xsl:param name="objId" />
    <xsl:param name="derivateId" />

    <div id="jp-content-laws" class="jp-content-laws">
      <xsl:apply-templates select="register" mode="template_thLegislativExekutiv">
        <xsl:with-param name="objId" select="$objId"/>
        <xsl:with-param name="derivateId" select="$derivateId"/>
      </xsl:apply-templates>
    </div>
  </xsl:template>

  <!-- ================================================================================= -->

  <!-- Print the head of the register and prints the laws -->
  <xsl:template match="register" mode="template_thLegislativExekutiv">
    <xsl:param name="objId" />
    <xsl:param name="derivateId" />

    <!-- Print head -->
    <table class="register">
      <tr>
        <td><b><xsl:value-of select="i18n:translate('jp.laws.register.titel')" />:</b></td>
        <td><xsl:value-of select="titel" /></td>
      </tr>
      <tr>
        <td><b><xsl:value-of select="i18n:translate('jp.laws.register.year')" />:</b></td>
        <td>
          <xsl:choose>
            <xsl:when test="jahresangabe/bis">
              <xsl:value-of select="concat(jahresangabe/von, ' - ', jahresangabe/von)" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="jahresangabe/von" />
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
      <xsl:if test="herzogtum">
        <tr>
          <td><b><xsl:value-of select="i18n:translate('jp.laws.register.territory')" />:</b></td>
          <td><xsl:value-of select="herzogtum" /></td>
        </tr>
      </xsl:if>
    </table>

    <!-- Print laws -->
    <xsl:apply-templates select="gesetze" mode="template_thLegislativExekutiv">
        <xsl:with-param name="objId" select="$objId"/>
        <xsl:with-param name="derivateId" select="$derivateId"/>
    </xsl:apply-templates>

  </xsl:template>

  <!-- ================================================================================= -->

  <xsl:template match="gesetze" mode="template_thLegislativExekutiv">
    <xsl:param name="objId" />
    <xsl:param name="derivateId" />

    <table class="laws">
      <tr>
        <th><xsl:value-of select="i18n:translate('jp.laws.register.nr')" /></th>
        <th><xsl:value-of select="i18n:translate('jp.laws.register.page')" /></th>
        <th><xsl:value-of select="i18n:translate('jp.laws.register.decree')" /></th>
        <th><xsl:value-of select="i18n:translate('jp.laws.register.dateofissue')" /></th>
        <th><xsl:value-of select="i18n:translate('jp.laws.register.content')" /></th>
      </tr>
      <xsl:for-each select="gesetz">
        <tr>
          <xsl:apply-templates select="." mode="template_thLegislativExekutiv">
            <xsl:with-param name="objId" select="$objId"/>
            <xsl:with-param name="derivateId" select="$derivateId"/>
          </xsl:apply-templates>
        </tr>
      </xsl:for-each>    
    </table>
  </xsl:template>

  <xsl:template match="gesetz" mode="template_thLegislativExekutiv">
    <xsl:param name="objId" />
    <xsl:param name="derivateId" />

    <xsl:variable name="image" select="laws:getImageByLaw(nummer, $derivateId)" />

    <td class="nr"><xsl:value-of select="nummer" /></td>
    <td class="page">
      <xsl:choose>
        <xsl:when test="seite/bis != seite/von">
          <xsl:value-of select="concat(seite/von, ' - ', seite/bis)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="seite/von" />
        </xsl:otherwise>
      </xsl:choose>
    </td>
    <td class="decree"><xsl:value-of select="datum/erlass" /></td>
    <td class="issuedate"><xsl:value-of select="datum/ausgabe" /></td>
    <td class="content">
      <xsl:choose>
        <xsl:when test="$image">
          <a href="{$WebApplicationBaseURL}rsc/viewer/{$derivateId}/{$image}">
            <xsl:value-of select="inhalt" />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="inhalt" />
        </xsl:otherwise>
      </xsl:choose>
    </td>
  </xsl:template>

</xsl:stylesheet>