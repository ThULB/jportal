<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"  xmlns:mcrver="xalan://org.mycore.common.MCRCoreVersion" exclude-result-prefixes="xalan i18n mcrver">
  <xsl:param name="MCR.Users.Guestuser.UserName" />

  <!--
    =================================================================================
    navigation.xsl
    ======================================================================================

    templates Table of Content - TOC - erzeugt auch im Inhaltsbereich
    disubRootNoe Verweise des Menues HistoryNavigationRow - erzeugt eine
    Navigationsleiste - zeigt die aktuelle Position in der Hirachie -
    Verweist auf Navigationsknoten NavigationRow NavigationTree -
    erzeugt das Hauptmenu - baumartige Struktur - Hauptpunkte beinhalten
    Unterpunkte - Unterpunkte sind versteckt bis Hauptpunkt aktiviert

    createTree - Funktion von NavigationTree - erzeugt die Baumstruktur
    addMenuRow - Funktion von createTree - erzeugt die Unterpunkte
    addlink - Funktion - erzeugt einen Verweis generateFlagButton
    flagPrinter

    ==================================================================================
  -->

  <!--
    =================================================================================
    Table of Contents
    ==================================================================================
  -->

  <xsl:template match="TOC | toc">
    <xsl:variable name="textSource">
    <xsl:choose>
        <xsl:when test="@mode='descriptive'">description</xsl:when>
    <xsl:otherwise>label</xsl:otherwise>
      </xsl:choose>
  </xsl:variable>

    <xsl:for-each select="$loaded_navigation_xml//item[@href=$browserAddress]">
      <xsl:variable name="subRootNode">
        <xsl:for-each select="ancestor-or-self::item[  @replaceMenu = 'true' ]">
          <xsl:if test="position()=last()">
            <xsl:value-of select="@href" />
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
      <xsl:if test="child::item">
        <ul class="toc">
          <xsl:for-each select="child::item">
            <xsl:variable name="access">
              <xsl:call-template name="get.readAccess">
                <xsl:with-param name="webpage" select="@href" />
                <xsl:with-param name="blockerWebpage"
                  select="$subRootNode" />
              </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$access='true'">
              <xsl:element name="li">
                <xsl:call-template name="addLink">
                  <xsl:with-param name="textSource" select="$textSource" />
                </xsl:call-template>
              </xsl:element>
            </xsl:if>
          </xsl:for-each>
        </ul>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!--
    =================================================================================
    HistoryNavigationRow
    ==================================================================================
  -->

  <xsl:template name="HistoryNavigationRow">

    <!-- get href of starting page -->
    <!--
      Variable beinhaltet die url der in navigation.xml angegebenen
      Startseite
    -->
    <xsl:variable name="hrefStartingPage"
      select="$loaded_navigation_xml/@hrefStartingPage" />
    <!-- END OF: get href of starting page -->

    <!--
      fuer jedes Element des Elternknotens <navigation> in
      navigation.xml
    -->
    <xsl:for-each select="$loaded_navigation_xml//item[@href]">
      <!-- pruefe ob ein Element gerade angezeigt wird -->
      <xsl:if test="@href = $browserAddress ">
        <!-- dann eroeffne einen Verweis -->
        <a>
          <!-- auf die Startseite der Webanwendung -->
          <xsl:attribute name="href">
            <!-- fuege der Adresse die session ID hinzu -->
            <xsl:call-template name="UrlAddSession">
              <xsl:with-param name="url"
            select="concat($WebApplicationBaseURL,substring-after($hrefStartingPage,'/'))" />
            </xsl:call-template>
          </xsl:attribute>

          <!-- Linktext ist der Haupttitel aus mycore.properties.wcms-->
          <xsl:value-of select="$MainTitle" />
        </a>
        <!-- END OF: Verweis -->

        <!-- fuer sich selbst und jedes seiner Elternelemente -->
        <xsl:for-each select="ancestor-or-self::item">
          <!--
            und fuer alle Seiten ausser der Startseite zeige den
            Seitentitel in der Navigationsleiste Verweis auf die
            Startseite existiert bereits s.o.
          -->
          <xsl:if test="$browserAddress != $hrefStartingPage ">
            <xsl:value-of select="' &gt; '" />
            <a>
              <xsl:attribute name="href">
                <xsl:call-template name="UrlAddSession">
                  <xsl:with-param name="url"
                select="concat($WebApplicationBaseURL,substring-after(@href,'/'))" />
                </xsl:call-template>
              </xsl:attribute>
              <xsl:choose>
                <xsl:when test="./label[lang($CurrentLang)] != ''">
                  <xsl:value-of select="./label[lang($CurrentLang)]" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="./label[lang($DefaultLang)]" />
                </xsl:otherwise>
              </xsl:choose>
            </a>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!--
    =================================================================================
    NavigationRow
    ==================================================================================
  -->

  <xsl:template name="NavigationRow">
    <xsl:param name="rootNode" />
    <xsl:param name="CSSLayoutClass" />
    <xsl:param name="includeLoginLinks" select="true()" />

      <!-- read xml item entries and create the link bar -->
      <ul>
        <xsl:for-each select="$rootNode/item">
          <xsl:variable name="access">
            <xsl:call-template name="get.readAccess">
              <xsl:with-param name="webpage" select="@href" />
              <xsl:with-param name="blockerWebpage"
                select="$rootNode" />
            </xsl:call-template>
          </xsl:variable>
          <xsl:if test="$access='true'">

            <xsl:variable name="linkCSSLayoutClass">
              <xsl:choose>
                <xsl:when test="current()[@href = $browserAddress ]">
                  <xsl:value-of select="'current '" />
                  <xsl:value-of select="$CSSLayoutClass" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$CSSLayoutClass" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <li>
              <xsl:call-template name="addLink">
                <xsl:with-param name="CSSLayoutClass"
                  select="$linkCSSLayoutClass" />
              </xsl:call-template>
              <xsl:if test="position() != last()"></xsl:if>
            </li>

          </xsl:if>
        </xsl:for-each>
        <xsl:if test="$includeLoginLinks">
          <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder"
            name="loginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?url=', encoder:encode( string( $RequestURL ) ) )" />
          <xsl:choose>
            <xsl:when test="$CurrentUser=$MCR.Users.Guestuser.UserName">
              <li class="{$CSSLayoutClass}">
                <a href="{$loginURL}" class="{$CSSLayoutClass}">
                  <xsl:value-of select="i18n:translate('component.userlogin.button.login')" />
                </a>
              </li>
            </xsl:when>
            <xsl:otherwise>
              <li class="{$CSSLayoutClass}">
                <a
                  href="{$ServletsBaseURL}logout{$HttpSession}"
                  class="{$CSSLayoutClass}">
                  <xsl:value-of select="i18n:translate('component.userlogin.button.logout')" />
                </a>
              </li>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </ul>
      <!-- END OF: read xml item entries and create the link bar -->
  </xsl:template>


  <!--
    =================================================================================
    PrintUserInfo
    ==================================================================================
  -->

  <xsl:template name="PrintUserInfo">
    <div id="userInfo">
        <xsl:call-template name="userInfo" />
    </div>
  </xsl:template>

  <!--
    =================================================================================
    NavigationTree
    ==================================================================================
  -->

  <xsl:template name="NavigationTree">

    <xsl:param name="rootNode" />
    <xsl:param name="CSSLayoutClass" />

    <!-- look for appropriate replaceMenu entry and assign-->
    <xsl:variable name="subRootNode">
      <xsl:for-each select="$rootNode//item[@href = $browserAddress]">
        <!-- collect @href's with replaceMenu="true" entries along an axis -->
        <xsl:for-each select="ancestor-or-self::item[  @replaceMenu = 'true' ]">
          <xsl:if test="position()=last()">
            <xsl:value-of select="@href" />
          </xsl:if>
        </xsl:for-each>
        <!--
          END OF: collect @href's with replaceMenu="true" entries along
          an axis
        -->
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="baseNodes">
      <xsl:choose>
        <xsl:when test=" $subRootNode != '' ">
          <xsl:copy-of select="$rootNode//item[@href = $subRootNode]/*" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$rootNode/item[@href]" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- END OF: look for appropriate replaceMenu entry and assign -->

    <!-- navigation tree -->
    <ul class="topList">
      <xsl:for-each select="xalan:nodeset($baseNodes)/item[@href]">
        <!-- main link -->
        <xsl:variable name="access">
          <xsl:call-template name="get.readAccess">
            <xsl:with-param name="webpage" select="@href" />
            <xsl:with-param name="blockerWebpage" select="$subRootNode" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="$access='true'">
          <xsl:variable name="linkKind">
            <xsl:apply-templates select="." mode="linkKind" />
          </xsl:variable>
          <xsl:variable name="linkCSSLayoutClass">
            <xsl:choose>
              <xsl:when test="current()[@href = $browserAddress ]">
                <xsl:value-of select="'current '" />
                <xsl:value-of select="$CSSLayoutClass" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$CSSLayoutClass" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <xsl:element name="li">
            <xsl:attribute name="class">
              <xsl:value-of select="$linkKind" />
            </xsl:attribute>
            <xsl:call-template name="addLink">
              <xsl:with-param name="CSSLayoutClass"
                select="$linkCSSLayoutClass" />
            </xsl:call-template>
            <!-- sub links -->
            <!-- test if below this MAIN menu point the searched link is located -->
            <xsl:if
              test="current()[@href = $browserAddress or @constrainPopUp='true' ] or descendant::item[@href = $browserAddress ] ">
              <xsl:call-template name="createTree">
                <xsl:with-param name="CSSLayoutClass"
                  select="$CSSLayoutClass" />
                <xsl:with-param name="subRootNode" select="$subRootNode" />
                <xsl:with-param name="rootNode" select="$rootNode" />
              </xsl:call-template>
            </xsl:if>
            <!--
              END OF: test if below this main menu point the searched link
              is located
            -->
            <!-- END OF: sub links -->
          </xsl:element>
        </xsl:if>
        <!-- END OF: main link -->
      </xsl:for-each>
      <!-- END OF: point to subRootNode -->
    </ul>
    <!-- END OF: navigation tree -->
  </xsl:template>


  <!--
    =================================================================================
    createTree
    ==================================================================================
  -->

  <xsl:template name="createTree">
    <xsl:param name="CSSLayoutClass" />
    <xsl:param name="subRootNode" />
    <xsl:param name="rootNode" />
    <!-- read all items within this name space -->
    <xsl:if test="child::item">
      <ul>
        <xsl:for-each select="child::item">
          <xsl:variable name="access">
            <xsl:call-template name="get.readAccess">
              <xsl:with-param name="webpage" select="@href" />
              <xsl:with-param name="blockerWebpage" select="$subRootNode" />
            </xsl:call-template>
          </xsl:variable>

          <xsl:if test="$access='true'">
            <!-- calculate kind of link to display the right icon -> $linkKind -->
            <xsl:variable name="linkKind">
              <xsl:apply-templates select="." mode="linkKind" />
            </xsl:variable>
            <xsl:variable name="linkCSSLayoutClass">
              <xsl:choose>
                <xsl:when test="current()[@href = $browserAddress ]">
                  <xsl:value-of select="'current '" />
                  <xsl:value-of select="$CSSLayoutClass" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$CSSLayoutClass" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <!--
              END OF: calculate kind of link to display the right icon ->
              $linkKind
            -->
            <li class="{$linkKind}">
              <xsl:call-template name="addMenuRow">
                <xsl:with-param name="CSSLayoutClass"
                  select="$linkCSSLayoutClass" />
                <xsl:with-param name="linkKind" select="$linkKind" />
                <xsl:with-param name="subRootNode" select="$subRootNode" />
                <xsl:with-param name="rootNode" select="$rootNode" />
              </xsl:call-template>
              <xsl:if test="contains($linkKind, 'poppedUp')">
                <xsl:call-template name="createTree">
                  <xsl:with-param name="CSSLayoutClass"
                    select="$CSSLayoutClass" />
                  <xsl:with-param name="subRootNode"
                    select="$subRootNode" />
                  <xsl:with-param name="rootNode" select="$rootNode" />
                </xsl:call-template>
              </xsl:if>
            </li>
          </xsl:if>
        </xsl:for-each>
      </ul>
    </xsl:if>
    <!-- END OF: read all items within this name space -->
  </xsl:template>

  <xsl:template match="item" mode="linkKind">
    <xsl:choose>
      <!-- if this item is the browser address -->
      <xsl:when test="current()[@href = $browserAddress ]">
        <xsl:choose>
          <!-- children -->
          <xsl:when test="descendant::item[@href]">
            <xsl:value-of select="'current poppedUp'" />
          </xsl:when>
          <!-- no children -->
          <xsl:otherwise>
            <xsl:value-of select="'current'" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <!-- END OF: if this item is the browser address -->
      <!-- if searched link is a descentant of the current one -->
      <xsl:when
        test="descendant::item[@href = $browserAddress ] or current()[@constrainPopUp='true']">
        <xsl:value-of select="'poppedUp'" />
      </xsl:when>
      <!-- END OF: if searched link is a descentant of the current one -->
      <!-- children -->
      <xsl:when test="descendant::item[@href]">
        <xsl:value-of select="'notPoppedUp'" />
      </xsl:when>
      <!-- no children -->
      <xsl:otherwise>
        <xsl:value-of select="'normal'" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--
    =================================================================================
    addMenuRow
    ==================================================================================
  -->

  <xsl:template name="addMenuRow">
    <xsl:param name="CSSLayoutClass" />
    <xsl:param name="linkKind" />
    <xsl:param name="subRootNode" />
    <xsl:param name="rootNode" />
    <!-- display link -->
    <xsl:call-template name="addLink">
      <xsl:with-param name="CSSLayoutClass" select="$CSSLayoutClass" />
    </xsl:call-template>
    <!-- END OF: display link -->
  </xsl:template>


  <!--
    =================================================================================
    addLink
    ==================================================================================
  -->

  <xsl:template name="addLink">
    <xsl:param name="CSSLayoutClass" />
    <xsl:param name="textSource" select="'label'" /> <!-- or 'description' -->

    <a>
      <xsl:if test="@style != 'normal' or string-length($CSSLayoutClass)>0">
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="@style != 'normal'">
              <xsl:value-of select="concat(@style, ' ',$CSSLayoutClass)" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$CSSLayoutClass" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="$wcms.useTargets = 'yes'">
        <xsl:attribute name="target">
          <xsl:value-of select="@target" />
        </xsl:attribute>
      </xsl:if>
      <xsl:attribute name="href">
        <xsl:choose>
          <!-- item @type is "intern" -> add the web application path before the link -->
          <xsl:when
        test=" starts-with(@href,'http:') or starts-with(@href,'https:') or starts-with(@href,'mailto:') or starts-with(@href,'ftp:')">
            <xsl:value-of select="@href" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="UrlAddSession">
              <xsl:with-param name="url" select="concat($WebApplicationBaseURL,substring-after(@href,'/'))" />
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="*[name()=$textSource][lang($CurrentLang)] != ''">
          <xsl:value-of select="*[name()=$textSource][lang($CurrentLang)]" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="*[name()=$textSource][lang($DefaultLang)]" />
        </xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>


  <!--
    =================================================================================
    generateFlagButton
    ==================================================================================
  -->

  <xsl:template name="generateFlagButton">
    <xsl:variable name="englishFlag">
      <img
        src="{$WebApplicationBaseURL}images/naviMenu/lang-en.gif"
        alt="new language: English"
        style="border:0px; height:13px; margin-left:10px; margin-right:10px; width:21px;" />
    </xsl:variable>
    <xsl:variable name="germanFlag">
      <img
        src="{$WebApplicationBaseURL}images/naviMenu/lang-de.gif"
        alt="neue Sprache: Deutsch"
        style="border:0px; height:13px; margin-left:10px; margin-right:10px; width:21px;" />
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$CurrentLang = 'en'">
        <xsl:call-template name="FlagPrinter">
          <xsl:with-param name="flag" select="$germanFlag" />
          <xsl:with-param name="lang" select="'de'" />
          <xsl:with-param name="url" select="$RequestURL" />
          <xsl:with-param name="alternative"
            select="concat($RequestURL, '?lang=de')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="FlagPrinter">
          <xsl:with-param name="flag" select="$englishFlag" />
          <xsl:with-param name="lang" select="'en'" />
          <xsl:with-param name="url" select="$RequestURL" />
          <xsl:with-param name="alternative"
            select="concat($RequestURL, '?lang=en')" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--
    =================================================================================
    FlagPrinter
    ==================================================================================
  -->

  <xsl:template name="FlagPrinter">
    <xsl:param name="flag" />
    <xsl:param name="lang" />
    <xsl:param name="url" />
    <xsl:param name="alternative" />
    <a class="changeLang">
      <xsl:attribute name="href">
        <xsl:variable name="newurl">
          <xsl:call-template name="UrlSetParam">
            <xsl:with-param name="url" select="$url" />
            <xsl:with-param name="par" select="'lang'" />
            <xsl:with-param name="value" select="$lang" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="contains($newurl,'MCR:ERROR')">
            <xsl:call-template name="UrlAddSession">
              <xsl:with-param name="url" select="$alternative" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="UrlAddSession">
              <xsl:with-param name="url" select="$newurl" />
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:copy-of select="$flag" />
    </a>
  </xsl:template>

  <!--
    =================================================================================
    Powered by Logo
    ==================================================================================
  -->

  <xsl:template name="PoweredByMycore">
    <div id="poweredByMycore">
      <img src="{$WebApplicationBaseURL}images/poweredby.gif" alt="{i18n:translate('mycore.version' , mcrver:getCompleteVersion())}" title="{i18n:translate('mycore.version' , mcrver:getCompleteVersion())}" />
    </div>
  </xsl:template>

</xsl:stylesheet>