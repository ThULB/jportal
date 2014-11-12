<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xed="http://www.mycore.de/xeditor" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:jp="http://www.mycore.de/components/jp" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:encoder="xalan://java.net.URLEncoder"
  exclude-result-prefixes="xed xlink jp i18n encoder">

  <xsl:include href="copynodes.xsl" />

  <xsl:template match="jp:section">
    <div>
      <xsl:apply-templates select="@*|node()"/>
    </div>
  </xsl:template>

  <xsl:template match="jp:template[@name='title']">
    <div class="row text-center">
      <xsl:if test="@left">
        <xsl:attribute name="class">col-md-12 text-left</xsl:attribute>
      </xsl:if>
      <label>
        <xed:output i18n="{@i18n}" />
      </label>
    </div>
  </xsl:template>

  <!-- 1 line is split into 3 parts: 1. title, 2. input (input, textArea, select) and 3. buttons -->
  <!-- Form: titel | input | buttons -->
  <xsl:template match="jp:template[contains('textInput|textInputSm|selectInput|textArea', @name)]">
    <div class="row">
      <xsl:if test="@small">
        <xsl:attribute name="class"></xsl:attribute>	
      </xsl:if>

      <!-- 1. part: title class="form-group col-md-12" -->
      <div class="col-md-2 text-right">
        <xsl:if test="@small">
          <xsl:attribute name="class">col-md-3</xsl:attribute>
          <xsl:attribute name="style">padding-left: 0px</xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="." mode="title" />
      </div>

      <!-- 2. part: input class="form-group col-md-8" -->
      <div class="col-md-8"> 
        <xsl:if test="@small">
          <xsl:attribute name="class">col-md-9</xsl:attribute>
          <xsl:attribute name="style">padding: 0px</xsl:attribute>
        </xsl:if>
        <xed:bind xpath="{@xpath}">
          <xsl:apply-templates select="jp:template[@name='textInput']" />
          <xsl:if test="@name!='textInputSm'">
            <div>
              <xsl:attribute name="class">form-group {$xed-validation-marker}</xsl:attribute>
              <xsl:if test="@add='10'">
                <xsl:attribute name="style">margin-bottom: 0px</xsl:attribute>
              </xsl:if>
              <xsl:apply-templates select="." mode="input" />
              <xsl:apply-templates select="." mode="required" />
            </div>
          </xsl:if>
        </xed:bind>
        <!-- add text <span> -->
        <xsl:apply-templates select="span[@type='addT']" />
        <xsl:apply-templates select="jp:template[@type='subselect']" mode="subselect"/>

        <xsl:if test="@add">
          <div class="form-group">
            <xsl:attribute name="style">
              <xsl:value-of select="concat('margin-top:',@add, 'px')" />
            </xsl:attribute> 
            <xsl:apply-templates select="xed:bind" />
          </div>
        </xsl:if>
      </div>
      <!-- 3.part: buttons -->
      <xsl:apply-templates select="." mode="buttons" />
    </div>
  </xsl:template>

  <xsl:template match="jp:template" mode="title">
    <xsl:choose>
      <xsl:when test="@required">
        <label>
          <xed:output i18n="{@i18n}" />
        </label>
      </xsl:when>
      <xsl:when test="@i18n">
        <span>
          <xed:output i18n="{@i18n}" />
        </span>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="jp:template[@name='textInput']" mode="input">
    <input type="text" class="form-control" maxlength="{@maxlength}" tabindex="1">
      <xsl:if test="@placeholder">
        <xsl:attribute name="placeholder">
          <xsl:value-of select="concat('{i18n:', @placeholder, '}')" />
        </xsl:attribute>
      </xsl:if>
    </input>
  </xsl:template>

  <xsl:template match="jp:template" mode="buttons">
    <xsl:if test="@buttons">
      <div class="col-md-2">
        <xed:controls>insert remove up down</xed:controls>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="jp:template" mode="required">
    <xsl:if test="@required">
      <xed:validate display="here" required="true">
        <div class="alert alert-danger" role="alert">
          <xed:output i18n="jp.editor.requiredInput" />
        </div>
      </xed:validate>
    </xsl:if>
  </xsl:template>

  <xsl:template match="jp:template[@name='textArea']" mode="input">
    <textarea class="form-control" wrap="" rows="3" cols="48" tabindex="1" />
  </xsl:template>

  <xsl:template match="jp:template[@name='selectInput']" mode="input">
    <xsl:apply-templates select="." mode="input_select" />
  </xsl:template>

  <xsl:template match="jp:template[@classification]" mode="input_select">
    <!-- load classification -->
    <select class="form-control" id="type" tabindex="1" size="1">
      <option value="">
        <xed:output i18n="editor.common.select" />
      </option>
      <xed:include>
        <xsl:attribute name="uri">
          <xsl:value-of select="concat('xslStyle:items2options:classification:editor:-1:children:', @classification)" />
        </xsl:attribute>
      </xed:include>
    </select>
  </xsl:template>

  <xsl:template match="jp:template[@list]" mode="input_select">
    <select class="form-control" id="type" tabindex="1" size="1">
      <option value="">
        <xed:output i18n="editor.common.select" />
      </option>
      <xed:include uri="{@list}" />
    </select>
  </xsl:template>

  <xsl:template match="jp:template[@option]" mode="input_select">
    <select class="form-control" id="type" tabindex="1" size="1">
      <option value="">
        <xed:output i18n="editor.common.select" />
      </option>
      <xsl:for-each select="option">
        <option style="padding-left: 0px;" value="{@value}">
          <xed:output i18n="{@i18n}" />
        </option>
      </xsl:for-each>
    </select>
  </xsl:template>

  <xsl:template match="jp:template[@type='subselect']" mode="subselect" >
    <xed:bind xpath="{@xpath}">
      <xed:output value="@xlink:title" />
    </xed:bind>
    <xed:bind xpath="{@xpath2}">
      <xed:output value="@xlink:href" />
    </xed:bind>

    <xed:if test="@xlink:title != ''">
      <xed:output value="@xlink:title" />
    </xed:if>
    <xed:if test="@xlink:href != ''">
      <label>
        (
        <xed:output value="@xlink:href" />
        )
      </label>
    </xed:if>

    <!-- Hier 2 buttons um personen auszuwählen und institutionen -->
    <div class="form-group">
      <button type="submit" xed:target="subselect"
        xed:href="{concat('solr/subselect?XSL.subselect.type=person&amp;XSL.subselect.varpath.SESSION=/mycoreobject/metadata/participants/participant&amp;XSL.subselect.webpage.SESSION=', @value)}"
        class="btn btn-default" tabindex="1">
        <xed:output i18n="jp.editor.person.select" />
      </button>
      <button type="submit" xed:target="subselect"
        xed:href="{concat('solr/subselect?XSL.subselect.type=jpinst&amp;XSL.subselect.varpath.SESSION=/mycoreobject/metadata/participants/participant&amp;XSL.subselect.webpage.SESSION=', @value)}"
        class="btn btn-default" tabindex="1">
        <xed:output i18n="jp.editor.inst.select" />
      </button>
    </div>
  </xsl:template>
</xsl:stylesheet>