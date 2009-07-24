<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.4 $ $Date: 2006/05/04 11:51:12 $ -->
<!-- ============================================== -->
<xsl:stylesheet
     version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dcmitype="http://purl.org/dc/dcmitype/"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     
    <xsl:output method="xml"
	             encoding="UTF-8"/>
                
    <xsl:param name="ServletsBaseURL" select="''" /> 
    <xsl:param name="WebApplicationBaseURL" select="''" /> 
    <xsl:param name="JSessionID" select="''" />   
    
    <xsl:include href="MyCoReDublinCoreTemplates.xsl" /> 
    
    <xsl:template match="/mycoreobject">
        <mycore:qualifieddc
               xmlns:mycore="http://www.mycore.de/xmlns/qdc/"
					xmlns:xlink="http://www.w3.org/1999/xlink"
               xmlns:dc="http://purl.org/dc/elements/1.1/"
               xmlns:dcmitype="http://purl.org/dc/dcmitype/"
               xmlns:dcterms="http://purl.org/dc/terms/"
					xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
					xsi:schemaLocation="http://www.mycore.de/xmlns/qdc/ http://www.mycore.de/schema/qdc.xsd">
         
             <xsl:call-template name="title" />
             <!-- if you set in mycore type="main" or 
                  type="sub", you can use the qualified
                  dublin core scheme -->
             <!--
             <xsl:call-template name="title_qdc"/>
             <xsl:call-template name="alternative_qdc"/>
             -->
             <xsl:call-template name="creator"/>
             <xsl:call-template name="subject_qdc"/>
             <xsl:call-template name="abstract"/>
             <xsl:call-template name="publisher"/>
             <xsl:call-template name="contributor"/> 
             <xsl:call-template name="date_qdc"/>
             <xsl:call-template name="type_qdc"/>
             <xsl:call-template name="identifier_qdc"/>
             <xsl:call-template name="format_qdc"/>
             <xsl:call-template name="language_qdc"/>         
             <xsl:call-template name="rights"/>

        </mycore:qualifieddc>
    </xsl:template>

    <xsl:template match="*">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:copy/>
		  	 	</xsl:for-each>
                <xsl:apply-templates/>
		  </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
