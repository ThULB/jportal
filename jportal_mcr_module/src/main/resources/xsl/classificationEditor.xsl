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

  <xsl:template match="classificationEditor">

    <xsl:variable name="webPath" select="concat($WebApplicationBaseURL, 'classification/editor')"/>
    <xsl:variable name="jsPath" select="concat($webPath, '/js')"/>
    <xsl:variable name="imgPath" select="concat($webPath, '/images')"/>

    <!-- do includes -->
    <script type="text/javascript">
      var classification = classification || {};
      var webApplicationBaseURL = "<xsl:value-of select='$WebApplicationBaseURL' />";
      var resourcePath = "<xsl:value-of select='$resourcePath' />"
      var class = "<xsl:value-of select='$class' />";
      var categ = "<xsl:value-of select='$categ' />";
      var webPath = "<xsl:value-of select='$webPath' />";
      var jsPath = "<xsl:value-of select='$jsPath' />";
      var imagePath = "<xsl:value-of select='$imgPath' />";
      var returnUrl = "<xsl:value-of select='$returnUrl' />";
      var showId = "<xsl:value-of select='$showId' />" === "true";
      var currentLang = "<xsl:value-of select='$CurrentLang' />";

      djConfig = {
        isDebug: true,
        parseOnLoad: true,
        baseUrl: webPath + "/",
        modulePaths: {
          "dojoclasses": "js/dojoclasses"
        },
        xdWaitSeconds: 10
      };
    </script>

    <!-- <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/dojo/1.6/dojo/dojo.xd.js"></script>-->
    <script type="text/javascript" src="http://yandex.st/dojo/1.6.1/dojo/dojo.xd.js"></script>

    <script type="text/javascript" src="{$jsPath}/dojoInclude.js"></script>
    <script type="text/javascript" src="{$jsPath}/ClassificationUtils.js"></script>
    <script type="text/javascript" src="{$jsPath}/SimpleI18nManager.js"></script>
    <script type="text/javascript" src="{$jsPath}/EventHandler.js"></script>
    <script type="text/javascript" src="{$jsPath}/LazyLoadingTree.js"></script>
    <script type="text/javascript" src="{$jsPath}/LabelEditor.js"></script>
    <script type="text/javascript" src="{$jsPath}/CategoryEditorPane.js"></script>
    <script type="text/javascript" src="{$jsPath}/TreePane.js"></script>
    <script type="text/javascript" src="{$jsPath}/Editor.js"></script>

    <!-- <link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/dojo/1.6/dijit/themes/claro/claro.css"></link>-->
    <link rel="stylesheet" type="text/css" href="http://yandex.st/dojo/1.6.1/dijit/themes/claro/claro.css"></link>
    <link rel="stylesheet" type="text/css" href="{$webPath}/css/classificationEditor.css"></link>

    <script type="text/javascript">
      function setup() {
        // TODO use mycore api to set this
        var supportedLanguages = ["de", "en", "pl"];

        var classEditor = new classification.Editor();
        classEditor.create(resourcePath, supportedLanguages, currentLang, showId);
        dijit.byId("classMainContainer").set('content', classEditor.domNode);
        classEditor.loadClassification(class, categ);
      }

      dojo.ready(setup);
    </script>

    <div class="claro">
      <div dojoType="dijit.layout.ContentPane" id="classMainContainer" style="width: 900px; height: 600px;">
      </div>
    </div>
    
  </xsl:template>
</xsl:stylesheet>