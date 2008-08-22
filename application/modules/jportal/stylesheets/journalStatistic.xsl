<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:param name="selecter" />
    <xsl:param name="journalStatistic.dateReceived.From" />
    <xsl:param name="journalStatistic.dateReceived.Till" />
    <xsl:param name="journalStatistic.view.objectListing" select="'false'" />
    <xsl:param name="journalStatistic.view.objectListing.journalID" />
    <xsl:param name="journalStatistic.showJournalwithoutActivity" select="'true'" />
    <xsl:param name="journalStatistic.view.objectListing.numberOfLabels" select="20" />
    <xsl:param name="journalStatistic.view.objectListing.numberOfValues" select="50" />
    <xsl:param name="journalStatistic.percentageOfVisibilty" select="'1'" />
    <xsl:param name="journalStatistic.dateDerivateCount" select="'1213113971040'" />

    <xsl:variable name="dateRange">
        <xsl:value-of
            select="count(/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))])-1" />
    </xsl:variable>

    <xsl:variable name="journalStatistic.date.From">
        <xsl:call-template name="get.journalStatistic.date.From" />
    </xsl:variable>
    <xsl:variable name="journalStatistic.date.Till">
        <xsl:call-template name="get.journalStatistic.date.Till" />
    </xsl:variable>

    <xsl:variable name="completeSumValues">
        <xsl:call-template name="journalStatistic.get.ActivityIndex" />
    </xsl:variable>
    <xsl:variable name="showStats">
        <xsl:call-template name="get.Selecter" />
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
    <!-- 
        maximal number of dates shown on the x-axis,
        20 is a optimal value for the picture width of 850 
    -->
    <xsl:variable name="numberOfLabels">
        <xsl:value-of select="$journalStatistic.view.objectListing.numberOfLabels" />
    </xsl:variable>
    <!-- 
        maximal number of values used in the line charts,
        if more values available a java class will decrease them
        limit the value according to the maximum allowed GET 
        parameter length given by browsers and applications
        (50 is a good value, if 1024 characters are allowed)
    -->
    <xsl:variable name="numberOfValues">
        <xsl:value-of select="$journalStatistic.view.objectListing.numberOfValues" />
    </xsl:variable>

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
                <a name="toc" />
                <xsl:call-template name="journalStatistic.selectDateSpace" />
                <xsl:call-template name="journalStatistic.selectActivitySwitch" />
                <xsl:call-template name="journalStatistic.toc" />
                <xsl:choose>
                    <xsl:when test="$showStats='gesamt' or $showStats='proz' or $showStats='gesamtAnz' or $showStats='ai' or $showStats='derivates'">
                        <p>
                            <a name="gesamt" />
                            <b>Gesamtübersicht:</b>
                            <a style="margin-left: 20px; border:1px solid black;" href="#toc">^^ zurück ^^</a>
                        </p>
                        <div style="border: 2px solid black; width: 900px;">
                            <xsl:call-template name="journalStatistic.total" />
                            <xsl:if test="$showStats='gesamt' or $showStats='ai'">
                                <xsl:call-template name="journalStatistic.PieChart.ArticleOverTime" />
                            </xsl:if>
                            <xsl:if test="$showStats='gesamt' or $showStats='derivates'">
                                <xsl:call-template name="journalStatistic.get.DerivateOverallStats" />
                            </xsl:if>
                        </div>
                    </xsl:when>
                    <xsl:when test="$showStats='detail' or contains($showStats,'jportal')">
                        <p>
                            <a name="detail" />
                            <b>Detailansicht:</b>
                            <a style="margin-left: 20px; border:1px solid black;" href="#toc">^^ zurück ^^</a>
                        </p>
                        <xsl:call-template name="journalStatistic.statistics" />
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>




            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.toc">
        <p>
            <h3>Inhaltsverzeichnis</h3>
        </p>
        <p>
            <a href="?XSL.selecter=gesamt&amp;XSL.journalStatistic.dateReceived.From={$journalStatistic.date.From}&amp;XSL.journalStatistic.dateReceived.Till={$journalStatistic.date.Till}#gesamt">
                <b>Gesamtübersicht:</b>
            </a>
            <ul>
                <li>
                    <a
                        href="?XSL.selecter=proz&amp;XSL.journalStatistic.dateReceived.From={$journalStatistic.date.From}&amp;XSL.journalStatistic.dateReceived.Till={$journalStatistic.date.Till}#proz">
                        Prozentualer Anteil einzelner Zeitschriften
                    </a>
                </li>
                <li>
                    <a
                        href="?XSL.selecter=gesamtAnz&amp;XSL.journalStatistic.dateReceived.From={$journalStatistic.date.From}&amp;XSL.journalStatistic.dateReceived.Till={$journalStatistic.date.Till}#gesamtAnz">
                        Gesamtanzahl der Artikel über die Zeit
                    </a>
                </li>
                <li>
                    <a
                        href="?XSL.selecter=ai&amp;XSL.journalStatistic.dateReceived.From={$journalStatistic.date.From}&amp;XSL.journalStatistic.dateReceived.Till={$journalStatistic.date.Till}#ai">
                        Aktivitätsindex aller Zeitschriften
                    </a>
                </li>
                <li>
                    <a
                        href="?XSL.selecter=derivates&amp;XSL.journalStatistic.dateReceived.From={$journalStatistic.date.From}&amp;XSL.journalStatistic.dateReceived.Till={$journalStatistic.date.Till}#derivates">
                        Anzahl der Digitalisate auf dem Zeitschriftenserver
                    </a>
                </li>
            </ul>
        </p>
        <p>
            <a href="?XSL.selecter=detail&amp;XSL.journalStatistic.dateReceived.From={$journalStatistic.date.From}&amp;XSL.journalStatistic.dateReceived.Till={$journalStatistic.date.Till}#detail">
                <b>Detailansicht:</b>
            </a>
            <ul>
                <xsl:for-each select="statistic[number(@date) = number($journalStatistic.date.Till)]/journal">
                    <xsl:sort select="@name" />
                    <xsl:variable name="jID">
                        <xsl:value-of select="@id" />
                    </xsl:variable>
                    <xsl:variable name="activityOfJournal">
                        <xsl:choose>
                            <xsl:when test="$journalStatistic.showJournalwithoutActivity='false'">
                                <xsl:value-of select="sum(xalan:nodeset($completeSumValues)/journal[@id=$jID]/text())" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="''" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="($journalStatistic.showJournalwithoutActivity='false') and ($activityOfJournal &lt;= 0)"></xsl:when>
                        <xsl:otherwise>
                            <li>
                                <a href="?XSL.selecter={$jID}&amp;XSL.journalStatistic.dateReceived.From={$journalStatistic.date.From}&amp;XSL.journalStatistic.dateReceived.Till={$journalStatistic.date.Till}#{$jID}">
                                    <xsl:value-of select="@name" />
                                </a>
                            </li>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </ul>

        </p>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.get.gridcount">

        <xsl:choose>
            <xsl:when test="$dateRange &lt;= $numberOfLabels">
                <xsl:value-of select="(100 div $dateRange)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="(100 div ($numberOfLabels - 1))" />
            </xsl:otherwise>
        </xsl:choose>


    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.objectListing">
        <p>
            <a name="incomplete" />
            <b style="padding:5px;">Artikel ohne Digitalisat:</b>
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
            <b style="padding:5px;">Bände ohne Artikel:</b>
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
                            <strong>Zeitspanne der Analyse:</strong>
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
                        <td style="width: 50px;">
                        <input type="hidden" name="XSL.selecter" value="{$showStats}"/>
                        </td>
                        <td>
                            <input type="submit" value=" Auswählen " />
                        </td>
                    </tr>
                </table>
            </form>
        </p>

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.selectActivitySwitch">
        <p>
            <form method="post" action="{$journalStatistic.sourceUrl}?XSL.selecter={$showStats}&amp;XSL.journalStatistic.dateReceived.From={$journalStatistic.date.From}&amp;XSL.journalStatistic.dateReceived.Till={$journalStatistic.date.Till}#{$showStats}" target="_self" id="activtySwitch">
                <table>
                    <tr>
                        <td colspan="2">
                            <strong>Zeitung ohne Aktivität im ausgewählten Zeitraum anzeigen:</strong>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input type="radio" name="XSL.journalStatistic.showJournalwithoutActivity.SESSION" value="true"
                                onChange="document.getElementById('activtySwitch').submit()">
                                <xsl:if test="$journalStatistic.showJournalwithoutActivity='true'">
                                    <xsl:attribute name="checked"><xsl:value-of select="checked" />
                                    </xsl:attribute>
                                </xsl:if>
                            </input>
                            ja
                            <br />
                            <input type="radio" name="XSL.journalStatistic.showJournalwithoutActivity.SESSION" value="false"
                                onChange="document.getElementById('activtySwitch').submit()">
                                <xsl:if test="$journalStatistic.showJournalwithoutActivity='false'">
                                    <xsl:attribute name="checked"><xsl:value-of select="checked" />
                                    </xsl:attribute>
                                </xsl:if>
                            </input>
                            nein
                        </td>
                    </tr>
                </table>
            </form>
        </p>

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template match="statistic">
        <xsl:choose>
            <xsl:when test="contains($showStats,'jportal')">
                <xsl:apply-templates select="journal[@id=$showStats]">
                    <xsl:sort select="@name" />
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="journal">
                    <xsl:sort select="@name" />
                </xsl:apply-templates>
            </xsl:otherwise>

        </xsl:choose>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.total">
        <xsl:if test="$showStats='gesamt' or $showStats='proz'">
            <xsl:for-each select="statistic[number(@date) = number($journalStatistic.date.Till)]">
                <br />
                <a name="proz" />
                <b style="padding: 5px;">
                    <u>Prozentualer Anteil einzelner Zeitschriften:</u>
                </b>
                <a style="margin-left: 20px; border:1px solid black;" href="#toc">^^ zurück ^^</a>
                <xsl:variable name="chartLabelstemp">
                    <xsl:for-each select="journal">
                        <xsl:sort select="./numberOfObjects/total/@percent" data-type="number" order="descending" />
                        <xsl:if test="numberOfObjects/total/@percent &gt;= $journalStatistic.percentageOfVisibilty">
                            <xsl:choose>
                                <xsl:when test="string-length(@name)>20">
                                    <xsl:value-of
                                        select="concat('|', substring(@name,1,20),'... (', numberOfObjects/total/text(),' - ', numberOfObjects/total/@percent,'%)')" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="concat('|', @name,' (', numberOfObjects/total/text(),' - ', numberOfObjects/total/@percent,'%)')" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:variable name="chartLabelsAll">
                    <xsl:value-of select="concat('&amp;chl=',substring($chartLabelstemp,2))" />
                </xsl:variable>
                <xsl:variable name="chartValuestemp">
                    <xsl:for-each select="journal">
                        <xsl:sort select="./numberOfObjects/total/@percent" data-type="number" order="descending" />
                        <xsl:if test="numberOfObjects/total/@percent &gt;= $journalStatistic.percentageOfVisibilty">
                            <xsl:value-of select="concat(',', numberOfObjects/total/@percent)" />
                        </xsl:if>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:variable name="chartValuesAll">
                    <xsl:value-of select="concat('&amp;chd=t:',substring($chartValuestemp,2))" />
                </xsl:variable>

                <xsl:variable name="labelsNotInChart">
                    <xsl:for-each select="journal">
                        <xsl:sort select="./numberOfObjects/total/@percent" data-type="number" order="descending" />
                        <xsl:if test="numberOfObjects/total/@percent &lt; $journalStatistic.percentageOfVisibilty">
                            <xsl:choose>
                                <xsl:when test="string-length(@name)>30">
                                    <xsl:value-of
                                        select="concat(', ', substring(@name,1,30),'... (', numberOfObjects/total/text(),' - ', numberOfObjects/total/@percent,'%)')" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="concat(', ', @name,' (', numberOfObjects/total/text(),' - ', numberOfObjects/total/@percent,'%)')" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:variable>

                <xsl:variable name="chartParamsAll" select="'chs=810x300&amp;cht=p'" />
                <xsl:variable name="CompletePieChartURLAll" select="concat($chartBaseUrl, $chartParamsAll, $chartValuesAll, $chartLabelsAll )" />
                <p style="text-align: center;">
                    <img src="{$CompletePieChartURLAll}" />
                </p>
                <div style="padding: 5px;">
                    <h4>
                        Zeitschriften mit prozentualem Anteil unter
                        <xsl:value-of select="$journalStatistic.percentageOfVisibilty" />
                        % im ausgewählten Zeitraum:
                    </h4>
                    <xsl:value-of select="$labelsNotInChart" />
                    <br />
                    <br />
                </div>
            </xsl:for-each>

        </xsl:if>
        <xsl:if test="$showStats='gesamt' or $showStats='gesamtAnz'">
            <xsl:variable name="allSums">
                <xsl:for-each
                    select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                    <node>
                        <xsl:value-of select="sum(journal[@type='fully']/numberOfObjects/total/text())" />
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
            <xsl:variable name="totalLC.valueCounter">
                <xsl:value-of select="count(xalan:nodeset($allSums)/node)" />
            </xsl:variable>

            <xsl:variable name="totalLC.values">
                <xsl:value-of select="substring($totalSum,1,string-length($totalSum)-1)" />
            </xsl:variable>
            <xsl:variable name="chartURL.xlabel">
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

            <xsl:variable name="chartURL.ylabel">
                <xsl:value-of select="concat('|',0,'|',$label1,'|',$label2,'|',$label3,'|',$label4)" />
            </xsl:variable>

            <xsl:variable name="totalLC.labels.decreased">
                <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                    select="mcrxml:decreaseLabels( string(substring($chartURL.xlabel,0,(string-length($chartURL.xlabel)-1))), string($chartURL.ylabel), string($numberOfLabels) )" />
            </xsl:variable>

            <!--  do layout -->
            <a name="gesamtAnz" />
            <b style="padding: 5px;">
                <u>
                    <xsl:value-of select="concat('Gesamtanzahl der Artikel über die Zeit (Differenz = ',$maxMinDistance,'):')" />
                </u>
            </b>
            <a style="margin-left: 20px; border:1px solid black;" href="#toc">^^ zurück ^^</a>

            <xsl:variable name="totalLC.values.decreased">
                <xsl:choose>
                    <xsl:when test="$totalLC.valueCounter>$numberOfValues">
                        <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                            select="mcrxml:decreaseValues( string( $totalLC.values ) , string($numberOfValues) )" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$totalLC.values" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <p style="text-align: center;">
                <img
                    src="{concat($chartBaseUrl,'cht=lc&amp;chd=t:',$totalLC.values.decreased,'&amp;chs=',$chartSize.objectDev,$totalLC.labels.decreased,'&amp;chco=0000ff','&amp;chg=',$xgrid,',25')}" />
            </p>

        </xsl:if>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template match="journal">

        <xsl:variable name="journal-id">
            <xsl:value-of select="@id" />
        </xsl:variable>
        <xsl:variable name="journal-name">
            <xsl:value-of select="@label" />
        </xsl:variable>

        <xsl:variable name="activityOfJournal">
            <xsl:value-of select="sum(xalan:nodeset($completeSumValues)/journal[@id=$journal-id]/text())" />
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="($journalStatistic.showJournalwithoutActivity='false') and ($activityOfJournal &lt;= 0)"></xsl:when>
            <xsl:otherwise>
                <xsl:variable name="derNumber">
                    <xsl:value-of select="sum(derivates/derivate/@number)" />
                </xsl:variable>
                <xsl:variable name="derSizeTemp">
                    <xsl:value-of select="sum(derivates/derivate/@size)" />
                </xsl:variable>

                <xsl:variable name="derSize">
                    <xsl:choose>
                        <xsl:when test="string-length($derSizeTemp) &lt; 4">
                            <xsl:value-of select="concat($derSizeTemp , ' Byte')" />
                        </xsl:when>
                        <xsl:when test="string-length($derSizeTemp) &lt; 7">
                            <xsl:variable name="sizeTemp">
                                <xsl:value-of select="$derSizeTemp div 1000" />
                            </xsl:variable>
                            <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), ' KiloByte')" />
                        </xsl:when>
                        <xsl:when test="string-length($derSizeTemp) &lt; 10">
                            <xsl:variable name="sizeTemp">
                                <xsl:value-of select="$derSizeTemp div 1000000" />
                            </xsl:variable>
                            <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), ' MegaByte')" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="sizeTemp">
                                <xsl:value-of select="$derSizeTemp div 1000000000" />
                            </xsl:variable>
                            <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), ' GigaByte')" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <!-- headline -->
                <xsl:variable name="journal-type">
                    <xsl:choose>
                        <xsl:when test="@type='fully'">
                            <xsl:value-of
                                select="concat(' (vollständige Erschliessung, mit insgesamt ', numberOfObjects/total/text(), ' Artikeln, ',$derNumber,' Dateien mit insges. ',$derSize,')')" />
                        </xsl:when>
                        <xsl:when test="@type='browse'">
                            <xsl:value-of
                                select="concat(' (Blätterzeitschrift, mit insgesamt ', numberOfObjects/total/text(), ' Bänden, ',$derNumber,' Dateien mit insges. ',$derSize,')')" />
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

                    <b style="padding: 5px;">
                        <xsl:value-of select="concat('Aktivitätsindex der Zeitung im ausgewählten Zeitraum: ',$activityOfJournal)" />
                    </b>
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
                    <br />
                    <br />
                    <!-- Derivate development -->
                    <xsl:call-template name="journalStatistic.derivateDev">
                        <xsl:with-param name="jID" select="@id"></xsl:with-param>
                    </xsl:call-template>
                </div>
                <br />
                <br />
            </xsl:otherwise>
        </xsl:choose>


    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.objectDev">
        <xsl:param name="jID" />

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

        <xsl:variable name="firstTotaltemp">
            <xsl:value-of select="/journalStatistic/statistic[number(@date) = number($journalStatistic.date.From)]/journal[@id=$jID]/numberOfObjects/total" />
        </xsl:variable>
        <xsl:variable name="firstTotal">
            <xsl:choose>
                <xsl:when test="number($firstTotaltemp)">
                    <xsl:value-of select="$firstTotaltemp" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="0" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="lastTotal">
            <xsl:value-of select="/journalStatistic/statistic[number(@date) = number($journalStatistic.date.Till)]/journal[@id=$jID]/numberOfObjects/total" />
        </xsl:variable>
        <xsl:variable name="lastFirstDistancetemp">
            <xsl:value-of select="number($lastTotal - $firstTotal)" />
        </xsl:variable>

        <xsl:variable name="lastFirstDistance">
            <xsl:choose>
                <xsl:when test="$lastFirstDistancetemp &gt; 0">
                    <xsl:value-of select="concat('+',$lastFirstDistancetemp)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$lastFirstDistancetemp" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="headline">
            <xsl:choose>
                <xsl:when test="@type='fully'">
                    <xsl:value-of select="concat('Zahl der Artikel im zeitlichen Verlauf (Differenz = ',$lastFirstDistance ,') :')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat('Zahl der Bände im zeitlichen Verlauf (Differenz = ',$lastFirstDistance ,') :')" />
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
                <xsl:variable name="tempValues">
                    <xsl:choose>
                        <xsl:when
                            test="(number(journal[@id=$jID]/numberOfObjects/total/text())) and (number(journal[@id=$jID]/numberOfObjects/total/text())=$maxTotal)">
                            <xsl:value-of select="100" />
                        </xsl:when>
                        <xsl:when test="(number(journal[@id=$jID]/numberOfObjects/total/text())) and (journal[@id=$jID]/numberOfObjects/total/text() = '0')">
                            <xsl:value-of select="0" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when
                                    test="number(journal[@id=$jID]/numberOfObjects/total/text()) and (number(journal[@id=$jID]/numberOfObjects/total/text()) &gt; 0)">
                                    <xsl:value-of select="(((number(journal[@id=$jID]/numberOfObjects/total/text())-$minTotal) div $maxMinDistance*75)+25)" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>

                </xsl:variable>
                <xsl:value-of select="concat(',', $tempValues)" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="lineChartValues" select="substring($lineVal,2)" />

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

        <xsl:variable name="ObjectDevChart.labels.decreased">
            <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                select="mcrxml:decreaseLabels( string(substring($chartURL.label.tmp,0,(string-length($chartURL.label.tmp)-1))), string(concat('|',0,'|',$label1,'|',$label2,'|',$label3,'|',$label4)), string($numberOfLabels) )" />
        </xsl:variable>
        <xsl:variable name="ObjectDevChart.values.decreased">
            <xsl:choose>
                <xsl:when test="$dateRange>$numberOfValues">
                    <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                        select="mcrxml:decreaseValues( string( $lineChartValues ) , string($numberOfValues) )" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$lineChartValues" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!--  do layout -->
        <b style="padding:5px;">
            <xsl:value-of select="$headline" />
        </b>
        <p style="text-align: center;">
            <img
                src="{concat($chartBaseUrl,'cht=lc&amp;chd=t:',$ObjectDevChart.values.decreased,'&amp;chs=',$chartSize.objectDev,$ObjectDevChart.labels.decreased,'&amp;chco=0000ff','&amp;chg=',$xgrid,',25')}" />
        </p>

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.consistancyDev">
        <xsl:param name="jID" />

        <xsl:variable name="CompleteDiffTemp">
            <xsl:call-template name="chartURL.get.ComplDiff">
                <xsl:with-param name="jID" select="$jID" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="IncompleteDiffTemp">
            <xsl:call-template name="chartURL.get.IncomplDiff">
                <xsl:with-param name="jID" select="$jID" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="MissingDiffTemp">
            <xsl:call-template name="chartURL.get.MissingDiff">
                <xsl:with-param name="jID" select="$jID" />
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="CompleteDiff">
            <xsl:choose>
                <xsl:when test="$CompleteDiffTemp &gt; 0">
                    <xsl:value-of select="concat('+',$CompleteDiffTemp)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$CompleteDiffTemp" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="IncompleteDiff">
            <xsl:choose>
                <xsl:when test="$IncompleteDiffTemp &gt; 0">
                    <xsl:value-of select="concat('+',$IncompleteDiffTemp)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$IncompleteDiffTemp" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="MissingDiff">
            <xsl:choose>
                <xsl:when test="$MissingDiffTemp &gt; 0">
                    <xsl:value-of select="concat('+',$MissingDiffTemp)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$MissingDiffTemp" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="headline">
            <xsl:choose>
                <xsl:when test="/journalStatistic/statistic[(number(@date) = number($journalStatistic.date.Till))]/journal[@id=$jID]/@type='fully'">
                    <xsl:value-of
                        select="concat('Vollständigkeitsprüfung im zeitlichen Verlauf (Vollständige = ',$CompleteDiff,', kein Digitalisat = ',$IncompleteDiff,', Band ohne Artikel = ',$MissingDiff,') :')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of
                        select="concat('Vollständigkeitsprüfung im zeitlichen Verlauf (Vollständige = ',$CompleteDiff,', kein Digitalisat = ',$IncompleteDiff,') :')" />
                </xsl:otherwise>
            </xsl:choose>
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

        <xsl:variable name="ConstChart.labels.decreased">
            <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                select="mcrxml:decreaseLabels( string(substring($chartURL.label,0,(string-length($chartURL.label)-1))), string('|0%|25%|50%|75%|100%'), string($numberOfLabels) )" />
        </xsl:variable>


        <!--  do layout -->
        <b style="padding:5px;">
            <xsl:copy-of select="$headline" />
        </b>
        <p style="text-align: center;">
            <img
                src="{concat($chartBaseUrl,'cht=lc&amp;chd=t:',$chartURL.values,'&amp;chs=',$chartSize.consistancyDev,$ConstChart.labels.decreased,'&amp;chco=',$chartURL.colors,'&amp;chm=',$fillColors,'&amp;chg=',$xgrid,',25')}" />
        </p>

    </xsl:template>
    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.derivateDev">
        <xsl:param name="jID" />

        <xsl:variable name="actualSizes">
            <values>
                <xsl:for-each
                    select="/journalStatistic/statistic[(number(@date) &lt;= number($journalStatistic.date.Till))]/journal[@id=$jID]/derivates/derivate">
                    <size>
                        <xsl:attribute name="type">
                            <xsl:value-of select="@type" />
                        </xsl:attribute>
                        <xsl:value-of select="@size" />
                    </size>
                </xsl:for-each>
            </values>
        </xsl:variable>



        <xsl:variable name="headline">
            <xsl:value-of select="'Übersicht über die Derivate der Zeitschrift:'" />
        </xsl:variable>

        <xsl:if test="count(/journalStatistic/statistic[(number(@date) = number($journalStatistic.date.Till))]/journal[@id=$jID]/derivates/derivate) &gt; 0">

            <b style="padding:5px;">
                <xsl:value-of select="$headline" />
            </b>

            <xsl:variable name="actualSizes">
                <values>
                    <xsl:for-each
                        select="/journalStatistic/statistic[(number(@date) = number($journalStatistic.date.Till))]/journal[@id=$jID]/derivates/derivate">
                        <size>
                            <xsl:attribute name="type">
                            <xsl:value-of select="@type" />
                        </xsl:attribute>
                            <xsl:value-of select="@size" />
                        </size>
                    </xsl:for-each>
                </values>
            </xsl:variable>

            <xsl:for-each select="/journalStatistic/statistic[number(@date) = number($journalStatistic.date.Till)]/journal[@id=$jID]/derivates/derivate">
                <xsl:call-template name="journalStatistic.singleDerivateDev">
                    <xsl:with-param name="jID" select="$jID" />
                    <xsl:with-param name="type" select="@type" />
                    <xsl:with-param name="actualSizes" select="$actualSizes" />
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.singleDerivateDev">
        <xsl:param name="jID" />
        <xsl:param name="type" />
        <xsl:param name="actualSizes" />

        <xsl:variable name="maxTotal">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]/journal[@id=$jID]/derivates/derivate[@type=$type]/@number">
                <xsl:sort data-type="number" order="descending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="minTotal">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]/journal[@id=$jID]/derivates/derivate[@type=$type]/@number">
                <xsl:sort data-type="number" order="ascending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="maxMinDistance">
            <xsl:value-of select="number($maxTotal - $minTotal)" />
        </xsl:variable>

        <xsl:variable name="firstTotaltemp">
            <xsl:value-of
                select="/journalStatistic/statistic[number(@date) = number($journalStatistic.date.From)]/journal[@id=$jID]/derivates/derivate[@type=$type]/@number" />
        </xsl:variable>
        <xsl:variable name="firstTotal">
            <xsl:choose>
                <xsl:when test="number($firstTotaltemp)">
                    <xsl:value-of select="$firstTotaltemp" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="0" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="lastTotal">
            <xsl:value-of
                select="/journalStatistic/statistic[number(@date) = number($journalStatistic.date.Till)]/journal[@id=$jID]/derivates/derivate[@type=$type]/@number" />
        </xsl:variable>
        <xsl:variable name="lastFirstDistancetemp">
            <xsl:value-of select="number($lastTotal - $firstTotal)" />
        </xsl:variable>

        <xsl:variable name="lastFirstDistance">
            <xsl:choose>
                <xsl:when test="$lastFirstDistancetemp &gt; 0">
                    <xsl:value-of select="concat('+',$lastFirstDistancetemp)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$lastFirstDistancetemp" />
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
                <xsl:variable name="tempValues">
                    <xsl:choose>
                        <xsl:when
                            test="(number(journal[@id=$jID]/derivates/derivate[@type=$type]/@number)) and (journal[@id=$jID]/derivates/derivate[@type=$type]/@number=$maxTotal)">
                            <xsl:value-of select="100" />
                        </xsl:when>
                        <xsl:when
                            test="(number(journal[@id=$jID]/derivates/derivate[@type=$type]/@number)) and (number(journal[@id=$jID]/derivates/derivate[@type=$type]/@number) = 0)">
                            <xsl:value-of select="0" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when
                                    test="(number(journal[@id=$jID]/derivates/derivate[@type=$type]/@number)) and (number(journal[@id=$jID]/derivates/derivate[@type=$type]/@number) &gt; 0)">
                                    <xsl:value-of
                                        select="(((number(journal[@id=$jID]/derivates/derivate[@type=$type]/@number)-$minTotal) div $maxMinDistance*75)+25)" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>

                </xsl:variable>
                <xsl:value-of select="concat(',', $tempValues)" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="derSizeTemp">
            <xsl:value-of select="xalan:nodeset($actualSizes)/values/size[@type=$type]/text()" />
        </xsl:variable>

        <xsl:variable name="derSize">
            <xsl:choose>
                <xsl:when test="string-length($derSizeTemp) &lt; 4">
                    <xsl:value-of select="concat($derSizeTemp , 'Byte')" />
                </xsl:when>
                <xsl:when test="string-length($derSizeTemp) &lt; 7">
                    <xsl:variable name="sizeTemp">
                        <xsl:value-of select="$derSizeTemp div 1000" />
                    </xsl:variable>
                    <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), 'KiloByte')" />
                </xsl:when>
                <xsl:when test="string-length($derSizeTemp) &lt; 10">
                    <xsl:variable name="sizeTemp">
                        <xsl:value-of select="$derSizeTemp div 1000000" />
                    </xsl:variable>
                    <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), 'MegaByte')" />
                </xsl:when>
                <xsl:when test="string-length($derSizeTemp) &lt; 13">
                    <xsl:variable name="sizeTemp">
                        <xsl:value-of select="$derSizeTemp div 1000000000" />
                    </xsl:variable>
                    <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), 'GigaByte')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="sizeTemp">
                        <xsl:value-of select="$derSizeTemp div 1000000000000" />
                    </xsl:variable>
                    <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), 'TeraByte')" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="headline">
            <xsl:value-of select="concat('Typ: ', $type, '; Größe: ',$derSize,'; Differenz=',$lastFirstDistance,';')" />
        </xsl:variable>

        <xsl:variable name="lineChartValues" select="substring($lineVal,2)" />

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

        <xsl:variable name="ObjectDevChart.labels.decreased">
            <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                select="mcrxml:decreaseLabels( string(substring($chartURL.label.tmp,0,(string-length($chartURL.label.tmp)-1))), string(concat('|',0,'|',$label1,'|',$label2,'|',$label3,'|',$label4)), string($numberOfLabels) )" />
        </xsl:variable>
        <xsl:variable name="ObjectDevChart.values.decreased">
            <xsl:choose>
                <xsl:when test="$dateRange>$numberOfValues">
                    <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                        select="mcrxml:decreaseValues( string( $lineChartValues ) , string($numberOfValues) )" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$lineChartValues" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!--  do layout -->
        <br />
        <div style="padding-left:10px;">
            <xsl:value-of select="$headline" />
        </div>
        <p style="text-align: center;">
            <img
                src="{concat($chartBaseUrl,'cht=lc&amp;chd=t:',$ObjectDevChart.values.decreased,'&amp;chs=',$chartSize.objectDev,$ObjectDevChart.labels.decreased,'&amp;chco=aa00ff','&amp;chg=',$xgrid,',25')}" />
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
            <xsl:choose>
                <xsl:when test="number(xalan:nodeset($allComplete)/complete[position()=1])">
                    <xsl:value-of select="number(xalan:nodeset($allComplete)/complete[position()=1])" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="0" />
                </xsl:otherwise>
            </xsl:choose>
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
            <xsl:choose>
                <xsl:when test="number(xalan:nodeset($allIncomplete)/incomplete[position()=1])">
                    <xsl:value-of select="number(xalan:nodeset($allIncomplete)/incomplete[position()=1])" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="0" />
                </xsl:otherwise>
            </xsl:choose>
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
            <xsl:choose>
                <xsl:when test="number(xalan:nodeset($allMissing)/missing[position()=1])">
                    <xsl:value-of select="number(xalan:nodeset($allMissing)/missing[position()=1])" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="0" />
                </xsl:otherwise>
            </xsl:choose>
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
        <xsl:value-of select="substring($chartURL.label.tmp,1,string-length($chartURL.label.tmp))" />
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
            <xsl:choose>
                <xsl:when test="$dateRange>$numberOfValues">
                    <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                        select="mcrxml:decreaseValues( string( substring($values.zero.tmp,1,string-length($values.zero.tmp)-1)) , string($numberOfValues) )" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring($values.zero.tmp,1,string-length($values.zero.tmp)-1)" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!-- missings -->
        <xsl:variable name="values.missing.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:variable name="missing.value">
                    <xsl:choose>
                        <xsl:when test="number(./journal[@id=$journalID]/numberOfObjects/missing/@percent)">
                            <xsl:value-of select="number(./journal[@id=$journalID]/numberOfObjects/missing/@percent)" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="0" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="contains($missing.value,'.')">
                        <xsl:value-of select="concat(substring-before($missing.value,'.'),',')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($missing.value,',')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.missing">
            <xsl:choose>
                <xsl:when test="$dateRange>$numberOfValues">
                    <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                        select="mcrxml:decreaseValues( string( substring($values.missing.tmp,1,string-length($values.missing.tmp)-1) ) , string($numberOfValues) )" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring($values.missing.tmp,1,string-length($values.missing.tmp)-1)" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!-- incompletes -->
        <xsl:variable name="values.incomplete.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:variable name="inc.value">
                    <xsl:choose>
                        <xsl:when test="number(./journal[@id=$journalID]/numberOfObjects/incomplete/@percent)">
                            <xsl:value-of select="number(./journal[@id=$journalID]/numberOfObjects/incomplete/@percent)" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="0" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="contains($inc.value,'.')">
                        <xsl:value-of select="concat(substring-before($inc.value,'.'),',')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($inc.value,',')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.incomplete">
            <xsl:choose>
                <xsl:when test="$dateRange>$numberOfValues">
                    <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                        select="mcrxml:decreaseValues( string( substring($values.incomplete.tmp,1,string-length($values.incomplete.tmp)-1) ) , string($numberOfValues) )" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring($values.incomplete.tmp,1,string-length($values.incomplete.tmp)-1)" />
                </xsl:otherwise>
            </xsl:choose>
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
                <xsl:variable name="cor.value">
                    <xsl:choose>
                        <xsl:when test="number($totalValue)">
                            <xsl:value-of select="number($totalValue)" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="0" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="contains($cor.value,'.')">
                        <xsl:value-of select="concat(substring-before($cor.value,'.'),',')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($cor.value,',')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="values.complete.ZeroCheck">
            <corrupt>
                <xsl:for-each
                    select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                    <xsl:sort select="@date" order="ascending" />
                    <xsl:variable name="totalValue">
                        <xsl:value-of select="number(./journal[@id=$journalID]/numberOfObjects/complete/@percent)" />
                    </xsl:variable>
                    <value>
                        <xsl:attribute name="date">
                        <xsl:value-of select="@date" />
                    </xsl:attribute>
                        <xsl:choose>
                            <xsl:when test="number($totalValue)">
                                <xsl:value-of select="number($totalValue)" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="0" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </value>
                </xsl:for-each>
            </corrupt>
        </xsl:variable>
        <xsl:variable name="values.corruptedTotal.ZeroCheck">
            <corrupt>
                <xsl:for-each
                    select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                    <xsl:sort select="@date" order="ascending" />
                    <xsl:variable name="totalValue">
                        <xsl:value-of
                            select="number(./journal[@id=$journalID]/numberOfObjects/incomplete/@percent) + number(./journal[@id=$journalID]/numberOfObjects/missing/@percent)" />
                    </xsl:variable>
                    <value>
                        <xsl:attribute name="date">
                        <xsl:value-of select="@date" />
                    </xsl:attribute>
                        <xsl:choose>
                            <xsl:when test="number($totalValue)">
                                <xsl:value-of select="number($totalValue)" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="0" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </value>
                </xsl:for-each>
            </corrupt>
        </xsl:variable>

        <xsl:variable name="values.corruptedTotal">
            <xsl:choose>
                <xsl:when test="$dateRange>$numberOfValues">
                    <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                        select="mcrxml:decreaseValues( string( substring($values.corruptedTotal.tmp,1,string-length($values.corruptedTotal.tmp)-1) ) , string($numberOfValues) )" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring($values.corruptedTotal.tmp,1,string-length($values.corruptedTotal.tmp)-1)" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!-- fully graph -->
        <xsl:variable name="values.100.tmp">
            <xsl:for-each
                select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:variable name="actualDate">
                    <xsl:value-of select="@date" />
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="number(./journal[@id=$journalID]/numberOfObjects/total/text())">
                        <xsl:choose>
                            <xsl:when
                                test="(./journal[@id=$journalID]/numberOfObjects/incomplete/@percent=0) and (./journal[@id=$journalID]/numberOfObjects/complete/@percent=0) and (./journal[@id=$journalID]/numberOfObjects/missing/@percent=0)">
                                <xsl:value-of select="concat(0,',')" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="concat(100,',')" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat(0,',')" />
                    </xsl:otherwise>
                </xsl:choose>

            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.100">
            <xsl:choose>
                <xsl:when test="$dateRange>$numberOfValues">
                    <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                        select="mcrxml:decreaseValues( string( substring($values.100.tmp,1,string-length($values.100.tmp)-1) ) , string($numberOfValues) )" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring($values.100.tmp,1,string-length($values.100.tmp)-1)" />
                </xsl:otherwise>
            </xsl:choose>
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
                    <a style="margin-left: 20px; border:1px solid black;" href="#toc">^^ zurück ^^</a>
                </td>
            </tr>
        </table>
        <br />
        <b style="padding:5px;">
            <xsl:value-of
                select="concat('  Vollständigkeitsprüfung für ',/journalStatistic/statistic[@date=number($journalStatistic.date.Till)]/@datePretty,' :')" />
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
        <a name="ai" />
        <b style="padding: 5px;">
            <u>Aktivitätsindex aller Zeitschriften:</u>
        </b>
        <a style="margin-left: 20px; border:1px solid black;" href="#toc">^^ zurück ^^</a>

        <xsl:variable name="activitySum">
            <xsl:value-of select="sum(xalan:nodeset($completeSumValues)/journal/text())" />
        </xsl:variable>
        <xsl:variable name="chartLabelsTemp">
            <xsl:for-each select="xalan:nodeset($completeSumValues)/journal">
                <xsl:sort data-type="number" order="descending" />
                <xsl:if test="((text()*100) div $activitySum) &gt;= $journalStatistic.percentageOfVisibilty">
                    <xsl:if test="number(text()) &gt; 0">
                        <xsl:choose>
                            <xsl:when test="string-length(@label) &gt; 20">
                                <xsl:value-of
                                    select="concat('|',substring(@label,1,20),'...','(',round(text()),' - ',substring(string(((text()*100) div $activitySum)),1,4),'%)')" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="concat('|',@label,'(',round(text()),' - ',substring(string(((text()*100) div $activitySum)),1,5),'%)')" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="chartLabelsNotVisible">
            <xsl:for-each select="xalan:nodeset($completeSumValues)/journal">
                <xsl:sort data-type="number" order="descending" />
                <xsl:if test="((text()*100) div $activitySum) &lt; $journalStatistic.percentageOfVisibilty">
                    <xsl:if test="number(text()) &gt; 0">
                        <xsl:choose>
                            <xsl:when test="string-length(@label) &gt; 30">
                                <xsl:value-of
                                    select="concat(', ',substring(@label,1,30),'...','(',round(text()),' - ',substring(string(((text()*100) div $activitySum)),1,4),'%)')" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="concat(', ',@label,'(',round(text()),' - ',substring(string(((text()*100) div $activitySum)),1,5),'%)')" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
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
                <xsl:if test="((text()*100) div $activitySum) &gt;= $journalStatistic.percentageOfVisibilty">
                    <xsl:value-of select="concat(',',(text()*100) div $activitySum)" />
                </xsl:if>
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
        <div style="padding-left: 5px; padding-right: 5px;">
            <h4>
                Zeitschriften mit Aktivitäten unter
                <xsl:value-of select="$journalStatistic.percentageOfVisibilty" />
                % im ausgewählten Zeitraum:
            </h4>
            <xsl:copy-of select="substring($chartLabelsNotVisible,2)" />
        </div>
        <div style="padding-left: 5px; padding-right: 5px;">
            <h4>Zeitschriften ohne Aktivitäten im ausgewählten Zeitraum:</h4>
            <xsl:for-each select="xalan:nodeset($ZeroLabels)/name">
                <xsl:choose>
                    <xsl:when test="position()=last()">
                        <xsl:value-of select="." />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat(.,', ')" />
                    </xsl:otherwise>
                </xsl:choose>

            </xsl:for-each>
        </div>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.get.ActivityIndex">

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
                    <xsl:attribute name="id">
                        <xsl:value-of select="@id" />
                    </xsl:attribute>
                    <xsl:variable name="jname" select="@id" />
                    <xsl:for-each select="xalan:nodeset($ListOfDates)/dates[position() &gt; 1]">
                        <xsl:variable name="dateposition" select="position()" />
                        <xsl:variable name="jdate" select="." />
                        <xsl:variable name="jdateBefore" select="xalan:nodeset($ListOfDates)/dates[position()=$dateposition]" />
                        <xsl:variable name="derivate">
                            <xsl:choose>
                                <xsl:when test="$jdate &lt; $journalStatistic.dateDerivateCount">
                                    <xsl:value-of
                                        select="number(sum(xalan:nodeset($XMLContainer)/statistic[number(@date)=$journalStatistic.dateDerivateCount]/journal[@id=$jname]/derivates/derivate/@number))" />
                                </xsl:when>
                                <xsl:when
                                    test="number(sum(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdate)]/journal[@id=$jname]/derivates/derivate/@number))">
                                    <xsl:value-of
                                        select="number(sum(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdate)]/journal[@id=$jname]/derivates/derivate/@number))" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="derivateBefore">
                            <xsl:choose>
                                <xsl:when test="$jdateBefore &lt; $journalStatistic.dateDerivateCount">
                                    <xsl:value-of
                                        select="number(sum(xalan:nodeset($XMLContainer)/statistic[number(@date)=$journalStatistic.dateDerivateCount]/journal[@id=$jname]/derivates/derivate/@number))" />
                                </xsl:when>
                                <xsl:when
                                    test="number(sum(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdateBefore)]/journal[@id=$jname]/derivates/derivate/@number))">
                                    <xsl:value-of
                                        select="number(sum(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdateBefore)]/journal[@id=$jname]/derivates/derivate/@number))" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="incomplete">
                            <xsl:choose>
                                <xsl:when
                                    test="number(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdate)]/journal[@id=$jname]/numberOfObjects/incomplete/text())">
                                    <xsl:value-of
                                        select="number(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdate)]/journal[@id=$jname]/numberOfObjects/incomplete/text())" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="incompleteBefore">
                            <xsl:choose>
                                <xsl:when
                                    test="number(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdateBefore)]/journal[@id=$jname]/numberOfObjects/incomplete/text())">
                                    <xsl:value-of
                                        select="number(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdateBefore)]/journal[@id=$jname]/numberOfObjects/incomplete/text())" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="missing">
                            <xsl:choose>
                                <xsl:when
                                    test="number(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdate)]/journal[@id=$jname]/numberOfObjects/missing/text())">
                                    <xsl:value-of
                                        select="number(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdate)]/journal[@id=$jname]/numberOfObjects/missing/text())" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="missingBefore">
                            <xsl:choose>
                                <xsl:when
                                    test="number(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdateBefore)]/journal[@id=$jname]/numberOfObjects/missing/text())">
                                    <xsl:value-of
                                        select="number(xalan:nodeset($XMLContainer)/statistic[number(@date)=number($jdateBefore)]/journal[@id=$jname]/numberOfObjects/missing/text())" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <diffs>
                            <derivate>
                                <xsl:choose>
                                    <xsl:when test="number(number($derivate)-number($derivateBefore))">
                                        <xsl:choose>
                                            <xsl:when test="(number($derivate)-number($derivateBefore)) &lt; 0">
                                                <xsl:value-of select="(number($derivate)-number($derivateBefore))*(-1)" />
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="(number($derivate)-number($derivateBefore))" />
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="0" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </derivate>
                            <incomplete>
                                <xsl:choose>
                                    <xsl:when test="number(number($incomplete)-number($incompleteBefore))">
                                        <xsl:choose>
                                            <xsl:when test="(number($incomplete)-number($incompleteBefore)) &lt; 0">
                                                <xsl:value-of select="'0'" />
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="(number($incomplete)-number($incompleteBefore))" />
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="0" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </incomplete>
                            <missing>
                                <xsl:choose>
                                    <xsl:when test="number(number($missing)-number($missingBefore))">
                                        <xsl:choose>
                                            <xsl:when test="(number($missing)-number($missingBefore)) &lt; 0">
                                                <xsl:value-of select="(number($missing)-number($missingBefore))*(-1)" />
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="(number($missing)-number($missingBefore))" />
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="0" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </missing>
                        </diffs>
                    </xsl:for-each>
                </journal>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="datecount">
            <xsl:value-of select="count(xalan:nodeset($ListOfDates)/dates)" />
        </xsl:variable>

        <xsl:variable name="categorySumValues">
            <xsl:for-each select="xalan:nodeset($differenceValues)/journal">
                <journal>
                    <xsl:attribute name="label">
                        <xsl:value-of select="@label" />
                    </xsl:attribute>
                    <xsl:attribute name="id">
                        <xsl:value-of select="@id" />
                    </xsl:attribute>
                    <xsl:for-each select="diffs">
                        <xsl:variable name="derivate" select="derivate/text()" />
                        <xsl:variable name="incomplete" select="incomplete/text()" />
                        <xsl:variable name="missing" select="missing/text()" />
                        <xsl:variable name="sum" select="($derivate+$incomplete+$missing)" />
                        <value>
                            <xsl:value-of select="$sum" />
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
                    <xsl:attribute name="id">
                        <xsl:value-of select="@id" />
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

    <xsl:template name="journalStatistic.get.DerivateOverallStats">
        <!-- get derivate values from the xml container -->

        <xsl:variable name="derivates">
            <derivates>
                <xsl:for-each
                    select="/journalStatistic/statistic[(number(@date) &gt;= number($journalStatistic.date.From)) and (number(@date) &lt;= number($journalStatistic.date.Till))]">
                    <date>
                        <xsl:attribute name="timestamp">
                            <xsl:value-of select="@date" />
                        </xsl:attribute>
                        <xsl:for-each select="journal">
                            <journal>
                                <number>
                                    <xsl:value-of select="sum(derivates/derivate/@number)" />
                                </number>
                                <size>
                                    <xsl:value-of select="sum(derivates/derivate/@size)" />
                                </size>
                                <test>
                                    <xsl:value-of select="number(derivates/derivate[position() = 1]/@size)" />
                                </test>
                            </journal>
                        </xsl:for-each>
                    </date>
                </xsl:for-each>
            </derivates>
        </xsl:variable>

        <!-- overall sums -->

        <xsl:variable name="derSizeTemp">
            <xsl:value-of select="sum(xalan:nodeset($derivates)/derivates/date[position()=last()]/journal/size/text())" />
        </xsl:variable>

        <xsl:variable name="overallDerSize">
            <xsl:choose>
                <xsl:when test="string-length($derSizeTemp) &lt; 4">
                    <xsl:value-of select="concat($derSizeTemp , ' Byte')" />
                </xsl:when>
                <xsl:when test="string-length($derSizeTemp) &lt; 7">
                    <xsl:variable name="sizeTemp">
                        <xsl:value-of select="$derSizeTemp div 1000" />
                    </xsl:variable>
                    <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), ' KiloByte')" />
                </xsl:when>
                <xsl:when test="string-length($derSizeTemp) &lt; 10">
                    <xsl:variable name="sizeTemp">
                        <xsl:value-of select="$derSizeTemp div 1000000" />
                    </xsl:variable>
                    <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), ' MegaByte')" />
                </xsl:when>
                <xsl:when test="string-length($derSizeTemp) &lt; 13">
                    <xsl:variable name="sizeTemp">
                        <xsl:value-of select="$derSizeTemp div 1000000000" />
                    </xsl:variable>
                    <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), ' GigaByte')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="sizeTemp">
                        <xsl:value-of select="$derSizeTemp div 1000000000000" />
                    </xsl:variable>
                    <xsl:value-of select="concat(substring-before($sizeTemp,'.'),'.',substring(substring-after($sizeTemp,'.'),1,2), ' TeraByte')" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>


        <xsl:variable name="overallDerNumber">
            <xsl:value-of select="sum(xalan:nodeset($derivates)/derivates/date[position()=last()]/journal/number/text())" />
        </xsl:variable>


        <!-- values for graph -->

        <xsl:variable name="derNumberPerDate">
            <xsl:for-each select="xalan:nodeset($derivates)/derivates/date">
                <date>
                    <xsl:attribute name="number">
                        <xsl:choose>
                            <xsl:when test="number(journal/test/text())">
                                <xsl:value-of select="'true'" /> 
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="'false'" />
                            </xsl:otherwise>
                        </xsl:choose>            
                    </xsl:attribute>
                    <xsl:value-of select="sum(journal/number/text())" />
                </date>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="maxTotal">
            <xsl:for-each select="xalan:nodeset($derNumberPerDate)/date/text()">
                <xsl:sort data-type="number" order="descending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="minTotal">
            <xsl:for-each select="xalan:nodeset($derNumberPerDate)/date[@number='true']/text()">
                <xsl:sort data-type="number" order="ascending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="maxMinDistance">
            <xsl:value-of select="number($maxTotal - $minTotal)" />
        </xsl:variable>

        <xsl:variable name="firstTotaltemp">
            <xsl:value-of select="xalan:nodeset($derNumberPerDate)/date[position()=1]/text()" />
        </xsl:variable>
        <xsl:variable name="firstTotal">
            <xsl:choose>
                <xsl:when test="number($firstTotaltemp)">
                    <xsl:value-of select="$firstTotaltemp" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="0" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="lastTotal">
            <xsl:value-of select="xalan:nodeset($derNumberPerDate)/date[position()=last()]/text()" />
        </xsl:variable>

        <xsl:variable name="lastFirstDistancetemp">
            <xsl:value-of select="number($lastTotal - $firstTotal)" />
        </xsl:variable>

        <xsl:variable name="lastFirstDistance">
            <xsl:choose>
                <xsl:when test="$lastFirstDistancetemp &gt; 0">
                    <xsl:value-of select="concat('+',$lastFirstDistancetemp)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$lastFirstDistancetemp" />
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
                <xsl:variable name="position">
                    <xsl:value-of select="position()" />
                </xsl:variable>
                <xsl:variable name="tempValues">
                    <xsl:choose>
                        <xsl:when
                            test="(number(xalan:nodeset($derNumberPerDate)/date[position()=$position]/text())) and (xalan:nodeset($derNumberPerDate)/date[position()=$position]/text()=$maxTotal)">
                            <xsl:value-of select="100" />
                        </xsl:when>
                        <xsl:when
                            test="(number(xalan:nodeset($derNumberPerDate)/date[position()=$position]/text())) and (xalan:nodeset($derNumberPerDate)/date[position()=$position]/text() = '0')">
                            <xsl:value-of select="0" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when
                                    test="number(xalan:nodeset($derNumberPerDate)/date[position()=$position]/text()) and (xalan:nodeset($derNumberPerDate)/date[position()=$position]/text() &gt; 0)">
                                    <xsl:value-of
                                        select="(((number(xalan:nodeset($derNumberPerDate)/date[position()=$position]/text())-$minTotal) div $maxMinDistance*75)+25)" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>

                </xsl:variable>
                <xsl:value-of select="concat(',', $tempValues)" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="lineChartValues" select="substring($lineVal,2)" />

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

        <xsl:variable name="ObjectDevChart.labels.decreased">
            <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                select="mcrxml:decreaseLabels( string(substring($chartURL.label.tmp,0,(string-length($chartURL.label.tmp)-1))), string(concat('|',0,'|',$label1,'|',$label2,'|',$label3,'|',$label4)), string($numberOfLabels) )" />
        </xsl:variable>
        <xsl:variable name="ObjectDevChart.values.decreased">
            <xsl:choose>
                <xsl:when test="$dateRange>$numberOfValues">
                    <xsl:value-of xmlns:mcrxml="xalan://org.mycore.frontend.cli.MCRJournalStatsUtilities"
                        select="mcrxml:decreaseValues( string( $lineChartValues ) , string($numberOfValues) )" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$lineChartValues" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <br />
        <br />
        <a name="derivates" />
        <b style="padding: 5px;">
            <u>
                <xsl:copy-of
                    select="concat('Anzahl der Digitalisate auf dem Zeitschriftenserver (Differenz=',$lastFirstDistance,' Grösse= ',$overallDerSize,'):')" />
            </u>
        </b>
        <a style="margin-left: 20px; border:1px solid black;" href="#toc">^^ zurück ^^</a>

        <p style="text-align: center;">
            <img
                src="{concat($chartBaseUrl,'cht=lc&amp;chd=t:',$ObjectDevChart.values.decreased,'&amp;chs=',$chartSize.objectDev,$ObjectDevChart.labels.decreased,'&amp;chco=0000ff','&amp;chg=',$xgrid,',25')}" />
        </p>

    </xsl:template>

    <!-- ======================================================================================================================== -->
    <xsl:template name="get.Selecter">
        <xsl:value-of select="$selecter" />
    </xsl:template>
    <!-- ======================================================================================================================== -->

</xsl:stylesheet>