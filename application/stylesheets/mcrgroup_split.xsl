<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2006/10/13 06:17:05 $ -->
<!-- ============================================== -->

<xsl:stylesheet 
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
  extension-element-prefixes="redirect">
	
  <xsl:output method="xml" encoding="UTF-8"/>
	
  <xsl:template match="/">
    <xsl:for-each select="*/group">
      <xsl:choose>
        <xsl:when test="element-available('redirect:write')">
          <xsl:choose>
            <xsl:when test="@ID = 'rootgroup'" />
            <xsl:when test="@ID = 'gastgroup'" />
            <xsl:otherwise>
              <redirect:write select="concat('group_',@ID,'.xml')">
<mycoregroup xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xsi:noNamespaceSchemaLocation="MCRGroup.xsd">
  <group>
	<xsl:attribute name="ID">
      <xsl:value-of select="@ID" />
	</xsl:attribute>	
	<xsl:copy-of select="group.description" />
  </group>
</mycoregroup>
              </redirect:write>
The file was writen for group <xsl:value-of select="@ID" />.
            </xsl:otherwise>
		  </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
The XALAN extention redirect is not available.
		</xsl:otherwise>
	  </xsl:choose>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>