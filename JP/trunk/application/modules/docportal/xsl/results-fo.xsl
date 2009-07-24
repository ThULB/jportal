<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.20 $ $Date: 2007-04-04 13:23:09 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="mcr xsl encoder i18n">

<xsl:output method="xml" encoding="UTF-8" media-type="application/pdf" />

<xsl:param name="WebApplicationBaseURL" />

<xsl:template match="/mcr:results">
  <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
  
    <fo:layout-master-set>
      <fo:simple-page-master master-name="pageLayout"
        page-height="29.7cm" page-width="21cm"
        margin-top="1.5cm" margin-bottom="1cm"
        margin-left="2.5cm" margin-right="2.5cm">
        <fo:region-before extent="15cm" />
        <fo:region-body background-color="white" margin-top="3cm" margin-bottom="2cm"/>
        <fo:region-after extent="2cm" />
      </fo:simple-page-master>
    </fo:layout-master-set>
    
    <fo:page-sequence master-reference="pageLayout" initial-page-number="1">                            
     
      
       <fo:static-content flow-name="xsl-region-before">        
        <fo:block>
          <fo:table width="200mm" >
           <fo:table-column column-number="1" />
           <fo:table-column column-number="2"  />
           <fo:table-body>
           <fo:table-row >
           <fo:table-cell column-number="1">
              <fo:block>
                 <fo:external-graphic src="{$WebApplicationBaseURL}templates/master/template_mycoresample-1/IMAGES/mycore_logo400502.jpg"/>
              </fo:block>
           </fo:table-cell>
           <fo:table-cell column-number="2">
              <fo:block font-size="20pt" font-family="serif" font-weight="bold"  space-before.optimum="20pt">
              Suchergebnisse
              </fo:block>                   
           </fo:table-cell>
          </fo:table-row>
          </fo:table-body>
          </fo:table>
         </fo:block>    
         <fo:block space-after="30pt"><fo:leader leader-pattern="rule" leader-length="16cm" /></fo:block>     
      </fo:static-content>    
      
	  <fo:static-content flow-name="xsl-region-after">           
        <fo:block><fo:leader leader-pattern="rule" leader-length="16cm" /></fo:block>        
        <fo:block font-size="10pt" font-family="serif" text-align="right">Seite <fo:page-number/>          
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

  <!-- ==== author ==== -->
  
  <fo:block font-size="12pt" font-family="serif" font-style="italic" space-before="20pt">
  <xsl:for-each select="metadata/creators/creator">
        <xsl:if test="position() != 1">
                <br />
              </xsl:if>
              <xsl:value-of select="." />
  </xsl:for-each>
  <xsl:text>:</xsl:text>
  </fo:block>
   
  <!-- ==== title ==== -->  
  
  <fo:block font-size="12pt" font-family="serif" font-weight="bold">
  <xsl:for-each select="metadata/titles/title" >
             <xsl:if test="position() = 1">                          
              <xsl:value-of select="text()" /> 
             </xsl:if>  
  </xsl:for-each>  
  </fo:block>
  
  <!-- ==== description === -->
    
  <xsl:for-each select="metadata/descriptions/description">  
    <fo:block font-size="10pt" font-family="serif">  
      <xsl:if test="position() = 1">                          
              <xsl:value-of select="text()" /> 
             </xsl:if> 
    </fo:block>
  </xsl:for-each>  
    
      
</xsl:template>

</xsl:stylesheet>
