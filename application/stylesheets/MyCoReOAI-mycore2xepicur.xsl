<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.2 $ $Date: 2006/07/25 11:26:23 $ -->
<!-- ============================================== -->
<xsl:stylesheet
     version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:urn="http://www.ddb.de/standards/urn"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns="http://www.openarchives.org/OAI/2.0/">
     
    <xsl:output method="xml"
	             encoding="UTF-8"/>
                
    <xsl:param name="ServletsBaseURL" select="''" /> 
    <xsl:param name="JSessionID" select="''" />    
    <xsl:param name="WebApplicationBaseURL" select="''" />
  

    <xsl:template match="/">
        <xsl:apply-templates select="*" />
    </xsl:template>
 
    <xsl:template match="mycoreobject">
        <metadata>  
            <epicur 
                xsi:schemaLocation="urn:nbn:de:1111-2004033116 http://nbn-resolving.de/urn/resolver.pl?urn=urn:nbn:de:1111-2004033116"
                xmlns="urn:nbn:de:1111-2004033116"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
             
                <xsl:variable name="epicurType">
                    <xsl:choose>
                        <xsl:when test="./metadata/urns/urn[@type='url_update_general']">url_update_general</xsl:when>
                        <xsl:when test="./metadata/urns/urn[@type='urn_new']">urn_new</xsl:when>
                        <xsl:when test="./metadata/urns/urn[@type='urn_new_version']">urn_new_version</xsl:when>                                        
                    </xsl:choose>
                </xsl:variable>
                <xsl:call-template name="administrative_data">
                    <xsl:with-param name="epicurType" select="$epicurType" />
                </xsl:call-template>
                 <xsl:call-template name="record">
                    <xsl:with-param name="epicurType" select="$epicurType" />
                </xsl:call-template>             
            </epicur>   
        </metadata>
    </xsl:template>

    <xsl:template name="linkQueryURL">
        <xsl:param name="type" select="'alldocs'"/>
        <xsl:param name="host" select="'local'"/>
        <xsl:param name="id"/>
        <xsl:value-of select="concat($ServletsBaseURL,'MCRQueryServlet',$JSessionID,'?XSL.Style=xml&amp;type=',$type,'&amp;hosts=',$host,'&amp;query=%2Fmycoreobject%5B%40ID%3D%27',$id,'%27%5D')" />
    </xsl:template>

    <xsl:template name="linkDerDetailsURL">
        <xsl:param name="host" select="'local'"/>
        <xsl:param name="id"/>
        <xsl:variable name="derivbase" select="concat($ServletsBaseURL,'MCRFileNodeServlet/',$id,'/')" />
        <xsl:value-of select="concat($derivbase,'?MCRSessionID=',$JSessionID,'&amp;hosts=',$host,'&amp;XSL.Style=xml')" />        
    </xsl:template>

    <xsl:template name="linkClassQueryURL">
        <xsl:param name="type" select="'class'"/>
        <xsl:param name="host" select="'local'"/>
        <xsl:param name="classid" select="''" />
        <xsl:param name="categid" select="''" />
        <xsl:value-of select="concat($ServletsBaseURL,'MCRQueryServlet',$JSessionID,'?XSL.Style=xml&amp;type=',$type,'&amp;hosts=',$host,'&amp;query=%2Fmycoreclass%5B%40ID%3D%27',$classid,'%27%20and%20*%2Fcategory%2F%40ID%3D%27',$categid,'%27%5D')" />
    </xsl:template>

    <xsl:template name="lang">
        <xsl:choose>
            <xsl:when test="./@xml:lang='de'">ger</xsl:when>
            <xsl:when test="./@xml:lang='en'">eng</xsl:when>            
            <xsl:when test="./@xml:lang='fr'">fre</xsl:when>            
            <xsl:when test="./@xml:lang='es'">spa</xsl:when>            
        </xsl:choose>
    </xsl:template>

    <xsl:template name="replaceSubSupTags">
        <xsl:param name="content" select="''" />
        <xsl:choose>
           <xsl:when test="contains($content,'&lt;sub&gt;')">
              <xsl:call-template name="replaceSubSupTags">
                  <xsl:with-param name="content" select="substring-before($content,'&lt;sub&gt;')" />
              </xsl:call-template>
              <xsl:text>_</xsl:text>
              <xsl:call-template name="replaceSubSupTags">
                  <xsl:with-param name="content" select="substring-after($content,'&lt;sub&gt;')" />
              </xsl:call-template>               
           </xsl:when>
           <xsl:when test="contains($content,'&lt;/sub&gt;')">
              <xsl:call-template name="replaceSubSupTags">
                  <xsl:with-param name="content" select="substring-before($content,'&lt;/sub&gt;')" />
              </xsl:call-template>
              <xsl:call-template name="replaceSubSupTags">
                  <xsl:with-param name="content" select="substring-after($content,'&lt;/sub&gt;')" />
              </xsl:call-template>               
           </xsl:when>           
           <xsl:when test="contains($content,'&lt;sup&gt;')">
              <xsl:call-template name="replaceSubSupTags">
                  <xsl:with-param name="content" select="substring-before($content,'&lt;sup&gt;')" />
              </xsl:call-template>
              <xsl:text>^</xsl:text>
              <xsl:call-template name="replaceSubSupTags">
                  <xsl:with-param name="content" select="substring-after($content,'&lt;sup&gt;')" />
              </xsl:call-template>               
           </xsl:when>
           <xsl:when test="contains($content,'&lt;/sup&gt;')">
              <xsl:call-template name="replaceSubSupTags">
                  <xsl:with-param name="content" select="substring-before($content,'&lt;/sup&gt;')" />
              </xsl:call-template>
              <xsl:call-template name="replaceSubSupTags">
                  <xsl:with-param name="content" select="substring-after($content,'&lt;/sup&gt;')" />
              </xsl:call-template>               
           </xsl:when>
           <xsl:otherwise>
               <xsl:value-of select="$content" />
           </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <xsl:template name="administrative_data">
        <xsl:param name="epicurType" select="''" />
        <xsl:element name="administrative_data" namespace="urn:nbn:de:1111-2004033116">
            <xsl:element name="delivery" namespace="urn:nbn:de:1111-2004033116">
               <xsl:element name="update_status" namespace="urn:nbn:de:1111-2004033116">
                   <xsl:attribute name="type"><xsl:value-of select="$epicurType" /></xsl:attribute>
               </xsl:element>
           </xsl:element>
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="url_update_general">
       
    </xsl:template>
    
    <xsl:template name="record">
        <xsl:param name="epicurType" select="''" />
        <xsl:variable name="mycoreobjectID" select="@ID" />        
        <xsl:element name="record" namespace="urn:nbn:de:1111-2004033116">
            <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
                <xsl:attribute name="scheme">urn:nbn:de</xsl:attribute>
                <xsl:value-of select="./metadata/urns/urn[@type=$epicurType]" />
            </xsl:element>
            <xsl:if test="$epicurType='urn_new_version'">
                <xsl:element name="isVersionOf" namespace="urn:nbn:de:1111-2004033116">
                    <xsl:attribute name="scheme">urn:nbn:de</xsl:attribute>
                    <xsl:value-of select="./metadata/urns/urn[@type='urn_first']" />
                </xsl:element>
            </xsl:if>
            <xsl:element name="resource" namespace="urn:nbn:de:1111-2004033116">
                <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
                    <xsl:attribute name="scheme">url</xsl:attribute>
                    <xsl:attribute name="role">primary</xsl:attribute>
                    <xsl:attribute name="origin">original</xsl:attribute>
                    <xsl:attribute name="type">frontpage</xsl:attribute>
                    <xsl:if test="$epicurType = 'urn_new'">
                        <xsl:attribute name="status">new</xsl:attribute>
                    </xsl:if>
                    <xsl:value-of select="concat($WebApplicationBaseURL,'metadata/', $mycoreobjectID)" />
                </xsl:element>
                <xsl:element name="format" namespace="urn:nbn:de:1111-2004033116">
                    <xsl:attribute name="scheme">imt</xsl:attribute>
                    <xsl:value-of select="'text/html'" />
                </xsl:element>
            </xsl:element>
            <xsl:for-each select="./structure/derobjects/derobject">
                   <xsl:variable name="derID" select="./@xlink:href" />
                   <xsl:variable name="filelink" select="concat($WebApplicationBaseURL,'file/',
                     $derID,'/?hosts=local&amp;XSL.Style=xml')" />
		             <xsl:variable name="details" select="document($filelink)" />
      			    <xsl:variable name="isPdfDerivate">
			           	 <xsl:for-each select="$details/mcr_directory/children/child[@type='file']">
	      		          <xsl:if test="./contentType='pdf'">
	             	         <xsl:value-of select="'true'" />
	               	    </xsl:if>
			             </xsl:for-each>
		             </xsl:variable>
		             <xsl:if test="contains($isPdfDerivate,'true')">
                      <xsl:variable name="filenumber" select="$details/mcr_directory/numChildren/total/files" />                  
                      <xsl:choose>
                        <xsl:when test="number($filenumber) &gt; 1">
                           <xsl:element name="resource" namespace="urn:nbn:de:1111-2004033116">
                             <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
	                             <xsl:attribute name="scheme">url</xsl:attribute>
                                <xsl:attribute name="target">transfer</xsl:attribute>
		                          <xsl:value-of select="concat($ServletsBaseURL,'MCRZipServlet?id=',$derID)" />
		                       </xsl:element> 
		                       <xsl:element name="format" namespace="urn:nbn:de:1111-2004033116">
		                       	  <xsl:attribute name="scheme">imt</xsl:attribute>
		                          <xsl:value-of select="'application/zip'" />
                            </xsl:element>
                           </xsl:element>
                        </xsl:when>
                        <xsl:otherwise>
                           <!-- 1 PDF File should be return - the default case -->
		                    <xsl:for-each select="$details/mcr_directory/children/child[@type='file']">
                            <xsl:element name="resource" namespace="urn:nbn:de:1111-2004033116">
	                             <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
	                             	<xsl:attribute name="scheme">url</xsl:attribute>
		                        	  <xsl:value-of select="concat($WebApplicationBaseURL,'file/',$derID,'/',./name)" />
		                    	   </xsl:element> 
		                	       <xsl:element name="format" namespace="urn:nbn:de:1111-2004033116">
		            	           	  <xsl:attribute name="scheme">imt</xsl:attribute>
    		                            <!-- just application/pdf possible -->
		    		                      <xsl:value-of select="concat('application/',./contentType)" />
    		                         </xsl:element>
	                            </xsl:element>                                   
		                    </xsl:for-each>                                   
		                  </xsl:otherwise>
                      </xsl:choose>
                   </xsl:if>                  
             </xsl:for-each>
        </xsl:element>
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
