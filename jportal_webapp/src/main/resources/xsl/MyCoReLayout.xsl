<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:websiteWriteProtection="xalan://org.mycore.frontend.MCRWebsiteWriteProtection"
  xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:mcrurn="xalan://fsu.jportal.urn.URNTools" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:gndo="http://d-nb.info/standards/elementset/gnd#" xmlns:geo="http://www.opengis.net/ont/geosparql#" 
  exclude-result-prefixes="rdf gndo geo xlink mcr acl i18n mcrxsl websiteWriteProtection xalan mcrurn">

  <xsl:include href="coreFunctions.xsl" />
  <xsl:include href="layout-utils.xsl" />
  <xsl:include href="generatePage.xsl" />

  <xsl:param name="DocumentBaseURL" />
  <xsl:param name="ServletsBaseURL" />
  <xsl:param name="RequestURL" />
  <xsl:param name="CurrentUser" />
  <xsl:param name="MCRSessionID" />
  <!-- HttpSession is empty if cookies are enabled, else ";jsessionid=<id>" -->
  <xsl:param name="HttpSession" />
  <!-- JSessionID is alway like ";jsessionid=<id>" and good for internal calls -->
  <xsl:param name="JSessionID" />
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="DefaultLang" />
  <xsl:param name="CurrentLang" />
  <xsl:param name="Referer" />
  <xsl:param name="TypeMapping" />
  <xsl:param name="MCR.Layout.JS.JQueryURI" />
  <xsl:variable name="hostfile" select="document('webapp:hosts.xml')" />

  <!-- website write protected ? -->
  <xsl:variable name="writeProtectedWebsite">
    <xsl:call-template name="get.writeProtectedWebsite" />
  </xsl:variable>
  <!-- get message, if write protected -->
  <xsl:variable name="writeProtectionMessage">
    <xsl:call-template name="get.writeProtectionMessage" />
  </xsl:variable>

  <xsl:variable name="direction">
    <xsl:choose>
      <xsl:when test="$CurrentLang = 'ar'">
        <xsl:text>rtl</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>ltr</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- ============================================================================================================= -->

  <xsl:template match="/">
    <xsl:call-template name="generatePage" />
  </xsl:template>

  <xsl:template match="/" mode="addHeader">
    <xsl:if test="string-length($MCR.Layout.JS.JQueryURI)&gt;0">
      <script type="text/javascript" src="{$MCR.Layout.JS.JQueryURI}" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="resulttitle">
    <!-- Overwrite this with either heigher priority or a more specific match -->
    <xsl:value-of select="@ID" />
  </xsl:template>

  <xsl:template name="printMetaText">
    <xsl:param name="text" />
    <xsl:param name="label" />
    <xsl:if test="$text">
      <tr>
        <td valign="top" class="metaname">
          <xsl:value-of select="concat($label,':')" />
        </td>
        <td class="metavalue">
          <xsl:value-of select="$text" />
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="printMetaDate">
    <!-- prints a table row for a given nodeset -->
    <xsl:param name="nodes" />
    <xsl:param name="label" select="local-name($nodes[1])" />
    
    <xsl:if test="$nodes">
      <tr id="metadata_{local-name($nodes[1])}" class="metadata_{substring-before(substring-after(@ID,'_'),'_')}_{local-name($nodes[1])}">
        <td valign="top" class="metaname">
          <xsl:value-of select="concat($label,':')" />
        </td>
        <td class="metavalue">
          <xsl:variable name="selectPresentLang">
            <xsl:call-template name="selectPresentLang">
              <xsl:with-param name="nodes" select="$nodes" />
            </xsl:call-template>
          </xsl:variable>
          <xsl:for-each select="$nodes">
            <xsl:choose>
              <xsl:when test="../@class='MCRMetaClassification'">
                <xsl:call-template name="printClass">
                  <xsl:with-param name="nodes" select="." />
                  <xsl:with-param name="host" select="$objectHost" />
                  <xsl:with-param name="next" select="'&lt;br /&gt;'" />
                </xsl:call-template>
                <xsl:call-template name="printClassInfo">
                  <xsl:with-param name="nodes" select="." />
                  <xsl:with-param name="host" select="$objectHost" />
                  <xsl:with-param name="next" select="'&lt;br /&gt;'" />
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="../@class='MCRMetaISO8601Date'">
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
              </xsl:when>
              <xsl:when test="../@class='MCRMetaHistoryDate'">
                <xsl:if test="not(@xml:lang) or @xml:lang=$selectPresentLang">
                  <xsl:call-template name="printHistoryDate">
                    <xsl:with-param name="nodes" select="." />
                    <xsl:with-param name="next" select="', '" />
                  </xsl:call-template>
                </xsl:if>
              </xsl:when>
              <xsl:when test="../@class='MCRMetaLinkID'">
                <xsl:call-template name="objectLink">
                  <xsl:with-param name="obj_id" select="@xlink:href" />
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="@class='MCRMetaDerivateLink'">
                <xsl:call-template name="derivateLink" />
              </xsl:when>
              <xsl:when test="../@class='MCRMetaLink'">
                <xsl:call-template name="webLink">
                  <xsl:with-param name="nodes" select="$nodes" />
                  <xsl:with-param name="next" select="'&lt;br /&gt;'" />
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:if test="not(@xml:lang) or @xml:lang=$selectPresentLang">
                  <xsl:call-template name="printI18N">
                    <xsl:with-param name="nodes" select="." />
                    <xsl:with-param name="host" select="$objectHost" />
                    <xsl:with-param name="next" select="'&lt;br /&gt;'" />
                  </xsl:call-template>
                </xsl:if>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="position()!=last()">
              <br />
            </xsl:if>
          </xsl:for-each>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="printMetaDateGND">
    <!-- prints a table row for a given nodeset -->
    <xsl:param name="nodes" />
    <xsl:param name="label" select="local-name($nodes[1])" />
    <xsl:if test="$nodes">
      <tr id="metadata_{local-name($nodes[1])}" class="metadata_{substring-before(substring-after(@ID,'_'),'_')}_{local-name($nodes[1])}">
        <td valign="top" class="metaname">
          <xsl:value-of select="concat($label,':')" />
        </td>
        <td class="metavalue">
          <xsl:variable name="selectPresentLang">
            <xsl:call-template name="selectPresentLang">
              <xsl:with-param name="nodes" select="$nodes" />
            </xsl:call-template>
          </xsl:variable>

          <xsl:for-each select="$nodes">

            <xsl:variable name="baseURL" select="concat('http://d-nb.info/gnd/', . , '/about/')" />
            <xsl:variable name="rdf" select="document(concat($baseURL, 'rdf'))" />

            <a href="{$baseURL}html">
              <xsl:value-of select="$rdf/rdf:RDF/rdf:Description/gndo:preferredNameForThePlaceOrGeographicName[1]" />
            </a>

            <xsl:if test="position()!=last()">
              <br />
            </xsl:if>
          </xsl:for-each>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- * Derivate Link * -->
  <!-- ******************************************************** -->
  <xsl:template name="derivateLink">
    <xsl:param name="staticURL" />

    <xsl:if test="$objectHost != 'local'">
      <a href="{$staticURL}">nur auf original Server</a>
    </xsl:if>
    <xsl:if test="$objectHost = 'local'">
      <xsl:for-each select="derivateLink">
        <xsl:variable select="substring-before(@xlink:href, '/')" name="deriv" />
        <xsl:variable name="derivateWithURN" select="mcrurn:hasURNAssigned(@xlink:href)" />
        <xsl:choose>
          <xsl:when test="acl:checkPermissionForReadingDerivate($deriv)">
            <xsl:variable name="firstSupportedFile" select="concat('/', substring-after(@xlink:href, '/'))" />
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
              <tr>
                <xsl:if test="annotation">
                  <xsl:value-of select="annotation" />
                  <br />
                </xsl:if>
              </tr>
              <tr>
                <td valign="top" align="left">
                  <!-- MCR-IView ..start -->
                  <xsl:call-template name="derivateLinkView">
                    <xsl:with-param name="derivateID" select="$deriv" />
                    <xsl:with-param name="file" select="$firstSupportedFile" />
                  </xsl:call-template>
                  <!-- MCR - IView ..end -->
                </td>
              </tr>
            </table>
          </xsl:when>
          <xsl:otherwise>
            <p>
              <!-- Zugriff auf 'Abbildung' gesperrt -->
              <xsl:variable select="substring-before(substring-after(/@ID,'_'),'_')" name="type" />
              <xsl:value-of select="i18n:translate('metaData.derivateLocked',i18n:translate(concat('metaData.',$type,'.[derivates]')))" />
            </p>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <!-- Person name form LegalEntity ******************************** -->
  <xsl:template match="names">
    <xsl:variable name="name" select="./name[1]" />
    <xsl:choose>
      <xsl:when test="$name/fullname">
        <xsl:value-of select="$name/fullname" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$name/academic" />
        <xsl:text></xsl:text>
        <xsl:value-of select="$name/peerage" />
        <xsl:text></xsl:text>
        <xsl:value-of select="$name/callname" />
        <xsl:text></xsl:text>
        <xsl:value-of select="$name/prefix" />
        <xsl:text></xsl:text>
        <xsl:value-of select="$name/surname" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- ============================================================================================================= -->
  <xsl:template match="printlatestobjects">
    <xsl:call-template name="printLatestObjects">
      <xsl:with-param name="objectType" select="@objecttype" />
      <xsl:with-param name="sortField" select="@sortfield" />
      <xsl:with-param name="mCRQLConditions" select="@mcrqlcond" />
      <xsl:with-param name="maxResults" select="@maxresults" />
      <xsl:with-param name="overwriteLayout" select="@overwritelayout" />
    </xsl:call-template>
  </xsl:template>
  <!-- ============================================================================================================= -->
  <xsl:template name="printLatestObjects">
    <xsl:param name="objectType" />
    <xsl:param name="sortField" />
    <xsl:param name="mCRQLConditions" />
    <xsl:param name="maxResults" />
    <xsl:param name="overwriteLayout" />
    <!-- build query term -->
    <xsl:variable name="objType" xmlns:encoder="xalan://java.net.URLEncoder">
      <xsl:value-of select="encoder:encode(concat('(objectType = ',$objectType,')') )" />
    </xsl:variable>
    <xsl:variable name="mCRQLConditions_encoded" xmlns:encoder="xalan://java.net.URLEncoder">
      <xsl:choose>
        <xsl:when test="$mCRQLConditions!=''">
          <xsl:value-of select="encoder:encode( concat(' and (',$mCRQLConditions,')') ) " />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="''" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="term">
      <xsl:value-of select="concat($objType,$mCRQLConditions_encoded)" />
    </xsl:variable>
    <xsl:variable name="queryURI">
      <xsl:value-of select="concat('query:term=',$term,'&amp;sortby=',$sortField,
            '&amp;order=descending&amp;maxResults=',$maxResults)" />
    </xsl:variable>
    <!-- do layout -->
    <xsl:choose>
      <xsl:when test="$overwriteLayout='true'">
        <xsl:for-each select="xalan:nodeset(document($queryURI))/mcr:results/mcr:hit">
          <xsl:variable name="mcrobj" select="document(concat('mcrobject:',@id))/mycoreobject" />
          <xsl:apply-templates select="." mode="latestObjects">
            <xsl:with-param name="mcrobj" select="$mcrobj" />
          </xsl:apply-templates>
        </xsl:for-each>
        <xsl:call-template name="printLatestObjects.all">
          <xsl:with-param name="query2" select="$term" />
          <xsl:with-param name="sortBy" select="$sortField" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <table id="resultList" cellpadding="0" cellspacing="0" xmlns:mcr="http://www.mycore.org/">
          <xsl:for-each select="xalan:nodeset(document($queryURI))/mcr:results/mcr:hit">
            <xsl:variable name="mcrobj" select="document(concat('notnull:mcrobject:',@id))/mycoreobject" />
            <xsl:apply-templates select=".">
              <xsl:with-param name="mcrobj" select="$mcrobj" />
            </xsl:apply-templates>
          </xsl:for-each>
          <tr>
            <td colspan="3" align="right">
              <xsl:call-template name="printLatestObjects.all">
                <xsl:with-param name="query2" select="$term" />
                <xsl:with-param name="sortBy" select="$sortField" />
              </xsl:call-template>
            </td>
          </tr>
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- ============================================================================================================= -->
  <xsl:template name="printLatestObjects.all">
    <xsl:param name="query2" />
    <xsl:param name="sortBy" />
    <div id="latestmore">
      <a
        href="{$ServletsBaseURL}MCRSearchServlet{$HttpSession}?query={$query2}&amp;{$sortBy}.sortField=descending&amp;numPerPage=10&amp;maxResults=0">
        <xsl:value-of select="i18n:translate('latestObjects.more')" />
      </a>
    </div>
  </xsl:template>
  <!-- ============================================================================================================= -->
  <xsl:template name="printNotLoggedIn">
    <div class="alert alert-error">
      <xsl:value-of select="i18n:translate('webpage.notLoggedIn')" disable-output-escaping="yes" />
    </div>
  </xsl:template>
  <!-- ============================================================================================================= -->
  <xsl:template name="userInfo">
    <xsl:value-of select="concat(i18n:translate('users.user'),': ')" />
    <xsl:choose>
      <xsl:when test="$CurrentUser='gast'">
        <xsl:value-of select="i18n:translate('users.error.notLoggedIn')" />
      </xsl:when>
      <xsl:otherwise>
        <a href="{concat( $ServletsBaseURL, 'MCRUserServlet',$HttpSession,'?action=show')}">
          <xsl:value-of select="$CurrentUser" />
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- ============================================================================================================= -->
  <xsl:template name="get.writeProtectionMessage">
    <xsl:if test="$writeProtectedWebsite='true'">
      <xsl:copy-of select="websiteWriteProtection:getMessage()" />
    </xsl:if>
  </xsl:template>
  <!-- ============================================================================================================= -->
  <xsl:template name="print.writeProtectionMessage">
    <xsl:if test="$writeProtectedWebsite='true' and not(/website-ReadOnly)">
      <p style="color:#FF0000;">
        <b>
          <xsl:copy-of select="$writeProtectionMessage" />
        </b>
      </p>
    </xsl:if>
  </xsl:template>
  <!-- ============================================================================================================= -->
  <xsl:template name="get.writeProtectedWebsite">
    <xsl:choose>
      <xsl:when test="$CurrentUser!='gast'">
        <xsl:value-of select="websiteWriteProtection:isActive()" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="false()" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ======== replace i18n keys with translation ======== -->
  <xsl:template match="@*[starts-with(.,'i18n:')]">
    <xsl:attribute name="{name()}">
      <xsl:value-of select="i18n:translate(substring-after(.,'i18n:'))" />
    </xsl:attribute>
  </xsl:template>

  <!-- ============================================================================================================= -->
  <!-- The template write the icon line to edit an object with derivate -->
  <xsl:template name="editobject_with_der">
    <xsl:param name="accessnbn" />
    <xsl:param name="accessedit" />
    <xsl:param name="accessdelete" />
    <xsl:param name="id" />
    <xsl:param name="layout" select="'$'" />
    <xsl:variable name="layoutparam">
      <xsl:if test="$layout != '$'">
        <xsl:value-of select="concat('&amp;layout=',$layout)" />
      </xsl:if>
    </xsl:variable>
    <xsl:if test="$objectHost = 'local'">
      <xsl:choose>
        <xsl:when test="acl:checkPermission($id,'writedb') or acl:checkPermission($id,'deletedb')">
          <xsl:variable name="type" select="substring-before(substring-after($id,'_'),'_')" />
          <tr>
            <td class="metaname">
              <xsl:value-of select="concat(i18n:translate('metaData.edit'),' :')" />
            </td>
            <td class="metavalue">
              <xsl:if test="acl:checkPermission($id,'writedb')">
                <a
                  href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=seditobj">
                  <img src="{$WebApplicationBaseURL}images/workflow_objedit.gif" title="{i18n:translate('component.swf.object.editObject')}" />
                </a>
                <a
                  href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;step=commit&amp;todo=scopyobj{$layoutparam}">
                  <img src="{$WebApplicationBaseURL}images/workflow_objcopy.gif" title="{i18n:translate('component.swf.object.copyObject')}"
                    border="0" />
                </a>
                <a
                  href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type=acl&amp;step=commit&amp;todo=seditacl">
                  <img src="{$WebApplicationBaseURL}images/workflow_acledit.gif" title="{i18n:translate('component.swf.object.editACL')}" />
                </a>
                <xsl:if test="$accessnbn = 'true'">
                  <a
                    href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=saddnbn">
                    <img src="{$WebApplicationBaseURL}images/workflow_addnbn.gif" title="{i18n:translate('component.swf.object.addNBN')}" />
                  </a>
                </xsl:if>
                <a
                  href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=snewder">
                  <img src="{$WebApplicationBaseURL}images/workflow_deradd_ltr.gif" title="{i18n:translate('component.swf.derivate.addDerivate')}" />
                </a>
              </xsl:if>
              <xsl:if test="acl:checkPermission($id,'deletedb')">
                <a
                  href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=sdelobj">
                  <img src="{$WebApplicationBaseURL}images/workflow_objdelete.gif" title="{i18n:translate('component.swf.object.delObject')}" />
                </a>
              </xsl:if>
            </td>
          </tr>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- The template write the icon line to edit an object -->
  <xsl:template name="editobject">
    <xsl:param name="accessedit" />
    <xsl:param name="accessdelete" />
    <xsl:param name="id" />
    <xsl:param name="layout" select="'$'" />
    <xsl:variable name="layoutparam">
      <xsl:if test="$layout != '$'">
        <xsl:value-of select="concat('&amp;layout=',$layout)" />
      </xsl:if>
    </xsl:variable>
    <xsl:if test="$objectHost = 'local'">
      <xsl:choose>
        <xsl:when test="acl:checkPermission($id,'writedb') or acl:checkPermission($id,'deletedb')">
          <xsl:variable name="type" select="substring-before(substring-after($id,'_'),'_')" />
          <tr>
            <td class="metaname">
              <xsl:value-of select="concat(i18n:translate('metaData.edit'),' :')" />
            </td>
            <td class="metavalue">
              <xsl:if test="acl:checkPermission($id,'writedb')">
                <a
                  href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}{$layoutparam}&amp;step=commit&amp;todo=seditobj">
                  <img src="{$WebApplicationBaseURL}images/workflow_objedit.gif" title="{i18n:translate('component.swf.object.editObject')}" />
                </a>
                <a
                  href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;step=commit&amp;todo=scopyobj{$layoutparam}">
                  <img src="{$WebApplicationBaseURL}images/workflow_objcopy.gif" title="{i18n:translate('component.swf.object.copyObject')}"
                    border="0" />
                </a>
                <a
                  href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type=acl&amp;step=commit&amp;todo=seditacl">
                  <img src="{$WebApplicationBaseURL}images/workflow_acledit.gif" title="{i18n:translate('component.swf.object.editACL')}" />
                </a>
              </xsl:if>
              <xsl:if test="acl:checkPermission($id,'deletedb')">
                <a
                  href="{$ServletsBaseURL}MCRStartEditorServlet{$HttpSession}?tf_mcrid={$id}&amp;re_mcrid={$id}&amp;se_mcrid={$id}&amp;type={$type}&amp;step=commit&amp;todo=sdelobj">
                  <img src="{$WebApplicationBaseURL}images/workflow_objdelete.gif" title="{i18n:translate('component.swf.object.delObject')}" />
                </a>
              </xsl:if>
            </td>
          </tr>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- template that matches if not files where hit by query fixed bug #2545125 -->
  <xsl:template match="mcr:hit" mode="hitInFiles" priority="0" />

</xsl:stylesheet>