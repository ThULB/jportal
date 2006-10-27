<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2006/09/20 12:37:05 $ -->
<!-- ============================================== -->
<xsl:stylesheet
     version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:xMetaDiss="http://www.ddb.de/standards/xMetaDiss/"
     xmlns:cc="http://www.ddb.de/standards/cc/"
     xmlns:ddb="http://www.ddb.de/standards/ddb/"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dcmitype="http://purl.org/dc/dcmitype/"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:pc="http://www.ddb.de/standards/pc"
     xmlns:urn="http://www.ddb.de/standards/urn"
     xmlns:thesis="http://www.ndltd.org/standards/metadata/etdms/1.0"
     xmlns="http://www.ddb.de/standards/subject/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance/"
     xsi:schemaLocation="http://www.ddb.de/standards/xMetaDiss/ http://www.ddb.de/standards/xmetadiss/xmetadiss.xsd">
     
    <xsl:output method="xml"
	             encoding="UTF-8"/>
   
    <xsl:param name="ServletsBaseURL" select="''" /> 
    <xsl:param name="JSessionID" select="''" />   
    
    <xsl:include href="MyCoReDublinCoreTemplates.xsl" />             
   
    <xsl:template match="/">
        <xsl:apply-templates select="*" />
    </xsl:template>

    <xsl:template match="mycoreobject">
			<metadata xmlns="http://www.openarchives.org/OAI/2.0/">
				<oai_dc:dc
					xmlns:xlink="http://www.w3.org/1999/xlink"
					xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
					xmlns:dc="http://purl.org/dc/elements/1.1/"
					xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
					xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd" >
 
             <xsl:call-template name="title_qdc"/>
             <xsl:call-template name="alternative_qdc"/>
             <xsl:call-template name="creator"/>
             <xsl:call-template name="subject_qdc"/>
             <xsl:call-template name="abstract_qdc"/>
             <xsl:call-template name="publisher"/>
             <xsl:call-template name="contributor"/> 
             <xsl:call-template name="date_qdc"/>
             <xsl:call-template name="type_qdc"/>
             <xsl:call-template name="identifier_qdc"/>
             <xsl:call-template name="format_qdc"/>
             <xsl:call-template name="language_qdc"/>         
             <xsl:call-template name="rights"/> 
             
           </oai_dc:dc>
        </metadata>
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
