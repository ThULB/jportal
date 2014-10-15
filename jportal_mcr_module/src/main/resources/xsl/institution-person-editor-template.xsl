<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xed="http://www.mycore.de/xeditor"
  xmlns:perInstitution="http://www.mycore.de/components/PersonenInstitution"  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:encoder="xalan://java.net.URLEncoder" exclude-result-prefixes="encoder" >

  <xsl:include href="copynodes.xsl" />

  <xsl:template match="perInstitution:template[@name='title']">
    <div class="col-md-12 text-center">
      <xsl:if test="@left">
        <xsl:attribute name="class">col-md-12 text-left</xsl:attribute>
      </xsl:if>
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
            <xsl:if test="@addT">
              <xsl:attribute name="style">margin-bottom: 0px</xsl:attribute>
            </xsl:if>
            <xsl:choose>
              <!-- dropbox -->
              <xsl:when test="@name='selectInput'">
                <xsl:choose>
                  <!-- load classification -->
                  <xsl:when test="@classification">
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
                  </xsl:when>
                  <!-- load items -->
                  <xsl:when test="@list">
                    <list class="form-control" type="dropdown" id="typ">
                      <item value="">
                        <label><xed:output i18n="editor.common.select" /></label>
                      </item>
                      <xed:include uri="{@list}" />
                   <!--  need this for jportal_getALLClassIDs: (but somehow it works without it sometimes)    -->
                      <xsl:if test="@cacheable">
                        <xsl:attribute name="style">cacheable="false"</xsl:attribute>
                      </xsl:if> 
                    </list>
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
                <!-- größe ändern abstellen "resize: none;" -->
                <textarea class="form-control" wrap="" rows="3" cols="48" tabindex="1" />
              </xsl:when>
              <!-- input -->
              <xsl:otherwise>
                <xsl:apply-templates select="." mode="input" />
              </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="." mode="required" />
          </div>
        </xed:bind>
        <xsl:if test="@addT">
          <xsl:apply-templates select="span" />
        </xsl:if>
        <xsl:if test="@subselect">
          
          <xed:bind xpath="{@xpath2}"><xed:output value="@xlink:title" /></xed:bind>
          <xed:bind xpath="{@xpath3}"><xed:output value="@xlink:href" /></xed:bind>
          
          <xed:output value="@xlink:title" />
          <label> (<xed:output value="@xlink:href" />)</label>
          
          <!-- Hier 2 buttons um personen auszuwählen und institutionen -->
          <div class="form-group">
            <button type="submit" xed:target="subselect" xed:href="{concat('solr/subselect?',encoder:encode('XSL.subselect.type=person&amp;XSL.subselect.varpath.SESSION=/mycoreobject/metadata/participants/participant&amp;XSL.subselect.webpage.SESSION=editor/common/jpjournal/editor-jpjournal.xed?'))}" class="btn btn-default" tabindex="1" ><xed:output i18n="jp.editor.person.select" /></button>
            <button type="submit" xed:target="subselect" xed:href="{concat('solr/subselect?',encoder:encode('XSL.subselect.type=jpinst&amp;XSL.subselect.varpath.SESSION=/mycoreobject/metadata/participants/participant&amp;XSL.subselect.webpage.SESSION=editor/common/jpjournal/Feditor-jpjournal.xed?'))}" class="btn btn-default" tabindex="1" ><xed:output i18n="jp.editor.inst.select" /></button> 
          </div>
          <!-- ?XSL.subselect.type=person&XSL.subselect.session.SESSION=h7rkmczi9&XSL.subselect.varpath.SESSION=/mycoreobject/metadata/participants/participant&XSL.subselect.webpage.SESSION=editor%2Fcommon%2Fjpjournal%2Feditor-jpjournal.xed%3F -->
          
<!--           http://localhost:8291/servlets/solr/subselect?XSL.subselect.type=person&_xed_subselect_session=1-39 -->
   
        </xsl:if>
        <xsl:if test="@add">
          <div class="form-group">
            <xsl:choose>
              <xsl:when test="@addT">
                <xsl:attribute name="style">margin-top: 10px</xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="style">margin-top: -10px</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
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
          <xsl:apply-templates select="." mode="input" />
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

  <xsl:template match="perInstitution:template" mode="input">
    <input type="text" class="form-control" maxlength="{@maxlength}" tabindex="1">
      <xsl:if test="@placeholder">
        <xsl:attribute name="placeholder">
          <xsl:value-of select="concat('{i18n:', @placeholder, '}')" />
        </xsl:attribute>
      </xsl:if>
    </input>
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