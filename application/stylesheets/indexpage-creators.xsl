<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.5 $ $Date: 2006/09/20 14:22:20 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:include href="MyCoReLayout.xsl" />
  <xsl:variable name="MainTitle" select="i18n:translate('indexpage.maintitle')" />
  <xsl:variable name="PageTitle" select="i18n:translate('indexpage.pagetitle')" />
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="HttpSession" />

  <!-- ========== Variablen ========== -->
  <xsl:variable name="search" select="/indexpage/results/@search" />
  <xsl:variable name="mode" select="/indexpage/results/@mode" />
  <xsl:variable name="IndexID" select="/indexpage/index/@id" />

  <!-- ======== headline ======== -->
  <xsl:template name="index.headline">
    <tr valign="top">
      <td class="metaname">
        <xsl:value-of select="i18n:translate('indexpage.headline.select')" />
        <xsl:value-of select="i18n:translate('indexpage.headline.indextitle')" />
      </td>
    </tr>
  </xsl:template>

  <!-- ======== intro text ======== -->
  <xsl:template name="index.intro">
    <tr>
      <td class="metavalue">
        <xsl:call-template name="IntroText" />
      </td>
    </tr>
  </xsl:template>

  <!-- ======== index search ======== -->
  <xsl:template name="index.search">
    <tr>
      <td>
        <table border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td class="metavalue">
              <form
                action="{$WebApplicationBaseURL}indexpage{$HttpSession}?searchclass={$IndexID}"
                method="post">
				<b>
					<xsl:value-of
						select="i18n:translate('indexpage.creators.index')"/>
				</b>
                <select name="mode" size="1" class="button">
                  <option value="prefix">
                    <xsl:if test="$mode = 'prefix'">
                      <xsl:attribute name="selected">
                        selected
                      </xsl:attribute>
                    </xsl:if>
                    <xsl:value-of
						select="i18n:translate('indexpage.creators.contains')"/>
                  </option>
                </select>
                <xsl:text></xsl:text>
                <input type="text" class="button" size="30"
                  name="search" value="{$search}" />
                <xsl:text></xsl:text>
                <input type="submit" class="button" value="{i18n:translate('indexpage.buttons.search')}" />
              </form>
            </td>
            <xsl:if test="string-length($search) &gt; 0 ">
              <td class="metavalue">
                <form
                  action="{$WebApplicationBaseURL}indexpage{$HttpSession}?searchclass={$IndexID}"
                  method="post">
                  <b>
                    <xsl:text></xsl:text>
                    <xsl:value-of select="results/@numHits" />
                    <xsl:value-of
						select="i18n:translate('indexpage.creators.hits')"/>
                  </b>
                  <input type="submit" class="button"
                    value="{i18n:translate('indexpage.buttons.filter.disable')}" />
                </form>
              </td>
            </xsl:if>
          </tr>
        </table>
      </td>
    </tr>
  </xsl:template>

  <!-- ======== indexpage ======== -->
  <xsl:template match="indexpage">
    <table>
      <xsl:call-template name="index.headline" />
      <xsl:call-template name="index.intro" />
      <xsl:call-template name="index.search" />
      <xsl:apply-templates select="results" />
    </table>
  </xsl:template>

  <xsl:variable name="up.url">
    <xsl:value-of
      select="concat('indexpage',$HttpSession,'?searchclass=',$IndexID)" />
    <xsl:if test="string-length($search) &gt; 0">
      <xsl:value-of select="concat('&amp;search=',$search)" />
    </xsl:if>
  </xsl:variable>

  <!-- ========== results ========== -->
  <xsl:template match="results">
    <tr>
      <td class="metavalue">
        <xsl:if test="range">
          <dl>
            <dt>
              <xsl:choose>
                <xsl:when test="contains(/indexpage/@path,'-')">
                  <b>
                    <a class="nav" href="{$up.url}"><xsl:value-of
						select="i18n:translate('indexpage.link.back')"/></a>
                  </b>
                </xsl:when>
                <xsl:when test="string-length($search) &gt; 0">
                  <b><xsl:value-of
						select="i18n:translate('indexpage.results.overallindex.filtered')"/>
					  </b>
                </xsl:when>
                <xsl:otherwise>
                  <b><xsl:value-of
						select="i18n:translate('indexpage.results.overallindex')"/></b>
                </xsl:otherwise>
              </xsl:choose>

            </dt>
            <xsl:apply-templates select="range" />
          </dl>
        </xsl:if>
        <xsl:if test="value">
          <dl>
            <dt>
              <xsl:choose>
                <xsl:when test="contains(/indexpage/@path,'-')">
                  <b>
                    <a class="nav" href="{$up.url}"><xsl:value-of
						select="i18n:translate('indexpage.link.back')"/></a>
                  </b>
                </xsl:when>
                <xsl:when test="string-length($search) &gt; 0">
                  <b><xsl:value-of
						select="i18n:translate('indexpage.results.entries.filtered')"/>
					  </b>
                </xsl:when>
              </xsl:choose>

            </dt>
            <dd>
              <table border="0" cellpadding="0" cellspacing="0"
                style="padding-bottom:5px">
                <xsl:apply-templates select="value" />
              </table>
            </dd>
          </dl>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>

  <!-- ========== value ========== -->
  <xsl:template match="value">
    <xsl:variable name="id" select="col[@name='id']" />
    <xsl:variable name="urlAuthor"
      select="concat($WebApplicationBaseURL,'receive/',$id, $HttpSession)">
    </xsl:variable>
    <tr>
      <td class="td1" valign="top">
        <img border="0" src="images/folder_plain.gif" />
      </td>
      <td class="td1" valign="top" style="padding-right:5px;">
        <xsl:value-of select="concat(sort,', ',col[@name='academic'],' ',col[@name='peerage'],' ',col[@name='firstname'],' ',col[@name='prefix'])" />
      </td>
      <td class="td1" valign="top" style="padding-right:5px;">
        <a href="{$urlAuthor}"><xsl:value-of
						select="i18n:translate('indexpage.link.details')"/></a>
      </td>
    </tr>

  </xsl:template>

  <!-- ========== range ========== -->
  <xsl:template match="range">
    <xsl:variable name="url">
      <xsl:value-of
        select="concat($WebApplicationBaseURL,'indexpage',$HttpSession,'?searchclass=',$IndexID,'&amp;fromTo=', from/@pos,'-', to/@pos )" />
      <xsl:if test="string-length($search) &gt; 0">
        <xsl:value-of select="concat('&amp;search=',$search)" />
      </xsl:if>
    </xsl:variable>

    <dd>
      <img border="0" src="images/folder_plus.gif" align="middle" />
      <a href="{$url}" class="nav">
        <xsl:value-of select="concat(from/@short,' - ',to/@short)" />
      </a>
    </dd>
  </xsl:template>


  <!-- ========== Titel ========== -->
  <xsl:variable name="IndexTitle" select="i18n:translate('indexpage.indextitle')" />

  <!-- ========== Einleitender Text ========== -->
  <xsl:template name="IntroText">
	  <xsl:value-of select="i18n:translate('indexpage.introtext')"/>
    <p>
      <xsl:value-of select="' | '" />
      <xsl:for-each select="xalan:nodeset($AtoZ)/search">
        <a
          href="{$WebApplicationBaseURL}indexpage{$HttpSession}?searchclass={$IndexID}&amp;search={@prefix}">
          <xsl:value-of select="@prefix" />
        </a>
        <xsl:value-of select="' | '" />
      </xsl:for-each>
    </p>
  </xsl:template>

  <xsl:variable name="AtoZ">
    <search prefix="A" />
    <search prefix="B" />
    <search prefix="C" />
    <search prefix="D" />
    <search prefix="E" />
    <search prefix="F" />
    <search prefix="G" />
    <search prefix="H" />
    <search prefix="I" />
    <search prefix="J" />
    <search prefix="K" />
    <search prefix="L" />
    <search prefix="M" />
    <search prefix="N" />
    <search prefix="O" />
    <search prefix="P" />
    <search prefix="Q" />
    <search prefix="R" />
    <search prefix="S" />
    <search prefix="T" />
    <search prefix="U" />
    <search prefix="V" />
    <search prefix="W" />
    <search prefix="X" />
    <search prefix="Y" />
    <search prefix="Z" />
  </xsl:variable>

</xsl:stylesheet>