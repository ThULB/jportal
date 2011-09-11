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

    var ObjIdList = function(conf) {
        var that = Controllable();

        var ul = $('<ul/>').appendTo(conf.parentTag);
        ul.selectable({
            filter : 'li.selFilter'
        });

        function selectableOff() {
            ul.selectable('option', 'distance', 20);
        }
        ;

        function selectableOn() {
            ul.selectable('option', 'distance', 0);
        }
        ;

        that.add = function(data) {
            var li = $('<li/>').appendTo(ul);
            var h3 = $('<h3/>').append(data.id).appendTo(li);
            var accessList = null;
            function setAccessList(list) {
                accessList = list
            }
            ;

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

        return that;
    };

    var AccessList = function(conf) {
        var that = Controllable();

        var ul = $('<ul/>').appendTo(conf.parentTag);

        that.add = function(data) {
            var li = $('<li class="selFilter"/>').appendTo(ul);
            var table = $('<table/>').appendTo(li);
            var tr = $('<tr/>').appendTo(table);
            var accessId = $('<td/>').append(data.id).appendTo(tr);

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

    var ACLEditorController = function(model, mainView) {
        var ObjIdListener = function() {
            return {
                loadAccessList : function(data) {
                    var access = model.getDataFromUrl(data.url);
                    var accessList = AccessList({
                        parentTag : data.parentTag
                    });

                    for ( var i = 0; i < access.length; i++) {
                        accessList.add(access[i]);
                        //TODO: add rule --> rule listener
                    }

                    data.setAccessList(accessList);
                }
            };
        };

        var MainWindowListener = function() {
            return {
                loadObjId : function(data) {
                    var objIds = model.getDataFromUrl(data.url);
                    var objIdList = ObjIdList({
                        parentTag : data.parentTag
                    });
                    objIdList.addListeners(ObjIdListener());

                    for ( var i = 0; i < objIds.length; i++) {
                        objIdList.add(objIds[i]);
                    }
                }
            }
        };
        mainView.addListeners(MainWindowListener());
        mainView.init();
    };
    
    // ##############################################
    var ObjIdListView = function(conf) {
        var ul = $('<ul/>').appendTo(conf.parentTag);
        ul.selectable({
            filter : 'li.selFilter'
        });

        function selectableOff() {
            ul.selectable('option', 'distance', 20);
        }
        ;

        function selectableOn() {
            ul.selectable('option', 'distance', 0);
        }
        ;

        function accessListFrom(data) {
            var accessList = AccessListView({
                parentTag : data.parentTag,
                mouseover : selectableOff,
                mouseleave : selectableOn
            });

            conf.getData({
                url : data.url,
                callback : accessList.addAccess
            });

            accessList.loadRules({
                from : conf.getData
            });
            return accessList;
        }

        var ObjIdListEntry = function(data) {
            var li = $('<li/>').appendTo(ul);
            var h3 = $('<h3/>').append(data.id).appendTo(li);
            var accessList = null;

            h3.click(function() {
                if (accessList == null) {
                    accessList = accessListFrom({
                        url : data.link,
                        parentTag : li,
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

        return {
            addId : function(data) {
                ObjIdListEntry(data);
                return this;
            }
        }
    };

    var IconText = function(prop) {
        var td = $('<td/>');

        var icon = null;
        if (prop.icon !== undefined) {
            icon = $('<span/>').appendTo(td).addClass(prop.icon).css({
                float : prop.iconPos
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
            prop.eventHandler('mouseover');
        }).mouseleave(function() {
            prop.eventHandler('mouseleave');
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

    var Rule = function(prop) {
        var parentTag = prop.parentTag;
        var objsUnderControll = [];
        function eventHandler(event) {
            for ( var i = 0; i < objsUnderControll.length; i++) {
                objsUnderControll[i][event]();
                prop.handleEvent(event);
            }
        }

        var changeButton = IconText({
            icon : 'ui-icon ui-icon-triangle-1-s',
            iconPos : 'left',
            eventHandler : eventHandler
        }).appendTo(parentTag);
        var decr = IconText({
            eventHandler : eventHandler
        }).appendTo(parentTag);
        var editButton = IconText({
            icon : 'ui-icon ui-icon-gear',
            iconPos : 'right',
            eventHandler : eventHandler
        }).appendTo(parentTag);

        objsUnderControll = [ changeButton, decr, editButton ];

        return {
            init : function(data) {
                var rid = typeof data.rid == 'string' ? data.rid : '--/--';
                var descrTxt = typeof data.description == 'string' ? data.description : '--/--';
                var creator = typeof data.creator == 'string' ? data.creator : '--/--';

                changeButton.setText(rid);
                decr.setText(descrTxt);
                editButton.setText(creator);
            }
        }
    };

    var AccessListView = function(conf) {
        var ul = $('<ul/>').appendTo(conf.parentTag);
        var ruleContainers = [];

        function handleEvent(event) {
            conf[event]();
        }

        function addAccessListEntry(data) {
            var li = $('<li class="selFilter"/>').appendTo(ul);
            var table = $('<table/>').appendTo(li);
            var tr = $('<tr/>').appendTo(table);
            var accessId = $('<td/>').append(data.id).appendTo(tr);
            ruleContainers.push({
                parentTag : tr,
                url : data.link
            });
            table.css({
                borderCollapse : 'collapse'
            })
        }

        function loadRules(data) {
            for ( var i = 0; i < ruleContainers.length; i++) {
                var parentTag = ruleContainers[i].parentTag;
                var url = ruleContainers[i].url;
                var rule = Rule({
                    parentTag : parentTag,
                    handleEvent : handleEvent
                });

                data.from({
                    url : url,
                    callback : rule.init
                })
            }
        }

        return {
            addAccess : addAccessListEntry,
            loadRules : loadRules,
            toggleView : function() {
                if (ul.css('display') == 'none') {
                    ul.show();
                } else {
                    ul.hide();
                }
            }
        }
    };

    var ACLEditorWindow = function(conf) {
        function getData(spec) {
            var data = conf.model.getDataFromUrl(spec.url);

            for ( var i = 0; i < data.length; i++) {
                data[i].prev = spec.url;
                spec.callback(data[i]);
            }
        }

        var startUrl = conf.startUrl;
        var parentTag = conf.parentTag;
        var objIdList = ObjIdListView({
            parentTag : parentTag,
            getData : getData
        });

        // init ObjIdList
        getData({
            url : startUrl,
            callback : objIdList.addId
        })
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