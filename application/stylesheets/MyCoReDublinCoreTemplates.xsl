<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 1.5 $ $Date: 2006/09/20 12:28:47 $ -->
<!-- ============================================== -->
<xsl:stylesheet
     version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dcmitype="http://purl.org/dc/dcmitype/"
     xmlns:dcterms="http://purl.org/dc/terms/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:template name="linkQueryURL">
        <xsl:param name="id"/>
        <xsl:value-of select="concat('mcrobject:',$id)" />
    </xsl:template>

    <xsl:template name="linkDerDetailsURL">
        <xsl:param name="id"/>
        <xsl:value-of select="concat('ifs:/',$id,'/')" />        
    </xsl:template>

    <xsl:template name="linkClassQueryURL">
        <xsl:param name="classid" select="''" />
        <xsl:param name="categid" select="''" />
        <xsl:value-of select="concat('classification:metadata:0:children:',$classid,':',$categid)" />
    </xsl:template>

    <xsl:template name="title">
        <xsl:for-each select="./metadata/titles/title">    
            <xsl:element name="dc:title">
               <xsl:attribute name="xml:lang"><xsl:value-of select="./@xml:lang" /></xsl:attribute>
               <xsl:value-of select="." />
            </xsl:element>     
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="title_qdc">
        <xsl:for-each select="./metadata/titles/title[@type='main']">    
            <xsl:element name="dc:title">
               <xsl:attribute name="xml:lang"><xsl:value-of select="./@xml:lang" /></xsl:attribute>
               <xsl:value-of select="." />
            </xsl:element>     
        </xsl:for-each>
    </xsl:template>    
   
    <xsl:template name="alternative_qdc">
       <xsl:for-each select="./metadata/titles/title[@type='sub']">    
           <xsl:element name="dcterms:alternative">
               <xsl:attribute name="xml:lang"><xsl:value-of select="./@xml:lang" /></xsl:attribute>
               <xsl:value-of select="." />
           </xsl:element>     
       </xsl:for-each>
    </xsl:template>          

    <xsl:template name="creator">
        <xsl:for-each select="./metadata/creatorlinks/creatorlink"> 
           <xsl:variable name="creatorlinkURL">
                <xsl:call-template name="linkQueryURL">
                    <xsl:with-param name="id" select="./@xlink:href" />
                    <xsl:with-param name="type" select="'author'" />
                </xsl:call-template>
           </xsl:variable>
           <xsl:for-each select="document($creatorlinkURL)/mycoreobject/metadata">
	             <xsl:element name="dc:creator">
                    <xsl:choose>
	                     <!-- for exotic names -->
	                     <xsl:when test="not(./names/name/firstname) and not(./names/name/surname) and ./names/name/fullname">
                           <xsl:value-of select="./names/name/fullname" />
	                     </xsl:when>
	                     <xsl:otherwise>
                            <xsl:value-of select="concat(./names/name/firstname,' ',./names/name/surname)" />
                        </xsl:otherwise>
                    </xsl:choose>
	            </xsl:element> 
	        </xsl:for-each>
        </xsl:for-each>
        <xsl:for-each select="./metadata/creators/creator">
            <xsl:element name="dc:creator">
               <xsl:value-of select="." />
            </xsl:element>
        </xsl:for-each>        
    </xsl:template>

    <xsl:template name="subject">
       <xsl:for-each select="./metadata/subjects/subject">
           <xsl:variable name="subjectlinkURL">
                <xsl:call-template name="linkClassQueryURL">
                    <xsl:with-param name="classid" select="@classid" />
                    <xsl:with-param name="categid" select="@categid" />
                </xsl:call-template>           
           </xsl:variable>
           <xsl:for-each select="document($subjectlinkURL)/mycoreclass/categories/category/label">
               <xsl:element name="dc:subject">
                   <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang" /></xsl:attribute>
                   <xsl:value-of select="@text" />
               </xsl:element>
           </xsl:for-each>
       </xsl:for-each>
       <xsl:for-each select="./metadata/keywords/keyword">
           <xsl:element name="dc:subject">
               <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
               <xsl:value-of select="."/>
           </xsl:element>
       </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="subject_qdc">
       <xsl:for-each select="./metadata/subjects/subject[@classid='mediaTUM_class_00000009']">
           <xsl:element name="dc:subject">
              <xsl:attribute name="xsi:type">dcterms:DDC</xsl:attribute>
              <xsl:value-of select="@categid" />
           </xsl:element>   
       </xsl:for-each>
       <xsl:for-each select="./metadata/subjects/subject[@classid != 'mediaTUM_class_00000009']">
           <xsl:variable name="subjectlinkURL">
                <xsl:call-template name="linkClassQueryURL">
                    <xsl:with-param name="classid" select="@classid" />
                    <xsl:with-param name="categid" select="@categid" />
                </xsl:call-template>           
           </xsl:variable>
           <xsl:for-each select="document($subjectlinkURL)/mycoreclass/categories/category/label">
               <xsl:element name="dc:subject">
                   <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang" /></xsl:attribute>
                   <xsl:value-of select="@text" />
               </xsl:element>
           </xsl:for-each>         
       </xsl:for-each>
       <xsl:for-each select="./metadata/keywords/keyword">
           <xsl:element name="dc:subject">
               <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
               <xsl:value-of select="."/>
           </xsl:element>
       </xsl:for-each>
    </xsl:template>

    <xsl:template name="abstract">
        <xsl:for-each select="./metadata/descriptions/description[@type='abstract']">
            <xsl:element name="dc:description">
                <xsl:attribute name="xml:lang"><xsl:value-of select="./@xml:lang" /></xsl:attribute>              
                <xsl:value-of select="." /> 
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="abstract_qdc">
        <xsl:for-each select="./metadata/descriptions/description[@type='abstract']">
            <xsl:element name="dcterms:abstract">
                <xsl:attribute name="xml:lang"><xsl:value-of select="./@xml:lang" /></xsl:attribute>              
                <xsl:value-of select="." /> 
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="publisher">
        <xsl:for-each select="./metadata/publishlinks/publishlink">
           <xsl:variable name="publishlinkURL">
                <xsl:call-template name="linkQueryURL">
                    <xsl:with-param name="id" select="./@xlink:href" />
                </xsl:call-template>
           </xsl:variable>
           <xsl:for-each select="document($publishlinkURL)/mycoreobject/metadata">
	            <xsl:element name="dc:publisher">
	                <xsl:value-of select="concat(./names/name[@xml:lang='de']/shortname,
                                                ', ',
                                                ./names/name[@xml:lang='de']/fullname)" />
	            </xsl:element>
           </xsl:for-each>
        </xsl:for-each>
        <xsl:for-each select="./metadata/publishers/publisher">
            <xsl:element name="dc:publisher">
                <xsl:value-of select="." />
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="contributor">
        <xsl:for-each select="./metadata/contriblinks/contriblink"> 
           <xsl:variable name="contriblinkURL">
                <xsl:call-template name="linkQueryURL">
                    <xsl:with-param name="id" select="./@xlink:href" />
                </xsl:call-template>
           </xsl:variable>
           <xsl:for-each select="document($contriblinkURL)/mycoreobject/metadata">
	             <xsl:element name="dc:contributor">
                    <xsl:choose>
	                     <!-- for exotic names -->
	                     <xsl:when test="not(./names/name/firstname) and not(./names/name/surname) and ./names/name/fullname">
                           <xsl:value-of select="./names/name/fullname" />
	                     </xsl:when>
	                     <xsl:otherwise>
                            <xsl:value-of select="concat(./names/name/academic,' ',./names/name/firstname,' ',./names/name/surname)" />
                        </xsl:otherwise>
                    </xsl:choose>
	            </xsl:element> 
	        </xsl:for-each>
        </xsl:for-each>      
        <xsl:for-each select="./metadata/contributors/contributor">
            <xsl:element name="dc:contributor">
                <xsl:value-of select="." />
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="date">
        <xsl:if test="./metadata/dates/date">
            <xsl:element name="dc:date">
                <xsl:value-of select="./metadata/dates/date" />
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template name="date_qdc">
        <xsl:choose>
            <xsl:when test="./metadata/dates/date[@type='accepted']">
	             <xsl:element name="dcterms:dateAccepted">
	                 <xsl:attribute name="xsi:type">dcterms:W3CDTF</xsl:attribute>
	                 <xsl:value-of select="substring(./metadata/dates/date[@type='accepted'],1,4)" />
	             </xsl:element>               
            </xsl:when>
            <xsl:otherwise>
               <xsl:call-template name="date" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="type">
        <xsl:for-each select="./metadata/formats/format">
            <xsl:variable name="formatclasslinkURL">
                 <xsl:call-template name="linkClassQueryURL">
                     <xsl:with-param name="classid" select="./@classid" />
                     <xsl:with-param name="categid" select="./@categid" />
                 </xsl:call-template>
            </xsl:variable>
            <xsl:for-each select="document($formatclasslinkURL)/mycoreclass/categories/category/label">
                <xsl:element name="dc:type">
                    <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang" /></xsl:attribute>
                    <xsl:value-of select="@text" />
                </xsl:element>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="type_qdc">
        <xsl:for-each select="./metadata/formats/format">
            <xsl:variable name="formatclasslinkURL">
                 <xsl:call-template name="linkClassQueryURL">
                     <xsl:with-param name="classid" select="./@classid" />
                     <xsl:with-param name="categid" select="./@categid" />
                 </xsl:call-template>
            </xsl:variable>
            <xsl:for-each select="document($formatclasslinkURL)/mycoreclass/categories/category/label">
                <xsl:element name="dc:type">
                    <xsl:attribute name="xsi:type">dcterms:DCMIType</xsl:attribute>
                    <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang" /></xsl:attribute>
                    <xsl:value-of select="@text" />
                </xsl:element>
            </xsl:for-each> 
          </xsl:for-each>
    </xsl:template>    

    <xsl:template name="identifier">
        <xsl:if test="./metadata/urns/urn">
           <xsl:element name="dc:identifier">
              <xsl:value-of select="./metadata/urns/urn/@xlink:href" /> 
           </xsl:element>
        </xsl:if>
        <xsl:element name="dc:identifier">
        	<xsl:variable name="ID" select="./@ID" />
            <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',$ID)" /> 
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="identifier_qdc">
        <xsl:if test="./metadata/urns/urn">
           <xsl:element name="dc:identifier">
              <xsl:attribute name="xsi:type">dcterms:URI</xsl:attribute>
              <xsl:value-of select="./metadata/urns/urn/@xlink:href" /> 
           </xsl:element>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="getFormatElement">
        <xsl:param name="formatValue" select="''" />
        <xsl:param name="dcType" select="'simple'" />
        <xsl:element name="dc:format">
            <xsl:if test="contains(dcType,'qualified')">
                <xsl:attribute name="xsi:type">dcterms:IMT</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="$formatValue" />
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="checkFormats">
       <xsl:param name="contentTypes" select="''" />
       <xsl:param name="dcType" select="'simple'" />
        <xsl:if test="contains($contentTypes,'ps')">
            <xsl:call-template name="getFormatElement">
               <xsl:with-param name="formatValue" select="'application/postscript'" />
               <xsl:with-param name="dcType" select="$dcType" />
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="contains($contentTypes,'pdf')">
            <xsl:call-template name="getFormatElement">
               <xsl:with-param name="formatValue" select="'application/pdf'" />
               <xsl:with-param name="dcType" select="$dcType" />
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="contains($contentTypes,'zip')">
            <xsl:call-template name="getFormatElement">
               <xsl:with-param name="formatValue" select="'application/zip'" />
               <xsl:with-param name="dcType" select="$dcType" />
            </xsl:call-template>         
        </xsl:if>
        <xsl:if test="contains($contentTypes,'jpg')">
            <xsl:call-template name="getFormatElement">
               <xsl:with-param name="formatValue" select="'image/jpeg'" />
               <xsl:with-param name="dcType" select="$dcType" />
            </xsl:call-template>         
        </xsl:if>
        <xsl:if test="contains($contentTypes,'png')">
            <xsl:call-template name="getFormatElement">
               <xsl:with-param name="formatValue" select="'image/png'" />
               <xsl:with-param name="dcType" select="$dcType" />
            </xsl:call-template>         
        </xsl:if>
        <xsl:if test="contains($contentTypes,'gif')">
            <xsl:call-template name="getFormatElement">
               <xsl:with-param name="formatValue" select="'image/gif'" />
               <xsl:with-param name="dcType" select="$dcType" />
            </xsl:call-template>         
        </xsl:if>       
    </xsl:template>
    
    <xsl:template name="format">
        <!-- avoiding doubled medium types -->
        <xsl:variable name="contentTypes">
            <xsl:for-each select="./structure/derobjects/derobject">
                <xsl:variable name="detailsURL">
                    <xsl:call-template name="linkDerDetailsURL">
                        <xsl:with-param name="id" select="./@xlink:href" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:for-each select="document($detailsURL)/mcr_directory/children/child">               
                    <xsl:value-of select="concat(./contentType/text(),',')" />
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>
        <xsl:call-template name="checkFormats">
           <xsl:with-param name="contentTypes" select="$contentTypes" />
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template name="format_qdc">
        <!-- avoiding doubled medium types -->
        <xsl:variable name="contentTypes">
            <xsl:for-each select="./structure/derobjects/derobject">
                <xsl:variable name="detailsURL">
                    <xsl:call-template name="linkDerDetailsURL">
                        <xsl:with-param name="id" select="./@xlink:href" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:for-each select="document($detailsURL)/mcr_directory/children/child">               
                    <xsl:value-of select="concat(./contentType/text(),',')" />
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>
        <xsl:call-template name="checkFormats">
           <xsl:with-param name="contentTypes" select="$contentTypes" />
           <xsl:with-param name="dcType" select="'qualified'" />
        </xsl:call-template>
    </xsl:template>    

    <xsl:template name="language">
        <xsl:for-each select="./metadata/languages/language">
            <xsl:variable name="classlinkURL">
                 <xsl:call-template name="linkClassQueryURL">
                     <xsl:with-param name="classid" select="./@classid" />
                     <xsl:with-param name="categid" select="./@categid" />
                 </xsl:call-template>
            </xsl:variable>
            <xsl:for-each select="document($classlinkURL)/mycoreclass/categories/category/label[@xml:lang='en']">  
                <xsl:element name="dc:language">
                   <xsl:value-of select="substring-before(./@description,'#')" />
                </xsl:element>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="language_qdc">
        <xsl:for-each select="./metadata/languages/language">
            <xsl:variable name="classlinkURL">
                 <xsl:call-template name="linkClassQueryURL">
                     <xsl:with-param name="classid" select="./@classid" />
                     <xsl:with-param name="categid" select="./@categid" />
                 </xsl:call-template>
            </xsl:variable>
            <xsl:for-each select="document($classlinkURL)/mycoreclass/categories/category/label[@xml:lang='en']">  
                <xsl:element name="dc:language">
                   <xsl:attribute name="xsi:type">dcterms:ISO639-2</xsl:attribute>
                   <xsl:value-of select="substring-before(./@description,'#')" />
                </xsl:element>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="rights">
        <xsl:for-each select="./metadata/rights/right">
            <xsl:element name="dc:rights">
                <xsl:attribute name="xml:lang"><xsl:value-of select="./@xml:lang" /></xsl:attribute>              
                <xsl:value-of select="." />               
            </xsl:element>
        </xsl:for-each>
        <xsl:for-each select="./metadata/rightslinks/rightslink">
            <xsl:element name="dc:rights">
                <xsl:value-of select="@xlink:href" />
            </xsl:element>
        </xsl:for-each>        
    </xsl:template>
    
</xsl:stylesheet>