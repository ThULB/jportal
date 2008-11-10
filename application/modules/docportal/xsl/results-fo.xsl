<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.20 $ $Date: 2007-04-04 13:23:09 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  exclude-result-prefixes="mcr xsl encoder">

<xsl:output method="xml" encoding="UTF-8" media-type="application/pdf" />

<xsl:template match="/mcr:results">
  <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
  
    <fo:layout-master-set>
      <fo:simple-page-master master-name="pageLayout"
        page-height="29.7cm" page-width="21cm"
        margin-top="1.5cm" margin-bottom="1cm"
        margin-left="2.5cm" margin-right="2.5cm">
        <fo:region-before extent="10cm" />
        <fo:region-body background-color="white" margin-top="2cm" margin-bottom="2cm"/>
        <fo:region-after extent="1cm" />
      </fo:simple-page-master>
    </fo:layout-master-set>
    
    <fo:page-sequence master-reference="pageLayout" initial-page-number="1">   
                          
      <fo:static-content flow-name="xsl-region-before">        
        <fo:block>
          Search results als PDF. Modify results-fo.xsl to produce nice output!
		</fo:block>        
      </fo:static-content> 	  
      
	  <fo:static-content flow-name="xsl-region-after">        
        <fo:block font-size="10pt" font-family="serif" >
          powered by MyCoRe
        </fo:block>
      </fo:static-content>
      
      <fo:flow flow-name="xsl-region-body">
        <xsl:apply-templates select="mcr:hit" />
      </fo:flow>
    </fo:page-sequence>
      
  </fo:root>
</xsl:template>

<xsl:template match="mcr:hit">
  <xsl:apply-templates select="document(concat('mcrobject:',@id))/mycoreobject" />
</xsl:template>

<xsl:template match="mycoreobject">
  <xsl:for-each select="metadata/titles/title|metadata/creators/creator|metadata/descriptions/description">
    <fo:block font-size="12pt" font-family="serif" space-after="10pt">  
      <xsl:value-of select="text()" />
    </fo:block>
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
