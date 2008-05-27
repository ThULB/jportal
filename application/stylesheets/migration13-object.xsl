<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2007-03-26 06:15:29 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
> 

<xsl:output method="xml" encoding="UTF-8"/>

<xsl:variable name="newline">
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:attribute-set name="tag">
  <xsl:attribute name="class"><xsl:value-of select="./@class" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
  <xsl:attribute name="notinherit"><xsl:value-of select="./@notinherit" /></xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="tagisodate">
  <xsl:attribute name="class"><xsl:value-of select="'MCRMetaISO8601Date'" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
  <xsl:attribute name="notinherit"><xsl:value-of select="./@notinherit" /></xsl:attribute>

<!--
  <xsl:attribute name="parasearch">
    <xsl:value-of select="./@parasearch" />
  </xsl:attribute>
  <xsl:attribute name="textsearch">
    <xsl:value-of select="./@textsearch" />
  </xsl:attribute>
-->
</xsl:attribute-set>

<xsl:attribute-set name="subtagisodate">
  <xsl:attribute name="type"><xsl:value-of select="./@type" /></xsl:attribute>
  <xsl:attribute name="inherited"><xsl:value-of select="./@inherited" />  </xsl:attribute>
</xsl:attribute-set>

<xsl:template match="/">
  <mycoreobject>
    <xsl:copy-of select="mycoreobject/@ID"/>
    <xsl:copy-of select="mycoreobject/@label"/>
    <xsl:choose>
      <xsl:when test="mycoreobject/@version">
        <xsl:copy-of select="mycoreobject/@version"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="version">1.3</xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:copy-of select="mycoreobject/@xsi:noNamespaceSchemaLocation"/>
    <xsl:value-of select="$newline"/>
    <structure>
<!--      <xsl:copy-of select="mycoreobject/structure/parents"/> -->
      <xsl:apply-templates select="mycoreobject/structure/parents"/>
    </structure>
    <xsl:value-of select="$newline"/>
    <metadata xml:lang="de">
    <xsl:value-of select="$newline"/>
    <xsl:for-each select="mycoreobject/metadata/*">
      <xsl:for-each select="." >
        <xsl:choose>
          <xsl:when test="@class = 'MCRMetaDate'">
            <xsl:if test="./*/@inherited = '0'">
<!--              <xsl:copy use-attribute-sets="tagisodate">-->
              <xsl:copy>
  <xsl:attribute name="class"><xsl:value-of select="'MCRMetaISO8601Date'" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
  <xsl:attribute name="notinherit"><xsl:value-of select="./@notinherit" /></xsl:attribute>
                <xsl:for-each select="*" >
                  <xsl:if test="@inherited = '0'">
<!--                    <xsl:copy use-attribute-sets="subtagisodate" >-->
                    <xsl:copy>
  <xsl:attribute name="type"><xsl:value-of select="./@type" /></xsl:attribute>
  <xsl:attribute name="inherited"><xsl:value-of select="./@inherited" />  </xsl:attribute>
                      <xsl:for-each select="." >
                        <xsl:value-of select="."/>
                      </xsl:for-each>
                    </xsl:copy>
                    <xsl:value-of select="$newline"/>
                  </xsl:if>
                </xsl:for-each>
              </xsl:copy>
              <xsl:value-of select="$newline"/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="./*/@inherited = '0'">
<!--              <xsl:copy use-attribute-sets="tag">-->
              <xsl:copy>
  <xsl:attribute name="class"><xsl:value-of select="./@class" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
  <xsl:attribute name="notinherit"><xsl:value-of select="./@notinherit" /></xsl:attribute>
                <xsl:for-each select="*" >
                  <xsl:if test="@inherited = '0'">
                    <xsl:copy-of select="."/>
                    <xsl:value-of select="$newline"/>
                  </xsl:if>
                </xsl:for-each>
              </xsl:copy>
              <xsl:value-of select="$newline"/>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:for-each>
    </metadata>
    <xsl:value-of select="$newline"/>
    <service>
      <xsl:value-of select="$newline"/>
      <xsl:for-each select="mycoreobject/service/servdates">
        <xsl:choose>
          <xsl:when test="@class = 'MCRMetaDate'">
            <xsl:if test="./*/@inherited = '0'">
<!--              <xsl:copy use-attribute-sets="tagisodate">-->
              <xsl:copy>
  <xsl:attribute name="class"><xsl:value-of select="'MCRMetaISO8601Date'" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
  <xsl:attribute name="notinherit"><xsl:value-of select="./@notinherit" /></xsl:attribute>
                <xsl:for-each select="*" >
                  <xsl:if test="@inherited = '0'">
<!--                    <xsl:copy use-attribute-sets="subtagisodate" >-->
                    <xsl:copy>
  <xsl:attribute name="type"><xsl:value-of select="./@type" /></xsl:attribute>
  <xsl:attribute name="inherited"><xsl:value-of select="./@inherited" />  </xsl:attribute>
                      <xsl:for-each select="." >
                        <xsl:value-of select="."/>
                      </xsl:for-each>
                    </xsl:copy>
                    <xsl:value-of select="$newline"/>
                  </xsl:if>
                </xsl:for-each>
              </xsl:copy>
              <xsl:value-of select="$newline"/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="./*/@inherited = '0'">
<!--              <xsl:copy use-attribute-sets="tag">-->
              <xsl:copy>
  <xsl:attribute name="class"><xsl:value-of select="./@class" /></xsl:attribute>
  <xsl:attribute name="heritable"><xsl:value-of select="./@heritable" /></xsl:attribute>
  <xsl:attribute name="notinherit"><xsl:value-of select="./@notinherit" /></xsl:attribute>
                <xsl:for-each select="*" >
                  <xsl:if test="@inherited = '0'">
                    <xsl:copy-of select="."/>
                    <xsl:value-of select="$newline"/>
                  </xsl:if>
                </xsl:for-each>
              </xsl:copy>
              <xsl:value-of select="$newline"/>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:value-of select="$newline"/>
<!--      <servacls class="MCRMetaAccessRule" heritable="false" notinherit="false" parasearch="false" textsearch="false"> -->
      <servacls class="MCRMetaAccessRule" heritable="false" notinherit="false">
        <!-- READ rule for web application -->
        <!-- member of the admin group has all rights -->
        <!-- read right have all groups with the Access:... privilege and all groups they are member of this -->
        <!-- group. If the IP is set the group condition will be AND-connect with the IP area --> 
        <xsl:value-of select="$newline"/>
        <servacl inherited="0" permission="read">
          <condition format="xml">
            <boolean operator="and">
              <xsl:variable name="access">
                <xsl:call-template name="getAccess" />
              </xsl:variable>
              <xsl:choose>
                <xsl:when test="$access = 'public'">
                  <boolean operator="or" />
                  <boolean operator="and" />
                  <boolean operator="or" />
                </xsl:when>
                <xsl:otherwise>
                  <boolean operator="or" >
                    <condition field="group" operator="=" value="admingroup" />
                    <xsl:call-template name="buildAccess" />
                  </boolean>
                </xsl:otherwise>
              </xsl:choose>
            </boolean>
          </condition>
        </servacl>
        <!-- WRITEDB rule for write access in the server -->
        <!-- member of the admin group has all rights -->
        <!-- write access have all users in the old servflags User: -->
        <xsl:value-of select="$newline"/>
        <servacl inherited="0" permission="writedb">
          <condition format="xml">
             <boolean operator="and">
               <boolean operator="or">
                 <condition field="group" operator="=" value="admingroup" />
                 <xsl:call-template name="buildAccess" />
                 <xsl:call-template name="buildUsers" />
              </boolean>
              <boolean operator="and" />
              <boolean operator="or" />
            </boolean>
          </condition>
        </servacl>
        <!-- DELETEDB rule for delete access in the server -->
        <!-- member of the admin group has all rights -->
        <!-- delete access have all users in the old servflags User: -->
        <xsl:value-of select="$newline"/>
        <servacl inherited="0" permission="deletedb">
          <condition format="xml">
            <boolean operator="and">
              <boolean operator="or">
                <condition field="group" operator="=" value="admingroup" />
                 <xsl:call-template name="buildAccess" />
                 <xsl:call-template name="buildUsers" />
              </boolean>
              <boolean operator="and" />
              <boolean operator="or" />
            </boolean>
          </condition>
        </servacl>
        <xsl:value-of select="$newline"/>
      </servacls>
      <xsl:value-of select="$newline"/>
    </service>
    <xsl:value-of select="$newline"/>
  </mycoreobject>
</xsl:template>

<xsl:template name="getAccess">
  <xsl:for-each select="mycoreobject/service/servflags/servflag[contains(text(),'Access')]">
    <xsl:choose>
      <xsl:when test="contains(text(),'Access: ')" >
        <xsl:copy-of select="substring-after(text(),'Access: ')" />
      </xsl:when>
      <xsl:when test="contains(text(),'Access:')" >
        <xsl:copy-of select="substring-after(text(),'Access:')" />
      </xsl:when>
    </xsl:choose>
  </xsl:for-each>
</xsl:template>

<xsl:template name="buildAccess">
  <xsl:for-each select="mycoreobject/service/servflags/*">
    <xsl:if test="contains(text(),'Access:')" >
      <xsl:variable name="access">
        <xsl:copy-of select="substring-after(text(),'Access:')" />
      </xsl:variable>
      <xsl:for-each select="document('migrate/user/groups.xml')/mycoregroup/group">
        <xsl:for-each select="group.privileges/privileges.privilege" >
          <xsl:if test="contains(text(),$access)" >
            <condition field="group" operator="=">
              <xsl:attribute name="value">
                <xsl:value-of select="../../@ID" />
              </xsl:attribute>
            </condition>
          </xsl:if>
        </xsl:for-each> 
      </xsl:for-each>
    </xsl:if>
  </xsl:for-each>
</xsl:template>

<xsl:template name="buildUsers">
  <xsl:variable name="type">
    <xsl:copy-of select="substring-before(substring-after(mycoreobject/@ID,'_'),'_')" />
  </xsl:variable>
  <xsl:if test="$type = 'author' or $type = 'document'" >
    <xsl:for-each select="mycoreobject/service/servflags/*">
      <xsl:if test="contains(text(),'User:')" >
        <xsl:variable name="user">
          <xsl:copy-of select="substring-after(text(),'User:')" />
        </xsl:variable>
        <condition field="user" operator="=">
          <xsl:attribute name="value">
            <xsl:value-of select="$user" />
          </xsl:attribute>
        </condition>
        <xsl:for-each select="document('migrate/user/users.xml')/mycoreuser/user[@ID = $user]">
          <xsl:value-of select="user.primary_group" />
          <xsl:if test="user.primary_group != 'admingroup'">
            <xsl:variable name="editor">
              <xsl:copy-of select="concat(substring-before(user.primary_group,'author'),'editor',substring-after(user.primary_group,'author'))" />
            </xsl:variable>
            <condition field="group" operator="=">
              <xsl:attribute name="value">
                <xsl:value-of select="$editor" />
              </xsl:attribute>
            </condition>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>
    </xsl:for-each>
  </xsl:if>
</xsl:template>

<xsl:template match='@*|node()'>
  <xsl:copy>
    <xsl:apply-templates select='@*|node()'/>
  </xsl:copy>
</xsl:template>

<xsl:template match='@parasearch | @textsearch' />


</xsl:stylesheet>

