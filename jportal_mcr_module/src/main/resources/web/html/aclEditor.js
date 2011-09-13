(function($) {
    var Controllable = function() {
        var listeners = [];

        return {
            addListeners : function(list) {
                listeners.push(list);
            },

            notify : function(msg) {
                for ( var i = 0; i < listeners.length; i++) {
                    if (typeof listeners[i][msg.event] == 'function') {
                        listeners[i][msg.event](msg.data);
                    }
                }
            }
        }
    }

    var ACLEditorMainWindow = function(conf) {
        var that = Controllable();

        that.init = function() {
            that.notify({
                event : 'loadObjId',
                data : {
                    url : conf.startUrl,
                    parentTag : conf.parentTag
                }
            });
        }

        return that;
    };

    var ObjIdList = function() {
        var that = Controllable();

        var ul = $('<ul/>');
        ul.selectable({
            filter : 'li.selFilter'
        });

        function selectableOff() {
            ul.selectable('option', 'distance', 20);
        }

        function selectableOn() {
            ul.selectable('option', 'distance', 0);
        }

        that.add = function(data) {
            var li = $('<li/>').appendTo(ul);
            var h3 = $('<h3/>').append(data.id).appendTo(li);
            var accessList = null;
            function setAccessList(list) {
                accessList = list
            }

            h3.click(function() {
                if (accessList == null) {
                    that.notify({
                        event : 'loadAccessList',
                        data : {
                            url : data.link,
                            parentTag : li,
                            setAccessList : setAccessList
                        }
                    });
                } else {
                    accessList.toggleView();
                }
            }).mouseover(function() {
                selectableOff();
            }).mouseleave(function() {
                selectableOn();
            });
        };

        that.selectableOff = selectableOff;
        that.selectableOn = selectableOn;
        that.appendTo = function(tag) {
            ul.appendTo(tag);
            return that;
        }
        return that;
    };

    var AccessList = function(conf) {
        var that = Controllable();

        var ul = $('<ul/>').appendTo(conf.parentTag);

        that.add = function(data) {
            var access = data.access;
            var rule = data.rule;
            var li = $('<li class="selFilter"/>').appendTo(ul);
            var table = $('<table/>').appendTo(li);
            var tr = $('<tr/>').appendTo(table);
            var accessId = $('<td/>').append(access.id).appendTo(tr);
            rule.appendTo(tr);

            table.css({
                borderCollapse : 'collapse'
            })
        }

        that.toggleView = function() {
            if (ul.css('display') == 'none') {
                ul.show();
            } else {
                ul.hide();
            }
        }

        return that;
    };

    var Rule = function(conf) {
        var that = Controllable();
        var data = conf.data;
        var ridTxt = typeof data.rid == 'string' ? data.rid : '--/--';
        var descrTxt = typeof data.description == 'string' ? data.description : '--/--';
        var creatorTxt = typeof data.creator == 'string' ? data.creator : '--/--';

        var tds = [];

        function setcss(event) {
            for ( var i = 0; i < tds.length; i++) {
                tds[i].css(tds[i]._css[event]);
            }
        }

        function create(conf) {
            var fn = (conf && conf.fn !== undefined) ? conf.fn : {mouseover : function(){}, mouseleave : function(){}}
            var td = $('<td/>').mouseover(function() {
                setcss('mouseover');
                fn.mouseover();
            }).mouseleave(function() {
                setcss('mouseleave');
                fn.mouseleave();
            });

            td._css = (conf && conf.css !== undefined) ? conf.css : {
                mouseover : {},
                mouseleave : {}
            };
            td._css.mouseover.backgroundColor = '#cccccc';
            td._css.mouseleave.backgroundColor = ''

            if (td._css._default !== undefined) {
                td.css(td._css._default);
            }

            tds.push(td);
            return td;
        }

        var rule = create().append(ridTxt);
        var descr = create().append(descrTxt);
        var creator = create().append(creatorTxt);

        that.appendTo = function(tag) {
            rule.appendTo(tag);
            descr.appendTo(tag);
            creator.appendTo(tag);
            return that;
        }
        that.createTd = create;
        return that;
    };

    var RuleWithButtons = function(conf) {
        var rule = Rule(conf);
        var setSelectable = {
                mouseover : function(){
                    rule.notify({event : 'selectableOff'});
                },
                
                mouseleave : function(){
                    rule.notify({event : 'selectableOn'});
                },
        };
        
        var changeButton = rule.createTd({
            css : {
                mouseover : {
                    visibility : 'visible'
                },
                mouseleave : {
                    visibility : 'hidden'
                },
                _default : {
                    visibility : 'hidden'
                }
            },
            fn : setSelectable
        }).append($('<span/>').addClass('ui-icon ui-icon-triangle-1-s'));
        var editButton = rule.createTd({
            css : {
                mouseover : {
                    visibility : 'visible'
                },
                mouseleave : {
                    visibility : 'hidden'
                },
                _default : {
                    visibility : 'hidden'
                }
            },
            fn : setSelectable
        }).append($('<span/>').addClass('ui-icon ui-icon-gear'));
        var ruleAppendTo = rule.appendTo;

        changeButton.click(function(){
            rule.notify({event : 'changeRule'});
        });
        
        editButton.click(function(){
            rule.notify({event : 'editRule'});
        });
        
        
        function appendTo(tag) {
            changeButton.appendTo(tag);
            ruleAppendTo(tag);
            editButton.appendTo(tag);
        }
        rule.appendTo = appendTo;
        return rule;
    };
    
    var RuleSelectBox = function(conf){
        var selectbox = Controllable();
        
        return selectbox;
    }

    var ACLEditorController = function(model, mainView) {
        var objIdList = ObjIdList();
        var ruleSelBox = RuleSelectBox();
        
        var RuleListener = function() {
            return {
                selectableOff : function() {
                    objIdList.selectableOff();
                },

                selectableOn : function() {
                    objIdList.selectableOn();
                },

                changeRule : function(ruleElement) {
                    console.log('change rule');
                },
                
                editRule : function(ruleElement) {
                    console.log('edit rule');
                }
            }
        };

        var ObjIdListener = function() {
            return {
                loadAccessList : function(data) {
                    var access = model.getDataFromUrl(data.url);
                    var accessList = AccessList({
                        parentTag : data.parentTag
                    });

                    for ( var i = 0; i < access.length; i++) {
                        var ruleData = model.getDataFromUrl(access[i].link);
                        var rule = RuleWithButtons({
                            data : ruleData[0]
                        });
                        rule.addListeners(RuleListener());
                        accessList.add({
                            access : access[i],
                            rule : rule
                        });
                    }

                    data.setAccessList(accessList);
                }
            };
        };

        var MainWindowListener = function() {
            return {
                loadObjId : function(data) {
                    var objIds = model.getDataFromUrl(data.url);
                    objIdList.appendTo(data.parentTag).addListeners(ObjIdListener());

                    for ( var i = 0; i < objIds.length; i++) {
                        objIdList.add(objIds[i]);
                    }
                },
                
                loadSelBox : function(data) {
                    
                }
            }
        };
        mainView.addListeners(MainWindowListener());
        mainView.init();
    };

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
            var mainWindow = ACLEditorMainWindow({
                startUrl : this.options.baseURL,
                parentTag : this.element[0]
            });
            var mainWindowController = ACLEditorController(model, mainWindow);
        }
    });
})(jQuery);