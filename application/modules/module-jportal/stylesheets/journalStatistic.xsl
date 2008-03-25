<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:variable name="PageTitle" value="journalStatistic" />
    <xsl:template match="journal">
        <table cellspacing="0" cellpadding="0" style="border-bottom: 1px solid black;">
            <tr>
                <td width="200">
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
        <br/>
        <table style="border: 1px solid black;" cellspacing="0" cellpadding="0">
            <tr>
                <td width="300">-</td>
                <td width="100">absolute</td>
                <td width="150">percent</td>
            </tr>
            <xsl:apply-templates select="numberOfObjects/total" />
            <xsl:apply-templates select="numberOfObjects/complete" />
            <xsl:apply-templates select="numberOfObjects/incomplete" />
            <xsl:apply-templates select="numberOfObjects/missing" />
        </table>
    </xsl:template>

    <xsl:template match="total | complete | incomplete | missing">
        <tr>
            <td width="300">
                <xsl:value-of select="name()" />
                :
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