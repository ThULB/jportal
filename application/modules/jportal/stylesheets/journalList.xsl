<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:mcr="http://www.mycore.org/">

    <xsl:include href="journalList-timeBar.xsl" />

    <xsl:param name="selected"/>
    
    <xsl:variable name="maxListObjectCount" select="'15'" />

    <!-- =================================================================================================== -->

    <xsl:template match="journalList[@mode='alphabetical'] | journallist[@mode='alphabetical']">

        <!-- get data -->
        <xsl:variable name="journalIDs">
            <xsl:call-template name="get.allJournalIDs" />
        </xsl:variable>
        <xsl:variable name="journalXMLs">
            <xsl:call-template name="get.journalXMLs">
                <xsl:with-param name="journalIDsIF" select="$journalIDs" />
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="objectCount" select="count(xalan:nodeset($journalXMLs)/journalXMLs/mycoreobject)" />

        <!-- do layout -->
        <xsl:choose>
          <xsl:when test="$objectCount > 0">
            <p>
              <xsl:value-of select="'Wählen sie eine Zeitschrift aus der folgenden Liste:'" />
            </p>
            <xsl:call-template name="journalList.doLayout">
                <xsl:with-param name="journalXMLsIF" select="$journalXMLs" />
                <xsl:with-param name="mode" select="'shortcut'" />
            </xsl:call-template>
            <xsl:call-template name="journalList.seperator" />
            <br />
            <br />
            <xsl:call-template name="journalList.doLayout">
                <xsl:with-param name="journalXMLsIF" select="$journalXMLs" />
                <xsl:with-param name="mode" select="'fully'" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <p>
              <b>
                <xsl:value-of select="i18n:translate('jportal.a-z.emptyList')"/>
              </b>
            </p>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.doLayout">
        <xsl:param name="journalXMLsIF" />
        <xsl:param name="mode" />
        
        <xsl:variable name="objectCount" select="count(xalan:nodeset($journalXMLsIF)/journalXMLs/mycoreobject)" />
        
        <xsl:variable name="firstTitle">
          <xsl:variable name="nameOfJournal" select="xalan:nodeset($journalXMLsIF)/journalXMLs/mycoreobject[position() = 1]" />
          <xsl:variable name="nameOfJournal.firstChar">
            <xsl:value-of select="substring($nameOfJournal,1,1)" />
          </xsl:variable>
          <xsl:call-template name="journalList.lowerCase">
            <xsl:with-param name="char" select="$nameOfJournal.firstChar" />
          </xsl:call-template>
        </xsl:variable>

        <xsl:for-each select="xalan:nodeset($journalXMLsIF)/journalXMLs/mycoreobject">
            <xsl:variable name="precTitle">
                <xsl:variable name="pos">
                    <xsl:call-template name="get.journalList.position" />
                </xsl:variable>
                <xsl:variable name="nameOfJournal">
                    <xsl:call-template name="get.journalList.a2z.nameOfJournal">
                        <xsl:with-param name="xml" select="xalan:nodeset($journalXMLsIF)/journalXMLs/mycoreobject[position() = $pos]" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="nameOfJournal.firstChar">
                    <xsl:value-of select="substring($nameOfJournal,1,1)" />
                </xsl:variable>
                <xsl:call-template name="journalList.lowerCase">
                    <xsl:with-param name="char" select="$nameOfJournal.firstChar" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="title">
                <xsl:variable name="title.nameOfJournal">
                    <xsl:call-template name="get.journalList.a2z.nameOfJournal">
                        <xsl:with-param name="xml" select="." />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="title.nameOfJournal.firstChar">
                    <xsl:value-of select="substring($title.nameOfJournal,1,1)" />
                </xsl:variable>
                <xsl:call-template name="journalList.lowerCase">
                    <xsl:with-param name="char" select="$title.nameOfJournal.firstChar" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
                <!-- The shortcuts on the top -->
                <xsl:when test="$mode = 'shortcut'">
                  <xsl:choose>
                    <!-- If 20 or more objects exist, each shortcut is a link -->
                    <xsl:when test="$objectCount > $maxListObjectCount" > 
                      <xsl:call-template name="journalList.doLayout.shortcuts">
                          <xsl:with-param name="prefixLabel" select="$precTitle = $title" />
                          <xsl:with-param name="titleIF" select="$title" />
                          <xsl:with-param name="wholeList" select="'true'" />
                      </xsl:call-template>
                    </xsl:when>
                    <!-- otherwise each shortcut is a # -->
                    <xsl:otherwise>
                      <xsl:call-template name="journalList.doLayout.shortcuts">
                            <xsl:with-param name="prefixLabel" select="$precTitle = $title" />
                            <xsl:with-param name="titleIF" select="$title" />
                            <xsl:with-param name="wholeList" select="'false'" />
                      </xsl:call-template>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                      <!-- If 20 or more objects exist, then seperate the list in its starting chars -->
                      <xsl:when test="$objectCount > $maxListObjectCount" >
                        <xsl:if test="($selected = $title) or ($selected = '' and $title = $firstTitle)" >
                          <xsl:call-template name="journalList.doLayout.journals">
                              <xsl:with-param name="prefixLabel" select="$precTitle = $title" />
                              <xsl:with-param name="titleIF" select="$title" />
                          </xsl:call-template>
                        </xsl:if>
                     </xsl:when>
                     <!-- otherwise display the whole list -->
                     <xsl:otherwise>
                       <xsl:call-template name="journalList.doLayout.journals">
                         <xsl:with-param name="prefixLabel" select="$precTitle = $title" />
                         <xsl:with-param name="titleIF" select="$title" />
                       </xsl:call-template>                       
                     </xsl:otherwise>
                   </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.journalList.a2z.nameOfJournal">
        <xsl:param name="xml" />
        <xsl:choose>
            <xsl:when test="xalan:nodeset($xml)/metadata/maintitlesForSorting/maintitleForSorting/text()">
                <xsl:value-of select="xalan:nodeset($xml)/metadata/maintitlesForSorting/maintitleForSorting/text()" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="xalan:nodeset($xml)/metadata/maintitles/maintitle/text()" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.journalList.position">
        <xsl:choose>
            <xsl:when test="position() = 1">
                <xsl:value-of select="1" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="position() - 1" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.doLayout.shortcuts">
        <xsl:param name="prefixLabel" />
        <xsl:param name="titleIF" />
        <xsl:param name="wholeList" />

        <xsl:if test="($prefixLabel = true) or (position() = 1)">
            <xsl:call-template name="journalList.seperator" />
            <xsl:variable name="title.upperCase">
                <xsl:call-template name="journalList.upperCase">
                    <xsl:with-param name="char" select="$titleIF" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
              <xsl:when test="$wholeList = 'true'" >
                <a href="?XSL.selected={$titleIF}">
                  <b>
                    <xsl:value-of select="$title.upperCase" />
                  </b>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <a href="{concat('#', $titleIF)}">
                  <b>
                    <xsl:value-of select="$title.upperCase" />
                  </b>
                </a>
              </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.doLayout.journals">
        <xsl:param name="prefixLabel" />
        <xsl:param name="titleIF" />

        <xsl:if test="($prefixLabel = true) or (position() = 1)">
            <xsl:variable name="labelUpperCase">
                <xsl:call-template name="journalList.upperCase">
                    <xsl:with-param name="char" select="$titleIF" />
                </xsl:call-template>
            </xsl:variable>
            <br />
            <b>
                <a name="{$titleIF}">
                    <b>
                        <xsl:value-of select="$labelUpperCase" />
                    </b>
                </a>
            </b>
            <br />
            <br />
        </xsl:if>
        <xsl:variable name="hit">
            <mcr:hit id="{@ID}" />
        </xsl:variable>

        <xsl:apply-templates select="xalan:nodeset($hit)/*">
            <xsl:with-param name="mcrobj" select="." />
        </xsl:apply-templates>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.journalXMLs">
        <xsl:param name="journalIDsIF" />
        <xsl:variable name="xmlsUnsorted">
            <xsl:element name="xmlsUnsorted">
                <xsl:for-each select="xalan:nodeset($journalIDsIF)/mcr:results/mcr:hit">
                    <xsl:copy-of select="document(concat('mcrobject:',@id))" />
                </xsl:for-each>
            </xsl:element>
        </xsl:variable>
        <xsl:element name="journalXMLs">
            <xsl:for-each select="xalan:nodeset($xmlsUnsorted)/xmlsUnsorted/mycoreobject">
                <xsl:sort select="./metadata/maintitlesForSorting/maintitleForSorting/text()" data-type="text" order="ascending" />
                <xsl:sort select="./metadata/maintitles/maintitle/text()" data-type="text" order="ascending" />
                <xsl:copy-of select="." />
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.seperator">
        <xsl:value-of select="'&#160;&#160;&#160;|&#160;&#160;&#160;'" />
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="get.allJournalIDs">
        <xsl:variable name="term">
            <xsl:value-of select="encoder:encode('((objectType = jpjournal) and (deletedFlag = false))')" />
        </xsl:variable>
        <xsl:variable name="queryURI">
            <xsl:value-of select="concat('query:term=',$term,'&amp;maxResults=0')" />
        </xsl:variable>
        <xsl:copy-of select="document($queryURI)" />
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.upperCase">
        <xsl:param name="char" />
        <xsl:value-of select="translate($char,'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ','ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
    </xsl:template>

    <!-- =================================================================================================== -->

    <xsl:template name="journalList.lowerCase">
        <xsl:param name="char" />
        <xsl:value-of select="translate($char,'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz')" />
    </xsl:template>

    <!-- =================================================================================================== -->

</xsl:stylesheet>
