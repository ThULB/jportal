<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation">

  <!--
  Entry point to print all laws of a register
  -->
  <xsl:template match="/gesetzessammlung">
    <xsl:apply-templates select="register" />
  </xsl:template>

  <!-- ================================================================================= -->

  <!-- Print the head of the register and prints the laws -->
  <xsl:template match="register">
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
          <td><b><xsl:value-of select="i18n:translate('jp.laws.register.duchy')" />:</b></td>
          <td><xsl:value-of select="herzogtum" /></td>
        </tr>
      </xsl:if>
    </table>

    <!-- Print laws -->
    <xsl:apply-templates select="gesetze" />

  </xsl:template>
  
  <!-- ================================================================================= -->

  <xsl:template match="gesetze">
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
          <xsl:apply-templates select="." />
        </tr>
      </xsl:for-each>    
    </table>
  </xsl:template>

  <xsl:template match="gesetz">
    <td class="nr"><xsl:value-of select="nummer"/></td>
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
    <td class="content"><xsl:value-of select="inhalt" /></td>
  </xsl:template>

</xsl:stylesheet>
