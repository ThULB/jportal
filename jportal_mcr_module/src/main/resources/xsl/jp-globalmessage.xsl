<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" exclude-result-prefixes="xalan mcrxml">

  <xsl:template match="/globalmessage">
  </xsl:template>

  <xsl:template match="/globalmessage[visibility != 'hidden']">
    <xsl:if test="(visibility = 'visible') or
                    (visibility = 'user' and not(mcrxml:isCurrentUserGuestUser())) or
                    (visibility = 'admin' and mcrxml:isCurrentUserInRole('admin'))">
      <xsl:call-template name="jp.globalmessage.print" />
    </xsl:if>
  </xsl:template>

  <xsl:template name="jp.globalmessage.print">
    <div class="message-wrapper">
      <div class="message">
        <h1>
          <xsl:value-of select="head" />
        </h1>
        <p>
          <xsl:value-of select="message" />
        </p>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
