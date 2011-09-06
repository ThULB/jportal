<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
                xmlns:property="xalan://org.mycore.common.xml.PropertyFunctions" 
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:acl="xalan://org.mycore.access.MCRAccessManager">
  <xsl:output method="html" indent="yes" doctype-public="-//IETF//DTD HTML 2.0//EN" />
  <xsl:param name="RequestURL" />
  <xsl:param name="CurrentLang" />
  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="returnUrl" select="$WebApplicationBaseURL"/>

  <xsl:template match="search-laws">
    <xsl:variable name="webPath" select="$WebApplicationBaseURL"/>

    <xsl:variable name="jsPath" select="concat($webPath, 'js')"/>
    <xsl:variable name="imgPath" select="concat($webPath, 'images')"/>

	<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/dojo/1.5/dojo/dojo.xd.js" djConfig="parseOnLoad: true"></script>
	<script type="text/javascript" src="{$jsPath}/searchMask.js"></script>
	<script type="text/javascript" src="{$jsPath}/fulltext.js"></script>
	<script type="text/javascript" src="{$jsPath}/years.js"></script>
	<script type="text/javascript" src="{$jsPath}/territories.js"></script>
	
	<link type="text/css" rel="stylesheet" href="http://ajax.googleapis.com/ajax/libs/dojo/1.5/dijit/themes/claro/claro.css"/>

	<div class="claro">
		<!-- <form id="search_form" action="/servlets/SearchLawsServlet" method="post"> -->
		<form id="search_form">
			<div id="searchWrap">
			</div>

		</form>
		<div id="buttons">
			<button type="button" dojoType="dijit.form.Button" onClick="submitForm">Search</button>
		</div>
		</div>
  </xsl:template>
  

<!-- zugehoeriges XSL integrieren: build/webapps/WEB-INF/classes/mycore.properties > include XSL
-->
  
</xsl:stylesheet>