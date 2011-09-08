(function($) {
    function AbstractView(/* {listeners : array} */spec) {
        var listeners = (spec && spec.listeners) || [];
        var controllReg = (spec && spec.controllReg) || [];

        return {
            addListener : function(list) {
                listeners.push(list);
            },

            addRegister : function(list) {
                controllReg.push(list);
            },

            getListeners : function() {
                return listeners;
            },

            notify : function(url) {
                for ( var i = 0; i < listeners.length; i++) {
                    listeners[i].loadData(url);
                }
            },

            register : function(view) {
                for ( var i = 0; i < controllReg.length; i++) {
                    controllReg[i].register(view);
                }
            }
        }
    }
    
    function MainGUI(spec){
        var that = AbstractView();
        var objIDs = ObjIDList({parentTag : spec.parentTag});
        
        that.init = function(){
            that.register(objIDs);
            objIDs.notify(spec.link);
        };
        
        return that;
    }
    
    function ObjIDList(spec){
        var that = UlListGUI({parentTag : spec.parentTag});
        
        that.addItem = function(item){
            var h3 = $('<h3/>').appendTo(item.parentTag);
            var accessIDs = null;
            
            h3.append(item.data.id);
            h3.click(function(){
                if(accessIDs == null){
                    accessIDs = AccessIDList({parentTag : item.parentTag});
                    that.register(accessIDs);
                    accessIDs.notify(item.data.link);
                }else{
                    accessIDs.toggleView();
                }
            })
        }
        
        return that;
    }
    
    function AccessIDList(spec){
        var that = UlListGUI({parentTag : spec.parentTag});
        
        function Button(spec){
            var button = $('<span/>').addClass(spec.icon).click(function() {
            });
            var textTag = $('<span/>');
            button.css({float : spec.float});
            var td = $('<td/>').append(button).append(textTag);
            
            button.css({visibility : 'hidden'});
            
            td.getTextTag = function(){
                return textTag;
            }
            
            td.toggleIcon = function(val){
                button.css({visibility : val});
            }
            
            return td;
        }
        
        function Rule(spec){
            var that = AbstractView();
            
            that.display = function(data){
                var rid = data.rid === undefined ? '--/--' : data.rid;
                var descr = data.description === undefined ? '--/--' : data.description;
                var creator = data.creator === undefined ? '--/--' : data.creator;
                
                spec.rid.append(rid);
                spec.descr.append(descr);
                spec.creator.append(creator);
            }
            return that;
        }
        
        function mouseHover(objs){
            function colorAndToggle(spec){
                for ( var i = 0; i < objs.length; i++) {
                    objs[i].css({
                        backgroundColor : spec.color 
                    });
                    
                    if(typeof objs[i].toggleIcon == 'function'){
                        objs[i].toggleIcon(spec.visibility);
                    }
                }
            }
            
            for ( var i = 0; i < objs.length; i++) {
                var currentObj = objs[i];
                currentObj.mouseover(function(){
                    colorAndToggle({color : '#cccccc', visibility: 'visible'})
                }).mouseleave(function(){
                    colorAndToggle({color : '', visibility: 'hidden'})
                })
            }
        }
        
        that.addItem = function(item){
            var entry = $('<table/>').appendTo(item.parentTag);
            var tr = $('<tr/>').appendTo(entry);
            var acId = $('<td/>').append(item.data.id);
            
            
            var ruleID = Button({icon : 'ui-icon ui-icon-triangle-1-s', float:'left'});
            var ruleDescrText = $('<span/>');
            var ruleDescr = $('<td/>').append(ruleDescrText);
            var ruleCreator = Button({icon : 'ui-icon ui-icon-gear', float: 'right'});
            
            var rule = Rule({
                rid : ruleID.getTextTag(),
                descr : ruleDescr,
                creator : ruleCreator.getTextTag()
            });
            
            entry.css({borderCollapse : 'collapse'});
            tr.append(acId).append(ruleID).append(ruleDescr).append(ruleCreator);
            mouseHover([ruleID, ruleDescr, ruleCreator]);
            that.register(rule);
            rule.notify(item.data.link);
        };
        
        return that;
    }
    
    function UlListGUI(spec){
        var that = AbstractView();
        var ul = $('<ul/>').appendTo(spec.parentTag);
        
        that.display = function(data){
            var li = $('<li/>').appendTo(ul);
            if(typeof that.addItem == 'function'){
                that.addItem({
                    parentTag : li,
                    data : data
                });
            }else {
                li.append(data.id);
            }
        };
        
        that.toggleView = function(){
            if(ul.css('display') == 'none'){
                ul.show();
            }else{
                ul.hide();
            }
        }
        
        return that;
    }

    function ViewController(model) {
        return {
            register : function(view) {
                var viewListener = {
                    loadData : function(url) {
                        var httpGET = model.getDataFromUrl(url);
                        var data = httpGET[url];

                        for ( var i = 0; i < data.length; i++) {
                            view.display(data[i]);
                        }
                    }
                };

                view.addListener(viewListener);
                view.addRegister(this);

                if (typeof view.init == 'function') {
                    view.init();
                }
            }
        }
    }

    function Model(httpGET) {
        var dataPool = {};

        return {
            getDataFromUrl : function(url) {
                var data = {};
                var dataObj = $(dataPool).prop(url);

                if (dataObj === undefined) {
                    httpGET(url, {}, function(target, _data) {
                        data = _data;
                        $.extend(dataPool, data);
                    });
                } else {
                    $(data).prop(url, dataObj);
                }

                return data;
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
            var guiController = ViewController(model);
            var mainGUI = MainGUI({
                parentTag : $(this.element[0]),
                link : this.options.baseURL
            });

            guiController.register(mainGUI);

            /*
             * this.view = new this._view($(this.element[0])); this.model = new
             * this._model(this.options.httpGET);
             * 
             * this.view.addListener(this);
             * this.view.create(this.options.baseURL);
             */
        },

        _view : function(mainTag) {
            var editorID = mainTag.attr('id');
            var classListID = editorID + 'ClassList';
            var permColID = editorID + 'permCol';
            var dropDownID = editorID + 'ruleListDropdown';

            var classList = $('<div/>').attr({
                id : classListID
            }).css({
                float : 'left'
            }).appendTo(mainTag);
            classList.selectable({
                filter : 'li',
                stop : function() {
                    permCol.empty();
                    var selectedObj = [];
                    $(".ui-selected", this).each(function() {
                        var objID = $(this).parentsUntil('div > h3').children('h3').html();
                        var accessID = $('.id', this).html();

                        selectedObj.push({
                            id : objID,
                            ac : accessID
                        });
                    });
                    // notifyLoadPerms(selectedObj);
                }
            });

            var permCol = $('<div/>').attr({
                id : permColID
            }).css({
                float : 'left'
            }).appendTo(mainTag);

            var ruleList = $('<div/>').attr({
                id : dropDownID
            }).css({
                float : 'left',
                display : 'none'
            }).appendTo(mainTag);

            var listeners = [];
            var mainTable = $('<table/>').appendTo(mainTag);

            function notifyLoadData(url, target, callback) {
                $.each(listeners, function(i, listener) {
                    listener.loadData(url, target, callback);
                })
            }

            function notifyLoadAllObjID() {
                $.each(listeners, function(i, listener) {
                    listener.loadAllObjID();
                })
            }

            function notifyLoadAllAccess(id, target) {
                $.each(listeners, function(i, listener) {
                    listener.loadAllAccess(id, target);
                })
            }

            function notifyLoadPerms(selectedObj) {
                $.each(listeners, function(i, listener) {
                    listener.loadPerms(selectedObj);
                })
            }

            function notifyLoadPerm(id, target) {
                $.each(listeners, function(i, listener) {
                    listener.loadPerm(id, target);
                })
            }

            function notifyClickChangeRule(target) {
                $.each(listeners, function(i, listener) {
                    listener.clickChangeRule(target);
                })
            }

            function turnOffSelectable(/* List */objects, options) {
                var _options = {
                    mouseover : function() {
                    },
                    mouseleave : function() {
                    }
                };

                if (options !== undefined) {
                    _options = options;
                }

                $.each(objects, function(i, obj) {
                    obj.mouseover(function() {
                        classList.selectable('option', 'distance', 20);
                        if (typeof _options.mouseover == 'function') {
                            _options.mouseover();
                        }
                    }).mouseleave(function() {
                        classList.selectable('option', 'distance', 0);
                        if (typeof _options.mouseleave == 'function') {
                            _options.mouseleave();
                        }
                    });
                })
            }

            function multiCSS(objects, css) {
                $.each(objects, function(i, obj) {
                    obj.css(css)
                })
            }

            this.create = function() {
                notifyLoadAllObjID();
            };

            this.addListener = function(list) {
                listeners.push(list);
            };

            this.displayObjId = function(obj, url) {
                var id = obj.id;
                var link = obj.link;
                var classListDiv = $('<div/>').appendTo(classList);
                var ul = $('<ul/>');
                var h3 = $('<h3/>').html(id).appendTo(classListDiv).after(ul)

                h3.click(function() {
                    if (ul.children().size() < 1) {
                        ul.show();
                        notifyLoadAllAccess(link, ul);
                    } else {
                        if (ul.css('display') == 'none') {
                            ul.show();
                        } else {
                            ul.hide();
                        }
                    }
                });
                turnOffSelectable([ h3 ]);
            };

            this.displayAccess = function(obj, target) {
                var objId = obj.id;
                var acId = obj.id;
                var link = obj.link;

                var table = $('<table/>').css({
                    border : 'solid black 1px'
                });

                var id = $('<td class="id"/>').html(acId);

                var permEdit = $('<span/>').addClass("ui-icon ui-icon-gear").click(function() {
                    console.log('Click Edit');
                });
                var permChange = $('<span/>').addClass("ui-icon ui-icon-triangle-1-s").click(function() {
                    console.log('Click Change');
                    notifyChangePerm(this);
                });

                var permText = $('<span />');
                var permDescrText = $('<span />');
                var permCreatorText = $('<span />');

                multiCSS([ permText, permDescrText, permCreatorText, permEdit, permChange ], {
                    float : 'left'
                });
                multiCSS([ permEdit, permChange ], {
                    visibility : 'hidden'
                });

                var perm = $('<td/>').append(permChange).append(permText);
                var descr = $('<td/>').append(permDescrText);
                var creator = $('<td/>').append(permCreatorText).append(permEdit);

                turnOffSelectable([ perm, descr, creator ], {
                    mouseover : function() {
                        multiCSS([ permEdit, permChange ], {
                            visibility : 'visible'
                        });
                        multiCSS([ perm, descr, creator ], {
                            backgroundColor : '#cccccc'
                        });
                    },
                    mouseleave : function() {
                        multiCSS([ permEdit, permChange ], {
                            visibility : 'hidden'
                        });
                        multiCSS([ perm, descr, creator ], {
                            backgroundColor : ''
                        });
                    }
                });

                $('<tr/>').append(id).append(perm).append(descr).append(creator).appendTo(table);
                $('<li/>').append(table).appendTo(target);
                notifyLoadPerm(link, {
                    perm : permText,
                    descr : permDescrText,
                    creator : permCreatorText
                });
            };

            this.displayPerm = function(permObj, target) {
                var notSet = '-/-'
                var permId = permObj.rid !== undefined ? permObj.rid : notSet;
                var permDescr = permObj.description !== undefined ? permObj.description : notSet;
                var permCreator = permObj.creator !== undefined ? permObj.creator : notSet;

                target.perm.html(permId);
                target.descr.html(permDescr);
                target.creator.html(permCreator);
            }
        },

        _model : function(httpGET) {
            var dataPool = {};

            this.getDataFromUrl = function(url) {
                var data = {};
                var dataObj = $(dataPool).prop(url);

                if (dataObj === undefined) {
                    httpGET(url, {}, function(target, _data) {
                        data = _data;
                        $.extend(dataPool, data);
                    });
                } else {
                    $(data).prop(url, dataObj);
                }

                return data;
            };
        },

        loadAllObjID : function() {
            var objIDs = this.model.getDataFromUrl(this.options.baseURL);
            var view = this.view;

            $.each(objIDs, function(url, data) {
                $.each(data, function(index, obj) {
                    view.displayObjId(obj, url);
                });
            })
        },

        loadAllAccess : function(url, target) {
            var allAccess = this.model.getDataFromUrl(url);
            var view = this.view;

            $.each(allAccess, function(url, data) {
                $.each(data, function(index, obj) {
                    view.displayAccess(obj, target);
                })
            });
        },

        loadPerm : function(url, target) {
            var permObj = this.model.getDataFromUrl(url);
            var view = this.view;

            $.each(permObj, function(url, data) {
                $.each(data, function(index, obj) {
                    view.displayPerm(obj, target);
                })
            });
        },

        clickChangeRule : function(target) {
            var rules = this.model.getDataFromUrl(this.options.ruleURL);
            var permObj = this.model.getDataFromUrl(url);
            var view = this.view;

            $.each(permObj, function(url, data) {
                $.each(data, function(index, obj) {
                    view.displayPerm(obj, target);
                })
            });
        }
    });
})(jQuery);