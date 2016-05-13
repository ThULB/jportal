<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xed="http://www.mycore.de/xeditor"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:jp="http://www.mycore.de/components/jp"
	xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
	xmlns:layoutTools="xalan://fsu.jportal.xml.LayoutTools" xmlns:encoder="xalan://java.net.URLEncoder"
	exclude-result-prefixes="xed xlink jp i18n encoder">

	<xsl:include href="copynodes.xsl" />
	<xsl:param name="xedIncParam" select="''" />

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
	<xsl:template
		match="jp:template[contains('textInput|textInputSm|selectInput|textArea|date_select|logoThumbnail', @name)]">
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
					<xsl:if test="@name!='textInputSm' and @name!='date_select'">
						<div>
							<xsl:attribute name="class">form-group {$xed-validation-marker}</xsl:attribute>
							<xsl:if test="contains(@inputClass, 'date-field')">
								<xsl:attribute name="class">form-group input-group</xsl:attribute>
							</xsl:if>
							<xsl:if test="@containerId">
								<xsl:attribute name="id"><xsl:value-of select="@containerId"></xsl:value-of></xsl:attribute>
							</xsl:if>
							<xsl:if
								test="@validate='interdependentSelect' or @bottom='littleSpace'">
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
  		<xsl:when test="@on" >
    		<span>
      			<xsl:value-of select="document(concat('classification:metadata:1:children:', @classification))/mycoreclass/label/@text" />
    		</span>
  		</xsl:when>
			<xsl:when test="@loadLabel">
				<xed:include cacheable="false" uri="{@loadLabel}" />
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="jp:template[@name='textInput']" mode="input">
		<input type="text" class="form-control" maxlength="{@maxlength}"
			tabindex="1" >
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
			<xsl:if test="contains(@inputClass, 'date-field')">
				<span class="input-group-addon btn btn-default jp-layout-white">
					<span class="glyphicon glyphicon-calendar"></span>
				</span>
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
		<xed:validate display="here"
			test="not(string-length({@selectXpath}) = 0 and string-length(text()) != 0)">
			<div class="alert alert-danger" role="alert">
				<xed:output i18n="jp.editor.requiredInput" />
			</div>
		</xed:validate>
	</xsl:template>
  
	<xsl:template match="jp:template[@validate='subselect']" mode="validation">
		<xed:validate display="here"
			test="((string-length(.) = 0) and (string-length(../@xlink:href) = 0)) or ((string-length(.) &gt; 0) and (string-length(../@xlink:href) &gt; 0))">
			<div class="alert alert-danger" role="alert">
				<xed:output i18n="jp.editor.select_help" />
			</div>
		</xed:validate>
	</xsl:template>

	<xsl:template match="jp:template[@name='textArea']" mode="input">
		<textarea class="form-control" wrap="" rows="3" cols="48"
			tabindex="1" />
	</xsl:template>
	
	<xsl:template match="jp:template[@name='logoThumbnail']" mode="input">
		<xed:bind xpath="{@xpathInput1}">
			<div class="col-md-6 text-center">
				<a id="thumbLogoPlain" class="thumbnail" >
					<p>Click hier um Logo auszuwählen.</p>
					<h5><xsl:value-of select="i18n:translate('jp.editor.inst.sLogo')"></xsl:value-of></h5>
				</a>
				<span id="delLogoPlain" class="glyphicon glyphicon-remove" style="display:none"></span>
				<input type="text" style="display:none" />
			</div>
		</xed:bind>
		<xed:bind xpath="{@xpathInput2}">
			<div class="col-md-6 text-center">
				<a id="thumbLogoText" class="thumbnail">
					<p>Click hier um Logo auszuwählen.</p>
					<h5><xsl:value-of select="i18n:translate('jp.editor.inst.logoWText')"></xsl:value-of></h5>
				</a>
				<span id="delLogoText" class="glyphicon glyphicon-remove" style="display:none"></span>
				<input type="text" style="display:none" />
			</div>
		</xed:bind>
		<xsl:apply-templates select="." mode="modal"/>
	</xsl:template>

	<xsl:template match="jp:template[@name='selectInput']"
		mode="input">
		<xsl:apply-templates select="." mode="input_select" />
	</xsl:template>

	<xsl:template match="jp:template[@classification]" mode="input_select">
		<!-- load classification -->
		<select class="form-control" id="type" tabindex="1" size="1">
  	  <xsl:copy-of select="@on" />
    	<xsl:if test="contains(@selectClass, 'journalTyp')">
      	<xsl:attribute name="data-classid">
        		<xsl:value-of select="@classification" />
      	</xsl:attribute>
    	</xsl:if>
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
          <xsl:value-of
					select="concat('xslStyle:items2options:classification:editor:-1:children:', $classID)" />
        </xsl:attribute>
			</xed:include>
		</select>
	</xsl:template>
  
<!--   <xsl:template match="jp:template[@classification='journalTyp']" mode="input_select"> -->
<!--     <xsl:variable name="docSettings" select="document('../xml/layoutDefaultSettings.xml')" /> -->
    
<!--     <xsl:for-each select="$docSettings/layoutSettings/journalSettings/types/type"> -->
<!--       <xsl:choose> -->
<!--         <xsl:when test="@repeatable = 'true'" > -->
<!--           <xed:repeat xpath="journalType[@inherited='0']" min="0" max="10"> -->
<!--             <xsl:if test="not(position() = 1)"> -->
<!--               <xsl:attribute name="xpath"> -->
<!--                 <xsl:value-of select="concat('journalType[',position(),']')" /> -->
<!--               </xsl:attribute> -->
<!--               <xed:bind xpath="@inherited" set="0" /> -->
<!--             </xsl:if> -->
<!--             <xed:bind xpath="@classid" set="{@class}" /> -->
<!--             <xed:bind xpath="@categid" > -->
<!--               <div class="form-group"> -->
<!--                 <xsl:attribute name="style"> -->
<!--                   <xsl:if test="not(position() = 1)"> -->
<!--                     <xsl:text>margin-top: 15px</xsl:text> -->
<!--                   </xsl:if> -->
<!--                 </xsl:attribute> -->
<!--                 <xsl:if test="@hidden = 'true'"> -->
<!--                   <xsl:attribute name="class">row collapse</xsl:attribute> -->
<!--                 </xsl:if> -->
<!--                 <xsl:if test="not(position() = 1)"> -->
<!--                   <div class="col-md-2 text-center"> -->
<!--                     <span> -->
<!--                       <xsl:value-of select="document(concat('classification:metadata:1:children:', @class))/mycoreclass/label/@text" /> -->
<!--                     </span> -->
<!--                   </div> -->
<!--                 </xsl:if> -->
<!--                 <div class="col-md-7"> -->
<!--                   <xsl:if test="position() = 1"> -->
<!--                     <xsl:attribute name="class"></xsl:attribute> -->
<!--                   </xsl:if> -->
<!--                   <select class="form-control" id="type" tabindex="1" size="1"> -->
<!--                     <xsl:copy-of select="@on" /> -->
<!--                     <xsl:if test="position() = 1"> -->
<!--                       <xsl:attribute name="class">form-control journalTyp</xsl:attribute> -->
<!--                       <xsl:attribute name="data-classid"> -->
<!--                         <xsl:value-of select="@class" /> -->
<!--                       </xsl:attribute> -->
<!--                     </xsl:if> -->
<!--                     <option value="" selected=""> -->
<!--                       <xed:output i18n="editor.common.select" /> -->
<!--                     </option> -->
<!--                     <xsl:variable name="classID"> -->
<!--                       <xsl:choose> -->
<!--                         <xsl:when test="@class = '{xedIncParam}'"> -->
<!--                           <xsl:value-of select="$xedIncParam" /> -->
<!--                         </xsl:when> -->
<!--                         <xsl:otherwise> -->
<!--                           <xsl:value-of select="@class" /> -->
<!--                         </xsl:otherwise> -->
<!--                       </xsl:choose> -->
<!--                     </xsl:variable> -->
<!--                     <xed:include> -->
<!--                       <xsl:attribute name="uri"> -->
<!--                         <xsl:value-of select="concat('xslStyle:items2options:classification:editor:-1:children:', $classID)" /> -->
<!--                       </xsl:attribute> -->
<!--                     </xed:include> -->
<!--                   </select> -->
<!--                 </div> -->
<!--                 <xsl:if test="@repeatable = 'true'"> -->
<!--                   <div class="col-md-3"> -->
<!--                    <xed:controls>insert remove up down</xed:controls> -->
<!--                  </div>   -->
<!--                 </xsl:if> -->
<!--               </div> -->
<!--             </xed:bind> -->
<!--           </xed:repeat> -->
<!--         </xsl:when> -->
<!--         <xsl:otherwise> -->
<!--           <xed:bind xpath="journalType[@inherited='0']" > -->
<!--             <xsl:if test="not(position() = 1)"> -->
<!--               <xsl:attribute name="xpath"> -->
<!--                 <xsl:value-of select="concat('journalType[',position(),']')" /> -->
<!--               </xsl:attribute> -->
<!--               <xed:bind xpath="@inherited" set="0" /> -->
<!--             </xsl:if> -->
<!--             <xed:bind xpath="@classid" set="{@class}" /> -->
<!--             <xed:bind xpath="@categid" > -->
<!--               <div class="form-group"> -->
<!--                 <xsl:attribute name="style"> -->
<!--                   <xsl:if test="not(position() = 1)"> -->
<!--                     <xsl:text>margin-top: 15px</xsl:text> -->
<!--                   </xsl:if> -->
<!--                 </xsl:attribute> -->
<!--                 <xsl:if test="@hidden = 'true'"> -->
<!--                   <xsl:attribute name="class">row collapse</xsl:attribute> -->
<!--                 </xsl:if> -->
<!--                 <xsl:if test="not(position() = 1)"> -->
<!--                   <div class="col-md-2 text-center"> -->
<!--                     <span> -->
<!--                       <xsl:value-of select="document(concat('classification:metadata:1:children:', @class))/mycoreclass/label/@text" /> -->
<!--                     </span> -->
<!--                   </div> -->
<!--                 </xsl:if> -->
<!--                 <div class="col-md-7"> -->
<!--                   <xsl:if test="position() = 1"> -->
<!--                     <xsl:attribute name="class"></xsl:attribute> -->
<!--                   </xsl:if> -->
<!--                   <select class="form-control" id="type" tabindex="1" size="1"> -->
<!--                     <xsl:copy-of select="@on" /> -->
<!--                     <xsl:if test="position() = 1"> -->
<!--                       <xsl:attribute name="class">form-control journalTyp</xsl:attribute> -->
<!--                       <xsl:attribute name="data-classid"> -->
<!--                         <xsl:value-of select="@class" /> -->
<!--                       </xsl:attribute> -->
<!--                     </xsl:if> -->
<!--                     <option value="" selected=""> -->
<!--                       <xed:output i18n="editor.common.select" /> -->
<!--                     </option> -->
<!--                     <xsl:variable name="classID"> -->
<!--                       <xsl:choose> -->
<!--                         <xsl:when test="@class = '{xedIncParam}'"> -->
<!--                           <xsl:value-of select="$xedIncParam" /> -->
<!--                         </xsl:when> -->
<!--                         <xsl:otherwise> -->
<!--                           <xsl:value-of select="@class" /> -->
<!--                         </xsl:otherwise> -->
<!--                       </xsl:choose> -->
<!--                     </xsl:variable> -->
<!--                     <xed:include> -->
<!--                       <xsl:attribute name="uri"> -->
<!--                         <xsl:value-of select="concat('xslStyle:items2options:classification:editor:-1:children:', $classID)" /> -->
<!--                       </xsl:attribute> -->
<!--                     </xed:include> -->
<!--                   </select> -->
<!--                 </div> -->
<!--                 <xsl:if test="@repeatable = 'true'"> -->
<!--                   <div class="col-md-3"> -->
<!--                    <xed:controls>insert remove up down</xed:controls> -->
<!--                  </div>   -->
<!--                 </xsl:if> -->
<!--               </div> -->
<!--             </xed:bind> -->
<!--           </xed:bind> -->
<!--         </xsl:otherwise> -->
<!--       </xsl:choose> -->
<!--     </xsl:for-each> -->
<!--   </xsl:template> -->
  
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
		<div class="jp-personSelect-name">
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
			<button type="button" class="btn btn-default jp-personSelect-person" tabindex="1">
				<xed:output i18n="jp.editor.person.select" />
			</button>
			<button type="button" class="btn btn-default jp-personSelect-inst" tabindex="1">
				<xed:output i18n="jp.editor.inst.select" />
			</button>
		</div>
		
	</xsl:template>
	
	<xsl:template match="jp:template[@name='modal']">
		<div class="modal fade" id="personSelect-modal" tabindex="-1" role="dialog" aria-labelledby="personSelect-modal-title" aria-hidden="true">
		  <div class="modal-dialog">
		    <div class="modal-content">
		      <div class="modal-header">
		        <button type="button" class="close personSelect-cancel" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">x</span></button>
		        <h4 class="modal-title" id="personSelect-modal-title"></h4>
		      </div>
		      <div id="personSelect-modal-body" class="modal-body">
		      </div>
		      <div class="modal-footer">
		        <button id="personSelect-cancel-button" type="button" class="btn btn-danger personSelect-cancel" data-dismiss="modal"></button>
		        <button id="personSelect-send" type="button" class="btn btn-primary" disabled="disabled"></button>
		      </div>
		    </div>
		  </div>
		</div>
	</xsl:template>
	
	<xsl:template match="jp:template[@type='date']" mode="date_select" >
		<div>
			<xsl:attribute name="id">
				<xsl:value-of select="@id_container" />
			</xsl:attribute>
			<xsl:attribute name="class">
				<xsl:value-of select="@class" />
			</xsl:attribute>
			<xsl:if test="@span_date = 'before'">
				<span class="input-group-addon btn btn-default jp-layout-white">
		      <span class="glyphicon-calendar glyphicon"></span>
		    </span>
			</xsl:if>
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
	  	<xsl:if test="@span_date = 'after'">
		  	<span class="input-group-addon btn btn-default jp-layout-white">
		      <span class="glyphicon-calendar glyphicon"></span>
		    </span>
	    </xsl:if>
		</div>
	</xsl:template>
</xsl:stylesheet>