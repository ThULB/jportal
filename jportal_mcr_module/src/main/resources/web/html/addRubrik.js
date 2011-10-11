/**
 * 
 */
var rules = [ 
   {
    rootid : 'jportal_jpjournal_00000016',
    text : 'Rubrik für blah',
    descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000020',
       text : 'JALZ',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }, 
   {
       rootid : 'jportal_jpjournal_00000016',
       text : 'Rubrik für blah',
       descr : 'fakerule'
   }
   
 ];

(function($) {
    var SelectDialog = function(conf){
        var selectDiag = $('<div id="select-diag"/>').appendTo(conf.parent);
        var listPane = $('<ul/>').appendTo(selectDiag);
        var newRuleButton = $('<div>Neu</div>').button();
        var selectRuleButton = $('<div>Auswählen</div>').button();
        var buttonPane = $('<div id="select-diag-buttonPane"/>').append(newRuleButton).append(selectRuleButton).appendTo(selectDiag);
        
        if(conf.hide === true){
            borderPane.hide();
        }
        
        function setInteractions(){
            listPane.delegate('li', 'hover', function(){
                $(this).toggleClass('ui-state-hover')
            }).delegate('li', 'click', function(){
                listPane.find('li.ui-state-active.selected:not(.ui-state-hover)').removeClass('ui-state-active selected');
                $(this).addClass('ui-state-active selected');
            });
            
            newRuleButton.click(function(){
                conf.parent.trigger('newRule');
            });
            
            selectRuleButton.click(function(){
                var categ = listPane.find('li.selected').data('categ');
                conf.parent.trigger('selectCateg', categ);
            })
        }
        
        function setCSS(){
            listPane.css({
                listStyle : 'none',
                height : '200px',
                width : '300px',
                border : 'solid 1px black',
                overflow : 'auto',
                padding : '3px',
                marginLeft: 'auto',
                marginRight: 'auto'
            }).addClass('ui-widget-content');
            
            buttonPane.css({
                width : '300px',
                marginLeft: 'auto',
                marginRight: 'auto'
            });
            
            selectRuleButton.css({
                float : 'right'
            })
        }
        
        setInteractions();
        setCSS();
        
        selectDiag.refresh = function(/* array */ data){
            listPane.empty();
            $.each(data, function(i, item) {
                $('<li class="ui-state-default">' + item.text + '</li>').appendTo(listPane).data('categ', item);
            });
        }
        
        return selectDiag;
    };
    
    var NewRuleDialog = function(conf){
        var newRuleDiag = $('<div id="new-rule-diag"/>').appendTo(conf.parent);
        var inputPane = $('<table class="ui-state-default"/>').appendTo(newRuleDiag);
        var inputLabels = {
                text : 'Text',
                descr : 'Bechreibung',
        };
        var cancelButton = $('<div>Abbrechen</div>').button();
        var okButton = $('<div>OK</div>').button();
        var buttonPane = $('<div id="new-rule-diag-buttonPane"/>').append(cancelButton).append(okButton).appendTo(newRuleDiag);
        
        if(conf.hide === true){
            borderPane.hide();
        }
        
        $.each(inputLabels, function(id, label){
            console.log('labels: ' +  id + ' # ' + label);
            inputPane.append('<tr><th>' + label + '</th></tr>')
            inputPane.append('<tr><td><textarea id="' + id + '"/></td></tr>')
        });
        
        function setInteractions(){
            cancelButton.click(function(){
                conf.parent.trigger('cancelNewRule');
            });
            
            okButton.click(function(){
                var text = inputPane.find('textarea#text').val();
                var descr = inputPane.find('textarea#descr').val();
                conf.parent.trigger('createNewRule', {text : text, descr : descr});
            });
        }
        
        function setCSS(){
            inputPane.css({
                height : '200px',
                width : '300px',
                border : 'solid 1px black',
                padding : '3px',
                marginLeft: 'auto',
                marginRight: 'auto',
                marginTop: '18px',
                marginBottom: '18px'
            });
            
            inputPane.find('th').css({
                fontSize : '10pt',
                textAlign: 'left'
            });
            
            inputPane.find('textarea').css({
                resize: 'none',
                width : '98%'
            });
            
            buttonPane.css({
                width : '300px',
                marginLeft: 'auto',
                marginRight: 'auto'
            });
            
            okButton.css({
                float : 'right'
            })
        }
        
        setInteractions();
        setCSS();
        
        newRuleDiag.refresh = function(){
            inputPane.find('textarea').val('');
        }
        
        return newRuleDiag;
    };
    
    $.widget("fsu.selectDiag", {
        options : {
            selectItems : null,
            clear : null
        },
        _create : function() {
            var _widget = this.element;
            var _options = this.options
            
            _widget.css({
                height : '300px',
                width : '348px',
                border : 'solid 1px black'
            }).addClass('ui-widget ui-corner-all');
            
            var selectDialog = new SelectDialog({parent : this.element});
            selectDialog.refresh(_options.selectItems);
            
            var newRuleDialog = new NewRuleDialog({parent : this.element});
            newRuleDialog.hide();
            
            _widget.bind('newRule', function(){
                selectDialog.hide();
                newRuleDialog.refresh();
                newRuleDialog.fadeIn();
            }).bind('cancelNewRule', function(){
                newRuleDialog.hide();
                selectDialog.fadeIn();
            }).bind('createNewRule', function(e, newRule){
                newRule.rootid = 'new';
                // TODO: save in DB
                _options.selectItems.push(newRule);
                selectDialog.refresh(_options.selectItems);
                newRuleDialog.hide();
                newRuleDialog.refresh();
                selectDialog.fadeIn();
            }).bind('selectCateg', function(e, selectedCateg){
                console.log("selected Categ " + selectedCateg.rootid + ' # ' + selectedCateg.text);
            });
        },
        
        destroy : function() {
            $.Widget.prototype.destroy.call(this);
        }
    })
}(jQuery));

$(document).ready(function() {
    console.log("hello world!");
    $('#selDiag').selectDiag({selectItems : rules});
})