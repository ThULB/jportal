<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation">

    <!-- ================================================================================= -->
    <xsl:template match="TOC | toc">
        <xsl:if test="$readAccess='true'">
            <xsl:for-each select="$loaded_navigation_xml//item[@href=$browserAddress]/item">
                <xsl:variable name="readAccess">
                    <xsl:call-template name="get.readAccess">
                        <xsl:with-param name="webpage" select="@href" />
                        <xsl:with-param name="blockerWebpage" select="$browserAddress" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:if test="$readAccess='true'">
                    <img src="{$WebApplicationBaseURL}images/naviMenu/greenArrow.gif" />
                    <xsl:call-template name="addLink" />
                    <br />
                </xsl:if>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    <!-- ================================================================================= -->
    <xsl:template name="navigation.history">
        <xsl:if test="$readAccess='true'">
            <xsl:for-each select="$loaded_navigation_xml//item[@href = $browserAddress]">
                <!-- start page -->
                <xsl:copy-of select="'Navigation: '" />
                <xsl:variable name="hrefStartingPage" select="$loaded_navigation_xml/@hrefStartingPage" />
                <a href="{$WebApplicationBaseURL}">
                    <xsl:copy-of select="$MainTitle" />
                </a>
                <!-- ancestors -->
                <xsl:for-each select="ancestor-or-self::item">
                    <xsl:if test="@href!=$hrefStartingPage">
                        <xsl:choose>
                            <xsl:when test="position()!=last()">
                                <xsl:value-of select="' > '" />
                                <xsl:call-template name="addLink" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="' > '" />
                                <xsl:choose>
                                    <xsl:when test="./label[lang($CurrentLang)] != ''">
                                        <xsl:value-of select="./label[lang($CurrentLang)]" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./label[lang($DefaultLang)]" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    <!-- ================================================================================= -->
    <xsl:template name="navigation.row">
        <xsl:param name="rootNode" />
        <xsl:param name="CSSLayoutClass" />
        <xsl:param name="menuPointHeigth" /><!-- use pixel values -->
        <xsl:param name="spaceBetweenLinks" /><!-- use pixel values -->
        <xsl:param name="seperatorChar" /><!-- use pixel values -->

        <xsl:variable name="readAccess">
            <xsl:call-template name="get.readAccess">
                <xsl:with-param name="webpage" select="$rootNode" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:if test="$readAccess='true'">
            <table class="{$CSSLayoutClass}" cellspacing="0" cellpadding="0">
                <tr>
                    <xsl:for-each select="$loaded_navigation_xml//*[@href=$rootNode]/item">
                        <xsl:variable name="access">
                            <xsl:call-template name="get.readAccess">
                                <xsl:with-param name="webpage" select="@href" />
                                <xsl:with-param name="blockerWebpage" select="$rootNode" />
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:if test="$access='true'">
                            <td>
                                <xsl:choose>
                                    <xsl:when test="@href = $browserAddress">
                                        <span class="marked">
                                            <xsl:call-template name="addLink" />
                                        </span>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:call-template name="addLink" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <xsl:call-template name="get.placeHolder">
                                <xsl:with-param name="spaceBetweenLinks" select="$spaceBetweenLinks" />
                                <xsl:with-param name="seperatorChar" select="$seperatorChar" />
                            </xsl:call-template>
                        </xsl:if>
                    </xsl:for-each>
                    <!-- login links -->
                    <xsl:call-template name="get.loginLinks">
                        <xsl:with-param name="spaceBetweenLinks" select="$spaceBetweenLinks" />
                        <xsl:with-param name="seperatorChar" select="$seperatorChar" />
                    </xsl:call-template>

                    <td>
                        <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:{number($spaceBetweenLinks) div 2}px; height:1px;" alt=""></img>
                    </td>
                    <td>|</td>
                    <td>
                        <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:{number($spaceBetweenLinks) div 2}px; height:1px;" alt=""></img>
                    </td>
                    <td>
                        <xsl:call-template name="navigation.flags" />
                    </td>
                    <td style="width:10px;"></td>
                </tr>
            </table>
        </xsl:if>
    </xsl:template>
    <!-- ================================================================================= -->
    <xsl:template name="get.placeHolder">
        <xsl:param name="spaceBetweenLinks" />
        <xsl:param name="seperatorChar" />
        <td style="width:{number($spaceBetweenLinks) div 2}px;">
            <img src="{$ImageBaseURL}emtyDot1Pix.gif" alt=""></img>
        </td>
        <xsl:if test="$seperatorChar != ''">
            <td>
                <xsl:value-of select="$seperatorChar" />
            </td>
        </xsl:if>
        <td style="width:{number($spaceBetweenLinks) div 2}px;">
            <img src="{$ImageBaseURL}emtyDot1Pix.gif" alt=""></img>
        </td>

    </xsl:template>
    <!-- ================================================================================= -->
    <xsl:template name="navigation.flags">
        <xsl:variable name="englishFlag">
            <img src="{$WebApplicationBaseURL}images/naviMenu/lang-en.gif" alt="new language: English" title="Union Jack"
                style="width:24px; height:12px; vertical-align:bottom; border-style:none;" />
        </xsl:variable>
        <xsl:variable name="germanFlag">
            <img src="{$WebApplicationBaseURL}images/naviMenu/lang-de.gif" alt="new language: German" title="Deutsch"
                style="width:24px; height:12px; vertical-align:bottom; border-style:none;" />
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$CurrentLang = 'en'">
                <xsl:call-template name="FlagPrinter">
                    <xsl:with-param name="flag" select="$germanFlag" />
                    <xsl:with-param name="lang" select="'de'" />
                    <xsl:with-param name="url" select="$RequestURL" />
                    <xsl:with-param name="alternative" select="concat($RequestURL, '?lang=de')" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="FlagPrinter">
                    <xsl:with-param name="flag" select="$englishFlag" />
                    <xsl:with-param name="lang" select="'en'" />
                    <xsl:with-param name="url" select="$RequestURL" />
                    <xsl:with-param name="alternative" select="concat($RequestURL, '?lang=en')" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- ================================================================================= -->
    <xsl:template name="forLoop.createColumns">
        <xsl:param name="i" />
        <xsl:param name="count" />
        <xsl:param name="columnWidthIcon" />
        <xsl:if test="$i &lt;= $count">
            <td style="width:{$columnWidthIcon}%;">
                <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:1px; height:1px" alt="" title=""></img>
            </td>
        </xsl:if>
        <xsl:if test="$i &lt;= $count">
            <xsl:call-template name="forLoop.createColumns">
                <xsl:with-param name="i">
                    <!-- Increment index-->
                    <xsl:value-of select="$i + 1" />
                </xsl:with-param>
                <xsl:with-param name="count">
                    <xsl:value-of select="$count" />
                </xsl:with-param>
                <xsl:with-param name="columnWidthIcon" select="$columnWidthIcon" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <!-- ================================================================================= -->
    <xsl:template name="navigation.tree">
        <xsl:param name="rootNode" />
        <xsl:param name="CSSLayoutClass" />
        <xsl:param name="menuPointHeigth" /><!-- use pixel values -->
        <xsl:param name="columnWidthIcon" /><!-- use percent values -->
        <xsl:param name="spaceBetweenMainLinks" /><!-- use pixel values -->
        <xsl:param name="borderWidthTopDown" /><!-- use pixel values -->
        <xsl:param name="borderWidthSides" /><!-- use percent values -->

        <xsl:variable name="subRootNode">
            <xsl:call-template name="get.subRootNode">
                <xsl:with-param name="rootNode" select="$rootNode" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="access">
            <xsl:call-template name="get.readAccess">
                <xsl:with-param name="webpage" select="$subRootNode" />
            </xsl:call-template>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$access='true'">
                <!-- get maximal depth -> $depth -->
                <xsl:variable name="depth">
                    <xsl:for-each select="$loaded_navigation_xml//item">
                        <xsl:sort select="count(ancestor-or-self::item)" data-type="number" />
                        <xsl:if test="position()=last()">
                            <xsl:value-of select="count(ancestor-or-self::item)" />
                        </xsl:if>
                    </xsl:for-each>
                </xsl:variable>
                <!-- general table -->
                <table cellspacing="0" cellpadding="0" class="{$CSSLayoutClass}" border="0">
                    <!-- head -->
                    <xsl:call-template name="navigation.tree.head">
                        <xsl:with-param name="depth" select="$depth" />
                        <xsl:with-param name="CSSLayoutClass" select="$CSSLayoutClass" />
                        <xsl:with-param name="menuPointHeigth" select="$menuPointHeigth" />
                        <xsl:with-param name="columnWidthIcon" select="$columnWidthIcon" />
                        <xsl:with-param name="spaceBetweenMainLinks" select="$spaceBetweenMainLinks" />
                        <xsl:with-param name="borderWidthTopDown" select="$borderWidthTopDown" />
                        <xsl:with-param name="borderWidthSides" select="$borderWidthSides" />
                    </xsl:call-template>
                    <!-- navigation tree -->
                    <!-- point to subRootNode -->
                    <xsl:for-each select="$loaded_navigation_xml//*[@href=$subRootNode]">
                        <xsl:for-each select="item">
                            <xsl:variable name="access">
                                <xsl:call-template name="get.readAccess">
                                    <xsl:with-param name="webpage" select="@href" />
                                    <xsl:with-param name="blockerWebpage" select="$subRootNode" />
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:if test="$access='true'">
                                <tr>
                                    <td style="height:{$menuPointHeigth}px;">
                                        <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:1px; height:1px" alt="" title=""></img>
                                    </td>
                                    <th colspan="{$depth + 1}" style="text-align:left">
                                        <xsl:call-template name="addLink" />
                                    </th>
                                </tr>
                                <xsl:variable name="constrainPopUp">
                                    <xsl:if test="ancestor-or-self::*[@constrainPopUp='true']">
                                        <xsl:value-of select="'true'" />
                                    </xsl:if>
                                </xsl:variable>
                                <!-- sub links, if below this MAIN menu point the searched link is located -->
                                <xsl:if test="current()[@href = $browserAddress ] or descendant::item[@href = $browserAddress ] or $constrainPopUp='true'  ">
                                    <xsl:call-template name="createTree">
                                        <xsl:with-param name="depth" select="$depth" />
                                        <xsl:with-param name="menuPointHeigth" select="$menuPointHeigth" />
                                        <xsl:with-param name="columnWidthIcon" select="$columnWidthIcon" />
                                        <xsl:with-param name="rootNode" select="$rootNode" />
                                        <xsl:with-param name="subRootNode" select="$subRootNode" />
                                        <xsl:with-param name="blockerWebpage" select="@href" />
                                    </xsl:call-template>
                                </xsl:if>
                                <!-- place holder between main links -->
                                <xsl:if test="count(following-sibling::item) &gt; 0">
                                    <tr>
                                        <td style="height:{$spaceBetweenMainLinks}px;" colspan="{$depth + 2}">
                                            <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:1px; height:1px" alt="" title=""></img>
                                        </td>
                                    </tr>
                                </xsl:if>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:for-each>
                    <!-- borderWidthDown -->
                    <tr>
                        <td style="height:{$borderWidthTopDown}px;" colspan="{$depth + 2}">
                            <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:1px; height:1px" alt="" title=""></img>
                        </td>
                    </tr>
                </table>
            </xsl:when>
        </xsl:choose>
        <div id="poweredByMycore" xmlns:mcrver="xalan://org.mycore.common.MCRCoreVersion">
            <a href="http://www.mycore.de">
                <img src="{$WebApplicationBaseURL}images/poweredby.gif" alt="powered by &lt;MyCoRe&gt;" title="{i18n:translate('mycore.version', mcrver:getCompleteVersion())}" />
            </a>
        </div>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template name="get.subRootNode">
        <xsl:param name="rootNode" />
        <!-- look for appropriate replaceMenu entry and assign to subRootNode if found one-->
        <xsl:for-each select="$loaded_navigation_xml//item[@href = $browserAddress]">
            <xsl:choose>
                <xsl:when test="ancestor-or-self::item[  @replaceMenu = 'true' ]">
                    <!-- collect @href's with replaceMenu="true" entries along an axis -->
                    <xsl:for-each select="ancestor-or-self::item[  @replaceMenu = 'true' ]">
                        <xsl:if test="position()=last()">
                            <xsl:value-of select="@href" />
                        </xsl:if>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$rootNode" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template name="navigation.tree.head">
        <xsl:param name="depth" />
        <xsl:param name="CSSLayoutClass" />
        <xsl:param name="menuPointHeigth" /><!-- use pixel values -->
        <xsl:param name="columnWidthIcon" /><!-- use percent values -->
        <xsl:param name="spaceBetweenMainLinks" /><!-- use pixel values -->
        <xsl:param name="borderWidthTopDown" /><!-- use pixel values -->
        <xsl:param name="borderWidthSides" /><!-- use percent values -->

        <!-- initialise columns -->
        <tr>
            <!-- border left -->
            <td style="height:1px; width:{$borderWidthSides}%;">
                <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:1px; height:1px" alt="" title=""></img>
            </td>
            <!-- END OF: border left -->
            <!-- create columns to give space for the icons -->
            <xsl:call-template name="forLoop.createColumns">
                <xsl:with-param name="i" select="1" />
                <xsl:with-param name="count" select="$depth - 1" />
                <xsl:with-param name="columnWidthIcon" select="$columnWidthIcon" />
            </xsl:call-template>
            <!-- END OF: create columns to give space for the icons -->
            <!-- fill rest -->
            <td style="width:{ 100 - (2*$borderWidthSides) - ($columnWidthIcon*($depth - 1)) }%px;">
                <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:1px; height:1px" alt="" title=""></img>
            </td>
            <!-- END OF: fill rest -->
            <!-- border right -->
            <td style="width:{$borderWidthSides}%;">
                <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:1px; height:1px" alt="" title=""></img>
            </td>
            <!-- END OF: border right -->
        </tr>
        <!-- END OF: initialise columns -->
        <!-- borderWidthTop -->
        <tr>
            <td style="height:{$borderWidthTopDown}px;" colspan="{$depth + 2}">
                <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:1px; height:1px" alt="" title=""></img>
            </td>
        </tr>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template name="createTree">
        <xsl:param name="depth" />
        <xsl:param name="menuPointHeigth" />
        <xsl:param name="columnWidthIcon" />
        <xsl:param name="rootNode" />
        <xsl:param name="subRootNode" />
        <xsl:param name="blockerWebpage" />

        <!-- all items -->
        <!-- TODO: REALLY should replace this for-each call by a recursive call, to avoid multiple access control verification and make it simpler -->
        <xsl:variable name="oneLevelDeepContrainPopup" select="descendant::item[ancestor-or-self::item[@constrainPopUp='true' and parent::item[@href = $browserAddress]] or parent::item[@href = $browserAddress] or @href = $browserAddress]"/>
        <xsl:variable name="oneLevelDeepContrainPopupSiblings" select="$oneLevelDeepContrainPopup/../item"/>
        <xsl:variable name="ancestorOfBrowserAddressComplete" select="descendant::item[@href = $browserAddress]/ancestor-or-self::item"/>
        <xsl:variable name="stopPosition">
          <xsl:choose>
            <xsl:when test="$ancestorOfBrowserAddressComplete[@href= $subRootNode]">
              <xsl:for-each select="$ancestorOfBrowserAddressComplete">
                <xsl:if test="@href= $subRootNode">
                  <xsl:value-of select="position()+1" />
                </xsl:if>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="1"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="ancestorOfBrowserAddress" select="$ancestorOfBrowserAddressComplete[position() &gt; $stopPosition]"/>
        <xsl:variable name="ancestorOfBrowserAddressSiblings" select="$ancestorOfBrowserAddress/../item"/>
        <xsl:variable name="siblingConstrainPopup" select="$ancestorOfBrowserAddressSiblings/../descendant-or-self::item[@constrainPopUp='true']/descendant::item" />
        <xsl:variable name="constrainPopup" select="self::node()[@constrainPopUp='true']/item/descendant::item | //item[@constrainPopUp='true']//item[@href=current()/@href]/descendant::item" />
        <xsl:for-each select="item | $oneLevelDeepContrainPopup | $oneLevelDeepContrainPopupSiblings | $ancestorOfBrowserAddress | $ancestorOfBrowserAddressSiblings | $siblingConstrainPopup | $constrainPopup">

            <xsl:variable name="access">
                <xsl:call-template name="get.readAccess">
                    <xsl:with-param name="webpage" select="@href" />
                    <xsl:with-param name="blockerWebpage" select="$blockerWebpage" />
                </xsl:call-template>
            </xsl:variable>

            <xsl:if test="$access='true'">
                <!--'true', if an ancestor node is @constrainPopUp='true'-->
                <xsl:variable name="constrainPopUp">
                    <!--has an ascentant @constrainPopUp='true' ?-->
                    <xsl:for-each select="ancestor-or-self::node()">
                        <xsl:if test="current()[@constrainPopUp='true']">
                            <xsl:value-of select="'true'" />
                        </xsl:if>
                    </xsl:for-each>
                    <!--has an ascentant @constrainPopUp='true' ?-->
                </xsl:variable>

                <!-- calculate kind of link to display the right icon -> $linkKind -->
                <xsl:variable name="linkKind">
                    <xsl:choose>
                        <!-- if this item is the browser address -->
                        <xsl:when test="current()[@href = $browserAddress ]">
                            <xsl:choose>
                                <!-- children -->
                                <xsl:when test="descendant::item[@href and not(ancestor-or-self::item[@constrainPopUp='true'])]">
                                    <xsl:value-of select="'current_popedUp'" />
                                </xsl:when>
                                <!-- no children -->
                                <xsl:otherwise>
                                    <xsl:value-of select="'current'" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <!-- if searched link is a descentant of the current one -->
                        <xsl:when test="descendant::item[@href = $browserAddress and not(ancestor-or-self::item[@constrainPopUp='true'])] ">
                            <xsl:value-of select="'popedUp'" />
                        </xsl:when>
                        <!-- END OF: if searched link is a descentant of the current one -->
                        <!--    parent::item[@href = $browserAddress ] -> if the searched link is the parent  
                            or   preceding-sibling::item[@href = $browserAddress ] -> if the searched link is a sibling 
                            or following-sibling::item[@href = $browserAddress ]           
                            or   preceding-sibling::item/descendant::item[@href = $browserAddress ] -> if the searched link is a decentant of a sibling  
                            or following-sibling::item/descendant::item[@href = $browserAddress ]"> -->
                        <xsl:when
                            test="(     parent::item[@href = $browserAddress ] 
                                or  preceding-sibling::item[@href = $browserAddress ] 
                                    or  following-sibling::item[@href = $browserAddress ]          
                                or  preceding-sibling::item/descendant::item[@href = $browserAddress ] 
                        or  following-sibling::item/descendant::item[@href = $browserAddress ])">
                            <xsl:choose>
                                <!-- children -->
                                <xsl:when test="descendant::item[@href and not(ancestor-or-self::item[@constrainPopUp='true'])]">
                                    <xsl:value-of select="'notPopedUp'" />
                                </xsl:when>
                                <!-- no children -->
                                <xsl:otherwise>
                                    <xsl:value-of select="'normal'" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <!--menu must be poped up ========================= -->
                        <xsl:when test="($constrainPopUp = 'true') ">
                            <xsl:value-of select="'normal'" />
                        </xsl:when>
                        <!--end: menu must be poped up ========================= -->
                        <xsl:otherwise>
                            <xsl:value-of select="'normal'" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <!-- END OF: calculate kind of link to display the right icon -> $linkKind -->
                <xsl:call-template name="addMenuRow">
                    <xsl:with-param name="linkKind" select="$linkKind" />
                    <xsl:with-param name="depth" select="$depth" />
                    <xsl:with-param name="menuPointHeigth" select="$menuPointHeigth" />
                    <xsl:with-param name="columnWidthIcon" select="$columnWidthIcon" />
                    <xsl:with-param name="rootNode" select="$rootNode" />
                    <xsl:with-param name="subRootNode" select="$subRootNode" />
                </xsl:call-template>
            </xsl:if>

        </xsl:for-each>
    </xsl:template>
    <!-- ================================================================================= -->
    <!-- ================================================================================= -->
    <xsl:template name="addMenuRow">
        <xsl:param name="linkKind" />
        <xsl:param name="depth" />
        <xsl:param name="menuPointHeigth" />
        <xsl:param name="columnWidthIcon" />
        <xsl:param name="rootNode" />
        <xsl:param name="subRootNode" />
        <xsl:param name="createSiteMap" />

        <xsl:variable name="depthSubRootNode">
            <xsl:variable name="subRootNodeDepthTemp">
                <xsl:for-each select="//item[@href = $subRootNode]">
                    <xsl:value-of select="count(ancestor-or-self::item)" />
                </xsl:for-each>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$subRootNodeDepthTemp != ''">
                    <xsl:value-of select="$subRootNodeDepthTemp"></xsl:value-of>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="0" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:if test="$linkKind != 'hide'">
            <!-- display complete link row when $linkKind != 'hide' -->
            <tr>
                <td style="height:{$menuPointHeigth}px;">
                    <img src="{$ImageBaseURL}emtyDot1Pix.gif" style="width:1px; height:{$menuPointHeigth}px;" alt="" title=""></img>
                </td>
                <!-- draw lines before icon and link -->
                <xsl:for-each select="ancestor::item">
                    <xsl:choose>
                        <xsl:when test="($depthSubRootNode = 0) and (position() &gt; 1)">
                            <td align="center">
                                <xsl:call-template name="addIcon">
                                    <xsl:with-param name="linkKind" select="'line'" />
                                    <xsl:with-param name="menuPointHeigth" select="$menuPointHeigth" />
                                    <xsl:with-param name="columnWidthIcon" select="$columnWidthIcon" />
                                </xsl:call-template>
                            </td>
                        </xsl:when>
                        <xsl:when test="($depthSubRootNode &gt; 0 and position() &gt; $depthSubRootNode+1)">
                            <td align="center">
                                <xsl:call-template name="addIcon">
                                    <xsl:with-param name="linkKind" select="'line'" />
                                    <xsl:with-param name="menuPointHeigth" select="$menuPointHeigth" />
                                    <xsl:with-param name="columnWidthIcon" select="$columnWidthIcon" />
                                </xsl:call-template>
                            </td>
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each>
                <!-- draw icon before the link -->
                <td align="center">
                    <xsl:call-template name="addIcon">
                        <xsl:with-param name="linkKind" select="$linkKind" />
                        <xsl:with-param name="menuPointHeigth" select="$menuPointHeigth" />
                        <xsl:with-param name="columnWidthIcon" select="$columnWidthIcon" />
                    </xsl:call-template>
                </td>
                <!-- display link -->
                <xsl:variable name="colSpanValue">
                    <xsl:choose>
                        <xsl:when test="$depthSubRootNode = 0">
                            <xsl:value-of select="$depth - count(ancestor::item) + 1" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$depth - count(ancestor::item) + $depthSubRootNode + 1 " />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <td colspan="{$colSpanValue}">
                    <xsl:choose>
                        <xsl:when test="$linkKind = 'current' or $linkKind = 'current_popedUp'">
                            <span class="marked">
                                <xsl:call-template name="addLink">
                                    <xsl:with-param name="linkKind" select="$linkKind" />
                                </xsl:call-template>
                            </span>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="addLink"></xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <!-- ================================================================================= -->
    <!-- ================================================================================= -->
    <xsl:template name="addIcon">
        <xsl:param name="linkKind" />
        <xsl:param name="menuPointHeigth" />
        <xsl:param name="columnWidthIcon" />
        <xsl:choose>
            <!-- list end -->
            <xsl:when test="count(following-sibling::item) &lt; 1">
                <xsl:choose>
                    <xsl:when test="$linkKind = 'line'">
                        <img src="{$WebApplicationBaseURL}images/naviMenu/empty-ri.gif" style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;" alt=""
                            title="">
                        </img>
                    </xsl:when>
                    <xsl:when test="$linkKind = 'normal'">
                        <img src="{$WebApplicationBaseURL}images/naviMenu/line-with-element_end.gif"
                            style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;" alt="" title="">
                        </img>
                    </xsl:when>
                    <xsl:when test="$linkKind = 'current'">
                        <img src="{$WebApplicationBaseURL}images/naviMenu/line-with-element-selected_end.gif"
                            style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;" alt="" title="">
                        </img>
                    </xsl:when>
                    <xsl:when test="$linkKind = 'current_popedUp'">
                        <a href="{concat($WebApplicationBaseURL,substring-after((parent::node()/@href),'/'))}">
                            <xsl:if test="$wcms.useTargets = 'yes'">
                                <xsl:attribute name="target">
                                    <xsl:value-of select="@target" />
                                </xsl:attribute>
                            </xsl:if>
                            <img src="{$WebApplicationBaseURL}images/naviMenu/minus-selected_end.gif"
                                style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;" alt="" title="">
                            </img>
                        </a>
                    </xsl:when>
                    <xsl:when test="$linkKind = 'popedUp'">
                        <a href="{concat($WebApplicationBaseURL,substring-after((parent::node()/@href),'/'))}">
                            <xsl:if test="$wcms.useTargets = 'yes'">
                                <xsl:attribute name="target">
                                    <xsl:value-of select="@target" />
                                </xsl:attribute>
                            </xsl:if>
                            <img src="{$WebApplicationBaseURL}images/naviMenu/minus_end.gif" style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;"
                                alt="" title="">
                            </img>
                        </a>
                    </xsl:when>
                    <xsl:when test="$linkKind = 'notPopedUp'">
                        <a href="{concat($WebApplicationBaseURL,substring-after(@href,'/'))}">
                            <xsl:if test="$wcms.useTargets = 'yes'">
                                <xsl:attribute name="target">
                                    <xsl:value-of select="@target" />
                                </xsl:attribute>
                            </xsl:if>
                            <img src="{$WebApplicationBaseURL}images/naviMenu/plus_end.gif" style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;"
                                alt="" title="" />
                        </a>
                    </xsl:when>
                </xsl:choose>
            </xsl:when>
            <!-- END OF: list end -->
            <!-- not list end -->
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$linkKind = 'line'">
                        <img src="{$WebApplicationBaseURL}images/naviMenu/line.gif" style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;" alt=""
                            title="">
                        </img>
                    </xsl:when>
                    <xsl:when test="$linkKind = 'normal'">
                        <img src="{$WebApplicationBaseURL}images/naviMenu/line-with-element.gif"
                            style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;" alt="" title="">
                        </img>
                    </xsl:when>
                    <xsl:when test="$linkKind = 'current'">
                        <img src="{$WebApplicationBaseURL}images/naviMenu/line-with-element-selected.gif"
                            style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;" alt="" title="">
                        </img>
                    </xsl:when>
                    <xsl:when test="$linkKind = 'current_popedUp'">
                        <a href="{concat($WebApplicationBaseURL,substring-after((parent::node()/@href),'/'))}">
                            <xsl:if test="$wcms.useTargets = 'yes'">
                                <xsl:attribute name="target">
                                    <xsl:value-of select="@target" />
                                </xsl:attribute>
                            </xsl:if>
                            <img src="{$WebApplicationBaseURL}images/naviMenu/minus-selected.gif"
                                style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;" alt="" title="">
                            </img>
                        </a>
                    </xsl:when>
                    <xsl:when test="$linkKind = 'popedUp'">
                        <a href="{concat($WebApplicationBaseURL,substring-after((parent::node()/@href),'/'))}">
                            <xsl:if test="$wcms.useTargets = 'yes'">
                                <xsl:attribute name="target">
                                    <xsl:value-of select="@target" />
                                </xsl:attribute>
                            </xsl:if>
                            <img src="{$WebApplicationBaseURL}images/naviMenu/minus.gif" style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;" alt=""
                                title="">
                            </img>
                        </a>
                    </xsl:when>
                    <xsl:when test="$linkKind = 'notPopedUp'">
                        <a href="{concat($WebApplicationBaseURL,substring-after(@href,'/'))}">
                            <xsl:if test="$wcms.useTargets = 'yes'">
                                <xsl:attribute name="target">
                                    <xsl:value-of select="@target" />
                                </xsl:attribute>
                            </xsl:if>
                            <img src="{$WebApplicationBaseURL}images/naviMenu/plus.gif" style="width:{$columnWidthIcon}px; height:{$menuPointHeigth}px;" alt=""
                                title="" />
                        </a>
                    </xsl:when>
                </xsl:choose>
            </xsl:otherwise>
            <!-- END OF: not list end -->
        </xsl:choose>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template name="addLink">
        <xsl:param name="createSiteMap" />

        <xsl:choose>
            <!-- item @type is "intern" -> add the web application path before the link -->
            <xsl:when test="@type = 'intern'">
                <a>
                    <!--target-->
                    <xsl:if test="$wcms.useTargets = 'yes'">
                        <xsl:attribute name="target">
                            <xsl:value-of select="@target" />
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:attribute name="href">
                         <xsl:call-template name="UrlAddSession">
                            <xsl:with-param name="url" select="concat($WebApplicationBaseURL,substring-after(@href,'/'))" />
                            </xsl:call-template>
                    </xsl:attribute>
                    <!-- label -->
                    <xsl:choose>
                        <xsl:when test="@style = 'bold'">
                            <span style="font-weight:bold;">
                                <xsl:choose>
                                    <xsl:when test="./label[lang($CurrentLang)] != ''">
                                        <xsl:value-of select="./label[lang($CurrentLang)]" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./label[lang($DefaultLang)]" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </span>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="./label[lang($CurrentLang)] != ''">
                                    <xsl:value-of select="./label[lang($CurrentLang)]" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="./label[lang($DefaultLang)]" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </xsl:when>
            <!-- item @type is extern-->
            <xsl:otherwise>
                <a>
                    <xsl:if test="$wcms.useTargets = 'yes'">
                        <xsl:attribute name="target">
                            <xsl:value-of select="@target" />
                        </xsl:attribute>
                    </xsl:if>
                    <!-- set attribute @href -->
                    <xsl:variable name="href_temp">
                        <xsl:choose>
                            <!-- build $webapplicationbaseurl before link in case @href doesn't start with 'http' & co. -->
                            <xsl:when test=" starts-with(@href,'http:') or starts-with(@href,'mailto:') or starts-with(@href,'ftp:')">
                                <xsl:value-of select="@href" />
                            </xsl:when>
                            <!-- link is relative and not starting with http ... -->
                            <xsl:otherwise>
                                <xsl:value-of select="concat($WebApplicationBaseURL,substring-after(@href,'/'))" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:attribute name="href">
                        <!--add session-->
                        <xsl:call-template name="UrlAddSession">
                            <xsl:with-param name="url" select="$href_temp" />
                        </xsl:call-template>
                    </xsl:attribute>
                    <!-- label -->
                    <xsl:choose>
                        <xsl:when test="@style = 'bold'">
                            <span style="font-weight:bold;">
                                <xsl:choose>
                                    <xsl:when test="./label[lang($CurrentLang)] != ''">
                                        <xsl:value-of select="./label[lang($CurrentLang)]" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./label[lang($DefaultLang)]" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </span>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="./label[lang($CurrentLang)] != ''">
                                    <xsl:value-of select="./label[lang($CurrentLang)]" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="./label[lang($DefaultLang)]" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>
    <!-- ================================================================================= -->
    <xsl:template name="FlagPrinter">
        <xsl:param name="flag" />
        <xsl:param name="lang" />
        <xsl:param name="url" />
        <xsl:param name="alternative" />
        <a>
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

    <!-- ================================================================================= -->

    <xsl:template name="get.loginLinks">
        <xsl:param name="spaceBetweenLinks" />
        <xsl:param name="seperatorChar" />
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="loginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?dummy=login&amp;lang=',$CurrentLang,'&amp;url=', encoder:encode( string( $RequestURL ) ) )" />
        <xsl:choose>
            <xsl:when test="$CurrentUser='gast'">
                <td>
                    <strong> 
                    <a href="{$loginURL}">                       
                            <xsl:value-of select="i18n:translate('component.userlogin.button.login')" />                        
                    </a>
                    </strong>
                </td>
            </xsl:when>
            <xsl:otherwise>
                <td>
                    <strong>
                    <a href="{$loginURL}&amp;uid=gast&amp;pwd=gast">                        
                            <xsl:value-of select="i18n:translate('component.userlogin.button.logout')" />                        
                    </a>
                    </strong>
                </td>
                <xsl:call-template name="get.placeHolder">
                    <xsl:with-param name="spaceBetweenLinks" select="$spaceBetweenLinks" />
                    <xsl:with-param name="seperatorChar" select="$seperatorChar" />
                </xsl:call-template>
                <td>
                    <strong>
                    <a href="{$loginURL}">                        
                            <xsl:value-of select="i18n:translate('component.userlogin.titles.pageTitle.login')" />                        
                    </a>
                    </strong>
                </td>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ================================================================================= -->

</xsl:stylesheet>
