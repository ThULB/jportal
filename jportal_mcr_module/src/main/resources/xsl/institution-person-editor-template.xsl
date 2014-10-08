<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xed="http://www.mycore.de/xeditor"
  xmlns:perInstitution="http://www.mycore.de/components/PersonenInstitution">

  <xsl:include href="copynodes.xsl" />

  <xsl:template match="perInstitution:template[@name='title']">
    <div class="col-md-12 text-center">
      <label>
        <xed:output i18n="{@i18n}" />
      </label>
    </div>
  </xsl:template>

  <!-- 1 line is split into 3 parts: 1. title, 2. input (input, textArea, select) and 3. buttons -->
  <xsl:template match="perInstitution:template[contains('textInput|selectInput|textArea', @name)]">
    <div class="col-md-12">

      <!-- 1. part: title class="form-group col-md-12" -->
      <div class="col-md-2 text-right">
        <xsl:apply-templates select="." mode="title" />
      </div>

      <!-- 2. part: input class="form-group col-md-8" -->
      <div class="col-md-8">
        <xed:bind xpath="{@xpath}">
          <div>
            <xsl:attribute name="class">form-group {$xed-validation-marker}</xsl:attribute>
            <xsl:choose>
              <!-- dropbox -->
              <xsl:when test="@name='selectInput'">
                <xsl:choose>
                  <!-- load select -->
                  <xsl:when test="@classification">
                    <xsl:apply-templates select="select" />
                  </xsl:when>
                  <!-- load options -->
                  <xsl:otherwise>
                    <select class="form-control" id="type" tabindex="1" size="1">
                      <xsl:for-each select="option">
                        <option style="padding-left: 0px;" value="{@value}">
                          <xed:output i18n="{@i18n}" />
                        </option>
                      </xsl:for-each>
                    </select>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <!-- textArea -->
              <xsl:when test="@name='textArea'">
                <textarea class="form-control" wrap="" rows="3" cols="48" tabindex="1" />
              </xsl:when>
              <!-- input -->
              <xsl:otherwise>
                <input type="text" class="form-control" maxlength="{@maxlength}" tabindex="1">
                  <xsl:if test="@placeholder">
                    <xsl:attribute name="placeholder">
                        <xsl:value-of select="concat('{i18n:', @placeholder, '}')" />
                      </xsl:attribute>
                  </xsl:if>
                </input>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="." mode="required" />
          </div>
        </xed:bind>
        <xsl:if test="@add">
          <div class="form-group">
            <xsl:attribute name="style">margin-top: -10px</xsl:attribute>
            <xsl:apply-templates select="xed:bind" />
          </div>
        </xsl:if>
      </div>
      <!-- 3.part: buttons -->
      <xsl:apply-templates select="." mode="buttons" />
    </div>
  </xsl:template>

  <!-- to creat new person, needed 1 extra title in the first half -->
  <xsl:template match="perInstitution:template[@name='textInputSm']">
    <!-- first title -->
    <div class="col-md-12">
      <div class="col-md-2 text-center">
        <xsl:if test="@i18nH">
          <span>
            <xed:output i18n="{@i18nH}" />
          </span>
        </xsl:if>
      </div>
      <!-- second title -->
      <div class="col-md-2">
        <xsl:apply-templates select="." mode="title" />
      </div>
      <!-- input part -->
      <xed:bind xpath="{@xpath}">
        <div>
          <xsl:attribute name="class">form-group {$xed-validation-marker} col-md-6</xsl:attribute>
          <input type="text" class="form-control" maxlength="{@maxlength}" tabindex="1" />
          <xsl:apply-templates select="." mode="required" />
        </div>
      </xed:bind>
      <!-- last part buttons -->
      <xsl:apply-templates select="." mode="buttons" />
    </div>
  </xsl:template>

  <xsl:template match="perInstitution:template" mode="title">
    <xsl:choose>
      <xsl:when test="@required">
        <label>
          <xed:output i18n="{@i18n}" />
        </label>
      </xsl:when>
      <xsl:otherwise>
        <span>
          <xed:output i18n="{@i18n}" />
        </span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="perInstitution:template" mode="buttons">
    <xsl:if test="@buttons">
      <div class="col-md-2">
        <xed:controls>insert remove up down</xed:controls>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="perInstitution:template" mode="required">
    <xsl:if test="@required">
      <xed:validate display="here" required="true">
        <div class="alert alert-danger" role="alert">
          <xed:output i18n="jp.editor.requiredInput" />
        </div>
      </xed:validate>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>