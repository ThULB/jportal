(function($) {
    var Editor = function(conf) {
        var model = conf.model;

        var mainList = $('<ul/>').appendTo(conf.parentTag).selectable({
            filter : 'li.ui-selectee'
        }).bind('selOff', function(e) {
            $(this).selectable({
                distance : '20'
            });
        }).bind('selOn', function(e) {
            $(this).selectable({
                distance : '0'
            });
        });

        var ruleSelBox = function() {
            var selBoxFrame = $('<div/>').appendTo(conf.parentTag).hide();
            var selBox = $('<ul/>').appendTo(selBoxFrame);
            var connectedObjs = $({});
            var connectedFn = function(){};
            var connectedSelected = function(){};
            
            $.each(model.getDataFromUrl(conf.rulesUrl), function(i, data) {
                var li = $('<li/>').append(data.id).appendTo(selBox);
                var oldColor = '';
                li.hover(function(){
                    oldColor = li.css('backgroundColor');
                    li.css({
                        backgroundColor : 'yellow',
                        cursor: 'pointer'
                    });
                }, function(){
                    li.css({
                        backgroundColor : oldColor,
                        cursor: 'auto'
                    });
                }).click(function(){
                    var ruleData = model.getDataFromUrl(data.link)[0];
                    connectedSelected(ruleData);
                });
            });
            
            selBoxFrame.css({
                backgroundColor : '#cccccc',
                width : '200px',
                maxHeight : '322px',
                overflow : 'auto'
            })
            
            
            selBoxFrame.mouseleave(function() {
                connectedObjs.trigger('mouseleave');
                selBoxFrame.hide();
                connectedObjs = $({});
                connectedFn = function(){};
            }).mouseover(function(){
                connectedFn();
            });
            
            return {
                show : function(css){
                    selBoxFrame.trigger('mouseover');
                    selBoxFrame.trigger('mouseleave');
                    selBoxFrame.show();
                    selBoxFrame.css(css);
                    return this;
                },
                hide : function(){
                    selBoxFrame.trigger('mouseover');
                    selBoxFrame.trigger('mouseleave');
                },
                connect : function(to){
                    connectedObjs = to.objs;
                    connectedFn = to.fn;
                    connectedSelected = to.selected;
                    return this;
                }
            };
        }();

        function loadAccess(url) {
            var ul = $('<ul/>');
            $.each(model.getDataFromUrl(url), function(i, data) {
                var li = $('<li class="ui-acleditor-access ui-selectee"/>').appendTo(ul);
                var tr = $('<tr/>')
                var ruleData = model.getDataFromUrl(data.link)[0];
                var ridTxt = typeof ruleData.rid == 'string' ? ruleData.rid : '--/--';
                var descrTxt = typeof ruleData.description == 'string' ? ruleData.description : '--/--';
                var creatorTxt = typeof ruleData.creator == 'string' ? ruleData.creator : '--/--';

                tr.append('<td class="acId">' + data.id + '</td>');
                tr.append('<td class="rule button change"><span class="ui-icon ui-icon-triangle-1-s" style="visibility:hidden"/></td>');
                tr.append('<td class="rule id">' + ridTxt + '</td>');
                tr.append('<td class="rule descr">' + descrTxt + '</td>');
                tr.append('<td class="rule creator">' + creatorTxt + '</td>');
                tr.append('<td class="rule button edit"><span class="ui-icon ui-icon-gear ui-rule-button"/></td>');
                tr.appendTo($('<table/>').css({
                    borderCollapse : 'collapse'
                }).appendTo(li));

                tr.delegate('.rule', 'mouseover', function() {
                    $('.rule', tr).addClass('ui-rule-selected');
                    $('.rule.button>span', tr).css({
                        visibility : 'visible'
                    });
                });

                var ruleMarkingOnMouseLeave = function() {
                    $('.rule', tr).removeClass('ui-rule-selected');
                    $('.rule.button>span', tr).css({
                        visibility : 'hidden'
                    });
                }
                tr.delegate('.rule', 'mouseleave', function() {
                    ruleMarkingOnMouseLeave();
                });

                tr.delegate('.rule.button', 'mouseover', function() {
                    mainList.trigger('selOff');
                });

                tr.delegate('.rule.button', 'mouseleave', function() {
                    mainList.trigger('selOn');
                });

                var hidden = true;
                var showSelBox = function(position){
                    hidden = false;
                    var safeRuleMarkingOnMouseLeave = ruleMarkingOnMouseLeave;
                    ruleMarkingOnMouseLeave = function() {
                    };
                    
                    ruleSelBox.show({
                        position : 'absolute',
                        top : position.top + $('table', li).outerHeight(),
                        left : position.left,
                    }).connect({
                        objs : $('.rule', tr),
                        fn : function(){
                            ruleMarkingOnMouseLeave = safeRuleMarkingOnMouseLeave
                            hidden = true;
                        },
                        selected: function(ruleData){
                            var ridTxt = typeof ruleData.rid == 'string' ? ruleData.rid : '--/--';
                            var descrTxt = typeof ruleData.description == 'string' ? ruleData.description : '--/--';
                            var creatorTxt = typeof ruleData.creator == 'string' ? ruleData.creator : '--/--';
                            $('.rule.id', tr).html(ridTxt);
                            $('.rule.descr', tr).html(descrTxt);
                            $('.rule.creator', tr).html(creatorTxt);
                        } 
                    });
                };
                
                var hideSelBox = function(){
                    hidden = true;
                    ruleSelBox.hide();
                }
                $('.rule.change.button', tr).click(function() {
                    var position = $(this).position();
                    
                    if(hidden){
                        showSelBox(position)
                    }else{
                        hideSelBox();
                    }
                });

                $('.rule.edit.button', tr).click(function() {
                    console.log('edit rule');
                })
            });

            return ul;
        }

        $.each(model.getDataFromUrl(conf.startUrl), function(i, data) {
            var li = $('<li class="ui-acleditor-objId"/>').appendTo(mainList);

            $('<h3 class="ui-acleditor-objId-txt"/>').append(data.id).appendTo(li).click(function(event) {
                var access = $(this).data('access')
                if (access === undefined) {
                    $(this).data('access', loadAccess(data.link).appendTo(li));
                } else {
                    access.fadeToggle('fast');
                }
            }).mouseover(function() {
                mainList.trigger('selOff');
            }).mouseleave(function() {
                mainList.trigger('selOn');
            });
        });
    }

    var Model = function(httpGET) {
        var dataPool = {};

        return {
            getDataFromUrl : function(url) {
                var dataObj = dataPool[url];

                if (dataObj === undefined) {
                    httpGET(url, {}, function(target, data) {
                        dataObj = data[url];
                        dataPool[url] = dataObj;
                    });
                }

                return dataObj;
            }
        }
    }

    $.widget('fsu.aclEditor', {
        options : {
            baseURL : '',
            httpGET : function(url, target, callBack) {
            }
        },

        _create : function() {
            var model = Model(this.options.httpGET);
            /*
             * console.log('rules url create ' + this.options.rulesURL) var
             * mainWindow = ACLEditorMainWindow({ startUrl :
             * this.options.baseURL, rulesUrl : this.options.rulesURL, parentTag :
             * this.element[0] }); var mainWindowController =
             * ACLEditorController(model, mainWindow);
             */
            Editor({
                model : model,
                startUrl : this.options.baseURL,
                rulesUrl : this.options.rulesURL,
                parentTag : this.element[0]
            })
        }
    });
})(jQuery);