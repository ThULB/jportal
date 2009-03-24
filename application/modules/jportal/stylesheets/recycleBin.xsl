<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/" >
    
    <xsl:param name="linkedList" />
    <xsl:param name="nonLinkedList" />
    <xsl:param name="errorList" />

    <xsl:variable name="ServletName" select="'MCRRecycleBinServlet'" />
    
    <!-- =================================================================================================== -->
    <xsl:template match="recycleBin">
        <!-- load js -->
        <xsl:call-template name="recycleBinJS" />

        <!-- get data -->
        <xsl:variable name="recycleIDs">
          <xsl:call-template name="get.allRecycleBinIDs" />
        </xsl:variable>
        <xsl:variable name="recycleXMLs">
          <xsl:call-template name="get.recycleBinXMLs">
            <xsl:with-param name="recycleIDsIF" select="$recycleIDs" />
          </xsl:call-template>
        </xsl:variable>
 
        <xsl:variable name="recycleDerivateIDs">
          <xsl:call-template name="get.allRecycleBinDerivateIDs" />
        </xsl:variable>
        <xsl:variable name="recycleDerivatesXMLs">
          <xsl:call-template name="get.recycleBinDerivatesXMLs">
            <xsl:with-param name="recycleIDsIF" select="$recycleDerivateIDs" />
          </xsl:call-template>
        </xsl:variable>

        <!-- do layout -->
        <xsl:choose>
          <!-- test if recycle bin is empty -->
          <xsl:when test="xalan:nodeset($recycleXMLs)/recycleXMLs/mycoreobject[position() = 1] or
          xalan:nodeset($recycleDerivatesXMLs)/recycleXMLs/mycorederivate[position() = 1] " >
            <form id="recycleBin" action="{$ServletsBaseURL}{$ServletName}" method="post">
              <table width="75%">
                <th width="5%"></th>
                <th width="30%" align="left"><xsl:value-of select="'Objekt'" /></th>
                <th width="15%" align="left"><xsl:value-of select="'Typ'" /></th>
                <th width="30%" align="left"><xsl:value-of select="'gelöscht am'" /></th>
                <th align="left"><xsl:value-of select="'gelöscht von'" /></th>
                <xsl:call-template name="recycleBin.printObjects">
                    <xsl:with-param name="recycleXMLsIF" select="$recycleXMLs" />
                </xsl:call-template>
                 <xsl:call-template name="recycleBin.printDerivates">
                    <xsl:with-param name="recycleXMLsIF" select="$recycleDerivatesXMLs" />
                </xsl:call-template>
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

    <xsl:template name="recycleBin.printObjects">
      <xsl:param name="recycleXMLsIF" />

      <xsl:for-each select="xalan:nodeset($recycleXMLsIF)/recycleXMLs/mycoreobject">
        <xsl:call-template name="printEntry" />
      </xsl:for-each>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="recycleBin.printDerivates">
      <xsl:param name="recycleXMLsIF" />
        <xsl:for-each select="xalan:nodeset($recycleXMLsIF)/recycleXMLs/mycorederivate">
          <xsl:call-template name="printEntry" />
        </xsl:for-each>
    </xsl:template>

    <!-- =================================================================================================== -->
    <xsl:template name="printEntry">
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

    <xsl:template name="get.recycleBinDerivatesXMLs">
        <xsl:param name="recycleIDsIF" />

        <!-- creates a string list of derivates separated by a comma -->
        <xsl:variable name="stringList">        
          <xsl:for-each select="xalan:nodeset($recycleIDsIF)/mcr:results/mcr:hit/mcr:metaData/mcr:field[@name = 'DerivateID']">
            <xsl:value-of select="text()" />
            <xsl:if test="position() != last()">
              <xsl:value-of select="','" />
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>

        <!-- loads the documents from the string list -->
        <xsl:variable name="xmlsUnsorted">
          <xsl:element name="xmlsUnsorted">
            <xsl:call-template name="recycleBin.createXMLFromStringList">
              <xsl:with-param name="list" select="$stringList"/>
              <xsl:with-param name="sep" select="','"/>
            </xsl:call-template>
          </xsl:element>
        </xsl:variable>

        <xsl:element name="recycleXMLs">
            <xsl:for-each select="xalan:nodeset($xmlsUnsorted)/xmlsUnsorted/mycorederivate">
              <xsl:sort select="@ID"  data-type="text" order="ascending" />
              <xsl:copy-of select="." />
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <!-- =================================================================================================== -->
 
    <!-- loads all objects from a string list. redundancy elements will be only loaded
    one time. Example: a,a,b,c,d,d,d -> loads the elements a,b,c,d -->
    <xsl:template name="recycleBin.createXMLFromStringList">
      <xsl:param name="list" />
      <xsl:param name="sep" />

      <xsl:variable name="first" select="substring-before($list, $sep)" />
      <xsl:variable name="listAfter" select="substring-after($list, $sep)" />

      <xsl:if test="$listAfter != ''">
        <xsl:variable name="next" select="substring-before($listAfter, $sep)" />
        <xsl:choose>
          <xsl:when test="($next = '' and $listAfter != $first) or ($next != '' and $first != $next)">
            <xsl:copy-of select="document(concat('mcrobject:',$first))" />
          </xsl:when>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="contains($listAfter, $sep)">
            <xsl:call-template name="recycleBin.createXMLFromStringList">
              <xsl:with-param name="list" select="$listAfter" />
              <xsl:with-param name="sep" select="$sep"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="document(concat('mcrobject:',$listAfter))" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
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
            <!-- do the default query, not needed to append deletedFlag or fileDeleted -->
            <xsl:value-of select="concat('query:term=',$term,'&amp;maxResults=0')" />
        </xsl:variable>
        <xsl:copy-of select="document($queryURI)" />
    </xsl:template>
    
    <!-- =================================================================================================== -->

    <xsl:template name="get.allRecycleBinDerivateIDs">
        <xsl:variable name="term">
            <xsl:value-of select="encoder:encode('(fileDeleted = true)')" />
        </xsl:variable>
        <xsl:variable name="queryURI">
            <!-- do the default query, not needed to append deletedFlag or fileDeleted -->
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
          <xsl:when test="$objectID = 'jportal_derivate'"><xsl:value-of select="'Derivat'"/></xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'Unbekannt'"/>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- =================================================================================================== -->
    <!-- =========== special page for deleted objects ============ -->
    <!-- =================================================================================================== -->
     <xsl:template match="recycleBinDeletedPage">
      <xsl:if test="$nonLinkedList != ''">
        <p>
          <strong><xsl:value-of select="'Gelöschte Objekte:'" /></strong>
          <br />
          <xsl:call-template name="printStringList">
            <xsl:with-param name="list" select="$nonLinkedList"/>
            <xsl:with-param name="sep" select="','"/>
          </xsl:call-template>
        </p>
      </xsl:if>
      <xsl:if test="$linkedList != ''">
        <p>
          <span style="font-weight: bolder; color:#992222;">
            <xsl:value-of select="'nicht gelöschte Objekte (noch verlinkt):'" />
          </span>
          <br />
          <xsl:call-template name="printStringList">
            <xsl:with-param name="list" select="$linkedList"/>
            <xsl:with-param name="sep" select="','"/>
          </xsl:call-template>
        </p>
      </xsl:if>
      <xsl:if test="$errorList != ''">
        <p>
          <span style="font-weight: bolder; color:#ff0000;">
            <xsl:value-of select="'nicht gelöschte Objekte (Exception aufgetreten!):'" />
          </span>
          <br />
          <xsl:call-template name="printStringList">
            <xsl:with-param name="list" select="$errorList"/>
            <xsl:with-param name="sep" select="','"/>
          </xsl:call-template>
        </p>
      </xsl:if>
      <p>
        <a href="{$WebApplicationBaseURL}/content/main/recyclebin.xml">zurück zum Papierkorb</a>
      </p>
    </xsl:template>

   <!-- =========== prints recursive a string list ========== -->    
    <xsl:template name="printStringList">
      <xsl:param name="list"/>
      <xsl:param name="sep" />

      <xsl:call-template name="printFirstElementInList">
        <xsl:with-param name="list" select="$list"/>
        <xsl:with-param name="sep" select="$sep"/>
      </xsl:call-template>
      <br />
      <xsl:variable name="nextEntry" select="substring-after($list, $sep)" />
      <xsl:if test="$nextEntry != ''">
        <xsl:call-template name="printStringList">
        <xsl:with-param name="list" select="$nextEntry"/>
        <xsl:with-param name="sep" select="$sep"/>
      </xsl:call-template>
      </xsl:if>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="printFirstElementInList">
      <xsl:param name="list"/>
      <xsl:param name="sep" />
      <xsl:choose>
        <xsl:when test="contains($list, $sep)">
          <xsl:value-of select="substring-before($list, $sep)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$list" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
</xsl:stylesheet>