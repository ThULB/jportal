<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.42 $ $Date: 2006/10/06 08:15:17 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink" 
    xmlns:mcr="http://www.mycore.org/" 
    xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
    xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
    exclude-result-prefixes="xlink mcr acl i18n">

  <xsl:include href="coreFunctions.xsl" />
  <xsl:include href="generatePage.xsl" />
	
  <xsl:param name="DocumentBaseURL" />
  <xsl:param name="ServletsBaseURL" />
  <xsl:param name="RequestURL" />
  <xsl:param name="CurrentUser" />
  <xsl:param name="CurrentGroups" />
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
  <xsl:param name="lastPage" />
  <xsl:param name="objectHost" select="'local'"/>

  <xsl:variable name="hostfile" select="document('webapp:hosts.xml')"/>
	
  <xsl:template match="/">
    <xsl:call-template name="generatePage" />
  </xsl:template>

  <xsl:template name="objectLink">
    <xsl:param name="obj_id" />
    <!-- 
      LOCAL REQUEST
    -->
    <xsl:if test="$objectHost = 'local'">
      <xsl:variable name="mcrobj" select="document(concat('mcrobject:',$obj_id))/mycoreobject" />
      <xsl:choose>
        <xsl:when test="acl:checkPermission($obj_id,'read')">
          <a href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}">
            <xsl:apply-templates select="$mcrobj" mode="resulttitle" />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <!-- Build Login URL for LoginServlet -->
          <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="LoginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?url=', encoder:encode( string( $RequestURL ) ) )" />
          <xsl:apply-templates select="$mcrobj" mode="resulttitle" />
          &#160;
          <a href="{$LoginURL}">
            <img src="{concat($WebApplicationBaseURL,'images/paper_lock.gif')}" />
          </a>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <!-- 
      REMOTE REQUEST
    -->
    <xsl:if test="$objectHost != 'local'">
      <xsl:variable name="mcrobj"
        select="document(concat('mcrws:operation=MCRDoRetrieveObject&amp;host=',$objectHost,'&amp;ID=',$obj_id))/mycoreobject" />
      <a href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}?host={@host}">
        <xsl:apply-templates select="$mcrobj" mode="resulttitle" />
      </a>
    </xsl:if>
  </xsl:template>

  <xsl:template match="/mycoreobject" mode="resulttitle">
    <!-- Overwrite this with either heigher priority or a more specific match -->
    <xsl:value-of select="@ID" />
  </xsl:template>

  <xsl:template name="printClass">
    <xsl:param name="nodes" />
    <xsl:param name="host" />
    <xsl:for-each select="$nodes">
      <xsl:if test="position() != 1">
        <br />
      </xsl:if>
      <xsl:variable name="classlink">
        <xsl:call-template name="ClassCategLink">
          <xsl:with-param name="classid" select="@classid" />
          <xsl:with-param name="categid" select="@categid" />
          <xsl:with-param name="host" select="$host" />
        </xsl:call-template>
      </xsl:variable>
      <xsl:for-each select="document($classlink)/mycoreclass/categories/category">
        <xsl:variable name="categurl">
          <xsl:if test="url">
            <xsl:choose>
              <!-- MCRObjectID should not contain a ':' so it must be an external link then -->
              <xsl:when test="contains(url/@xlink:href,':')">
                <xsl:value-of select="url/@xlink:href" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',url/@xlink:href,$HttpSession)" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </xsl:variable>
        <xsl:variable name="selectLang">
          <xsl:call-template name="selectLang">
            <xsl:with-param name="nodes" select="./label" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:for-each select="./label[lang($selectLang)]">
          <xsl:choose>
            <xsl:when test="string-length($categurl) != 0">
              <a href="{$categurl}">
                <xsl:if test="$wcms.useTargets = 'yes'">
                  <xsl:attribute name="target">_blank</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="@text" />
              </a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@text" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="printI18N">
    <xsl:param name="nodes" />
    <xsl:variable name="selectPresentLang">
      <xsl:call-template name="selectPresentLang">
        <xsl:with-param name="nodes" select="$nodes" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$nodes[lang($selectPresentLang)]">
        <xsl:for-each select="$nodes[lang($selectPresentLang)]" >
          <xsl:if test="position() != 1">, </xsl:if>
          <xsl:value-of select="." />
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$nodes" />
     </xsl:otherwise>
   </xsl:choose>
  </xsl:template>

  <xsl:template name="webLink">
    <xsl:param name="nodes" />
    <xsl:for-each select="$nodes">
      <xsl:if test="position() != 1">
        <br />
      </xsl:if>
      <xsl:variable name="href" select="@xlink:href" />
      <xsl:variable name="title">
        <xsl:choose>
          <xsl:when test="@xlink:title">
            <xsl:value-of select="@xlink:title" />
          </xsl:when>
          <xsl:when test="@xlink:label">
            <xsl:value-of select="@xlink:label" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@xlink:href" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <a href="{@xlink:href}" target="_blank">
        <xsl:value-of select="$title" />
      </a>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="mailLink">
    <xsl:param name="nodes" />
    <xsl:variable name="selectLang">
      <xsl:call-template name="selectLang">
        <xsl:with-param name="nodes" select="$nodes" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:for-each select="$nodes[lang($selectLang)]">
      <xsl:if test="position() != 1">
        <br />
      </xsl:if>
      <xsl:variable name="email" select="." />
      <a href="mailto:{$email}">
        <xsl:value-of select="$email" />
      </a>
    </xsl:for-each>
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

</xsl:stylesheet>
