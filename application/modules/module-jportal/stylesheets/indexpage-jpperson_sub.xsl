<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xalan="http://xml.apache.org/xalan">

  <xsl:variable name="MainTitle" select="i18n:translate('indexpage.sub.maintitle')" />
  <xsl:variable name="PageTitle" select="i18n:translate('indexpage.sub.pagetitle')" />
  <xsl:variable name="Servlet" select="'MCRIndexBrowserServlet'" />
  <xsl:include href="MyCoReLayout.xsl" />
  <xsl:param name="WebApplicationBaseURL" />

  <!-- ========== Navigation ========== -->
  <xsl:variable name="page.title" select="$PageTitle" />
  <xsl:variable name="PageID" select="concat('index.', /indexpage/index/@id)" />

  <!-- ========== Variablen ========== -->
  <xsl:variable name="search" select="/indexpage/results/@search" />
  <xsl:variable name="mode" select="/indexpage/results/@mode" />
  <xsl:variable name="IndexID" select="/indexpage/index/@id" />

  <!-- ========== Subselect Parameter ========== -->
  <xsl:param name="subselect.session" />
  <xsl:param name="subselect.varpath" />
  <xsl:param name="subselect.webpage" />

  <xsl:variable name="subselect.params">
    <xsl:text>XSL.subselect.session=</xsl:text>
    <xsl:value-of select="$subselect.session" />
    <xsl:text>&amp;XSL.subselect.varpath=</xsl:text>
    <xsl:value-of select="$subselect.varpath" />
    <xsl:text>&amp;XSL.subselect.webpage=</xsl:text>
    <xsl:value-of select="$subselect.webpage" />
  </xsl:variable>

  <!-- ======== headline ======== -->
  <xsl:template name="index.headline">
<!--    <tr valign="top">
      <td class="metaname">
        <xsl:value-of select="i18n:translate('indexpage.sub.headline.select')" />
        <xsl:value-of select="$IndexTitle" />
      </td>
    </tr>-->
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
              <form action="{$WebApplicationBaseURL}indexpage{$HttpSession}?searchclass={$IndexID}" method="post">
                <b>
					<xsl:value-of select="i18n:translate('indexpage.sub.index')" />
				</b>
                <select name="mode" size="1" class="button">
                  <option value="prefix">
                    <xsl:if test="$mode = 'prefix'">
                      <xsl:attribute name="selected">selected</xsl:attribute>
                    </xsl:if>
                    <xsl:value-of select="i18n:translate('indexpage.sub.contains')" />
                  </option>
                </select>
                <xsl:text></xsl:text>
				  <xsl:choose>
					  <xsl:when test="$search='xxxxxxxxxxxxxxxxxxxxx'">
			                <input type="text" class="button" size="30" name="search" value="" />						  
					  </xsl:when>
					  <xsl:otherwise>
			                <input type="text" class="button" size="30" name="search" value="{$search}" />						  
					  </xsl:otherwise>
				  </xsl:choose>
                <xsl:text></xsl:text>
                <input type="hidden" name="XSL.subselect.session" value="{$subselect.session}" />
                <input type="hidden" name="XSL.subselect.varpath" value="{$subselect.varpath}" />
                <input type="hidden" name="XSL.subselect.webpage" value="{$subselect.webpage}" />
                <input type="submit" class="button" value="{i18n:translate('indexpage.sub.buttons.search')}" />
              </form>
            </td>
            <xsl:if test="string-length($search) &gt; 0 ">
              <td class="metavalue">
                <form
                  action="{$WebApplicationBaseURL}indexpage{$HttpSession}?searchclass={$IndexID}&amp;search={@prefix}"
                  method="post">
                  <b>
                    <xsl:text></xsl:text>
                    <xsl:value-of select="results/@numHits" />
                    <xsl:value-of select="i18n:translate('indexpage.sub.hits')" />
                  </b>
                  <input type="hidden" name="XSL.subselect.session" value="{$subselect.session}" />
                  <input type="hidden" name="XSL.subselect.varpath" value="{$subselect.varpath}" />
                  <input type="hidden" name="XSL.subselect.webpage" value="{$subselect.webpage}" />
<!--                  <input type="submit" class="button" value="{i18n:translate('indexpage.sub.buttons.filter.disable')}" />-->
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
			<xsl:call-template name="index.headline"/>
			<xsl:call-template name="index.intro"/>
			<xsl:call-template name="index.search"/>
			<xsl:apply-templates select="results"/>
		</table>				
  </xsl:template>
<!--	http://141.35.20.199:8291/indexpage?searchclass=jpperson_sub&mode=prefix&search=G&XSL.subselect.session=9r4c5hhb96cc-pm0fvqve&XSL.subselect.varpath=/mycoreobject/metadata/participants/participant&XSL.subselect.webpage=editor_form_commit-jpjournal.xml-->
  <xsl:variable name="up.url">
    <xsl:text>indexpage?searchclass=</xsl:text>
    <xsl:value-of select="$IndexID" />
    <xsl:text>&amp;</xsl:text>
    <xsl:value-of select="$subselect.params" />
    <xsl:if test="string-length($search) &gt; 0">
      <xsl:text>&amp;search=</xsl:text>
      <xsl:value-of select="$search" />
      <xsl:text>&amp;mode=</xsl:text>
      <xsl:value-of select="$mode" />
    </xsl:if>
  </xsl:variable>

  <!-- ========== results ========== -->
  <xsl:template match="results">
    <tr>
      <td class="metavalue">
        <xsl:if test="range">
          <dl>
            <dt>
              <img border="0" src="{$WebApplicationBaseURL}images/folder_open_in_use.gif" align="middle" />

              <xsl:choose>
                <xsl:when test="contains(/indexpage/@path,'-')">
                  <b>
                    <a class="nav" href="{$up.url}"><xsl:value-of select="i18n:translate('metaData.back')"/></a>
                  </b>
                </xsl:when>
                <xsl:when test="string-length($search) &gt; 0">
                  <b><xsl:value-of
						select="i18n:translate('indexpage.results.overallindex.filtered')"/></b>
                </xsl:when>
                <xsl:otherwise>
                  <b><xsl:value-of
						select="i18n:translate('indexpage.sub.results.overallindex')"/></b>
                </xsl:otherwise>
              </xsl:choose>

            </dt>
            <xsl:apply-templates select="range" />
          </dl>
        </xsl:if>
        <xsl:if test="value">
          <dl>
            <dt>
              <img border="0" src="{$WebApplicationBaseURL}images/folder_open_in_use.gif" align="middle" />

              <xsl:choose>
                <xsl:when test="contains(/indexpage/@path,'-')">
                  <b>
                    <a class="nav" href="{$up.url}"><xsl:value-of
						select="i18n:translate('indexpage.link.back')"/></a>
                  </b>
                </xsl:when>
                <xsl:when test="string-length($search) &gt; 0">
                  <b><xsl:value-of
						select="i18n:translate('indexpage.results.entries.filtered')"/></b>
                </xsl:when>
              </xsl:choose>

            </dt>
            <dd>
              <table border="0" cellpadding="0" cellspacing="0" style="padding-bottom:5px">
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
    <xsl:variable name="url"
      select="concat($ServletsBaseURL,'XMLEditor',$HttpSession,
      '?_action=end.subselect&amp;subselect.session=',$subselect.session,
      '&amp;subselect.varpath=', $subselect.varpath,
      '&amp;subselect.webpage=', $subselect.webpage)" />
	<xsl:variable name="label">
		<xsl:value-of select="concat(sort, ', ',idx)"/>  
	</xsl:variable>
	<xsl:variable name="nameXML">
		<xsl:copy-of select="document(concat('mcrobject:',col[@name='id']))/mycoreobject/metadata/def.heading/heading"/>
	</xsl:variable>
	  <xsl:variable name="lastName">
		  <xsl:value-of select="xalan:nodeset($nameXML)/heading/lastName/text()"/>
	  </xsl:variable>
	  <xsl:variable name="firstName">
		  <xsl:value-of select="xalan:nodeset($nameXML)/heading/firstName/text()"/>
	  </xsl:variable>	  
	<xsl:variable name="beautiLabel">
		<xsl:choose>
			<xsl:when test="$firstName!=''" >
				<xsl:value-of select="concat(xalan:nodeset($nameXML)/heading/lastName/text(),', ',xalan:nodeset($nameXML)/heading/firstName/text())"/>				
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$lastName" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
    <tr>
      <td class="td1" valign="top">
        <img border="0" src="{$WebApplicationBaseURL}images/folder_plain.gif" />
      </td>
      <td class="td1" valign="top" style="padding-right:5px;">
        <a href="{$url}&amp;_var_@href={col[@name='id']}&amp;_var_@label={$label}
			&amp;_var_@field=participants_art&amp;_var_@operator==&amp;_var_@value={col[@name='id']}">
			<xsl:copy-of select="$beautiLabel"/>
        </a>
      </td>
    </tr>
  </xsl:template>
  <!-- ========== range ========== -->
  <xsl:template match="range">
    <xsl:variable name="url">
      <xsl:value-of
        select="concat($WebApplicationBaseURL,'indexpage',$HttpSession,'?searchclass=',$IndexID,'&amp;fromTo=', from/@pos,'-', to/@pos )" />
      <xsl:value-of select="concat('&amp;',$subselect.params)" />      
      <xsl:if test="string-length($search) &gt; 0">
        <xsl:value-of select="concat('&amp;search=',$search)" />
      </xsl:if>
    </xsl:variable>

    <dd>
      <img border="0" src="{$WebApplicationBaseURL}images/folder_closed_in_use.gif" align="middle" />
      <a href="{$url}" class="nav">
        <xsl:value-of select="concat(from/@short,' - ',to/@short)" />
      </a>
    </dd>
  </xsl:template>

  <!-- ========== Titel ========== -->
  <xsl:variable name="IndexTitle" select="i18n:translate('indexpage.sub.indextitle')" />

  <!-- ========== Einleitender Text ========== -->
  <xsl:template name="IntroText">
    <xsl:value-of select="i18n:translate('indexpage.sub.introtext.person')"/>
    <p>
      <form
        action="{$WebApplicationBaseURL}{$subselect.webpage}{$HttpSession}?XSL.editor.session.id={$subselect.session}"
        method="post">
        <input type="submit" class="submit" value="{i18n:translate('indexpage.sub.select.cancel')}" />
      </form>
    </p>
  </xsl:template>

</xsl:stylesheet>
