<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.4 $ $Date: 2007-08-16 06:51:55 $ -->
<!-- ============================================== -->
    <!--
        + | This stylesheet controls the Web-Layout of the MCRClassificationBrowser Servlet. | | This Template is embedded as a Part in the XML-Site,
        configurated in the Classification | section of the mycore.properties. | The complete XML stream is sent to the Layout Servlet and finally handled by
        this stylesheet. | | Authors: A.Schaar | Last changes: 2004-30-10 +
    -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    exclude-result-prefixes="xlink">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:variable name="Navigation.title" select="i18n:translate('component.classhandler.cal.title')" />
    <xsl:variable name="MainTitle" select="i18n:translate('common.titles.mainTitle')" />
    <xsl:variable name="PageTitle" select="$Navigation.title" />
    <xsl:param name="filterChecked" select="false"/>

<!-- The main template -->
    <xsl:template match="classificationBrowser">
        <xsl:variable name="startPath" select="startPath" />
        <xsl:variable name="hrefstart" select="concat($WebApplicationBaseURL, 'browse/', startPath,$HttpSession)" />
        <xsl:variable name="path" select="concat($WebApplicationBaseURL, 'browse/', uri)" />
        <xsl:variable name="filterJournalID" select="concat('+and+(journalID_vol+=+', $journalsID, ')')" />
        <script type="text/javascript">
            <xsl:value-of select="'&lt;!--'" />
            function filterSearch() {
              for (i=0; i &lt; document.anchors.length; i++) {
                help = document.anchors[i].attributes["href"].nodeValue;
                document.anchors[i].attributes["href"].nodeValue = document.anchors[i].attributes["altHref"].nodeValue;
                document.anchors[i].attributes["altHref"].nodeValue = help;
              }
            }
            //-->
        </script>

    <div id="classificationBrowser" >
   <p class="classBrowserHeadline">
       <a href='{$hrefstart}'>
           <xsl:value-of select="description" />
       </a>
   </p>
   <!-- IE Fix for padding and border -->
   <!-- CSS style can be found in style_general.css from template_calendar -->
   <table class="classiSearchOption">
        <tr>
            <td><xsl:value-of select="concat(i18n:translate('component.classhandler.cal.label.browsAll'),': ')"/> </td>
            <td>
                <xsl:choose>
                  <xsl:when test="$filterChecked='true'">
                    <input type="checkbox" onclick="filterSearch()" checked="checked" />
                  </xsl:when>
                  <xsl:otherwise>
                    <input type="checkbox" onclick="filterSearch()" />
                  </xsl:otherwise>
                </xsl:choose>
                
            </td>
        </tr>
    </table>
<hr/>


<div id="navigationtree">
<!-- Navigation table -->


<table id="browseClass" cellspacing="0" cellpadding="0">
	
<xsl:variable name="type" select="startPath" />
<xsl:variable name="search" select="searchField" />

<xsl:for-each select="navigationtree">	
 <xsl:variable name="predecessor" select="@predecessor" />
 <xsl:variable name="classifID" select="@classifID" />
 <xsl:variable name="view" select="@view" />
 <xsl:variable name="restriction" select="@restriction" />
 <xsl:for-each select="row">
   <xsl:variable name="href_folder" select="concat($WebApplicationBaseURL, 'browse', col[2]/@searchbase, $HttpSession)" />
   <xsl:variable name="href_folder_filter" select="concat($WebApplicationBaseURL, 'browse', col[2]/@searchbase, $HttpSession, '?XSL.filterChecked=true')" />
   
   <xsl:variable name="actnode" select="position()" />  
   <xsl:variable name="query">
    <xsl:value-of select="concat('(',$search,'1+=+&quot;', normalize-space(col[2]/@lineID), '&quot;)')"/>
<!--<xsl:value-of select="concat('+and+(',$search,'2+=+', 'site)')"/> -->
    <xsl:value-of select="''"/>
    <xsl:if test="string-length(../@doctype)>0">
      <xsl:value-of select="concat('+and+', ../@doctype)"/>
    </xsl:if>
   </xsl:variable>
   <xsl:variable name="href2All" select="concat($ServletsBaseURL, 'MCRSearchServlet', $HttpSession, '?numPerPage=10','&amp;mask=browse/',$type,'&amp;query=',$query)" />
   <xsl:variable name="href2" select="concat($ServletsBaseURL, 'MCRSearchServlet', $HttpSession, '?numPerPage=10','&amp;mask=browse/',$type,'&amp;query=',$query,$filterJournalID)" />
<!--   <xsl:variable name="img1"  select="concat($WebApplicationBaseURL, 'images/', col[1]/@folder1, '.gif')" />-->
   <xsl:variable name="img2"  select="concat($WebApplicationBaseURL, 'images/', col[1]/@folder2, '.gif')" />
   <xsl:variable name="img3"  select="concat($WebApplicationBaseURL, 'images/folder_blank.gif')" />
   <xsl:variable name="img1">
        <xsl:choose>
            <xsl:when test="col[1]/@folder1 = 'folder_plus'">
                <xsl:value-of select="concat($WebApplicationBaseURL, 'images/arrow_close.png')" />
            </xsl:when>
            <xsl:when test="col[1]/@folder1 = 'folder_minus'">
                <xsl:value-of select="concat($WebApplicationBaseURL, 'images/arrow_open.png')" />
            </xsl:when>
            <xsl:when test="col[1]/@folder1 = 'folder_plain'">
                <xsl:value-of select="concat($WebApplicationBaseURL, 'images/keypoint.png')" />
            </xsl:when>
        </xsl:choose>
   </xsl:variable>
   
   <xsl:variable name="childpos" select="col[1]/@childpos" />	  
	  
   <tr class="result" valign="top" >
        <!-- CSS style can be found in style_general.css from template_calendar -->
      <td class="classiBrowseImage" >
        <xsl:call-template name="lineLevelLoop">
          <xsl:with-param name="anz" select="col[1]/@lineLevel" />
          <xsl:with-param name="img" select="$img3" />
        </xsl:call-template>
        <xsl:choose>
         <xsl:when test="col[1]/@plusminusbase">
           <xsl:choose>
              <xsl:when test="$filterChecked='true'">
                <a name="folder" href="{$href_folder_filter}" altHref="{$href_folder}" ><img border="0" src='{$img1}' /></a>
              </xsl:when>
              <xsl:otherwise>
                <a name="folder" href="{$href_folder}" altHref="{$href_folder_filter}" ><img border="0" src='{$img1}' /></a>  
              </xsl:otherwise>
           </xsl:choose>
            
		 </xsl:when>
		 <xsl:otherwise>
		  <img border="0" src='{$img1}' />
         </xsl:otherwise>
        </xsl:choose>
       </td >
       <td class="desc">
          <xsl:choose>
            <xsl:when test="col[2]/@hasLinks='true'">
              <xsl:choose>
                <xsl:when test="$filterChecked='true'">
                  <a name="classLink" href='{$href2All}' altHref="{$href2}"><xsl:value-of select="col[2]/text()" /></a>
                </xsl:when>
                <xsl:otherwise>
                  <a name="classLink" href='{$href2}' altHref="{$href2All}"><xsl:value-of select="col[2]/text()" /></a>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="col[2]/text()" />
            </xsl:otherwise>
          </xsl:choose>
       </td>
       <td class="desc">
	      <xsl:choose>
            <xsl:when test="col[2]/comment != ''">
              <i> (<xsl:apply-templates select="col[2]/comment" />) </i>
            </xsl:when>
            <xsl:otherwise>&#160;&#160;           </xsl:otherwise>
          </xsl:choose>
       </td>
     </tr>
	</xsl:for-each>
  </xsl:for-each>
 </table>
</div>	 
</div>
</xsl:template>



<!-- - - - - - - - - Identity Transformation  - - - - - - - - - -->

<xsl:template match='@*|node()'>
  <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
  </xsl:copy>
</xsl:template>

<xsl:template match="comment">
  <xsl:apply-templates select='@*|node()'/>
</xsl:template>

<xsl:template name="lineLevelLoop">
  <xsl:param name="anz" />
  <xsl:param name="img" />

  <xsl:if test="$anz > 0">
    <img border="0" width="10" src='{$img}' />
    <xsl:call-template name="lineLevelLoop">
      <xsl:with-param name="anz" select="$anz - 1" />
      <xsl:with-param name="img" select="$img" />
    </xsl:call-template>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
