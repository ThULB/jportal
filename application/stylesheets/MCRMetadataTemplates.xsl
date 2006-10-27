<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2004/05/10 08:56:01 $ -->
<!-- ============================================== -->

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsd='http://www.w3.org/2001/XMLSchema'
  version="1.0">

<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

<xsl:param name="mycore_home"/>
<xsl:param name="mycore_appl"/>

<xsl:variable name="newline">
 <xsl:text>
 </xsl:text>
</xsl:variable>

<!-- Template for the application dependence metadata -->

<!-- Template for the metadata MCRMetaInstitutionName -->

<xsl:template match="mcrmetainstitutionname">
<xsd:sequence>
 <xsd:element name="{@name}" minOccurs="{@minOccurs}" maxOccurs="{@maxOccurs}">
  <xsd:complexType>
   <xsd:all>
    <xsd:element name="fullname" type="xsd:string" minOccurs='1'
     maxOccurs='1'/>
    <xsd:element name="nickname" type="xsd:string" minOccurs='0'
     maxOccurs='1'/>
    <xsd:element name="property" type="xsd:string" minOccurs='0'
     maxOccurs='1'/>
   </xsd:all>
   <xsd:attribute name="type" use="optional" type="mcrdefaulttype" />
   <xsd:attribute name="inherited" use="optional" type="xsd:integer" />
   <xsd:attribute ref="xml:lang" />
  </xsd:complexType>
 </xsd:element>
</xsd:sequence>
<xsd:attribute name="class" type="xsd:string" use="required"
  fixed="MCRMetaInstitutionName"/>
</xsl:template>

<!-- Template for the metadata MCRMetaPersonName -->

<xsl:template match="mcrmetapersonname">
<xsd:sequence>
 <xsd:element name="{@name}" minOccurs="{@minOccurs}" maxOccurs="{@maxOccurs}">
  <xsd:complexType>
   <xsd:all>
    <xsd:element name="firstname" type="xsd:string" minOccurs='0'
     maxOccurs='1'/>
    <xsd:element name="callname" type="xsd:string" minOccurs='0'
     maxOccurs='1'/>
    <xsd:element name="surname" type="xsd:string" minOccurs='1'
     maxOccurs='1'/>
    <xsd:element name="fullname" type="xsd:string" minOccurs='0'
     maxOccurs='1'/>
    <xsd:element name="academic" type="xsd:string" minOccurs='0'
     maxOccurs='1'/>
    <xsd:element name="peerage" type="xsd:string" minOccurs='0'
     maxOccurs='1'/>
    <xsd:element name="prefix" type="xsd:string" minOccurs='0'
     maxOccurs='1'/>
   </xsd:all>
   <xsd:attribute name="type" use="optional" type="mcrdefaulttype" />
   <xsd:attribute ref="xml:lang" />
   <xsd:attribute name="inherited" use="optional" type="xsd:integer" />
  </xsd:complexType>
 </xsd:element>
</xsd:sequence>
<xsd:attribute name="class" type="xsd:string" use="required"
  fixed="MCRMetaPersonName"/>
</xsl:template>

</xsl:stylesheet>

