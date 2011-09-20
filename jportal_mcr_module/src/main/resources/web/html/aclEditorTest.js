/**
 * 
 */
$(document).ready(function() {
        console.log('Ready!');
        var editorConfig = {
                baseURL : 'http://localhost:8291/rsc/acl/rsc',
                rulesURL : 'http://localhost:8291/rsc/acl/rules',
                rulesListURL: 'http://localhost:8291/rsc/acl/rules',
                httpGET: function(url, target, callBack){
                    var data = {
                            'http://localhost:8291/rsc/acl/rsc' : [
                                {objid: 'fsu.jportal.resources.FOOResource', link: url + '/FOOResource'},
                                {objid: 'fsu.jportal.resources.ACLResource', link: url + '/ACLResource'},
                                {objid: 'fsu.jportal.resources.JournalClassificationResource', link : url + '/JournalClassificationResource'}
                                ],
                            'http://localhost:8291/rsc/acl/rsc/FOOResource' : [
                                {id : '/acl/rsc_GET', link : 'http://System000001'}, 
                                {id : '/acl/rsc_POST', link : url + '/acl/rsc_POST'}
                                ],
                            'http://localhost:8291/rsc/acl/rsc/ACLResource' : [
                                {id: '/classifications/jp/{id}/save_POST', link : url + '/classifications/jp/{id}/save_POST'}
                                ],
                            'http://localhost:8291/rsc/acl/rsc/JournalClassificationResource' : [
                                {id: '/classifications/foo/save_POST',link : url + '/classifications/foo/save_POST'}
                                ],
                            'http://System000001' : [{
                                rid: 'System000001',
                                creator: 'admin',
                                creationdate: 'heute',
                                rule: 'true',
                                description: 'fakerule',
                                }],
                            'http://System000002' : [{
                                rid: 'System000002',
                                creator: 'chi',
                                creationdate: 'morgen',
                                rule: 'false',
                                description: 'dummy',
                                }],
                            'http://localhost:8291/rsc/acl/rules' : [
                              {id: 'System000001', link : 'http://System000001'},
                              {id: 'System000002', link : 'http://System000002'},
                              {id: 'System000003', link : 'http://System000003'},
                              {id: 'System000004', link : 'http://System000004'},
                              ] 
                    };
                    
                    var retrievedData = {};
                    if(data[url] !== undefined){
                        retrievedData[url] = data[url];
                    }else{
                        retrievedData[url] = [{}];
                        console.log("default url: " + url);
                    }
                    callBack(target, retrievedData);
                }
        };
        
        var editorConfig1 = {
                baseURL : 'http://localhost:8291/rsc/acl/rsc'
        };
        
        var obj = function(){
            return {
                print : function(){
                    console.log('hello');
                }
            }
        }();
         var extObj = function(){
             var parent = obj
             var superPrint = obj.print;
             parent.print = function(){
                 superPrint();
                 
             }
             obj['print']()
             return parent;
         }
        $('#aclEditor').aclEditor(editorConfig);
        
 })