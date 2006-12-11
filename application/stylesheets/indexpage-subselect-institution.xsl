<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.3 $ $Date: 2006/06/22 15:27:16 $ -->
<!-- ============================================== -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  version="1.0" exclude-result-prefixes="xlink">

  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:variable name="PageTitle">Auswahlliste für Institutionen</xsl:variable>
  <xsl:variable name="Servlet" select="'undefined'" />

  <!-- ======== Subselect Parameter ======== -->
  <xsl:param name="subselect.session" />
  <xsl:param name="subselect.varpath" />
  <xsl:param name="subselect.webpage" />

  <!-- ======== Prepare the return URL ======== -->
  <xsl:variable name="url"
    select="concat($ServletsBaseURL,'XMLEditor',$HttpSession,
      '?_action=end.subselect&amp;subselect.session=',$subselect.session,
      '&amp;subselect.varpath=', $subselect.varpath,
      '&amp;subselect.webpage=', $subselect.webpage)" />

  <!-- ======== page start ======== -->
  <xsl:template match="/indexpage">
    <center>
      <table class="contentArea" width="100%" height="100%" border="0" cellspacing="0" cellpadding="20">
        <tr>
          <td align="center">
            <table class="sitemap" width="90%" height="100%" border="0" cellspacing="10" cellpadding="0">
              <tr>
                <th class="sitemap" style="font-size : 16px;">
                  <xsl:copy-of select="$PageTitle" />
                </th>
              </tr>
              <tr>
                <td class="sitemap">
                  <p align="justify">
                    Wählen Sie aus der gegebenen Liste eine Institution aus, auf welche Sie referenzieren möchten.
                  </p>
                  <p align="center">
                    <center>
                      <form
                        action="{$WebApplicationBaseURL}{$subselect.webpage}{$HttpSession}?XSL.editor.session.id={$subselect.session}"
                        method="post">
                        <input type="submit" class="submit" value="Auswahl abbrechen" />
                      </form>
                    </center>
                  </p>
                </td>
              </tr>
              <tr>
                <td>
                  <p>
                    <ul>
                      <xsl:apply-templates select="results/*" />
                    </ul>
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </center>
  </xsl:template>

  <!-- ========== value ========== -->
  <xsl:template match="value">
    <li>
      <xsl:variable name="urldoc" select="concat('mcrobject:',idx)" />
      <xsl:variable name="fullname" select="document($urldoc)/mycoreobject/metadata/names/name/fullname" />
      <a href="{$url}&amp;_var_@href={idx}&amp;_var_@title={$fullname}">
        <xsl:value-of select="$fullname" />
      </a>
    </li>
  </xsl:template>

  <!-- ========== range ========== -->
  <xsl:template match="range">
    <li>
      <a class="sitemap"
        href="{$WebApplicationBaseURL}{/indexpage/@path}{from/@pos}-{to/@pos}/index.html{$HttpSession}">
        <xsl:value-of select="concat('Dokumente von ', from/@short,' bis ', to/@short)" />
      </a>
    </li>
  </xsl:template>

</xsl:stylesheet>
