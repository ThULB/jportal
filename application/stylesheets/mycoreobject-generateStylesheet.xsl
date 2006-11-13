<?xml version="1.0" encoding="iso-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.2 $ $Date: 2006/11/10 09:58:15 $ -->
<!-- ============================================== -->
<gxsl:stylesheet version="1.0" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:gxsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsl="http://www.w3.org/1999/XSL/TransformAlias">
  <!-- 
    This stylesheet can be used to generate templates for parts of
    messages_*.properties. These parts are those beginning with
    "metaData.<ObjectType>.*"
  -->
  <gxsl:namespace-alias stylesheet-prefix="xsl" result-prefix="gxsl" />
  <gxsl:output encoding="UTF-8" media-type="text/xml" method="xml" indent="yes" xalan:indent-amount="2" />
  <gxsl:strip-space elements="*"/>
  <gxsl:param name="useIView" select="'off'" />
  <gxsl:param name="withDerivates" select="'off'" />
  <gxsl:param name="propPrefix" select="'metaData.'" />
  <gxsl:param name="childObjectTypes"/>
  <gxsl:variable name="objectType">
    <gxsl:apply-templates select="/*" mode="getObjectType"/>
  </gxsl:variable>

  <gxsl:template match="/mycoreobject">
    <xsl:stylesheet version="1.0"
      xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mcr="http://www.mycore.org/"
      xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
      xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xlink mcr i18n acl">
      <xsl:param name="objectHost" select="'local'"/>
      <gxsl:if test="$useIView='on'">
        <xsl:include href="mcr-module-startIview.xsl" />
      </gxsl:if>
      <gxsl:call-template name="generateHitTemplate" />
      <gxsl:call-template name="generateLinkTemplate" />
      <gxsl:call-template name="generateTitleTemplate" />
      <gxsl:call-template name="generateMetaTemplate" />
    </xsl:stylesheet>
  </gxsl:template>

  <gxsl:template name="generateHitTemplate">
    <gxsl:comment>Template for result list hit: see results.xsl</gxsl:comment>

    <xsl:template match="mcr:hit[contains(@id,'_{$objectType}_')]">
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
        <div>
          <gxsl:text>please edit &lt;template match=mcr:hit[contains(@id,'_</gxsl:text>
          <gxsl:value-of select="$objectType" />
          <gxsl:text>_')]&gt; for object type: </gxsl:text>
          <gxsl:value-of select="$objectType" />
        </div>
        <gxsl:comment>
          you could insert here a preview for your metadata, e.g.
          uncomment the next block and replace "your-tags/here"
          by something of your metadata
        </gxsl:comment>
        <gxsl:comment>
            &lt;div&gt;
              short description:
              &lt;xsl:call-template name="printI18N"&gt;
                &lt;xsl:with-param name="nodes" select="$mcrobj/metadata/your-tags/here" /&gt;
              &lt;/xsl:call-template&gt;
            &lt;/div&gt;
          </gxsl:comment>
          <span class="properties">
            <xsl:variable name="date">
              <xsl:call-template name="formatISODate">
                <xsl:with-param name="date" select="$mcrobj/service/servdates/servdate[@type='modifydate']" />
                <xsl:with-param name="format" select="i18n:translate('metaData.date')" />
              </xsl:call-template>
            </xsl:variable>
            <xsl:value-of select="i18n:translate('results.lastChanged',$date)" />
          </span>
        </td>
      </tr>
    </xsl:template>
  </gxsl:template>

  <gxsl:template name="generateLinkTemplate">
    <gxsl:comment>Template for generated link names and result titles: see mycoreobject.xsl, results.xsl, MyCoReLayout.xsl</gxsl:comment>
    <xsl:template match="/mycoreobject[contains(@ID,'_{$objectType}_')]" mode="resulttitle" priority="1">
      <xsl:choose>
        <gxsl:comment>
          you could insert any title-like metadata here, e.g.
          replace "your-tags/here" by something of your metadata
        </gxsl:comment>
        <xsl:when test="./metadata/your-tags">
          <xsl:call-template name="printI18N">
            <xsl:with-param name="nodes" select="./metadata/your-tags/here" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@label" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
  </gxsl:template>

  <gxsl:template name="generateTitleTemplate">
    <gxsl:comment>Template for title in metadata view: see mycoreobject.xsl</gxsl:comment>
    <xsl:template match="/mycoreobject[contains(@ID,'_{$objectType}_')]" mode="title" priority="1">
      <xsl:choose>
        <gxsl:comment>
          you could insert any title-like metadata here, e.g.
          replace "your-tags/here" by something of your metadata
        </gxsl:comment>
        <xsl:when test="./metadata/your-tags">
          <xsl:call-template name="printI18N">
            <xsl:with-param name="nodes" select="./metadata/your-tags/here" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
  </gxsl:template>

  <gxsl:template name="generateMetaTemplate">
    <gxsl:comment>Template for metadata view: see mycoreobject.xsl</gxsl:comment>
    <xsl:template match="/mycoreobject[contains(@ID,'_{$objectType}_')]" mode="present" priority="1">
      <xsl:param name="obj_host" select="$objectHost" />
      <xsl:param name="accessedit" />
      <xsl:param name="accessdelete" />
      <xsl:variable name="objectBaseURL">
        <xsl:if test="$objectHost != 'local'">
          <xsl:value-of
            select="document('webapp:hosts.xml')/mcr:hosts/mcr:host[@alias=$objectHost]/mcr:url[@type='object']/@href" />
        </xsl:if>
        <xsl:if test="$objectHost = 'local'">
          <xsl:value-of select="concat($WebApplicationBaseURL,'receive/')" />
        </xsl:if>
      </xsl:variable>
      <xsl:variable name="staticURL">
        <xsl:value-of select="concat($objectBaseURL,@ID)" />
      </xsl:variable>

      <table id="metaData" cellpadding="0" cellspacing="0">
          <!--
            For messages_*.properties we must filter out
            1.) tags with the same parent and same type attribute
            2.) tags with the same parent an no type attribut
            
            We define a helper variable to perform this filtering now.
          -->
        <gxsl:variable name="unique-list" select="./metadata/*/*[not(local-name()=local-name(following::*) and (@type=following::*/@type))]"/>
        <gxsl:for-each select="$unique-list">
          <gxsl:variable name="defTag" select="local-name(..)" />
          <gxsl:variable name="curTag" select="local-name()" />
          <gxsl:variable name="predicate">
            <gxsl:if test="string-length(@type) &gt; 0">
              <gxsl:text>[@type='</gxsl:text>
              <gxsl:value-of select="@type" />
              <gxsl:text>']</gxsl:text>
            </gxsl:if>
          </gxsl:variable>
          <gxsl:comment>
            <gxsl:value-of select="position()" />
            <gxsl:text>***</gxsl:text>
            <gxsl:value-of select="$curTag" />
            <gxsl:text>*************************************</gxsl:text>
          </gxsl:comment>
          <xsl:call-template name="printMetaDate">
            <gxsl:variable name="I18Nkey">
              <gxsl:apply-templates mode="messageProperty" select="." />
            </gxsl:variable>
            <xsl:with-param name="nodes" select="{concat('./metadata/',$defTag,'/',$curTag,$predicate)}" />
            <xsl:with-param name="label" select="i18n:translate('{$I18Nkey}')" />
          </xsl:call-template>
        </gxsl:for-each>

        <gxsl:comment>*** Editor Buttons ************************************* </gxsl:comment>
        <gxsl:choose>
          <gxsl:when test="$withDerivates = 'on'">
            <xsl:call-template name="editobject_with_der">
              <xsl:with-param name="accessedit" select="$accessedit" />
              <xsl:with-param name="id" select="./@ID" />
            </xsl:call-template>
          </gxsl:when>
          <gxsl:otherwise>
            <xsl:call-template name="editobject">
              <xsl:with-param name="accessedit" select="$accessedit" />
              <xsl:with-param name="id" select="./@ID" />
            </xsl:call-template>
          </gxsl:otherwise>
        </gxsl:choose>
        <gxsl:if test="string-length($childObjectTypes)>0">
          <xsl:call-template name="addChild">
            <xsl:with-param select="./@ID" name="id" />
            <xsl:with-param select="'{$childObjectTypes}'" name="types" />
          </xsl:call-template>
        </gxsl:if>

        <gxsl:comment>*** List children per object type ************************************* </gxsl:comment>
        <gxsl:comment>
          1.) get a list of objectTypes of all child elements
          2.) remove duplicates from this list
          3.) for-each objectTyp id list child elements
        </gxsl:comment>
        <xsl:variable name="objectTypes">
          <xsl:for-each select="./structure/children/child/@xlink:href">
            <id>
              <xsl:copy-of select="substring-before(substring-after(.,'_'),'_')" />
            </id>
          </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="unique-ids" select="xalan:nodeset($objectTypes)/id[not(.=following::id)]" />
        <gxsl:comment>
        the for-each would iterate over &lt;id&gt; with root not beeing /mycoreobject
        so we save the current node in variable context to access needed nodes
        </gxsl:comment>
        <xsl:variable name="context" select="."/>
        <xsl:for-each select="$unique-ids">
          <xsl:variable name="thisObjectType" select="." />
          <xsl:variable name="label">
            <xsl:choose>
              <xsl:when test="count($context/structure/children/child[contains(@xlink:href,$thisObjectType)])=1">
                <xsl:value-of select="i18n:translate(concat('{$propPrefix}',$thisObjectType,'.[singular]'))" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="i18n:translate(concat('{$propPrefix}',$thisObjectType,'.[plural]'))" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:call-template name="printMetaDate">
            <xsl:with-param name="nodes" select="$context/structure/children/child[contains(@xlink:href, concat('_',$thisObjectType,'_'))]" />
            <xsl:with-param name="label" select="$label" />
          </xsl:call-template>
        </xsl:for-each>

        <!-- Derivate *** interne und externe Referenzen ******************* -->
        <gxsl:if test="$withDerivates = 'on'">
          <xsl:call-template name="Derobjects">
            <xsl:with-param name="staticURL" select="$staticURL" />
            <xsl:with-param name="obj_host" select="$obj_host" />
          </xsl:call-template>
        </gxsl:if>

        <gxsl:comment>*** Created ************************************* </gxsl:comment>
        <xsl:call-template name="printMetaDate">
          <xsl:with-param name="nodes" select="./service/servdates/servdate[@type='createdate']" />
          <xsl:with-param name="label" select="i18n:translate('metaData.createdAt')" />
        </xsl:call-template>
        <gxsl:comment>*** Last Modified ************************************* </gxsl:comment>
        <xsl:call-template name="printMetaDate">
          <xsl:with-param name="nodes" select="./service/servdates/servdate[@type='modifydate']" />
          <xsl:with-param name="label" select="i18n:translate('metaData.lastChanged')" />
        </xsl:call-template>
        <gxsl:comment>*** MyCoRe-ID ************************************* </gxsl:comment>
        <tr>
          <td class="metaname">
            <xsl:value-of select="concat(i18n:translate('metaData.ID'),' :')" />
          </td>
          <td class="metavalue">
            <xsl:value-of select="./@ID" />
          </td>
        </tr>
      </table>
    </xsl:template>

    <gxsl:if test="string-length($childObjectTypes)>0">
      <xsl:template name="addChild">
        <xsl:param name="id" />
        <xsl:param name="layout" />
        <xsl:param name="types" />
        <xsl:param name="xmltempl" select="concat('&amp;_xml_structure%2Fparents%2Fparent%2F%40href=',$id)" />
        <xsl:variable name="suffix">
          <xsl:if test="string-length($layout)>0">
            <xsl:value-of select="concat('&amp;layout=',$layout)" />
          </xsl:if>
        </xsl:variable>
        <xsl:variable name="typeToken">
          <xsl:call-template name="Tokenizer">
            <xsl:with-param name="string" select="$types" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="acl:checkPermission($id,'writedb')">
          <tr>
            <td class="metaname"><xsl:value-of select="concat(i18n:translate('metaData.addChildObject'),':')"/></td>
            <td class="metavalue">
              <ul>
                <xsl:for-each select="xalan:nodeset($typeToken)/token">
                  <xsl:variable name="type" select="." />
                  <li>
                    <a
                      href="{'{'}$ServletsBaseURL{'}'}MCRStartEditorServlet{'{'}$HttpSession{'}'}?type={'{'}$type{'}'}&amp;step=author&amp;todo=wnewobj{'{'}$suffix{'}'}{'{'}$xmltempl{'}'}">
                      <xsl:value-of select="i18n:translate(concat('metaData.',$type,'.[singular]'))" />
                    </a>
                  </li>
                </xsl:for-each>
              </ul>
            </td>
          </tr>
        </xsl:if>
      </xsl:template>
    </gxsl:if>

    <gxsl:if test="$withDerivates = 'on'">
      <!-- *** Derobjects *** interne und externe Referenzen ******************* -->
      <!-- FileAdd, FileEdit, FileDelete -->
      <xsl:template name="Derobjects">
        <xsl:param name="obj_host" />
        <xsl:param name="staticURL" />
        <xsl:param name="layout" />
        <!--        <xsl:param name="xmltempl" select="concat('&amp;_xml_structure%2Fparents%2Fparent%2F%40href=',$id)"/> -->
        <xsl:param name="xmltempl" />
        <xsl:variable name="type" select="substring-before(substring-after(./@ID,'_'),'_')" />
        <xsl:variable name="suffix">
          <xsl:if test="string-length($layout)>0">
            <xsl:value-of select="concat('&amp;layout=',$layout)" />
          </xsl:if>
        </xsl:variable>
        <xsl:if test="./structure/derobjects">
          <tr>
            <td class="metaname" style="vertical-align:top;">
              <xsl:value-of select="i18n:translate('{concat($propPrefix,$objectType,'.[derivates]')}')"/>
            </td>
            <td class="metavalue">
              <xsl:if test="$objectHost != 'local'">
                <a href="{'{'}$staticURL{'}'}">nur auf original Server</a>
              </xsl:if>
              <xsl:if test="$objectHost = 'local'">
                <xsl:for-each select="./structure/derobjects/derobject">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td align="left" valign="top">
                        <div class="derivateBox">
                          <xsl:variable name="deriv" select="@xlink:href" />
                          <xsl:variable name="derivlink" select="concat('mcrobject:',$deriv)" />
                          <xsl:variable name="derivate" select="document($derivlink)" />
                          <xsl:apply-templates select="$derivate/mycorederivate/derivate/internals" />
                          <xsl:apply-templates select="$derivate/mycorederivate/derivate/externals" />
                        </div>
                      </td>
                      <xsl:if test="acl:checkPermission(./@ID,'writedb')">
                        <td valign="top" align="right">
                          <a
                            href="{'{'}$ServletsBaseURL{'}'}MCRStartEditorServlet{'{'}$HttpSession{'}'}?type={'{'}$type{'}'}&amp;re_mcrid={'{'}../../../@ID{'}'}&amp;se_mcrid={'{'}@xlink:href{'}'}&amp;te_mcrid={'{'}@xlink:href{'}'}&amp;todo=saddfile{'{'}$suffix{'}'}{'{'}$xmltempl{'}'}">
                            <img src="{'{'}$WebApplicationBaseURL{'}'}images/workflow_deradd.gif" title="Datei hinzufügen" />
                          </a>
                          <a
                            href="{'{'}$ServletsBaseURL{'}'}MCRStartEditorServlet{'{'}$HttpSession{'}'}?type={'{'}$type{'}'}&amp;re_mcrid={'{'}../../../@ID{'}'}&amp;se_mcrid={'{'}@xlink:href{'}'}&amp;te_mcrid={'{'}@xlink:href{'}'}&amp;todo=seditder{'{'}$suffix{'}'}{'{'}$xmltempl{'}'}">
                            <img src="{'{'}$WebApplicationBaseURL{'}'}images/workflow_deredit.gif" title="Derivat bearbeiten" />
                          </a>
                          <a
                            href="{'{'}$ServletsBaseURL{'}'}MCRStartEditorServlet{'{'}$HttpSession{'}'}?type={'{'}$type{'}'}&amp;re_mcrid={'{'}../../../@ID{'}'}&amp;se_mcrid={'{'}@xlink:href{'}'}&amp;te_mcrid={'{'}@xlink:href{'}'}&amp;todo=sdelder{'{'}$suffix{'}'}{'{'}$xmltempl{'}'}">
                            <img src="{'{'}$WebApplicationBaseURL{'}'}images/workflow_derdelete.gif" title="Derivat löschen" />
                          </a>
                        </td>
                      </xsl:if>
                    </tr>
                  </table>
                </xsl:for-each>
              </xsl:if>
            </td>
          </tr>
          <gxsl:if test="$useIView='on'">
            <gxsl:comment> MCR-IView ..start </gxsl:comment>
            <gxsl:comment> example implementation </gxsl:comment>
            <xsl:if test="$objectHost = 'local'">
              <xsl:for-each select="./structure/derobjects/derobject">
                <xsl:variable name="deriv" select="@xlink:href" />
                <xsl:variable name="firstSupportedFile">
                  <xsl:call-template name="iview.getSupport">
                    <xsl:with-param name="derivID" select="$deriv" />
                  </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                  <xsl:when test="$firstSupportedFile != ''">
                    <tr>
                      <td class="metanone" colspan="2">
                        <br />
                        <xsl:call-template name="iview">
                          <xsl:with-param name="derivID" select="$deriv" />
                          <xsl:with-param name="pathOfImage" select="$firstSupportedFile" />
                          <xsl:with-param name="height" select="'500'" />
                          <xsl:with-param name="width" select="'750'" />
                          <xsl:with-param name="scaleFactor" select="'fitToWidth'" />
                          <xsl:with-param name="display" select="'normal'" />
                          <xsl:with-param name="style" select="'image'" />
                        </xsl:call-template>
                      </td>
                    </tr>
                  </xsl:when>
                </xsl:choose>
              </xsl:for-each>
            </xsl:if>
            <gxsl:comment> MCR - IView ..end </gxsl:comment>
          </gxsl:if>
        </xsl:if>
      </xsl:template>
    </gxsl:if><!-- /derobjects -->
  </gxsl:template>

  <!-- Helper templates -->
  <gxsl:template match="/*" mode="getObjectType">
    <gxsl:choose>
      <gxsl:when test="@ID">
        <gxsl:value-of select="substring-before(substring-after(@ID,'_'),'_')" />
      </gxsl:when>
      <gxsl:otherwise>
        <gxsl:value-of select="local-name()" />
      </gxsl:otherwise>
    </gxsl:choose>
  </gxsl:template>

  <gxsl:template match="*" mode="messageProperty">
    <!-- generates message_*.properties names: see mycoreobject-generateMessages.xsl -->
    <gxsl:variable name="suffix">
      <gxsl:if test="@type">
        <gxsl:value-of select="concat('.',translate(@type,' ','_'))" />
      </gxsl:if>
    </gxsl:variable>
    <gxsl:value-of select="concat($propPrefix,$objectType,'.',local-name(),$suffix)" />
  </gxsl:template>

</gxsl:stylesheet>