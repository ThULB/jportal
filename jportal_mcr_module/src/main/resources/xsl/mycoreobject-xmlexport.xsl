<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
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
