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

    var IconTextView = function(prop) {
        var td = $('<td/>');

        var icon = null;
        if (prop.icon !== undefined) {
            icon = $('<span/>').appendTo(td).addClass(prop.icon).css({
                float : prop.iconPos
            }).click(function() {
                prop.click();
            });
        }
        var text = $('<span/>').appendTo(td);

        function toggleIcon(visibility) {
            if (icon != null) {
                icon.css({
                    visibility : visibility
                });
            }
            return this;
        }

        toggleIcon('hidden');
        td.mouseover(function() {
            prop.mouseEvents['mouseover']();
        }).mouseleave(function() {
            prop.mouseEvents['mouseleave']();
        });

        return {
            setText : function(val) {
                text.html(val);
                return this;
            },

            toggleIcon : toggleIcon,

            mouseover : function() {
                toggleIcon('visible');
                td.css({
                    backgroundColor : '#cccccc'
                });
            },

            mouseleave : function() {
                toggleIcon('hidden');
                td.css({
                    backgroundColor : ''
                });
            },

            appendTo : function(tag) {
                td.appendTo(tag);
                return this;
            }
        }
    };

    var Rule = function(conf){
        var that = Controllable();
        var data = conf.data;
        var ridTxt = typeof data.rid == 'string' ? data.rid : '--/--';
        var descrTxt = typeof data.description == 'string' ? data.description : '--/--';
        var creatorTxt = typeof data.creator == 'string' ? data.creator : '--/--';
        
        var tdFact = function(){
            var tds = [];
            function greyBackground(){
                for ( var i = 0; i < tds.length; i++) {
                    tds[i].css({
                        backgroundColor : '#cccccc'
                    });
                }
            }
            
            function noBackground(){
                for ( var i = 0; i < tds.length; i++) {
                    tds[i].css({
                        backgroundColor : ''
                    });
                }
            }
            
            return {
                create : function(){
                    var td = $('<td/>').mouseover(function() {
                        greyBackground();
                    }).mouseleave(function() {
                        noBackground();
                    });
                    tds.push(td);
                    return td;
                }
            };
        }();
        
        var rule = tdFact.create().append(ridTxt);
        var descr = tdFact.create().append(descrTxt);
        var creator = tdFact.create().append(creatorTxt);
        
        that.appendTo = function(tag){
            rule.appendTo(tag);
            descr.appendTo(tag);
            creator.appendTo(tag);
            return that;
        }
        
        return that;
    };
    
    var RuleView = function(prop) {
        var that = Controllable();
        var data = prop.data;
        var rid = typeof data.rid == 'string' ? data.rid : '--/--';
        var descrTxt = typeof data.description == 'string' ? data.description : '--/--';
        var creator = typeof data.creator == 'string' ? data.creator : '--/--';

        var mouseEvents = {
            mouseover : function() {
                changeButton.mouseover();
                decr.mouseover();
                editButton.mouseover();
                that.notify({event : 'selectableOff'});
            },
            
            mouseleave : function() {
                changeButton.mouseleave();
                decr.mouseleave();
                editButton.mouseleave();
                that.notify({event : 'selectableOn'});
            }
        }

        var changeButton = IconTextView({
            icon : 'ui-icon ui-icon-triangle-1-s',
            iconPos : 'left',
            mouseEvents : mouseEvents,
            click : function(){
                that.notify({event : 'changeRule'});
            }
        }).setText(rid);
        var decr = IconTextView({
            mouseEvents : mouseEvents
        }).setText(descrTxt);
        var editButton = IconTextView({
            icon : 'ui-icon ui-icon-gear',
            iconPos : 'right',
            mouseEvents : mouseEvents,
            click : function(){
                that.notify({event : 'editRule'});
            }
        }).setText(creator);

        function notify(msg) {
            var buttonController = {

            }
            that.notify({
                event : event,
                data : [ changeButton, decr, editButton ]
            });
        }

        that.appendTo = function(tag) {
            changeButton.appendTo(tag);
            decr.appendTo(tag);
            editButton.appendTo(tag);
            return that;
        };

        return that;
    };

    var ACLEditorController = function(model, mainView) {
        var objIdList = ObjIdList();

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
                        // TODO: add rule --> rule listener
                        var ruleData = model.getDataFromUrl(access[i].link);
                        var rule = Rule({
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
            /*
             * var model = Model(this.options.httpGET); var aclEditor =
             * ACLEditorWindow({ startUrl : this.options.baseURL, model : model,
             * parentTag : this.element[0] });
             */

            var model = Model(this.options.httpGET);
            var mainWindow = ACLEditorMainWindow({
                startUrl : this.options.baseURL,
                parentTag : this.element[0]
            });
            var mainWindowController = ACLEditorController(model, mainWindow);
        }
    });
})(jQuery);