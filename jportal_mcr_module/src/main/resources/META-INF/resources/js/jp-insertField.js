$(function () {
	$("#main > .section").on("click", "button[name*='_xed_submit_insert']", function(event) {
        event.preventDefault();
        sendEditor($(this).attr("name"));
	});
	
	$("#main > .section").on("click", "button[name*='_xed_submit_remove']", function(event) {
        event.preventDefault();
        sendEditor($(this).attr("name"));
	});
	
	$("#main > .section").on("click", "button[name*='_xed_submit_up']", function(event) {
        event.preventDefault();
        sendEditor($(this).attr("name"));
	});
	
	$("#main > .section").on("click", "button[name*='_xed_submit_down']", function(event) {
        event.preventDefault();
        sendEditor($(this).attr("name"));
	});
	
	function sendEditor(name){
		$.ajax({
			url: jp.baseURL + "servlets/XEditor",
			type: "POST",
			dataType: "text",
			data: $("#main").find("form").first().serialize() + "&" + encodeURIComponent(name),
			success: function(data) {
						var html = $("<div></div>").append(data);
//						$(html).find("script").remove();
//						$(html).find("link").remove();
						$("#main").html($(html).find("#main").html());
						$("#main").trigger("changed");
					},
			error: function(error) {
						alert(error);
					}
		});
	}
});