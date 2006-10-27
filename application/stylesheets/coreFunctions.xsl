<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.35 $ $Date: 2006/10/05 12:55:18 $ -->
<!-- ============================================== -->
<!-- Authors: Thomas Scheffler (yagee) -->
<!-- Authors: Andreas Trappe (lezard) -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" 
      exclude-result-prefixes="xlink">
    <!--
    Template: UrlSetParam
    synopsis: Inserts a $HttpSession to an URL
    param:

    url: URL to include the session
    -->
    <xsl:template name="UrlAddSession">
        <xsl:param name="url"/>
        <!-- There are two possibility for a parameter to appear in an URL:
            1.) after a ? sign
            2.) after a & sign
            In both cases the value is either limited by a & sign or the string end
        //-->
        <xsl:choose>
            <xsl:when test="starts-with($url,$WebApplicationBaseURL)">
                <!--The document is on our server-->
                <xsl:variable name="pathPart">
                    <xsl:choose>
                        <xsl:when test="contains($url,'?')">
                            <xsl:value-of select="substring-before($url,'?')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$url"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="queryPart">
                    <xsl:value-of select="substring-after($url,$pathPart)"/>
                </xsl:variable>
                <xsl:value-of select="concat($pathPart,$HttpSession,$queryPart)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$url"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--
    Template: UrlSetParam
    synopsis: Replaces a parameter value or adds a parameter to an URL
    param:

    url: URL to contain the parameter and value
    par: name of the parameter
    value: new value
    -->
    <xsl:template name="UrlSetParam">
        <xsl:param name="url"/>
        <xsl:param name="par"/>
        <xsl:param name="value"/>
        <!-- There are two possibility for a parameter to appear in an URL:
            1.) after a ? sign
            2.) after a & sign
            In both cases the value is either limited by a & sign or the string end
        //-->
        <xsl:variable name="asFirstParam">
            <xsl:value-of select="concat('?',$par,'=')"/>
        </xsl:variable>
        <xsl:variable name="asOtherParam">
            <xsl:value-of select="concat('&amp;',$par,'=')"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($url,$asFirstParam) or contains($url,$asOtherParam)">
            <!-- Parameter is present -->
                <xsl:variable name="asParam">
                    <xsl:choose>
                        <xsl:when test="contains($url,$asFirstParam)">
                            <!-- Parameter is right after a question mark //-->
                            <xsl:value-of select="$asFirstParam"/>
                        </xsl:when>
                        <xsl:when test="contains($url,asOtherParam)">
                            <!-- Parameter is right after a & sign //-->
                            <xsl:value-of select="$asOtherParam"/>
                        </xsl:when>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="newurl">
                    <xsl:value-of select="substring-before($url,$asParam)"/>
                    <xsl:value-of select="$asParam"/>
                    <xsl:value-of select="$value"/>
                    <xsl:if test="contains(substring-after($url,$asParam),'&amp;')">
                        <!--OK now we know that there are parameters left //-->
                        <xsl:value-of select="concat('&amp;',substring-after(substring-after($url,$asParam),'&amp;'))"/>
                    </xsl:if>
                </xsl:variable>
                <xsl:value-of select="$newurl"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- The parameter is not yet specified //-->
                <xsl:choose>
                    <xsl:when test="contains($url,'?')">
                        <!-- Other parameters are present //-->
                        <xsl:value-of select="concat($url,'&amp;',$par,'=',$value)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- No other parameter are present //-->
                        <xsl:value-of select="concat($url,'?',$par,'=',$value)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--
    Template: UrlGetParam
    synopsis: Gets the value of a given parameter from a specific URL
    param:

    url: URL containing the parameter and value
    par: name of the parameter
    -->
    <xsl:template name="UrlGetParam">
        <xsl:param name="url"/>
        <xsl:param name="par"/>
        <!-- There are two possibility for a parameter to appear in an URL:
        1.) after a ? sign
        2.) after a & sign
        In both cases the value is either limited by a & sign or the string end
        //-->
        <xsl:variable name="afterParam">
            <xsl:choose>
                <xsl:when test="contains($url,concat('?',$par,'='))">
                    <!-- Parameter is right after a question mark //-->
                    <xsl:value-of select="substring-after($url,concat('?',$par,'='))"/>
                </xsl:when>
                <xsl:when test="contains($url,concat('&amp;',$par,'='))">
                    <!-- Parameter is right after a & sign //-->
                    <xsl:value-of select="substring-after($url,concat('&amp;',$par,'='))"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- The parameter is not specified //-->
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($afterParam,'&amp;')">
                <!-- cut off other parameters -->
                <xsl:value-of select="substring-before($afterParam,'&amp;')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$afterParam"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!--
    Template: UrlDelParam
    synopsis: Removes the parameter and value of a given parameter from a specific URL

    url: URL containing the parameter and value
    par: name of the parameter
    -->
    <xsl:template name="UrlDelParam">
        <xsl:param name="url"/>
        <xsl:param name="par"/>

        <xsl:choose>
            <xsl:when test="contains($url,concat($par,'='))">
                <!-- get value of par's value -->
                <xsl:variable name="valueOfPar">
                    <!-- cut off everything before value -->
                    <xsl:variable name="valueBlured" >
                        <xsl:choose>
                            <xsl:when test="contains($url,concat('?',$par,'=')) ">
                                <xsl:value-of select="substring-after($url,concat('?',$par,'='))" />
                            </xsl:when>
                            <xsl:when test="contains($url,concat('&amp;',$par,'='))">
                                <xsl:value-of select="substring-after($url,concat('&amp;',$par,'='))"/>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:choose>
                        <!-- found value is not the last one in $url -->
                        <xsl:when test="contains($valueBlured,'&amp;')">
                            <xsl:value-of select="substring-before($valueBlured,'&amp;')" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$valueBlured"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>              
                <xsl:variable name="parAndVal">
                    <xsl:value-of select="concat($par,'=',$valueOfPar)"/>
                </xsl:variable>

                <xsl:choose>
                    <!-- more params append afterwards -->                              
                    <xsl:when test="contains(substring-after($url,$parAndVal),'&amp;')">
                        <xsl:choose>
                            <!-- $par is not the first in list -->
                            <xsl:when 
                                                test="contains(substring($url,string-length(substring-before($url,$parAndVal)+1),string-length(substring-before($url,$parAndVal)+1)),'&amp;')">
                                <xsl:value-of 
                                                      select="concat(substring-before($url,concat('&amp;',$parAndVal)),substring-after($url,$parAndVal))"/>
                            </xsl:when>
                            <!-- $par is logical the first one in $url-->
                            <xsl:otherwise>
                                <xsl:value-of select="concat(substring-before($url,$parAndVal),substring-after($url,concat($parAndVal,'&amp;')))"/>
                            </xsl:otherwise>
                        </xsl:choose>           
                    </xsl:when>
                    <!-- no more params append afterwards -->
                    <xsl:otherwise>
                        <xsl:value-of select="substring($url,1, (string-length($url)-(string-length($parAndVal)+1))) "/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$url"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>      
      
    <!--
    Template: ShortenText
    synopsis: Cuts text after a maximum of given chars but at end of the word that would be affected. If the text is shortened "..." is appended.
    param:

    text: the text to be shorten
    length: the number of chars
    -->
    <xsl:template name="ShortenText">
        <xsl:param name="text"/>
        <xsl:param name="length"/>
        <xsl:choose>
            <xsl:when test="string-length($text) > $length">
                <xsl:value-of select="concat(substring($text,1,$length),substring-before(substring($text,($length+1)),' '),'...')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
      
    <!--
    Template: ClassCategLink
    synopsis: Generates a link to get a classification
    param:

    classid: classification id
    categid: category id
    host: host to query
    -->
    <xsl:template name="ClassCategLink">
        <xsl:param name="classid"/>
        <xsl:param name="categid"/>
        <xsl:param name="host" select="'local'"/>
        <xsl:choose>
          <xsl:when test="$host != 'local'">
            <xsl:value-of
              select="concat('mcrws:operation=MCRDoRetrieveClassification&amp;level=0&amp;type=children&amp;classid=',$classid,'&amp;categid=',$categid,'&amp;format=metadata','&amp;host=',$host)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat('classification:metadata:0:children:',$classid,':',$categid)" />
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!--
    Template: ClassLink
    synopsis: Generates a link to get a classification
    param:

    classid: classification id
    host: host to query
    -->
    <xsl:template name="ClassLink">
        <xsl:param name="classid"/>
        <xsl:param name="host" select="'local'"/>
        <xsl:choose>
          <xsl:when test="$host != 'local'">
            <xsl:value-of
              select="concat('mcrws:operation=MCRDoRetrieveClassification&amp;level=-1&amp;type=children&amp;classid=',$classid,'&amp;format=metadata','&amp;host=',$host)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat('classification:metadata:-1:children:',$classid)" />
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--
    Tamplate: PageGen
    synopsis: returns a list of links to access other pages of a result list
    
    parameters:
        i: running indicator - leave untouched
        type: document type
        href: baselink to access resultlists
        size: howmany results per page
        offset: start at which result offset
        currentpage: what is the current page displayed?
        totalpage: how many pages exist?
    -->
    <xsl:template name="PageGen">
        <xsl:param name="i"           select="1" />
        <xsl:param name="type"                   />
        <xsl:param name="href"        select="concat($WebApplicationBaseURL, 'servlets/MCRQueryServlet',$JSessionID,'?mode=CachedResultList&amp;type=', $type)" />
        <xsl:param name="size"                   />
        <xsl:param name="currentpage"            />
        <xsl:param name="totalpage"              />
        <xsl:variable name="PageWindowSize" select="10" />
        <!-- jumpSize is to determine the pages to be skipped -->
        <xsl:variable name="jumpSize">
            <xsl:choose>
                <!-- current printed page number is smaller than current displayed page -->
                <xsl:when test="$i &lt; $currentpage"> 
                    <xsl:choose>
                        <!-- This is to support a bigger PageWindow at the end of page listing and
                        to skip a jump of 2 -->
                        <xsl:when test="(($totalpage - $PageWindowSize - 1) &lt;= $i) or
                        (($currentpage - floor(($PageWindowSize -1) div 2) - 1) = 2)">
                            <xsl:value-of select="1"/>
                        </xsl:when>
                        <!-- This is to support a bigger PageWindow at the begin of page listing -->
                        <xsl:when test="($totalpage - $currentpage) &lt; $PageWindowSize">
                            <xsl:value-of select="($totalpage - $PageWindowSize - 1)"/>
                        </xsl:when>
                        <xsl:when test="(($currentpage - $i) &lt;= floor(($PageWindowSize -1) div 2))">
                            <xsl:value-of select="1"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="($currentpage - floor(($PageWindowSize -1) div 2) - 1)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:when test="$i &gt; $currentpage"> 
                    <xsl:choose>
                        <!-- jump only one if your near currentpage,
                        or at last page 
                        or to support bigger window at beginning
                        or to skip a jump of 2 -->
                        <xsl:when test="( (($i - $currentpage) &lt; round(($PageWindowSize -1) div 2)) or ($i = $totalpage) or ($currentpage &lt;=$PageWindowSize and $i &lt;= $PageWindowSize) or ($totalpage - $i = 2))">
                            <xsl:value-of select="1"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="($totalpage - $i)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="1"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="running">
            <xsl:if test="$i &lt;= $totalpage">
                <xsl:text>true</xsl:text>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="offset" select="($i - 1) * $size"/>
        <xsl:if test="$running='true'">
            <xsl:if test="$i=$currentpage"><xsl:text>[</xsl:text></xsl:if>
            <a href="{concat($href, '&amp;offset=', $offset, '&amp;size=', $size)}">
                <xsl:value-of select="$i"/>
            </a>
            <xsl:if test="$i=$currentpage"><xsl:text>]</xsl:text></xsl:if>
            <xsl:if test="$jumpSize &gt; 1"><xsl:text>&#160;...</xsl:text></xsl:if>
            <xsl:text>&#160;</xsl:text><!--
                <xsl:comment>
                    <xsl:value-of select="concat('$i=',$i,                                       ' $totalpage=',$totalpage,' $jumpSize=',$jumpSize,' floor=',floor(($PageWindowSize -1) div 2),' round=',round(($PageWindowSize -1) div 2))"/>
                </xsl:comment>
                <xsl:text>
                </xsl:text>  -->
            <xsl:call-template name="PageGen">
                <xsl:with-param name="i"           select="$i + $jumpSize"/>
                <xsl:with-param name="href"        select="$href"         />
                <xsl:with-param name="size"        select="$size"         />
                <xsl:with-param name="currentpage" select="$currentpage"  />
                <xsl:with-param name="totalpage"   select="$totalpage"    />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <!--
    Template: PageGenNew
    synopsis: returns a list of links to access other pages of a result list
    
    parameters:
        i: running indicator - leave untouched
        id: editorID
        href: baselink to access resultlists
        size: how many results per page
        offset: start at which result offset
        currentpage: what is the current page displayed?
        totalpage: how many pages exist?
    -->
    <xsl:template name="PageGenNew">
    <!--
    MCRSearchServlet?mode=results&amp;id={@id}&amp;numPerPage={@numPerPage}&amp;page={number(@page)+1}
    -->
        <xsl:param name="i"           select="1" />
        <xsl:param name="href"        select="concat($ServletsBaseURL, 'MCRSearchServlet',$HttpSession,'?mode=results')" />
        <xsl:param name="id"                   />
        <xsl:param name="size"                   />
        <xsl:param name="currentpage"            />
        <xsl:param name="totalpage"              />
        <xsl:variable name="PageWindowSize" select="10" />
        <!-- jumpSize is to determine the pages to be skipped -->
        <xsl:variable name="jumpSize">
            <xsl:choose>
                <!-- current printed page number is smaller than current displayed page -->
                <xsl:when test="$i &lt; $currentpage"> 
                    <xsl:choose>
                        <!-- This is to support a bigger PageWindow at the end of page listing and
                        to skip a jump of 2 -->
                        <xsl:when test="(($totalpage - $PageWindowSize - 1) &lt;= $i) or
                        (($currentpage - floor(($PageWindowSize -1) div 2) - 1) = 2)">
                            <xsl:value-of select="1"/>
                        </xsl:when>
                        <!-- This is to support a bigger PageWindow at the begin of page listing -->
                        <xsl:when test="($totalpage - $currentpage) &lt; $PageWindowSize">
                            <xsl:value-of select="($totalpage - $PageWindowSize - 1)"/>
                        </xsl:when>
                        <xsl:when test="(($currentpage - $i) &lt;= floor(($PageWindowSize -1) div 2))">
                            <xsl:value-of select="1"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="($currentpage - floor(($PageWindowSize -1) div 2) - 1)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:when test="$i &gt; $currentpage"> 
                    <xsl:choose>
                        <!-- jump only one if your near currentpage,
                        or at last page 
                        or to support bigger window at beginning
                        or to skip a jump of 2 -->
                        <xsl:when test="( (($i - $currentpage) &lt; round(($PageWindowSize -1) div 2)) or ($i = $totalpage) or ($currentpage &lt;=$PageWindowSize and $i &lt;= $PageWindowSize) or ($totalpage - $i = 2))">
                            <xsl:value-of select="1"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="($totalpage - $i)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="1"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="running">
            <xsl:if test="$i &lt;= $totalpage">
                <xsl:text>true</xsl:text>
            </xsl:if>
        </xsl:variable>
        <xsl:if test="$running='true'">
            <xsl:if test="$i=$currentpage"><xsl:text>[</xsl:text></xsl:if>
            <a href="{concat($href, '&amp;id=',$id,'&amp;page=',$i, '&amp;numPerPage=', $size)}">
                <xsl:value-of select="$i"/>
            </a>
            <xsl:if test="$i=$currentpage"><xsl:text>]</xsl:text></xsl:if>
            <xsl:if test="$jumpSize &gt; 1"><xsl:text>&#160;...</xsl:text></xsl:if>
            <xsl:text>&#160;</xsl:text><!--
                <xsl:comment>
                    <xsl:value-of select="concat('$i=',$i,' $totalpage=',$totalpage,' $jumpSize=',$jumpSize,' floor=',floor(($PageWindowSize -1) div 2),' round=',round(($PageWindowSize -1) div 2))"/>
                </xsl:comment>
                <xsl:text>
                </xsl:text>-->
            <xsl:call-template name="PageGenNew">
                <xsl:with-param name="i"           select="$i + $jumpSize"/>
                <xsl:with-param name="id"          select="$id"           />
                <xsl:with-param name="href"        select="$href"         />
                <xsl:with-param name="size"        select="$size"         />
                <xsl:with-param name="currentpage" select="$currentpage"  />
                <xsl:with-param name="totalpage"   select="$totalpage"    />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <!-- Template typeOfObjectID
    synopsis: returns the type of the ObjectID submitted usally the second part of the ID
    
    parameters:
    id: MCRObjectID
    -->
    <xsl:template name="typeOfObjectID">
        <xsl:param name="id" />
        <xsl:variable name="delim" select="'_'"/>
        <xsl:value-of select="substring-before(substring-after($id,$delim),$delim)"/>
    </xsl:template>
    <!-- Template mappedTypeOfObjectID
    synopsis: returns the mapped type of the ObjectID submitted usally the second part of the ID.
        Use this when calling an ObjectMetaData or ResultList page.
    
    parameters:
    id: MCRObjectID
    -->
    <xsl:template name="mappedTypeOfObjectID">
        <xsl:param name="id" />
        <xsl:variable name="type">
            <xsl:call-template name="typeOfObjectID">
                <xsl:with-param name="id" select="$id"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="mapping">
            <xsl:call-template name="getValue">
                <xsl:with-param name="pairs" select="$TypeMapping"/>
                <xsl:with-param name="name" select="$type"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- the mapping -->
        <xsl:choose>
            <xsl:when test="string-length($mapping) > 0">
                <xsl:value-of select="$mapping"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$type"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Template getValue
    synopsis: returns the value of name value pairs in this layout:
        name1:value1;name2:value2;...;
    parameters:
    pairs: the name value pairs as described above
    name: a name to return the value for
    -->
    <xsl:template name="getValue">
        <xsl:param name="pairs"/>
        <xsl:param name="name"/>
        <xsl:if test="string-length($pairs) > string-length($name) and string-length($name) > 0">
            <xsl:value-of select="substring-before(substring-after($pairs,concat($name,':')),';')"/>
        </xsl:if>
    </xsl:template>

    <!-- Template selectLang
    synopsis: returns $CurrentLang if $nodes[lang($CurrentLang)] is not empty, else $DefaultLang

    parameters:
    nodes: the nodeset to check
    -->
    <xsl:template name="selectLang">
        <xsl:param name="nodes" />
        <xsl:choose>
            <xsl:when test="$nodes[lang($CurrentLang)]">
                <xsl:value-of select="$CurrentLang"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$DefaultLang"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Template selectPresentLang
    synopsis: returns the result of selectLang if nodes for that language are present, else returns a language for which nodes a present

    parameters:
    nodes: the nodeset to check
    -->
    <xsl:template name="selectPresentLang">
        <xsl:param name="nodes" />
        <xsl:variable name="check">
            <xsl:call-template name="selectLang">
                <xsl:with-param name="nodes" select="$nodes"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$nodes[lang($check)]">
                <xsl:value-of select="$check"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$nodes[1]/@xml:lang"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
      <!-- =================================================================================================== -->    
<!--
Template: wcms.getBrowserAddress
synopsis: The template will be used to identify the currently selected menu entry and the belonging element item/@href in the navigationBase
These strategies are embarked on:
1. RequestURL - lang ?= @href - lang
2. RequestURL - $WebApplicationBaseURL - lang ?= @href - lang
3. Root element ?= item//dynamicContentBinding/rootTag
-->
  
      <xsl:template name="getBrowserAddress">
		  <xsl:choose>
			  <xsl:when test="$href=''">
				<!--remove lastPage-->
		            <xsl:variable name="RequestURL.lastPageDel" >
					<xsl:call-template name="UrlDelParam"> 
					      <xsl:with-param name="url" select="$RequestURL"  />
			                  <xsl:with-param name="par" select="'XSL.lastPage.SESSION'" /> 
		                  </xsl:call-template>
		            </xsl:variable>	
		            <xsl:variable name="WebApplicationBaseURL.lastPageDel" >
					<xsl:call-template name="UrlDelParam"> 
					      <xsl:with-param name="url" select="$WebApplicationBaseURL"  />
			                  <xsl:with-param name="par" select="'XSL.lastPage.SESSION'" /> 
		                  </xsl:call-template>
		            </xsl:variable>	
				<!--end: remove lastPage-->
		                        		
		            <xsl:variable name="RequestURL.langDel" >
					<xsl:call-template name="UrlDelParam"> 
					      <xsl:with-param name="url" select="$RequestURL.lastPageDel"  />
			                  <xsl:with-param name="par" select="'lang'" /> 
		                  </xsl:call-template>
		            </xsl:variable>
		            <xsl:variable name="RequestURL.WebURLDel" >
					<xsl:value-of select="concat('/',substring-after($RequestURL.lastPageDel,$WebApplicationBaseURL.lastPageDel))" />
		            </xsl:variable>
		            <xsl:variable name="RequestURL.WebURLDel.langDel" >
					<xsl:call-template name="UrlDelParam"> 
					      <xsl:with-param name="url" select="$RequestURL.WebURLDel"  />
			                  <xsl:with-param name="par" select="'lang'" /> 
		                  </xsl:call-template>
		            </xsl:variable>
		            
		            <!-- test if navigation.xml contains the current browser address -->
		            <!--look for $browserAddress_href-->
		            <xsl:variable name="browserAddress_href" >
		                  <!-- verify each item  -->
		                  <xsl:for-each select="$loaded_navigation_xml//item[@href]" >
		                        <!-- remove par lang from @href -->
		                        <xsl:variable name="href.langDel">
							<xsl:call-template name="UrlDelParam"> 
							      <xsl:with-param name="url" select="current()/@href"  />
					                  <xsl:with-param name="par" select="'lang'" /> 
				                  </xsl:call-template>                              
		                        </xsl:variable>
		
			                  <xsl:if test="( $RequestURL.langDel = $href.langDel )
		                              or
		                              ($RequestURL.WebURLDel.langDel = $href.langDel) ">
		                              <xsl:value-of select="@href" />
		                        </xsl:if>
		                        
			            </xsl:for-each>
		                  <!-- END OF: verify each item -->
		            </xsl:variable>
		            <!-- end: look for $browserAddress_href-->            
		
		            <!-- look for $browserAddress_dynamicContentBinding -->
		            <xsl:variable name="browserAddress_dynamicContentBinding" >
		                  <xsl:if test="  ($browserAddress_href = '') " >
		                        <!-- assign name of rootTag -> $rootTag -->
		                        <xsl:variable name="rootTag" select="name(*)" />
		                        <xsl:for-each select="$loaded_navigation_xml//dynamicContentBinding/rootTag" >
		                              <xsl:if test=" current() = $rootTag " >
		                                    <xsl:for-each select="ancestor-or-self::*[@href]">
		                                          <xsl:if test="position()=last()" >
		                                                <xsl:value-of select="@href" />
		                                          </xsl:if>
		                                    </xsl:for-each>
		                              </xsl:if>
		                        </xsl:for-each>
		                  </xsl:if>
		            </xsl:variable>
		            <!-- END OF: look $browserAddress_dynamicContentBinding -->
		            
		            <!-- look for $lastPage -->
		            <xsl:variable name="browserAddress_lastPage">
		              <xsl:if
		                test=" ($browserAddress_href = '') and ($browserAddress_dynamicContentBinding = '') ">
		                <xsl:value-of xmlns:decoder="xalan://java.net.URLDecoder" select="decoder:decode($lastPage,'UTF-8')" />
		              </xsl:if>
		            </xsl:variable>
		            <!-- END OF: look $browserAddress_lastPage -->                        
		            
		
		            <!-- END OF: test if navigation.xml contains the current browser address -->
		            <!-- assign right browser address -->
		            <xsl:choose>
		                  <xsl:when test=" $browserAddress_href != '' " >
		                        <xsl:value-of select="$browserAddress_href" />
		                  </xsl:when>
		                  <xsl:when test=" $browserAddress_dynamicContentBinding != '' " >
		                        <xsl:value-of select="$browserAddress_dynamicContentBinding" />
		                  </xsl:when>                  
		                  <xsl:when test=" $browserAddress_lastPage != '' " >
		                        <xsl:value-of select="$browserAddress_lastPage" />
		                  </xsl:when>                  
		            </xsl:choose>
		            <!-- END OF: assign right browser address -->				  
			  </xsl:when>
			  <!--archived page called-->
			  <xsl:otherwise>
				  <xsl:value-of select="$href"/>
			  </xsl:otherwise>
		  </xsl:choose>


      </xsl:template>
      <!-- =================================================================================================== -->
      <xsl:template name="getTemplate">
            <xsl:param name="browserAddress" />            
            <xsl:param name="navigationBase" />                        
           
           <xsl:variable name="template_tmp">
	            <!-- point to rigth item -->
	            <xsl:for-each select="$loaded_navigation_xml//item[@href = $browserAddress]" >
	                  <!-- collect @template !='' entries along the choosen axis -->
	                  <xsl:if test="position()=last()" >
	                  	<xsl:for-each select="ancestor-or-self::*[  @template != '' ]">
	                        	<xsl:if test="position()=last()" >
	                              		<xsl:value-of select="@template" />
	                        	</xsl:if>
	                  	</xsl:for-each>
			  </xsl:if>
	                  <!-- END OF: collect @template !='' entries along the choosen axis -->
	            </xsl:for-each>
	            <!-- END OF: point to rigth item -->
            </xsl:variable>
	            
		<xsl:choose>
                  <!-- assign appropriate template -->
                  <xsl:when test="$template_tmp != ''">
                  	<xsl:value-of select="$template_tmp" />                        
                  </xsl:when>
                  <!-- default template -->
                  <xsl:otherwise>
                        <xsl:value-of select="$loaded_navigation_xml/@template" />
                  </xsl:otherwise>
            </xsl:choose>
            
      </xsl:template>
      <!-- =================================================================================================== -->	
    <!--
    Template: formatISODate
    synopsis: formates the given date (ISO 8601) to the defined local format
    param:

    date: date in ISO 8601 format
    format: target format (must suit to SimpleDateFormat)
    locale: use local, e.g. "de" "en"
    -->
    <xsl:template name="formatISODate">
        <xsl:param name="date"/>
        <xsl:param name="format"/>
        <xsl:param name="locale" select="$CurrentLang"/>
        <xsl:value-of xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" select="mcrxml:formatISODate( string( $date ),string( $format ),string( $locale ) )" />
    </xsl:template>


<!-- ====================================================================================={

section: template name="ersetzen"

	Search for a part in a string and replace it

parameters:
	vorlage - the original string
	raus - the searched string to replace
	rein - the new string

}===================================================================================== -->

	<xsl:template name="ersetzen">
		<xsl:param name="vorlage"/>
		<xsl:param name="raus" />
		<xsl:param name="rein" />
		<xsl:choose>
			<xsl:when test="contains($vorlage,$raus)">
				<xsl:value-of select="concat(substring-before($vorlage,$raus),$rein)"/>
					<xsl:call-template name="ersetzen">
						<xsl:with-param name="vorlage" select="substring-after($vorlage,$raus)"/>
						<xsl:with-param name="raus" select="$raus"/>
						<xsl:with-param name="rein" select="$rein"/>
					</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$vorlage"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template> 

<!-- =================================================================================== -->

</xsl:stylesheet>
