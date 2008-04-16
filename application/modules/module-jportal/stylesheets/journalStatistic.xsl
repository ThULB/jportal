<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:param name="journalStatistic.dateReceived.From" />
    <xsl:param name="journalStatistic.dateReceived.Till" />
    <xsl:param name="journalStatistic.view.objectListing" select="'false'" />
    <xsl:param name="journalStatistic.view.objectListing.journalID" />

    <xsl:variable name="journalStatistic.date.From">
        <xsl:call-template name="get.journalStatistic.date.From" />
    </xsl:variable>
    <xsl:variable name="journalStatistic.date.Till">
        <xsl:call-template name="get.journalStatistic.date.Till" />
    </xsl:variable>
    <xsl:variable name="completeSumValues">
        <xsl:call-template name="journalStatistic.get.ActivityIndex">
            <xsl:with-param name="quantifier" select="'2'" />
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="PageTitle" select="'Zeitschriftenstatistik'" />
    <xsl:variable name="chartBaseUrl" select="'http://chart.apis.google.com/chart?'" />
    <xsl:variable name="journalStatistic.sourceUrl" select="concat($WebApplicationBaseURL,'journalStatistic.xml')" />
    <xsl:variable name="color.complete" select="'44ca20'" />
    <xsl:variable name="color.incomplete" select="'ca2020'" />
    <xsl:variable name="color.missing" select="'f96820'" />
    <xsl:variable name="color.corruptedTotal" select="'ffffff'" />
    <xsl:variable name="color.corruptedTotalFill" select="'f1f1f1'" />
    <xsl:variable name="color.zero" select="'eeeeee'" />
    <xsl:variable name="chartSize.objectDev" select="'850x150'" />
    <xsl:variable name="chartSize.consistancyDev" select="'850x250'" />

    <xsl:variable name="xgrid">
        <xsl:call-template name="journalStatistic.get.gridcount" />
    </xsl:variable>

    <!-- ======================================================================================================================== -->

    <xsl:template match="journalStatistic">

        <xsl:choose>
            <xsl:when test="$CurrentUser='gast'">Zugriff untersagt. Bitte melden sie sich am System an!</xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="journalStatistic.main" />
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.main">
        <xsl:choose>
            <xsl:when test="$journalStatistic.view.objectListing='true'">
                <xsl:call-template name="journalStatistic.objectListing" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="journalStatistic.selectDateSpace" />
                <xsl:call-template name="journalStatistic.total" />
                <div style="border: 2px solid black; width: 900px;">
                    <h3>Aktivitätsindex aller Zeitschriften:</h3>

                    <xsl:call-template name="journalStatistic.PieChart.ArticleOverTime" />
                </div>
                <xsl:call-template name="journalStatistic.toc" />
                <xsl:call-template name="journalStatistic.statistics" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.get.gridcount">

        <xsl:value-of
            select="(100 div (count(/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))])-1))" />

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.objectListing">
        <p>
            <a name="incomplete" />
            <b>Artikel ohne Digitalisat:</b>
            <ul>
                <xsl:for-each
                    select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]/journal[@id=$journalStatistic.view.objectListing.journalID]/objectList[@type='incomplete']/object">
                    <li>
                        <a href="{$WebApplicationBaseURL}receive/{@id}">
                            Artikel
                            <xsl:value-of select="@id" />
                        </a>
                    </li>
                </xsl:for-each>
            </ul>
            <br />
            <br />
            <a name="missing" />
            <b>Bände ohne Artikel:</b>
            <ul>
                <xsl:for-each
                    select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]/journal[@id=$journalStatistic.view.objectListing.journalID]/objectList[@type='missing']/object">
                    <li>
                        <a href="{$WebApplicationBaseURL}receive/{@id}">
                            Band
                            <xsl:value-of select="@id" />
                        </a>
                    </li>
                </xsl:for-each>
            </ul>
        </p>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.statistics">
        <br />
        <br />
        <xsl:apply-templates select="statistic[@date=$journalStatistic.date.Till]">
            <xsl:sort select="@date" />
        </xsl:apply-templates>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.selectDateSpace">
        <p>
            <form method="get" action="{$journalStatistic.sourceUrl}" target="_self" id="sortByDate1">
                <table>
                    <tr>
                        <td colspan="2">
                            <strong>Zeitspanne der Erhebung:</strong>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            von:
                            <select size="1" name="XSL.journalStatistic.dateReceived.From">
                                <xsl:for-each select="statistic">
                                    <xsl:sort select="@date" order="descending" />
                                    <option value="{@date}">
                                        <xsl:if test="@date=$journalStatistic.date.From">
                                            <xsl:attribute name="selected"><xsl:value-of select="'selected'" />
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:value-of select="@datePretty" />
                                    </option>
                                </xsl:for-each>
                            </select>
                        </td>
                        <td>
                            bis:
                            <select size="1" name="XSL.journalStatistic.dateReceived.Till">
                                <xsl:for-each select="statistic">
                                    <xsl:sort select="@date" order="descending" />
                                    <option value="{@date}">
                                        <xsl:if test="@date=$journalStatistic.date.Till">
                                            <xsl:attribute name="selected"><xsl:value-of select="'selected'" />
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:value-of select="@datePretty" />
                                    </option>
                                </xsl:for-each>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="submit" value=" Auswählen " />
                        </td>
                    </tr>
                </table>
            </form>
        </p>

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.toc">
        <p>
            <b>Detailierte Statistik für Zeitschrift:</b>
            <ul>
                <xsl:for-each select="statistic[number(@date) = number($journalStatistic.date.Till)]/journal">
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
            <b>Gesamtübersicht:</b>
        </p>
        <xsl:for-each select="statistic[number(@date) = number($journalStatistic.date.Till)]">
            <xsl:variable name="chartLabelstemp">
                <xsl:for-each select="journal">
                    <xsl:sort select="./numberOfObjects/total/@percent" data-type="number" order="descending" />
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
                    <xsl:sort select="./numberOfObjects/total/@percent" data-type="number" order="descending" />
                    <xsl:value-of select="concat(',', numberOfObjects/total/@percent)" />
                </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="chartValuesAll">
                <xsl:value-of select="concat('&amp;chd=t:',substring($chartValuestemp,2))" />
            </xsl:variable>

            <xsl:variable name="chartParamsAll" select="'chs=810x300&amp;cht=p'" />
            <xsl:variable name="CompletePieChartURLAll" select="concat($chartBaseUrl, $chartParamsAll, $chartValuesAll, $chartLabelsAll )" />
            <p style="text-align: center;">
                <img src="{$CompletePieChartURLAll}" />
            </p>
        </xsl:for-each>

        <xsl:variable name="allSums">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <node>
                    <xsl:value-of select="sum(journal/numberOfObjects/total/text())" />
                </node>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="maxSum">
            <xsl:for-each select="xalan:nodeset($allSums)/node">
                <xsl:sort data-type="number" order="descending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="minSum">
            <xsl:for-each select="xalan:nodeset($allSums)/node">
                <xsl:sort data-type="number" order="ascending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="maxMinDistance">
            <xsl:value-of select="($maxSum - $minSum)" />
        </xsl:variable>

        <xsl:variable name="theoreticMinimum">
            <xsl:value-of select="($minSum - (($maxMinDistance div 75)*25))" />
        </xsl:variable>

        <xsl:variable name="totalSum">
            <xsl:for-each select="xalan:nodeset($allSums)/node">
                <xsl:value-of select="concat( (((text() - $theoreticMinimum)*100) div ($maxSum - $theoreticMinimum)) ,',')" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="totalLC.values">
            <xsl:value-of select="substring($totalSum,1,string-length($totalSum)-1)" />
        </xsl:variable>
        <xsl:variable name="chartURL.label.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="concat(@datePretty,'|')" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="label1">
            <xsl:value-of select="round($minSum)" />
        </xsl:variable>
        <xsl:variable name="label2">
            <xsl:value-of select="round($minSum+($maxMinDistance*0.33))" />
        </xsl:variable>
        <xsl:variable name="label3">
            <xsl:value-of select="round($minSum+($maxMinDistance*0.66))" />
        </xsl:variable>
        <xsl:variable name="label4">
            <xsl:value-of select="round($maxSum)" />
        </xsl:variable>

        <xsl:variable name="chartURL.label">
            <xsl:value-of select="concat('|',$chartURL.label.tmp,'1:|',0,'|',$label1,'|',$label2,'|',$label3,'|',$label4)" />
        </xsl:variable>

        <!--  do layout -->
        <b>
            <xsl:value-of select="concat('Gesamtanzahl der Artikel über die Zeit (Differenz = ',$maxMinDistance,'):')" />
        </b>
        <p style="text-align: center;">
            <img
                src="{concat($chartBaseUrl,'cht=lc&amp;chd=t:',$totalLC.values,'&amp;chs=',$chartSize.objectDev,'&amp;chxt=x,y&amp;chxl=0:',$chartURL.label,'&amp;chco=0000ff','&amp;chg=',$xgrid,',25')}" />
        </p>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template match="journal">

        <xsl:variable name="journal-name">
            <xsl:value-of select="@name" />
        </xsl:variable>

        <!-- headline -->
        <xsl:variable name="journal-type">
            <xsl:choose>
                <xsl:when test="@type='fully'">
                    <xsl:value-of select="concat(' (vollständige Erschliessung, mit insgesamt ', numberOfObjects/total/text(), ' Artikeln)')" />
                </xsl:when>
                <xsl:when test="@type='browse'">
                    <xsl:value-of select="concat(' (Blätterzeitschrift, mit insgesamt ', numberOfObjects/total/text(), ' Bänden)')" />
                </xsl:when>
            </xsl:choose>
        </xsl:variable>

        <div style="width: 900px; border: 2px solid black;">
            <!-- Pie chart of currently state -->
            <xsl:call-template name="journalStatistic.currentlyState">
                <xsl:with-param name="journalType" select="$journal-type" />
            </xsl:call-template>
            <br />
            <br />
            
            <xsl:variable name="activityOfJournal">
                <xsl:value-of select="sum(xalan:nodeset($completeSumValues)/journal[@label=$journal-name]/text())" />
            </xsl:variable>
            <xsl:value-of select="concat('Aktivitätsindex der Zeitung im ausgewählten Zeitraum: ',$activityOfJournal)" />
            <br />
            <br />
            <!-- consistancy development -->
            <xsl:call-template name="journalStatistic.consistancyDev">
                <xsl:with-param name="jID" select="@id"></xsl:with-param>
            </xsl:call-template>
            <br />
            <br />
            <!-- Object development -->
            <xsl:call-template name="journalStatistic.objectDev">
                <xsl:with-param name="jID" select="@id"></xsl:with-param>
            </xsl:call-template>


        </div>
        <br />
        <br />
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.objectDev">
        <xsl:param name="jID"></xsl:param>

        <xsl:variable name="maxTotal">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]/journal[@id=$jID]/numberOfObjects/total">
                <xsl:sort data-type="number" order="descending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="minTotal">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]/journal[@id=$jID]/numberOfObjects/total">
                <xsl:sort data-type="number" order="ascending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="maxMinDistance">
            <xsl:value-of select="number($maxTotal - $minTotal)" />
        </xsl:variable>

        <xsl:variable name="headline">
            <xsl:choose>
                <xsl:when test="@type='fully'">
                    <xsl:value-of select="concat('Zahl der Artikel im zeitlichen Verlauf (Differenz = ',$maxMinDistance ,') :')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat('Zahl der Bände im zeitlichen Verlauf (Differenz = ',$maxMinDistance ,') :')" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="theoreticMinimum">
            <xsl:choose>
                <xsl:when test="$maxMinDistance!='0'">
                    <xsl:value-of select="($minTotal - (($maxMinDistance div 75)*25))" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="0" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="lineVal">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:value-of
                    select="concat(',', (((number(journal[@id=$jID]/numberOfObjects/total/text()) - $theoreticMinimum)*100) div ($maxTotal - $theoreticMinimum)) )" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="lineChartValues" select="concat('&amp;chd=t:', substring($lineVal,2))" />

        <xsl:variable name="chartURL.values.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="concat(number(journal[@id=$jID]/numberOfObjects/total/text()),',')" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="chartURL.values">
            <xsl:value-of select="substring($chartURL.values.tmp,1,string-length($chartURL.values.tmp)-1)" />
        </xsl:variable>
        <xsl:variable name="chartURL.label.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="concat(@datePretty,'|')" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="label1">
            <xsl:choose>
                <xsl:when test="$maxMinDistance!='0'">
                    <xsl:value-of select="round($minTotal)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="''" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="label2">
            <xsl:choose>
                <xsl:when test="$maxMinDistance!='0'">
                    <xsl:value-of select="round($minTotal+($maxMinDistance*0.33))" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="''" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="label3">
            <xsl:choose>
                <xsl:when test="$maxMinDistance!='0'">
                    <xsl:value-of select="round($minTotal+($maxMinDistance*0.66))" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="''" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="label4">
            <xsl:value-of select="round($maxTotal)" />
        </xsl:variable>

        <xsl:variable name="chartURL.label">
            <xsl:value-of select="concat('|',$chartURL.label.tmp,'1:|',0,'|',$label1,'|',$label2,'|',$label3,'|',$label4)" />
        </xsl:variable>

        <!--  do layout -->
        <b>
            <xsl:value-of select="$headline" />
        </b>
        <p style="text-align: center;">
            <img
                src="{concat($chartBaseUrl,'cht=lc&amp;chd=t:',$lineChartValues,'&amp;chs=',$chartSize.objectDev,'&amp;chxt=x,y&amp;chxl=0:',$chartURL.label,'&amp;chco=0000ff','&amp;chg=',$xgrid,',25')}" />
        </p>

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.consistancyDev">
        <xsl:param name="jID" />

        <xsl:variable name="CompleteDiff">
            <xsl:call-template name="chartURL.get.ComplDiff">
                <xsl:with-param name="jID" select="$jID" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="IncompleteDiff">
            <xsl:call-template name="chartURL.get.IncomplDiff">
                <xsl:with-param name="jID" select="$jID" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="MissingDiff">
            <xsl:call-template name="chartURL.get.MissingDiff">
                <xsl:with-param name="jID" select="$jID" />
            </xsl:call-template>
        </xsl:variable>


        <xsl:variable name="headline">
            <xsl:value-of
                select="concat('Konsistenzanalyse im zeitlichen Verlauf (Vollständige = ',$CompleteDiff,', kein Digitalisat = ',$IncompleteDiff,', Band ohne Artikel = ',$MissingDiff,') :')" />
        </xsl:variable>

        <xsl:variable name="chartURL.values">
            <xsl:call-template name="journalStatistic.consistancyDev.collectValues">
                <xsl:with-param name="journalID" select="$jID" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="chartURL.label">
            <xsl:call-template name="chartURL.label" />
        </xsl:variable>
        <xsl:variable name="chartURL.colors">
            <xsl:call-template name="chartURL.colors" />
        </xsl:variable>
        <xsl:variable name="fillColors">
            <xsl:call-template name="chartURL.fillColors" />
        </xsl:variable>

        <!--  do layout -->
        <b>
            <xsl:copy-of select="$headline" />
        </b>
        <p style="text-align: center;">
            <img
                src="{concat($chartBaseUrl,'cht=lc&amp;chd=t:',$chartURL.values,'&amp;chs=',$chartSize.consistancyDev,'&amp;chxt=x,y&amp;chxl=0:',$chartURL.label,'&amp;chco=',$chartURL.colors,'&amp;chm=',$fillColors,'&amp;chg=',$xgrid,',25')}" />
        </p>

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="chartURL.get.ComplDiff">
        <xsl:param name="jID" />

        <xsl:variable name="allComplete">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <complete>
                    <xsl:value-of select="number(journal[@id=$jID]/numberOfObjects/complete/text())" />
                </complete>
            </xsl:for-each>

        </xsl:variable>

        <xsl:variable name="maxTotal">
            <xsl:value-of select="xalan:nodeset($allComplete)/complete[position()=last()]" />
        </xsl:variable>
        <xsl:variable name="minTotal">
            <xsl:value-of select="xalan:nodeset($allComplete)/complete[position()=1]" />
        </xsl:variable>

        <xsl:value-of select="number($maxTotal - $minTotal)" />
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="chartURL.get.IncomplDiff">
        <xsl:param name="jID" />

        <xsl:variable name="allIncomplete">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <incomplete>
                    <xsl:value-of select="journal[@id=$jID]/numberOfObjects/incomplete/text()" />
                </incomplete>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="maxTotal">
            <xsl:value-of select="xalan:nodeset($allIncomplete)/incomplete[position()=last()]" />
        </xsl:variable>
        <xsl:variable name="minTotal">
            <xsl:value-of select="xalan:nodeset($allIncomplete)/incomplete[position()=1]" />
        </xsl:variable>

        <xsl:value-of select="number($maxTotal - $minTotal)" />
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="chartURL.get.MissingDiff">
        <xsl:param name="jID" />

        <xsl:variable name="allMissing">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <missing>
                    <xsl:value-of select="journal[@id=$jID]/numberOfObjects/missing/text()" />
                </missing>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="maxTotal">
            <xsl:value-of select="xalan:nodeset($allMissing)/missing[position()=last()]" />
        </xsl:variable>
        <xsl:variable name="minTotal">
            <xsl:value-of select="xalan:nodeset($allMissing)/missing[position()=1]" />
        </xsl:variable>

        <xsl:value-of select="number($maxTotal - $minTotal)" />
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="chartURL.fillColors">
        <xsl:choose>
            <xsl:when test="@type='fully'">
                <xsl:value-of
                    select="concat('b,',$color.complete,',0,1,0|b,',$color.corruptedTotalFill,',1,2,0|b,',$color.corruptedTotalFill,',2,3,0|b,',$color.corruptedTotalFill,',3,4,0')" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat('b,',$color.complete,',0,1,0|b,',$color.incomplete,',1,2,0')" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="chartURL.colors">
        <xsl:choose>
            <xsl:when test="@type='fully'">
                <xsl:value-of
                    select="concat($color.complete,',',$color.corruptedTotal,',',$color.incomplete,',',$color.missing,',',$color.complete,',',$color.zero)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat($color.complete,',',$color.incomplete,',',$color.zero)" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="chartURL.label">
        <xsl:variable name="chartURL.label.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="concat(@datePretty,'|')" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:value-of select="concat('|',substring($chartURL.label.tmp,1,string-length($chartURL.label.tmp)),'1:|0%|25%|50%|75%|100%')" />
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.consistancyDev.collectValues">
        <xsl:param name="journalID" />
        <!-- zero graph -->
        <xsl:variable name="values.zero.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="'0,'" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.zero">
            <xsl:value-of select="substring($values.zero.tmp,1,string-length($values.zero.tmp)-1)" />
        </xsl:variable>
        <!-- missings -->
        <xsl:variable name="values.missing.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="concat(./journal[@id=$journalID]/numberOfObjects/missing/@percent,',')" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.missing">
            <xsl:value-of select="substring($values.missing.tmp,1,string-length($values.missing.tmp)-1)" />
        </xsl:variable>
        <!-- incompletes -->
        <xsl:variable name="values.incomplete.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="concat(./journal[@id=$journalID]/numberOfObjects/incomplete/@percent,',')" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.incomplete">
            <xsl:value-of select="substring($values.incomplete.tmp,1,string-length($values.incomplete.tmp)-1)" />
        </xsl:variable>
        <!-- corrupted total -->
        <xsl:variable name="values.corruptedTotal.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:variable name="totalValue">
                    <xsl:value-of
                        select="number(./journal[@id=$journalID]/numberOfObjects/incomplete/@percent) + number(./journal[@id=$journalID]/numberOfObjects/missing/@percent)" />
                </xsl:variable>
                <xsl:value-of select="concat($totalValue,',')" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.corruptedTotal">
            <xsl:value-of select="substring($values.corruptedTotal.tmp,1,string-length($values.corruptedTotal.tmp)-1)" />
        </xsl:variable>
        <!-- fully graph -->
        <xsl:variable name="values.100.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="concat(100,',')" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.100">
            <xsl:value-of select="substring($values.100.tmp,1,string-length($values.100.tmp)-1)" />
        </xsl:variable>
        <!-- return value -->
        <xsl:choose>
            <xsl:when test="@type='fully'">
                <xsl:value-of select="concat($values.100,'|',$values.corruptedTotal,'|',$values.incomplete,'|',$values.missing,'|',$values.zero)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat($values.100,'|',$values.incomplete,'|',$values.zero)" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.currentlyState">
        <xsl:param name="journalType" />

        <a name="{@id}" />
        <table cellspacing="0" cellpadding="0" style="border-bottom: 1px solid black; padding: 5px;">
            <tr>
                <td width="900" style="font-weight: bold;">
                    <xsl:text>Zeitschrift: 
                        </xsl:text>
                    <a href="{$WebApplicationBaseURL}receive/{@id}">
                        <xsl:value-of select="concat(@name, $journalType)" />
                    </a>
                </td>
            </tr>
        </table>
        <br />
        <b>
            <xsl:value-of select="'  Konsistenzanalyse für gewähltes Datum:'" />
        </b>
        <xsl:variable name="pervalue1" select="numberOfObjects/complete/@percent" />
        <xsl:variable name="pervalue2" select="numberOfObjects/incomplete/@percent" />
        <xsl:variable name="pervalue3" select="numberOfObjects/missing/@percent" />
        <xsl:variable name="absvalue1" select="numberOfObjects/complete/text()" />
        <xsl:variable name="absvalue2" select="numberOfObjects/incomplete/text()" />
        <xsl:variable name="absvalue3" select="numberOfObjects/missing/text()" />

        <xsl:variable name="scale">
            <xsl:value-of select="round(numberOfObjects/total/@scale)" />
        </xsl:variable>

        <xsl:variable name="label1" select="'Vollständig:'" />
        <xsl:variable name="label2" select="'kein Digitalisat:'" />
        <xsl:variable name="label3" select="'Band ohne Artikel:'" />

        <xsl:variable name="chartLabels"
            select="concat('&amp;chl=', $label1, ' (',$absvalue1,' - ',$pervalue1,'%)', '|', $label2, ' (',$absvalue2,' - ',$pervalue2,'%)', '|', $label3, ' (',$absvalue3,' - ',$pervalue3,'%)')" />
        <xsl:variable name="chartParams" select="concat('chs=', '600x', $scale,'&amp;cht=p')" />
        <xsl:variable name="chartValues" select="concat('&amp;chd=t:', $pervalue1, ',', $pervalue2, ',', $pervalue3)" />
        <xsl:variable name="chartColor" select="concat('&amp;chco=', '44ca20', ',', 'ca2020', ',', 'f96820')" />
        <xsl:variable name="CompletePieChartURL" select="concat($chartBaseUrl, $chartParams, $chartValues, $chartColor)" />
        <p style="text-align: center;">
            <img src="{$CompletePieChartURL}" />
            <!-- Legende -->
            <div style="border: 1px inset black; width: 220px; margin: 5px;">
                <table>
                    <tr>
                        <td style="border: 1px inset black; background-color: #{$color.complete}; width: 20px;">
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
                        <td style="border: 1px inset black; background-color: #{$color.incomplete}">
                            <xsl:value-of select="' '" />
                        </td>
                        <td>
                            <xsl:choose>
                                <xsl:when test="$absvalue2 &gt; 0">
                                    <a target="_blank"
                                        href="{$journalStatistic.sourceUrl}?XSL.journalStatistic.view.objectListing=true&amp;XSL.journalStatistic.view.objectListing.journalID={@id}#incomplete">
                                        <xsl:value-of select="$label2" />
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="$label2" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </td>
                        <td>
                            <xsl:value-of select="concat($absvalue2,' - ',$pervalue2, '%')" />
                        </td>
                    </tr>
                    <xsl:if test="@type='fully'">
                        <tr>
                            <td style="border: 1px inset black; background-color: #{$color.missing}">
                                <xsl:value-of select="' '" />
                            </td>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$absvalue3 &gt; 0">
                                        <a target="_blank"
                                            href="{$journalStatistic.sourceUrl}?XSL.journalStatistic.view.objectListing=true&amp;XSL.journalStatistic.view.objectListing.journalID={@id}#missing">
                                            <xsl:value-of select="$label3" />
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$label3" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <xsl:value-of select="concat($absvalue3,' - ',$pervalue3, '%')" />
                            </td>
                        </tr>
                    </xsl:if>
                </table>
            </div>
        </p>
    </xsl:template>

    <!-- ======================================================================================================================== -->
    <xsl:template name="get.journalStatistic.date.From">
        <xsl:choose>
            <xsl:when test="$journalStatistic.dateReceived.From=''">
                <xsl:for-each select="/journalStatistic/statistic">
                    <xsl:sort select="@date" order="ascending" />
                    <xsl:if test="position()=1">
                        <xsl:value-of select="@date" />
                    </xsl:if>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$journalStatistic.dateReceived.From" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ======================================================================================================================== -->
    <xsl:template name="get.journalStatistic.date.Till">
        <xsl:choose>
            <xsl:when test="$journalStatistic.dateReceived.Till=''">
                <xsl:for-each select="/journalStatistic/statistic">
                    <xsl:sort select="@date" order="descending" />
                    <xsl:if test="position()=1">
                        <xsl:value-of select="@date" />
                    </xsl:if>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$journalStatistic.dateReceived.Till" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ======================================================================================================================== -->
    <xsl:template name="journalStatistic.PieChart.ArticleOverTime">

        <xsl:variable name="activitySum">
            <xsl:value-of select="sum(xalan:nodeset($completeSumValues)/journal/text())" />
        </xsl:variable>

        <xsl:variable name="chartLabelsTemp">
            <xsl:for-each select="xalan:nodeset($completeSumValues)/journal">
                <xsl:sort data-type="number" order="descending" />
                <xsl:if test="number(text()) &gt; 0">
                    <xsl:choose>
                        <xsl:when test="string-length(@label) &gt; 28">
                            <xsl:value-of
                                select="concat('|',substring(@label,1,28),'...','(',round(text()),' - ',substring(string(((text()*100) div $activitySum)),1,4),'%)')" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat('|',@label,'(',round(text()),' - ',substring(string(((text()*100) div $activitySum)),1,5),'%)')" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="ZeroLabels">
            <xsl:for-each select="xalan:nodeset($completeSumValues)/journal">
                <xsl:sort data-type="text" select="@label" order="ascending" />
                <xsl:if test="number(text()) = 0">
                    <name>
                        <xsl:value-of select="@label" />
                    </name>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="chartValuesTemp">
            <xsl:for-each select="xalan:nodeset($completeSumValues)/journal">
                <xsl:sort data-type="number" order="descending" />
                <xsl:value-of select="concat(',',(text()*100) div $activitySum)" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="chartLabels" select="concat('&amp;chl=',substring($chartLabelsTemp,2))" />
        <xsl:variable name="chartParams" select="concat('chs=', '800x300', '&amp;cht=p')" />
        <xsl:variable name="chartValues" select="concat('&amp;chd=t:', substring($chartValuesTemp,2))" />
        <xsl:variable name="chartColor" select="concat('&amp;chco=', '44ca20', ',', 'ca2020', ',', 'f96820')" />
        <xsl:variable name="CompletePieChartURL" select="concat($chartBaseUrl, $chartParams, $chartValues, $chartLabels)" />
        <p style="text-align: center;">
            <xsl:choose>
                <xsl:when test="$chartLabelsTemp=''">In diesem Zeitraum ist nichts passiert.</xsl:when>
                <xsl:otherwise>
                    <img src="{$CompletePieChartURL}" />
                </xsl:otherwise>
            </xsl:choose>
        </p>
        <div style="margin: 5px; padding: 5px; border: 1px solid black; width: 420px;">
            Legende:
            <br />
            Zeitschriftname(Aktivitäten absolut - prozentualer Anteil an allen Aktivitäten)
            <br />
            <br />
            Als Aktivität zählt das Neuanlegen eines Objektes, genauso wie deren Zustandsänderung. Zustandsänderung bedeutet z.B. einem Objekt ein Digitalisat
            hinzufügen. Da sich hierbei aber 2 Werte verändern, wird diese Teilsumme halbiert.
        </div>
        <div style="padding: 5px;">
            <h4>Zeitschriften ohne Aktivitäten im ausgewählten Zeitraum:</h4>
            <ul>
                <xsl:for-each select="xalan:nodeset($ZeroLabels)/name">
                    <li>
                        <xsl:value-of select="." />
                    </li>
                </xsl:for-each>
            </ul>
        </div>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.get.ActivityIndex">
        <xsl:param name="quantifier" />

        <xsl:variable name="ListOfDates">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <dates>
                    <xsl:value-of select="@date" />
                </dates>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="XMLContainer">
            <xsl:copy-of select="/journalStatistic/*" />
        </xsl:variable>

        <xsl:variable name="differenceValues">
            <xsl:for-each select="/journalStatistic/statistic[number(@date) = number($journalStatistic.date.Till)]/journal">
                <journal>
                    <xsl:attribute name="label">
                        <xsl:value-of select="@name" />
                    </xsl:attribute>
                    <xsl:variable name="jname" select="@name" />
                    <xsl:for-each select="xalan:nodeset($ListOfDates)/dates[position() &gt; 1]">
                        <xsl:variable name="dateposition" select="position()" />
                        <xsl:variable name="jdate" select="." />
                        <xsl:variable name="jdateBefore" select="xalan:nodeset($ListOfDates)/dates[position()=$dateposition]" />
                        <xsl:variable name="total"
                            select="xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdate)]/journal[@name=$jname]/numberOfObjects/total/text()" />
                        <xsl:variable name="totalBefore"
                            select="xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdateBefore)]/journal[@name=$jname]/numberOfObjects/total/text()" />
                        <xsl:variable name="complete"
                            select="xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdate)]/journal[@name=$jname]/numberOfObjects/complete/text()" />
                        <xsl:variable name="completeBefore"
                            select="xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdateBefore)]/journal[@name=$jname]/numberOfObjects/complete/text()" />
                        <xsl:variable name="incomplete"
                            select="xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdate)]/journal[@name=$jname]/numberOfObjects/incomplete/text()" />
                        <xsl:variable name="incompleteBefore"
                            select="xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdateBefore)]/journal[@name=$jname]/numberOfObjects/incomplete/text()" />
                        <xsl:variable name="missing"
                            select="xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdate)]/journal[@name=$jname]/numberOfObjects/missing/text()" />
                        <xsl:variable name="missingBefore"
                            select="xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdateBefore)]/journal[@name=$jname]/numberOfObjects/missing/text()" />
                        <diffs>
                            <total>
                                <xsl:choose>
                                    <xsl:when test="(number($total)-number($totalBefore)) &lt; 0">
                                        <xsl:value-of select="(number($total)-number($totalBefore))*(-1)" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="(number($total)-number($totalBefore))" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </total>
                            <complete>
                                <xsl:choose>
                                    <xsl:when test="(number($complete)-number($completeBefore)) &lt; 0">
                                        <xsl:value-of select="(number($complete)-number($completeBefore))*(-1)" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="(number($complete)-number($completeBefore))" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </complete>
                            <incomplete>
                                <xsl:choose>
                                    <xsl:when test="(number($incomplete)-number($incompleteBefore)) &lt; 0">
                                        <xsl:value-of select="(number($incomplete)-number($incompleteBefore))*(-1)" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="(number($incomplete)-number($incompleteBefore))" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </incomplete>
                            <missing>
                                <xsl:choose>
                                    <xsl:when test="(number($missing)-number($missingBefore)) &lt; 0">
                                        <xsl:value-of select="(number($missing)-number($missingBefore))*(-1)" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="(number($missing)-number($missingBefore))" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </missing>
                        </diffs>
                    </xsl:for-each>
                </journal>
            </xsl:for-each>
        </xsl:variable>

        <!--
            xml max=
            <xsl:copy-of select="$differenceValues"></xsl:copy-of>
            ...
        -->
        <xsl:variable name="datecount">
            <xsl:value-of select="count(xalan:nodeset($ListOfDates)/dates)" />
        </xsl:variable>

        <xsl:variable name="categorySumValues">
            <xsl:for-each select="xalan:nodeset($differenceValues)/journal">
                <journal>
                    <xsl:attribute name="label">
                        <xsl:value-of select="@label" />
                    </xsl:attribute>
                    <xsl:for-each select="diffs">
                        <xsl:variable name="total" select="total/text()" />
                        <xsl:variable name="complete" select="complete/text()" />
                        <xsl:variable name="incomplete" select="incomplete/text()" />
                        <xsl:variable name="missing" select="missing/text()" />
                        <xsl:variable name="sum" select="($complete+$incomplete+$missing)" />
                        <value>
                            <xsl:value-of select="((($sum - $total) div 2) + $total)" />
                        </value>
                    </xsl:for-each>
                </journal>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="return">
            <xsl:for-each select="xalan:nodeset($categorySumValues)/journal">
                <journal>
                    <xsl:attribute name="label">
                        <xsl:value-of select="@label" />
                    </xsl:attribute>
                    <xsl:variable name="label" select="@label" />
                    <xsl:variable name="sum" select="sum(value)" />
                    <xsl:value-of select="$sum" />
                </journal>
            </xsl:for-each>
        </xsl:variable>

        <xsl:copy-of select="$return" />
    </xsl:template>

    <!-- ======================================================================================================================== -->

</xsl:stylesheet>