<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xed="http://www.mycore.de/xeditor"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xalan="http://xml.apache.org/xalan" xmlns:jp="http://www.mycore.de/components/jp"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools" xmlns:encoder="xalan://java.net.URLEncoder"
                exclude-result-prefixes="xed xlink xalan jp i18n encoder">

  <xsl:include href="copynodes.xsl" />
  <xsl:include href="coreFunctions.xsl" />
  <xsl:include href="jp-layout-functions.xsl" />

  <xsl:param name="xedIncParam" select="''" />
  <xsl:param name="CurrentLang" />

  <xsl:template match="jp:journalID">
    <xsl:choose>
      <xsl:when test="$xedIncParam != '{$parent}' and $xedIncParam != ''">
        <xsl:value-of select="layoutTools:getJournalID($xedIncParam)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="./*" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xed:cancel[@name='cancelUrl']">
    <xsl:copy>
      <xsl:attribute name="url">
      <xsl:choose>
        <xsl:when test="$xedIncParam != '{$parent}' and $xedIncParam != ''">
          <xsl:value-of select="concat(@url,$xedIncParam)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat(@url,@default)" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="jp:section">
    <div>
      <xsl:apply-templates select="@*|node()" />
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

  <!-- 1 line is split into 3 parts: 1. title, 2. input (input, textArea, 
  	select) and 3. buttons -->
  <!-- Form: titel | input | buttons -->
  <xsl:template match="jp:template[contains('textInput|textInputSm|selectInput|textArea|date_select|jpdate_select|logoThumbnail|subselect|geo_subselect|gnd_location', @name)]">
    <div class="row">
      <xsl:if test="@small">
        <xsl:attribute name="class"></xsl:attribute>
      </xsl:if>
      <xsl:if test="@parentClass">
        <xsl:attribute name="class">
          <xsl:value-of select="@parentClass" />
        </xsl:attribute>
      </xsl:if>

	  <!-- 1. part: title -->
      <div class="col-md-2 text-right">
        <xsl:if test="@small">
          <xsl:attribute name="class">col-md-3</xsl:attribute>
          <xsl:attribute name="style">padding-left: 0px</xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="." mode="title" />
      </div>

	  <!-- 2. part: input -->
      <div class="col-md-8">
        <xsl:if test="@small">
          <xsl:attribute name="class">col-md-9</xsl:attribute>
          <xsl:attribute name="style">padding: 0px</xsl:attribute>
        </xsl:if>
        <xsl:if test="@myClass">
          <xsl:attribute name="class">
	        <xsl:value-of select="@myClass" />
		  </xsl:attribute>
        </xsl:if>
        <xed:bind xpath="{@xpath}">
          <xsl:apply-templates select="jp:template[@name='textInput']" />
          <xsl:apply-templates select="jp:template[@name='selectInput']" />
          <xsl:if test="@name!='textInputSm' and @name!='date_select' and @name!='jpdate_select'">
            <div>
              <xsl:attribute name="class">form-group {$xed-validation-marker}</xsl:attribute>
              <xsl:if test="contains(@inputClass, 'date-field')">
                <xsl:attribute name="class">form-group input-group</xsl:attribute>
              </xsl:if>
              <xsl:if test="@containerId">
                <xsl:attribute name="id"><xsl:value-of select="@containerId"></xsl:value-of></xsl:attribute>
              </xsl:if>
              <xsl:if test="@validate='interdependentSelect' or @bottom='littleSpace'">
                <xsl:attribute name="style">margin-bottom: 5px</xsl:attribute>
              </xsl:if>
              <xsl:apply-templates select="." mode="input" />
              <xsl:apply-templates select="." mode="validation" />
            </div>
          </xsl:if>
          <xsl:if test="@name='date_select'">
            <div class="form-group">
              <xsl:apply-templates select="jp:template[@type='date']" mode="date_select" />
            </div>
          </xsl:if>
          <xsl:if test="@name='jpdate_select'">
            <div class="form-group jpdate-group" id="jpdate-group-{@id}">
              <div>
                <input type="radio" name="date_type_select-{@id}" value="date" id="jpdate-type-date-{@id}" />
                <label for="jpdate-type-date-{@id}"> Datum</label>
                <input type="radio" name="date_type_select-{@id}" value="range"  id="jpdate-type-range-{@id}" />
                <label for="jpdate-type-range-{@id}"> Datumsbereich</label>
              </div>
              <xsl:apply-templates select="jp:template[@type='date']" mode="jpdate_select" />
              <xsl:apply-templates select="jp:template[@type='textInput']" mode="jpdate_select" />
            </div>
          </xsl:if>
        </xed:bind>
		<!-- add text <span> -->
        <xsl:apply-templates select="span[@type='addT']" />
        <xsl:apply-templates select="jp:template[@type='subselect']" mode="subselect" />
      </div>
	  <!-- 3.part: buttons -->
      <xsl:apply-templates select="." mode="buttons" />
    </div>
  </xsl:template>

  <xsl:template match="jp:template" mode="title">
    <xsl:choose>
      <xsl:when test="@validate = 'required' or @validate = 'requiredPersonName'">
        <label>
          <xed:output i18n="{@i18n}" />
        </label>
      </xsl:when>
      <xsl:when test="@i18n">
        <span>
          <xed:output i18n="{@i18n}" />
        </span>
      </xsl:when>
      <xsl:when test="@classification">
        <span>
          <xsl:call-template name="jp.printClass">
            <xsl:with-param name="nodes" select="document(concat('classification:metadata:1:children:', @classification))/mycoreclass" />
            <xsl:with-param name="lang" select="$CurrentLang" />
          </xsl:call-template>
        </span>
      </xsl:when>
      <xsl:when test="@loadLabel">
        <xed:include cacheable="false" uri="{@loadLabel}" />
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="jp:template[@name='textInput' and @maxlength]" mode="input">
    <xsl:call-template name="jp-editor-textInput">
      <xsl:with-param name="maxlength" select="@maxlength" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="jp:template[@name='textInput' and not(@maxlength)]" mode="input">
    <xsl:call-template name="jp-editor-textInput">
      <xsl:with-param name="maxlength" select="'256'" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="jp-editor-textInput">
    <xsl:param name="maxlength" />
    <input type="text" class="form-control" maxlength="{$maxlength}" tabindex="1">
      <xsl:if test="@placeholder">
        <xsl:attribute name="placeholder">
          <xsl:value-of select="concat('{i18n:', @placeholder, '}')" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@inputClass">
        <xsl:attribute name="class">
          <xsl:value-of select="@inputClass" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@generateID">
        <xsl:attribute name="id">
          <xsl:value-of select="@generateID" />
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

  <xsl:template match="jp:template[@validate='display-validation-message']" mode="validation">
    <xed:display-validation-message />
  </xsl:template>

  <xsl:template match="jp:template[@validate='required']" mode="validation">
    <xed:validate display="here" required="true">
      <div class="alert alert-danger" role="alert">
        <xed:output i18n="jp.editor.requiredInput" />
      </div>
    </xed:validate>
  </xsl:template>

  <xsl:template match="jp:template[@validate='requiredPersonName']" mode="validation">
    <xed:validate display="here" test="(string-length(../lastName) &gt; 0) or (string-length(../firstName) &gt; 0) or (string-length(../name) &gt; 0)">
      <div class="alert alert-danger" role="alert">
        <xed:output i18n="jp.editor.requiredPersonName" />
      </div>
    </xed:validate>
  </xsl:template>

  <xsl:template match="jp:template[@validate='interdependentSelect']" mode="validation">
    <xed:validate display="here" test="not(string-length(.) = 0 and string-length(..) != 0)">
      <div class="alert alert-danger" role="alert">
        <xed:output i18n="jp.editor.requiredSelect" />
      </div>
    </xed:validate>
  </xsl:template>

  <xsl:template match="jp:template[@validate='interdependentInput']" mode="validation">
    <xed:validate display="here" test="not(string-length({@selectXpath}) = 0 and string-length(text()) != 0)">
      <div class="alert alert-danger" role="alert">
        <xed:output i18n="jp.editor.requiredInput" />
      </div>
    </xed:validate>
  </xsl:template>

  <xsl:template match="jp:template[@validate='subselect']" mode="validation">
    <xed:validate display="here" test="((string-length(.) = 0) and (string-length(../@xlink:href) = 0)) or ((string-length(.) &gt; 0) and (string-length(../@xlink:href) &gt; 0))">
      <div class="alert alert-danger" role="alert">
        <xed:output i18n="jp.editor.select_help" />
      </div>
    </xed:validate>
  </xsl:template>

  <xsl:template match="jp:template[@name='textArea']" mode="input">
    <textarea class="form-control" wrap="" rows="3" cols="48" tabindex="1" />
  </xsl:template>

  <xsl:template match="jp:template[@name='logoThumbnail']" mode="input">
    <xed:bind xpath="{@xpathInput1}">
      <div class="col-md-6 text-center">
        <a id="thumbLogoPlain" class="thumbnail">
          <p>Click hier um Logo auszuwählen.</p>
          <h5>
            <xsl:value-of select="i18n:translate('jp.editor.inst.sLogo')"></xsl:value-of>
          </h5>
        </a>
        <span id="delLogoPlain" class="glyphicon glyphicon-remove" style="display:none"></span>
        <input type="text" style="display:none" />
      </div>
    </xed:bind>
    <xed:bind xpath="{@xpathInput2}">
      <div class="col-md-6 text-center">
        <a id="thumbLogoText" class="thumbnail">
          <p>Click hier um Logo auszuwählen.</p>
          <h5>
            <xsl:value-of select="i18n:translate('jp.editor.inst.logoWText')"></xsl:value-of>
          </h5>
        </a>
        <span id="delLogoText" class="glyphicon glyphicon-remove" style="display:none"></span>
        <input type="text" style="display:none" />
      </div>
    </xed:bind>
    <xsl:apply-templates select="." mode="modal" />
  </xsl:template>

  <xsl:template match="jp:template[@name='selectInput']" mode="input">
    <xsl:apply-templates select="." mode="input_select" />
  </xsl:template>

  <xsl:template match="jp:template[@classification]" mode="input_select">
	<!-- load classification -->
    <select class="form-control" id="type" tabindex="1" size="1">
      <xsl:copy-of select="@on" />
      <xsl:attribute name="data-classid">
        <xsl:value-of select="@classification" />
      </xsl:attribute>
      <xsl:if test="@selectClass">
        <xsl:attribute name="class">
          <xsl:value-of select="@selectClass" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="not(@noPleaseSelect) or @noPleaseSelect = 'false'">
        <option value="" selected="">
          <xed:output i18n="editor.common.select" />
        </option>
      </xsl:if>
      <xsl:variable name="classID">
        <xsl:choose>
          <xsl:when test="@classification = '{xedIncParam}'">
            <xsl:value-of select="$xedIncParam" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@classification" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xed:include>
        <xsl:attribute name="uri">
          <xsl:value-of select="concat('xslStyle:items2options:classification:editor:-1:children:', $classID)" />
        </xsl:attribute>
      </xed:include>
    </select>
  </xsl:template>

  <xsl:template match="jp:template[@list]" mode="input_select">
    <select class="form-control" id="type" tabindex="1" size="1">
      <option value="" selected="">
        <xed:output i18n="editor.common.select" />
      </option>
      <xed:include uri="{@list}" cacheable="false" />
    </select>
  </xsl:template>

  <xsl:template match="jp:template[@option]" mode="input_select">
    <select class="form-control jp-personSelect-select" id="type" tabindex="1" size="1">
      <xsl:if test="@noXpath">
        <xsl:attribute name="name"></xsl:attribute>
        <xsl:attribute name="id"><xsl:value-of select="@noXpath" /></xsl:attribute>
      </xsl:if>
      <xsl:if test="not(@noPleaseSelect) or @noPleaseSelect = 'false'">
        <option value="">
          <xsl:if test="@selected != ''">
            <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
          <xed:output i18n="editor.common.select" />
        </option>
      </xsl:if>
      <xsl:for-each select="option">
        <option style="padding-left: 0px;" value="{@value}">
          <xsl:attribute name="title">
			<xsl:value-of select="concat('{i18n:', @i18n, '}')" />
		  </xsl:attribute>
          <xsl:if test="@selected">
            <xsl:attribute name="selected"></xsl:attribute>
          </xsl:if>
          <xed:output i18n="{@i18n}" />
        </option>
      </xsl:for-each>
    </select>
  </xsl:template>

  <xsl:template match="jp:template[@type='subselect']" mode="subselect">
    <div class="jp-subSelect-name">
      <div class="jp-name-display"></div>
      <xed:bind xpath="{@xpath}">
        <input type="text" style="display:none" />
      </xed:bind>
      <xed:bind xpath="{@xpath2}">
        <input type="text" style="display:none" />
      </xed:bind>
    </div>

	<!-- 2 buttons for selection of person or institution _ subselect -->
    <div class="form-group">
      <xsl:apply-templates mode="subselectButtons" select="@objectTypes"/>
    </div>
  </xsl:template>


  <xsl:template mode="subselectButtons" match="@objectTypes[contains(., '|')]|text()[contains(., '|')]">
    <xsl:apply-templates mode="subselectButtons" select="xalan:nodeset(substring-before(.,'|'))"/>
    <xsl:apply-templates mode="subselectButtons" select="xalan:nodeset(substring-after(.,'|'))"/>
  </xsl:template>

  <xsl:template mode="subselectButtons" match="@objectTypes[not(contains(., '|'))]|text()[not(contains(., '|'))]">
    <button type="button" class="btn btn-default jp-subSelect-button" data-type="{.}" tabindex="1">
      <xed:output i18n="{concat('jp.editor.select.', .)}" />
    </button>
  </xsl:template>

  <xsl:template match="jp:template[@name='subselect']" mode="input">
    <div class="jp-subSelect-name">
      <div class="jp-name-display"></div>
      <xed:bind xpath="@xlink:title">
        <input type="text" style="display:none" />
      </xed:bind>
      <xed:bind xpath="@xlink:href">
        <input type="text" style="display:none" />
      </xed:bind>
    </div>
    <div class="form-group">
      <button type="button" data-type="jpinst" tabindex="1">
        <xsl:attribute name="class">
          <xsl:value-of select="'btn btn-default jp-subSelect-button'" />
        </xsl:attribute>
        <xed:output i18n="jp.editor.select.jpinst" />
      </button>
    </div>
  </xsl:template>

  <xsl:template match="jp:template[@name='geo_subselect']" mode="input">
    <div class="jp-geo-input">
      <div class="jp-geo-coordinates-display"></div>
      <xed:bind xpath=".">
        <input type="text" style="display:none" class="jp-geo-input-data" />
      </xed:bind>
    </div>
    <div class="form-group">
      <button type="button" tabindex="1">
        <xsl:attribute name="class">
          <xsl:value-of select="concat('btn btn-default ', @subselectClass)" />
        </xsl:attribute>
        <xed:output i18n="jp.editor.inst.geoCoordinates" />
      </button>
    </div>
  </xsl:template>

  <xsl:template match="jp:template[@name='gnd_location']" mode="input">
    <div class="jp-gnd-location-input">
      <div class="jp-gnd-location-input-display"></div>
      <xed:bind xpath=".">
        <input type="text" style="display:none" class="jp-gnd-location-input-data" />
      </xed:bind>
      <xed:bind xpath="@id">
        <input type="text" style="display:none" class="jp-gnd-location-input-id" />
      </xed:bind>
      <xed:bind xpath="@label">
        <input type="text" style="display:none" class="jp-gnd-location-input-label" />
      </xed:bind>
      <xed:bind xpath="@areaCode">
        <input type="text" style="display:none" class="jp-gnd-location-input-areaCode" />
      </xed:bind>
    </div>
    <div class="form-inline jp-gnd-location-form">
      <button type="button" tabindex="1" class="btn btn-default jp-gnd-location-select">
        <xed:output i18n="metaData.jparticle.linkedLocations.apply" />
      </button>
    </div>
  </xsl:template>

  <xsl:template match="jp:template[@name='modal']">
    <div class="modal fade jp-modal" id="{@type}Select-modal" tabindex="-1" role="dialog" aria-labelledby="{@type}Select-modal-title" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close {@type}Select-cancel" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">x</span>
            </button>
            <h4 class="modal-title" id="{@type}Select-modal-title"></h4>
          </div>
          <div id="{@type}Select-modal-body" class="modal-body">
          </div>
          <div class="modal-footer">
            <button id="{@type}Select-cancel-button" type="button" class="btn btn-danger {@type}Select-cancel" data-dismiss="modal"></button>
            <button id="{@type}Select-send" type="button" class="btn btn-primary" disabled="disabled"></button>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="jp:template[@name='modal-geo']">
    <div class="modal fade jp-modal" id="geo-select-modal" tabindex="-1" role="dialog" aria-labelledby="geo-select-modal-title" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close geo-select-modal-cancel" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">x</span>
            </button>
            <h4 class="modal-title" id="geo-select-modal-title"></h4>
          </div>
          <div class="modal-body geo-select-modal-body">
            <div class="input-group">
              <input class="form-control geo-select-modal-search" />
              <span class="input-group-btn">
                <button type="button" class="btn btn-default geo-select-modal-search-button">Suche</button>
              </span>
            </div>
            <div class="geo-select-modal-map-container">
              <div class="geo-select-modal-search-results">
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-danger geo-select-modal-cancel" data-dismiss="modal">Abbrechen</button>
            <button type="button" class="btn btn-primary geo-select-modal-send" disabled="disabled">Übernehmen</button>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="jp:template[@type='date']" mode="date_select">
    <div>
      <xsl:attribute name="id">
        <xsl:value-of select="@id_container" />
      </xsl:attribute>
      <xsl:attribute name="class">
        <xsl:value-of select="@class" />
      </xsl:attribute>
      <xed:bind xpath="{@xpath}">
        <input class="form-control date-field" type="text" placeholder="yyyy-MM-dd" maxlength="10">
          <xsl:attribute name="id">
            <xsl:value-of select="@id_input" />
          </xsl:attribute>
          <xsl:if test="@class_input">
            <xsl:attribute name="class">
              <xsl:value-of select="@class_input" />
            </xsl:attribute>
          </xsl:if>
        </input>
      </xed:bind>
      <xsl:if test="@seperator='true'">
        <span id="dateSeperator" class="input-group-addon hidden-xs jp-layout-sharpBorderRight">-</span>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template match="jp:template[@type='date']" mode="jpdate_select">
    <div>
      <xsl:attribute name="class">
        <xsl:value-of select="@class" />
      </xsl:attribute>
      <xed:bind xpath="{@xpath}">
        <input class="form-control date-field" type="text" placeholder="yyyy-MM-dd" maxlength="10">
        </input>
      </xed:bind>
      <xsl:if test="@seperator='true'">
        <span id="dateSeperator" class="input-group-addon hidden-xs jp-layout-sharpBorderRight">-</span>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template match="jp:template[@type='textInput']" mode="jpdate_select">
    <xed:bind xpath="{@xpath}">
      <xsl:call-template name="jp-editor-textInput">
        <xsl:with-param name="maxlength" select="'256'" />
      </xsl:call-template>
    </xed:bind>
  </xsl:template>

 <!-- DYNAMIC CLASSIFICATION'S -->
  <xsl:template match="jp:template[@name='dynamicBinding']">
    <xsl:variable name="settings" select="document('../xml/layoutDefaultSettings.xml')/layoutSettings" />
    <xsl:variable name="type" select="@type" />
    <xsl:apply-templates select="$settings/editor/*[name() = $type]/bind" mode="dynamicBinding" />
  </xsl:template>

  <xsl:template match="bind" mode="dynamicBinding">
    <xed:bind xpath="{@xpath}">
      <xsl:apply-templates select="row" mode="dynamicBinding" />
    </xed:bind>
  </xsl:template>

  <xsl:template match="row" mode="dynamicBinding">
    <xsl:variable name="templateXML">
      <jp:template name="selectInput" selectClass="form-control dynamicBinding">
        <xsl:attribute name="xpath">
          <xsl:value-of select="concat(@xpath, '[@inherited=&quot;0&quot;][@classid=&quot;', @class, '&quot;]/@categid')" />
        </xsl:attribute>
        <xsl:attribute name="classification">
          <xsl:value-of select="@class" />
        </xsl:attribute>
        <xsl:if test="@on">
          <xsl:attribute name="on">
            <xsl:value-of select="@on" />
          </xsl:attribute>
        </xsl:if>
      </jp:template>
    </xsl:variable>
    <xsl:apply-templates select="xalan:nodeset($templateXML)/jp:template" />
  </xsl:template>

  <xsl:template match="row[@repeatable]" mode="dynamicBinding">
    <xed:repeat min="0" max="99">
      <xsl:attribute name="xpath">
        <xsl:value-of select="concat(@xpath, '[@inherited=&quot;0&quot;][@classid=&quot;', @class, '&quot;]')" />
      </xsl:attribute>
      <xsl:variable name="templateXML">
        <jp:template name="selectInput" selectClass="form-control dynamicBinding" buttons="true">
          <xsl:attribute name="xpath">
            <xsl:value-of select="'@categid'" />
          </xsl:attribute>
          <xsl:attribute name="classification">
            <xsl:value-of select="@class" />
          </xsl:attribute>
          <xsl:if test="@on">
            <xsl:attribute name="on">
              <xsl:value-of select="@on" />
            </xsl:attribute>
          </xsl:if>
        </jp:template>
      </xsl:variable>
      <xsl:apply-templates select="xalan:nodeset($templateXML)/jp:template" />
    </xed:repeat>
  </xsl:template>

</xsl:stylesheet>
