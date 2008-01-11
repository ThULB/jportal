<?xml version="1.0" encoding="ISO-8859-1" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:include href="objecttypes.xsl" />
    <xsl:variable name="PageTitle" select="'Dublettenfinder'" />
    <xsl:param name="toc.pageSize" select="10" />
    <xsl:param name="toc.pos" select="1" />
    <!-- ===================================================================================== -->

    <xsl:template match="redundancyMap">
        <table>
            <xsl:call-template name="redundancy.head" />
            <xsl:call-template name="lineBreak" />
            <xsl:call-template name="printDublicates" />
        </table>
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
            <xsl:value-of select="count(./redundancyID)" />
        </xsl:variable>

        <table>
            <tr>
                <td colspan="2">
                    <xsl:value-of select="concat(i18n:translate('metaData.sortbuttons.numberofres'),': ')" />
                    <b>
                        <xsl:value-of select="$numChildren" />
                    </b>
                    <xsl:call-template name="redundancy.printTOCNavi.chooseHitPage">
                        <xsl:with-param name="children" select="." />
                    </xsl:call-template>
                </td>
            </tr>
        </table>
    </xsl:template>

    <!-- ===================================================================================================== -->

    <xsl:template name="redundancy.printTOCNavi.chooseHitPage">
        <xsl:param name="children" />

        <xsl:variable name="numberOfChildren">
            <xsl:value-of select="count(./redundancyID)" />
        </xsl:variable>
        <xsl:variable name="numberOfHitPages">
            <xsl:value-of select="ceiling(number($numberOfChildren) div number($toc.pageSize))" />
        </xsl:variable>
        <xsl:if test="number($numberOfChildren)>number($toc.pageSize)">
            <xsl:value-of select="concat(', ',i18n:translate('metaData.resultpage'))" />
            <xsl:for-each select="./redundancyID[number($numberOfHitPages)>=position()]">
                <xsl:variable name="jumpToPos">
                    <xsl:value-of select="(position()*number($toc.pageSize))-number($toc.pageSize)" />
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="number($jumpToPos)+1=number($toc.pos)">
                        <xsl:value-of select="concat(' [',position(),'] ')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <a
                            href="{$WebApplicationBaseURL}{$HttpSession}redundancy.xml?XSL.template=template_18thCentury&amp;XSL.toc.pos.SESSION={$jumpToPos+1}">
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

        <xsl:for-each select="./redundancyID[(position()>=$toc.pos.verif) and ($toc.pos.verif+$toc.pageSize>position())]">
            <tr valign="top">
                <td width="49%" valign="top">
                    <b>
                        <form>
                            <xsl:copy-of select="concat('Dublette ',$toc.pos.verif + position() - 1,':  ')" />
                            <input type="radio" name="acknowledge" value="true" />
                            JA, bestätigen!
                            <input type="radio" name="acknowledge" value="false" />
                            NEIN, ablehnen!
                        </form>
                    </b>
                    <br />
                    <xsl:variable name="mcrobj" select="document(concat('mcrobject:',./text()))" />
                    <i>
                        <xsl:apply-templates select="$mcrobj" mode="present" />
                    </i>
                </td>
                <td width="2%"></td>
                <td width="49%" valign="top">

                    <b>
                        <form>Davon ist die Dublette:</form>
                    </b>
                    <br />
                    <xsl:variable name="mcrobj" select="document(concat('mcrobject:',@notRedundancyID))" />
                    <xsl:apply-templates select="$mcrobj" mode="present" />
                </td>
                <xsl:call-template name="lineBreak" />
            </tr>
        </xsl:for-each>

    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="lineBreak">
        <tr>
            <td colspan="3">
                <br />
                <hr />
                <br />
            </td>
        </tr>
    </xsl:template>

    <!-- ===================================================================================== -->

    <xsl:template name="editobject_with_der"></xsl:template>
    <xsl:template name="editobject"></xsl:template>
</xsl:stylesheet>