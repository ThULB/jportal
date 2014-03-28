var jp = jp || {};

jp.subselect = {

	cache: [],

	init: function(/*String*/ hoverSelector, /*String*/ previewSelector) {
		var div = $(previewSelector);
		$(hoverSelector).hover(function() {
			var mcrid = $(this).attr("data-jp-mcrid");
			var pos = $(this).position();
			div.css("top", pos.top + 30);
			div.css("left", pos.left - 8);
			jp.subselect.get(mcrid, function(html) {
				div.html(html);
			});
			div.removeClass("hidden");
			div.stop().animate({opacity: 1}, 500);
		},
		function () {
			div.stop().animate({opacity: 0}, 500, function() {
				div.css("opacity", "0");
				div.addClass("hidden");
			});
		});
	},

	get: function(/*String*/ mcrid, /*function*/ onSuccess) {
		if(jp.subselect.cache[mcrid]) {
			onSuccess(jp.subselect.cache[mcrid]);
			return;
		}
		$.ajax({
			url: jp.baseURL + "rsc/render/object/" + mcrid,
			dataType: "html"
		}).done(function(html) {
			jp.subselect.cache[mcrid] = html;
			onSuccess(html);
		});
	}

};