<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.12 $ $Date: 2006/11/14 10:49:45 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="http://www.mycore.org/"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xlink mcr acl i18n">
	
  <xsl:param name="objectHost" select="'local'"/>

  <!-- Template for result list hit -->
  <xsl:template match="mcr:hit[contains(@id,'_author_')]">
    <xsl:param name="mcrobj" />
    <xsl:param name="mcrobjlink" />
    <xsl:variable name="DESCRIPTION_LENGTH" select="100" />

    <xsl:variable name="host" select="@host" />
    <xsl:variable name="obj_id">
      <xsl:value-of select="@id" />
    </xsl:variable>
    <tr>
      <td class="resultTitle" colspan="2">
        <xsl:copy-of select="$mcrobjlink" />
      </td>
    </tr>
    <tr>
      <td class="description" colspan="2">
        <xsl:value-of select="concat($obj_id,'; ')" />
        <xsl:variable name="date">
          <xsl:call-template name="formatISODate">
            <xsl:with-param name="date" select="$mcrobj/service/servdates/servdate[@type='modifydate']" />
            <xsl:with-param name="format" select="i18n:translate('metaData.date')" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="i18n:translate('results.lastChanged',$date)" />
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="/mycoreobject[contains(@ID,'_author_')]" mode="resulttitle" priority="1">
    <xsl:choose>
      <xsl:when test="metadata/names">
        <xsl:apply-templates select="metadata/names" mode="present" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@label" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="names" mode="present">
    <xsl:choose>
      <xsl:when test="./name[lang($CurrentLang)]">
        <xsl:value-of
          select="concat(./name[lang($CurrentLang)]/academic,' ')" />
        <xsl:value-of
          select="concat(./name[lang($CurrentLang)]/peerage,' ')" />
        <xsl:value-of
          select="concat(./name[lang($CurrentLang)]/prefix,' ')" />
        <xsl:value-of
          select="concat(./name[lang($CurrentLang)]/surname,', ')" />
        <xsl:value-of select="./name[lang($CurrentLang)]/callname" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of
          select="concat(./name[lang($DefaultLang)]/academic,' ')" />
        <xsl:value-of
          select="concat(./name[lang($DefaultLang)]/peerage,' ')" />
        <xsl:value-of
          select="concat(./name[lang($DefaultLang)]/prefix,' ')" />
        <xsl:value-of
          select="concat(./name[lang($DefaultLang)]/surname,', ')" />
        <xsl:value-of select="./name[lang($DefaultLang)]/callname" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/mycoreobject[contains(@ID,'_author_')]"
    mode="title" priority="1">
    <xsl:apply-templates select="/mycoreobject/metadata/names" />
  </xsl:template>
  <xsl:template match="/mycoreobject[contains(@ID,'_author_')]"
    mode="present" priority="1">
    <xsl:param name="obj_host" select="$objectHost" />
    <xsl:variable name="objectBaseURL">
      <xsl:if test="$objectHost != 'local'">
        <xsl:value-of
          select="concat($hostfile/mcr:hosts/mcr:host[@alias=$objectHost]/@url,$hostfile/mcr:hosts/mcr:host[@alias=$objectHost]/@staticpath)" />
      </xsl:if>
      <xsl:if test="$objectHost = 'local'">
        <xsl:value-of select="concat($WebApplicationBaseURL,'receive/')" />
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="staticURL">
        <xsl:value-of select="concat($objectBaseURL,@ID)" />
    </xsl:variable>

    <table id="metaData" cellpadding="0" cellspacing="0">

      <!-- Female ************************************************** -->

      <xsl:if test="./metadata/females">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('metaData.author.gender'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:choose>
              <xsl:when
                test="./metadata/females/female/text() = 'true'">
                <xsl:value-of
                  select="i18n:translate('metaData.author.gender.female')" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of
                  select="i18n:translate('metaData.author.gender.male')" />
              </xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
      </xsl:if>

      <!-- Institution ********************************************* -->

      <xsl:if test="./metadata/institutions">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('metaData.author.institution'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:call-template name="printClass">
              <xsl:with-param name="nodes"
                select="./metadata/institutions/institution" />
              <xsl:with-param name="host" select="$obj_host" />
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>

      <!-- Adresses ********************************************* -->

      <xsl:if test="./metadata/addresses">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('contactData.address'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:variable name="selectLang">
              <xsl:call-template name="selectLang">
                <xsl:with-param name="nodes"
                  select="./metadata/addresses/address" />
              </xsl:call-template>
            </xsl:variable>
            <xsl:for-each
              select="./metadata/addresses/address[lang($selectLang)]">
              <div class="addressBox">
                <div class="addressType">
                  <xsl:choose>
                    <xsl:when test="@type = 'office'">
                      <xsl:value-of
                        select="i18n:translate('contactData.address.office')" />
                    </xsl:when>
                    <xsl:when test="@type = 'private'">
                      <xsl:value-of
                        select="i18n:translate('contactData.address.private')" />
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="@type" />
                    </xsl:otherwise>
                  </xsl:choose>
                </div>
                <div class="address">
                  <xsl:if test="street">
                    <xsl:value-of select="concat(street,' ')" />
                  </xsl:if>
                  <xsl:if test="number">
                    <xsl:value-of select="number" />
                    <br />
                  </xsl:if>
                  <xsl:if test="zipcode">
                    <xsl:value-of select="concat(zipcode,' ')" />
                  </xsl:if>
                  <xsl:if test="city">
                    <xsl:value-of select="city" />
                    <br />
                  </xsl:if>
                  <xsl:if test="state">
                    <xsl:value-of select="concat(state,', ')" />
                  </xsl:if>
                  <xsl:if test="country">
                    <xsl:value-of select="country" />
                  </xsl:if>
                </div>
              </div>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>

      <!-- Dates ********************************************* -->

      <xsl:if test="./metadata/dates">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('metaData.author.date'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:variable name="selectLang">
              <xsl:call-template name="selectLang">
                <xsl:with-param name="nodes"
                  select="./metadata/dates/date" />
              </xsl:call-template>
            </xsl:variable>
            <xsl:for-each
              select="./metadata/dates/date[lang($selectLang)]">
              <xsl:if test="position() != 1">
                <br />
              </xsl:if>
              <xsl:variable name="date">
                <xsl:call-template name="formatISODate">
                  <xsl:with-param name="date" select="." />
                  <xsl:with-param name="format"
                    select="i18n:translate('metaData.date')" />
                </xsl:call-template>
              </xsl:variable>
              <xsl:variable name="datetype">
                <xsl:choose>
                  <xsl:when test="@type = 'birth'">
                    <xsl:value-of
                      select="i18n:translate('contactData.birthday')" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="@type" />
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:value-of
                select="concat($date,'&#160;(',$datetype,')')" />
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>

      <!-- Phones ********************************************* -->

      <xsl:if test="./metadata/phones">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('contactData.telefon'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:variable name="selectLang">
              <xsl:call-template name="selectLang">
                <xsl:with-param name="nodes"
                  select="./metadata/phones/phone" />
              </xsl:call-template>
            </xsl:variable>
            <xsl:for-each
              select="./metadata/phones/phone[lang($selectLang)]">
              <xsl:if test="position() != 1">
                <br />
              </xsl:if>
              <xsl:variable name="phonetype">
                <xsl:choose>
                  <xsl:when test="@type = 'phone'">
                    <xsl:value-of
                      select="i18n:translate('contactData.telefon')" />
                  </xsl:when>
                  <xsl:when test="@type = 'fax'">
                    <xsl:value-of
                      select="i18n:translate('contactData.fax')" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="@type" />
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:value-of select="concat(.,'&#160;(',$phonetype,')')" />
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>

      <!-- Profession ***************************************** -->

      <xsl:if test="./metadata/professions">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('metaData.author.proffesion'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:variable name="selectLang">
              <xsl:call-template name="selectLang">
                <xsl:with-param name="nodes"
                  select="./metadata/professions/profession" />
              </xsl:call-template>
            </xsl:variable>
            <xsl:for-each
              select="./metadata/professions/profession[lang($selectLang)]">
              <xsl:if test="position() != 1">
                <br />
              </xsl:if>
              <xsl:variable name="proftype">
                <xsl:choose>
                  <xsl:when test="@type = 'job'">
                    <xsl:value-of
                      select="i18n:translate('metaData.author.job')" />
                  </xsl:when>
                  <xsl:when test="@type = 'profession'">
                    <xsl:value-of
                      select="i18n:translate('metaData.author.proffesion')" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="@type" />
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:value-of select="concat(.,'&#160;(',$proftype,')')" />
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>

      <!-- Profession Class ******************************************** -->

      <xsl:if test="./metadata/profclasses">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="i18n:translate('metaData.author.profClass')" />
          </td>
          <td class="metavalue">
            <xsl:call-template name="printClass">
              <xsl:with-param name="nodes"
                select="./metadata/profclasses/profclass" />
              <xsl:with-param name="host" select="$obj_host" />
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>

      <!-- Nationality ************************************************* -->

      <xsl:if test="./metadata/nationals">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('metaData.author.nationality'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:call-template name="printClass">
              <xsl:with-param name="nodes"
                select="./metadata/nationals/national" />
              <xsl:with-param name="host" select="$obj_host" />
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>

      <!-- URLs ********************************************* -->

      <xsl:if test="./metadata/urls">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('contactData.web'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:call-template name="webLink">
              <xsl:with-param name="nodes" select="./metadata/urls/url" />
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>

      <!-- eMails ******************************************* -->

      <xsl:if test="./metadata/emails">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('contactData.email'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:call-template name="mailLink">
              <xsl:with-param name="nodes"
                select="./metadata/emails/email" />
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>

      <!-- References *************************************** -->

      <xsl:if test="./metadata/references">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('metaData.author.references'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:call-template name="webLink">
              <xsl:with-param name="nodes"
                select="./metadata/references/reference" />
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>

      <!-- Notes ******************************************** -->

      <xsl:if test="./metadata/notes">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('metaData.notes'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:variable name="selectLang">
              <xsl:call-template name="selectLang">
                <xsl:with-param name="nodes"
                  select="./metadata/notes/note" />
              </xsl:call-template>
            </xsl:variable>
            <xsl:for-each
              select="./metadata/notes/note[lang($selectLang)]">
              <xsl:if test="position() != 1">
                <br />
              </xsl:if>
              <xsl:value-of select="." />
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>

      <!-- Publications ************************************* -->

      <xsl:if test="./metadata/publications">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('metaData.author.publications'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:variable name="selectLang">
              <xsl:call-template name="selectLang">
                <xsl:with-param name="nodes"
                  select="./metadata/publications/publication" />
              </xsl:call-template>
            </xsl:variable>
            <xsl:for-each
              select="./metadata/publications/publication[lang($CurrentLang)]">
              <xsl:if test="position() != 1">
                <br />
              </xsl:if>
              <xsl:value-of select="." />
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>

      <!-- Empty line ************************************************** -->

      <tr>
        <td class="metanone" colspan="2">&#160;</td>
      </tr>

      <!-- Created ***************************************************** -->

      <xsl:if test="./service/servdates/servdate[@type='createdate']">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('metaData.createdAt'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:call-template name="formatISODate">
              <xsl:with-param name="date"
                select="./service/servdates/servdate[@type='createdate']" />
              <xsl:with-param name="format"
                select="i18n:translate('metaData.dateTime')" />
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>

      <!-- Last Change ************************************************* -->

      <xsl:if test="./service/servdates/servdate[@type='modifydate']">
        <tr>
          <td class="metaname">
            <xsl:value-of
              select="concat(i18n:translate('metaData.lastChanged'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:call-template name="formatISODate">
              <xsl:with-param name="date"
                select="./service/servdates/servdate[@type='modifydate']" />
              <xsl:with-param name="format"
                select="i18n:translate('metaData.dateTime')" />
            </xsl:call-template>
          </td>
        </tr>
      </xsl:if>

      <!-- MyCoRe ID *************************************************** -->

      <tr>
        <td class="metaname">
          <xsl:value-of
            select="concat(i18n:translate('metaData.ID'),' :')" />
        </td>
        <td class="metavalue">
          <xsl:value-of select="./@ID" />
        </td>
      </tr>

      <!-- More from the person **************************************** -->

      <xsl:if test="$objectHost = 'local'">
        <tr>
          <td class="metaname">
            <xsl:value-of select="concat(i18n:translate('metaData.author.documents'),' :')" />
          </td>
          <td class="metavalue">
            <a xmlns:encoder="xalan://java.net.URLEncoder"
              href="{$ServletsBaseURL}MCRSearchServlet{$HttpSession}?query={encoder:encode(concat('(authorID = ',./@ID,' &amp; objectType = document)'))}&amp;numPerPage=10">
              <xsl:value-of select="i18n:translate('buttons.startSearch')" />
            </a>
          </td>
        </tr>
      </xsl:if>

      <!-- Static URL ************************************************** -->
      <tr>
        <td class="metaname">
          <xsl:value-of
            select="concat(i18n:translate('metaData.staticURL'),' :')" />
        </td>
        <td class="metavalue">
          <a>
            <xsl:attribute name="href">
              <xsl:copy-of select="$staticURL" />
            </xsl:attribute>
            <xsl:copy-of select="$staticURL" />
          </a>
        </td>
      </tr>
      <!-- Editor Buttons ********************************************** -->
      <xsl:call-template name="editobject">
        <xsl:with-param name="id" select="./@ID" />
      </xsl:call-template>
    </table>
  </xsl:template>
</xsl:stylesheet>