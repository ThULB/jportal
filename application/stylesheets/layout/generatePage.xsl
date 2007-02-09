<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="html" indent="yes" encoding="UTF-8" media-type="text/html" 
     doctype-public="-//W3C//DTD HTML 4.01//EN"
     doctype-system="http://www.w3.org/TR/html4/strict.dtd" />
      
	<!-- ================== get some wcms required global variables ===================================== -->      
      <!-- location of navigation base -->
      <xsl:param name="navi"/>	
      <xsl:variable name="navigationBase" >
		  <xsl:call-template name="get.naviBase"/>
	  </xsl:variable>
    <!-- load navigation.xml -->
    <xsl:variable name="loaded_navigation_xml" select="document($navigationBase)/navigation" />
            
      <!-- base image path -->
      <xsl:variable name="ImageBaseURL" select="concat($WebApplicationBaseURL,'images/') " />
	<!-- main title configured in mycore.properties -->
      <xsl:param name="MCR.nameOfProject"/>
      <xsl:variable name="MainTitle">
            <xsl:value-of select="$MCR.nameOfProject"/>
      </xsl:variable>
	
	  <xsl:param name="href"/>
      <xsl:variable name="browserAddress_tmp">
            <xsl:call-template name="getBrowserAddress" />
      </xsl:variable>
	  <!-- has web context been reset -->
	  <xsl:variable name="wcReset">
		  <xsl:call-template name="haveWCReset">
			  <xsl:with-param name="detectionString" select="$browserAddress_tmp"/>
		  </xsl:call-template>
	  </xsl:variable>
	  <!-- assign right browser address -->	
	  <xsl:param name="browserAddress">
		<xsl:call-template name="getBrowserAddressFromTmp">
			<xsl:with-param name="bat" select="$browserAddress_tmp"/>
		</xsl:call-template>		  
	  </xsl:param>
		
      <!-- look for appropriate template entry and assign -> $template -->
      <xsl:param name="template" >
            <xsl:call-template name="getTemplate" >
                  <xsl:with-param name="browserAddress" select="$browserAddress"/>
                  <xsl:with-param name="navigationBase" select="$navigationBase"/>                  
            </xsl:call-template>
      </xsl:param>
      <!-- set useTarget to 'yes' if you want the target attribute to appear in links
        the wcms controls. This would break HTML 4.01 strict compatiblity but allows
        the browser to open new windows when clicking on certain links.
        To keep standard compliance it's default turned of, as it may annoy some
        people, too.
      -->
      <xsl:variable name="wcms.useTargets" select="'yes'" />

    <xsl:include href="chooseTemplate.xsl" />
	<xsl:include href="pagetitle.xsl" />
	<xsl:include href="navi_main.xsl" />
	<xsl:include href="footer.xsl" />
	<xsl:include href="navigation.xsl" /> 
	<xsl:include href="wcms_common.xsl" />     
	<xsl:include href="jp_extensions.xsl" />	

      <!-- =================================================================================================== -->
      <xsl:template name="generatePage">
            <!-- call the appropriate template -->
            <xsl:call-template name="chooseTemplate" />
      </xsl:template>
      <!-- ================================================================================= -->
	  <xsl:template name="get.naviBase">
		<xsl:choose>
			<xsl:when test="$navi=''">
				<xsl:value-of select="'webapp:config/navigation.xml'" />				
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat('File:',$navi)" />
			</xsl:otherwise>								
		</xsl:choose>
	</xsl:template>
      <!-- ================================================================================= -->
	<xsl:template name="getBrowserAddressFromTmp">
		<xsl:param name="bat" />
		<xsl:choose>
			<xsl:when test="contains($bat,'wcReset')">
				<xsl:value-of select="substring-after($bat,'wcReset')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$bat"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>	
      <!-- =================================================================================================== -->		
	<xsl:template name="haveWCReset">
		<xsl:param name="detectionString" />
		<xsl:choose>
			<xsl:when test="contains($detectionString,'wcReset')">
				<xsl:value-of select="substring-after($detectionString,'wcReset')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'false'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>		
      <!-- =================================================================================================== -->		
</xsl:stylesheet>