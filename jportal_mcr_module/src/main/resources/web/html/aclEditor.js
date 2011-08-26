/**
 * 
 */

(function($) {
    $.widget("fsu.aclEditor", {
        options : {
            baseURL : null,
            getRscClasses : function(url, target, callBack){
                $.getJSON(url , function(data){
                    callBack(target, data);
                });
            },
            getRscMethods : function(url, target, callBack){
                $.getJSON(url, function(data){
                    callBack(target, data);
                });
            },
            getRscPerm : function(url, target, callBack){
                $.getJSON(url, function(data){
                    callBack(target, data);
                });
            }
        },

        _create : function() {
            var getClasses = this.options.getRscClasses;
            var getMethods = this.options.getRscMethods;
            var getPerms = this.options.getRscPerm;
            
            var editorElem = $(this.element[0]);
            var editorID = editorElem.attr('id');
            var classListID = editorID + 'ClassList';
            var methodListID = editorID + 'MethodList';
            var permColID = editorID + 'permCol';
            
            var classList = $('<div/>').attr({id: classListID}).append('<ul/>').appendTo(editorElem);
            var methodList = $('<div/>').attr({id: methodListID}).append('<ul/>').appendTo(editorElem);
            var permCol = $('<div/>').attr({id: permColID}).appendTo(editorElem);
            
            var classListUl = $('ul', classList);
            var methodListUl = $('ul', methodList);
            var fillList = this._fillList;
            
            classListUl.selectable({
                stop : function(){
                    methodListUl.empty();
                    $(".ui-selected", this).each(function() {
                        var li = $(this);
                        var next = li.data('next');

                        getMethods(next, methodListUl, fillList);
                    });
                }
            });
            getClasses(this.options.baseURL, classListUl, fillList);
            
            var fillPerm = this._fillPerm;
            methodListUl.selectable({
                stop : function(){
                    permCol.empty();
                    $(".ui-selected", this).each(function() {
                        var li = $(this);
                        var next = li.data('next');
                        
                        getPerms(next, permCol, fillPerm);
                    })
                }
            });
        },

        _fillList : function(ul, content) {
            $.each(content, function(key, value) {
                var li = $('<li/>').attr({'class':'ui-widget-content'}).html(key);
                li.data('next', value);
                ul.append(li);
            });
        },
        
        _fillPerm : function(div, content){
            if($.isEmptyObject(content)){
                $('<p/>').html('No rule assigned').appendTo(div); 
            } else{
                $.each(content, function(key, value){
                    if(key == 'rid'){
                        $('<button/>').html(value).button({
                            icons: {secondary: "ui-icon-triangle-1-s"}
                        }).appendTo(div); 
                    } else {
                        $('<p/>').html(key + ': ' + value).appendTo(div); 
                    }
                });
            }
        },

        destroy : function() {
            $.Widget.prototype.destroy.apply(this, arguments);
        }
    });
})(jQuery);