/**
 * 
 */
$(document).ready(function() {
        console.log('Ready!');
        var editorConfig = {
                accessURL : 'http://localhost:8291/rsc/acl/mcraccess',
                rulesURL : 'http://localhost:8291/rsc/acl/mcraccessrule',
                httpGET: function(url, callBack){
                    var data = {
                            'http://localhost:8291/rsc/acl/mcraccess' : {
                                'url1' : {objid: 'fsu.jportal.resources.FOOResource', acpool : '/acl/rsc_GET', rid : 'System000001'},
                                'url2' : {objid: 'fsu.jportal.resources.FOOResource', acpool : '/acl/rsc_POST'},
                                'url3' : {objid: 'fsu.jportal.resources.ACLResource', acpool : '/classifications/jp/{id}/save_POST', rid : 'System000003'},
                                'url4' : {objid: 'fsu.jportal.resources.JournalClassificationResource', acpool : '/classifications/foo/save_POST'}
                            },
                            'http://localhost:8291/rsc/acl/mcraccessrule' : {
                                'http://localhost:8291/rsc/acl/mcraccessrule/System000001' : {rid: 'System000001', rule : 'true', descr : 'fakerule'},
                                'http://localhost:8291/rsc/acl/mcraccessrule/System000002' : {rid: 'System000002', rule : 'false', descr : 'dummy'},
                                'http://localhost:8291/rsc/acl/mcraccessrule/System000003' : {rid: 'System000003', rule : 'false or true'},
                            }
                    };
                    
                    var retrievedData = {};
                    if(data[url] !== undefined){
                        retrievedData = data[url];
                    }else{
                        console.log("default url: " + url);
                    }
                    callBack(retrievedData);
                },
                
                ajax : function(options){
                    console.log('-- ajax --');
                    $.each(options, function(name,data){
                        console.log(name + ': ' + data);
                    })
                }
        };
        
        var editorConfig1 = {
                accessURL : 'http://localhost:8291/rsc/acl/rsc',
                rulesURL : 'http://localhost:8291/rsc/acl/rules'
        }
        
        //$('#aclEditor').aclEditor(editorConfig);
        $('#rcsACLTable').aclTable(editorConfig);
 })