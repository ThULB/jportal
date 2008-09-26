<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 965 $ $Date: 2008-03-14 13:17:49 +0100 (Fr, 14 Mär 2008) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- headline template from editor.xsl rev13507 -->
  <xsl:template match="headline" priority="2">
    <tr>
      <td>
        <xsl:call-template name="editor.set.css">
          <xsl:with-param name="class" select="'editorHeadline'" />
        </xsl:call-template>
        <xsl:call-template name="editor.set.anchor" />
        <xsl:apply-templates select="text | output">
          <xsl:with-param name="var" select="../@var" />
        </xsl:apply-templates>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="editor" priority="2">
    <form>
      <xsl:call-template name="editor.set.css">
        <xsl:with-param name="class" select="'editor'" />
      </xsl:call-template>
      <xsl:call-template name="editor.set.form.attrib" />
      <table>
        <xsl:call-template name="editor.set.css">
          <xsl:with-param name="class" select="'editor'" />
        </xsl:call-template>
        <!-- ======== build nested panel structure ======== -->
        <xsl:apply-templates select="components" />
      </table>
    </form>
  </xsl:template>
  <xsl:template match="components" priority="2">
      <!-- ======== if exists, output editor headline ======== -->
    <xsl:apply-templates select="headline" />
      <!-- ======== if validation errors exist, display message ======== -->
    <xsl:apply-templates select="ancestor::editor/failed" />
    <tr>
      <td>
        <!--
          Workaround for browser behavior: Use a hidden submit button, so that when user hits the enter key, really
          submit the form, instead of executing the [+] button of the first repeater
        -->
        <xsl:if test="//repeater">
          <input style="width:0px; height:0px; border-width:0px; float:left;" value="submit" type="submit"
            tabindex="99" />
        </xsl:if>
          <!-- ======== start at the root panel ======== -->
        <xsl:apply-templates select="panel[@id=current()/@root]">
          <xsl:with-param name="var" select="@var" />
        </xsl:apply-templates>
      </td>
    </tr>
  </xsl:template>
<!-- ======== validation errors exist ======== -->
  <xsl:template match="failed" priority="2">
    <tr>
      <td class="editorValidationMessage">
        <xsl:for-each select="ancestor::editor/validationMessage">
          <xsl:call-template name="output.label">
            <xsl:with-param name="usefont" select="'yes'" />
          </xsl:call-template>
        </xsl:for-each>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="panel[count(cell)=1]" priority="2">
    <xsl:param name="var" />
    <xsl:param name="pos" select="1" />
    <xsl:if test="ancestor::editor/failed/field[@sortnr=$pos]">
      <div>
        <xsl:attribute name="class">editorValidationFailed</xsl:attribute>
        <xsl:variable name="message">
          <xsl:for-each select="//condition[@id=ancestor::editor/failed/field[@sortnr=$pos]/@condition]">
            <xsl:call-template name="output.label" />
          </xsl:for-each>
        </xsl:variable>
        <img border="0" align="absbottom" src="{$WebApplicationBaseURL}images/validation-error.png" alt="{$message}"
          title="{$message}" />
      </div>
    </xsl:if>
    <xsl:for-each select="cell">
      <xsl:call-template name="cell">
        <xsl:with-param name="var" select="$var" />
        <xsl:with-param name="pos">
          <xsl:value-of select="$pos" />
          <xsl:if test="$pos">
            <xsl:text>.</xsl:text>
          </xsl:if>
          <xsl:choose>
            <xsl:when test="@sortnr">
              <xsl:value-of select="@sortnr" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="2" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>
    <!-- ======== handle hidden fields ======== -->
    <xsl:apply-templates select="ancestor::components/panel[@id = current()/include/@ref]/hidden|hidden">
      <xsl:with-param name="cells" select="cell" />
      <xsl:with-param name="var" select="$var" />
      <xsl:with-param name="pos" select="$pos" />
    </xsl:apply-templates>
    <!-- ======== handle panel validation conditions ======== -->
    <xsl:for-each select="ancestor::components/panel[@id = current()/include/@ref]/condition|condition">
      <input type="hidden" name="_cond-{$var}" value="{@id}" />
      <input type="hidden" name="_sortnr-{$var}" value="{$pos}" />
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>