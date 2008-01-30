<?xml version="1.0" encoding="ISO-8859-1" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:include href="objecttypes.xsl" />
    <xsl:param name="toc.pageSize" select="10" />
    <xsl:param name="toc.pos" select="1" />
    <xsl:param name="template" select="'template_DublicateFinder'" />

    <xsl:variable name="PageTitle" select="'Dublettenfinder'" />
    <xsl:variable name="ServletName" select="'MCRDublicateFinderServlet'" />
    <!-- ===================================================================================== -->

    <xsl:template match="redundancyMap">
        <xsl:choose>
            <xsl:when test="$CurrentUser='gast'">Zugriff verweigert! Bitte melden sie sich an.</xsl:when>
            <xsl:otherwise>
                <table>
                    <xsl:call-template name="redundancy.filter" />
                    <xsl:call-template name="redundancy.head" />
                    <xsl:call-template name="redundancy.progressStatus" />
                    <xsl:call-template name="lineBreak" />
                    <xsl:call-template name="printDublicates" />
                    <xsl:call-template name="redundancy.head" />
                </table>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="redundancy.head">
        <tr>
            <td colspan="3">
                <b>
                    <xsl:call-template name="redundancy.printTOCNavi">
                        <xsl:with-param name="location" select="'navi'" />
                        <xsl:with-param name="childrenXML" select="." />
                    </xsl:call-template>
                </b>
            </td>
        </tr>
    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="redundancy.filter">
        <tr>
            <td colspan="3">
                <b>
                    <xsl:copy-of select="' Filter: '" />
                    <xsl:choose>
                        <xsl:when test="$redunMode='closed'">
                            <xsl:copy-of select="' Bereits bearbeitete Dubletten '" />
                            <a href="{$WebApplicationBaseURL}{$RedunMap}?XSL.redunMode.SESSION=open&amp;XSL.toc.pos.SESSION=1">
                                <xsl:copy-of select="' (Wechsel zur Ansicht -Offene Dubletten-)'" />
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of select="' Offene Dubletten '" />
                            <a href="{$WebApplicationBaseURL}{$RedunMap}?XSL.redunMode.SESSION=closed&amp;XSL.toc.pos.SESSION=1">
                                <xsl:copy-of select="' (Wechsel zur Ansicht -Bereits bearbeitete Dubletten-) '" />
                            </a>
                        </xsl:otherwise>
                    </xsl:choose>
                </b>
            </td>
        </tr>
    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="redundancy.progressStatus">
        <xsl:variable name="numTotal">
            <xsl:value-of select="count(redundancyID)" />
        </xsl:variable>
        <xsl:variable name="numDenied">
            <xsl:value-of select="count(redundancyID[@status='denied'])" />
        </xsl:variable>
        <xsl:variable name="numAccepted">
            <xsl:value-of select="count(redundancyID[@status='accepted'])" />
        </xsl:variable>
        <xsl:variable name="progressTotal">
            <xsl:value-of select="format-number(((($numAccepted+$numDenied) div $numTotal) * 100),'#.##' )" />
        </xsl:variable>
        <xsl:variable name="progressAccepted">
            <xsl:value-of select="format-number(($numAccepted div ($numAccepted+$numDenied)) * 100,'#.##')" />
        </xsl:variable>
        <xsl:variable name="progressDenied">
            <xsl:value-of select="format-number(($numDenied div ($numAccepted+$numDenied)) * 100,'#.##')" />
        </xsl:variable>
        <tr>
            <td colspan="3">
                <b>
                    <xsl:copy-of select="concat(' Bearbeitungsstatus: ',$progressTotal,'% (',$numAccepted+$numDenied,'/',$numTotal,'), davon ',$progressAccepted,'% reelle Dubletten und ',$progressDenied,'% falsch erkannte Dubletten')" />
                </b>
            </td>
        </tr>
    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="redundancy.printTOCNavi">
        <xsl:param name="location" />
        <xsl:param name="childrenXML" />

        <xsl:variable name="pred">
            <xsl:value-of select="number($toc.pos)-(number($toc.pageSize)+1)" />
        </xsl:variable>
        <xsl:variable name="succ">
            <xsl:value-of select="number($toc.pos)+number($toc.pageSize)+1" />
        </xsl:variable>
        <xsl:variable name="numChildren">
            <xsl:value-of select="count(xalan:nodeset($filteredRedunMap)/redundancyID)" />
        </xsl:variable>

        <table>
            <tr>
                <td colspan="2">
                    <xsl:value-of select="concat(i18n:translate('metaData.sortbuttons.numberofres'),': ')" />
                    <b>
                        <xsl:value-of select="$numChildren" />
                    </b>
                    <xsl:call-template name="redundancy.printTOCNavi.chooseHitPage">
                        <xsl:with-param name="children" select="xalan:nodeset($filteredRedunMap)" />
                    </xsl:call-template>
                </td>
            </tr>
        </table>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="redundancy.printTOCNavi.chooseHitPage">
        <xsl:param name="children" />

        <xsl:variable name="numberOfChildren">
            <xsl:value-of select="count(xalan:nodeset($filteredRedunMap)/redundancyID)" />
        </xsl:variable>
        <xsl:variable name="numberOfHitPages">
            <xsl:value-of select="ceiling(number($numberOfChildren) div number($toc.pageSize))" />
        </xsl:variable>
        <xsl:if test="number($numberOfChildren)>number($toc.pageSize)">
            <xsl:value-of select="concat(', ',i18n:translate('metaData.resultpage'))" />
            <xsl:for-each select="xalan:nodeset($filteredRedunMap)/redundancyID[number($numberOfHitPages)>=position()]">
                <xsl:variable name="jumpToPos">
                    <xsl:value-of select="(position()*number($toc.pageSize))-number($toc.pageSize)" />
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="number($jumpToPos)+1=number($toc.pos)">
                        <xsl:value-of select="concat(' [',position(),'] ')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <a href="{$WebApplicationBaseURL}{$HttpSession}redundancy.xml?XSL.toc.pos.SESSION={$jumpToPos+1}">
                            <xsl:value-of select="concat(' ',position(),' ')" />
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:if>

    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="printDublicates">

        <xsl:variable name="toc.pos.verif">
            <xsl:choose>
                <xsl:when test="$toc.pageSize>count(./redundancyID)">
                    <xsl:value-of select="1" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$toc.pos" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$redunMode='open'">
                <xsl:for-each
                    select="xalan:nodeset($filteredRedunMap)/redundancyID[(position()>=$toc.pos.verif) and ($toc.pos.verif+$toc.pageSize>position())]">
                    <xsl:call-template name="printDublicates.entry">
                        <xsl:with-param name="toc.Pos.verif" select="$toc.pos.verif" />
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each
                    select="xalan:nodeset($filteredRedunMap)/redundancyID[(position()>=$toc.pos.verif) and ($toc.pos.verif+$toc.pageSize>position())]">
                    <xsl:sort select="@time" order="ascending"/>
                    <xsl:call-template name="printDublicates.entry">
                        <xsl:with-param name="toc.Pos.verif" select="$toc.pos.verif" />
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="printDublicates.entry">
        <xsl:param name="toc.Pos.verif" />
        
        <tr valign="top">
            <td width="49%" valign="top">
                <xsl:variable name="posOfDoublet">
                    <xsl:value-of select="$toc.Pos.verif + position() - 1" />
                </xsl:variable>
                <b>
                    <xsl:choose>
                        <xsl:when test="$redunMode='open'">
                            <form id="{$ServletName}.{position()}" action="{$ServletsBaseURL}{$ServletName}" method="get">
                                <input name="dublicate-id" type="hidden" value="{./text()}" />
                                <input name="redunMap" type="hidden" value="{$RedunMap}" />
                                <input name="ankerPosition" type="hidden" value="{$posOfDoublet}" />
                                <a name="{$posOfDoublet}" />
                                <xsl:copy-of select="concat('Dublette ',$posOfDoublet)" />
                                <br />
                                <xsl:copy-of select="'Bewertung:  '" />
                                <input name="status" type="radio" value="accepted" onclick="document.getElementById('{$ServletName}.{position()}').submit()" />
                                <xsl:call-template name="redun.getStatusText">
                                    <xsl:with-param name="statusID" select="'accepted'" />
                                </xsl:call-template>
                                <input name="status" type="radio" value="denied" onclick="document.getElementById('{$ServletName}.{position()}').submit()" />
                                <xsl:call-template name="redun.getStatusText">
                                    <xsl:with-param name="statusID" select="'denied'" />
                                </xsl:call-template>
                            </form>
                        </xsl:when>
                        <xsl:otherwise>
                            <b>
                                <a name="{$posOfDoublet}" />
                                <xsl:copy-of select="concat('Dublette ',$posOfDoublet)" />
                                <br />
                                <xsl:variable name="statusText">
                                    <xsl:call-template name="redun.getStatusText">
                                        <xsl:with-param name="statusID" select="@status" />
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:copy-of select="concat('Bewertung: ',$statusText)" />
                                <br />
                                <xsl:choose>
                                    <xsl:when test="$CurrentUser='root'">
                                        <a href="#" title="{@user}" alt="{@user}">
                                            <xsl:copy-of select="concat('Nutzer: ',@userRealName)" />
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:copy-of select="concat('Nutzer: ',@userRealName)" />
                                    </xsl:otherwise>
                                </xsl:choose>
                                <br />
                                <xsl:copy-of select="concat('Zeit: ',@timePretty)" />
                                <xsl:if test="$CurrentUser='root'">
                                    <br />
                                    <form id="{$ServletName}.reopen.{position()}" action="{$ServletsBaseURL}{$ServletName}" method="get">
                                        <input name="dublicate-id" type="hidden" value="{./text()}" />
                                        <input name="redunMap" type="hidden" value="{$RedunMap}" />
                                        <input name="ankerPosition" type="hidden" value="{$posOfDoublet}" />
                                        <input name="status" type="radio" value="open"
                                            onclick="document.getElementById('{$ServletName}.reopen.{position()}').submit()" />
                                        Dublette wieder zur Bearbeitung freigeben!
                                    </form>
                                </xsl:if>
                            </b>
                        </xsl:otherwise>
                    </xsl:choose>
                </b>
                <br />
                <xsl:variable name="mcrobj" select="document(concat('mcrobject:',./text()))" />
                <i>
                    <xsl:apply-templates select="$mcrobj" mode="present" />
                </i>
            </td>
            <td width="2%"></td>
            <td width="49%" valign="top">
                <br />
                <b>
                    <form>Davon ist die Dublette:</form>
                </b>
                <br />
                <br />
                <xsl:variable name="mcrobj" select="document(concat('mcrobject:',@notRedundancyID))" />
                <xsl:apply-templates select="$mcrobj" mode="present" />
            </td>
            <xsl:call-template name="lineBreak" />
        </tr>
    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="lineBreak">
        <tr>
            <td colspan="3">
                <br />
                __________________________________________________________________________________________________
                <br />
                <br />
            </td>
        </tr>
    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="redun.getStatusText">
        <xsl:param name="statusID" />
        <xsl:choose>
            <xsl:when test="$statusID='accepted'">
                <xsl:copy-of select="'JA, ist eine Dublette'" />
            </xsl:when>
            <xsl:when test="$statusID='denied'">
                <xsl:copy-of select="'NEIN, ist keine Dublette'" />
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="editobject_with_der"></xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="editobject">
        <xsl:param name="accessedit" />
        <xsl:param name="accessdelete" />
        <xsl:param name="id" />
        <xsl:param name="layout" select="'$'" />
        <xsl:variable name="layoutparam">
            <xsl:if test="$layout != '$'">
                <xsl:value-of select="concat('&amp;layout=',$layout)" />
            </xsl:if>
        </xsl:variable>
        <xsl:if test="$objectHost = 'local' and not(xalan:nodeset($filteredRedunMap)//redundancyID[text()=$id])">
            <xsl:choose>
                <xsl:when test="acl:checkPermission($id,'writedb') or acl:checkPermission($id,'deletedb')">
                    <xsl:variable name="type" select="substring-before(substring-after($id,'_'),'_')" />
                    <tr>
                        <td class="metaname">
                            <xsl:value-of select="concat(i18n:translate('metaData.edit'),' :')" />
                        </td>
                        <td class="metavalue">
                            <xsl:if test="acl:checkPermission($id,'writedb')">
                                <a
                                    href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=seditobj"
                                    target="_blank">
                                    <img src="{$WebApplicationBaseURL}images/workflow_objedit.gif" title="{i18n:translate('swf.object.editObject')}" />
                                </a>
                            </xsl:if>
                        </td>
                    </tr>
                </xsl:when>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <!-- ===================================================================================== -->

</xsl:stylesheet>






