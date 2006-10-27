<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.7 $ $Date: 2006/05/26 15:28:26 $ -->
<!-- ============================================== -->

<!-- +
     | This stylesheet controls the Web-Layout of the Login Servlet. The Login Servlet
     | gathers information about the session, user ID, password and calling URL and
     | then tries to login the user by delegating the login request to the user manager.
     | Depending on whether the login was successful or not, the Login Servlet generates
     | the following XML output stream:
     |
     | <mcr_user unknown_user="true|false"
     |           user_disabled="true|false"
     |           invalid_password="true|false">
     |   <guest_id>...</guest_id>
     |   <guest_pwd>...</guest_pwd>
     |   <backto_url>...<backto_url>
     | </mcr_user>
     |
     | The XML stream is sent to the Layout Servlet and finally handled by this stylesheet.
     |
     | Authors: Detlev Degenhardt, Thomas Scheffler
     + -->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" 
  xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="xlink encoder i18n">
	
<xsl:include href="mcr_user-lang.xsl"/>
<xsl:include href="MyCoReLayout.xsl" />

<xsl:variable name="heading">
    <xsl:value-of select="i18n:translate('users.tasks.currentAccount')"/>&#160;&#160;
    [&#160;<xsl:value-of select="$CurrentUser"/>&#160;]
</xsl:variable>
    
<xsl:variable name="MainTitle" select="i18n:translate('titles.mainTitle')"/>
<xsl:variable name="PageTitle" select="i18n:translate('titles.pageTitle.login')"/>

<xsl:template name="userAction">
    <form action="{$ServletsBaseURL}MCRLoginServlet{$HttpSession}" method="post">
        <input type="hidden" name="url" value="{backto_url}"/>
        <table id="userAction">
            <!-- Here come the input fields... -->
            <tr>
                <td class="inputCaption"><xsl:value-of select="concat(i18n:translate('users.tasks.login.account'),' :')"/></td>
                <td class="inputField"><input name="uid" type="text" class="text" maxlength="30"/></td>
            </tr>
            <tr>
                <td class="inputCaption"><xsl:value-of select="concat(i18n:translate('users.tasks.login.password'),' :')"/></td>
                <td class="inputField"><input name="pwd" type="password" class="text" maxlength="30"/></td>
            </tr>
        </table>
        <hr/>
        <div class="submitButton">
            <a class="submitbutton" href="{$href-login}&amp;uid={$CurrentUser}">
                &#160;&#160;<xsl:value-of select="concat('&lt;&lt; ', i18n:translate('buttons.cancel'))"/>&#160;&#160;
            </a>
            <span style="width:30px;">&#160;</span>
            <a class="submitbutton" href="{$href-login}&amp;uid={$guest_id}&amp;pwd={$guest_pwd}">
              &#160;&#160;<xsl:value-of select="i18n:translate('buttons.logout')"/>&#160;&#160;
            </a>
            <span style="width:15px;">&#160;</span>
                <input type="submit" class="submitButton" value="{i18n:translate('buttons.login')} &gt;&gt;"/>
        </div>
    </form>
</xsl:template>
<xsl:template name="userStatus">
    <xsl:if test="/mcr_user/@invalid_password='true' or /mcr_user/@unknown_user='true' or /mcr_user/@user_disabled='true'">
        <xsl:value-of select="i18n:translate('users.tasks.login.failed')"/>
    </xsl:if>
    <xsl:if test="/mcr_user/@invalid_password='true'">
        <xsl:value-of select="i18n:translate('users.tasks.login.invalidPwd')"/>
    </xsl:if>
    <xsl:if test="/mcr_user/@unknown_user='true'">
        <xsl:value-of select="i18n:translate('users.tasks.login.userUnknown')"/>
    </xsl:if>
    <xsl:if test="/mcr_user/@user_disabled='true'">
        <xsl:value-of select="i18n:translate('users.tasks.login.userDisabled')"/>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
