<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2004/05/10 08:56:01 $ -->
<!-- ============================================== -->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
<xsl:output
  method="html"
  encoding="ISO-8859-1"
  media-type="text/html"
  doctype-public="-//W3C//DTD HTML 3.2 Final//EN"
/>

<xsl:include href="master.xsl" />
<xsl:param name="WebApplicationBaseURL" />
<xsl:param name="Author"/>
<xsl:param name="Comment"/>
<xsl:param name="Address"/>
<xsl:param name="LocalContact"/>
<xsl:param name="nbn"/>
<xsl:param name="Resolver"/>
<xsl:param name="CodedUrn"/>
<xsl:variable name="PageTitle" select="/nbn-printable/@title" />


<xsl:template match="/">
  <html>
    <head>
      <title><xsl:value-of select="$PageTitle"/></title>
    </head>
    <link href="{$WebApplicationBaseURL}i/unie.css" rel="stylesheet"/>
    <body bgcolor="#FFFFFF" text="#000000">
      <a href="{$WebApplicationBaseURL}">
        <img src="{$WebApplicationBaseURL}images/logo.gif" align="absmiddle" border="0" alt="MILESS Logo"/>
      </a>
      <xsl:text disable-output-escaping="yes"> &amp;nbsp; &amp;nbsp; &amp;nbsp; </xsl:text>
      <font size="+2"><xsl:value-of select="$PageTitle"/></font>
      <br/>
      <hr noshade="noshade" size="1"/>
        <xsl:apply-templates select="nbn-printable/section" />
      <hr noshade="noshade" size="1"/>
      <table border="0" width="100%" class="font">
        <tr>
          <td width="25%" align="left" valign="middle">
            EMail: <b>miless@uni-essen.de</b>
          </td>
          <td width="50%" align="middle">
            Homepage: <b>http://miless.uni-essen.de/</b>
          </td>
          <td width="25%" align="right" valign="middle">
            <p><xsl:text disable-output-escaping="yes">&amp;copy; 2002 </xsl:text>
              <img src="{$WebApplicationBaseURL}images/uni-essen.gif" border="0" alt="Universität Essen" align="absmiddle"/>
            </p>
          </td>
        </tr>
      </table>
    </body>
  </html>
</xsl:template>

<xsl:template match="section">
  <blockquote class="font">
    <xsl:copy-of select="* | text()"/>
  </blockquote>

  <blockquote class="font">
  <h3 class="font">Reservierungsdaten:</h3>
    <ul>
      <li>
        <b>Autor:</b><br/>
        <xsl:value-of select="$Author"/>
      </li>
      <li>
        <b>NBN:</b><br/>
        <xsl:value-of select="$nbn"/>
      </li>
      <li><b>Resolver:</b><br/>
        <xsl:value-of select="$Resolver"/>
      </li>
      <li>
        <b>Wiedergabe der Daten im Dokument:</b><br/>
        <xsl:value-of select="$nbn"/><br/>
        [<xsl:value-of select="concat($Resolver,$CodedUrn)"/>]
      </li>
      <xsl:if test="$Comment!=''">
        <li><b>Kommentar:</b><br/>
          <xsl:value-of select="$Comment"/>
        </li>
      </xsl:if>
    </ul>
    <h3 class="font">Kontakt:</h3>
    <ul>
      <li>
        <b>Adresse:</b><br/>
        <xsl:value-of select="$Address"/>
      </li>
      <li>
        <b>E-mail:</b><br/>
        <xsl:value-of select="$LocalContact"/>
      </li>
    </ul>
  </blockquote>
</xsl:template>

<!-- - - - - - - - - Navigation - - - - - - - - -->

<xsl:template name="NavigationColumn">
</xsl:template>

<!-- - - - - - - - - Content column - - - - - - - - -->

<xsl:template name="ContentColumn">
</xsl:template>

</xsl:stylesheet>
