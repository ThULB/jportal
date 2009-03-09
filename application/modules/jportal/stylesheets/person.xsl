<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mcr="http://www.mycore.org/"
    xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xlink mcr i18n acl" version="1.0">
    <xsl:include href="redundancyMap-coreFunctions.xsl" />
    <xsl:param select="'local'" name="objectHost" />
    <!--Template for result list hit: see results.xsl-->
    <xsl:template match="mcr:hit[contains(@id,'_person_')]">
        <xsl:param name="mcrobj" />
        <xsl:param name="mcrobjlink" />
        <xsl:variable select="100" name="DESCRIPTION_LENGTH" />
        <xsl:variable select="@host" name="host" />
        <xsl:variable name="obj_id">
            <xsl:value-of select="@id" />
        </xsl:variable>

        <xsl:variable name="cXML">
            <xsl:copy-of select="document(concat('mcrobject:',@id))" />
        </xsl:variable>
        <table cellspacing="0" cellpadding="0" id="leaf-all">
            <tr>
                <td id="leaf-front" rowspan="5">
                    <div>
                        <img src="{$WebApplicationBaseURL}images/person2.gif" />
                    </div>
                </td>
                <td id="leaf-linkarea">
                    <xsl:variable name="lastName_shorted">
                        <xsl:call-template name="ShortenText">
                            <xsl:with-param name="text" select="xalan:nodeset($cXML)/mycoreobject/metadata/def.heading/heading/lastName/text()" />
                            <xsl:with-param name="length" select="50" />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="firstName_shorted">
                        <xsl:call-template name="ShortenText">
                            <xsl:with-param name="text" select="xalan:nodeset($cXML)/mycoreobject/metadata/def.heading/heading/firstName/text()" />
                            <xsl:with-param name="length" select="50" />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="name">
                        <xsl:choose>
                            <xsl:when test="xalan:nodeset($cXML)/mycoreobject/metadata/def.dateOfDeath/dateOfDeath/text()">
                                <xsl:value-of select="concat('† ',$lastName_shorted,', ',$firstName_shorted)" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="concat($lastName_shorted,', ',$firstName_shorted)" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="date">
                        <xsl:choose>
                            <xsl:when test="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date[@inherited='0']">
                                <xsl:variable name="date">
                                    <xsl:value-of select="xalan:nodeset($cXML)/mycoreobject/metadata/dates/date/text()" />
                                </xsl:variable>
                                <xsl:value-of select="concat(' (',$date,')')" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="''" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="label">
                        <xsl:value-of select="concat($name,$date)" />
                    </xsl:variable>
                    <xsl:call-template name="objectLinking">
                        <xsl:with-param name="obj_id" select="@id" />
                        <xsl:with-param name="obj_name" select="$label" />
                        <xsl:with-param name="requestParam" select="'XSL.view.objectmetadata.SESSION=false&amp;XSL.toc.pos.SESSION=0'" />
                    </xsl:call-template>
                </td>
            </tr>
        </table>
        <table cellspacing="0" cellpadding="0">
            <tr id="leaf-whitespaces">
                <td colspan="2"></td>
            </tr>
        </table>
    </xsl:template>
    <!--Template for generated link names and result titles: see mycoreobject.xsl, results.xsl, MyCoReLayout.xsl-->
    <xsl:template priority="1" mode="resulttitle" match="/mycoreobject[contains(@ID,'_person_')]">
        <xsl:apply-templates select="." mode="title" />
    </xsl:template>
    <!--Template for title in metadata view: see mycoreobject.xsl-->
    <xsl:template priority="1" mode="title" match="/mycoreobject[contains(@ID,'_person_')]">
        <xsl:choose>
            <xsl:when test="./metadata/def.heading/heading">
                <xsl:choose>
                    <xsl:when test="./metadata/def.dateOfDeath/dateOfDeath/text()">
                        <xsl:value-of select="concat('† ',./metadata/def.heading/heading/lastName/text(),', ',./metadata/def.heading/heading/firstName/text())" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat(./metadata/def.heading/heading/lastName/text(),', ',./metadata/def.heading/heading/firstName/text())" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@ID" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--Template for metadata view: see mycoreobject.xsl-->
    <xsl:template priority="1" mode="present" match="/mycoreobject[contains(@ID,'_person_')]">
        <xsl:variable name="objectBaseURL">
            <xsl:if test="$objectHost != 'local'">
                <xsl:value-of select="document('webapp:hosts.xml')/mcr:hosts/mcr:host[@alias=$objectHost]/mcr:url[@type='object']/@href" />
            </xsl:if>
            <xsl:if test="$objectHost = 'local'">
                <xsl:value-of select="concat($WebApplicationBaseURL,'receive/')" />
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="staticURL">
            <xsl:value-of select="concat($objectBaseURL,@ID)" />
        </xsl:variable>
        <div id="detailed-frame">
            <table border="0" cellspacing="0">
                <tr>
                    <td id="detailed-cube">
                        <img src="{$WebApplicationBaseURL}images/person.gif" />
                    </td>
                    <td id="detailed-mainheadline">
                        <xsl:if test="/mycoreobject/metadata/def.dateOfDeath/dateOfDeath/text()">†</xsl:if>
                        <xsl:variable name="lastName_shorted">
                            <xsl:value-of select="./metadata/def.heading/heading/lastName/text()" />
                        </xsl:variable>
                        <xsl:variable name="firstName_shorted">
                            <xsl:value-of select="./metadata/def.heading/heading/firstName/text()" />
                        </xsl:variable>
                        <xsl:value-of select="concat($lastName_shorted,', ',$firstName_shorted)" />
                    </td>
                    <!--<td id="detailed-links" colspan="1" rowspan="3">
                        <table id="detailed-contenttable" border="0" cellspacing="0">
                        
                        <xsl:call-template name="printDerivates">
                        <xsl:with-param name="obj_id" select="@ID"/>
                        </xsl:call-template>
                        
                        </table>
                        </td>-->
                </tr>
                <tr>
                    <td colspan="2">
                        <!--1***heading*************************************-->
                        <!--<xsl:call-template name="printPersonName">
                            <xsl:with-param select="./metadata/def.heading/heading" name="nodes"/>
                            <xsl:with-param select="i18n:translate('metaData.person.heading')" name="label"/>
                            </xsl:call-template>-->
                        <!--2***alternative*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printPersonName">
                                <xsl:with-param select="./metadata/def.alternative/alternative" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.person.Lalternative')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--3***peerage*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/def.peerage/peerage" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.person.Lpeerage')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--3a*** gender  *************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/def.gender/gender" name="nodes" />
                                <xsl:with-param select="i18n:translate('metaData.author.gender')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--3b*** contact *************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDate_typeSensitive">
                                <xsl:with-param select="'right'" name="textalign" />
                                <xsl:with-param select="./metadata/def.contact/contact" name="nodes" />
                                <xsl:with-param select="i18n:translate('metaData.person.contact')" name="label" />
                                <xsl:with-param name="typeClassi" select="'urmel_class_002'" />
                                <xsl:with-param name="mode" select="'text'" />
                            </xsl:call-template>
                        </table>
                        <!--4***role*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/def.role/role" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.person.Lrole')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--5***placeOfActivity*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/def.placeOfActivity/placeOfActivity" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.person.LplaceOfActivity')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--6***dateOfBirth*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/def.dateOfBirth/dateOfBirth" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.person.LdateOfBirth')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--7***placeOfBirth*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/def.placeOfBirth/placeOfBirth" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.person.LplaceOfBirth')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--8***dateOfDeath*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/def.dateOfDeath/dateOfDeath" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.person.LdateOfDeath')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--9***placeOfDeath*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="printMetaDates">
                                <xsl:with-param select="./metadata/def.placeOfDeath/placeOfDeath" name="nodes" />
                                <xsl:with-param select="i18n:translate('editormask.person.LplaceOfDeath')" name="label" />
                            </xsl:call-template>
                        </table>
                        <!--10***note*************************************-->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:if test="$CurrentUser!='gast' or ./metadata/def.note/note[@type] = 'visible'">
                                <xsl:call-template name="printMetaDates">
                                    <xsl:with-param select="./metadata/def.note/note" name="nodes" />
                                    <xsl:with-param select="i18n:translate('editormask.labels.note')" name="label" />
                                </xsl:call-template>
                            </xsl:if>
                        </table>
                        <xsl:call-template name="getIdentifier"/>
                        <!-- linked articles-->
                        <xsl:call-template name="listLinkedArts" />

                        <!-- linked calendars-->
                        <xsl:call-template name="listLinkedCals" />

                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-divlines">
                            <tr>
                                <td colspan="2" id="detailed-innerdivlines">
                                    <br />
                                </td>
                            </tr>
                        </table>

                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <tr>
                                <td id="detailed-headlines">
                                    <xsl:value-of select="i18n:translate('metaData.headlines.systemdata')" />
                                </td>
                            </tr>
                        </table>
                        <!--*** List children per object type ************************************* -->
                        <!--
                            1.) get a list of objectTypes of all child elements
                            2.) remove duplicates from this list
                            3.) for-each objectTyp id list child elements
                        -->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:variable name="objectTypes">
                                <xsl:for-each select="./structure/children/child/@xlink:href">
                                    <id>
                                        <xsl:copy-of select="substring-before(substring-after(.,'_'),'_')" />
                                    </id>
                                </xsl:for-each>
                            </xsl:variable>
                            <xsl:variable select="xalan:nodeset($objectTypes)/id[not(.=following::id)]" name="unique-ids" />
                            <!--
                                the for-each would iterate over <id> with root not beeing /mycoreobject
                                so we save the current node in variable context to access needed nodes
                            -->
                            <xsl:variable select="." name="context" />
                            <xsl:for-each select="$unique-ids">
                                <xsl:variable select="." name="thisObjectType" />
                                <xsl:variable name="label">
                                    <xsl:choose>
                                        <xsl:when test="count($context/structure/children/child[contains(@xlink:href,$thisObjectType)])=1">
                                            <xsl:value-of select="i18n:translate(concat('metaData.',$thisObjectType,'.[singular]'))" />
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="i18n:translate(concat('metaData.',$thisObjectType,'.[plural]'))" />
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:variable>
                                <xsl:call-template name="printMetaDates">
                                    <xsl:with-param select="$context/structure/children/child[contains(@xlink:href, concat('_',$thisObjectType,'_'))]"
                                        name="nodes" />
                                    <xsl:with-param select="$label" name="label" />
                                </xsl:call-template>
                            </xsl:for-each>
                        </table>
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-divlines">
                            <tr>
                                <td colspan="2" id="detailed-innerdivlines">
                                    <br />
                                </td>
                            </tr>
                        </table>
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <tr>
                                <td id="detailed-headlines">
                                    <xsl:value-of select="i18n:translate('metaData.headlines.systemdata')" />
                                </td>
                                <td>
                                    <br />
                                </td>
                            </tr>
                        </table>              
                        <xsl:call-template name="get.systemData"/>
                        
                        <!-- Static URL ************************************************** -->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                            <xsl:call-template name="get.staticURL">
                                <xsl:with-param name="stURL" select="$staticURL" />
                            </xsl:call-template>
                            <xsl:call-template name="emptyRow" />
                        </table>
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                        <!--13***identifier*************************************-->
      
                        
                        </table>
                        <!--*** Editor Buttons ************************************* -->
                        <table border="0" cellspacing="0" cellpadding="0" id="detailed-view">
                          <xsl:call-template name="editobject">
                            <xsl:with-param select="'false'" name="accessdelete" />
                            <xsl:with-param select="./@ID" name="id" />
                          </xsl:call-template>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>
    
    <xsl:template name="getIdentifier">
      <xsl:if test="./metadata/def.identifier/identifier[@type='pnd']">
        <table>
            <tr>
                <td id="detailed-labels">
                    <xsl:value-of select="concat(i18n:translate('metaData.person.identifier.pnd'),' :')" />
                </td>
                <td class="metavalue">
                    <xsl:value-of select="./metadata/def.identifier/identifier[@type='pnd']" />

                    <xsl:value-of select="' ('" />
                    <a class="external" href="{concat('http://dispatch.opac.ddb.de/DB=4.1/PPN?PPN=',./metadata/def.identifier/identifier[@type='pnd'])}">
                        <xsl:value-of select="i18n:translate('metaData.person.lookUp')" />
                    </a>
                    <xsl:value-of select="', '" />
                    <a class="external" href="{concat('http://dispatch.opac.ddb.de/DB=4.1/SET=6/TTL=1/PRS=PP%7F/PPN?PPN=',./metadata/def.identifier/identifier[@type='pnd'])}">
                        <xsl:value-of select="i18n:translate('metaData.person.lookUp.raw')" />
                    </a>
                    <xsl:value-of select="', '" />

                    <a class="external" href="{concat('http://dispatch.opac.ddb.de/REL?PPN=',./metadata/def.identifier/identifier[@type='pnd'])}">
                        <xsl:value-of select="i18n:translate('metaData.person.lookUp.rel')" />
                    </a>
                    <xsl:value-of select="')'" />
                </td>
            </tr>
        </table>
      </xsl:if>
            
      <!--14***identifier*************************************-->
      <xsl:if test="./metadata/def.identifier/identifier[@type='ppn']">
        <table>
            <tr>
                <td id="detailed-labels">
                    <xsl:value-of select="concat(i18n:translate('metaData.person.identifier.ppn'),' :')" />
                </td>
                <td class="metavalue">
                    <xsl:value-of select="./metadata/def.identifier/identifier[@type='ppn']" />
                    <xsl:value-of select="' ('" />
                    <a class="external" href="{concat('https://kataloge.thulb.uni-jena.de/DB=1/SET=2/TTL=1/PPN?PPN=',./metadata/def.identifier/identifier[@type='ppn'])}">
                        <xsl:value-of select="i18n:translate('metaData.person.lookUp')" />

                    </a>
                    <xsl:value-of select="', '" />
                    <a class="external"
                        href="{concat('https://kataloge.thulb.uni-jena.de/DB=1/SET=2/TTL=1/PRS=PP%7F/PPN?PPN=',./metadata/def.identifier/identifier[@type='ppn'])}">
                        <xsl:value-of select="i18n:translate('metaData.person.lookUp.raw')" />
                    </a>
                    <xsl:value-of select="', '" />
                    <a class="external" href="{concat('https://kataloge.thulb.uni-jena.de/DB=1/SET=2/TTL=1/REL?PPN=',./metadata/def.identifier/identifier[@type='ppn'])}">
                        <xsl:value-of select="i18n:translate('metaData.person.lookUp.rel')" />
                    </a>

                    <xsl:value-of select="')'" />
                </td>
            </tr>
        </table>
      </xsl:if>
      <!--12***link*************************************-->
      <xsl:if test="./metadata/def.link/link/@xlink:href">
        <table>
            <tr>
                <td id="detailed-labels">
                    <xsl:value-of select="concat(i18n:translate('metaData.person.link'),' :')" />

                </td>
                <td class="metavalue">
                    <xsl:for-each select="./metadata/def.link/link/@xlink:href">
                        <a href="." class="external">
                            <xsl:value-of select="." />
                        </a>
                        <xsl:if test="position() != last()">
                            <br />
                        </xsl:if>

                    </xsl:for-each>
                </td>
            </tr>
        </table>
      </xsl:if>
    </xsl:template>

    <xsl:template name="printPersonName">
        <xsl:param name="nodes" />
        <xsl:param name="label" select="local-name($nodes[1])" />
        <xsl:if test="$nodes">
            <tr>
                <td id="detailed-labels">
                    <xsl:value-of select="$label" />
                </td>
                <td class="metavalue">
                    <xsl:choose>
                        <xsl:when test="count($nodes)>1">
                            <ul>
                                <xsl:for-each select="$nodes">
                                    <li>
                                        <xsl:apply-templates select="." />
                                    </li>
                                </xsl:for-each>
                            </ul>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates select="$nodes" />
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*[lastName | name]">
        <xsl:choose>
            <xsl:when test="lastName and firstName">
                <xsl:value-of select="concat(lastName,', ',firstName)" />
            </xsl:when>
            <xsl:when test="lastName">
                <xsl:value-of select="lastName" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="name" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="*[../@class='MCRMetaISO8601Date']">
        <xsl:variable name="format">
            <xsl:choose>
                <xsl:when test="string-length(normalize-space(.))=4">
                    <xsl:value-of select="i18n:translate('metaData.dateYear')" />
                </xsl:when>
                <xsl:when test="string-length(normalize-space(.))=7">
                    <xsl:value-of select="i18n:translate('metaData.dateYearMonth')" />
                </xsl:when>
                <xsl:when test="string-length(normalize-space(.))=10">
                    <xsl:value-of select="i18n:translate('metaData.dateYearMonthDay')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="i18n:translate('metaData.dateTime')" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:call-template name="formatISODate">
            <xsl:with-param name="date" select="." />
            <xsl:with-param name="format" select="$format" />
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
