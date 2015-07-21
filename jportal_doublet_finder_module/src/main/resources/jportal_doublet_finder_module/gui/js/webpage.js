$(document).ready(function() {
	var DubCheckGUI = $("#jportal_doublet_finder_module").on({
		init : function(){
			var dubCheckGUI = $(this);
			
			$("span.numDub").on("initGUIElem", function(){
				var numDubDispl = $(this);
				var type = numDubDispl.attr("type");
				$.get(jp.baseURL + "servlets/solr/select?wt=json&q=%2BdoubletOf:* %2BobjectType:"+type,function(data){
					var numDoublets = data.response.numFound;
					numDubDispl.text(numDoublets);
					if(numDoublets == 0){
						$("#"+type+"_doublets a.doubletsLink").hide();
					}else {
						$("#"+type+"_doublets a.doubletsLink").show();
						$("#delDubButton").removeAttr("disabled");
					}
			    },"json")
			});
			
			$("span.numGND").on("initGUIElem", function(){
				var numDubDispl = $(this);
				var type = numDubDispl.attr("type");
				$.get(jp.baseURL + "servlets/solr/select?wt=json&q=-id.gnd:* %2BobjectType:"+type,function(data){
					var numDoublets = data.response.numFound;
					numDubDispl.text(numDoublets);
					if(numDoublets == 0){
						$("#"+type+"_gnd a.gndLink").hide();
					}else {
						$("#"+type+"_gnd a.gndLink").show();
					}
				},"json")
			});
			
			$("span.message").on("toggleMsg", function(){
				$(this).toggle();
			});
			
			$("#delDubButton").on({
				click : function(){
					$(this).trigger("removeDub");
				},
				
				removeDub : function(){
					$.each(["person", "jpinst"], function(index, type){
						$.ajax({
							method: "DELETE",
							url: jp.baseURL + 'rsc/doublets/' + type,
						}).success(function(data){
							if(!dubCheckGUI.data("statusTriggered")){
								dubCheckGUI.trigger("status");
								dubCheckGUI.data("statusTriggered", true);
							}
					    }).error(function(err){
							if (err.status == "401") {
								$(location).attr('href', jp.baseURL + "servlets/MCRLoginServlet?action=login&url=" + jp.baseURL + "rsc%2Fdoublets");
							}
					        console.log('removeDuplicate failed with Error Code ' + err.status);
					    });
					})
				},
				
				initGUIElem : function(){
					$(this).attr("disabled","disabled");
				}
			});
			
			$("#delDubButton").trigger("initGUIElem");
			$("span.numDub").trigger("initGUIElem");
			$("span.numGND").trigger("initGUIElem");
		},

		status : function(){
			var dubCheckGUI = $(this);
			var processRunning = true;
			$.ajax({ 
		        type :  'GET',
		        url: jp.baseURL + "servlets/solr/select?wt=json&q=%2BdoubletOf:*",
				dataType: "json",
		        success: function(data){
					if (data.response.numFound < 1) {
						processRunning = false;
					}
		        }, 
		        complete: function(){
		        	if(processRunning == true){
		        		$("#progressMsg").show();
						setTimeout(function() {
							dubCheckGUI.trigger("status");
						}, 1000);
		        	}else {
		        		$("#progressMsg").hide();
		        		dubCheckGUI.trigger("init");
		        	}
		        }, 
		        timeout: 2000
		    });
		}
	});
	
	DubCheckGUI.trigger("init");
});