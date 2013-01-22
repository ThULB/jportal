<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jpxml="xalan://org.mycore.common.xml.MCRJPortalXMLFunctions"
	exclude-result-prefixes="jpxml">

  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <xsl:template mode="printListEntry" match="*">
    <li>
      <xsl:apply-templates mode="printListEntryContent" select="." />
    </li>
  </xsl:template>

  <xsl:template name="shortenString">
    <xsl:param name="string" />
    <xsl:param name="length" />
    <xsl:param name="remainder" select="'...'" />

    <xsl:choose>
      <xsl:when test="string-length($string) > $length">
        <xsl:value-of select="concat(substring($string,0,$length), $remainder)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="uppercase">
    <xsl:param name="string" />
    <xsl:value-of select="translate($string,$lcletters,$ucletters)" />
  </xsl:template>

  <xsl:template name="lowercase">
    <xsl:param name="string" />
    <xsl:value-of select="translate($string,$ucletters, $lcletters)" />
  </xsl:template>
  
  <xsl:template name="printJPClassification">
    <xsl:param name="nodes"/>
      <xsl:for-each select="$nodes">
          <xsl:choose>
            <xsl:when test="string-length(./label[lang($CurrentLang)]/@text) = 0">
    			<xsl:call-template name="JPClassLang">
    				<xsl:with-param name="node" select="." />
    				<xsl:with-param name="pos" select="1" />
    			</xsl:call-template>          
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="./label[lang($CurrentLang)]/@text"/>
            </xsl:otherwise>
          </xsl:choose> 
      </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="JPClassLang">
    <xsl:param name="node"/>
    <xsl:param name="pos"/>
    <xsl:variable name="languages" select="jpxml:getLanguages()/languages/lang" />
    <xsl:variable name="classlabel">
		<xsl:value-of select="$node/label[lang($languages[$pos]/text())]/@text" /> 
 	</xsl:variable>
 	<xsl:choose>
    	<xsl:when test="string-length($classlabel) != 0">
			<xsl:value-of select="$classlabel" />
    	</xsl:when>
	   	<xsl:otherwise>
			<xsl:if test="$languages[$pos + 1]">
    		 	<xsl:call-template name="JPClassLang">
    				<xsl:with-param name="node" select="$node" />
    				<xsl:with-param name="pos" select="$pos + 1" />
    			</xsl:call-template> 
    		</xsl:if>
    	</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>