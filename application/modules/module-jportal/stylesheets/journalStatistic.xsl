<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:template match="/journal">
    <table style="border: 1px solid black;">
        <tr>
            <td width="300px">Name:
               <xsl:value-of select="@name"/>
            </td>
            <td width="100">
               Type:
               <xsl:value-of select="@type"/>
            </td>
            <td width="150">
               ID:
               <xsl:value-of select="@id"/>
            </td>
        </tr>
        <xsl:apply-templates select="numberOfObjects/total"/>
        <xsl:apply-templates select="numberOfObjects/complete"/>
        <xsl:apply-templates select="numberOfObjects/incomplete"/>
        <xsl:apply-templates select="numberOfObjects/missing"/>
    </table>
    
  </xsl:template>
  
  <xsl:template match="/total | /complete | /incomplete | /missing">
    <tr>
        <td>
           <xsl:value-of select="@percent"/>
        </td>
        <td>
           <xsl:value-of select="name()"/>:
        </td>
        <td>
           <xsl:value-of select="text()"/>
        </td>
    </tr>
  </xsl:template>
  
</xsl:stylesheet>