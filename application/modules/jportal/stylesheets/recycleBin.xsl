<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/" >

    <xsl:include href="MyCoReLayout.xsl" />
    <xsl:include href="objecttypes.xsl" />

    <xsl:variable name="ServletName" select="'MCRRecycleBinServlet'" />
    <xsl:variable name="PageTitle" select="'Papierkorb'" />

    <!-- =================================================================================================== -->
    <xsl:template match="recycleBin">
        <!-- load js -->
        <xsl:call-template name="recycleBinJS" />

        <!-- do layout -->
        <xsl:choose>
          <!-- test if recycle bin is empty -->
          <xsl:when test="count(entries/entry) != 0" >
            <form id="recycleBin" action="{$ServletsBaseURL}{$ServletName}" method="post">
              <table width="75%">
                <th width="5%"></th>
                <th width="30%" align="left"><xsl:value-of select="'Objekt'" /></th>
                <th width="15%" align="left"><xsl:value-of select="'Typ'" /></th>
                <th width="30%" align="left"><xsl:value-of select="'gelöscht am'" /></th>
                <th align="left"><xsl:value-of select="'gelöscht von'" /></th>
                
                <xsl:for-each select="entries/entry">
                  <xsl:call-template name="printEntry" />
                </xsl:for-each>
              </table>
              <br />
              <span style="padding-right:24px">
                <input type="button" value="select all" onclick="selectAll();" />
              </span>
              <span style="padding-right:8px">
                <input type="submit" name="submit" value="Delete" />
              </span>
              <span>
                <input type="submit" name="submit" value="Restore" />
              </span>
            </form>
          </xsl:when>
          <xsl:otherwise>
            <b>
              <xsl:value-of select="'Der Papierkorb ist leer.'" />
            </b>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="recycleBinJS">
      <script type="text/javascript">
        function selectAll() {
            var el = document.forms['recycleBin'].elements;
            var i = 0;
            while(i != el.length) {
                if(el[i].type.toLowerCase() == 'checkbox')
                  el[i].checked = true;
                i++;
            }
        }
      </script>
    </xsl:template>

    <!-- =================================================================================================== -->
    <xsl:template name="printEntry">
        <tr>
          <!-- Checkbox -->
          <td>
            <input type="checkbox" name="{concat('cb_', @id)}" />
          </td>
          <!-- ID -->
          <td>
            <a href="{$WebApplicationBaseURL}receive/{@id}{$HttpSession}" >
              <xsl:value-of select="@id" />
            </a>
          </td>
          <!-- Type -->
          <td>
            <xsl:call-template name="recylceList.getNameByID">
                <xsl:with-param name="objectID" select="@type" />
            </xsl:call-template>        
          </td>
          <!-- Date -->
          <td>
            <xsl:value-of select="@deletedAt" />
          </td>
          <!-- deleted from -->
          <td>
            <xsl:value-of select="@deletedFrom" />
          </td>
        </tr>
    </xsl:template>

    <!-- =================================================================================================== -->
    <xsl:template name="recylceList.getNameByID">
        <xsl:param name="objectID" />
        <xsl:choose>
          <xsl:when test="$objectID = 'jpinst'"><xsl:value-of select="'Institution'"/></xsl:when>
          <xsl:when test="$objectID = 'jpjournal'"><xsl:value-of select="'Zeitschrift'"/></xsl:when>
          <xsl:when test="$objectID = 'person'"><xsl:value-of select="'Person'"/></xsl:when>
          <xsl:when test="$objectID = 'jpvolume'"><xsl:value-of select="'Band'"/></xsl:when>
          <xsl:when test="$objectID = 'jparticle'"><xsl:value-of select="'Artikel'"/></xsl:when>
          <xsl:when test="$objectID = 'derivate'"><xsl:value-of select="'Derivat'"/></xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'Unbekannt'"/>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>