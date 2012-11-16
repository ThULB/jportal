<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- here one has to set the url to the mycore content repository -->
  <xsl:param name="WebApplicationBaseURL" select="'http://141.35.20.219:8291/'" />
  <xsl:param name="WebApplicationServletURL" select="concat($WebApplicationBaseURL, 'servlets/')" />

</xsl:stylesheet>

