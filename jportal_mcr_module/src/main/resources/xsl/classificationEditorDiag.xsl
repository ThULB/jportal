<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="resourcePath" select="'/rsc/classifications/'"/>
  <!-- use this to set a specific class - if this is empty, all classifications are loaded-->
  <!-- e.g. docportal_class_00000001 -->
  <xsl:param name="class" select="''"/>
  <xsl:param name="categ" select="''"/>
  <xsl:param name="CurrentLang" select="'de'"/>
  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="returnUrl" select="$WebApplicationBaseURL"/>
  <xsl:param name="showId" select="'false'" />
  
  <xsl:template match="classificationEditorDiag">
  <xsl:message>
  		<xsl:value-of select="'##### Diaglog #####'"/>
  	</xsl:message>

    <xsl:variable name="webPath" select="concat($WebApplicationBaseURL, 'classification/editor')"/>
    <xsl:variable name="jsPath" select="concat($webPath, '/js')"/>
    <xsl:variable name="imgPath" select="concat($webPath, '/images')"/>

    <script type="text/javascript" src="{$jsPath}/ClassificationEditor.js"></script>

    <script type="text/javascript">
    startClassificationEditor({
    	baseUrl : "<xsl:value-of select='$webPath' />" + "/",
    	resourcePath : "<xsl:value-of select='$resourcePath' />",
    	//classificationId : "<xsl:value-of select='$class' />",
    	classificationId : "jportal_class_00000083",
    	categoryId : "<xsl:value-of select='$categ' />",
    	showId : "<xsl:value-of select='$showId' />" === "true",
    	currentLang : "<xsl:value-of select='$CurrentLang' />",
    	jsPath : "<xsl:value-of select='$jsPath' />",
    	buttonID : "diagButton"
    });
    </script>
    <button id="diagButton">View Terms and Conditions</button>

    <div>
      <div dojoType="dijit.layout.ContentPane" id="classMainContainer" style="width: 900px; height: 600px;">
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>