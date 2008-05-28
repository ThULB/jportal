<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xlink xalan">

    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:param name="PageTitle" select="'Zeitschriften-Kontext anlegen'" />
    <xsl:param name="MCR.users_superuser_username" />
    <xsl:param name="MCR.JPortal.Create-JournalContext.ID" select="'null'" />

    <xsl:variable name="userList">
        <xsl:copy-of select="document('request:servlets/MCRJPortalCreateJournalContextServlet?mode=getUsers')" />
    </xsl:variable>

    <!-- =================================================================================================== -->

    <xsl:template match="/create-journalContext">
        <xsl:choose>
            <xsl:when test="not(acl:checkPermission($MCR.JPortal.Create-JournalContext.ID, 'writedb'))">Zugriff untersagt. Bitte melden sie sich an!</xsl:when>
            <xsl:otherwise>
                <form action="{$ServletsBaseURL}MCRJPortalCreateJournalContextServlet" method="post">
                    <input type="hidden" name="mode" value="createContext" />
                    <table style="border:solid 1px">
                        <tr>
                            <td style="padding:4px;font-weight:bold;">Kürzel für die Zeitschrift:</td>
                            <td style="padding:4px;">
                                <input name="jp.cjc.shortCut" type="text" size="14" maxlength="14" />
                            </td>
                        </tr>
                        <xsl:call-template name="jp.cjc.emptyRow" />
                        <tr>
                            <td colspan="2" style="padding:4px;font-weight:bold;">
                                Vorgängerseite (Seiten nach der die Journal-Webseiten eingepflegt werden):
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" style="padding:4px;">
                                <xsl:call-template name="jp.cjc.getWebpages" />
                            </td>
                        </tr>
                        <xsl:call-template name="jp.cjc.emptyRow" />
                        <tr>
                            <td style="padding:4px;font-weight:bold;">Template, falls nicht das Standard-Template ('template_kürzel')</td>
                            <td style="padding:4px;">
                                <xsl:call-template name="jp.cjc.getTemplates" />
                            </td>
                        </tr>
                        <xsl:call-template name="jp.cjc.emptyRow" />
                        <tr>
                            <td colspan="2" style="padding:4px;font-weight:bold;">Rechtezuweisung:</td>
                        </tr>
                        <tr>
                            <td colspan="2" style="padding:4px;">
                                <xsl:call-template name="jp.cjc.getUserLists" />
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" style="padding:4px;">
                                <input type="submit" value="Zeitschriftenkontext anlegen" />
                            </td>
                        </tr>
                    </table>
                </form>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="jp.cjc.getUserLists">
        <table>
            <tr>
                <td>TOC:</td>
                <td>Artikel:</td>
                <td>TOC + Artikel:</td>
            </tr>
            <tr>
                <td>
                    <xsl:call-template name="jp.cjc.getUsers">
                        <xsl:with-param name="idOfSelectBox" select="'usersTOC'" />
                    </xsl:call-template>
                </td>
                <td>
                    <xsl:call-template name="jp.cjc.getUsers">
                        <xsl:with-param name="idOfSelectBox" select="'usersART'" />
                    </xsl:call-template>
                </td>
                <td>
                    <xsl:call-template name="jp.cjc.getUsers">
                        <xsl:with-param name="idOfSelectBox" select="'usersALL'" />
                    </xsl:call-template>
                </td>
            </tr>
        </table>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="jp.cjc.getWebpages">
        <select name="jp.cjc.preceedingItemHref" size="1">
            <xsl:for-each select="xalan:nodeset($loaded_navigation_xml)//item">
                <xsl:variable name="preLabel">
                    <xsl:for-each select="ancestor-or-self::node()">
                        <xsl:copy-of select="'---'" />
                    </xsl:for-each>
                </xsl:variable>
                <xsl:variable name="label">
                    <xsl:value-of select="concat($preLabel,./label[lang($CurrentLang)]/text())" />
                </xsl:variable>
                <option value="{@href}">
                    <xsl:copy-of select="$label" />
                </option>
            </xsl:for-each>
        </select>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="jp.cjc.getTemplates">
        <xsl:variable name="templateList">
            <xsl:call-template name="get.templates" />
        </xsl:variable>
        <select name="jp.cjc.layoutTemplate" size="1">
            <option value="default" selected="selected">
                <xsl:copy-of select="'Standard-Template'" />
            </option>
            <xsl:for-each select="xalan:nodeset($templateList)/templates/template[@category='master']">
                <option value="{text()}">
                    <xsl:copy-of select="text()" />
                </option>
            </xsl:for-each>
        </select>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="jp.cjc.getUsers">
        <xsl:param name="idOfSelectBox" />
        <select name="jp.cjc.{$idOfSelectBox}" size="17" multiple="multiple">
            <option value="" selected="selected">
                <xsl:copy-of select="'bitte wählen'" />
            </option>
            <xsl:for-each select="xalan:nodeset($userList)/users/user">
                <xsl:sort select="text()" />
                <option value="{@id}">
                    <xsl:copy-of select="concat(text(),' (',@id,')')" />
                </option>
            </xsl:for-each>
        </select>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="jp.cjc.emptyRow">
        <tr>
            <td colspan="2">
                <br />
            </td>
        </tr>
    </xsl:template>

    <!-- =================================================================================================== -->

</xsl:stylesheet>