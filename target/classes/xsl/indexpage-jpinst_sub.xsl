<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1066 $ $Date: 2010-01-15 09:42:41 +0100 (Fr, 15 Jan 2010) $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder">

    <xsl:variable name="MainTitle" select="i18n:translate('indexpage.sub.maintitle')" />
    <xsl:variable name="PageTitle" select="i18n:translate('indexpage.sub.pagetitle.institution')" />
    <xsl:variable name="Servlet" select="'MCRIndexBrowserServlet'" />
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:include href="indexpage-common.xsl" />
    <xsl:param name="WebApplicationBaseURL" />

    <!-- ========== Navigation ========== -->
    <xsl:variable name="page.title" select="$PageTitle" />
    <xsl:variable name="PageID" select="concat('index.', /indexpage/index/@id)" />

    <!-- ========== Variablen ========== -->
    <xsl:variable name="search" select="/indexpage/results/@search" />
    <xsl:variable name="mode" select="/indexpage/results/@mode" />
    <xsl:variable name="IndexID" select="/indexpage/index/@id" />

    <!-- ========== Subselect Parameter ========== -->
    <xsl:param name="subselect.session" />
    <xsl:param name="subselect.varpath" />
    <xsl:param name="subselect.webpage" />

    <!-- ======== headline ======== -->
    <xsl:template name="index.headline">
        <!-- 
            <tr valign="top">
            <td class="metaname">
            <xsl:value-of select="i18n:translate('indexpage.sub.headline.select')" />
            <xsl:value-of select="$IndexTitle" />
            </td>
            </tr> -->
    </xsl:template>

    <!-- ======== intro text ======== -->
    <xsl:template name="index.intro">
        <tr>
            <td class="metavalue">
                <xsl:call-template name="IntroText" />
            </td>
        </tr>
    </xsl:template>

    <!-- ======== index search ======== -->
    <xsl:template name="index.search">
        <tr>
            <td>
                <table border="0" cellpadding="0" cellspacing="4">
                    <tr>
                        <form action="{$WebApplicationBaseURL}indexpage{$HttpSession}?searchclass={$IndexID}" method="post">
                            <td class="metavalue">
                                <b>
                                    <xsl:value-of select="i18n:translate('indexpage.sub.index')" />
                                    <xsl:text> </xsl:text>
                                    <xsl:call-template name="getSelectBox" />
                                </b>
                            </td>
                            <td>
                                <!-- 
                                    <input type="text" class="button" size="30" name="search" value="{$search}" />
                                -->
                                <xsl:choose>
                                    <xsl:when test="$search='xxxxxxxxxxxxxxxxxxxxx'">
                                        <input type="text" class="button" size="30" name="search" value="" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <input type="text" class="button" size="30" name="search" value="{$search}" />
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:text> </xsl:text>
                                <input type="submit" class="button" value="{i18n:translate('indexpage.sub.buttons.search')}" />
                            </td>
                        </form>
                        <!-- 
                            <xsl:if test="string-length($search) &gt; 0 ">
                            <td>
                            <form action="{$WebApplicationBaseURL}indexpage{$HttpSession}?searchclass={$IndexID}&amp;search={@prefix}" method="post">
                            <input type="submit" class="button" value="{i18n:translate('indexpage.sub.buttons.filter.disable')}" />
                            </form>
                            </td>
                            </xsl:if> -->
                    </tr>

                    <xsl:if test="string-length($search) &gt; 0 ">
                        <tr>
                            <td class="metavalue" colspan="3">
                                <br />
                                <b>
                                    <xsl:text></xsl:text>
                                    <xsl:value-of select="results/@numHits" />
                                    <xsl:value-of select="i18n:translate('indexpage.sub.hits')" />
                                </b>
                            </td>
                        </tr>
                    </xsl:if>

                </table>
            </td>
        </tr>
    </xsl:template>

    <!-- ======== indexpage ======== -->
    <xsl:template match="indexpage">
        <table>
            <xsl:call-template name="index.headline" />
            <xsl:call-template name="index.intro" />
            <xsl:call-template name="index.search" />
            <xsl:apply-templates select="results" />
        </table>
    </xsl:template>

    <xsl:variable name="up.url">
        <xsl:text>indexpage?searchclass=</xsl:text>
        <xsl:value-of select="$IndexID" />
        <xsl:if test="string-length($search) &gt; 0">
            <xsl:text>&amp;search=</xsl:text>
            <xsl:value-of select="$search" />
            <xsl:text>&amp;mode=</xsl:text>
            <xsl:value-of select="$mode" />
        </xsl:if>
    </xsl:variable>

    <!-- ========== results ========== -->
    <xsl:template match="results">
        <tr>
            <td class="metavalue">
                <xsl:if test="range">
                    <dl>
                        <dt>
                            <img border="0" src="{$WebApplicationBaseURL}images/folder_open_in_use.gif" align="middle" />

                            <xsl:choose>
                                <xsl:when test="contains(/indexpage/@path,'-')">
                                    <b>
                                        <a class="nav" href="{$up.url}">
                                            <xsl:value-of select="i18n:translate('indexpage.sub.link.back')" />
                                        </a>
                                    </b>
                                </xsl:when>
                                <xsl:when test="string-length($search) &gt; 0">
                                    <b>
                                        <xsl:value-of select="i18n:translate('indexpage.sub.results.overallindex.filtered')" />
                                    </b>
                                </xsl:when>
                                <xsl:otherwise>
                                    <b>
                                        <xsl:value-of select="i18n:translate('indexpage.sub.results.overallindex')" />
                                    </b>
                                </xsl:otherwise>
                            </xsl:choose>

                        </dt>
                        <xsl:apply-templates select="range" />
                    </dl>
                </xsl:if>
                <xsl:if test="value">
                    <dl>
                        <dt>
                            <img border="0" src="{$WebApplicationBaseURL}images/folder_open_in_use.gif" align="middle" />

                            <xsl:choose>
                                <xsl:when test="contains(/indexpage/@path,'-')">
                                    <b>
                                        <a class="nav" href="{$up.url}">
                                            <xsl:value-of select="i18n:translate('indexpage.link.back')" />
                                        </a>
                                    </b>
                                </xsl:when>
                                <xsl:when test="string-length($search) &gt; 0">
                                    <b>
                                        <xsl:value-of select="i18n:translate('indexpage.results.entries.filtered')" />
                                    </b>
                                </xsl:when>
                                <xsl:otherwise>
                                    <b>
                                        <xsl:value-of select="i18n:translate('indexpage.sub.results.overallindex')" />
                                    </b>
                                </xsl:otherwise>
                            </xsl:choose>

                        </dt>
                        <dd>
                            <table border="0" cellpadding="0" cellspacing="0" style="padding-bottom:5px">
                                <xsl:apply-templates select="value" />
                            </table>
                        </dd>
                    </dl>
                </xsl:if>
            </td>
        </tr>
    </xsl:template>

    <!-- ========== value ========== -->
    <xsl:template match="value">
        <xsl:variable name="url"
            select="concat($ServletsBaseURL,'XMLEditor',$HttpSession,
      '?_action=end.subselect&amp;subselect.session=',$subselect.session,
      '&amp;subselect.varpath=', $subselect.varpath,
      '&amp;subselect.webpage=', encoder:encode($subselect.webpage),'&amp;mode=',$mode)" />
        <xsl:variable name="label">
            <xsl:value-of select="sort" />
        </xsl:variable>
        <xsl:variable name="nameXML">
            <xsl:copy-of select="document(concat('mcrobject:',col[@name='id']))/mycoreobject/metadata" />
        </xsl:variable>
        <xsl:variable name="name">
            <xsl:value-of select="xalan:nodeset($nameXML)/metadata/names/name/fullname/text()" />
        </xsl:variable>
        <xsl:variable name="city">
            <xsl:value-of select="xalan:nodeset($nameXML)/metadata/addresses/address/city/text()" />
        </xsl:variable>
        <xsl:variable name="beautiLabel">
            <xsl:choose>
                <xsl:when test="$city!=''">
                    <xsl:value-of select="concat($name,' (',$city,')')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$name" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <tr>
            <td class="td1" valign="top">
                <img border="0" src="{$WebApplicationBaseURL}images/folder_plain.gif" />
            </td>
            <td class="td1" valign="top" style="padding-right:5px;">
                <a
                    href="{$url}&amp;_var_@xlink:href={col[@name='id']}&amp;_var_@xlink:label={$beautiLabel}
            &amp;_var_@field=participants_art&amp;_var_@operator==&amp;_var_@value={col[@name='id']}">
                    <xsl:copy-of select="concat($beautiLabel,' (',col[@name='id'],')')" />
                    <!--                     <xsl:value-of
                        select="concat(col[@name='surname'],', ',col[@name='academic'],' ',col[@name='peerage'],' ',col[@name='firstname'],' ',col[@name='prefix'])" /> -->
                </a>
            </td>
        </tr>
    </xsl:template>

    <!-- ========== range ========== -->
    <xsl:template match="range">
        <xsl:variable name="url">
            <xsl:value-of select="concat($WebApplicationBaseURL,'indexpage',$HttpSession,'?searchclass=',$IndexID,'&amp;fromTo=', from/@pos,'-', to/@pos )" />
            <xsl:value-of select="concat('&amp;mode=',$mode)" />
            <xsl:if test="string-length($search) &gt; 0">
                <xsl:value-of select="concat('&amp;search=',$search)" />
            </xsl:if>
        </xsl:variable>

        <dd>
            <img border="0" src="{$WebApplicationBaseURL}images/folder_closed_in_use.gif" align="middle" />
            <a href="{$url}" class="nav">
                <xsl:value-of select="concat(from/@short,' - ',to/@short)" />
            </a>
        </dd>
    </xsl:template>

    <!-- ========== Titel ========== -->
    <xsl:variable name="IndexTitle" select="i18n:translate('indexpage.sub.indextitle')" />

    <!-- ========== Einleitender Text ========== -->
    <xsl:template name="IntroText">
        <xsl:value-of select="i18n:translate('indexpage.sub.introtext.institution')" />
        <p>
            <form method="post">
                <xsl:attribute name="action">
                    <xsl:value-of select="concat($WebApplicationBaseURL,$subselect.webpage)" />
                    <xsl:if test="not(contains($subselect.webpage,$subselect.session))">
                      <xsl:value-of select="concat('XSL.editor.session.id=',$subselect.session)" />
                    </xsl:if>
                </xsl:attribute>
                <input type="submit" class="submit" value="{i18n:translate('indexpage.sub.select.cancel')}" />
            </form>
        </p>
        <!-- 
            <p>
            <xsl:value-of select="' | '" />
            <xsl:for-each select="xalan:nodeset($AtoZ)/search">
            <a href="{$WebApplicationBaseURL}indexpage{$HttpSession}?searchclass={$IndexID}&amp;mode=prefix&amp;search={@prefix}">
            <xsl:value-of select="@prefix" />
            </a>
            <xsl:value-of select="' | '" />
            </xsl:for-each>
            </p> -->
    </xsl:template>

    <xsl:variable name="AtoZ">
        <search prefix="A" />
        <search prefix="B" />
        <search prefix="C" />
        <search prefix="D" />
        <search prefix="E" />
        <search prefix="F" />
        <search prefix="G" />
        <search prefix="H" />
        <search prefix="I" />
        <search prefix="J" />
        <search prefix="K" />
        <search prefix="L" />
        <search prefix="M" />
        <search prefix="N" />
        <search prefix="O" />
        <search prefix="P" />
        <search prefix="Q" />
        <search prefix="R" />
        <search prefix="S" />
        <search prefix="T" />
        <search prefix="U" />
        <search prefix="V" />
        <search prefix="W" />
        <search prefix="X" />
        <search prefix="Y" />
        <search prefix="Z" />
    </xsl:variable>

</xsl:stylesheet>
