<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:param name="journalStatistic.dateReceived" />

    <xsl:variable name="journalStatistic.date">
        <xsl:call-template name="get.journalStatistic.date" />
    </xsl:variable>
    <xsl:variable name="PageTitle" select="'Statistik aller Zeitschriften'" />
    <xsl:variable name="chartBaseUrl" select="'http://chart.apis.google.com/chart?'" />
    <xsl:variable name="journalStatistic.sourceUrl" select="concat($WebApplicationBaseURL,'journalStatistic.xml')" />

    <!-- ======================================================================================================================== -->

    <xsl:template match="journalStatistic">

        <xsl:call-template name="journalStatistic.selectDate" />

        <xsl:call-template name="journalStatistic.total" />

        <xsl:call-template name="journalStatistic.toc" />
        <br />
        <br />
        <xsl:apply-templates select="statistic[@date=$journalStatistic.date]">
            <xsl:sort select="@date" />
        </xsl:apply-templates>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.selectDate">
        <p>
            <form method="get" action="{$journalStatistic.sourceUrl}" target="_self" id="sortByDate">
                Datum w�hlen:
                <select size="1" name="XSL.journalStatistic.dateReceived" onChange="document.getElementById('sortByDate').submit()">
                    <xsl:for-each select="statistic">
                        <xsl:sort select="@date" order="descending" />
                        <option value="{@date}">
                            <xsl:if test="@date=$journalStatistic.date">
                                <xsl:attribute name="selected"><xsl:value-of select="'selected'" />
                            </xsl:attribute>
                            </xsl:if>
                            <xsl:value-of select="@date" />
                        </option>
                    </xsl:for-each>
                </select>
            </form>
        </p>

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.toc">
        <p>
            <b>Detailierte Statistik f�r Zeitschrift:</b>
            <ul>
                <xsl:for-each select="statistic[@date=$journalStatistic.date]/journal">
                    <xsl:sort select="@name" />
                    <li>
                        <a href="#{@id}">
                            <xsl:value-of select="@name" />
                        </a>
                    </li>
                </xsl:for-each>
            </ul>
        </p>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template match="statistic">
        <xsl:apply-templates select="journal">
            <xsl:sort select="@name" />
        </xsl:apply-templates>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.total">
        <p>
            <b>Gesamt�bersicht:</b>
        </p>
        <xsl:for-each select="statistic[@date=$journalStatistic.date]">
            <xsl:variable name="chartLabelstemp">
                <xsl:for-each select="journal">
                    <xsl:sort select="./numberOfObjects/total/text()" />
                    <xsl:choose>
                        <xsl:when test="string-length(@name)>30">
                            <xsl:value-of
                                select="concat('|', substring(@name,1,30),'... (', numberOfObjects/total/text(),' - ', numberOfObjects/total/@percent,'%)')" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat('|', @name,' (', numberOfObjects/total/text(),' - ', numberOfObjects/total/@percent,'%)')" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="chartLabelsAll">
                <xsl:value-of select="concat('&amp;chl=',substring($chartLabelstemp,2))" />
            </xsl:variable>
            <xsl:variable name="chartValuestemp">
                <xsl:for-each select="journal">
                    <xsl:sort select="./numberOfObjects/total/text()" />
                    <xsl:value-of select="concat(',', numberOfObjects/total/@percent)" />
                </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="chartValuesAll">
                <xsl:value-of select="concat('&amp;chd=t:',substring($chartValuestemp,2))" />
            </xsl:variable>

            <xsl:variable name="chartParamsAll" select="'chs=810x300&amp;cht=p'" />
            <xsl:variable name="CompletePieChartURLAll" select="concat($chartBaseUrl, $chartParamsAll, $chartLabelsAll, $chartValuesAll)" />
            <p style="text-align: center;">
                <img src="{$CompletePieChartURLAll}" />
            </p>
        </xsl:for-each>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template match="journal">
        <xsl:variable name="journal-id" select="@id" />
        <xsl:variable name="total-obj" select="numberOfObjects/total/text()" />
        <xsl:variable name="journal-type">
            <xsl:choose>
                <xsl:when test="@type='fully'">
                    <xsl:value-of select="concat(' (vollst�ndige Erschliessung, mit insgesamt ', $total-obj, ' Artikeln)')" />
                </xsl:when>
                <xsl:when test="@type='browse'">
                    <xsl:value-of select="concat(' (Bl�tterzeitschrift, mit insgesamt ', $total-obj, ' B�nden)')" />
                </xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <div style="width: 700px; border: 1px solid black;">
            <a name="{@id}" />
            <table cellspacing="0" cellpadding="0" style="border-bottom: 1px solid black; padding: 5px;">
                <tr>
                    <td width="500" style="font-weight: bold;">
                        <xsl:text>Zeitschrift: 
                        </xsl:text>
                        <a href="{$WebApplicationBaseURL}receive/{$journal-id}">
                            <xsl:value-of select="concat(@name, $journal-type)" />
                        </a>
                    </td>
                </tr>
            </table>
            <br />

            <xsl:variable name="pervalue1" select="numberOfObjects/complete/@percent" />
            <xsl:variable name="pervalue2" select="numberOfObjects/incomplete/@percent" />
            <xsl:variable name="pervalue3" select="numberOfObjects/missing/@percent" />
            <xsl:variable name="absvalue1" select="numberOfObjects/complete/text()" />
            <xsl:variable name="absvalue2" select="numberOfObjects/incomplete/text()" />
            <xsl:variable name="absvalue3" select="numberOfObjects/missing/text()" />

            <xsl:variable name="scale">
                <xsl:value-of select="round(numberOfObjects/total/@scale)" />
            </xsl:variable>

            <xsl:variable name="label1" select="'Vollst�ndig'" />
            <xsl:variable name="label2" select="'kein Digitalisat'" />
            <xsl:variable name="label3" select="'kein Artikel'" />

            <xsl:variable name="chartTitle" select="concat('&amp;chtt=','','&amp;chts=','000000,10')" />
            <xsl:variable name="chartLabels"
                select="concat('&amp;chl=', $label1, ' (',$absvalue1,' - ',$pervalue1,'%)', '|', $label2, ' (',$absvalue2,' - ',$pervalue2,'%)', '|', $label3, ' (',$absvalue3,' - ',$pervalue3,'%)')" />
            <xsl:variable name="chartParams" select="concat('chs=', '600x', $scale,'&amp;cht=p')" />
            <xsl:variable name="chartValues" select="concat('&amp;chd=t:', $pervalue1, ',', $pervalue2, ',', $pervalue3)" />
            <xsl:variable name="chartColor" select="concat('&amp;chco=', '44ca20', ',', 'ca2020', ',', 'f96820')" />
            <xsl:variable name="CompletePieChartURL" select="concat($chartBaseUrl, $chartParams, $chartValues, $chartColor)" />

            <xsl:text>
                 Zustand der Zeitschrift zum ausgew�hlten Datum:
            </xsl:text>

            <p style="text-align: center;">
                <img src="{$CompletePieChartURL}" />
            </p>
            <!-- Legende -->
            <div style="border: 1px inset black; width: 200px; margin: 5px;">
                <table>
                    <tr>
                        <td style="border: 1px inset black; background-color: #44ca20; width: 20px;">
                            <xsl:value-of select="' '" />
                        </td>
                        <td>
                            <xsl:value-of select="$label1" />
                        </td>
                        <td>
                            <xsl:value-of select="concat($absvalue1,' - ',$pervalue1, '%')" />
                        </td>
                    </tr>
                    <tr>
                        <td style="border: 1px inset black; background-color: #ca2020;">
                            <xsl:value-of select="' '" />
                        </td>
                        <td>
                            <xsl:value-of select="$label2" />
                        </td>
                        <td>
                            <xsl:value-of select="concat($absvalue2,' - ',$pervalue2, '%')" />
                        </td>
                    </tr>
                    <tr>
                        <td style="border: 1px inset black; background-color: #f96820;">
                            <xsl:value-of select="' '" />
                        </td>
                        <td>
                            <xsl:value-of select="$label3" />
                        </td>
                        <td>
                            <xsl:value-of select="concat($absvalue3,' - ',$pervalue3, '%')" />
                        </td>
                    </tr>
                </table>
            </div>
            <xsl:text>
                 Entwicklung des Datenbestands �ber die Zeit:
            </xsl:text>
            <xsl:call-template name="get.journalStatistic.BarChart">
                <xsl:with-param name="j_id" select="@id" />
            </xsl:call-template>
        </div>
        <br />
        <br />

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="get.journalStatistic.date">
        <xsl:choose>
            <xsl:when test="$journalStatistic.dateReceived=''">
                <xsl:for-each select="/journalStatistic/statistic">
                    <xsl:sort select="@date" order="descending" />
                    <xsl:if test="position()=1">
                        <xsl:value-of select="@date" />
                    </xsl:if>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$journalStatistic.dateReceived" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="get.journalStatistic.BarChart">
        <xsl:param name="j_id" />

        <xsl:variable name="maxTotal">
            <xsl:for-each select="/journalStatistic/statistic/journal[@id=$j_id]/numberOfObjects/total">
                <xsl:sort data-type="text()" order="descending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="complVal">
            <xsl:for-each select="/journalStatistic/statistic">
                <xsl:value-of
                    select="concat(',',(journal[@id=$j_id]/numberOfObjects/complete/text()*(journal[@id=$j_id]/numberOfObjects/total/text()*100 div $maxTotal) div journal[@id=$j_id]/numberOfObjects/total/text()))" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="incomplVal">
            <xsl:for-each select="/journalStatistic/statistic">
                <xsl:value-of
                    select="concat(',',(journal[@id=$j_id]/numberOfObjects/incomplete/text()*(journal[@id=$j_id]/numberOfObjects/total/text()*100 div $maxTotal) div journal[@id=$j_id]/numberOfObjects/total/text()))" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="missingVal">
            <xsl:for-each select="/journalStatistic/statistic">
                <xsl:value-of
                    select="concat(',',(journal[@id=$j_id]/numberOfObjects/missing/text()*(journal[@id=$j_id]/numberOfObjects/total/text()*100 div $maxTotal) div journal[@id=$j_id]/numberOfObjects/total/text()))" />
            </xsl:for-each>
        </xsl:variable>
        
        <xsl:variable name="lineVal">
            <xsl:for-each select="/journalStatistic/statistic">
                <xsl:value-of
                    select="concat(',',(journal[@id=$j_id]/numberOfObjects/complete/text()*(journal[@id=$j_id]/numberOfObjects/total/text()*100 div $maxTotal) div journal[@id=$j_id]/numberOfObjects/total/text())+(journal[@id=$j_id]/numberOfObjects/incomplete/text()*(journal[@id=$j_id]/numberOfObjects/total/text()*100 div $maxTotal) div journal[@id=$j_id]/numberOfObjects/total/text()))" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="xLabels">
            <xsl:for-each select="/journalStatistic/statistic">
                <xsl:value-of select="concat('|',substring(@datePretty,4))" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="barChartLabels" select="concat('&amp;chxt=x&amp;chxl=0:',$xLabels)" />
        <xsl:variable name="barChartParams" select="concat('chs=320x250', '&amp;cht=bvs')" />
        <xsl:variable name="barChartValues"
            select="concat('&amp;chd=t:', substring($complVal,2), '|', substring($incomplVal,2), '|', substring($missingVal,2))" />
        <xsl:variable name="barChartColor" select="concat('&amp;chco=', '44ca20', ',', 'ca2020', ',', 'f96820')" />
        <xsl:variable name="barChartWidth" select="concat('&amp;chbh=','25,25')" />
        <xsl:variable name="CompleteBarChartURL"
            select="concat($chartBaseUrl, $barChartParams, $barChartValues, $barChartColor, $barChartLabels, $barChartWidth)" />

        <xsl:variable name="lineChartLabels" select="concat('&amp;chxt=x&amp;chxl=0:',$xLabels)" />
        <xsl:variable name="lineChartParams" select="concat('chs=320x250', '&amp;cht=lc')" />
        <xsl:variable name="lineChartValues"
            select="concat('&amp;chd=t:', substring($lineVal,2))" />
        <xsl:variable name="lineChartColor" select="concat('&amp;chco=', '76A4FB')" />
        <xsl:variable name="CompleteLineChartURL"
            select="concat($chartBaseUrl, $lineChartParams, $lineChartValues, $lineChartColor, $lineChartLabels)" />

        <p style="padding-left: 10px;">
            <table>
                <tr>
                    <td>
                        <img src="{$CompleteBarChartURL}" />
                    </td>
                    <td style="width: 20px;">
                       
                    </td>
                    <td>
                        <img src="{$CompleteLineChartURL}" />
                    </td>
                </tr>
            </table>
        </p>
    </xsl:template>

    <!-- ======================================================================================================================== -->


</xsl:stylesheet>