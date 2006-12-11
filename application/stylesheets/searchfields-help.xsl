<?xml version="1.0" encoding="iso-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2006/05/08 13:56:55 $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mcr="http://www.mycore.org/"
>

<xsl:output
  method="html"
  encoding="ISO-8859-1"
/>

<xsl:template match="/mcr:searchfields">
 <html>
  <table border="0">
    <tr>
      <th>Field:</th>
      <th>Type:</th>
      <th>Index:</th>
    </tr>
    <xsl:apply-templates select="mcr:index/mcr:field" />
  </table>
 </html>
</xsl:template>

<xsl:template match="mcr:field">
  <tr>
    <td>
      <xsl:value-of select="@name" />
    </td>
    <td>
      <xsl:value-of select="@type" />
    </td>
    <td>
      <xsl:value-of select="../@id" />
    </td>
  </tr>
</xsl:template>

</xsl:stylesheet>