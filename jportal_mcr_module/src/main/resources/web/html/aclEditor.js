(function($) {
    var AbstractView = function(){
        var handlers = [];
        
        return {
            addHandlers : function(list){
                handlers.push(list);
            },
            
            notify : function(msg){
                for ( var i = 0; i < handlers.length; i++) {
                    if(typeof handlers[i][msg.event] == 'function'){
                        handlers[i][msg.event](msg.args);
                    }
                }
            }
        }
    };
    
    var MainView = function(conf){
        var mainView = AbstractView();
        var table = $('<table/>').appendTo(conf.parentTag);
        var head = $('<tr/>').append('<th>objId</th><th>acpool</th><th>rule</th>').appendTo(table);
        function getRuleDescr(rid){
            var rule = '--/--';
            if(rid !== undefined){
                rule = rid;
                mainView.notify({
                    event : 'loadRule',
                    args : {
                        rid : rid,
                        callback : function(data){
                            if(data.descr !== undefined){
                                rule = data.descr;
                            }
                        }
                    }
                });
            }
            
            return rule;
        }
        
        mainView.create = function(){
            mainView.notify({
                event : 'loadAccess'
            });
        };
        
        mainView.addAccess = function(url, data){
            var tr = $('<tr/>').appendTo(table);
            $('<td/>').append(data.objid).appendTo(tr);
            $('<td/>').append(data.acpool).appendTo(tr);
            $('<td/>').append(getRuleDescr(data.rid)).appendTo(tr);
        };
        return mainView;
    }
    
    var Controller = function(view, model){
        var MainViewHandler = function(){
            return {
                loadAccess : function(){
                    var access = model.getAccess();
                    $.each(access, function(url, data){
                        view.addAccess(url, data);
                    });
                },
                loadRule : function(args){
                    var rule = model.getRule(args.rid);
                    args.callback(rule);
                }
            }
        }
        view.addHandlers(MainViewHandler());
        view.create()
    };
    
    var Model = function(conf) {
        var access = {};
        var rules = {};
        
        conf.httpGET(conf.accessURL, function(data){
            access = data;
        });
        
        conf.httpGET(conf.rulesURL, function(data){
            rules = data;
        });
        return {
            getAccess : function(url) {
                if(url === undefined){
                    return access;
                }else{
                    return access[url];
                }
            },
            
            getRule : function(rid) {
                if(rid === undefined){
                    return rules;
                }else{
                    var url = conf.rulesURL + '/' + rid;
                    return rules[url];
                }
            },
        }
    }

    $.widget('fsu.aclEditor', {
        options : {
            baseURL : '',
            httpGET : function(url, target, callBack) {
            }
        },

        _create : function() {
            var model = Model(this.options);
            var view = MainView({
                parentTag : this.element[0]
            });
            Controller(view,model);
        }
    });
})(jQuery);