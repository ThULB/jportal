<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xlink xalan">

    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:param name="PageTitle" select="'Zeitschriften-Kontext anlegen'" />
    <xsl:param name="MCR.users_superuser_username" />
    <xsl:param name="MCR.JPortal.Create-JournalContext.ID" select="'null'" />

    <!-- =================================================================================================== -->

    <xsl:template match="/create-journalContext">
        <xsl:choose>
            <xsl:when test="not(acl:checkPermission($MCR.JPortal.Create-JournalContext.ID, 'create_jpjournal'))">Zugriff untersagt. Bitte melden Sie sich an!</xsl:when>
            <xsl:otherwise>
                <form action="{$ServletsBaseURL}MCRJPortalCreateJournalContextServlet" method="post">
                    <input type="hidden" name="mode" value="createContext" />
                    <input type="hidden" name="jp.cjc.shortCut" value="{$MCR.JPortal.Create-JournalContext.ID}" />
                    <table style="border:solid 1px">
                        <tr>
                            <td style="padding:4px;font-weight:bold;">
                                Kategorie:
                            </td>
                            <td style="padding:4px;">
                                <xsl:call-template name="jp.cjc.getWebpages" />
                            </td>
                        </tr>
                        <xsl:call-template name="jp.cjc.emptyRow" />
                        <tr>
                            <td style="padding:4px;font-weight:bold;">Layout:</td>
                            <td style="padding:4px;">
                                <xsl:call-template name="jp.cjc.getTemplates" />
                            </td>
                        </tr>
                        <xsl:call-template name="jp.cjc.emptyRow" />
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

    <xsl:template name="jp.cjc.getWebpages">
        <select name="jp.cjc.preceedingItemHref" size="1">
                <option value="/content/main/journalList/dummy.xml">
                    Zeitschrift
                </option>
                <option value="/content/main/calendarList/dummy.xml">
                    Kalender
                </option>
        </select>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="jp.cjc.getTemplates">
        <xsl:variable name="templateList">
            <xsl:call-template name="get.templates" />
        </xsl:variable>
        <select name="jp.cjc.layoutTemplate" size="1">
            <option value="template_default" selected="selected">
                Standard-Template
            </option>
            <xsl:for-each select="xalan:nodeset($templateList)/templates/template[@category='master']">
                <option value="{text()}">
                    <xsl:copy-of select="text()" />
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