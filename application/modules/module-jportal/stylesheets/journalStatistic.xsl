<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:variable name="PageTitle" />
    <xsl:variable name="svgURL" select="concat($RequestURL, '?XSL.Style=svg')" />
    <!-- ======================================================================================================================== -->

    <xsl:template match="journalStatistic">
        <xsl:apply-templates select="statistic">
            <xsl:sort select="@date" />
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="statistic">
        <div style="font-size: 16px; text-decoration:underline;">
        <xsl:value-of select="concat(' - ','Date: ',@date)"/>
        </div>
        <xsl:apply-templates select="journal">
            <xsl:sort select="@name" />
        </xsl:apply-templates>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template match="journal">
    <div style="border: 1px solid black;">
        <table cellspacing="0" cellpadding="0" style="border-bottom: 1px solid black; padding: 5px;">
            <tr>
                <td width="200" style="font-weight: bold;">
                    Name:
                    <xsl:value-of select="@name" />
                </td>
                <td width="100">
                    Type:
                    <xsl:value-of select="@type" />
                </td>
                <td width="150">
                    ID:
                    <xsl:value-of select="@id" />
                </td>
            </tr>
        </table>
        <br />
        <table id="GraphThisTable" cellspacing="0" cellpadding="0" style="padding: 5px;">
            <tr>
                <td width="300">Status</td>
                <td width="100">Absolute</td>
                <td width="150">Percent</td>
            </tr>
            <xsl:apply-templates select="numberOfObjects/total" />
            <xsl:apply-templates select="numberOfObjects/complete" />
            <xsl:apply-templates select="numberOfObjects/incomplete" />
            <xsl:apply-templates select="numberOfObjects/missing" />
        </table>
        <script language="JavaScript" src="{$WebApplicationBaseURL}/journalStatistic/piechart.js" type="text/javascript" />
        </div>
        <br />
        <br />

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template match="total | complete | incomplete | missing">
        <tr>
            <td width="300">
                <xsl:value-of select="name()" />
            </td>
            <td width="100">
                <xsl:value-of select="text()" />
            </td>
            <td width="150">
                <xsl:value-of select="@percent" />
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>