<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.6 $ $Date: 2006/05/26 15:28:26 $ -->
<!-- ============================================== -->

<!-- +
     | This stylesheet controls the Web-Layout of the "ShowUser"-mode of the UserServlet.
     | The UserServlet provides data as an XML stream with the following syntax (an
     | example is provided).
     |   The first three elements (guest_id, guest_pwd and backto_url) are used for controlling
     | the application logic while the next element (user) simply is the XML representation
     | of a mycore user object. The elements and attributes of this object are displayed.
     |
     | <mcr_user>
     |   <guest_id>...</guest_id>
     |   <guest_pwd>...</guest_pwd>
     |   <backto_url>...</backto_url>
     |   <user numID="2001" ID="ddegen" id_enabled="true" update_allowed="true">
     |     <user.password>??????</user.password>
     |     <user.description>Ein Mycore Entwickler</user.description>
     |     <user.primary_group>zauberer</user.primary_group>
     |     <user.contact>
     |       <contact.salutation>Dr.</contact.salutation>
     |       <contact.firstname>Detlev</contact.firstname>
     |       <contact.lastname>Degenhardt</contact.lastname>
     |       <contact.street>Herrmann Herder Strasse 10</contact.street>
     |       <contact.city>Freiburg</contact.city>
     |       <contact.postalcode>79104</contact.postalcode>
     |       <contact.country>Germany</contact.country>
     |       <contact.institution>Universitaet Freiburg</contact.institution>
     |       <contact.faculty></contact.faculty>
     |       <contact.department>Anwendungsabteilung</contact.department>
     |       <contact.institute>Rechenzentrum</contact.institute>
     |       <contact.telephone>0761-2034655</contact.telephone>
     |       <contact.fax>0761-2034643</contact.fax>
     |       <contact.email>Detlev.Degenhardt@rz.uni-freiburg.de</contact.email>
     |       <contact.cellphone></contact.cellphone>
     |     </user.contact>
     |     <user.groups>
     |       <groups.groupID>menschen</groups.groupID>
     |     </user.groups>
     |   </user>
     | </mcr_user>
     |
     | Author: Detlev Degenhardt
     + -->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xlink i18n">
	
<xsl:include href="mcr_user-lang.xsl"/>
<xsl:include href="MyCoReLayout.xsl" />

<xsl:variable name="heading">
    ID: <xsl:value-of select="/mcr_user/user/@ID"/>
</xsl:variable>

<xsl:variable name="MainTitle" select="i18n:translate('titles.mainTitle')"/>
<xsl:variable name="PageTitle" select="i18n:translate('titles.pageTitle.contactData')"/>

<xsl:template name="userAction">
    <!-- Now we present the user data in a table. First the account data ... -->
    <table id="metaData" cellpadding="0" cellspacing="0" >
      <tr>
        <th class="metahead" colspan="2"><xsl:copy-of select="i18n:translate('contactData.accountData')"/></th>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.numID')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/@numID"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.status')"/>:</td>
        <xsl:choose>
          <xsl:when test="./user/@id_enabled='true'">
            <td class="metavalue"><xsl:value-of select="i18n:translate('contactData.enabled')"/></td>
          </xsl:when>
          <xsl:otherwise>
            <td class="metavalue"><xsl:value-of select="i18n:translate('contactData.disabled')"/></td>
          </xsl:otherwise>
        </xsl:choose>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.update')"/>:</td>
        <xsl:choose>
          <xsl:when test="./user/@update_allowed='true'">
            <td class="metavalue"><xsl:value-of select="i18n:translate('contactData.allowed')"/></td>
          </xsl:when>
          <xsl:otherwise>
            <td class="metavalue"><xsl:value-of select="i18n:translate('contactData.denied')"/></td>
          </xsl:otherwise>
        </xsl:choose>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.creator')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.creator"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.creationDate')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.creation_date"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.lastModified')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.last_modified"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.description')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.description"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.primaryGroup')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.primary_group"/></td>
      </tr>

      <!-- Now the groups the user is a member of are displayed... -->
      <tr>
        <xsl:variable name="numGroups" select="count(./user/user.groups/groups.groupID)"/>
        <td class="metaname" ><xsl:copy-of select="i18n:translate('contactData.groups')"/>:</td>
        <td class="metavalue">
          <xsl:for-each select="./user/user.groups/groups.groupID" >
            <xsl:value-of select="."/><br />
          </xsl:for-each>
        </td>
      </tr>

      <!-- and finally we show the contact information. -->
      <tr>
        <th class="metahead" colspan="2"><xsl:copy-of select="i18n:translate('contactData.contact')"/></th>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.salutation')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.salutation"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.firstName')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.firstname"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.lastName')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.lastname"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.street')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.street"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.city')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.city"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.postalCode')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.postalcode"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.country')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.country"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.state')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.state"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.institution')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.institution"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.faculty')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.faculty"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.department')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.department"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.institute')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.institute"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.telefon')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.telephone"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.fax')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.fax"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.email')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.email"/></td>
      </tr>
      <tr>
        <td class="metaname"><xsl:value-of select="i18n:translate('contactData.cell')"/>:</td>
        <td class="metavalue"><xsl:value-of select="./user/user.contact/contact.cellphone"/></td>
      </tr>
    </table>
    <hr/>
    <div class="submitButton">
        <a class="submitbutton" href="{$href-user}&amp;mode=Select">
        <xsl:value-of select="i18n:translate('buttons.next')" />
        </a>
    </div>    
</xsl:template>
<xsl:template name="userStatus"/>

</xsl:stylesheet>
