<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/" >
    <xsl:variable name="ServletName" select="'MCRRecycleBinServlet'" />
    
    <!-- =================================================================================================== -->
    <xsl:template match="recycleBin">
        <!-- get data -->
        <xsl:variable name="recycleIDs">
            <xsl:call-template name="get.allRecycleBinIDs" />
        </xsl:variable>
        <xsl:variable name="recycleXMLs">
            <xsl:call-template name="get.recycleBinXMLs">
                <xsl:with-param name="recycleIDsIF" select="$recycleIDs" />
            </xsl:call-template>
        </xsl:variable>

        <!-- do layout -->
        <xsl:choose>
          <!-- test if recycle bin is empty -->
          <xsl:when test="xalan:nodeset($recycleXMLs)/recycleXMLs/mycoreobject[position() = 1]" >
            <form id="recycleBin" action="{$ServletsBaseURL}{$ServletName}" method="post">
              <table width="75%">
                <th width="5%"></th>
                <th width="30%" align="left"><xsl:value-of select="'Objekt'" /></th>
                <th width="15%" align="left"><xsl:value-of select="'Typ'" /></th>
                <th width="30%" align="left"><xsl:value-of select="'gelöscht am'" /></th>
                <th align="left"><xsl:value-of select="'gelöscht von'" /></th>
                <xsl:call-template name="recycleBin.doLayout">
                    <xsl:with-param name="recycleXMLsIF" select="$recycleXMLs" />
                </xsl:call-template>
              </table>
              <br />
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

    <xsl:template name="recycleBin.doLayout">
      <xsl:param name="recycleXMLsIF" />

      <xsl:for-each select="xalan:nodeset($recycleXMLsIF)/recycleXMLs/mycoreobject">        
        <tr>
          <!-- Checkbox -->
          <td>
            <input type="checkbox" name="{concat('cb_', @ID)}" />
          </td>
          <!-- ID -->
          <td>
            <a href="{$WebApplicationBaseURL}receive/{@ID}{$HttpSession}" >
              <xsl:value-of select="@ID" />
            </a>
          </td>
          <!-- Type -->
          <td>
            <xsl:variable name="type">
              <xsl:variable name="nameOfObject" select="@ID" />
              <xsl:call-template name="recycleList.cutID">
                <xsl:with-param name="fullID" select="$nameOfObject" />
              </xsl:call-template>
            </xsl:variable>
            <xsl:call-template name="recylceList.getNameByID">
                <xsl:with-param name="objectID" select="$type" />
            </xsl:call-template>        
          </td>
          <!-- Date -->
          <td>
            <xsl:value-of select="./service/servdates/servdate[@type='modifydate']" />
          </td>
          <!-- deleted from -->
          <td>
            <xsl:value-of select="./service/servflags/servflag[@type='deletedFrom']/text()" />
          </td>
        </tr>
      </xsl:for-each>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.recycleBinXMLs">
        <xsl:param name="recycleIDsIF" />
        <xsl:variable name="xmlsUnsorted">
             <xsl:element name="xmlsUnsorted">
                <xsl:for-each select="xalan:nodeset($recycleIDsIF)/mcr:results/mcr:hit">
                  <xsl:copy-of select="document(concat('mcrobject:',@id))" />
                </xsl:for-each>
            </xsl:element>
        </xsl:variable>
        <xsl:element name="recycleXMLs">
            <xsl:for-each select="xalan:nodeset($xmlsUnsorted)/xmlsUnsorted/mycoreobject">
              <xsl:sort select="@ID"  data-type="text" order="ascending" />
              <xsl:copy-of select="." />
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="recycleList.cutID">
        <xsl:param name="fullID" />
        <xsl:variable name="temp" select="substring-after($fullID, '_')"/>
        <xsl:variable name="newID" select="substring-before($temp, '_')"/>
        <xsl:value-of select="concat('jportal_', $newID)"/>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.allRecycleBinIDs">
        <xsl:variable name="term">
            <xsl:value-of select="encoder:encode('(deletedFlag = true)')" />
        </xsl:variable>
        <xsl:variable name="queryURI">
            <xsl:value-of select="concat('query:term=',$term,'&amp;maxResults=0')" />
        </xsl:variable>
        <xsl:copy-of select="document($queryURI)" />
    </xsl:template>

    <!-- =================================================================================================== -->
    <xsl:template name="recylceList.getNameByID">
        <xsl:param name="objectID" />
        <xsl:choose>
          <xsl:when test="$objectID = 'jportal_jpinst'"><xsl:value-of select="'Institution'"/></xsl:when>
          <xsl:when test="$objectID = 'jportal_jpjournal'"><xsl:value-of select="'Zeitschrift'"/></xsl:when>
          <xsl:when test="$objectID = 'jportal_person'"><xsl:value-of select="'Person'"/></xsl:when>
          <xsl:when test="$objectID = 'jportal_jpvolume'"><xsl:value-of select="'Band'"/></xsl:when>
          <xsl:when test="$objectID = 'jportal_jparticle'"><xsl:value-of select="'Artikel'"/></xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'Unbekannt'"/>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>