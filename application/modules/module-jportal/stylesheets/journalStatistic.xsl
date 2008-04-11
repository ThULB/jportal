<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:param name="journalStatistic.dateReceived" />
    <xsl:param name="journalStatistic.view.objectListing" select="'false'" />
    <xsl:param name="journalStatistic.view.objectListing.journalID" />

    <xsl:variable name="journalStatistic.date">
        <xsl:call-template name="get.journalStatistic.date" />
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
                <xsl:call-template name="journalStatistic.selectDate" />
                <xsl:call-template name="journalStatistic.total" />
                <xsl:call-template name="journalStatistic.toc" />
                <xsl:call-template name="journalStatistic.statistics" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.objectListing">
        <p>
            <a name="incomplete" />
            <b>Artikel ohne Digitalisat:</b>
            <ul>
                <xsl:for-each
                    select="/journalStatistic/statistic[@date=$journalStatistic.date]/journal[@id=$journalStatistic.view.objectListing.journalID]/objectList[@type='incomplete']/object">
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
            <b>B�nde ohne Artikel:</b>
            <ul>
                <xsl:for-each
                    select="/journalStatistic/statistic[@date=$journalStatistic.date]/journal[@id=$journalStatistic.view.objectListing.journalID]/objectList[@type='missing']/object">
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
                            <xsl:value-of select="@datePretty" />
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
    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template match="journal">

        <!-- headline -->
        <xsl:variable name="journal-type">
            <xsl:choose>
                <xsl:when test="@type='fully'">
                    <xsl:value-of
                        select="concat(' (vollst�ndige Erschliessung, mit insgesamt ', number(numberOfObjects/total/text()), ' Artikeln)')" />
                </xsl:when>
                <xsl:when test="@type='browse'">
                    <xsl:value-of select="concat(' (Bl�tterzeitschrift, mit insgesamt ', numberOfObjects/total/text(), ' B�nden)')" />
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

        <xsl:variable name="headline">
            <xsl:choose>
                <xsl:when test="@type='fully'">
                    <xsl:value-of select="'Zahl der Artikel im zeitlichen Verlauf :'" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="'Zahl der B�nde im zeitlichen Verlauf :'" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="maxTotal">
            <xsl:for-each select="/journalStatistic/statistic/journal[@id=$jID]/numberOfObjects/total">
                <xsl:sort data-type="number" order="descending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="lineVal">
            <xsl:for-each select="/journalStatistic/statistic">
                <xsl:value-of
                    select="concat(',',(journal[@id=$jID]/numberOfObjects/complete/text()*(journal[@id=$jID]/numberOfObjects/total/text()*100 div $maxTotal) div journal[@id=$jID]/numberOfObjects/total/text())+(journal[@id=$jID]/numberOfObjects/incomplete/text()*(journal[@id=$jID]/numberOfObjects/total/text()*100 div $maxTotal) div journal[@id=$jID]/numberOfObjects/total/text()))" />
            </xsl:for-each>
        </xsl:variable>
         <xsl:variable name="lineChartValues"
            select="concat('&amp;chd=t:', substring($lineVal,2))" />
        
        <xsl:variable name="chartURL.values.tmp">
            <xsl:for-each select="/journalStatistic/statistic[number(@date) &lt;= number($journalStatistic.date)]">
                <xsl:sort select="@date" order="ascending" />           
                    <xsl:value-of select="concat(number(journal[@id=$jID]/numberOfObjects/total/text()),',')" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="chartURL.values">
            <xsl:value-of select="substring($chartURL.values.tmp,1,string-length($chartURL.values.tmp)-1)" />
        </xsl:variable>
        <xsl:variable name="chartURL.label.tmp">
            <xsl:for-each select="/journalStatistic/statistic[number(@date) &lt;= number($journalStatistic.date)]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="concat(@datePretty,'|')" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="chartURL.label">
            <xsl:value-of select="concat('|',$chartURL.label.tmp,'1:|',0,'|',round($maxTotal*0.25),'|',round($maxTotal*0.5),'|',round($maxTotal*0.75),'|',$maxTotal)" />
        </xsl:variable>

        <!--  do layout -->
        <b>
            <xsl:value-of select="$headline" />
        </b>
        <p style="text-align: center;">
            <img
                src="{concat($chartBaseUrl,'cht=lc&amp;chd=t:',$lineChartValues,'&amp;chs=',$chartSize.objectDev,'&amp;chxt=x,y&amp;chxl=0:',$chartURL.label,'&amp;chco=0000ff','&amp;chg=20,25')}" />
        </p>

    </xsl:template>

    <!-- ======================================================================================================================== -->

    <xsl:template name="journalStatistic.consistancyDev">
        <xsl:param name="jID" />

        <xsl:variable name="headline">
            <xsl:value-of select="'Konsistenzanalyse im zeitlichen Verlauf:'" />
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
                src="{concat($chartBaseUrl,'cht=lc&amp;chd=t:',$chartURL.values,'&amp;chs=',$chartSize.consistancyDev,'&amp;chxt=x,y&amp;chxl=0:',$chartURL.label,'&amp;chco=',$chartURL.colors,'&amp;chm=',$fillColors,'&amp;chg=20,25')}" />
        </p>

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
            <xsl:for-each select="/journalStatistic/statistic[number(@date) &lt;= number($journalStatistic.date)]">
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
            <xsl:for-each select="/journalStatistic/statistic[number(@date) &lt;= number($journalStatistic.date)]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="'0,'" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.zero">
            <xsl:value-of select="substring($values.zero.tmp,1,string-length($values.zero.tmp)-1)" />
        </xsl:variable>
        <!-- missings -->
        <xsl:variable name="values.missing.tmp">
            <xsl:for-each select="/journalStatistic/statistic[number(@date) &lt;= number($journalStatistic.date)]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="concat(./journal[@id=$journalID]/numberOfObjects/missing/@percent,',')" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.missing">
            <xsl:value-of select="substring($values.missing.tmp,1,string-length($values.missing.tmp)-1)" />
        </xsl:variable>
        <!-- incompletes -->
        <xsl:variable name="values.incomplete.tmp">
            <xsl:for-each select="/journalStatistic/statistic[number(@date) &lt;= number($journalStatistic.date)]">
                <xsl:sort select="@date" order="ascending" />
                <xsl:value-of select="concat(./journal[@id=$journalID]/numberOfObjects/incomplete/@percent,',')" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="values.incomplete">
            <xsl:value-of select="substring($values.incomplete.tmp,1,string-length($values.incomplete.tmp)-1)" />
        </xsl:variable>
        <!-- corrupted total -->
        <xsl:variable name="values.corruptedTotal.tmp">
            <xsl:for-each select="/journalStatistic/statistic[number(@date) &lt;= number($journalStatistic.date)]">
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
            <xsl:for-each select="/journalStatistic/statistic[number(@date) &lt;= number($journalStatistic.date)]">
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
            <xsl:value-of select="'  Konsistenzanalyse f�r gew�hltes Datum:'" />
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

        <xsl:variable name="label1" select="'Vollst�ndig:'" />
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

</xsl:stylesheet>