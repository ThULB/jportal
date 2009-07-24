<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 270 $ $Date: 2007-06-15 16:18:04 +0200 (Fri, 15 Jun 2007) $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
> 

<xsl:output method="xml" encoding="UTF-8"/>

<xsl:template match="/mycoreobject">
  <mycoreobject>
    <xsl:copy-of select="@ID"/>
    <xsl:copy-of select="@label"/>
    <xsl:copy-of select="@version"/>
    <xsl:copy-of select="@xsi:noNamespaceSchemaLocation"/>
	<!-- check the WRITEDB permission -->
	<xsl:if test="acl:checkPermission(@ID,'read')">
<!--      <xsl:copy-of select="structure"/>-->
      <xsl:copy-of select="metadata"/>
<!--      <xsl:copy-of select="service"/>-->
    </xsl:if>
  </mycoreobject>
</xsl:template>

</xsl:stylesheet>
