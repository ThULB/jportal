<?xml version="1.0" encoding="ISO-8859-1" ?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager">
  <xsl:include href="MyCoReLayout.xsl" />
  <xsl:include href="objecttypes.xsl" />
  <xsl:param name="toc.pageSize" select="20" />
  <xsl:param name="toc.pos" select="1" />
  <xsl:param name="redunObject" />
  <xsl:param name="exceptionId" />
  <xsl:param name="template" select="'template_DublicateFinder'" />

  <xsl:variable name="PageTitle" select="'Dublettenfinder'" />
  <xsl:variable name="ServletName" select="'MCRDuplicateFinderServlet'" />

  <!--
    =====================================================================================
  -->

  <xsl:template match="redundancyMap">
    <xsl:choose>
      <xsl:when test="$CurrentUser='gast'">
        Zugriff verweigert! Bitte melden Sie sich an.
      </xsl:when>
      <xsl:otherwise>
        <table width="100%">
          <xsl:choose>
            <xsl:when test="$redunObject=''">
              <xsl:call-template name="redundancy.filter" />
              <xsl:call-template name="redundancy.head" />
              <xsl:call-template name="redundancy.progressStatus" />
              <xsl:call-template name="lineBreak" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="redundancy.back" />
            </xsl:otherwise>
          </xsl:choose>
          <xsl:call-template name="printDublicates" />
          <xsl:if test="$redunObject!=''">
            <xsl:call-template name="redundancy.back" />
          </xsl:if>
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    =====================================================================================
  -->
  <xsl:template name="redundancy.back">
    <tr>
      <td>
        <div style="padding-bottom:8px">
          <a
            href="{$WebApplicationBaseURL}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}">zurück zur Übersicht</a>
        </div>
      </td>
    </tr>
  </xsl:template>
  <!--
    =====================================================================================
  -->

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

  <!--
    =====================================================================================
  -->

  <xsl:template name="redundancy.filter">
    <tr>
      <td colspan="3">
        <div style="font-weight:bold; padding-bottom:12px">
          <xsl:choose>
            <xsl:when test="$redunMode='open'">
              <div style="font-size:larger; padding-bottom:5px">
                <xsl:value-of select="' Offene Aufnahmen '" />
              </div>
              <a
                href="{$WebApplicationBaseURL}{$RedunMap}?XSL.redunMode.SESSION=closed&amp;XSL.toc.pos.SESSION=1">
                <xsl:copy-of
                  select="' (Wechsel zur Ansicht -Bereits bearbeitete Aufnahmen-) '" />
              </a>
              <xsl:value-of select="' oder '" />
              <a
                href="{$WebApplicationBaseURL}{$RedunMap}?XSL.redunMode.SESSION=error&amp;XSL.toc.pos.SESSION=1">
                <xsl:copy-of
                  select="' (Wechsel zur Ansicht -Falsch erkannte Aufnahmen-) '" />
              </a>
            </xsl:when>
            <xsl:when test="$redunMode='closed'">
              <div style="font-size:larger; padding-bottom:5px">
                <xsl:value-of select="' Bereits bearbeitete Aufnahmen '" />
              </div>
              <a
                href="{$WebApplicationBaseURL}{$RedunMap}?XSL.redunMode.SESSION=open&amp;XSL.toc.pos.SESSION=1">
                <xsl:copy-of
                  select="' (Wechsel zur Ansicht -Offene Aufnahmen-)'" />
              </a>
              <xsl:value-of select="' oder '" />
              <a
                href="{$WebApplicationBaseURL}{$RedunMap}?XSL.redunMode.SESSION=error&amp;XSL.toc.pos.SESSION=1">
                <xsl:copy-of
                  select="' (Wechsel zur Ansicht -Falsch erkannte Aufnahmen-) '" />
              </a>
            </xsl:when>
            <xsl:otherwise>
              <div style="font-size:larger; padding-bottom:5px">
                <xsl:value-of select="'Falsch erkannte Aufnahmen'" />
              </div>
              <a
                href="{$WebApplicationBaseURL}{$RedunMap}?XSL.redunMode.SESSION=open&amp;XSL.toc.pos.SESSION=1">
                <xsl:copy-of
                  select="' (Wechsel zur Ansicht -Offene Aufnahmen-)'" />
              </a>
              <xsl:value-of select="' oder '" />
              <a
                href="{$WebApplicationBaseURL}{$RedunMap}?XSL.redunMode.SESSION=closed&amp;XSL.toc.pos.SESSION=1">
                <xsl:copy-of
                  select="' (Wechsel zur Ansicht -Bereits bearbeitete Aufnahmen-) '" />
              </a>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
    </tr>
  </xsl:template>

  <!--
    =====================================================================================
  -->

  <xsl:template name="redundancy.progressStatus">
    <xsl:variable name="numTotal">
      <xsl:value-of select="count(redundancyObjects/object)" />
    </xsl:variable>
    <xsl:variable name="numNonDoublets">
      <xsl:value-of
        select="count(redundancyObjects/object[@status='nonDoublet'])" />
    </xsl:variable>
    <xsl:variable name="numDoublets">
      <xsl:value-of select="count(redundancyObjects/object[@status='doublet'])" />
    </xsl:variable>
    <xsl:variable name="numError">
      <xsl:value-of select="count(redundancyObjects/object[@status='error'])" />
    </xsl:variable>
    <xsl:variable name="progressTotal">
      <xsl:value-of
        select="format-number(((($numNonDoublets+$numDoublets+$numError) div $numTotal) * 100),'#.##' )" />
    </xsl:variable>
    <xsl:variable name="progressAccepted">
      <xsl:value-of
        select="format-number(($numDoublets div ($numDoublets+$numNonDoublets+$numError)) * 100,'#.##')" />
    </xsl:variable>
    <xsl:variable name="progressDenied">
      <xsl:value-of
        select="format-number(($numNonDoublets div ($numDoublets+$numNonDoublets+$numError)) * 100,'#.##')" />
    </xsl:variable>
    <xsl:variable name="progressError">
      <xsl:value-of
        select="format-number(($numError div ($numDoublets+$numNonDoublets+$numError)) * 100,'#.##')" />
    </xsl:variable>
    <tr>
      <td colspan="3">
        <b>
          <xsl:copy-of
            select="concat(' Bearbeitungsstatus: ',$progressTotal,'% (',$numDoublets+$numNonDoublets+$numError,'/',$numTotal,')')" />
          <xsl:if test="$progressTotal != 0">
            <xsl:copy-of
              select="concat(', davon ',$progressAccepted,'% Dubletten, ',$progressDenied,'% Originale und ', $progressError,'% falsch erkannte Aufnahmen')" />
          </xsl:if>
        </b>
      </td>
    </tr>
  </xsl:template>

  <!--
    =====================================================================================
  -->

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
      <xsl:value-of
        select="count(xalan:nodeset($filteredRedunMap)/redundancyObjects)" />
    </xsl:variable>

    <table>
      <tr>
        <td colspan="2">
          <xsl:value-of
            select="concat(i18n:translate('metaData.sortbuttons.numberofres'),': ')" />
          <b>
            <xsl:value-of select="$numChildren" />
          </b>
          <xsl:call-template name="redundancy.printTOCNavi.chooseHitPage">
            <xsl:with-param name="children"
              select="xalan:nodeset($filteredRedunMap)" />
          </xsl:call-template>
        </td>
      </tr>
    </table>
  </xsl:template>

  <!--
    =====================================================================================================
  -->

  <xsl:template name="redundancy.printTOCNavi.chooseHitPage">
    <xsl:param name="children" />

    <xsl:variable name="numberOfChildren">
      <xsl:value-of
        select="count(xalan:nodeset($filteredRedunMap)/redundancyObjects)" />
    </xsl:variable>
    <xsl:variable name="numberOfHitPages">
      <xsl:value-of
        select="ceiling(number($numberOfChildren) div number($toc.pageSize))" />
    </xsl:variable>
    <xsl:if test="number($numberOfChildren)>number($toc.pageSize)">
      <xsl:value-of select="concat(', ',i18n:translate('metaData.resultpage'))" />
      <xsl:for-each
        select="xalan:nodeset($filteredRedunMap)/redundancyObjects[number($numberOfHitPages)>=position()]">
        <xsl:variable name="jumpToPos">
          <xsl:value-of
            select="(position()*number($toc.pageSize))-number($toc.pageSize)" />
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="number($jumpToPos)+1=number($toc.pos)">
            <xsl:value-of select="concat(' [',position(),'] ')" />
          </xsl:when>
          <xsl:otherwise>
            <a
              href="{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.toc.pos.SESSION={$jumpToPos+1}">
              <xsl:value-of select="concat(' ',position(),' ')" />
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:if>

  </xsl:template>

  <!--
    =====================================================================================================
  -->

  <xsl:template name="printDublicates">
    <xsl:variable name="toc.pos.verif">
      <xsl:choose>
        <xsl:when test="$toc.pageSize>count(./redundancyObjects)">
          <xsl:value-of select="1" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$toc.pos" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <tr>
      <td>
        <table>
          <xsl:choose>
            <xsl:when test="$redunObject!=''">
              <xsl:call-template name="printDublicates.entries">
                <xsl:with-param name="redunObject"
                  select="$redunObject" />
              </xsl:call-template>
            </xsl:when>
            <xsl:when test="$redunMode='open'">
              <xsl:call-template name="printDublicates.tableHead" />
              <xsl:for-each
                select="xalan:nodeset($filteredRedunMap)/redundancyObjects[(position()>=$toc.pos.verif) and ($toc.pos.verif+$toc.pageSize>position())]">
                <tr>
                  <xsl:call-template name="printDublicates.openList" />
                </tr>
              </xsl:for-each>
            </xsl:when>
            <xsl:when test="$redunMode='closed'">
              <xsl:call-template name="printDublicates.tableClosedHead" />
              <xsl:for-each
                select="xalan:nodeset($filteredRedunMap)/redundancyObjects[(position()>=$toc.pos.verif) and ($toc.pos.verif+$toc.pageSize>position())]">
                <xsl:sort select="@time" order="ascending" />
                <tr>
                  <xsl:call-template name="printDublicates.closedList" />
                </tr>
              </xsl:for-each>
            </xsl:when>
            <xsl:when test="$redunMode='error' ">
              <xsl:call-template name="printDublicates.tableHead" />

              <!--
                <xsl:for-each select="redundancyObjects[
                (position()>=$toc.pos.verif) and
                ($toc.pos.verif+$toc.pageSize>position())]/object[@status='error']">
                <xsl:sort select="@time" order="ascending" /> <tr>
                <xsl:call-template name="printDublicates.errorList" />
                </tr> </xsl:for-each> - <xsl:for-each
                select="xalan:nodeset($filteredRedunMap)/object[(position()>=$toc.pos.verif)
                and ($toc.pos.verif+$toc.pageSize>position())]">
                <xsl:sort select="@time" order="ascending" /> <tr>
                <xsl:call-template name="printDublicates.errorList" />
                </tr> </xsl:for-each>
              -->

              <xsl:for-each
                select="xalan:nodeset($filteredRedunMap)/redundancyObjects[(position()>=$toc.pos.verif) and ($toc.pos.verif+$toc.pageSize>position())]">
                <xsl:sort select="@time" order="ascending" />
                <tr>
                  <xsl:call-template name="printDublicates.errorList" />
                </tr>
              </xsl:for-each>
            </xsl:when>
          </xsl:choose>
        </table>
      </td>
    </tr>
    <xsl:call-template name="lineBreak" />
  </xsl:template>

  <!--
    ===================================================================================
    Overview perspective
    ===================================================================================
  -->
  <xsl:template name="printDublicates.tableHead">
    <xsl:variable name="col1" select="substring-before(@tableHead, ',')" />
    <xsl:variable name="col2" select="substring-after(@tableHead, ',')" />
    <tr>
      <xsl:choose>
        <xsl:when test="$col1 != ''">
          <th align="left" style="padding-right:25px">
            <xsl:value-of select="$col1" />
          </th>
          <th align="left" style="padding-right:25px">
            <xsl:value-of select="$col2" />
          </th>
        </xsl:when>
        <xsl:otherwise>
          <th align="left" style="padding-right:25px">
            <xsl:value-of select="@tableHead" />
          </th>
        </xsl:otherwise>
      </xsl:choose>
    </tr>
  </xsl:template>

  <!--
    ================================================================================
  -->

  <xsl:template name="printDublicates.tableClosedHead">
    <xsl:variable name="col1" select="substring-before(@tableHead, ',')" />
    <xsl:variable name="col2" select="substring-after(@tableHead, ',')" />
    <tr>
      <xsl:choose>
        <xsl:when test="$col1 != ''">
          <th align="left" style="padding-right:25px">
            <xsl:value-of select="$col1" />
          </th>
          <th align="left" style="padding-right:25px">
            <xsl:value-of select="$col2" />
          </th>
        </xsl:when>
        <xsl:otherwise>
          <th align="left" style="padding-right:25px">
            <xsl:value-of select="@tableHead" />
          </th>
        </xsl:otherwise>
      </xsl:choose>
      <th align="left" style="padding-right:25px">
        <xsl:value-of select="'Bearbeiter'" />
      </th>
      <th align="left">
        <xsl:value-of select="'letzte Änderung'" />
      </th>
    </tr>
  </xsl:template>

  <!--
    =================================================================================
  -->

  <xsl:template name="printDublicates.openList">
    <xsl:variable name="col1" select="substring-before(@name, ',')" />
    <xsl:variable name="col2" select="substring-after(@name, ',')" />

    <tr>
      <xsl:choose>
        <xsl:when test="$col1 != ''">
          <td style="padding-right:25px; text-align:left">
            <a
              href="{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}&amp;XSL.redunObject={@id}">
              <xsl:value-of select="$col1" />
            </a>
          </td>
          <td style="padding-right:25px; text-align:left">
            <a
              href="{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}&amp;XSL.redunObject={@id}">
              <xsl:value-of select="$col2" />
            </a>
          </td>
        </xsl:when>
        <xsl:otherwise>
          <td style="padding-right:25px; text-align:left">
            <a
              href="{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}&amp;XSL.redunObject={@id}">
              <xsl:value-of select="@name" />
            </a>
          </td>
        </xsl:otherwise>
      </xsl:choose>
      <td style="text-align:left">
        <xsl:value-of select="concat('(', count(object), ' Aufnahmen)')" />
      </td>
    </tr>
  </xsl:template>

  <!--
    =================================================================================
  -->

  <xsl:template name="printDublicates.closedList">
    <xsl:variable name="col1" select="substring-before(@name, ',')" />
    <xsl:variable name="col2" select="substring-after(@name, ',')" />

    <tr>
      <xsl:choose>
        <xsl:when test="$col1 != ''">
          <td style="padding-right:25px; text-align:left">
            <a
              href="{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}&amp;XSL.redunObject={@id}">
              <xsl:value-of select="$col1" />
            </a>
          </td>
          <td style="padding-right:25px; text-align:left">
            <a
              href="{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}&amp;XSL.redunObject={@id}">
              <xsl:value-of select="$col2" />
            </a>
          </td>
        </xsl:when>
        <xsl:otherwise>
          <td style="padding-right:25px; text-align:left">
            <a
              href="{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}&amp;XSL.redunObject={@id}">
              <xsl:value-of select="@name" />
            </a>
          </td>
        </xsl:otherwise>
      </xsl:choose>
      <td style="padding-right:25px; text-align:left">
        <xsl:value-of select="@userRealName" />
      </td>
      <td style="padding-right:25px; text-align:left">
        <xsl:value-of select="@timePretty" />
      </td>
      <td style="text-align:left">
        <xsl:value-of select="concat('(', count(object), ' Aufnahmen)')" />
      </td>
    </tr>
  </xsl:template>

  <!--
    ===================================================================================
  -->

  <xsl:template name="printDublicates.errorList">

    <!--
      <tr><td> <xsl:value-of select="../@status" /> </td></tr>
    -->

    <xsl:variable name="col1" select="substring-before(@name, ',')" />
    <xsl:variable name="col2" select="substring-after(@name, ',')" />
    <tr>
      <xsl:choose>
        <xsl:when test="$col1 != ''">
          <td style="padding-right:25px; text-align:left">
            <a
              href="{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}&amp;XSL.redunObject={@id}">
              <xsl:value-of select="$col1" />
            </a>
          </td>
          <td style="padding-right:25px; text-align:left">
            <a
              href="{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}&amp;XSL.redunObject={@id}">
              <xsl:value-of select="$col2" />
            </a>
          </td>
        </xsl:when>
        <xsl:otherwise>
          <td style="padding-right:25px; text-align:left">
            <a
              href="{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}&amp;XSL.redunObject={@id}">
              <xsl:value-of select="@name" />
            </a>
          </td>
        </xsl:otherwise>
      </xsl:choose>
      <td style="text-align:left">
        <xsl:variable name="errorCount" select='count(object[@status="error"])' />
        <xsl:choose>
          <xsl:when test="$errorCount &gt; 1">
            <xsl:value-of
              select="concat('(', $errorCount,' falsch erkannte Aufnahmen)')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of
              select="concat('(', 'eine falsch erkannte Aufnahme)')" />
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <!--
    ===================================================================================
    Detailview
    ===================================================================================
  -->

  <xsl:template name="printDublicates.entries">
    <xsl:param name="redunObject" />
    <!--  Error -->
    <xsl:if test="$exceptionId &gt; 0">
      <tr>
        <td colspan="3" align="center">
          <xsl:call-template name="printException" />
        </td>
      </tr>
    </xsl:if>
    <!-- Head -->
    <tr>
      <td colspan="3">
        <div style="padding-bottom:25px;">
          <strong>
            <u>
              <xsl:variable name="objectName"
                select="redundancyObjects[@id=$redunObject]/@name" />
              <xsl:variable name="o1"
                select="substring-before($objectName, ',')" />
              <xsl:variable name="o2"
                select="substring-after($objectName, ',')" />
              <xsl:choose>
                <xsl:when test="$o1 != ''">
                  <xsl:value-of select="concat($o1, ', ', $o2)" />
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$objectName" />
                </xsl:otherwise>
              </xsl:choose>
              <xsl:value-of select="':'" />
            </u>
          </strong>
        </div>
      </td>
    </tr>
    <!-- Content -->
    <form id="object_{$redunObject}" action="{$ServletsBaseURL}{$ServletName}"
      method="get">
      <input name="redunMode" type="hidden" value="{$redunMode}" />
      <input name="redunMap" type="hidden" value="{$RedunMap}" />
      <input name="redunObject" type="hidden" value="{$redunObject}" />
      <tr>
        <td>
          <xsl:for-each select="redundancyObjects[@id=$redunObject]/object">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 1">
                <div style="width:49%; float:left; padding-bottom:25px;">
                  <xsl:call-template name="printDublicates.entry" />
                </div>
              </xsl:when>
              <xsl:otherwise>
                <div style="width:49%; float:right; padding-bottom:25px;">
                  <xsl:call-template name="printDublicates.entry" />
                </div>
                <!-- forcing a newline -->
                <div style="clear:left" />
                <div style="clear:right" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </td>
      </tr>
      <tr>
        <td>
          <div style="float:left">
            <input type="button" value="&lt; vorheriges"
              onclick="location='{$WebApplicationBaseURL}{$HttpSession}{$RedunMap}?XSL.redunMode.SESSION={$redunMode}&amp;XSL.redunObject={$redunObject - 1}'" />
          </div>
          <div style="float:right">
            <input type="submit" value="Speichern >" />
          </div>
        </td>
      </tr>
    </form>
  </xsl:template>

  <!--
    ===============================================================================
  -->

  <xsl:template name="printDublicates.entry">
    <xsl:variable name="selectedStatus" select="@status" />
    <xsl:variable name="mcrobj"
      select="document(concat('mcrobject:',./@objId))" />

    <div style="width:30%; float:left; padding-bottom:5px;">
      <xsl:value-of select="@objId" />
    </div>
    <div style="width:70%; padding-bottom:5px;">
      <input name="object-id_{position()}" type="hidden" value="{@objId}" />
      <select name="selection_{position()}"
        value="(this.form.selection_{position()}.options[this.form.selection_{position()}.selectedIndex].value)">
        <xsl:call-template name="selectedOption">
          <xsl:with-param name="value" select="''" />
          <xsl:with-param name="text" select="'noch nicht bearbeitet'" />
          <xsl:with-param name="selectedStatus" select="$selectedStatus" />
        </xsl:call-template>
        <xsl:call-template name="selectedOption">
          <xsl:with-param name="value" select="'nonDoublet'" />
          <xsl:with-param name="text"
            select="'keine Dublette (Original)'" />
          <xsl:with-param name="selectedStatus" select="$selectedStatus" />
        </xsl:call-template>
        <xsl:call-template name="selectedOption">
          <xsl:with-param name="value" select="'doublet'" />
          <xsl:with-param name="text" select="'Dublette'" />
          <xsl:with-param name="selectedStatus" select="$selectedStatus" />
        </xsl:call-template>
        <xsl:call-template name="selectedOption">
          <xsl:with-param name="value" select="'error'" />
          <xsl:with-param name="text"
            select="'falsch erkannte Aufnahme'" />
          <xsl:with-param name="selectedStatus" select="$selectedStatus" />
        </xsl:call-template>
      </select>
    </div>
    <i>
      <xsl:apply-templates select="$mcrobj" mode="present" />
    </i>
  </xsl:template>

  <!--
    ============================================================================
  -->

  <xsl:template name="printException">
    <p style="color:#CC1111;font-weight:bolder;">
      <xsl:value-of select="'Fehler: '" />
      <xsl:choose>
        <xsl:when test="$exceptionId=1">
          <xsl:value-of
            select="'Dublette(n) konnten keiner Original-Aufnahme zugewiesen werden.'" />
          <br />
          <xsl:value-of
            select="'Bitte definieren Sie nur EINE Aufnahme als `keine Dublette (Original)`.'" />
        </xsl:when>
        <xsl:when test="$exceptionId=2">
          <xsl:value-of
            select="'Dublette(n) konnten keine Original-Aufnahme zugewiesen werden.'" />
          <br />
          <xsl:value-of
            select="'Bitte definieren eine Aufnahme als `keine Dublette (Original)`.'" />
        </xsl:when>
      </xsl:choose>
    </p>
  </xsl:template>

  <!--
    =====================================================================================
  -->
  <xsl:template name="selectedOption">
    <xsl:param name="value" />
    <xsl:param name="text" />
    <xsl:param name="selectedStatus" />

    <xsl:choose>
      <xsl:when test="$selectedStatus=$value">
        <option value="{$value}" selected="selected">
          <xsl:value-of select="$text" />
        </option>
      </xsl:when>
      <xsl:otherwise>
        <option value="{$value}">
          <xsl:value-of select="$text" />
        </option>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    =====================================================================================
  -->

  <xsl:template name="lineBreak">
    <tr>
      <td colspan="3">
        __________________________________________________________________________________________________
        <br />
        <br />
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>