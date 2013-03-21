var jp = jp || {};

jp.subselect = {

	cache: [],
	
	previewSelector: null,

	init: function(/*String*/ hoverSelector, /*String*/ previewSelector) {
		jp.subselect.previewSelector = previewSelector;
		$(hoverSelector).each(function() {
			$(this).on("mouseenter", jp.subselect.togglePreview);
			$(this).on("mouseleave", jp.subselect.togglePreview);
		});
	},

	get: function(/*String*/ mcrid, /*function*/ onSuccess) {
		if(jp.subselect.cache[mcrid]) {
			onSuccess(jp.subselect.cache[mcrid]);
			return;
		}
		$.ajax({
			url: "/rsc/render/object/" + mcrid,
			dataType: "html"
		}).done(function(html) {
			jp.subselect.cache[mcrid] = html;
			onSuccess(html);
		});
	},

	togglePreview: function() {
		var mcrid = $(this).attr("data-jp-mcrid");;
		var div = $(jp.subselect.previewSelector);
		if(div.hasClass("hidden")) {
			div.fadeIn();
			div.toggleClass("hidden");
			var pos = $(this).position();
			div.css("top", pos.top + 30);
			div.css("left", pos.left - 8);
			jp.subselect.get(mcrid, function(html) {
				div.html(html);
			});
		} else {
			div.fadeOut('slow', function() {
				div.toggleClass("hidden");
			});
		}
	}

};