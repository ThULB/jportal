$(document).ready(function() {
    $.get("/rsc/search?q=%2BdoubletOf:* %2BobjectType:person",function(data){
    	$("#numDubPers").text(data.response.numFound);
    },"json")
    $.get("/rsc/search?q=%2BdoubletOf:* %2BobjectType:jpinst",function(data){
    	$("#numDubInst").text(data.response.numFound);
    },"json")
    
    $("#delDubButton").click(function(){
    	$.ajax({
    		url: "/rsc/doublets/person",
    		type: "DELETE",
    		success: function(){
    			console.log("del dub type Pers");
    		}
    	})
    })
});