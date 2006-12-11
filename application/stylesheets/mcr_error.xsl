<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.2 $ $Date: 2006/05/26 15:28:26 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xlink"
>
<xsl:variable name="Type" select="'document'" />

<xsl:variable name="MainTitle" select="concat(/mcr_error/@HttpError,': DocPortal')"/>
<xsl:variable name="PageTitle" select="i18n:translate('titles.pageTitle.error')"/>

<xsl:template match="/mcr_error">
<div id="errormessage" style="background-color: #CAD9E0; border-style: solid; border-width: 1px; border-color:#05516E;">
<!-- Here put in dynamic search mask -->
 <table border="0" width="90%">
  <tr>
   <td class="errormain">
    <xsl:value-of select="concat(i18n:translate('error.intro'),' :')" />
   </td>
  </tr>
  <tr>
   <td class="errortrace">
   <pre><xsl:value-of select="text()" /></pre>
   </td>
  </tr>
  <tr>
   <xsl:choose>
   <xsl:when test="exception!=''">
   <td class="errortrace">
   <p><xsl:value-of select="concat(i18n:translate('error.stackTrace'),' :')"/></p>
   <pre style="font-size:0.8em;"><xsl:value-of select="exception/trace" /></pre>
   </td>
   </xsl:when>
   <xsl:otherwise>
   <td class="errortrace" style="text-align:center;"><xsl:value-of select="i18n:translate('error.noInfo')"/></td>
   </xsl:otherwise>
   </xsl:choose>
  </tr>
 </table>
</div>
</xsl:template>
<xsl:include href="MyCoReLayout.xsl" />
</xsl:stylesheet>
