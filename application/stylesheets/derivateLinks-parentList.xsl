<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink"
  xmlns:encoder="xalan://java.net.URLEncoder">
  
  <xsl:include href="MyCoReLayout.xsl" />
  <xsl:include href="objecttypes.xsl" />

  <xsl:variable name="PageTitle" select="'Derivat Verknüpfung - Eltern Objekte'" />

  <xsl:template match="derivateLinks-parentList">
    <!-- needed by iview servlet, no idea why -->
    <xsl:variable name="iviewSession" select="concat('&amp;XSL.MCRSessionID=', @session)" />
    <xsl:variable name="session" select="concat('&amp;XSL.subselect.session=', @session)" />
    <xsl:variable name="varpath" select="concat('&amp;XSL.subselect.varpath=', @varpath)" />
    <xsl:variable name="webpage" select="concat('&amp;XSL.subselect.webpage=', encoder:encode(@webpage))" />

    <xsl:variable name="xslVars" select="concat($iviewSession, $session, $varpath, $webpage)"/>

    <table id="parentDerivates">
      <th class="metahead">
        <xsl:value-of select="'MyCoRe Objekt'" />
      </th>
      <th class="metahead">
        <xsl:value-of select="'Derivat Liste'" />
      </th>
      <xsl:for-each select="mycoreobject">
        <tr>
          <td class="objectID">
            <a href="{$WebApplicationBaseURL}receive/{@id}" >
              <xsl:apply-templates
                       select="document(concat('mcrobject:',@id))/mycoreobject"
                       mode="resulttitle"/>
            </a>
          </td>
          <td class="derivates">
            <xsl:if test="derivate">
              <ul>
                <xsl:for-each select="derivate">
                  <li>
                    <xsl:variable name="hrefPath" select="concat('MCRFileNodeServlet/', @id, '/?XSL.Style=selectFile', $xslVars)" />
                    <a href="{$hrefPath}" >
                       <xsl:variable name="derLabel" select="document(concat('mcrobject:',@id))/mycorederivate/@label"/>
                       <xsl:choose>
                         <xsl:when test="starts-with($derLabel, 'data object')">
                           <xsl:value-of select="@id" />
                         </xsl:when>
                         <xsl:otherwise>
                           <xsl:value-of select="concat($derLabel, ' (', @id, ')')" />
                         </xsl:otherwise>
                       </xsl:choose>
                    </a>
                  </li>
                </xsl:for-each>
              </ul>
            </xsl:if>
          </td>
        </tr>
      </xsl:for-each>
    </table>

    <p>
      <form method="post">
        <xsl:attribute name="action">
          <xsl:value-of select="concat($WebApplicationBaseURL,@webpage)" />
          <xsl:if test="not(contains(@webpage,@session))">
            <xsl:value-of select="concat('XSL.editor.session.id=',@session)" />
          </xsl:if>
        </xsl:attribute>
        <input type="submit" class="submit" value="{i18n:translate('indexpage.sub.select.cancel')}" />
      </form>
    </p>

  </xsl:template>
</xsl:stylesheet>