(function($) {
    $.widget('fsu.aclEditor', {
        options : {
            accessURL : '',
            rulesURL : '',
            httpGET : $.get,
            ajax : $.ajax
        },

        _create : function() {
            var accessURL = this.options.accessURL;
            var rulesURL = this.options.rulesURL;
            var httpGET = this.options.httpGET;
            var ajax = this.options.ajax;
            
            var selBox = $('<select id="selBox"/>').append('<option id="undefined">------</option>');
            httpGET(rulesURL, function(data){
                $.each(data, function(url, rule){
                    $('<option/>').attr('id', rule.rid).appendTo(selBox)
                    .html(rule.descr === undefined || rule.descr === '' ? rule.rid : rule.descr)
                    .data('url', url).data('rule', rule.rule);
                    console.log('rule url ' +url);
                })
            });
            
            var aclTable = $('<table/>').appendTo(this.element[0]);
            httpGET(accessURL, function(data){
                $.each(data,function(url,access){
                    var ruleTxt = $('#' + access.rid, selBox).html();
                    var ruleCell = $('<td/>').append('<span class="ruleCell">' + (ruleTxt !== null ? ruleTxt : '------') + '</span>')
                    ruleCell.data('access', access).data('url', url);
                    
                    var tr = $('<tr/>').appendTo(aclTable)
                    .append('<td>'+access.objid+'</td>')
                    .append('<td>'+access.acpool+'</td>')
                    .append(ruleCell);
                });
            });
            
            // hide select box, when clicking outside
            $('body').click(function(event){
                event.stopPropagation();
                if(!$(event.target).is('#selBox')){
                    selBox.unbind('change');
                    selBox.detach();
                    $('.ruleCell').trigger('redraw');
                }
            })
            
            selBox.bind('show', function(e, link){
                var ruleCell = $(link);
                var rid = ruleCell.parent().data('access')['rid'];
                ruleCell.hide();
                
                $('#'+ rid, selBox).prop('selected', true)
                selBox.hide();
                selBox.appendTo(ruleCell.parent());
                selBox.fadeIn();
                
                selBox.unbind('change');
                selBox.bind('change', function(){
                    var selected = $('option:selected')
                    var newrid = selected.attr('id');
                    var txt = selected.html();
                    
                    ruleCell.html(txt);
                    ruleCell.parent().data('access')['rid'] = newrid;
                    ajax({
                        type : 'PUT',
                        url : ruleCell.parent().data('url'),
                        data : newrid
                    })
                    selBox.unbind('change');
                })
            })
            
            $('.ruleCell').live('dblclick', function(){
                selBox.triggerHandler('show', this);
            }).live('mouseover', function(){
                $(this).css('cursor', 'pointer');
            }).live('mouseleave', function(){
                $(this).css('cursor', 'auto');
            }).live('redraw', function(){
                $(this).show();
            });
        }
    });
})(jQuery);