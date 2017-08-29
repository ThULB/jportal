<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ================================================ -->
<!-- This stylesheet generates the response to an OAI -->
<!-- harvester. -->
<!-- ================================================ -->
<xsl:stylesheet version="1.0" xmlns:mcr="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:urn="http://www.ddb.de/standards/urn" xmlns="http://www.openarchives.org/OAI/2.0/"
                xmlns:encoder="xalan://java.net.URLEncoder" exclude-result-prefixes="encoder mcr">

  <xsl:param name="ServletsBaseURL" select="''" />
  <xsl:param name="JSessionID" select="''" />
  <xsl:param name="WebApplicationBaseURL" select="''" />

  <xsl:template match="mycorederivate | mycoreobject">
    <xsl:comment>
      Start match="mycorederivate | mycoreobject (epicur.xsl)
    </xsl:comment>
    <xsl:if test="mcr:exists(@ID) = 'true'">
      <xsl:element name="epicur" namespace="urn:nbn:de:1111-2004033116">
        <xsl:attribute name="xsi:schemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance">
          <xsl:value-of select="'urn:nbn:de:1111-2004033116 http://www.persistent-identifier.de/xepicur/version1.0/xepicur.xsd'" />
        </xsl:attribute>
        <xsl:variable name="epicurType" select="'urn_new'" />
        <xsl:call-template name="administrative_data">
          <xsl:with-param name="epicurType" select="$epicurType" />
        </xsl:call-template>
        <xsl:call-template name="recordDerivateObject">
          <xsl:with-param name="epicurType" select="$epicurType" />
        </xsl:call-template>
      </xsl:element>
    </xsl:if>
    <xsl:comment>
      End match="mycorederivate | mycoreobject (epicur.xsl)
    </xsl:comment>
  </xsl:template>

  <xsl:template name="administrative_data">
    <xsl:param name="epicurType" select="''" />
    <xsl:element name="administrative_data" namespace="urn:nbn:de:1111-2004033116">
      <xsl:element name="delivery" namespace="urn:nbn:de:1111-2004033116">
        <xsl:element name="update_status" namespace="urn:nbn:de:1111-2004033116">
          <xsl:attribute name="type"><xsl:value-of select="$epicurType" /></xsl:attribute>
        </xsl:element>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template name="recordMCRObject">
    <xsl:param name="epicurType" select="''" />
    <xsl:variable name="mycoreobjectID" select="@ID" />
    <xsl:element name="record" namespace="urn:nbn:de:1111-2004033116">
      <!-- ################# -->
      <!-- URN der Metadaten -->
      <!-- ################# -->
      <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
        <xsl:attribute name="scheme">urn:nbn:de</xsl:attribute>
        <xsl:value-of select="./metadata/def.identifier/identifier[@type='urn']" />
      </xsl:element>
      <xsl:if test="$epicurType='urn_new_version'">
        <xsl:element name="isVersionOf" namespace="urn:nbn:de:1111-2004033116">
          <xsl:attribute name="scheme">urn:nbn:de</xsl:attribute>
          <xsl:value-of select="./metadata/def.identifier/identifier[@type='urn']" />
        </xsl:element>
      </xsl:if>
      <!-- ##################### -->
      <!-- Link zu den Metadaten -->
      <!-- ##################### -->
      <xsl:element name="resource" namespace="urn:nbn:de:1111-2004033116">
        <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
          <xsl:attribute name="scheme">url</xsl:attribute>
          <xsl:attribute name="role">primary</xsl:attribute>
          <xsl:attribute name="origin">original</xsl:attribute>
          <xsl:attribute name="type">frontpage</xsl:attribute>
          <xsl:if test="$epicurType = 'urn_new'">
            <xsl:attribute name="status">new</xsl:attribute>
          </xsl:if>
          <xsl:value-of select="concat($WebApplicationBaseURL,'receive/', $mycoreobjectID)" />
        </xsl:element>
        <xsl:element name="format" namespace="urn:nbn:de:1111-2004033116">
          <xsl:attribute name="scheme">imt</xsl:attribute>
          <xsl:value-of select="'text/html'" />
        </xsl:element>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template name="recordDerivateObject">
    <xsl:param name="epicurType" select="''" />
    <xsl:variable name="parentObjectID" select="./derivate/linkmetas/linkmeta/@xlink:href" />
    <xsl:variable name="mainURN" select="./derivate/fileset/@urn"/>
    <xsl:variable name="derivateID" select="@ID"/>
    
    <xsl:element name="record" namespace="urn:nbn:de:1111-2004033116">
      <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
        <xsl:attribute name="scheme">urn:nbn:de</xsl:attribute>
        <xsl:value-of select="$mainURN" />
      </xsl:element>
      <xsl:if test="$epicurType='urn_new_version'">
        <xsl:element name="isVersionOf" namespace="urn:nbn:de:1111-2004033116">
          <xsl:attribute name="scheme">urn:nbn:de</xsl:attribute>
          <xsl:value-of select="./derivate/fileset/@urn" />
        </xsl:element>
      </xsl:if>
      <!-- ################## -->
      <!-- URN/URL des Werkes -->
      <!-- ################## -->
      <xsl:element name="resource" namespace="urn:nbn:de:1111-2004033116">
        <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
          <xsl:attribute name="scheme">url</xsl:attribute>
          <xsl:attribute name="role">primary</xsl:attribute>
          <xsl:attribute name="origin">original</xsl:attribute>
          <xsl:attribute name="type">frontpage</xsl:attribute>
          <xsl:if test="$epicurType = 'urn_new'">
            <xsl:attribute name="status">new</xsl:attribute>
          </xsl:if>
          <xsl:variable name="startImage" select="./derivate/internals/internal/@maindoc" />
          <xsl:value-of
            select="concat($WebApplicationBaseURL,'receive/',$parentObjectID,'?jumpback=true&amp;maximized=true&amp;page=',$startImage)" />
        </xsl:element>
        <xsl:element name="format" namespace="urn:nbn:de:1111-2004033116">
          <xsl:attribute name="scheme">imt</xsl:attribute>
          <xsl:value-of select="'text/html'" />
        </xsl:element>
      </xsl:element>

      <!-- ############################ -->
      <!-- URN/URL der einzelnen Seiten -->
      <!-- ############################ -->
      <xsl:variable name="toAppend" select="'?jumpback=true&amp;maximized=true&amp;page='" />
      <xsl:for-each select="./derivate/fileset/file">
        <!-- include a urn pointing to the dfg viewer -->
        <xsl:if test="position() = 1">
          <xsl:element name="isPartOf" namespace="urn:nbn:de:1111-2004033116">
            <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
              <xsl:attribute name="scheme">urn:nbn:de</xsl:attribute>
              <xsl:value-of select="mcr:createAlternativeURN($mainURN,'dfg')" />
            </xsl:element>
            <xsl:element name="resource" namespace="urn:nbn:de:1111-2004033116">
              <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
                <xsl:attribute name="scheme">url</xsl:attribute>
                <xsl:value-of
                  select="concat('http://dfg-viewer.de/demo/viewer/?set[mets]=', encoder:encode(concat($WebApplicationBaseURL,'servlets/MCRMETSServlet/',$derivateID,'?XSL.Style=dfg')))" />
              </xsl:element>
              <xsl:element name="format" namespace="urn:nbn:de:1111-2004033116">
                <xsl:attribute name="scheme">imt</xsl:attribute>
                <xsl:value-of select="'text/html'" />
              </xsl:element>
            </xsl:element>
          </xsl:element>
        </xsl:if>

        <xsl:element name="isPartOf" namespace="urn:nbn:de:1111-2004033116">
          <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
            <xsl:attribute name="scheme">urn:nbn:de</xsl:attribute>
            <xsl:value-of select="urn" />
          </xsl:element>
          <xsl:element name="resource" namespace="urn:nbn:de:1111-2004033116">
            <xsl:element name="identifier" namespace="urn:nbn:de:1111-2004033116">
              <xsl:attribute name="scheme">url</xsl:attribute>
              <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',$parentObjectID,$toAppend,@name)" />
            </xsl:element>
            <xsl:element name="format" namespace="urn:nbn:de:1111-2004033116">
              <xsl:attribute name="scheme">imt</xsl:attribute>
              <xsl:value-of select="'text/html'" />
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*">
    <xsl:copy>
      <xsl:for-each select="@*">
        <xsl:copy />
      </xsl:for-each>
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>


</xsl:stylesheet>