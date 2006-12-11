<?xml version="1.0" encoding="UTF-8"?>

<!-- ============================================== -->
<!-- $Revision: 1.3 $ $Date: 2006/10/20 19:21:32 $ -->
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
  xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="xlink encoder">
    
    <xsl:variable name="backto_url" select="/mcr_user/backto_url" />
    <xsl:variable name="guest_id" select="/mcr_user/guest_id" />
    <xsl:variable name="guest_pwd" select="/mcr_user/guest_pwd" />
    <xsl:variable
      name="href-login"
      select="concat($ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?url=', encoder:encode(string($backto_url)))">
    </xsl:variable>
    <xsl:variable
      name="href-user"
      select="concat($ServletsBaseURL, 'MCRUserServlet',$HttpSession,'?url=', encoder:encode(string($backto_url)))">
    </xsl:variable>


<!-- The main template -->
<xsl:template match="/mcr_user">
    <!-- At first we display the current user in a head line. -->
    <xsl:call-template name="usersub"/>

    <!-- +
         | There are three possible error-conditions: wrong password, unknown user and disabled
         | user. If one of these conditions occured, the corresponding information will be
         | presented at the top of the page.
         + -->
    <div id="userStatus">
        <xsl:call-template name="userStatus"/>
    </div>
    <xsl:call-template name="userAction"/>
</xsl:template>
<!-- Generates a header for the login actions -->

<xsl:template name="usersub">
 <table id="metaHeading" cellpadding="0" cellspacing="0">
  <tr>
   <td class="titles">
     <xsl:value-of select="$heading"/>
   </td>
  </tr>
 </table>
 <!-- IE Fix for padding and border -->
 <hr/>
</xsl:template>

</xsl:stylesheet>
