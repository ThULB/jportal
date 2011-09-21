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
    
    var RuleSelectBox = function(conf){
        var selectBox= AbstractView();
        var select = $('<select/>').appendTo(conf.parentTag);
        
        var Option = function(data){
            var val = data.descr === undefined ? data.rid : data.descr;
            var option = $('<option/>').html(val);
            
            return {
                appendTo : function(tag){
                    option.appendTo(tag);
                }
            }
        }
        
        selectBox.addOption = function(content){
            var option = Option(content);
            option.appendTo(select);
        };
        
        selectBox.create = function(){
            selectBox.notify({
                event : 'loadSelBox'
            })
        };
        
        selectBox.appendTo = function(tag){
            select.appendTo(tag);
        };
        
        
        return selectBox;
    }
    
    var RuleCell = function(conf){
        var ruleCell= AbstractView();
        var buttonSet = $('<span/>').appendTo(conf.parentTag);
        var ridButton = $('<span/>').html(conf.rid).appendTo(buttonSet);
        var changeButton = $('<span/>').html('change rule').button({
            text: false,
            icons: {
                primary: "ui-icon-triangle-1-s"
            }
        });
        
        function hoverOn(){
            ridButton.button();
            changeButton.appendTo(buttonSet);
            buttonSet.buttonset()
        }
        
        function hoverOff(){
            ridButton.button('destroy');
            changeButton.detach();
        }
        
        buttonSet.hover(hoverOn,hoverOff);
        
        return ruleCell;
    }
    
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
            var ruleCell = $('<td/>').appendTo(tr);
            
            RuleCell({
                rid : getRuleDescr(data.rid),
                parentTag : ruleCell
            });
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
        view.create();
        
        var ruleSelBox = RuleSelectBox({
            parentTag : document.body
        });
        var SelectBoxHandler = function(){
            return {
                loadSelBox : function(){
                    var rules = model.getRule();
                    $.each(rules, function(name, data){
                        ruleSelBox.addOption(data);
                    })
                }
            }
        }
        ruleSelBox.addHandlers(SelectBoxHandler());
        ruleSelBox.create();
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