<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.8 $ $Date: 2006/10/20 19:21:32 $ -->
<!-- ============================================== -->

<!-- +
     | This stylesheet controls the Web-Layout of the "CreatePwdDialog"- and "ChangePwd"-modes
     | of the UserServlet. In the first mode empty password fields for the change password
     | dialog are presented. The passwords are sent back to the UserServlet. In case there
     | are errors (e.g. mismatching passwords) the UserServlet will send the error messages
     | back to this stylesheet (using the LayoutServlet). The following syntax of the XML-stream
     | is provided by the UserServlet:
     |
     | <mcr_user new_pwd_mismatch="true|false"
     |           old_pwd_mismatch="true|false">
     |   <error>...</error>                       (Here messages from exceptions might appear.)
     |   <guest_id>...</guest_id>
     |   <guest_pwd>...</guest_pwd>
     |   <backto_url>...<backto_url>
     | </mcr_user>
     |
     | The XML stream is sent to the Layout Servlet and finally handled by this stylesheet.
     |
     | Authors: Detlev Degenhardt
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
    <xsl:value-of select="concat(i18n:translate('users.tasks.currentAccount'),' :')"/>&#160;&#160;
    [&#160;<xsl:value-of select="$CurrentUser"/>&#160;]
</xsl:variable>

<xsl:variable name="MainTitle" select="i18n:translate('titles.mainTitle')"/>
<xsl:variable name="PageTitle" select="i18n:translate('titles.pageTitle.changePass')"/>
<xsl:variable name="Servlet" select="'UserServlet'"/>
<xsl:template name="userAction">
    <!-- +
         | There are 3 possible error-conditions: the provided new passwords are not equal, the
         | old password is incorrect or something happened while setting the password in the
         | core system of mycore (i.e. the user manager). If one of these conditions occured,
         | the corresponding information will be presented at the top of the page.
         + -->

    <form action="{$ServletsBaseURL}MCRUserServlet{$HttpSession}?mode=ChangePwd" method="post">
        <input type="hidden" name="url" value="{backto_url}"/>
        <table id="userAction">
            <tr>
                <td class="inputCaption"><xsl:value-of select="concat(i18n:translate('users.tasks.changePass.newPass'),' :')"/></td>
                <td class="inputField"><input name="pwd_1" class="text" type="password" maxlength="30"/></td>
            </tr>
            <tr>
                <td class="inputCaption"><xsl:value-of select="concat(i18n:translate('users.tasks.changePass.repeatPass'),' :')"/></td>
                <td class="inputField"><input name="pwd_2" class="text" type="password" maxlength="30"/></td>
            </tr>
            <tr >
                <th class="inputHead" colspan="2">
                    <xsl:value-of select="i18n:translate('users.tasks.changePass.securityNote')"/>
                </th>
            </tr>
            <tr>
                <td class="inputCaption"><xsl:value-of select="i18n:translate('users.tasks.changePass.oldPass')"/></td>
                <td class="inputField"><input name="oldpwd" class="text" type="password" maxlength="30"/></td>
            </tr>
        </table>
        <hr/>
        <div class="submitButton">
            <a class="submitButton" href="{$href-login}&amp;uid={$CurrentUser}&amp;mode=Select">
                <xsl:value-of select="concat('&lt;&lt; ', i18n:translate('buttons.cancel'))"/>
            </a>
            <span style="width:15px;">&#160;</span>
            <input type="submit" class="submitButton" value="{i18n:translate('users.tasks.changePass.submit')} &gt;&gt;" name="ChangePwdSubmit"/>
        </div>
    </form>
</xsl:template>

<xsl:template name="userStatus">
    <xsl:if test="/mcr_user/@new_pwd_mismatch='true' or /mcr_user/@old_pwd_mismatch='true' or /mcr_user/error">
        <xsl:value-of select="i18n:translate('users.tasks.changePass.failed')"/>
    </xsl:if>
    <xsl:if test="@new_pwd_mismatch='true'">
        <xsl:value-of select="i18n:translate('users.tasks.changePass.newPassMismatch')"/>
    </xsl:if>
    <xsl:if test="@old_pwd_mismatch='true'">
        <xsl:value-of select="i18n:translate('users.tasks.changePass.oldPassMismatch')"/>
    </xsl:if>
    <xsl:if test="/mcr_user/error">
        <xsl:value-of select="/mcr_user/error"/>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
