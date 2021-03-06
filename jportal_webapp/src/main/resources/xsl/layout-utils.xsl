<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="acl xlink">
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="ServletsBaseURL" />
  <xsl:param name="RequestURL" />
  <xsl:param name="HttpSession" />
  <xsl:param name="objectHost" select="'local'" />
  <xsl:template name="printI18N">
    <xsl:param name="nodes" />
    <xsl:param name="next" />
    <xsl:variable name="selectPresentLang">
      <xsl:call-template name="selectPresentLang">
        <xsl:with-param name="nodes" select="$nodes" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string-length($selectPresentLang)">
        <xsl:for-each select="$nodes[lang($selectPresentLang)]">
          <xsl:if test="position() != 1">
            <xsl:value-of select="$next" disable-output-escaping="yes" />
          </xsl:if>
          <xsl:call-template name="lf2br">
            <xsl:with-param name="string" select="." />
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="$nodes">
          <xsl:if test="position() != 1">
            <xsl:value-of select="$next" disable-output-escaping="yes" />
          </xsl:if>
          <xsl:call-template name="lf2br">
            <xsl:with-param name="string" select="." />
          </xsl:call-template>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="lf2br">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="contains($string,'&#xA;')">
        <xsl:value-of select="substring-before($string,'&#xA;')" />
        <!-- replace line break character by xhtml tag -->
        <br />
        <xsl:call-template name="lf2br">
          <xsl:with-param name="string" select="substring-after($string,'&#xA;')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- ******************************************************** -->
  <!-- * Object Link                                          * -->
  <!-- ******************************************************** -->
  <xsl:template name="objectLink">
    <!-- specify either one of them -->
    <xsl:param name="obj_id" />
    <xsl:param name="mcrobj" />
    <xsl:choose>
      <xsl:when test="$mcrobj">
        <xsl:choose>
          <xsl:when test="$objectHost != 'local' and string-length($objectHost) &gt; 0">
            <!-- 
            REMOTE REQUEST
            -->
            <xsl:variable name="mcrobj"
              select="document(concat('mcrws:operation=MCRDoRetrieveObject&amp;host=',$objectHost,'&amp;ID=',$obj_id))/mycoreobject" />
            <a href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}?host={@host}">
              <xsl:apply-templates select="$mcrobj" mode="resulttitle" />
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="obj_id" select="$mcrobj/@ID" />
            <xsl:choose>
              <xsl:when test="acl:checkPermission($obj_id,'read')">
                <a href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}">
                  <xsl:attribute name="title"><xsl:apply-templates select="$mcrobj" mode="fulltitle" /></xsl:attribute>
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
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="string-length($obj_id)&gt;0">
        <!-- handle old way which may cause a double parsing of mcrobject: -->
        <xsl:choose>
          <xsl:when test="$objectHost != 'local' and string-length($objectHost) &gt; 0">
            <!-- 
            REMOTE REQUEST
            -->
            <xsl:variable name="mcrobj"
              select="document(concat('mcrws:operation=MCRDoRetrieveObject&amp;host=',$objectHost,'&amp;ID=',$obj_id))/mycoreobject" />
            <a href="{$WebApplicationBaseURL}receive/{$obj_id}{$HttpSession}?host={@host}">
              <xsl:apply-templates select="$mcrobj" mode="resulttitle" />
            </a>
          </xsl:when>
          <xsl:otherwise>
            <!-- 
            LOCAL REQUEST
            -->
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
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="printClass">
    <xsl:param name="nodes" />
    <xsl:param name="host" select="$objectHost" />
    <xsl:param name="next" select="''" />
    <xsl:for-each select="$nodes">
      <xsl:if test="position() != 1">
        <xsl:value-of select="$next" disable-output-escaping="yes" />
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

  <xsl:template name="printClassInfo">
    <xsl:param name="nodes" />
    <xsl:param name="host" />
    <xsl:param name="next" />
    <xsl:for-each select="$nodes">
      <xsl:if test="position() != 1">
        <xsl:value-of select="$next" disable-output-escaping="yes" />
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
        <xsl:for-each select="./label[lang($selectLang) and @description]">
          <xsl:choose>
            <xsl:when test="string-length($categurl) != 0">
              <a href="{$categurl}">
                <xsl:value-of select="concat('(',@description,')')" />
              </a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="concat('(',@description,')')" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="webLink">
    <xsl:param name="nodes" />
    <xsl:param name="next" />
    <xsl:for-each select="$nodes">
      <xsl:if test="position() != 1">
        <xsl:value-of select="$next" disable-output-escaping="yes" />
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
      <a href="{@xlink:href}">
        <xsl:value-of select="$title" />
      </a>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="mailLink">
    <xsl:param name="nodes" />
    <xsl:param name="next" />
    <xsl:variable name="selectLang">
      <xsl:call-template name="selectLang">
        <xsl:with-param name="nodes" select="$nodes" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:for-each select="$nodes[lang($selectLang)]">
      <xsl:if test="position() != 1">
        <xsl:value-of select="$next" disable-output-escaping="yes" />
      </xsl:if>
      <xsl:variable name="email" select="." />
      <a href="mailto:{$email}">
        <xsl:value-of select="$email" />
      </a>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="printHistoryDate">
    <xsl:param name="nodes" />
    <xsl:param name="next" />
    <xsl:variable name="selectLang">
      <xsl:call-template name="selectLang">
        <xsl:with-param name="nodes" select="$nodes" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:for-each select="$nodes[lang($selectLang)]">
      <xsl:if test="position() != 1">
        <xsl:value-of select="$next" disable-output-escaping="yes" />
      </xsl:if>
      <xsl:value-of select="text" />
      <xsl:text> (</xsl:text>
      <xsl:value-of select="von" />
      <xsl:text> - </xsl:text>
      <xsl:value-of select="bis" />
      <xsl:text> )</xsl:text>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>