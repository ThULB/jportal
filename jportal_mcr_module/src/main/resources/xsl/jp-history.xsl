<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="xlink mcrxsl i18n">

  <xsl:param name="id" />
  <xsl:param name="objectUrl" select="concat($WebApplicationBaseURL, 'receive/', $id)"/>

  <xsl:template match="history" >
    <xsl:variable name="verinfo" select="document(concat('notnull:versioninfo:',$id))" />
    <xsl:choose>
      <xsl:when test="$verinfo/versions">
        <p>
          <a href="{$objectUrl}"><xsl:value-of select="i18n:translate('metaData.back')" /></a>
        </p>
        <xsl:call-template name="printVersionInfo">
          <xsl:with-param name="verinfo" select="$verinfo" />
        </xsl:call-template>
        <p>
          <a href="{$objectUrl}"><xsl:value-of select="i18n:translate('metaData.back')" /></a>
        </p>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="i18n:translate('metaData.history.noobject', $id)"></xsl:value-of>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="printVersionInfo">
    <xsl:param name="verinfo" />
    <table class="versioninfo">
      <colgroup>
        <col width="65" />
        <col width="80" />
        <col width="80" />
        <col width="165" />
        <col width="*" />
      </colgroup>
      <!-- head -->
      <tr>
        <th><xsl:value-of select="i18n:translate('metaData.versions.version')" /></th>
        <th><xsl:value-of select="i18n:translate('metaData.versions.revision')" /></th>
        <th><xsl:value-of select="i18n:translate('metaData.versions.action')" /></th>
        <th><xsl:value-of select="i18n:translate('metaData.versions.date')" /></th>
        <th><xsl:value-of select="i18n:translate('metaData.versions.user')" /></th>
      </tr>
      <!-- body -->
      <xsl:for-each select="$verinfo/versions/version">
        <xsl:sort order="descending" select="position()" data-type="number" />
        <tr>
          <td class="ver">
            <xsl:number level="single" format="1."/>
          </td>
          <td class="rev">
            <xsl:if test="@r">
              <xsl:choose>
                <xsl:when test="@action='D'">
                  <xsl:value-of select="@r" />
                </xsl:when>
                <xsl:when test="position() = 1">
                  <a href="{$objectUrl}">
                    <xsl:value-of select="i18n:translate('metaData.versions.current')" />
                    <xsl:value-of select="concat(' (',@r, ')')" />
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <a href="{$objectUrl}?r={@r}">
                    <xsl:value-of select="@r" />
                  </a>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </td>
          <td class="action">
            <xsl:if test="@action">
              <xsl:value-of select="i18n:translate(concat('metaData.versions.action.',@action))" />
            </xsl:if>
          </td>
          <td class="@date">
            <xsl:call-template name="formatISODate">
              <xsl:with-param name="date" select="@date" />
              <xsl:with-param name="format" select="i18n:translate('metaData.dateTime')" />
            </xsl:call-template>
          </td>
          <td class="user">
           <xsl:if test="@user">
             <xsl:value-of select="@user" />
            </xsl:if>
          </td>
        </tr>
      </xsl:for-each>
    </table> 
  </xsl:template>

</xsl:stylesheet>