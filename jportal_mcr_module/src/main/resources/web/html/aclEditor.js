(function($) {
    function AbstractView(/*{listeners : array}*/ spec) {
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

    function MainView(spec) {
        var that = AbstractView();

        var editorID = spec.mainTag.attr('id');
        var classListID = editorID + 'ClassList';
        var permColID = editorID + 'permCol';
        var dropDownID = editorID + 'ruleListDropdown';

        var classList = $('<div/>').attr({
            id : classListID
        }).appendTo(spec.mainTag);

        function display(data) {
            var objIdView = ObjIdView({
                parentTag : classList,
                listeners : that.getListeners(),
                id : data.id,
                url : data.link
            });
        }

        function init() {
            that.notify(spec.url);
        }

        that.display = display;
        that.init = init;
        return that;
    }

    function ObjIdView(spec) {
        var that = AbstractView({listeners: spec.listeners});

        var classListDiv = $('<div/>').appendTo(spec.parentTag);
        var h3 = $('<h3/>').html(spec.id).appendTo(classListDiv);
        var accessView = null;

        h3.click(function() {
            if (accessView == null) {
                accessView = AccessIdView({
                    parentTag : classListDiv,
                    listeners : that.getListeners(),
                });
                that.notify(spec.url);
            }
        });

        function display(data) {
            console.log('ObjIdView addData');
            accessView.addData({
                id : data.id,
                link : data.link
            });
        }

        /*-----------------------
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
        -----------------------*/

        that.display = display;
        return that;
    }

    function AccessIdView(spec) {
        var that = AbstractView({listeners: spec.listeners});
        var ul = $('<ul/>').appendTo(spec.parentTag);
        
        function addData(spec){
            console.log('AccessIdView addData');
            spec.parentTag = ul;
            AccessIdEntryView(spec);
        }
        
        that.addData = addData;
        return that;
    }
    
    function AccessIdEntryView(spec){
        var that = AbstractView({listeners: spec.listeners});
        
        console.log('AccessIdEntryView ' + spec.parentTag);
        var li = $('<li/>').appendTo(spec.parentTag);
        var table = $('<table/>').appendTo(li);
        var tr = $('<tr/>').appendTo(table);
        var id = $('<td class="id"/>').html(spec.id).appendTo(tr);
        
        return that;
    }

    function ViewController(view, model) {
        var ViewListener = function() {
            return {
                loadData : function(url) {
                    console.log('URL: ' + url);
                    var httpGET = model.getDataFromUrl(url);
                    var data = httpGET[url];
                    console.log('Data: ' + data.length);

                    for ( var i = 0; i < data.length; i++) {
                        view.display(data[i]);
                    }
                }
            }
        }

        view.addListener(ViewListener());
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
            var ControllReg = function(){
                return {
                    register : function(view){
                        ViewController(view, model);
                        view.addRegister(this);
                    }
                }
            }
            var controllReg = ControllReg();
            var mainGUI = MainView({
                mainTag : $(this.element[0]),
                url : this.options.baseURL
            });
            
            ViewController(mainGUI, model);
            //controllReg.register(mainGUI);
            mainGUI.init();

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