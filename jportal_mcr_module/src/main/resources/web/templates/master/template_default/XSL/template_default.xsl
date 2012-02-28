<?xml version="1.0" encoding="ISO-8859-1"?>
  <!-- ============================================== -->
  <!-- $Revision: 575 $ $Date: 2008-09-04 14:26:32 +0200 (Do, 04 Sep 2008) $ -->
  <!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xlink i18n" xmlns:xalan="http://xml.apache.org/xalan">

  <!-- ============================================== -->
  <!-- the template                                   -->
  <!-- ============================================== -->
  <xsl:template name="template_default">
    <xsl:variable name="journalsID">
      <xsl:value-of select="document('jportal_getJournalID:XPathDoesNotExist')/dummyRoot/hidden/@default" />
    </xsl:variable>
    <xsl:variable name="journalXML">
      <xsl:copy-of select="document(concat('mcrobject:',$journalsID))" />
    </xsl:variable>
    <xsl:variable name="journalMaintitle">
      <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/maintitles/maintitle/text()" />
    </xsl:variable>
    <xsl:variable name="periodetitle">
      <xsl:variable name="dates" select="xalan:nodeset($journalXML)/mycoreobject/metadata/dates"/>
      <xsl:choose>
        <xsl:when test="$dates/date[@type='published']">
           <xsl:copy-of select="$dates/date[@type='published']/text()" />
        </xsl:when>
        <xsl:when test="dates/date[@type='published_from']">
          <xsl:copy-of select="concat($dates/date[@type='published_from']/text(), ' - ', $dates/date[@type='published_until']/text())" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="additionalTitle">
      <xsl:value-of select="xalan:nodeset($journalXML)/mycoreobject/metadata/subtitles/subtitle/text()" />
    </xsl:variable>

    <html>
      <head>
        <xsl:call-template name="jp.layout.getHTMLHeader">
          <xsl:with-param name="nameOfTemplate" select="'template_default'" />
        </xsl:call-template>
      </head>
      <body>
        <table width="100%" height="30px" border="0" cellspacing="0" cellpadding="0" style="background-color: #858688;">
          <tr valign="center">
            <td style="padding-left: 10px;">
              <div id="headline">
                <a href="http://www.urmel-dl.de/" target="_blank">UrMEL</a>
                <xsl:copy-of select="'     |     '" />
                <a href="http://zs.thulb.uni-jena.de/content/below/index.xml" target="_self">Journals@UrMEL</a>
              </div>
            </td>
            <td align="right">
              <div id="navigation_box">
                <xsl:call-template name="navigation.row" />
              </div>
            </td>
          </tr>
        </table>

        <table height="206" width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color:transparent;">
          <tr height="136">
            <td style="background-image:url({$WebApplicationBaseURL}templates/master/template_default/IMAGES/header.png); background-repeat:repeat-x;">
              <div id="journal-title">
                <xsl:copy-of select="$journalMaintitle" />
              </div>
            </td>
          </tr>
          <tr height="30">
            <td style="background-color: #467491">
              <div id="journal-subtitle">
                <xsl:copy-of select="concat($additionalTitle, ' ', $periodetitle)" />
              </div>
            </td>
          </tr>
          <tr height="40">
            <div id="navi_history">
              <xsl:call-template name="navigation.history" />
            </div>
          </tr>
        </table>

        <table width="100%" height="max" border="0" cellspacing="0" cellpadding="0" style="padding-bottom: 10px;" valign="top">
          <tr valign="top">
            <td width="200px" valign="top" rowspan="2">
              <div id="div_navi_main">
                <xsl:call-template name="Navigation_main">
                  <!-- use percent values -->
                  <xsl:with-param name="spaceBetweenMainLinks" select="'2'" />
                  <!-- use pixel values -->
                  <xsl:with-param name="borderWidthSides" select="'0'" />
                  <!-- use percent values -->
                </xsl:call-template>
              </div>
              <!-- are special logos for current used journal available ? -> if yes, get them -->
              <xsl:call-template name="template_logos.getLogos">
                <xsl:with-param name="journalsID" select="$journalID" />
              </xsl:call-template>
            </td>
            <td width="max" valign="top" style="height: 12px">
              <xsl:text> </xsl:text>
            </td>
            <td width="135px" rowspan="2" valign="top" style="height: 500px;">
              <xsl:text> </xsl:text>
            </td>
          </tr>
          <tr valign="top">
            <td width="max" valign="top" style="height: 500px;">
              <div id="contentArea">
                <div id="contentWrapper">
                  <xsl:call-template name="template_default.write.content" />
                </div>
              </div>
            </td>
          </tr>
        </table>
      </body>
    </html>

  </xsl:template>

  <!-- Template for Content ================================================================================== -->
  <xsl:template name="template_default.write.content">
    <xsl:call-template name="jp.layout.getHTMLContent" />
  </xsl:template>

</xsl:stylesheet>
