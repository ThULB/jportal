(function($) {
    var AbstractGUI = function(){
        var listeners = [];

        return {
            addListener : function(list) {
                listeners.push(list);
            },

            notify : function(msg) {
                for ( var i = 0; i < listeners.length; i++) {
                    listeners[i].loadData(msg);
                }
            },

            handleEvent : function(event) {
                for ( var i = 0; i < controllReg.length; i++) {
                    controllReg[i].handleEvent(event);
                }
            }
        }
        
    };

    var AccessList = function(spec){
        var accesList = $('<ul/>').appendTo(spec.parentTag);
        
        accesList.toggleView = function(){
            if(accesList.css('display') == 'none'){
                accesList.show();
            }else{
                accesList.hide();
            }
        };
        
        return accesList;
    };
    
    var RuleInfo = function(spec){
        var parentTag =  spec.ParentTag;
        
        
    };
    
    var ACLEditorGUI = function(spec){
        var that = AbstractGUI();
        var objIdList = $('<ul/>').appendTo(spec.parentTag);
        
        
        that.fillObjIdList = function(data){
            var li = $('<li/>').appendTo(objIdList);
            var h3 = $('<h3/>').append(data.id).appendTo(li);
            var accessList = null;
            
            h3.click(function(){
                if(accessList == null){
                    accessList = AccessList({parentTag: li});
                    that.notify({
                        link : data.link,
                        callback : 'fillAccessIdList',
                        parentTag : accessList
                    });
                }else {
                    accessList.toggleView();
                }
            })
        }
        
        that.fillAccessIdList = function(data){
            var access = $('<li/>').appendTo(data.parentTag);
            var accessView = $('<tr/>').appendTo($('<table/>').appendTo(access));
            var accessId = $('<td/>').append(data.id).appendTo(accessView);
            
            that.notify({
                link : data.link,
                callback : 'fillRuleInfo',
                parentTag : accessView
            });
        }
        
        that.fillRuleInfo = function(data){
            var ruleInfo = RuleInfo(data);
        }
        
        that.init = function(){
            that.notify({
                link : spec.link,
                callback : 'fillObjIdList'
            });
        }
        
        return that;
    };
    
    var GUIController = function(model){
        var views = [];
        return {
            register : function(view) {
                var viewListener = {
                    loadData : function(msg) {
                        var httpGET = model.getDataFromUrl(msg.link);
                        var data = httpGET[msg.link];

                        for ( var i = 0; i < data.length; i++) {
                            var callbackData = data[i];
                            callbackData.parentTag = msg.parentTag;
                            view[msg.callback](callbackData);
                        }
                    }
                };

                view.addListener(viewListener);
                views.push(view);

                if (typeof view.init == 'function') {
                    view.init();
                }
            },
            
            handleEvent : function(eventName){
                for ( var i = 0; i < views.length; i++) {
                    if(typeof views[i][eventName] == 'function'){
                        views[i][eventName].apply(views[i]);
                    }
                }
            }
        }
    };
    
    // ###############################################################
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
            },
            
            handleEvent : function(event) {
                for ( var i = 0; i < controllReg.length; i++) {
                    controllReg[i].handleEvent(event);
                }
            }
        }
    }
    
    function MainGUI(spec){
        var that = AbstractView();
        var objIDs = ObjIDList({parentTag : spec.parentTag});
        
        spec.parentTag.selectable({
            filter : 'li.selFilter'
        });
        
        that.init = function(){
            that.register(objIDs);
            objIDs.notify(spec.link);
        };
        
        that.eventSelectableOff = function(){
            spec.parentTag.selectable('option', 'distance', 20);
        };
        
        that.eventSelectableOn = function(){
            spec.parentTag.selectable('option', 'distance', 0);
        };
        
        return that;
    }
    
    function ObjIDList(spec){
        var that = UlListGUI({parentTag : spec.parentTag});
        
        that.addItem = function(item){
            var h3 = $('<h3/>').appendTo(item.parentTag);
            var accessIDs = null;
            
            h3.mouseover(function(){
                that.handleEvent('eventSelectableOff');
            }).mouseleave(function(){
                that.handleEvent('eventSelectableOn');
            });
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
        var that = UlListGUI({
            parentTag : spec.parentTag,
            liClass : 'selFilter'
            });
        
        function Button(spec){
            var button = $('<span/>').addClass(spec.icon).click(function() {
                console.log('button click ' + spec.icon)
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
                    that.handleEvent('eventSelectableOff');
                    colorAndToggle({color : '#cccccc', visibility: 'visible'})
                }).mouseleave(function(){
                    that.handleEvent('eventSelectableOn');
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
            
            if(spec.liClass !== undefined){
                li.addClass(spec.liClass);
            }
            
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
        var views = [];
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
                views.push(view);

                if (typeof view.init == 'function') {
                    view.init();
                }
            },
            
            handleEvent : function(eventName){
                for ( var i = 0; i < views.length; i++) {
                    if(typeof views[i][eventName] == 'function'){
                        views[i][eventName].apply(views[i]);
                    }
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
            var guiController = GUIController(model);
            var mainGUI = ACLEditorGUI({
                parentTag : $(this.element[0]),
                link : this.options.baseURL
            });

            guiController.register(mainGUI);
            /*
            var guiController = ViewController(model);
            var mainGUI = MainGUI({
                parentTag : $(this.element[0]),
                link : this.options.baseURL
            });

            guiController.register(mainGUI);
            */
        }
    });
})(jQuery);