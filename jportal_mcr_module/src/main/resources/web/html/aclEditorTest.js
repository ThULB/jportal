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
                    };
                    switch(url){
                    case 'http://localhost:8291/rsc/acl/rsc' :
                        data = { 'http://localhost:8291/rsc/acl/rsc' : [
                            {id: 'fsu.jportal.resources.FOOResource', link: url + '/FOOResource'},
                            {id: 'fsu.jportal.resources.ACLResource', link: url + '/ACLResource'},
                            {id: 'fsu.jportal.resources.JournalClassificationResource', link : url + '/JournalClassificationResource'}
                              ]};
                        break;
                    case 'http://localhost:8291/rsc/acl/rsc/FOOResource' : 
                        data = { 'http://localhost:8291/rsc/acl/rsc/FOOResource' : [
                        {id : '/acl/rsc_GET', link : 'http://System000001'}, 
                        {id : '/acl/rsc_POST', link : url + '/acl/rsc_POST'}
                        ]};
                        break;
                    case 'http://localhost:8291/rsc/acl/rsc/ACLResource' :
                        data = {'http://localhost:8291/rsc/acl/rsc/ACLResource' : [
                            {id: '/classifications/jp/{id}/save_POST', link : url + '/classifications/jp/{id}/save_POST'}
                            ]};
                        break;
                    case 'http://localhost:8291/rsc/acl/rsc/JournalClassificationResource' :
                        data = {'http://localhost:8291/rsc/acl/rsc/JournalClassificationResource' : [
                            {id: '/classifications/foo/save_POST',link : url + '/classifications/foo/save_POST'}
                            ]};
                        break;
                    case 'http://System000001' :
                        data = {'http://System000001' : [{
                            
                                rid: 'System000001',
                                creator: 'admin',
                                creationdate: 'heute',
                                rule: 'true',
                                description: 'fakerule',
                        }
                        ]};
                        break;
                    case 'http://localhost:8291/rsc/acl/rules' : 
                        data = {'http://localhost:8291/rsc/acl/rules' : [
                              {id: 'System000001', link : 'http://System000001'},
                              {id: 'System000002', link : 'http://System000002'},
                              {id: 'System000003', link : 'http://System000003'},
                              {id: 'System000004', link : 'http://System000004'},
                              ]}; 
                        break;
                    default:
                        console.log("default url: " + url);
                    $(data).prop(url,[{}]);
                        break;
                    };
                    callBack(target, data);
                }
        };
        
        var editorConfig1 = {
                baseURL : 'http://localhost:8291/rsc/acl/rsc'
        };
        
        $('#aclEditor').aclEditor(editorConfig);
        
 })